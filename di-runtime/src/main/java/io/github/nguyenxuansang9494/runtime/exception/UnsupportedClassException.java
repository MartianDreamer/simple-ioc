package io.github.nguyenxuansang9494.runtime.exception;

/**
 * UnsupportedClass
 */
public class UnsupportedClassException extends RuntimeException {

    public UnsupportedClassException() {
    }

    public UnsupportedClassException(String message) {
        super(message);
    }

    public UnsupportedClassException(Throwable cause) {
        super(cause);
    }

    public UnsupportedClassException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedClassException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
