package io.github.nguyenxuansang9494.runtime;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class ClassHelper {

    public Field[] findDeclaredAnnotatedFields(Class<?> clazz, Class<? extends Annotation> annotation) {
        return Stream.of(clazz.getDeclaredFields()).filter(f -> f.getAnnotation(annotation)!=null).toArray(Field[]::new);
    }

    public Field[] findAnnotatedFields(Class<?> clazz, Class<? extends Annotation> annotation) {
        Class<?> superClass = clazz.getSuperclass();
        List<Field> injectAnnotatedFields = new LinkedList<>(Arrays.asList(findDeclaredAnnotatedFields(clazz, annotation)));
        while (superClass != null && superClass != Object.class) {
            injectAnnotatedFields.addAll(Arrays.asList(findDeclaredAnnotatedFields(superClass, annotation)));
            superClass = superClass.getSuperclass();
        }
        return injectAnnotatedFields.toArray(new Field[]{});
    }

    public Method[] findDeclaredAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annotation) {
        return Stream.of(clazz.getDeclaredMethods()).filter(m -> m.getDeclaredAnnotation(annotation)!=null).toArray(Method[]::new);
    }
}
