package europa.edit.stepdef;

import europa.edit.pages.AnnotationPage;
import europa.edit.pages.CommonPage;
import europa.edit.pages.ExpMemoPage;
import europa.edit.util.BaseDriver;
import europa.edit.util.E2eUtil;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import static europa.edit.util.Common.*;
import static europa.edit.util.E2eUtil.*;
import static org.testng.Assert.*;

public class AnnotationSteps extends BaseDriver {

    @Then("comment button is not displayed")
    public void commentButtonIsNotDisplayed() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement shadowHostElement = waitForElementTobePresent(driver, By.xpath("//hypothesis-adder"));
        SearchContext shadowRootElement = (SearchContext) js.executeScript("return arguments[0].shadowRoot", shadowHostElement);
        WebElement annotatorAdderActions = shadowRootElement.findElement(By.cssSelector(".annotator-adder-actions"));
        WebElement button = annotatorAdderActions.findElement(By.cssSelector("button.h-icon-annotate"));
        assertFalse(button.isDisplayed(), "comment button is displayed");
    }

    @And("highlight button is not displayed")
    public void highlightButtonIsNotDisplayed() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement shadowHostElement = waitForElementTobePresent(driver, By.xpath("//hypothesis-adder"));
        SearchContext shadowRootElement = (SearchContext) js.executeScript("return arguments[0].shadowRoot", shadowHostElement);
        WebElement annotatorAdderActions = shadowRootElement.findElement(By.cssSelector(".annotator-adder-actions"));
        WebElement button = annotatorAdderActions.findElement(By.cssSelector("button.h-icon-highlight"));
        assertFalse(button.isDisplayed(), "highlight button is displayed");
    }

    @And("suggest button is not displayed")
    public void suggestButtonIsNotDisplayed() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement shadowHostElement = waitForElementTobePresent(driver, By.xpath("//hypothesis-adder"));
        SearchContext shadowRootElement = (SearchContext) js.executeScript("return arguments[0].shadowRoot", shadowHostElement);
        WebElement annotatorAdderActions = shadowRootElement.findElement(By.cssSelector(".annotator-adder-actions"));
        WebElement button = annotatorAdderActions.findElement(By.cssSelector("button.js-suggestion-btn"));
        assertFalse(button.isDisplayed(), "suggest button is displayed");
    }

    @When("click on accept button present in selected suggest box")
    public void clickOnAcceptButtonPresentInSuggestBox() {
        scrollandClick(driver, AnnotationPage.SELECTED_SUGGESTION_BOX_ACCEPT_BUTTON);
        boolean bool = waitUnTillElementIsNotPresent(driver, By.xpath(CommonPage.XPATH_TEXT_1 + "Suggestion successfully merged" + CommonPage.XPATH_TEXT_1));
        assertTrue(bool, "Suggestion successfully merged message is still showing");
    }

    @Then("{string} is showing in the selected comment box")
    public void isShowingInTheSelectedCommentBox(String arg0) {
        boolean bool = false;
        List<WebElement> elementList = driver.findElements(AnnotationPage.SELECTED_COMMENT_BOX_PARAGRAPH);
        assertTrue(elementList.size() > 0, "no comment box present");
        for (WebElement element : elementList) {
            if (element.getText().contains(arg0)) {
                bool = true;
                break;
            }
        }
        assertTrue(bool, arg0 + " is not showing in the comment text box");
    }

    @When("click on edit icon of selected comment box")
    public void clickOnEditButtonInSelectedCommentBox() {
        scrollTo(driver, AnnotationPage.SELECTED_COMMENT_BOX_EDIT_ICON);
        waitForElementTobePresent(driver, AnnotationPage.SELECTED_COMMENT_BOX_EDIT_ICON);
        elementClickJS(driver, AnnotationPage.SELECTED_COMMENT_BOX_EDIT_ICON);
    }

    @Then("rich text area editor is displayed in selected comment box")
    public void richTextAreaIsDisplayedAndEditableInSelectedCommentBox() {
        scrollTo(driver, AnnotationPage.COMMENT_HIGHLIGHT_SUGGESTION_RICH_TEXTAREA_PARAGRAPH);
        boolean bool = waitForElementTobeDisPlayed(driver, AnnotationPage.COMMENT_HIGHLIGHT_SUGGESTION_RICH_TEXTAREA_PARAGRAPH);
        assertTrue(bool, "rich text area editor is not displayed in selected comment box");
    }

    @When("replace content {string} with existing content in rich text area of selected comment box")
    public void replaceContentWithExistingContentInRichTextAreaOfSelectedCommentBox(String arg0) {
        scrollTo(driver, AnnotationPage.COMMENT_HIGHLIGHT_SUGGESTION_RICH_TEXTAREA_PARAGRAPH);
        elementEcasSendkeys(driver, AnnotationPage.COMMENT_HIGHLIGHT_SUGGESTION_RICH_TEXTAREA_PARAGRAPH, arg0);
    }

    @Then("{string} is not present in rich text area of selected comment box")
    public void isNotPresentInRichTextAreaOfSelectedCommentBox(String arg0) {
        String str = getElementText(driver, AnnotationPage.COMMENT_HIGHLIGHT_SUGGESTION_RICH_TEXTAREA_PARAGRAPH);
        assertNotEquals(str, arg0, arg0 + " is present in rich text area of selected comment box");
    }

    @And("click on cancel button present in selected comment box")
    public void clickOnCancelButtonInSelectedCommentBox() {
        scrollTo(driver, AnnotationPage.SELECTED_COMMENT_BOX_CANCEL_BUTTON);
        waitForElementClickable(driver, AnnotationPage.SELECTED_COMMENT_BOX_CANCEL_BUTTON);
        elementClickJS(driver, AnnotationPage.SELECTED_COMMENT_BOX_CANCEL_BUTTON);
    }

    @When("click on reply button in selected comment box")
    public void clickOnReplyButtonInSelectedCommentBox() {
        scrollandClick(driver, AnnotationPage.SELECTED_COMMENT_BOX_REPLY_ICON);
    }

    @Then("add reply button is displayed and enabled in selected comment box")
    public void addReplyButtonIsDisplayedAndEnabled() {
        scrollTo(driver, AnnotationPage.SELECTED_COMMENT_BOX_ADD_REPLY_BUTTON);
        boolean bool = waitForElementTobeDisPlayed(driver, AnnotationPage.SELECTED_COMMENT_BOX_ADD_REPLY_BUTTON);
        assertTrue(bool, "add reply button is not displayed in selected comment box");
        boolean bool1 = verifyElementIsEnabled(driver, AnnotationPage.SELECTED_COMMENT_BOX_ADD_REPLY_BUTTON);
        assertTrue(bool1, "add reply button is not enabled in selected comment box");
    }

    @And("hide reply button is displayed and enabled in selected comment box")
    public void hideReplyButtonIsDisplayedAndEnabledInSelectedCommentBox() {
        scrollTo(driver, AnnotationPage.SELECTED_COMMENT_BOX_HIDE_REPLY_BUTTON);
        boolean bool = waitForElementTobeDisPlayed(driver, AnnotationPage.SELECTED_COMMENT_BOX_HIDE_REPLY_BUTTON);
        assertTrue(bool, "hide reply button is not displayed in selected comment box");
        boolean bool1 = verifyElementIsEnabled(driver, AnnotationPage.SELECTED_COMMENT_BOX_HIDE_REPLY_BUTTON);
        assertTrue(bool1, "hide reply button is not enabled in selected comment box");
    }

    @Then("{string} is showing in the reply list of selected comment box")
    public void isShowingInTheReplyListOfCommentBox(String arg0) {
        boolean bool = false;
        List<WebElement> elementList = driver.findElements(AnnotationPage.SELECTED_COMMENT_BOX_ALL_REPLY_LIST_PARAGRAPH_LIST);
        for (WebElement element : elementList) {
            if (arg0.equalsIgnoreCase(getElementAttributeInnerText(element))) {
                bool = true;
                break;
            }
        }
        assertTrue(bool, arg0 + " is not showing in the reply list of selected comment box");
    }

    @And("add justification button is displayed and enabled in selected suggestion box")
    public void addJustificationButtonIsDisplayedAndEnabledInSelectedSuggestionBox() {
        scrollTo(driver, AnnotationPage.SELECTED_SUGGESTION_BOX_ADD_JUSTIFICATION_LINK);
        boolean bool = waitForElementTobeDisPlayed(driver, AnnotationPage.SELECTED_SUGGESTION_BOX_ADD_JUSTIFICATION_LINK);
        assertTrue(bool, "add justification button is not displayed in selected suggestion box");
        boolean bool1 = verifyElementIsEnabled(driver, AnnotationPage.SELECTED_SUGGESTION_BOX_ADD_JUSTIFICATION_LINK);
        assertTrue(bool1, "add justification button is not enabled in selected suggestion box");
    }

    @And("click on add justification link of selected suggestion box")
    public void clickOnAddJustificationButtonInSelectedSuggestionBox() {
        scrollandClick(driver, AnnotationPage.SELECTED_SUGGESTION_BOX_ADD_JUSTIFICATION_LINK);
    }

    @And("enter {string} in justification box of selected suggest box")
    public void enterInJustificationBoxOfSelectedSuggestBox(String arg0) {
        scrollTo(driver, AnnotationPage.COMMENT_HIGHLIGHT_SUGGESTION_RICH_TEXTAREA_PARAGRAPH);
        elementEcasSendkeys(driver, AnnotationPage.COMMENT_HIGHLIGHT_SUGGESTION_RICH_TEXTAREA_PARAGRAPH, arg0);
    }

    @And("{string} is showing in justification list of selected suggest box")
    public void isShowingInJustificationListOfSelectedSuggestBox(String arg0) {
        boolean bool = false;
        List<WebElement> elementList = driver.findElements(AnnotationPage.SELECTED_SUGGESTION_BOX_JUSTIFICATION_PARAGRAPH_LIST);
        for (WebElement element : elementList) {
            if (arg0.equalsIgnoreCase(getElementAttributeInnerText(element))) {
                bool = true;
                break;
            }
        }
        assertTrue(bool, arg0 + " is not showing in justification list of selected suggest box");
    }

    @And("hide justification button is displayed and enabled in selected suggestion box")
    public void hideJustificationButtonIsDisplayedAndEnabledInSelectedSuggestionBox() {
        scrollTo(driver, AnnotationPage.SELECTED_SUGGESTION_BOX_HIDE_JUSTIFICATION_LINK);
        boolean bool = waitForElementTobeDisPlayed(driver, AnnotationPage.SELECTED_SUGGESTION_BOX_HIDE_JUSTIFICATION_LINK);
        assertTrue(bool, "hide justification button is not displayed in selected suggestion box");
        boolean bool1 = verifyElementIsEnabled(driver, AnnotationPage.SELECTED_SUGGESTION_BOX_HIDE_JUSTIFICATION_LINK);
        assertTrue(bool1, "hide justification button is not enabled in selected suggestion box");
    }


    @When("click on sort by button")
    public void clickOnSortByButton() {
        scrollandClick(driver, AnnotationPage.SORT_BY_BUTTON);
    }

    @Then("below options are showing in sort by list")
    public void belowOptionsAreShowingInSortByList(DataTable dataTable) {
        List<String> actualOptionList = new ArrayList<>();
        List<String> givenOptionList = dataTable.asList(String.class);
        List<WebElement> elementList = driver.findElements(AnnotationPage.SORT_BY_DROP_DOWN_LIST);
        for (WebElement element : elementList) {
            actualOptionList.add(getElementAttributeInnerText(element));
        }
        assertTrue(actualOptionList.containsAll(givenOptionList), "mentioned options might not be present in the actual options list");
    }

    @And("{string} option is default selection in sort by list")
    public void optionIsSelectedByDefaultInSortByList(String arg0) {
        String str = getElementAttributeInnerText(driver, AnnotationPage.SORT_BY_DROP_DOWN_LIST_SELECTED_OPTION);
        assertEquals(str, arg0);
    }

    @When("click on option {string} in sort by list")
    public void clickOnOptionInSortByList(String arg0) {
        elementClick(driver, By.xpath("//ul[@class='dropdown-menu pull-right']//li//a[text()='" + arg0 + "']//preceding-sibling::span[@class='dropdown-menu-radio']"));
    }

    @Then("all the annotations are showing in ascending order")
    public void allTheAnotationsAreShowingInAscendingOrder() throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy, hh:mm");
        boolean flag = true;
        Date date = null;
        String text;
        int size = Integer.parseInt(getElementText(driver, AnnotationPage.ANNOTATION_COUNT).trim());
        if (size > 1) {
            for (int i = 1; i <= size; i++) {
                text = getElementAttributeInnerText(driver, By.xpath("(//div[contains(@class,'thread-list__card')])[" + i + "]//timestamp//span[@class='annotation-header__timestamp']"));
                if (null != date) {
                    if (date.compareTo(df.parse(text)) <= 0) {
                        date = df.parse(text);
                    } else {
                        flag = false;
                        break;
                    }
                } else {
                    date = df.parse(text);
                }
            }
        }
        assertTrue(flag, "annotations are not showing in ascending order");
    }

    @Then("cancel button is not present in selected comment box")
    public void cancelButtonIsNotPresentInSelectedCommentBox() {
        boolean bool = waitUnTillElementIsNotPresent(driver, AnnotationPage.SELECTED_COMMENT_BOX_CANCEL_BUTTON);
        assertTrue(bool, "cancel button is present in selected comment box");
    }

    @Then("no comment box is selected")
    public void noCommentBoxIsSelected() {
        boolean bool = waitUnTillElementIsNotPresent(driver, AnnotationPage.SELECTED_COMMENT_BOX);
        assertTrue(bool, "at least one comment box is selected");
    }

    @Then("selected suggest text box is not present")
    public void selectedSuggestTextBoxIsNotPresent() {
        boolean bool = waitUnTillElementIsNotPresent(driver, AnnotationPage.SELECTED_SUGGESTION_BOX);
        assertTrue(bool, "at least one selected suggest text box is present");
    }

    @When("mouse hover on selected comment text box")
    public void mouseHoverOnSelectedCommentTextBox() {
        Actions act = new Actions(driver);
        WebElement ele = waitForElementTobePresent(driver, AnnotationPage.SELECTED_COMMENT_BOX);
        scrollToElement(driver, ele);
        act.moveToElement(ele).build().perform();
    }

    @When("click on the comment box having comment {string}")
    public void clickOnTheCommentBoxHavingComment(String arg0) {
        scrollandClick(driver, By.xpath(AnnotationPage.THREAD_LIST_CARD_IS_COMMENT + AnnotationPage.MARKDOWN_PREVIEW_HAS_CONTENT + "//p[text()='" + arg0 + "']"));
    }

    @Then("comment box having comment {string} is selected")
    public void commentBoxHavingCommentIsSelected(String arg0) {
        boolean bool = verifyElement(driver, By.xpath(AnnotationPage.MARKDOWN_PREVIEW_HAS_CONTENT + "//p[text()='" + arg0 + "']//ancestor::div[@class='thread-list__card is-comment is-annotation-selected']"));
        assertTrue(bool, "comment box having comment " + arg0 + " is selected");
    }

    @And("click on show justification link in selected suggestion box")
    public void clickOnShowJustificationLinkInSelectedSuggestionBox() {
        scrollandClick(driver, AnnotationPage.SELECTED_SUGGESTION_BOX_SHOW_JUSTIFICATION_LINK);
    }

    @When("click on reject button of selected suggest text box")
    public void rejectSelectedSuggestTextBox() {
        scrollandClick(driver, AnnotationPage.SELECTED_SUGGESTION_BOX_REJECT_BUTTON);
    }

    @Then("at least one comment box is selected")
    public void atLeastOneCommentBoxIsSelected() {
        boolean bool = driver.findElements(AnnotationPage.SELECTED_COMMENT_BOX).size() > 0;
        assertTrue(bool, "even one comment box is not selected");
    }

    @Then("at least one suggestion box is selected")
    public void atLeastOneSuggestBoxIsSelected() {
        boolean bool = driver.findElements(AnnotationPage.SELECTED_SUGGESTION_BOX).size() > 0;
        assertTrue(bool, "even one suggestion box is not selected");
    }

    @Then("comment box rich text area is displayed")
    public void comment_box_text_area_is_displayed() {
        scrollTo(driver, AnnotationPage.COMMENT_RICH_TEXTAREA);
        boolean bool = verifyElement(driver, AnnotationPage.COMMENT_RICH_TEXTAREA);
        assertTrue(bool, "comment box rich text area is not displayed");
    }

    @When("enter {string} in comment box rich textarea")
    public void enter_in_comment_box_textarea(String string) {
        scrollTo(driver, AnnotationPage.COMMENT_HIGHLIGHT_SUGGESTION_RICH_TEXTAREA_PARAGRAPH);
        elementEcasSendkeys(driver, AnnotationPage.COMMENT_HIGHLIGHT_SUGGESTION_RICH_TEXTAREA_PARAGRAPH, string);
    }

    @When("click on {string} annotation sharing setting")
    public void click_on_annotation_sharing_setting(String string) {
        if (string.equals("comment")) {
            scrollTo(driver, AnnotationPage.COMMENT_ARROW_DOWN_BUTTON);
            elementClick(driver, AnnotationPage.COMMENT_ARROW_DOWN_BUTTON);
        }
        if (string.equals("suggest")) {
            scrollTo(driver, AnnotationPage.SUGGESTION_ARROW_DOWN_BUTTON);
            elementClick(driver, AnnotationPage.SUGGESTION_ARROW_DOWN_BUTTON);
        }
        if (string.equals("highlight")) {
            scrollTo(driver, AnnotationPage.HIGHLIGHT_ARROW_DOWN_BUTTON);
            elementClick(driver, AnnotationPage.HIGHLIGHT_ARROW_DOWN_BUTTON);
        }
    }

    @Then("below groups are displayed in the annotation sharing setting list")
    public void below_groups_are_displayed_in_the_annotation_sharing_setting_list(DataTable dataTable) {
        String text;
        List<String> ActualSharingSettingList = new ArrayList<>();
        List<String> givenSharingSettingList = dataTable.asList(String.class);
        List<WebElement> elements = driver.findElements(AnnotationPage.PUBLISH_ANNOTATION_UL_LI_A);
        if (null != elements && !elements.isEmpty()) {
            for (WebElement element : elements) {
                text = getElementAttributeInnerText(element).trim();
                ActualSharingSettingList.add(text);
            }
            assertTrue(ActualSharingSettingList.containsAll(givenSharingSettingList), "Given Options are not present in the List");
        }
    }

    @When("click on {string} option in the annotation sharing setting list")
    public void click_on_option_in_the_annotation_sharing_setting_list(String string) {
        String text;
        List<WebElement> elements = driver.findElements(AnnotationPage.PUBLISH_ANNOTATION_UL_LI_A);
        if (null != elements && !elements.isEmpty()) {
            for (WebElement element : elements) {
                text = getElementAttributeInnerText(element).trim();
                if (text.equalsIgnoreCase(string)) {
                    scrollToElement(driver, element);
                    elementClickJS(driver, element);
                    break;
                }
            }
        }
    }

    @When("click on {string} publish button")
    public void click_on_publish_button(String string) {
        if (string.equals("comment")) {
            scrollTo(driver, AnnotationPage.COMMENT_PUBLISH_BUTTON);
            scrollandClick(driver, AnnotationPage.COMMENT_PUBLISH_BUTTON);
            E2eUtil.wait(5000);
        }
        if (string.equals("suggest")) {
            scrollTo(driver, AnnotationPage.SUGGESTION_PUBLISH_BUTTON);
            scrollandClick(driver, AnnotationPage.SUGGESTION_PUBLISH_BUTTON);
            E2eUtil.wait(5000);
        }
        if (string.equals("highlight")) {
            scrollandClick(driver, AnnotationPage.HIGHLIGHT_PUBLISH_BUTTON);
            E2eUtil.wait(5000);
        }
    }

    @Then("{string} is showing in the comment text box")
    public void is_showing_in_the_comment_text_box(String string) {
        String text;
        boolean bool = false;
        List<WebElement> elementList = driver.findElements(AnnotationPage.COMMENT_TEXTAREA_PARAGRAPH_INNERTEXT);
        assertTrue(elementList.size() > 0, "comment box doesn't exist");
        for (WebElement element : elementList) {
            text = element.getText();
            if (text.contains(string)) {
                bool = true;
                break;
            }
        }
        assertTrue(bool, string + " is not showing in the comment text box");
    }

    @Then("suggest textarea is displayed")
    public void suggest_box_text_area_is_displayed() {
        scrollTo(driver, AnnotationPage.SUGGESTION_TEXTAREA);
        boolean bool = verifyElement(driver, AnnotationPage.SUGGESTION_TEXTAREA);
        assertTrue(bool, "suggest textarea is not displayed");
    }

    @When("enter {string} in suggest box textarea")
    public void enter_in_suggest_box_textarea(String string) {
        scrollTo(driver, AnnotationPage.SUGGESTION_TEXTAREA);
        elementEcasSendkeys(driver, AnnotationPage.SUGGESTION_TEXTAREA, string);
    }

    @Then("{string} is showing in the suggest text box")
    public void is_showing_in_the_suggest_text_box(String string) {
        String text;
        boolean bool = false;
        List<WebElement> elementList = driver.findElements(AnnotationPage.SUGGESTION_TEXTAREA_PARAGRAPH_INNERTEXT);
        assertTrue(elementList.size() > 0, "suggestion test box doesn't exist");
        for (WebElement element : elementList) {
            text = element.getText();
            if (text.contains(string)) {
                bool = true;
                break;
            }
        }
        assertTrue(bool, string + " is not present in the suggest text box");
    }

    @Then("highlight text box is displayed")
    public void highlight_box_is_displayed() {
        scrollTo(driver, AnnotationPage.HIGHLIGHT_TEXTBOX);
        boolean bool = waitForElementTobePresent(driver, AnnotationPage.HIGHLIGHT_TEXTBOX).isDisplayed();
        assertTrue(bool, "highlight text box is not displayed");
    }

    @When("click on edit button on highlight box")
    public void click_on_edit_button_oh_highlight_box() {
        scrollTo(driver, AnnotationPage.HIGHLIGHT_TEXTBOX_EDIT_BUTTON);
        elementClick(driver, AnnotationPage.HIGHLIGHT_TEXTBOX_EDIT_BUTTON);
    }

    @Then("highlight rich textarea is displayed")
    public void highlightRichTextareaIsDisplayed() {
        scrollTo(driver, AnnotationPage.HIGHLIGHT_RICH_TEXTAREA);
        boolean bool = verifyElement(driver, AnnotationPage.HIGHLIGHT_RICH_TEXTAREA);
        assertTrue(bool);
    }

    @Then("highlight box textarea is displayed")
    public void highlight_box_textarea_is_displayed() {
        scrollTo(driver, AnnotationPage.HIGHLIGHT_TEXTAREA);
        boolean bool = verifyElement(driver, AnnotationPage.HIGHLIGHT_TEXTAREA);
        assertTrue(bool);
    }

    @When("enter {string} in highlight box rich textarea")
    public void enter_in_highlight_box_textarea(String string) {
        scrollTo(driver, AnnotationPage.COMMENT_HIGHLIGHT_SUGGESTION_RICH_TEXTAREA_PARAGRAPH);
        elementEcasSendkeys(driver, AnnotationPage.COMMENT_HIGHLIGHT_SUGGESTION_RICH_TEXTAREA_PARAGRAPH, string);
    }

    @Then("{string} is showing in the highlight text box")
    public void is_showing_in_the_highlight_text_box(String string) {
        String text;
        boolean bool = false;
        List<WebElement> elementList = driver.findElements(AnnotationPage.HIGHLIGHT_TEXTAREA_PARAGRAPH_INNERTEXT);
        assertTrue(elementList.size() > 0, "highlight box doesn't exist");
        for (WebElement element : elementList) {
            text = element.getText();
            if (text.contains(string)) {
                bool = true;
                break;
            }
        }
        assertTrue(bool, string + " is not present in the highlight text box");
    }

    @And("{string} button is showing in suggest text box")
    public void ButtonIsShowingInTextBox(String string) {
        if (string.equals("Accept")) {
            scrollTo(driver, AnnotationPage.SUGGESTION_ACCEPT_BUTTON);
            boolean bool = verifyElement(driver, AnnotationPage.SUGGESTION_ACCEPT_BUTTON);
            assertTrue(bool, "accept button is not displayed");
        }
        if (string.equals("Reject")) {
            scrollTo(driver, AnnotationPage.SUGGESTION_REJECT_BUTTON);
            boolean bool = verifyElement(driver, AnnotationPage.SUGGESTION_REJECT_BUTTON);
            assertTrue(bool, "reject button is not displayed");
        }
    }

    @When("mouse hover on highlight text box")
    public void mouseHoverOnHighlightTextBox() {
        Actions act = new Actions(driver);
        WebElement ele = waitForElementTobePresent(driver, AnnotationPage.HIGHLIGHT_TEXTBOX);
        act.moveToElement(ele).build().perform();
    }

    @When("mouse hover on comment text box")
    public void mouseHoverOnCommentTextBox() {
        Actions act = new Actions(driver);
        WebElement ele = waitForElementTobePresent(driver, By.xpath(AnnotationPage.COMMENT_TEXTBOX));
        act.moveToElement(ele).build().perform();
    }

    @When("click on delete icon of comment text box")
    public void deleteCommentTextBox() {
        scrollandClick(driver, AnnotationPage.COMMENT_TEXTBOX_DELETE_BUTTON);
    }

    @When("click on delete icon of highlight text box")
    public void deleteHighlightTextBox() {
        scrollandClick(driver, AnnotationPage.HIGHLIGHT_TEXTBOX_DELETE_BUTTON);
    }

    @When("click on reject button of suggest text box")
    public void rejectSuggestTextBox() {
        scrollandClick(driver, AnnotationPage.SUGGESTION_REJECT_BUTTON);
    }

    @Then("comment text box is not present")
    public void commentTextBoxIsNotPresent() {
        boolean bool = waitUnTillElementIsNotPresent(driver, By.xpath(AnnotationPage.COMMENT_TEXTBOX));
        assertTrue(bool, "comment text box is present");
    }

    @Then("highlight text box is not present")
    public void highlightTextBoxIsNotPresent() {
        boolean bool = waitUnTillElementIsNotPresent(driver, AnnotationPage.HIGHLIGHT_TEXTBOX);
        assertTrue(bool, "highlight text box is present");
    }

    @Then("suggest text box is not present")
    public void suggestTextBoxIsNotPresent() {
        boolean bool = waitUnTillElementIsNotPresent(driver, By.xpath(AnnotationPage.SUGGESTION_TEXTBOX));
        assertTrue(bool, "suggest text box is present");
    }

    @When("switch to {string} rich textarea iframe")
    public void switchToIframe(String arg0) {
        if (arg0.equalsIgnoreCase("comment")) {
            driver.switchTo().frame(driver.findElement(By.xpath(AnnotationPage.COMMENT_ANNOTATION + AnnotationPage.NG_SHOW_EDITOR + AnnotationPage.RICH_TEXTAREA_IFRAME)));
        }
        if (arg0.equalsIgnoreCase("highlight")) {
            driver.switchTo().frame(driver.findElement(By.xpath(AnnotationPage.HIGHLIGHT_ANNOTATION + AnnotationPage.NG_SHOW_EDITOR + AnnotationPage.RICH_TEXTAREA_IFRAME)));
        }
        if (arg0.equalsIgnoreCase("suggest")) {
            driver.switchTo().frame(driver.findElement(By.xpath(AnnotationPage.SUGGESTION_ANNOTATION + AnnotationPage.NG_SHOW_EDITOR + AnnotationPage.RICH_TEXTAREA_IFRAME)));
        }
    }

    @Then("below comments are showing in the comment text boxes")
    public void belowCommentsAreShowingInTheCommentTextBoxes(DataTable dataTable) {
        String text;
        List<String> actualCommentList = new ArrayList<>();
        List<String> givenCommentList = dataTable.asList(String.class);
        List<WebElement> elements = driver.findElements(AnnotationPage.ANNOTATION_COMMENT_PARAGRAPH);
        assertTrue(null != elements && !elements.isEmpty(), "no comment is present in the annotation list");
        for (WebElement element : elements) {
            text = element.getText();
            actualCommentList.add(text);
        }
        assertTrue(actualCommentList.containsAll(givenCommentList), "below comments are not showing in the comment text boxes");
    }

    @Then("below suggestions are showing in the suggestion text boxes")
    public void belowSuggestionsAreShowingInTheSuggestionTextBoxes(DataTable dataTable) {
        String text;
        List<String> actualSuggestionList = new ArrayList<>();
        List<String> givenSuggestionList = dataTable.asList(String.class);
        List<WebElement> elements = driver.findElements(AnnotationPage.ANNOTATION_SUGGESTION_CONTENT_NEW);
        assertTrue(null != elements && !elements.isEmpty(), "no suggestion is present in the annotation list");
        for (WebElement element : elements) {
            text = element.getText();
            actualSuggestionList.add(text);
        }
        assertTrue(actualSuggestionList.containsAll(givenSuggestionList), "below suggestions are not showing in the comment text boxes");
    }

    @When("click on comment box {int}")
    public void clickOnCommentBox(int arg0) {
        elementClick(driver, By.xpath(AnnotationPage.COMMENT_TEXTBOX + "[" + arg0 + "]"));
    }

    @When("click on suggest box {int}")
    public void clickOnSuggestBox(int arg0) {
        scrollandClick(driver, By.xpath(AnnotationPage.SUGGESTION_TEXTBOX + "[" + arg0 + "]"));
    }

    @And("mouse hover on selected suggest box")
    public void mouseHoverOnSelectedSuggestBox() {
        Actions act = new Actions(driver);
        WebElement ele = waitForElementTobePresent(driver, AnnotationPage.SELECTED_SUGGESTION_BOX);
        act.moveToElement(ele).build().perform();
    }

    @And("click on reply button in selected suggest box")
    public void clickOnReplyButtonInSelectedSuggestBox() {
        scrollandClick(driver, AnnotationPage.SELECTED_SUGGESTION_BOX_REPLY_ICON);
    }

    @Then("add reply button is displayed and enabled in selected suggest box")
    public void addReplyButtonIsDisplayedAndEnabledInSelectedSuggestBox() {
        scrollTo(driver, AnnotationPage.SELECTED_SUGGESTION_BOX_ADD_REPLY_BUTTON);
        boolean bool = waitForElementTobeDisPlayed(driver, AnnotationPage.SELECTED_SUGGESTION_BOX_ADD_REPLY_BUTTON);
        assertTrue(bool, "add reply button is not displayed in selected suggestion box");
        boolean bool1 = verifyElementIsEnabled(driver, AnnotationPage.SELECTED_SUGGESTION_BOX_ADD_REPLY_BUTTON);
        assertTrue(bool1, "add reply button is not enabled in selected suggestion box");
    }

    @Then("{string} is showing in the reply list of selected suggest box")
    public void isShowingInTheReplyListOfSelectedSuggestBox(String arg0) {
        boolean bool = false;
        List<WebElement> elementList = driver.findElements(AnnotationPage.SELECTED_SUGGESTION_BOX_ALL_REPLY_LIST_PARAGRAPH_LIST);
        assertTrue(elementList.size() > 0, "no selected suggestion box exists");
        for (WebElement element : elementList) {
            if (arg0.equalsIgnoreCase(getElementAttributeInnerText(element))) {
                bool = true;
                break;
            }
        }
        assertTrue(bool, arg0 + " is not showing in the reply list of selected suggestion box");
    }

    @Then("comment button is displayed")
    public void commentButtonShouldBeDisplayed() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement shadowHostElement = waitForElementTobePresent(driver, By.xpath("//hypothesis-adder"));
        SearchContext shadowRootElement = (SearchContext) js.executeScript("return arguments[0].shadowRoot", shadowHostElement);
        WebElement annotatorAddedActions = shadowRootElement.findElement(By.cssSelector(".annotator-adder-actions"));
        WebElement button = annotatorAddedActions.findElement(By.cssSelector("button.h-icon-annotate"));
        highlightElement(driver, button);
    }

    @Then("highlight button is displayed")
    public void highlightButtonShouldBeDisplayed() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement shadowHostElement = waitForElementTobePresent(driver, By.xpath("//hypothesis-adder"));
        SearchContext shadowRootElement = (SearchContext) js.executeScript("return arguments[0].shadowRoot", shadowHostElement);
        WebElement annotatorAddedActions = shadowRootElement.findElement(By.cssSelector(".annotator-adder-actions"));
        WebElement button = annotatorAddedActions.findElement(By.cssSelector("button.h-icon-highlight"));
        highlightElement(driver, button);
    }

    @When("click on annotation pop up button")
    public void clickOnAnnotationPopUpButton() {
        elementClick(driver, ExpMemoPage.ENABLE_ANNOTATION_POPUP);
    }

    @When("click on comment button")
    public void click_on_comment_button() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement shadowHostElement = waitForElementTobePresent(driver, By.xpath("//hypothesis-adder"));
        SearchContext shadowRootElement = (SearchContext) js.executeScript("return arguments[0].shadowRoot", shadowHostElement);
        WebElement annotatorAddedActions = shadowRootElement.findElement(By.cssSelector(".annotator-adder-actions"));
        WebElement button = annotatorAddedActions.findElement(By.cssSelector("button.h-icon-annotate"));
        highlightElement(driver, button);
        button.click();
    }

    @When("click on suggest button")
    public void click_on_suggest_button() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement shadowHostElement = waitForElementTobePresent(driver, By.xpath("//hypothesis-adder"));
        SearchContext shadowRootElement = (SearchContext) js.executeScript("return arguments[0].shadowRoot", shadowHostElement);
        WebElement annotatorAddedActions = shadowRootElement.findElement(By.cssSelector(".annotator-adder-actions"));
        WebElement button = annotatorAddedActions.findElement(By.cssSelector("button.js-suggestion-btn"));
        highlightElement(driver, button);
        button.click();
    }

    @When("click on highlight button")
    public void click_on_highlight_button() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement shadowHostElement = waitForElementTobePresent(driver, By.xpath("//hypothesis-adder"));
        SearchContext shadowRootElement = (SearchContext) js.executeScript("return arguments[0].shadowRoot", shadowHostElement);
        WebElement annotatorAddedActions = shadowRootElement.findElement(By.cssSelector(".annotator-adder-actions"));
        WebElement button = annotatorAddedActions.findElement(By.cssSelector("button.h-icon-highlight"));
        highlightElement(driver, button);
        button.click();
    }

    @Then("suggest button is displayed")
    public void suggestButtonIsDisplayed() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement shadowHostElement = waitForElementTobePresent(driver, By.xpath("//hypothesis-adder"));
        SearchContext shadowRootElement = (SearchContext) js.executeScript("return arguments[0].shadowRoot", shadowHostElement);
        WebElement annotatorAddedActions = shadowRootElement.findElement(By.cssSelector(".annotator-adder-actions"));
        WebElement button = annotatorAddedActions.findElement(By.cssSelector("button.js-suggestion-btn"));
        highlightElement(driver, button);
    }

    @And("wait for disappearance of the message suggestion successfully merged")
    public void waitForDisappearanceOfTheMessage() {
        boolean bool = waitUnTillElementIsNotPresent(driver, AnnotationPage.SUGGESTION_SUCCESSFULLY_MERGED_MESSAGE);
        assertTrue(bool, "Suggestion successfully merged message is still showing");
    }

    @Then("suggestion successfully merged is displayed")
    public void messageSuggestionSuccessfullyMergedIsDisplayed() {
        boolean bool = waitForElementTobeDisPlayed(driver, AnnotationPage.SUGGESTION_SUCCESSFULLY_MERGED_MESSAGE);
        assertTrue(bool, "Suggestion successfully merged" + " message is not displayed");
    }

    @Then("comment, suggest and highlight buttons are not displayed")
    public void commentSuggestHighlightButtonIsNotDisplayed() {
        boolean bool = false;
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement shadowHostElement = waitForElementTobePresent(driver, By.xpath("//hypothesis-adder"));
        SearchContext shadowRootElement = (SearchContext) js.executeScript("return arguments[0].shadowRoot", shadowHostElement);
        WebElement hypothesisAdderToolbar = shadowRootElement.findElement(By.cssSelector("hypothesis-adder-toolbar.annotator-adder[style='visibility: hidden;']"));
        if (null != hypothesisAdderToolbar) {
            bool = true;
        }
        assertTrue(bool, "comment, suggest and highlight buttons are displayed");
    }

    @Then("suggest button is disabled")
    public void suggestButtonIsDisabled() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement shadowHostElement = waitForElementTobePresent(driver, By.xpath("//hypothesis-adder"));
        SearchContext shadowRootElement = (SearchContext) js.executeScript("return arguments[0].shadowRoot", shadowHostElement);
        WebElement annotatorAddedActions = shadowRootElement.findElement(By.cssSelector(".annotator-adder-actions"));
        WebElement suggestButton = annotatorAddedActions.findElement(By.cssSelector("button.js-suggestion-btn.annotator-disabled"));
        highlightElement(driver, suggestButton);
    }

    @And("click on post button")
    public void clickOnPostButton() {
        elementClick(driver, AnnotationPage.REPLY_POST_BTN);
    }

    @And("annotation side bar is present")
    public void isAnnotationSideBarPresent() {
        boolean bool = waitForElementTobeDisPlayed(driver, AnnotationPage.ANNOTATION_TOOLBAR);
        assertTrue(bool);
    }

    @Then("selection pane is displayed")
    public void selectionPaneIsDisplayed() {
        boolean bool = waitForElementTobeDisPlayed(driver, AnnotationPage.SELECTION_PANE);
        assertTrue(bool);
    }

    @And("{string} label is displayed in selection pane")
    public void labelIsDisplayedInSelectionPane(String arg0) {
        String label = getElementAttributeInnerText(driver, AnnotationPage.LEOS_SELECTION_PANE_LABEL);
        assertEquals(label, arg0);
    }

    @And("{string} action label is displayed in selection pane")
    public void actionLabelIsDisplayedInSelectionPane(String arg0) {
        String label = getElementAttributeInnerText(driver, AnnotationPage.LEOS_SELECTION_PANE_ACTION_LABEL);
        assertEquals(label, arg0);
    }

    @And("{string} option is selected by default in selection pane")
    public void optionIsSelectedByDefaultInSelectionPane(String arg0) {
        boolean bool = verifyElementIsEnabled(driver, By.xpath("//*[@class='leos-selection-pane__button' and text()='"+arg0+"']"));
        assertTrue(bool);
    }

    @And("below options are showing for click on annotation to select several or in selection pane")
    public void belowOptionsAreShowingForClickOnAnnotationToSelectSeveralOrInSelectionPane(DataTable dataTable) {
        String text;
        List<String> ActualSharingSettingList = new ArrayList<>();
        List<String> givenSharingSettingList = dataTable.asList(String.class);
        List<WebElement> elements = driver.findElements(AnnotationPage.LEOS_SELECTION_PANE_BUTTON);
        if (null != elements && !elements.isEmpty()) {
            for (WebElement element : elements) {
                text = getElementAttributeInnerText(element).trim();
                ActualSharingSettingList.add(text);
            }
            assertTrue(ActualSharingSettingList.containsAll(givenSharingSettingList), "Given Options are not present in the List");
        }
    }

    @And("below options are showing for actions on selected annotations in selection pane")
    public void belowOptionsAreShowingForActionsOnSelectedAnnotationsInSelectionPane(DataTable dataTable) {
        String text;
        List<String> ActualSharingSettingList = new ArrayList<>();
        List<String> givenSharingSettingList = dataTable.asList(String.class);
        List<WebElement> elements = driver.findElements(AnnotationPage.LEOS_SELECTION_PANE_ACTIONS_BUTTON);
        if (null != elements && !elements.isEmpty()) {
            for (WebElement element : elements) {
                text = getElementAttributeInnerText(element).trim();
                ActualSharingSettingList.add(text);
            }
            assertTrue(ActualSharingSettingList.containsAll(givenSharingSettingList), "Given Options are not present in the List");
        }
    }

    @Then("there is no annotations in this group")
    public void thereIsNoAnnotationsInThisGroup() {
        String message = getElementAttributeInnerText(driver, AnnotationPage.ANNOTATION_UNAVAILABLE_MESSAGE_LABEL);
        assertTrue(message.contains("There are no annotations in this group."));
    }

    @When("click on the checkbox of mark as processed element in comment {int}")
    public void clickOnTheCheckboxOfMarkAsProcessedElementInComment(int arg0) {
        List<WebElement> elementList =  driver.findElements(AnnotationPage.ANNOTATION_COMMENT);
        WebElement element = elementList.get(arg0-1).findElement(AnnotationPage.ANNOTATION_ACTION_TREATED_INPUT);
        scrollAndClick(driver, element);
    }

    @When("click on the checkbox of mark as processed element in suggestion {int}")
    public void clickOnTheCheckboxOfMarkAsProcessedElementInSuggestion(int arg0) {
        List<WebElement> elementList =  driver.findElements(AnnotationPage.ANNOTATION_SUGGESTION);
        WebElement element = elementList.get(arg0-1).findElement(AnnotationPage.ANNOTATION_ACTION_TREATED_INPUT);
        scrollAndClick(driver, element);
    }

    @Then("total annotation count is {int} in annotation module")
    public void totalAnnotationCountIsInAnnotationModule(int arg0) {
        int count = Integer.parseInt(getElementAttributeInnerText(driver, AnnotationPage.ANNOTATION_COUNT).trim());
        assertEquals(count, arg0);
    }

    @And("mark as processed text is present in the footer of comment {int}")
    public void markAsProcessedTextIsPresentWithCheckBoxNotTickedInComment(int arg0) {
        List<WebElement> elementList =  driver.findElements(AnnotationPage.ANNOTATION_COMMENT);
        String text = getElementAttributeInnerText(elementList.get(arg0-1).findElement(AnnotationPage.ANNOTATION_ACTION_TREATED_SPAN));
        assertEquals(text, "Mark as processed");
    }

    @And("mark as processed text is present in the footer of suggestion {int}")
    public void markAsProcessedTextIsPresentWithCheckBoxNotTickedInSuggestion(int arg0) {
        List<WebElement> elementList =  driver.findElements(AnnotationPage.ANNOTATION_SUGGESTION);
        String text = getElementAttributeInnerText(elementList.get(arg0-1).findElement(AnnotationPage.ANNOTATION_ACTION_TREATED_SPAN));
        assertEquals(text, "Mark as processed");
    }

    @And("mark as processed text is not present in comment action footer")
    public void markAsProcessedTextIsNotPresentInCommentActionFooter() {
        boolean bool = waitUnTillElementIsNotPresent(driver, AnnotationPage.ANNOTATION_COMMENT_MARK_AS_PROCESSED);
        assertTrue(bool);
    }

    @And("mark as processed text is not present in suggestion action footer")
    public void markAsProcessedTextIsNotPresentInSuggestionActionFooter() {
        boolean bool = waitUnTillElementIsNotPresent(driver, AnnotationPage.ANNOTATION_SUGGESTION_MARK_AS_PROCESSED);
        assertTrue(bool);
    }
}
