package org.photomemento.back.monitoring.provider;

import lombok.extern.slf4j.Slf4j;
import org.photomemento.back.exceptions.InvalidConfigurationError;
import org.photomemento.back.exceptions.InvalidStateError;
import org.photomemento.back.types.enu.FILE_TYPE;
import org.photomemento.back.util.StrUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * This class handles monitoring of files/directories from a path directory
 */
@Slf4j
@Component
public class FileWatcherProvider implements Runnable {

    @Value("${file.monitoring.watchdir}")
    public String watchDir;
    @Value("${file.monitoring.startup.visit.files}")
    public boolean startupVisitFiles;
    @Value("${file.monitoring.startup.reprocess.files}")
    public boolean startupReprocessFiles;
    @Value("${file.monitoring.files.exclusions}")
    public String fileExclusionsProp;


    public final ConcurrentHashMap<String, WatchKey> watchKeys = new ConcurrentHashMap<>(); //NOSONAR
    private FileWatcherEventListener fileWatcherEventListener;
    private boolean watching;
    private WatchService internalWatchService;
    private File watchDirRoot;
    private Set<Pattern> fileExclusions;

    public FileWatcherProvider init(FileWatcherEventListener fileWatcherEventListener) {
        if (!StringUtils.hasText(watchDir))
            throw new InvalidConfigurationError("Expected a valid directory provided null/empty");
        watchDirRoot = new File(watchDir);
        if (!watchDirRoot.exists())
            throw new InvalidConfigurationError(String.format("Expected a valid directory to be watched, provided is not found: %s", watchDir));
        if (!watchDirRoot.isDirectory())
            throw new InvalidConfigurationError(String.format("Expected a valid directory to be watched, provided is not a directory: %s", watchDir));

        this.fileWatcherEventListener = fileWatcherEventListener;

        processFileExclusions(this.fileExclusionsProp);

        configureWatcher();

        return this;
    }

    private void processFileExclusions(String fileExclusionsProp) {
        try {
            fileExclusions = StrUtils.strListToUniquePatternList(fileExclusionsProp);
        } catch (Exception e) {
            log.error(String.format("There was an error while parsing file \"file.monitoring.files.exclusions\", with value: %s, due to: %s", fileExclusionsProp, e.getMessage()));
        }
    }

    private void configureWatcher() {
        try {
            internalWatchService = FileSystems.getDefault().newWatchService();
            onStartCheckFiles();
        } catch (IOException | ClosedWatchServiceException e) {
            tryCloseWatchService();
            throw new InvalidConfigurationError(String.format("There was an error configuring file watcher: %s", e.getMessage()), e);
        }
    }

    /**
     * Register the given directory and all its sub-directories with the WatchService.
     */
    private void onStartCheckFiles() {
        checkFiles(Paths.get(watchDirRoot.getAbsolutePath()), this.startupVisitFiles, this.startupReprocessFiles, null);
    }

    /**
     * Register the given directory and all its sub-directories with the WatchService.
     */
    public void checkFiles(final boolean revisitFiles, final boolean reprocess, final FILE_TYPE applyOnlyForFileType) {
        checkFiles(Paths.get(watchDirRoot.getAbsolutePath()), revisitFiles, reprocess, applyOnlyForFileType);
    }

