package org.photomemento.back.monitoring.provider;

import org.photomemento.back.types.enu.FILE_TYPE;
import org.photomemento.back.util.StrUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;

@Component
public class FileTypeProvider {

    @Value("${file.media.photo.available.extensions}")
    public String photoAvailableExtensions;
    @Value("${file.media.video.available.extensions}")
    public String videoAvailableExtensions;

    @PostConstruct
    private void initEnums() {
        addAvailableFileExtensions(FILE_TYPE.PHOTO, photoAvailableExtensions);
        addAvailableFileExtensions(FILE_TYPE.VIDEO, videoAvailableExtensions);
    }

    private void addAvailableFileExtensions(FILE_TYPE ft, String availableFileExtensions) {
        ft.addAvailableExtensions(StrUtils.strListToUniqueStrList(availableFileExtensions));
    }

    public FILE_TYPE getType(File f) {
        if (f.isDirectory())
            return FILE_TYPE.DIRECTORY;
        for (FILE_TYPE file_type : FILE_TYPE.values())
            if (file_type.matches(f)) return file_type;
        return null;
    }
}
