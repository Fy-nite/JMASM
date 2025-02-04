package org.Finite;
import org.Finite.common;
import org.Finite.debug.*;
import org.Finite.ArgumentParser;
import com.beust.jcommander.JCommander;
public class Main {
    public static void main(String[] args) {
        System.out.print("\033[H\033[2J");
        System.out.flush();
        common.print("java MASM interperter: 2025 (C) Finite\n");
        common.print("Type 'help' for a list of commands\n");
        
        ArgumentParser.Args arguments = new ArgumentParser.Args();
        JCommander.newBuilder()
                .addObject(arguments)
                .build()
                .parse(args);

        if (arguments.help) {
            common.box("Help", "This is the help message");
        }
        if (arguments.debug) {
            debug.DebugRepl();
        }
    }
}