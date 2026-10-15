package com.rohit.naukri.automation.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ConfigReader {

    private static final String CONFIG_FILE = "/config.properties";
    private static final Path LOCAL_CONFIG_FILE = Path.of("local.properties");
    private static final Pattern ENVIRONMENT_VARIABLE = Pattern.compile("\\$\\{([A-Za-z_][A-Za-z0-9_]*)}");
    private static final ConfigReader INSTANCE = load();

    private final Properties properties;

    private ConfigReader(Properties properties) {
        this.properties = properties;
    }

    public static ConfigReader getInstance() {
        return INSTANCE;
    }

    public String getProperty(String key) {
        return getProperty(key, "");
    }

    public String getProperty(String key, String defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return resolveEnvironmentVariables(value);
    }

    public String getRequiredProperty(String key) {
        String value = getProperty(key);
        if (value.isBlank()) {
            throw new IllegalStateException(
                    "Required property '" + key + "' is missing or its environment variable is not set "
                            + "in " + CONFIG_FILE + " or " + LOCAL_CONFIG_FILE + ".");
        }
        return value;
    }

    private static ConfigReader load() {
        Properties properties;
        try (InputStream defaults = ConfigReader.class.getResourceAsStream(CONFIG_FILE)) {
            if (defaults == null) {
                throw new IllegalStateException("Could not find " + CONFIG_FILE + " on the classpath.");
            }
            properties = loadProperties(defaults, LOCAL_CONFIG_FILE);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not read " + CONFIG_FILE + " or " + LOCAL_CONFIG_FILE + ".", exception);
        }
        return new ConfigReader(properties);
    }

    static Properties loadProperties(InputStream defaults, Path localConfigFile) throws IOException {
        Properties properties = new Properties();
        properties.load(defaults);
        if (Files.isRegularFile(localConfigFile)) {
            try (InputStream localConfig = Files.newInputStream(localConfigFile)) {
                properties.load(localConfig);
            }
        }
        return properties;
    }

    private static String resolveEnvironmentVariables(String value) {
        Matcher matcher = ENVIRONMENT_VARIABLE.matcher(value);
        StringBuilder resolved = new StringBuilder();
        while (matcher.find()) {
            String environmentValue = System.getenv(matcher.group(1));
            matcher.appendReplacement(
                    resolved,
                    Matcher.quoteReplacement(environmentValue == null ? "" : environmentValue));
        }
        matcher.appendTail(resolved);
        return resolved.toString();
    }
}
