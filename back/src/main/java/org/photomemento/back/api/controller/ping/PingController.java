package org.photomemento.back.api.controller.ping;

import org.photomemento.back.types.apiresponse.ApiResponse;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.photomemento.back.types.Constants.ABS_PING;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(path= ABS_PING)
public class PingController {

    @GetMapping(path = "/")
    public ApiResponse<String> ping(){
        return ApiResponse.<String>builder().data("pong").build();
    }

}
