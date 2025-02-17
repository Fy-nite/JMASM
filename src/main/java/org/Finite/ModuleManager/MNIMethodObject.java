package org.Finite.ModuleManager;

import org.Finite.Common.common;

public class MNIMethodObject {
    // Register constants
    public static final String[] REGISTERS = {
        "R0", "R1", "R2", "R3", "R4", "R5", "R6", "R7", "R8", "R9",
        "RAX", "RBX", "RCX", "RDX",
        "RIP", "RSP", "RBP","RFLAGS"
    };

    public int arg1;
    public int arg2;
    public String reg1;  // Register name for first argument
    public String reg2;  // Register name for second argument
    private final int[] memory;

    // System state
    private int lastCompareResult;
    private boolean zeroFlag;
    private boolean carryFlag;

    public MNIMethodObject(int[] memory, String reg1, String reg2) {
        this.memory = memory;
        this.reg1 = reg1;
        this.reg2 = reg2;
        this.arg1 = common.ReadRegister(reg1);
        this.arg2 = common.ReadRegister(reg2);
    }

    // Memory operations
    public int readMemory(int address) {
        return common.ReadMemory(memory, address);
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
