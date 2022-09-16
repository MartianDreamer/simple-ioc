package io.github.nguyenxuansang9494.runtime.processor;

import io.github.nguyenxuansang9494.runtime.exception.ClassNotFoundRuntimeException;
import io.github.nguyenxuansang9494.runtime.exception.InvalidClassPathException;

import java.io.File;
import java.util.LinkedList;
import java.util.List;


public class ClassPathProcessor {
    private static final ClassPathProcessor instance = new ClassPathProcessor();

    private ClassPathProcessor() {
        super();
    }
    public static ClassPathProcessor getInstance() {
        return instance;
    }

    private List<File> findAllFiles(File classPath) {
        List<File> result = new LinkedList<>();
        File[] files = classPath.listFiles();
        if (files == null) {
            throw new InvalidClassPathException("ClassPathProcessor.findAllFiles - classpath is not a directory");
        }
        for (File e : files) {
            if (e.isFile() && e.getName().endsWith(".class"))
                result.add(e);
            else if (e.isDirectory())
                result.addAll(findAllFiles(e));
        }
        return result;
    }

    private String getClassName(String classPath, String filePath) {
        String className = filePath.substring(classPath.length()+1, filePath.indexOf(".class"));
        className = className.replace(File.separatorChar, '.');
        return className;
    }

    public List<Class<?>> scanAllClasses(Class<?> clazz) {
        final String WHITESPACE = "%20";
        String classPathStr = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
        classPathStr = classPathStr.replace(WHITESPACE, " ");
        File classPath = new File(classPathStr);
        List<File> files = findAllFiles(classPath);
        List<Class<?>> classes = new LinkedList<>();
        for (File file : files) {
            String className = getClassName(classPath.getPath(), file.getPath());
            try {
                classes.add(Class.forName(className));
            } catch (ClassNotFoundException e) {
                throw new ClassNotFoundRuntimeException(e);
            }
        }
        return classes;
    }
}
