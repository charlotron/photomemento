package org.photomemento.back.api.controller.media;

import com.drew.lang.annotations.NotNull;
import org.photomemento.back.domain.entity.file.Media;
import org.photomemento.back.repository.media.MediaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static org.photomemento.back.types.Constants.*;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(path = ABS_MEDIA)
public class MediaController extends ParentMediaController {

    public MediaController(MediaRepository mediaRepository) {
        super(mediaRepository);
    }

    @GetMapping(path = ABS_ROOT)
    public Page<Media> listMedia(@NotNull final Pageable pageable) {
        return mediaRepository.findAllByHasMediaTrueOrderByShotDateDesc(pageable);
    }

    @GetMapping(path = REL_BY_ID)
    public Optional<Media> getById(@PathVariable(ID) String imageId) {
        return mediaRepository.findById(imageId);
    }


}
