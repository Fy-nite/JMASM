package org.finite;

import static org.finite.common.print;
import static org.finite.common.printerr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.finite.Exceptions.MASMException;
import org.finite.interp.instructions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Functions {

    private static final Logger logger = LoggerFactory.getLogger(
            Functions.class
    );

    public static int calculate_box_value(String expression, instructions instrs) {

        // is the first thing a $
        boolean isDollar = expression.startsWith("$");
        common.dbgprint("Expression starts with $: {}", isDollar);
        // Remove leading $ if present
        if (isDollar) {
            expression = expression.substring(1); // we will use this later
            common.dbgprint("Expression starts with $, treating as memory address");
            common.dbgprint("Expression: {}", expression);
        }
        // Remove brackets and trim
        expression = expression.replace("[", "").replace("]", "").trim();
        common.dbgprint("Calculating expression: {}", expression);

        // Split by spaces and handle multiple operands
        String[] parts = expression.split("\\s+");

        if (parts.length < 1) {
            throw new MASMException("Empty expression", instrs.currentLine,
                    instrs.currentlineContents, "Error in array offset calculation");
        }

        // Initialize result with first operand
        int result = getOperandValue(parts[0], instrs);

        // Process remaining operands and operators
        for (int i = 1; i < parts.length; i += 2) {
            if (i + 1 >= parts.length) {
                throw new MASMException("Missing operand after operator", instrs.currentLine,
                        instrs.currentlineContents, "Error in array offset calculation");
            }

            String operator = parts[i];
            int nextValue = getOperandValue(parts[i + 1], instrs);

            switch (operator) {
                case "+":
                    result += nextValue;
                    break;
                case "-":
                    result -= nextValue;
                    break;
                case "*":
                    result *= nextValue;
                    break;
                case "/":
                    if (nextValue == 0) {
                        throw new MASMException("Division by zero", instrs.currentLine,
                                instrs.currentlineContents, "Error in array offset calculation");
                    }
                    result /= nextValue;
                    break;
                default:
                    throw new MASMException("Invalid operator: " + operator, instrs.currentLine,
                            instrs.currentlineContents, "Error in array offset calculation");
            }
        }

        common.dbgprint("Calculated result: {}", result);

        // we should check if the operation wants to use the dollar sign
        if (isDollar)
        {
            return common.ReadMemory(instrs.Memory, result);
        }
        else
        {
            return result;
        }
    }

    private static int getOperandValue(String operand, instructions instrs) {
        if (operand == null || operand.trim().isEmpty()) {
            throw new MASMException("Empty operand", instrs.currentLine,
                    instrs.currentlineContents, "Error in array offset calculation");
        }

        operand = operand.trim();

        // Check if it's a register
        if (Parsing.INSTANCE.isValidRegister(operand)) {
            return common.ReadRegister(operand);
        }

        // Check if it's a number
        try {
            return Integer.parseInt(operand);
        } catch (NumberFormatException e) {
            throw new MASMException("Invalid operand: " + operand, instrs.currentLine,
                    instrs.currentlineContents, "Error in array offset calculation");
        }
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
            if (sp <= common.MAX_MEMORY - common.STACK_SIZE) {
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
                            instrs.currentLine,
                            instrs.currentlineContents,
                            "Error including file"
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

    public void sub(int[] memory, String reg1, String reg2, instructions instrs) {
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

    public void mul(int[] memory, String reg1, String reg2, instructions instrs) {
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



    public void out(int[] memory, String fd, String source, instructions instrs) {
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

            // Handle memory address starting with $[
            if (source.startsWith("$[") && source.endsWith("]")) {
                // Remove $[ and ] to get the expression
                String expression = source.substring(2, source.length() - 1);
                // Calculate the memory address using the expression
                int memoryAddress = calculate_box_value(expression, instrs);

                // Read from the calculated memory address
                if (memoryAddress < 0 || memoryAddress >= memory.length) {
                    throw new MASMException("Memory address out of bounds: " + memoryAddress,
                            instrs.currentLine, instrs.currentlineContents, "Error in instruction: out");
                }

                // Try to read as null-terminated string first
                StringBuilder sb = new StringBuilder();
                int i = 0;
                while (memoryAddress + i < memory.length && memory[memoryAddress + i] != 0) {
                    sb.append((char) memory[memoryAddress + i]);
                    i++;
                }
                value = Parsing.INSTANCE.processEscapeSequences(sb.toString());

                // If empty, use numeric value
                if (value.isEmpty()) {
                    value = Integer.toString(memory[memoryAddress]);
                }
            }
            // Handle old $address format
            else if (source.startsWith("$")) {
                // Rest of existing $ handling code...
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
                    value = Parsing.INSTANCE.processEscapeSequences(sb.toString());

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
                    value = Parsing.INSTANCE.processEscapeSequences(sb.toString());

                    // If empty, try as number
                    if (value.isEmpty()) {
                        value = Integer.toString(memory[regAddr]);
                    }
                }
            }

            else if (source.startsWith("[") && source.endsWith("]")) {
                String expression = source.substring(1, source.length() - 1);
                value = Integer.toString(calculate_box_value(expression, instrs));



            }
            else if (source.startsWith("R")) {
                if (!Parsing.INSTANCE.isValidRegister(source)) {
                    throw new MASMException("Invalid register: " + source,
                            instrs.currentLine, instrs.currentlineContents, "Error in instruction: out");
                }
                value = Integer.toString(common.ReadRegister(source));
            }
            else {
                try {
                    int numValue = Integer.parseInt(source);
                    value = Integer.toString(numValue);
                } catch (NumberFormatException e) {
                    try {
                        value = Integer.toString(common.ReadRegister(source));
                    } catch (Exception ex) {
                        value = Parsing.INSTANCE.processEscapeSequences(source);
                    }
                }
            }

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
                    memory[address + i] = input.charAt(i);
                }
                memory[address + input.length()] = 0; // Null terminator
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: in");
            }
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: in");
        }
    }

    public void db(int[] memory, instructions instrs, String... argz) {
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
                if (memoryAddress < 0 || memoryAddress >= memory.length) {
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

                if (memoryAddress + bytes.length >= memory.length) {
                    throw new MASMException("String data exceeds memory bounds",
                            instrs.currentLine, instrs.currentlineContents,
                            "Error in instruction: db");
                }

                for (int i = 0; i < bytes.length; i++) {
                    memory[memoryAddress + i] = bytes[i] & 0xFF;
                }
                memory[memoryAddress + bytes.length] = 0; // Null terminator

                common.dbgprint("DB stored string of length {} at address {}", bytes.length, memoryAddress);
            } else {
                // Numeric data
                String[] values = dataPart.split(",");
                if (memoryAddress + values.length >= memory.length) {
                    throw new MASMException("Numeric data exceeds memory bounds",
                            instrs.currentLine, instrs.currentlineContents,
                            "Error in instruction: db");
                }

                for (int i = 0; i < values.length; i++) {
                    try {
                        memory[memoryAddress + i] = Integer.parseInt(values[i].trim());
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



    public void mov(int[] memory, String dest, String source, instructions instrs) {
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
                    if (address < 0 || address >= memory.length) {
                        throw new MASMException("Memory address out of bounds: " + address,
                                instrs.currentLine, instrs.currentlineContents, "Error in instruction: mov");
                    }
                    value = memory[address];
                } catch (NumberFormatException e) {
                    // Could be a register-based memory access
                    if (!common.registersMap.containsKey(memAddr.toUpperCase())) {
                        throw new MASMException("Invalid memory address or register: " + memAddr,
                                instrs.currentLine, instrs.currentlineContents, "Error in instruction: mov");
                    }
                    value = memory[common.registersMap.get(memAddr.toUpperCase())];
                }
            } else if (source.startsWith("R")) {
                // Register access
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
                    if (address < 0 || address >= memory.length) {
                        throw new MASMException("Memory address out of bounds: " + address,
                                instrs.currentLine, instrs.currentlineContents, "Error in instruction: mov");
                    }
                    memory[address] = value;
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



    public void cmp(int[] memory, String reg1, String reg2, instructions instrs) {
        try {
            int value1;
            int value2;
            common.dbgprint("Comparing %s and %s\n", reg1, reg2);
            // Handle first operand
            if (reg1.startsWith("$")) {
                try {
                    int address = Integer.parseInt(reg1.substring(1));
                    value1 = memory[address];
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
                        value2 = memory[address];
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
    public void calle(int[] memory, String target, instructions instrs) {
        // if rflags is 1, jump to the target
        if (common.ReadRegister("RFLAGS") == 1) {
            call(memory, target, instrs);
        }

    }

    public void callne(int[] memory, String target, instructions instrs) {
        // if rflags is 0, jump to the target
        if (common.ReadRegister("RFLAGS") == 0) {
            call(memory, target, instrs);
        }
    }

    public void jne(int[] memory, String target, instructions instrs) {
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

    public void jmp(int[] memory, String target, instructions instrs) {
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