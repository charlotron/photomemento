package org.photomemento.back.api.controller.media.geodata;

import com.drew.lang.annotations.NotNull;
import org.photomemento.back.api.controller.media.ParentMediaController;
import org.photomemento.back.domain.entity.GeoDataTotal;
import org.photomemento.back.domain.entity.file.media.geodata.ZoomLevels;
import org.photomemento.back.exceptions.PhotoMementoError;
import org.photomemento.back.repository.media.MediaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.photomemento.back.types.Constants.*;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(path = ABS_GEO_DATA_DIR)
public class GeoDataController extends ParentMediaController {

    public GeoDataController(MediaRepository mediaRepository) {
        super(mediaRepository);
    }

    @GetMapping(path = REL_GEO_DATA_BY_ZOOM)
    public List<GeoDataTotal> listAllGeoDataTotals(@NotNull final Pageable pageable, @PathVariable(ZOOM) ZoomLevels.ZOOM_LEVEL zoom) {
        if(zoom==null) throw new PhotoMementoError(
                String.format("Invalid zoom level use one of the available: %s", Arrays.stream(ZoomLevels.ZOOM_LEVEL.values()).map(ZoomLevels.ZOOM_LEVEL::name).collect(Collectors.toList())));

        return mediaRepository.findAllLocationsTotals(pageable,zoom);
    }
}
