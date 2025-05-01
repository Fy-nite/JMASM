package org.finite;
import org.finite.Exceptions.MASMException;  // Add this import
import com.beust.jcommander.JCommander;
import org.finite.Modules.extentions.StringOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.finite.ModuleManager.ModuleInit;
import org.finite.ModuleManager.examples.MathModule;
import org.finite.debug;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static ArgumentParser.Args arguments;  // Add this field

    public static void main(String[] args) {
        try {
            arguments = new ArgumentParser.Args();  // Initialize arguments
            JCommander.newBuilder()
                .addObject(arguments)
                .build()
                .parse(args);
            
            arguments.configureLogging();  // Configure logging based on debug flag
            
            if (ArgumentParser.Args.debug) {
                logger.info("Starting MASM interpreter");
            System.out.print("\033[H\033[2J");
            System.out.flush();
            }

            ModuleInit.initallmodules();  // Initialize modules

            if (arguments.help) {
                common.dbgprint("Showing help message");
                common.box("Help", "This is the help message", "info");
            }
//            if (ArgumentParser.Args.debug) {
//                common.box("java MASM interpreter", "2025 (C) finite\nType 'help' for a list of commands\n", "info");
//                common.dbgprint("Starting debug REPL");
//                debug.DebugRepl();
//            }
            if (arguments.file != null && !arguments.file.isEmpty()) {
                logger.info("Running file: {}", arguments.file);
                common.box("java MASM interpreter", "2025 (C) finite\nrunning file: " + arguments.file + "\n", "info");
                interp.runFile(arguments.file);
            }
            if (arguments.info)
            {
                common.box("infomation",common.joined(common.information,"\n"),"info");
                if (common.exitOnHLT)
                {
                    System.exit(6969420);
                }
            }
            else {
            // get the argument regardless, could be a file or a command
                String arg = ArgumentParser.Args.getEffectiveFile();
                if (arg != null) {
                    logger.info("Running file: {}", arg);
                    common.box("java MASM interpreter", "2025 (C) finite\nrunning file: " + arg + "\n", "info");
                    interp.runFile(arg);
                }
                else 
                {
                    common.box("java MASM interpreter", "2025 (C) finite\nType 'help' for a list of commands\n", "info");
                    common.dbgprint("Starting debug REPL");
                    debug.DebugRepl();
                }
             
            }
        } catch (MASMException e) {
            common.box("Error", e.getMessage(), "error");
            if (ArgumentParser.Args.debug) {
                e.printStackTrace();
            }
            if (common.exitOnHLT) {
                System.exit(1);
            }
        } catch (Exception e) {
            common.box("Error", "Unexpected error: " + e.getMessage(), "error");
            if (ArgumentParser.Args.debug) {
                e.printStackTrace();
            }
            if (common.exitOnHLT) {
                System.exit(1);
            }
        }
    }
}