package io.github.nguyenxuansang9494.runtime.exception;

public class ClassNotFoundRuntimeException extends RuntimeException {

    public ClassNotFoundRuntimeException() {
    }

    public ClassNotFoundRuntimeException(String message) {
        super(message);
    }

    public ClassNotFoundRuntimeException(Throwable cause) {
        super(cause);
    }

    public ClassNotFoundRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClassNotFoundRuntimeException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
