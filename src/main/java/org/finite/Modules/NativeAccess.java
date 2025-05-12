package org.finite.Modules;
// import JNA
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;

public class NativeAccess {
    // JNA interface for C library
    public interface CLibrary extends Library {
        int printf(String format, Object... args);
    }

    private static final CLibrary cLib;

    static {
        String libName;
        if (Platform.isWindows()) {
            libName = "msvcrt";
        } else {
            libName = "c";
        }
        cLib = Native.load(libName, CLibrary.class);
    }

    public static int printf(String format, Object... args) {
        return cLib.printf(format, args);
    }
}
