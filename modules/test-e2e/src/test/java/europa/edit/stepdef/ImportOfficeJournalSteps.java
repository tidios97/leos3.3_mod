package europa.edit.stepdef;

import europa.edit.pages.CommonPage;
import europa.edit.pages.ImportFromOfficeJournal;
import europa.edit.util.BaseDriver;
import europa.edit.util.E2eUtil;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.NoSuchElementException;
import static europa.edit.util.Common.*;
import static org.testng.Assert.*;

public class ImportOfficeJournalSteps extends BaseDriver {

    @And("below options are displayed in Type dropdown")
    public void belowOptionsAreDisplayedInTypeDropdown(DataTable dataTable) {
        String text;
        List<String> actualOptionList = new ArrayList<>();
        List<String> givenOptionList = dataTable.asList(String.class);
        List<WebElement> elements = driver.findElements(ImportFromOfficeJournal.TYPE_SELECT_CLASS_OPTION);
        assertTrue((null != elements && !elements.isEmpty()), "No element present for type dropdown");
        for (WebElement element : elements) {
            text = element.getText();
            actualOptionList.add(text);
        }
        assertTrue(actualOptionList.containsAll(givenOptionList), "given options are not present in the type dropdown values");
    }

    @And("{string} option is selected by default in Type field")
    public void optionIsSelectedByDefaultInTypeField(String arg0) {
        Select select = new Select(driver.findElement(ImportFromOfficeJournal.TYPE_SELECT_CLASS));
        String text = select.getFirstSelectedOption().getText();
        assertEquals(text, arg0, arg0 + " is not selected by default in Type field.Selected Value is " + text);
    }

    @And("current year is selected by default for Year field")
    public void currentYearIsSelectedByDefaultForYearField() {
        String currentYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        Select select = new Select(driver.findElement(ImportFromOfficeJournal.YEAR_SELECT_CLASS));
        String text = select.getFirstSelectedOption().getText();
        assertEquals(text, currentYear, currentYear + " is not selected by default for Year field.Selected Value is " + text);
    }

    @And("blank input box is present for Nr. field")
    public void blankInputBoxIsPresentForNrField() {
        String text = getElementAttributeValue(driver, ImportFromOfficeJournal.NR_INPUT);
        assertEquals(text, "", "provided value is " + text);
    }

    @And("Search button is displayed and enabled")
    public void searchButtonIsDisplayedAndEnabled() {
        boolean bool = verifyElement(driver, ImportFromOfficeJournal.SEARCH_BUTTON);
        assertTrue(bool, "Search button is not displayed");
        boolean bool1 = verifyElementIsEnabled(driver, ImportFromOfficeJournal.SEARCH_BUTTON);
        assertTrue(bool1, "Search button is not enabled");
    }

    @And("i button is displayed")
    public void iButtonIsDisplayed() {
        boolean bool = waitForElementTobeDisPlayed(driver, ImportFromOfficeJournal.I_BUTTON);
        assertTrue(bool, "i button is not displayed");
    }

    @And("select all recitals button is displayed but disabled")
    public void selectAllRecitalsButtonIsDisplayedButDisabled() {
        boolean bool = verifyElement(driver, ImportFromOfficeJournal.SELECT_ALL_RECITALS_BUTTON);
        assertTrue(bool, "select all recitals button is not displayed");
        boolean bool1 = verifyElementIsEnabled(driver, ImportFromOfficeJournal.SELECT_ALL_RECITALS_BUTTON_DISABLED);
        assertTrue(bool1, "select all recitals button is not disabled");
    }

    @And("select all articles button is displayed but disabled")
    public void selectAllArticlesButtonIsDisplayedButDisabled() {
        boolean bool = verifyElement(driver, ImportFromOfficeJournal.SELECT_ALL_ARTICLES_BUTTON);
        assertTrue(bool, "select all articles button is not displayed");
        boolean bool1 = verifyElementIsEnabled(driver, ImportFromOfficeJournal.SELECT_ALL_ARTICLES_BUTTON_DISABLED);
        assertTrue(bool1, "select all articles button is not disabled");
    }

    @And("import button is displayed but disabled")
    public void importButtonIsDisplayedButDisabled() {
        boolean bool = verifyElement(driver, ImportFromOfficeJournal.IMPORT_BUTTON);
        assertTrue(bool, "import button is not displayed");
        boolean bool1 = verifyElement(driver, ImportFromOfficeJournal.IMPORT_BUTTON_DISABLED);
        assertTrue(bool1, "import button is not disabled");
    }

    @And("close button in import office journal window is displayed and enabled")
    public void closeButtonInImportOfficeJournalWindowIsDisplayedAndEnabled() {
        boolean bool = verifyElement(driver, ImportFromOfficeJournal.CLOSE_BUTTON);
        assertTrue(bool, "close button is not displayed");
        boolean bool1 = verifyElementIsEnabled(driver, ImportFromOfficeJournal.CLOSE_BUTTON);
        assertTrue(bool1, "close button is not enabled");
    }

