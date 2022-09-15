package io.github.nguyenxuansang9494.runtime.processor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ClassProcessor {
    private static final ClassProcessor instance = new ClassProcessor();

    private ClassProcessor() {
        super();
    }

    public static ClassProcessor getInstance() {
        return instance;
    }

    private List<Field> findDeclaredAnnotatedFields(Class<?> clazz, Class<? extends Annotation> annotation) {
        return Stream.of(clazz.getDeclaredFields()).filter(f -> f.getAnnotation(annotation)!=null).collect(Collectors.toList());
    }

    public List<Field> findInheritedAnnotatedFields(Class<?> clazz, Class<? extends Annotation> annotation) {
        Class<?> superClass = clazz.getSuperclass();
        List<Field> annotatedFields = findDeclaredAnnotatedFields(clazz, annotation);
        while (superClass != null && superClass != Object.class) {
            annotatedFields.addAll(findDeclaredAnnotatedFields(superClass, annotation));
            superClass = superClass.getSuperclass();
        }
        return annotatedFields;
    }

    public List<Method> findDeclaredAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annotation) {
        return Stream.of(clazz.getDeclaredMethods()).filter(m -> m.getDeclaredAnnotation(annotation)!=null).collect(Collectors.toList());
    }
}
