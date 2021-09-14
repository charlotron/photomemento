package org.photomemento.back.monitoring.service;

import org.photomemento.back.domain.entity.file.Directory;
import org.photomemento.back.domain.entity.file.Media;
import org.photomemento.back.repository.directory.DirectoryRepository;
import org.photomemento.back.repository.media.MediaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class ManageService {
    private final MediaRepository mediaRepository;
    private final FileWatcherService fws;
    private final DirectoryRepository directoryRepository;

    public ManageService(
            MediaRepository mediaRepository,
            DirectoryRepository directoryRepository,
            FileWatcherService fws) {
        this.mediaRepository = mediaRepository;
        this.directoryRepository = directoryRepository;
        this.fws = fws;
    }

    //TODO: THIS SHOULD BE IMPROVED
    public void checkIntegrity() {
        Pageable pageable = Pageable.ofSize(200);

        while (true) { //NOSONAR
            Page<Media> page = this.mediaRepository.findAllByHasMediaTrueOrderByShotDateDesc(pageable);
            if (page.hasContent()) {
                List<Media> listMedia = page.getContent();
                listMedia.forEach(media -> {
                    File f = new File(media.getPath());
                    if (!f.exists())
                        fws.onDeleted(f, null);
                });
            }

            if (page.hasNext())
                pageable = page.nextPageable();
            else break;
        }

        while (true) { //NOSONAR
            Page<Directory> page = this.directoryRepository.findAllByOrderByPathAsc(pageable);
            if (page.hasContent()) {
                List<Directory> listMedia = page.getContent();
                listMedia.forEach(media -> {
                    File f = new File(media.getPath());
                    if (!f.exists())
                        fws.onDeleted(f, null);
                });
            }

            if (page.hasNext())
                pageable = page.nextPageable();
            else break;
        }
    }

}
