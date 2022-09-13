package io.github.nguyenxuansang9494.runtime;

import java.util.List;
import java.util.Map.Entry;

public interface DIContext {

    public List<Entry<Class<?>, List<Object>>> getChildrenClassComponent(Class<?> clazz);
    public List<Object> getComponents(Class<?> clazz);
    public void registerComponent(Class<?> clazz, Object component);
}
