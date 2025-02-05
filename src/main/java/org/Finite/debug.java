package org.Finite;

import java.util.Arrays;
import java.util.Scanner;

import static org.Finite.common.*;

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
                    if (args.length != 2) {
                        // do nothing, we don't need to do anything.
                    } else {

                        di.length = command.length();
                        di.instructions = new interp.instruction[1];
                        di.instructions[0] = new interp.instruction();
                        // assign the instruction to the array
                        di.instructions[0].name = command;
                        di.instructions[0].opcode = 0;
                        di.instructions[0].sop1 = args[0];
                        di.instructions[0].sop2 = args[1];
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
                        return;
                    case "help":
                        if (Arrays.asList(instructions).contains(command)) {
                            print("Instruction: %s\n", command);
                        } else {
                            print("Command: %s\n", command);
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
                            int out = interp.ExecuteSingleInstruction(di.instructions[0], di.Memory);
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