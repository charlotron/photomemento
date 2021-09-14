package org.photomemento.back.repository.directory;

import org.photomemento.back.domain.entity.file.Directory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DirectoryRepository extends MongoRepository<Directory, String>, IDirectoryRepository {
    Page<Directory> findAllByOrderByPathAsc(Pageable pageable);
    boolean existsByPath(String path);
    Page<List<Directory>> findByParentHashOrderByPathAsc(String directoryId, Pageable pageable);
}