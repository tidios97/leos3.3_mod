package europa.edit.stepdef;

import europa.edit.pages.CommonPage;
import europa.edit.pages.ContributionPanePage;
import europa.edit.util.BaseDriver;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import java.util.ArrayList;
import java.util.List;
import static europa.edit.util.Common.*;
import static org.testng.Assert.*;

public class ContributionPaneSteps extends BaseDriver {
    @Then("contribution from legal service with version {string} is displayed")
    public void contributionFromLegalServiceWithIsDisplayed(String arg0) {
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath(ContributionPanePage.CONTRIBUTION_CARD + ContributionPanePage.VERSION_CARD_READER + CommonPage.XPATH_TEXT_1 + arg0 + CommonPage.XPATH_TEXT_2));
        assertTrue(bool);
    }

    @When("click on hamburger icon of contribution from legal service with {string} in contribution pane")
    public void clickOnHamburgerIconOfContributionFromLegalServiceWithInContributionPane(String arg0) {
        elementClick(driver, By.xpath(CommonPage.XPATH_TEXT_1 + arg0 + CommonPage.XPATH_TEXT_2 + "//ancestor::*[@id='contributionCard']" + ContributionPanePage.CONTRIBUTION_CARD_ACTION));
    }

    @Then("below options are displayed under contribution actions hamburger icon")
    public void belowOptionsAreDisplayedUnderContributionActionsHamburgerIcon(DataTable dataTable) {
        String text;
        List<String> actualOptionList = new ArrayList<>();
        List<String> givenOptionList = dataTable.asList(String.class);
        List<WebElement> elements = driver.findElements(ContributionPanePage.SUBMENU_ITEM_CAPTION);
        assertTrue(null != elements && !elements.isEmpty(), "no element present for action menu bar pop up");
        for (WebElement element : elements) {
            text = element.getText();
            actualOptionList.add(text);
        }
        assertTrue(actualOptionList.containsAll(givenOptionList), "given options are not present in the actual options list");
    }

    @When("click on option {string} in contribution actions hamburger icon")
    public void clickOnOptionInContributionActionsHamburgerIcon(String arg0) {
        elementClick(driver, By.xpath(ContributionPanePage.V_MENUBAR_SUBMENU + ContributionPanePage.ROLE_MENUITEM + CommonPage.XPATH_TEXT_1 + arg0 + CommonPage.XPATH_TEXT_2));
    }

    @Then("contribution view and merge screen is displayed")
    public void contributionViewAndMergeScreenIsDisplayed() {
        boolean bool1 = waitForElementTobeDisPlayed(driver, ContributionPanePage.SECOND_CONTAINER_REVISION_BAR);
        assertTrue(bool1);
        boolean bool2 = waitForElementTobeDisPlayed(driver, ContributionPanePage.SECOND_CONTAINER_REVISION_CONTENT);
        assertTrue(bool2);
    }

    @When("click on close button present in contribution view and merge screen")
    public void clickOnCloseButtonPresentInContributionViewAndMergeScreen() {
        elementClick(driver, ContributionPanePage.CONTRIBUTION_VIEW_MERGE_CLOSE_BUTTON);
    }

    @Then("contribution view and merge screen is not displayed")
    public void contributionViewAndMergeScreenIsNotDisplayed() {
        boolean bool = waitUnTillElementIsNotPresent(driver, ContributionPanePage.SECOND_CONTAINER_REVISION_CONTENT);
        assertTrue(bool);
    }
}
