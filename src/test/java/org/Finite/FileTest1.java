package org.Finite;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import static org.Finite.interp.functions;
import static org.Finite.interp.include;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import java.io.*;

public class FileTest1 {
    @TempDir
    Path tempDir;
    private File testFile;

    @BeforeEach
    void setUp() {
        testFile = tempDir.resolve("test.masm").toFile();
    }

    @Nested
    @DisplayName("File Reading Operations")
    class FileReadingOperations {
        @BeforeEach
        void createTestFile() throws IOException {
            try (FileWriter writer = new FileWriter(testFile)) {
                writer.write("#include \"stdlib.test.meow\"\n");
                writer.write("MOV RAX 10\n");
                writer.write("ADD RAX 5\n");
            }
        }

        @Test
        @DisplayName("Test Includes")
        void testIncludes() {
            String CurrentFileContents = "";

            try {
                BufferedReader reader = new BufferedReader
                        (new FileReader(testFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    CurrentFileContents += line + "\n";
                }
                reader.close();
            } catch (IOException e) {
                System.out.println("Error reading file: " + testFile);
            }


            String contents = includemanager.include("stdlib.test.meow", CurrentFileContents);

            try
            {
            String TestContents = ReadResourceFile.read("stdlib/test/meow.masm");
            assertEquals(contents, TestContents + "\nMOV RAX 10\nADD RAX 5\n");
            }
            catch (Exception e)
            {
                System.out.println("Error reading file: " + "stdlib/test/meow.masm");
                System.out.println(e);
            }

        }
        @Test
        @DisplayName("Test File Exists")
        void testFileExists() {
            assertTrue(testFile.exists());
        }

    
    }
}
