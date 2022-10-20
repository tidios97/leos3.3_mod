package europa.edit.stepdef;

import europa.edit.pages.*;
import europa.edit.util.BaseDriver;
import europa.edit.util.E2eUtil;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static europa.edit.util.Common.*;
import static europa.edit.util.E2eUtil.scrollandClick;
import static org.testng.Assert.*;

public class LegalActSteps extends BaseDriver {

    @Then("legal act page is displayed")
    public void legalActPageIsDisplayed() {
        WebElement ele = waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        assertNotNull(ele);
        boolean bool = verifyElement(driver, LegalActPage.LEGAL_ACT_TEXT);
        assertTrue(bool);
    }

    @When("click on citation link present in navigation pane")
    public void clickOnCitationLinkPresentInNavigationPane() {
        elementClick(driver, LegalActPage.CITATION_LINK);
    }

    @And("double click on citation {int}")
    public void doubleClickOnFirstCitationParagraph(Integer arg0) {
        E2eUtil.wait(2000);
        doubleClick(driver, By.xpath(LegalActPage.CITATION + "[" + arg0.toString() + "]/aknp"));
    }

    @When("click on preamble toggle link")
    public void clickOnPreamble() {
        scrollandClick(driver, LegalActPage.PREAMBLE_TOGGLE_LINK);
    }

    @And("first citation is showing as read only")
    public void firstCitationIsShowingAsReadOnly() {
        E2eUtil.wait(2000);
        boolean bool = verifyElement(driver, LegalActPage.CITATION_BEFORE_CKEDITOR);
        assertTrue(bool);
    }

    @When("mouseHover and click on show all action button and click on edit button of citation {int}")
    public void mouseHoverAndClickOnShowAllActionButtonOfSecondCitation(int arg0) {
        E2eUtil.wait(2000);
        Actions actions = new Actions(driver);
        elementClick(driver, By.xpath(LegalActPage.CITATION + "[" + arg0 + "]/aknp"));
        actions.moveToElement(waitForElementTobePresent(driver, By.xpath(LegalActPage.CITATION + "[" + arg0 + "]/aknp"))).build().perform();
        E2eUtil.wait(2000);
        actions.moveToElement(waitForElementTobePresent(driver, By.xpath(LegalActPage.CITATION + "[" + arg0 + "]//following-sibling::div[1][@class='leos-actions Vaadin-Icons']"))).build().perform();
        E2eUtil.wait(2000);
        actions.moveToElement(waitForElementTobePresent(driver, By.xpath(LegalActPage.CITATION + "[" + arg0 + "]//following-sibling::div[1][@class='leos-actions Vaadin-Icons']/span[@data-widget-type='edit']"))).build().perform();
        actions.click().build().perform();
    }

    @Then("ck editor window is displayed")
    public void ckEditorWindowIsEnabled() {
        E2eUtil.wait(1000);
        WebElement ele = waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        assertNotNull(ele, "unable to show ck editor in maximum given time");
        WebElement ele1 = waitForElementTobePresent(driver, LegalActPage.CK_EDITOR_WINDOW_2);
        assertNotNull(ele1);
        WebElement ele2 = waitForElementTobePresent(driver, LegalActPage.CK_EDITOR_WINDOW_1);
        assertNotNull(ele2);
    }

    @When("click on close button present in legal act page")
    public void clickOnCloseButtonPresentInLegalActPage() {
        WebElement ele = waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        assertNotNull(ele, "unable to load the page in the specified time duration");
        elementClick(driver, CommonPage.CLOSE_BUTTON);
    }

    @Then("ck editor window is not displayed")
    public void ckEditorWindowIsNotDisplayed() {
        E2eUtil.wait(1000);
        WebElement ele = waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        assertNotNull(ele, "unable to load the page in the specified time duration");
        boolean bool = waitUnTillElementIsNotPresent(driver, LegalActPage.CK_EDITOR_WINDOW_1);
        assertTrue(bool, "cke_browser_webkit element is displayed");
        boolean bool1 = waitUnTillElementIsNotPresent(driver, LegalActPage.CK_EDITOR_WINDOW_2);
        assertTrue(bool1, "cke_widget_block element is displayed");
    }

    @When("click on close button of ck editor")
    public void clickOnCloseButtonOfCkEditor() {
        elementClick(driver, CKEditorPage.CLOSE_BUTTON);
        E2eUtil.wait(2000);
    }

    @And("get text from ck editor text box")
    public void getTextFromCkEditorTextBox() {
        getElementAttributeInnerText(driver, LegalActPage.CK_EDTOR_PARAGRAPH_INNERTEXT);
    }

    @And("get text from ck editor li text box")
    public void getTextLiFromCkEditorTextBox() {
        getElementAttributeInnerText(driver, LegalActPage.CK_EDTOR_LI_INNERTEXT);
    }

    @Then("toggle bar moved to right")
    public void toggleBarMovedToRight() {
        boolean bool = waitForElementTobeDisPlayed(driver, LegalActPage.TOGGLE_BAR_IN_RIGHT_SIDE);
        assertTrue(bool, "toggle bar is not moved to right");
    }

    @When("click on toggle bar move to right")
    public void clickOnToggleBarMoveToRight() {
        if (driver.findElements(LegalActPage.TOGGLE_BAR_IN_LEFT_SIDE).size() > 0) {
            boolean bool = waitForElementTobeDisPlayed(driver, LegalActPage.TOGGLE_BAR_IN_LEFT_SIDE);
            assertTrue(bool);
            elementActionClick(driver, LegalActPage.TOGGLE_BAR_IN_LEFT_SIDE);
        }
    }

    @When("add {string} and delete {string} in the ck editor text box")
    public void addAndDeleteInTheCkEditorTextBox(String arg0, String arg1) {
        String existingText = getElementAttributeInnerText(driver, LegalActPage.CK_EDTOR_PARAGRAPH_INNERTEXT);
        String deleteText = existingText.replace(arg1, "");
        String newText = arg0 + " " + deleteText + " " + arg0;
        elementEcasSendkeys(driver, LegalActPage.CK_EDTOR_PARAGRAPH_INNERTEXT, newText);
    }

    @When("add {string} and delete {string} in the ck editor li text box")
    public void addAndDeleteInTheCkEditorLiTextBox(String arg0, String arg1) {
        String existingText = getElementAttributeInnerText(driver, LegalActPage.CK_EDTOR_LI_INNERTEXT);
        String deleteText = existingText.replace(arg1, "");
        String newText = arg0 + " " + deleteText + " " + arg0 + ".";
        elementEcasSendkeys(driver, LegalActPage.CK_EDTOR_LI_INNERTEXT, newText);
    }

    @And("click on save close button of ck editor")
    public void clickOnSaveCloseButtonOfCkEditor() {
        WebElement element = waitForElementTobePresent(driver, CKEditorPage.SAVE_CLOSE_BUTTON);
        scrollToElement(driver, element);
        elementClick(element);
        E2eUtil.wait(2000);
    }

