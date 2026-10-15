# Naukri Resume Automation

A Selenium 4 / Java 17 Maven project using Page Object Model (POM) and TestNG. It signs in to Naukri and uploads the resume file you specify.

## Prerequisites

- JDK 17 or later
- Maven 3.8+
- Google Chrome
- A local resume file in PDF or DOC/DOCX format

Selenium Manager, included with Selenium, resolves the matching ChromeDriver. The first run may need internet access to download it.

## Configure settings, credentials, and resume

Application settings are in [`config.properties`](<src/main/resources/config.properties>). Put your Naukri username and password in a root-level `local.properties` file; it is ignored by Git. For example:

```properties
naukri.username=your-login-email
naukri.password=your-password
```

The local file overrides settings in the classpath config. You can also provide `NAUKRI_EMAIL` and `NAUKRI_PASSWORD` environment variables instead. Set the resume path through `RESUME_FILE_PATH`; these environment variables continue to work as an alternative to local credentials. The classpath properties file sets the Naukri home/login/profile URLs, browser, headless mode, page-load timeout, and ExtentReports output path. Update non-secret settings there as needed.

Create `local.properties` in the project root, fill in the credentials, then run the TestNG suite. Or use environment variables in PowerShell (replace the values and resume path with your own):

```powershell
$env:NAUKRI_EMAIL = "your-login-email"
$env:NAUKRI_PASSWORD = "your-password"
$env:RESUME_FILE_PATH = "C:\path\to\your-resume.pdf"
mvn clean test
```

## Test execution and reporting

`NaukriResumeTest` uses the Login and Profile page objects. `BaseTest` directly creates and closes ChromeDriver; there is no WebDriverFactory. If credentials or the required resume path are missing, the live test is skipped while the config-reader tests still run.

The TestNG listener generates a new ExtentReports HTML report for each run under `reports/extent-reports/`, with a date-and-time suffix (for example, `reports/extent-reports/ExtentReport_2026-10-15_00-01-51-324.html`). This location is outside Maven's `target/` directory, so `mvn clean test` will not remove prior reports. If a report filename already exists, a numeric suffix is added instead of overwriting it. Step and failure screenshots are embedded in the HTML report; no separate screenshots folder is created.

The browser is visible by default. The test enters the credentials, submits the sign-in form, then opens the profile page and uploads the configured file. If Naukri asks for a CAPTCHA, OTP, or another verification step, complete it manually in the open browser; the automation waits for the sign-in flow to complete and does not attempt to bypass verification.

The Naukri interface can change. If a required element is no longer recognized, the run exits with an error rather than reporting a false success. A successful run requires the page to display a resume-upload confirmation.

## Project layout

```text
src/
  main/java/com/rohit/naukri/automation/
    config/AutomationConfig.java
    config/ConfigReader.java
    pages/LoginPage.java
    pages/ProfilePage.java
  main/resources/config.properties
  test/java/com/rohit/naukri/automation/
    BaseTest.java
    NaukriResumeTest.java
    config/AutomationConfigTest.java
    reporting/ExtentReportListener.java
  test/resources/testng.xml
```
