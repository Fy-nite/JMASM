package org.Finite;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

    @Nested
    @DisplayName("Instruction Execution")
    class InstructionExecution {
        @Test
        @DisplayName("Test Single Instruction Execution")
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
        @DisplayName("Test DB and OUT Instructions")
        void testDBAndOutInstructions() {
            common.WriteRegister("RIP", 0);
            common.WriteRegister("RAX", 0);
            common.WriteRegister("RBX", 0);

            // First instruction: MOV RAX 1
            interp.instruction instr1 = new interp.instruction();
            instr1.name = "MOV";
            instr1.sop1 = "RAX";
            instr1.sop2 = "1";
            int result1 = interp.ExecuteSingleInstruction(instr1, instrs.Memory, instrs);
            assertEquals(0, result1);
            assertEquals(1, common.ReadRegister("RAX"));

            // Second instruction: MOV RBX 50
            interp.instruction instr2 = new interp.instruction();
            instr2.name = "MOV";
            instr2.sop1 = "RBX";
            instr2.sop2 = "50";
            int result2 = interp.ExecuteSingleInstruction(instr2, instrs.Memory, instrs);
            assertEquals(0, result2);
            assertEquals(50, common.ReadRegister("RBX"));

            // Third instruction: DB $RBX "Hello, world!\n"
            String testString = "Hello, wooooorld!\n";
            for (int i = 0; i < testString.length(); i++) {
                instrs.Memory[50 + i] = testString.charAt(i);
            }

            // Fourth instruction: OUT RAX $RBX
            interp.instruction instr4 = new interp.instruction();
            instr4.name = "OUT";
            instr4.sop1 = "RAX";
            instr4.sop2 = "$RBX";
            int result4 = interp.ExecuteSingleInstruction(instr4, instrs.Memory, instrs);
            assertEquals(0, result4);
        }
    }

    @Nested
    @DisplayName("File Operations")
    class FileOperations {
        @TempDir
        Path tempDir;

        @Test
        @DisplayName("Test Running File")
        void testRunFile() throws IOException {
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
        @DisplayName("Test Label Execution")
        void testLabelExecution() throws IOException {
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
}