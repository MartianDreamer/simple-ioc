package io.github.nguyenxuansang9494.runtime.context.model;

public class OptionalObject {
    Object object;
    boolean isComplete;

    public OptionalObject(Object object, boolean isComplete) {
        this.object = object;
        this.isComplete = isComplete;
    }

    public Object getObject() {
        return object;
    }

    public boolean isComplete() {
        return isComplete;
    }
}
