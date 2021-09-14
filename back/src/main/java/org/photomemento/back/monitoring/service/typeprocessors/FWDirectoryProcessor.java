package org.photomemento.back.monitoring.service.typeprocessors;

import lombok.extern.slf4j.Slf4j;
import org.photomemento.back.domain.entity.HierarchyDir;
import org.photomemento.back.domain.entity.file.Directory;
import org.photomemento.back.monitoring.adapter.HierarchyDirAdapter;
import org.photomemento.back.repository.directory.DirectoryRepository;
import org.photomemento.back.types.enu.FILE_TYPE;
import org.photomemento.back.util.IdUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class FWDirectoryProcessor implements IFileWatcherProcessor {
    private final DirectoryRepository directoryRepository;
    private final HierarchyDirAdapter hierarchyDirAdapter;

    public FWDirectoryProcessor(
            HierarchyDirAdapter hierarchyDirAdapter,
            DirectoryRepository directoryRepository) {
        this.hierarchyDirAdapter = hierarchyDirAdapter;
        this.directoryRepository = directoryRepository;
    }

    @Override
    public void onModified(File file, FILE_TYPE type) {
        //Unused
    }

    @Override
    public void onCreated(File f, FILE_TYPE type) {
        final String absPath = f.getAbsolutePath();
        final String parentAbsPath = f.getParent();
        final String parentId = parentAbsPath.hashCode() + "";

        //Resolve parent dir hierarchy
        final Optional<Directory> optParent = directoryRepository.findById(parentId);
        List<HierarchyDir> hierarchy = null;
        if (optParent.isPresent()) {
            Directory parent = optParent.get();
            hierarchy = parent.getHierarchy();
            if (CollectionUtils.isEmpty(hierarchy))
                hierarchy = new ArrayList<>();
            hierarchy.add(hierarchyDirAdapter.fromDir(parent));
        }

        //Generate the dir obj
        Directory directory = Directory
                .builder()
                .id(IdUtils.getIdFromAbsPath(absPath))
                .path(absPath)
                .parentHash(parentId)
                .parent(parentAbsPath)
                .name(f.getName())
                .hierarchy(hierarchy)
                .build();
        directoryRepository.save(directory);
        log.debug(String.format("Storing directory on db: %s", directory.getPath()));
    }

    @Override
    public void onDeleted(File file, FILE_TYPE type) {
        directoryRepository.deleteRecursivelyByPath(file.getAbsolutePath());
    }

    @Override
    public void onVisit(File f, FILE_TYPE type, boolean reprocess) {
        if (!directoryRepository.existsByPath(f.getAbsolutePath()) || reprocess)
            onCreated(f, type);
    }
}
