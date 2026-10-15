package com.rohit.naukri.automation;

import com.rohit.naukri.automation.pages.LoginPage;
import com.rohit.naukri.automation.pages.ProfilePage;
import com.rohit.naukri.automation.reporting.ExtentReportListener;
import org.testng.annotations.Test;

public class NaukriResumeTest extends BaseTest {

    @Test(description = "Signs in to Naukri and uploads the configured resume through the profile page")
    public void updatesResume() {
        new LoginPage(driver, ExtentReportListener::captureStep)
                .signIn(config.loginUrl(), config.email(), config.password());
        new ProfilePage(driver, ExtentReportListener::captureStep)
                .uploadResume(config.profileUrl(), config.resumePath());
    }
}
