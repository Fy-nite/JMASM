package org.Finite.java.extentions;
import java.io.File;
import org.Finite.*;
import org.Finite.Common.common;
import org.Finite.ModuleManager.*;
import org.Finite.ModuleManager.annotations.*;


// MNI StringOperations.cmp 100 200
// checks the string locations at 100 and 200 and sees if they are equal
@MNIClass("StringOperations")
public class stringoperations {

    @MNIFunction(module = "StringOperations", name = "cmp")
    public static void cmp(MNIMethodObject obj) {
        String str1 = "";
        String str2 = "";
        int address1 = obj.arg1;
        int address2 = obj.arg2;
        char c1 = ' ';
        char c2 = ' ';
        while ((c1 = (char) obj.readMemory(address1)) != 0) {
            str1 += c1;
            address1++;
        }
        while ((c2 = (char) obj.readMemory(address2)) != 0) {
            str2 += c2;
            address2++;
        }
        common.print("Comparing " + str1 + " and " + str2);
        if (str1.equals(str2)) {
            obj.setRegister("RFLAGS", 1);
        } else {
            obj.setRegister("RFLAGS", 0);
        }

    }

}
