package org.Finite;

import java.util.Arrays;
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
    }

    public static void ExecuteAllInstructions(instructions instrs) {
        for (int i = 0; i < instrs.length; i++) {
            print("Executing instruction: %s\n", instrs.instructions[i].name);
            ExecuteSingleInstruction(instrs.instructions[i], instrs.Memory);
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

        Scanner scanner = new Scanner(System.in);
        try {
            scanner = new Scanner(new java.io.File(filename));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty() && !line.startsWith(";")) {
                    instruction instr = new instruction();
                    String[] parts = line.split("\\s+");
                    instr.name = parts[0];
                    if (parts.length > 1) instr.sop1 = parts[1];
                    if (parts.length > 2) instr.sop2 = parts[2];
                    instrs.instructions[instrs.length++] = instr;
                }
            }
            ExecuteAllInstructions(instrs);
        } catch (java.io.FileNotFoundException e) {
            printerr("File not found: " + filename);
        } finally {
            scanner.close();
        }
    }

    public static int ExecuteSingleInstruction(instruction instr, int[] memory) {
        print("Executing instruction: %s\n", instr.name);
        switch (instr.name.toLowerCase()) {
            case "mov":
                functions.mov(memory, instr.sop1, instr.sop2);
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
            case "out":
                functions.out(memory, instr.sop1);
                break;

            default:
                printerr("Unknown instruction: " + instr.name);
                return -1;
        }

        return 0;
    }

}