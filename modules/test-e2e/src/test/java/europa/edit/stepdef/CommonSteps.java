package europa.edit.stepdef;

import europa.edit.pages.*;
import europa.edit.util.*;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import static europa.edit.util.Common.*;
import static europa.edit.util.E2eUtil.*;
import static org.testng.Assert.*;


public class CommonSteps extends BaseDriver {
    private static final Logger logger = LoggerFactory.getLogger(CommonSteps.class);

    @When("^enter username \"([^\"]*)\" and password \"([^\"]*)\"$")
    public void enterCredentials(String name, String pwd) {
        String userName = config.getProperty(name);
        String userPwd = config.getProperty(pwd);
        if (!TestParameters.getInstance().getEnvironment().equals("local")) {
            if (elementExistsWithOutwait(driver, SignInPage.USER_NAME)) {
                elementEcasSendkeys(driver, SignInPage.USER_NAME, userName);
                elementClick(driver, SignInPage.NEXT_BUTTON);
                elementEcasSendkeys(driver, PasswordPage.PASSWORD, td.decrypt(userPwd));
                elementClick(driver, PasswordPage.SIGN_IN_BUTTON);
            }
        }
    }

    @And("^user name is present in the Top right upper corner$")
    public void VerifyUserNamePresent() {
        boolean bool = verifyElement(driver, RepositoryBrowserPage.USERNAME_ICON);
        assertTrue(bool);
    }

    @When("^click on home button$")
    public void clickOnHomeButton() {
        elementClick(driver, ProposalViewerPage.HOME_BTN);
    }

    @When("^click on minimize application header button$")
    public void iClickOnMinimizeApplicationHeaderButton() {
        elementClick(driver, CommonPage.MIN_MAX_APP_HEADER_ICON);
    }

    @Then("^application header is minimized$")
    public void applicationHeaderIsMinimized() {
        boolean bool = verifyElement(driver, CommonPage.SUBTITLE_HEADER_ELEMENT);
        assertTrue(bool);
    }

    @When("^click on maximize application header button$")
    public void iClickOnMaximizeApplicationHeaderButton() {
        elementClick(driver, CommonPage.MIN_MAX_APP_HEADER_ICON);
    }

    @Then("^application header is maximized$")
    public void applicationHeaderIsMaximized() {
        boolean bool = verifyElement(driver, CommonPage.TITLE_HEADER_ELEMENT);
        assertTrue(bool);
    }

    @And("^sleep for (.*) milliseconds")
    public void takeTimerSleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @When("find the recent {string} file in download path and unzip it in {string} and get the latest {string} file")
    public void findAndUnzipFile(String fileType, String relativePath, String searchFileType) throws IOException {
        boolean bool = waitForElementTobeDisPlayed(driver, ProposalViewerPage.PROPOSAL_DOWNLOAD_MESSAGE);
        assertTrue(bool, "Proposal download message is not showing");
        scrollandClick(driver, ProposalViewerPage.PROPOSAL_DOWNLOAD_MESSAGE);
        boolean bool1 = waitUnTillElementIsNotPresent(driver, ProposalViewerPage.PROPOSAL_DOWNLOAD_MESSAGE);
        assertTrue(bool1, "Proposal download message is still showing");
        E2eUtil.findAndUnzipFile(fileType, relativePath, searchFileType);
    }

    @Then("print the latest {string} file name in relative location {string}")
    public void printFileName(String arg0, String arg1) throws IOException {
        HashMap<String, String> map = findLatestFile(arg0, arg1, null);
        assertNotNull(map.get(Constants.FILE_FULL_PATH), "Unable to find the file");
    }

    @And("^\"([^\"]*)\" button is present$")
    public void VerifySpecificButton(String btn) {
        boolean bool = verifyElement(driver, By.xpath(CommonPage.XPATH_TEXT_1 + btn + CommonPage.XPATH_TEXT_2));
        assertTrue(bool);
    }

    @When("^click on \"([^\"]*)\" button$")
    public void clickOnSpecificButton(String var1) {
        scrollandClick(driver, By.xpath(CommonPage.XPATH_TEXT_1 + var1 + CommonPage.XPATH_TEXT_2));
    }

    @Then("{string} window is displayed")
    public void windowIsDisplayed(String arg0) {
        WebElement ele = waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        assertNotNull(ele, "unable to load the page in the specified time duration");
        boolean bool = verifyElement(driver, By.xpath(CommonPage.XPATH_TEXT_1 + arg0 + CommonPage.XPATH_TEXT_2));
        assertTrue(bool, "message " + arg0 + " is not displayed");
    }

    @When("upload a latest {string} file for creating proposal from location {string}")
    public void uploadALatestFileForCreatingProposalFromLocation(String arg0, String arg1) throws IOException {
        HashMap<String, String> map = findLatestFile(arg0, arg1, null);
        String FileNamePath = map.get(Constants.FILE_FULL_PATH);
        assertNotNull(FileNamePath, "Unable to find the file");
        elementEcasSendkeys(driver, CreateProposalWindowPage.UPLOAD_BTN_UPLOAD_WINDOW, FileNamePath);
    }

    @And("click on logout button")
    public void logoutFromBrowser() {
        elementClick(driver, CommonPage.LOGOUT_BUTTON);
    }

    @When("redirect the browser to ECAS url")
    public void redirectToECASUrl() {
        if (!TestParameters.getInstance().getEnvironment().equals("local")) {
            String str = config.getProperty("ecas.appUrl");
            driver.get(str);
        }
    }

