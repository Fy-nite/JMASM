package org.finite;

import org.jline.reader.*;
import org.jline.terminal.*;
import static org.finite.common.*;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Debugger {
    private static final Logger logger = LoggerFactory.getLogger(Debugger.class);
    private static interp.instructions currentProgram = null;
    private static Terminal terminal;
    private static LineReader reader;
    private static final int WINDOW_SIZE = 10;

    public static void main(String[] args) {
        Debugger debugger = new Debugger();
        debugger.startRepl();
    }

    private void initializeTerminal() throws Exception {
        terminal = TerminalBuilder.builder().system(true).jansi(true).build();
        reader = LineReaderBuilder.builder().terminal(terminal).appName("MASM Debugger").build();
    }

    private void startRepl() {
        try {
            initializeTerminal();
            while (true) {
                display();
                String input = reader.readLine().trim();
                processCommand(input);
            }
        } catch (Exception e) {
            logger.error("Error in REPL: ", e);
        }
    }

    private void processCommand(String input) {
        try {
            String[] tokens = input.split(" ");
            String command = tokens[0].toLowerCase();
            String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);

            switch (command) {
                case "load":
                    if (args.length < 1) {
                        print("Usage: load <filename>\n");
                    } else {
                        loadProgram(args[0]);
                    }
                    break;
                case "step":
                    stepInstruction();
                    break;
                case "run":
                    runProgram();
                    break;
                case "quit":
                    terminal.close();
                    System.exit(0);
                    break;
                default:
                    printHelp();
                    break;
            }
        } catch (Exception e) {
            print("Error: %s\n", e.getMessage());
        }
    }

    private void loadProgram(String filename) {
        try {
            common.WriteRegister("RIP", 0);
            common.WriteRegister("RSP", common.MAX_MEMORY - 1);
            currentProgram = new interp.instructions();
            currentProgram.Memory = new int[common.MAX_MEMORY];
            currentProgram.instructions = new interp.instruction[1000];

            java.nio.file.Path path = java.nio.file.Paths.get(filename);
            String[] lines = java.nio.file.Files.readAllLines(path).toArray(new String[0]);
            currentProgram = interp.parseInstructions(lines);

            common.dbgprint("Loaded program with {} instructions", currentProgram.length);
        } catch (Exception e) {
            logger.error("Error loading program: ", e);
            print("Error loading file: %s\n", e.getMessage());
            currentProgram = null;
        }
    }

    private void stepInstruction() {
        if (currentProgram == null) {
            print("No program loaded. Use 'load <file>' to load a program.\n");
            return;
        }

        try {
            int rip = common.ReadRegister("RIP");
            while (rip >= 0 && rip < currentProgram.length) {
                interp.instruction currentInstruction = currentProgram.instructions[rip];

                if (currentInstruction != null && "lbl".equalsIgnoreCase(currentInstruction.name)) {
                    common.WriteRegister("RIP", rip + 1);
                    rip = common.ReadRegister("RIP");
                    continue;
                }

                if (currentInstruction == null || currentInstruction.name == null || currentInstruction.name.isEmpty()) {
                    print("Invalid or unsupported instruction at line %d: %s\n", rip, currentInstruction);
                    return;
                }

                interp terp = new interp();
                terp.ExecuteSingleInstruction(currentInstruction, currentProgram.Memory, currentProgram);

                if (common.ReadRegister("RIP") == rip) {
                    common.WriteRegister("RIP", rip + 1);
                }

                break;
            }
        } catch (Exception e) {
            logger.error("Error during step execution: ", e);
            print("Error during step execution: %s\n", e.getMessage());
        }
    }

    private void runProgram() {
        if (currentProgram == null) {
            print("No program loaded. Use 'load <file>' to load a program.\n");
            return;
        }

        try {
            while (common.ReadRegister("RIP") >= 0 && common.ReadRegister("RIP") < currentProgram.length) {
                stepInstruction();
            }
        } catch (Exception e) {
            logger.error("Error during program execution: ", e);
            print("Error during program execution: %s\n", e.getMessage());
        }
    }

    private void display() {
        if (currentProgram == null) {
            print("\nNo program loaded.\n");
            return;
        }

        try {
            print("\033[H\033[2J\033[0m");
            print("=== MASM Debugger ===\n\n");

            int rip = common.ReadRegister("RIP");
            print("Registers:\n");
            print("RIP: %04d  RAX: %04d  RBX: %04d  RCX: %04d  RDX: %04d\n\n",
                rip,
                common.ReadRegister("RAX"),
                common.ReadRegister("RBX"),
                common.ReadRegister("RCX"),
                common.ReadRegister("RDX"));

            print("Memory:\n");
            if (rip >= 0 && rip < currentProgram.Memory.length) {
                print("$%04d: %04d %04d %04d %04d\n\n",
                    rip,
                    currentProgram.Memory[rip],
                    rip + 1 < currentProgram.Memory.length ? currentProgram.Memory[rip + 1] : 0,
                    rip + 2 < currentProgram.Memory.length ? currentProgram.Memory[rip + 2] : 0,
                    rip + 3 < currentProgram.Memory.length ? currentProgram.Memory[rip + 3] : 0);
            }

            print("Code:\n");
            int start = Math.max(0, rip - WINDOW_SIZE / 2);
            int end = Math.min(currentProgram.length, start + WINDOW_SIZE);

            for (int i = start; i < end; i++) {
                if (i >= currentProgram.length || currentProgram.instructions[i] == null) continue;

                interp.instruction instr = currentProgram.instructions[i];
                String marker = (i == rip) ? ">" : " ";

                if (i == rip) {
                    print("\033[33m");
                }

                print("%s [%04d] %-8s %-15s %s\n",
                    marker,
                    instr.lineNumber,
                    instr.name != null ? instr.name : "",
                    instr.sop1 != null ? instr.sop1 : "",
                    instr.sop2 != null ? instr.sop2 : "");

                if (i == rip) {
                    print("\033[0m");
                }
            }

            print("\nCommands: load <file>, step, run, quit\n");
        } catch (Exception e) {
            logger.error("Display error: ", e);
        }
    }

    private void printHelp() {
        print("Available commands:\n");
        print("  load <file>  - Load a MASM file\n");
        print("  step         - Execute single instruction\n");
        print("  run          - Run program from current position\n");
        print("  quit         - Exit debugger\n");
    }

    private void print(String format, Object... args) {
        terminal.writer().printf(format, args);
        terminal.writer().flush();
    }
}