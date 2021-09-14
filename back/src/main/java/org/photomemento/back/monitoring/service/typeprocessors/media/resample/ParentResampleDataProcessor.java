package org.photomemento.back.monitoring.service.typeprocessors.media.resample;

import lombok.extern.slf4j.Slf4j;
import org.photomemento.back.domain.entity.file.Media;
import org.photomemento.back.monitoring.service.ProcessorQueueService;
import org.photomemento.back.monitoring.service.ThreadLockService;
import org.photomemento.back.repository.media.MediaRepository;
import org.photomemento.back.types.Constants;
import org.photomemento.back.types.ThreadWorker;
import org.photomemento.back.types.enu.STATUS_STEP;
import org.photomemento.back.util.IdUtils;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Optional;

//TODO: A bit verbose
@Slf4j
public abstract class ParentResampleDataProcessor<T extends Media> implements ProcessorQueueService.ProcessorQueueServiceProcessor<String> {

    @Value("${cache.resources.media.dir}")
    protected String cacheMediaDir;

    private final ThreadLockService mediaLockService;
    private final Class<T> clazz;
    private final MediaRepository mediaRepository;
    private final ProcessorQueueService processorQueueService;
    private ThreadWorker<String> threadWorker;

    protected ParentResampleDataProcessor(
            Class<T> clazz,
            ThreadLockService mediaLockService,
            MediaRepository mediaRepository,
            ProcessorQueueService processorQueueService) {
        this.clazz = clazz;
        this.mediaLockService = mediaLockService;
        this.mediaRepository = mediaRepository;
        this.processorQueueService = processorQueueService;
    }

    public abstract void init();

    protected void createWorker(int workersNum, int workersDelay) {
        //Create the worker
        threadWorker = processorQueueService.newQueueForClassWithWorkers(this.getClass().getSimpleName(), workersNum, workersDelay, this, true);
    }

    public void processLater(Media media) {
        threadWorker.offer(media.getPath());
    }

    protected abstract boolean process(T media);

    @Override
    public boolean processQueueItem(String mediaPath) {
        final String mediaId= IdUtils.getIdFromAbsPath(mediaPath);
        return mediaLockService.lockRefreshFromDbRun(
                mediaId,
                clazz,
                (media -> {
                    boolean isError=false;
                    try {
                        boolean res = process(media);

                        //SET STATUS
                        media.setStatusValue(this.getClass().getSimpleName(), res ? STATUS_STEP.OK : STATUS_STEP.N_A);

                        //Set has media flag used in listing
                        if (res && Optional.ofNullable(media.getMedia()).map(Map::isEmpty).isPresent())
                            media.setHasMedia(true);

                    } catch (Exception | Error e) { //NOSONAR
                        log.error(String.format("There was an error generating new image from media: %s, error: %s", media.getPath(), e.getMessage()), e);
                        media.setStatusValue(this.getClass().getSimpleName(), STATUS_STEP.FAIL);
                        isError=true;
                    }

                    //SAVE
                    mediaRepository.save(media);

                    return !isError;
                }),getMaxProcessingTime());
    }

    public abstract long getMaxProcessingTime();

    public Class<T> getProcessClass() {
        return clazz;
    }

    public abstract void removeCachedMultimedia(Media media);

    public void removeCachedMedia(Media media, String suffix, String format) {
        String cachedMedia = getResampleMediaPath(media.getId(), suffix, format);
        try {
            File f = new File(cachedMedia);
            if (f.exists())
                Files.delete(f.toPath());
        } catch (IOException e) {
            log.error(String.format("[Deleted original] Could not delete cached file: %s, due to: %s", cachedMedia, e.getMessage()), e);
        }
    }

    protected String getResampleMediaPath(String id, String suffix, String format) {
        return String.format(Constants.RESAMPLED_MEDIA_FILE_FORMATTED, cacheMediaDir, id, suffix, format);
    }
}
