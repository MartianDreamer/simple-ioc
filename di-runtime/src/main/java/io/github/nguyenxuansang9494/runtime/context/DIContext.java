package io.github.nguyenxuansang9494.runtime.context;

import java.util.List;

public interface DIContext {

    List<Object> getChildrenClassComponent(Class<?> clazz);
    List<Object> getComponents(Class<?> clazz);
    void registerComponent(Class<?> clazz, Object component);
    void registerExecutor(MethodExecutor methodExecutor);
    void executeRunners();
    <T> T getComponent(Class<T> clazz);
}
