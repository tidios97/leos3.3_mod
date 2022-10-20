package europa.edit.stepdef;

import java.util.List;

import static europa.edit.util.E2eUtil.scrollAndClick;
import static org.testng.Assert.*;

import europa.edit.pages.CommonPage;
import europa.edit.pages.ProposalViewerPage;
import europa.edit.pages.RepositoryBrowserPage;
import europa.edit.util.BaseDriver;
import europa.edit.util.Common;
import europa.edit.util.E2eUtil;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static europa.edit.util.Common.*;
import static europa.edit.util.E2eUtil.scrollandClick;

public class RepositoryBrowserSteps extends BaseDriver {

    @And("^proposal/mandate list is displayed$")
    public void proposalMandateListIsDisplayed() {
        boolean bool = verifyElement(driver, RepositoryBrowserPage.PROPOSAL_MANDATE_LIST_FIRST_TR);
        assertTrue(bool);
    }

    @When("search {string} in Repository Browser Search Bar")
    public void searchProposal(String arg0) {
        elementEcasSendkeys(driver, RepositoryBrowserPage.SEARCHBAR, arg0);
    }

    @And("^delete all the mandate containing keyword$")
    public void deleteAllTheMandateMandateContainingKeyword(DataTable dataTable) {
        List<String> details = dataTable.asList(String.class);
        for (String keyword : details) {
            while (findNumberOfRowsRepoPage(keyword)) {
                elementClick(driver, RepositoryBrowserPage.OPEN_BTN_1ST_PROPOSAL);
                waitForElementTobeDisPlayed(driver, ProposalViewerPage.OVERVIEWTEXT);
                elementClick(driver, ProposalViewerPage.DELETE_BTN);
                boolean bool = verifyElement(driver, ProposalViewerPage.MANDATE_DELETION_CONFIRMATION_POPUP);
                assertTrue(bool);
                elementActionClick(driver, ProposalViewerPage.CONFIRM_POPUP_DELETE_BTN);
                E2eUtil.wait(1000);
                WebElement ele = waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
                assertNotNull(ele, "unable to load the page in the specified time duration");
                boolean bool1 = verifyElement(driver, RepositoryBrowserPage.REPOSITORY_BROWSER_TEXT);
                assertTrue(bool1, "Repository Browser page is not displayed");
                boolean bool2 = waitForElementTobeDisPlayed(driver, By.xpath(CommonPage.XPATH_TEXT_1 + "The proposal has been deleted" + CommonPage.XPATH_TEXT_2));
                assertTrue(bool2);
                elementClick(driver, By.xpath(CommonPage.XPATH_TEXT_1 + "The proposal has been deleted" + CommonPage.XPATH_TEXT_2));
            }
        }
    }

    @When("search keyword {string} in the search bar of repository browser page")
    public void searchKeywordInTheSearchBarOfRepositoryBrowserPage(String arg0) {
        boolean bool = findNumberOfRowsRepoPage(arg0);
        assertTrue(bool, "No results found with keyword : " + arg0);
    }

    @Then("^navigate to Repository Browser page$")
    public void NavigateToRepositoryBrowserPage() {
        WebElement ele = waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        assertNotNull(ele, "unable to load the page in the specified time duration");
        boolean bool = verifyElement(driver, RepositoryBrowserPage.REPOSITORY_BROWSER_TEXT);
        assertTrue(bool);
    }

    @And("^filter section is present$")
    public void VerifyFilterSectionPresent() {
        boolean bool = verifyElement(driver, RepositoryBrowserPage.FILTER_SECTION);
        assertTrue(bool);
    }

    @And("^search bar is present$")
    public void VerifySearchBarPresent() {
        boolean bool = verifyElement(driver, RepositoryBrowserPage.SEARCHBAR);
        assertTrue(bool);
    }

    @And("^upload button is present$")
    public void VerifyUploadBtnPresent() {
        WebElement ele = waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        assertNotNull(ele, "unable to load the page in the specified time duration");
        boolean bool = verifyElement(driver, RepositoryBrowserPage.UPLOAD_BUTTON);
        assertTrue(bool);
    }

    @And("^create proposal button is displayed and enabled$")
    public void IsCreateProposalBtnPresent() {
        boolean bool = verifyElement(driver, RepositoryBrowserPage.CREATE_PROPOSAL_BUTTON);
        assertTrue(bool);
        boolean bool1 = verifyElementIsEnabled(driver, RepositoryBrowserPage.CREATE_PROPOSAL_BUTTON);
        assertTrue(bool1);
    }

