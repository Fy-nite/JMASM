package org.finite.ModuleManager;

import org.finite.common;
import org.finite.Exceptions.MASMException;
import org.finite.*;
public class MNIMethodObject {
    // Register constants
    public static final String[] REGISTERS = {
        "R0", "R1", "R2", "R3", "R4", "R5", "R6", "R7", "R8", "R9","R10", "R11", "R12", "R13", "R14", "R15",
        "RAX", "RBX", "RCX", "RDX",
        "RIP", "RSP", "RBP","RFLAGS"
    };

    // Restore original public variables for compatibility
    public int arg1;
    public int arg2;
    public String reg1;
    public String reg2;
    public int[] args;         // Keep this for existing code
    public String[] argregs;   // Keep this for existing code

    private final int[] memory;
    private final int[] arguments;
    private final String[] argumentRegisters;

    // System state
    private int lastCompareResult;
    private boolean zeroFlag;
    private boolean carryFlag;

    public MNIMethodObject(int[] memory, String... args) {
        this.memory = memory;
        this.arguments = new int[args.length];
        this.argumentRegisters = args;
        
        // Set up legacy fields
        this.args = new int[args.length];
        this.argregs = args;
        if (args.length > 0) {
            this.reg1 = args[0];
            if (args.length > 1) {
                this.reg2 = args[1];
            }
        }

        // Process each argument
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            int value;
            
            if (arg.startsWith("$")) {
                try {
                    int address = Integer.parseInt(arg.substring(1));
                    value = memory[address];
                } catch (NumberFormatException e) {
                    throw new MASMException("Invalid memory address: " + arg, 0, arg, "Invalid memory address");
                }
            } else if (arg.startsWith("0x")) {
                value = Integer.parseInt(arg.substring(2), 16);
            } else if (arg.matches("-?\\d+")) {
                value = Integer.parseInt(arg);
            } else {
                value = common.ReadRegister(arg);
            }
            
            this.arguments[i] = value;
            this.args[i] = value;  // Set legacy array
            
            // Set legacy arg1/arg2
            if (i == 0) this.arg1 = value;
            if (i == 1) this.arg2 = value;
        }
    }

    // New methods to access arguments
    public int getArgument(int index) {
        if (index >= 0 && index < arguments.length) {
            return arguments[index];
        }
        throw new IndexOutOfBoundsException("Argument index out of range: " + index);
    }

    public String getArgumentRegister(int index) {
        if (index >= 0 && index < argumentRegisters.length) {
            return argumentRegisters[index];
        }
        throw new IndexOutOfBoundsException("Argument register index out of range: " + index);
    }

    public int getArgumentCount() {
        return arguments.length;
    }

    // For backward compatibility
    public int getArg1() {
        return getArgument(0);
    }

    public int getArg2() {
        return getArgument(1);
    }

    public String getReg1() {
        return getArgumentRegister(0);
    }

    public String getReg2() {
        return getArgumentRegister(1);
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
        // Check if the name corresponds to a state variable
        if (interp.stateVariables.containsKey(reg)) {
            return interp.getStateVariableValue(reg, memory);
        }
        // Otherwise, treat it as a regular register
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
