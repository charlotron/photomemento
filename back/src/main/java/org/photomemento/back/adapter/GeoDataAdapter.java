package org.photomemento.back.adapter;

import org.photomemento.back.domain.entity.file.media.GeoData;
import org.photomemento.back.domain.entity.file.media.geodata.ZoomLevels;
import org.photomemento.back.types.geo.OSMGeocoding;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class GeoDataAdapter {

    public GeoData fromOsmGeocoding(OSMGeocoding osmGeocoding) {

        String lowLevel=resolveFirstNotNull(osmGeocoding,OSMGeocoding::getCountry);
        String midLevel=resolveFirstNotNull(osmGeocoding,OSMGeocoding::getState/*,OSMGeocoding::getCountry*/);
        String highLevel=resolveFirstNotNull(osmGeocoding,/*OSMGeocoding::getVillage,OSMGeocoding::getTown,*/OSMGeocoding::getCity);

        ZoomLevels zoomLevels = null;
        if(lowLevel!=null && midLevel!=null && highLevel!=null){
            zoomLevels=ZoomLevels.builder()
                    .lowLevel(lowLevel)
                    .mediumLevel(midLevel)
                    .highLevel(highLevel)
                    .build();
        }

        return GeoData.builder()
                .zoomLevels(zoomLevels)
                //.name(osmGeocoding.getName())
                //.street(osmGeocoding.getStreet())
                //.postcode(osmGeocoding.getPostcode())
                //.cityDistrict(osmGeocoding.getCityDistrict())
                //.district(osmGeocoding.getDistrict())
                //.borough(osmGeocoding.getBorough())
                //.suburb(osmGeocoding.getSuburb())
                //.subdivision(osmGeocoding.getSubdivision())
                //.village(osmGeocoding.getVillage())
                //.town(osmGeocoding.getTown())
                .city(osmGeocoding.getCity())
                //.municipality(osmGeocoding.getMunicipality())
                //.stateDistrict(osmGeocoding.getStateDistrict())
                //.region(osmGeocoding.getRegion())
                //.county(osmGeocoding.getCounty())
                .state(osmGeocoding.getState())
                .country(osmGeocoding.getCountry())
                //.continent(osmGeocoding.getContinent())
                .build();
    }

    private String resolveFirstNotNull(OSMGeocoding osmGeocoding, Resolver... resolvers) {
        if(resolvers==null) return null;
        for(Resolver resolver:resolvers){
            String str=resolver.valueResolver(osmGeocoding);
            if(StringUtils.hasText(str)) return str;
        }
        return null;
    }

    @FunctionalInterface
    private interface Resolver{
        String valueResolver(OSMGeocoding osmGeocoding);
    }
}
