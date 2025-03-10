package org.finite;

import static org.finite.Common.common.print;
import static org.finite.Common.common.printerr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.finite.Common.common;
import org.finite.Exceptions.MASMException;
import org.finite.interp.instructions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Functions {

    private static final Logger logger = LoggerFactory.getLogger(
        Functions.class
    );

    public static String include(String filename, String CurrentFileContents) {
        logger.debug("Including file: {}", filename);
        // Convert the dot notation to path
        String resourcePath =
            filename.replace("\"", "").replace(".", "/") + ".masm";
        try {
            // Get resource as stream from classpath
            ClassLoader classLoader = Functions.class.getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(
                resourcePath
            );
            if (inputStream == null) {
                logger.debug(
                    "Resource not found in classpath, trying local directory"
                );
                File localFile = new File(resourcePath);
                if (!localFile.exists()) {
                    // Also try current working directory
                    localFile = new File(
                        System.getProperty("user.dir"),
                        resourcePath
                    );
                }
                if (localFile.exists()) {
                    inputStream = new FileInputStream(localFile);
                } else {
                    throw new IOException(
                        "Resource not found: " + resourcePath
                    );
                }
            }

            // Read the file
            StringBuilder content = new StringBuilder();
            try (
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream)
                )
            ) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }

            // Replace the include statement with the file contents
            String includeStatement =
                "#include \"" + filename.replace("\"", "") + "\"";
            return CurrentFileContents.replace(
                includeStatement,
                content.toString()
            );
        } catch (IOException e) {
            logger.error("Failed to include file: {}", resourcePath, e);
            printerr(
                "Error including file %s: %s\n",
                resourcePath,
                e.getMessage()
            );
            return CurrentFileContents;
        }
    }
    public static void hlt()
    {
        common.isRunning = false;
        if (common.exitOnHLT) {
            System.exit(0);
        }
    }
    // Stack operations using last 1024 bytes of memory
    public static void push(int[] memory, String reg1, instructions instrs) {
        try {
            int sp = common.ReadRegister("RSP");
            if (sp <= common.MAX_MEMORY - 1024) {
                throw new MASMException("Stack overflow", instrs.currentLine, instrs.currentlineContents, "Error in instruction: push");
            }
            int value = common.ReadRegister(reg1);
            sp--;
            common.WriteMemory(memory, sp, value);
            common.WriteRegister("RSP", sp);
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: push");
        }
    }

    public static void pop(int[] memory, String reg1, instructions instrs) {
        try {
            int sp = common.ReadRegister("RSP");
            if (sp >= common.MAX_MEMORY) {
                throw new MASMException("Stack underflow", instrs.currentLine, instrs.currentlineContents, "Error in instruction: pop");
            }
            int value = common.ReadMemory(memory, sp);
            sp++;
            common.WriteRegister("RSP", sp);
            common.WriteRegister(reg1, value);
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: pop");
        }
    }

    public static void include(String filename, instructions instrs) {
        String resourcePath =
            filename.replace("\"", "").replace(".", "/") + ".masm";

        try {
            ClassLoader classLoader = Functions.class.getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(
                resourcePath
            );
            if (inputStream == null) {
                logger.debug(
                    "Resource not found in classpath, trying local directory"
                );
                File localFile = new File(resourcePath);
                if (!localFile.exists()) {
                    // Also try current working directory
                    localFile = new File(
                        System.getProperty("user.dir"),
                        resourcePath
                    );
                }
                if (localFile.exists()) {
                    inputStream = new FileInputStream(localFile);
                } else {
                    throw new IOException(
                        "Resource not found: " + resourcePath
                    );
                }
            }

            try (
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream)
                )
            ) {
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
            printerr(
                "Error including file %s: %s\n",
                resourcePath,
                e.getMessage()
            );
        }
    }

    public void add(int[] memory, String reg1, String reg2, instructions instrs) {
        try {
            if (!isValidRegister(reg1) || !isValidRegister(reg2)) {
                throw new MASMException("Invalid register name", instrs.currentLine, instrs.currentlineContents, "Error in instruction: add");
            }
            int value1 = common.ReadRegister(reg1);
            int value2 = common.ReadRegister(reg2);
            int result = value1 + value2;
            common.WriteRegister(reg1, result);
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: add");
        }
    }

    public void sub(int[] memory, String reg1, String reg2, instructions instrs) {
        try {
            if (!isValidRegister(reg1) || !isValidRegister(reg2)) {
                throw new MASMException("Invalid register name", instrs.currentLine, instrs.currentlineContents, "Error in instruction: sub");
            }
            int value1 = common.ReadRegister(reg1);
            int value2 = common.ReadRegister(reg2);
            int result = value1 - value2;
            common.WriteRegister(reg1, result);
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: sub");
        }
    }

    public void mul(int[] memory, String reg1, String reg2, instructions instrs) {
        try {
            if (!isValidRegister(reg1) || !isValidRegister(reg2)) {
                throw new MASMException("Invalid register name", instrs.currentLine, instrs.currentlineContents, "Error in instruction: mul");
            }
            int value1 = common.ReadRegister(reg1);
            int value2 = common.ReadRegister(reg2);
            int result = value1 * value2;
            common.WriteRegister(reg1, result);
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: mul");
        }
    }

    public void div(int[] memory, String reg1, String reg2, instructions instrs) {
        try {
            int value1 = common.ReadRegister(reg1);
            int value2 = common.ReadRegister(reg2);
            int result = value1 / value2;
            common.WriteRegister(reg1, result);
        } catch (ArithmeticException e) {
            throw new MASMException("Division by zero", instrs.currentLine, instrs.currentlineContents, "Error in instruction: div");
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: div");
        }
    }

    private String processEscapeSequences(String input) {
        StringBuilder result = new StringBuilder();
        boolean inEscape = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (inEscape) {
                switch (c) {
                    case 'n':
                        result.append('\n');
                        break; // newline
                    case 't':
                        result.append('\t');
                        break; // tab
                    case 'r':
                        result.append('\r');
                        break; // carriage return
                    case 'b':
                        result.append('\b');
                        break; // backspace
                    case 'f':
                        result.append('\f');
                        break; // form feed
                    case 'a':
                        result.append('\007');
                        break; // bell/alert
                    case '\\':
                        result.append('\\');
                        break; // backslash
                    case '0':
                        result.append('\0');
                        break; // null character
                    default:
                        result.append(c);
                }
                inEscape = false;
            } else if (c == '\\') {
                inEscape = true;
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

    public void out(int[] memory, String fd, String source, instructions instrs) {
        try {
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
                    // Always try to read as null-terminated string first
                    StringBuilder sb = new StringBuilder();
                    int i = 0;
                    while (
                        address + i < memory.length && memory[address + i] != 0
                    ) {
                        sb.append((char) memory[address + i]);
                        i++;
                    }
                    value = processEscapeSequences(sb.toString());

                    // If empty, try as number
                    if (value.isEmpty()) {
                        value = Integer.toString(memory[address]);
                    }
                } catch (Exception e) {
                    // Handle register containing address
                    int regAddr = common.ReadRegister(addr);
                    StringBuilder sb = new StringBuilder();
                    int i = 0;
                    while (
                        regAddr + i < memory.length && memory[regAddr + i] != 0
                    ) {
                        sb.append((char) memory[regAddr + i]);
                        i++;
                    }
                    value = processEscapeSequences(sb.toString());

                    // If empty, try as number
                    if (value.isEmpty()) {
                        value = Integer.toString(memory[regAddr]);
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
                        // If all else fails, treat as string literal with escape sequences
                        value = processEscapeSequences(source);
                    }
                }
            }
            //print("DEBUG: Writing to file descriptor %d: %s\n", fileDescriptor, value);
            if (fileDescriptor == 1) {
                print("%s", value);
            } else if (fileDescriptor == 2) {
                printerr("%s", value);
            } else {
                common.box(
                    "Error",
                    "Invalid file descriptor: " + fileDescriptor,
                    "error"
                );
            }
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: out");
        }
    }

    //HOw to use
    // in memory
    //
    public void in(int[] memory, String fd, String dest, instructions instrs) {
        try {
            // Validate inputs first
            if (fd == null || dest == null) {
                throw new IllegalArgumentException(
                    "File descriptor and destination cannot be null"
                );
            }

            // Validate file descriptor
            int fileDescriptor;
            try {
                fileDescriptor = Integer.parseInt(fd);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                    "Invalid file descriptor format: " + fd
                );
            }

            if (fileDescriptor != 1) {
                throw new IllegalArgumentException(
                    "Only stdin (fd 1) is supported"
                );
            }

            // Validate destination format first
            if (!dest.startsWith("$")) {
                throw new IllegalArgumentException(
                    "Invalid destination format. Must be memory address ($)"
                );
            }

            // Validate memory address format before attempting input
            try {
                int address = Integer.parseInt(dest.substring(1));
                if (address < 0 || address >= common.MAX_MEMORY) {
                    throw new IllegalArgumentException(
                        "Memory address out of bounds: " + address
                    );
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                    "Invalid memory address format: " + dest
                );
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
                throw new IllegalArgumentException(
                    "Input operation failed: " + e.getMessage()
                );
            }
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: in");
        }
    }

    public void db(int[] memory, instructions instrs, String... argz) {
        try {
            if (argz == null || argz.length == 0) {
                throw new MASMException(
                    "DB instruction requires arguments",
                    instrs.currentLine,
                    instrs.currentlineContents,
                    "Error in instruction: db"
                );
            }

            String fullArg = String.join(" ", argz).trim();
            logger.debug("DB instruction received: '{}'", fullArg);

            // Skip any extra whitespace after DB
            fullArg = fullArg.replaceAll("\\s+", " ");

            String[] parts = fullArg.split(" ", 2);
            if (parts.length < 2) {
                logger.error("Invalid DB format: '{}'", fullArg);
                throw new MASMException(
                    "DB instruction requires address and data",
                    instrs.currentLine,
                    instrs.currentlineContents,
                    "Error in instruction: db"
                );
            }

            String addressPart = parts[0];
            String dataPart = parts[1];

            logger.debug(
                "DB parsed - Address: '{}', Data: '{}'",
                addressPart,
                dataPart
            );

            // Parse memory address
            int memoryAddress;
            try {
                if (addressPart.startsWith("$")) {
                    String addressStr = addressPart.substring(1);
                    try {
                        memoryAddress = Integer.parseInt(addressStr);
                    } catch (NumberFormatException e) {
                        memoryAddress = common.ReadRegister(addressStr);
                    }
                } else {
                    memoryAddress = Integer.parseInt(addressPart);
                }

                if (memoryAddress < 0 || memoryAddress >= common.MAX_MEMORY) {
                    throw new MASMException(
                        "Invalid memory address: " + memoryAddress,
                        instrs.currentLine,
                        instrs.currentlineContents,
                        "Error in instruction: db"
                    );
                }
            } catch (NumberFormatException e) {
                logger.error("Invalid memory address: '{}'", addressPart);
                throw new MASMException(
                    "Invalid memory address: " + addressPart,
                    instrs.currentLine,
                    instrs.currentlineContents,
                    "Error in instruction: db"
                );
            }

            // Clean up the data string
            if (dataPart.startsWith("\"") && dataPart.endsWith("\"")) {
                dataPart = dataPart.substring(1, dataPart.length() - 1);
            }

            logger.debug(
                "Writing '{}' to memory address {}",
                dataPart,
                memoryAddress
            );
            // Write to memory
            for (int i = 0; i < dataPart.length(); i++) {
                memory[memoryAddress + i] = dataPart.charAt(i);
            }
            memory[memoryAddress + dataPart.length()] = 0; // Null terminate
        } catch (Exception e) {
            throw new MASMException(
                e.getMessage(),
                instrs.currentLine,
                instrs.currentlineContents,
                "Error in instruction: db"
            );
        }
    }

    public void mov(int[] memory, String dest, String source, instructions instrs) {
        try {
            logger.trace("MOV {} {}", dest, source);
            
            int value;
            // Handle source operand first
            if (source.startsWith("$")) {
                // Source is memory address
                try {
                    int address = Integer.parseInt(source.substring(1));
                    value = memory[address];
                } catch (NumberFormatException e) {
                    throw new MASMException("Invalid memory address: " + source, instrs.currentLine, instrs.currentlineContents, "Error in instruction: mov");
                }
            } else {
                // Source is register or immediate value
                try {
                    value = Integer.parseInt(source);
                } catch (NumberFormatException e) {
                    if (!isValidRegister(source)) {
                        throw new MASMException("Invalid source: " + source, instrs.currentLine, instrs.currentlineContents, "Error in instruction: mov");
                    }
                    value = common.ReadRegister(source);
                }
            }

            // Handle destination operand
            if (dest.startsWith("$")) {
                // Destination is memory address
                try {
                    int address = Integer.parseInt(dest.substring(1));
                    memory[address] = value;
                } catch (NumberFormatException e) {
                    throw new MASMException("Invalid memory address: " + dest, instrs.currentLine, instrs.currentlineContents, "Error in instruction: mov");
                }
            } else {
                // Destination is register
                if (!isValidRegister(dest)) {
                    throw new MASMException("Invalid destination register: " + dest, instrs.currentLine, instrs.currentlineContents, "Error in instruction: mov");
                }
                common.WriteRegister(dest, value);
            }
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: mov");
        }
    }

    public void ret(instructions instrs) {
        try {
            // Get current stack pointer
            int sp = common.ReadRegister("RSP");
            if (sp >= common.MAX_MEMORY) {
                throw new IllegalStateException("Stack underflow in ret");
            }

            // Get return address from stack
            int returnAddr = common.ReadMemory(instrs.Memory, sp);

            // Increment stack pointer
            sp++;
            common.WriteRegister("RSP", sp);

            // Jump to return address
            common.WriteRegister("RIP", returnAddr);
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: ret");
        }
    }

    // Helper method to validate registers
    private boolean isValidRegister(String reg) {
        return (
            reg != null &&
            (reg.equals("RAX") ||
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
                reg.equals("RFLAGS"))
        );
    }

    public void cmp(int[] memory, String reg1, String reg2, instructions instrs) {
        try {
            int value1;
            int value2;
            print("Comparing %s and %s\n", reg1, reg2);
            // Handle first operand
            if (reg1.startsWith("$")) {
                try {
                    int address = Integer.parseInt(reg1.substring(1));
                    value1 = memory[address];
                } catch (NumberFormatException e) {
                    throw new MASMException("Invalid memory address: " + reg1, instrs.currentLine, instrs.currentlineContents, "Error in instruction: cmp");
                }
            } else {
                if (!isValidRegister(reg1)) {
                    throw new MASMException("Invalid register name: " + reg1, instrs.currentLine, instrs.currentlineContents, "Error in instruction: cmp");
                }
                value1 = common.ReadRegister(reg1);
            }

            // Handle second operand
            try {
                value2 = Integer.parseInt(reg2);
            } catch (NumberFormatException e) {
                if (reg2.startsWith("$")) {
                    try {
                        int address = Integer.parseInt(reg2.substring(1));
                        value2 = memory[address];
                    } catch (NumberFormatException ex) {
                        throw new MASMException("Invalid memory address: " + reg2, instrs.currentLine, instrs.currentlineContents, "Error in instruction: cmp");
                    }
                } else {
                    if (!isValidRegister(reg2)) {
                        throw new MASMException("Invalid register name: " + reg2, instrs.currentLine, instrs.currentlineContents, "Error in instruction: cmp");
                    }
                    value2 = common.ReadRegister(reg2);
                }
            }

            common.WriteRegister("RFLAGS", (value1 == value2) ? 1 : 0);
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: cmp");
        }
    }

    public void cout(int[] memory, String fd, String reg, instructions instrs) {
        try {
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
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: cout");
        }
    }

    public static void inc(int[] memory, String reg, instructions instrs) {
        try {
            // read the register hashmap
            int value = common.ReadRegister(reg);
            // increment the value
            value++;
            // write the value back to the register
            common.WriteRegister(reg, value);
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: inc");
        }
    }

    public static void dec(int[] memory, String reg, instructions instrs) {
        try {
            // read the register hashmap
            int value = common.ReadRegister(reg);
            // decrement the value
            value--;
            // write the value back to the register
            common.WriteRegister(reg, value);
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: dec");
        }
    }

    public static void shl(int[] memory, String reg1, String reg2, instructions instrs) {
        try {
            // read the register hashmap
            int value1 = common.ReadRegister(reg1);
            int value2 = common.ReadRegister(reg2);
            // shift the value
            value1 = value1 << value2;
            // write the value back to the register
            common.WriteRegister(reg1, value1);
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: shl");
        }
    }

    public static void shr(int[] memory, String reg1, String reg2, instructions instrs) {
        try {
            // read the register hashmap
            int value1 = common.ReadRegister(reg1);
            int value2 = common.ReadRegister(reg2);
            // shift the value
            value1 = value1 >> value2;
            // write the value back to the register
            common.WriteRegister(reg1, value1);
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: shr");
        }
    }

    public static void and(int[] memory, String reg1, String reg2, instructions instrs) {
        try {
            // read the register hashmap
            int value1 = common.ReadRegister(reg1);
            int value2 = common.ReadRegister(reg2);
            // and the values
            int result = value1 & value2;
            // write the result back to the register
            common.WriteRegister(reg1, result);
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: and");
        }
    }

    public static void or(int[] memory, String reg1, String reg2, instructions instrs) {
        try {
            // read the register hashmap
            int value1 = common.ReadRegister(reg1);
            int value2 = common.ReadRegister(reg2);
            // or the values
            int result = value1 | value2;
            // write the result back to the register
            common.WriteRegister(reg1, result);
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: or");
        }
    }

    public static void xor(int[] memory, String reg1, String reg2, instructions instrs) {
        try {
            // read the register hashmap
            int value1 = common.ReadRegister(reg1);
            int value2 = common.ReadRegister(reg2);
            // xor the values
            int result = value1 ^ value2;
            // write the result back to the register
            common.WriteRegister(reg1, result);
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: xor");
        }
    }

    public static void not(int[] memory, String reg, instructions instrs) {
        try {
            // read the register hashmap
            int value = common.ReadRegister(reg);
            // not the value
            value = ~value;
            // write the value back to the register
            common.WriteRegister(reg, value);
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: not");
        }
    }

    public static void neg(int[] memory, String reg, instructions instrs) {
        try {
            // read the register hashmap
            int value = common.ReadRegister(reg);
            // negate the value
            value = -value;
            // write the value back to the register
            common.WriteRegister(reg, value);
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: neg");
        }
    }

    public void call(int[] memory, String target, instructions instrs) {
        try {
            logger.debug("CALL to target: {}", target);
            if (target == null || instrs == null) {
                throw new NullPointerException("Target or instructions cannot be null");
            }

            // Save current instruction pointer
            int currentRIP = common.ReadRegister("RIP");
            
            // Push return address (next instruction) onto stack
            common.WriteRegister("RIP", currentRIP);
            Functions.push(memory, "RIP", instrs);

            try {
                // Handle labels similar to jmp
                if (target.startsWith("#")) {
                    String labelName = target.substring(1);
                    Integer labelAddress = instrs.labelMap.get(labelName);
                    if (labelAddress == null) {
                        common.box("Error", "Unknown label: " + labelName, "error");
                        return;
                    }
                    common.WriteRegister("RIP", labelAddress - 1);
                    return;
                }

                // Handle direct addresses
                int targetAddress = parseTarget(target, instrs);
                if (targetAddress == -1) {
                    common.box("Error", "Invalid call target: " + target, "error");
                    return;
                }
                common.WriteRegister("RIP", targetAddress - 1);
            } catch (Exception e) {
                common.box("Error", "Call failed: " + e.getMessage(), "error");
            }
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: call");
        }
    }

    public void jeq(int[] memory, String target, instructions instrs) {
        try {
            // read the register hashmap
            int value = common.ReadRegister("RFLAGS");
            String fixed = target.substring(1);
            int targetz;
            try
            {
                targetz = instrs.labelMap.get(fixed);
            }
            catch (Exception e)
            {
                throw new MASMException("Invalid label", instrs.currentLine, instrs.currentlineContents, "Error in instruction: jeq");
            }

            // debug print the label map

            for (String key : instrs.labelMap.keySet()) {
                logger.debug(
                    "Label: {} Address: {}",
                    key,
                    instrs.labelMap.get(key)
                );
            }

            // if the value is 1 jump to the target

            if (value == 1) {
                jmp(memory, target, instrs);
            }
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: jeq");
        }
    }

    public void jne(int[] memory, String target, instructions instrs) {
        try {
            logger.debug("JNE instruction with target: {}", target);
            
            if (target == null || instrs == null) {
                throw new NullPointerException("Target or instructions cannot be null");
            }

            // read the RFLAGS register
            int value = common.ReadRegister("RFLAGS");
            logger.debug("RFLAGS value: {}", value);

            // Get label name by removing the '#' prefix
            if (!target.startsWith("#")) {
                common.box("Error", "JNE target must be a label (start with #)", "error");
                return;
            }

            String labelName = target.substring(1);
            Integer targetAddress = instrs.labelMap.get(labelName);
            
            if (targetAddress == null) {
                common.box("Error", "Unknown label: " + labelName, "error");
                return;
            }

            logger.debug("Target address for label {}: {}", labelName, targetAddress);

            // Jump if RFLAGS is 0 (values were not equal)
            if (value == 0) {
                common.WriteRegister("RIP", targetAddress - 1);
                logger.debug("Jump taken - setting RIP to {}", targetAddress - 1);
            } else {
                logger.debug("Jump not taken - RFLAGS was 1");
            }
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: jne");
        }
    }

    public void jmp(int[] memory, String target, instructions instrs) {
        try {
            logger.debug("JMP to target: {}", target);
            int value;
            if (target == null || instrs == null) {
                //  print("DEBUG: ERROR - Null target or instructions\n");
                throw new NullPointerException(
                    "Target or instructions cannot be null"
                );
            }
            //print("DEBUG: Attempting to jump to target: %s with instructions context\n", target);
            try
            {

            value = parseTarget(target, instrs);
            if (value == -1) {
                //print("DEBUG: Jump failed - invalid target: %s\n", target);
                common.box("Error", "Unknown address or label: " + target, "error");
                return;
            }
            common.WriteRegister("RIP", value - 1);
            } catch (Exception e) {
                // try as label
                value = instrs.labelMap.get(target.substring(1));
                if (value == -1) {
                    //print("DEBUG: Jump failed - invalid target: %s\n", target);
                    common.box("Error", "Unknown address or label: " + target, "error");
                    return;
                }
                common.WriteRegister("RIP", value - 1);
            }
            //print("DEBUG: Jump successful - Setting RIP to %d\n", value);
            //TODO: figure out why -1 saves stdlib functions from dying?
            common.WriteRegister("RIP", value - 1);
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: jmp");
        }
    }

    private static int parseTarget(String target) {
        // print("DEBUG: Parsing target: %s\n", target);
        try {
            int value = Integer.parseInt(target);
            //  print("DEBUG: Target parsed as direct integer: %d\n", value);
            return value;
        } catch (NumberFormatException e) {
            // Do not catch NumberFormatException here, let it propagate up
            throw e;
        }
    }

    private static int parseTarget(String target, instructions instrs) {
        if (target == null || instrs == null) {
            // print("DEBUG: ERROR - Null target or instructions in parseTarget\n");
            return -1;
        }
        //  print("DEBUG: Parsing target with instructions context: %s\n", target);

        // If it's a label reference
        if (target.startsWith("#")) {
            String labelName = target.substring(1);
            // print("DEBUG: Processing as label: %s\n", labelName);
            //print("DEBUG: Label map contents: %s\n", instrs.labelMap);

            if (instrs.labelMap == null) {
                //     print("DEBUG: ERROR - Label map is null!\n");
                return -1;
            }

            Integer labelAddress = instrs.labelMap.get(labelName);
            //   print("DEBUG: Label lookup result: %s\n", labelAddress);

            if (labelAddress != null) {
                //      print("DEBUG: Found label address: %d\n", labelAddress);
                return labelAddress;
            } else {
                //     print("DEBUG: Label '%s' not found in map\n", labelName);
                return -1;
            }
        }

        // Try parsing as direct number
        try {
            int value = Integer.parseInt(target);
            //  print("DEBUG: Parsed as direct number: %d\n", value);
            return value;
        } catch (NumberFormatException e) {
            // print("DEBUG: Not a number, trying as register name: %s\n", target);

            // Try as register
            try {
                int regValue = common.ReadRegister(target);
                //    print("DEBUG: Found in register with value: %d\n", regValue);
                return regValue;
            } catch (Exception ex) {
                //   print("DEBUG: Register lookup failed: %s\n", ex.getMessage());
                return -1;
            }
        }
    }
}
