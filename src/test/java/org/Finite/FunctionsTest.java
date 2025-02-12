package org.Finite;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

public class FunctionsTest {
    private Functions functions;
    private int[] memory;
    private interp.instructions instrs;

    @BeforeEach
    void setUp() {
        functions = new Functions();
        memory = new int[common.MAX_MEMORY];
        common.WriteRegister("RAX", 0);
        common.WriteRegister("RBX", 0);
        common.WriteRegister("RCX", 0);
    }

    @Nested
    @DisplayName("Basic Operations")
    class BasicOperations {
        @Test
        @DisplayName("Test MOV function")
        void testMov() {
            functions.mov(memory, "RAX", "10");
            assertEquals(10, common.ReadRegister("RAX"));
            
            common.WriteRegister("RBX", 20);
            functions.mov(memory, "RAX", "RBX");
            assertEquals(20, common.ReadRegister("RAX"));
        }

        @Nested
        @DisplayName("Arithmetic Operations")
        class ArithmeticOperations {
            @BeforeEach
            void validateMov() {
                // Verify MOV works before running dependent tests
                functions.mov(memory, "RAX", "5");
                Assertions.assertEquals(5, common.ReadRegister("RAX"));
            }

            @Test
            @DisplayName("Test ADD function")
            void testAdd() {
                functions.mov(memory, "RAX", "5");
                functions.mov(memory, "RBX", "3");
                functions.add(memory, "RAX", "RBX");
                assertEquals(8, common.ReadRegister("RAX"));
            }

            @Test
            @DisplayName("Test SUB function")
            void testSub() {
                functions.mov(memory, "RAX", "10");
                functions.mov(memory, "RBX", "4");
                functions.sub(memory, "RAX", "RBX");
                assertEquals(6, common.ReadRegister("RAX"));
            }

            @Test
            @DisplayName("Test MUL function")
            void testMul() {
                functions.mov(memory, "RAX", "5");
                functions.mov(memory, "RBX", "4");
                functions.mul(memory, "RAX", "RBX");
                assertEquals(20, common.ReadRegister("RAX"));
            }

            @Test
            @DisplayName("Test DIV function")
            void testDiv() {
                functions.mov(memory, "RAX", "15");
                functions.mov(memory, "RBX", "3");
                functions.div(memory, "RAX", "RBX");
                assertEquals(5, common.ReadRegister("RAX"));
            }
        }

        @Nested
        @DisplayName("Flow Operations")
        class FlowOperations {
            @Test
            @DisplayName("Test JMP function")
            void testJmp() {
                functions.mov(memory, "RIP", "0");
                functions.jmp(memory, "5");
                assertEquals(5, common.ReadRegister("RIP"));
            }

            @Test
            @DisplayName("Test JMP function with label")
            void testJmpWithLabel() {
                String label = "test";
                String junkOperations[] = {
                    "lbl main",
                    "MOV RAX 5",
                    "MOV RBX 3",
                    "ADD RAX RBX",
                    "MOV RAX 10",
                    "lbl #" + label,
                    "MOV RAX 15",
                    "HLT"
                };
                // write the operations to the instructions list
                common.WrapStdinToFile(junkOperations);
                functions.jmp(memory, "#" + label);
                assertEquals(5, common.ReadRegister("RIP"));
            }

        }
    }


}
