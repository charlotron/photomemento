package org.photomemento.back.monitoring.service.typeprocessors.media;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.photomemento.back.domain.entity.file.Media;
import org.photomemento.back.domain.entity.file.media.Metadata;
import org.photomemento.back.exceptions.PhotoMementoError;
import org.photomemento.back.types.Constants;
import org.photomemento.back.util.DateUtils;
import org.photomemento.back.util.JsonUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class resolves the main date from metadata, file name, etc... (It is using for media ordering purposes)
 */
@Service
@Slf4j
public class DateDataProcessor {
    private static final String CREATION_TIME = "creationTime";
    private static final String LAST_MODIFIED_TIME = "lastModifiedTime";
    private static final String DATE_IN_FILENAME = "DATE_IN_FILENAME";

    @Value("${file.media.meta.shotDate.strategy.fields}")
    private String shotDateStrategyFieldGroupsStr;
    @Value("${file.media.meta.shotDate.minYear}")
    private int minValidYear;

    private List<List<DateRule>> shotDateStrategyFieldGroups;
    private List<String> allowedFields = new ArrayList<>();

    @PostConstruct
    public void init() {
        parseShotDateStrategyFields();
    }

    private void parseShotDateStrategyFields() {
        try {
            shotDateStrategyFieldGroups = JsonUtils.toObj(shotDateStrategyFieldGroupsStr, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new PhotoMementoError(String.format("There was an error parsing {file.media.meta.shotDate.strategy.fields} property, error is: %s", e.getMessage()), e);
        }

        if (shotDateStrategyFieldGroups.isEmpty())
            throw new PhotoMementoError("Empty date resolution rules in {file.media.meta.shotDate.strategy.fields} property, this is invalid");

        shotDateStrategyFieldGroups.forEach(group -> group.forEach(rule -> allowedFields.add(rule.getF())));
    }

    public void parseMainDate(Media media) {
        Map<String, Instant> detectedDatesMap = detectDatesFromMedia(media);
        for (List<DateRule> shotDateStrategyFieldGroup : shotDateStrategyFieldGroups) {
            Instant detectedDate = detectedDatesMap.entrySet().stream()
                    .filter(entry ->
                            shotDateStrategyFieldGroup.stream()
                                    .anyMatch(dateRule -> {
                                        if (!dateRule.getF().equals(entry.getKey())) return false;
                                        return dateRule.getMax() == null || dateRule.getMax() <= 0 || dateRule.getMax() >= DateUtils.getYear(entry.getValue());
                                    }))
                    .peek(entry -> log.trace(String.format("Date detection for file %s, one of the final candidates is \"%s\":\"%s\"", media.getPath(), entry.getKey(), entry.getValue()))) //NOSONAR
                    .map(Map.Entry::getValue)
                    .reduce(null, DateUtils::getMostAccurateOldestDate);

            if (detectedDate != null) {
                log.info(String.format("Main date for %s, date chosen is: %s, from a list of valid dates (%s)", media.getPath(), detectedDate, JsonUtils.toJson(detectedDatesMap)));
                media.setShotDate(detectedDate);
                break;
            }
        }
    }

    private Map<String, Instant> detectDatesFromMedia(Media media) {
        HashMap<String, Instant> detectedDates = new HashMap<>();

        //Creation date
        try {
            Optional.ofNullable(getFileTime(media, CREATION_TIME))
                    .ifPresent(date -> detectedDates.put(CREATION_TIME, date));
        } catch (IOException e) {
            throw new PhotoMementoError(String.format("Error resolving creation date, due to: %s", e.getMessage()), e);
        }
        //Last modification date
        try {
            Optional.ofNullable(getFileTime(media, LAST_MODIFIED_TIME))
                    .ifPresent(date -> detectedDates.put(LAST_MODIFIED_TIME, date));
        } catch (IOException e) {
            throw new PhotoMementoError(String.format("Error resolving last modification date, due to: %s", e.getMessage()), e);
        }
        //Date in name?
        Optional.ofNullable(DateUtils.detectDate(media.getName()))
                .ifPresent(date -> detectedDates.put(DATE_IN_FILENAME, date));

        //Detect more dates in metadata
        Optional.ofNullable(media.getMetadata())
                .ifPresent(metas -> metas.stream()
                        .filter(this::isValidDateMetadata)
                        .forEach(meta -> {
                            String name = meta.getName();
                            Optional.ofNullable(DateUtils.detectDate(meta.getValue()))
                                    .ifPresent(date -> detectedDates.put(name, date));
                        }));

        return detectedDates
                .entrySet().stream()
                .filter(entry -> entry.getKey().equals(DATE_IN_FILENAME) || DateUtils.getYear(entry.getValue()) > minValidYear)
                .filter(entry -> entry.getValue().compareTo(Instant.now()) < 0) //Date detected should be lower than today else something weird happened
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Instant getFileTime(Media media, String key) throws IOException {
        return Optional.ofNullable((FileTime) Files.getAttribute(Path.of(media.getPath()), key))
                .map(FileTime::toInstant)
                .orElse(null);
    }

    private boolean isValidDateMetadata(Metadata meta) {
        final String name = Optional.ofNullable(meta)
                .map(Metadata::getName)
                .map(String::toLowerCase)
                .orElse(null);

        if (!StringUtils.hasText(name) || !name.contains(Constants.DATE_TIME)) return false;

        if (this.allowedFields.stream().noneMatch(field -> field.toLowerCase().equals(name)))
            return false;

        return StringUtils.hasText(meta.getValue());
    }

    @NoArgsConstructor
    @Setter
    @Getter
    private static class DateRule {
        private String f;
        private Integer max;
    }
}
