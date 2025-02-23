package org.finite;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

public class ReadResourceFile {
    public static String[] readDir(String path) {
        String[] files = new String[0];
        try {
            InputStream inputStream = ReadResourceFile.class.getClassLoader().getResourceAsStream(path);
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found: " + path);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                files = line.split("\\s+");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return files;
    }

    public static String read(String resourceName) {
        StringBuilder content = new StringBuilder();
        try (InputStream inputStream = ReadResourceFile.class.getClassLoader().getResourceAsStream(resourceName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found: " + resourceName);
            }
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }
}