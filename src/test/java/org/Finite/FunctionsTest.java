package org.Finite;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

public class FunctionsTest {
    private Functions functions;
    private int[] memory;

    @BeforeEach
    @DisplayName("Set up")
    void setUp() {
        functions = new Functions();
        memory = new int[common.MAX_MEMORY];
        // Reset registers before each test
        common.WriteRegister("RAX", 0);
        common.WriteRegister("RBX", 0);
        common.WriteRegister("RCX", 0);
    }

    @Test
    @DisplayName("Test ADD function")
    void testAdd() {
        setUp();
        common.WriteRegister("RAX", 5);
        common.WriteRegister("RBX", 3);
        functions.add(memory, "RAX", "RBX");
        assertEquals(8, common.ReadRegister("RAX"));
    }

    @Test
    @DisplayName("Test SUB function")
    void testSub() {
        setUp();
        common.WriteRegister("RAX", 10);
        common.WriteRegister("RBX", 4);
        functions.sub(memory, "RAX", "RBX");
        assertEquals(6, common.ReadRegister("RAX"));
    }

    @Test
    @DisplayName("Test MUL function")
    void testMul() {
        setUp();
        common.WriteRegister("RAX", 5);
        common.WriteRegister("RBX", 4);
        functions.mul(memory, "RAX", "RBX");
        assertEquals(20, common.ReadRegister("RAX"));
    }

    @Test
    @DisplayName("Test DIV function")
    void testDiv() {
        setUp();
        common.WriteRegister("RAX", 15);
        common.WriteRegister("RBX", 3);
        functions.div(memory, "RAX", "RBX");
        assertEquals(5, common.ReadRegister("RAX"));
    }

    @Test
    @DisplayName("Test MOV function")
    void testMov() {
        setUp();
        functions.mov(memory, "RAX", "10");
        assertEquals(10, common.ReadRegister("RAX"));
        
        common.WriteRegister("RBX", 20);
        functions.mov(memory, "RAX", "RBX");
        assertEquals(20, common.ReadRegister("RAX"));
    }

    @Test
    @DisplayName("Test COUT function")
    void testCout(){
        setUp();
        File fd = common.WrapStdoutToFile();
        String path = fd.getAbsolutePath();
        common.WriteRegister("RAX", 65);
        functions.cout(memory, "1", "RAX");
        assertEquals("A", common.ReadFromFile(path));
    }

    @Test
    @DisplayName("Test OUT function")
    void testOut() {
        // setUp();
        // common.WriteRegister("RAX", 65);
        // functions.out(memory, "1", "RAX");
        // // move to memory 50
        // memory[50] = 25;
        // functions.out(memory, "1", "$RAX");
        // common.WriteRegister("RAX", 50); // move 50 to RAX
        // functions.out(memory, "1", "RAX");

        File temp = common.WrapStdoutToFile();
        // resolve the path of the temp file
        String tempPath = temp.getAbsolutePath();


        common.WriteRegister("RAX", 65);// write A to RAX
        functions.out(memory, "1", "RAX");
        assertEquals("65", common.ReadFromFile(tempPath));
    }
    @Test
    @DisplayName("Test IN function")
    void testIn()
    {
        setUp();
        common.WrapStdinToFile("5\n");
        functions.in(memory, "1", "RAX");
        assertEquals(0, common.ReadRegister("RAX"));
        common.UnwrapStdin();
    }

    @Test
    @DisplayName("Test JMP function")
    void TestJMP() {
        setUp();
        functions.jmp(memory, "10");
        assertEquals(10, common.ReadRegister("RIP"));
    }


    @Test
    @DisplayName("Test DB function")
    void testDB() {
        setUp();
        functions.db(memory, "$10 meow");
        // from address 10, we should have the string "meow"
        assertEquals(109, memory[10]);
        assertEquals(101, memory[11]);
        assertEquals(111, memory[12]);
        assertEquals(119, memory[13]);

    }

    @Test
    @DisplayName("Test compare function")
    void testCmp() {
        setUp();
        common.WriteRegister("RAX", 5);
        common.WriteRegister("RBX", 5);
        functions.cmp(memory, "RAX", "RBX");
        assertEquals(1, common.ReadRegister("RFLAGS"));

        functions.cmp(memory, "RAX", "10");
        assertEquals(0, common.ReadRegister("RFLAGS"));
    }
}