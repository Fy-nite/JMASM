package org.Finite;

import java.util.Scanner;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import com.beust.jcommander.Parameter;
public class common {
    // THIS IS FINAL~~!!?!??!?!??!?!?!??!?!?!?!??!?!?
    public static final int MAX_MEMORY = 4096; // are you angry?
    public static String[] commands = new String[]{
            "dumpMemory",
            "dumpRegisters",
            "readMemory <address>",
            "writeMemory <address> <value>",
            "readRegister <register>",
            "writeRegister <register> <value>",
            "exit",
            "help"
    };
    public static String[] registers = {"RAX","RBX","RCX","RDX","RBP","RSP","RIP","R8","R9","R10","R11","R12","R13","R14","R15","FLAGS"};
    public static String[] instructions = {"MOV","ADD","SUB","MUL","DIV","AND","OR","XOR","NOT","SHL","SHR","CMP","JMP","JE","JNE","JG","JGE","JL","JLE","CALL","RET","PUSH","POP","HLT"};
    public static int[] memory = new int[MAX_MEMORY];

    public static Map<String, Integer> registersMap = new HashMap<String, Integer>() {{
        put("RAX", 0);
        put("RBX", 1);
        put("RCX", 2);
        put("RDX", 3);
        put("RBP", 4);
        put("RSP", 5);
        put("RIP", 6);
        put("R8", 7);
        put("R9", 8);
        put("R10", 9);
        put("R11", 10);
        put("R12", 11);
        put("R13", 12);
        put("R14", 13);
        put("R15", 14);
        put("FLAGS", 15);
    }};

    /**
     * Dumps the contents of the memory array to the console.
     */
    public static void dumpMemory() {
        // Check if the memory array has been initialized
        if (memory == null || memory.length != MAX_MEMORY) {
            System.err.println("Error: Memory array not initialized or incorrect size.");
            return;
        }

        // Iterate over each index in the memory array and print its contents
        for (int i = 0; i < MAX_MEMORY; i++) {
            System.out.printf("%d: %d%n", i, memory[i]);
        }
    }


    public static void dumpRegisters() {
        for (int i = 0; i < 16; i++) {
            print("%s: %d", registers[i], memory[i]);
        }
    }

    public static void WriteRegister(int register, int value) {
        memory[register] = value;
    }

    public static int ReadRegister(String register) {
        // use the hashmap to get the register
        if (!registersMap.containsKey(register)) {
            printerr("Error: Invalid register name: " + register + "\n");
            return -1;
        }
        return memory[registersMap.get(register)];
    }
public static void box(String... messages) {
    for (String message : messages) {
        print("\033[34m┏");
        for (int i = 0; i < message.length(); i++) {
            print("━");
        }
        print("┓\n");
        print("┃\033[31m%s\033[34m┃\n", message);
        print("\033[34m┗");
        for (int i = 0; i < message.length(); i++) {
            print("━");
        }
        print("┛\033[0m\n");
        print("\n"); // Add a new line between boxes
    }
}
    public static void printerr(String message) {
        // we gonna box the message in red
        print("\033[34m┏");
        for (int i = 0; i < message.length(); i++) {
          print("━");
        }
        print("┓\n");
        print("┃\033[31m%s\033[34m┃\n", message);
        print("\033[34m┗");
        for (int i = 0; i < message.length(); i++) {
          print("━");
        }
        print("┛\033[0m\n");
    }


    /**
     * Writes a value to the global or shared memory.
     * <p>
     * This function assumes that the 'memory' array is a contiguous block of space,
     * where each element at an index represents a value stored in memory. The caller
     * is responsible for ensuring that this assumption is valid and for updating the
     * 'memory' array as needed to reflect changes in the underlying storage.
     *
     * @param address The memory address to write the value to.
     * @param value   The value to write to memory.
     */

public static void WriteMemory(int address, int value) {
        memory[address] = value;
    }

    /**
     * Reads a value from the global or shared memory.
     * <p>
     * This function assumes that the 'memory' array is a contiguous block of space,
     * where each element at an index represents a value stored in memory. The caller
     * is responsible for ensuring that this assumption is valid and for updating the
     * 'memory' array as needed to reflect changes in the underlying storage.
     *
     * @param address The memory address of the value to read.
     * @return The value stored at the specified memory address.
     */
    public static int ReadMemory(int address) {
        // Directly access the element at the given index from the 'memory' array
        return memory[address];
    }

    public static void print(String message, Object... args) {
        System.out.printf(message, args);
    }


//
}
