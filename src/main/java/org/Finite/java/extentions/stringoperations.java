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

    

}