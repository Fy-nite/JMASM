package org.Finite.Common;

import org.tomlj.*;
import java.io.File;
import java.io.IOException;

public class Config {
    private static final String DEFAULT_BASE_DIR = "masm-data";
    private static final String DEFAULT_ROOT_DIR = "MASM";
    private static final String DEFAULT_CONFIG_FILE = "config.toml";
    private static final String DEFAULT_MODULES_DIR = "modules";

    private static Config instance = null;

    private String basedir;
    private String rootdir;
    private String configdir;
    private String configpath;
    private String modules_dir;
    private String modules_path;

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    private Config() {
        try {
            initializeWithDefaults();
            loadConfigFile();
            validateAndCreateDirectories();
        } catch (IOException e) {
            System.err.println("Failed to load config file: " + e.getMessage());
            // Keep using defaults if config file fails to load
            validateAndCreateDirectories();
        }
    }

    private void initializeWithDefaults() {
        basedir = DEFAULT_BASE_DIR;
        rootdir = DEFAULT_ROOT_DIR;
        configdir = DEFAULT_CONFIG_FILE;
        modules_dir = DEFAULT_MODULES_DIR;
        updatePaths();
    }

    private void loadConfigFile() throws IOException {
        TomlTable config = Toml.parse(new File(configpath).getAbsolutePath());
        if (config.contains("basedir")) basedir = config.getString("basedir");
        if (config.contains("rootdir")) rootdir = config.getString("rootdir");
        if (config.contains("configdir")) configdir = config.getString("configdir");
        if (config.contains("modules_dir")) modules_dir = config.getString("modules_dir");
        updatePaths();
    }

    private void updatePaths() {
        String baseRootPath = buildPath(basedir, rootdir);
        configpath = buildPath(baseRootPath, configdir);
        modules_path = buildPath(baseRootPath, modules_dir);
    }

    private String buildPath(String... parts) {
        return String.join(File.separator, parts);
    }

    private void validateAndCreateDirectories() {
        String baseRootPath = buildPath(basedir, rootdir);
        createDirectoryIfNotExists(basedir);
        createDirectoryIfNotExists(baseRootPath);
        createDirectoryIfNotExists(modules_path);
    }

    private void createDirectoryIfNotExists(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                System.err.println("Failed to create directory: " + path);
            }
        }
    }

    // Getters
    public String getBaseDir() { return basedir; }
    public String getRootDir() { return rootdir; }
    public String getConfigDir() { return configdir; }
    public String getConfigPath() { return configpath; }
    public String getModulesDir() { return modules_dir; }
    public String getModulesPath() { return modules_path; }
}
