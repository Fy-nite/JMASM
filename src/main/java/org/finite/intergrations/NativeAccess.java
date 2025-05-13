package org.finite.Modules;

import com.kenai.jffi.Library;
import jnr.*;
import jnr.ffi.LibraryLoader;
import jnr.ffi.Runtime;
import jnr.ffi.annotations.In;

import jnr.ffi.annotations.Out;
import jnr.ffi.annotations.Transient;
import jnr.ffi.Platform;

public class NativeAccess {
    // clib interface variable
    public static CLibrary cLib = null;

    // ffi interface for the heavy rust code
    public static RLibrary rmasm = null;

    // JNA interface for C library
    public interface CLibrary {
        int printf(String format, Object... args);
    }

    // JNR-FFI interface for Rust library
    public interface RLibrary  {
        // Define the functions you want to call from the Rust library
        int output(String format, Object... args);
    }

    static {
        String libName;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            libName = "msvcrt";
        } else {
            libName = "c";
        }
        try {
            cLib = LibraryLoader.create(CLibrary.class).library(libName).load();
            rmasm = LibraryLoader.create(RLibrary.class).library("rmasm").load();
        } catch (Throwable e) {
            System.err.println("Failed to load C library: " + e.getMessage());
        }

    }

}
