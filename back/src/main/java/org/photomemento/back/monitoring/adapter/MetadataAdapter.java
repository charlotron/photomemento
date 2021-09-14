package org.photomemento.back.monitoring.adapter;

import org.photomemento.back.domain.entity.file.media.Metadata;
import org.photomemento.back.types.initializer.I;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.photomemento.back.types.Constants.FF_FORMAT;
import static org.photomemento.back.types.Constants.FF_STREAM;

@Component
public class MetadataAdapter {
    private static final SimpleDateFormat SDF =    //NOSONAR
            I.ofTapGet(
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss'Z'"),
                    sdf -> sdf.setTimeZone(TimeZone.getTimeZone("UTC")));
    public static final String STREAMS = "streams";
    public static final String FORMAT = "format";

    private static final String FILE_MODIFIED_DATE = "File Modified Date";
    private static final Pattern REGEX_CAPTURE_META_DATA = Pattern.compile("^\\[([^\\]]+)\\]\\s*([^\\-]+)\\s*-\\s*([^ ].*[^ ])\\s*$");

    public List<Metadata> toMetadata(com.drew.metadata.Metadata originalMetadata, Map<String, Object> videoMetadata, long lastModified) {
        List<Metadata> metadataList = new ArrayList<>();
        if (originalMetadata != null)
            processOriginalMetadata(metadataList, originalMetadata, lastModified);
        if (videoMetadata != null)
            processVideoMetadata(metadataList, videoMetadata);
        return metadataList.isEmpty() ? null : metadataList;
    }

    private void processOriginalMetadata(List<Metadata> metadataList, com.drew.metadata.Metadata originalMetadata, long lastModified) {
        originalMetadata.getDirectories().forEach(directory ->
                directory.getTags().forEach(tag -> {
                    Matcher matcher = REGEX_CAPTURE_META_DATA.matcher(tag.toString());
                    if (!matcher.matches()) return;
                    final String type = matcher.group(1).trim();
                    final String name = matcher.group(2).trim();
                    String value = matcher.group(3).trim();
                    if (name.equals(FILE_MODIFIED_DATE))
                        value = SDF.format(new Date(lastModified));
                    Metadata metadata = Metadata.builder()
                            .type(type)
                            .name(name)
                            .value(value)
                            .build();
                    metadataList.add(metadata);
                }));
    }

    @SuppressWarnings({"unchecked"})
    private void processVideoMetadata(List<Metadata> metadataList, Map<String, Object> videoMetadata) {
        List<Object> streams = getExpectedData(videoMetadata, STREAMS);
        if (streams != null) {
            for (int i = 0; i < streams.size(); i++) {
                Object streamObj = streams.get(i);
                String groupName = String.format(FF_STREAM, i);
                if (streamObj instanceof Map)
                    parseMapDataToMeta(metadataList, groupName, (Map<String, Object>) streamObj);
            }
        }

        Map<String, Object> format = getExpectedData(videoMetadata, FORMAT);
        if (format != null)
            parseMapDataToMeta(metadataList, FF_FORMAT, format);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> T getExpectedData(Object map, String key) {
        if (map == null) return null;
        if (!(map instanceof Map)) return null;
        return (T) ((Map) map).get(key);
    }

    public static void parseMapDataToMeta(List<Metadata> metadataList, String groupName, Map<String, Object> map) {
        map
                .entrySet()
                .stream()
                .filter(entry -> !(entry.getValue() instanceof Map || entry.getValue() instanceof List))
                .forEach(entry ->
                        metadataList.add(
                                Metadata.builder()
                                        .type(groupName)
                                        .name(entry.getKey())
                                        .value(entry.getValue().toString())
                                        .build()));
    }
}

