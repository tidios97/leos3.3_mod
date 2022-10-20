package europa.edit.stepdef;

import europa.edit.pages.AnnexPage;
import europa.edit.pages.CommonPage;
import europa.edit.pages.ProposalViewerPage;
import europa.edit.util.BaseDriver;
import europa.edit.util.E2eUtil;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static europa.edit.util.Common.*;
import static europa.edit.util.E2eUtil.scrollandClick;
import static org.testng.Assert.*;

public class ProposalViewerSteps extends BaseDriver {

    @When("^click on add button in milestones section$")
    public void clickOnAddButtonInMileStonesSection() {
        scrollandClick(driver, ProposalViewerPage.MILESTONE_ADD_ICON);
    }

    @Then("{string} option is selected by default")
    public void optionIsSelectedByDefault(String arg0) {
        String str = getElementText(driver, ProposalViewerPage.MILESTONE_OPTIONS_SELECTED);
        assertEquals(str, arg0, arg0 + " is not selected by default");
    }

    @Then("^milestone title textbox is disabled$")
    public void titleTextboxIsDisabled() {
        boolean bool = verifyElementIsEnabled(driver, ProposalViewerPage.MILESTONE_TITLE_TEXTAREA);
        assertFalse(bool, "Element is enabled but should be disabled");
    }

    @When("^click on milestone dropdown icon$")
    public void clickOnOptionInWindow() {
        elementClick(driver, ProposalViewerPage.MILESTONE_DROPDOWN_ICON);
    }

    @When("^click on milestone option as Other$")
    public void clickOptionOther() {
        elementClick(driver, ProposalViewerPage.MILESTONE_OPTION_OTHER);
    }

    @And("type {string} in title box")
    public void typeInTitleBox(String arg0) {
        boolean bool = waitForElementTobeDisPlayed(driver, ProposalViewerPage.V_REQUIRED_FIELD_INDICATOR);
        assertTrue(bool, "title box is not displayed");
        elementEcasSendkeys(driver, ProposalViewerPage.MILESTONE_TITLE_TEXTAREA, arg0);
    }

    @When("^click on title of the mandate$")
    public void clickOnTitleOfTheMandate() {
        elementClick(driver, ProposalViewerPage.TITLE_ELEMENT);
    }

    @Then("^title save button is displayed and enabled$")
    public void tileSaveButtonIsDisplayedAndEnabled() {
        boolean bool = verifyElement(driver, By.xpath(ProposalViewerPage.TITLE_SAVE_BTN));
        assertTrue(bool);
    }

    @And("^title cancel button is displayed and enabled$")
    public void titleCancelButtonIsDisplayedAndEnabled() {
        boolean bool = verifyElement(driver, By.xpath(ProposalViewerPage.TITLE_CANCEL_BTN));
        assertTrue(bool);
    }

    @When("^append \"([^\"]*)\" keyword in the title of the proposal/mandate$")
    public void addKeywordInTheTitleOfTheMandate(String arg0) {
        elementEcasSendkeys(driver, ProposalViewerPage.TITLE_ELEMENT, getElementAttributeValue(driver, ProposalViewerPage.TITLE_ELEMENT).concat(arg0));
    }

    @And("^click on title save button$")
    public void clickOnSaveButton() {
        elementClick(driver, By.xpath(ProposalViewerPage.TITLE_SAVE_BTN));
    }

    @Then("^title of the proposal/mandate contains \"([^\"]*)\" keyword$")
    public void titleOfTheMandateContainsKeyword(String arg0) {
        WebElement ele = waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        assertNotNull(ele, "unable to load the page in the specified time duration");
        String text = getElementAttributeValue(driver, ProposalViewerPage.TITLE_ELEMENT);
        assertTrue(text.contains(arg0), arg0 + " is not present in the title of the proposal/mandate");
    }

    @Then("these are below options displayed for milestone dropdown")
    public void verifyMileStoneOptions(DataTable mileStoneOptions) {
        String text;
        List<String> details = mileStoneOptions.asList(String.class);
        List<WebElement> elements = driver.findElements(By.xpath(ProposalViewerPage.MILESTONE_DROPDOWN_LIST_TR));
        assertTrue((null != elements && !elements.isEmpty()), "element list is empty");
        for (int i = 1; i <= elements.size(); i++) {
            text = driver.findElement(By.xpath(ProposalViewerPage.MILESTONE_DROPDOWN_LIST_TR + "[" + i + "]" + ProposalViewerPage.MILESTONE_DROPDOWN_LIST_TD_SPAN)).getText();
            assertTrue(details.contains(text), text + " is not present in the data provided");
        }
    }

    @When("click on open button of legal act")
    public void clickOnOpenButtonOfLegalAct() {
        scrollandClick(driver, ProposalViewerPage.LEGAL_ACT_OPEN_BUTTON);
    }

    @And("{string} section is displayed")
    public void sectionIsDisplayed(String arg0) {
        boolean bool = verifyElement(driver, By.xpath(CommonPage.XPATH_TEXT_1 + arg0 + CommonPage.XPATH_TEXT_2));
        assertTrue(bool);
    }

