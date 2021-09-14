package org.photomemento.back.api.controller.media.search;

import com.drew.lang.annotations.NotNull;
import org.photomemento.back.api.controller.media.ParentMediaController;
import org.photomemento.back.domain.entity.file.Media;
import org.photomemento.back.repository.media.MediaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.photomemento.back.types.Constants.*;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(path = ABS_SEARCH_DIR)
public class SearchController extends ParentMediaController {

    @Value("${search.stopwords}")
    public String stopWordsStr;
    private List<String> stopWords;

    @PostConstruct
    public void init() {
        stopWords = Arrays.asList(stopWordsStr.split("[ ]+"));
    }

    public SearchController(MediaRepository mediaRepository) {
        super(mediaRepository);
    }

    @GetMapping(path = REL_SEARCH_BY_QUERY)
    public Page<Media> search(@NotNull final Pageable pageable, @PathVariable(QUERY) String query) {
        query = processQuery(query);
        return mediaRepository.searchAndHasMedia(pageable, query);
    }

    private String processQuery(String query) {
        Stream<String> queryParts = Arrays.stream(query.toLowerCase(Locale.ROOT).trim().split("[ ]+")).distinct();
        return queryParts.filter(queryPart->!stopWords.contains(queryPart)).collect(Collectors.joining(" "));
    }
}
