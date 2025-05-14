package org.finite;

import static org.finite.common.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.lang.reflect.Method;
import org.finite.ModuleManager.MNIMethodObject;
import org.finite.ModuleManager.MNIHandler;
import org.finite.Exceptions.MASMException; // Add this import
import org.finite.Exceptions.MNIException;
import org.finite.ArgumentParser;

public class interp {

    public static boolean testmode = true;
    public static boolean testMode = Boolean.getBoolean("testMode"); // This line changes

    /*
     * A class to represent macros in the assembly language.
     * It contains the macro name, parameters, and body.
     * This is used for macro
     * expansion during preprocessing.
     */
    public static class Macro {
        String name;
        List<String> parameters;
        List<String> body;

        public Macro(String name, List<String> parameters, List<String> body) {
            this.name = name;
            this.parameters = parameters;
            this.body = body;
        }
    }
    /*
     * A class to represent state variables in the assembly language.
     * It contains the variable name, type, address, and size.
     * This is used for state variable management.
     */
    public static class StateVariable {
        String name;
        String type;
        int address;
        int size;

        public StateVariable(String name, String type, int address, int size) {
            this.name = name;
            this.type = type;
            this.address = address;
            this.size = size;
        }
    }

    public static HashMap<String, StateVariable> stateVariables = new HashMap<>();
    private static int memoryPointer = 0; // Tracks the next available memory address

    private static boolean hasIncludes(String content) {
        return content.toLowerCase().contains("#include");
    }


    /*
     * Handles the STATE declaration in the assembly language.
     */
    private static void handleStateDeclaration(String line) {
        String[] parts = line.split("\\s+");
        if (parts.length < 3) {
            throw new MASMException("Invalid STATE declaration", 0, line, "Error in instruction: STATE");
        }

        String name = parts[1];
        String type = parts[2].toUpperCase();
        int size = switch (type) {
            case "<BYTE>" -> 1;
            case "<WORD>" -> 2;
            case "<DWORD>" -> 4;
            case "<QWORD>", "<PTR>" -> 8;
            case "<FLOAT>" -> 4;
            case "<DOUBLE>" -> 8;
            default -> throw new MASMException("Unknown type in STATE declaration: " + type, 0, line,
                    "Error in instruction: STATE");
        };

        int initialValue = 0;
        if (parts.length > 3) {
            try {
                initialValue = Integer.parseInt(parts[3]);
            } catch (NumberFormatException e) {
                throw new MASMException("Invalid initial value in STATE declaration", 0, line,
                        "Error in instruction: STATE");
            }
        }

        if (stateVariables.containsKey(name)) {
            throw new MASMException("Duplicate STATE variable: " + name, 0, line, "Error in instruction: STATE");
        }

        StateVariable stateVar = new StateVariable(name, type, memoryPointer, size);
        stateVariables.put(name, stateVar);

        // Initialize memory with the initial value
        for (int i = 0; i < size; i++) {
            if (memoryPointer + i < common.MAX_MEMORY) {
                common.WriteMemory(common.memory, memoryPointer + i, (initialValue >> (i * 8)) & 0xFF);
            } else {
                throw new MASMException("Memory allocation exceeds available memory", 0, line, 
                        "Error in instruction: STATE");
            }
        }

        memoryPointer += size;
    }

    // Add at the top
    private static final org.finite.ModuleManager.ModuleRegistry mniRegistry =
        org.finite.ModuleManager.ModuleRegistry.getInstance();

