package org.photomemento.back.types;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.photomemento.back.monitoring.service.typeprocessors.IFileWatcherProcessor;
import org.photomemento.back.types.enu.FILE_TYPE;

import java.io.File;

@Builder
@Getter
@Setter
public class OnEventProcess implements Comparable<OnEventProcess> {
    @Override
    public int compareTo(OnEventProcess o) {
        return o == null ?
                1 :
                getFile().compareTo(o.getFile());
    }

    public enum EVENT_TYPE {CREATE, MODIFY, DELETE, VISIT, VISIT_REPROCESS}

    File file;
    FILE_TYPE type;
    IFileWatcherProcessor processor;
    EVENT_TYPE eventType;
    long previousSize;

    @Override
    public String toString() {
        return file.getAbsolutePath()+" ("+eventType.name()+")";
    }
}
