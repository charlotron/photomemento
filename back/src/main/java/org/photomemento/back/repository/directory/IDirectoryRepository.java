package org.photomemento.back.repository.directory;

public interface IDirectoryRepository {
    void deleteRecursivelyById(String id);
    void deleteRecursivelyByPath(String path);
}