    @And("add new explanatory button is displayed and enabled")
    public void addNewExplantoryButtonIsDisplayedAndEnabled() {
        boolean bool = verifyElement(driver, ProposalViewerPage.ADD_NEW_EXPLANATORY_BUTTON);
        assertTrue(bool);
        boolean bool1 = verifyElementIsEnabled(driver, ProposalViewerPage.ADD_NEW_EXPLANATORY_BUTTON);
        assertTrue(bool1);
    }

    @And("there are below columns displayed under council explanatory section")
    public void thereAreBelowColumnsDisplayedUnderCouncilExplantorySection(DataTable dataTable) {
        String headerText;
        List<String> headerDetails = dataTable.asList(String.class);
        List<WebElement> elements = driver.findElements(By.xpath(ProposalViewerPage.COUNCIL_EXPLANATORY_TABLE_THEAD_TR_TH));
        assertNotNull(elements, "No header found in the Council Explanatory List");
        for (int i = 1; i <= 4; i++) {
            headerText = driver.findElement(By.xpath(ProposalViewerPage.COUNCIL_EXPLANATORY_TABLE_THEAD_TR_TH + "[" + i + "]" + "//div[1]")).getText();
            assertTrue(headerDetails.contains(headerText), headerText + " is not present in the header of Council Explanatory List");
        }
    }

    @When("click on open button of {string} explanatory")
    public void clickOnOpenButtonOfExplanatory(String arg0) {
        scrollandClick(driver, By.xpath("//*[@id='" + arg0 + "']" + ProposalViewerPage.FONTAWESOME));
    }

    @When("click on add new explanatory button")
    public void clickOnAddNewExplantoryButton() {
        elementActionClick(driver, ProposalViewerPage.ADD_NEW_EXPLANATORY_BUTTON);
    }

    @And("delete button is enabled for council explanatory {int}")
    public void deleteButtonIsEnabledForNewCouncilExplanatory(int arg0) {
        List<WebElement> elements = driver.findElements(ProposalViewerPage.COUNCIL_EXPLANATORY_TABLE_TBODY_TR);
        assertNotNull(elements, "No row found in the Council Explanatory List");
        WebElement element = driver.findElement(By.xpath(ProposalViewerPage.COUNCIL_EXPLANATORY_TABLE_TBODY_TR_STRING + "[" + arg0 + "]" + ProposalViewerPage.DELETE_BUTTON_NOT_DISABLED));
        assertFalse(null == element || !element.isEnabled(), "delete button is not enabled for New Council Explanatory");
    }

    @When("click on delete button of council explanatory {int}")
    public void clickOnDeleteButtonOfNewExplanatoryWithTitle(int arg0) {
        scrollandClick(driver, By.xpath(ProposalViewerPage.COUNCIL_EXPLANATORY_TABLE_TBODY_TR_STRING + "[" + arg0 + "]" + ProposalViewerPage.DELETE_BUTTON_NOT_DISABLED));
    }

    @Then("{string} pop up should be displayed with cancel and delete button enabled")
    public void popUpShouldBeDisplayedWithCancelAndDeleteButtonEnabled(String arg0) {
        boolean bool = verifyElement(driver, By.xpath(CommonPage.XPATH_TEXT_1 + arg0 + CommonPage.XPATH_TEXT_2));
        assertTrue(bool);
        boolean bool1 = verifyElementIsEnabled(driver, ProposalViewerPage.COUNCIL_EXPLANATORY_CONFIRM_PAGE_CANCEL_BUTTON);
        assertTrue(bool1);
        boolean bool2 = verifyElementIsEnabled(driver, ProposalViewerPage.COUNCIL_EXPLANATORY_CONFIRM_PAGE_DELETE_BUTTON);
        assertTrue(bool2);
    }

    @And("messages {string} and {string} are displayed in explanatory deletion : confirmation pop up window")
    public void messagesAndAreDisplayedInPopUpWindow(String arg0, String arg1) {
        boolean bool = verifyElement(driver, By.xpath(CommonPage.XPATH_TEXT_1 + arg0 + CommonPage.XPATH_TEXT_2));
        assertTrue(bool);
        boolean bool1 = verifyElement(driver, By.xpath(CommonPage.XPATH_TEXT_1 + arg1 + CommonPage.XPATH_TEXT_2));
        assertTrue(bool1);
    }

    @When("click on delete button in Explanatory deletion : confirmation pop up")
    public void clickOnDeleteButtonInPopUp() {
        elementClick(driver, ProposalViewerPage.COUNCIL_EXPLANATORY_CONFIRM_PAGE_DELETE_BUTTON);
    }

    @When("click on close button present in proposal viewer page")
    public void click_on_close_button_present_in_proposal_viewer_page() {
        elementClick(driver, ProposalViewerPage.CLOSE_BUTTON);
    }

    @And("no milestone exists in milestones section")
    public void noMilestoneExistsInMilestonesSection() {
        boolean bool = waitUnTillElementIsNotPresent(driver, By.xpath(ProposalViewerPage.MILESTONE_TABLE_TBODY_TR));
        assertTrue(bool, "milestone exists in milestones section");
    }

