package org.photomemento.back.repository.directory;

import org.photomemento.back.domain.entity.file.Directory;
import org.photomemento.back.repository.media.MediaRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Repository
public class DirectoryRepositoryImpl implements IDirectoryRepository {
    private final MongoTemplate mongoTemplate;
    private final MediaRepository mediaRepository;

    public DirectoryRepositoryImpl(MongoTemplate mongoTemplate, MediaRepository mediaRepository) {
        this.mongoTemplate = mongoTemplate;
        this.mediaRepository = mediaRepository;
    }

    public void deleteRecursivelyById(String id) {
        Directory directory = mongoTemplate.findById(id, Directory.class);
        if (directory != null)
            deleteRecursively(directory);
    }

    public void deleteRecursivelyByPath(String path) {
        //List existing (should be only one)
        Directory directory = mongoTemplate.findOne(new Query(where("path").is(path)), Directory.class);
        if (directory != null)
            deleteRecursively(directory);
    }

    private void deleteRecursively(Directory directory) {
        //Execute for every directory with parent
        List<Directory> children = mongoTemplate.find(new Query(where("parent").is(directory.getParent())), Directory.class);
        children.forEach(this::deleteRecursively);
        mediaRepository.deleteByParent(directory.getPath());
        mongoTemplate.remove(directory);
    }
}
