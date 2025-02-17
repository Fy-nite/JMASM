package org.Finite;
import org.Finite.Common.common;
import org.Finite.Exceptions.MASMException;  // Add this import
import com.beust.jcommander.JCommander;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.Finite.ModuleManager.ModuleInit;
import org.Finite.ModuleManager.examples.MathModule;
import org.Finite.ModuleManager.examples.WindowModule;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static ArgumentParser.Args arguments;  // Add this field

    public static void main(String[] args) {
        try {
            logger.info("Starting MASM interpreter");
            System.out.print("\033[H\033[2J");
            System.out.flush();
            
            ModuleInit.init();
            ModuleInit.registerBuiltInModule(MathModule.class);
            ModuleInit.registerBuiltInModule(WindowModule.class);
            
            arguments = new ArgumentParser.Args();  // Initialize arguments
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
        } catch (MASMException e) {
            common.box("Error", e.getMessage(), "error");
            if (arguments.debug) {
                e.printStackTrace();
            }
            System.exit(1);
        } catch (Exception e) {
            common.box("Error", "Unexpected error: " + e.getMessage(), "error");
            if (arguments.debug) {
                e.printStackTrace();
            }
            System.exit(1);
        }
    }
}