package org.photomemento.back.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.photomemento.back.domain.entity.file.media.geodata.ZoomLevels;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Document("locations")
@CompoundIndex(name = "findByNameAndZoomLevel", def = "{'zoomLevel':1, 'name': 1}")
public class Location {
    @Id
    private Long id;
    @Indexed
    private String name;
    @Indexed
    private ZoomLevels.ZOOM_LEVEL zoomLevel;
    protected Double latitude;
    protected Double longitude;
}
