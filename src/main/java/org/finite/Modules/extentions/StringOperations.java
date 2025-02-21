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
}