package europa.edit.stepdef;

import europa.edit.pages.AnnexPage;
import europa.edit.pages.CKEditorPage;
import europa.edit.pages.LegalActPage;
import europa.edit.util.BaseDriver;
import europa.edit.util.E2eUtil;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

import static europa.edit.util.Common.*;
import static org.testng.Assert.*;

public class CKEditorSteps extends BaseDriver {

    @And("{int} plugins are available in ck editor window")
    public void pluginsAreAvailableInCkEditorWindow(int arg0) {
        int size = driver.findElements(CKEditorPage.CKE_BUTTON).size();
        assertEquals(size, arg0);
    }

    @And("save button is disabled in ck editor")
    public void saveButtonIsDisabledInCkEditor() {
        String isDisabled = getAttributeValueFromElement(driver,CKEditorPage.SAVE_BUTTON, "aria-disabled");
        assertEquals(isDisabled, "true");
    }

    @And("save close button is disabled in ck editor")
    public void saveCloseButtonIsDisabledInCkEditor() {
        String isDisabled = getAttributeValueFromElement(driver,CKEditorPage.SAVE_CLOSE_BUTTON, "aria-disabled");
        assertEquals(isDisabled, "true");
    }
    
    @And("close button is enabled in ck editor")
    public void closeButtonIsEnabledInCkEditor() {
        String isDisabled = getAttributeValueFromElement(driver,CKEditorPage.CLOSE_BUTTON, "aria-disabled");
        assertEquals(isDisabled, "false");
    }

    @And("cut button is disabled in ck editor")
    public void cutButtonIsDisabledInCkEditor() {
        String isDisabled = getAttributeValueFromElement(driver,CKEditorPage.CUT_BUTTON, "aria-disabled");
        assertEquals(isDisabled, "true");
    }

    @And("copy button is disabled in ck editor")
    public void copyButtonIsDisabledInCkEditor() {
        String isDisabled = getAttributeValueFromElement(driver,CKEditorPage.COPY_BUTTON, "aria-disabled");
        assertEquals(isDisabled, "true");
    }

    @And("undo button is disabled in ck editor")
    public void undoButtonIsDisabledInCkEditor() {
        String isDisabled = getAttributeValueFromElement(driver,CKEditorPage.UNDO_BUTTON, "aria-disabled");
        assertEquals(isDisabled, "true");
    }

    @And("redo button is disabled in ck editor")
    public void redoButtonIsDisabledInCkEditor() {
        String isDisabled = getAttributeValueFromElement(driver,CKEditorPage.REDO_BUTTON, "aria-disabled");
        assertEquals(isDisabled, "true");
    }

    @And("subscript button is enabled in ck editor")
    public void subscriptButtonIsEnabledInCkEditor() {
        String isDisabled = getAttributeValueFromElement(driver,CKEditorPage.SUBSCRIPT_BUTTON, "aria-disabled");
        assertEquals(isDisabled, "false");
    }

    @And("superscript button is enabled in ck editor")
    public void superscriptButtonIsEnabledInCkEditor() {
        String isDisabled = getAttributeValueFromElement(driver,CKEditorPage.SUPERSCRIPT_BUTTON, "aria-disabled");
        assertEquals(isDisabled, "false");
    }

    @And("special character button is enabled in ck editor")
    public void specialCharacterButtonIsEnabledInCkEditor() {
        String isDisabled = getAttributeValueFromElement(driver,CKEditorPage.SPECIAL_CHARACTER_BUTTON, "aria-disabled");
        assertEquals(isDisabled, "false");
    }

    @And("paste button is enabled in ck editor")
    public void pasteButtonIsDisabledInCkEditor() {
        String isDisabled = getAttributeValueFromElement(driver,CKEditorPage.PASTE_BUTTON, "aria-disabled");
        assertEquals(isDisabled, "false");
    }

    @And("source button is enabled in ck editor")
    public void sourceButtonIsEnabledInCkEditor() {
        String isDisabled = getAttributeValueFromElement(driver,CKEditorPage.SOURCE_DIALOG_BUTTON, "aria-disabled");
        assertEquals(isDisabled, "false");
    }

    @And("show blocks button is enabled in ck editor")
    public void showBlocksButtonIsEnabledInCkEditor() {
        String isDisabled = getAttributeValueFromElement(driver,CKEditorPage.SHOW_BLOCKS_BUTTON, "aria-disabled");
        assertEquals(isDisabled, "false");
    }

    @Then("increase indent icon is disabled in ck editor")
    public void increaseIndentIconIsDisabledInCkEditor() {
        String isDisabled = getAttributeValueFromElement(driver,CKEditorPage.INCREASE_INTEND_ICON, "aria-disabled");
        assertEquals(isDisabled, "true");
    }

    @When("click on increase indent icon present in ck editor panel")
    public void clickOnIncreaseIndentIconPresentInCkEditorPanel() {
        elementClick(driver, CKEditorPage.INCREASE_INTEND_ICON);
    }

