public class Memory {
    private final int[] mem;
    public Memory(int size) { mem = new int[size]; }
    public int get(int addr) {
        if (addr < 0 || addr >= mem.length) throw new IndexOutOfBoundsException("Invalid memory address: " + addr);
        return mem[addr];
    }
    public void set(int addr, int value) {
        if (addr < 0 || addr >= mem.length) throw new IndexOutOfBoundsException("Invalid memory address: " + addr);
        mem[addr] = value;
    }
    public int size() { return mem.length; }
}
