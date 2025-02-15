package org.Finite.ModuleManager;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
//TODO: make this actualy a thing that people can use.
public class ModuleRegistry {
    private static final ModuleRegistry instance = new ModuleRegistry();
    private final Map<String, Map<String, Map<String, Method>>> moduleMap;

    private ModuleRegistry() {
        moduleMap = new HashMap<>();
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
        return moduleMap.getOrDefault(jarName, new HashMap<>())
                .getOrDefault(className, new HashMap<>())
                .get(methodName);
    }

    public Map<String, Map<String, Map<String, Method>>> getModuleMap() {
        return moduleMap;
    }
}
