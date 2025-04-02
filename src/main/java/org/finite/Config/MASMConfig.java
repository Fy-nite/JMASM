package org.finite.Config;

import java.io.FileInputStream;
import java.util.Properties;

public class MASMConfig {
    private static final MASMConfig INSTANCE = new MASMConfig();
    private final Properties properties;
    
    private MASMConfig() {
        properties = new Properties();
        loadDefaults();
        loadFromFile();
    }

    private void loadDefaults() {
        properties.setProperty("base.dir", "JMASM");
        properties.setProperty("modules.dir", "${base.dir}/modules");
        properties.setProperty("config.dir", "${base.dir}/config");
        properties.setProperty("logs.dir", "${base.dir}/logs");
        
        properties.setProperty("memory.size", "4096");
    }

    private void loadFromFile() {
        String configPath = getPath("config.dir") + "/masm.properties";
        try (FileInputStream fis = new FileInputStream(configPath)) {
            properties.load(fis);
        } catch (Exception e) {
            // Config file doesn't exist, use defaults
        }
    }

    public static MASMConfig getInstance() {
        return INSTANCE;
    }

    public String getPath(String key) {
        String value = properties.getProperty(key);
        if (value != null) {
            return value.replace("${base.dir}", properties.getProperty("base.dir"));
        }
        return null;
    }

    public int getMemorySize() {
        return Integer.parseInt(properties.getProperty("memory.size", "4096"));
    }
}
