package europa.edit.stepdef;

import europa.edit.pages.*;
import europa.edit.util.BaseDriver;
import europa.edit.util.E2eUtil;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import static europa.edit.util.Common.*;
import static europa.edit.util.E2eUtil.scrollandClick;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class TOCSteps extends BaseDriver {

    @And("{string} link is showing in grey and strikethrough in TOC")
    public void linkShowingGreyAndStrikethrough(String arg0) {
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath(CommonPage.XPATH_TEXT_1 + arg0 + CommonPage.XPATH_TEXT_2 + TOCPage.ANCESTOR_TR_CLASS_LEOS_SOFT_REMOVED));
        assertTrue(bool);
    }

    @When("click on cancel button in navigation pane")
    public void clickOnCancelButtonInNavigationPane() {
        elementClick(driver, AnnexPage.TOC_CANCEL_BUTTON);
    }

    @When("click on save and close button in navigation pane")
    public void clickOnSaveAndCloseButtonInNavigationPane() {
        E2eUtil.wait(1000);
        WebElement ele = waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        assertNotNull(ele, "unable to load the page in the specified time duration");
        elementClick(driver, AnnexPage.TOC_SAVE_AND_CLOSE_BUTTON);
    }

    @Then("elements menu lists are not displayed")
    public void elementsMenuListsAreNotDisplayed() {
        boolean bool = waitUnTillElementIsNotPresent(driver, LegalActPage.NAVIGATION_ELEMENTS_LIST);
        assertTrue(bool, "elements menu lists are displayed");
    }

    @And("click on row number {int} in navigation pane")
    public void clickOnRowNumberInNavigationPane(int arg0) {
        scrollandClick(driver, By.xpath(AnnexPage.TOC_TABLE_TR + "[" + arg0 + "]" + "//div[contains(@class,'gwt-HTML')]"));
    }

    @And("click on actions hamburger icon of a major version {string}")
    public void clickOnActionsHamburgerIconOfAMajorVersion(String arg0) {
        By by = By.xpath(CommonPage.XPATH_CONTAINS_TEXT_1 + arg0 + CommonPage.XPATH_CONTAINS_TEXT_2 + LegalActPage.ANCESTOR_VERSION_CARD + LegalActPage.VERSION_ACTIONS_PNG);
        scrollTo(driver, by);
        elementClickJS(driver, waitForElementTobePresent(driver, by));
    }

    @Then("restore version window is displayed")
    public void restoreVersionWindowIsDisplayed() {
        boolean bool = waitForElementTobeDisPlayed(driver, AnnexPage.RESTORE_VERSION_WINDOW);
        assertTrue(bool, "restore version window is not displayed");
    }

    @When("click on revert button present in restore version window")
    public void clickOnRevertButtonPresentInRestoreVersionWindow() {
        elementClick(driver, AnnexPage.REVERT_BUTTON_IN_RESTORE_VERSION_WINDOW);
    }

    @Then("save this version window is displayed")
    public void saveThisVersionWindowIsDisplayed() {
        boolean bool = waitForElementTobeDisPlayed(driver, AnnexPage.SAVE_THIS_VERSION_WINDOW);
        assertTrue(bool, "save this version window is not displayed");
    }

    @When("provide {string} as title in save this version window")
    public void provideAsTitleInSaveThisVersionWindow(String arg0) {
        elementEcasSendkeys(driver, AnnexPage.TITLE_SAVE_THIS_VERSION_WINDOW, arg0);
    }

    @When("click on actions hamburger icon for a major version whose title is {string}")
    public void clickOnActionsHamburgerIconForAMajorVersionWhoseTitleIs(String arg0) {
        By by = By.xpath(LegalActPage.V_SLOT_TITLE + CommonPage.XPATH_TEXT_1 + arg0 + CommonPage.XPATH_TEXT_2 + LegalActPage.ANCESTOR_VERSION_CARD + LegalActPage.VERSION_ACTIONS_PNG);
        scrollTo(driver, by);
        elementClickJS(driver, waitForElementTobePresent(driver, by));
    }
}