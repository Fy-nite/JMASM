package org.finite;

import org.finite.Common.common;
import org.jline.reader.*;
import org.jline.terminal.*;
import java.util.Arrays;
import static org.finite.Common.common.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class debug {
    private static final Logger logger = LoggerFactory.getLogger(debug.class);
    private static interp.instructions currentProgram = null;
    private static Terminal terminal;
    private static LineReader reader;
    private static int windowSize = 10;
    
    private static void initializeTerminal() throws Exception {
        terminal = TerminalBuilder.builder()
            .system(true)
            .jansi(true)
            .build();
        
        reader = LineReaderBuilder.builder()
            .terminal(terminal)
            .appName("MASM Debugger")
      
            .build();
    }

    private static void printDisplay() {
        if (currentProgram == null) {
            print("\nNo program loaded.\n");
            return;
        }
        
        try {
            // Clear screen
            print("\033[H\033[2J");
            print("\033[0m"); // Reset all attributes
            
            // Header
            print("=== MASM Debugger ===\n\n");
            
            // Registers
            int rip = common.ReadRegister("RIP");
            print("Registers:\n");
            print("RIP: %04d  RAX: %04d  RBX: %04d  RCX: %04d  RDX: %04d\n\n",
                rip,
                common.ReadRegister("RAX"),
                common.ReadRegister("RBX"),
                common.ReadRegister("RCX"),
                common.ReadRegister("RDX"));
            
            // Memory at current position
            print("Memory:\n");
            if (rip >= 0 && rip < currentProgram.Memory.length) {
                print("$%04d: %04d %04d %04d %04d\n\n", 
                    rip,
                    currentProgram.Memory[rip],
                    rip + 1 < currentProgram.Memory.length ? currentProgram.Memory[rip + 1] : 0,
                    rip + 2 < currentProgram.Memory.length ? currentProgram.Memory[rip + 2] : 0,
                    rip + 3 < currentProgram.Memory.length ? currentProgram.Memory[rip + 3] : 0);
            }
            
            // Code section
            print("Code:\n");
            int start = Math.max(0, rip - windowSize/2);
            int end = Math.min(currentProgram.length, start + windowSize);
            
            for (int i = start; i < end; i++) {
                if (i >= currentProgram.length || currentProgram.instructions[i] == null) continue;
                
                interp.instruction instr = currentProgram.instructions[i];
                String marker = (i == rip) ? ">" : " ";
                
                // Highlight current instruction
                if (i == rip) {
                    print("\033[33m"); // Yellow text
                }
                
                print("%s [%04d] %-8s %-15s %s\n",
                    marker,
                    instr.lineNumber,
                    instr.name != null ? instr.name : "",
                    instr.sop1 != null ? instr.sop1 : "",
                    instr.sop2 != null ? instr.sop2 : "");
                
                if (i == rip) {
                    print("\033[0m"); // Reset color
                }
            }
            
            // Footer
            print("\nCommands: (s)tep, (r)un, (q)uit, (h)elp\n");
            print("debug> ");
            
        } catch (Exception e) {
            logger.error("Display error: ", e);
        }
    }

    public static void DebugRepl() {
        try {
            initializeTerminal();
            
            while (true) {
                printDisplay();
                String input = reader.readLine().trim();
                
                try {
                    String[] tokens = input.split(" ");
                    String command = tokens[0].toLowerCase();
                    String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);

                    switch (command) {
                        case "l":
                        case "load":
                            if (args.length < 1) {
                                print("Usage: load <filename>\n");
                                break;
                            }
                            loadProgram(args[0]);
                            break;
                            
                        case "r":
                        case "run":
                            if (currentProgram != null) {
                                interp.ExecuteAllInstructions(currentProgram);
                            }
                            break;

                        case "s":
                        case "step":
                            if (currentProgram != null) {
                                stepInstruction();
                            }
                            break;

                        case "q":
                        case "quit":
                            terminal.close();
                            return;

                        case "h":
                        case "help":
                        default:
                            printHelp();
                            break;
                    }
                } catch (Exception e) {
                    print("Error: %s\n", e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadProgram(String filename) {
        try {
            // Initialize new program state
            common.WriteRegister("RIP", 0);
            common.WriteRegister("RSP", common.MAX_MEMORY - 1);
            common.isRunning = true;
            
            // Create and initialize program
            currentProgram = new interp.instructions();
            currentProgram.Memory = new int[common.MAX_MEMORY];
            currentProgram.instructions = new interp.instruction[1000];
            
            // Read and parse file
            java.nio.file.Path path = java.nio.file.Paths.get(filename);
            String[] lines = java.nio.file.Files.readAllLines(path).toArray(new String[0]);
            currentProgram = interp.parseInstructions(lines);
            
            common.dbgprint("Loaded program with {} instructions", currentProgram.length);
            printDisplay();
        } catch (Exception e) {
            logger.error("Error loading program: ", e);
            terminal.writer().println("Error loading file: " + e.getMessage());
            currentProgram = null;
        }
    }

    private static void stepInstruction() {
        if (currentProgram == null) return;
        
        int rip = common.ReadRegister("RIP");
        if (rip < currentProgram.length) {
            interp terp = new interp();
            terp.ExecuteSingleInstruction(
                currentProgram.instructions[rip],
                currentProgram.Memory,
                currentProgram
            );
            if (common.ReadRegister("RIP") == rip) {
                common.WriteRegister("RIP", rip + 1);
            }
            printDisplay();
        }
    }

    private static void printHelp() {
        print("Available commands:\n");
        print("  l, load <file>  - Load a MASM file\n");
        print("  r, run          - Run program from current position\n");
        print("  s, step         - Execute single instruction\n");
        print("  h, help         - Show this help\n");
        print("  q, quit         - Exit debugger\n");
    }
}