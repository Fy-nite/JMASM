package org.finite;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=")
public class ArgumentParser {
    public static class Args {
        @Parameter(names = {"--help", "-h"}, help = true)
        public boolean help = false;
        @Parameter(names = {"--version", "-v"}, help = true)
        public boolean version = false;
        @Parameter(names = {"--debug", "-d"}, help = true, description = "Enable debug logging")
        public boolean debug = false;
        // get the current dir the program is running in
        public String currentDir = System.getProperty("user.dir");
        
        @Parameter(description = "Main file to process", required = false)
        public java.util.List<String> mainParameter = new java.util.ArrayList<>();

        @Parameter(names = {"--file", "-f"}, description = "Path to the MASM file to run")
        public String file = null;

        // Get the effective file path from either main parameter or --file option
        public String getEffectiveFile() {
            if (file != null && !file.isEmpty()) {
                return file;
            }
            if (!mainParameter.isEmpty()) {
                return mainParameter.get(0);
            }
            return null;
        }

        public void configureLogging() {
            ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) 
                org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
            if (debug) {
                root.setLevel(ch.qos.logback.classic.Level.DEBUG);
            } else {
                root.setLevel(ch.qos.logback.classic.Level.INFO);
            }
        }

        @Parameter(names = {"--compile", "-c"}, description = "Compile the MASM file to bytecode")
        public boolean compile = false;
        @Parameter(names = {"--bytecode", "-b"}, description = "Run bytecode file instead of MASM")
        public boolean bytecode = false;
        @Parameter(names = {"--output-debug", "-o"}, description = "Output preprocessed MASM file")
        public boolean outputDebug = false;
    }
}
