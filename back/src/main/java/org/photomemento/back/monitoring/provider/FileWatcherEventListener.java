package org.photomemento.back.monitoring.provider;

import lombok.extern.slf4j.Slf4j;
import org.photomemento.back.exceptions.PhotoMementoError;
import org.photomemento.back.types.enu.FILE_TYPE;

import java.io.File;

/**
 * Abstract class that will be inherited in charge of processing file/dir events (Create, delete, etc..) when
 * the file-watcher detects an event int the watched folders.
 *
 * @see org/photomemento/back/monitoring/service/FileWatcherService.java
 */
@Slf4j
public abstract class FileWatcherEventListener {
    public static final String WATCH_EVENT_FIRED = "Watch event: [%s] %s";
    public static final String ERROR_WATCH_EVENT = "Error parsing event: [%s] %s: due to %s";

    private final FileTypeProvider fileTypeProvider;

    protected FileWatcherEventListener(FileTypeProvider fileTypeProvider){
        this.fileTypeProvider = fileTypeProvider;
    }

    public void onModified(File f, FILE_TYPE type) {
    }

    public void onCreated(File f, FILE_TYPE type) {
    }

    public void onDeleted(File f, FILE_TYPE type) {
    }

    public void onVisit(File f, boolean reprocess, FILE_TYPE type) {
    }

    public final void onEventFired(FileWatcherProvider.FW_EVENT event, File f, FILE_TYPE applyOnlyForFileType) {
        try {
            FILE_TYPE type = getTypeOfFile(f);
            if(applyOnlyForFileType!=null && type != FILE_TYPE.DIRECTORY && applyOnlyForFileType!=type) return;

            log.debug(String.format(WATCH_EVENT_FIRED, event.name(), f.getAbsolutePath()));
            switch (event) {
                case CREATE:
                    onCreated(f, type);
                    break;
                case MODIFY:
                    onModified(f, type);
                    break;
                case DELETE:
                    onDeleted(f, type);
                    break;
                case VISIT:
                case VISIT_REPROCESS:
                    onVisit(f, event == FileWatcherProvider.FW_EVENT.VISIT_REPROCESS, type);
                    break;
            }

            onEvent(event, f, type);
        } catch (PhotoMementoError e) {
            log.error(String.format(ERROR_WATCH_EVENT, event.name(), f.getAbsolutePath(), e.getMessage()), e);
        }
    }

    public void onEvent(FileWatcherProvider.FW_EVENT event, File f, FILE_TYPE fileType) {
    }



    public FILE_TYPE getTypeOfFile(File f) {
        return fileTypeProvider.getType(f);
    }
}
