package io.github.nguyenxuansang9494.runtime.exception;

/**
 * UnsupportedClass
 */
public class UnsupportedClass extends RuntimeException {

    public UnsupportedClass() {
    }

    public UnsupportedClass(String message) {
        super(message);
    }

    public UnsupportedClass(Throwable cause) {
        super(cause);
    }

    public UnsupportedClass(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedClass(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
