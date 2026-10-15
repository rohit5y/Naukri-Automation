package com.rohit.naukri.automation.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.rohit.naukri.automation.BaseTest;
import com.rohit.naukri.automation.config.ConfigReader;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;

public class ExtentReportListener implements ITestListener, ISuiteListener {

    private static final ThreadLocal<ExtentTest> CURRENT_TEST = new ThreadLocal<>();
    private static final DateTimeFormatter RUN_TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS", Locale.ROOT);
    private static ExtentReports extentReports;

    @Override
    public void onStart(ISuite suite) {
        String configuredPath = ConfigReader.getInstance()
                .getProperty("extent.report.path", "target/extent-reports/ExtentReport.html");
        Path configuredReportPath = Path.of(configuredPath).toAbsolutePath().normalize();
        String timestamp = LocalDateTime.now().format(RUN_TIMESTAMP_FORMAT);
        String reportFileName = configuredReportPath.getFileName().toString();
        int extensionIndex = reportFileName.lastIndexOf('.');
        String reportName = extensionIndex < 0
                ? reportFileName + "_" + timestamp
                : reportFileName.substring(0, extensionIndex) + "_" + timestamp
                        + reportFileName.substring(extensionIndex);
        Path reportPath = configuredReportPath.resolveSibling(reportName);
        int collisionSuffix = 2;
        while (Files.exists(reportPath)) {
            String uniqueReportName = extensionIndex < 0
                    ? reportFileName + "_" + timestamp + "_" + collisionSuffix
                    : reportFileName.substring(0, extensionIndex) + "_" + timestamp + "_"
                            + collisionSuffix + reportFileName.substring(extensionIndex);
            reportPath = configuredReportPath.resolveSibling(uniqueReportName);
            collisionSuffix++;
        }
        try {
            Files.createDirectories(reportPath.getParent());
        } catch (IOException exception) {
            throw new IllegalStateException("Could not create ExtentReports output directory.", exception);
        }

        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath.toString());
        sparkReporter.config().setDocumentTitle("Naukri Resume Automation");
        sparkReporter.config().setReportName("Test Execution Report");
        extentReports = new ExtentReports();
        extentReports.attachReporter(sparkReporter);
        extentReports.setSystemInfo("Framework", "Selenium / TestNG");
        extentReports.setSystemInfo("Suite", suite.getName());
    }

    @Override
    public void onTestStart(ITestResult result) {
        ExtentTest test = extentReports.createTest(result.getMethod().getMethodName());
        test.info(result.getMethod().getDescription() == null
                ? "Test started."
                : result.getMethod().getDescription());
        CURRENT_TEST.set(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        currentTest().pass("Test passed.");
        CURRENT_TEST.remove();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = currentTest();
        test.fail(result.getThrowable());
        attachFailureScreenshot(result, test);
        CURRENT_TEST.remove();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = currentTest();
        if (result.getThrowable() == null) {
            test.skip("Test skipped.");
        } else {
            test.skip(result.getThrowable());
        }
        CURRENT_TEST.remove();
    }

    @Override
    public void onFinish(ISuite suite) {
        if (extentReports != null) {
            extentReports.flush();
        }
    }

    private ExtentTest currentTest() {
        ExtentTest test = CURRENT_TEST.get();
        if (test == null) {
            throw new IllegalStateException("No ExtentReports test is active on this thread.");
        }
        return test;
    }

    public static void captureStep(WebDriver driver, String stepDescription) {
        ExtentTest test = CURRENT_TEST.get();
        if (test == null) {
            throw new IllegalStateException(
                    "Cannot capture step screenshot because no ExtentReports test is active.");
        }
        captureScreenshot(driver, test, stepDescription);
    }

    private void attachFailureScreenshot(ITestResult result, ExtentTest test) {
        Object testInstance = result.getInstance();
        if (!(testInstance instanceof BaseTest baseTest)) {
            return;
        }

        WebDriver driver = baseTest.getDriver();
        captureScreenshot(driver, test, "Failure state");
    }

    private static void captureScreenshot(
            WebDriver driver,
            ExtentTest test,
            String stepDescription) {
        if (!(driver instanceof TakesScreenshot screenshotDriver)) {
            test.warning("Screenshot unavailable for step '" + stepDescription
                    + "': WebDriver does not support screenshots.");
            return;
        }

        try {
            byte[] screenshot = screenshotDriver.getScreenshotAs(OutputType.BYTES);
            String base64 = Base64.getEncoder().encodeToString(screenshot);
            test.info(
                    "Step: " + stepDescription,
                    MediaEntityBuilder.createScreenCaptureFromBase64String(base64).build());
        } catch (WebDriverException exception) {
            test.warning("Screenshot capture failed for step '" + stepDescription
                    + "': " + exception.getMessage());
        }
    }
}
