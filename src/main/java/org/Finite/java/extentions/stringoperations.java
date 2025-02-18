package org.Finite.java.extentions;
import static org.Finite.Common.common.print;

import java.io.File;
import org.Finite.Common.common;
import org.Finite.ModuleManager.*;
import org.Finite.ModuleManager.annotations.*;

@MNIClass("StringOperations")
public class stringoperations {

    @MNIFunction(name = "cmp", module = "StringOperations")
    public static void cmp(MNIMethodObject obj)
    {
        String st1 = obj.readString(obj.arg1);
        String st2 = obj.readString(obj.arg2);


        if (st1.equals(st2))
        {
            obj.setRegister("RFLAGS", 1);
        }
        else{
            obj.setRegister("RFLAGS",0);
        }
    }

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