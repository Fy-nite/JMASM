package org.Finite;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
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
                writer.write("MOV RAX 10\n");
                writer.write("ADD RAX 5\n");
            }
        }

        @Test
        @DisplayName("Test File Exists")
        void testFileExists() {
            assertTrue(testFile.exists());
        }

    
    }
}
