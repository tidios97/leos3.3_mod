package europa.edit.stepdef;

import europa.edit.pages.CommonPage;
import europa.edit.pages.CoverPage;
import europa.edit.pages.TOCPage;
import europa.edit.util.BaseDriver;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import static europa.edit.util.Common.*;
import static org.testng.Assert.*;

public class CoverPageSteps extends BaseDriver {

    @Then("cover page is displayed")
    public void coverPageIsDisplayed() {
        boolean bool = waitForElementTobeDisPlayed(driver, CoverPage.COVER_PAGE_LEOS_HEADER);
        assertTrue(bool);
    }

    @Then("cover page long title is {string}")
    public void coverPageLongTitleIsDisplayed(String title) {
        String longTitle = getElementText(driver, CoverPage.COVER_PAGE_LONG_TITLE);
        assertEquals(longTitle, title);
    }

    @When("mouse hover and click on edit icon in long title of cover page")
    public void mouseHoverAndClickOnEditIconInLongTitleOfCoverPage() {
        waitForOneSecond();
        Actions actions = new Actions(driver);
        WebElement longTitle = waitForElementTobePresent(driver, CoverPage.COVER_PAGE_LONG_TITLE);
        actions.moveToElement(longTitle).build().perform();
        waitForOneSecond();
        WebElement editBtnBeforeMO = driver.findElement(CoverPage.COVER_PAGE_LONG_TITLE_EDIT_BUTTON);
        actions.moveToElement(editBtnBeforeMO).build().perform();
        waitForOneSecond();
        WebElement editButton = driver.findElement(CoverPage.COVER_PAGE_LONG_TITLE_EDIT_BUTTON);
        actions.moveToElement(editButton).build().perform();
        actions.click().build().perform();
        waitForOneSecond();
    }

    @When("click on close button present in cover page")
    public void clickOnCloseButtonPresentInCoverPage() {
        WebElement ele = waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        assertNotNull(ele, "unable to load the page in the specified time duration");
        elementClick(driver, CommonPage.CLOSE_BUTTON);
    }

    @And("only {string} element is present in TOC")
    public void onlyTitleElementIsPresentInTOC(String arg0) {
        int size = driver.findElements(TOCPage.TABLE_TREE_GRID_TBODY_TR).size();
        assertEquals(size, 1);
        String title = waitForElementTobePresent(driver, TOCPage.TABLE_TREE_GRID_TBODY_TR).findElement(By.cssSelector("td .gwt-HTML")).getText();
        assertEquals(title, arg0);
    }

    @When("double click on long title of doc purpose")
    public void doubleClickOnLongTitleOfDocPurpose() {
        waitForTwoSecond();
        doubleClick(driver, CoverPage.COVER_PAGE_MAIN_LONG_TITLE_DOCPURPOSE);
    }

    @When("select {string} from cover page container main doc location")
    public void selectFromCoverPageContainerMainDocLocation(String arg0) {
        waitForOneSecond();
        selectTextFromElement(driver, CoverPage.COVER_PAGE_MAIN_DOC_LOCATION, arg0);
    }

    @When("select {string} from cover page long title doc stage")
    public void selectFromCoverPageLongTitleDocStage(String arg0) {
        waitForOneSecond();
        selectTextFromElement(driver, CoverPage.COVER_PAGE_MAIN_LONG_TITLE_DOCSTAGE, arg0);
    }

    @When("select {string} from cover page long title doc type")
    public void selectFromCoverPageLongTitleDocType(String arg0) {
        waitForOneSecond();
        selectTextFromElement(driver, CoverPage.COVER_PAGE_MAIN_LONG_TITLE_DOCTYPE, arg0);
    }

    @When("select {string} from cover page long title doc purpose")
    public void selectFromCoverPageLongTitleDocPurpose(String arg0) {
        waitForOneSecond();
        selectTextFromElement(driver, CoverPage.COVER_PAGE_MAIN_LONG_TITLE_DOCPURPOSE, arg0);
    }

    @And("click on selected suggested portion in cover page long title doc purpose")
    public void clickOnSelectedSuggestedPortionInCoverPageLongTitleDocPurpose() {
        elementClick(driver, CoverPage.COVER_PAGE_MAIN_LONG_TITLE_DOCPURPOSE_HIGHLIGHT_ANNOTATOR_HL);
    }
}