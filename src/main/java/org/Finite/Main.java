package org.Finite;
import org.Finite.common;
import org.Finite.debug.*;
import org.Finite.ArgumentParser;
import com.beust.jcommander.JCommander;
import org.Finite.interp;

public class Main {
    public static void main(String[] args) {
        System.out.print("\033[H\033[2J");
        // get the main system resource
        System.out.flush();
        common.box("java MASM interpreter", "2025 (C) Finite\nType 'help' for a list of commands\n", "info");
        
        ArgumentParser.Args arguments = new ArgumentParser.Args();
        JCommander.newBuilder()
                .addObject(arguments)
                .build()
                .parse(args);

        if (arguments.help) {
            common.box("Help", "This is the help message", "info");
        }
        if (arguments.debug) {
            debug.DebugRepl();
        }
        if (arguments.file != null && !arguments.file.isEmpty()) {
            interp.runFile(arguments.file);
        }
        else 
        {
            String help = ReadResourceFile.read("help.txt");
            common.box("Error",help, "error");
        }
    }
}