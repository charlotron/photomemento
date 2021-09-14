package org.photomemento.back.monitoring.service;

import org.photomemento.back.monitoring.provider.FileTypeProvider;
import org.photomemento.back.monitoring.provider.FileWatcherEventListener;
import org.photomemento.back.monitoring.provider.FileWatcherProvider;
import org.photomemento.back.monitoring.service.typeprocessors.*;
import org.photomemento.back.types.OnEventProcess;
import org.photomemento.back.types.ThreadWorker;
import org.photomemento.back.types.enu.FILE_TYPE;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Map;

/**
 * This class handles the filewatcher and processes final event.
 * When processing calls to different media processors depending of the file type
 *
 * @see org/photomemento/back/types/FILE_TYPE.java - the file types
 * @see org/photomemento/back/monitoring/service/typeprocessors/FWMediaProcessor.java - the media processor
 */
@Service
public class FileWatcherService extends FileWatcherEventListener implements ProcessorQueueService.ProcessorQueueServiceProcessor<OnEventProcess> {

    @Value("${queue.file-watcher.workers.num}")
    private int queueFileWatcherWorkersNum;
    @Value("${queue.file-watcher.workers.delay}")
    private int queueFileWatcherWorkersDelay;

    private final Map<FILE_TYPE, IFileWatcherProcessor> typeProcessors;
    private final ProcessorQueueService processorQueueService;
    private ThreadWorker<OnEventProcess> threadWorker;

    public FileWatcherService(
            FileWatcherProvider fileWatcher,
            FileTypeProvider fileTypeProvider,
            FWDirectoryProcessor fwDirectoryProcessor,
            FWPhotoProcessor fwPhotoProcessor,
            FWVideoProcessor fwVideoProcessor,
            ProcessorQueueService processorQueueService) {
        super(fileTypeProvider);

        this.processorQueueService = processorQueueService;
        this.typeProcessors = Map.of(
                FILE_TYPE.DIRECTORY, fwDirectoryProcessor,
                FILE_TYPE.PHOTO, fwPhotoProcessor,
                FILE_TYPE.VIDEO, fwVideoProcessor
        );

        fileWatcher
                .init(this)
                .watch();
    }

    @PostConstruct
    public void init() {
        threadWorker = processorQueueService.newQueueForClassWithWorkers(this.getClass().getSimpleName(), queueFileWatcherWorkersNum, queueFileWatcherWorkersDelay, this, true);
    }

    @Override
    public void onCreated(File f, FILE_TYPE type) {
        if (!f.exists()) return; //Nothing to be done
        processFileEvent(
                f,
                type,
                OnEventProcess.EVENT_TYPE.CREATE,
                processor -> processor.onCreated(f, type));
    }

    @Override
    public void onVisit(File f, boolean reprocess, FILE_TYPE type) {
        if (!f.exists()) return; //Nothing to be done
        processFileEvent(
                f,
                type,
                reprocess ?
                        OnEventProcess.EVENT_TYPE.VISIT_REPROCESS :
                        OnEventProcess.EVENT_TYPE.VISIT,
                processor -> processor.onVisit(f, type,reprocess));
    }

    @Override
    public void onModified(File f, FILE_TYPE type) {
        if (!f.exists() || type == FILE_TYPE.DIRECTORY) return; //Nothing to be done
        processFileEvent(
                f,
                type,
                OnEventProcess.EVENT_TYPE.MODIFY,
                processor -> processor.onModified(f, type));
    }

    @Override
    public void onDeleted(File f, FILE_TYPE type) {
        launchEventLater(f, type, null, OnEventProcess.EVENT_TYPE.DELETE);
    }

    private void processFileEvent(File f, FILE_TYPE type, OnEventProcess.EVENT_TYPE eventType, ProcessorMethodCall processorMethodCall) {
        IFileWatcherProcessor processor = getProcessor(type);
        if (processor == null) return;
        if (processor instanceof FWParentProcessor)
            launchEventLater(f, type, processor, eventType);
        else
            processorMethodCall.call(processor);
    }

    @FunctionalInterface
    private interface ProcessorMethodCall {
        void call(IFileWatcherProcessor processor);
    }

    public IFileWatcherProcessor getProcessor(FILE_TYPE fileType) {
        if (fileType == null) return null;
        return typeProcessors.get(fileType);
    }

    @Override
    public boolean processQueueItem(OnEventProcess eventProcess) {
        //Prevents firing event while copying is in progress
        if (eventProcess.getEventType() == OnEventProcess.EVENT_TYPE.CREATE || eventProcess.getEventType() == OnEventProcess.EVENT_TYPE.MODIFY) {
            final long currentSize = eventProcess.getFile().length();
            if (currentSize != eventProcess.getPreviousSize()) {
                launchEventLater(eventProcess);
                return true;
            }
        }

        switch (eventProcess.getEventType()) {
            case CREATE:
                eventProcess.getProcessor().onCreated(eventProcess.getFile(), eventProcess.getType());
                break;
            case MODIFY:
                eventProcess.getProcessor().onModified(eventProcess.getFile(), eventProcess.getType());
                break;
            case DELETE:
                typeProcessors.values().forEach(processor -> processor.onDeleted(eventProcess.getFile(), eventProcess.getType()));
                break;
            case VISIT:
            case VISIT_REPROCESS:
                eventProcess.getProcessor().onVisit(eventProcess.getFile(), eventProcess.getType(), eventProcess.getEventType() == OnEventProcess.EVENT_TYPE.VISIT_REPROCESS);
                break;
        }
        return true;
    }

    private void launchEventLater(File f, FILE_TYPE type, IFileWatcherProcessor processor, OnEventProcess.EVENT_TYPE eventType) {
        launchEventLater(OnEventProcess
                .builder()
                .file(f)
                .previousSize(f.length())
                .eventType(eventType)
                .processor(processor)
                .type(type)
                .build());
    }

    private void launchEventLater(OnEventProcess eventProcess) {
        threadWorker.offer(eventProcess);
    }
}
