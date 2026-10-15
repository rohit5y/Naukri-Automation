package com.rohit.naukri.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;

public class ProfilePage {

    private static final Duration PAGE_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration UPLOAD_TIMEOUT = Duration.ofSeconds(60);
    private static final By FILE_INPUTS = By.cssSelector("input[type='file']");
    private static final By RESUME_UPLOAD_SUCCESS = By.xpath(
            "//*[contains(translate(normalize-space(.),"
                    + " 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),"
                    + " 'resume has been uploaded successfully')"
                    + " or contains(translate(normalize-space(.),"
                    + " 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'resume uploaded')"
                    + " or contains(translate(normalize-space(.),"
                    + " 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'resume updated')"
                    + " or contains(translate(normalize-space(.),"
                    + " 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'successfully uploaded')]");

    private final WebDriver driver;
    private final BiConsumer<WebDriver, String> stepReporter;

    public ProfilePage(WebDriver driver, BiConsumer<WebDriver, String> stepReporter) {
        this.driver = driver;
        this.stepReporter = stepReporter;
    }

    public void uploadResume(String profileUrl, Path resumePath) {
        driver.get(profileUrl);
        WebDriverWait wait = new WebDriverWait(driver, PAGE_TIMEOUT);
        List<WebElement> fileInputs;
        try {
            fileInputs = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(FILE_INPUTS));
        } catch (TimeoutException exception) {
            throw new TimeoutException(
                    "Could not find the resume upload control on the profile page. "
                            + "Check the profile URL and Naukri's current page layout.", exception);
        }

        stepReporter.accept(driver, "Profile page and resume section loaded");
        WebElement fileInput = selectResumeInput(fileInputs);
        fileInput.sendKeys(resumePath.toString());
        stepReporter.accept(driver, "Resume file selected for upload");
        waitForUploadConfirmation();
        stepReporter.accept(driver, "Success message: Resume has been uploaded successfully");
    }

    private WebElement selectResumeInput(List<WebElement> fileInputs) {
        List<WebElement> resumeInputs = fileInputs.stream()
                .filter(input -> {
                    String metadata = (input.getAttribute("accept") + " "
                            + input.getAttribute("name") + " "
                            + input.getAttribute("id") + " "
                            + input.getAttribute("aria-label")).toLowerCase(Locale.ROOT);
                    return metadata.contains("resume") || metadata.contains(".pdf")
                            || metadata.contains(".doc") || metadata.contains(".rtf");
                })
                .toList();

        if (resumeInputs.size() == 1) {
            return resumeInputs.get(0);
        }
        if (resumeInputs.isEmpty() && fileInputs.size() == 1) {
            return fileInputs.get(0);
        }
        throw new IllegalStateException(
                "Could not uniquely identify Naukri's resume file input. Found "
                        + fileInputs.size() + " file inputs and " + resumeInputs.size()
                        + " resume-like inputs; refusing to upload to an ambiguous control.");
    }

    private void waitForUploadConfirmation() {
        try {
            new WebDriverWait(driver, UPLOAD_TIMEOUT)
                    .until(ExpectedConditions.visibilityOfElementLocated(RESUME_UPLOAD_SUCCESS));
        } catch (TimeoutException exception) {
            throw new TimeoutException(
                    "The resume upload was submitted, but no success confirmation appeared "
                            + "within 60 seconds. Verify the profile manually before retrying.", exception);
        }
    }
}
