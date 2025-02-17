package org.Finite;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import org.Finite.Common.common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class includemanager {
    private static final Logger log = LoggerFactory.getLogger(includemanager.class);

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
                InputStream inputStream = null;

                try {
                    // Try to get resource as stream from classpath
                    ClassLoader classLoader = includemanager.class.getClassLoader();
                    inputStream = classLoader.getResourceAsStream(resourcePath);

                    // If not found in classpath, try to read from local directory
                    if (inputStream == null) {
                        File localFile = new File(resourcePath);
                        if (localFile.exists()) {
                            inputStream = new FileInputStream(localFile);
                        } else {
                            throw new IOException("Resource not found: " + resourcePath);
                        }
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
