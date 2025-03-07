package org.finite.Modules.extentions;

import org.finite.Functions;
import org.finite.ModuleManager.MNIMethodObject;
import org.finite.ModuleManager.annotations.MNIClass;
import org.finite.ModuleManager.annotations.MNIFunction;
import org.finite.Parsing;
@MNIClass("IO")
public class IO {
    @MNIFunction(name = "write", module = "IO")
    public static void write(MNIMethodObject obj)
    {
        if (obj.arg1 == 1) {
            String contents = obj.readString(obj.arg2);
            String output = Parsing.parseAnsiTerminal(contents);
            System.out.print(output);
        } else {
            // printerr
            String contents = obj.readString(obj.arg2);
            String output = Parsing.parseAnsiTerminal(contents);
            System.err.print(output);
        }
    }

    @MNIFunction(name = "flush", module = "IO")
    public static void flush(MNIMethodObject obj)
    {
        if (obj.arg1 == 1) {
            System.out.flush();
        } else {
            // printerr
            System.err.flush();
        }
    }
}
