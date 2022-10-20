package europa.edit.stepdef;

import europa.edit.pages.AnnotationPage;
import europa.edit.pages.MileStoneExplorerPage;
import europa.edit.util.BaseDriver;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import java.util.List;
import static europa.edit.util.Common.*;
import static org.testng.Assert.*;

public class MileStoneExplorerPageSteps extends BaseDriver {
    @Then("milestone explorer page is displayed")
    public void milestoneExplorerPageIsDisplayed() {
        boolean bool = verifyElement(driver, MileStoneExplorerPage.MILESTONE_EXPLORER_TEXT);
        assertTrue(bool);
    }

    @And("explanatory memorandum section is displayed")
    public void explanatoryMemorandumSectionIsDisplayed() {
        boolean bool = waitForElementTobeDisPlayed(driver, MileStoneExplorerPage.EXP_MEMO_TEXT);
        assertTrue(bool, "explanatory memorandum section is not displayed");
    }

    @And("legal act section is displayed in milestone explorer page")
    public void legalActSectionIsDisplayed() {
        boolean bool = waitForElementTobeDisPlayed(driver, MileStoneExplorerPage.LEGAL_ACT_TEXT);
        assertTrue(bool, "legal act section is not displayed");
    }

    @And("click on close button present in milestone explorer page")
    public void clickOnCloseButtonPresentInMilestoneExplorerPage() {
        elementClick(driver, MileStoneExplorerPage.CLOSE_BUTTON);
    }

    @When("click on legal act section in milestone explorer page")
    public void clickOnLegalActSectionInMilestoneExplorerPage() {
        elementClick(driver, MileStoneExplorerPage.LEGAL_ACT_TEXT);
    }

    @And("annotations section is opened in milestone explorer page")
    public void annotationsWindowIsOpened() {
        WebElement ele = waitForElementTobePresent(driver, MileStoneExplorerPage.ANNOTATION_SIDE_BAR_RIGHT_SIDE);
        assertNotNull(ele, "annotations section is not opened in milestone explorer page");
    }

    @Then("there are {int} annotations present in annotations window in milestone explorer page")
    public void thereAreAnnotationsPresentInAnnotationsWindowInMilestoneExplorerPage(int arg0) {
        String count = getElementAttributeInnerText(driver, AnnotationPage.ANNOTATION_COUNT).trim();
        assertEquals(Integer.parseInt(count), arg0, arg0 + " annotations are not present in annotations window in milestone explorer page");
    }

    @Then("there are {int} annotations present in orphans window in milestone explorer page")
    public void thereAreAnnotationsPresentInOrphansWindowInMilestoneExplorerPage(int arg0) {
        String count = getElementAttributeInnerText(driver, AnnotationPage.ORPHANS_COUNT).trim();
        assertEquals(Integer.parseInt(count), arg0, arg0 + " annotations are not present in orphans window in milestone explorer page");
    }

    @Then("citations section is displayed in milestone explorer page")
    public void citationsSectionIsDisplayed() {
        boolean bool = waitForElementTobeDisPlayed(driver, MileStoneExplorerPage.CITATIONS_TEXT);
        assertTrue(bool, "citations section is not displayed in milestone explorer page");
    }

    @Then("recitals section is displayed in milestone explorer page")
    public void recitalsSectionIsDisplayed() {
        boolean bool = waitForElementTobeDisPlayed(driver, MileStoneExplorerPage.RECITALS_TEXT);
        assertTrue(bool, "recitals section is not displayed in milestone explorer page");
    }

    @When("click on Orphans link present in annotations window in milestone explorer page")
    public void clickOnOrphansLinkPresentInAnnotationsWindowInMilestoneExplorerPage() {
        elementActionClick(driver, MileStoneExplorerPage.ORPHANS_LINK_IN_ANNOTATION_WINDOW);
    }

    @And("annex section is displayed in milestone explorer page")
    public void annexSectionIsDisplayedInMilestoneExplorerPage() {
        boolean bool = waitForElementTobeDisPlayed(driver, MileStoneExplorerPage.TABITEM_ANNEX_TEXT);
        assertTrue(bool, "annex section is not displayed in milestone explorer page");
    }

    @When("click on annex section in milestone explorer page")
    public void clickOnAnnexSectionInMilestoneExplorerPage() {
        elementClick(driver, MileStoneExplorerPage.TABITEM_ANNEX_TEXT);
    }

    @Then("annex page is displayed in milestone explorer page")
    public void annexPageIsDisplayedInMilestoneExplorerPage() {
        boolean bool = waitForElementTobeDisPlayed(driver, MileStoneExplorerPage.PREFACE_CONTAINER_BLOCK_NUM);
        assertTrue(bool, "long title doesn't exist");
        String str = getElementAttributeInnerText(driver, MileStoneExplorerPage.PREFACE_CONTAINER_BLOCK_NUM);
        assertEquals(str, "Annex", "annex page is not displayed in milestone explorer page");
    }

    @And("annex {int} section is displayed in milestone explorer page")
    public void annexSectionIsDisplayedInMilestoneExplorerPage(int arg0) {
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath(MileStoneExplorerPage.V_TABSHEET_TABITEM + "//*[contains(text(),'Annex " + arg0 + "')]"));
        assertTrue(bool);
    }

    @When("click on export button present in milestone explorer page")
    public void clickOnExportButtonPresentInMilestoneExplorerPage() {
        elementClick(driver, MileStoneExplorerPage.EXPORT_BUTTON);
    }

    @And("cover page section is displayed in milestone explorer page")
    public void coverPageSectionIsDisplayedInMilestoneExplorerPage() {
        boolean bool = waitForElementTobeDisPlayed(driver, MileStoneExplorerPage.COVER_PAGE_TAB);
        assertTrue(bool, "legal act section is not displayed");
    }

    @And("{string} is the tab {int} in milestone explorer window")
    public void isTheTabInMilestoneExplorerWindow(String arg0, int arg1) {
        List<WebElement> tabList = driver.findElements(MileStoneExplorerPage.ROLE_TAB_LIST_TABLE_TR_TD);
        WebElement tabElement = tabList.get(arg1-1).findElement(MileStoneExplorerPage.V_CAPTION_TEXT);
        String tabName = getElementAttributeInnerText(tabElement);
        assertEquals(tabName, arg0);
    }
}
