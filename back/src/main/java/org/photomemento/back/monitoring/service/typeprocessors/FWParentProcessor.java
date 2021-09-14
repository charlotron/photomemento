package org.photomemento.back.monitoring.service.typeprocessors;

import lombok.extern.slf4j.Slf4j;
import org.photomemento.back.domain.entity.file.Media;
import org.photomemento.back.monitoring.service.ProcessorQueueService;
import org.photomemento.back.monitoring.service.ThreadLockService;
import org.photomemento.back.monitoring.service.typeprocessors.media.DateDataProcessor;
import org.photomemento.back.monitoring.service.typeprocessors.media.GeoDataProcessor;
import org.photomemento.back.monitoring.service.typeprocessors.media.MetaExtractorProcessor;
import org.photomemento.back.monitoring.service.typeprocessors.media.MultimediaDataProcessor;
import org.photomemento.back.repository.media.MediaRepository;
import org.photomemento.back.types.ThreadWorker;
import org.photomemento.back.types.enu.FILE_TYPE;
import org.photomemento.back.types.enu.STATUS_STEP;
import org.photomemento.back.util.IdUtils;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Optional;

@Slf4j
public abstract class FWParentProcessor<T extends Media, Z extends Media.MediaBuilder<T, ?>> implements IFileWatcherProcessor, ProcessorQueueService.ProcessorQueueServiceProcessor<String> {

    @Value("${queue.media.workers.num}")
    private int queueMediaWorkersNum;
    @Value("${queue.media.workers.delay}")
    private int queueMediaWorkersDelay;
    @Value("${queue.media.workers.visit.max.processing.time.secs}")
    private int visitMaxProcessingTimeSecs;
    @Value("${queue.media.workers.delete.max.processing.time.secs}")
    private int deleteMaxProcessingTimeSecs;
    @Value("${queue.media.workers.process.max.processing.time.secs}")
    private int processingMaxProcessingTimeSecs;

    protected final MetaExtractorProcessor metaExtractorMediaProcessor;
    protected final DateDataProcessor dateDataMediaProcessor;
    protected final MultimediaDataProcessor multimediaDataProcessor;
    protected final ThreadLockService mediaLockService;
    protected final MediaRepository mediaRepository;
    protected final ProcessorQueueService processorQueueService;
    protected final Class<T> forClass;
    protected final GeoDataProcessor geoDataProcessor;

    private ThreadWorker<String> threadWorker;

    protected FWParentProcessor( //NOSONAR
                                 MetaExtractorProcessor metaExtractorMediaProcessor,
                                 DateDataProcessor dateDataMediaProcessor,
                                 MultimediaDataProcessor multimediaDataProcessor,
                                 ThreadLockService mediaLockService,
                                 MediaRepository mediaRepository,
                                 ProcessorQueueService processorQueueService,
                                 GeoDataProcessor geoMediaProcessor,
                                 Class<T> forClass) {
        this.metaExtractorMediaProcessor = metaExtractorMediaProcessor;
        this.dateDataMediaProcessor = dateDataMediaProcessor;
        this.multimediaDataProcessor = multimediaDataProcessor;
        this.mediaLockService = mediaLockService;
        this.mediaRepository = mediaRepository;
        this.processorQueueService = processorQueueService;
        this.geoDataProcessor = geoMediaProcessor;
        this.forClass = forClass;
    }

    @PostConstruct
    public void init() {
        threadWorker = processorQueueService.newQueueForClassWithWorkers(this.getClass().getSimpleName(), queueMediaWorkersNum, queueMediaWorkersDelay, this, true);
    }


    protected void processLater(T media) {
        threadWorker.offer(media.getPath());
    }