    // Extend preprocess method to handle macros
    private static String preprocess(String[] lines) {
        StringBuilder processed = new StringBuilder();
        HashMap<String, Macro> macros = new HashMap<>(); // Store macros
        boolean inMacro = false;
        Macro currentMacro = null;

        for (String line : lines) {
            line = line.trim();

            if (line.toUpperCase().startsWith("MACRO")) {
                inMacro = true;
                String[] parts = line.split(" ", 2);
                String macroHeader = parts[1].trim();
                String[] headerParts = macroHeader.split(" ", 2);
                String macroName = headerParts[0];
                List<String> parameters = headerParts.length > 1 ? Arrays.asList(headerParts[1].split(","))
                        : new ArrayList<>();
                currentMacro = new Macro(macroName, parameters, new ArrayList<>());
                continue;
            }

            if (line.toUpperCase().equals("ENDMACRO")) {
                inMacro = false;
                macros.put(currentMacro.name, currentMacro);
                currentMacro = null;
                continue;
            }

            if (inMacro) {
                currentMacro.body.add(line);
                continue;
            }

            if (line.toUpperCase().startsWith("STATE ")) {
                handleStateDeclaration(line);
                continue;
            }

            // Expand macros
            String[] parts = line.split(" ", 2);
            String instruction = parts[0];
            if (macros.containsKey(instruction)) {
                Macro macro = macros.get(instruction);
                List<String> arguments = parts.length > 1 ? Arrays.asList(parts[1].split(",")) : new ArrayList<>();
                if (arguments.size() != macro.parameters.size()) {
                    throw new MASMException("Macro argument mismatch for: " + instruction, 0, line,
                            "Error in macro expansion");
                }

                HashMap<String, String> paramMap = new HashMap<>();
                for (int i = 0; i < macro.parameters.size(); i++) {
                    paramMap.put(macro.parameters.get(i).trim(), arguments.get(i).trim());
                }

                for (String macroLine : macro.body) {
                    String expandedLine = macroLine;
                    for (String param : paramMap.keySet()) {
                        expandedLine = expandedLine.replace(param, paramMap.get(param));
                    }
                    processed.append(expandedLine).append("\n");
                }
                continue;
            }
            // Try custom macro provider (annotation-based)
            Method macroMethod = mniRegistry.getMacroProvider(instruction);
            if (macroMethod != null) {
                try {
                    String[] macroArgs = parts.length > 1 ? parts[1].split(",") : new String[0];
                    Object macroBody = macroMethod.invoke(null, (Object) macroArgs);
                    if (macroBody != null) {
                        processed.append(macroBody.toString()).append("\n");
                        continue;
                    }
                } catch (Exception e) {
                    throw new org.finite.Exceptions.MASMException(
                        "Error calling macro provider: " + instruction,
                        0, line, "Error in macro expansion"
                    );
                }
            }

            processed.append(line).append("\n");
        }

        String result = processed.toString();
        int maxPasses = 10; // Prevent infinite recursion
        int currentPass = 0;

        while (hasIncludes(result) && currentPass < maxPasses) {
            StringBuilder newProcessed = new StringBuilder();
            String[] currentLines = result.split("\n");

            for (String line : currentLines) {
                line = line.trim();
                if (line.toLowerCase().startsWith("#include")) {
                    int start = line.indexOf("\"");
                    int end = line.lastIndexOf("\"");
                    if (start != -1 && end != -1 && start != end) {
                        String path = line.substring(start + 1, end);
                        newProcessed.append(Includemanager.include(path, line));
                    }
                } else if (line.startsWith(";")) {
                    continue;
                } else {
                    int commentStart = line.indexOf(';');
                    if (commentStart != -1) {
                        line = line.substring(0, commentStart).trim();
                    }
                    if (!line.isEmpty()) {
                        newProcessed.append(line).append("\n");
                    }
                }
            }

            result = newProcessed.toString();
            currentPass++;
        }

        if (currentPass >= maxPasses) {
            throw new MASMException("Too many include passes", 0, "", "Error in instruction: #include");
        }
        //System.out.println("Preprocessed code:\n" + result);
        return result;
    }

