package org.photomemento.back.monitoring.service.typeprocessors.media.resample;

import lombok.extern.slf4j.Slf4j;
import org.photomemento.back.domain.entity.file.Media;
import org.photomemento.back.domain.entity.file.media.Video;
import org.photomemento.back.exceptions.PhotoMementoError;
import org.photomemento.back.monitoring.service.FFMpegService;
import org.photomemento.back.monitoring.service.ProcessorQueueService;
import org.photomemento.back.monitoring.service.ThreadLockService;
import org.photomemento.back.repository.media.MediaRepository;
import org.photomemento.back.types.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.photomemento.back.types.Constants.*;

@Service
@Slf4j
public class VideoResampleDataProcessor extends ParentResampleDataProcessor<Video> {

    @Value("${image.resample.mini.height}")
    private int imageMiniHeight;
    @Value("${image.resample.format}")
    private String imageFormat;
    @Value("${video.resample.normal.height}")
    private int videoNormalHeight;
    @Value("${video.resample.mini.height}")
    private int videoMiniHeight;
    @Value("${video.resample.mini.duration.secs}")
    private int videoMiniDurationSecs;
    @Value("${video.resample.format}")
    private String videoFormat;

    @Value("${queue.video.resample.workers.num}")
    private int queueVideoResampleWorkersNum;
    @Value("${queue.video.resample.workers.delay}")
    private int queueVideoResampleWorkersDelay;
    @Value("${queue.video.resample.workers.max.processing.time.secs}")
    private long queueVideoResampleWorkersMaxProcessingTimeSecs;

    private final FFMpegService ffMpegService;

    protected VideoResampleDataProcessor(
            ThreadLockService mediaLockService,
            MediaRepository mediaRepository,
            ProcessorQueueService processorQueueService,
            FFMpegService ffMpegService) {
        super(Video.class, mediaLockService, mediaRepository, processorQueueService);
        this.ffMpegService = ffMpegService;
    }

    @PostConstruct
    @Override
    public void init() {
        super.createWorker(queueVideoResampleWorkersNum, queueVideoResampleWorkersDelay);
    }

    @Override
    protected boolean process(Video videoFile) {
        //Don't process in these cases
        if (videoFile.getWidthHeightRatio() == null || videoFile.getDuration() == null || videoFile.getDuration()==0 || videoFile.getHeight() == 0 || videoFile.getWidth() == 0)
            return true;

        //Calculate
        long duration = videoFile.getDuration();

        int photoPos = Math.round(duration / 2f);
        long miniStart = Math.max(0, photoPos - (videoMiniDurationSecs / 2));
        long miniLength = (miniStart + videoMiniDurationSecs) >= duration ?
                duration - miniStart :
                videoMiniDurationSecs;

        //Get the path for starting video
        String path = videoFile.getPath();

        //Add original
        videoFile.addMedia(Constants.V_ORIGINAL_SUFFIX, videoFile.getPath());

        //Generate animated thumb from video
        path = generateResampledVideo(videoFile, path, null, null, Math.min(videoFile.getHeight(), videoNormalHeight), V_NORMAL_SUFFIX, true);
        //Ge use resampled video for further seek starting bytes as long as mpg videos can fail if trying to seek position
        generateResampledVideo(videoFile, path, miniStart, miniLength, videoMiniHeight, V_MINI_SUFFIX, false);

        //Generate image thumb from video
        generateImageFromVideo(videoFile, path, photoPos, imageMiniHeight, MINI_SUFFIX);
        //TODO: do we must generate a normal version of the image?
        return true;
    }

    @Override
    public long getMaxProcessingTime() {
        return this.queueVideoResampleWorkersMaxProcessingTimeSecs;
    }

    private void generateImageFromVideo(Video videoFile, String videoPath, long startPositionSecs, int targetHeight, String suffix) {
        //Calculating ratio height/width to
        targetHeight = fixHeight(targetHeight);
        int targetWidth = calculateScaledWidth(targetHeight, videoFile.getWidthHeightRatio());

        final String newImagePath = getResampleMediaPath(videoFile.getId(), suffix, imageFormat);
        log.info(String.format("Resampling start from video: %s to image[%s]: %s.", videoFile.getName(), suffix, newImagePath));
        try {
            ffMpegService.generateImageFromVideoAtPosition(
                    videoPath,
                    newImagePath,
                    startPositionSecs,
                    targetWidth,
                    targetHeight);
            videoFile.addMedia(suffix, newImagePath);
        } catch (PhotoMementoError e) {
            if (!Files.exists(Path.of(newImagePath)))
                throw new PhotoMementoError(String.format("There was a problem while resampling video: %s with video [%s]: %s", videoFile.getName(), suffix, newImagePath), e);
        }

        log.info(String.format("Resampled from video: %s to image[%s]: %s.", videoFile.getName(), suffix, newImagePath));
    }

    private String generateResampledVideo(Video videoFile, String videoPath, Long startPositionSecs, Long lengthFromStart, int targetHeight, String suffix, boolean withAudio) {
        final String newVideoPath = getResampleMediaPath(videoFile.getId(), suffix, videoFormat);
        targetHeight = fixHeight(targetHeight);
        int targetWidth = calculateScaledWidth(targetHeight, videoFile.getWidthHeightRatio());
        log.info(String.format("Resampling start from video: %s to video [%s]: %s.", videoFile.getName(), suffix, newVideoPath));
        try {
            ffMpegService.resampleAndTransformVideo(
                    videoPath,
                    newVideoPath,
                    startPositionSecs,
                    lengthFromStart,
                    targetWidth,
                    targetHeight,
                    withAudio);
        } catch (PhotoMementoError e) {
            if (!Files.exists(Path.of(newVideoPath)))
                throw new PhotoMementoError(String.format("There was a problem while resampling video: %s with video [%s]: %s", videoFile.getName(), suffix, newVideoPath), e);
        }
        videoFile.addMedia(suffix, newVideoPath);

        log.info(String.format("Resampled from video: %s to video [%s]: %s.", videoFile.getName(), suffix, newVideoPath));
        return newVideoPath;
    }

    private int fixHeight(int height) {
        return height % 2 == 0 ? height : height + 1;
    }

    private int calculateScaledWidth(int targetHeight, float ratioHW) {
        int targetWidth = Math.round(targetHeight * ratioHW);
        return targetWidth % 2 == 0 ? targetWidth : targetWidth + 1;
    }

    @Override
    public void removeCachedMultimedia(Media media) {
        removeCachedMedia(media, Constants.MINI_SUFFIX, imageFormat);
        removeCachedMedia(media, V_MINI_SUFFIX, videoFormat);
    }
}
