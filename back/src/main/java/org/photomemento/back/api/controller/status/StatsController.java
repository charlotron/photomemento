package org.photomemento.back.api.controller.status;

import org.photomemento.back.adapter.StatsAdapter;
import org.photomemento.back.types.Stats;
import org.springframework.web.bind.annotation.*;

import static org.photomemento.back.types.Constants.ABS_ROOT;
import static org.photomemento.back.types.Constants.ABS_STATS;

@RestController
@RequestMapping(path = ABS_STATS)
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class StatsController {

    private final StatsAdapter statsAdapter;

    public StatsController(StatsAdapter statsAdapter) {
        this.statsAdapter = statsAdapter;
    }

    @GetMapping(path = ABS_ROOT)
    public Stats getStats(@RequestParam(required = false) boolean withThreads) {
        return statsAdapter.newStats(withThreads);
    }
}