    // Optimization pass: simple constant folding, dead code elimination, peephole,
    // etc.
    public static String optimize(String code) {
        System.out.println("Optimizing code:\n" + code);

        String[] lines = code.split("\n");
        for (String line : lines) {
            System.out.println("Line: " + line);
        }
        List<String> optimized = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            String lineUpper = line.toUpperCase(); // Normalize for matching
            if (ArgumentParser.Args.debug) {
                common.dbgprint("Optimizing line %d: '%s'", i, line);
            }

            // --- New: Combine consecutive ADD/SUB/MUL/DIV on same register with immediates ---
            if (lineUpper.matches("^(ADD|SUB|MUL|DIV)\\s+R\\w+\\s+[-]?\\d+$")) {
                String[] parts = line.split("\\s+");
                String op = parts[0].toUpperCase();
                String reg = parts[1];
                int val = Integer.parseInt(parts[2]);
                int j = i + 1;
                // Combine as long as next line is same op/reg/immediate
                while (j < lines.length) {
                    String next = lines[j].trim();
                    String nextUpper = next.toUpperCase();
                    if (nextUpper.matches("^" + op + "\\s+" + reg.toUpperCase() + "\\s+[-]?\\d+$")) {
                        int nextVal = Integer.parseInt(next.split("\\s+")[2]);
                        // Fold for ADD/SUB/MUL/DIV
                        if (op.equals("ADD")) val += nextVal;
                        else if (op.equals("SUB")) val += nextVal; // SUB x y; SUB x z == SUB x (y+z)
                        else if (op.equals("MUL")) val *= nextVal;
                        else if (op.equals("DIV")) val /= nextVal;
                        j++;
                    } else {
                        break;
                    }
                }
                // For SUB, keep as SUB x (y+z)
                optimized.add(op + " " + reg + " " + val);
                i = j - 1;
                continue;
            }


            // Remove redundant MOV (e.g., MOV R1 R1)
            if (lineUpper.matches("^MOV\\s+(R\\w+)\\s+\\1$")) {
                if (ArgumentParser.Args.debug) {
                    common.dbgprint("Removing redundant MOV: '%s'", line);
                }
                continue;
            }
            // Peephole: INC R1; INC R1 → ADD R1 2
            if (i + 1 < lines.length &&
                    lineUpper.matches("^INC\\s+(R\\w+)$") &&
                    lines[i + 1].trim().toUpperCase().equals(lineUpper)) {
                common.dbgprint("Peephole optimization: INC R1; INC R1 → ADD R1 2");
                String reg = line.split("\\s+")[1];
                optimized.add("ADD " + reg + " 2");
                i++; // Skip next line
                continue;
            }
            // Peephole: DEC R1; DEC R1 → SUB R1 2
            if (i + 1 < lines.length &&
                    lineUpper.matches("^DEC\\s+(R\\w+)$") &&
                    lines[i + 1].trim().toUpperCase().equals(lineUpper)) {
                String reg = line.split("\\s+")[1];
                optimized.add("SUB " + reg + " 2");
                i++; // Skip next line
                continue;
            }
            // Peephole: INC R1; DEC R1 or DEC R1; INC R1 → (remove both)
            if (i + 1 < lines.length) {
                String l1 = line;
                String l2 = lines[i + 1].trim();
                String l1Upper = l1.toUpperCase();
                String l2Upper = l2.toUpperCase();
                if (l1Upper.matches("^INC\\s+(R\\w+)$") && l2Upper.equals("DEC " + l1.split("\\s+")[1].toUpperCase())) {
                    i++; // Skip next line
                    continue;
                }
                if (l1Upper.matches("^DEC\\s+(R\\w+)$") && l2Upper.equals("INC " + l1.split("\\s+")[1].toUpperCase())) {
                    i++; // Skip next line
                    continue;
                }
            }
            // Remove ADD Rn 0 and SUB Rn 0 (no-op)
            if (lineUpper.matches("^(ADD|SUB)\\s+R\\w+\\s+0$")) {
                continue;
            }
            // Remove MUL Rn 1 and DIV Rn 1 (no-op)
            if (lineUpper.matches("^(MUL|DIV)\\s+R\\w+\\s+1$")) {
                continue;
            }
            // Replace MUL Rn 0 with MOV Rn 0
            if (lineUpper.matches("^MUL\\s+(R\\w+)\\s+0$")) {
                String reg = line.split("\\s+")[1];
                optimized.add("MOV " + reg + " 0");
                continue;
            }
            // Replace DIV Rn Rn with MOV Rn 1 (x/x = 1, if x != 0)
            if (lineUpper.matches("^DIV\\s+(R\\w+)\\s+\\1$")) {
                String reg = line.split("\\s+")[1];
                optimized.add("MOV " + reg + " 1");
                continue;
            }
            // Dead code after unconditional JMP/HLT
            if (!optimized.isEmpty() &&
                    (optimized.get(optimized.size() - 1).toUpperCase().matches("^JMP\\b.*") ||
                            optimized.get(optimized.size() - 1).toUpperCase().matches("^HLT\\b.*"))) {
                // Skip until next label
                if (!lineUpper.startsWith("LBL"))
                    continue;
            }
            optimized.add(line);
        }
        if (ArgumentParser.Args.debug) {
            common.dbgprint("Optimization complete. %d lines after optimization.", optimized.size());
        }
        return String.join("\n", optimized);
    }

    public static interp.instructions parseInstructions(String[] ops) {
        // Preprocess includes first
        String preprocessed = preprocess(ops);
        // Add optimization pass here
        System.out.println("Preprocessed code:\n" + preprocessed);
        String optimized = optimize(preprocessed);


        String[] processedOps = optimized.split("\n");

        interp.instructions instrs = new interp.instructions();
        instrs.instructions = new interp.instruction[100]; // reasonable default size
        instrs.Memory = new int[1000]; // reasonable default memory size
        instrs.length = 0;
        instrs.memory_size = 1000;
        // calculate the total size
        int totalSize = Arrays.stream(ops).mapToInt(String::length).sum();
        instrs.max_labels = totalSize / 10;
        instrs.max_instructions = totalSize / 10;
        instrs.labels = new int[instrs.max_labels];
        instrs.functions = new Functions();

        // First pass: collect labels
        int currentLine = 0;
        for (String line : processedOps) {
            line = line.trim();
            if (!line.isEmpty() && !line.startsWith(";")) {
                if (line.startsWith("LBL ")) {
                    String labelName = line.substring(4).trim();
                    instrs.labelMap.put(labelName, currentLine);
                    continue;
                }
                currentLine++;
            }
        }

        // Second pass: read instructions
        currentLine = 0;
        for (String line : processedOps) {
            line = line.trim();
            if (!line.isEmpty() &&
                    !line.startsWith(";") &&
                    !line.startsWith("LBL")) {
                interp.instruction instr = new interp.instruction();
                instr.lineNumber = currentLine;
                instr.originalLine = line;

                // Special handling for DB instruction
                if (line.toUpperCase().startsWith("DB ")) {
                    instr.name = "DB";
                    // Keep everything after "DB " as is
                    String restOfLine = line.substring(2).trim();
                    instr.sop1 = restOfLine;
                    instr.sop2 = null; // DB doesn't need sop2
                } else {
                    // Normal instruction parsing
                    String[] parts = line.split("\\s+", 3); // Limit split to 3 parts
                    instr.name = parts[0];
                    // Remove any inline comments from the operands
                    if (parts.length > 1) {
                        String op1 = parts[1];
                        int commentStart = op1.indexOf(';');
                        if (commentStart != -1) {
                            op1 = op1.substring(0, commentStart).trim();
                        }
                        instr.sop1 = op1;
                    }
                    if (parts.length > 2) {
                        String op2 = parts[2];
                        int commentStart = op2.indexOf(';');
                        if (commentStart != -1) {
                            op2 = op2.substring(0, commentStart).trim();
                        }
                        instr.sop2 = op2;
                    }
                }

                instrs.instructions[instrs.length] = instr;
                instrs.Memory[instrs.length] = instrs.length; // Add this line to populate memory
                instrs.length++;
            }
        }

        if (ArgumentParser.Args.debug) {
            print(
                    "Read %d instructions and %d labels\n",
                    instrs.length,
                    instrs.labelMap.size());

        }

        return instrs;
    }

    public static class instruction {

        String name;
        int opcode;
        int iop1;
        int iop2;
        String sop1;
        String sop2;
        int lineNumber; // Add line number tracking
        String originalLine; // Add original line tracking
    }

    public static Functions functions = new Functions();
    public static ArgumentParser.Args arguments = new ArgumentParser.Args();

    // instructions class
    public static class instructions {

        // holds all the instructions
        public instruction[] instructions;
        // holds the length of the instructions array
        public int length;
        // holds the memory size we requested
        public int memory_size;
        public int max_labels;
        public int max_instructions;
        public int Memory[];
        public int labels[];
        public Functions functions;
        public HashMap<String, Integer> labelMap;
        public int currentLine; // Add current line tracking
        public String currentlineContents; // Add current line contents tracking

        public instructions() {
            labelMap = new HashMap<>();
        }
    }

    public static interp terp = new interp();

    public static void ExecuteAllInstructions(instructions instrs) {
        Integer mainAddress = instrs.labelMap.get("main");

        // Only enforce main label in non-test mode
        if (!testMode && mainAddress == null) {
            throw new MASMException("No 'main' label found in instructions", instrs.currentLine,
                    instrs.currentlineContents, "Error in instruction: main");

        }

        // In test mode, start from instruction 0 if no main
        common.WriteRegister("RIP", mainAddress != null ? mainAddress : 0);
        String instrString = "";
        int rip = common.ReadRegister("RIP");
        common.isRunning = true; // Reset running state
        long startTime = System.currentTimeMillis();
        while (rip < instrs.length && common.isRunning) {

            // use arguments.cpu speed to control execution speed
            // Higher values = faster execution (less sleep time)
            // if (ArgumentParser.Args.cpuSpeed > 0) {
            //     try {
            //         Thread.sleep(1000 / ArgumentParser.Args.cpuSpeed);
            //     } catch (InterruptedException e) {
            //         // Handle exception
            //     }
            // }

            instruction instr = instrs.instructions[rip];
            if (ArgumentParser.Args.debug) {
                common.box(
                        "Debug",
                        "Executing instruction: " + instr.name,
                        "info");
            }
            instrs.currentLine = instr.lineNumber; // Track current line

            // Track current line contents
            instrString = instr.name + " " + (instr.sop1 != null ? instr.sop1 : "") + " "
                    + (instr.sop2 != null ? instr.sop2 : "");
            instrs.currentlineContents = instrString;
            terp.ExecuteSingleInstruction(instr, instrs.Memory, instrs);
            if (!common.isRunning)
                break; // Exit if HLT was called
            rip = common.ReadRegister("RIP") + 1;
            common.WriteRegister("RIP", rip);
        }
        // Flush output buffers at the end
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        System.out.println("Execution time: " + executionTime + " ms");
        System.out.println("Start time: " + startTime);
        System.out.println("End time: " + endTime);
        Functions.flushAllBuffers();
    }

    public static void printinstructions(instructions instrs) {
        for (int i = 0; i < instrs.length; i++) {
            print(
                    "Instruction %d: %s %s %s\n",
                    i,
                    instrs.instructions[i].name,
                    instrs.instructions[i].sop1,
                    instrs.instructions[i].sop2);
        }
    }

    public static void runFile(String filename) {
        instructions instrs = new instructions();
        instrs.instructions = new instruction[21463]; // reasonable default size
        instrs.Memory = new int[common.MAX_MEMORY]; // reasonable default memory size
        instrs.length = 0;
        instrs.memory_size = common.MAX_MEMORY;
        instrs.max_labels = 21463;
        instrs.max_instructions = 21463;
        instrs.labels = new int[instrs.max_labels];
        instrs.functions = new Functions();

        // init the stack pointer
        common.WriteRegister("RSP", common.MAX_MEMORY - 1);

        try {
            // Read entire file into string array first
            List<String> lines = new ArrayList<>();
            Scanner scanner = new Scanner(new java.io.File(filename));
            while (scanner.hasNextLine()) {
                lines.add(scanner.nextLine());
            }
            scanner.close();

            // Preprocess and parse
            String preprocessed = preprocess(lines.toArray(new String[0]));
            // Add optimization pass here
            preprocessed = optimize(preprocessed);
            System.out.println("Optimized code:\n" + preprocessed);
            String[] processedLines = preprocessed.split("\n");

            // First pass: collect labels
            int currentLine = 0;
            for (String line : processedLines) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith(";")) {
                    if (line.toLowerCase().startsWith("lbl ")) {
                        String labelName = line.substring(4).trim();
                        instrs.labelMap.put(labelName, currentLine);
                        continue;
                    }
                    currentLine++;
                }
            }

            // Second pass: read instructions
            for (String line : processedLines) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith(";")) {
                    // Skip label declarations but don't treat them as errors
                    if (line.toLowerCase().startsWith("lbl ")) {
                        continue;
                    }

                    instruction instr = new instruction();
                    instr.lineNumber = currentLine;
                    instr.originalLine = line;

                    // Special handling for DB instruction
                    if (line.toUpperCase().startsWith("DB ")) {
                        instr.name = "DB";
                        instr.sop1 = line.substring(2).trim(); // Keep everything after "DB"
                    } else {
                        // Normal instruction parsing
                        String[] parts = line.split("\\s+", 3); // Limit split to 3 parts
                        instr.name = parts[0];
                        // Remove any inline comments from the operands
                        if (parts.length > 1) {
                            String op1 = parts[1];
                            int commentStart = op1.indexOf(';');
                            if (commentStart != -1) {
                                op1 = op1.substring(0, commentStart).trim();
                            }
                            instr.sop1 = op1;
                        }
                        if (parts.length > 2) {
                            String op2 = parts[2];
                            int commentStart = op2.indexOf(';');
                            if (commentStart != -1) {
                                op2 = op2.substring(0, commentStart).trim();
                            }
                            instr.sop2 = op2;
                        }
                    }

                    instrs.instructions[instrs.length] = instr;
                    instrs.Memory[instrs.length] = instrs.length;
                    instrs.length++;
                }
            }

            // Only check for main label in non-test mode
            if (!testMode && !instrs.labelMap.containsKey("main")) {
                throw new MASMException("No 'main' label found in instructions", instrs.currentLine,
                        instrs.currentlineContents, "Error in instruction: main");

            }
            // System.out.println(preprocessed);
            ExecuteAllInstructions(instrs);
        } catch (java.io.FileNotFoundException e) {
            common.box("Error", "File not found: " + filename, "error");
        }
    }

    private static void dumpinstr(instructions instrs) {
        for (int i = 0; i < instrs.length; i++) {
            print(
                    "Instruction %d: %s %s %s\n",
                    i,
                    instrs.instructions[i].name,
                    instrs.instructions[i].sop1,
                    instrs.instructions[i].sop2);
        }
    }

    private static final Map<String, InstructionHandler> instructionTable = new HashMap<>();
    static {
        instructionTable.put("mov", (instr, memory, instrs) -> {
            functions.mov(memory, instr.sop1, instr.sop2, instrs);
            return 0;
        });
        instructionTable.put("add", (instr, memory, instrs) -> {
            functions.add(memory, instr.sop1, instr.sop2, instrs);
            return 0;
        });
        instructionTable.put("sub", (instr, memory, instrs) -> {
            functions.sub(memory, instr.sop1, instr.sop2, instrs);
            return 0;
        });
        instructionTable.put("mul", (instr, memory, instrs) -> {
            functions.mul(memory, instr.sop1, instr.sop2, instrs);
            return 0;
        });
        instructionTable.put("div", (instr, memory, instrs) -> {
            functions.div(memory, instr.sop1, instr.sop2, instrs);
            return 0;
        });
        instructionTable.put("cmp", (instr, memory, instrs) -> {
            functions.cmp(memory, instr.sop1, instr.sop2, instrs);
            return 0;
        });
        instructionTable.put("jmp", (instr, memory, instrs) -> {
            functions.jmp(memory, instr.sop1, instrs);
            return 0;
        });
        instructionTable.put("jeq", (instr, memory, instrs) -> {
            functions.jeq(memory, instr.sop1, instrs);
            return 0;
        });
        instructionTable.put("jne", (instr, memory, instrs) -> {
            functions.jne(memory, instr.sop1, instrs);
            return 0;
        });
        instructionTable.put("call", (instr, memory, instrs) -> {
            functions.call(memory, instr.sop1, instrs);
            return 0;
        });
        instructionTable.put("ret", (instr, memory, instrs) -> {
            functions.ret(instrs);
            return 0;
        });
        instructionTable.put("push", (instr, memory, instrs) -> {
            Functions.push(memory, instr.sop1, instrs);
            return 0;
        });
        instructionTable.put("pop", (instr, memory, instrs) -> {
            Functions.pop(memory, instr.sop1, instrs);
            return 0;
        });
        instructionTable.put("inc", (instr, memory, instrs) -> {
            Functions.inc(memory, instr.sop1, instrs);
            return 0;
        });
        instructionTable.put("dec", (instr, memory, instrs) -> {
            Functions.dec(memory, instr.sop1, instrs);
            return 0;
        });
        instructionTable.put("shl", (instr, memory, instrs) -> {
            Functions.shl(memory, instr.sop1, instr.sop2, instrs);
            return 0;
        });
        instructionTable.put("shr", (instr, memory, instrs) -> {
            Functions.shr(memory, instr.sop1, instr.sop2, instrs);
            return 0;
        });
        instructionTable.put("and", (instr, memory, instrs) -> {
            Functions.and(memory, instr.sop1, instr.sop2, instrs);
            return 0;
        });
        instructionTable.put("or", (instr, memory, instrs) -> {
            Functions.or(memory, instr.sop1, instr.sop2, instrs);
            return 0;
        });
        instructionTable.put("xor", (instr, memory, instrs) -> {
            Functions.xor(memory, instr.sop1, instr.sop2, instrs);
            return 0;
        });
        instructionTable.put("not", (instr, memory, instrs) -> {
            Functions.not(memory, instr.sop1, instrs);
            return 0;
        });
        instructionTable.put("neg", (instr, memory, instrs) -> {
            Functions.neg(memory, instr.sop1, instrs);
            return 0;
        });
        instructionTable.put("out", (instr, memory, instrs) -> {
            functions.out(memory, instr.sop1, instr.sop2, instrs);
            return 0;
        });
        instructionTable.put("cout", (instr, memory, instrs) -> {
            functions.cout(memory, instr.sop1, instr.sop2, instrs);
            return 0;
        });
        instructionTable.put("in", (instr, memory, instrs) -> {
            functions.in(memory, instr.sop1, instr.sop2, instrs);
            return 0;
        });
        instructionTable.put("db", (instr, memory, instrs) -> {
            // DB may have all args in sop1, so split if needed
            String[] args = instr.sop1 != null ? instr.sop1.split("\\s+", 2) : new String[0];
            functions.db(memory, instrs, args);
            return 0;
        });
        instructionTable.put("hlt", (instr, memory, instrs) -> {
            Functions.hlt();
            return 0;
        });
        instructionTable.put("mni", (instr, memory, instrs) -> {

            try {
                if (instr.sop1 == null) {
                    throw new MASMException(
                            "Missing MNI function name",
                            instr.lineNumber,
                            instr.originalLine,
                            "Error in instruction: MNI");
                }

                String[] mniParts = instr.sop1.split("\\.");
                if (mniParts.length != 2) {
                    throw new MASMException(
                            "Invalid MNI function name",
                            instr.lineNumber,
                            instr.originalLine,
                            "Error in instruction: MNI");
                }

                String moduleName = mniParts[0];
                String functionName = mniParts[1];

                // Parse the register arguments
                if (instr.sop2 == null) {
                    throw new MASMException(
                            "Missing MNI register arguments",
                            instr.lineNumber,
                            instr.originalLine,
                            "Error in instruction: MNI");
                }

                String[] registerArgs = instr.sop2.trim().split("\\s+");
                if (registerArgs.length < 2) {
                    throw new MASMException(
                            "MNI requires at least two arguments",
                            instr.lineNumber,
                            instr.originalLine,
                            "Error in instruction: MNI");
                }

                // Handle state variables in arguments
                int[] resolvedArgs = new int[registerArgs.length];
                for (int i = 0; i < registerArgs.length; i++) {
                    String arg = registerArgs[i];
                    if (stateVariables.containsKey(arg)) {
                        resolvedArgs[i] = getStateVariableValue(arg, memory);
                    } else if (arg.startsWith("$")) {
                        resolvedArgs[i] = memory[Integer.parseInt(arg.substring(1))];
                    } else {
                        resolvedArgs[i] = common.ReadRegister(arg);
                    }
                }

                // Create MNI object with resolved arguments
                MNIMethodObject methodObj = new MNIMethodObject(memory, registerArgs);
                methodObj.args = resolvedArgs; // Set resolved arguments
                methodObj.argregs = registerArgs;

                MNIHandler.handleMNICall(moduleName, functionName, methodObj);

            } catch (Exception e) {
                throw new MASMException("Error in MNI instruction: " + e.getMessage(),
                        instr.lineNumber, instr.originalLine, "Error in instruction: mni");
            }
            return 0;
        });

    }

    @FunctionalInterface
    interface InstructionHandler {
        int execute(instruction instr, int[] memory, instructions instrs);
    }

    public int ExecuteSingleInstruction(
            instruction instr,
            int[] memory,
            instructions instrs) {
        try {
            InstructionHandler handler = instructionTable.get(instr.name.toLowerCase());
            if (handler != null) {
                return handler.execute(instr, memory, instrs);
            }

        } catch (Exception e) {
            if (e instanceof MASMException) {
                // throw e;
            } else if (e instanceof MNIException) {
                throw new MASMException(
                        e.getMessage(),
                        instr.lineNumber,
                        instr.originalLine,
                        "Error in instruction: " + instr.name);
            }
            throw new MASMException(
                    e.getMessage(),
                    instr.lineNumber,
                    instr.originalLine,
                    String.format("Error in instruction: %s %s %s",
                            instr.name,
                            instr.sop1 != null ? instr.sop1 : "",
                            instr.sop2 != null ? instr.sop2 : ""));
        }
        return 0;
    }

    // Helper method to parse values (either direct numbers or register references)
    private int parseValue(String arg, int[] memory) {
        if (arg.startsWith("$")) {
            // Memory reference
            int addr = Integer.parseInt(arg.substring(1));
            return memory[addr];
        } else if (arg.startsWith("R")) {
            // Register reference
            return common.ReadRegister(arg);
        } else {
            // Direct number
            try {
                return Integer.parseInt(arg);
            } catch (NumberFormatException e) {
                throw new MASMException(
                        "Invalid number format: " + arg,
                        0,
                        "",
                        "Error in instruction");
            }
        }
    }

    // Helper function to get the type of a state variable
    public static String getStateVariableType(String name) {
        if (!stateVariables.containsKey(name)) {
            throw new MASMException("State variable not found: " + name, 0, "", "Error in state variable access");
        }
        return stateVariables.get(name).type;
    }

    // Helper function to get the value of a state variable
    public static int getStateVariableValue(String name, int[] memory) {
        if (!stateVariables.containsKey(name)) {
            throw new MASMException("State variable not found: " + name, 0, "", "Error in state variable access");
        }
        StateVariable stateVar = stateVariables.get(name);
        int value = 0;
        for (int i = 0; i < stateVar.size; i++) {
            value |= (common.ReadMemory(memory, stateVar.address + i) & 0xFF) << (i * 8);
        }
        return value;
    }

    // Helper function to set the value of a state variable
    public static void setStateVariableValue(String name, int value, int[] memory) {
        if (!stateVariables.containsKey(name)) {
            throw new MASMException("State variable not found: " + name, 0, "", "Error in state variable access");
        }
        StateVariable stateVar = stateVariables.get(name);
        for (int i = 0; i < stateVar.size; i++) {
            common.WriteMemory(memory, stateVar.address + i, (value >> (i * 8)) & 0xFF);
        }
    }

    public static void batchCompareExample(int[] memory, instructions instrs) {
        // Prepare operand pairs
        List<String[]> operandPairs = new ArrayList<>();
        operandPairs.add(new String[] { "RAX", "RBX" });
        operandPairs.add(new String[] { "RCX", "RDX" });
        operandPairs.add(new String[] { "$100", "R8" });

        // Call parallelCmp
        List<Integer> results = instrs.functions.parallelCmp(memory, operandPairs, instrs);

        // Process results
        for (int i = 0; i < results.size(); i++) {
            print("Comparison %d result: %d\n", i, results.get(i));
        }
    }

    // Helper: is this a floating-point register?
    public static boolean isFloatRegister(String reg) {
        return common.floatRegistersMap.containsKey(reg);
    }

    // Helper: read floating-point register
    public static double readFPR(String reg) {
        return common.ReadFloatRegister(reg);
    }

    // Helper: write floating-point register
    public static void writeFPR(String reg, double value) {
        common.WriteFloatRegister(reg, value);
    }
}
