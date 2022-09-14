package io.github.nguyenxuansang9494.runtime;

import java.util.List;

public interface DIContext {

    List<Object> getChildrenClassComponent(Class<?> clazz);
    List<Object> getComponents(Class<?> clazz);
    void registerComponent(Class<?> clazz, Object component);
    <T> T getComponent(Class<T> clazz);
}
