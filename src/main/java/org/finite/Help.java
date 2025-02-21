package org.finite;

public class help {
    private static final String[][] INSTRUCTION_HELP = {
        {"mov", "Moves data between registers or memory\nUsage: MOV dest, source\nExample: MOV RAX, 42"},
        {"add", "Adds two values and stores the result\nUsage: ADD dest, source\nExample: ADD RAX, RBX"},
        {"sub", "Subtracts source from destination\nUsage: SUB dest, source\nExample: SUB RAX, 5"},
        {"mul", "Multiplies two values\nUsage: MUL dest, source\nExample: MUL RAX, RBX"},
        {"div", "Divides destination by source\nUsage: DIV dest, source\nExample: DIV RAX, 2"},
        {"cmp", "Compares two values\nUsage: CMP value1, value2\nExample: CMP RAX, 0"},
        {"jmp", "Jumps to a specified label\nUsage: JMP label\nExample: JMP loop_start"},
        {"out", "Outputs value to specified stream\nUsage: OUT stream, value\nExample: OUT 1, RAX"},
        {"db", "Defines a byte in memory\nUsage: DB value\nExample: DB 65"},
        {"hlt", "Halts program execution\nUsage: HLT\nExample: HLT"}
    };

    public static void help() {
        System.out.println("Available instructions:");
        for (String[] instr : INSTRUCTION_HELP) {
            System.out.println(instr[0]);
        }
        System.out.println("\nUse 'HELP <instruction>' for detailed information.");
    }

    public static void help(String instruction) {
        instruction = instruction.toLowerCase();
        for (String[] instr : INSTRUCTION_HELP) {
            if (instr[0].toLowerCase().equals(instruction)) {
                System.out.println("\n" + instr[1] + "\n");
                return;
            }
        }
        System.out.println("No help available for instruction: " + instruction);
    }
}
