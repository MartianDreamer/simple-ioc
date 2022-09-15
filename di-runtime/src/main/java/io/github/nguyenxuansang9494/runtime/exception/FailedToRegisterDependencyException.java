package io.github.nguyenxuansang9494.runtime.exception;

public class FailedToRegisterDependencyException extends RuntimeException {

    public FailedToRegisterDependencyException() {
    }

    public FailedToRegisterDependencyException(String message) {
        super(message);
    }

    public FailedToRegisterDependencyException(Throwable cause) {
        super(cause);
    }

    public FailedToRegisterDependencyException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailedToRegisterDependencyException(String message, Throwable cause, boolean enableSuppression,
                                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
