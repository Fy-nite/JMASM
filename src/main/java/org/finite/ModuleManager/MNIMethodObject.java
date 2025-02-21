package org.Finite.ModuleManager;

import org.Finite.Common.common;

public class MNIMethodObject {
    // Register constants
    public static final String[] REGISTERS = {
        "R0", "R1", "R2", "R3", "R4", "R5", "R6", "R7", "R8", "R9","R10", "R11", "R12", "R13", "R14", "R15",
        "RAX", "RBX", "RCX", "RDX",
        "RIP", "RSP", "RBP","RFLAGS"
    };

    public int arg1;
    public int arg2;
    public String reg1;  // Register name for first argument
    public String reg2;  // Register name for second argument
    public int args[]; // Arguments
    public String[] argregs;
    private final int[] memory;

    // System state
    private int lastCompareResult;
    private boolean zeroFlag;
    private boolean carryFlag;

    public MNIMethodObject(int[] memory, String reg1, String reg2) {
        this.memory = memory;
        this.reg1 = reg1;
        this.reg2 = reg2;

        // Handle first argument
        if (reg1.startsWith("$")) {
            try {
                int address = Integer.parseInt(reg1.substring(1));
                this.arg1 = memory[address];
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid memory address: " + reg1);
            }
        } else if (reg1.startsWith("0x")) {
            this.arg1 = Integer.parseInt(reg1.substring(2), 16);
        } 
        // register can be a number
        else if (reg1.matches("-?\\d+")) {
            this.arg1 = Integer.parseInt(reg1);
        } 
        else {
            this.arg1 = common.ReadRegister(reg1);
        }

        // Handle second argument
        if (reg2.startsWith("$")) {
            try {
                int address = Integer.parseInt(reg2.substring(1));
                this.arg2 = memory[address];
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid memory address: " + reg2);
            }
        } else if (reg2.startsWith("0x")) {
            this.arg2 = Integer.parseInt(reg2.substring(2), 16);
        }
        // register can be a number
        else if (reg2.matches("-?\\d+")) {
            this.arg2 = Integer.parseInt(reg2);
        } 
        else {
            this.arg2 = common.ReadRegister(reg2);
        }
    }

    // Memory operations
    public int readMemory(int address) {
        return common.ReadMemory(memory, address);
    }
    public String readString(int address) {
        StringBuilder sb = new StringBuilder();
        char c;
        while ((c = (char)readMemory(address++)) != 0) {
            sb.append(c);
        }
        return sb.toString();
    }

    public void writeString(int address, String value) {
        for (int i = 0; i < value.length(); i++) {
            common.WriteMemory(memory, address + i, value.charAt(i));
        }
        common.WriteMemory(memory, address + value.length(), 0); // Null terminator
    }

    public void writeMemory(int address, String value) {
        for (int i = 0; i < value.length(); i++) {
            common.WriteMemory(memory, address + i, value.charAt(i));
        }
        common.WriteMemory(memory, address + value.length(), 0); // Null terminator
    }

    public void writeMemory(int address, int value) {
        common.WriteMemory(memory, address, value);
    }

    // Register operations
    public int getRegister(String reg) {
        return common.ReadRegister(reg);
    }

    public void setRegister(String reg, int value) {
        common.WriteRegister(reg, value);
    }

    // Special register accessors
    public int getIP() {
        return getRegister("RIP");
    }

    public int getSP() {
        return getRegister("RSP");
    }

    public void setSP(int value) {
        setRegister("RSP", value);
    }

    // Flags and state
    public void setCompareResult(int result) {
        this.lastCompareResult = result;
        this.zeroFlag = (result == 0);
    }

    public boolean isZeroFlag() {
        return this.zeroFlag;
    }

    public void setCarryFlag(boolean value) {
        this.carryFlag = value;
    }

    public boolean getCarryFlag() {
        return this.carryFlag;
    }

    // Stack operations
    public void push(int value) {
        int sp = getSP();
        writeMemory(sp, value);
        setSP(sp - 1);
    }

    public int pop() {
        int sp = getSP() + 1;
        setSP(sp);
        return readMemory(sp);
    }
}
