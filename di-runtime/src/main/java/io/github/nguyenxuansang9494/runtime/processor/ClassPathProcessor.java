package io.github.nguyenxuansang9494.runtime.processor;

import io.github.nguyenxuansang9494.runtime.exception.ClassNotFoundRuntimeException;
import io.github.nguyenxuansang9494.runtime.exception.InvalidClassPathException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
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

    private List<String> findAllFiles(File classPath, File directory, String[] packages) {
        List<String> classes = new LinkedList<>();
        File[] files = directory.listFiles();
        if (files == null) {
            throw new InvalidClassPathException("ClassPathProcessor.findAllFiles - classpath is not a directory");
        }
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(CLASS_EXTENSION)) {
                String className = getClassName(classPath.getPath(), file.getPath());
                for (String packageName : packages) {
                    if (className.contains(packageName)) {
                        classes.add(className);
                        break;
                    }
                }
            }
            else if (file.isDirectory()) {
                classes.addAll(findAllFiles(classPath, file, packages));
            }
        }
        return classes;
    }

    private List<String> findAllClassesName(File classPath, String[] packages) {
        if (classPath.isDirectory()) {
            return findAllFiles(classPath, classPath, packages);
        }
        return findAllClassNameInJar(classPath, packages);
    }

    private String getClassName(String classPath, String filePath) {
        String className = filePath.substring(classPath.length() + 1, filePath.indexOf(CLASS_EXTENSION));
        className = className.replace(File.separatorChar, '.');
        return className;
    }

    public List<Class<?>> scanAllClasses(Class<?> clazz, String[] packages) {
        final String WHITESPACE = "%20";
        String classPathStr = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
        classPathStr = classPathStr.replace(WHITESPACE, " ");
        File classPath = new File(classPathStr);
        List<String> classNames = findAllClassesName(classPath, packages);
        List<Class<?>> classes = new LinkedList<>();
        for (String className: classNames) {
            try {
                classes.add(Class.forName(className));
            } catch (ClassNotFoundException e) {
                throw new ClassNotFoundRuntimeException(e);
            }
        }
        return classes;
    }

    public List<String> findAllClassNameInJar(File classPath, String[] packages) {
        List<String> classes = new LinkedList<>();
        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(classPath.toPath()))) {
            for (ZipEntry zipEntry = zipInputStream.getNextEntry(); zipEntry != null; zipEntry = zipInputStream.getNextEntry()) {
                if (zipEntry.getName().endsWith(CLASS_EXTENSION) && !zipEntry.isDirectory()) {
                    String className = zipEntry.getName().replace(File.separatorChar, '.');
                    className = className.substring(0, className.length() - CLASS_EXTENSION.length());
                    for (String packageName : packages) {
                        if (className.contains(packageName)) {
                            classes.add(className);
                            break;
                        }
                    }
                }
            }
            return classes;
        } catch (IOException e) {
            throw new InvalidClassPathException(e);
        }
    }
}