    @Override
    public boolean processQueueItem(String mediaPath) {
        final String mediaId = IdUtils.getIdFromAbsPath(mediaPath);
        return mediaLockService.lockRefreshFromDbRun(
                mediaId,
                forClass,
                (media -> {
                    if (!new File(media.getPath()).exists()) return false;

                    boolean isError = false;
                    try {
                        //Extracts all metadata from media
                        metaExtractorMediaProcessor.extractMetadata(media);
                        //Resolve the main date for this media
                        dateDataMediaProcessor.parseMainDate(media);
                        //Parse and fix orientation and image related data
                        multimediaDataProcessor.parseThumbData(media);
                        //Parse geo data from metadata and resolve against OSM
                        geoDataProcessor.parseMetadataGeoInfo(media);

                        //Let children to process what they want before save
                        processBeforeSave(media);

                        //Set status
                        media.setStatusValue(this.getClass().getSimpleName(), STATUS_STEP.OK);
                    } catch (Exception | Error e) { //NOSONAR
                        log.error(String.format("There was a problem processing media: %s, error: %s", media.getPath(), e.getMessage()), e);
                        media.setStatusValue(this.getClass().getSimpleName(), STATUS_STEP.FAIL);
                        isError = true;
                    }

                    //Save if no exception is thrown
                    mediaRepository.save(media);

                    //Error happened so stop!
                    if (isError) return false;

                    //Let children chain next actions
                    processAfterSave(media);
                    return true;
                }), processingMaxProcessingTimeSecs);
    }


    protected void processBeforeSave(T media) {
    }

    protected void processAfterSave(T media) {
    }

    @Override
    public void onModified(File file, FILE_TYPE type) {
        onCreated(file, type);
    }

    @Override
    public void onCreated(File f, FILE_TYPE type) {
        processMedia(f, type);
    }

    @Override
    public void onDeleted(File file, FILE_TYPE type) {
        String id = IdUtils.getIdFromFile(file);
        mediaLockService.lockRefreshFromDbRun(
                id,
                Media.class,
                media -> {
                    mediaRepository.deleteById(id);
                    multimediaDataProcessor.removeCachedMultimedia(media);
                    return true;
                }, deleteMaxProcessingTimeSecs);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onVisit(File f, FILE_TYPE type, final boolean reprocess) {
        String calculatedId = IdUtils.getIdFromAbsPath(f.getAbsolutePath());
        mediaLockService.threadSafeProcessById(calculatedId, id -> {
            if (reprocess)
                //Has to reprocess? do it!
                processMedia(f, type);
            else {
                Optional<Media> optMedia = mediaRepository.findById(IdUtils.getIdFromFile(f));
                if (optMedia.isEmpty())
                    //Not found? Process it!
                    processMedia(f, type);
                else if (checkReprocess((T) optMedia.get()))
                    //Found and not to re-process? Notify to re-process if needed
                    log.debug(String.format("Found pending to process '%s' events for: %s", this.getClass().getSimpleName(), optMedia.get().getPath()));
            }
            return true;
        }, visitMaxProcessingTimeSecs);
    }

    protected boolean checkReprocess(T media) {
        if (media.getStatusValue(this.getClass().getSimpleName()) == STATUS_STEP.PENDING) {
            log.debug(String.format("Detected media: '%s' still not processed by '%s', offering to processing queue.", media.getPath(), this.getClass()));
            processLater(media);
            return true;
        }
        return false;
    }

    private void processMedia(File f, FILE_TYPE type) {
        final String absPath = f.getAbsolutePath();
        final String parentAbsPath = f.getParent();
        T mediaFile = getBuilder()
                .id(IdUtils.getIdFromAbsPath(absPath))
                .path(absPath)
                .parent(parentAbsPath)
                .parentHash(IdUtils.getIdFromAbsPath(parentAbsPath))
                .name(f.getName())
                .type(type)
                .build();
        mediaFile.setStatusValue(this.getClass().getSimpleName(), STATUS_STEP.PENDING);
        mediaRepository.save(mediaFile);
        log.debug(String.format("Storing %s on db: %s", forClass.getSimpleName(), mediaFile.getPath()));
        processLater(mediaFile); //NOSONAR
    }

    protected abstract Z getBuilder();

}
