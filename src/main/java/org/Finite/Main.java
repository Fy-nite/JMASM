package org.Finite;
import org.Finite.common;
import org.Finite.debug.*;
import org.Finite.ArgumentParser;
import com.beust.jcommander.JCommander;
import org.Finite.interp;

public class Main {
    public static void main(String[] args) {
        System.out.print("\033[H\033[2J");
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
            common.box("Error", "No file specified, please use -d for debug to go into a prompt or pass a file with -f", "error");
        }
    }
}