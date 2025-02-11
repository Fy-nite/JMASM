package org.Finite;

import org.junit.jupiter.api.Test;
import com.beust.jcommander.JCommander;
import static org.junit.jupiter.api.Assertions.*;

public class ArgumentParserTest {
    @Test
    void testDebugFlag() {
        ArgumentParser.Args args = new ArgumentParser.Args();
        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse("-d");
        assertTrue(args.debug);
    }

    @Test
    void testFileArgument() {
        ArgumentParser.Args args = new ArgumentParser.Args();
        String testFile = "test.masm";
        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse("-f", testFile);
        assertEquals(testFile, args.file);
    }

    @Test
    void testHelpFlag() {
        ArgumentParser.Args args = new ArgumentParser.Args();
        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse("--help");
        assertTrue(args.help);
    }

    @Test
    void testVersionFlag() {
        ArgumentParser.Args args = new ArgumentParser.Args();
        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse("-v");
        assertTrue(args.version);
    }

    @Test
    void testMultipleArguments() {
        ArgumentParser.Args args = new ArgumentParser.Args();
        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse("-d", "-f", "test.masm");
        assertTrue(args.debug);
        assertEquals("test.masm", args.file);
    }
}