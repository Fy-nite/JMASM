package org.Finite;
import com.beust.jcommander.Parameter;
public class ArgumentParser {
    public static class Args {
        @Parameter(names = {"--help", "-h"}, help = true)
        public boolean help = false;
        @Parameter(names = {"--version", "-v"}, help = true)
        public boolean version = false;
        @Parameter(names = {"--debug", "-d"}, help = true)
        public boolean debug = false;
        // get the current dir the program is running in
        public String currentDir = System.getProperty("user.dir");
        @Parameter(names = {"--file", "-f"}, description = "Path to the MASM file to run")
        public String file = "";
    }
}
