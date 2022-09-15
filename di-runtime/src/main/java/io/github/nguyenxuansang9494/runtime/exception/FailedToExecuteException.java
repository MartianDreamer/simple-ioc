package io.github.nguyenxuansang9494.runtime.exception;

public class FailedToExecuteException extends RuntimeException {

    public FailedToExecuteException() {
        super();
    }

    public FailedToExecuteException(String message) {
        super(message);
    }

    public FailedToExecuteException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailedToExecuteException(Throwable cause) {
        super(cause);
    }

    protected FailedToExecuteException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
