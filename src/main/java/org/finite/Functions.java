package org.finite;

import static org.finite.common.print;
import static org.finite.common.printerr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.finite.Exceptions.MASMException;
import org.finite.interp.instructions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.finite.Parsing;
import org.finite.Memory.MemoryMappedManager;
import org.finite.Memory.MemoryAdapter;

public class Functions {

    private static final Logger logger = LoggerFactory.getLogger(
        Functions.class
    );

    // Create adapter method to convert between MemoryMappedManager and int[]
    private MemoryAdapter getAdapter(MemoryMappedManager memory) {
        return new MemoryAdapter(memory);
    }

    public static String include(String filename, String CurrentFileContents) {
        common.dbgprint("Including file: {}", filename);
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
                common.dbgprint(
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
                    throw new MASMException(
                        "Resource not found: " + resourcePath,
                        0,
                        "",
                        "Error including file"
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

    public static void include(String filename, instructions instrs) {
        try {
            String includeStatement = "#include \"" + filename.replace("\"", "") + "\"";
            String content = new String(Files.readAllBytes(Paths.get(filename)));
            instrs.currentlineContents = instrs.currentlineContents.replace(includeStatement, content);
        } catch (IOException e) {
            logger.error("Failed to include file: {}", filename, e);
            printerr("Error including file %s: %s\n", filename, e.getMessage());
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
    public static void push(MemoryMappedManager memory, String reg1, instructions instrs) {
        MemoryAdapter adapter = new MemoryAdapter(memory);
        try {
            int sp = common.ReadRegister("RSP");
            if (sp <= common.MAX_MEMORY - 1024) {
                throw new MASMException("Stack overflow", instrs.currentLine, instrs.currentlineContents, "Error in instruction: push");
            }
            int value = common.ReadRegister(reg1);
            sp--;
            adapter.write(sp, value);
            common.WriteRegister("RSP", sp);
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: push");
        }
    }

    public static void pop(MemoryMappedManager memory, String reg1, instructions instrs) {
        MemoryAdapter adapter = new MemoryAdapter(memory);
        try {
            int sp = common.ReadRegister("RSP");
            if (sp >= common.MAX_MEMORY) {
                throw new MASMException("Stack underflow", instrs.currentLine, instrs.currentlineContents, "Error in instruction: pop");
            }
            int value = adapter.read(sp);
            sp++;
            common.WriteRegister("RSP", sp);
            common.WriteRegister(reg1, value);
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: pop");
        }
    }

    public void add(MemoryMappedManager memory, String reg1, String reg2, instructions instrs) {
        MemoryAdapter adapter = getAdapter(memory);
        try {
            if (!Parsing.INSTANCE.isValidRegister(reg1) || !Parsing.INSTANCE.isValidRegister(reg2)) {
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

    public void sub(MemoryMappedManager memory, String reg1, String reg2, instructions instrs) {
        MemoryAdapter adapter = getAdapter(memory);
        try {
            if (!Parsing.INSTANCE.isValidRegister(reg1) || !Parsing.INSTANCE.isValidRegister(reg2)) {
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

    public void mul(MemoryMappedManager memory, String reg1, String reg2, instructions instrs) {
        MemoryAdapter adapter = getAdapter(memory);
        try {
            if (!Parsing.INSTANCE.isValidRegister(reg1) || !Parsing.INSTANCE.isValidRegister(reg2)) {
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

    public void div(MemoryMappedManager memory, String reg1, String reg2, instructions instrs) {
        MemoryAdapter adapter = getAdapter(memory);
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



    public void out(MemoryMappedManager memory, String fd, String source, instructions instrs) {
        MemoryAdapter adapter = getAdapter(memory);
        try {
            common.dbgprint("Writing to file descriptor %s: %s\n", fd, source);

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
                        address + i < memory.getMemorySize() && adapter.read(address + i) != 0
                    ) {
                        sb.append((char) adapter.read(address + i));
                        i++;
                    }
                    value = Parsing.INSTANCE.processEscapeSequences(sb.toString());

                    // If empty, try as number
                    if (value.isEmpty()) {
                        value = Integer.toString(adapter.read(address));
                    }
                } catch (Exception e) {
                    // Handle register containing address
                    int regAddr = common.ReadRegister(addr);
                    StringBuilder sb = new StringBuilder();
                    int i = 0;
                    while (
                        regAddr + i < memory.getMemorySize() && adapter.read(regAddr + i) != 0
                    ) {
                        sb.append((char) adapter.read(regAddr + i));
                        i++;
                    }
                    value = Parsing.INSTANCE.processEscapeSequences(sb.toString());

                    // If empty, try as number
                    if (value.isEmpty()) {
                        value = Integer.toString(adapter.read(regAddr));
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
                        value = Parsing.INSTANCE.processEscapeSequences(source);
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
    public void in(MemoryMappedManager memory, String fd, String dest, instructions instrs) {
        MemoryAdapter adapter = getAdapter(memory);
        try {
            // Validate inputs first
            if (fd == null || dest == null) {
                throw new MASMException("Invalid arguments", instrs.currentLine, instrs.currentlineContents, "Error in instruction: in");
            }

            // Validate file descriptor
            int fileDescriptor;
            try {
                fileDescriptor = Integer.parseInt(fd);
            } catch (NumberFormatException e) {
                throw new MASMException("Invalid file descriptor: " + fd, instrs.currentLine, instrs.currentlineContents, "Error in instruction: in");
            }

            if (fileDescriptor != 1) {
                throw new MASMException("Invalid file descriptor: " + fd, instrs.currentLine, instrs.currentlineContents, "Error in instruction: in");
            }

            // Validate destination format first
            if (!dest.startsWith("$")) {
                throw new MASMException("Invalid destination format: " + dest, instrs.currentLine, instrs.currentlineContents, "Error in instruction: in");
            }

            // Validate memory address format before attempting input
            try {
                int address = Integer.parseInt(dest.substring(1));
                if (address < 0 || address >= common.MAX_MEMORY) {
                    throw new MASMException("Invalid memory address: " + dest, instrs.currentLine, instrs.currentlineContents, "Error in instruction: in");
                }
            } catch (NumberFormatException e) {
                throw new MASMException("Invalid memory address: " + dest, instrs.currentLine, instrs.currentlineContents, "Error in instruction: in");
            }

            // After all validation passes, try to read input
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                String input = reader.readLine();
                if (input == null) {
                    throw new MASMException("End of input stream", instrs.currentLine, instrs.currentlineContents, "Error in instruction: in");
                }

                // Write the input to memory
                int address = Integer.parseInt(dest.substring(1));
                for (int i = 0; i < input.length(); i++) {
                    adapter.write(address + i, input.charAt(i));
                }
                adapter.write(address + input.length(), 0); // Null terminator
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: in");
            }
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: in");
        }
    }

    public void db(MemoryMappedManager memory, instructions instrs, String... argz) {
        MemoryAdapter adapter = getAdapter(memory);
        try {
            if (argz == null || argz.length == 0) {
                throw new MASMException("DB instruction requires arguments", 
                    instrs.currentLine, instrs.currentlineContents, 
                    "Error in instruction: db");
            }

            // Join arguments and handle quotes properly
            String fullArg = String.join(" ", argz).trim();
            common.dbgprint("DB instruction processing: '{}'", fullArg);

            // Validate minimum format
            if (!fullArg.contains(" ")) {
                throw new MASMException("DB instruction requires address and data separated by space", 
                    instrs.currentLine, instrs.currentlineContents, 
                    "Error in instruction: db");
            }

            // Extract address and data parts safely
            int spaceIdx = fullArg.indexOf(' ');
            String addressPart = fullArg.substring(0, spaceIdx).trim();
            String dataPart = fullArg.substring(spaceIdx + 1).trim();

            common.dbgprint("DB parsed address: '{}', data: '{}'", addressPart, dataPart);

            // Validate and parse memory address
            if (!addressPart.startsWith("$")) {
                throw new MASMException("DB address must start with $", 
                    instrs.currentLine, instrs.currentlineContents, 
                    "Error in instruction: db");
            }

            int memoryAddress;
            try {
                memoryAddress = Integer.parseInt(addressPart.substring(1));
                if (memoryAddress < 0 || memoryAddress >= memory.getMemorySize()) {
                    throw new MASMException("Memory address out of bounds: " + memoryAddress, 
                        instrs.currentLine, instrs.currentlineContents, 
                        "Error in instruction: db");
                }
            } catch (NumberFormatException e) {
                throw new MASMException("Invalid memory address format: " + addressPart, 
                    instrs.currentLine, instrs.currentlineContents, 
                    "Error in instruction: db");
            }

            // Handle the data part
            if (dataPart.startsWith("\"") && dataPart.endsWith("\"")) {
                // String literal
                String strContent = dataPart.substring(1, dataPart.length() - 1);
                strContent = Parsing.INSTANCE.processEscapeSequences(strContent);
                byte[] bytes = strContent.getBytes();

                if (memoryAddress + bytes.length >= memory.getMemorySize()) {
                    throw new MASMException("String data exceeds memory bounds", 
                        instrs.currentLine, instrs.currentlineContents, 
                        "Error in instruction: db");
                }

                for (int i = 0; i < bytes.length; i++) {
                    adapter.write(memoryAddress + i, bytes[i] & 0xFF);
                }
                adapter.write(memoryAddress + bytes.length, 0); // Null terminator

                common.dbgprint("DB stored string of length {} at address {}", bytes.length, memoryAddress);
            } else {
                // Numeric data
                String[] values = dataPart.split(",");
                if (memoryAddress + values.length >= memory.getMemorySize()) {
                    throw new MASMException("Numeric data exceeds memory bounds", 
                        instrs.currentLine, instrs.currentlineContents, 
                        "Error in instruction: db");
                }

                for (int i = 0; i < values.length; i++) {
                    try {
                        adapter.write(memoryAddress + i, Integer.parseInt(values[i].trim()));
                    } catch (NumberFormatException e) {
                        throw new MASMException("Invalid numeric value: " + values[i], 
                            instrs.currentLine, instrs.currentlineContents, 
                            "Error in instruction: db");
                    }
                }

                common.dbgprint("DB stored {} numeric values at address {}", values.length, memoryAddress);
            }
        } catch (Exception e) {
            if (e instanceof MASMException) {
                throw e;
            }
            throw new MASMException(e.getMessage(), 
                instrs.currentLine, instrs.currentlineContents, 
                "Error in instruction: db");
        }
    }



    public void mov(MemoryMappedManager memory, String dest, String source, instructions instrs) {
        MemoryAdapter adapter = getAdapter(memory);
        try {
            if (dest == null || source == null) {
                throw new MASMException("MOV requires two operands", instrs.currentLine, instrs.currentlineContents, "Error in instruction: mov");
            }

            common.dbgprint("MOV {} {}", dest, source);
            int value;

            // Handle source operand
            if (source.startsWith("$")) {
                // Memory access
                String memAddr = source.substring(1);
                try {
                    int address = Integer.parseInt(memAddr);
                    if (address < 0 || address >= memory.getMemorySize()) {
                        throw new MASMException("Memory address out of bounds: " + address, 
                            instrs.currentLine, instrs.currentlineContents, "Error in instruction: mov");
                    }
                    value = adapter.read(address);
                } catch (NumberFormatException e) {
                    // Could be a register-based memory access
                    if (!common.registersMap.containsKey(memAddr.toUpperCase())) {
                        throw new MASMException("Invalid memory address: " + memAddr,
                            instrs.currentLine, instrs.currentlineContents, "Error in instruction: mov");
                    }
                    value = common.ReadRegister(memAddr);
                }
            } else if (source.startsWith("R")) {
                // Register source
                if (!common.registersMap.containsKey(source.toUpperCase())) {
                    throw new MASMException("Invalid register: " + source,
                        instrs.currentLine, instrs.currentlineContents, "Error in instruction: mov");
                }
                value = common.ReadRegister(source);
            } else {
                // Immediate value
                try {
                    value = Integer.parseInt(source);
                } catch (NumberFormatException e) {
                    throw new MASMException("Invalid immediate value: " + source,
                        instrs.currentLine, instrs.currentlineContents, "Error in instruction: mov");
                }
            }

            // Handle destination operand
            if (dest.startsWith("$")) {
                // Memory destination
                String memAddr = dest.substring(1);
                try {
                    int address = Integer.parseInt(memAddr);
                    if (address < 0 || address >= memory.getMemorySize()) {
                        throw new MASMException("Memory address out of bounds: " + address,
                            instrs.currentLine, instrs.currentlineContents, "Error in instruction: mov");
                    }
                    adapter.write(address, value);
                } catch (NumberFormatException e) {
                    throw new MASMException("Invalid memory address: " + memAddr,
                        instrs.currentLine, instrs.currentlineContents, "Error in instruction: mov");
                }
            } else if (dest.startsWith("R")) {
                // Register destination
                if (!common.registersMap.containsKey(dest.toUpperCase())) {
                    throw new MASMException("Invalid register: " + dest,
                        instrs.currentLine, instrs.currentlineContents, "Error in instruction: mov");
                }
                common.WriteRegister(dest, value);
            } else {
                throw new MASMException("Invalid destination: " + dest,
                    instrs.currentLine, instrs.currentlineContents, "Error in instruction: mov");
            }

            common.dbgprint("MOV completed: {} <- {}", dest, value);

        } catch (Exception e) {
            if (e instanceof MASMException) {
                throw e;
            }
            throw new MASMException(e.getMessage(), 
                instrs.currentLine, instrs.currentlineContents, "Error in instruction: mov");
        }
    }

    public void ret(instructions instrs) {
        try {
            // Get current stack pointer
            int sp = common.ReadRegister("RSP");
            if (sp >= common.MAX_MEMORY) {
                throw new MASMException("Stack underflow", instrs.currentLine, instrs.currentlineContents, "Error in instruction: ret");
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



    public void cmp(MemoryMappedManager memory, String reg1, String reg2, instructions instrs) {
        MemoryAdapter adapter = getAdapter(memory);
        try {
            int value1;
            int value2;
            common.dbgprint("Comparing %s and %s\n", reg1, reg2);
            // Handle first operand
            if (reg1.startsWith("$")) {
                try {
                    int address = Integer.parseInt(reg1.substring(1));
                    value1 = adapter.read(address);
                } catch (NumberFormatException e) {
                    throw new MASMException("Invalid memory address: " + reg1, instrs.currentLine, instrs.currentlineContents, "Error in instruction: cmp");
                }
            } else {
                if (!Parsing.INSTANCE.isValidRegister(reg1)) {
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
                        value2 = adapter.read(address);
                    } catch (NumberFormatException ex) {
                        throw new MASMException("Invalid memory address: " + reg2, instrs.currentLine, instrs.currentlineContents, "Error in instruction: cmp");
                    }
                } else {
                    if (!Parsing.INSTANCE.isValidRegister(reg2)) {
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

    public void cout(MemoryMappedManager memory, String fd, String reg, instructions instrs) {
        MemoryAdapter adapter = getAdapter(memory);
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

    public static void inc(MemoryMappedManager memory, String reg, instructions instrs) {
        MemoryAdapter adapter = new MemoryAdapter(memory);
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

    public static void dec(MemoryMappedManager memory, String reg, instructions instrs) {
        MemoryAdapter adapter = new MemoryAdapter(memory);
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

    public static void shl(MemoryMappedManager memory, String reg1, String reg2, instructions instrs) {
        MemoryAdapter adapter = new MemoryAdapter(memory);
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

    public static void shr(MemoryMappedManager memory, String reg1, String reg2, instructions instrs) {
        MemoryAdapter adapter = new MemoryAdapter(memory);
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

    public static void and(MemoryMappedManager memory, String reg1, String reg2, instructions instrs) {
        MemoryAdapter adapter = new MemoryAdapter(memory);
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

    public static void or(MemoryMappedManager memory, String reg1, String reg2, instructions instrs) {
        MemoryAdapter adapter = new MemoryAdapter(memory);
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

    public static void xor(MemoryMappedManager memory, String reg1, String reg2, instructions instrs) {
        MemoryAdapter adapter = new MemoryAdapter(memory);
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

    public static void not(MemoryMappedManager memory, String reg, instructions instrs) {
        MemoryAdapter adapter = new MemoryAdapter(memory);
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

    public static void neg(MemoryMappedManager memory, String reg, instructions instrs) {
        MemoryAdapter adapter = new MemoryAdapter(memory);
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

    public void call(MemoryMappedManager memory, String target, instructions instrs) {
        MemoryAdapter adapter = getAdapter(memory);
        try {
            common.dbgprint("CALL to target: {}", target);
            if (target == null || instrs == null) {
                throw new MASMException("Target or instructions cannot be null", instrs.currentLine, instrs.currentlineContents, "Error in instruction: call");
            }

            // Save current instruction pointer
            int currentRIP = common.ReadRegister("RIP");

            // Handle labels
            if (target.startsWith("#")) {
                String labelName = target.substring(1);
                Integer labelAddress = instrs.labelMap.get(labelName);

                common.dbgprint("Looking up label '{}' -> address: {}", labelName, labelAddress);

                if (labelAddress == null) {
                    throw new MASMException("Unknown label: " + labelName, instrs.currentLine, instrs.currentlineContents, "Error in instruction: call");
                }

                // Push return address (next instruction) onto stack
                push(memory, "RIP", instrs);

                // Jump to label
                common.WriteRegister("RIP", labelAddress - 1);
                common.dbgprint("Jumped to address {} for label {}", labelAddress - 1, labelName);
            } else {
                throw new MASMException("Call target must be a label", instrs.currentLine, instrs.currentlineContents, "Error in instruction: call");
            }
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: call");
        }
    }

    public void jeq(MemoryMappedManager memory, String target, instructions instrs) {
        MemoryAdapter adapter = getAdapter(memory);
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
                common.dbgprint(
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
    // call but only if rflags is 1
    public void calle(MemoryMappedManager memory, String target, instructions instrs) {
        MemoryAdapter adapter = getAdapter(memory);
        // if rflags is 1, jump to the target
        if (common.ReadRegister("RFLAGS") == 1) {
            call(memory, target, instrs);
        }

    }

    public void callne(MemoryMappedManager memory, String target, instructions instrs) {
        MemoryAdapter adapter = getAdapter(memory);
        // if rflags is 0, jump to the target
        if (common.ReadRegister("RFLAGS") == 0) {
            call(memory, target, instrs);
        }
    }

    public void jne(MemoryMappedManager memory, String target, instructions instrs) {
        MemoryAdapter adapter = getAdapter(memory);
        try {
            common.dbgprint("JNE instruction with target: {}", target);

            if (target == null || instrs == null) {
                throw new MASMException("Target or instructions cannot be null", instrs.currentLine, instrs.currentlineContents, "Error in instruction: jne");
            }

            // read the RFLAGS register
            int value = common.ReadRegister("RFLAGS");
            common.dbgprint("RFLAGS value: {}", value);

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

            common.dbgprint("Target address for label {}: {}", labelName, targetAddress);

            // Jump if RFLAGS is 0 (values were not equal)
            if (value == 0) {
                common.WriteRegister("RIP", targetAddress - 1);
                common.dbgprint("Jump taken - setting RIP to {}", targetAddress - 1);
            } else {
                common.dbgprint("Jump not taken - RFLAGS was 1");
            }
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: jne");
        }
    }

    public void jmp(MemoryMappedManager memory, String target, instructions instrs) {
        MemoryAdapter adapter = getAdapter(memory);
        try {
            common.dbgprint("JMP to target: {}", target);
            int value;
            if (target == null || instrs == null) {
                //  print("DEBUG: ERROR - Null target or instructions\n");
                throw new MASMException("Target or instructions cannot be null", instrs.currentLine, instrs.currentlineContents, "Error in instruction: jmp");
            }
            //print("DEBUG: Attempting to jump to target: %s with instructions context\n", target);
            try
            {

            value = Parsing.INSTANCE.parseTarget(target, instrs);
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



}
