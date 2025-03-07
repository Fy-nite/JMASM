package org.finite;

import static org.finite.common.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.finite.ModuleManager.MNIMethodObject;
import org.finite.ModuleManager.MNIHandler;
import org.finite.Exceptions.MASMException;  // Add this import
import org.finite.Exceptions.MNIException;

public class interp {

    public static boolean testmode = true;
    public static boolean testMode = Boolean.getBoolean("testMode"); // This line changes

    private static boolean hasIncludes(String content) {
        return content.toLowerCase().contains("#include");
    }

    private static String preprocess(String[] lines) {
        StringBuilder processed = new StringBuilder();
        for (String line : lines) {
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
        
        return result;
    }

    public static interp.instructions parseInstructions(String[] ops) {
        // Preprocess includes first
        String preprocessed = preprocess(ops);
        String[] processedOps = preprocessed.split("\n");
        System.out.println(preprocessed);
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
            if (
                !line.isEmpty() &&
                !line.startsWith(";") &&
                !line.startsWith("LBL")
            ) {
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

        if (arguments.debug) {
            print(
                "Read %d instructions and %d labels\n",
                instrs.length,
                instrs.labelMap.size()
            );
            printinstructions(instrs);
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
        int lineNumber;  // Add line number tracking
        String originalLine;  // Add original line tracking
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
        public int currentLine;  // Add current line tracking
        public String currentlineContents;  // Add current line contents tracking
         

        public instructions() {
            labelMap = new HashMap<>();
        }
    }

    public static interp terp = new interp();

    public static void ExecuteAllInstructions(instructions instrs) {
        Integer mainAddress = instrs.labelMap.get("main");

        // Only enforce main label in non-test mode
        if (!testMode && mainAddress == null) {
           throw new MASMException("No 'main' label found in instructions", instrs.currentLine, instrs.currentlineContents, "Error in instruction: main");
          
        }

        // In test mode, start from instruction 0 if no main
        common.WriteRegister("RIP", mainAddress != null ? mainAddress : 0);
        String instrString = "";
        int rip = common.ReadRegister("RIP");
        common.isRunning = true;  // Reset running state
        while (rip < instrs.length && common.isRunning) {
            instruction instr = instrs.instructions[rip];
            if (arguments.debug) {
                common.box(
                    "Debug",
                    "Executing instruction: " + instr.name,
                    "info"
                );
            }
            instrs.currentLine = instr.lineNumber;  // Track current line

            // Track current line contents
            instrString = instr.name + " " + (instr.sop1 != null ? instr.sop1 : "") + " " + (instr.sop2 != null ? instr.sop2 : "");
            instrs.currentlineContents = instrString;
            terp.ExecuteSingleInstruction(instr, instrs.Memory, instrs);
            if (!common.isRunning) break;  // Exit if HLT was called
            rip = common.ReadRegister("RIP") + 1;
            common.WriteRegister("RIP", rip);
        }
    }

    public static void printinstructions(instructions instrs) {
        for (int i = 0; i < instrs.length; i++) {
            print(
                "Instruction %d: %s %s %s\n",
                i,
                instrs.instructions[i].name,
                instrs.instructions[i].sop1,
                instrs.instructions[i].sop2
            );
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
            for (String line : lines) {
                System.out.println(line);
            }
            
            // Preprocess and parse
            String preprocessed = preprocess(lines.toArray(new String[0]));

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
            throw new MASMException("No 'main' label found in instructions", instrs.currentLine, instrs.currentlineContents, "Error in instruction: main");
                
            }
            //System.out.println(preprocessed);
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
                instrs.instructions[i].sop2
            );
        }
    }

    public int ExecuteSingleInstruction(
        instruction instr,
        int[] memory,
        instructions instrs
    ) {
        try {
            if (arguments.debug) {
                common.box("Debug", "Executing instruction: " + instr.name, "info");
            }

            switch (instr.name.toLowerCase()) {
                case "mov":
                    functions.mov(memory, instr.sop1, instr.sop2,instrs);
                    break;
                case "dumpinstr":
                    dumpinstr(instrs);
                    break;
                case "add":
                    functions.add(memory, instr.sop1, instr.sop2,instrs);
                    break;
                case "sub":
                    functions.sub(memory, instr.sop1, instr.sop2,instrs);
                    break;
                case "mul":
                    functions.mul(memory, instr.sop1, instr.sop2,instrs);
                    break;
                case "div":
                    functions.div(memory, instr.sop1, instr.sop2,instrs);
                    break;
                case "cmp":
                    functions.cmp(memory, instr.sop1, instr.sop2,instrs);
                    break;
                case "ret":
                    functions.ret(instrs);
                    break;
                case "hlt":
                    functions.hlt();
                    break;
                case "nop":
                    // skip
                    break;
                case "calle":
                // memory,  target  , instrs
                    functions.calle(memory, instr.sop1, instrs);
                    break;
                case "callne":
                    functions.callne(memory, instr.sop1, instrs);
                    break;
                case "out":
                    // out wants a fd or "place to output to"
                    // 1 is stdout where as 2 is stderr
                    String Splitted = instr.sop1.split(" ")[0];
                    functions.out(memory, Splitted, instr.sop2,instrs);
                    break;
                case "in":
                    // in wants a fd or "place to input from"
                    // 0 is stdin
                    String Splitted1 = instr.sop1.split(" ")[0];
                    functions.in(memory, Splitted1, instr.sop2,instrs);
                    break;
                case "cout":
                    String Splitted2 = instr.sop1.split(" ")[0];
                    functions.cout(memory, Splitted2, instr.sop2,instrs);
                    break;
                case "jmp":
                    functions.jmp(memory, instr.sop1, instrs);
                    break;
                case "je":
                    functions.jeq(memory, instr.sop1, instrs);
                    break;
                case "jne":
                    functions.jne(memory, instr.sop1, instrs);
                    break;
                case "db":
                    String[] dbParts = instr.sop1.split("\\s+");
                    functions.db(memory, instrs,dbParts);
                    break;
                case "push":
                    functions.push(memory, instr.sop1,instrs);
                    break;
                case "pop":
                    functions.pop(memory, instr.sop1,instrs);
                    break;
                case "inc":
                    functions.inc(memory, instr.sop1,instrs);
                    break;
                case "dec":
                    functions.dec(memory, instr.sop1,instrs);
                    break;
                case "call":
                    functions.call(memory, instr.sop1, instrs);
                    break;
                case "shl":
                    functions.shl(memory, instr.sop1, instr.sop2,instrs);
                    break;
                case "shr":
                    functions.shr(memory, instr.sop1, instr.sop2,instrs);
                    break;
                case "and":
                    functions.and(memory, instr.sop1, instr.sop2,instrs);
                    break;
                case "or":
                    functions.or(memory, instr.sop1, instr.sop2,instrs);
                    break;
                case "xor":
                    functions.xor(memory, instr.sop1, instr.sop2,instrs);
                    break;
                case "not":
                    functions.not(memory, instr.sop1,instrs);
                    break;
                case "neg":
                    functions.neg(memory, instr.sop1,instrs);
                    break;
                case "mni":
                    // MNI format: MNI module.function reg1 reg2
                    if (instr.sop1 == null) {
                        throw new MASMException(
                            "Missing MNI function name",
                            instr.lineNumber,
                            instr.originalLine,
                            "Error in instruction: MNI"
                        );
                    }

                    String[] mniParts = instr.sop1.split("\\.");
             
                    if (mniParts.length != 2) {
                        throw new MASMException(
                            "Invalid MNI function name",
                            instr.lineNumber,
                            instr.originalLine,
                            "Error in instruction: MNI"
                        );
                    }

                    String moduleName = mniParts[0];
                    String functionName = mniParts[1];

                    // Parse the register arguments
                    if (instr.sop2 == null) {
                        throw new MASMException(
                            "Missing MNI register arguments",
                            instr.lineNumber,
                            instr.originalLine,
                            "Error in instruction: MNI"
                        );
                    }

                    String[] registerArgs = instr.sop2.trim().split("\\s+");
                   // do not care about min and max args
                    if (registerArgs.length < 2) {
                        throw new MASMException(
                            "MNI requires at least two register arguments",
                            instr.lineNumber,
                            instr.originalLine,
                            "Error in instruction: MNI"
                        );
                    }
                    // Create MNI object with register names
                    MNIMethodObject methodObj = new MNIMethodObject(memory, registerArgs[0], registerArgs[1]);
                    methodObj.argregs = registerArgs;
                    MNIHandler.handleMNICall(moduleName, functionName, methodObj);
                    break;
                default:
                throw new MASMException(
                    "Unknown instruction: " + instr.name,
                    instr.lineNumber,
                    instr.originalLine,
                    "Error in instruction: " + instr.name
                );
            }

            return 0;
        } catch (Exception e) {
            if (e instanceof MASMException) {
               // throw e;
            }
            else if (e instanceof MNIException) {
                throw new MASMException(
                    e.getMessage(),
                    instr.lineNumber,
                    instr.originalLine,
                    "Error in instruction: " + instr.name
                );
            }
            throw new MASMException(
                e.getMessage(),
                instr.lineNumber,
                instr.originalLine,
                String.format("Error in instruction: %s %s %s",
                    instr.name,
                    instr.sop1 != null ? instr.sop1 : "",
                    instr.sop2 != null ? instr.sop2 : "")
            );
        }
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
                    "Error in instruction"
                );
            }
        }
    }
}
