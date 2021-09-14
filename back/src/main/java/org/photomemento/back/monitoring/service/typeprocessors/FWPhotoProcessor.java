package org.photomemento.back.monitoring.service.typeprocessors;

import lombok.extern.slf4j.Slf4j;
import org.photomemento.back.domain.entity.file.media.Photo;
import org.photomemento.back.monitoring.service.ProcessorQueueService;
import org.photomemento.back.monitoring.service.ThreadLockService;
import org.photomemento.back.monitoring.service.typeprocessors.media.DateDataProcessor;
import org.photomemento.back.monitoring.service.typeprocessors.media.GeoDataProcessor;
import org.photomemento.back.monitoring.service.typeprocessors.media.MetaExtractorProcessor;
import org.photomemento.back.monitoring.service.typeprocessors.media.MultimediaDataProcessor;
import org.photomemento.back.monitoring.service.typeprocessors.media.resample.PhotoResampleDataProcessor;
import org.photomemento.back.repository.media.MediaRepository;
import org.photomemento.back.types.enu.STATUS_STEP;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FWPhotoProcessor extends FWParentProcessor<Photo, Photo.PhotoBuilder<Photo, ?>> {
    public FWPhotoProcessor(
            MetaExtractorProcessor metaExtractorMediaProcessor,
            DateDataProcessor dateDataMediaProcessor,
            MultimediaDataProcessor thumbDataMediaProcessor,
            ThreadLockService mediaLockService,
            MediaRepository mediaRepository,
            ProcessorQueueService processorQueueService,
            GeoDataProcessor geoMediaProcessor) {
        super(
                metaExtractorMediaProcessor,
                dateDataMediaProcessor,
                thumbDataMediaProcessor,
                mediaLockService,
                mediaRepository,
                processorQueueService,
                geoMediaProcessor,
                Photo.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Photo.PhotoBuilder<Photo, ?> getBuilder() {
        return (Photo.PhotoBuilder<Photo, ?>) Photo.builder();
    }

    @Override
    protected void processBeforeSave(Photo media) {
        super.processBeforeSave(media);

        if (geoDataProcessor.hasToBeParsed(media))
            media.setStatusValue(GeoDataProcessor.class.getSimpleName(), STATUS_STEP.PENDING);
        media.setStatusValue(PhotoResampleDataProcessor.class.getSimpleName(), STATUS_STEP.PENDING);
    }

    @Override
    protected void processAfterSave(Photo media) {
        geoDataProcessor.processLater(media);
        multimediaDataProcessor.processLater(media);
    }

    @Override
    protected boolean checkReprocess(Photo media) {
        if (super.checkReprocess(media)) return true; //NOSONAR

        boolean reprocessFired = false;

        if (media.getStatusValue(GeoDataProcessor.class.getSimpleName()) == STATUS_STEP.PENDING) {
            log.debug(String.format("Detected media: %s still not processed by '%s', offering to processing queue.", media.getPath(), GeoDataProcessor.class.getSimpleName()));
            geoDataProcessor.processLater(media);
            reprocessFired = true;
        }

        if (media.getStatusValue(PhotoResampleDataProcessor.class.getSimpleName()) == STATUS_STEP.PENDING) {
            log.debug(String.format("Detected media: '%s' still not processed by '%s', offering to processing queue.", media.getPath(), PhotoResampleDataProcessor.class.getSimpleName()));
            multimediaDataProcessor.processLater(media);
            reprocessFired = true;
        }

        return reprocessFired;
    }
}
