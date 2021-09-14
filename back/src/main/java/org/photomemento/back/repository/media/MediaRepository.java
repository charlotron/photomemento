package org.photomemento.back.repository.media;

import org.photomemento.back.domain.entity.file.Media;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface MediaRepository extends MongoRepository<Media, String>, IMediaRepository {
    @Override
    Optional<Media> findById(String id);

    @Query(fields = "{'metadata' : 0, 'status': 0}")
    Page<Media> findAllByHasMediaTrueOrderByShotDateDesc(Pageable pageable);

    @Query(fields = "{ 'metadata' : 0, 'status': 0}")
    Page<Media> findAllByHasMediaTrueAndParentHashOrderByShotDateDesc(String parentHash, Pageable pageable);

    void deleteByParent(String parent);

    Media deleteByPath(String path);

}