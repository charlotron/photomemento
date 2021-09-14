package org.photomemento.back.monitoring.service.typeprocessors.media.resample;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.photomemento.back.domain.entity.file.Media;
import org.photomemento.back.domain.entity.file.media.Photo;
import org.photomemento.back.exceptions.PhotoMementoError;
import org.photomemento.back.monitoring.service.ProcessorQueueService;
import org.photomemento.back.monitoring.service.ThreadLockService;
import org.photomemento.back.repository.media.MediaRepository;
import org.photomemento.back.types.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;

@Service
@Slf4j
public class PhotoResampleDataProcessor extends ParentResampleDataProcessor<Photo> {

    @Value("${image.resample.mini.quality}")
    public double miniQuality;
    @Value("${image.resample.mini.height}")
    private int miniHeight;
    @Value("${image.resample.normal.quality}")
    public double normalQuality;
    @Value("${image.resample.normal.height}")
    private int normalHeight;

    @Value("${image.resample.format}")
    public String format;

    @Value("${queue.image.resample.workers.num}")
    private int queueImageResampleWorkersNum;
    @Value("${queue.image.resample.workers.delay}")
    private int queueImageResampleWorkersDelay;
    @Value("${queue.image.resample.workers.max.processing.time.secs}")
    private long queueImageResampleWorkersMaxProcessingTimeSecs;

    protected PhotoResampleDataProcessor(
            ThreadLockService mediaLockService,
            MediaRepository mediaRepository,
            ProcessorQueueService processorQueueService) {
        super(Photo.class, mediaLockService, mediaRepository, processorQueueService);
    }

    @PostConstruct
    @Override
    public void init() {
        super.createWorker(queueImageResampleWorkersNum, queueImageResampleWorkersDelay);
    }

    @Override
    public boolean process(Photo photo) {
        photo.addMedia(Constants.ORIGINAL_SUFFIX, photo.getPath());
        //Generate versions of current image with new resolution
        generateNewImage(photo, photo.getHeight(), miniHeight, miniQuality, Constants.MINI_SUFFIX);
        generateNewImage(photo, photo.getHeight(), normalHeight, normalQuality, Constants.NORMAL_SUFFIX);
        return true;
    }

    @Override
    public long getMaxProcessingTime() {
        return this.queueImageResampleWorkersMaxProcessingTimeSecs;
    }

    private void generateNewImage(Media media, int actualHeight, int maxHeight, double quality, String suffix) {
        int height = Math.min(maxHeight, actualHeight);

        //Generate thumbnail and save it to main
        String resampledImage = resampleImage(media.getId(), media.getPath(), media.getRotation(), suffix, height, quality);
        media.addMedia(suffix, resampledImage);

        log.info(String.format("Resampled: %s with image[%s]: %s.", media.getName(), suffix, resampledImage));
    }

    protected String resampleImage(String id, String sourceImagePath, Integer rotation, String suffix, int height, double quality) {
        String destMiniThumb = getResampleMediaPath(id, suffix, format);
        try(FileInputStream is=new FileInputStream(new File(sourceImagePath))) {
            BufferedImage sourceImage = ImageIO.read(is);
            if(sourceImage==null)
                throw new PhotoMementoError("Cannot read source image as an image, maybe it is corrupted.");
            Thumbnails
                    .of(sourceImage)
                    .rotate(Optional.ofNullable(rotation).orElse(0))
                    .outputFormat(format)
                    .outputQuality(quality)
                    .height(height)
                    .toFile(new File(destMiniThumb));
        } catch (IOException|PhotoMementoError e) {
            log.error(String.format("Could not generate cache [height: %d] for image[%s]: %s. Target image: %s", height, suffix, sourceImagePath, destMiniThumb), e);
            return null;
        }
        return destMiniThumb;
    }

    public void removeCachedMultimedia(Media media) {
        removeCachedMedia(media, Constants.MINI_SUFFIX, format);
        removeCachedMedia(media, Constants.NORMAL_SUFFIX, format);
    }
}
