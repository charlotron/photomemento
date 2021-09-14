package org.photomemento.back.api.controller.media.image;

import org.photomemento.back.api.controller.media.ParentMediaController;
import org.photomemento.back.domain.entity.file.Media;
import org.photomemento.back.exceptions.api.NotFoundException;
import org.photomemento.back.exceptions.api.ServerErrorException;
import org.photomemento.back.repository.media.MediaRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static org.apache.commons.io.FileUtils.readFileToByteArray;
import static org.photomemento.back.types.Constants.*;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(path = ABS_MEDIA_IMG)
public class ImageController extends ParentMediaController {

    public ImageController(MediaRepository mediaRepository) {
        super(mediaRepository);
    }

    @GetMapping(value = REL_BY_ID, produces = MediaType.IMAGE_JPEG_VALUE)
    public @ResponseBody
    HttpEntity<byte[]> getOriginalImageFromId(
            @PathVariable("id") String imageId,
            @RequestParam(name = "download", required = false) boolean isDownload) {
        Optional<Media> optMedia = mediaRepository.findById(imageId);
        if (optMedia.isEmpty())
            throw new NotFoundException("Id was not found in database.");
        Media media = optMedia.get();
        if (CollectionUtils.isEmpty(media.getMedia()) || media.getMedia().get(ORIGINAL_SUFFIX) == null)
            throw new NotFoundException("Original image is not registered in database for given id.");
        String imagePath = media.getMedia().get(ORIGINAL_SUFFIX);
        File file = new File(imagePath);
        if (!file.exists())
            throw new NotFoundException(String.format("Original image does not exist in target path or unable to access it: %s", imagePath));
        byte[] bytes;
        try {
            bytes = readFileToByteArray(new File(imagePath));
        } catch (IOException ex) {
            throw new ServerErrorException(String.format("Problems reading original image does not exist in target path or unable to access it: %s", imagePath), ex);
        }
        HttpHeaders headers = new HttpHeaders();
        if (isDownload) {
            headers.setContentType(MediaType.IMAGE_JPEG);
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + media.getName());
            headers.setContentLength(bytes.length);
        }

        return new HttpEntity<>(bytes, headers);
    }
}