    /**
     * Register the given directory and all its sub-directories with the WatchService.
     */
    public void checkFiles(final Path start, final boolean revisitFiles, final boolean reprocess, final FILE_TYPE applyOnlyForFileType) {
        new Thread(() -> {
            try {
                Files.walkFileTree(start, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        if (isExcluded(dir))
                            return FileVisitResult.SKIP_SUBTREE;
                        if(applyOnlyForFileType!=null && applyOnlyForFileType!=FILE_TYPE.DIRECTORY)
                            return FileVisitResult.CONTINUE;
                        registerPath(dir);
                        return onVisit(dir);
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (isExcluded(file)) return FileVisitResult.SKIP_SUBTREE;
                        return onVisit(file);
                    }

                    private FileVisitResult onVisit(Path fileOrDir) throws IOException {
                        if (revisitFiles) notifyEvent(
                                reprocess ?
                                        FW_EVENT.VISIT_REPROCESS :
                                        FW_EVENT.VISIT,
                                fileOrDir.toFile(), applyOnlyForFileType);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                log.error(String.format("There was a problem while trying to check files: %s, due to: %s", start.toString(), e.getMessage()), e);
            }
        }).start();
    }

    private boolean isExcluded(Path file) {
        return fileExclusions.stream().anyMatch(pat -> {
            if (pat.matcher(file.getFileName().toString()).matches()) {
                log.debug(String.format("Event fired on excluded file (ignoring): %s (pattern: %s)", file.getFileName().toString(), pat.pattern()));
                return true;
            }
            return false;
        });
    }

    private void registerPath(final Path dir) throws IOException {
        WatchKey watchKey = dir.register(internalWatchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        synchronized (watchKeys) {
            watchKeys.put(dir.toString(), watchKey);
        }
    }

    public void watch() {
        synchronized (this) {
            if (watching) throw new InvalidStateError("Cannot start watching twice");
            log.info(String.format("Watcher started on directory: %s", watchDirRoot.getAbsolutePath()));
            Thread thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();
            watching = true;
        }
    }

    @Override
    public void run() {
        try {
            //noinspection InfiniteLoopStatement
            while (true) pollEvents(); //NOSONAR
        } catch (InterruptedException | ClosedWatchServiceException e) {
            tryCloseWatchService();
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            log.error(String.format("Exception while registering directory, due to: %s", e.getMessage()), e);
        }
    }

    protected void pollEvents() throws InterruptedException, IOException {
        WatchKey key = internalWatchService.take();
        Path parentPath = (Path) key.watchable();
        for (WatchEvent<?> event : key.pollEvents()) {
            try {
                Path path = parentPath.resolve((Path) event.context());
                if (isExcluded(path)) continue;
                notifyEvent(FW_EVENT.fromWatcherEvent(event.kind()), path.toFile(), null);
            } catch (NullPointerException e) {
                //This is a strange situation so just go next
            }
        }
        key.reset();
    }

    private void tryCloseWatchService() {
        try {
            if (internalWatchService != null)
                internalWatchService.close();
        } catch (Exception e) {
            //Not important
        }
    }

    /**
     * Handles file watcher events (add new created folder, remove old ones, etc..)
     * Then calls onEventFired with the event kind and the file (which handles next step depending of the type)
     */
    private void notifyEvent(FW_EVENT event, File f, FILE_TYPE applyOnlyForFileType) throws IOException {
        if (event == FW_EVENT.CREATE && f.isDirectory())
            registerPath(Paths.get(f.getAbsolutePath()));
        else if (event == FW_EVENT.MODIFY && f.isDirectory())
            return;
        else if (event == FW_EVENT.DELETE && f.isDirectory()) {
            synchronized (watchKeys) {
                WatchKey watchKey = watchKeys.get(f.getAbsolutePath());
                if (watchKey != null) {
                    try {
                        watchKey.cancel();
                    } catch (Exception e) {
                        //Dont mind
                    }
                    watchKeys.remove(f.getAbsolutePath());
                }
            }
        }
        fileWatcherEventListener.onEventFired(event, f, applyOnlyForFileType);
    }

    public enum FW_EVENT {
        CREATE,
        MODIFY,
        DELETE,
        VISIT,
        VISIT_REPROCESS;

        public static FW_EVENT fromWatcherEvent(WatchEvent.Kind<?> event) {
            if (event == ENTRY_CREATE) return CREATE;
            else if (event == ENTRY_MODIFY) return MODIFY;
            else if (event == ENTRY_DELETE) return DELETE;
            return null;
        }
    }

    public String getRelativePath(String absolutePath) {
        return Optional.ofNullable(absolutePath)
                .filter(abs -> abs.contains(watchDir))
                .map(abs -> abs.substring(abs.indexOf(watchDir)))
                .orElse(absolutePath);
    }
}