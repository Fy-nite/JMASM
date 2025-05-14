package org.finite;

import static org.finite.common.print;
import static org.finite.common.printerr;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.finite.Exceptions.MASMException;
import org.finite.interp.instructions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Functions {

    private static final Logger logger = LoggerFactory.getLogger(
            Functions.class
    );

    // Add static buffered output streams for stdout and stderr
    private static final BufferedOutputStream bufferedStdout = new BufferedOutputStream(System.out, 8192);
    private static final BufferedOutputStream bufferedStderr = new BufferedOutputStream(System.err, 8192);

    private static final int FLUSH_THRESHOLD = 4096;
    private static int stdoutBufferCount = 0;
    private static int stderrBufferCount = 0;

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

    // Helper to get value from register, memory, or immediate
    private int getValue(int[] memory, String operand, instructions instrs) {
        operand = operand.trim();
        if (operand.startsWith("$")) {
            String addr = operand.substring(1);
            int address;
            try {
                address = Integer.parseInt(addr);
            } catch (NumberFormatException e) {
                if (!common.registersMap.containsKey(addr.toUpperCase()))
                    throw new MASMException("Invalid memory address or register: " + addr, instrs.currentLine, instrs.currentlineContents, "Error in operand");
                address = common.registersMap.get(addr.toUpperCase());
            }
            if (address < 0 || address >= memory.length)
                throw new MASMException("Memory address out of bounds: " + address, instrs.currentLine, instrs.currentlineContents, "Error in operand");
            return memory[address];
        } else if (operand.startsWith("R")) {
            if (!common.registersMap.containsKey(operand.toUpperCase()))
                throw new MASMException("Invalid register: " + operand, instrs.currentLine, instrs.currentlineContents, "Error in operand");
            return common.ReadRegister(operand);
        } else {
            try {
                return Integer.parseInt(operand);
            } catch (NumberFormatException e) {
                throw new MASMException("Invalid immediate value: " + operand, instrs.currentLine, instrs.currentlineContents, "Error in operand");
            }
        }
    }

    // Helper to set value to register or memory
    private void setValue(int[] memory, String dest, int value, instructions instrs) {
        dest = dest.trim();
        if (dest.startsWith("$")) {
            String addr = dest.substring(1);
            int address;
            try {
                address = Integer.parseInt(addr);
            } catch (NumberFormatException e) {
                throw new MASMException("Invalid memory address: " + addr, instrs.currentLine, instrs.currentlineContents, "Error in destination");
            }
            if (address < 0 || address >= memory.length)
                throw new MASMException("Memory address out of bounds: " + address, instrs.currentLine, instrs.currentlineContents, "Error in destination");
            memory[address] = value;
        } else if (dest.startsWith("R")) {
            if (!common.registersMap.containsKey(dest.toUpperCase()))
                throw new MASMException("Invalid register: " + dest, instrs.currentLine, instrs.currentlineContents, "Error in destination");
            common.WriteRegister(dest, value);
        } else {
            throw new MASMException("Invalid destination: " + dest, instrs.currentLine, instrs.currentlineContents, "Error in destination");
        }
    }

    public void add(int[] memory, String reg1, String reg2, instructions instrs) {
        try {
            if (!Parsing.INSTANCE.isValidRegister(reg1) || !Parsing.INSTANCE.isValidRegister(reg2)) {
                throw new MASMException("Invalid register name", instrs.currentLine, instrs.currentlineContents, "Error in instruction: add");
            }
            int value1 = common.ReadRegister(reg1);
            int value2 = common.ReadRegister(reg2);
            long result64 = (long)value1 + (long)value2;
            int result = value1 + value2;
            common.WriteRegister(reg1, result);

            // Set flags: ZF, SF, CF, OF
            common.setZF(result == 0);
            common.setSF(result < 0);
            common.setCF((result64 & 0x1_0000_0000L) != 0); // Carry if overflowed 32 bits
            // OF: if sign of operands same, but sign of result differs
            boolean of = ((value1 ^ result) & (value2 ^ result) & 0x80000000) != 0;
            common.setOF(of);
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
            long result64 = (long)value1 - (long)value2;
            int result = value1 - value2;
            common.WriteRegister(reg1, result);

            // Set flags: ZF, SF, CF, OF
            common.setZF(result == 0);
            common.setSF(result < 0);
            common.setCF((result64 & 0x1_0000_0000L) != 0); // Borrow
            boolean of = ((value1 ^ value2) & (value1 ^ result) & 0x80000000) != 0;
            common.setOF(of);
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
            long result64 = (long)value1 * (long)value2;
            int result = value1 * value2;
            common.WriteRegister(reg1, result);

            // Set flags: ZF, SF, CF, OF
            common.setZF(result == 0);
            common.setSF(result < 0);
            // CF/OF set if upper 32 bits of result are nonzero
            boolean overflow = (result64 > Integer.MAX_VALUE || result64 < Integer.MIN_VALUE);
            common.setCF(overflow);
            common.setOF(overflow);
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: mul");
        }
    }

    public void div(int[] memory, String reg1, String reg2, instructions instrs) {
        try {
            int value1 = common.ReadRegister(reg1);
            int value2 = common.ReadRegister(reg2);
            if (value2 == 0) throw new ArithmeticException();
            int result = value1 / value2;
            common.WriteRegister(reg1, result);

            // Set flags: ZF, SF, CF, OF (CF/OF cleared)
            common.setZF(result == 0);
            common.setSF(result < 0);
            common.setCF(false);
            common.setOF(false);
        } catch (ArithmeticException e) {
            throw new MASMException("Division by zero", instrs.currentLine, instrs.currentlineContents, "Error in instruction: div");
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: div");
        }
    }

    public void out(int[] memory, String fd, String source, instructions instrs) {
        try {
            common.dbgprint("Writing to file descriptor %s: %s\n", fd, source);

            // Default to standard output
            OutputStream outputStream = bufferedStdout;

            if (source == null) {
                return;
            }

            // Determine output stream based on file descriptor
            int fileDescriptor;
            try {
                fileDescriptor = Integer.parseInt(fd);
            } catch (Exception e) {
                fileDescriptor = common.ReadRegister(fd);
            }

            if (fileDescriptor == 2) {
                outputStream = bufferedStderr;
            } else if (fileDescriptor != 1) {
                throw new MASMException("Invalid file descriptor: " + fileDescriptor,
                        instrs.currentLine, instrs.currentlineContents, "Error in instruction: out");
            }

            // Write directly to the output stream based on source type
            if (source.startsWith("$[") && source.endsWith("]")) {
                // Handle memory address with expression
                String expression = source.substring(2, source.length() - 1);
                int memoryAddress = calculate_box_value(expression, instrs);

                if (memoryAddress < 0 || memoryAddress >= memory.length) {
                    throw new MASMException("Memory address out of bounds: " + memoryAddress,
                            instrs.currentLine, instrs.currentlineContents, "Error in instruction: out");
                }

                // Output as string if null-terminated
                int i = 0;
                while (memoryAddress + i < memory.length && memory[memoryAddress + i] != 0) {
                    outputStream.write(memory[memoryAddress + i] & 0xFF);
                    i++;
                }
                // If no characters were written, output the numeric value as bytes
                if (i == 0) {
                    int val = memory[memoryAddress];
                    outputStream.write(Integer.toString(val).getBytes());
                }
            }
            else if (source.startsWith("$")) {
                // Handle direct memory address
                String addr = source.substring(1);
                int address;
                try {
                    address = Integer.parseInt(addr);
                } catch (Exception e) {
                    address = common.ReadRegister(addr);
                }
                int i = 0;
                while (address + i < memory.length && memory[address + i] != 0) {
                    outputStream.write(memory[address + i] & 0xFF);
                    i++;
                }
                if (i == 0) {
                    int val = memory[address];
                    outputStream.write(Integer.toString(val).getBytes());
                }
            }
            else if (source.startsWith("[") && source.endsWith("]")) {
                String expression = source.substring(1, source.length() - 1);
                int val = calculate_box_value(expression, instrs);
                outputStream.write(Integer.toString(val).getBytes());
            }
            else if (source.startsWith("R")) {
                if (!Parsing.INSTANCE.isValidRegister(source)) {
                    throw new MASMException("Invalid register: " + source,
                            instrs.currentLine, instrs.currentlineContents, "Error in instruction: out");
                }
                int val = common.ReadRegister(source);
                outputStream.write(Integer.toString(val).getBytes());
            }
            else if (interp.stateVariables.containsKey(source)) {
                int val = interp.getStateVariableValue(source, memory);
                outputStream.write(Integer.toString(val).getBytes());
            }
            else {
                try {
                    int val = Integer.parseInt(source);
                    outputStream.write(Integer.toString(val).getBytes());
                } catch (NumberFormatException e) {
                    try {
                        int val = common.ReadRegister(source);
                        outputStream.write(Integer.toString(val).getBytes());
                    } catch (Exception ex) {
                        byte[] bytes = Parsing.INSTANCE.processEscapeSequences(source).getBytes();
                        outputStream.write(bytes);
                    }
                }
            }

            // Buffer flush logic
            if (outputStream == bufferedStdout) {
                stdoutBufferCount++;
                if (stdoutBufferCount >= FLUSH_THRESHOLD) {
                    bufferedStdout.flush();
                    stdoutBufferCount = 0;
                }
            } else if (outputStream == bufferedStderr) {
                stderrBufferCount++;
                if (stderrBufferCount >= FLUSH_THRESHOLD) {
                    bufferedStderr.flush();
                    stderrBufferCount = 0;
                }
            }
            // ...do not call outputStream.flush() every time...

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
            if (instrs == null) {
                throw new MASMException("Instructions context is null", 0, "", "Error in instruction: mov");
            }
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
                    instrs != null ? instrs.currentLine : 0,
                    instrs != null ? instrs.currentlineContents : "",
                    "Error in instruction: mov");
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

            int result = value1 - value2;
            // Set flags: ZF, SF, CF, OF
            common.setZF(result == 0);
            common.setSF(result < 0);
            // CF: set if unsigned borrow occurred
            common.setCF((Integer.compareUnsigned(value1, value2) < 0));
            // OF: set if signed overflow
            boolean of = ((value1 ^ value2) & (value1 ^ result) & 0x80000000) != 0;
            common.setOF(of);

            // For legacy compatibility, also set RFLAGS=1 if equal, 0 if not
            common.WriteRegister("RFLAGS", (result == 0) ? 1 : 0);
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: cmp");
        }
    }

    /**
     * Parallel batch comparison of operand pairs.
     * Each pair is compared in a separate thread.
     * Returns a list of comparison results (1 if equal, 0 if not).
     */
    public List<Integer> parallelCmp(
            int[] memory,
            List<String[]> operandPairs,
            instructions instrs
    ) {
        ExecutorService executor = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors()
        );
        try {
            List<Callable<Integer>> tasks = new java.util.ArrayList<>();
            for (String[] pair : operandPairs) {
                String reg1 = pair[0];
                String reg2 = pair[1];
                tasks.add(() -> {
                    int value1;
                    int value2;
                
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
                    return (value1 == value2) ? 1 : 0;
                });
            }
            List<Future<Integer>> results = executor.invokeAll(tasks);
            List<Integer> cmpResults = new java.util.ArrayList<>();
            for (Future<Integer> f : results) {
                cmpResults.add(f.get());
            }
            return cmpResults;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error in parallelCmp: " + e.getMessage(), e);
        } finally {
            executor.shutdown();
        }
    }

    // Floating-point compare (FCMP) and flag setting
    public static void fcmp(double val1, double val2) {
        boolean isNaN = Double.isNaN(val1) || Double.isNaN(val2);
        common.setFE(false);
        common.setFLT(false);
        common.setFGT(false);
        common.setFUO(false);
        if (isNaN) {
            common.setFUO(true);
        } else if (val1 == val2) {
            common.setFE(true);
        } else if (val1 < val2) {
            common.setFLT(true);
        } else if (val1 > val2) {
            common.setFGT(true);
        }
    }

    // Floating-point conditional jump helpers
    public static boolean fjz() { return common.getFE(); }
    public static boolean fjne() { return !common.getFE() && !common.getFUO(); }
    public static boolean fjlt() { return common.getFLT(); }
    public static boolean fjle() { return common.getFE() || common.getFLT(); }
    public static boolean fjgt() { return common.getFGT(); }
    public static boolean fjge() { return common.getFE() || common.getFGT(); }
    public static boolean fjuo() { return common.getFUO(); }

    public void cout(int[] memory, String fd, String reg, instructions instrs) {
        try {
            common.dbgprint("Writing to file descriptor %s: %s\n", fd, reg);

            int fileDescriptor;
            try {
                fileDescriptor = Integer.parseInt(fd);
            } catch (Exception e) {
                // If fd is a register, get its value
                fileDescriptor = common.ReadRegister(fd);
            }

            OutputStream outputStream = (fileDescriptor == 2) ? bufferedStderr : bufferedStdout;
            int value;
            if (reg.startsWith("R")) {
                if (!Parsing.INSTANCE.isValidRegister(reg)) {
                    throw new MASMException("Invalid register: " + reg, instrs.currentLine, instrs.currentlineContents, "Error in instruction: cout");
                }
                value = common.ReadRegister(reg);
            } else if (reg.startsWith("$")) {
                // Memory reference
                String addr = reg.substring(1);
                int address;
                try {
                    address = Integer.parseInt(addr);
                } catch (Exception e) {
                    address = common.ReadRegister(addr);
                }
                value = memory[address];
            } else {
                value = Integer.parseInt(reg);
            }
            // Only write the lowest byte (as a character)
            outputStream.write(value & 0xFF);

            // Buffer flush logic
            if (outputStream == bufferedStdout) {
                stdoutBufferCount++;
                if (stdoutBufferCount >= FLUSH_THRESHOLD) {
                    bufferedStdout.flush();
                    stdoutBufferCount = 0;
                }
            } else if (outputStream == bufferedStderr) {
                stderrBufferCount++;
                if (stderrBufferCount >= FLUSH_THRESHOLD) {
                    bufferedStderr.flush();
                    stderrBufferCount = 0;
                }
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
            int value1 = common.ReadRegister(reg1);
            int value2 = common.ReadRegister(reg2);
            int shift = value2 & 0x1F;
            int result = value1 << shift;
            // Set CF to last bit shifted out
            boolean cf = shift > 0 && ((value1 >> (32 - shift)) & 1) != 0;
            common.WriteRegister(reg1, result);
            common.setZF(result == 0);
            common.setSF(result < 0);
            common.setCF(cf);
            // OF: if shift == 1, OF = MSB(result) ^ CF
            if (shift == 1) {
                boolean of = ((result >> 31) & 1) != (cf ? 1 : 0);
                common.setOF(of);
            } else {
                common.setOF(false);
            }
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: shl");
        }
    }

    public static void shr(int[] memory, String reg1, String reg2, instructions instrs) {
        try {
            int value1 = common.ReadRegister(reg1);
            int value2 = common.ReadRegister(reg2);
            int shift = value2 & 0x1F;
            int result = value1 >>> shift;
            // Set CF to last bit shifted out
            boolean cf = shift > 0 && ((value1 >> (shift - 1)) & 1) != 0;
            common.WriteRegister(reg1, result);
            common.setZF(result == 0);
            common.setSF(result < 0);
            common.setCF(cf);
            // OF: if shift == 1, OF = MSB(value1)
            if (shift == 1) {
                boolean of = ((value1 >> 31) & 1) != 0;
                common.setOF(of);
            } else {
                common.setOF(false);
            }
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: shr");
        }
    }

    public static void and(int[] memory, String reg1, String reg2, instructions instrs) {
        try {
            int value1 = common.ReadRegister(reg1);
            int value2 = common.ReadRegister(reg2);
            int result = value1 & value2;
            common.WriteRegister(reg1, result);
            // Set flags: ZF, SF, CF=0, OF=0
            common.setZF(result == 0);
            common.setSF(result < 0);
            common.setCF(false);
            common.setOF(false);
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: and");
        }
    }

    public static void or(int[] memory, String reg1, String reg2, instructions instrs) {
        try {
            int value1 = common.ReadRegister(reg1);
            int value2 = common.ReadRegister(reg2);
            int result = value1 | value2;
            common.WriteRegister(reg1, result);
            // Set flags: ZF, SF, CF=0, OF=0
            common.setZF(result == 0);
            common.setSF(result < 0);
            common.setCF(false);
            common.setOF(false);
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: or");
        }
    }

    public static void xor(int[] memory, String reg1, String reg2, instructions instrs) {
        try {
            int value1 = common.ReadRegister(reg1);
            int value2 = common.ReadRegister(reg2);
            int result = value1 ^ value2;
            common.WriteRegister(reg1, result);
            // Set flags: ZF, SF, CF=0, OF=0
            common.setZF(result == 0);
            common.setSF(result < 0);
            common.setCF(false);
            common.setOF(false);
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: xor");
        }
    }

    public static void not(int[] memory, String reg, instructions instrs) {
        try {
            int value = common.ReadRegister(reg);
            value = ~value;
            common.WriteRegister(reg, value);
            // NOT does not affect flags
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

    // At the end of program execution, flush buffers
    public static void flushAllBuffers() {
        try {
            bufferedStdout.flush();
            bufferedStderr.flush();
        } catch (Exception e) {
            // ignore
        }
    }

    // Jump if greater (signed): ZF==0 && SF==OF
    public void jg(int[] memory, String target, instructions instrs) {
        try {
            if (!common.getZF() && (common.getSF() == common.getOF())) {
                jmp(memory, target, instrs);
            }
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: jg");
        }
    }

    // Jump if greater or equal (signed): SF==OF
    public void jge(int[] memory, String target, instructions instrs) {
        try {
            if (common.getSF() == common.getOF()) {
                jmp(memory, target, instrs);
            }
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: jge");
        }
    }

    // Jump if less (signed): SF!=OF
    public void jl(int[] memory, String target, instructions instrs) {
        try {
            if (common.getSF() != common.getOF()) {
                jmp(memory, target, instrs);
            }
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: jl");
        }
    }

    // Jump if less or equal (signed): ZF==1 || SF!=OF
    public void jle(int[] memory, String target, instructions instrs) {
        try {
            if (common.getZF() || (common.getSF() != common.getOF())) {
                jmp(memory, target, instrs);
            }
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: jle");
        }
    }

    // Jump if overflow
    public void jo(int[] memory, String target, instructions instrs) {
        try {
            if (common.getOF()) {
                jmp(memory, target, instrs);
            }
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: jo");
        }
    }

    // Jump if not overflow
    public void jno(int[] memory, String target, instructions instrs) {
        try {
            if (!common.getOF()) {
                jmp(memory, target, instrs);
            }
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: jno");
        }
    }

    // Jump if carry
    public void jc(int[] memory, String target, instructions instrs) {
        try {
            if (common.getCF()) {
                jmp(memory, target, instrs);
            }
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: jc");
        }
    }

    // Jump if not carry
    public void jnc(int[] memory, String target, instructions instrs) {
        try {
            if (!common.getCF()) {
                jmp(memory, target, instrs);
            }
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: jnc");
        }
    }

    // Floating-point jump if equal (FE)
    public void fjz(int[] memory, String target, instructions instrs) {
        try {
            if (fjz()) {
                jmp(memory, target, instrs);
            }
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: fjz");
        }
    }

    // Floating-point jump if not equal (!FE && !FUO)
    public void fjne(int[] memory, String target, instructions instrs) {
        try {
            if (fjne()) {
                jmp(memory, target, instrs);
            }
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: fjne");
        }
    }

    // Floating-point jump if less than (FLT)
    public void fjlt(int[] memory, String target, instructions instrs) {
        try {
            if (fjlt()) {
                jmp(memory, target, instrs);
            }
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: fjlt");
        }
    }

    // Floating-point jump if less or equal (FE || FLT)
    public void fjle(int[] memory, String target, instructions instrs) {
        try {
            if (fjle()) {
                jmp(memory, target, instrs);
            }
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: fjle");
        }
    }

    // Floating-point jump if greater than (FGT)
    public void fjgt(int[] memory, String target, instructions instrs) {
        try {
            if (fjgt()) {
                jmp(memory, target, instrs);
            }
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: fjgt");
        }
    }

    // Floating-point jump if greater or equal (FE || FGT)
    public void fjge(int[] memory, String target, instructions instrs) {
        try {
            if (fjge()) {
                jmp(memory, target, instrs);
            }
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: fjge");
        }
    }

    // Floating-point jump if unordered (FUO)
    public void fjuo(int[] memory, String target, instructions instrs) {
        try {
            if (fjuo()) {
                jmp(memory, target, instrs);
            }
        } catch (Exception e) {
            throw new MASMException(e.getMessage(), instrs.currentLine, instrs.currentlineContents, "Error in instruction: fjuo");
        }
    }

}