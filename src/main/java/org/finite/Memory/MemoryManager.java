package org.finite.Memory;

import org.finite.Config.MASMConfig;
import org.finite.Exceptions.MASMException;
import java.util.Arrays;

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

    public final void write(int address, int value) {
        // Inline validation for speed
        if (address < 0 || address >= memorySize) {
            throw new MASMException(
                "Memory access violation",
                0,
                "Memory access",
                String.format("Address %d is out of bounds (0-%d)", address, memorySize - 1)
            );
        }
        memory[address] = value;
    }

    public final int read(int address) {
        if (address < 0 || address >= memorySize) {
            throw new MASMException(
                "Memory access violation",
                0,
                "Memory access",
                String.format("Address %d is out of bounds (0-%d)", address, memorySize - 1)
            );
        }
        return memory[address];
    }

    public final void reset() {
        Arrays.fill(memory, 0);
    }
}
