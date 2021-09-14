package org.photomemento.back.monitoring.service.typeprocessors.media;

import lombok.extern.slf4j.Slf4j;
import org.photomemento.back.domain.entity.file.Media;
import org.photomemento.back.domain.entity.file.media.Video;
import org.photomemento.back.monitoring.service.typeprocessors.media.resample.ParentResampleDataProcessor;
import org.photomemento.back.monitoring.service.typeprocessors.media.resample.PhotoResampleDataProcessor;
import org.photomemento.back.monitoring.service.typeprocessors.media.resample.VideoResampleDataProcessor;
import org.photomemento.back.types.Constants;
import org.photomemento.back.types.initializer.I;
import org.photomemento.back.util.NumberUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.photomemento.back.types.Constants.FF_FORMAT;

/**
 * This class does two things:
 * - Parse metadata and fix orientation of images (width/height)
 * - Generate multiple thumbnail/versions of images
 */
@Service
@Slf4j
public class MultimediaDataProcessor {

    @Value("${cache.resources.media.dir}")
    protected String cacheImagesDir;

    Map<Class<? extends Media>, ParentResampleDataProcessor<?>> processors;

    public MultimediaDataProcessor(
            PhotoResampleDataProcessor photoThumbDataMediaProcessor,
            VideoResampleDataProcessor videoThumbDataMediaProcessor) {
        processors = Map.of(
                photoThumbDataMediaProcessor.getProcessClass(), photoThumbDataMediaProcessor,
                videoThumbDataMediaProcessor.getProcessClass(), videoThumbDataMediaProcessor);

    }

    @PostConstruct
    public void init() {
        //Ensure thumbs dir exists
        try {
            Files.createDirectories(Paths.get(cacheImagesDir));
        } catch (IOException e) {
            log.error(String.format("Unable to create directory: %s", cacheImagesDir), e);
        }
    }

    public void parseThumbData(Media media) {
        parseRotation(media);
        parseWidthHeight(media);
        parseContentType(media);
        parseDuration(media);
    }

    private void parseRotation(Media media) {
        media.setRotation(parseRotationFromMetadata(media));
    }

    public void parseWidthHeight(Media media) {
        boolean rotated = Optional.ofNullable(media.getRotation()).isPresent();

        Integer width = tryGetPropertyAsInt(media, Constants.IMAGE_WIDTH, Constants.WIDTH);
        Integer height = tryGetPropertyAsInt(media, Constants.IMAGE_HEIGHT, Constants.HEIGHT);

        if (width != null && width > 0 && height != null && height > 0) {
            media.setHeight(rotated ? width : height);
            media.setWidth(rotated ? height : width);
            media.setWidthHeightRatio((float) media.getWidth() / media.getHeight());
        }
    }

    public void parseContentType(Media media) {
        media.setContentType(media.getValueOfMetadata(Constants.DETECTED_MIME_TYPE));
        log.debug(String.format("Detected mime type %s, for media: %s", media.getContentType(), media.getPath()));
    }

    public void parseDuration(Media media) {
        if (!(media instanceof Video)) return;
        Video video = (Video) media;
        Long duration = I.ofMapGet(
                media.getValueOfMetadataByType(FF_FORMAT, Constants.DURATION),
                strDuration -> {
                    try {
                        return (long) NumberUtils.parseAsDoubleFixingCommas(strDuration);
                    } catch (Exception e) {
                        log.error(String.format(Constants.COULD_NOT_FIND_PROPERTY_S_ON_METADATA_FOR_FILE_S, Constants.DURATION, media.getPath()));
                        return null;
                    }
                });
        video.setDuration(duration);
    }

    //Rotation or orientation detected?
    private Integer parseRotationFromMetadata(Media media) {
        //PHOTO meta
        Integer rotation = parseRotationIntegerMeta(
                I.of(media.getValueOfMetadata(Constants.ORIENTATION))
                        .filterGet(or -> or.toLowerCase(Locale.ROOT).contains(Constants.ORIENTATION_ROTATED)));
        if (rotation != null)
            return rotation;

        //VIDEO meta
        return parseRotationIntegerMeta(media.getValueOfMetadata(Constants.ROTATION));
    }

    private Integer parseRotationIntegerMeta(String rotationAsText) {
        return I.of(rotationAsText)
                .filter(StringUtils::hasText)
                .map(NumberUtils::extractInteger)
                .filter(num -> num != 0)
                .filterGet(num -> num % Constants.ORIENTATION_ROTATED_90_DEGREES == 0);
    }

    private Integer tryGetPropertyAsInt(Media media, String... properties) {
        return I.of(media.tryGetPropertyAsInt(properties))
                .nullTapGet(() -> log.error(String.format(Constants.COULD_NOT_FIND_PROPERTY_S_ON_METADATA_FOR_FILE_S, List.of(properties), media.getPath())));
    }

    public void processLater(Media media) {
        Optional.ofNullable(processors.get(media.getClass())).ifPresent(processor -> processor.processLater(media));
    }

    public void removeCachedMultimedia(Media media) {
        Optional.ofNullable(processors.get(media.getClass())).ifPresent(processor -> processor.removeCachedMultimedia(media));
    }
}
