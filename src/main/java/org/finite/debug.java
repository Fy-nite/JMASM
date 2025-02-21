package org.finite;

import org.finite.Common.common;

import java.util.Arrays;
import java.util.Scanner;

import static org.finite.Common.common.*;

public class debug {

    public static void DebugRepl() {
        try (Scanner scanner = new Scanner(System.in)) {
            interp.instructions di = new interp.instructions();
            while (true) {
                print("> ");
                String input = scanner.nextLine();
                String[] tokens = input.split(" ");
                String command = tokens[0];
                String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);
                try {
                    // Clear the instruction state first
                    di.instructions = new interp.instruction[1];
                    di.instructions[0] = new interp.instruction();
                    di.length = 0;

                    // Only set up instruction if it's not a built-in command
                    if (!Arrays.asList("dumpmemory", "dumpregisters", "readmemory", 
                            "writememory", "readregister", "writeregister", "exit", "help")
                            .contains(command.toLowerCase())) {
                        di.instructions[0].name = command;
                        di.instructions[0].opcode = 0;
                        if (args.length >= 1) {
                            di.instructions[0].sop1 = args[0];
                        }
                        if (args.length >= 2) {
                            di.instructions[0].sop2 = args[1];
                        }
                        di.length = command.length();
                    }
                } catch (Exception e) {
                    printerr("Error: " + e.getMessage());
                }

                switch (command) {
                    case "dumpmemory":
                        dumpMemory(common.memory);
                        break;
                    case "dumpregisters":
                        dumpRegisters();
                        break;
                    case "readmemory":
                        try {
                            print("%d", ReadMemory(common.memory, Integer.parseInt(args[0])));
                        } catch (Exception e) {
                            printerr("Error: " + e.getMessage());
                        }
                        break;
                    case "#include":
                        try {
                            Includemanager.include(args[0], input);
                        } catch (Exception e) {
                            printerr("Error: " + e.getMessage());
                        }
                        break;
                    case "writememory":
                        try {
                            WriteMemory(common.memory, Integer.parseInt(args[0]), Integer.parseInt(args[1]));
                        } catch (Exception e) {
                            printerr("Error: " + e.getMessage());
                        }
                        break;
                    case "readregister":
                        try {
                            print("%d", ReadRegister(args[0]));
                        } catch (Exception e) {
                            printerr("Error: " + e.getMessage());
                        }
                        break;
                    case "writeregister":
                        try {
                            WriteRegister(args[0], Integer.parseInt(args[1]));
                        } catch (Exception e) {
                            printerr("Error: " + e.getMessage());
                        }
                        break;
                    case "exit":
                        System.exit(0);
                        return;
                        case "help":
                        if (di.instructions[0].sop1 != null) {
                            Help.help(di.instructions[0].sop1);
                        } else {
                            Help.help();
                        }
                        break;

                    default:
                        // check if the instruction is valid
                        String upperCommand = command.toUpperCase();
                        if (!Arrays.asList(common.instructions).contains(upperCommand)) {
                            printerr("Error: Unknown instruction: " + command);
                            break;
                        }
                        // execute the instruction
                        try {
                            interp terp = new   interp();
                            int out = terp.ExecuteSingleInstruction(di.instructions[0], di.Memory, di);
                            print("Output: %d\n", out);
                        } catch (Exception e) {
                            printerr("Error: " + e.getMessage());
                        }

                        break;
                }
            }
        }
    }
}