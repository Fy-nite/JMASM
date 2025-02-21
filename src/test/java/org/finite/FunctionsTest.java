package org.finite;

import static org.junit.jupiter.api.Assertions.*;

import org.finite.Common.common;
import org.junit.jupiter.api.*;

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

    @AfterEach
    void tearDown() {
        // Reset all registers to prevent test interference
        common.WriteRegister("RAX", 0);
        common.WriteRegister("RBX", 0);
        common.WriteRegister("RCX", 0);
        common.WriteRegister("RIP", 0);
        common.WriteRegister("RFLAGS", 0);

        // Clear memory
        for (int i = 0; i < memory.length; i++) {
            memory[i] = 0;
        }
    }

    @BeforeAll
    static void setUpClass() {
        // Enable debug mode
        common.print("DEBUG: Starting test suite\n");
    }

    @AfterAll
    static void tearDownClass() {
        common.print("DEBUG: Finishing test suite\n");
    }

    @Nested
    @DisplayName("Basic Operations")
    class BasicOperations {

        @Test
        @DisplayName("Test MOV function safely")
        void testMov() {
            // Test valid moves
            functions.mov(memory, "RAX", "10", instrs);
            assertEquals(10, common.ReadRegister("RAX"), "Basic MOV failed");

            common.WriteRegister("RBX", 20);
            functions.mov(memory, "RAX", "RBX", instrs);
            assertEquals(
                20,
                common.ReadRegister("RAX"),
                "Register to register MOV failed"
            );

            // Test invalid source register
            IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> functions.mov(memory, "RAX", "INVALID_REG", instrs),
                "Should throw IllegalArgumentException for invalid source register"
            );
//            assertTrue(thrown.getMessage().contains("Invalid source register"));

            // Test invalid destination register
            thrown = assertThrows(
                IllegalArgumentException.class,
                () -> functions.mov(memory, "INVALID_REG", "10", instrs),
                "Should throw IllegalArgumentException for invalid destination register"
            );
            assertTrue(
                thrown.getMessage().contains("Invalid destination register")
            );

            // Test invalid memory reference
            thrown = assertThrows(
                IllegalArgumentException.class,
                () -> functions.mov(memory, "RAX", "$invalid", instrs),
                "Should throw IllegalArgumentException for invalid memory address"
            );
            assertTrue(thrown.getMessage().contains("Invalid memory address"));
        }

        @Nested
        @DisplayName("Arithmetic Operations")
        class ArithmeticOperations {

            @BeforeEach
            void validateMov() {
                // Verify MOV works before running dependent tests
                functions.mov(memory, "RAX", "5", instrs);
                Assertions.assertEquals(5, common.ReadRegister("RAX"));
            }

            @Test
            @DisplayName("Test ADD function")
            void testAdd() {
                functions.mov(memory, "RAX", "5", instrs);
                functions.mov(memory, "RBX", "3", instrs);
                functions.add(memory, "RAX", "RBX", instrs);
                assertEquals(8, common.ReadRegister("RAX"));
            }

            @Test
            @DisplayName("Test SUB function")
            void testSub() {
                functions.mov(memory, "RAX", "10", instrs);
                functions.mov(memory, "RBX", "4", instrs);
                functions.sub(memory, "RAX", "RBX", instrs);
                assertEquals(6, common.ReadRegister("RAX"));
            }

            @Test
            @DisplayName("Test MUL function")
            void testMul() {
                functions.mov(memory, "RAX", "5", instrs);
                functions.mov(memory, "RBX", "4", instrs);
                functions.mul(memory, "RAX", "RBX", instrs);
                assertEquals(20, common.ReadRegister("RAX"));
            }

            @Test
            @DisplayName("Test DIV function")
            void testDiv() {
                functions.mov(memory, "RAX", "15", instrs);
                functions.mov(memory, "RBX", "3", instrs);
                functions.div(memory, "RAX", "RBX", instrs);
                assertEquals(5, common.ReadRegister("RAX"));
            }

            @Test
            @DisplayName("Test DIV by zero")
            void testDivByZero() {
                functions.mov(memory, "RAX", "10", instrs);
                functions.mov(memory, "RBX", "0", instrs);
                assertThrows(ArithmeticException.class, () -> {
                    functions.div(memory, "RAX", "RBX", instrs);
                });
            }

            @Test
            @DisplayName("Test shl function")
            @Disabled
            void testShl() {
                functions.mov(memory, "RAX", "1", instrs);
                functions.shl(memory, "RAX", "3", instrs);
                assertEquals(8, common.ReadRegister("RAX"));
            }

            @Test
            @DisplayName("Test shr function")
            @Disabled
            void testShr() {
                functions.mov(memory, "RAX", "8", instrs);
                functions.shr(memory, "RAX", "3", instrs);
                assertEquals(1, common.ReadRegister("RAX"));
            }

            @Test
            @DisplayName("Test inc function")
            void testInc() {
                functions.mov(memory, "RAX", "5", instrs);
                functions.inc(memory, "RAX", instrs);
                assertEquals(6, common.ReadRegister("RAX"));
            }

            @Test
            @DisplayName("Test dec function")
            void testDec() {
                functions.mov(memory, "RAX", "5", instrs);
                functions.dec(memory, "RAX", instrs);
                assertEquals(4, common.ReadRegister("RAX"));
            }

            @Test
            @DisplayName("Test neg function")
            void testNeg() {
                functions.mov(memory, "RAX", "5", instrs);
                functions.neg(memory, "RAX", instrs);
                assertEquals(-5, common.ReadRegister("RAX"));
            }

            @Test
            @DisplayName("Test not function")
            void testNot() {
                functions.mov(memory, "RAX", "5", instrs);
                functions.not(memory, "RAX", instrs);
                assertEquals(-6, common.ReadRegister("RAX"));
            }

            @Test
            @DisplayName("Test xor function")
            void testXor() {
                functions.mov(memory, "RAX", "5", instrs);
                functions.mov(memory, "RBX", "3", instrs);
                functions.xor(memory, "RAX", "RBX", instrs);
                assertEquals(6, common.ReadRegister("RAX"));
            }

            @Test
            @DisplayName("Test or function")
            void testOr() {
                functions.mov(memory, "RAX", "5", instrs);
                functions.mov(memory, "RBX", "3", instrs);
                functions.or(memory, "RAX", "RBX", instrs);
                assertEquals(7, common.ReadRegister("RAX"));
            }

            @Test
            @DisplayName("Test and function")
            void testAnd() {
                functions.mov(memory, "RAX", "5", instrs);
                functions.mov(memory, "RBX", "3", instrs);
                functions.and(memory, "RAX", "RBX", instrs);
                assertEquals(1, common.ReadRegister("RAX"));
            }

            @Test
            @DisplayName("Test arithmetic with invalid registers")
            void testInvalidRegisters() {
                assertThrows(Exception.class, () -> {
                    functions.add(memory, "INVALID_REG", "RAX", instrs);
                });
                assertThrows(Exception.class, () -> {
                    functions.sub(memory, "RAX", "INVALID_REG", instrs);
                });
                assertThrows(Exception.class, () -> {
                    functions.mul(memory, "INVALID_REG", "INVALID_REG", instrs);
                });
//                assertThrows(Exception.class, () -> {
//                    functions.div(memory, "RAX", "INVALID_REG");
//                });
            }

            @Test
            @DisplayName("Test overflow conditions")
            void testOverflow() {
                functions.mov(
                    memory,
                    "RAX",
                    Integer.toString(Integer.MAX_VALUE)
                        , instrs);
                functions.mov(memory, "RBX", "1", instrs);
                functions.add(memory, "RAX", "RBX", instrs);
                assertEquals(Integer.MIN_VALUE, common.ReadRegister("RAX"));
            }
        }

        @Nested
        @DisplayName("Safe Flow Operations")
        class FlowOperations {

            @Test
            @DisplayName("Test JMP function with safeguards")
            // skip this test
            @Disabled
            void testJmp() {
                try {
                    common.print("DEBUG: Starting JMP test\n");
                    functions.jmp(memory, "5", instrs);
                    assertEquals(
                        5,
                        common.ReadRegister("RIP"),
                        "Direct jump failed"
                    );
                    common.print("DEBUG: JMP test completed successfully\n");
                } catch (Exception e) {
                    common.print(
                        "DEBUG: Unexpected error in JMP test: %s\n",
                        e.getMessage()
                    );
                    throw e;
                }
            }

            @Test
            @DisplayName("Test JMP function with label safely")
            void testJmpWithLabel() {
                try {
                    common.print("DEBUG: Starting JMP with label test\n");
                    String label = "test";
                    int address = 5;
                    interp.instructions instrsz = new interp.instructions();
                    instrsz.labelMap = new java.util.HashMap<String, Integer>();
                    instrsz.labelMap.put(label, address);

                    assertNotNull(
                        instrsz.labelMap,
                        "Label map initialization failed"
                    );
                    assertTrue(
                        instrsz.labelMap.containsKey(label),
                        "Label not added to map"
                    );
                    assertEquals(
                        address,
                        instrsz.labelMap.get(label),
                        "Label address mismatch"
                    );

                    functions.jmp(memory, "#" + label, instrsz);
                    assertEquals(
                        address,
                        common.ReadRegister("RIP") + 1,
                        "Label jump failed"
                    );
                    common.print(
                        "DEBUG: JMP with label test completed successfully\n"
                    );
                } catch (Exception e) {
                    common.print(
                        "DEBUG: Unexpected error in JMP with label test: %s\n",
                        e.getMessage()
                    );
                    throw e;
                }
            }

            @Test
            @DisplayName("Test JMP with invalid inputs")
            void testInvalidJump() {
                // Test jump with invalid direct address and instructions
                assertThrows(NullPointerException.class, () -> {
                    functions.jmp(memory, "not_a_number", instrs);
                });

                // Test jump with null instructions
                assertThrows(NullPointerException.class, () -> {
                    functions.jmp(memory, "#label", null);
                });

                // Test jump with invalid label
                interp.instructions instrsz = new interp.instructions();
                instrsz.labelMap = new java.util.HashMap<String, Integer>();
                functions.jmp(memory, "#nonexistent", instrsz);
                // Should not change RIP when jump fails
                assertEquals(0, common.ReadRegister("RIP"));
            }
        }

        @Nested
        @DisplayName("Memory Operations")
        class MemoryOperations {

            // @Test
            // @Ignore
            // @DisplayName("Test DB function")
            // void testDb() {
            //     // Test valid DB operation
            //     functions.db(memory, "$0 \"Hello\"");
            //     assertEquals('H', memory[0]);
            //     assertEquals('e', memory[1]);

            //     // Test invalid memory address
            //     assertThrows(NumberFormatException.class, () -> {
            //         functions.db(memory, "invalid $address \"Test\"");
            //     });

            //     // Test empty string
            //     functions.db(memory, "$10 \"\"");
            //     assertEquals(0, memory[10]);
            // }

            @Test
            @DisplayName("Test OUT function")
            void testOut() {
                // Test invalid file descriptor
                functions.mov(memory, "RAX", "65", instrs); // ASCII 'A'
                functions.out(memory, "3", "RAX", instrs); // Should not output anything

                // Test null source
                functions.out(memory, "1", null, instrs);

                // Test invalid register
                functions.out(memory, "1", "INVALID_REG", instrs);

                // Test valid output
                functions.out(memory, "1", "RAX", instrs);
            }

            @Test
            @DisplayName("Test IN function")
            void testIn() {
                try {
                    // Setup test input with proper cleanup
                    common.UnwrapStdin();
                    common.WrapStdinToFile("test input");

                    // Test valid input first
                    functions.in(memory, "1", "$0", instrs);
                    assertEquals('t', memory[0]);
                    assertEquals('e', memory[1]);
                    assertEquals('s', memory[2]);
                    assertEquals('t', memory[3]);

                    // Test invalid file descriptor (non-zero)
                    IllegalArgumentException thrown = assertThrows(
                        IllegalArgumentException.class,
                        () -> functions.in(memory, "999", "$0", instrs)
                    );
                    assertEquals(
                        "Only stdin (fd 1) is supported",
                        thrown.getMessage()
                    );

                    // Test invalid file descriptor format
                    thrown = assertThrows(IllegalArgumentException.class, () ->
                        functions.in(memory, "xyz", "$0", instrs)
                    );
                    assertEquals(
                        "Invalid file descriptor format: xyz",
                        thrown.getMessage()
                    );

                    // Test null inputs
                    assertThrows(
                        IllegalArgumentException.class,
                        () -> functions.in(memory, null, "$0", instrs),
                        "File descriptor and destination cannot be null"
                    );

                    assertThrows(
                        IllegalArgumentException.class,
                        () -> functions.in(memory, "0", null, instrs),
                        "File descriptor and destination cannot be null"
                    );

                    // Test invalid destination format
                    thrown = assertThrows(IllegalArgumentException.class, () ->
                        functions.in(memory, "0", "invalid_dest", instrs)
                    );


                    // Test invalid memory address format
                    thrown = assertThrows(IllegalArgumentException.class, () ->
                        functions.in(memory, "0", "$invalid", instrs)
                    );
//                    assertEquals(
//                        "Invalid memory address format: $invalid",
//                        thrown.getMessage()
//                    );
                } finally {
                    // Always clean up
                    common.UnwrapStdin();
                }
            }
        }

        @Test
        @DisplayName("Test CMP function")
        void testCmp() {
            // Test equal values
            functions.mov(memory, "RAX", "10", instrs);
            functions.mov(memory, "RBX", "10", instrs);
            functions.cmp(memory, "RAX", "RBX", instrs);
            assertEquals(1, common.ReadRegister("RFLAGS"));

            // Test unequal values
            functions.mov(memory, "RBX", "20", instrs);
            functions.cmp(memory, "RAX", "RBX", instrs);
            assertEquals(0, common.ReadRegister("RFLAGS"));

            // Test with immediate value
            functions.cmp(memory, "RAX", "10", instrs);
            assertEquals(1, common.ReadRegister("RFLAGS"));

            // Test with invalid register
            assertThrows(Exception.class, () -> functions.cmp(memory, "INVALID_REG", "10", instrs));
        }
    }
}
