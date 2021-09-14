package org.photomemento.back.domain.entity.file;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.photomemento.back.domain.entity.File;
import org.photomemento.back.domain.entity.file.media.GeoData;
import org.photomemento.back.domain.entity.file.media.Metadata;
import org.photomemento.back.types.enu.FILE_TYPE;
import org.photomemento.back.util.NumberUtils;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.*;


@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
@Document("media")
@CompoundIndex(name = "findAllByHasMediaByParentHashOrderByShotDateDesc", def = "{'hasMedia': 1, 'parentHash': 1, 'shotDate':-1}")
@CompoundIndex(name = "findAllByHasMediaHashOrderByShotDateDesc", def = "{'hasMedia': 1, 'shotDate':-1}")
//TextIndexed fields are on MongoConfig class
public class Media extends File { //NOSONAR
    @Indexed
    protected FILE_TYPE type;
    protected Map<String, String> media; //NOSONAR
    @Indexed(sparse = true)
    protected boolean hasMedia;
    List<Metadata> metadata;
    @Indexed(direction = IndexDirection.DESCENDING)
    protected Instant shotDate;
    protected Integer width;
    protected Integer height;
    protected Float widthHeightRatio;
    protected Integer rotation;
    protected Double latitude;
    protected Double longitude;
    protected GeoData geoData;
    protected String contentType;
    protected float score; //This is used only on searches (as long as mongo does not store null values, dont mind having on main domain obj)

    public String getValueOfMetadata(String... names) {
        return getValueOfMetadataByType(null, names);
    }

    public String getValueOfMetadataByType(String type, String... names) {
        return getMetadata()
                .stream()
                .filter(meta -> Optional.ofNullable(type)
                        .map(t -> meta.getType().equalsIgnoreCase(t))
                        .orElse(true))
                .filter(meta ->
                        names != null &&
                                names.length > 0 &&
                                Arrays.stream(names)
                                        .anyMatch(name -> name.equalsIgnoreCase(meta.getName())))
                .findFirst()
                .map(Metadata::getValue)
                .orElse(null);
    }

    public void addMedia(String key, String path) {
        if (media == null) media = new HashMap<>();
        media.put(key, path);
    }

    public Integer tryGetPropertyAsInt(String... properties) {
        return tryGet(NumberUtils::extractDigits, properties);
    }

    public Float tryGetPropertyAsFloat(String... properties) {
        return tryGet(NumberUtils::parseAsFloatFixingCommas, properties);
    }

    public Double tryGetPropertyAsDouble(String... properties) {
        return tryGet(NumberUtils::parseAsDoubleFixingCommas, properties);
    }

    public <T> T tryGet(TryGetProcessor<T> processor, String... properties) {
        try {
            return Optional.ofNullable(getValueOfMetadata(properties))
                    .filter(StringUtils::hasText)
                    .map(processor::process)
                    .orElse(null);
        } catch (Exception | Error e) { //NOSONAR
            return null;
        }
    }

    @FunctionalInterface
    public interface TryGetProcessor<T> {
        T process(String txt);
    }
}
