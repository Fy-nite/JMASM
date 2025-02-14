package org.Finite;

import org.Finite.common.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.Finite.common.print;
import static org.Finite.common.printerr;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStream;

import org.Finite.interp.instructions;

public class Functions {
    private static final Logger logger = LoggerFactory.getLogger(Functions.class);

    public static String include(String filename, String CurrentFileContents) {
        logger.debug("Including file: {}", filename);
        // Convert the dot notation to path
        String resourcePath = filename.replace("\"", "").replace(".", "/") + ".masm";
        
        try {
            // Get resource as stream from classpath
            ClassLoader classLoader = Functions.class.getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(resourcePath);
            if (inputStream == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            
            // Read the file
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }
            
            // Replace the include statement with the file contents
            String includeStatement = "#include \"" + filename.replace("\"", "") + "\"";
            return CurrentFileContents.replace(includeStatement, content.toString());
            
        } catch (IOException e) {
            logger.error("Failed to include file: {}", resourcePath, e);
            printerr("Error including file %s: %s\n", resourcePath, e.getMessage());
            return CurrentFileContents;
        }
    }

    public static void include(String filename, instructions instrs) {
        String resourcePath = filename.replace("\"", "").replace(".", "/") + ".masm";
        
        try {
            ClassLoader classLoader = Functions.class.getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(resourcePath);
            if (inputStream == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith(";")) {
                        if (line.startsWith("LBL ")) {
                            String labelName = line.substring(4).trim();
                            instrs.labelMap.put(labelName, instrs.length);
                            continue;
                        }
                        interp.instruction instr = new interp.instruction();
                        String[] parts = line.split("\\s+");
                        instr.name = parts[0];
                        if (parts.length > 1) instr.sop1 = parts[1];
                        if (parts.length > 2) instr.sop2 = parts[2];
                        instrs.instructions[instrs.length] = instr;
                        instrs.Memory[instrs.length] = instrs.length;
                        instrs.length++;
                    }
                }
            }
        } catch (IOException e) {
            printerr("Error including file %s: %s\n", resourcePath, e.getMessage());
        }
    }
    
    public void add(int[] memory, String reg1, String reg2) {
        if (!isValidRegister(reg1) || !isValidRegister(reg2)) {
            throw new IllegalArgumentException("Invalid register name");
        }
        int value1 = common.ReadRegister(reg1);
        int value2 = common.ReadRegister(reg2);
        int result = value1 + value2;
        common.WriteRegister(reg1, result);
    }

    public void sub(int[] memory, String reg1, String reg2) {
        if (!isValidRegister(reg1) || !isValidRegister(reg2)) {
            throw new IllegalArgumentException("Invalid register name");
        }
        int value1 = common.ReadRegister(reg1);
        int value2 = common.ReadRegister(reg2);
        int result = value1 - value2;
        common.WriteRegister(reg1, result);

    }

    public void mul(int[] memory, String reg1, String reg2) {
        if (!isValidRegister(reg1) || !isValidRegister(reg2)) {
            throw new IllegalArgumentException("Invalid register name");
        }
        int value1 = common.ReadRegister(reg1);
        int value2 = common.ReadRegister(reg2);
        int result = value1 * value2;
        common.WriteRegister(reg1, result);
    }

    public void div(int[] memory, String reg1, String reg2) {
        // read the register hashmap
        int value1 = common.ReadRegister(reg1);
        int value2 = common.ReadRegister(reg2);
        // div the values
        int result = value1 / value2;
        // write the result to the first register
        common.WriteRegister(reg1, result);
    }


    public void out(int[] memory, String fd, String source) {
        String value = "";
        int fileDescriptor;
        if (source == null) {
            return;
        }
        try {
            fileDescriptor = Integer.parseInt(fd);
        } catch (Exception e) {
            // If fd is a register, get its value
            fileDescriptor = common.ReadRegister(fd);
        }

        // Handle memory address starting with $
        if (source.startsWith("$")) {
            String addr = source.substring(1);
            try {
                // Check if it's a direct memory address
                int address = Integer.parseInt(addr);
                // First try to read as a number from memory
                int numValue = memory[address];
                if (numValue != 0) {
                    value = Integer.toString(numValue);
                } else {
                    // If zero, try reading as string
                    int i = 0;
                    while (memory[address + i] != 0) {
                        value += (char) memory[address + i];
                        i++;
                    }
                }
            } catch (Exception e) {
                // If not direct address, it's a register containing address
                int regAddr = common.ReadRegister(addr);
                // First try to read as a number
                int numValue = memory[regAddr];
                if (numValue != 0) {
                    value = Integer.toString(numValue);
                } else {
                    // If zero, try reading as string
                    int i = 0;
                    while (memory[regAddr + i] != 0) {
                        value += (char) memory[regAddr + i];
                        i++;
                    }
                }
            }
        } else {
            // Direct register or literal value
            try {
                // Try parsing as number first
                int numValue = Integer.parseInt(source);
                value = Integer.toString(numValue);
            } catch (NumberFormatException e) {
                try {
                    // Try reading from register
                    value = Integer.toString(common.ReadRegister(source));
                } catch (Exception ex) {
                    // If all else fails, treat as string literal
                    value = source;
                }
            }
        }

        if (fileDescriptor == 1) {
            print("%s", value);
        } else if (fileDescriptor == 2) {
            printerr("%s", value);
        }
        else {
            common.box("Error", "Invalid file descriptor: " + fileDescriptor, "error");
        }
    }

    public void in(int[] memory, String fd, String dest) {
        // Validate inputs first
        if (fd == null || dest == null) {
            throw new IllegalArgumentException("File descriptor and destination cannot be null");
        }

        // Validate file descriptor
        int fileDescriptor;
        try {
            fileDescriptor = Integer.parseInt(fd);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid file descriptor format: " + fd);
        }

        if (fileDescriptor != 0) {
            throw new IllegalArgumentException("Only stdin (fd 0) is supported");
        }

        // Validate destination format first
        if (!dest.startsWith("$")) {
            throw new IllegalArgumentException("Invalid destination format. Must be memory address ($)");
        }

        // Validate memory address format before attempting input
        try {
            int address = Integer.parseInt(dest.substring(1));
            if (address < 0 || address >= common.MAX_MEMORY) {
                throw new IllegalArgumentException("Memory address out of bounds: " + address);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid memory address format: " + dest);
        }

        // After all validation passes, try to read input
        try {
            String value = common.inbox("Enter input: ");
            int address = Integer.parseInt(dest.substring(1));
            
            // Write input to memory
            for (int i = 0; i < value.length(); i++) {
                memory[address + i] = value.charAt(i);
            }
            memory[address + value.length()] = 0; // Null terminate
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Input operation failed: " + e.getMessage());
        }
    }
    
    public void db(int[] memory, String... argz) {
        // split argz into two parts
        String[] args = argz[0].split(" ");
        // check if the first argument is a memory address
        if (args[0].matches("\\$\\d+")) {
            int address = Integer.parseInt(args[0].substring(1));
            String str = args[1].replaceAll("^\"|\"$", "");
            for (int i = 0; i < str.length(); i++) {
                memory[address + i] = str.charAt(i);
            }
        } else {
            print("Error: %s\n", args[0]);
            int address = Integer.parseInt(args[0]);
            String str = args[1].replaceAll("^\"|\"$", "");
            for (int i = 0; i < str.length(); i++) {
                memory[address + i] = str.charAt(i);
            }
        }

    }

    public void mov(int[] memory, String reg1, String reg2) {
        logger.trace("MOV {} {}", reg1, reg2);
        // Validate destination register first
        if (!isValidRegister(reg1)) {
            throw new IllegalArgumentException("Invalid destination register: " + reg1);
        }

        int value = 0;
        if (reg2.startsWith("$")) {
            try {
                int address = Integer.parseInt(reg2.substring(1));
                value = memory[address];
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid memory address: " + reg2);
            }
        } else {
            try {
                value = Integer.parseInt(reg2);
            } catch (NumberFormatException e) {
                if (!isValidRegister(reg2)) {
                    throw new IllegalArgumentException("Invalid source register: " + reg2);
                }
                value = common.ReadRegister(reg2);
            }
        }
        common.WriteRegister(reg1, value);
    }

    public void ret(instructions instr)
    {
        // pop a value off the stack at last 1024 bytes of memory and set rip to it
        // stack is 1024 bytes long and goes from the top of memory down
        int value = common.ReadMemory(instr.Memory, common.MAX_MEMORY - 1024);
        common.WriteRegister("RIP", value);
        common.WriteMemory(instr.Memory, common.MAX_MEMORY - 1024, 0);
    }

    // Helper method to validate registers
    private boolean isValidRegister(String reg) {
        return reg != null && (
            reg.equals("RAX") || 
            reg.equals("RBX") || 
            reg.equals("RCX") || 
            reg.equals("RDX") ||
            reg.equals("RSI") ||
            reg.equals("RDI") ||
            reg.equals("RIP") ||
            reg.equals("RSP") ||
            reg.equals("RBP") ||
            reg.equals("R0") ||
            reg.equals("R1") ||
            reg.equals("R2") ||
            reg.equals("R3") ||
            reg.equals("R4") ||
            reg.equals("R5") ||
            reg.equals("R6") ||
            reg.equals("R7") ||
            reg.equals("R8") ||
            reg.equals("R9") ||
            reg.equals("R10") ||
            reg.equals("R11") ||
            reg.equals("R12") ||
            reg.equals("R13") ||
            reg.equals("R14") ||
            reg.equals("R15") ||
            reg.equals("RFLAGS")
        );
    }

    public void cmp(int[] memory, String reg1, String reg2) {
        if (!isValidRegister(reg1)) {
            throw new IllegalArgumentException("Invalid register name: " + reg1);
        }
        
        int value1 = common.ReadRegister(reg1);
        int value2;

        try {
            value2 = Integer.parseInt(reg2);
        } catch (NumberFormatException e) {
            if (!isValidRegister(reg2)) {
                throw new IllegalArgumentException("Invalid register name: " + reg2);
            }
            value2 = common.ReadRegister(reg2);
        }

        common.WriteRegister("RFLAGS", (value1 == value2) ? 1 : 0);
    }

    public void cout(int[] memory, String fd, String reg) {
        // read the register hashmap
        int value = common.ReadRegister(reg);
        // print the value
        if (fd.equals("1")) {
            // convert to character
            char c = (char) value;
            print("%c", c);
        } else if (fd.equals("2")) {
            // convert to character
            char c = (char) value;
            printerr("%c", c);
        }
    }


    public static void jmp(int[] memory, String target) {
        print("DEBUG: Attempting to jump to target: %s\n", target);
        try {
            // Try parsing as number first
            Integer.parseInt(target); // This will throw NumberFormatException if not a number
            int value = parseTarget(target);
            if (value == -1) {
                common.box("Error", "Unknown address or label: " + target, "error");
                return;
            }
            print("DEBUG: Jump successful - Setting RIP to %d\n", value);
            common.WriteRegister("RIP", value);
        } catch (NumberFormatException e) {
            print("DEBUG: Jump failed - invalid number format: %s\n", target);
            throw e; // Re-throw the NumberFormatException
        } catch (Exception e) {
            print("DEBUG: Jump failed with exception: %s\n", e.getMessage());
            throw e;
        }
    }

    public void jmp(int[] memory, String target, instructions instrs) {
        logger.debug("JMP to target: {}", target);
        if (target == null || instrs == null) {
            print("DEBUG: ERROR - Null target or instructions\n");
            throw new NullPointerException("Target or instructions cannot be null");
        }
        print("DEBUG: Attempting to jump to target: %s with instructions context\n", target);
        int value = parseTarget(target, instrs);
        if (value == -1) {
            print("DEBUG: Jump failed - invalid target: %s\n", target);
            common.box("Error", "Unknown address or label: " + target, "error");
            return;
        }
        print("DEBUG: Jump successful - Setting RIP to %d\n", value);
        common.WriteRegister("RIP", value);
    }

    private static int parseTarget(String target) {
        print("DEBUG: Parsing target: %s\n", target);
        try {
            int value = Integer.parseInt(target);
            print("DEBUG: Target parsed as direct integer: %d\n", value);
            return value;
        } catch (NumberFormatException e) {
            // Do not catch NumberFormatException here, let it propagate up
            throw e;
        }
    }

    private static int parseTarget(String target, instructions instrs) {
        if (target == null || instrs == null) {
            print("DEBUG: ERROR - Null target or instructions in parseTarget\n");
            return -1;
        }
        print("DEBUG: Parsing target with instructions context: %s\n", target);
        
        // If it's a label reference
        if (target.startsWith("#")) {
            String labelName = target.substring(1);
            print("DEBUG: Processing as label: %s\n", labelName);
            print("DEBUG: Label map contents: %s\n", instrs.labelMap);
            
            if (instrs.labelMap == null) {
                print("DEBUG: ERROR - Label map is null!\n");
                return -1;
            }
            
            Integer labelAddress = instrs.labelMap.get(labelName);
            print("DEBUG: Label lookup result: %s\n", labelAddress);
            
            if (labelAddress != null) {
                print("DEBUG: Found label address: %d\n", labelAddress);
                return labelAddress;
            } else {
                print("DEBUG: Label '%s' not found in map\n", labelName);
                return -1;
            }
        }
        
        // Try parsing as direct number
        try {
            int value = Integer.parseInt(target);
            print("DEBUG: Parsed as direct number: %d\n", value);
            return value;
        } catch (NumberFormatException e) {
            print("DEBUG: Not a number, trying as register name: %s\n", target);
            
            // Try as register
            try {
                int regValue = common.ReadRegister(target);
                print("DEBUG: Found in register with value: %d\n", regValue);
                return regValue;
            } catch (Exception ex) {
                print("DEBUG: Register lookup failed: %s\n", ex.getMessage());
                return -1;
            }
        }
    }
}

