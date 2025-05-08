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
import org.finite.ArgumentParser;
public class interp {

    public static boolean testmode = true;
    public static boolean testMode = Boolean.getBoolean("testMode"); // This line changes

    // Add a Macro class to represent macros
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
            default -> throw new MASMException("Unknown type in STATE declaration: " + type, 0, line, "Error in instruction: STATE");
        };

        int initialValue = 0;
        if (parts.length > 3) {
            try {
                initialValue = Integer.parseInt(parts[3]);
            } catch (NumberFormatException e) {
                throw new MASMException("Invalid initial value in STATE declaration", 0, line, "Error in instruction: STATE");
            }
        }

        if (stateVariables.containsKey(name)) {
            throw new MASMException("Duplicate STATE variable: " + name, 0, line, "Error in instruction: STATE");
        }

        StateVariable stateVar = new StateVariable(name, type, memoryPointer, size);
        stateVariables.put(name, stateVar);

        // Initialize memory with the initial value
        // for (int i = 0; i < size; i++) {
        //     common.WriteMemory(memoryPointer + i, (initialValue >> (i * 8)) & 0xFF);
        // }

        memoryPointer += size;
    }

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
                List<String> parameters = headerParts.length > 1 ? Arrays.asList(headerParts[1].split(",")) : new ArrayList<>();
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
                    throw new MASMException("Macro argument mismatch for: " + instruction, 0, line, "Error in macro expansion");
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

        if (ArgumentParser.Args.debug) {
            print(
                "Read %d instructions and %d labels\n",
                instrs.length,
                instrs.labelMap.size()
            );
           
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
            
            // use arguments.cpu speed to control execution speed
            // Higher values = faster execution (less sleep time)
            if (ArgumentParser.Args.cpuSpeed > 0) {
                try {
                    Thread.sleep(1000 / ArgumentParser.Args.cpuSpeed); 
                } catch (InterruptedException e) {
                    // Handle exception
                }
            }

            instruction instr = instrs.instructions[rip];
            if (ArgumentParser.Args.debug) {
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
            if (ArgumentParser.Args.debug) {
                common.box("Debug", "Executing instruction: " + instr.name, "info");
                //read common.registersMap
                for (String key : common.registersMap.keySet()) {
                    //print("%s: %d\n", key, common.ReadRegister(key));
                  //  print("en");
                }
            }
            
            switch (instr.name.toLowerCase()) {
                case "mov":
                    if (stateVariables.containsKey(instr.sop1)) {
                        StateVariable stateVar = stateVariables.get(instr.sop1);
                        int value = parseValue(instr.sop2, memory);
                        for (int i = 0; i < stateVar.size; i++) {
                            common.WriteMemory(memory, stateVar.address + i, (value >> (i * 8)) & 0xFF); // Fix: Add 'memory' as the first argument
                        }
                    } else if (stateVariables.containsKey(instr.sop2)) {
                        StateVariable stateVar = stateVariables.get(instr.sop2);
                        int value = 0;
                        for (int i = 0; i < stateVar.size; i++) {
                            value |= (common.ReadMemory(memory, stateVar.address + i) & 0xFF) << (i * 8); // Fix: Add 'memory' as the first argument
                        }
                        common.WriteRegister(instr.sop1, value);
                    } else {
                        functions.mov(memory, instr.sop1, instr.sop2, instrs);
                    }
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
                    if (registerArgs.length < 2) {
                        throw new MASMException(
                            "MNI requires at least two arguments",
                            instr.lineNumber,
                            instr.originalLine,
                            "Error in instruction: MNI"
                        );
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
}
