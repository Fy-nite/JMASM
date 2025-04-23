package org.finite;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import com.beust.jcommander.JCommander;
import static org.junit.jupiter.api.Assertions.*;

public class ArgumentParserTest {
    private ArgumentParser.Args args;
    private JCommander commander;

    @BeforeEach
    void setUp() {
        args = new ArgumentParser.Args();
        commander = JCommander.newBuilder()
                .addObject(args)
                .build();
    }

    @Nested
    @DisplayName("Flag Arguments")
    class FlagArguments {
        @Test
        @DisplayName("Test Debug Flag")
        void testDebugFlag() {
            commander.parse("-d");
            assertTrue(args.debug);
        }

        @Test
        @DisplayName("Test Help Flag")
        void testHelpFlag() {
            commander.parse("--help");
            assertTrue(args.help);
        }

        @Test
        @DisplayName("Test Version Flag")
        void testVersionFlag() {
            commander.parse("-v");
            assertTrue(args.version);
        }

        @Test
        @DisplayName("Test Compile Flag")
        void testCompileFlag() {
            commander.parse("-c");
            assertTrue(args.compile);
        }
    }

    @Nested
    @DisplayName("File Arguments")
    class FileArguments {
        @Test
        @DisplayName("Test File Argument")
        void testFileArgument() {
            String testFile = "test.masm";
            commander.parse("-f", testFile);
            assertEquals(testFile, args.file);
        }
    }

    @Nested
    @DisplayName("Combined Arguments")
    class CombinedArguments {
        @Test
        @DisplayName("Test Multiple Arguments")
        void testMultipleArguments() {
            commander.parse("-d", "-f", "test.masm");
            assertTrue(args.debug);
            assertEquals("test.masm", args.file);
        }
    }
}