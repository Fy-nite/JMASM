package org.finite.Modules.extentions;

import org.finite.ModuleManager.*;
import org.finite.ModuleManager.annotations.*;


@MNIClass("StringOperations")
public class StringOperations {

    @MNIFunction(name = "cmp", module = "StringOperations")
    public static void cmp(MNIMethodObject obj)
    {
        String st1 = obj.readString(obj.arg1);
        String st2 = obj.readString(obj.arg2);

        if (st1.equals(st2))
        {
            obj.setRegister("RFLAGS",1);
        }
        else{
            obj.setRegister("RFLAGS", 0);
        }

    }
    @MNIFunction(name = "concat", module = "StringOperations")
    public static void concat(MNIMethodObject obj)
    {
        String st1 = obj.readString(obj.args[0]);
        String st2 = obj.readString(obj.args[1]);

        obj.writeString(obj.args[2], st1 + st2);


    }
    @MNIFunction(name = "len", module = "StringOperations")
    public static void length(MNIMethodObject obj)
    {
        int reg;
        String st1;
        // check if the first argument is a register
        if (obj.reg1.startsWith("R"))
        {
            reg = obj.getRegister(obj.reg1);
            st1 = Integer.toString(reg);
        }
        else
        {
            st1 = obj.readString(obj.arg1);
        }
        obj.setRegister(obj.reg2, st1.length());
            

    }
    //     MNI StringOperations.split R0 R1 $5000 ; Split result into array at $5000
    @MNIFunction(name = "concat", module = "StringOperations")
    public static void split(MNIMethodObject obj)
    {
        String st1 = obj.readString(obj.arg1);
        String[] arr = st1.split(obj.readString(obj.arg2));
        for (int i = 0; i < arr.length; i++)
        {
            obj.writeString(obj.args[2] + i, arr[i]);
        }

    }
    @MNIFunction(name = "replace", module = "StringOperations")
    public static void replace(MNIMethodObject obj)
    {
        String st1 = obj.readString(obj.args[0]);
        String st2 = obj.readString(obj.args[1]);
        String st3 = obj.readString(obj.args[2]);

        obj.writeString(obj.args[3], st1.replace(st2, st3));
        

    }
}