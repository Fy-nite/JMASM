package org.finite.ModuleManager;

import org.finite.common;
import org.finite.Exceptions.MASMException;
import org.finite.Exceptions.MNIException;

import java.lang.reflect.Method;

public class MNIHandler {
    private static final ModuleRegistry registry = ModuleRegistry.getInstance();

    public static void handleMNICall(String moduleName, String functionName, MNIMethodObject methodObj) throws MNIException {
        try {
            Method method = registry.getMNIMethod(moduleName, functionName);
            if (method == null) {
                throw new MASMException("MNI method not found", 0, functionName, "MNI method not found");
            }
            method.invoke(null, methodObj);
        } catch (Exception e) {
            throw new MNIException("Error calling MNI method", moduleName, functionName);
        }
    }

    // Call a string-returning function from a custom library
    public static String callCustomLibFunction(String libName, String functionName, Object... args) throws Exception {
        Class<?> clazz = registry.getCustomLib(libName);
        if (clazz == null) throw new RuntimeException("Custom lib not found: " + libName);
        java.lang.reflect.Method method = clazz.getMethod(functionName, toClassArray(args));
        Object result = method.invoke(null, args);
        if (result == null) return null;
        return result.toString();
    }

    private static Class<?>[] toClassArray(Object[] args) {
        if (args == null) return new Class<?>[0];
        Class<?>[] arr = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            arr[i] = args[i] == null ? Object.class : args[i].getClass();
        }
        return arr;
    }
}
