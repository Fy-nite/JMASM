package org.finite;

import org.finite.Config.MASMConfig;
import org.finite.Exceptions.IncludeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.finite.*;
import org.finite.Argumentparser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

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

    public static String include(String filename, String currentFileContents) {
        String cleanFilename = filename.replace("\"", "");
        // Check for custom include provider (annotation-based)
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
        // Check for custom macro provider (MNI-based)
        int dotIdx = cleanFilename.lastIndexOf('.');
        if (dotIdx > 0) {
            String libName = cleanFilename.substring(0, dotIdx);
            String macroName = cleanFilename.substring(dotIdx + 1);
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
            String fileContent = readFileContent(cleanFilename);
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

    private static String readFileContent(String filename) throws IOException {
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
        if (Argumentparser.Args.stdlibDir != null && !Argumentparser.Args.stdlibDir.isEmpty()) {
            paths.add(new File(Argumentparser.Args.stdlibDir, resourcePath));
        }
        
        // Current directory
        paths.add(new File(resourcePath));
        
        // Working directory
        paths.add(new File(System.getProperty("user.dir"), resourcePath));
        
        // Module directory
        paths.add(new File(config.getPath("modules.dir"), resourcePath));
        
        // Stdlib directory
        paths.add(new File(config.getPath("stdlib.dir"), resourcePath));
        
        paths.add(Argumentparser.Args.replacementDir);
        
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
