package org.photomemento.back.api.controller.media.video;

import org.photomemento.back.api.controller.media.ParentMediaController;
import org.photomemento.back.domain.entity.file.Media;
import org.photomemento.back.exceptions.api.NotFoundException;
import org.photomemento.back.exceptions.api.ServerErrorException;
import org.photomemento.back.repository.media.MediaRepository;
import org.photomemento.back.service.VideoStreamService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.Optional;

import static org.photomemento.back.types.Constants.*;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(path = ABS_MEDIA_VID)
public class VideoStreamController  extends ParentMediaController {

    private final VideoStreamService videoStreamService;

    public VideoStreamController(
            MediaRepository mediaRepository,
            VideoStreamService videoStreamService) {
        super(mediaRepository);
        this.videoStreamService = videoStreamService;
    }

    @GetMapping(value = REL_BY_ID)
    public ResponseEntity<byte[]> streamVideo(
            @RequestHeader(value = "Range", required = false) String httpRangeList,
            @PathVariable("id") String imageId,
            @RequestParam(name = "download", required = false) boolean isDownload) {

        Optional<Media> optMedia = mediaRepository.findById(imageId);
        if (optMedia.isEmpty())
            throw new NotFoundException("Id was not found in database.");

        Media media = optMedia.get();
        String imagePath = media.getMedia().get(V_ORIGINAL_SUFFIX);

        File file = new File(imagePath);
        if (!file.exists())
            throw new NotFoundException(String.format("Original video does not exist in target path or unable to access it: %s", imagePath));

        if(media.getContentType()==null)
            throw new ServerErrorException("There was an error while retrieving media content type (null)");

        return videoStreamService.prepareContent(file, media.getContentType(), httpRangeList, isDownload, media.getName());
    }
}