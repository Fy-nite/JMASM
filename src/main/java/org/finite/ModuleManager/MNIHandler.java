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
}
