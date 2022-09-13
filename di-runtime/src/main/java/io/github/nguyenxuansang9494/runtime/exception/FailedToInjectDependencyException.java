package io.github.nguyenxuansang9494.runtime.exception;

public class FailedToInjectDependencyException extends RuntimeException {

    public FailedToInjectDependencyException() {
    }

    public FailedToInjectDependencyException(String message) {
        super(message);
    }

    public FailedToInjectDependencyException(Throwable cause) {
        super(cause);
    }

    public FailedToInjectDependencyException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailedToInjectDependencyException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
