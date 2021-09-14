package org.photomemento.back.exceptions;

public class InvalidStateError extends PhotoMementoError{

    private static final String PREFIX="Unexpected state: ";

    public InvalidStateError(String message, Throwable e){
        super(PREFIX, message,e);
    }
    public InvalidStateError(String message){
        super(PREFIX, message);
    }
}
