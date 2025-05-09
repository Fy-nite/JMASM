package org.finite.Comp;

// Use static imports for the methods we need
import static org.bytedeco.llvm.global.LLVM.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

// These are the types we'll use
import org.bytedeco.llvm.LLVM.LLVMContextRef;
import org.bytedeco.llvm.LLVM.LLVMModuleRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;
import org.bytedeco.llvm.LLVM.LLVMBasicBlockRef;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMTypeRef;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.PointerPointer;

public class LLMASM {

    public static void start() {
        // Extract and load native libraries from the JAR
        try {
            loadNativeLibrary("nativelibs/jniLLVM");
            loadNativeLibrary("nativelibs/libRemarks");
            loadNativeLibrary("nativelibs/libRemarks.so.19.1");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load native library", e);
        }

        // Initialize LLVM
        LLVMInitializeNativeTarget();
        LLVMInitializeNativeAsmPrinter();

        // Create a new LLVM context
        LLVMContextRef context = LLVMContextCreate();
        
        // Create a new LLVM module
        LLVMModuleRef module = LLVMModuleCreateWithNameInContext("my_module", context);
        
        // Create a proper PointerPointer for parameter types
        PointerPointer<LLVMTypeRef> paramTypes = new PointerPointer<>(0); // empty array
        
        // create a simple demonstration function with correctly typed arguments
        LLVMValueRef function = LLVMAddFunction(module, "my_function", 
                LLVMFunctionType(LLVMInt32TypeInContext(context), paramTypes, 0, 0));
        
        // Create a basic block
        LLVMBasicBlockRef block = LLVMAppendBasicBlockInContext(context, function, "entry");
        
        // Create an LLVM IR builder
        LLVMBuilderRef builder = LLVMCreateBuilderInContext(context);
        
        // Set the insertion point to the end of the block
        LLVMPositionBuilderAtEnd(builder, block);
        
        // Create a return instruction
        LLVMValueRef retVal = LLVMConstInt(LLVMInt32TypeInContext(context), 0, 0);
        LLVMBuildRet(builder, retVal);
        
        // Print the module to file with proper error handling
        BytePointer error = new BytePointer((BytePointer)null);
        LLVMPrintModuleToFile(module, "my_module.ll", error);
        
        // Clean up resources
        LLVMDisposeBuilder(builder);
        LLVMDisposeModule(module);
        LLVMContextDispose(context);
    }

    private static void loadNativeLibrary(String baseName) throws IOException {
        String libName = getPlatformSpecificLibraryName(baseName);
        File tempLib = extractLibrary(libName);
        System.load(tempLib.getAbsolutePath());
    }

    private static String getPlatformSpecificLibraryName(String baseName) {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("windows")) {
            return baseName + ".dll";
        } else if (osName.contains("mac")) {
            return baseName + ".dylib";
        } else {
            return baseName + ".so";
        }
    }

    private static File extractLibrary(String libName) throws IOException {
        InputStream libStream = LLMASM.class.getClassLoader().getResourceAsStream(libName);
        if (libStream == null) {
            throw new IOException("Library " + libName + " not found in JAR");
        }

        String extension = libName.substring(libName.lastIndexOf('.'));
        File tempFile = File.createTempFile("native", extension);
        tempFile.deleteOnExit();

        // Ensure the file is written correctly
        try (libStream) {
            Files.copy(libStream, tempFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }

        return tempFile;
    }
}