    @When("mouse hover on i button")
    public void mouseHoverOnIButton() {
        Actions actions = new Actions(driver);
        WebElement ele = driver.findElement(ImportFromOfficeJournal.I_BUTTON);
        actions.moveToElement(ele).build().perform();
    }

    @Then("tooltip contains messages {string} and {string}")
    public void tooltipMessageIsDisplayed(String arg0, String arg1) {
        String text = getElementAttributeInnerText(driver, ImportFromOfficeJournal.I_MOUSE_HOVER_TEXT);
        assertTrue(text.contains(arg0), arg0 + " is not part of " + text);
        assertTrue(text.contains(arg1), arg1 + " is not part of " + text);
    }

    @When("click on search button in import office journal window")
    public void clickOnSearchButtonInImportOfficeJournalWindow() {
        elementClick(driver, ImportFromOfficeJournal.SEARCH_BUTTON);
        E2eUtil.wait(2000);
    }

    @Then("border of input box is showing as {string} color")
    public void borderOfInputBoxIsShowingAsRedColor(String arg0) {
        boolean bool = verifyElement(driver, ImportFromOfficeJournal.NR_INPUT_ERROR_INDICATOR);
        assertTrue(bool, "no error color present on the border of Nr. input box");
        WebElement ele = driver.findElement(ImportFromOfficeJournal.NR_INPUT_ERROR_INDICATOR);
        assertNotNull(ele);
        String borderColor = ele.getCssValue("border-bottom-color");
        assertEquals(borderColor, arg0, "border of input box is not showing as per the mentioned color");
    }

    @And("exclamation mark is appeared with {string} color")
    public void exclamationMarkIsAppearedWithRedColor(String arg0) {
        boolean bool = verifyElement(driver, ImportFromOfficeJournal.ERROR_INDICATOR);
        assertTrue(bool, "exclamation mark is not displayed");
        WebElement ele = driver.findElement(ImportFromOfficeJournal.ERROR_INDICATOR);
        assertNotNull(ele);
        String color = ele.getCssValue("color");
        assertEquals(color, arg0, "exclamation mark is not showing as per the mentioned color");
    }

    @When("select option {string} in Type field")
    public void selectOptionInTypeField(String arg0) {
        Select select = new Select(driver.findElement(ImportFromOfficeJournal.TYPE_SELECT_CLASS));
        select.selectByVisibleText(arg0);
    }

    @And("select option {string} in Year field")
    public void selectOptionInYearField(String arg0) {
        Select select = new Select(driver.findElement(ImportFromOfficeJournal.YEAR_SELECT_CLASS));
        select.selectByVisibleText(arg0);
    }

    @And("provide value {string} in Nr. field")
    public void provideValueInNrField(String arg0) {
        elementEcasSendkeys(driver, ImportFromOfficeJournal.NR_INPUT, arg0);
    }

