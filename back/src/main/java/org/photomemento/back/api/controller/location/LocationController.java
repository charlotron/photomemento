package org.photomemento.back.api.controller.location;

import com.drew.lang.annotations.NotNull;
import org.photomemento.back.api.controller.ApiController;
import org.photomemento.back.domain.entity.Location;
import org.photomemento.back.domain.entity.file.media.geodata.ZoomLevels;
import org.photomemento.back.exceptions.PhotoMementoError;
import org.photomemento.back.repository.location.LocationRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.photomemento.back.types.Constants.*;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(path = ABS_LOCATION)
public class LocationController extends ApiController {

    private final LocationRepository locationRepository;

    public LocationController(LocationRepository locationRepository) {
       this.locationRepository=locationRepository;
    }

    @GetMapping(path = REL_LOCATION_BY_ZOOM)
    public List<Location> listAllLocationsByZoom(@NotNull final Pageable pageable, @PathVariable(ZOOM) ZoomLevels.ZOOM_LEVEL zoom) {
        if(zoom==null) throw new PhotoMementoError(
                String.format("Invalid zoom level use one of the available: %s", Arrays.stream(ZoomLevels.ZOOM_LEVEL.values()).map(ZoomLevels.ZOOM_LEVEL::name).collect(Collectors.toList())));

        return locationRepository.findByZoomLevelOrderByNameAsc(zoom);
    }
}
