package org.finite.Modules.extentions;

import org.finite.ModuleManager.MNIMethodObject;
import org.finite.ModuleManager.annotations.MNIFunction;
import org.finite.ModuleManager.annotations.MNIClass;
@MNIClass("MNISystem")
public class MNISystem {
    @MNIFunction(name = "exit", module = "MNISystem")
    public static void exit(MNIMethodObject obj) {
        System.exit(0);
    }

    @MNIFunction(name = "halt", module = "MNISystem")
    public static void halt(MNIMethodObject obj) {
        System.out.println("Halted");
     
    }
    @MNIFunction(name = "sleep", module = "MNISystem")
    public static void sleep(MNIMethodObject obj) {
        int milliseconds = obj.arg1; // arg<num> is the value of the register
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }    
}
