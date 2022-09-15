package io.github.nguyenxuansang9494.runtime.model;

import java.lang.annotation.Annotation;

public class ClassWrapper<T> {
    Class<T> tClass;
    Annotation declaredAnnotation;

    @Override
    public int hashCode() {
        return tClass.getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ClassWrapper<?>)) {
            return false;
        }
        return ((ClassWrapper<?>) obj).tClass == this.tClass;
    }
}