    @Then("ECAS successful login page is displayed")
    public void pageIsDisplayed() {
        if (!TestParameters.getInstance().getEnvironment().equals("local")) {
            boolean bool = verifyElement(driver, SignInPage.ECAS_SUCCESSFUL_LOGIN_TEXT);
            assertTrue(bool, "ECAS successful login page is not displayed");
        }
    }

    @When("click on logout button in ECAS logged in page")
    public void clickOnLogoutButtonInECASLoggedInPage() {
        if (!TestParameters.getInstance().getEnvironment().equals("local")) {
            elementClick(driver, SignInPage.ECAS_LOG_OUT_BUTTON);
        }
    }

    @Then("user is logged out from ECAS")
    public void userIsLoggedOutFromECAS() {
        if (!TestParameters.getInstance().getEnvironment().equals("local")) {
            boolean bool = verifyElement(driver, SignInPage.ECAS_LOGGED_OUT_MESSAGE);
            assertTrue(bool, "user is not logged out from ECAS");
        }
    }

    @Then("sign in with a different e-mail address page is displayed")
    public void signInWithADifferentEMailAddressPageIsDisplayed() {
        if (!TestParameters.getInstance().getEnvironment().equals("local")) {
            boolean bool = verifyElement(driver, SignInPage.ECAS_SIGN_IN_WITH_DIFFERENT_USER);
            assertTrue(bool, "sign in with a different e-mail address page is not displayed");
        }
    }

    @When("click on sign in with a different e-mail address hyperlink")
    public void clickOnSignInWithADifferentEMailAddressHyperlink() {
        if (!TestParameters.getInstance().getEnvironment().equals("local")) {
            elementClick(driver, SignInPage.ECAS_SIGN_IN_WITH_DIFFERENT_USER);
        }
    }

    @Then("sign in to continue page is displayed")
    public void signInToContinuePageIsDisplayed() {
        if (!TestParameters.getInstance().getEnvironment().equals("local")) {
            boolean bool = verifyElement(driver, SignInPage.ECAS_SIGN_IN_USER_TEXT);
            assertTrue(bool, "sign in to continue page is not displayed");
        }
    }

    @When("double click on minimize maximize button present in the right upper corner of the application")
    public void doubleClickOnMinimizeMaximizeButtonPresentInTheRightUpperCornerOfTheApplication() {
        elementClick(driver, CommonPage.MIN_MAX_APP_HEADER_ICON);
        E2eUtil.wait(1000);
        elementClick(driver, CommonPage.MIN_MAX_APP_HEADER_ICON);
    }

    @And("click on message {string}")
    public void clickOnMessage(String arg0) {
        scrollandClick(driver, By.xpath(CommonPage.XPATH_TEXT_1 + arg0 + CommonPage.XPATH_TEXT_2));
        E2eUtil.wait(2000);
    }

    @And("switch from main window to iframe {string}")
    public void switchFromMainWindowToIframe(String arg0) {
        driver.switchTo().frame(arg0);
    }

    @And("switch from iframe to main window")
    public void switchFromIframeToMainWindow() {
        driver.switchTo().defaultContent();
    }

    @When("click on ok button present in windows alert pop up")
    public void clickOnOkButtonPresentInWindowsAlertPopUp() {
        driver.switchTo().alert().accept();
        driver.switchTo().defaultContent();
    }

    @And("switch to parent frame")
    public void switchToParentFrame() {
        driver.switchTo().parentFrame();
    }

    @And("^\"([^\\\"]*)\" message is displayed$")
    @Then("^([^\\\"]*) message is displayed$")
    public void messageIsDisplayed(String arg0) {
        WebElement ele = waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        assertNotNull(ele, "unable to load the page in the specified time duration");
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath(CommonPage.XPATH_TEXT_1 + arg0 + CommonPage.XPATH_TEXT_2));
        assertTrue(bool, arg0 + " message is not displayed");
    }

    @And("move the recent pdf file from download folder to location {string}")
    public void moveTheRecentPdfFileToSomeFolder(String relativeLocation) throws IOException {
        HashMap<String, String> map = findLatestFile("pdf", null, null);
        String fileName = map.get(Constants.FILE_NAME);
        String sourceFileFullPath = map.get(Constants.FILE_NAME);
        boolean bool = moveFile(relativeLocation, sourceFileFullPath, fileName);
        assertTrue(bool);
    }

    @And("below words are present in the recent pdf file present in location {string}")
    public void belowWordsArePresentInTheRecentPdfFileInSomeFolder(String relativeLocation, DataTable dataTable) throws IOException {
        List<String> contentList = dataTable.asList(String.class);
        HashMap<String, String> map = findLatestFile("pdf", relativeLocation, null);
        String fileName = map.get(Constants.FILE_NAME);
        String sourceFileFullPath = map.get(Constants.FILE_FULL_PATH);
        InputStream in = readFile(relativeLocation, sourceFileFullPath, fileName);
        BufferedInputStream bf = new BufferedInputStream(in);
        PDDocument doc = PDDocument.load(bf);
        String content = new PDFTextStripper().getText(doc);
        for (String str : contentList) {
            logger.info(str);
            assertTrue(content.contains(str));
        }
        doc.close();
    }

    @When("click enter button from keyboard")
    public void clickEnterButtonFromKeyboard() {
        Actions action = new Actions(driver);
        action.sendKeys(Keys.ENTER).perform();
        E2eUtil.wait(1000);
    }

    @And("{string} message is displayed in delete item confirmation pop up window")
    public void messageIsDisplayedInDeleteItemConfirmationPopUpWindow(String arg0) {
        String message =  getElementAttributeTextContent(driver, CommonPage.CONFIRMATION_DIALOG_MESSAGE);
        assertEquals(message, arg0);
    }
}
