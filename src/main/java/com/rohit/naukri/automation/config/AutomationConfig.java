package com.rohit.naukri.automation.config;

import java.nio.file.Files;
import java.nio.file.Path;

public record AutomationConfig(
        String email,
        String password,
        Path resumePath,
        String loginUrl,
        String profileUrl,
        String browser,
        boolean headless,
        long pageLoadTimeoutSeconds) {

    public static AutomationConfig from(ConfigReader configReader) {
        String email = configReader.getRequiredProperty("naukri.username");
        String password = configReader.getRequiredProperty("naukri.password");
        Path resumePath = Path.of(configReader.getRequiredProperty("resume.path"))
                .toAbsolutePath()
                .normalize();

        if (!Files.isRegularFile(resumePath) || !Files.isReadable(resumePath)) {
            throw new IllegalArgumentException(
                    "The configured resume.path must point to an existing, readable file: " + resumePath);
        }

        String browser = configReader.getRequiredProperty("browser");
        if (!browser.equalsIgnoreCase("chrome")) {
            throw new IllegalArgumentException(
                    "Unsupported browser '" + browser + "'. This project currently supports chrome.");
        }

        return new AutomationConfig(
                email,
                password,
                resumePath,
                configReader.getRequiredProperty("naukri.login.url"),
                configReader.getRequiredProperty("naukri.profile.url"),
                browser,
                Boolean.parseBoolean(configReader.getProperty("browser.headless", "false")),
                Long.parseLong(configReader.getProperty("browser.pageLoadTimeoutSeconds", "45")));
    }
}
