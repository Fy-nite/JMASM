package org.finite;

import org.finite.Common.common;
import org.finite.Exceptions.MASMException;

public class Instructions {
    // Instruction types
    public static final int TYPE_ARITHMETIC = 1;
    public static final int TYPE_MEMORY = 2;
    public static final int TYPE_CONTROL = 3;
    public static final int TYPE_STACK = 4;
    public static final int TYPE_IO = 5;

    public static class InstructionDefinition {
        public final String name;
        public final int type;
        public final int opcode;
        public final int operandCount;
        public final boolean hasLabel;

        public InstructionDefinition(String name, int type, int opcode, int operandCount, boolean hasLabel) {
            this.name = name;
            this.type = type;
            this.opcode = opcode;
            this.operandCount = operandCount;
            this.hasLabel = hasLabel;
        }
    }

    private static final InstructionDefinition[] INSTRUCTIONS = {
        new InstructionDefinition("mov", TYPE_MEMORY, 0x01, 2, false),
        new InstructionDefinition("add", TYPE_ARITHMETIC, 0x02, 2, false),
        new InstructionDefinition("sub", TYPE_ARITHMETIC, 0x03, 2, false),
        new InstructionDefinition("mul", TYPE_ARITHMETIC, 0x04, 2, false),
        new InstructionDefinition("div", TYPE_ARITHMETIC, 0x05, 2, false),
        new InstructionDefinition("cmp", TYPE_ARITHMETIC, 0x06, 2, false),
        new InstructionDefinition("jmp", TYPE_CONTROL, 0x07, 1, true),
        new InstructionDefinition("je", TYPE_CONTROL, 0x08, 1, true),
        new InstructionDefinition("jne", TYPE_CONTROL, 0x09, 1, true),
        new InstructionDefinition("call", TYPE_CONTROL, 0x0A, 1, true),
        new InstructionDefinition("ret", TYPE_CONTROL, 0x0B, 0, false),
        new InstructionDefinition("push", TYPE_STACK, 0x0C, 1, false),
        new InstructionDefinition("pop", TYPE_STACK, 0x0D, 1, false),
        new InstructionDefinition("db", TYPE_MEMORY, 0x0E, 2, false),
        new InstructionDefinition("hlt", TYPE_CONTROL, 0xFF, 0, false),
        new InstructionDefinition("lbl", TYPE_CONTROL, 0x0F, 1, true),
    };

    public static InstructionDefinition getDefinition(String name) {
        name = name.toLowerCase();
        for (InstructionDefinition def : INSTRUCTIONS) {
            if (def.name.equals(name)) {
                return def;
            }
        }
        return null;
    }

    public static InstructionDefinition getDefinition(int opcode) {
        for (InstructionDefinition def : INSTRUCTIONS) {
            if (def.opcode == opcode) {
                return def;
            }
        }
        return null;
    }

    public static boolean validateInstruction(String name, String op1, String op2) {
        InstructionDefinition def = getDefinition(name);
        if (def == null) {
            return false;
        }

        // Check operand count
        if (def.operandCount == 0 && (op1 != null || op2 != null)) {
            return false;
        }
        if (def.operandCount == 1 && op2 != null) {
            return false;
        }
        if (def.operandCount == 2 && (op1 == null || op2 == null)) {
            return false;
        }

        return true;
    }

    public static int executeInstruction(String name, String op1, String op2, int[] memory, interp.instructions instrs) {
        InstructionDefinition def = getDefinition(name);
        if (def == null) {
            throw new MASMException("Unknown instruction: " + name, instrs.currentLine, instrs.currentlineContents, "Error in instruction");
        }

        switch (def.type) {
            case TYPE_ARITHMETIC:
                return executeArithmetic(def, op1, op2, memory, instrs);
            case TYPE_MEMORY:
                return executeMemory(def, op1, op2, memory, instrs);
            case TYPE_CONTROL:
                return executeControl(def, op1, op2, memory, instrs);
            case TYPE_STACK:
                return executeStack(def, op1, op2, memory, instrs);
            case TYPE_IO:
                return executeIO(def, op1, op2, memory, instrs);
            default:
                throw new MASMException("Unknown instruction type", instrs.currentLine, instrs.currentlineContents, "Error in instruction");
        }
    }

    private static int executeArithmetic(InstructionDefinition def, String op1, String op2, int[] memory, interp.instructions instrs) {
        // Implementation for arithmetic instructions
        int value1 = common.ReadRegister(op1);
        int value2 = common.ReadRegister(op2);
        
        switch (def.name) {
            case "add":
                common.WriteRegister(op1, value1 + value2);
                break;
            case "sub":
                common.WriteRegister(op1, value1 - value2);
                break;
            case "mul":
                common.WriteRegister(op1, value1 * value2);
                break;
            case "div":
                if (value2 == 0) {
                    throw new MASMException("Division by zero", instrs.currentLine, instrs.currentlineContents, "Error in instruction");
                }
                common.WriteRegister(op1, value1 / value2);
                break;
            case "cmp":
                common.WriteRegister("RFLAGS", (value1 == value2) ? 1 : 0);
                break;
        }
        return 0;
    }

    // ... implement other execute methods similarly ...

    private static int executeMemory(InstructionDefinition def, String op1, String op2, int[] memory, interp.instructions instrs) {
        // Implementation for memory instructions
        return 0;
    }

    private static int executeControl(InstructionDefinition def, String op1, String op2, int[] memory, interp.instructions instrs) {
        // Implementation for control flow instructions
        return 0;
    }

    private static int executeStack(InstructionDefinition def, String op1, String op2, int[] memory, interp.instructions instrs) {
        // Implementation for stack instructions
        return 0;
    }

    private static int executeIO(InstructionDefinition def, String op1, String op2, int[] memory, interp.instructions instrs) {
        // Implementation for I/O instructions
        return 0;
    }
}
