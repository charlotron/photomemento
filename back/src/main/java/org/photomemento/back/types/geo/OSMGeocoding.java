package org.photomemento.back.types.geo;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

//Full List: https://nominatim.org/release-docs/develop/api/Output/#addressdetails
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OSMGeocoding {
    @JsonAlias("place_id")
    private long placeId;
    @JsonAlias("osm_type")
    private String osmType;
    @JsonAlias("osm_id")
    private long osmId;
    private String type;
    private String label;
    private String name;

    private String street;
    private String postcode; //Found that some postcode was buggy and returned a text :S

    @JsonAlias("city_district")
    private String cityDistrict;
    private String district;
    private String borough;
    private String suburb;
    private String subdivision;

    private String village;
    private String town;
    private String city;
    private String municipality;

    @JsonAlias("state_district")
    private String stateDistrict;

    private String region;
    private String county;
    private String state;

    private String country;

    private String continent;
}

/*
 * Sample obj
{
    "place_id":178167699,
    "osm_type":"way",
    "osm_id":377787760,
    "type":"track",
    "accuracy":0,
    "label":"Camino de Fuentenovilla, Peña Ambite, Ambite, Las Vegas, Comunidad de Madrid, España",
    "name":"Camino de Fuentenovilla",
    "country":"España",
    "state":"Comunidad de Madrid",
    "city":"Ambite",
    "district":"Peña Ambite",
    "admin":{
        "level4":"Comunidad de Madrid",
        "level7":"Las Vegas",
        "level8":"Ambite"
    }
}

 */