    @And("decrease indent icon is displayed and enabled in ck editor panel")
    public void decreaseIndentIconIsDisplayedAndEnabledInCkEditorPanel() {
        boolean isDisplayed = waitForElementTobeDisPlayed(driver, CKEditorPage.DECREASE_INTEND_ICON);
        assertTrue(isDisplayed);
        boolean isEnabled = verifyElementIsEnabled(driver, CKEditorPage.DECREASE_INTEND_ICON);
        assertTrue(isEnabled);
    }

    @When("click on decrease indent icon present in ck editor panel")
    public void clickOnDecreaseIndentIconPresentInCkEditorPanel() {
        elementClick(driver, CKEditorPage.DECREASE_INTEND_ICON);
    }

    @And("increase indent icon is displayed and enabled in ck editor panel")
    public void increaseIndentIconIsDisplayedAndEnabledInCkEditorPanel() {
        boolean isDisplayed = waitForElementTobeDisPlayed(driver, CKEditorPage.INCREASE_INTEND_ICON);
        assertTrue(isDisplayed);
        boolean isEnabled = verifyElementIsEnabled(driver, CKEditorPage.INCREASE_INTEND_ICON);
        assertTrue(isEnabled);
    }

    @And("click on soft enter icon present in ck editor panel")
    public void clickOnSoftEnterIconPresentInCkEditorPanel() {
        elementClick(driver, CKEditorPage.SOFT_ENTER);
    }

    @Then("show all actions icon is displayed")
    public void showAllActionsIconIsDisplayed() {
        boolean bool = verifyElement(driver, AnnexPage.SHOW_ALL_ACTIONS);
        assertTrue(bool, "show all actions icon is not displayed");
    }

    @When("click on show all actions icon")
    public void clickOnShowAllActionsIcon() {
        Actions actions = new Actions(driver);
        WebElement ele = waitForElementTobePresent(driver, AnnexPage.SHOW_ALL_ACTIONS);
        actions.moveToElement(ele).build().perform();
    }

    @When("click on table icon of ck editor")
    public void clickOnTableIconOfCkEditor() {
        elementClick(driver, CKEditorPage.TABLE_ICON);
        E2eUtil.wait(2000);
    }

    @Then("table properties window is displayed")
    public void tablePropertiesWindowIsDisplayed() {
        boolean bool = waitForElementTobeDisPlayed(driver, LegalActPage.TABLE_PROPERTIES_WINDOW);
        assertTrue(bool, "table properties window is not displayed");
    }

    @When("provide rows as {int} in table properties window")
    public void provideRowsAsInTablePropertiesWindow(int arg0) {
        elementEcasSendkeys(driver, LegalActPage.TABLE_PROPERTIES_WINDOW_ROWS_INPUT, String.valueOf(arg0));
    }

    @And("provide columns as {int} in table properties window")
    public void provideColumnsAsInTablePropertiesWindow(int arg0) {
        elementEcasSendkeys(driver, LegalActPage.TABLE_PROPERTIES_WINDOW_COLUMNS_INPUT, String.valueOf(arg0));
    }

    @And("select option with value {string} from headers dropdown in table properties window")
    public void selectOptionWithValueFromHeadersDropdownInTablePropertiesWindow(String arg0) {
        elementClick(driver, LegalActPage.TABLE_PROPERTIES_WINDOW_HEADER_SELECT);
        WebElement ele = waitForElementTobePresent(driver, LegalActPage.TABLE_PROPERTIES_WINDOW_HEADER_SELECT);
        Select objSelect = new Select(ele);
        objSelect.selectByValue(arg0);
    }

    @And("click on ok button present in table properties window")
    public void clickOnOkButtonPresentInTablePropertiesWindow() {
        elementClick(driver, LegalActPage.TABLE_PROPERTIES_WINDOW_HEADER_OK_BUTTON);
    }

    @And("click on insert footnote icon present in ck editor panel")
    public void clickOnInsertFootnoteIconPresentInCkEditorPanel() {
        elementClick(driver, CKEditorPage.INSERT_FOOTNOTE_ICON);
        E2eUtil.wait(2000);
    }

    @Then("edit footnote window is displayed")
    public void editFootnoteWindowIsDisplayed() {
        boolean bool = waitForElementTobeDisPlayed(driver, LegalActPage.EDIT_FOOTNOTE_WINDOW);
        assertTrue(bool, "edit footnote window is not displayed");
    }

    @When("enter {string} in text area of edit footnote window")
    public void enterInTextAreaOfEditFootnoteWindow(String arg0) {
        elementEcasSendkeys(driver, LegalActPage.FOOTNOTE_WINDOW_TEXTAREA, arg0);
    }

    @And("click on ok button present in edit footnote window")
    public void clickOnOkButtonPresentInEditFootnoteWindow() {
        elementClick(driver, LegalActPage.FOOTNOTE_WINDOW_OK_BUTTON);
    }
}