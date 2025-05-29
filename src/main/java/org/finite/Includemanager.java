package org.finite;

import org.finite.Config.MASMConfig;
import org.finite.Exceptions.IncludeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.finite.*;
import org.finite.ArgumentParser; // Add this import


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// methods
import java.lang.reflect.Method;
import java.util.List;

public class Includemanager {
    private static final Logger log = LoggerFactory.getLogger(Includemanager.class);
    private static final Set<String> includedFiles = new HashSet<>();
    private static final MASMConfig config = MASMConfig.getInstance();
    private static final org.finite.ModuleManager.ModuleRegistry mniRegistry =
        org.finite.ModuleManager.ModuleRegistry.getInstance();

    static {
        // Configure logger level based on debug flag
        
            ((ch.qos.logback.classic.Logger) log).setLevel(ch.qos.logback.classic.Level.INFO);
        
    }

    private enum ImportType {
        STDLIB,     // STDLIB:module
        PATH,       // path/to/file.mas or path/to/file.masm
        DOT_NOTATION, // existing dot.notation
        MODULE_MACRO // modulename:macroname
    }

    public static String include(String filename, String currentFileContents) {
        String cleanFilename = filename.replace("\"", "");
        
        // Parse the import type and get the actual filename/path
        ImportInfo importInfo = parseImportType(cleanFilename);
        
        // Check for custom include provider (annotation-based) - only for non-stdlib imports
        if (importInfo.type != ImportType.STDLIB) {
            Method includeMethod = mniRegistry.getIncludeProvider(cleanFilename);
            if (includeMethod != null) {
                try {
                    Object result = includeMethod.invoke(null);
                    if (result != null) {
                        return result.toString();
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Error calling include provider: " + cleanFilename, e);
                }
            }
        }
        
        // Check for custom macro provider (MNI-based) - for both dot notation and module:macro format
        if (importInfo.type == ImportType.DOT_NOTATION || importInfo.type == ImportType.MODULE_MACRO) {
            String libName;
            String macroName;
            
            if (importInfo.type == ImportType.MODULE_MACRO) {
                // Parse modulename:macroname format
                int colonIdx = cleanFilename.indexOf(':');
                libName = cleanFilename.substring(0, colonIdx);
                macroName = cleanFilename.substring(colonIdx + 1);
            } else {
                // Parse dot notation format
                int dotIdx = cleanFilename.lastIndexOf('.');
                if (dotIdx <= 0) {
                    // No dot found, continue to file reading
                    libName = null;
                    macroName = null;
                } else {
                    libName = cleanFilename.substring(0, dotIdx);
                    macroName = cleanFilename.substring(dotIdx + 1);
                }
            }
            
            if (libName != null && macroName != null) {
                Class<?> macroProvider = mniRegistry.getCustomLib(libName);
                if (macroProvider != null) {
                    try {
                        java.lang.reflect.Method macroMethod = macroProvider.getMethod(macroName);
                        Object macroBody = macroMethod.invoke(null);
                        if (macroBody != null) {
                            return macroBody.toString();
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Error calling macro provider: " + libName + "." + macroName, e);
                    }
                }
            }
        }
        if (includedFiles.contains(cleanFilename)) {
            throw new IncludeException(
                "Circular include detected",
                0,
                cleanFilename,
                "File has already been included"
            );
        }

        try {
            includedFiles.add(cleanFilename);
            String fileContent = readFileContent(importInfo);
            String processed = processIncludes(currentFileContents, fileContent, cleanFilename);
            includedFiles.remove(cleanFilename);
            return processed;
        } catch (IOException e) {
            throw new IncludeException(
                "Failed to include file",
                0,
                cleanFilename,
                e.getMessage()
            );
        }
    }

    private static class ImportInfo {
        ImportType type;
        String path;
        String originalFilename;

        ImportInfo(ImportType type, String path, String originalFilename) {
            this.type = type;
            this.path = path;
            this.originalFilename = originalFilename;
        }
    }

    private static ImportInfo parseImportType(String filename) {
        if (filename.startsWith("STDLIB:")) {
            String moduleName = filename.substring(7); // Remove "STDLIB:" prefix
            return new ImportInfo(ImportType.STDLIB, moduleName, filename);
        } else if (filename.contains(":") && !filename.startsWith("STDLIB:")) {
            // Handle modulename:macroname format
            return new ImportInfo(ImportType.MODULE_MACRO, filename, filename);
        } else if (filename.contains("/") || filename.endsWith(".mas") || filename.endsWith(".masm")) {
            return new ImportInfo(ImportType.PATH, filename, filename);
        } else {
            // Default to dot notation for backward compatibility
            return new ImportInfo(ImportType.DOT_NOTATION, filename, filename);
        }
    }

    private static String readFileContent(ImportInfo importInfo) throws IOException {
        switch (importInfo.type) {
            case STDLIB:
                return readStdlibContent(importInfo.path);
            case PATH:
                return readPathContent(importInfo.path);
            case DOT_NOTATION:
                return readDotNotationContent(importInfo.path);
            case MODULE_MACRO:
                // If macro provider didn't handle it, treat as regular file
                return readPathContent(importInfo.path);
            default:
                throw new IOException("Unknown import type");
        }
    }

    private static String readStdlibContent(String moduleName) throws IOException {
        // Try both .masm and .mas extensions for stdlib
        String[] extensions = {".masm", ".mas"};
        
        for (String ext : extensions) {
            String resourcePath = "stdlib/" + moduleName + ext;
            try (InputStream is = Includemanager.class.getClassLoader().getResourceAsStream(resourcePath)) {
                if (is != null) {
                    return new String(is.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
        }

        // If not found in classpath, try custom stdlib directory
        if (ArgumentParser.Args.stdlibDir != null && !ArgumentParser.Args.stdlibDir.isEmpty()) {
            for (String ext : extensions) {
                File stdlibFile = new File(ArgumentParser.Args.stdlibDir, moduleName + ext);
                if (stdlibFile.exists()) {
                    return readFile(stdlibFile);
                }
            }
        }

        throw new IOException("Stdlib module not found: " + moduleName);
    }

    private static String readPathContent(String path) throws IOException {
        File file = new File(path);
        
        // If the path doesn't have an extension, try both .masm and .mas
        if (!path.endsWith(".mas") && !path.endsWith(".masm")) {
            File masmFile = new File(path + ".masm");
            File masFile = new File(path + ".mas");
            
            if (masmFile.exists()) {
                return readFile(masmFile);
            } else if (masFile.exists()) {
                return readFile(masFile);
            }
        } else if (file.exists()) {
            return readFile(file);
        }

        throw new IOException("File not found: " + path);
    }

    private static String readDotNotationContent(String filename) throws IOException {
        List<File> searchPaths = getSearchPaths(filename);
        
        // First try loading from classpath if it's a stdlib file
        String resourcePath = filename.replace(".", "/") + ".masm";
        String resourcePathAsm = filename.replace(".", "/") + ".mas";
            try (InputStream is = Includemanager.class.getClassLoader().getResourceAsStream(resourcePath)) {
                if (is != null) {
                    return new String(is.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
            try (InputStream is = Includemanager.class.getClassLoader().getResourceAsStream(resourcePathAsm)) {
                if (is != null) {
                    return new String(is.readAllBytes(), StandardCharsets.UTF_8);
                }
            
            }
    
        
        // Fall back to file system search
        for (File file : searchPaths) {
            if (file.exists()) {
                return readFile(file);
            }
        }
        
        StringBuilder errorMsg = new StringBuilder("File not found in any search path or resources:\n");
        errorMsg.append("- classpath:").append(resourcePath).append("\n");
        for (File path : searchPaths) {
            errorMsg.append("- ").append(path.getAbsolutePath()).append("\n");
        }
        throw new IncludeException(resourcePath, 0, filename, errorMsg.toString());
    }

    private static List<File> getSearchPaths(String filename) {
        String resourcePath = filename.replace(".", "/") + ".masm";
        List<File> paths = new ArrayList<>();

        // Add custom stdlib path first if provided
        if (ArgumentParser.Args.stdlibDir != null && !ArgumentParser.Args.stdlibDir.isEmpty()) {
            paths.add(new File(ArgumentParser.Args.stdlibDir, resourcePath));
        }
        
        // Current directory
        paths.add(new File(resourcePath));
        
        // Working directory
        paths.add(new File(System.getProperty("user.dir"), resourcePath));
        
        // Module directory
        paths.add(new File(config.getPath("modules.dir"), resourcePath));
        
        // Stdlib directory
        paths.add(new File(config.getPath("stdlib.dir"), resourcePath));

        // Remove: paths.add(Args.replacementDir);

        return paths;
    }

    private static String readFile(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    private static String processIncludes(String currentContent, String includeContent, String includePath) {
        String[] lines = currentContent.split("\n");
        StringBuilder processed = new StringBuilder();
        String includeStatement = "#include \"" + includePath + "\"";
        
        boolean includeFound = false;
        for (String line : lines) {
            if (line.trim().equals(includeStatement)) {
                if (!includeFound) {
                    processed.append(includeContent);
                    includeFound = true;
                }
            } else {
                processed.append(line).append("\n");
            }
        }
        
        return processed.toString() + '\n';
    }
}
