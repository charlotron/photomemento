package org.photomemento.back.types.geo;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OSMSearchRes {
    @JsonAlias("place_id")
    private long placeId;
    @JsonAlias("display_name")
    private String displayName;
    private double lat;
    private double lon;
}
