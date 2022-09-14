package io.github.nguyenxuansang9494.runtime;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClassProcessor {

    private List<Field> findDeclaredAnnotatedFields(Class<?> clazz, Class<? extends Annotation> annotation) {
        return Stream.of(clazz.getDeclaredFields()).filter(f -> f.getAnnotation(annotation)!=null).collect(Collectors.toList());
    }

    public int countAllAnnotations(Field[] annotatedFields, Class<? extends Annotation> annotation) {
        int count = annotatedFields.length;
        for (Field field : annotatedFields) {
            Field[] depAnnotatedFields = findAnnotatedFields(field.getType(), annotation);
            count+= depAnnotatedFields.length;
        }
        return count;
    }

    public Field[] findAnnotatedFields(Class<?> clazz, Class<? extends Annotation> annotation) {
        Class<?> superClass = clazz.getSuperclass();
        List<Field> annotatedFields = findDeclaredAnnotatedFields(clazz, annotation);
        while (superClass != null && superClass != Object.class) {
            annotatedFields.addAll(findDeclaredAnnotatedFields(superClass, annotation));
            superClass = superClass.getSuperclass();
        }
        return annotatedFields.toArray(new Field[]{});
    }

    public Method[] findDeclaredAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annotation) {
        return Stream.of(clazz.getDeclaredMethods()).filter(m -> m.getDeclaredAnnotation(annotation)!=null).toArray(Method[]::new);
    }
}
