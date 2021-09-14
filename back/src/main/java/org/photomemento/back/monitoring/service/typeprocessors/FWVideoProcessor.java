package org.photomemento.back.monitoring.service.typeprocessors;

import lombok.extern.slf4j.Slf4j;
import org.photomemento.back.domain.entity.file.media.Video;
import org.photomemento.back.monitoring.service.ProcessorQueueService;
import org.photomemento.back.monitoring.service.ThreadLockService;
import org.photomemento.back.monitoring.service.typeprocessors.media.DateDataProcessor;
import org.photomemento.back.monitoring.service.typeprocessors.media.GeoDataProcessor;
import org.photomemento.back.monitoring.service.typeprocessors.media.MetaExtractorProcessor;
import org.photomemento.back.monitoring.service.typeprocessors.media.MultimediaDataProcessor;
import org.photomemento.back.monitoring.service.typeprocessors.media.resample.VideoResampleDataProcessor;
import org.photomemento.back.repository.media.MediaRepository;
import org.photomemento.back.types.enu.STATUS_STEP;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FWVideoProcessor extends FWParentProcessor<Video, Video.VideoBuilder<Video, ?>> {
    public FWVideoProcessor(
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
                Video.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Video.VideoBuilder<Video, ?> getBuilder() {
        return (Video.VideoBuilder<Video, ?>) Video.builder();
    }

    @Override
    protected void processBeforeSave(Video media) {
        super.processBeforeSave(media);

        if (geoDataProcessor.hasToBeParsed(media))
            media.setStatusValue(GeoDataProcessor.class.getSimpleName(), STATUS_STEP.PENDING);
        media.setStatusValue(VideoResampleDataProcessor.class.getSimpleName(), STATUS_STEP.PENDING);
    }

    @Override
    protected void processAfterSave(Video media) {
        geoDataProcessor.processLater(media);
        multimediaDataProcessor.processLater(media);
    }

    @Override
    protected boolean checkReprocess(Video media) {
        if(super.checkReprocess(media)) return true; //NOSONAR

        boolean reprocessFired = false;

        if (media.getStatusValue(GeoDataProcessor.class.getSimpleName()) == STATUS_STEP.PENDING) {
            log.debug(String.format("Detected media: %s still not processed by '%s', offering to processing queue.", media.getPath(), GeoDataProcessor.class.getSimpleName()));
            geoDataProcessor.processLater(media);
            reprocessFired = true;
        }

        if (media.getStatusValue(VideoResampleDataProcessor.class.getSimpleName()) == STATUS_STEP.PENDING) {
            log.debug(String.format("Detected media: '%s' still not processed by '%s', offering to processing queue.", media.getPath(), VideoResampleDataProcessor.class.getSimpleName()));
            multimediaDataProcessor.processLater(media);
            reprocessFired = true;
        }

        return reprocessFired;
    }
}
