package org.photomemento.back.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.photomemento.back.exceptions.InvalidStateError;
import org.photomemento.back.exceptions.PhotoMementoError;

public class JsonUtils {

    private static final ObjectMapper jsonMapper = new ObjectMapper()
            .findAndRegisterModules()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    private JsonUtils() {
        throw new InvalidStateError("Should not be used");
    }

    public static <T> T toObj(String json, TypeReference<T> typeReference) {
        try {
            return jsonMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new PhotoMementoError(String.format("There was a problem while converting json to obj, due to: %s, original: %s", e.getMessage(), json), e);
        }
    }

    public static String toJson(Object obj) {
        return toJson(obj, false);
    }

    public static String toJson(Object obj, boolean pretty) {
        try {
            return (pretty ?
                    jsonMapper.writerWithDefaultPrettyPrinter() :
                    jsonMapper.writer())
                    .writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new PhotoMementoError(String.format("There was a problem while converting obj to string, due to: %s", e.getMessage()), e);
        }
    }
}
