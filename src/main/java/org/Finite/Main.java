package org.Finite;
import org.Finite.common;
import org.Finite.debug.*;
import org.Finite.ArgumentParser;
import com.beust.jcommander.JCommander;
import org.Finite.interp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Starting MASM interpreter");
        System.out.print("\033[H\033[2J");
        // get the main system resource
        System.out.flush();
        
        ArgumentParser.Args arguments = new ArgumentParser.Args();
        JCommander.newBuilder()
        .addObject(arguments)
        .build()
        .parse(args);
        
        if (arguments.help) {
            logger.debug("Showing help message");
            common.box("Help", "This is the help message", "info");
        }
        if (arguments.debug) {
            common.box("java MASM interpreter", "2025 (C) Finite\nType 'help' for a list of commands\n", "info");
            logger.debug("Starting debug REPL");
            debug.DebugRepl();
        }
        if (arguments.file != null && !arguments.file.isEmpty()) {
            logger.info("Running file: {}", arguments.file);
            common.box("java MASM interpreter", "2025 (C) Finite\nrunning file: " + arguments.file + "\n", "info");
            interp.runFile(arguments.file);
        }
        else {
            logger.warn("No input file specified");
            String help = ReadResourceFile.read("help.txt");
            common.box("Error", help, "error");
        }
    }
}