package org.finite.Modules;

import org.finite.ModuleManager.MNIMethodObject;
import org.finite.ModuleManager.annotations.MNIFunction;

public class stringhelpers {
        @MNIFunction(name = "length", module = "StringOperations")
    public static void length(MNIMethodObject obj)
    {
        String st1 = obj.readString(obj.arg1);
        obj.setRegister(obj.reg2, st1.length());
    }

    @MNIFunction(name = "concat", module = "StringOperations")
    public static void concat(MNIMethodObject obj)
    {
        String st1 = obj.readString(obj.arg1);
        String st2 = obj.readString(obj.arg2);
        obj.writeString(obj.arg1, st1 + st2);
    }

    @MNIFunction(name = "substring", module = "StringOperations")
    public static void substring(MNIMethodObject obj)
    {
        String st1 = obj.readString(obj.arg1);
        int start = obj.arg2;
        int end = obj.args[2];
        obj.writeString(obj.arg1, st1.substring(start, end));
    }

    
}
