package org.photomemento.back.api.controller.manage;

import org.photomemento.back.api.controller.ApiController;
import org.photomemento.back.monitoring.provider.FileWatcherProvider;
import org.photomemento.back.monitoring.service.ManageService;
import org.photomemento.back.types.apiresponse.ApiResponse;
import org.photomemento.back.types.enu.FILE_TYPE;
import org.springframework.web.bind.annotation.*;

import static org.photomemento.back.types.Constants.*;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(path = ABS_MANAGE)
public class ManageController extends ApiController {

    public static final String RECHECK_STARTED = "Recheck started!";
    public static final String REPROCESS_STARTED = "Reprocess started!";
    public static final String CHECK_INTEGRITY_STARTED = "Check integrity started!";

    private final FileWatcherProvider fileWatcherProvider;
    private final ManageService manageService;

    public ManageController(
            FileWatcherProvider fileWatcherProvider,
            ManageService manageService) {
        super();
        this.fileWatcherProvider = fileWatcherProvider;
        this.manageService = manageService;
    }

    @GetMapping(path = REL_MANAGE_RECHECK)
    public ApiResponse<String> recheckFiles(@RequestParam(value = "type",required = false) String typeStr) {
        return executeThenRespond(
                () -> fileWatcherProvider.checkFiles(true, false, FILE_TYPE.get(typeStr)),
                RECHECK_STARTED);
    }

    @GetMapping(path = REL_MANAGE_REPROCESS)
    public ApiResponse<String> reprocessFiles(@RequestParam(value = "type",required = false) String typeStr) {
        return executeThenRespond(
                () -> fileWatcherProvider.checkFiles(true, true, FILE_TYPE.get(typeStr)),
                REPROCESS_STARTED);
    }

    @GetMapping(path = REL_MANAGE_CHECK_INTEGRITY)
    public ApiResponse<String> checkIntegrity() {
        return executeThenRespond(
                manageService::checkIntegrity,
                CHECK_INTEGRITY_STARTED);
    }

}
