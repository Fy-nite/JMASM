package org.Finite.Memory;

import org.Finite.Config.MASMConfig;
import org.Finite.Exceptions.MASMException;

public class MemoryManager {
    private static final MemoryManager INSTANCE = new MemoryManager();
    private final int[] memory;
    private final int memorySize;

    private MemoryManager() {
        memorySize = MASMConfig.getInstance().getMemorySize();
        memory = new int[memorySize];
    }

    public static MemoryManager getInstance() {
        return INSTANCE;
    }

    public void write(int address, int value) {
        validateAddress(address);
        memory[address] = value;
    }

    public int read(int address) {
        validateAddress(address);
        return memory[address];
    }

    private void validateAddress(int address) {
        if (address < 0 || address >= memorySize) {
            throw new MASMException(
                "Memory access violation",
                0,
                "Memory access",
                String.format("Address %d is out of bounds (0-%d)", address, memorySize - 1)
            );
        }
    }

    public void reset() {
        for (int i = 0; i < memorySize; i++) {
            memory[i] = 0;
        }
    }
}
