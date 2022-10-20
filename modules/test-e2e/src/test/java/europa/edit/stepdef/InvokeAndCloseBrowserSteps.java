package europa.edit.stepdef;

import europa.edit.util.BaseDriver;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvokeAndCloseBrowserSteps extends BaseDriver {
	
	private static final Logger logger = LoggerFactory.getLogger(InvokeAndCloseBrowserSteps.class);

    @When("^navigate to ([^\"]*) edit application$")
    @Given("^navigate to \"([^\"]*)\" edit application$")
    public void invokeApp(String appType) {
        startApp(driver, appType);
    }

    @And("close the browser")
    public void quitTheBrowser() {
        driver.quit();
    }

    public void startApp(WebDriver driver, String applicationType) {
        String applicationURL = getAppUrl(applicationType);
        if (!driver.getCurrentUrl().trim().contains(applicationURL)) {
            driver.get(applicationURL);
        }
    }
    private String getAppUrl(String applicationType) {
        String appUrl = "";
        if (applicationType.equalsIgnoreCase("Commission")) {
            appUrl = config.getProperty("edit.appUrl.ec");
        }
        if (applicationType.equalsIgnoreCase("Council")) {
            appUrl = config.getProperty("edit.appUrl.cn");
        }
        logger.info("Open application {} url {}", applicationType, appUrl);
        return appUrl;
    }
}
