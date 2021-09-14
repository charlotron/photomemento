package org.photomemento.back.repository.media;

import org.photomemento.back.domain.entity.GeoDataTotal;
import org.photomemento.back.domain.entity.file.Media;
import org.photomemento.back.domain.entity.file.media.geodata.ZoomLevels;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IMediaRepository {
    List<GeoDataTotal> findAllLocationsTotals(Pageable pageable, ZoomLevels.ZOOM_LEVEL zoom);
    Page<Media> searchAndHasMedia(Pageable pageable, String text);
}
