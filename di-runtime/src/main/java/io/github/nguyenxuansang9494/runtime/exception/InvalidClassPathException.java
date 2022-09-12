package io.github.nguyenxuansang9494.runtime.exception;

public class InvalidClassPathException extends RuntimeException {

    public InvalidClassPathException() {
    }

    public InvalidClassPathException(String message) {
        super(message);
    }

    public InvalidClassPathException(Throwable cause) {
        super(cause);
    }

    public InvalidClassPathException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidClassPathException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
