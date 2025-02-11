package org.Finite;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;

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
    public static String include(String path, String CurrentFileContents) {
        // check the resources for a file with the name of path
        // if it exists, include it
//        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("test.csv");
//        assert inputStream != null : "File not found";
//        InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
//        BufferedReader reader = new BufferedReader(streamReader);
//        String[] lines;
//        String line;
//        try {
//            lines = reader.lines().toArray(String[]::new);
//
//        } catch (IOException e) {
//
//            common.printerr("Error reading file: " + path);
//        }
//

        // check if the file exists
        File file = new File(path);
        if (!file.exists()) {
            common.box("Error", "file " + path + " does not exist", "error");
            return CurrentFileContents;
        }

        // read the file
        try {
            BufferedReader reader = new BufferedReader
                    (new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                CurrentFileContents += line + "\n";
            }
            reader.close();
        } catch (IOException e) {
            common.box("Error", "Error reading file: " + path, "error");
        }
        return CurrentFileContents;
    }
}
