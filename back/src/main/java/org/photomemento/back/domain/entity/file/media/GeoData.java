package org.photomemento.back.domain.entity.file.media;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.photomemento.back.domain.entity.file.media.geodata.ZoomLevels;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
//TextIndexed fields are on MongoConfig class
//https://nominatim.org/release-docs/develop/api/Output/#addressdetails
public class GeoData {
    private ZoomLevels zoomLevels;

    /*    private String name;

        private String street;
       private Integer postcode;

        private String cityDistrict;
        private String district;
        private String borough;
        private String suburb;
        private String subdivision;

        private String village;
        private String town;*/

    private String city;

    /*   private String municipality;

       private String stateDistrict;

       private String region;
       private String county;*/

    private String state;

    private String country;

    /*    private String continent;*/
}
