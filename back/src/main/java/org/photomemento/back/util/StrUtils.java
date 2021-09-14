package org.photomemento.back.util;

import org.photomemento.back.exceptions.InvalidStateError;
import org.photomemento.back.types.Constants;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StrUtils {

    private StrUtils() {
        throw new InvalidStateError("Should not be used");
    }

    public static Set<String> strListToUniqueStrList(String strList){
        if(!StringUtils.hasText(strList)) return new HashSet<>();
        return Arrays.stream(strList.split(Constants.SEP_STR_LIST))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
    }
    public static Set<Pattern> strListToUniquePatternList(String strList){
        if(!StringUtils.hasText(strList)) return new HashSet<>();
        return Arrays.stream(strList.split(Constants.SEP_STR_LIST))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(Pattern::compile)
                .collect(Collectors.toSet());
    }
}
