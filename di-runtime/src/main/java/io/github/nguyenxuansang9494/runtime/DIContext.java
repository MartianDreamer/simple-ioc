package io.github.nguyenxuansang9494.runtime;

import java.util.List;

public interface DIContext {

    public  List<Object> getChildrenClassComponent(Class<?> clazz);
    public List<Object> getComponents(Class<?> clazz);
    public void registerComponent(Class<?> clazz, Object component);
}
