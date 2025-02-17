package org.Finite.ModuleManager;

import org.Finite.common;
import org.Finite.debug.*;
import org.Finite.ArgumentParser;
import org.Finite.interp;
import org.Finite.ReadResourceFile;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.lang.reflect.*;
import org.tomlj.Toml;
import java.net.URLClassLoader;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.Finite.ModuleManager.annotations.MNIClass;
import org.Finite.ModuleManager.annotations.MNIFunction;

/*
    * Module init is a class that is responsible for initializing the modules that are loaded into the interpreter.
    * init handles things like starting the actual ModuleInstaner and calling it for things such as loading from a directory,
    * or loading from a jar file, or even loading from a remote server.
 */


public class ModuleInit {
    private static final Logger logger = LoggerFactory.getLogger(ModuleInit.class);
    private static final ModuleRegistry registry = ModuleRegistry.getInstance();
    private static final String DEFAULT_MODULE_DIR = "modules"; // Default directory for modules

    public static void init() {
        try {
            logger.info("Initializing module system");
            String moduleDir = getModuleDirectory();
            logger.debug("Using module directory: {}", moduleDir);
            loadModulesFromDirectory(new File(moduleDir));
        } catch (Exception e) {
            logger.error("Failed to initialize module system", e);
            e.printStackTrace();
        }
    }

    private static String getModuleDirectory() {
        // TODO: Read from TOML config
        return DEFAULT_MODULE_DIR;
    }

    private static void loadModulesFromDirectory(File directory) {
        if (!directory.exists() || !directory.isDirectory()) {
            logger.warn("Module directory does not exist, creating: {}", directory);
            directory.mkdirs();
            return;
        }

        File[] jarFiles = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));
        if (jarFiles == null) {
            logger.warn("No jar files found in module directory");
            return;
        }

        logger.info("Found {} potential module files", jarFiles.length);

        for (File jarFile : jarFiles) {
            try {
                loadJarFile(jarFile);
            } catch (Exception e) {
                System.err.println("Failed to load jar: " + jarFile.getName());
                e.printStackTrace();
            }
        }
    }

    private static void loadJarFile(File jarFile) throws Exception {
        logger.debug("Loading jar file: {}", jarFile.getName());
        URL[] urls = { jarFile.toURI().toURL() };
        try (URLClassLoader classLoader = new URLClassLoader(urls);
             JarFile jar = new JarFile(jarFile)) {
            
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.getName().endsWith(".class")) continue;

                // Convert path to class name
                String className = entry.getName().replace('/', '.')
                                                .substring(0, entry.getName().length() - 6);
                try {
                    Class<?> clazz = classLoader.loadClass(className);
                    registerClassMethods(jarFile.getName(), clazz);
                } catch (Exception e) {
                    System.err.println("Failed to load class: " + className);
                }
            }
        }
    }

    private static void registerClassMethods(String jarName, Class<?> clazz) {
        MNIClass mniClass = clazz.getAnnotation(MNIClass.class);
        if (mniClass != null) {
            String moduleName = mniClass.value();
            for (Method method : clazz.getDeclaredMethods()) {
                MNIFunction mniFunction = method.getAnnotation(MNIFunction.class);
                if (mniFunction != null) {
                    registry.registerMNIModule(moduleName, mniFunction.name(), method);
                    logger.debug("Registered MNI function: {}.{}", moduleName, mniFunction.name());
                }
            }
        }
        
        // Regular module registration
        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(MNIFunction.class)) {
                registry.registerModule(jarName, clazz.getName(), method.getName(), method);
            }
        }
    }

    // Method to access registered modules at runtime
    public static Method getModuleMethod(String jarName, String className, String methodName) {
        return registry.getMethod(jarName, className, methodName);
    }

    public static void registerBuiltInModule(Class<?> moduleClass) {
        try {
            logger.info("Registering built-in module: {}", moduleClass.getName());
            registerClassMethods("built-in", moduleClass);
        } catch (Exception e) {
            logger.error("Failed to register built-in module: {}", moduleClass.getName(), e);
        }
    }
}