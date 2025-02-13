package org.Finite;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import org.Finite.common.*;
import org.Finite.debug.*;

import java.nio.charset.*;
import java.util.*;
import java.io.*;

public class includemanager {
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
    public static String include(@org.jetbrains.annotations.NotNull String path, String currentFileContents) {
        // Convert the dot notation to path
        String resourcePath = path.replace(".", "/") + ".masm";
        try {
            // Get resource as stream from classpath
            ClassLoader classLoader = includemanager.class.getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(resourcePath);
            if (inputStream == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            
            // Read the file
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }
            
            // Replace the include statement with the file contents
            String includeStatement = "#include \"" + path + "\"";
            currentFileContents = currentFileContents.replace(includeStatement, content.toString());
            
        } catch (IOException e) {
            common.box("Error", "Error including file " + resourcePath + ": " + e.getMessage(), "error");
        }
        
        return currentFileContents;
    }

}
