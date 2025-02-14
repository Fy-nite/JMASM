package org.Finite;

import java.util.HashMap;
import java.util.Map;
import java.nio.charset.*;
import java.util.*;
import java.io.*;

import static org.Finite.interp.arguments;

public class common {
    String modulesDir = "JMASM/modules";
    String configDir = "JMASM/config";
    String configPath = configDir + "/config.toml";
    String modulesPath = modulesDir + "/modules.toml";
    String[] modules = ReadResourceFile.readDir(modulesPath);
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
    public String Error = "";
    public boolean ErrorState = false;
    // TODO: find a better way to do this bullfuck
    public static String[] registers = {"RAX", "RBX", "RCX", "RDX", "RBP", "RSP", "RIP", "RDI", "RSI", "R0", "R1", "R2", "R3", "R4", "R5", "R6", "R7", "R8", "R9", "R10", "R11", "R12", "R13", "R14", "R15", "RFLAGS"};
    public static String[] instructions = {"MOV", "ADD", "SUB", "MUL", "DIV", "AND", "OR", "XOR", "NOT", "SHL", "SHR", "CMP", "JMP", "JE", "JNE", "JG", "JGE", "JL", "JLE", "CALL", "RET", "PUSH", "POP", "HLT", "NOP","OUT"};
    public static int[] memory = new int[MAX_MEMORY];
    public static Map<String, Integer> registersMap = new HashMap<String, Integer>() {{
        put("RAX", 0);
        put("RBX", 1);
        put("RCX", 2);
        put("RDX", 3);
        put("RBP", 4);
        put("RSP", 5);
        put("RIP", 6);
        put("RDI", 7);
        put("RSI", 8);
        put("R0", 9);
        put("R1", 10);
        put("R2", 11);
        put("R3", 12);
        put("R4", 13);
        put("R5", 14);
        put("R6", 15);
        put("R7", 16);
        put("R8", 17);
        put("R9", 18);
        put("R10", 19);
        put("R11", 20);
        put("R12", 21);
        put("R13", 22);
        put("R14", 23);
        put("R15", 24);
        put("RFLAGS", 25);

        
    }};

    /**
     * Dumps the contents of the memory array to the console.
     */
    public static void dumpMemory(int memory[]) {
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

    public static void WrapStdinToFile(String... file_contents)
    {
        // create a temp file
        try {
            File temp = File.createTempFile("tempfile", ".tmp");
            temp.deleteOnExit();
            FileWriter writer = new FileWriter(temp);
            for (String line : file_contents) {
                writer.write(line + "\n");
            }
            writer.close();
            System.setIn(new FileInputStream(temp));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String ReadFromFile(String filename)
    {
        try {
            File file = new File(filename);
            Scanner scanner = new Scanner(file);
            StringBuilder sb = new StringBuilder();
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine());
            }
            scanner.close();
            return sb.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static File WrapStdoutToFile()
    {
        // create a temp file
        File temp = null;
        try {
            temp = File.createTempFile("tempfile", ".tmp");
            temp.deleteOnExit();
            System.setOut(new PrintStream(new FileOutputStream(temp)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return temp;
    }
    public static void WrapStderrToFile()
    {
        // create a temp file
        try {
            File temp = File.createTempFile("tempfile", ".tmp");
            temp.deleteOnExit();
            System.setErr(new PrintStream(new FileOutputStream(temp)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void UnwrapStdin()
    {
        System.setIn(System.in);
    }
    public static void UnwrapStdout()
    {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
    }
    public static void UnwrapStderr()
    {
        System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err)));
    }
    public static void dumpRegisters() {
        for (int i = 0; i < 16; i++) {
            print("%s: %d", registers[i], memory[i]);
        }
    }

    public static void WriteRegister(String Register, int value) {
        // use the hashmap to get the register
        if (!registersMap.containsKey(Register)) {
            printerr("Error: Invalid register name: " + Register + "\n");
            return;
        }
        if (arguments.debug)
        {

            print("Writing %d to %s\n", value, Register);
        }
        memory[registersMap.get(Register)] = value;
    }

    public static int ReadRegister(String register) {
        // use the hashmap to get the register
        if (!registersMap.containsKey(register)) {
            printerr("Error: Invalid register name: " + register + "\n");
            return -1;
        }
        //print("Reading %s\n",register);
       // print("Reading %d from %s\n", memory[registersMap.get(register)], register);
        if (arguments.debug) {
            print("Reading %d from %s\n", memory[registersMap.get(register)], register);
        }
        return memory[registersMap.get(register)];
    }

    private static Scanner scanner = null;

    public static String inbox(String prompt) {
        try {
            if (scanner == null) {
                scanner = new Scanner(System.in);
            }
            
            if (!scanner.hasNextLine()) {
                // Reset scanner if no more input
                scanner.close();
                scanner = new Scanner(System.in);
                throw new IllegalArgumentException("No input available");
            }
            
            return scanner.nextLine();
            
        } catch (NoSuchElementException | IllegalStateException e) {
            // Reset scanner and rethrow as IllegalArgumentException
            if (scanner != null) {
                scanner.close();
            }
            scanner = new Scanner(System.in);
            throw new IllegalArgumentException("Invalid input operation");
        }
    }

    public static void box(String title, String message, String type) {
        String color;
        boolean iserror = false;
        switch (type.toLowerCase()) {
            case "error":
                iserror = true;
                color = "\u001B[31m"; // Red
                break;
            case "info":
            default:
                color = "\u001B[34m"; // Blue
                break;
        }

        String reset = "\u001B[0m";
        String[] lines = message.split("\n");
        if (iserror) {
            title = "Error: " + title;
            int maxLength = title.length();
            for (String line : lines) {
                if (line.length() > maxLength) {
                    maxLength = line.length();
                }
            }

            String border = "+" + "-".repeat(maxLength + 2) + "+";
            System.out.println(color + border);
            System.out.println("| " + title + " ".repeat(maxLength - title.length()) + " |");
            System.out.println(border);

            for (String line : lines) {
                System.out.println("| " + line + " ".repeat(maxLength - line.length()) + " |");
            }

            System.out.println(border + reset);
        } else {
            int maxLength = title.length();
            for (String line : lines) {
                if (line.length() > maxLength) {
                    maxLength = line.length();
                }
            }

            String border = "+" + "-".repeat(maxLength + 2) + "+";
            System.out.println(color + border);
            System.out.println("| " + title + " ".repeat(maxLength - title.length()) + " |");
            System.out.println(border);

            for (String line : lines) {
                System.out.println("| " + line + " ".repeat(maxLength - line.length()) + " |");
            }

            System.out.println(border + reset);
        }
    }
    // Overloaded method for backward compatibility
    public static void box(String title, String message) {
        box(title, message, "info");
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

    public static void WriteMemory(int memory[], int address, int value) {
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
    public static int ReadMemory(int memory[], int address) {
        // Directly access the element at the given index from the 'memory' array
        if (address < 0 || address >= MAX_MEMORY) {
            System.err.println("Error: Invalid memory address.");
            return -1;
        }
        else if (memory == null || memory.length != MAX_MEMORY) {
            System.err.println("Error: Memory array not initialized or incorrect size.");
            return -1;
        }
        return memory[address];
    }

    public static void print(String message, Object... args) {
        System.out.printf(message, args);
    }
    public static void printerr(String message, Object... args) {
        System.err.printf(message, args);
    }


//
}
