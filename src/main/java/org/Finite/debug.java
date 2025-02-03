package org.Finite;

import java.util.Arrays;
import java.util.Scanner;
import static org.Finite.common.*;
public class debug {
    public static void DebugRepl() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            print("> ");
            String input = scanner.nextLine();
            String[] tokens = input.split(" ");
            String command = tokens[0];
            String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);
            switch (command) {
                case "dumpmemory":
                    dumpMemory();
                    break;
                case "dumpregisters":
                    dumpRegisters();
                    break;
                case "readmemory":
                    print("%d", ReadMemory(Integer.parseInt(args[0])));
                    break;
                case "writememory":
                    WriteMemory(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
                    break;
                case "readregister":
                    print("%d", ReadRegister(String.valueOf(Integer.parseInt(args[0]))));
                    break;
                case "writeregister":
                    WriteRegister(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
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
                    // it's a command or instruction
                    for (String s : instructions) {
                        if (s.equals(command)) {
                            print("Instruction: %s\n", s);
                            break;
                        }
                    }
                    break;
            }
        }
    }
}
