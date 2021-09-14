package org.photomemento.back.exceptions;

public class InvalidConfigurationError extends PhotoMementoError{

    private static final String PREFIX="Cannot start due to invalid configuration: ";

    public InvalidConfigurationError(String message, Throwable e){
        super(PREFIX, message,e);
    }
    public InvalidConfigurationError(String message){
        super(PREFIX, message);
    }
}
