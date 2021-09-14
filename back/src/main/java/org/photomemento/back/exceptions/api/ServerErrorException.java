package org.photomemento.back.exceptions.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Internal error")
public class ServerErrorException extends RuntimeException {
    public ServerErrorException(String message) {
        super(message);
    }
    public ServerErrorException(String message, Exception ex) {
        super(message, ex);
    }
}