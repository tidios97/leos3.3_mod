package europa.edit.stepdef;

import europa.edit.pages.CommonPage;
import europa.edit.pages.CreateMandatePage;
import europa.edit.pages.CreateProposalWindowPage;
import europa.edit.util.*;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static europa.edit.util.Common.*;
import static europa.edit.util.E2eUtil.findLatestFile;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class CreateProposalSteps extends BaseDriver {

    @Then("^Create proposal window is opened$")
    public void verifyCreateProposalWindow() {
        boolean bool = verifyElement(driver, CreateProposalWindowPage.CREATE_BTN);
        assertTrue(bool);
        boolean bool1 = verifyElement(driver, By.xpath(CommonPage.XPATH_TEXT_1 + "Create new legislative document - Template selection (1/2)" + CommonPage.XPATH_TEXT_2));
        assertTrue(bool1);
    }

    @And("^previous button is disabled$")
    public void isPreviousBtnDisabled() {
        isElementDisabled(driver, CreateProposalWindowPage.PREVIOUS_BTN);
    }

    @When("select template {string}")
    public void selectOneTemplate(String arg0) {
        elementClick(driver, By.xpath(CreateProposalWindowPage.INTER_PROCEDURE + CommonPage.XPATH_TEXT_1 + arg0 + CommonPage.XPATH_TEXT_2));
    }

    @Then("^next button is enabled$")
    public void nextBtnIsEnabled() {
        boolean bool = verifyElementIsEnabled(driver, CreateProposalWindowPage.NEXTBTN);
        assertTrue(bool, "next button is not enabled");
    }

    @When("^click on next button$")
    public void clickNextBtn() {
        elementClick(driver, CreateProposalWindowPage.NEXTBTN);
    }

    @Then("^\"([^\\\"]*)\" is displayed$")
    public void showDocumentMetaDataPage(String var1) {
        WebElement ele = waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        assertNotNull(ele, "unable to load the page in the specified time duration");
        boolean bool = verifyElement(driver, By.xpath(CommonPage.XPATH_TEXT_1 + var1 + CommonPage.XPATH_TEXT_2));
        assertTrue(bool, var1 + " is not displayed");
    }

    @And("^previous button is enabled$")
    public void isPreviousBtnEnabled() {
        boolean bool = verifyElementIsEnabled(driver, CreateProposalWindowPage.PREVIOUS_BTN);
        assertTrue(bool, "previous button is not enabled");
    }

    @When("^click on previous button$")
    public void clickPreviousBtn() {
        elementClick(driver, CreateProposalWindowPage.PREVIOUS_BTN);
    }

    @Then("{string} template window is displayed")
    public void showTemplateSelectionPage(String arg0) {
        boolean bool = verifyElement(driver, By.xpath(CommonPage.XPATH_TEXT_1 + arg0 + CommonPage.XPATH_TEXT_2));
        assertTrue(bool, "template window is not displayed");
    }

    @And("^cancel button is displayed and enabled$")
    public void isCancelBtnDisplayedAndEnabled() {
        boolean bool = verifyElement(driver, CreateProposalWindowPage.CANCELBTN);
        assertTrue(bool, "cancel button is not displayed");
        boolean bool1 = verifyElementIsEnabled(driver, CreateProposalWindowPage.CANCELBTN);
        assertTrue(bool1, "cancel button is not enabled$");
    }

    @When("^click on cancel button$")
    public void clickCancelBtn() {
        elementClick(driver, CreateProposalWindowPage.CANCELBTN);
    }

    @When("^provide document title \"([^\"]*)\" in document metadata page$")
    public void iProvideDocumentTitleInDocumentMetadataPage(String var1) {
        elementEcasSendkeys(driver, CreateProposalWindowPage.DOCUMENT_TITLE_INPUT, var1);
    }

    @And("^click on create button$")
    public void clickOnCreateButton() {
        elementClick(driver, CreateProposalWindowPage.CREATE_BTN);
    }

    @Then("upload window 'Upload a leg file 1/2' is showing$")
    public void uploadWindowShowingInRepoBrowser() {
        boolean bool = verifyElement(driver, CreateProposalWindowPage.UPLOAD_WINDOW_FIRST_PAGE);
        assertTrue(bool, "upload window 'Upload a leg file 1/2' is showing");
    }

    @Then("^file name should be displayed in upload window$")
    public void showFileNameInUploadWindow() {
        boolean bool = verifyElement(driver, CreateProposalWindowPage.FILENAME_TXT);
        assertTrue(bool, "file name is not displayed in upload window");
    }

    @And("^valid icon should be displayed in upload window$")
    public void showValidIconInUploadWindow() {
        boolean bool = verifyElement(driver, CreateProposalWindowPage.VALID_ICON);
        assertTrue(bool);
    }

    @Then("upload screen is showing with \"([^\"]*)\" page$")
    public void verifyUploadPage(String var2) {
        boolean bool = verifyElement(driver, By.xpath(CommonPage.XPATH_TEXT_1 + var2 + CommonPage.XPATH_TEXT_2));
        assertTrue(bool);
    }

    @When("^upload a leg file for creating a mandate$")
    public void uploadLegFileCouncil() throws IOException {
        HashMap<String,String> map = findLatestFile("leg", "council" + Constants.SLASH + "createMandate", null);
        String legFileNamePath = map.get(Constants.FILE_FULL_PATH);
        assertNotNull(legFileNamePath, "Unable to upload the file");
        elementEcasSendkeys(driver, CreateMandatePage.UPLOAD_ICON_INPUT, legFileNamePath);
    }

    @When("upload a leg file for creating mandate from location {string}")
    public void uploadALegFileForCreatingMandateFromLocation(String filepath) {
        String fileAbsolutePath = "";
        if (TestParameters.getInstance().getMode().equalsIgnoreCase("remote")) {
            fileAbsolutePath = config.getProperty("path.remote.download") + File.separator + filepath;
        }
        if (TestParameters.getInstance().getMode().equalsIgnoreCase("local")) {
            fileAbsolutePath = System.getProperty("user.dir") + config.getProperty("relative.upload.path.local") + File.separator + filepath;
        }
        assertNotNull(fileAbsolutePath, "Unable find file absolute path");
        elementEcasSendkeys(driver, CreateMandatePage.UPLOAD_ICON_INPUT, fileAbsolutePath);
    }

    @Then("upload screen is showing with Create new mandate - Draft metadata page")
    public void uploadScreenIsShowingWithCreateNewMandateDraftMetadataPage() {
        boolean bool = waitForElementTobeDisPlayed(driver, CreateMandatePage.CREATE_NEW_MANDATE_PAGE_2);
        assertTrue(bool, "Create new mandate - Draft metadata page is not showing");
    }

    @When("upload a leg file for creating proposal from location {string}")
    public void uploadALegFileForCreatingProposalFromLocation(String filepath) {
        String fileAbsolutePath = "";
        if (TestParameters.getInstance().getMode().equalsIgnoreCase("remote")) {
            fileAbsolutePath = config.getProperty("path.remote.download") + File.separator + filepath;
        }
        if (TestParameters.getInstance().getMode().equalsIgnoreCase("local")) {
            fileAbsolutePath = System.getProperty("user.dir") + config.getProperty("relative.upload.path.local") + File.separator + filepath;
        }
        assertNotNull(fileAbsolutePath, "Unable find file absolute path");
        elementEcasSendkeys(driver, CreateProposalWindowPage.UPLOAD_BTN_UPLOAD_WINDOW, fileAbsolutePath);
    }
}