    @And("today's date is showing in date column of milestones table")
    public void todaySDateIsShowingInDateColumnOfMilestonesTable() {
        String pattern = "dd/MM/yyyy";
        String date = getElementText(driver, By.xpath(ProposalViewerPage.MILESTONE_TABLE_TBODY_TR + "[1]/td[2]"));
        assertNotNull(date, "unable to retrieve date in string format");
        String subStringDate = date.substring(0, date.length() - 9);
        String dateInString = new SimpleDateFormat(pattern).format(new Date());
        assertEquals(subStringDate, dateInString, "date mentioned is not today's date");
    }

    @And("{string} is showing in status column of milestones table")
    public void isShowingInStatusColumnOfMilestonesTable(String arg0) {
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath(ProposalViewerPage.MILESTONE_TABLE_TBODY_TR + "[1]/td[3]"));
        assertTrue(bool, "status column of milestones table is not " + arg0 + " with in maximum time provided");
        String status = getElementText(driver, By.xpath(ProposalViewerPage.MILESTONE_TABLE_TBODY_TR + "[1]/td[3]"));
        assertEquals(status, arg0, "status is not equal to " + arg0);
    }

    @When("click on the hamburger menu showing in row {int} of milestones table")
    public void clickOnTheLinkInTitleColumnOfTheFirstMilestone(int arg0) {
        elementClick(driver, By.xpath(ProposalViewerPage.MILESTONE_TABLE_TBODY_TR + "[" + arg0 + "]/td[4]//img[contains(@src,'version_actions.png')]"));
    }

    @Then("Add a milestone window is displayed")
    public void addAMilestoneWindowIsDisplayed() {
        WebElement ele = waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        assertNotNull(ele, "unable to load the page in the specified time duration");
        assertTrue(verifyElement(driver, ProposalViewerPage.ADD_A_MILESTONE_TEXT), "Add a milestone window is not displayed");
    }

    @When("click on add collaborator button")
    public void clickOnAddCollaboratorButton() {
        elementClick(driver, ProposalViewerPage.COLLABORATORS_ADD_BUTTON);
    }

    @Then("^mandate deletion confirmation page should be displayed$")
    public void mandateDeletionConfirmationPageShouldBeDisplayed() {
        boolean bool = verifyElement(driver, ProposalViewerPage.MANDATE_DELETION_CONFIRMATION_POPUP);
        assertTrue(bool);
    }

    @When("click on add a new annex button")
    public void clickOnAddANewAnnexButton() {
        scrollandClick(driver, ProposalViewerPage.ANNEXES_ADD_BUTTON);
    }

    @Then("{string} is added to Annexes")
    public void isAddedToAnnexes(String arg0) {
        WebElement ele = waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        assertNotNull(ele, "unable to load the page in the specified time duration");
        boolean bool = verifyElement(driver, By.xpath(CommonPage.XPATH_TEXT_1 + arg0 + CommonPage.XPATH_TEXT_2));
        assertTrue(bool, arg0 + " is not added to annexes");
    }

    @When("click on open button of Annex {int}")
    public void clickOnOpenButtonOfAnnex(int arg0) {
        scrollandClick(driver, By.xpath(ProposalViewerPage.ANNEX_BLOCK + "[" + arg0 + "]" + ProposalViewerPage.OPEN_TEXT));
    }

    @When("click on delete button of annex {int}")
    public void clickOnDeleteButtonOfAnnex(int arg0) {
        scrollandClick(driver, By.xpath(ProposalViewerPage.ANNEX_BLOCK + "[" + arg0 + "]" + ProposalViewerPage.ICON_ONLY_DELETE_BUTTON));
    }

    @When("click on delete button in annex deletion confirmation page")
    public void clickOnDeleteButtonInAnnexDeletionConfirmationPage() {
        elementClick(driver, AnnexPage.ANNEX_DELETION_BUTTON);
    }

    @Then("{string} is changed to {string}")
    public void isChangedTo(String arg0, String arg1) {
        boolean bool = verifyElement(driver, By.xpath(CommonPage.XPATH_TEXT_1 + arg1 + CommonPage.XPATH_TEXT_2));
        assertTrue(bool, arg1 + " is not displayed");
        boolean bool1 = waitUnTillElementIsNotPresent(driver, By.xpath(CommonPage.XPATH_TEXT_1 + arg0 + CommonPage.XPATH_TEXT_2));
        assertTrue(bool1, arg0 + " is displayed");
    }

    @When("click on title of the Annex {int}")
    public void clickOnTitleOfTheAnnex(int arg0) {
        elementClick(driver, By.xpath(ProposalViewerPage.ANNEX_BLOCK + "[" + arg0 + "]" + ProposalViewerPage.ANNEX_TITLE_INPUT));
    }

    @Then("title save button of Annex {int} is displayed and enabled")
    public void titleSaveButtonOfAnnexIsDisplayedAndEnabled(int arg0) {
        boolean bool = verifyElement(driver, By.xpath(ProposalViewerPage.ANNEX_BLOCK + "[" + arg0 + "]" + ProposalViewerPage.TITLE_SAVE_BTN));
        assertTrue(bool, "title save button of Annex " + arg0 + " is not displayed");
    }

    @And("title cancel button of Annex {int} is displayed and enabled")
    public void titleCancelButtonOfAnnexIsDisplayedAndEnabled(int arg0) {
        boolean bool = verifyElement(driver, By.xpath(ProposalViewerPage.ANNEX_BLOCK + "[" + arg0 + "]" + ProposalViewerPage.TITLE_CANCEL_BTN));
        assertTrue(bool, "title cancel button of Annex " + arg0 + " is not displayed");
    }

    @When("add title {string} to Annex {int}")
    public void addTitleToAnnex(String arg0, int arg1) {
        elementEcasSendkeys(driver, By.xpath(ProposalViewerPage.ANNEX_BLOCK + "[" + arg1 + "]" + ProposalViewerPage.ANNEX_TITLE_INPUT), arg0);
    }

    @And("click on title save button of Annex {int}")
    public void clickOnTitleSaveButtonOfAnnex(int arg0) {
        elementClick(driver, By.xpath(ProposalViewerPage.ANNEX_BLOCK + "[" + arg0 + "]" + ProposalViewerPage.TITLE_SAVE_BTN));
    }

    @Then("title of Annex {int} contains {string}")
    public void titleOfAnnexContains(int arg0, String arg1) {
        String text = getElementAttributeValue(driver, By.xpath(ProposalViewerPage.ANNEX_BLOCK + "[" + arg0 + "]" + ProposalViewerPage.ANNEX_TITLE_INPUT));
        assertTrue(text.contains(arg1), "title of Annex " + arg0 + " doesn't contain " + arg1);
    }

    @Then("numbers of annex present in proposal viewer screen is {int}")
    public void numbersOfAnnexPresentInProposalViewerScreenIs(int arg0) {
        List<WebElement> elementList = driver.findElements(By.xpath(ProposalViewerPage.ANNEX_BLOCK));
        assertEquals(elementList.size(), arg0, "numbers of annex present in proposal viewer screen is not " + arg0);
    }

    @When("Add title {string} to council explanatory {int}")
    public void addTitleToCouncilExplanatory(String arg0, int arg1) {
        setValueToElementAttribute(driver, By.xpath(ProposalViewerPage.COUNCIL_EXPLANATORY_TABLE_TBODY_TR_STRING + "[" + arg1 + "]" + ProposalViewerPage.INPUT), arg0);
    }

    @And("click on save button for council explanatory {int}")
    public void clickOnSaveButtonForCouncilExplanatory(int arg0) {
        elementClick(driver, By.xpath(ProposalViewerPage.COUNCIL_EXPLANATORY_TABLE_TBODY_TR_STRING + "[" + arg0 + "]" + ProposalViewerPage.SAVE_BUTTON));
    }

    @And("title of council explanatory {int} is {string}")
    public void titleOfCouncilExplanatoryIs(int arg0, String arg1) {
        String newTitleText = driver.findElement(By.xpath(ProposalViewerPage.COUNCIL_EXPLANATORY_TABLE_TBODY_TR_STRING + "[" + arg0 + "]" + ProposalViewerPage.INPUT)).getAttribute("value");
        assertEquals(newTitleText, arg1, "Title of new council explanatory is not " + arg1);
    }

    @When("click on title input element of council explanatory {int}")
    public void clickOnTitleInputElementOfCouncilExplanatory(int arg0) {
        elementClick(driver, By.xpath(ProposalViewerPage.COUNCIL_EXPLANATORY_TABLE_TBODY_TR_STRING + "[" + arg0 + "]" + ProposalViewerPage.INPUT));
    }

    @Then("save button is displayed in title input element of council explanatory {int}")
    public void saveButtonIsDisplayedInTitleInputElementOfCouncilExplanatory(int arg0) {
        boolean bool = verifyElement(driver, By.xpath(ProposalViewerPage.COUNCIL_EXPLANATORY_TABLE_TBODY_TR_STRING + "[" + arg0 + "]" + ProposalViewerPage.SAVE_BUTTON));
        assertTrue(bool, "save button is displayed in title input element of council explanatory " + arg0);
    }

    @And("cancel button is displayed in title input element of council explanatory {int}")
    public void cancelButtonIsDisplayedInTitleInputElementOfCouncilExplanatory(int arg0) {
        boolean bool = verifyElement(driver, By.xpath(ProposalViewerPage.COUNCIL_EXPLANATORY_TABLE_TBODY_TR_STRING + "[" + arg0 + "]" + ProposalViewerPage.CANCEL_BUTTON));
        assertTrue(bool, "cancel button is displayed in title input element of council explanatory " + arg0);
    }

    @Then("{string} is showing under title column row {int} in Export to eConsilium section")
    public void isShowingUnderTitleColumnRowInExportToEConsiliumSection(String arg0, int arg1) {
        String text = getElementAttributeValue(driver, By.xpath(ProposalViewerPage.ECONSILIUM_TABLE_TBODY_TR + "[" + arg1 + "]/td[1]//textarea"));
        assertEquals(text, arg0, arg0 + " is showing under title column row " + arg1 + " in Export to eConsilium section");
    }

    @And("today's date is showing under date column row {int} in Export to eConsilium section")
    public void todaySDateIsShowingUnderDateColumnRowInExportToEConsiliumSection(int arg0) {
        String pattern = "dd/MM/yyyy";
        String date = getElementAttributeInnerText(driver, By.xpath(ProposalViewerPage.ECONSILIUM_TABLE_TBODY_TR + "[" + arg0 + "]/td[2]"));
        assertNotNull(date, "unable to retrieve date in string format");
        String subStringDate = date.substring(0, date.length() - 6);
        String dateInString = new SimpleDateFormat(pattern).format(new Date());
        assertEquals(subStringDate, dateInString, "date mentioned is not today's date");
    }

    @And("{string} is showing under status column row {int} in Export to eConsilium section")
    public void isShowingUnderStatusColumnRowInExportToEConsiliumSection(String arg0, int arg1) {
        String status = getElementAttributeInnerText(driver, By.xpath(ProposalViewerPage.ECONSILIUM_TABLE_TBODY_TR + "[" + arg1 + "]/td[3]"));
        assertEquals(status, arg0, "status is not equal to " + arg0);
    }

    @And("^export button is displayed and enabled$")
    public void exportButtonIsDisplayedAndEnabled() {
        boolean bool = verifyElement(driver, ProposalViewerPage.EXPORT_BTN);
        assertTrue(bool);
        boolean bool1 = verifyElementIsEnabled(driver, ProposalViewerPage.EXPORT_BTN);
        assertTrue(bool1);
    }

    @And("^download button is displayed and enabled$")
    public void downloadButtonIsDisplayedAndEnabled() {
        boolean bool = verifyElement(driver, ProposalViewerPage.DOWNLOAD_BTN);
        assertTrue(bool);
        boolean bool1 = verifyElementIsEnabled(driver, ProposalViewerPage.DOWNLOAD_BTN);
        assertTrue(bool1);
    }

    @And("^delete button is displayed and enabled$")
    public void deleteButtonIsDisplayedAndEnabled() {
        boolean bool = verifyElement(driver, ProposalViewerPage.DELETE_BTN);
        assertTrue(bool);
        boolean bool1 = verifyElementIsEnabled(driver, ProposalViewerPage.DELETE_BTN);
        assertTrue(bool1);
    }

    @And("^close button is displayed and enabled$")
    public void closeButtonIsDisplayedAndEnabled() {
        boolean bool = verifyElement(driver, ProposalViewerPage.CLOSE_BTN);
        assertTrue(bool);
        boolean bool1 = verifyElementIsEnabled(driver, ProposalViewerPage.CLOSE_BTN);
        assertTrue(bool1);
    }

    @And("^explanatory memorandum section is present$")
    public void explanatoryMemorandumSectionIsPresent() {
        boolean bool = verifyElement(driver, ProposalViewerPage.EXPLN_MEMORANDUM_TEXT);
        assertTrue(bool);
    }

    @And("^legal act section is present$")
    public void legalActSectionIsPresent() {
        boolean bool = verifyElement(driver, ProposalViewerPage.LEGALACTTEXT);
        assertTrue(bool);
    }

    @And("^annexes section is present$")
    public void annexesSectionIsPresent() {
        boolean bool = verifyElement(driver, ProposalViewerPage.ANNEXESTEXT);
        assertTrue(bool);
    }

    @And("^collaborators section is Present$")
    public void collaboratorsSectionIsPresent() {
        boolean bool = verifyElement(driver, ProposalViewerPage.COLLABORATORSTEXT);
        assertTrue(bool);
    }

    @And("^milestones section is present$")
    public void milestonesSectionIsPresent() {
        boolean bool = verifyElement(driver, ProposalViewerPage.MILESTONESTEXT);
        assertTrue(bool);
    }

    @When("^click on close button$")
    public void clickOnCloseButton() {
        elementClick(driver, ProposalViewerPage.CLOSE_BTN);
    }

    @When("^click on delete button$")
    public void clickOnDeleteButton() {
        elementClick(driver, ProposalViewerPage.DELETE_BTN);
    }

    @Then("^proposal deletion confirmation page should be displayed$")
    public void proposalDeletionConfirmationPageShouldBeDisplayed() {
        boolean bool = verifyElement(driver, ProposalViewerPage.PROPOSAL_DELETION_CONFIRMATION_POPUP);
        assertTrue(bool);
    }

    @And("^cancel button is displayed and enabled in proposal deletion confirmation pop up$")
    public void cancelButtonIsDisplayedAndEnabledInProposalDeletionConfirmationPopUp() {
        boolean bool = verifyElement(driver, ProposalViewerPage.CONFIRM_POPUP_CANCEL_BTN);
        assertTrue(bool);
        boolean bool1 = verifyElementIsEnabled(driver, ProposalViewerPage.CONFIRM_POPUP_CANCEL_BTN);
        assertTrue(bool1);
    }

    @And("^delete button is displayed and enabled in proposal deletion confirmation pop up$")
    public void deleteButtonIsDisplayedAndEnabledInProposalDeletionConfirmationPopUp() {
        boolean bool = verifyElement(driver, ProposalViewerPage.CONFIRM_POPUP_DELETE_BTN);
        assertTrue(bool);
        boolean bool1 = verifyElementIsEnabled(driver, ProposalViewerPage.CONFIRM_POPUP_DELETE_BTN);
        assertTrue(bool1);
    }

    @When("^click on delete button present in confirmation pop up$")
    public void clickOnDeleteButtonPresentInConfirmationPopUp() {
        elementClick(driver, ProposalViewerPage.CONFIRM_POPUP_DELETE_BTN);
    }

    @When("^click on download button$")
    public void clickOnDownloadBtn() {
        elementClick(driver, ProposalViewerPage.DOWNLOAD_BTN);
    }

    @And("close button is not displayed")
    public void closeButtonIsNotDisplayed() {
        boolean bool = waitUnTillElementIsNotPresent(driver, ProposalViewerPage.CLOSE_BTN);
        assertTrue(bool, "close button is displayed");
    }

    @When("click on actions hamburger icon of first milestone")
    public void clickOnActionsHamburgerIconOfFirstMilestone() {
        scrollandClick(driver, ProposalViewerPage.MILESTONE_ACTIONS_MENU_ITEM);
    }

    @Then("below options are displayed under milestone actions hamburger icon")
    public void belowOptionsAreDisplayedUnderMilestoneActionsHamburgerIcon(DataTable dataTable) {
        String text;
        List<String> actualMenuItemList = new ArrayList<>();
        List<String> givenMenuItemList = dataTable.asList(String.class);
        List<WebElement> elements = driver.findElements(By.xpath(ProposalViewerPage.MILESTONE_ACTIONS_MENU_ITEM_CAPTION));
        assertTrue(null != elements && !elements.isEmpty());
        for (WebElement element : elements) {
            text = element.getText();
            actualMenuItemList.add(text);
        }
        assertTrue(actualMenuItemList.containsAll(givenMenuItemList), "given options are not present in the action menu list");
    }

    @When("click on send a copy for contribution option")
    public void clickOnSendACopyForContributionOption() {
        elementClick(driver, ProposalViewerPage.MILESTONE_SEND_COPY_FOR_CONTRIBUTION);
    }

    @And("{string} is mentioned in target user input field")
    public void targetUserFieldIsBlank(String arg0) {
        String str = getElementAttributeValue(driver, ProposalViewerPage.SHARE_MILESTONE_TARGET_USER_INPUT);
        assertEquals(str, arg0, "target user field is not blank");
    }

    @And("send for contribution button is displayed but disabled")
    public void sendForContributionButtonIsDisplayedButDisabled() {
        boolean bool = verifyElement(driver, ProposalViewerPage.SEND_FOR_CONTRIBUTION_BUTTON);
        assertTrue(bool, "send for contribution button is not displayed");
        boolean bool1 = verifyElementIsEnabled(driver, ProposalViewerPage.SEND_FOR_CONTRIBUTION_DISABLED_BUTTON);
        assertTrue(bool1, "send for contribution button is not disabled");
    }

    @And("close button is displayed and enabled in Send a copy of the milestone for contribution window")
    public void closeButtonIsDisplayedAndEnabledInSendCopyMileStoneContributionWindow() {
        boolean bool = verifyElement(driver, ProposalViewerPage.SEND_COPY_MILESTONE_CONTRIBUTION_CANCEL_BUTTON);
        assertTrue(bool, "cancel button is not displayed in Send a copy of the milestone for contribution window");
        boolean bool1 = verifyElementIsEnabled(driver, ProposalViewerPage.SEND_COPY_MILESTONE_CONTRIBUTION_CANCEL_BUTTON);
        assertTrue(bool1, "cancel button is not enabled in Send a copy of the milestone for contribution window");
    }

    @When("search {string} in the target user field")
    public void searchInTheTargetUserField(String arg0) {
        elementEcasSendkeys(driver, ProposalViewerPage.SHARE_MILESTONE_TARGET_USER_INPUT, arg0);
        E2eUtil.wait(3000);
    }

    @Then("{string} user is selected in the target user input field")
    public void userIsSelectedInTheTargetUserInputField(String arg0) {
        String text = getElementAttributeValue(driver, ProposalViewerPage.SHARE_MILESTONE_TARGET_USER_INPUT);
        assertTrue(text.contains(arg0), arg0 + " user is not selected in the target user input field");
    }

    @And("send for contribution button is displayed and enabled")
    public void sendForRevisionButtonIsDisplayedAndEnabled() {
        boolean bool = verifyElement(driver, ProposalViewerPage.SEND_FOR_CONTRIBUTION_BUTTON);
        assertTrue(bool, "send for contribution button is not displayed");
        boolean bool1 = verifyElementIsEnabled(driver, ProposalViewerPage.SEND_FOR_CONTRIBUTION_BUTTON);
        assertTrue(bool1, "send for contribution button is not enabled");
    }

    @When("click on send for contribution button")
    public void clickOnSendForRevisionButton() {
        elementClick(driver, ProposalViewerPage.SEND_FOR_CONTRIBUTION_BUTTON);
    }

    @Then("{string} is showing under title column row {int} of milestones table")
    public void isShowingUnderTitleColumnRowOfMilestonesTable(String arg0, int arg1) {
        WebElement ele = waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        assertNotNull(ele, "unable to load the page in the specified time duration");
        String text = getElementText(driver, By.xpath(ProposalViewerPage.MILESTONE_TABLE_TBODY_TR + "[" + arg1 + "]/td[1]" + ProposalViewerPage.GWT_HTML));
        assertEquals(text, arg0, arg0 + " is showing under title column row " + arg1 + " of milestones table");
    }

    @And("today's date is showing under date column row {int} of milestones table")
    public void todaySDateIsShowingUnderDateColumnRowOfMilestonesTable(int arg0) {
        String pattern = "dd/MM/yyyy";
        String date = getElementText(driver, By.xpath(ProposalViewerPage.MILESTONE_TABLE_TBODY_TR + "[" + arg0 + "]/td[2]"));
        assertNotNull(date, "unable to retrieve date in string format");
        String subStringDate = date.substring(0, date.length() - 9);
        String dateInString = new SimpleDateFormat(pattern).format(new Date());
        assertEquals(subStringDate, dateInString, "date mentioned is not today's date");
    }

    @And("{string} is showing under status column row {int} of milestones table")
    public void isShowingUnderStatusColumnRowOfMilestonesTable(String arg0, int arg1) {
        String str = getElementText(driver, By.xpath(ProposalViewerPage.MILESTONE_TABLE_TBODY_TR + "[" + arg1 + "]/td[3]"));
        assertEquals(str, arg0, "status is not equal to " + arg0);
    }

    @And("proposal title has a label CONTRIBUTION EdiT in proposal viewer page")
    public void proposalHasALabelCONTRIBUTIONEdiTInProposalViewerPage() {
        String text;
        boolean bool = true;
        List<WebElement> elementList = driver.findElements(ProposalViewerPage.CLONED_LABELS);
        assertTrue(elementList.size() > 0, "proposal has no label CONTRIBUTION EdiT in proposal viewer page");
        for (WebElement element : elementList) {
            text = element.getText();
            if (!(text.equals("CONTRIBUTION") || text.equals("EdiT"))) {
                bool = false;
            }
        }
        assertTrue(bool, "proposal has no label CONTRIBUTION or EdiT in proposal viewer page");
    }

    @And("{string} is showing in row {int} of title column of milestones table in commission instance")
    public void isShowingInTitleColumnOfMilestonesTableCommission(String arg0, int arg1) {
        scrollTo(driver, By.xpath(ProposalViewerPage.MILESTONE_TABLE_TBODY_TR + "[" + arg1 + "]//td[1]" + ProposalViewerPage.GWT_HTML));
        String title = getElementText(driver, By.xpath(ProposalViewerPage.MILESTONE_TABLE_TBODY_TR + "[" + arg1 + "]/td[1]" + ProposalViewerPage.GWT_HTML));
        assertEquals(title, arg0, "title is not equal to " + arg0);
    }

    @And("{string} is showing in row {int} of title column of milestones table in council instance")
    public void isShowingInTitleColumnOfMilestonesTableCouncil(String arg0, int arg1) {
        scrollTo(driver, By.xpath(ProposalViewerPage.MILESTONE_TABLE_TBODY_TR + "[" + arg1 + "]//td[1]" + ProposalViewerPage.V_BUTTON_CAPTION));
        String title = getElementText(driver, By.xpath(ProposalViewerPage.MILESTONE_TABLE_TBODY_TR + "[" + arg1 + "]/td[1]" + ProposalViewerPage.V_BUTTON_CAPTION));
        assertEquals(title, arg0, "title is not equal to " + arg0);
    }

    @When("click on create milestone button")
    public void clickOnCreateMilestoneButton() {
        elementClick(driver, ProposalViewerPage.CREATE_MILESTONE_BUTTON);
    }

    @Then("Proposal Viewer screen is displayed")
    public void proposalViewerScreenIsDisplayed() {
        boolean bool = waitForElementTobeDisPlayed(driver, ProposalViewerPage.PROPOSALVIEWERTEXT);
        assertTrue(bool, "Proposal Viewer screen is not displayed");
    }

    @When("click on the link present in the row {int} of title column in milestones table")
    public void clickOnTheLinkPresentInTheRowOfTitleColumnInMilestonesTable(int arg0) {
        scrollandClick(driver, By.xpath(ProposalViewerPage.MILESTONE_TABLE_TBODY_TR + "[" + arg0 + "]/td[1]" + ProposalViewerPage.ROLE_BUTTON));
    }

    @Then("OverView screen is displayed")
    public void overviewScreenIsDisplayed() {
        boolean bool = waitForElementTobeDisPlayed(driver, ProposalViewerPage.OVERVIEWTEXT);
        assertTrue(bool, "OverView screen is not displayed");
    }

    @And("click on milestone option For Interservice Consultation")
    public void clickOnMilestoneOptionForInterserviceConsultation() {
        elementClick(driver, ProposalViewerPage.MILESTONE_OPTION_FOR_INTERSERVICE_CONSULTATION);
    }

    @When("click on view option displayed under milestone actions hamburger icon")
    public void clickOnViewOptionDisplayedUnderMilestoneActionsHamburgerIcon() {
        elementClick(driver, ProposalViewerPage.VIEW_BUTTON_HAMBURGER_MENU);
    }

    @When("click on open button of council explanatory {int}")
    public void clickOnOpenButtonOfCouncilExplanatory(int arg0) {
        elementClick(driver, By.cssSelector(String.format(ProposalViewerPage.COUNCIL_EXPLANATORY_TR_OPEN_BUTTON, arg0)));
    }

    @Then("create contribution milestone window is displayed")
    public void createContributionMilestoneWindowIsDisplayed() {
        boolean bool = waitForElementTobeDisPlayed(driver, ProposalViewerPage.CONFIRMATION_DIALOG_WINDOW_CREAT_CONTRIBUTION_MILESTONE);
        assertTrue(bool);
    }

    @And("{string} message is showing in create contribution milestone window")
    public void messageIsShowingInCreateContributionMilestoneWindow(String arg0) {
        WebElement confirmationDialog = waitForElementTobePresent(driver, ProposalViewerPage.CONFIRMATION_DIALOG_WINDOW);
        boolean bool = confirmationDialog.findElement(By.xpath(CommonPage.XPATH_TEXT_1 + arg0 + CommonPage.XPATH_TEXT_2)).isDisplayed();
        assertTrue(bool);
    }

    @When("click on create milestone button in create contribution milestone window")
    public void clickOnCreateMilestoneButtonInCreateContributionMilestoneWindow() {
        elementClick(driver, ProposalViewerPage.CONFIRMATION_DIALOG_OK_BUTTON);
    }

    @When("click on actions hamburger icon of milestone {int}")
    public void clickOnActionsHamburgerIconOfMilestone(int arg0) {
        WebElement mileStoneTable = waitForElementTobePresent(driver, ProposalViewerPage.MILESTONE_BLOCK_TABLE_BODY);
        WebElement tr_td_element = mileStoneTable.findElement(By.cssSelector("tr:nth-child(" + arg0 + ") td:nth-child(4)"));
        WebElement milestoneMenu = tr_td_element.findElement(ProposalViewerPage.LEOS_ACTIONS_MILESTONE_MENU);
        scrollToElement(driver, milestoneMenu);
        elementClick(milestoneMenu);
    }

    @When("click on option {string} in milestone actions hamburger icon")
    public void clickOnOptionInMilestoneActionsHamburgerIcon(String arg0) {
        elementClick(driver, By.xpath(ProposalViewerPage.MILESTONE_ACTIONS_MENU_ITEM_CAPTION + "[text()='" + arg0 + "']"));
    }

    @And("cover page section is present")
    public void coverPageSectionIsPresent() {
        boolean bool = waitForElementTobeDisPlayed(driver, ProposalViewerPage.COVER_PAGE_BLOCK);
        assertTrue(bool);
    }

    @When("click on open button of cover page")
    public void clickOnOpenButtonOfCoverPage() {
        elementClick(driver, ProposalViewerPage.COVER_PAGE_OPEN_BUTTON);
    }

    @And("council explanatory document stage is {string}")
    public void councilExplanatoryDocumentStageIs(String arg0) {
        String docStage = getElementText(driver, ProposalViewerPage.DOC_STAGE);
        assertEquals(docStage, arg0);
    }

    @And("council explanatory document type is {string}")
    public void councilExplanatoryDocumentTypeIs(String arg0) {
        String DocType = getElementText(driver, ProposalViewerPage.DOC_TYPE);
        assertEquals(DocType, arg0);
    }

    @When("^replace \"([^\"]*)\" keyword in the title of the proposal/mandate$")
    public void replaceKeywordInTheTitleOfTheProposalMandate(String arg0) {
        elementEcasSendkeys(driver, ProposalViewerPage.TITLE_ELEMENT, arg0);
    }

    @Then("{string} option is disabled in milestone actions hamburger icon")
    public void optionIsDisabledInMilestoneActionsHamburgerIcon(String arg0) {
        String value = "false";
        List<WebElement> options = driver.findElements(ProposalViewerPage.LEOS_ACTIONS_SUB_MENU_ITEM);
        for (WebElement option : options) {
            if (getElementAttributeInnerText(option).equals(arg0)) {
                value = getAttributeValueFromElement(option, "aria-disabled");
            }
        }
        assertEquals(value, "true");
    }
}
