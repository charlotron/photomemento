package org.photomemento.back.types.geo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
//https://nominatim.org/release-docs/develop/api/Overview/
public class OSMReverseGeocodeRes {
    private List<OSMFeature> features;
}

/*
 * Sample obj
{
   "type":"FeatureCollection",
   "geocoding":{
      "version":"0.1.0",
      "attribution":"Data © OpenStreetMap contributors, ODbL 1.0. https://osm.org/copyright",
      "licence":"ODbL",
      "query":"40.344469,-3.157608"
   },
   "features":[
      {
         "type":"Feature",
         "properties":{
            "geocoding":{
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
         },
         "geometry":{
            "type":"Point",
            "coordinates":[
               -3.1571940929325533,
               40.343450995251324
            ]
         }
      }
   ]
}
 */

