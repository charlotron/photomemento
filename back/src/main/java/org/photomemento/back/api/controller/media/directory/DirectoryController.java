package org.photomemento.back.api.controller.media.directory;

import com.drew.lang.annotations.NotNull;
import org.photomemento.back.api.controller.ApiController;
import org.photomemento.back.domain.entity.file.Directory;
import org.photomemento.back.domain.entity.file.Media;
import org.photomemento.back.monitoring.provider.FileWatcherProvider;
import org.photomemento.back.repository.directory.DirectoryRepository;
import org.photomemento.back.repository.media.MediaRepository;
import org.photomemento.back.types.Constants;
import org.photomemento.back.types.apiresponse.ApiResponse;
import org.photomemento.back.types.enu.FILE_TYPE;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static org.photomemento.back.api.controller.manage.ManageController.REPROCESS_STARTED;
import static org.photomemento.back.types.Constants.*;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(path = ABS_MEDIA_DIR)
public class DirectoryController extends ApiController {
    private final MediaRepository mediaRepository;
    private final DirectoryRepository directoryRepository;
    private final FileWatcherProvider fileWatcherProvider;

    public DirectoryController(
            MediaRepository mediaRepository,
            DirectoryRepository directoryRepository,
            FileWatcherProvider fileWatcherProvider) {
        super();
        this.mediaRepository = mediaRepository;
        this.directoryRepository = directoryRepository;
        this.fileWatcherProvider=fileWatcherProvider;
    }

    @GetMapping(path = ABS_ROOT)
    public Page<Directory> listAllDirs(@NotNull final Pageable pageable) {
        return directoryRepository.findAllByOrderByPathAsc(pageable);
    }

    @GetMapping(path = REL_BY_ID)
    public Optional<Directory> getDirById(@PathVariable(Constants.ID) String directoryId) {
        return directoryRepository.findById(directoryId);
    }

    @GetMapping(path = REL_DIR_CHILDS)
    public Page<List<Directory>> listChilds(@PathVariable(Constants.ID) String directoryId, @NotNull final Pageable pageable) {
        return directoryRepository.findByParentHashOrderByPathAsc(directoryId, pageable);
    }

    @GetMapping(path = REL_DIR_MEDIA)
    public Page<Media> listDirMedia(@PathVariable(Constants.ID) String directoryId, @NotNull final Pageable pageable) {
        return mediaRepository.findAllByHasMediaTrueAndParentHashOrderByShotDateDesc(directoryId, pageable);
    }


    @GetMapping(path = REL_DIR_REPROCESS)
    public ApiResponse<String> reprocessFilesForDir(@PathVariable(Constants.ID) String directoryId, @RequestParam(value = "type",required = false) String typeStr) {
        Optional<Directory> dir = directoryRepository.findById(directoryId);
        if (dir.isEmpty()) return null;
        return executeThenRespond(
                () -> fileWatcherProvider.checkFiles(dir.get().getFile().toPath(),true, true, FILE_TYPE.get(typeStr)),
                REPROCESS_STARTED);
    }
}
