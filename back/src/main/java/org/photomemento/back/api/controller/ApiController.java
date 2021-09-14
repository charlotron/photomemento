package org.photomemento.back.api.controller;

import org.photomemento.back.types.apiresponse.ApiResponse;

public class ApiController {

    protected ApiResponse<String> executeThenRespond(Runnable runnable, String message){
        runnable.run();
        return ApiResponse
                .<String>builder()
                .data(message)
                .build();
    }
}