    @And("{string} is added in the text box")
    public void isAddedInTheTextbox(String arg0) {
        boolean bool = false;
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.SPAN + LegalActPage.LEOS_CONTENT_SOFT_NEW));
        for (WebElement element : elementList) {
            String str = getElementText(element);
            if (str.equals(arg0)) {
                bool = true;
            }
        }
        assertTrue(bool, arg0 + " is not added in the textbox");
    }

    @And("{string} is deleted with strikeout symbol in the text box")
    public void isDeletedWithStrikeoutSymbolInTheTextbox(String arg0) {
        boolean bool = false;
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.SPAN + LegalActPage.LEOS_CONTENT_SOFT_REMOVED));
        for (WebElement element : elementList) {
            String str = getElementText(element);
            if (str.equals(arg0)) {
                bool = true;
            }
        }
        assertTrue(bool, arg0 + " is not deleted in the textbox");
    }

    @Then("toc editing button is available")
    public void tocEditingButtonIsAvailable() {
        boolean bool = verifyElement(driver, ExpMemoPage.TOC_EDIT_BUTON);
        assertTrue(bool);
    }

    @When("click on the second citation")
    public void clickOnTheFirstCitation() {
        elementActionClick(driver, LegalActPage.CITATION_SECOND_PARAGRAPH);
    }

    @When("click on the first preamble formula")
    public void clickOnTheFirstPremableFormula() {
        elementClick(driver, LegalActPage.PREAMBLE_FORMULA_AKNP);
    }

    @When("select content on first preamble formula")
    public void selectContentOnFirstPremableFormula() {
        selectText(driver, LegalActPage.PREAMBLE_FORMULA_AKNP);
    }

    @When("click on preamble text present in TOC")
    public void clickOnPreambleTextPresentInTOC() {
        elementClick(driver, LegalActPage.PREAMBLE_TEXT);
    }

    @When("select content in citation {int}")
    public void select_content_in_citation(Integer arg0) {
        E2eUtil.wait(2000);
        selectText(driver, By.xpath(LegalActPage.CITATION + "[" + arg0 + "]/aknp"));
    }

    @When("mouseHover and click on show all action button and click on edit button of recital {int}")
    public void mousehoverAndClickOnShowAllActionButtonAndClickOnEditButtonOfSecondRecital(int arg0) {
        E2eUtil.wait(5000);
        Actions actions = new Actions(driver);
        elementClick(driver, By.xpath(LegalActPage.RECITAL + "[" + arg0 + "]/aknp"));
        actions.moveToElement(waitForElementTobePresent(driver, By.xpath(LegalActPage.RECITAL + "[" + arg0 + "]/aknp"))).build().perform();
        E2eUtil.wait(2000);
        WebElement showAllMenu = waitForElementTobePresent(driver, By.xpath(LegalActPage.RECITAL + "[" + arg0 + "]//following-sibling::div[1][@class='leos-actions Vaadin-Icons']/span[@title='Show all actions']"));
        actions.moveToElement(showAllMenu).build().perform();
        E2eUtil.wait(3000);
        WebElement editButton = waitForElementTobePresent(driver, By.xpath(LegalActPage.RECITAL + "[" + arg0 + "]//following-sibling::div[1][@class='leos-actions Vaadin-Icons']/span[@data-widget-type='edit']"));
        actions.moveToElement(editButton).build().perform();
        E2eUtil.wait(2000);
        actions.click().build().perform();
    }

    @When("click on recital link present in navigation pane")
    public void clickOnRecitalLinkPresentInNavigationPane() {
        elementClick(driver, By.xpath(LegalActPage.TOC_RECITAL_LINK));
    }

    @And("double click on recital {int}")
    public void doubleClickOnRecital(int arg0) {
        E2eUtil.wait(2000);
        doubleClick(driver, By.xpath(LegalActPage.RECITAL + "[" + arg0 + "]" + LegalActPage.AKNP));
    }

    @When("select content in recital {int}")
    public void selectContentInRecital(int arg0) {
        E2eUtil.wait(2000);
        selectText(driver, By.xpath(LegalActPage.RECITAL + "[" + arg0 + "]" + LegalActPage.AKNP));
    }

    @When("click on article {int} in navigation pane")
    public void clickOnArticleInNavigatonPane(int arg0) {
        scrollandClick(driver, By.xpath(LegalActPage.TOC_TABLE_TREE_GRID + CommonPage.XPATH_CONTAINS_TEXT_1 + "Article " + arg0 + CommonPage.XPATH_CONTAINS_TEXT_2));
        E2eUtil.wait(2000);
    }

    @And("double click on paragraph {int}")
    public void doubleClickOnParagraph(int arg0) {
        E2eUtil.wait(2000);
        doubleClick(driver, By.xpath("(" + LegalActPage.PARAGRAPH + ")[" + arg0 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP));
    }

    @When("mousehover and click on show all action button and click on edit button of point {int}")
    public void mousehoverAndClickOnShowAllActionButtonAndClickOnEditButtonOfPoint(int arg0) {
        E2eUtil.wait(2000);
        Actions actions = new Actions(driver);
        elementClick(driver, By.xpath("(" + LegalActPage.POINT + ")[" + arg0 + "]//aknp"));
        WebElement citation = driver.findElement(By.xpath("(" + LegalActPage.POINT + ")[" + arg0 + "]//aknp"));
        actions.moveToElement(citation).build().perform();
        E2eUtil.wait(2000);
        WebElement showAllMenu = driver.findElement(By.xpath("(" + LegalActPage.POINT + ")[" + arg0 + "]//following-sibling::div[1][@class='leos-actions Vaadin-Icons']"));
        actions.moveToElement(showAllMenu).build().perform();
        E2eUtil.wait(3000);
        WebElement editButton = driver.findElement(By.xpath("(" + LegalActPage.POINT + ")[" + arg0 + "]//following-sibling::div[1][@class='leos-actions Vaadin-Icons']/span[@data-widget-type='edit']"));
        actions.moveToElement(editButton).build().perform();
        actions.click().build().perform();
    }

    @When("click on toc edit button")
    public void click_on_toc_edition() {
        elementClick(driver, LegalActPage.TOC_EDIT_BUTON);
        E2eUtil.wait(2000);
    }

    @Then("save button in navigation pane is disabled")
    public void save_button_in_navigation_pane_is_disabled() {
        String str = driver.findElement(LegalActPage.NAVIGATION_PANE_SAVE_BUTTON).getAttribute("class");
        assertTrue(str.contains("v-disabled"), "save button in navigation pane is not disabled");
    }

    @Then("save and close button in navigation pane is disabled")
    public void save_and_close_button_in_navigation_pane_is_disabled() {
        String str = driver.findElement(LegalActPage.NAVIGATION_PANE_SAVE_AND_CLOSE_BUTTON).getAttribute("class");
        assertTrue(str.contains("v-disabled"), "save and close button in navigation pane is not disabled");
    }

    @Then("cancel button in navigation pane is displayed and enabled")
    public void close_buton_in_navigation_pane_is_displayed_and_enabled() {
        boolean bool = verifyElement(driver, LegalActPage.NAVIGATION_PANE_CANCEL_BUTTON);
        assertTrue(bool, "cancel buton in navigation pane is not displayed");
        boolean bool1 = verifyElementIsEnabled(driver, LegalActPage.NAVIGATION_PANE_CANCEL_BUTTON);
        assertTrue(bool1, "cancel buton in navigation pane is not enabled");
    }

    @Then("selected element section is displayed")
    public void selected_element_section_is_displayed() {
        boolean bool = verifyElement(driver, By.xpath(LegalActPage.SELECTED_ELEMENT_TEXT));
        assertTrue(bool, "selected element section is not displayed");
    }

    @Then("input value {string} for element Type is disabled in selected element section")
    public void typeIsDisabledWithValue(String arg0) {
        String str = getElementAttributeValue(driver, LegalActPage.SELECTED_ELEMENT_TYPE_INPUT);
        assertEquals(str, arg0);
        String className = driver.findElement(LegalActPage.SELECTED_ELEMENT_TYPE_INPUT).getAttribute("class");
        assertTrue(className.contains("v-disabled"), "input value for element Type is not disabled");
    }

    @Then("input value {string} for element Number is disabled in selected element section")
    public void numberIsDisabledWithValue(String arg0) {
        String str = getElementAttributeValue(driver, LegalActPage.SELECTED_ELEMENT_NUMBER_INPUT);
        assertEquals(str, arg0, "input value is not " + arg0);
        String className = driver.findElement(LegalActPage.SELECTED_ELEMENT_NUMBER_INPUT).getAttribute("class");
        assertTrue(className.contains("v-disabled"), "input value for element Number is not disabled");
    }

    @Then("input value {string} for element Heading is editable in selected element section")
    public void heading_of_the_article_is_editable(String arg0) {
        String str = getElementAttributeValue(driver, LegalActPage.SELECTED_ELEMENT_HEADING_INPUT);
        assertEquals(str, arg0);
        boolean bool = verifyElementIsEnabled(driver, LegalActPage.SELECTED_ELEMENT_HEADING_INPUT);
        assertTrue(bool, "input value for element Heading is not editable");
    }

    @Then("Paragraph Numbering has below options")
    public void paragraph_numbering_has_below_two_options(DataTable dataTable) {
        List<String> actualParagraphNumberingOptions = new ArrayList<>();
        List<String> givenParagraphNumberingOptions = dataTable.asList(String.class);
        List<WebElement> elementList = driver.findElements(LegalActPage.SELECTED_ELEMENT_PARAGRAPH_NUMBERING_LABEL_LIST);
        assertNotNull(elementList);
        for (int i = 0; i < elementList.size(); i++) {
            String str = elementList.get(i).getText();
            actualParagraphNumberingOptions.add(i, str);
        }
        assertTrue(actualParagraphNumberingOptions.containsAll(givenParagraphNumberingOptions), "one or more options are not present in Paragraph Numbering options");
    }

    @Then("both the options of Paragraph Numbering are editable")
    public void both_options_are_editable() {
        boolean bool = verifyElementIsEnabled(driver, LegalActPage.SELECTED_ELEMENT_PARAGRAPH_NUMBERING_NUMBERED_INPUT);
        assertTrue(bool, "option Numbered of element Paragraph Numbering is not editable");
        boolean bool1 = verifyElementIsEnabled(driver, LegalActPage.SELECTED_ELEMENT_PARAGRAPH_NUMBERING_UNNUMBERED_INPUT);
        assertTrue(bool1, "option Unnumbered of element Paragraph Numbering is not editable");
    }

    @Then("delete button is displayed and enabled in selected element section")
    public void deleteButtonIsDisplayedAndEnabledInSelectedElementSection() {
        boolean bool = verifyElement(driver, LegalActPage.SELECTED_ELEMENT_DELETE_BUTTON);
        assertTrue(bool, "delete button in selected element section is not displayed");
        boolean bool1 = verifyElementIsEnabled(driver, LegalActPage.SELECTED_ELEMENT_DELETE_BUTTON);
        assertTrue(bool1, "delete button in selected element section is not enabled");
    }

    @When("click on cross symbol of the selected element")
    public void click_on_cross_symbol_for_the_selected_element() {
        elementClick(driver, LegalActPage.SELECTED_ELEMENT_CLOSE_BUTTON);
    }

    @Then("selected element section is not displayed")
    public void selected_element_section_is_not_displayed() {
        boolean bool = waitUnTillElementIsNotPresent(driver, By.xpath(LegalActPage.SELECTED_ELEMENT_TEXT));
        assertTrue(bool, "Selected element Section is displayed");
    }

    @Then("save button in navigation pane is not displayed")
    public void saveButtonInNavigationPaneIsNotDisplayed() {
        boolean bool = waitUnTillElementIsNotPresent(driver, LegalActPage.NAVIGATION_PANE_SAVE_BUTTON);
        assertTrue(bool, "save button in navigation pane is present");
    }

    @Then("save and close button in navigation pane is not displayed")
    public void saveAndCloseButtonInNavigationPaneIsNotDisplayed() {
        boolean bool = waitUnTillElementIsNotPresent(driver, LegalActPage.NAVIGATION_PANE_SAVE_AND_CLOSE_BUTTON);
        assertTrue(bool, "save and close button in navigation pane is present");
    }

    @Then("cancel button in navigation pane is not displayed")
    public void cancelButtonInNavigationPaneIsNotDisplayed() {
        boolean bool = waitUnTillElementIsNotPresent(driver, LegalActPage.NAVIGATION_PANE_CANCEL_BUTTON);
        assertTrue(bool, "cancel button in navigation pane is present");
    }

    @Then("below element lists are displayed in Elements menu")
    public void below_element_lists_is_displayed_in_element_menu(DataTable dataTable) {
        List<String> actualElementList = new ArrayList<>();
        List<String> givenElementList = dataTable.asList(String.class);
        String str;
        List<WebElement> elementList = driver.findElements(LegalActPage.LEOS_DRAG_ITEM_LIST);
        assertNotNull(elementList);
        for (WebElement webElement : elementList) {
            str = getElementAttributeInnerText(webElement);
            actualElementList.add(str);
        }
        assertEquals(actualElementList.size(), givenElementList.size());
        assertTrue(actualElementList.containsAll(givenElementList), "one or more element types are not present in Elements menu");
    }

    @When("click on {string} button present in navigation pane")
    public void click_on_button_present_in_navigation_pane(String str) {
        if (str.equals("cancel")) {
            scrollandClick(driver, LegalActPage.NAVIGATION_PANE_CANCEL_BUTTON);
        }
        if (str.equals("save and close")) {
            scrollandClick(driver, LegalActPage.NAVIGATION_PANE_SAVE_AND_CLOSE_BUTTON);
        }
        if (str.equals("save")) {
            scrollandClick(driver, LegalActPage.NAVIGATION_PANE_SAVE_BUTTON);
        }
        E2eUtil.wait(2000);
    }

    @When("click on delete button present in selected element section")
    public void click_on_delete_button_present_in_selected_element_section() {
        elementClick(driver, LegalActPage.SELECTED_ELEMENT_DELETE_BUTTON);
    }

    @Then("message {string} is displayed in the {string} pop up window")
    public void message_is_displayed_in_the_pop_up_window(String string1, String string2) {
        boolean bool = verifyElement(driver, By.xpath("//*[text()='" + string2 + "']//ancestor::div[@class='popupContent']//*[text()='" + string1 + "']"));
        assertTrue(bool);
    }

    @When("click on continue button present in Delete item: confirmation pop up window")
    public void click_on_button_present_in_pop_up_window() {
        elementClick(driver, CommonPage.DELETE_ITEM_CONFIRMATION_WINDOW_CONTINUE_BUTTON);
    }

    @Then("successful message {string} is showing above selected element section in navigation pane")
    public void successful_message_is_showing_above_selected_element_section_in_navigation_pane(String string) {
        boolean bool = verifyElement(driver, By.xpath(CommonPage.XPATH_TEXT_1 + string + CommonPage.XPATH_TEXT_2));
        assertTrue(bool);
    }

    @Then("error message {string} is showing above selected element section in navigation pane")
    public void error_message_is_showing_above_selected_element_section_in_navigation_pane(String string) {
        boolean bool = verifyElement(driver, By.xpath(CommonPage.XPATH_TEXT_1 + string + CommonPage.XPATH_TEXT_2));
        assertTrue(bool);
    }

    @Then("elements section attached to navigation pane is not displayed")
    public void elementsSectionAttachedToNavigationIsNotDisplayed() {
        boolean bool = waitUnTillElementIsNotPresent(driver, LegalActPage.NAVIGATION_ELEMENTS_LEFT_SLIDER_PANEL);
        assertTrue(bool, "elements section attached to navigation pane is still displayed");
    }

    @And("legal act content is displayed")
    public void legalActContentIsDisplayed() {
        boolean bool = waitForElementTobeDisPlayed(driver, LegalActPage.LEGAL_ACT_CONTENT);
        assertTrue(bool, "legal act content is not displayed");
    }

    @When("click on actions hamburger icon")
    public void clickOnActionsHamburgerIcon() {
        elementClick(driver, LegalActPage.ACTION_MENU);
    }

    @Then("below options are displayed")
    public void belowOptionsAreDisplayed(DataTable dataTable) {
        String text;
        List<String> actualOptionList = new ArrayList<>();
        List<String> givenOptionList = dataTable.asList(String.class);
        List<WebElement> elements = driver.findElements(LegalActPage.ACTIONS_MENU_BAR_POP_UP);
        assertTrue(null != elements && !elements.isEmpty(), "no element present for action menu bar pop up");
        for (WebElement element : elements) {
            text = element.getText();
            actualOptionList.add(text);
        }
        assertTrue(actualOptionList.containsAll(givenOptionList), "given options are not present in the actual options list");
    }

    @And("{string} option is checked")
    public void optionIsChecked(String arg0) {
        boolean bool = verifyElement(driver, By.xpath(CommonPage.XPATH_TEXT_1 + arg0 + CommonPage.XPATH_TEXT_2 + LegalActPage.NAVIGATION_MENU_ITEM_CHECKED));
        assertTrue(bool, arg0 + " option is not checked");
    }

    @When("click on {string} option")
    public void clickOnOption(String arg0) {
        elementClick(driver, By.xpath(LegalActPage.ACTIONS_SUB_MENU_ITEM + CommonPage.XPATH_TEXT_1 + arg0 + CommonPage.XPATH_TEXT_2));
    }

    @When("click on versions pane accordion")
    public void clickOnVersionsPaneAccordian() {
        elementClick(driver, LegalActPage.VERSIONS_PANE_TOGGLE_LINK);
        E2eUtil.wait(1000);
    }

    @When("click on show more button in recent changes section inside version pane")
    public void clickOnShowMoreButtonInRecentChangesSectionInsideVersionPane() {
        elementClick(driver, LegalActPage.RECENT_CHANGES_SHOW_MORE_BUTTON);
        E2eUtil.wait(500);
    }

    @Then("{int} last technical versions are the imports from office journal")
    public void lastTechnicalVersionsAreTheImportsFromOfficeJournal(int arg0) {
        String text;
        for (int i = 1; i <= arg0; i++) {
            text = getElementText(driver, By.xpath(LegalActPage.RECENT_CHANGES_TEXT + "//ancestor::div[@id='versionCard']//*[@id='versionsBlock']//table//tr[" + i + "]/td//div[contains(@class,'v-label-undef-w')]"));
            assertTrue(text.contains("Import element(s) inserted"), "last " + arg0 + " technical versions are not the imports from office journal");
        }
    }

    @When("click on toggle bar move to left")
    public void clickOnToggleBarMoveToLeft() {
        if (driver.findElements(LegalActPage.TOGGLE_BAR_IN_RIGHT_SIDE).size() > 0) {
            boolean bool = waitForElementTobeDisPlayed(driver, LegalActPage.TOGGLE_BAR_IN_RIGHT_SIDE);
            assertTrue(bool);
            elementClick(driver, LegalActPage.TOGGLE_BAR_IN_RIGHT_SIDE);
        }
    }

    @Then("toggle bar moved to left")
    public void toggleBarMovedToLeft() {
        boolean bool = verifyElement(driver, LegalActPage.TOGGLE_BAR_IN_LEFT_SIDE);
        assertTrue(bool, "toggle bar is not moved to left");
    }

    @Then("{int} recitals are added at the end of the recitals part")
    public void recitalsAreAddedAtTheEndOfTheRecitalsPart(int arg0) {
        WebElement ele = waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        assertNotNull(ele, "unable to load the page in the specified time duration");
        List<WebElement> elementList = driver.findElements(LegalActPage.RECITALS_SOFT_NEW);
        assertEquals(elementList.size(), arg0, arg0 + " recitals are not added at the end of the recitals part");
    }

    @Then("{int} articles are added at the end of the articles part")
    public void articlesAreAddedAtTheEndOfTheArticlesPart(int arg0) {
        WebElement ele = waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        assertNotNull(ele, "unable to load the page in the specified time duration");
        List<WebElement> elementList = driver.findElements(LegalActPage.ARTICLES_SOFT_NEW);
        assertEquals(elementList.size(), arg0, arg0 + " articles are not added at the end of the recitals part");
    }

    @And("mouse hover and click on show all action button and click on edit button of article {int}")
    public void mouseHoverAndClickOnShowAllActionButtonAndClickOnEditButtonOfArticle(int arg0) {
        E2eUtil.wait(3000);
        Actions actions = new Actions(driver);
        WebElement article1 = driver.findElement(By.xpath(LegalActPage.BILL + LegalActPage.ARTICLE + "[" + arg0 + "]"));
        actions.moveToElement(article1).build().perform();
        E2eUtil.wait(2000);
        WebElement showAllMenu = driver.findElement(By.xpath(LegalActPage.BILL + LegalActPage.ARTICLE + "[" + arg0 + "]//following-sibling::div[1][@class='leos-actions Vaadin-Icons']/span[@title='Show all actions']"));
        actions.moveToElement(showAllMenu).build().perform();
        E2eUtil.wait(3000);
        WebElement editButton = driver.findElement(By.xpath(LegalActPage.BILL + LegalActPage.ARTICLE + "[" + arg0 + "]//following-sibling::div[1][@class='leos-actions Vaadin-Icons']/span[@data-widget-type='edit']"));
        actions.moveToElement(editButton).build().perform();
        E2eUtil.wait(2000);
        actions.click().build().perform();
    }

    @When("double click on article {int}")
    public void doubleClickOnArticle(int arg0) {
        E2eUtil.wait(2000);
        doubleClick(driver, By.xpath(LegalActPage.BILL + LegalActPage.ARTICLE + "[" + arg0 + "]"));
    }

    @When("append {string} at the end of the paragraph of article")
    public void appendAtTheEndOfTheParagraphOfArticle(String arg0) {
        String existingText = getElementAttributeInnerText(driver, By.xpath(LegalActPage.BILL + LegalActPage.CK_EDITABLE_INLINE + LegalActPage.ARTICLE + "[1]/ol/li"));
        String newText = existingText + " " + arg0;
        elementEcasSendkeys(driver, By.xpath(LegalActPage.BILL + LegalActPage.CK_EDITABLE_INLINE + LegalActPage.ARTICLE + "[1]/ol/li"), newText);
    }

    @Then("confirm cancel editing window is displayed")
    public void confirmCancelEditingWindowIsDisplayed() {
        boolean bool = verifyElement(driver, LegalActPage.CONFIRM_CANCEL_EDITING);
        assertTrue(bool, "confirm cancel editing window is not displayed");
    }

    @When("click on ok button in confirm cancel editing window")
    public void clickOnOkButtonInConfirmCancelEditingWindow() {
        elementClick(driver, LegalActPage.OK_BUTTON);
        E2eUtil.wait(500);
    }

    @Then("{string} is added with colour {string} to the paragraph {int} of article {int}")
    public void isAddedWithColourToTheParagraphOfArticle(String arg0, String arg1, int arg2, int arg3) {
        String color = "";
        boolean bool = false;
        List<WebElement> elementList = driver.findElements(By.xpath("//bill//article[" + arg3 + "]//paragraph[" + arg2 + "]//aknp//*[@class='leos-content-new']"));
        assertNotNull(elementList);
        for (WebElement element : elementList) {
            String str = getElementAttributeInnerText(element).trim();
            if (str.contains(arg0)) {
                bool = true;
                color = element.getCssValue("color");
                break;
            }
        }
        assertTrue(bool, arg0 + " is not added in the article " + arg3 + " paragraph " + arg2);
        assertEquals(color, arg1, arg0 + " is not added with colour " + arg1);
    }

    @Then("{int} recitals are added in bill content")
    public void recitalsAreAddedInBillContent(int arg0) {
        WebElement ele = waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        assertNotNull(ele, "unable to load the page in the specified time duration");
        List<WebElement> elementList = driver.findElements(LegalActPage.RECITAL_NEW);
        assertEquals(elementList.size(), arg0, arg0 + " recitals are not added at the end of the recitals part");
    }

    @Then("{int} articles are added in bill content")
    public void articlesAreAddedInBillContent(int arg0) {
        WebElement ele = waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        assertNotNull(ele, "unable to load the page in the specified time duration");
        List<WebElement> elementList = driver.findElements(LegalActPage.ARTICLE_NEW);
        assertEquals(elementList.size(), arg0, arg0 + " articles are not added at the end of the recitals part");
    }

    @And("{string} is added to citation {int} in legal act")
    public void isAddedInCitationInECLegalAct(String arg0, int arg1) {
        String text = getElementText(driver, By.xpath(LegalActPage.CITATIONS + LegalActPage.CITATION + "[" + arg1 + "]" + LegalActPage.AKNP));
        assertTrue(text.contains(arg0), arg0 + " is not added to citation " + arg1 + " in legal act");
    }

    @And("{string} is deleted from citation {int} in legal act")
    public void isDeletedFromCitationInECLegalAct(String arg0, int arg1) {
        String text = getElementText(driver, By.xpath(LegalActPage.CITATIONS + LegalActPage.CITATION + "[" + arg1 + "]" + LegalActPage.AKNP));
        assertFalse(text.contains(arg0), arg0 + " is not deleted from citation " + arg1 + " in legal act");
    }

    @And("{string} is added to recital {int} in legal act")
    public void isAddedToRecitalInECLegalAct(String arg0, int arg1) {
        String text = getElementText(driver, By.xpath(LegalActPage.RECITALS + LegalActPage.RECITAL + "[" + arg1 + "]" + LegalActPage.AKNP));
        assertTrue(text.contains(arg0), arg0 + " is not added to recital " + arg1 + " in legal act");
    }

    @And("{string} is deleted from recital {int} in legal act")
    public void isDeletedFromRecitalInECLegalAct(String arg0, int arg1) {
        String text = getElementText(driver, By.xpath(LegalActPage.RECITALS + LegalActPage.RECITAL + "[" + arg1 + "]" + LegalActPage.AKNP));
        assertFalse(text.contains(arg0), arg0 + " is not deleted from recital " + arg1 + " in legal act");
    }

    @And("{string} is added to article {int} paragraph {int} in legal act")
    public void isAddedToArticleParagraphInECLegalAct(String arg0, int arg1, int arg2) {
        String text = getElementText(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg2 + "]" + LegalActPage.AKNP));
        assertTrue(text.contains(arg0), arg0 + " is not added to the article in legal act");
    }

    @And("{string} is deleted from article {int} paragraph {int} in legal act")
    public void isDeletedFromArticleParagraphInECLegalAct(String arg0, int arg1, int arg2) {
        String text = getElementText(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg2 + "]" + LegalActPage.AKNP));
        assertFalse(text.contains(arg0), arg0 + " is not deleted from the article in legal act");
    }

    @When("add {string} and delete {string} in the ck editor of article list {int}")
    public void addAndDeleteInTheCkEditorOfArticleList(String arg0, String arg1, int arg2) {
        String existingText = getElementAttributeInnerText(driver, By.xpath(LegalActPage.CK_EDITABLE_INLINE + LegalActPage.LI + "[" + arg2 + "]"));
        String deleteText = existingText.replace(arg1, "");
        String newText = arg0 + " " + deleteText + " " + arg0 + ".";
        elementEcasSendkeys(driver, By.xpath(LegalActPage.CK_EDITABLE_INLINE + LegalActPage.LI + "[" + arg2 + "]"), newText);
    }

    @When("select content in article {int} paragraph {int}")
    public void selectContentInArticleParagraph(int arg0, int arg1) {
        E2eUtil.wait(2000);
        selectText(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg0 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP));
    }

    @And("total number of article is {int} in enacting terms")
    public void totalNumberOfArticleIsInEnactingTerms(int arg0) {
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.BILL + LegalActPage.AKNBODY + LegalActPage.ARTICLE));
        assertEquals(elementList.size(), arg0, "total number of article is not " + arg0 + " in enacting terms");
    }

    @Then("article {int} is displayed in bill content")
    public void articleIsDisplayedInBillContent(int arg0) {
        boolean bool = verifyElement(driver, By.xpath(LegalActPage.BILL + LegalActPage.AKNBODY + LegalActPage.ARTICLE + "[" + arg0 + "]"));
        assertTrue(bool, "article " + arg0 + " is not displayed in bill content");
    }

    @When("click on insert before icon present in show all actions icon of article {int}")
    public void clickOnInsertBeforeIconPresentInShowAllActionsIconOfArticle(int arg0) {
        Actions actions = new Actions(driver);
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath(LegalActPage.BILL + LegalActPage.AKNBODY + LegalActPage.ARTICLE + "[" + arg0 + "]"));
        if (bool) {
            WebElement article = waitForElementTobePresent(driver, By.xpath(LegalActPage.BILL + LegalActPage.AKNBODY + LegalActPage.ARTICLE + "[" + arg0 + "]"));
            actions.moveToElement(article).build().perform();
        }
        boolean bool1 = waitForElementTobeDisPlayed(driver, By.xpath(LegalActPage.BILL + LegalActPage.AKNBODY + LegalActPage.ARTICLE + "[" + arg0 + "]" + "//following-sibling::div[1][@class='leos-actions Vaadin-Icons']/span[@title='Show all actions']"));
        if (bool1) {
            WebElement showAllActionsIcon = waitForElementTobePresent(driver, By.xpath(LegalActPage.BILL + LegalActPage.AKNBODY + LegalActPage.ARTICLE + "[" + arg0 + "]" + "//following-sibling::div[1][@class='leos-actions Vaadin-Icons']/span[@title='Show all actions']"));
            actions.moveToElement(showAllActionsIcon).build().perform();
        }
        boolean bool2 = waitForElementTobeDisPlayed(driver, LegalActPage.SHOW_ALL_ACTIONS_INSERT_BEFORE);
        if (bool2) {
            WebElement insertBefore = waitForElementTobePresent(driver, LegalActPage.SHOW_ALL_ACTIONS_INSERT_BEFORE);
            actions.moveToElement(insertBefore).build().perform();
            actions.click().release().build().perform();
        }
    }

    @When("click on insert after icon present in show all actions icon of article {int}")
    public void clickOnInsertAfterIconPresentInShowAllActionsIconOfArticle(int arg0) {
        E2eUtil.wait(2000);
        Actions actions = new Actions(driver);
        WebElement article = driver.findElement(By.xpath(LegalActPage.BILL + LegalActPage.AKNBODY + LegalActPage.ARTICLE + "[" + arg0 + "]"));
        actions.moveToElement(article).build().perform();
        E2eUtil.wait(2000);
        WebElement showAllActionsIcon = driver.findElement(By.xpath(LegalActPage.BILL + LegalActPage.AKNBODY + LegalActPage.ARTICLE + "[" + arg0 + "]" + "//following-sibling::div[1][@class='leos-actions Vaadin-Icons']/span[@title='Show all actions']"));
        actions.moveToElement(showAllActionsIcon).build().perform();
        E2eUtil.wait(3000);
        WebElement insertBefore = driver.findElement(LegalActPage.SHOW_ALL_ACTIONS_INSERT_AFTER);
        actions.moveToElement(insertBefore).build().perform();
        actions.click().build().perform();
        E2eUtil.wait(2000);
    }

    @When("click on edit icon present in show all actions icon of article {int}")
    public void clickOnEditIconPresentInShowAllActionsIconOfArticle(int arg0) {
        E2eUtil.wait(2000);
        Actions actions = new Actions(driver);
        WebElement article = driver.findElement(By.xpath(LegalActPage.BILL + LegalActPage.AKNBODY + LegalActPage.ARTICLE + "[" + arg0 + "]"));
        actions.moveToElement(article).build().perform();
        E2eUtil.wait(2000);
        WebElement showAllActionsIcon = driver.findElement(By.xpath(LegalActPage.BILL + LegalActPage.AKNBODY + LegalActPage.ARTICLE + "[" + arg0 + "]" + "//following-sibling::div[1][@class='leos-actions Vaadin-Icons']/span[@title='Show all actions']"));
        actions.moveToElement(showAllActionsIcon).build().perform();
        E2eUtil.wait(3000);
        WebElement insertBefore = driver.findElement(LegalActPage.SHOW_ALL_ACTIONS_EDIT);
        actions.moveToElement(insertBefore).build().perform();
        actions.click().build().perform();
        E2eUtil.wait(2000);
    }

    @When("click on delete icon present in show all actions icon of article {int}")
    public void clickOnDeleteIconPresentInShowAllActionsIconOfArticle(int arg0) {
        Actions actions = new Actions(driver);
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath(LegalActPage.BILL + LegalActPage.AKNBODY + LegalActPage.ARTICLE + "[" + arg0 + "]"));
        if (bool) {
            WebElement article = waitForElementTobePresent(driver, By.xpath(LegalActPage.BILL + LegalActPage.AKNBODY + LegalActPage.ARTICLE + "[" + arg0 + "]"));
            actions.moveToElement(article).build().perform();
        }
        boolean bool1 = waitForElementTobeDisPlayed(driver, By.xpath(LegalActPage.BILL + LegalActPage.AKNBODY + LegalActPage.ARTICLE + "[" + arg0 + "]" + "//following-sibling::div[1][@class='leos-actions Vaadin-Icons']/span[@title='Show all actions']"));
        if (bool1) {
            WebElement showAllActionsIcon = waitForElementTobePresent(driver, By.xpath(LegalActPage.BILL + LegalActPage.AKNBODY + LegalActPage.ARTICLE + "[" + arg0 + "]" + "//following-sibling::div[1][@class='leos-actions Vaadin-Icons']/span[@title='Show all actions']"));
            actions.moveToElement(showAllActionsIcon).build().perform();
        }
        boolean bool2 = waitForElementTobeDisPlayed(driver, LegalActPage.SHOW_ALL_ACTIONS_DELETE);
        if (bool2) {
            WebElement delete = waitForElementTobePresent(driver, LegalActPage.SHOW_ALL_ACTIONS_DELETE);
            actions.moveToElement(delete).build().perform();
            actions.click().release().build().perform();
        }
    }

    @When("add {string} to CK EDITOR of article paragraph {int}")
    public void addInArticleParagraph(String arg0, int arg1) {
        By xPath = By.xpath(LegalActPage.CK_EDITABLE_INLINE + LegalActPage.ARTICLE + LegalActPage.LI + "[" + arg1 + "]");
        String text = getElementAttributeInnerText(driver, xPath);
        String newText = text + " " + arg0;
        elementEcasSendkeys(driver, xPath, newText);
    }

    @And("{string} is added to article {int} paragraph {int}")
    public void isAddedToArticleParagraph(String arg0, int arg1, int arg2) {
        String text = getElementText(driver, By.xpath(LegalActPage.BILL + LegalActPage.AKNBODY + LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg2 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP));
        assertTrue(text.contains(arg0), arg0 + " is not added to article " + arg1 + " paragraph " + arg2);
    }

    @When("remove {string} from CK EDITOR of article paragraph {int}")
    public void removeFromTheContentOfArticleParagraph(String arg0, int arg1) {
        By xPath = By.xpath(LegalActPage.CK_EDITABLE_INLINE + LegalActPage.ARTICLE + LegalActPage.LI + "[" + arg1 + "]");
        String text = getElementAttributeInnerText(driver, xPath);
        String deletedText = text.replace(arg0, "");
        elementEcasSendkeys(driver, xPath, deletedText);
    }

    @And("{string} is removed from article {int} paragraph {int}")
    public void isRemovedFromArticleParagraph(String arg0, int arg1, int arg2) {
        String text = getElementText(driver, By.xpath(LegalActPage.BILL + LegalActPage.AKNBODY + LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg2 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP));
        assertFalse(text.contains(arg0), arg0 + " is not removed from article " + arg1 + " paragraph " + arg2);
    }

    @When("append text {string} to the heading of the element in selected element section")
    public void appendTextToTheHeadingOfTheElementInSelectedElementSection(String arg0) {
        String text = getElementAttributeValue(driver, LegalActPage.SELECTED_ELEMENT_HEADING_INPUT);
        elementEcasSendkeys(driver, LegalActPage.SELECTED_ELEMENT_HEADING_INPUT, text + arg0);
    }

    @And("click on save button in navigation pane")
    public void clickOnSaveButtonInNavigationPane() {
        elementClick(driver, AnnexPage.TOC_SAVE_BUTTON);
    }

    @Then("heading of article {int} contains {string}")
    public void headingOfArticleContains(int arg0, String arg1) {
        String text = getElementText(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg0 + "]" + LegalActPage.HEADING));
        assertTrue(text.contains(arg1), "heading of article " + arg0 + " doesn't contain " + arg1);
    }

    @When("remove text {string} from the heading of the element in selected element section")
    public void removeTextFromTheHeadingOfTheElementInSelectedElementSection(String arg0) {
        String text = getElementAttributeValue(driver, LegalActPage.SELECTED_ELEMENT_HEADING_INPUT);
        String deletedText = text.replace(arg0, "");
        elementEcasSendkeys(driver, LegalActPage.SELECTED_ELEMENT_HEADING_INPUT, deletedText);
    }

    @Then("heading of article {int} doesn't contain {string}")
    public void headingOfArticleDoesntContain(int arg0, String arg1) {
        String text = getElementText(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg0 + "]" + LegalActPage.HEADING));
        assertFalse(text.contains(arg1), "heading of article " + arg0 + " contains " + arg1);
    }

    @And("click on element {string}")
    public void clickOnElement(String arg0) {
        elementClick(driver, By.xpath(CommonPage.XPATH_TEXT_1 + arg0 + CommonPage.XPATH_TEXT_2));
    }

    @And("heading of Article {int} is {string}")
    public void headingOfArticleIs(int arg0, String arg1) {
        By articleHeading = By.xpath(LegalActPage.ARTICLE + "[" + arg0 + "]" + LegalActPage.HEADING);
        scrollTo(driver, articleHeading);
        String text = getElementText(driver, articleHeading);
        assertEquals(text, arg1, "heading of Article " + arg0 + " is not " + arg1);
    }

    @When("click on recital toggle link")
    public void clickOnRecitalToggleLink() {
        elementClick(driver, LegalActPage.RECITALS_TOGGLE_LINK);
    }

    @When("click on element {int} of recital in legal act page")
    public void clickElementOfRecital(int arg0) {
        scrollandClick(driver, By.xpath(LegalActPage.TOC_RECITAL_LINK));
        E2eUtil.wait(2000);
        elementClick(driver, By.xpath(LegalActPage.TOC_RECITAL_LINK + LegalActPage.ANCESTOR + "::tr" + LegalActPage.FOLLOWING_SIBLING + "::tr[" + arg0 + "]" + LegalActPage.GWT_HTML_CLASS));
        E2eUtil.wait(2000);
    }

    @When("add {string} at the beginning of the ck editor text box")
    public void addAtTheBeginningOfTheCkEditorTextBox(String arg0) {
        String existingText = getElementAttributeInnerText(driver, LegalActPage.CK_EDTOR_PARAGRAPH_INNERTEXT);
        String newText = arg0 + existingText;
        elementEcasSendkeys(driver, LegalActPage.CK_EDTOR_PARAGRAPH_INNERTEXT, newText);
    }

    @When("add {string} at the end of the ck editor text box")
    public void addAtTheEndOfTheCkEditorTextBox(String arg0) {
        String existingText = getElementAttributeInnerText(driver, LegalActPage.CK_EDTOR_PARAGRAPH_INNERTEXT);
        String newText = existingText + arg0;
        elementEcasSendkeys(driver, LegalActPage.CK_EDTOR_PARAGRAPH_INNERTEXT, newText);
    }

    @When("remove below words from the ck editor text box")
    public void removeBelowWordsFromTheCkEditorTextBox(DataTable dataTable) {
        List<String> strList = dataTable.asList(String.class);
        String textToDelete = getElementAttributeInnerText(driver, LegalActPage.CK_EDTOR_PARAGRAPH_INNERTEXT);
        for (String str : strList) {
            textToDelete = textToDelete.replace(str, "");
        }
        elementEcasSendkeys(driver, LegalActPage.CK_EDTOR_PARAGRAPH_INNERTEXT, textToDelete);
    }

    @And("{string} is added to the recital {int}")
    public void isAddedToTheBeginningOfRecital(String arg0, int arg1) {
        String text = getElementAttributeInnerText(driver, By.xpath(LegalActPage.RECITALS + LegalActPage.RECITAL + "[" + arg1 + "]" + LegalActPage.AKNP));
        assertTrue(text.contains(arg0), arg0 + " is not added to recital " + arg1 + " in legal act");
    }

    @And("below words are showing as grey and strikethrough in recital {int}")
    public void belowWordsAreShowingAsGreyAndStrikethroughInRecital(int arg0, DataTable dataTable) {
        List<String> strList = dataTable.asList(String.class);
        List<String> deletedStrList = new ArrayList<>();
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.RECITALS + LegalActPage.RECITAL + "[" + arg0 + "]" + LegalActPage.AKNP + LegalActPage.SPAN + LegalActPage.LEOS_CONTENT_SOFT_REMOVED));
        assertNotNull(elementList);
        for (WebElement ele : elementList) {
            deletedStrList.add(getElementAttributeInnerText(ele).trim());
        }
        assertTrue(deletedStrList.containsAll(strList), "Given Options are not showing as grey or strikethrough in recital " + arg0);
    }

    @Then("compare versions button is displayed in versions pane section")
    public void compareVersionsButtonIsDisplayedInVersionsPaneSection() {
        WebElement ele = waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        assertNotNull(ele, "unable to load the page in the specified time duration");
        boolean bool = verifyElement(driver, LegalActPage.COMPARE_VERSIONS_BUTTON);
        assertTrue(bool, "compare versions button is not displayed in versions pane section");
    }

    @And("search button is displayed in versions pane section")
    public void searchButtonIsDisplayedInVersionsPaneSection() {
        WebElement ele = waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        assertNotNull(ele, "unable to load the page in the specified time duration");
        boolean bool = verifyElement(driver, LegalActPage.SEARCH_BUTTON);
        assertTrue(bool, "search button is not displayed in versions pane section");
    }

    @When("click on compare versions button present in versions pane section")
    public void clickOnCompareVersionsButtonPresentInVersionsPaneSection() {
        elementClick(driver, LegalActPage.COMPARE_VERSIONS_BUTTON);
    }

    @Then("show less button is showing in recent changes section inside version pane")
    public void showLessButtonIsShowingInRecentChangesSectionInsideVersionPane() {
        boolean bool = waitForElementTobeDisPlayed(driver, LegalActPage.SHOW_LESS_BUTTON);
        assertTrue(bool, "show less button is not showing in recent changes section inside version pane");
    }

    @When("tick on checkbox of milestone version {string}")
    public void tickOnCheckboxOfMileStoneVersion(String arg0) {
        By by = By.xpath(LegalActPage.VERSION_CARD + CommonPage.XPATH_CONTAINS_TEXT_1 + arg0 + CommonPage.XPATH_CONTAINS_TEXT_2 + LegalActPage.VERSION_CARD_CHECKBOX);
        scrollTo(driver, by);
        elementClickJS(driver, waitForElementTobePresent(driver, by));
    }

    @When("tick on checkbox of version {string} in recent changes")
    public void tickOnCheckboxOfVersionRecentChanges(String arg0) {
        int iterationNumber;
        int len = 0;
        boolean flag = false;
        By by = By.xpath(LegalActPage.VERSION_CARD + CommonPage.XPATH_CONTAINS_TEXT_1 + arg0 + CommonPage.XPATH_CONTAINS_TEXT_2 + LegalActPage.COMPONENT_WRAP_CHECKBOX);
        WebElement innerScrollElement = driver.findElement(By.cssSelector(".v-grid-scroller.v-grid-scroller-vertical"));
        WebElement outerScrollElement = driver.findElement(By.cssSelector(".v-verticallayout-versions-cards-holder-scroller"));
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        long outerScrollHeight = (long) executor.executeScript("return arguments[0].scrollHeight;", outerScrollElement);
        executor.executeScript("arguments[0].scrollTop=-arguments[1];", outerScrollElement, outerScrollHeight);
        long innerScrollHeight = (long) executor.executeScript("return arguments[0].scrollHeight;", innerScrollElement);
        long innerOffSetHeight = (long) executor.executeScript("return arguments[0].offsetHeight;", innerScrollElement);
        executor.executeScript("arguments[0].scrollTop=-arguments[1];", innerScrollElement, innerScrollHeight);
        boolean bool = waitElementToBeDisplayedWithInSpecifiedTime(driver, by, 10);
        if (!bool) {
            if (innerOffSetHeight != 0) {
                iterationNumber = (int) (innerScrollHeight / innerOffSetHeight);
                for (int i = 0; i <= iterationNumber; i++) {
                    len = len + (int) innerOffSetHeight;
                    executor.executeScript("arguments[0].scrollTop=arguments[1];", innerScrollElement, len);
                    bool = waitElementToBeDisplayedWithInSpecifiedTime(driver, by, 10);
                    if (bool) {
                        scrollandClick(driver, by);
                        flag = true;
                        break;
                    }
                }
                executor.executeScript("arguments[0].scrollTop=-arguments[1];", innerScrollElement, innerScrollHeight);
            }
        } else {
            scrollandClick(driver, by);
            flag = true;
        }
        assertTrue(flag, "version " + arg0 + " in recent changes section is not ticked");
    }

    @When("tick on checkbox of minor version {string} in major version {string}")
    public void tickOnCheckboxOfVersion(String arg0, String arg1) {
        int iterationNumber;
        int len = 0;
        boolean flag = false;
        By by = By.xpath(LegalActPage.VERSION_CARD + CommonPage.XPATH_CONTAINS_TEXT_1 + arg0 + CommonPage.XPATH_CONTAINS_TEXT_2 + LegalActPage.COMPONENT_WRAP_CHECKBOX);
        WebElement innerScrollElement = driver.findElement(By.xpath("//*[text()='" + arg1 + "']//ancestor::div[@id='versionCard']" + LegalActPage.V_GRID_SCROLLER));
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        long innerScrollHeight = (long) executor.executeScript("return arguments[0].scrollHeight;", innerScrollElement);
        long innerOffSetHeight = (long) executor.executeScript("return arguments[0].offsetHeight;", innerScrollElement);
        executor.executeScript("arguments[0].scrollTop=-arguments[1];", innerScrollElement, innerScrollHeight);
        boolean bool = waitElementToBeDisplayedWithInSpecifiedTime(driver, by, 10);
        if (!bool) {
            if (innerOffSetHeight != 0) {
                iterationNumber = (int) (innerScrollHeight / innerOffSetHeight);
                for (int i = 0; i <= iterationNumber; i++) {
                    len = len + (int) innerOffSetHeight;
                    executor.executeScript("arguments[0].scrollTop=arguments[1];", innerScrollElement, len);
                    bool = waitElementToBeDisplayedWithInSpecifiedTime(driver, by, 10);
                    if (bool) {
                        scrollandClick(driver, by);
                        flag = true;
                        break;
                    }
                }
                executor.executeScript("arguments[0].scrollTop=-arguments[1];", innerScrollElement, innerScrollHeight);
            }
        } else {
            scrollandClick(driver, by);
            flag = true;
        }
        assertTrue(flag, "version " + arg0 + " in recent changes section is not ticked");
    }

    @Then("{string} is underlined in the bill title in comparision page")
    public void isUnderlinedInTheBillTitleInComparisionPage(String arg0) {
        String text = getElementText(driver, By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + LegalActPage.BILL + LegalActPage.PREFACE + LegalActPage.DOC_PURPOSE + LegalActPage.SPAN + LegalActPage.LEOS_CONTENT_NEW_CN));
        assertTrue(text.contains(arg0), arg0 + "is not underlined in the bill title in comparision page");
    }

    @When("uncheck version {string} in recent changes section")
    public void untickOnCheckboxOfVersion(String arg0) {
        int iterationNumber;
        int len = 0;
        boolean flag = false;
        By by = By.xpath(LegalActPage.VERSION_CARD + CommonPage.XPATH_CONTAINS_TEXT_1 + arg0 + CommonPage.XPATH_CONTAINS_TEXT_2 + LegalActPage.COMPONENT_WRAP_CHECKBOX);
        WebElement innerScrollElement = driver.findElement(By.cssSelector(".v-grid-scroller.v-grid-scroller-vertical"));
        WebElement outerScrollElement = driver.findElement(By.cssSelector(".v-verticallayout-versions-cards-holder-scroller"));
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        long outerScrollHeight = (long) executor.executeScript("return arguments[0].scrollHeight;", outerScrollElement);
        executor.executeScript("arguments[0].scrollTop=-arguments[1];", outerScrollElement, outerScrollHeight);
        long innerScrollHeight = (long) executor.executeScript("return arguments[0].scrollHeight;", innerScrollElement);
        long innerOffSetHeight = (long) executor.executeScript("return arguments[0].offsetHeight;", innerScrollElement);
        executor.executeScript("arguments[0].scrollTop=-arguments[1];", innerScrollElement, innerScrollHeight);
        boolean bool = waitElementToBeDisplayedWithInSpecifiedTime(driver, by, 10);
        if (!bool) {
            if (innerOffSetHeight != 0) {
                iterationNumber = (int) (innerScrollHeight / innerOffSetHeight);
                for (int i = 0; i <= iterationNumber; i++) {
                    len = len + (int) innerOffSetHeight;
                    executor.executeScript("arguments[0].scrollTop=arguments[1];", innerScrollElement, len);
                    bool = waitElementToBeDisplayedWithInSpecifiedTime(driver, by, 10);
                    if (bool) {
                        scrollandClick(driver, by);
                        flag = true;
                        break;
                    }
                }
                executor.executeScript("arguments[0].scrollTop=-arguments[1];", innerScrollElement, innerScrollHeight);
            }
        } else {
            scrollandClick(driver, by);
            flag = true;
        }
        assertTrue(flag, "version " + arg0 + " in recent changes section is ticked");
    }

    @And("{string} is showing as bold and underlined in recital {int} in comparision page")
    public void isShowingAsBoldAndUnderlinedAtInRecitalInComparisionPage(String arg0, int arg1) {
        List<String> strList = new ArrayList<>();
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + LegalActPage.RECITAL + "[" + arg1 + "]" + LegalActPage.SPAN + LegalActPage.LEOS_CONTENT_NEW_CN));
        for (WebElement ele : elementList) {
            strList.add(getElementAttributeInnerText(ele).trim());
        }
        assertTrue(strList.contains(arg0), arg0 + " is showing neither bold nor underlined in recital " + arg1 + " in comparision page");
    }

    @And("below words are showing as bold, strikethrough and underlined in recital {int} in comparision page")
    public void belowWordsAreShowingAsBoldStrikethroughAndUnderlinedInRecitalInComparisionPage(int arg0, DataTable dataTable) {
        List<String> actualStrList = new ArrayList<>();
        List<String> giveStrList = dataTable.asList(String.class);
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + LegalActPage.RECITAL + "[" + arg0 + "]" + LegalActPage.SPAN + LegalActPage.LEOS_CONTENT_REMOVED_CN));
        assertNotNull(elementList);
        for (WebElement ele : elementList) {
            actualStrList.add(getElementAttributeInnerText(ele).trim());
        }
        assertTrue(actualStrList.containsAll(giveStrList), "Given options are not present in the actual removed elements list");
    }

    @When("click on close button present in comparision page")
    public void clickOnCloseButtonPresentInComparisionPage() {
        elementClick(driver, By.xpath("(" + LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.LEOS_TOOLBAR_BUTTON + ")[7]"));
    }

    @Then("comparision page is not displayed")
    public void comparisionPageIsNotDisplayedInLegalActPage() {
        boolean bool = waitUnTillElementIsNotPresent(driver, By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR));
        assertTrue(bool, "comparision page is displayed in legal act page");
    }

    @When("uncheck version {string} in milestone version")
    public void untickOnCheckboxOfVersionInMilestoneVersion(String arg0) {
        elementClick(driver, By.xpath(LegalActPage.VERSION_CARD + CommonPage.XPATH_CONTAINS_TEXT_1 + arg0 + CommonPage.XPATH_CONTAINS_TEXT_2 + LegalActPage.VERSION_CARD_CHECKBOX));
    }

    @And("{string} is showing as bold in recital {int} in double diff comparision page")
    public void isShowingAsBoldInRecitalInDoubleDiffComparisionPage(String arg0, int arg1) {
        List<String> strList = new ArrayList<>();
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + LegalActPage.RECITAL + "[" + arg1 + "]" + LegalActPage.SPAN + LegalActPage.LEOS_DOUBLE_COMPARE_ADDED_ORIGINAL));
        for (WebElement ele : elementList) {
            strList.add(getElementAttributeInnerText(ele).trim());
        }
        assertTrue(strList.contains(arg0), arg0 + " is not showing as bold in recital " + arg1 + " in double diff comparision page");
    }

    @And("{string} is showing as bold and underlined in recital {int} in double diff comparision page")
    public void isShowingAsBoldAndUnderlinedInRecitalInDoubleDiffComparisionPage(String arg0, int arg1) {
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + LegalActPage.RECITAL + "[" + arg1 + "]" + LegalActPage.SPAN + LegalActPage.LEOS_DOUBLE_COMPARE_ADDED_INTERMEDIATE));
        List<String> strList = new ArrayList<>();
        for (WebElement ele : elementList) {
            strList.add(getElementAttributeInnerText(ele).trim());
        }
        assertTrue(strList.contains(arg0), arg0 + " is neither showing as bold nor underlined in recital " + arg1 + " in double diff comparision page");
    }

    @And("below words are showing as bold, strikethrough in recital {int} in double diff comparision page")
    public void belowWordsAreShowingAsBoldStrikethroughInRecitalInDoubleDiffComparisionPage(int arg0, DataTable dataTable) {
        List<String> giveStrList = dataTable.asList(String.class);
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + LegalActPage.RECITAL + "[" + arg0 + "]" + LegalActPage.SPAN + LegalActPage.LEOS_DOUBLE_COMPARE_REMOVED_ORIGINAL));
        List<String> actualStrList = new ArrayList<>();
        for (WebElement ele : elementList) {
            actualStrList.add(getElementAttributeInnerText(ele).trim());
        }
        assertTrue(actualStrList.containsAll(giveStrList), "Given options are not present in the actual removed elements list");
    }

    @And("below words are showing as bold, strikethrough and underlined in recital {int} in double diff comparision page")
    public void belowWordsAreShowingAsBoldStrikethroughAndUnderlinedInRecitalInDoubleDiffComparisionPage(int arg0, DataTable dataTable) {
        List<String> giveStrList = dataTable.asList(String.class);
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + LegalActPage.RECITAL + "[" + arg0 + "]" + LegalActPage.SPAN + LegalActPage.LEOS_DOUBLE_COMPARE_REMOVED_INTERMEDIATE));
        List<String> actualStrList = new ArrayList<>();
        for (WebElement ele : elementList) {
            actualStrList.add(getElementAttributeInnerText(ele).trim());
        }
        assertTrue(actualStrList.containsAll(giveStrList), "Given options are not present in the actual removed elements list");
    }

    @When("click on navigation pane toggle link")
    public void clickOnToggleLinkToOpenTheNavigationPane() {
        WebElement ele = waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        assertNotNull(ele);
        elementClick(driver, LegalActPage.NAVIGATION_PANE_TOGGLE_LINK);
    }

    @And("{string} version present in recent changes section is unchecked")
    public void versionPresentInRecentChangesSectionIsUnticked(String arg0) {
        WebElement ele = waitForElementTobePresent(driver, By.xpath(LegalActPage.VERSION_CARD + CommonPage.XPATH_CONTAINS_TEXT_1 + arg0 + CommonPage.XPATH_CONTAINS_TEXT_2 + LegalActPage.COMPONENT_WRAP_CHECKBOX));
        assertNotNull(ele);
        assertFalse(ele.isSelected(), arg0 + " version present in recent changes section is checked");
    }

    @And("{string} version present in recent changes section is checked")
    public void versionPresentInRecentChangesSectionIsTicked(String arg0) {
        WebElement ele = waitForElementTobePresent(driver, By.xpath(LegalActPage.VERSION_CARD + CommonPage.XPATH_CONTAINS_TEXT_1 + arg0 + CommonPage.XPATH_CONTAINS_TEXT_2 + LegalActPage.COMPONENT_WRAP_CHECKBOX));
        assertNotNull(ele);
        assertTrue(ele.isSelected(), arg0 + " version present in recent changes section is unchecked");
    }

    @When("replace content {string} with the existing content in ck editor text box")
    public void replaceContentWithTheExistingContentInCkEditorTextBox(String arg0) {
        elementEcasSendkeys(driver, LegalActPage.CK_EDTOR_PARAGRAPH_INNERTEXT, arg0);
        E2eUtil.wait(2000);
    }

    @And("content of recital {int} is greyed and strikethrough i.e. soft deleted")
    public void contentOfRecitalIsGreyedAndStrikethrough(int arg0) {
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath(LegalActPage.RECITALS + LegalActPage.RECITAL + "[" + arg0 + "]" + LegalActPage.LEOS_CONTENT_SOFT_REMOVED));
        assertTrue(bool, "content of recital " + arg0 + " is not soft deleted");
    }

    @And("content of recital {int} is bold, underlined and strikethrough in double diff comparision page")
    public void contentOfRecitalIsBoldUnderlinedAndStrikethroughInDoubleDiffComparisionPage(int arg0) {
        WebElement element = waitForElementTobePresent(driver, By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + LegalActPage.RECITAL + "[" + arg0 + "]" + LegalActPage.LEOS_DOUBLE_COMPARE_REMOVED_INTERMEDIATE));
        assertNotNull(element, "content of recital " + arg0 + " is not bold, underlined and strikethrough in double diff comparison page");
    }

    @And("there is only {int} addition\\(s) in single diff comparision page")
    public void thereIsAreOnlyChangeSInSingleDiffComparisionPage(int arg0) {
        List<WebElement> docPurposeElementList = getElementListForComparision(driver, By.xpath(LegalActPage.DOC_PURPOSE + LegalActPage.SPAN + LegalActPage.LEOS_CONTENT_NEW_CN), 10);
        List<WebElement> elementList = getElementListForComparision(driver, By.xpath(LegalActPage.RECITAL + LegalActPage.NOT_LEOS_CONTENT_NEW_CN + LegalActPage.SPAN + LegalActPage.LEOS_CONTENT_NEW_CN), 10);
        List<WebElement> elementAKNPList = getElementListForComparision(driver, By.xpath(LegalActPage.AKNP + LegalActPage.LEOS_CONTENT_NEW_CN), 10);
        assertEquals(docPurposeElementList.size() + elementList.size() + elementAKNPList.size(), arg0, "either there are no changes or multiple additions in single diff comparision page");
    }

    @And("there is only {int} deletion\\(s) in single diff comparision page")
    public void thereIsOnlyDeletionSInSingleDiffComparisionPage(int arg0) {
        List<WebElement> elementList = getElementListForComparision(driver, By.xpath(LegalActPage.SPAN + LegalActPage.LEOS_CONTENT_REMOVED_CN), 10);
        List<WebElement> elementAKNPList = getElementListForComparision(driver, By.xpath(LegalActPage.AKNP + LegalActPage.LEOS_CONTENT_REMOVED_CN), 10);
        assertEquals(elementList.size() + elementAKNPList.size(), arg0, "either there are no changes or multiple deletions in single diff comparision page");
    }

    @And("there is only {int} addition\\(s) in double diff comparision page")
    public void thereIsOnlyAdditionSInDoubleDiffComparisionPage(int arg0) {
        List<WebElement> interMediateAddedElementList = getElementListForComparision(driver, By.xpath(LegalActPage.RECITAL + LegalActPage.NOT_LEOS_DOUBLE_COMPARE_ADDED_INTERMEDIATE + LegalActPage.SPAN + LegalActPage.LEOS_DOUBLE_COMPARE_ADDED_INTERMEDIATE), 10);
        List<WebElement> originalAddedElementList = getElementListForComparision(driver, By.xpath(LegalActPage.SPAN + LegalActPage.LEOS_DOUBLE_COMPARE_ADDED_ORIGINAL), 10);
        List<WebElement> interMediateAKNPAddedElementList = getElementListForComparision(driver, By.xpath(LegalActPage.AKNP + LegalActPage.LEOS_DOUBLE_COMPARE_ADDED_INTERMEDIATE), 10);
        assertEquals(interMediateAddedElementList.size() + originalAddedElementList.size() + interMediateAKNPAddedElementList.size(), arg0, "either there are no changes or multiple Additions in double diff comparision page");
    }

    @And("there is only {int} deletion\\(s) in double diff comparision page")
    public void thereIsOnlyDeletionSInDoubleDiffComparisionPage(int arg0) {
        List<WebElement> interMediateRemovedElementList = getElementListForComparision(driver, By.xpath(LegalActPage.SPAN + LegalActPage.LEOS_DOUBLE_COMPARE_REMOVED_INTERMEDIATE), 10);
        List<WebElement> interMediateAKNPRemovedElementList = getElementListForComparision(driver, By.xpath(LegalActPage.AKNP + LegalActPage.LEOS_DOUBLE_COMPARE_REMOVED_INTERMEDIATE), 10);
        List<WebElement> originalRemovedElementList = getElementListForComparision(driver, By.xpath(LegalActPage.SPAN + LegalActPage.LEOS_DOUBLE_COMPARE_REMOVED_ORIGINAL), 10);
        assertEquals(interMediateRemovedElementList.size() + originalRemovedElementList.size() + interMediateAKNPRemovedElementList.size(), arg0, "either there are no changes or multiple deletions in double diff comparision page");
    }

    @Then("{string} is added in the bill title in double diff comparision page")
    public void isBoldInTheBillTitleInDoubleDiffComparisionPage(String arg0) {
        boolean bool = verifyElement(driver, By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + LegalActPage.BILL + LegalActPage.PREFACE + LegalActPage.DOC_PURPOSE + LegalActPage.SPAN + LegalActPage.LEOS_DOUBLE_COMPARE_ADDED_ORIGINAL));
        assertTrue(bool, arg0 + "is not added in double diff comparision page");
    }

    @And("content of recital {int} is bold, underlined and strikethrough in single diff comparision page")
    public void contentOfRecitalIsBoldUnderlinedAndStrikethroughInSingleDiffComparisionPage(int arg0) {
        WebElement element = waitForElementTobePresent(driver, By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + LegalActPage.RECITAL + "[" + arg0 + "]" + LegalActPage.LEOS_CONTENT_REMOVED_CN));
        assertNotNull(element, "content of recital " + arg0 + " is not bold, underlined and strikethrough in single diff comparision page");
    }

    @When("click on insert before icon present in show all actions icon of recital {int}")
    public void clickOnInsertBeforeIconPresentInShowAllActionsIconOfRecital(int arg0) {
        E2eUtil.wait(2000);
        Actions actions = new Actions(driver);
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath(LegalActPage.BILL + LegalActPage.RECITALS + LegalActPage.RECITAL + "[" + arg0 + "]"));
        if (bool) {
            WebElement article = waitForElementTobePresent(driver, By.xpath(LegalActPage.BILL + LegalActPage.RECITALS + LegalActPage.RECITAL + "[" + arg0 + "]"));
            actions.moveToElement(article).build().perform();
            E2eUtil.wait(2000);
        }
        boolean bool1 = waitForElementTobeDisPlayed(driver, By.xpath(LegalActPage.BILL + LegalActPage.RECITALS + LegalActPage.RECITAL + "[" + arg0 + "]" + "//following-sibling::div[1][@class='leos-actions Vaadin-Icons']/span[@title='Show all actions']"));
        if (bool1) {
            WebElement showAllActionsIcon = waitForElementTobePresent(driver, By.xpath(LegalActPage.BILL + LegalActPage.RECITALS + LegalActPage.RECITAL + "[" + arg0 + "]" + "//following-sibling::div[1][@class='leos-actions Vaadin-Icons']/span[@title='Show all actions']"));
            actions.moveToElement(showAllActionsIcon).build().perform();
            E2eUtil.wait(2000);
        }
        boolean bool2 = waitForElementTobeDisPlayed(driver, LegalActPage.SHOW_ALL_ACTIONS_INSERT_BEFORE);
        if (bool2) {
            WebElement insertBefore = waitForElementTobePresent(driver, LegalActPage.SHOW_ALL_ACTIONS_INSERT_BEFORE);
            actions.moveToElement(insertBefore).build().perform();
            actions.click().release().build().perform();
            E2eUtil.wait(2000);
        }
    }

    @When("click on insert after icon present in show all actions icon of recital {int}")
    public void clickOnInsertAfterIconPresentInShowAllActionsIconOfRecital(int arg0) {
        E2eUtil.wait(2000);
        Actions actions = new Actions(driver);
        WebElement article = driver.findElement(By.xpath(LegalActPage.BILL + LegalActPage.RECITALS + LegalActPage.RECITAL + "[" + arg0 + "]"));
        scrollToElement(driver, article);
        actions.moveToElement(article).build().perform();
        E2eUtil.wait(2000);
        WebElement showAllActionsIcon = driver.findElement(By.xpath(LegalActPage.BILL + LegalActPage.RECITALS + LegalActPage.RECITAL + "[" + arg0 + "]" + "//following-sibling::div[1][@class='leos-actions Vaadin-Icons']/span[@title='Show all actions']"));
        scrollToElement(driver, showAllActionsIcon);
        actions.moveToElement(showAllActionsIcon).build().perform();
        E2eUtil.wait(3000);
        WebElement insertBefore = driver.findElement(LegalActPage.SHOW_ALL_ACTIONS_INSERT_AFTER);
        actions.moveToElement(insertBefore).build().perform();
        actions.click().build().perform();
        E2eUtil.wait(2000);
    }

    @Then("recital {string} is added to the bill in legal act page")
    public void recitalIsAddedToTheBillInLegalActPage(String arg0) {
        assertTrue(verifyElement(driver, By.xpath("//num[contains(text(),'" + arg0 + "')]//parent::recital")), "recital " + arg0 + " is added to the bill in legal act page");
    }

    @Then("content of recital {string} is showing bold in legal act live page")
    public void contentOfRecitalIsShowingBoldInLegalActLivePage(String arg0) {
        WebElement ele = waitForElementTobePresent(driver, By.xpath("//num[contains(text(),'" + arg0 + "')]//parent::recital" + LegalActPage.LEOS_CONTENT_SOFT_NEW));
        assertNotNull(ele, "recital " + arg0 + " is not showing bold in legal act page");
    }

    @And("content of recital {string} is bold, underlined in double diff comparision page")
    public void contentOfRecitalIsBoldUnderlinedInDoubleDiffComparisionPage(String arg0) {
        WebElement element = waitForElementTobePresent(driver, By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + "//span[contains(text(),'" + arg0 + "')]//ancestor::recital" + LegalActPage.LEOS_DOUBLE_COMPARE_ADDED_INTERMEDIATE));
        assertNotNull(element, "content of recital " + arg0 + " is not bold, underlined in double diff comparison page");
    }

    @And("content of recital {string} is bold, underlined in single diff comparision page")
    public void contentOfRecitalIsBoldUnderlinedInSingleDiffComparisionPage(String arg0) {
        WebElement element = waitForElementTobePresent(driver, By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + "//num[contains(text(),'" + arg0 + "')]//parent::recital" + LegalActPage.LEOS_CONTENT_NEW_CN));
        assertNotNull(element, "content of recital " + arg0 + " is not bold, underlined in single diff comparision page");
    }

    @And("below words are added to recital {int}")
    public void belowWordsAreAddedToRecital(int arg0, DataTable dataTable) {
        List<String> AddedStrList = new ArrayList<>();
        List<String> strList = dataTable.asList(String.class);
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.RECITALS + LegalActPage.RECITAL + "[" + arg0 + "]" + LegalActPage.AKNP + LegalActPage.SPAN + LegalActPage.LEOS_CONTENT_SOFT_NEW));
        for (WebElement ele : elementList) {
            AddedStrList.add(getElementAttributeInnerText(ele).trim());
        }
        assertTrue(AddedStrList.containsAll(strList), "Given Options are not added to recital " + arg0);
    }

    @Then("recital {int} is displayed in legal act page")
    public void recitalIsDisplayedInLegalActPage(int arg0) {
        assertTrue(waitForElementTobeDisPlayed(driver, By.xpath(LegalActPage.RECITAL + "[" + arg0 + "]")), "recital " + arg0 + " is displayed in legal act page");
    }

    @And("below words are showing as bold and underlined in recital {int} in double diff comparision page")
    public void belowWordsAreShowingAsBoldAndUnderlinedInRecitalInDoubleDiffComparisionPage(int arg0, DataTable dataTable) {
        List<String> actualStrList = new ArrayList<>();
        List<String> giveStrList = dataTable.asList(String.class);
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + LegalActPage.RECITAL + "[" + arg0 + "]" + LegalActPage.SPAN + LegalActPage.LEOS_DOUBLE_COMPARE_ADDED_INTERMEDIATE));
        for (WebElement ele : elementList) {
            actualStrList.add(getElementAttributeInnerText(ele).trim());
        }
        assertTrue(actualStrList.containsAll(giveStrList), "Given options are not present in the actual removed elements list");
    }

    @When("click on recital {string} present in navigation pane in legal act page")
    public void clickOnRecitalInLegalActPage(String arg0) {
        int len = 0;
        int iterationNumber;
        boolean flag = false;
        By by = By.xpath("//div[contains(@class,'gwt-HTML') and contains(text(),'" + arg0 + "')]");
        WebElement ele = driver.findElement(By.cssSelector(".v-treegrid-scroller.v-treegrid-scroller-vertical"));
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        long scrollHeight = (long) executor.executeScript("return arguments[0].scrollHeight;", ele);
        long offsetHeight = (long) executor.executeScript("return arguments[0].offsetHeight;", ele);
        executor.executeScript("arguments[0].scrollTop=-arguments[1];", ele, scrollHeight);
        boolean bool = waitElementToBeDisplayedWithInSpecifiedTime(driver, by, 10);
        if (!bool) {
            if (offsetHeight != 0) {
                iterationNumber = (int) (scrollHeight / offsetHeight);
                for (int i = 0; i <= iterationNumber; i++) {
                    len = len + (int) offsetHeight;
                    executor.executeScript("arguments[0].scrollTop=arguments[1];", ele, len);
                    bool = waitElementToBeDisplayedWithInSpecifiedTime(driver, by, 10);
                    if (bool) {
                        scrollandClick(driver, by);
                        flag = true;
                        break;
                    }
                }
                executor.executeScript("arguments[0].scrollTop=-arguments[1];", ele, scrollHeight);
            }
        } else {
            scrollandClick(driver, by);
            flag = true;
        }
        assertTrue(flag, "recital " + arg0 + " is not clicked in legal act page");
        E2eUtil.wait(2000);
    }

    @And("content of recital {string} contains below hyperlink\\(s)")
    public void contentOfRecitalContainsBelowLinkS(String arg0, DataTable dataTable) {
        List<String> linkList = new ArrayList<>();
        List<String> strList = dataTable.asList(String.class);
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.RECITAL + "//num[contains(text(),'" + arg0 + "')]//parent::recital//aknp//a"));
        for (WebElement ele : elementList) {
            linkList.add(getElementAttributeInnerText(ele).trim());
        }
        assertTrue(linkList.containsAll(strList), "content of recital doesn't contain these options");
    }

    @And("click on recital {int} in legal act page")
    public void clickOnRecital(int arg0) {
        elementClick(driver, By.xpath(LegalActPage.RECITAL + "[" + arg0 + "]"));
    }

    @When("click on save this version link present in Actions menu")
    public void clickOnSaveThisVersionLinkPresentInActionsMenu() {
        elementClick(driver, LegalActPage.SAVE_THIS_VERSION_ACTIONS_MENU_ITEM);
    }

    @When("provide {string} in the title text box of save this version window")
    public void provideInTheTitleTextBoxOfSaveThisVersionWindow(String arg0) {
        elementEcasSendkeys(driver, LegalActPage.TITLE_INPUT_SAVE_THIS_VERSION_WINDOW, arg0);
    }

    @And("click on save button in save this version window")
    public void clickOnSaveButtonInSaveThisVersionWindow() {
        elementClick(driver, LegalActPage.SAVE_BUTTON_SAVE_THIS_VERSION_WINDOW);
    }

    @And("No changes after last version is displayed under recent changes section")
    public void isDisplayedUnderRecentChangesSection() {
        boolean bool = waitForElementTobeDisPlayed(driver, LegalActPage.NO_CHANGES_AFTER_LAST_VERSION);
        assertTrue(bool, "No changes after last version is not displayed under recent changes section");
    }

    @And("{string} is displayed under version pane")
    public void isDisplayedUnderVersionPane(String arg0) {
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath(LegalActPage.LEOS_ACCORDION_VERSIONS_PANE + "//*[text()='" + arg0 + "']"));
        assertTrue(bool, arg0 + " is not displayed under version pane");
    }

    @When("click on show modifications button present under version {string} in version pane")
    public void clickOnShowModificationsButtonPresentUnderVersionInVersionPane(String arg0) {
        elementClick(driver, By.xpath(LegalActPage.VERSIONS_PANE + "//*[text()='" + arg0 + "']//ancestor::div[@id='versionCard']//*[text()='Show modifications']"));
    }

    @Then("hide modifications button is showing for version {string} in version pane")
    public void hideModificationsButtonIsShowingUnderVersionInVersionPane(String arg0) {
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath(LegalActPage.VERSIONS_PANE + "//*[text()='" + arg0 + "']//ancestor::div[@id='versionCard']//*[text()='Hide modifications']"));
        assertTrue(bool, "hide modifications button is showing under version " + arg0 + " in version pane");
    }

    @And("below minor versions are showing under version {string} in version pane")
    public void belowMinorVersionsAreShowingUnderVersionInVersionPane(String arg0, DataTable dataTable) {
        int len = 0;
        int iterationNumber;
        Set<String> actualVersionSet = new HashSet<>();
        List<String> givenVersionList = dataTable.asList(String.class);
        Set<String> givenVersionSet = new HashSet<>(givenVersionList);
        By by = By.xpath("//*[contains(@class,'v-label-intermediate') and text()='" + arg0 + "']//ancestor::div[@id='versionCard']//table/tbody/tr//div[@class='v-label v-widget v-label-undef-w']");
        WebElement innerScrollElement = driver.findElement(By.xpath("//*[contains(@class,'v-label-intermediate') and text()='" + arg0 + "']//ancestor::div[@id='versionCard']//div[@class='v-grid-scroller v-grid-scroller-vertical']"));
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        long innerScrollHeight = (long) executor.executeScript("return arguments[0].scrollHeight;", innerScrollElement);
        long innerOffSetHeight = (long) executor.executeScript("return arguments[0].offsetHeight;", innerScrollElement);
        executor.executeScript("arguments[0].scrollTop=-arguments[1];", innerScrollElement, innerScrollHeight);
        List<WebElement> elementList = driver.findElements(by);
        for (WebElement element : elementList) {
            actualVersionSet.add(getElementAttributeInnerText(element));
        }
        if (innerOffSetHeight != 0) {
            iterationNumber = (int) (innerScrollHeight / innerOffSetHeight);
            for (int i = 0; i <= iterationNumber; i++) {
                len = len + (int) innerOffSetHeight;
                executor.executeScript("arguments[0].scrollTop=arguments[1];", innerScrollElement, len);
                elementList = driver.findElements(by);
                for (WebElement element : elementList) {
                    actualVersionSet.add(getElementAttributeInnerText(element));
                }
            }
            executor.executeScript("arguments[0].scrollTop=-arguments[1];", innerScrollElement, innerScrollHeight);
        }
        assertTrue(actualVersionSet.containsAll(givenVersionSet), "given versions list is not present in major version " + arg0);
    }

    @When("click on {string} link in navigation pane")
    public void clickOnLinkInNavigationPane(String arg0) {
        boolean flag = false;
        Actions actions = new Actions(driver);
        By by = By.xpath(LegalActPage.TOC_TABLE_TREE_GRID + "//tbody//tr//div[contains(@class,'gwt-HTML') and contains(text(),'" + arg0 + "')]" + "//ancestor::tr");
        boolean bool = waitElementToBeDisplayedWithInSpecifiedTime(driver, by, 10);
        if (bool) {
            actions.moveToElement(driver.findElement(by)).click().release().build().perform();
            flag = true;
        } else {
            int len = 0;
            int iterationNumber;
            JavascriptExecutor executor = (JavascriptExecutor) driver;
            WebElement innerScrollElement = driver.findElement(LegalActPage.VERTICAL_SCROLL_BAR);
            long innerScrollHeight = (long) executor.executeScript("return arguments[0].scrollHeight;", innerScrollElement);
            long innerOffSetHeight = (long) executor.executeScript("return arguments[0].offsetHeight;", innerScrollElement);
            if (innerOffSetHeight != 0) {
                executor.executeScript("arguments[0].scrollTop=-arguments[1];", innerScrollElement, innerScrollHeight);
                bool = waitElementToBeDisplayedWithInSpecifiedTime(driver, by, 10);
                if (bool) {
                    actions.moveToElement(driver.findElement(by)).click().release().build().perform();
                    //actions.click().build().perform();
                    flag = true;
                } else {
                    iterationNumber = (int) (innerScrollHeight / innerOffSetHeight);
                    for (int i = 0; i <= iterationNumber; i++) {
                        len = len + (int) innerOffSetHeight;
                        executor.executeScript("arguments[0].scrollTop=arguments[1];", innerScrollElement, len);
                        bool = waitElementToBeDisplayedWithInSpecifiedTime(driver, by, 10);
                        if (bool) {
                            actions.moveToElement(driver.findElement(by)).click().release().build().perform();
                            actions.click().build().perform();
                            flag = true;
                            break;
                        }
                    }
                }
            }
        }
        assertTrue(flag, arg0 + " is not found in navigation pane");
    }

    @Then("article {int} is displayed in legal act")
    public void articleIsDisplayedInLegalAct(int arg0) {
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg0 + "]"));
        assertTrue(bool, "article " + arg0 + " is not displayed in legal act");
    }

    @When("click on edit button of article {int} heading")
    public void clickOnEditButtonArticleHeading(int arg0) {
        WebElement articleHeading = driver.findElement(By.xpath(LegalActPage.ARTICLE + "[" + arg0 + "]" + LegalActPage.HEADING));
        Actions actions = new Actions(driver);
        actions.moveToElement(articleHeading).build().perform();
        E2eUtil.wait(1000);
        WebElement editButton = driver.findElement(By.xpath(LegalActPage.ARTICLE + "[" + arg0 + "]//heading//following-sibling::div[1][@class='leos-actions Vaadin-Icons']//span[@data-widget-type='edit']"));
        actions.moveToElement(editButton).build().perform();
        actions.click().build().perform();
        E2eUtil.wait(1000);
    }

    @When("replace {string} with the existing content in ck editor text box for akn heading element origin from ec")
    public void replaceWithTheExistingContentInCkEditorTextBoxForAknHeadingElementOriginFromEc(String arg0) {
        elementEcasSendkeys(driver, LegalActPage.CK_EDTOR_HEADING_ORIGIN_EC, arg0);
        E2eUtil.wait(1000);
    }

    @And("{string} is showing as bold in heading of article {int} in legal act page")
    public void isShowingAsBoldInHeadingOfArticleInLegalActPage(String arg0, int arg1) {
        String text = getElementText(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.HEADING + LegalActPage.SPAN + LegalActPage.LEOS_CONTENT_SOFT_NEW));
        assertEquals(text, arg0, arg0 + " is not showing as bold in heading of article " + arg1);
    }

    @And("{string} is showing as grey and strikethrough in heading of article {int} in legal act page")
    public void isShowingAsGreyAndStrikethroughInHeadingOfArticleInLegalActPage(String arg0, int arg1) {
        String text = getElementText(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.HEADING + LegalActPage.SPAN + LegalActPage.LEOS_CONTENT_SOFT_REMOVED));
        assertEquals(text, arg0, arg0 + " is not showing as grey and strikethrough in heading of article " + arg1);
    }

    @Then("point {int} of paragraph {int} of article {int} is displayed")
    public void pointOfArticleIsDisplayed(int arg0, int arg1, int arg2) {
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.POINT + "[" + arg0 + "]"));
        assertTrue(bool, "point " + arg0 + " of paragraph " + arg1 + " of article " + arg2 + " is not displayed");
    }

    @When("double click on point {int} of paragraph {int} of article {int}")
    public void doubleClickOnPointOfArticle(int arg0, int arg1, int arg2) {
        E2eUtil.wait(2000);
        doubleClick(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.POINT + "[" + arg0 + "]" + LegalActPage.CONTENT));
    }

    @And("the content {string} is displayed for subparagraph of paragraph {int} of article {int}")
    public void theContentIsDisplayedForSubparagraphOfArticle(String arg0, int arg1, int arg2) {
        String text = getElementText(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.SUBPARAGRAPH + LegalActPage.CONTENT + LegalActPage.AKNP));
        assertEquals(text, arg0, "the content is not displayed for subparagraph of paragraph " + arg1 + " of article " + arg2);
    }

    @When("replace content {string} with the existing content in ck editor text box of a point inside article origin from ec")
    public void replaceContentWithTheExistingContentInCkEditorTextBoxOfAPointInsideArticleOriginFromEc(String arg0) {
        elementEcasSendkeys(driver, LegalActPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI, arg0);
        E2eUtil.wait(2000);
    }

    @And("below words are showing as grey and strikethrough in point {int} of paragraph {int} of article {int}")
    public void belowWordsAreShowingAsGreyAndStrikethroughInPointOfArticle(int arg0, int arg1, int arg2, DataTable dataTable) {
        List<String> deletedStrList = new ArrayList<>();
        List<String> strList = dataTable.asList(String.class);
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.LIST + LegalActPage.POINT + "[" + arg0 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP + LegalActPage.SPAN + LegalActPage.LEOS_CONTENT_SOFT_REMOVED));
        for (WebElement ele : elementList) {
            deletedStrList.add(getElementAttributeInnerText(ele).trim());
        }
        assertTrue(deletedStrList.containsAll(strList), "Given Options are not showing as grey or strikethrough in point " + arg0);
    }

    @And("below words are showing as bold in point {int} of paragraph {int} of article {int}")
    public void belowWordsAreShowingAsBoldInPointOfArticle(int arg0, int arg1, int arg2, DataTable dataTable) {
        List<String> addedStrList = new ArrayList<>();
        List<String> strList = dataTable.asList(String.class);
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.LIST + LegalActPage.POINT + "[" + arg0 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP + LegalActPage.SPAN + LegalActPage.LEOS_CONTENT_SOFT_NEW));
        for (WebElement ele : elementList) {
            addedStrList.add(getElementAttributeInnerText(ele).trim());
        }
        assertTrue(addedStrList.containsAll(strList), "Given Options are not showing as bold in point " + arg0);
    }

    @When("click on insert before icon present in show all actions icon of point {int} of paragraph {int} of article {int}")
    public void clickOnInsertBeforeIconPresentInShowAllActionsIconOfPointOfParagraphOfArticle(int arg0, int arg1, int arg2) {
        E2eUtil.wait(2000);
        Actions actions = new Actions(driver);
        WebElement point = driver.findElement(By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.LIST + LegalActPage.POINT + "[" + arg0 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP));
        actions.moveToElement(point).build().perform();
        E2eUtil.wait(2000);
        WebElement showAllAction = driver.findElement(By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.LIST + LegalActPage.POINT + "[" + arg0 + "]" + "//following-sibling::div[1][@class='leos-actions Vaadin-Icons']//span[@data-widget-type='show.all.actions']"));
        actions.moveToElement(showAllAction).build().perform();
        E2eUtil.wait(2000);
        WebElement insertBefore = driver.findElement(By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.LIST + LegalActPage.POINT + "[" + arg0 + "]" + "//following-sibling::div[1][@class='leos-actions Vaadin-Icons']//span[@data-widget-type='insert.before']"));
        actions.moveToElement(insertBefore).build().perform();
        actions.click().build().perform();
        E2eUtil.wait(2000);
    }

    @And("a point with number {string} is added in paragraph {int} of article {int}")
    public void aPointWithNumberIsShowingAsBoldInParagraphOfArticle(String arg0, int arg1, int arg2) {
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.LIST + LegalActPage.POINT + LegalActPage.LEOS_CONTENT_SOFT_NEW + "//num[text()='" + arg0 + "']"));
        assertTrue(bool);
    }

    @And("content of point {string} of paragraph {int} of article {int} is showing bold in legal act live page")
    public void contentOfPointOfParagraphOfArticleIsShowingBoldInLegalActLivePage(String arg0, int arg1, int arg2) {
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.LIST + LegalActPage.POINT + "//num[text()='" + arg0 + "']//following-sibling::content" + LegalActPage.AKNP + LegalActPage.LEOS_CONTENT_SOFT_NEW));
        assertTrue(bool);
    }

    @Then("paragraph {int} of article {int} is displayed")
    public void paragraphOfArticleIsDisplayed(int arg0, int arg1) {
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg0 + "]"));
        assertTrue(bool, "paragraph " + arg0 + " of article " + arg1 + " is not displayed");
    }

    @When("double click on paragraph {int} of article {int}")
    public void doubleClickOnParagraphOfArticle(int arg0, int arg1) {
        E2eUtil.wait(2000);
        doubleClick(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg0 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP));
        E2eUtil.wait(2000);
    }

    @And("below words are showing as bold in paragraph {int} of article {int}")
    public void belowWordsAreShowingAsBoldInParagraphOfArticle(int arg0, int arg1, DataTable dataTable) {
        List<String> addedStrList = new ArrayList<>();
        List<String> strList = dataTable.asList(String.class);
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg0 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP + LegalActPage.SPAN + LegalActPage.LEOS_CONTENT_SOFT_NEW));
        for (WebElement ele : elementList) {
            addedStrList.add(getElementAttributeInnerText(ele));
        }
        assertTrue(addedStrList.containsAll(strList), "Given Options are not showing as bold in point " + arg0);
    }

    @And("below words are showing as grey and strikethrough in paragraph {int} of article {int}")
    public void belowWordsAreShowingGreyStrikthroughParagraphArticle(int arg0, int arg1, DataTable dataTable) {
        List<String> deletedStrList = new ArrayList<>();
        List<String> strList = dataTable.asList(String.class);
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg0 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP + LegalActPage.SPAN + LegalActPage.LEOS_CONTENT_SOFT_REMOVED));
        for (WebElement ele : elementList) {
            deletedStrList.add(getElementAttributeInnerText(ele));
        }
        assertTrue(deletedStrList.containsAll(strList), "Given Options are not showing as grey or strikethrough in point " + arg0);
    }

    @When("replace content {string} with the existing content in ck editor text box of a paragraph inside article origin from ec")
    public void replaceContentWithExistingContentParagraphInArticleOriginEC(String arg0) {
        elementEcasSendkeys(driver, LegalActPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI, arg0);
        E2eUtil.wait(2000);
    }

    @When("click on insert after icon present in show all actions icon of point {int} of paragraph {int} of article {int}")
    public void clickOnInsertAfterIconPresentInShowAllActionsIconOfPointOfParagraphOfArticle(int arg0, int arg1, int arg2) {
        E2eUtil.wait(2000);
        Actions actions = new Actions(driver);
        WebElement point = driver.findElement(By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.LIST + LegalActPage.POINT + "[" + arg0 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP));
        actions.moveToElement(point).build().perform();
        E2eUtil.wait(2000);
        WebElement showAllAction = driver.findElement(By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.LIST + LegalActPage.POINT + "[" + arg0 + "]" + "//following-sibling::div[1][@class='leos-actions Vaadin-Icons']//span[@data-widget-type='show.all.actions']"));
        actions.moveToElement(showAllAction).build().perform();
        E2eUtil.wait(2000);
        WebElement insertAfter = driver.findElement(By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.LIST + LegalActPage.POINT + "[" + arg0 + "]" + "//following-sibling::div[1][@class='leos-actions Vaadin-Icons']//span[@data-widget-type='insert.after']"));
        actions.moveToElement(insertAfter).build().perform();
        actions.click().build().perform();
        E2eUtil.wait(2000);
    }

    @When("click on delete icon present in show all actions icon of paragraph {int} of article {int}")
    public void clickOnDeleteIconPresentInShowAllActionsIconOfParagraphOfArticle(int arg0, int arg1) {
        E2eUtil.wait(2000);
        Actions actions = new Actions(driver);
        WebElement subParagraph = driver.findElement(By.xpath(LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg0 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP));
        actions.moveToElement(subParagraph).build().perform();
        E2eUtil.wait(2000);
        WebElement showAllAction = driver.findElement(By.xpath(LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg0 + "]" + "//following-sibling::div[1][@class='leos-actions Vaadin-Icons']//span[@data-widget-type='show.all.actions']"));
        actions.moveToElement(showAllAction).build().perform();
        E2eUtil.wait(2000);
        WebElement delete = driver.findElement(By.xpath(LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg0 + "]" + "//following-sibling::div[1][@class='leos-actions Vaadin-Icons']//span[@data-widget-type='delete']"));
        actions.moveToElement(delete).build().perform();
        actions.click().build().perform();
        E2eUtil.wait(2000);
    }

    @And("content of paragraph {int} of article {int} is showing as grey and strikethrough in legal act live page")
    public void contentOfParagraphOfArticleIsShowingAsGreyAndStrikethroughInLegalActLivePage(int arg0, int arg1) {
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg0 + "]" + LegalActPage.LEOS_CONTENT_SOFT_REMOVED + LegalActPage.CONTENT_SINGLE_SLASH + LegalActPage.LEOS_CONTENT_SOFT_REMOVED + LegalActPage.AKNP_SINGLE_SLASH + LegalActPage.LEOS_CONTENT_SOFT_REMOVED));
        assertTrue(bool, "content of paragraph " + arg0 + " of article " + arg1 + " is not showing as grey and strikethrough in legal act live page");
    }

    @And("undelete button is displayed and enabled in selected element section")
    public void undeleteButtonIsDisplayedAndEnabledInSelectedElementSection() {
        assertTrue(waitForElementTobeDisPlayed(driver, LegalActPage.UN_DELETE_BUTTON), "undelete button is not displayed in selected element section");
        assertTrue(verifyElementIsEnabled(driver, LegalActPage.UN_DELETE_BUTTON), "undelete button is not enabled in selected element section");
    }

    @When("click on undelete button present in selected element section")
    public void clickOnUndeleteButtonPresentInSelectedElementSection() {
        elementClick(driver, LegalActPage.UN_DELETE_BUTTON);
    }

    @And("content of paragraph {int} of article {int} is showing normal in legal act live page")
    public void contentOfParagraphOfArticleIsShowingNormalInLegalActLivePage(int arg0, int arg1) {
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg0 + "]" + LegalActPage.NOT_LEOS_CONTENT_SOFT_REMOVED));
        assertTrue(bool, "content of paragraph " + arg0 + " of article " + arg1 + " is not showing normal in legal act live page");
    }

    @Then("article {int} is displayed")
    public void articleIsDisplayed(int arg0) {
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg0 + "]"));
        assertTrue(bool, "article " + arg0 + " is not displayed");
    }

    @And("{string} option is selected in paragraph numbering in selected element section")
    public void optionIsSelectedInParagraphNumberingInSelectedElementSection(String arg0) {
        boolean bool = verifyIsElementSelected(driver, By.xpath(LegalActPage.SELECTED_ELEMENT_TEXT + "//ancestor::div[contains(@class,'v-slot-leos-bottom-slider-panel')]//label[text()='" + arg0 + "']//preceding-sibling::input[@type='radio']"));
        assertTrue(bool, arg0 + " option is not selected in paragraph numbering in selected element section");
    }

    @When("click on option {string} in paragraph numbering in selected element section")
    public void clickOnOptionInParagraphNumberingInSelectedElementSection(String arg0) {
        elementClick(driver, By.xpath(LegalActPage.SELECTED_ELEMENT_TEXT + "//ancestor::div[contains(@class,'v-slot-leos-bottom-slider-panel')]//label[text()='" + arg0 + "']//preceding-sibling::input[@type='radio']"));
    }

    @And("number {string} is added to existing paragraph {int} of article {int}")
    public void numberIsAddedToExistingParagraphOfArticle(String arg0, int arg1, int arg2) {
        String text = getElementText(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]/num" + LegalActPage.LEOS_CONTENT_SOFT_NEW + LegalActPage.SPAN + LegalActPage.LEOS_CONTENT_SOFT_NEW));
        assertEquals(text, arg0, "number " + arg0 + " is not added to paragraph " + arg1 + " of article " + arg2);
    }

    @And("number {string} is added to new paragraph {int} of article {int}")
    public void numberIsAddedToNewParagraphOfArticle(String arg0, int arg1, int arg2) {
        String text = getElementText(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]/num" + LegalActPage.LEOS_CONTENT_SOFT_NEW));
        assertEquals(text, arg0, "number " + arg0 + " is not added to paragraph " + arg1 + " of article " + arg2);
    }

    @When("click on insert after icon present in show all actions icon of subparagraph {int} of paragraph {int} of article {int}")
    public void clickOnInsertAfterIconPresentInShowAllActionsIconOfSubparagraphOfParagraphOfArticle(int arg0, int arg1, int arg2) {
        E2eUtil.wait(2000);
        WebElement subParagraph = driver.findElement(By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.SUBPARAGRAPH + "[" + arg0 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP));
        Actions actions = new Actions(driver);
        actions.moveToElement(subParagraph).build().perform();
        E2eUtil.wait(2000);
        WebElement showAllAction = driver.findElement(By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.SUBPARAGRAPH + "[" + arg0 + "]" + "//following-sibling::div[1][@class='leos-actions Vaadin-Icons']//span[@data-widget-type='show.all.actions']"));
        actions.moveToElement(showAllAction).build().perform();
        E2eUtil.wait(2000);
        WebElement insertAfter = driver.findElement(By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.SUBPARAGRAPH + "[" + arg0 + "]" + "//following-sibling::div[1][@class='leos-actions Vaadin-Icons']//span[@data-widget-type='insert.after']"));
        actions.moveToElement(insertAfter).build().perform();
        actions.click().build().perform();
        E2eUtil.wait(2000);
    }

    @Then("paragraph {int} is added to article {int} in legal act live page")
    public void paragraphIsAddedToArticleInLegalActLivePage(int arg0, int arg1) {
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg0 + "]" + LegalActPage.LEOS_CONTENT_SOFT_NEW));
        assertTrue(bool, "paragraph " + arg0 + " is not added to article " + arg1 + " in legal act live page");
    }

    @When("click on hamburger icon of version {string} in versions pane")
    public void clickOnHamburgerIconOfVersionInVersionsPane(String arg0) {
        elementClick(driver, By.xpath("//*[contains(@class,'v-label-undef-w') and text()='" + arg0 + "']//ancestor::div[@id='versionCard']" + LegalActPage.VERSION_ACTIONS_PNG));
    }

    @Then("below options are displayed in versions pane section")
    public void belowOptionsAreDisplayedInVersionsPaneSection(DataTable dataTable) {
        String text;
        List<String> actualOptionList = new ArrayList<>();
        List<String> givenOptionList = dataTable.asList(String.class);
        List<WebElement> elements = driver.findElements(LegalActPage.VIEW_THIS_VERSION_MENU);
        assertTrue(null != elements && !elements.isEmpty(), "no element present in menu bar pop up");
        for (WebElement element : elements) {
            text = element.getText();
            actualOptionList.add(text);
        }
        assertTrue(actualOptionList.containsAll(givenOptionList), "given options are not present in the actual options list");
    }

    @When("click on the option {string} showing in versions pane")
    public void clickOnTheOptionShowingForVersionInVersionsPane(String arg0) {
        elementClick(driver, By.xpath(CommonPage.XPATH_TEXT_1 + arg0 + CommonPage.XPATH_TEXT_2));
    }

    @Then("{string} notification is displayed under recent changes section")
    public void notificationIsDisplayedUnderRecentChangesSection(String arg0) {
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath(LegalActPage.V_LABEL_UNDEF_RECENT_CHANGES + "//ancestor::div[@id='versionCard']//*[text()='" + arg0 + "']"));
        assertTrue(bool, arg0 + " notification is not displayed under recent changes section");
    }

    @Then("there are no changes in paragraph {int} of article {int}")
    public void thereAreNoChangesInParagraphOfArticle(int arg0, int arg1) {
        boolean bool1 = waitUnTillElementIsNotPresent(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg0 + "]" + LegalActPage.SUBPARAGRAPH + LegalActPage.CONTENT + LegalActPage.AKNP + LegalActPage.SPAN + LegalActPage.LEOS_CONTENT_SOFT_NEW));
        assertTrue(bool1, "there is at least one addition in paragraph " + arg0 + " of article " + arg1);
        boolean bool2 = waitUnTillElementIsNotPresent(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg0 + "]" + LegalActPage.SUBPARAGRAPH + LegalActPage.CONTENT + LegalActPage.AKNP + LegalActPage.SPAN + LegalActPage.LEOS_CONTENT_SOFT_REMOVED));
        assertTrue(bool2, "there is at least one deletion in paragraph " + arg0 + " of article " + arg1);
    }

    @And("there is only {int} addition\\(s) in article section in single diff comparision page")
    public void thereIsOnlyAdditionSInArticleSectionInSingleDiffComparisionPage(int arg0) {
        List<WebElement> docPurposeElementList = getElementListForComparision(driver, By.xpath(LegalActPage.DOC_PURPOSE + LegalActPage.SPAN + LegalActPage.LEOS_CONTENT_NEW_CN), 10);
        assertEquals(docPurposeElementList.size(), arg0, "either there are no changes or multiple additions in article section in single diff comparision page");
    }

    @And("{string} is showing as bold and underlined in heading of article {int} in double diff comparision page")
    public void isShowingAsBoldAndUnderlinedInHeadingOfArticleInDoubleDiffComparisionPage(String arg0, int arg1) {
        String text = waitForElementTobePresent(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.HEADING + LegalActPage.SPAN + LegalActPage.LEOS_DOUBLE_COMPARE_ADDED_INTERMEDIATE)).getText();
        assertEquals(text, arg0, arg0 + " is not showing as bold and underlined in heading of article " + arg1 + " in double diff comparision page");
    }

    @And("below words are showing as bold and underlined in point {int} of paragraph {int} of article {int} in double diff comparision page")
    public void belowWordsAreShowingAsBoldAndUnderlinedInPointOfParagraphOfArticleInDoubleDiffComparisionPage(int arg0, int arg1, int arg2, DataTable dataTable) {
        List<String> actualStrList = new ArrayList<>();
        List<String> giveStrList = dataTable.asList(String.class);
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.POINT + "[" + arg0 + "]" + LegalActPage.SPAN + LegalActPage.LEOS_DOUBLE_COMPARE_ADDED_INTERMEDIATE));
        for (WebElement ele : elementList) {
            actualStrList.add(getElementAttributeInnerText(ele).trim());
        }
        assertTrue(actualStrList.containsAll(giveStrList), "Given options are not bold and underlined in point in double diff comparision page");
    }

    @And("below words are showing as bold, strikethrough and underlined in point {int} of paragraph {int} of article {int} in double diff comparision page")
    public void belowWordsAreShowingAsBoldStrikethroughAndUnderlinedInPointOfParagraphOfArticleInDoubleDiffComparisionPage(int arg0, int arg1, int arg2, DataTable dataTable) {
        List<String> actualStrList = new ArrayList<>();
        List<String> giveStrList = dataTable.asList(String.class);
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.POINT + "[" + arg0 + "]" + LegalActPage.SPAN + LegalActPage.LEOS_DOUBLE_COMPARE_REMOVED_INTERMEDIATE));
        for (WebElement ele : elementList) {
            actualStrList.add(getElementAttributeInnerText(ele).trim());
        }
        assertTrue(actualStrList.containsAll(giveStrList), "Given options are not bold, strikethrough and underlined in point in double diff comparision page");
    }

    @And("there is only {int} addition\\(s) in article section in double diff comparision page")
    public void thereIsOnlyAdditionSInArticleSectionInDoubleDiffComparisionPage(int arg0) {
        List<WebElement> elementList1 = getElementListForComparision(driver, By.xpath("//article//point[@class='leos-double-compare-added-intermediate']"), 10);
        List<WebElement> elementList2 = getElementListForComparision(driver, By.xpath("//article//heading//span[@class='leos-double-compare-added-intermediate']"), 10);
        List<WebElement> elementList3 = getElementListForComparision(driver, By.xpath("//article//point[not(@class='leos-double-compare-added-intermediate')]//span[@class='leos-double-compare-added-intermediate']"), 10);
        List<WebElement> elementList4 = getElementListForComparision(driver, By.xpath("//article//paragraph[not(@class='leos-double-compare-added-intermediate')]/content//aknp//span[@class='leos-double-compare-added-intermediate']"), 10);
        List<WebElement> elementList5 = getElementListForComparision(driver, By.xpath("//article//paragraph[not(@class='leos-double-compare-added-intermediate')]/num[@class='leos-double-compare-added-intermediate']//span[@class='leos-double-compare-added-intermediate']"), 10);
        List<WebElement> elementList6 = getElementListForComparision(driver, By.xpath("//article//paragraph[@class='leos-double-compare-added-intermediate']"), 10);
        List<WebElement> elementList7 = getElementListForComparision(driver, By.xpath("//docpurpose//span[@class='leos-double-compare-added-original']"), 10);
        List<WebElement> elementList8 = getElementListForComparision(driver, By.xpath("//article//heading//span[@class='leos-double-compare-added-original']"), 10);
        List<WebElement> elementList9 = getElementListForComparision(driver, By.xpath("//article//point[not(@class='leos-double-compare-added-original')]//span[@class='leos-double-compare-added-original']"), 10);
        List<WebElement> elementList10 = getElementListForComparision(driver, By.xpath("//article//point[@class='leos-double-compare-added-original']"), 10);
        List<WebElement> elementList11 = getElementListForComparision(driver, By.xpath("//article//paragraph[not(@class='leos-double-compare-added-original')]/content//aknp//span[@class='leos-double-compare-added-original']"), 10);
        int size = elementList1.size() + elementList2.size() + elementList3.size() + elementList4.size() + elementList5.size() + elementList6.size() + elementList7.size() + elementList8.size() + elementList9.size() + elementList10.size() + elementList11.size();
        assertEquals(size, arg0, "there is more or less than " + arg0 + " addition\\(s) in article section in double diff comparision page");
    }

    @And("there is only {int} deletion\\(s) in article section in double diff comparision page")
    public void thereIsOnlyDeletionSInArticleSectionInDoubleDiffComparisionPage(int arg0) {
        List<WebElement> elementList1 = getElementListForComparision(driver, By.xpath("//article//heading//span[@class='leos-double-compare-removed-intermediate']"), 10);
        List<WebElement> elementList2 = getElementListForComparision(driver, By.xpath("//article//point[not(@class='leos-double-compare-removed-intermediate')]//span[@class='leos-double-compare-removed-intermediate']"), 10);
        List<WebElement> elementList3 = getElementListForComparision(driver, By.xpath("//article//paragraph[@class='leos-double-compare-removed-intermediate']"), 10);
        List<WebElement> elementList4 = getElementListForComparision(driver, By.xpath("//article//paragraph/content//aknp//span[@class='leos-double-compare-removed-intermediate']"), 10);
        List<WebElement> elementList5 = getElementListForComparision(driver, By.xpath("//article//heading//span[@class='leos-double-compare-removed-original']"), 10);
        List<WebElement> elementList6 = getElementListForComparision(driver, By.xpath("//article//point[not(@class='leos-double-compare-removed-original')]//span[@class='leos-double-compare-removed-original']"), 10);
        List<WebElement> elementList7 = getElementListForComparision(driver, By.xpath("//article//paragraph[@class='leos-double-compare-removed-original']"), 10);
        List<WebElement> elementList8 = getElementListForComparision(driver, By.xpath("//article//paragraph[not(@class='leos-double-compare-removed-original')]/content//aknp//span[@class='leos-double-compare-removed-original']"), 10);
        int size = elementList1.size() + elementList2.size() + elementList3.size() + elementList4.size() + elementList5.size() + elementList6.size() + elementList7.size() + elementList8.size();
        assertEquals(size, arg0, "there is more or less than " + arg0 + " deletion\\(s) in article section in double diff comparision page");
    }

    @When("untick on checkbox of minor version {string} in major version {string}")
    public void untickOnCheckboxOfMinorVersionInMajorVersion(String arg0, String arg1) {
        int len = 0;
        int iterationNumber;
        boolean flag = false;
        By by = By.xpath(LegalActPage.VERSION_CARD + CommonPage.XPATH_CONTAINS_TEXT_1 + arg0 + CommonPage.XPATH_CONTAINS_TEXT_2 + LegalActPage.COMPONENT_WRAP_CHECKBOX);
        WebElement innerScrollElement = driver.findElement(By.xpath("//*[text()='" + arg1 + "']//ancestor::div[@id='versionCard']" + LegalActPage.V_GRID_SCROLLER));
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        long innerScrollHeight = (long) executor.executeScript("return arguments[0].scrollHeight;", innerScrollElement);
        long innerOffSetHeight = (long) executor.executeScript("return arguments[0].offsetHeight;", innerScrollElement);
        executor.executeScript("arguments[0].scrollTop=-arguments[1];", innerScrollElement, innerScrollHeight);
        boolean bool = waitElementToBeDisplayedWithInSpecifiedTime(driver, by, 10);
        if (!bool) {
            if (innerOffSetHeight != 0) {
                iterationNumber = (int) (innerScrollHeight / innerOffSetHeight);
                for (int i = 0; i <= iterationNumber; i++) {
                    len = len + (int) innerOffSetHeight;
                    executor.executeScript("arguments[0].scrollTop=arguments[1];", innerScrollElement, len);
                    bool = waitElementToBeDisplayedWithInSpecifiedTime(driver, by, 10);
                    if (bool) {
                        scrollandClick(driver, by);
                        flag = true;
                        break;
                    }
                }
                executor.executeScript("arguments[0].scrollTop=-arguments[1];", innerScrollElement, innerScrollHeight);
            }
        } else {
            scrollandClick(driver, by);
            flag = true;
        }
        assertTrue(flag, "version " + arg0 + " in recent changes section is ticked");
    }

    @And("content of point {string} of paragraph {int} of article {int} is showing bold and underlined in double diff comparision page")
    public void contentOfPointOfParagraphOfArticleIsShowingBoldAndUnderlinedInDoubleDiffComparisionPage(String arg0, int arg1, int arg2) {
        WebElement ele = waitForElementTobePresent(driver, By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.POINT + LegalActPage.LEOS_DOUBLE_COMPARE_ADDED_INTERMEDIATE + LegalActPage.NUM + LegalActPage.LEOS_DOUBLE_COMPARE_ADDED_INTERMEDIATE + LegalActPage.SPAN + "[@class='leos-double-compare-added-intermediate' and text()='" + arg0 + "']"));
        assertNotNull(ele, "num of point is not showing bold and underlined");
        WebElement ele1 = waitForElementTobePresent(driver, By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.POINT + LegalActPage.LEOS_DOUBLE_COMPARE_ADDED_INTERMEDIATE + LegalActPage.CONTENT + LegalActPage.LEOS_DOUBLE_COMPARE_ADDED_INTERMEDIATE + LegalActPage.AKNP + LegalActPage.LEOS_DOUBLE_COMPARE_ADDED_INTERMEDIATE + LegalActPage.SPAN + LegalActPage.LEOS_DOUBLE_COMPARE_ADDED_INTERMEDIATE));
        assertNotNull(ele1, "content of point is not showing bold and underlined");
    }

    @And("below words are showing as bold and underlined in paragraph {int} of article {int} in double diff comparision page")
    public void belowWordsAreShowingAsBoldAndUnderlinedInParagraphOfArticleInDoubleDiffComparisionPage(int arg0, int arg1, DataTable dataTable) {
        List<String> giveStrList = dataTable.asList(String.class);
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg0 + "]" + LegalActPage.SPAN + LegalActPage.LEOS_DOUBLE_COMPARE_ADDED_INTERMEDIATE));
        List<String> actualStrList = new ArrayList<>();
        for (WebElement ele : elementList) {
            actualStrList.add(getElementAttributeInnerText(ele));
        }
        assertTrue(actualStrList.containsAll(giveStrList), "Given options are not bold and underlined in paragraph in double diff comparision page");
    }

    @And("below words are showing as bold, strikethrough and underlined in paragraph {int} of article {int} in double diff comparision page")
    public void belowWordsAreShowingAsBoldStrikethroughAndUnderlinedInParagraphOfArticleInDoubleDiffComparisionPage(int arg0, int arg1, DataTable dataTable) {
        List<String> giveStrList = dataTable.asList(String.class);
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg0 + "]" + LegalActPage.SPAN + LegalActPage.LEOS_DOUBLE_COMPARE_REMOVED_INTERMEDIATE));
        List<String> actualStrList = new ArrayList<>();
        for (WebElement ele : elementList) {
            actualStrList.add(getElementAttributeInnerText(ele));
        }
        assertTrue(actualStrList.containsAll(giveStrList), "Given options are not bold, strikethrough and underlined in paragraph in double diff comparision page");
    }

    @And("content of paragraph {int} of article {int} is showing as bold, strikethrough and underlined in double diff comparision page")
    public void contentOfParagraphOfArticleIsShowingAsBoldStrikethroughAndUnderlinedInDoubleDiffComparisionPage(int arg0, int arg1) {
        WebElement ele = waitForElementTobePresent(driver, By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg0 + "]" + LegalActPage.LEOS_DOUBLE_COMPARE_REMOVED_INTERMEDIATE + LegalActPage.NUM + LegalActPage.LEOS_DOUBLE_COMPARE_REMOVED_INTERMEDIATE));
        assertNotNull(ele, "num of paragraph is not showing bold, strikethrough and underlined");
        WebElement ele1 = waitForElementTobePresent(driver, By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg0 + "]" + LegalActPage.LEOS_DOUBLE_COMPARE_REMOVED_INTERMEDIATE + LegalActPage.CONTENT + LegalActPage.LEOS_DOUBLE_COMPARE_REMOVED_INTERMEDIATE + LegalActPage.AKNP + LegalActPage.LEOS_DOUBLE_COMPARE_REMOVED_INTERMEDIATE));
        assertNotNull(ele1, "content of paragraph is not showing bold, strikethrough and underlined");
    }

    @When("tick on checkbox of major version {string}")
    public void tickOnCheckboxOfMajorVersion(String arg0) {
        scrollandClick(driver, By.xpath("//div[contains(@class,'v-label-undef-w') and text()='" + arg0 + "']//ancestor::div[@id='versionCard']" + LegalActPage.VERSION_CARD_ACTION_BLOCK + LegalActPage.INPUT_TYPE_CHECK_BOX));
    }

    @And("{string} is showing as bold, strikethrough and underlined in heading of article {int} in double diff comparision page")
    public void isShowingAsBoldStrikethroughAndUnderlinedInHeadingOfArticleInDoubleDiffComparisionPage(String arg0, int arg1) {
        String text = waitForElementTobePresent(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.HEADING + LegalActPage.SPAN + LegalActPage.LEOS_DOUBLE_COMPARE_REMOVED_INTERMEDIATE)).getText();
        assertEquals(text, arg0, arg0 + " is not showing as bold, strikethrough and underlined in heading of article " + arg1 + " in double diff comparision page");
    }

    @And("{string} is showing as bold in heading of article {int} in double diff comparision page")
    public void isShowingAsBoldInHeadingOfArticleInDoubleDiffComparisionPage(String arg0, int arg1) {
        WebElement element = driver.findElement(By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.HEADING + LegalActPage.SPAN + LegalActPage.LEOS_DOUBLE_COMPARE_ADDED_ORIGINAL));
        assertEquals(getElementAttributeInnerText(element), arg0, arg0 + " is not showing as bold in heading of article " + arg1 + " in double diff comparision page");
    }

    @And("{string} is showing as bold and strikethrough in heading of article {int} in double diff comparision page")
    public void isShowingAsBoldAndStrikethroughInHeadingOfArticleInDoubleDiffComparisionPage(String arg0, int arg1) {
        WebElement element = driver.findElement(By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.HEADING + LegalActPage.SPAN + LegalActPage.LEOS_DOUBLE_COMPARE_REMOVED_ORIGINAL));
        assertNotNull(element);
        assertEquals(getElementAttributeInnerText(element), arg0, arg0 + " is not showing as bold and strikethrough in heading of article " + arg1 + " in double diff comparision page");
    }

    @And("below words are showing as bold in point {int} of paragraph {int} of article {int} in double diff comparision page")
    public void belowWordsAreShowingAsBoldInPointOfParagraphOfArticleInDoubleDiffComparisionPage(int arg0, int arg1, int arg2, DataTable dataTable) {
        List<String> actualStrList = new ArrayList<>();
        List<String> giveStrList = dataTable.asList(String.class);
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.POINT + "[" + arg0 + "]" + LegalActPage.SPAN + LegalActPage.LEOS_DOUBLE_COMPARE_ADDED_ORIGINAL));
        for (WebElement ele : elementList) {
            actualStrList.add(getElementAttributeInnerText(ele).trim());
        }
        assertTrue(actualStrList.containsAll(giveStrList), "Given options are not bold in point in double diff comparision page");
    }

    @And("below words are showing as bold and strikethrough in point {int} of paragraph {int} of article {int} in double diff comparision page")
    public void belowWordsAreShowingAsBoldAndStrikethroughInPointOfParagraphOfArticleInDoubleDiffComparisionPage(int arg0, int arg1, int arg2, DataTable dataTable) {
        List<String> actualStrList = new ArrayList<>();
        List<String> giveStrList = dataTable.asList(String.class);
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.POINT + "[" + arg0 + "]" + LegalActPage.SPAN + LegalActPage.LEOS_DOUBLE_COMPARE_REMOVED_ORIGINAL));
        for (WebElement ele : elementList) {
            actualStrList.add(getElementAttributeInnerText(ele).trim());
        }
        assertTrue(actualStrList.containsAll(giveStrList), "Given options are not bold and strikethrough in point in double diff comparision page");
    }

    @And("content of point {string} of paragraph {int} of article {int} is showing bold in double diff comparision page")
    public void contentOfPointOfParagraphOfArticleIsShowingBoldInDoubleDiffComparisionPage(String arg0, int arg1, int arg2) {
        WebElement ele = waitForElementTobePresent(driver, By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.POINT + LegalActPage.LEOS_DOUBLE_COMPARE_ADDED_ORIGINAL + LegalActPage.NUM + LegalActPage.SPAN + "[@class='leos-double-compare-added-original' and text()='" + arg0 + "']"));
        assertNotNull(ele, "num of point is not showing bold");
        WebElement ele1 = waitForElementTobePresent(driver, By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.POINT + LegalActPage.LEOS_DOUBLE_COMPARE_ADDED_ORIGINAL + LegalActPage.CONTENT + LegalActPage.AKNP + LegalActPage.SPAN + LegalActPage.LEOS_DOUBLE_COMPARE_ADDED_ORIGINAL));
        assertNotNull(ele1, "content of point is not showing bold");
    }

    @And("below words are showing as bold and strikethrough in paragraph {int} of article {int} in double diff comparision page")
    public void belowWordsAreShowingAsBoldAndStrikethroughInParagraphOfArticleInDoubleDiffComparisionPage(int arg0, int arg1, DataTable dataTable) {
        List<String> actualStrList = new ArrayList<>();
        List<String> giveStrList = dataTable.asList(String.class);
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg0 + "]" + LegalActPage.SPAN + LegalActPage.LEOS_DOUBLE_COMPARE_REMOVED_ORIGINAL));
        for (WebElement ele : elementList) {
            actualStrList.add(getElementAttributeInnerText(ele));
        }
        assertTrue(actualStrList.containsAll(giveStrList), "Given options are not bold, strikethrough in paragraph in double diff comparision page");
    }

    @And("content of paragraph {int} of article {int} is showing as bold and strikethrough in double diff comparision page")
    public void contentOfParagraphOfArticleIsShowingAsBoldAndStrikethroughInDoubleDiffComparisionPage(int arg0, int arg1) {
        WebElement ele = waitForElementTobePresent(driver, By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg0 + "]" + LegalActPage.LEOS_DOUBLE_COMPARE_REMOVED_ORIGINAL + LegalActPage.NUM + LegalActPage.LEOS_DOUBLE_COMPARE_REMOVED_ORIGINAL));
        assertNotNull(ele, "num of paragraph is not showing bold, strikethrough");
        WebElement ele1 = waitForElementTobePresent(driver, By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg0 + "]" + LegalActPage.LEOS_DOUBLE_COMPARE_REMOVED_ORIGINAL + LegalActPage.CONTENT + LegalActPage.LEOS_DOUBLE_COMPARE_REMOVED_ORIGINAL + LegalActPage.AKNP + LegalActPage.LEOS_DOUBLE_COMPARE_REMOVED_ORIGINAL));
        assertNotNull(ele1, "content of paragraph is not showing bold, strikethrough");
    }

    @And("content of numbered paragraph {string} of article {int} is showing as bold and underlined in double diff comparision page")
    public void contentOfNumberedParagraphOfArticleIsShowingAsBoldAndUnderlinedInDoubleDiffComparisionPage(String arg0, int arg1) {
        WebElement ele = waitForElementTobePresent(driver, By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + LegalActPage.NUM + LegalActPage.LEOS_DOUBLE_COMPARE_ADDED_INTERMEDIATE + LegalActPage.SPAN + "[@class='leos-double-compare-added-intermediate' and text()='" + arg0 + "']"));
        assertNotNull(ele, "num of numbered paragraph is not showing bold and underlined");
        WebElement ele1 = waitForElementTobePresent(driver, By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + LegalActPage.NUM + LegalActPage.SPAN + "[text()='" + arg0 + "']//ancestor::paragraph" + LegalActPage.CONTENT + LegalActPage.LEOS_DOUBLE_COMPARE_ADDED_INTERMEDIATE + LegalActPage.AKNP + LegalActPage.LEOS_DOUBLE_COMPARE_ADDED_INTERMEDIATE + LegalActPage.SPAN + LegalActPage.LEOS_DOUBLE_COMPARE_ADDED_INTERMEDIATE));
        assertNotNull(ele1, "content of numbered paragraph is not showing bold and underlined");
    }

    @And("num tag of numbered paragraph {string} of article {int} is showing as bold and underlined in double diff comparision page")
    public void numTagOfParagraphOfArticleIsShowingAsBoldAndUnderlinedInDoubleDiffComparisionPage(String arg0, int arg1) {
        WebElement ele = waitForElementTobePresent(driver, By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + LegalActPage.NUM + LegalActPage.LEOS_DOUBLE_COMPARE_ADDED_INTERMEDIATE + LegalActPage.SPAN + "[@class='leos-double-compare-added-intermediate' and text()='" + arg0 + "']"));
        assertNotNull(ele, "num tag of numbered paragraph is not showing as bold and underlined in double diff comparision page");
    }

    @And("below words are showing as bold in paragraph {int} of article {int} in double diff comparision page")
    public void belowWordsAreShowingAsBoldInParagraphOfArticleInDoubleDiffComparisionPage(int arg0, int arg1, DataTable dataTable) {
        List<String> actualStrList = new ArrayList<>();
        List<String> giveStrList = dataTable.asList(String.class);
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg0 + "]" + LegalActPage.SPAN + LegalActPage.LEOS_DOUBLE_COMPARE_ADDED_ORIGINAL));
        for (WebElement ele : elementList) {
            actualStrList.add(getElementAttributeInnerText(ele));
        }
        assertTrue(actualStrList.containsAll(giveStrList), "Given options are not bold in paragraph in double diff comparision page");
    }

    @Then("recital {int} is displayed")
    public void recitalIsDisplayed(int arg0) {
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath(LegalActPage.RECITAL + "[" + arg0 + "]"));
        assertTrue(bool, "recital " + arg0 + " is not displayed");
    }

    @And("{string} is showing as bold in recital {int}")
    public void isShowingAsBoldInRecital(String arg0, int arg1) {
        WebElement ele = waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        assertNotNull(ele, "unable to load the page in the specified time duration");
        List<String> strList = new ArrayList<>();
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.RECITAL + "[" + arg1 + "]" + LegalActPage.AKNP + LegalActPage.SPAN + LegalActPage.LEOS_CONTENT_SOFT_NEW));
        for (WebElement element : elementList) {
            strList.add(getElementAttributeInnerText(element));
        }
        assertTrue(strList.contains(arg0), arg0 + " is showing as bold in recital " + arg1);
    }

    @When("select content in subparagraph {int} of paragraph {int} of article {int} in legal act page")
    public void selectContentInSubparagraphOfParagraphOfArticleInLegalActPage(int arg0, int arg1, int arg2) {
        E2eUtil.wait(2000);
        selectText(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.SUBPARAGRAPH + "[" + arg0 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP));
    }

    @Then("recitals section is not displayed")
    public void recitalsSectionIsNotDisplayed() {
        boolean bool = waitUnTillElementIsNotPresent(driver, LegalActPage.RECITALS_TEXT);
        assertTrue(bool, "recitals section is displayed");
    }

    @When("select content in point {int} of list {int} of paragraph {int} of article {int} in legal act page")
    public void selectContentInPointOfParagraphOfArticleInLegalActPage(int arg0, int arg1, int arg2, int arg3) {
        E2eUtil.wait(2000);
        selectText(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg3 + "]" + LegalActPage.PARAGRAPH + "[" + arg2 + "]" + LegalActPage.LIST + "[" + arg1 + "]" + LegalActPage.POINT + "[" + arg0 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP));
    }

    @Then("subparagraph {int} of paragraph {int} of article {int} is displayed")
    public void subparagraphOfParagraphOfArticleIsDisplayed(int arg0, int arg1, int arg2) {
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.SUBPARAGRAPH + "[" + arg0 + "]"));
        assertTrue(bool, "subparagraph " + arg0 + " of paragraph " + arg1 + " of article " + arg2 + " is not displayed");
    }

    @When("select content in paragraph {int} of article {int} in legal act page")
    public void selectContentInParagraphOfArticleInLegalActPage(int arg0, int arg1) {
        E2eUtil.wait(2000);
        selectText(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg0 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP));
    }

    @Then("recitals section is displayed")
    public void recitalsSectionIsDisplayed() {
        assertTrue(waitForElementTobeDisPlayed(driver, LegalActPage.RECITALS_TEXT), "recitals section is not displayed");
    }

    @When("click on position {int} commented portion in recital {int}")
    public void clickOnCommentedPortionInRecital(int arg0, int arg1) {
        elementClick(driver, By.xpath(LegalActPage.RECITAL + "[" + arg1 + "]" + LegalActPage.HYPOTHESIS_HIGHLIGHT + "[" + arg0 + "]"));
    }

    @When("click on position {int} suggested portion in recital {string}")
    public void clickOnSuggestedPortionInRecital(int arg0, String arg1) {
        elementClick(driver, By.xpath("//num[text()='" + arg1 + "']//parent::recital" + LegalActPage.HYPOTHESIS_HIGHLIGHT + "[" + arg0 + "]"));

    }

    @When("click on position {int} commented portion in point {int} of paragraph {int} of article {int} in legal act page")
    public void clickOnCommentedPortionInPointOfParagraphOfArticleInLegalActPage(int arg0, int arg1, int arg2, int arg3) {
        elementClick(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg3 + "]" + LegalActPage.PARAGRAPH + "[" + arg2 + "]" + LegalActPage.POINT + "[" + arg1 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP + LegalActPage.HYPOTHESIS_HIGHLIGHT + "[" + arg0 + "]"));
    }

    @And("click on position {int} commented portion in subparagraph {int} of paragraph {int} of article {int} in legal act page")
    public void clickOnPositionCommentedPortionInSubparagraphOfParagraphOfArticleInLegalActPage(int arg0, int arg1, int arg2, int arg3) {
        elementClick(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg3 + "]" + LegalActPage.PARAGRAPH + "[" + arg2 + "]" + LegalActPage.SUBPARAGRAPH + "[" + arg1 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP + LegalActPage.HYPOTHESIS_HIGHLIGHT + "[" + arg0 + "]"));
    }

    @And("{string} is showing as bold in subparagraph {int} of paragraph {int} of article {int} in legal act page")
    public void isShowingAsBoldInSubparagraphOfParagraphOfArticleInLegalActPage(String arg0, int arg1, int arg2, int arg3) {
        List<String> strList = new ArrayList<>();
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.ARTICLE + "[" + arg3 + "]" + LegalActPage.PARAGRAPH + "[" + arg2 + "]" + LegalActPage.SUBPARAGRAPH + "[" + arg1 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP + LegalActPage.SPAN + LegalActPage.LEOS_CONTENT_SOFT_NEW));
        for (WebElement ele : elementList) {
            strList.add(getElementAttributeInnerText(ele));
        }
        assertTrue(strList.contains(arg0), arg0 + " is showing as bold in subparagraph " + arg1);
    }

    @And("click on selected commented portion in recital {int}")
    public void clickOnSelectedCommentedPortionInRecital(int arg0) {
        elementClick(driver, By.xpath(LegalActPage.RECITAL + "[" + arg0 + "]" + LegalActPage.HYPOTHESIS_HIGHLIGHT_SELECTED));
    }

    @And("click on selected commented portion in point {int} of paragraph {int} of article {int} in legal act page")
    public void clickOnSelectedCommentedPortionInPointOfParagraphOfArticleInLegalActPage(int arg0, int arg1, int arg2) {
        String xpathStr = LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.POINT + "[" + arg0 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP;
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath(xpathStr + LegalActPage.HYPOTHESIS_HIGHLIGHT_SELECTED));
        if (bool) {
            elementClick(driver, By.xpath(xpathStr + LegalActPage.HYPOTHESIS_HIGHLIGHT_SELECTED));
        } else {
            elementClick(driver, By.xpath(xpathStr + LegalActPage.HYPOTHESIS_HIGHLIGHT + "[1]"));
        }
    }

    @And("click on selected commented portion in subparagraph {int} of paragraph {int} of article {int} in legal act page")
    public void clickOnSelectedCommentedPortionInSubparagraphOfParagraphOfArticleInLegalActPage(int arg0, int arg1, int arg2) {
        elementClick(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.SUBPARAGRAPH + "[" + arg0 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP + LegalActPage.HYPOTHESIS_HIGHLIGHT_SELECTED));
    }

    @And("no suggestion is present for subparagraph {int} of paragraph {int} of article {int} in legal act page")
    public void noSuggestionIsPresentForSubparagraphOfParagraphOfArticleInLegalActPage(int arg0, int arg1, int arg2) {
        boolean bool = waitUnTillElementIsNotPresent(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.SUBPARAGRAPH + "[" + arg0 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP + LegalActPage.HYPOTHESIS_HIGHLIGHT_SELECTED));
        assertTrue(bool, "at least one suggestion is present for subparagraph " + arg0);
    }

    @And("click on selected suggested portion in recital {int}")
    public void clickOnSelectedSuggestedPortionInRecitalInLegalActPage(int arg0) {
        elementClick(driver, By.xpath(LegalActPage.RECITAL + "[" + arg0 + "]" + LegalActPage.HYPOTHESIS_HIGHLIGHT_SELECTED));
    }

    @And("click on selected suggested portion in subparagraph {int} of paragraph {int} of article {int} in legal act page")
    public void clickOnSelectedSuggestedPortionInSubparagraphOfParagraphOfArticleInLegalActPage(int arg0, int arg1, int arg2) {
        elementClick(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.SUBPARAGRAPH + "[" + arg0 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP + LegalActPage.HYPOTHESIS_HIGHLIGHT_SELECTED));
    }

    @And("click on focused suggested portion in subparagraph {int} of paragraph {int} of article {int} in legal act page")
    public void clickOnFocusedSuggestedPortionInSubparagraphOfParagraphOfArticleInLegalActPage(int arg0, int arg1, int arg2) {
        elementClick(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.SUBPARAGRAPH + "[" + arg0 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP + LegalActPage.HYPOTHESIS_HIGHLIGHT_FOCUSED));
    }

    @And("click on bold icon present in ck editor panel")
    public void clickOnBoldIconPresentInCkEditorPanel() {
        elementClick(driver, CKEditorPage.BOLD_ICON);
    }

    @Then("no words are showing as bold in paragraph {int} of article {int}")
    public void noWordsAreShowingAsBoldInParagraphOfArticle(int arg0, int arg1) {
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg0 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP + LegalActPage.SPAN + LegalActPage.LEOS_CONTENT_SOFT_NEW));
        for (WebElement ele : elementList) {
            assertEquals(getElementAttributeInnerText(ele).trim().length(), 0, "new word length is more than 0");
        }
    }

    @And("no word is shown as bold and underlined in paragraph {int} of article {int} in single diff comparison page")
    public void noWordsIsShownAsBoldUnderlinedInParagraphOfArticleSingleDiffing(int arg0, int arg1) {
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg0 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP + LegalActPage.SPAN + LegalActPage.LEOS_CONTENT_NEW_CN));
        for (WebElement ele : elementList) {
            assertEquals(getElementAttributeInnerText(ele).trim().length(), 0, "new word length is more than 0");
        }
    }

    @And("no word is shown as bold and underlined in paragraph {int} of article {int} in double diff comparison page")
    public void noWordsIsShownAsBoldUnderlinedInParagraphOfArticleDoubleDiffing(int arg0, int arg1) {
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg0 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP + LegalActPage.SPAN + LegalActPage.LEOS_DOUBLE_COMPARE_ADDED_INTERMEDIATE));
        for (WebElement ele : elementList) {
            assertEquals(getElementAttributeInnerText(ele).trim().length(), 0, "new word length is more than 0");
        }
    }

    @When("append content {string} with the existing content in ck editor text box of a recital")
    public void appendContentWithTheExistingContentInCkEditorTextBoxOfARecital(String arg0) {
        String text = getElementAttributeInnerText(driver, LegalActPage.CK_EDTOR_PARAGRAPH_INNERTEXT);
        elementEcasSendkeys(driver, LegalActPage.CK_EDTOR_PARAGRAPH_INNERTEXT, text + arg0);
        E2eUtil.wait(2000);
    }

    @When("append content {string} with the existing content in ck editor text box of a paragraph inside article")
    public void appendContentStringWithTheExistingContentInCkEditorTextBoxOfAParagraphInsideArticle(String arg0) {
        String text = getElementAttributeInnerText(driver, LegalActPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI);
        elementEcasSendkeys(driver, LegalActPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI, text + arg0);
        E2eUtil.wait(2000);
    }

    @And("num tag is not present in paragraph {int} of article {int}")
    public void numberIsNotAddedToParagraphOfArticle(int arg1, int arg2) {
        boolean bool = waitUnTillElementIsNotPresent(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]/num"));
        assertTrue(bool, "num tag is present in paragraph " + arg1 + " of article " + arg2);
    }

    @When("double click on point {int} of list {int} of paragraph {int} of article {int}")
    public void doubleClickOnPointOfListOfParagraphOfArticle(int arg0, int arg1, int arg2, int arg3) {
        doubleClick(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg3 + "]" + LegalActPage.PARAGRAPH + "[" + arg2 + "]" + LegalActPage.LIST + "[" + arg1 + "]" + LegalActPage.POINT + "[" + arg0 + "]"));
    }

    @Then("{string} is showing in subparagraph {int} of paragraph {int} of article {int}")
    public void isShowingInSubparagraphOfParagraphOfArticle(String arg0, int arg1, int arg2, int arg3) {
        String str = getElementAttributeInnerText(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg3 + "]" + LegalActPage.PARAGRAPH + "[" + arg2 + "]" + LegalActPage.SUBPARAGRAPH + "[" + arg1 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP));
        assertEquals(str, arg0);
    }

    @And("number {string} is shown as grey and strikethrough in paragraph {int} of article {int}")
    public void numberIsShownAsGreyAndStrikethroughInParagraphOfArticle(String arg0, int arg1, int arg2) {
        WebElement ele = waitForElementTobePresent(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + "/num[text()='" + arg0 + "' and @class='leos-content-soft-removed']"));//.getAttribute("leos:softaction");
        assertNotNull(ele);
    }

    @And("data-akn number is showing as {string}")
    public void dataAknNumberIsShowingAs(String arg0) {
        WebElement ele = waitForElementTobePresent(driver, LegalActPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI);
        assertNotNull(ele);
        String str = ele.getAttribute("data-akn-num");
        assertEquals(str, arg0);
    }

    @Then("data-akn number attribute is not present inside ck editor")
    public void dataAknNumberAttributeIsNotPresentInsideCkEditor() {
        WebElement ele = waitForElementTobePresent(driver, LegalActPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI);
        assertNotNull(ele);
        String str = ele.getAttribute("data-akn-num");
        System.out.println("str : " + str);
        assertNull(str);
    }

    @When("double click on subparagraph {int} of paragraph {int} of article {int}")
    public void doubleClickOnSubparagraphOfParagraphOfArticle(int arg0, int arg1, int arg2) {
        doubleClick(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.SUBPARAGRAPH + "[" + arg0 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP));
    }

    @Then("{string} is showing in paragraph {int} of article {int}")
    public void isShowingInParagraphOfArticle(String arg0, int arg1, int arg2) {
        String str = getElementAttributeInnerText(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP));
        assertEquals(str, arg0);
    }

    @And("{string} is showing as grey and strikethrough in subparagraph {int} of paragraph {int} of article {int}")
    public void isShowingAsGreyAndStrikethroughInSubparagraphOfParagraphOfArticle(String arg0, int arg1, int arg2, int arg3) {
        String attr = waitForElementTobePresent(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg3 + "]" + LegalActPage.PARAGRAPH + "[" + arg2 + "]" + LegalActPage.SUBPARAGRAPH + "[" + arg1 + "]")).getAttribute("leos:indent-origin-num");
        assertEquals(attr, arg0);
    }

    @And("click on citation toggle link")
    public void clickOnCitationToggleLink() {
        elementClick(driver, LegalActPage.CITATIONS_TOGGLE_LINK);
    }

    @Then("citation {int} is displayed")
    public void citationIsDisplayed(int arg0) {
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath(LegalActPage.CITATION + "[" + arg0 + "]"));
        assertTrue(bool, "citation " + arg0 + " is not displayed");
    }

    @When("click on {string} of internal reference link {int} of citation {int}")
    public void clickOnOfInternalReferenceLinkOfCitation(String arg0, int arg1, int arg2) {
        elementClick(driver, By.xpath(LegalActPage.CITATION + "[" + arg2 + "]" + LegalActPage.AKNP + LegalActPage.MREF + "[" + arg1 + "]" + LegalActPage.REF + "[text()='" + arg0 + "']"));
    }

    @When("click on {string} of internal reference link {int} of recital {int}")
    public void clickOnOfInternalReferenceLinkOfRecital(String arg0, int arg1, int arg2) {
        elementClick(driver, By.xpath(LegalActPage.RECITAL + "[" + arg2 + "]" + LegalActPage.AKNP + LegalActPage.MREF + "[" + arg1 + "]" + LegalActPage.REF + "[text()='" + arg0 + "']"));
    }

    @When("click on {string} of internal reference link {int} of point {int} of paragraph {int} of article {int}")
    public void clickOnOfInternalReferenceLinkOfPointOfParagraphOfArticle(String arg0, int arg1, int arg2, int arg3, int arg4) {
        scrollTo(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg4 + "]" + LegalActPage.PARAGRAPH + "[" + arg3 + "]" + LegalActPage.POINT + "[" + arg2 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP));
        elementClick(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg4 + "]" + LegalActPage.PARAGRAPH + "[" + arg3 + "]" + LegalActPage.POINT + "[" + arg2 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP + LegalActPage.MREF + "[" + arg1 + "]" + LegalActPage.REF + "[text()='" + arg0 + "']"));
    }

    @When("click on {string} of internal reference link {int} of paragraph {int} of article {int}")
    public void clickOnOfInternalReferenceLinkOfParagraphOfArticle(String arg0, int arg1, int arg2, int arg3) {
        elementClick(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg3 + "]" + LegalActPage.PARAGRAPH + "[" + arg2 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP + LegalActPage.MREF + "[" + arg1 + "]" + LegalActPage.REF + "[text()='" + arg0 + "']"));
    }

    @When("click on internal reference icon present in ck editor panel")
    public void clickOnInternalReferenceIconPresentInCkEditorPanel() {
        elementClick(driver, CKEditorPage.INTERNAL_REFERENCE_ICON);
    }

    @Then("internal reference window is displayed")
    public void internalReferenceWindowIsDisplayed() {
        E2eUtil.wait(1000);
        WebElement ele = waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        assertNotNull(ele, "unable to load the page in the specified time duration");
        String title = getElementAttributeInnerText(driver, CKEditorPage.INTERNAL_REFERENCE_WINDOW);
        assertEquals(title, "Internal reference");
    }

    @And("click on ok button of internal reference window")
    public void clickOnOkButtonOfInternalReferenceWindow() {
        elementClick(driver, LegalActPage.OK_BUTTON_INTERNAL_REFERENCE_WINDOW);
    }

    @When("click on {string} link on the left side of internal reference window")
    public void clickOnLinkOnTheLeftSideOfInternalReferenceWindow(String arg0) {
        elementClickJS(driver, By.xpath(LegalActPage.CKE_DIALOG_CONTENTS + "//*[text()='" + arg0 + "']"));
    }

    @Then("click on point {int} of paragraph {int} of article on the right side of internal reference window")
    public void clickOnPointOfParagraphOfArticleOnTheRightSideOfInternalReferenceWindow(int arg0, int arg1) {
        elementClickJS(driver, By.xpath(LegalActPage.CKE_DIALOG_UI_HBOX_LAST + LegalActPage.ARTICLE + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.POINT + "[" + arg0 + "]"));
    }

    @And("click on paragraph {int} of article on the right side of internal reference window")
    public void clickOnParagraphOfArticleOnTheRightSideOfInternalReferenceWindow(int arg0) {
        elementClickJS(driver, By.xpath(LegalActPage.CKE_DIALOG_UI_HBOX_LAST + LegalActPage.ARTICLE + LegalActPage.PARAGRAPH + "[" + arg0 + "]"));
    }

    @When("click on point {int} of ordered list in ck editor")
    public void clickOnPointOfOrderedListInCkEditor(int arg0) {
        By by = By.xpath("//*[contains(@class,cke_editable_inline) and @role='textbox']//ol[@data-akn-name='aknOrderedList']//li[@data-akn-element='point'][" + arg0 + "]");
        //scrollTo(driver, by);
        elementClick(driver, by);
    }

    @Then("{string} is added to internal reference {int} of citation {int}")
    public void isAddedToInternalReferenceOfCitation(String arg0, int arg1, int arg2) {
        String innerText = getElementAttributeInnerText(driver, By.xpath(LegalActPage.CITATION + "[" + arg2 + "]" + LegalActPage.MREF + "[" + arg1 + "]"));
        assertEquals(innerText, arg0);
    }

    @Then("{string} is added to internal reference {int} of recital {int}")
    public void isAddedToInternalReferenceOfRecital(String arg0, int arg1, int arg2) {
        String innerText = getElementAttributeInnerText(driver, By.xpath(LegalActPage.RECITAL + "[" + arg2 + "]" + LegalActPage.MREF + "[" + arg1 + "]"));
        assertEquals(innerText, arg0);
    }

    @Then("{string} is added to internal reference {int} of point {int} of paragraph {int} of article {int}")
    public void isAddedToInternalReferenceOfPointOfParagraphOfArticle(String arg0, int arg1, int arg2, int arg3, int arg4) {
        String innerText = getElementAttributeInnerText(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg4 + "]" + LegalActPage.PARAGRAPH + "[" + arg3 + "]" + LegalActPage.POINT + "[" + arg2 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP + LegalActPage.MREF + "[" + arg1 + "]"));
        assertEquals(innerText, arg0);
    }

    @Then("{string} is added to internal reference {int} of paragraph {int} of article {int}")
    public void isAddedToInternalReferenceOfParagraphOfArticle(String arg0, int arg1, int arg2, int arg3) {
        String innerText = getElementAttributeInnerText(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg3 + "]" + LegalActPage.PARAGRAPH + "[" + arg2 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP + LegalActPage.MREF + "[" + arg1 + "]"));
        assertEquals(innerText, arg0);
    }

    @When("scroll to point {int} of ordered list in ck editor")
    public void scrollToPointOfOrderedListInCkEditor(int arg0) {
        By by = By.xpath("//*[contains(@class,cke_editable_inline) and @role='textbox']//ol[@data-akn-name='aknOrderedList']//li[@data-akn-element='point'][" + arg0 + "]");
        scrollTo(driver, by);
    }

    @And("there are only {int} paragraphs are present in article {int}")
    public void thereAreOnlyParagraphsArePresentInArticle(int arg0, int arg1) {
        int size = driver.findElements(By.xpath(LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH)).size();
        assertEquals(size, arg0);
    }

    @And("there are only {int} subparagraphs are present in paragraph {int} of article {int}")
    public void thereAreOnlySubparagraphsArePresentInParagraphOfArticle(int arg0, int arg1, int arg2) {
        int size = driver.findElements(By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.SUBPARAGRAPH)).size();
        assertEquals(size, arg0);
    }

    @And("below words are showing as bold in subparagraph {int} of paragraph {int} of article {int}")
    public void belowWordsAreShowingAsBoldInSubparagraphOfParagraphOfArticle(int arg0, int arg1, int arg2, DataTable dataTable) {
        List<String> addedStrList = new ArrayList<>();
        List<String> strList = dataTable.asList(String.class);
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.SUBPARAGRAPH + "[" + arg0 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP + LegalActPage.SPAN + LegalActPage.LEOS_CONTENT_SOFT_NEW));
        for (WebElement ele : elementList) {
            addedStrList.add(getElementAttributeInnerText(ele).trim());
        }
        assertTrue(addedStrList.containsAll(strList), "Given Options are not showing as bold in subparagraph " + arg0);
    }

    @And("num tag is not showing in bold for any numebered paragraph in article {int}")
    public void numTagIsNotShowingInBoldForAnyNumeberedParagraphInArticle(int arg0) {
        boolean bool = waitUnTillElementIsNotPresent(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg0 + "]" + LegalActPage.PARAGRAPH + LegalActPage.NUM + LegalActPage.LEOS_CONTENT_SOFT_NEW));
        assertTrue(bool);
    }

    @And("content is not showing in bold for any numebered paragraph in article {int}")
    public void contentIsNotShowingInBoldForAnyNumeberedParagraphInArticle(int arg0) {
        boolean bool = waitUnTillElementIsNotPresent(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg0 + "]" + LegalActPage.PARAGRAPH + LegalActPage.CONTENT + LegalActPage.AKNP + LegalActPage.SPAN + LegalActPage.LEOS_CONTENT_SOFT_NEW));
        assertTrue(bool);
    }

    @When("click on insert before icon present in show all actions icon of paragraph {int} of article {int}")
    public void clickOnInsertBeforeIconPresentInShowAllActionsIconOfParagraphOfArticle(int arg0, int arg1) {
        E2eUtil.wait(2000);
        WebElement subParagraph = driver.findElement(By.xpath(LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg0 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP));
        Actions actions = new Actions(driver);
        actions.moveToElement(subParagraph).build().perform();
        E2eUtil.wait(2000);
        WebElement showAllAction = driver.findElement(By.xpath(LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg0 + "]" + "//following-sibling::div[1][@class='leos-actions Vaadin-Icons']//span[@data-widget-type='show.all.actions']"));
        actions.moveToElement(showAllAction).build().perform();
        E2eUtil.wait(2000);
        WebElement insertAfter = driver.findElement(By.xpath(LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg0 + "]" + "//following-sibling::div[1][@class='leos-actions Vaadin-Icons']//span[@data-widget-type='insert.before']"));
        actions.moveToElement(insertAfter).build().perform();
        actions.click().build().perform();
        E2eUtil.wait(2000);
    }

    @When("click on insert after icon present in show all actions icon of paragraph {int} of article {int}")
    public void clickOnInsertAfterIconPresentInShowAllActionsIconOfParagraphOfArticle(int arg0, int arg1) {
        E2eUtil.wait(2000);
        WebElement subParagraph = driver.findElement(By.xpath(LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg0 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP));
        Actions actions = new Actions(driver);
        actions.moveToElement(subParagraph).build().perform();
        E2eUtil.wait(2000);
        WebElement showAllAction = driver.findElement(By.xpath(LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg0 + "]" + "//following-sibling::div[1][@class='leos-actions Vaadin-Icons']//span[@data-widget-type='show.all.actions']"));
        actions.moveToElement(showAllAction).build().perform();
        E2eUtil.wait(2000);
        WebElement insertAfter = driver.findElement(By.xpath(LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg0 + "]" + "//following-sibling::div[1][@class='leos-actions Vaadin-Icons']//span[@data-widget-type='insert.after']"));
        actions.moveToElement(insertAfter).build().perform();
        actions.click().build().perform();
        E2eUtil.wait(2000);
    }

    @Then("subparagraph {int} is not present in paragraph {int} of article {int}")
    public void subparagraphIsNotPresentInParagraphOfArticle(int arg0, int arg1, int arg2) {
        boolean bool = waitUnTillElementIsNotPresent(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.SUBPARAGRAPH + "[" + arg0 + "]"));
        assertTrue(bool);
    }

    @And("num {string} is present in point {int} of list {int} of paragraph {int} of article {int}")
    public void numIsPresentInPointOfListOfParagraphOfArticle(String arg0, int arg1, int arg2, int arg3, int arg4) {
        String text = getElementText(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg4 + "]" + LegalActPage.PARAGRAPH + "[" + arg3 + "]" + LegalActPage.LIST + "[" + arg2 + "]" + LegalActPage.POINT + "[" + arg1 + "]" + LegalActPage.NUM));
        assertEquals(text, arg0);
    }

    @And("number {string} is present in paragraph {int} of article {int}")
    public void numberIsPresentInParagraphOfArticle(String arg0, int arg1, int arg2) {
        String text = getElementAttributeInnerText(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + "/num"));
        assertEquals(text, arg0);
    }

    @And("num {string} is not shown in grey and strikethrough in point {int} of list {int} of paragraph {int} of article {int}")
    public void numIsNotGreyAndStrikethroughInPointOfListOfParagraphOfArticle(String arg0, int arg1, int arg2, int arg3, int arg4) {
        boolean bool = waitUnTillElementIsNotPresent(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg4 + "]" + LegalActPage.PARAGRAPH + "[" + arg3 + "]" + LegalActPage.LIST + "[" + arg2 + "]" + LegalActPage.POINT + "[" + arg1 + "]" + "/num" + LegalActPage.LEOS_CONTENT_SOFT_REMOVED));
        assertTrue(bool);
        String numText = getElementAttributeInnerText(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg4 + "]" + LegalActPage.PARAGRAPH + "[" + arg3 + "]" + LegalActPage.LIST + "[" + arg2 + "]" + LegalActPage.POINT + "[" + arg1 + "]" + "/num" + LegalActPage.NOT_LEOS_CONTENT_SOFT_REMOVED));
        assertEquals(numText, arg0);
    }

    @And("number {string} is not shown in grey and strikethrough in paragraph {int} of article {int}")
    public void numberIsNotGreyAndStrikethroughOfParagraphOfArticle(String arg0, int arg1, int arg2) {
        boolean bool = waitUnTillElementIsNotPresent(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + "/num" + LegalActPage.LEOS_CONTENT_SOFT_REMOVED));
        assertTrue(bool);
        String numText = getElementAttributeInnerText(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + "/num" + LegalActPage.NOT_LEOS_CONTENT_SOFT_REMOVED));
        assertEquals(numText, arg0);
    }

    @And("no subparagraph is present in paragraph {int} of article {int}")
    public void noSubparagraphIsPresentInParagraphOfArticle(int arg0, int arg1) {
        boolean bool = waitUnTillElementIsNotPresent(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg0 + "]" + LegalActPage.SUBPARAGRAPH));
        assertTrue(bool);
    }

    @And("the content {string} is displayed for paragraph {int} of article {int}")
    public void theContentIsDisplayedForParagraphOfArticle(String arg0, int arg1, int arg2) {
        String content = getElementText(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP));
        assertEquals(content, arg0);
    }

    @When("click on delete icon present in show all actions icon of point {int} of paragraph {int} of article {int}")
    public void click_on_delete_icon_present_in_show_all_actions_icon_of_point_of_paragraph_of_article(int arg0, int arg1, int arg2) {
        E2eUtil.wait(2000);
        Actions actions = new Actions(driver);
        WebElement point = driver.findElement(By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + "/list/point" + "[" + arg0 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP));
        actions.moveToElement(point).build().perform();
        E2eUtil.wait(2000);
        WebElement showAllAction = driver.findElement(By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + "/list/point" + "[" + arg0 + "]" + "//following-sibling::div[1][@class='leos-actions Vaadin-Icons']//span[@data-widget-type='show.all.actions']"));
        actions.moveToElement(showAllAction).build().perform();
        E2eUtil.wait(2000);
        WebElement delete = driver.findElement(By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + "/list/point" + "[" + arg0 + "]" + "//following-sibling::div[1][@class='leos-actions Vaadin-Icons']//span[@data-widget-type='delete']"));
        actions.moveToElement(delete).build().perform();
        actions.click().build().perform();
        E2eUtil.wait(2000);
    }

    @Then("point {int} of paragraph {int} of article {int} is showing as grey and strikethrough")
    public void point_of_paragraph_of_article_is_showing_as_grey_and_strikethrough(int arg0, int arg1, int arg2) {
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + "/list/point" + "[" + arg0 + "]" + LegalActPage.LEOS_CONTENT_SOFT_REMOVED));
        assertTrue(bool);
    }

    @When("click on delete icon present in show all actions icon of citation {int}")
    public void clickOnDeleteIconPresentInShowAllActionsIconOfCitation(int arg0) {
        E2eUtil.wait(2000);
        Actions actions = new Actions(driver);
        actions.moveToElement(waitForElementTobePresent(driver, By.xpath(LegalActPage.CITATION + "[" + arg0 + "]"))).build().perform();
        E2eUtil.wait(2000);
        actions.moveToElement(waitForElementTobePresent(driver, By.xpath(LegalActPage.CITATION + "[" + arg0 + "]" + AnnexPage.SHOW_ALL_ACTIONS_ICON))).build().perform();
        E2eUtil.wait(2000);
        actions.moveToElement(waitForElementTobePresent(driver, AnnexPage.SHOW_ALL_ACTIONS_DELETE)).click().release().build().perform();
        E2eUtil.wait(2000);
    }

    @And("citation {int} is showing as grey and strikethrough")
    public void citationIsShowingAsGreyAndStrikethrough(int arg0) {
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath(LegalActPage.CITATION + "[" + arg0 + "]" + LegalActPage.LEOS_CONTENT_SOFT_REMOVED));
        assertTrue(bool);
    }

    @Then("footnote number with marker {int} is showing in grey and strikethrough in footnote table")
    public void footnoteNumberWithMarkerIsShowingInGreyAndStrikethroughInFootnoteTable(int arg0) {
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath("//marker[text()='" + arg0 + "']//parent::span[@class='leos-authnote leos-content-soft-removed']"));
        assertTrue(bool);
    }

    @And("there is only {int} footnote present with marker {int} in footnote table")
    public void thereIsOnlyFootnotePresentWithMarker(int arg0, int arg1) {
        int count = 0;
        List<WebElement> elementList = driver.findElements(LegalActPage.AUTHORIAL_NOTE_TABLE);
        for (WebElement element : elementList) {
            if (getElementAttributeInnerText(element).equals(String.valueOf(arg1))) {
                count++;
            }
        }
        assertEquals(count, arg0);
    }

    @Then("point {int} of list {int} of paragraph {int} of article {int} is displayed")
    public void pointOfListOfParagraphOfArticleIsDisplayed(int arg0, int arg1, int arg2, int arg3) {
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg3 + "]" + LegalActPage.PARAGRAPH + "[" + arg2 + "]" + LegalActPage.LIST + "[" + arg1 + "]" + LegalActPage.POINT + "[" + arg0 + "]"));
        assertTrue(bool);
    }

    @And("num {string} is shown in grey and strikethrough in point {int} of list {int} of paragraph {int} of article {int}")
    public void numIsShownInGreyAndStrikethroughInPointOfListOfParagraphOfArticle(String arg0, int arg1, int arg2, int arg3, int arg4) {
        String num = getElementAttributeInnerText(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg4 + "]" + LegalActPage.PARAGRAPH + "[" + arg3 + "]" + LegalActPage.LIST + "[" + arg2 + "]" + LegalActPage.POINT + "[" + arg1 + "]" + LegalActPage.NUM_SINGLE_SLASH + LegalActPage.SPAN + LegalActPage.LEOS_CONTENT_SOFT_REMOVED));
        assertEquals(num, arg0);
    }

    @And("num {string} is shown in bold in point {int} of list {int} of paragraph {int} of article {int}")
    public void numIsShownInBoldInPointOfListOfParagraphOfArticle(String arg0, int arg1, int arg2, int arg3, int arg4) {
        String num = getElementAttributeInnerText(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg4 + "]" + LegalActPage.PARAGRAPH + "[" + arg3 + "]" + LegalActPage.LIST + "[" + arg2 + "]" + LegalActPage.POINT + "[" + arg1 + "]" + LegalActPage.NUM_SINGLE_SLASH + LegalActPage.SPAN + LegalActPage.LEOS_CONTENT_SOFT_NEW));
        assertEquals(num, arg0);
    }

    @Then("num {string} is shown in bold in paragraph {int} of article {int}")
    public void num_is_shown_in_bold_in_paragraph_of_article(String str, int arg0, int arg1) {
        String num = getElementAttributeInnerText(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg0 + "]" + LegalActPage.NUM_SINGLE_SLASH + LegalActPage.SPAN + LegalActPage.LEOS_CONTENT_SOFT_NEW));
        assertEquals(num, str);
    }

    @Then("user guidance is not present in the page")
    public void userGuidanceIsNotPresentInThePage() {
        boolean bool = waitUnTillElementIsNotPresent(driver, ExpMemoPage.GUIDANCE_SPAN);
        assertTrue(bool);
    }

    @Then("toc edit button is displayed and enabled")
    public void tocEditButtonIsEnabled() {
        boolean bool = waitForElementTobeDisPlayed(driver, LegalActPage.TOC_EDIT_BUTON);
        assertTrue(bool);
        boolean bool1 = verifyElementIsEnabled(driver, LegalActPage.TOC_EDIT_BUTON);
        assertTrue(bool1);
    }

    @And("num tag is not present in subparagraph {int} of paragraph {int} of article {int}")
    public void numTagIsNotPresentInSubparagraphOfParagraphOfArticle(int arg0, int arg1, int arg2) {
        boolean bool = waitUnTillElementIsNotPresent(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.SUBPARAGRAPH + "[" + arg0 + "]" + "/num"));
        assertTrue(bool);
    }

    @And("paragraph {int} of article {int} is not present")
    public void paragraphOfArticleIsNotPresent(int arg0, int arg1) {
        boolean bool = waitUnTillElementIsNotPresent(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg0 + "]"));
        assertTrue(bool);
    }

    @When("click on delete icon present in show all actions icon of recital {int}")
    public void clickOnDeleteIconPresentInShowAllActionsIconOfRecital(int arg0) {
        E2eUtil.wait(2000);
        Actions actions = new Actions(driver);
        actions.moveToElement(waitForElementTobePresent(driver, By.xpath(LegalActPage.RECITAL + "[" + arg0 + "]"))).build().perform();
        E2eUtil.wait(2000);
        actions.moveToElement(waitForElementTobePresent(driver, By.xpath(LegalActPage.RECITAL + "[" + arg0 + "]" + AnnexPage.SHOW_ALL_ACTIONS_ICON))).build().perform();
        E2eUtil.wait(2000);
        actions.moveToElement(waitForElementTobePresent(driver, AnnexPage.SHOW_ALL_ACTIONS_DELETE)).click().release().build().perform();
        E2eUtil.wait(2000);
    }

    @When("select content {string} from recital {int}")
    public void selectContentFromRecital(String arg0, int arg1) {
        E2eUtil.wait(2000);
        selectTextFromElement(driver, By.xpath(LegalActPage.RECITAL + "[" + arg1 + "]" + LegalActPage.AKNP), arg0);
    }

    @When("select content {string} from heading of article {int}")
    public void selectContentFromArticleHeading(String arg0, int arg1) {
        selectTextFromElement(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.HEADING), arg0);
    }

    @When("select content {string} from point {int} of list {int} of paragraph {int} of article {int}")
    public void selectContentInPointOfParagraphOfArticleInLegalActPage(String content, int arg0, int arg1, int arg2, int arg3) {
        E2eUtil.wait(2000);
        selectTextFromElement(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg3 + "]" + LegalActPage.PARAGRAPH + "[" + arg2 + "]" + LegalActPage.LIST + "[" + arg1 + "]" + LegalActPage.POINT + "[" + arg0 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP), content);
    }

    @Then("below words are showing as green and underline in citation {int}")
    public void belowWordsAreShowingAsGreenAndUnderlineInCitation(int arg0, DataTable dataTable) {
        List<String> actualOptionList = new ArrayList<>();
        List<String> givenOptionList = dataTable.asList(String.class);
        List<WebElement> elements = driver.findElements(By.xpath(LegalActPage.CITATION + "[" + arg0 + "]" + LegalActPage.AKNP + LegalActPage.SPAN + LegalActPage.LEOS_CONTENT_NEW));
        assertTrue(null != elements && !elements.isEmpty(), "no element present");
        for (WebElement element : elements) {
            actualOptionList.add(getElementAttributeInnerText(element).trim());
        }
        assertTrue(actualOptionList.containsAll(givenOptionList), "given options are not present in the actual options list");
    }

    @And("below words are showing as red and strikethrough in citation {int}")
    public void belowWordsAreShowingAsRedAndStrikethroughInCitation(int arg0, DataTable dataTable) {
        List<String> actualOptionList = new ArrayList<>();
        List<String> givenOptionList = dataTable.asList(String.class);
        List<WebElement> elements = driver.findElements(By.xpath(LegalActPage.CITATION + "[" + arg0 + "]" + LegalActPage.AKNP + LegalActPage.SPAN + LegalActPage.LEOS_CONTENT_REMOVED));
        assertTrue(null != elements && !elements.isEmpty(), "no element present");
        for (WebElement element : elements) {
            actualOptionList.add(getElementAttributeInnerText(element).trim());
        }
        assertTrue(actualOptionList.containsAll(givenOptionList), "given options are not present in the actual options list");
    }

    @And("below words are shown as green in paragraph {int} of new article {int}")
    public void belowWordsAreShownAsGreenInParagraphOfNewArticle(int arg0, int arg1, DataTable dataTable) {
        List<String> actualOptionList = new ArrayList<>();
        List<String> givenOptionList = dataTable.asList(String.class);
        List<WebElement> elements = driver.findElements(By.xpath(LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.LEOS_CONTENT_NEW + LegalActPage.PARAGRAPH + "[" + arg0 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP + LegalActPage.SPAN + LegalActPage.LEOS_CONTENT_NEW));
        assertTrue(null != elements && !elements.isEmpty(), "no element present");
        for (WebElement element : elements) {
            actualOptionList.add(getElementAttributeInnerText(element).trim());
        }
        assertTrue(actualOptionList.containsAll(givenOptionList), "given options are not present in the actual options list");
    }

    @And("num {string} is shown as green and underlined in new article {int}")
    public void numIsShownAsGreenAndUnderlinedInNewArticle(String arg0, int arg1) {
        String num = getElementAttributeInnerText(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.LEOS_CONTENT_NEW + "/num"));
        assertEquals(num, arg0);
    }

    @And("heading {string} is shown as green and bold in new article {int}")
    public void headingIsShownAsGreenAndBoldInNewArticle(String arg0, int arg1) {
        String heading = getElementAttributeInnerText(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.LEOS_CONTENT_NEW + "/heading/span" + LegalActPage.LEOS_CONTENT_NEW));
        assertEquals(heading, arg0);
    }

    @And("num {string} is shown as green in paragraph {int} of new article {int}")
    public void numIsShownAsGreenInParagraphOfNewArticle(String arg0, int arg1, int arg2) {
        String numParagraph = getElementAttributeInnerText(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.LEOS_CONTENT_NEW + LegalActPage.PARAGRAPH + "[" + arg1 + "]" +"/num"));
        assertEquals(numParagraph, arg0);
    }

    @And("num {string} is shown as red and strikethrough in article {int}")
    public void numIsShownAsRedAndStrikethroughInArticle(String arg0, int arg1) {
        String num = getElementAttributeInnerText(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg1 + "]" + "/num/span" + LegalActPage.LEOS_CONTENT_REMOVED));
        assertEquals(num, arg0);
    }

    @And("num {string} is shown as green and underlined in article {int}")
    public void numIsShownAsGreenAndUnderlinedInArticle(String arg0, int arg1) {
        String num = getElementAttributeInnerText(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg1 + "]" + "/num/span" + LegalActPage.LEOS_CONTENT_NEW));
        assertEquals(num, arg0);
    }

    @And("article {int} is showing as red and strikethrough")
    public void articleIsShowingAsRedAndStrikethrough(int arg0) {
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg0 + "]" + LegalActPage.LEOS_CONTENT_REMOVED));
        assertTrue(bool);
    }

    @When("click on contribution pane accordion")
    public void clickOnContributionPaneAccordion() {
        elementClick(driver, ContributionPanePage.CONTRIBUTION_PANE_ACCORDION);
    }

    @And("document title contains {string} in legal act page")
    public void documentTitleContainsInLegalActPage(String title) {
        String docTitle = getElementText(driver, LegalActPage.LEOS_DOC_TITLE);
        assertTrue(docTitle.contains(title));
    }

    @When("select {string} from content of paragraph {int} of article {int}")
    public void selectFromContentOfParagraphOfArticle(String arg0, int arg1, int arg2) {
        E2eUtil.wait(1000);
        selectTextFromElement(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP), arg0);
    }

    @When("select content {string} from the ck editor")
    public void selectContentFromTheCkEditor(String arg0) {
        E2eUtil.wait(2000);
        selectTextFromElement(driver, LegalActPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI, arg0);
    }

    @Then("{int} subparagraph exists in paragraph {int} of article {int}")
    public void subparagraphExistsInParagraphOfArticle(int arg0, int arg1, int arg2) {
        int size = driver.findElements(By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.SUBPARAGRAPH)).size();
        assertEquals(size, arg0);
    }

    @And("subparagraph {int} of paragraph {int} of article {int} is from {string} origin")
    public void subparagraphOfParagraphOfArticleIsFromEcOrigin(int arg0, int arg1, int arg2, String arg3) {
        WebElement element = waitForElementTobePresent(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.SUBPARAGRAPH + "[" + arg0 + "]"));
        String attribute = getAttributeValueFromElement(element, "leos:origin");
        assertEquals(attribute, arg3);
    }

    @Then("{int} alinea exists in point {int} of list {int} of paragraph {int} of article {int}")
    public void alineaExistsInPointOfListOfParagraphOfArticle(int arg0, int arg1, int arg2, int arg3, int arg4) {
        int size = driver.findElements(By.xpath(LegalActPage.ARTICLE + "[" + arg4 + "]" + LegalActPage.PARAGRAPH + "[" + arg3 + "]" + LegalActPage.LIST_SINGLE_SLASH + "[" + arg2 + "]" + LegalActPage.POINT_SINGLE_SLASH + "[" + arg1 + "]" + LegalActPage.ALINEA_SINGLE_SLASH)).size();
        assertEquals(size, arg0);
    }

    @And("alinea {int} of point {int} of list {int} of paragraph {int} of article {int} is from {string} origin")
    public void alineaOfPointOfListOfParagraphOfArticleIsFromEcOrigin(int arg0, int arg1, int arg2, int arg3, int arg4, String arg5) {
        WebElement element = waitForElementTobePresent(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg4 + "]" + LegalActPage.PARAGRAPH + "[" + arg3 + "]" + LegalActPage.LIST_SINGLE_SLASH + "[" + arg2 + "]" + LegalActPage.POINT_SINGLE_SLASH + "[" + arg1 + "]" + LegalActPage.ALINEA_SINGLE_SLASH + "[" + arg0 + "]"));
        String attribute = getAttributeValueFromElement(element, "leos:origin");
        assertEquals(attribute, arg5);
    }

    @And("below words are showing as normal text in alinea {int} of point {int} of list {int} of paragraph {int} of article {int}")
    public void belowWordsAreShowingAsNormalTextInAlineaOfPointOfListOfParagraphOfArticle(int arg0, int arg1, int arg2, int arg3, int arg4, DataTable dataTable) {
        List<String> addedStrList = new ArrayList<>();
        List<String> strList = dataTable.asList(String.class);
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.ARTICLE + "[" + arg4 + "]" + LegalActPage.PARAGRAPH + "[" + arg3 + "]" + LegalActPage.LIST_SINGLE_SLASH + "[" + arg2 + "]" + LegalActPage.POINT_SINGLE_SLASH + "[" + arg1 + "]" + LegalActPage.ALINEA_SINGLE_SLASH + "[" + arg0 + "]" +LegalActPage.CONTENT_SINGLE_SLASH + LegalActPage.AKNP_SINGLE_SLASH));
        for (WebElement ele : elementList) {
            addedStrList.add(getNodeTextFromElement(driver, ele).trim());
        }
        assertTrue(addedStrList.containsAll(strList), "Given Options are not showing as bold in subparagraph " + arg0);
    }

    @And("below words are showing as grey and strikethrough in alinea {int} of point {int} of list {int} of paragraph {int} of article {int}")
    public void belowWordsAreShowingAsGreyAndStrikethroughInAlineaOfPointOfListOfParagraphOfArticle(int arg0, int arg1, int arg2, int arg3, int arg4, DataTable dataTable) {
        List<String> addedStrList = new ArrayList<>();
        List<String> strList = dataTable.asList(String.class);
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.ARTICLE + "[" + arg4 + "]" + LegalActPage.PARAGRAPH + "[" + arg3 + "]" + LegalActPage.LIST_SINGLE_SLASH + "[" + arg2 + "]" + LegalActPage.POINT_SINGLE_SLASH + "[" + arg1 + "]" + LegalActPage.ALINEA_SINGLE_SLASH + "[" + arg0 + "]" +LegalActPage.CONTENT_SINGLE_SLASH + LegalActPage.AKNP_SINGLE_SLASH + LegalActPage.SPAN_SINGLE_SLASH + LegalActPage.LEOS_CONTENT_SOFT_REMOVED));
        for (WebElement ele : elementList) {
            addedStrList.add(getElementAttributeInnerText(ele).trim());
        }
        assertTrue(addedStrList.containsAll(strList), "Given Options are not showing as bold in subparagraph " + arg0);
    }

    @And("below words are showing as bold in alinea {int} of point {int} of list {int} of paragraph {int} of article {int}")
    public void belowWordsAreShowingAsBoldInAlineaOfPointOfListOfParagraphOfArticle(int arg0, int arg1, int arg2, int arg3, int arg4, DataTable dataTable) {
        List<String> addedStrList = new ArrayList<>();
        List<String> strList = dataTable.asList(String.class);
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.ARTICLE + "[" + arg4 + "]" + LegalActPage.PARAGRAPH + "[" + arg3 + "]" + LegalActPage.LIST_SINGLE_SLASH + "[" + arg2 + "]" + LegalActPage.POINT_SINGLE_SLASH + "[" + arg1 + "]" + LegalActPage.ALINEA_SINGLE_SLASH + "[" + arg0 + "]" +LegalActPage.CONTENT_SINGLE_SLASH + LegalActPage.AKNP_SINGLE_SLASH + LegalActPage.SPAN_SINGLE_SLASH + LegalActPage.LEOS_CONTENT_SOFT_NEW));
        for (WebElement ele : elementList) {
            addedStrList.add(getElementAttributeInnerText(ele).trim());
        }
        assertTrue(addedStrList.containsAll(strList), "Given Options are not showing as bold in subparagraph " + arg0);
    }

    @And("below words are showing as normal text in subparagraph {int} of paragraph {int} of article {int}")
    public void belowWordsAreShowingAsNormalTextInSubparagraphOfParagraphOfArticle(int arg0, int arg1, int arg2, DataTable dataTable) {
        List<String> addedStrList = new ArrayList<>();
        List<String> strList = dataTable.asList(String.class);
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.SUBPARAGRAPH + "[" + arg0 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP));
        for (WebElement ele : elementList) {
            addedStrList.add(getNodeTextFromElement(driver, ele).trim());
        }
        assertTrue(addedStrList.containsAll(strList), "Given Options are not showing as bold in subparagraph " + arg0);
    }

    @And("below words are showing as grey and strikethrough in subparagraph {int} of paragraph {int} of article {int}")
    public void belowWordsAreShowingAsGreyAndStrikethroughInSubparagraphOfParagraphOfArticle(int arg0, int arg1, int arg2, DataTable dataTable) {
        List<String> addedStrList = new ArrayList<>();
        List<String> strList = dataTable.asList(String.class);
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.ARTICLE + "[" + arg2 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.SUBPARAGRAPH + "[" + arg0 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP + LegalActPage.SPAN + LegalActPage.LEOS_CONTENT_SOFT_REMOVED));
        for (WebElement ele : elementList) {
            addedStrList.add(getElementAttributeInnerText(ele).trim());
        }
        assertTrue(addedStrList.containsAll(strList), "Given Options are not showing as bold in subparagraph " + arg0);
    }
}
