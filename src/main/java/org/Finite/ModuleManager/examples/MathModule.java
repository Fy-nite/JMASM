package org.Finite.ModuleManager.examples;

import org.Finite.ModuleManager.annotations.MNIClass;
import org.Finite.ModuleManager.annotations.MNIFunction;
import org.Finite.ModuleManager.MNIMethodObject;

@MNIClass("math")
public class MathModule {
    @MNIFunction(module = "math", name = "add")
    public static void add(MNIMethodObject obj) {
        // Get values using register names
        int value1 = obj.getRegister(obj.reg1);
        int value2 = obj.getRegister(obj.reg2);
        int result = value1 + value2;
        // Store result back in first register
        obj.setRegister(obj.reg1, result);
    }
}