    @Then("bill content is appeared in import window")
    public void billContentIsAppearedInImportWindow() {
        WebElement ele = waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        assertNotNull(ele, "unable to load the page in the specified time duration");
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath(ImportFromOfficeJournal.BILL));
        assertTrue(bool, "bill content is not appeared in import window");
    }

    @And("checkbox is available beside to each recital")
    public void checkboxIsAvailableBesideToEachRecital() {
        WebElement element;
        List<WebElement> inputElementList = new ArrayList<>();
        List<WebElement> elementList = driver.findElements(By.xpath(ImportFromOfficeJournal.RECITAL));
        assertTrue(elementList.size() > 0, "No recital present in the bill");
        for (int i = 1; i <= elementList.size(); i++) {
            try {
                element = waitForElementTobePresent(driver, By.xpath("(" + ImportFromOfficeJournal.RECITAL + ")" + "[" + i + "]//parent::div//preceding-sibling::input"));
            } catch (NoSuchElementException | StaleElementReferenceException e) {
                element = null;
            }
            if (null != element)
                inputElementList.add(element);
        }
        assertEquals(inputElementList.size(), elementList.size(), "checkbox is not available beside to each recital");
    }

    @And("checkbox is available beside to each article")
    public void checkboxIsAvailableBesideToEachArticle() {
        WebElement element;
        List<WebElement> inputElementList = new ArrayList<>();
        List<WebElement> elementList = driver.findElements(By.xpath(ImportFromOfficeJournal.ARTICLE));
        assertTrue(elementList.size() > 0, "No article present in the bill");
        for (int i = 1; i <= elementList.size(); i++) {
            try {
                element = waitForElementTobePresent(driver, By.xpath("(" + ImportFromOfficeJournal.ARTICLE + ")" + "[" + i + "]//parent::div//preceding-sibling::input"));
            } catch (NoSuchElementException | StaleElementReferenceException e) {
                element = null;
            }
            if (null != element)
                inputElementList.add(element);
        }
        assertEquals(inputElementList.size(), elementList.size(), "checkbox is not available beside to each article");
    }

    @When("click on checkbox of recital {int}")
    public void clickOnCheckboxOfRecital(int arg0) {
        elementClick(driver, By.xpath(ImportFromOfficeJournal.BILL + ImportFromOfficeJournal.RECITALS + ImportFromOfficeJournal.LEOS_IMPORT_WRAPPER + "[" + arg0 + "]//input"));
    }

    @When("click on checkbox of article {int}")
    public void clickOnCheckboxOfArticle(int arg0) {
        elementClick(driver, By.xpath(ImportFromOfficeJournal.BILL + ImportFromOfficeJournal.AKNBODY + ImportFromOfficeJournal.LEOS_IMPORT_WRAPPER + "[" + arg0 + "]//input"));
    }

    @When("click on import button")
    public void clickOnImportButton() {
        elementClick(driver, ImportFromOfficeJournal.IMPORT_BUTTON);
        E2eUtil.wait(3000);
    }

    @When("click on select all recitals button in import office journal window")
    public void clickOnSelectAllRecitalsButtonInImportOfficeJournalWindow() {
        elementClick(driver, ImportFromOfficeJournal.SELECT_ALL_RECITALS_BUTTON);
    }

    @When("click on select all articles button in import office journal window")
    public void clickOnSelectAllArticlesButtonInImportOfficeJournalWindow() {
        elementClick(driver, ImportFromOfficeJournal.SELECT_ALL_ARTICLES_BUTTON);
    }

    @Then("checkboxes of all the recitals are selected")
    public void checkboxesOfAllTheRecitalsAreSelected() {
        WebElement element;
        List<WebElement> inputElementList = new ArrayList<>();
        List<WebElement> elementList = driver.findElements(By.xpath(ImportFromOfficeJournal.RECITAL));

        for (int i = 1; i <= elementList.size(); i++) {
            try {
                element = waitForElementTobePresent(driver, By.xpath("(" + ImportFromOfficeJournal.RECITAL + ")" + "[" + i + "]//parent::div//preceding-sibling::input"));
                boolean bool = element.isSelected();
                if (bool)
                    inputElementList.add(element);
            } catch (NoSuchElementException | StaleElementReferenceException e) {
                e.printStackTrace();
            }
        }
        assertEquals(inputElementList.size(), elementList.size(), "checkboxes of all the recitals are not selected");
    }

    @And("number of recitals selected is {int}")
    public void numberOfRecitalsSelectedIs(int arg0) {
        WebElement element;
        List<WebElement> inputElementList = new ArrayList<>();
        List<WebElement> elementList = driver.findElements(By.xpath(ImportFromOfficeJournal.RECITAL));
        assertTrue(elementList.size() > 0, "No recital present in the bill");
        for (int i = 1; i <= elementList.size(); i++) {
            try {
                element = waitForElementTobePresent(driver, By.xpath("(" + ImportFromOfficeJournal.RECITAL + ")" + "[" + i + "]//parent::div//preceding-sibling::input"));
                boolean bool = element.isSelected();
                if (bool)
                    inputElementList.add(element);
            } catch (NoSuchElementException | StaleElementReferenceException e) {
                e.printStackTrace();
            }
        }
        assertEquals(inputElementList.size(), arg0, " number of recitals are selected not " + arg0);
    }

    @Then("checkboxes of all the articles are selected")
    public void checkboxesOfAllTheArticlesAreSelected() {
        WebElement element;
        List<WebElement> inputElementList = new ArrayList<>();
        List<WebElement> elementList = driver.findElements(By.xpath(ImportFromOfficeJournal.ARTICLE));
        assertTrue(elementList.size() > 0, "No article present in the bill");
        for (int i = 1; i <= elementList.size(); i++) {
            try {
                element = waitForElementTobePresent(driver, By.xpath("(" + ImportFromOfficeJournal.ARTICLE + ")" + "[" + i + "]//parent::div//preceding-sibling::input"));
                boolean bool = element.isSelected();
                if (bool)
                    inputElementList.add(element);
            } catch (NoSuchElementException | StaleElementReferenceException e) {
                e.printStackTrace();
            }
        }
        assertEquals(inputElementList.size(), elementList.size(), "checkboxes of all the articles are not selected");
    }

    @And("number of articles selected is {int}")
    public void numberOfArticlesSelectedIs(int arg0) {
        WebElement element;
        List<WebElement> inputElementList = new ArrayList<>();
        List<WebElement> elementList = driver.findElements(By.xpath(ImportFromOfficeJournal.ARTICLE));
        assertTrue(elementList.size() > 0, "No article present in the bill");
        for (int i = 1; i <= elementList.size(); i++) {
            try {
                element = waitForElementTobePresent(driver, By.xpath("(" + ImportFromOfficeJournal.ARTICLE + ")" + "[" + i + "]//parent::div//preceding-sibling::input"));
                boolean bool = element.isSelected();
                if (bool)
                    inputElementList.add(element);
            } catch (NoSuchElementException | StaleElementReferenceException e) {
                e.printStackTrace();
            }
        }
        assertEquals(inputElementList.size(), arg0, " number of articles are selected not " + arg0);
    }
}
