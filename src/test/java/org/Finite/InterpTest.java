package org.Finite;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class InterpTest {
    private interp.instructions instrs;

    @BeforeEach
    void setUp() {
        common.WriteRegister("RIP", 0);
        common.WriteRegister("RAX", 0);
        common.WriteRegister("RBX", 0);
        instrs = new interp.instructions();
        instrs.instructions = new interp.instruction[100];
        instrs.Memory = new int[1000];
        instrs.length = 0;
        instrs.memory_size = 1000;
        instrs.max_labels = 100;
        instrs.max_instructions = 100;
        instrs.labels = new int[100];
        instrs.functions = new Functions();
    }

    @Test
    void testExecuteSingleInstruction() {
        common.WriteRegister("RIP", 0);
        common.WriteRegister("RAX", 0);
        common.WriteRegister("RBX", 0);
        interp.instruction instr = new interp.instruction();
        instr.name = "MOV";
        instr.sop1 = "RAX";
        instr.sop2 = "5";
        
        int result = interp.ExecuteSingleInstruction(instr, instrs.Memory, instrs);
        assertEquals(0, result);
        assertEquals(5, common.ReadRegister("RAX"));
    }

    @Test
    void testRunFile(@TempDir Path tempDir) throws IOException {
        common.WriteRegister("RIP", 0);
        common.WriteRegister("RAX", 0);
        common.WriteRegister("RBX", 0);
        File testFile = tempDir.resolve("test.masm").toFile();
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("MOV RAX 10\n");
            writer.write("MOV RBX 5\n");
            writer.write("ADD RAX RBX\n");
        }

        interp.runFile(testFile.getAbsolutePath());
        assertEquals(15, common.ReadRegister("RAX"));
    }

    @Test
    void testLabelExecution(@TempDir Path tempDir) throws IOException {
        common.WriteRegister("RIP", 0);
        common.WriteRegister("RAX", 0);
        common.WriteRegister("RBX", 0);
        File testFile = tempDir.resolve("test_label.masm").toFile();
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("LBL main\n");
            writer.write("    MOV RAX 5\n");
            writer.write("    MOV RBX 3\n");
            writer.write("    ADD RAX RBX\n");
        }

        interp.runFile(testFile.getAbsolutePath());

        assertEquals(8, common.ReadRegister("RAX"));
    }
}