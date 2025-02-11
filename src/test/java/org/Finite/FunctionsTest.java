package org.Finite;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FunctionsTest {
    private Functions functions;
    private int[] memory;

    @BeforeEach
    void setUp() {
        functions = new Functions();
        memory = new int[common.MAX_MEMORY];
        // Reset registers before each test
        common.WriteRegister("RAX", 0);
        common.WriteRegister("RBX", 0);
        common.WriteRegister("RCX", 0);
    }

    @Test
    void testAdd() {
        common.WriteRegister("RAX", 0);
        common.WriteRegister("RBX", 0);
        common.WriteRegister("RCX", 0);
        common.WriteRegister("RAX", 5);
        common.WriteRegister("RBX", 3);
        functions.add(memory, "RAX", "RBX");
        assertEquals(8, common.ReadRegister("RAX"));
    }

    @Test
    void testSub() {
        common.WriteRegister("RAX", 0);
        common.WriteRegister("RBX", 0);
        common.WriteRegister("RCX", 0);
        common.WriteRegister("RAX", 10);
        common.WriteRegister("RBX", 4);
        functions.sub(memory, "RAX", "RBX");
        assertEquals(6, common.ReadRegister("RAX"));
    }

    @Test
    void testMul() {
        common.WriteRegister("RAX", 0);
        common.WriteRegister("RBX", 0);
        common.WriteRegister("RCX", 0);
        common.WriteRegister("RAX", 5);
        common.WriteRegister("RBX", 4);
        functions.mul(memory, "RAX", "RBX");
        assertEquals(20, common.ReadRegister("RAX"));
    }

    @Test
    void testDiv() {
        common.WriteRegister("RAX", 0);
        common.WriteRegister("RBX", 0);
        common.WriteRegister("RCX", 0);
        common.WriteRegister("RAX", 15);
        common.WriteRegister("RBX", 3);
        functions.div(memory, "RAX", "RBX");
        assertEquals(5, common.ReadRegister("RAX"));
    }

    @Test
    void testMov() {
        common.WriteRegister("RAX", 0);
        common.WriteRegister("RBX", 0);
        common.WriteRegister("RCX", 0);
        functions.mov(memory, "RAX", "10");
        assertEquals(10, common.ReadRegister("RAX"));
        
        common.WriteRegister("RBX", 20);
        functions.mov(memory, "RAX", "RBX");
        assertEquals(20, common.ReadRegister("RAX"));
    }


    @Test
    void testOut() {
        common.WriteRegister("RAX", 0);
        common.WriteRegister("RBX", 0);
        common.WriteRegister("RCX", 0);
        common.WriteRegister("RAX", 65);
        functions.out(memory, "RAX");
        // move to memory 50
        memory[50] = 25;
        functions.out(memory, "$50");// should print 25
        common.WriteRegister("RAX", 50); // move 50 to RAX
        functions.out(memory, "$RAX"); // should print 25

    }

    @Test
    void testCmp() {
        common.WriteRegister("RAX", 0);
        common.WriteRegister("RBX", 0);
        common.WriteRegister("RCX", 0);

        common.WriteRegister("RAX", 5);
        common.WriteRegister("RBX", 5);
        functions.cmp(memory, "RAX", "RBX");
        assertEquals(1, common.ReadRegister("RFLAGS"));

        functions.cmp(memory, "RAX", "10");
        assertEquals(0, common.ReadRegister("RFLAGS"));
    }
}