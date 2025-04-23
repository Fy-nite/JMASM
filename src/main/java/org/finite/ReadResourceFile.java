package org.finite;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadResourceFile {
    private static final Logger logger = LoggerFactory.getLogger(ReadResourceFile.class);

    public static String read(String filename) {
        try {
            // First try as classpath resource
            InputStream is = ReadResourceFile.class.getClassLoader().getResourceAsStream(filename);
            if (is != null) {
                return new String(is.readAllBytes());
            }
            
            // If not found in classpath, try as regular file
            File file = new File(filename);
            if (file.exists()) {
                return new String(new FileInputStream(file).readAllBytes());
            }

            throw new FileNotFoundException("Resource not found: " + filename);
        } catch (IOException e) {
            logger.error("Failed to read file: {}", filename, e);
            return "";
        }
    }

    public static String[] readLines(String filename) throws IOException {
        List<String> lines = new ArrayList<>();
        
        // First try as regular file
        File file = new File(filename);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
                return lines.toArray(new String[0]);
            }
        }
        
        // If not found as file, try as classpath resource
        try (InputStream is = ReadResourceFile.class.getClassLoader().getResourceAsStream(filename)) {
            if (is != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        lines.add(line);
                    }
                    return lines.toArray(new String[0]);
                }
            }
        }
        
        throw new FileNotFoundException("Resource not found: " + filename);
    }

    public static String[] readDir(String path) {
        File dir = new File(path);
        if (dir.exists() && dir.isDirectory()) {
            return dir.list();
        }
        return new String[0];
    }
}