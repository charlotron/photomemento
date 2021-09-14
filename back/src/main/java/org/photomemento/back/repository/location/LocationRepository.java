package org.photomemento.back.repository.location;

import org.photomemento.back.domain.entity.Location;
import org.photomemento.back.domain.entity.file.media.geodata.ZoomLevels;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface LocationRepository extends MongoRepository<Location, String>, ILocationRepository {
    Optional<Location> findByZoomLevelAndName(ZoomLevels.ZOOM_LEVEL zoomLevel, String name);
    List<Location> findByZoomLevelOrderByNameAsc(ZoomLevels.ZOOM_LEVEL zoomLevel);
}