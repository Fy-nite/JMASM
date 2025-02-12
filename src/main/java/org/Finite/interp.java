package org.Finite;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import static org.Finite.common.*;
import static org.Finite.debug.*;

import org.Finite.Functions.*;

public class interp {
    public static class instruction {
        String name;
        int opcode;
        int iop1;
        int iop2;
        String sop1;
        String sop2;
    }

    static Functions functions = new Functions();
    static ArgumentParser.Args arguments = new ArgumentParser.Args();
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
        public HashMap<String, Integer> labelMap; // Add this field

        public instructions() {
            labelMap = new HashMap<>();
        }
    }

    public static void include(String filename, instructions instrs) {

    }

    public static void ExecuteAllInstructions(instructions instrs) {
        int rip = common.ReadRegister("RIP");
        while (rip < instrs.length) {  // Changed condition to check actual instruction count
            instruction instr = instrs.instructions[rip];
            if (arguments.debug) {
                common.box("Debug", "Executing instruction: " + instr.name, "info");
            }
            ExecuteSingleInstruction(instr, instrs.Memory, instrs);
            rip = common.ReadRegister("RIP") + 1;
            common.WriteRegister("RIP", rip);
        }
    }

    public static void printinstructions(instructions instrs) {
        for (int i = 0; i < instrs.length; i++) {
            print("Instruction %d: %s %s %s\n", i, instrs.instructions[i].name, instrs.instructions[i].sop1,
                    instrs.instructions[i].sop2);
        }
    }
    public static void runFile(String filename) {
        instructions instrs = new instructions();
        instrs.instructions = new instruction[100];  // reasonable default size
        instrs.Memory = new int[1000];              // reasonable default memory size
        instrs.length = 0;
        instrs.memory_size = 1000;
        instrs.max_labels = 100;
        instrs.max_instructions = 100;
        instrs.labels = new int[100];
        instrs.functions = new Functions();

        // First pass: collect labels
        try {
            Scanner scanner = new Scanner(new java.io.File(filename));
            int currentLine = 0;

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty() && !line.startsWith(";")) {
                    if (line.startsWith("LBL ")) {
                        String labelName = line.substring(4).trim();
                        instrs.labelMap.put(labelName, currentLine);
                        continue;
                    }
                    currentLine++;
                }
            }
            scanner.close();

            // Second pass: read instructions
            scanner = new Scanner(new java.io.File(filename));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty() && !line.startsWith(";") && !line.startsWith("LBL")) {
                    instruction instr = new instruction();
                    String[] parts = line.split("\\s+");
                    instr.name = parts[0];
                    if (parts.length > 1) instr.sop1 = parts[1];
                    if (parts.length > 2) instr.sop2 = parts[2];
                    instrs.instructions[instrs.length] = instr;
                    instrs.Memory[instrs.length] = instrs.length;  // Add this line to populate memory
                    instrs.length++;
                }
            }

            if (arguments.debug) {
                print("Read %d instructions and %d labels\n", instrs.length, instrs.labelMap.size());
                printinstructions(instrs);
            }

            ExecuteAllInstructions(instrs);
        } catch (java.io.FileNotFoundException e) {
            common.box("Error", "File not found: " + filename, "error");
        }
    }

    public static int ExecuteSingleInstruction(instruction instr, int[] memory, instructions instrs) {
        if (arguments.debug) {
            common.box("Debug", "Executing instruction: " + instr.name, "info");
        }

        switch (instr.name.toLowerCase()) {
            case "mov":
                functions.mov(memory, instr.sop1, instr.sop2);
                break;
            case "#include":
                functions.include(instr.sop1, instrs);
                break;
            case "add":
                functions.add(memory, instr.sop1, instr.sop2);
                break;
            case "sub":
                functions.sub(memory, instr.sop1, instr.sop2);
                break;
            case "mul":
                functions.mul(memory, instr.sop1, instr.sop2);
                break;
            case "div":
                functions.div(memory, instr.sop1, instr.sop2);
                break;
            case "cmp":
                functions.cmp(memory, instr.sop1, instr.sop2);
                break;
            case "hlt":
                System.exit(0);
                break;
            case "out":
                // out wants a fd or "place to output to"
                // 1 is stdout where as 2 is stderr
                String Splitted = instr.sop1.split(" ")[0];

                functions.out(memory, Splitted, instr.sop2);
                break;
            case "jmp":
                if (instr.sop1.startsWith("#")) {
                    // Handle label jump
                    String labelName = instr.sop1.substring(1);
                    Integer labelAddress = instrs.labelMap.get(labelName);
                    if (labelAddress != null) {
                        functions.jmp(memory, String.valueOf(labelAddress));
                    } else {
                        common.box("Error", "Unknown label: " + labelName, "error");
                        return -1;
                    }
                } else {
                    functions.jmp(memory, instr.sop1);
                }
                break;
            case "db":
                functions.db(memory, instr.sop1);

                break;

            default:
                common.box("Error", "Unknown instruction: " + instr.name, "error");
                return -1;
        }

        return 0;
    }

}