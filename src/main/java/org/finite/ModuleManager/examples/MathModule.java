package org.finite.ModuleManager.examples;

import org.finite.ModuleManager.annotations.MNIClass;
import org.finite.ModuleManager.annotations.MNIFunction;
import org.finite.ModuleManager.MNIMethodObject;

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
    @MNIFunction(module = "math", name = "square")
    public static void square(MNIMethodObject obj) {
        // Get value using register name
        int value = obj.getRegister(obj.reg1);
        int result = value * value;
        // Store result back in first register
        obj.setRegister(obj.reg1, result);
    }
}
