package org.Finite;

import org.Finite.Common.common;
import org.Finite.Exceptions.MASMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.Finite.Common.common.printerr;

import java.io.*;
import java.util.*;

public class includemanager {
    private static final Logger log = LoggerFactory.getLogger(includemanager.class);
    private static final Set<String> includedFiles = new HashSet<>();

    /*
     * include's a file into the current file from the resources directory
     * take for example the following code:
     * #include "stdlib.term.io"
     * this will include the file stdlib/term/io.masm into the current file
     * if the user does not specify the file extension, it will default to .masm
     * if the file does not exist, it will throw an error
     * if the file does exist, it will include the file
     * another thing is that if the user does
     * #include "stdlib.term.io.*"
     * then we just include the whole directory because the user wants literly everything inside that directory for some reason.
     */
    public static String include(String filename, String CurrentFileContents) {
        // Remove quotes from filename
        String cleanFilename = filename.replace("\"", "");
        
        if (includedFiles.contains(cleanFilename)) {
            throw new MASMException(
                "Recursive include detected", 
                0,  // We'll need to pass line numbers from the parser
                "#include \"" + filename + "\"",
                "File has already been included: " + cleanFilename
            );
        }
        
        // Convert the dot notation to path
        String resourcePath = cleanFilename.replace(".", "/") + ".masm";
        
        try {
            // Track this file as being processed
            includedFiles.add(cleanFilename);
            
            String fileContent = readFileContent(resourcePath);
            if (fileContent == null) {
                throw new MASMException(
                    "Include file not found",
                    0,  // Line number should be passed from parser
                    "#include \"" + filename + "\"",
                    "Could not find file: " + resourcePath
                );
            }

            // Process the include statement line by line
            String[] lines = CurrentFileContents.split("\n");
            StringBuilder processedContent = new StringBuilder();
            String includeStatement = "#include \"" + filename.replace("\"", "") + "\"";
            
            boolean includeFound = false;
            for (int lineNum = 0; lineNum < lines.length; lineNum++) {
                String line = lines[lineNum];
                if (line.trim().equals(includeStatement)) {
                    if (!includeFound) {
                        processedContent.append(fileContent).append("\n");
                        includeFound = true;
                    } else {
                        throw new MASMException(
                            "Duplicate include statement",
                            lineNum + 1,
                            line,
                            "File already included: " + filename
                        );
                    }
                } else {
                    processedContent.append(line).append("\n");
                }
            }

            // Remove this file from being processed
            includedFiles.remove(cleanFilename);
            
            return processedContent.toString();
            
        } catch (IOException e) {
            throw new MASMException(
                "Failed to process include",
                0,  // Line number should be passed from parser
                "#include \"" + filename + "\"",
                e.getMessage()
            );
        }
    }

    private static String readFileContent(String resourcePath) throws IOException {
        // Try classpath first
        ClassLoader classLoader = Functions.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(resourcePath);
        
        // If not found in classpath, try file system
        if (inputStream == null) {
            File localFile = new File(resourcePath);
            if (!localFile.exists()) {
                localFile = new File(System.getProperty("user.dir"), resourcePath);
            }
            if (localFile.exists()) {
                inputStream = new FileInputStream(localFile);
            }
        }
        
        if (inputStream == null) {
            return null;
        }

        // Read the file content
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        }
    }
}
