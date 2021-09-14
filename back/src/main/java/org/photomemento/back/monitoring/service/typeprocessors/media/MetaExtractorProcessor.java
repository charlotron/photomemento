package org.photomemento.back.monitoring.service.typeprocessors.media;

import com.drew.imaging.ImageMetadataReader;
import lombok.extern.slf4j.Slf4j;
import org.photomemento.back.domain.entity.file.Media;
import org.photomemento.back.monitoring.adapter.MetadataAdapter;
import org.photomemento.back.monitoring.service.FFMpegService;
import org.photomemento.back.types.enu.FILE_TYPE;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Map;

/**
 * This class resolves the main date from metadata, file name, etc... (It is using for media ordering purposes)
 */
@Service
@Slf4j
public class MetaExtractorProcessor {

    private final MetadataAdapter metadataAdapter;
    private final FFMpegService ffMpegService;

    public MetaExtractorProcessor(MetadataAdapter metadataAdapter, FFMpegService ffMpegService) {
        this.metadataAdapter = metadataAdapter;
        this.ffMpegService = ffMpegService;
    }

    public void extractMetadata(Media media) {
        String filePathStr = media.getPath();

        File file = new File(filePathStr);
        long lastModified = file.lastModified();

        com.drew.metadata.Metadata originalMetadata = null;
        try {
            originalMetadata = ImageMetadataReader.readMetadata(file);
        } catch (Exception e) {
            log.warn(String.format("There was a problem while reading file to extract metadata: %s", file), e);
        }

        Map<String, Object> videoMetadata = null;
        if (media.getType() == FILE_TYPE.VIDEO)
            videoMetadata = ffMpegService.getMetadata(media.getPath());

        media.setMetadata(metadataAdapter.toMetadata(originalMetadata, videoMetadata, lastModified));

    }
}