    @When("^click on create proposal button$")
    public void clickCreateProposalBtn() {
        elementClick(driver, RepositoryBrowserPage.CREATE_PROPOSAL_BUTTON);
    }

    @When("^untick \"([^\"]*)\" in act category under filter section$")
    public void untickElement(String var1) {
        elementClick(driver, By.xpath(CommonPage.XPATH_TEXT_1 + var1 + CommonPage.XPATH_TEXT_2 + RepositoryBrowserPage.PRECEDING_SIBLING_INPUT));
    }

    @Then("^\"([^\"]*)\" in act category is unticked$")
    public void verifyElementIsUntickedOrNot(String var1) {
        boolean bool = verifyElement(driver, By.xpath(CommonPage.XPATH_TEXT_1 + var1 + CommonPage.XPATH_TEXT_2 + RepositoryBrowserPage.PRECEDING_SIBLING_INPUT + RepositoryBrowserPage.CHECKBOX_NOT_CHECKED));
        assertTrue(bool);
    }

    @When("^click on reset button$")
    public void clickResetBtn() {
        scrollandClick(driver, RepositoryBrowserPage.RESET_BTN);
    }

    @Then("^\"([^\"]*)\" is ticked in act category under filter section$")
    public void getDataTicked(String var1) {
        boolean bool = verifyElement(driver, By.xpath(CommonPage.XPATH_TEXT_1 + var1 + CommonPage.XPATH_TEXT_2 + RepositoryBrowserPage.PRECEDING_SIBLING_INPUT + RepositoryBrowserPage.CHECKBOX_CHECKED));
        assertTrue(bool);
    }

    @And("^created document is showing on the top of the document list$")
    public void createdDocumentIsShowingOnTheTopOfTheDocumentList() {
        verifyStringContainsText(driver, RepositoryBrowserPage.FIRST_PROPOSAL);
    }

    @When("^click on the open button of first proposal/mandate$")
    public void iClickOnOpenButton() {
        WebElement ele = waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        assertNotNull(ele, "unable to load the page in the specified time duration");
        elementActionClick(driver, RepositoryBrowserPage.OPEN_BTN_1ST_PROPOSAL);
    }

    @When("^double click on first proposal$")
    public void doubleClickOnProposal() {
        doubleClick(driver, RepositoryBrowserPage.OPEN_BTN_1ST_PROPOSAL);
    }

    @When("^click on upload button present in the Repository Browser page$")
    public void clickUploadBtnInRepositoryBrowser() {
        elementClick(driver, RepositoryBrowserPage.UPLOAD_BUTTON);
    }

    @And("^upload button is not present in Repository Browser page$")
    public void VerifyUploadBtnPresentIsNotPresent() {
        boolean bool = waitUnTillElementIsNotPresent(driver, RepositoryBrowserPage.UPLOAD_BUTTON);
        assertTrue(bool, "upload button is present in Repository Browser page");
    }

    @And("^delete all the proposal containing keyword$")
    public void deleteAllTheProposalMandateContainingKeyword(DataTable dataTable) {
        List<String> details = dataTable.asList(String.class);
        for (String keyword : details) {
            while (findNumberOfRowsRepoPage(keyword)) {
                List<WebElement> elementList = driver.findElements(By.xpath(RepositoryBrowserPage.CLONED_PROPOSAL));
                if (elementList.size() > 0) {
                    scrollAndClick(driver, elementList.get(0).findElement(RepositoryBrowserPage.ROLE_BUTTON));
                } else {
                    elementClick(driver, RepositoryBrowserPage.OPEN_BTN_1ST_PROPOSAL);
                }
                waitForElementTobeDisPlayed(driver, ProposalViewerPage.PROPOSALVIEWERTEXT);
                elementClick(driver, ProposalViewerPage.DELETE_BTN);
                boolean bool = waitForElementTobeDisPlayed(driver, ProposalViewerPage.PROPOSAL_DELETION_CONFIRMATION_POPUP);
                assertTrue(bool);
                elementClick(driver, ProposalViewerPage.CONFIRM_POPUP_DELETE_BTN);
                E2eUtil.wait(1000);
                WebElement ele = waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
                assertNotNull(ele);
                boolean bool1 = waitForElementTobeDisPlayed(driver, RepositoryBrowserPage.REPOSITORY_BROWSER_TEXT);
                assertTrue(bool1, "unable to load the page in the specified time duration");
                boolean bool2 = waitForElementTobeDisPlayed(driver, By.xpath(CommonPage.XPATH_TEXT_1 + "The proposal has been deleted" + CommonPage.XPATH_TEXT_2));
                assertTrue(bool2);
                elementClick(driver, By.xpath(CommonPage.XPATH_TEXT_1 + "The proposal has been deleted" + CommonPage.XPATH_TEXT_2));
            }
        }
    }

