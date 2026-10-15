package com.rohit.naukri.automation.config;

import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;

class AutomationConfigTest {

    @Test
    void readsNaukriUrlsAndBrowserDefaultsFromProperties() {
        ConfigReader config = ConfigReader.getInstance();

        assertEquals(config.getProperty("naukri.home.url"), "https://www.naukri.com/");
        assertEquals(config.getProperty("naukri.login.url"), "https://www.naukri.com/nlogin/login");
        assertEquals(config.getProperty("naukri.profile.url"), "https://www.naukri.com/mnjuser/profile");
        assertEquals(config.getProperty("browser"), "chrome");
        assertEquals(config.getProperty("browser.headless"), System.getenv().getOrDefault("BROWSER_HEADLESS", ""));
    }

    @Test
    void returnsDefaultForUnknownOptionalProperty() {
        assertEquals(ConfigReader.getInstance().getProperty("not.configured", "fallback"), "fallback");
    }

    @Test
    void rejectsUnknownRequiredProperty() {
        expectThrows(
                IllegalStateException.class,
                () -> ConfigReader.getInstance().getRequiredProperty("not.configured"));
    }

    @Test
    void localPropertiesOverrideClasspathDefaults() throws IOException {
        Path localConfig = Files.createTempFile("naukri-local", ".properties");
        try {
            Files.writeString(localConfig, "naukri.username=local-user\n", StandardCharsets.UTF_8);
            ByteArrayInputStream defaults = new ByteArrayInputStream(
                    "naukri.username=default-user\nnaukri.password=default-password\n"
                            .getBytes(StandardCharsets.UTF_8));

            Properties properties = ConfigReader.loadProperties(defaults, localConfig);

            assertEquals(properties.getProperty("naukri.username"), "local-user");
            assertEquals(properties.getProperty("naukri.password"), "default-password");
        } finally {
            Files.deleteIfExists(localConfig);
        }
    }
}
