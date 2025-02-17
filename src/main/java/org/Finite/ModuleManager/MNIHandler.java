package org.Finite.ModuleManager;

import org.Finite.common;
import java.lang.reflect.Method;

public class MNIHandler {
    private static final ModuleRegistry registry = ModuleRegistry.getInstance();

    public static void handleMNICall(String moduleName, String functionName, MNIMethodObject methodObj) {
        try {
            Method method = registry.getMNIMethod(moduleName, functionName);
            if (method == null) {
                throw new RuntimeException("MNI function not found: " + moduleName + "." + functionName);
            }
            method.invoke(null, methodObj);
        } catch (Exception e) {
            common.printerr("MNI call failed: " + e.getMessage());
        }
    }
}
