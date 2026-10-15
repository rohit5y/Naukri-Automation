package com.rohit.naukri.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.function.BiConsumer;

public class LoginPage {

    private static final Duration ELEMENT_TIMEOUT = Duration.ofSeconds(20);
    private static final Duration VERIFICATION_TIMEOUT = Duration.ofSeconds(120);
    private static final By EMAIL_INPUT = By.cssSelector(
            "input[type='email'], input[placeholder*='Email'], input[placeholder*='Username']");
    private static final By PASSWORD_INPUT = By.cssSelector("input[type='password']");
    private static final By LOGIN_BUTTON = By.xpath(
            "//button[normalize-space()='Login' or normalize-space()='Log in']"
                    + " | //input[@type='submit' and (@value='Login' or @value='Log in')]");

    private final WebDriver driver;
    private final BiConsumer<WebDriver, String> stepReporter;

    public LoginPage(WebDriver driver, BiConsumer<WebDriver, String> stepReporter) {
        this.driver = driver;
        this.stepReporter = stepReporter;
    }

    public void signIn(String loginUrl, String email, String password) {
        driver.get(loginUrl);
        stepReporter.accept(driver, "Login page loaded");
        WebDriverWait wait = new WebDriverWait(driver, ELEMENT_TIMEOUT);

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_INPUT));
        emailField.clear();
        emailField.sendKeys(email);

        WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(PASSWORD_INPUT));
        passwordField.clear();
        passwordField.sendKeys(password);

        wait.until(ExpectedConditions.elementToBeClickable(LOGIN_BUTTON)).click();
        stepReporter.accept(driver, "Login form submitted");
        waitForSignInCompletion();
        stepReporter.accept(driver, "Login completed");
    }

    private void waitForSignInCompletion() {
        try {
            new WebDriverWait(driver, VERIFICATION_TIMEOUT).until(currentDriver -> {
                String currentUrl = currentDriver.getCurrentUrl().toLowerCase();
                if (!currentUrl.contains("nlogin") && !currentUrl.contains("/login")) {
                    return true;
                }

                String pageText = currentDriver.findElement(By.tagName("body")).getText().toLowerCase();
                return pageText.contains("invalid") || pageText.contains("incorrect")
                        || pageText.contains("unable to login");
            });
        } catch (TimeoutException exception) {
            throw new TimeoutException(
                    "Sign-in did not complete in 120 seconds. Complete any CAPTCHA/OTP manually "
                            + "in the browser and retry, or verify the credentials.", exception);
        }

        String pageText = driver.findElement(By.tagName("body")).getText().toLowerCase();
        if (pageText.contains("invalid") || pageText.contains("incorrect")
                || pageText.contains("unable to login")) {
            throw new IllegalStateException(
                    "Naukri did not accept the sign-in attempt. Check the credentials and try again.");
        }
    }
}
