package org.photomemento.back.api.controller.media;

import org.photomemento.back.api.controller.ApiController;
import org.photomemento.back.repository.media.MediaRepository;

public abstract class ParentMediaController  extends ApiController {

    protected final MediaRepository mediaRepository;

    protected ParentMediaController(MediaRepository mediaRepository) {
        super();
        this.mediaRepository = mediaRepository;
    }
}
