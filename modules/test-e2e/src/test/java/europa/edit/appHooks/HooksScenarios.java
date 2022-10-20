package europa.edit.appHooks;

import europa.edit.util.E2eUtil;
import europa.edit.util.TestParameters;
import europa.edit.util.WebDriverFactory;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.apache.commons.io.FileUtils;
import org.testng.ITestResult;
import org.testng.Reporter;

import java.io.File;

public class HooksScenarios {

    @Before
    public void startScenario(Scenario scenario) {
        TestParameters.getInstance().setScenario(scenario);
        WebDriverFactory.getInstance().setWebDriver();
    }

    @After
    public void closeScenario() {
        if (TestParameters.getInstance().getScenario().isFailed()) {
            E2eUtil.takeSnapShot(WebDriverFactory.getInstance().getWebdriver(), "FAIL");
            Reporter.getCurrentTestResult().setStatus(ITestResult.FAILURE);
            TestParameters.getInstance().getScenario().log("Click below for Screenshot");
            try {
                byte[] bytes = FileUtils.readFileToByteArray(new File(TestParameters.getInstance().getScreenshotPath()));
                TestParameters.getInstance().getScenario().attach(bytes, "image/png", "ErrorScreenshot");
            } catch (Exception e) {
                TestParameters.getInstance().getScenario().log("Exception happen while getting screenshot");
            }
            try {
                if (null != WebDriverFactory.getInstance().getWebdriver()) {
                    WebDriverFactory.getInstance().getWebdriver().quit();
                    TestParameters.getInstance().getScenario().log("Close Browser");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                if (null != WebDriverFactory.getInstance().getWebdriver()) {
                    WebDriverFactory.getInstance().getWebdriver().quit();
                    TestParameters.getInstance().getScenario().log("Close Browser");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        TestParameters.getInstance().reset();
    }
}
