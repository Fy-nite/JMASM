package org.finite.ModuleManager;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
//TODO: make this actualy a thing that people can use.
public class ModuleRegistry {
    private static final ModuleRegistry instance = new ModuleRegistry();
    private final Map<String, Map<String, Map<String, Method>>> moduleMap;

    private ModuleRegistry() {
        // Use ConcurrentHashMap for better concurrency
        moduleMap = new ConcurrentHashMap<>();
    }

    public static ModuleRegistry getInstance() {
        return instance;
    }

    public void registerModule(String jarName, String className, String methodName, Method method) {
        moduleMap.computeIfAbsent(jarName, k -> new HashMap<>())
                .computeIfAbsent(className, k -> new HashMap<>())
                .put(methodName, method);
    }

    public Method getMethod(String jarName, String className, String methodName) {
        Map<String, Map<String, Method>> classMap = moduleMap.get(jarName);
        if (classMap == null) return null;
        Map<String, Method> methodMap = classMap.get(className);
        if (methodMap == null) return null;
        return methodMap.get(methodName);
    }

    public void registerMNIModule(String moduleName, String functionName, Method method) {
        registerModule("mni", moduleName, functionName, method);
    }

    public Method getMNIMethod(String moduleName, String functionName) {
        return getMethod("mni", moduleName, functionName);
    }

    public Map<String, Map<String, Map<String, Method>>> getModuleMap() {
        return moduleMap;
    }
}
