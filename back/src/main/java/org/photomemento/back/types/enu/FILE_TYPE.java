package org.photomemento.back.types.enu;

import org.springframework.util.StringUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Set;

import static org.photomemento.back.types.Constants.FILE_EXT_SEPARATOR;

public enum FILE_TYPE {
    DIRECTORY,
    PHOTO,
    VIDEO;

    private Set<String> availableFileExtensions;

    public void addAvailableExtensions(Set<String> availableFileExtensions) {
        this.availableFileExtensions = availableFileExtensions;
    }

    public boolean matches(File f) {
        if(this==DIRECTORY) return false;

        String absFileName = f.getAbsolutePath();
        if (!absFileName.contains(FILE_EXT_SEPARATOR)) return false;

        String extension = absFileName.substring(absFileName.lastIndexOf(FILE_EXT_SEPARATOR)+1);
        return availableFileExtensions != null && availableFileExtensions.contains(extension);
    }

    public static FILE_TYPE get(String type){
        if(!StringUtils.hasText(type)) return null;
        return Arrays.stream(FILE_TYPE.values())
                .filter(ft->ft.name().equals(type))
                .findFirst()
                .orElse(null);
    }
}
