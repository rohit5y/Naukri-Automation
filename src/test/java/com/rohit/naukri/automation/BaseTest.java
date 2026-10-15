package com.rohit.naukri.automation;

import com.rohit.naukri.automation.config.AutomationConfig;
import com.rohit.naukri.automation.config.ConfigReader;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

public abstract class BaseTest {

    protected AutomationConfig config;
    protected WebDriver driver;

    @BeforeMethod
    public void startBrowser() {
        ConfigReader configReader = ConfigReader.getInstance();
        if (configReader.getProperty("naukri.username").isBlank()
                || configReader.getProperty("naukri.password").isBlank()
                || configReader.getProperty("resume.path").isBlank()) {
            throw new SkipException(
                    "Set naukri.username and naukri.password in local.properties (or NAUKRI_EMAIL "
                            + "and NAUKRI_PASSWORD), and set RESUME_FILE_PATH before running the live test.");
        }

        config = AutomationConfig.from(configReader);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-notifications");
        options.addArguments("--window-size=1440,1000");
        if (config.headless()) {
            options.addArguments("--headless=new");
        }

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(config.pageLoadTimeoutSeconds()));
    }

    @AfterMethod(alwaysRun = true)
    public void closeBrowser() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    public WebDriver getDriver() {
        return driver;
    }
}
