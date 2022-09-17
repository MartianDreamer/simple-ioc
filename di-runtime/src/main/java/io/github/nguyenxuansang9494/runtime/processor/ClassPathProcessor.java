package io.github.nguyenxuansang9494.runtime.processor;

import io.github.nguyenxuansang9494.runtime.exception.ClassNotFoundRuntimeException;
import io.github.nguyenxuansang9494.runtime.exception.InvalidClassPathException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class ClassPathProcessor {
    private static final ClassPathProcessor instance = new ClassPathProcessor();
    private static final String CLASS_EXTENSION = ".class";

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
            if (e.isFile() && e.getName().endsWith(CLASS_EXTENSION))
                result.add(e);
            else if (e.isDirectory())
                result.addAll(findAllFiles(e));
        }
        return result;
    }

    private List<String> findAllClassesName(File classPath) {
        if (classPath.isDirectory()) {
            List<File> files = findAllFiles(classPath);
            return files.stream().map(f -> getClassName(classPath.getPath(), f.getPath())).collect(Collectors.toList());
        }
        return findAllClassNameInJar(classPath);
    }

    private String getClassName(String classPath, String filePath) {
        String className = filePath.substring(classPath.length() + 1, filePath.indexOf(CLASS_EXTENSION));
        className = className.replace(File.separatorChar, '.');
        return className;
    }

    public List<Class<?>> scanAllClasses(Class<?> clazz) {
        final String WHITESPACE = "%20";
        String classPathStr = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
        classPathStr = classPathStr.replace(WHITESPACE, " ");
        File classPath = new File(classPathStr);
        List<String> classNames = findAllClassesName(classPath);
        List<Class<?>> classes = new LinkedList<>();
        String mainClassFullName = clazz.getCanonicalName();
        String basePackage = mainClassFullName.substring(0, mainClassFullName.lastIndexOf("."));
        for (String className: classNames) {
            if (!className.contains(basePackage)) {
                continue;
            }
            try {
                classes.add(Class.forName(className));
            } catch (ClassNotFoundException e) {
                throw new ClassNotFoundRuntimeException(e);
            }
        }
        return classes;
    }

    public List<String> findAllClassNameInJar(File classPath) {
        List<String> classes = new LinkedList<>();
        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(classPath.toPath()))) {
            for (ZipEntry zipEntry = zipInputStream.getNextEntry(); zipEntry != null; zipEntry = zipInputStream.getNextEntry()) {
                if (zipEntry.getName().endsWith(CLASS_EXTENSION) && !zipEntry.isDirectory()) {
                    String className = zipEntry.getName().replace(File.separatorChar, '.');
                    className = className.substring(0, className.length() - CLASS_EXTENSION.length());
                    classes.add(className);
                }
            }
            return classes;
        } catch (IOException e) {
            throw new InvalidClassPathException(e);
        }
    }
}
