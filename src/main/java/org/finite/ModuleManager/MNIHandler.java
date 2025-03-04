package org.finite.ModuleManager;

import org.finite.Common.common;
import org.finite.Exceptions.MASMException;

import java.lang.reflect.Method;

public class MNIHandler {
    private static final ModuleRegistry registry = ModuleRegistry.getInstance();

    public static void handleMNICall(String moduleName, String functionName, MNIMethodObject methodObj) {
        try {
            Method method = registry.getMNIMethod(moduleName, functionName);
            if (method == null) {
                throw new MASMException("MNI method not found", 0, functionName, "MNI method not found");
            }
            method.invoke(null, methodObj);
        } catch (Exception e) {
            common.printerr("MNI call failed: " + e.getMessage());
        }
    }
}
