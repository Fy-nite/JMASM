package org.finite;

import org.finite.Exceptions.MASMException;

import java.util.HashMap;
import java.util.Map;
import java.util.*;
import java.io.*;

import static org.finite.interp.arguments;

public class common {
    public static boolean isRunning = true;
    public static boolean exitOnHLT = true;
    String modulesDir = "JMASM/modules";
    String configDir = "JMASM/config";
    String configPath = configDir + "/config.toml";
    String modulesPath = modulesDir + "/modules.toml";
    String[] modules = ReadResourceFile.readDir(modulesPath);
    public static OutputStream outputStream = System.out;
    // THIS IS FINAL~~!!?!??!?!??!?!?!??!?!?!?!??!?!?
    public static final int MAX_MEMORY = 32767; // are you angry?
    public static final int STACK_SIZE = 1024; // stack size
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
    public static String[] registers = {"RAX", "RBX", "RCX", "RDX", "RBP", "RSP", "RIP", "RDI", "RSI", "R0", "R1", "R2", "R3", "R4", "R5", "R6", "R7", "R8", "R9", "R10", "R11", "R12", "R13", "R14", "R15", "RFLAGS", "FPR0", "FPR1", "FPR2", "FPR3", "FPR4", "FPR5", "FPR6", "FPR7", "FPR8", "FPR9", "FPR10", "FPR11", "FPR12", "FPR13", "FPR14", "FPR15"};
    public static String[] instructions = {"MOV", "ADD", "SUB", "MUL", "DIV", "AND", "OR", "XOR", "NOT", "SHL", "SHR", "CMP", "JMP", "JE", "JNE", "JG", "JGE", "JL", "JLE", "CALL", "RET", "PUSH", "POP", "HLT", "NOP","OUT"};
    public static int[] memory = new int[MAX_MEMORY];
    public static float version = 1;
    public static long startTime = System.currentTimeMillis();
    
    public static String joined(String[] items, String separator) {
        String output = "";
        for (int i = 0; i < items.length; i++) {
            output += items[i];
            if (i < items.length - 1) {
                output += separator;
            }
        }
        return output;
    }
    
    public static String[] information = {
        "max Memory: " + memory.length,
        "Number of Registers: " + Integer.toString(registers.length),
        "Registers: " + joined(registers,","),
        "Number of instructions: " +  Integer.toString(instructions.length),
        "Instructions: " + joined(instructions,","),
        "Version: " + Float.toString(version)
    };

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

    // Add floating-point registers (16 double-precision)
    public static double[] floatRegisters = new double[16];

    // Add mapping for floating-point registers
    public static Map<String, Integer> floatRegistersMap = new HashMap<String, Integer>() {{
        put("FPR0", 0);  put("FPR1", 1);  put("FPR2", 2);  put("FPR3", 3);
        put("FPR4", 4);  put("FPR5", 5);  put("FPR6", 6);  put("FPR7", 7);
        put("FPR8", 8);  put("FPR9", 9);  put("FPR10", 10); put("FPR11", 11);
        put("FPR12", 12); put("FPR13", 13); put("FPR14", 14); put("FPR15", 15);
    }};

    // Floating-point register accessors
    public static double ReadFloatRegister(String reg) {
        if (!floatRegistersMap.containsKey(reg)) {
            printerr("Error: Invalid floating-point register name: " + reg + "\n");
            return Double.NaN;
        }
        return floatRegisters[floatRegistersMap.get(reg)];
    }

    public static void WriteFloatRegister(String reg, double value) {
        if (!floatRegistersMap.containsKey(reg)) {
            printerr("Error: Invalid floating-point register name: " + reg + "\n");
            return;
        }
        floatRegisters[floatRegistersMap.get(reg)] = value;
    }

    // Bit positions for integer flags in RFLAGS
    public static final int ZF_BIT = 0; // Zero Flag
    public static final int SF_BIT = 1; // Sign Flag
    public static final int CF_BIT = 2; // Carry Flag
    public static final int OF_BIT = 3; // Overflow Flag

