package org.photomemento.back.exceptions;

import org.springframework.util.StringUtils;

public class PhotoMementoError extends RuntimeException {
    private static final String NO_MESSAGE = "<no message>";

    protected PhotoMementoError(String prefix, String message, Throwable e) {
        super(parseMessage(prefix, message), e);
    }

    protected PhotoMementoError(String prefix, String message) {
        super(parseMessage(prefix, message));
    }

    public PhotoMementoError(String message, Throwable e) {
        super(message, e);
    }

    public PhotoMementoError(String message) {
        super(message);
    }

    public PhotoMementoError(Throwable e) {
        super(e);
    }

    public PhotoMementoError() {
        super();
    }

    private static String parseMessage(String prefix, String message) {
        message = StringUtils.hasText(message) ? message : NO_MESSAGE;
        if (StringUtils.hasText(prefix))
            message = prefix + message;
        return message;
    }
}
