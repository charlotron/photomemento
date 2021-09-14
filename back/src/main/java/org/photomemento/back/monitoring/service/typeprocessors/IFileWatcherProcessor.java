package org.photomemento.back.monitoring.service.typeprocessors;

import org.photomemento.back.types.enu.FILE_TYPE;

import java.io.File;

public interface IFileWatcherProcessor {
    void onModified(File file, FILE_TYPE type);
    void onCreated(File file, FILE_TYPE type);
    void onDeleted(File file, FILE_TYPE type);
    void onVisit(File file, FILE_TYPE type, boolean reprocess);
}