    // Bit positions for floating-point flags in RFLAGS (upper bits)
    public static final int FE_BIT  = 16; // FP Equal
    public static final int FLT_BIT = 17; // FP Less Than
    public static final int FGT_BIT = 18; // FP Greater Than
    public static final int FUO_BIT = 19; // FP Unordered (NaN)

    // Set/Clear/Test integer flags
    public static void setZF(boolean val) { setFlag(ZF_BIT, val); }
    public static void setSF(boolean val) { setFlag(SF_BIT, val); }
    public static void setCF(boolean val) { setFlag(CF_BIT, val); }
    public static void setOF(boolean val) { setFlag(OF_BIT, val); }
    public static boolean getZF() { return getFlag(ZF_BIT); }
    public static boolean getSF() { return getFlag(SF_BIT); }
    public static boolean getCF() { return getFlag(CF_BIT); }
    public static boolean getOF() { return getFlag(OF_BIT); }

    // Set/Clear/Test floating-point flags
    public static void setFE(boolean val) { setFlag(FE_BIT, val); }
    public static void setFLT(boolean val) { setFlag(FLT_BIT, val); }
    public static void setFGT(boolean val) { setFlag(FGT_BIT, val); }
    public static void setFUO(boolean val) { setFlag(FUO_BIT, val); }
    public static boolean getFE() { return getFlag(FE_BIT); }
    public static boolean getFLT() { return getFlag(FLT_BIT); }
    public static boolean getFGT() { return getFlag(FGT_BIT); }
    public static boolean getFUO() { return getFlag(FUO_BIT); }

    private static void setFlag(int bit, boolean val) {
        int flags = ReadRegister("RFLAGS");
        if (val) {
            flags |= (1 << bit);
        } else {
            flags &= ~(1 << bit);
        }
        WriteRegister("RFLAGS", flags);
    }
    private static boolean getFlag(int bit) {
        int flags = ReadRegister("RFLAGS");
        return ((flags >> bit) & 1) != 0;
    }

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

    public static void WriteToFile(String filename, String contents)
    {
        try {
            FileWriter writer = new FileWriter(filename);
            writer.write(contents);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void dbgprint(Object...  message)
    {
        if (ArgumentParser.Args.debug)
        {
            print(message + "\n");
        }
    }

    public static void dbgprinterr(String... message)
    {
        if (ArgumentParser.Args.debug)
        {
            printerr(message + "\n");
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
        if (ArgumentParser.Args.debug)
        {

            print("Writing %d to %s\n", value, Register);
        }
        memory[registersMap.get(Register)] = value;
    }

    public static int ReadRegister(String register) {
        if (!registersMap.containsKey(register)) {
            printerr("Error: Invalid register name: " + register + "\n");
            return -1;
        }
        if (ArgumentParser.Args.debug) {
            print("Reading %d from %s\n", memory[registersMap.get(register)], register);
        }
        return memory[registersMap.get(register)];
    }

    private static Scanner scanner = null;
    //TODO: remove this function at somepoint because wtf?
    @Deprecated(forRemoval = true, since = "1.0")
    public static String inbox(String prompt) {
        try {
            if (scanner == null) {
                scanner = new Scanner(System.in);
            }
            
            if (!scanner.hasNextLine()) {
                // Reset scanner if no more input
                scanner.close();
                scanner = new Scanner(System.in);
                throw new MASMException("Error reading input", 0, "", "No more input");
            }
            
            return scanner.nextLine();
            
        } catch (NoSuchElementException | IllegalStateException e) {
            // Reset scanner and rethrow as IllegalArgumentException
            if (scanner != null) {
                scanner.close();
            }
            scanner = new Scanner(System.in);
            throw new MASMException("Error reading input", 0, "", e.getMessage());
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
                color = "\u001B[32m"; // Green
                break;
            case "warning":
                color = "\u001B[33m"; // Yellow
                break;
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
    //TODO: tell other people that this exists, it's handy
    public static void print(String message, Object... args) {
        System.out.printf(message, args);
    }
    public static void printerr(String message, Object... args) {
        System.err.printf(message, args);
    }


//
}
