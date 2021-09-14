package org.photomemento.back.util;

import org.photomemento.back.exceptions.InvalidStateError;
import org.springframework.util.StringUtils;

import java.io.File;

public class IdUtils {

    private IdUtils() {
        throw new InvalidStateError("Should not be used");
    }

    public static String getIdFromFile(File f){
        return getIdFromAbsPath(f.getAbsolutePath());
    }

    public static String getIdFromAbsPath(String absolutePath){
        return StringUtils.hasText(absolutePath)?
                absolutePath.hashCode()+"":
                null;
    }
}