    public boolean findNumberOfRowsRepoPage(String str) {
        elementEcasSendkeys(driver, RepositoryBrowserPage.SEARCHBAR, str);
        E2eUtil.wait(2000);
        WebElement ele = waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        assertNotNull(ele);
        try {
            List<WebElement> elements = driver.findElements(RepositoryBrowserPage.PROPOSAL_MANDATE_LIST_TR);
            return null != elements && !elements.isEmpty();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Then("^each proposal/mandate in the search results contain keyword \"([^\"]*)\"$")
    public void searchResultsContainKeywordForEachProposalMandateS(String arg0) {
        String text;
        WebElement ele = waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        assertNotNull(ele, "Proposal name doesnot contain keyword " + arg0);
        List<WebElement> elements = driver.findElements(RepositoryBrowserPage.PROPOSAL_MANDATE_LIST_TR);
        assertTrue(null != elements && !elements.isEmpty(), "No results found");
        for (int i = 1; i <= elements.size(); i++) {
            text = driver.findElement(By.xpath(RepositoryBrowserPage.PROPOSAL_MANDATE_LIST_TR_STRING + "[" + i + "]" + RepositoryBrowserPage.LEOS_CARD_TITLE)).getText();
            assertTrue(text.contains(arg0), "name of this proposal/mandate doesn't contain string " + arg0);
        }
    }

    @And("first proposal name contains {string}")
    public void firstProposalNameContains(String arg0) {
        E2eUtil.wait(2000);
        WebElement ele = waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        assertNotNull(ele, "Proposal name doesnot contain keyword " + arg0);
        String text = getElementText(driver, By.xpath(RepositoryBrowserPage.PROPOSAL_MANDATE_LIST_TR_STRING + "[1]" + RepositoryBrowserPage.LEOS_CARD_TITLE));
        assertTrue(text.contains(arg0), "first proposal doesn't contain " + arg0);
    }

    @And("colour of first proposal is {string}")
    public void colourOfFirstProposalIsGrey(String arg0) {
        String str = driver.findElement(By.xpath(RepositoryBrowserPage.PROPOSAL_MANDATE_LIST_TR_STRING + "[1]/td" + RepositoryBrowserPage.CLONED_PROPOSAL)).getCssValue("background-color");
        assertEquals(str, arg0, "colour of first proposal is not " + arg0);
    }

    @And("first proposal contains keyword Contribution status: Sent for contribution")
    public void firstProposalContainsKeywordRevisionStatusForRevision() {
        String text1 = Common.waitForElementTobePresent(driver, By.xpath(RepositoryBrowserPage.PROPOSAL_MANDATE_LIST_TR_STRING + "[1]/td//div[contains(@class,'v-slot-leos-caption') and not(contains(@class,'leos-card-language'))]//span[@class='v-captiontext']")).getText();
        assertEquals(text1, "Contribution status:", "first proposal doesn't contains keyword Contribution status:");
        String text2 = Common.waitForElementTobePresent(driver, By.xpath(RepositoryBrowserPage.PROPOSAL_MANDATE_LIST_TR_STRING + "[1]/td//div[contains(@class,'v-slot-leos-caption') and not(contains(@class,'leos-card-language'))]//div[contains(@class,'v-label-undef-w')]")).getText();
        assertEquals(text2, "Sent for contribution", "first proposal doesn't contains keyword Sent for contribution");
    }

    @And("first proposal contains keyword CONTRIBUTION EdiT")
    public void firstProposalContainsKeywordCONTRIBUTIONEdiT() {
        String text;
        boolean bool = true;
        List<WebElement> elementList = driver.findElements(By.xpath(RepositoryBrowserPage.PROPOSAL_MANDATE_LIST_TR_STRING + "[1]" + RepositoryBrowserPage.V_LABEL_CLONED_LABEL));
        assertTrue(elementList.size() > 0, "first proposal has no label CONTRIBUTION EdiT");
        for (WebElement element : elementList) {
            text = element.getText();
            if (!(text.equals("CONTRIBUTION") || text.equals("EdiT"))) {
                bool = false;
            }
        }
        assertTrue(bool, "first proposal has no label CONTRIBUTION or EdiT");
    }

    @When("^click on the open button of proposal/mandate (.*)$")
    public void ClickOnOpenButton(Integer arg0) {
        WebElement ele = waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        assertNotNull(ele, "unable to load the page in the specified time duration");
        elementClick(driver, By.cssSelector("table[role='grid'] tr:nth-child("+ arg0 +") [role='button']"));
    }
}
