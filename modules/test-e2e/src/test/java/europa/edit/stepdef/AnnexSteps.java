package europa.edit.stepdef;

import europa.edit.pages.AnnexPage;
import europa.edit.pages.CommonPage;
import europa.edit.pages.LegalActPage;
import europa.edit.util.BaseDriver;
import europa.edit.util.E2eUtil;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import java.util.ArrayList;
import java.util.List;
import static europa.edit.util.Common.*;
import static europa.edit.util.E2eUtil.scrollandClick;
import static org.testng.Assert.*;

public class AnnexSteps extends BaseDriver {
    @Then("Annex page is displayed")
    public void annexPageIsDisplayed() {
        WebElement ele = waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        assertNotNull(ele, "unable to load the page in the specified time duration");
        boolean bool = verifyElement(driver, AnnexPage.ANNEX);
        assertTrue(bool, "Annex page is not displayed");
    }

    @And("preface and body is present in annex navigation pane")
    public void prefaceAndBodyIsPresentInAnnexNavigationPane() {
        boolean bool = verifyElement(driver, AnnexPage.PREFACE);
        assertTrue(bool, "preface is not present in annex navigation pane");
        boolean bool1 = verifyElement(driver, AnnexPage.BODY);
        assertTrue(bool1, "body is not present in annex navigation pane");
    }

    @When("click on insert before icon present in show all actions icon of level {int}")
    public void clickOnInsertBeforeIcon(int arg0) {
        E2eUtil.wait(2000);
        Actions actions = new Actions(driver);
        WebElement ele = waitForElementTobePresent(driver, By.xpath(AnnexPage.LEVEL + "[" + arg0 + "]"));
        actions.moveToElement(ele).build().perform();
        E2eUtil.wait(2000);
        WebElement ele1 = waitForElementTobePresent(driver, By.xpath(AnnexPage.LEVEL + "[" + arg0 + "]" + AnnexPage.SHOW_ALL_ACTIONS_ICON));
        actions.moveToElement(ele1).build().perform();
        E2eUtil.wait(2000);
        WebElement ele2 = waitForElementTobePresent(driver, AnnexPage.SHOW_ALL_ACTIONS_INSERT_BEFORE);
        actions.moveToElement(ele2).click().release().build().perform();
        E2eUtil.wait(2000);
    }

    @When("click on edit icon present in show all actions icon of level {int}")
    public void clickOnEditIconPresentInShowAllActionsIcon(int arg0) {
        E2eUtil.wait(2000);
        Actions actions = new Actions(driver);
        actions.moveToElement(waitForElementTobePresent(driver, By.xpath(AnnexPage.LEVEL + "[" + arg0 + "]"))).build().perform();
        E2eUtil.wait(2000);
        actions.moveToElement(waitForElementTobePresent(driver, By.xpath(AnnexPage.LEVEL + "[" + arg0 + "]" + AnnexPage.SHOW_ALL_ACTIONS_ICON))).build().perform();
        E2eUtil.wait(3000);
        actions.moveToElement(waitForElementTobePresent(driver, AnnexPage.SHOW_ALL_ACTIONS_EDIT)).click().release().build().perform();
        E2eUtil.wait(2000);
    }

    @When("click on insert after icon present in show all actions icon of level {int}")
    public void clickOnInsertAfterIconPresentInShowAllActionsIcon(int arg0) {
        E2eUtil.wait(2000);
        Actions actions = new Actions(driver);
        actions.moveToElement(waitForElementTobePresent(driver, By.xpath(AnnexPage.LEVEL + "[" + arg0 + "]"))).build().perform();
        E2eUtil.wait(2000);
        actions.moveToElement(waitForElementTobePresent(driver, By.xpath(AnnexPage.LEVEL + "[" + arg0 + "]" + AnnexPage.SHOW_ALL_ACTIONS_ICON))).build().perform();
        E2eUtil.wait(3000);
        actions.moveToElement(waitForElementTobePresent(driver, AnnexPage.SHOW_ALL_ACTIONS_INSERT_AFTER)).click().release().build().perform();
        E2eUtil.wait(2000);
    }

    @And("{int} level is present in the body of annex page")
    public void levelPresentInTheBodyOfAnnexPage(int arg0) {
        int size = driver.findElements(By.xpath(AnnexPage.LEVEL)).size();
        assertEquals(size, arg0, arg0 + " level is not present in the body of annex page");
    }

    @When("append {string} at the end of the content of level")
    public void appendAtTheEndOfTheContentOfLevel(String arg0) {
        String existingText = getElementAttributeInnerText(driver, LegalActPage.CK_EDTOR_PARAGRAPH_INNERTEXT);
        String newText = existingText + arg0;
        elementEcasSendkeys(driver, LegalActPage.CK_EDTOR_PARAGRAPH_INNERTEXT, newText);
    }

    @And("{string} is added to content of level {int}")
    public void isAddedToContentOfLevel(String arg0, int arg1) {
        String text = getElementText(driver, By.xpath(AnnexPage.LEVEL + "[" + arg1 + "]" + AnnexPage.AKNP));
        assertTrue(text.contains(arg0), arg0 + " is not added to content of level " + arg1);
    }

    @When("double click on level {int}")
    public void doubleClickOnLevel(int arg0) {
        By by = By.xpath(AnnexPage.LEVEL + "[" + arg0 + "]" + "//content");
        boolean bool = waitForElementTobeDisPlayed(driver, by);
        assertTrue(bool, "level is not displayed");
        doubleClick(driver, by);
    }

    @When("remove {string} from the content of level")
    public void removeFromTheContentOfLevel(String arg0) {
        String existingText = getElementAttributeInnerText(driver, LegalActPage.CK_EDTOR_PARAGRAPH_INNERTEXT);
        String deleteText = existingText.replace(arg0, "");
        elementEcasSendkeys(driver, LegalActPage.CK_EDTOR_PARAGRAPH_INNERTEXT, deleteText);
    }

    @And("{string} is removed from content of level {int}")
    public void isRemovedFromContentOfLevel(String arg0, int arg1) {
        String text = getElementText(driver, By.xpath(AnnexPage.LEVEL + "[" + arg1 + "]" + AnnexPage.AKNP));
        assertFalse(text.contains(arg0), arg0 + " is not removed from content of level " + arg1);
    }

    @When("click on close button present in annex page")
    public void clickOnCloseButtonPresentInAnnexPage() {
        elementClick(driver, AnnexPage.CLOSE_BUTTON);
    }

    @And("total number of level is {int}")
    public void totalNumberOfLevelIs(int arg0) {
        List<WebElement> elementList = driver.findElements(By.xpath(AnnexPage.LEVEL));
        assertEquals(elementList.size(), arg0, "total number of level is " + elementList.size());
    }

    @When("click on delete icon present in show all actions icon of level {int}")
    public void clickOnDeleteIconPresentInShowAllActionsIcon(int arg0) {
        E2eUtil.wait(2000);
        Actions actions = new Actions(driver);
        actions.moveToElement(waitForElementTobePresent(driver, By.xpath(AnnexPage.LEVEL + "[" + arg0 + "]"))).build().perform();
        E2eUtil.wait(2000);
        actions.moveToElement(waitForElementTobePresent(driver, By.xpath(AnnexPage.LEVEL + "[" + arg0 + "]" + AnnexPage.SHOW_ALL_ACTIONS_ICON))).build().perform();
        E2eUtil.wait(2000);
        actions.moveToElement(waitForElementTobePresent(driver, AnnexPage.SHOW_ALL_ACTIONS_DELETE)).click().release().build().perform();
        E2eUtil.wait(2000);
    }

    @When("scroll to level {int} in the content page")
    public void scrollToLevelInTheContentPage(int arg0) {
        scrollTo(driver, By.xpath(AnnexPage.LEVEL + "[" + arg0 + "]"));
    }

    @When("click on element {int} in annex")
    public void clickOnElementInAnnex(int arg0) {
        int index = arg0 + 2;
        scrollandClick(driver, By.xpath(AnnexPage.TOC_TABLE_TR + "[" + index + "]" + "//div[contains(@class,'gwt-HTML')]"));
    }

    @Then("content of subparagraph {int} of paragraph {int} is displayed in annex page")
    public void contentOfSubparagraphOfParagraphIsDisplayedInAnnexPage(int arg0, int arg1) {
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath(AnnexPage.PARAGRAPH + "[" + arg1 + "]" + AnnexPage.SUBPARAGRAPH + "[" + arg0 + "]" + AnnexPage.CONTENT + AnnexPage.AKNP));
        assertTrue(bool, "content of subparagraph " + arg0 + " of paragraph " + arg1 + " is not displayed in annex page");
    }

    @When("double click on the content of subparagraph {int} of paragraph {int} in annex page")
    public void doubleClickOnTheContentOfSubparagraphOfParagraphInAnnexPage(int arg0, int arg1) {
        doubleClick(driver, By.xpath(AnnexPage.PARAGRAPH + "[" + arg1 + "]" + AnnexPage.SUBPARAGRAPH + "[" + arg0 + "]" + AnnexPage.CONTENT + AnnexPage.AKNP));
        E2eUtil.wait(2000);
    }

    @And("below words are showing as bold in subparagraph {int} of paragraph {int} in annex page")
    public void belowWordsAreShowingAsBoldInSubparagraphOfParagraphInAnnexPage(int arg0, int arg1, DataTable dataTable) {
        List<String> addedStrList = new ArrayList<>();
        List<String> strList = dataTable.asList(String.class);
        List<WebElement> elementList = driver.findElements(By.xpath(AnnexPage.PARAGRAPH + "[" + arg1 + "]" + AnnexPage.SUBPARAGRAPH + "[" + arg0 + "]" + AnnexPage.CONTENT + AnnexPage.AKNP + AnnexPage.SPAN + AnnexPage.LEOS_CONTENT_SOFT_NEW));
        for (WebElement ele : elementList) {
            addedStrList.add(getElementAttributeInnerText(ele).trim());
        }
        assertTrue(addedStrList.containsAll(strList), "Given options are not showing as bold in subparagraph " + arg0);
    }

    @And("below words are showing as grey and strikethrough in subparagraph {int} of paragraph {int} in annex page")
    public void belowWordsAreShowingAsGreyAndStrikethroughInSubparagraphOfParagraphInAnnexPage(int arg0, int arg1, DataTable dataTable) {
        List<String> strList = dataTable.asList(String.class);
        List<String> deletedStrList = new ArrayList<>();
        List<WebElement> elementList = driver.findElements(By.xpath(AnnexPage.PARAGRAPH + "[" + arg1 + "]" + AnnexPage.SUBPARAGRAPH + "[" + arg0 + "]" + AnnexPage.CONTENT + AnnexPage.AKNP + AnnexPage.SPAN + AnnexPage.LEOS_CONTENT_SOFT_REMOVED));
        for (WebElement ele : elementList) {
            deletedStrList.add(getElementAttributeInnerText(ele));
        }
        assertTrue(deletedStrList.containsAll(strList), "Given options are not showing as grey or strikethrough in subparagraph " + arg0);
    }


    @Then("content of alinea {int} of point {int} of paragraph {int} is displayed in annex page")
    public void alineaOfPointOfParagraphIsDisplayedInAnnexPage(int arg0, int arg1, int arg2) {
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath(AnnexPage.PARAGRAPH + "[" + arg2 + "]" + AnnexPage.POINT + "[" + arg1 + "]" + AnnexPage.ALINEA + "[" + arg0 + "]" + AnnexPage.CONTENT + AnnexPage.AKNP));
        assertTrue(bool, "content of alinea " + arg0 + " of point " + arg1 + " of paragraph " + arg2 + "is not displayed in annex page");
    }

    @When("double click on alinea {int} of point {int} of paragraph {int} in annex page")
    public void doubleClickOnAlineaOfPointOfParagraphInAnnexPage(int arg0, int arg1, int arg2) {
        doubleClick(driver, By.xpath(AnnexPage.PARAGRAPH + "[" + arg2 + "]" + AnnexPage.POINT + "[" + arg1 + "]" + AnnexPage.ALINEA + "[" + arg0 + "]" + AnnexPage.CONTENT + AnnexPage.AKNP));
        E2eUtil.wait(2000);
    }

    @When("Add {string} at the end of the ck editor text box of a subparagraph in annex page")
    public void addAtTheEndOfTheCkEditorTextBoxOfASubparagraphInAnnexPage(String arg0) {
        By by = By.xpath(AnnexPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI);
        String text = getElementText(driver, by);
        elementEcasSendkeys(driver, by, text + arg0);
        E2eUtil.wait(1000);
    }

    @When("remove {string} at the end of the ck editor text box of a subparagraph in annex page")
    public void removeAtTheEndOfTheCkEditorTextBoxOfASubparagraphInAnnexPage(String arg0) {
        By by = By.xpath(AnnexPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI);
        String text = getElementText(driver, by);
        assertNotNull(text);
        assertTrue(text.length() > 0, "text inside ck editor has length 0");
        assertEquals(String.valueOf(text.charAt(text.length() - 1)), arg0, "text inside ck editor doesn't contain " + arg0);
        elementEcasSendkeys(driver, by, StringUtils.substring(text, 0, text.length() - 1));
        E2eUtil.wait(1000);
    }

    @When("Add {string} at the end of the ck editor text box of an alinea in annex page")
    public void addAtTheEndOfTheCkEditorTextBoxOfAnAlineaInAnnexPage(String arg0) {
        By by = By.xpath(AnnexPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI);
        elementEcasSendkeys(driver, by, getElementText(driver, by) + arg0);
        E2eUtil.wait(1000);
    }

    @And("{string} is not added at the end of alinea {int} of point {int} of paragraph {int} in annex page")
    public void isNotPresentAtTheEndOfAlineaOfPointOfParagraphInAnnexPage(String arg0, int arg1, int arg2, int arg3) {
        String text = getElementText(driver, By.xpath(AnnexPage.PARAGRAPH + "[" + arg3 + "]" + AnnexPage.POINT + "[" + arg2 + "]" + AnnexPage.ALINEA + "[" + arg1 + "]" + AnnexPage.CONTENT + AnnexPage.AKNP + AnnexPage.SPAN + AnnexPage.LEOS_CONTENT_SOFT_NEW));
        assertNotEquals(text, arg0, arg0 + " is added at the end of alinea ");
    }

    @When("replace content {string} with the existing content in ck editor text box of a subparagraph in annex page")
    public void replaceContentWithTheExistingContentInCkEditorTextBoxOfASubparagraphInAnnexPage(String arg0) {
        elementEcasSendkeys(driver, By.xpath(AnnexPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI), arg0);
        E2eUtil.wait(2000);
    }

    @And("below words are showing as bold and underlined in subparagraph {int} of paragraph {int} in single diffing page of annex page")
    public void belowWordsAreShowingAsBoldAndUnderlinedInSubparagraphOfParagraphInSingleDiffingPageOfAnnexPage(int arg0, int arg1, DataTable dataTable) {
        List<String> actualStrList = new ArrayList<>();
        List<String> giveStrList = dataTable.asList(String.class);
        List<WebElement> elementList = driver.findElements(By.xpath(AnnexPage.PARAGRAPH + "[" + arg1 + "]" + AnnexPage.SUBPARAGRAPH + "[" + arg0 + "]" + AnnexPage.CONTENT + AnnexPage.AKNP + AnnexPage.SPAN + AnnexPage.LEOS_CONTENT_NEW_CN));
        for (WebElement ele : elementList) {
            actualStrList.add(getElementAttributeInnerText(ele).trim());
        }
        assertTrue(actualStrList.containsAll(giveStrList), "Given options are not present in the actual added elements list in single diffing page of annex page");
    }


    @And("below words are showing as bold, underlined and strikethrough in subparagraph {int} of paragraph {int} in single diffing page of annex page")
    public void belowWordsAreShowingAsBoldUnderlinedAndStrikethroughInSubparagraphOfParagraphInSingleDiffingPageOfAnnexPage(int arg0, int arg1, DataTable dataTable) {
        List<String> actualStrList = new ArrayList<>();
        List<String> giveStrList = dataTable.asList(String.class);
        List<WebElement> elementList = driver.findElements(By.xpath(AnnexPage.PARAGRAPH + "[" + arg1 + "]" + AnnexPage.SUBPARAGRAPH + "[" + arg0 + "]" + AnnexPage.CONTENT + AnnexPage.AKNP + AnnexPage.SPAN + AnnexPage.LEOS_CONTENT_REMOVED_CN));
        for (WebElement ele : elementList) {
            actualStrList.add(getElementAttributeInnerText(ele).trim());
        }
        assertTrue(actualStrList.containsAll(giveStrList), "Given options are not present in the actual deleted elements list in single diffing page of annex page");
    }

    @And("below words are showing as bold in subparagraph {int} of paragraph {int} in double diffing page of annex page")
    public void belowWordsAreShowingAsBoldInSubparagraphOfParagraphInDoubleDiffingPageOfAnnexPage(int arg0, int arg1, DataTable dataTable) {
        List<String> actualStrList = new ArrayList<>();
        List<String> giveStrList = dataTable.asList(String.class);
        List<WebElement> elementList = driver.findElements(By.xpath(AnnexPage.PARAGRAPH + "[" + arg1 + "]" + AnnexPage.SUBPARAGRAPH + "[" + arg0 + "]" + AnnexPage.CONTENT + AnnexPage.AKNP + AnnexPage.SPAN + AnnexPage.LEOS_DOUBLE_COMPARE_ADDED_ORIGINAL));
        for (WebElement ele : elementList) {
            actualStrList.add(getElementAttributeInnerText(ele).trim());
        }
        assertTrue(actualStrList.containsAll(giveStrList), "Given options are not bold in double diffing page of annex page");
    }

    @And("below words are showing as bold and strikethrough in subparagraph {int} of paragraph {int} in double diffing page of annex page")
    public void belowWordsAreShowingAsBoldAndStrikethroughInSubparagraphOfParagraphInDoubleDiffingPageOfAnnexPage(int arg0, int arg1, DataTable dataTable) {
        List<String> actualStrList = new ArrayList<>();
        List<String> giveStrList = dataTable.asList(String.class);
        List<WebElement> elementList = driver.findElements(By.xpath(AnnexPage.PARAGRAPH + "[" + arg1 + "]" + AnnexPage.SUBPARAGRAPH + "[" + arg0 + "]" + AnnexPage.CONTENT + AnnexPage.AKNP + AnnexPage.SPAN + AnnexPage.LEOS_DOUBLE_COMPARE_REMOVED_ORIGINAL));
        for (WebElement ele : elementList) {
            actualStrList.add(getElementAttributeInnerText(ele).trim());
        }
        assertTrue(actualStrList.containsAll(giveStrList), "Given options are not bold and strikethrough in double diffing page of annex page");
    }

    @And("below words are showing as bold and underlined in subparagraph {int} of paragraph {int} in double diffing page of annex page")
    public void belowWordsAreShowingAsBoldAndUnderlinedInSubparagraphOfParagraphInDoubleDiffingPageOfAnnexPage(int arg0, int arg1, DataTable dataTable) {
        List<String> actualStrList = new ArrayList<>();
        List<String> giveStrList = dataTable.asList(String.class);
        List<WebElement> elementList = driver.findElements(By.xpath(AnnexPage.PARAGRAPH + "[" + arg1 + "]" + AnnexPage.SUBPARAGRAPH + "[" + arg0 + "]" + AnnexPage.CONTENT + AnnexPage.AKNP + AnnexPage.SPAN + AnnexPage.LEOS_DOUBLE_COMPARE_ADDED_INTERMEDIATE));
        for (WebElement ele : elementList) {
            actualStrList.add(getElementAttributeInnerText(ele).trim());
        }
        assertTrue(actualStrList.containsAll(giveStrList), "Given options are not bold and underlined in double diffing page of annex page");
    }

    @And("there is only {int} addition\\(s) in double diff comparision page of annex page")
    public void thereIsOnlyAdditionSInDoubleDiffComparisionPageOfAnnexPage(int arg0) {
        List<WebElement> originalAddedElementList = getElementListForComparision(driver, By.xpath(AnnexPage.SPAN + AnnexPage.LEOS_DOUBLE_COMPARE_ADDED_ORIGINAL), 10);
        List<WebElement> interMediateAddedElementList = getElementListForComparision(driver, By.xpath(AnnexPage.SPAN + AnnexPage.LEOS_DOUBLE_COMPARE_ADDED_INTERMEDIATE), 10);
        assertEquals(originalAddedElementList.size() + interMediateAddedElementList.size(), arg0, "either there are no changes or multiple additions in double diff comparision page of annex page");
    }

    @And("there is only {int} deletion\\(s) in double diff comparision page of annex page")
    public void thereIsOnlyDeletionSInDoubleDiffComparisionPageOfAnnexPage(int arg0) {
        List<WebElement> originalRemovedElementList = getElementListForComparision(driver, By.xpath(AnnexPage.SPAN + AnnexPage.LEOS_DOUBLE_COMPARE_REMOVED_ORIGINAL), 10);
        List<WebElement> interMediateRemovedElementList = getElementListForComparision(driver, By.xpath(AnnexPage.SPAN + AnnexPage.LEOS_DOUBLE_COMPARE_REMOVED_INTERMEDIATE), 10);
        assertEquals(originalRemovedElementList.size() + interMediateRemovedElementList.size(), arg0, "either there are no changes or multiple deletions in double diff comparision page of annex page");
    }

    @And("there is only {int} addition\\(s) in single diff comparision page of annex page")
    public void thereIsOnlyAdditionSInSingleDiffComparisionPageOfAnnexPage(int arg0) {
        List<WebElement> elementList = getElementListForComparision(driver, By.xpath(AnnexPage.SPAN + AnnexPage.LEOS_CONTENT_NEW_CN), 10);
        assertEquals(elementList.size(), arg0, "either there are no changes or multiple additions in single diff comparision page of annex page");
    }

    @And("there is only {int} deletion\\(s) in single diff comparision page of annex page")
    public void thereIsOnlyDeletionSInSingleDiffComparisionPageOfAnnexPage(int arg0) {
        List<WebElement> elementList = getElementListForComparision(driver, By.xpath(AnnexPage.SPAN + AnnexPage.LEOS_CONTENT_REMOVED_CN), 10);
        assertEquals(elementList.size(), arg0, "either there are no changes or multiple deletions in single diff comparision page of annex page");
    }

    @Then("table {int} in paragraph {int} is displayed in annex page")
    public void tableIsDisplayedInParagraphInAnnexPage(int arg0, int arg1) {
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath(AnnexPage.PARAGRAPH + "[" + arg1 + "]" + AnnexPage.TABLE + "[" + arg0 + "]"));
        assertTrue(bool, "table in paragraph " + arg1 + " is not displayed in annex page");
    }

    @When("click on insert before icon present in show all actions icon of paragraph {int} in annex page")
    public void clickOnInsertBeforeIconPresentInShowAllActionsIconOfParagraphInAnnexPage(int arg0) {
        E2eUtil.wait(2000);
        Actions actions = new Actions(driver);
        actions.moveToElement(waitForElementTobePresent(driver, By.xpath(AnnexPage.PARAGRAPH + "[" + arg0 + "]"))).build().perform();
        E2eUtil.wait(2000);
        actions.moveToElement(waitForElementTobePresent(driver, By.xpath(AnnexPage.PARAGRAPH + "[" + arg0 + "]" + "//following-sibling::div[1][@class='leos-actions Vaadin-Icons']//span[@data-widget-type='show.all.actions']"))).build().perform();
        E2eUtil.wait(2000);
        actions.moveToElement(waitForElementTobePresent(driver, By.xpath(AnnexPage.PARAGRAPH + "[" + arg0 + "]" + "//following-sibling::div[1][@class='leos-actions Vaadin-Icons']//span[@data-widget-type='insert.before']"))).build().perform();
        actions.click().build().perform();
        E2eUtil.wait(2000);
    }

    @Then("paragraph {int} is displayed in annex page")
    public void paragraphIsDisplayedInAnnexPage(int arg0) {
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath(AnnexPage.PARAGRAPH + "[" + arg0 + "]"));
        assertTrue(bool, "paragraph " + arg0 + " is displayed in annex page");
    }

    @When("append content {string} with the existing content in ck editor text box of a paragraph in annex page")
    public void replaceContentWithExistingContentParagraphInArticleOriginEC(String arg0) {
        By by = By.xpath(AnnexPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI);
        elementEcasSendkeys(driver, by, getElementAttributeInnerText(driver, by) + arg0);
        E2eUtil.wait(2000);
    }


    @Then("{string} is appended to the paragraph {int} content in annex page")
    public void isAppendedToTheParagraphContentInAnnexPage(String arg0, int arg1) {
        String text = getElementAttributeInnerText(driver, By.xpath(AnnexPage.PARAGRAPH + "[" + arg1 + "]" + AnnexPage.AKNP));
        assertTrue(text.contains(arg0), arg0 + " is not added to paragraph " + arg1 + " in annex page");
    }

    @When("click on insert after icon present in show all actions icon of paragraph {int} in annex page")
    public void clickOnInsertAfterIconPresentInShowAllActionsIconOfParagraphInAnnexPage(int arg0) {
        E2eUtil.wait(2000);
        Actions actions = new Actions(driver);
        actions.moveToElement(waitForElementTobePresent(driver, By.xpath(AnnexPage.PARAGRAPH + "[" + arg0 + "]"))).build().perform();
        E2eUtil.wait(2000);
        actions.moveToElement(waitForElementTobePresent(driver, By.xpath(AnnexPage.PARAGRAPH + "[" + arg0 + "]" + "//following-sibling::div[1][@class='leos-actions Vaadin-Icons']//span[@data-widget-type='show.all.actions']"))).build().perform();
        E2eUtil.wait(2000);
        actions.moveToElement(waitForElementTobePresent(driver, By.xpath(AnnexPage.PARAGRAPH + "[" + arg0 + "]" + "//following-sibling::div[1][@class='leos-actions Vaadin-Icons']//span[@data-widget-type='insert.after']"))).build().perform();
        actions.click().build().perform();
        E2eUtil.wait(2000);
    }

    @And("click on header cell {int} of row {int} of table header in paragraph while ck editor is open")
    public void clickOnHeaderCellOfRowOfTableHeaderInParagraph(int arg0, int arg1) {
        elementClick(driver, By.xpath(AnnexPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI_TABLE_THEAD_TR + "[" + arg1 + "]" + AnnexPage.TH + "[" + arg0 + "]" + AnnexPage.P));
    }

    @And("add {string} in header cell {int} of row {int} of table header in paragraph while ck editor is open")
    public void addInHeaderCellOfRowOfTableHeaderInParagraph(String arg0, int arg1, int arg2) {
        elementEcasSendkeys(driver, By.xpath(AnnexPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI_TABLE_THEAD_TR + "[" + arg2 + "]" + AnnexPage.TH + "[" + arg1 + "]" + AnnexPage.P), arg0);
    }

    @And("click on header cell {int} of row {int} of table body in paragraph while ck editor is open")
    public void clickOnHeaderCellOfRowOfTableBodyInParagraph(int arg0, int arg1) {
        elementClick(driver, By.xpath(AnnexPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI_TABLE_TBODY_TR + "[" + arg1 + "]" + AnnexPage.TH + "[" + arg0 + "]" + AnnexPage.P));
    }

    @And("add {string} in header cell {int} of row {int} of table body in paragraph while ck editor is open")
    public void addInHeaderCellOfRowOfTableBodyInParagraph(String arg0, int arg1, int arg2) {
        elementEcasSendkeys(driver, By.xpath(AnnexPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI_TABLE_TBODY_TR + "[" + arg2 + "]" + AnnexPage.TH + "[" + arg1 + "]" + AnnexPage.P), arg0);
    }

    @And("click on data cell {int} of row {int} of table body in paragraph while ck editor is open")
    public void clickOnDataCellOfRowOfTableBodyInParagraph(int arg0, int arg1) {
        elementClick(driver, By.xpath(AnnexPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI_TABLE_TBODY_TR + "[" + arg1 + "]" + AnnexPage.TD + "[" + arg0 + "]"));
    }

    @And("click on paragraph of data cell {int} of row {int} of table body while ck editor is open")
    public void clickOnParagraphOfDataCellOfRowOfTableBody(int arg0, int arg1) {
        elementClick(driver, By.xpath(AnnexPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI_TABLE_TBODY_TR + "[" + arg1 + "]" + AnnexPage.TD + "[" + arg0 + "]" + AnnexPage.P));
    }

    @And("add {string} in data cell {int} of row {int} of table body in paragraph while ck editor is open")
    public void addInDataCellOfRowOfTableBodyInParagraph(String arg0, int arg1, int arg2) {
        elementEcasSendkeys(driver, By.xpath(AnnexPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI_TABLE_TBODY_TR + "[" + arg2 + "]" + AnnexPage.TD + "[" + arg1 + "]"), arg0);
    }

    @And("add {string} in paragraph of data cell {int} of row {int} of table body while ck editor is open")
    public void addInParagraphOfDataCellOfRowOfTableBody(String arg0, int arg1, int arg2) {
        elementEcasSendkeys(driver, By.xpath(AnnexPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI_TABLE_TBODY_TR + "[" + arg2 + "]" + AnnexPage.TD + "[" + arg1 + "]" + AnnexPage.P), arg0);
    }

    @And("append {string} in data cell {int} of row {int} of table body in paragraph while ck editor is open")
    public void appendInDataCellOfRowOfTableBodyInParagraphWhileCkEditorIsOpen(String arg0, int arg1, int arg2) {
        String text = getElementAttributeInnerText(driver, By.xpath(AnnexPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI_TABLE_TBODY_TR + "[" + arg2 + "]" + AnnexPage.TD + "[" + arg1 + "]" + AnnexPage.P));
        elementEcasSendkeys(driver, By.xpath(AnnexPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI_TABLE_TBODY_TR + "[" + arg2 + "]" + AnnexPage.TD + "[" + arg1 + "]" + AnnexPage.P), text + arg0);
    }

    @And("the data {string} is added in header cell {int} of row {int} of table body in paragraph {int}")
    public void theDataIsPresentInHeaderCellOfRowOfTableBodyInParagraph(String arg0, int arg1, int arg2, int arg3) {
        String str;
        boolean bool = false;
        List<WebElement> elementList = driver.findElements(By.xpath(AnnexPage.PARAGRAPH + "[" + arg3 + "]" + AnnexPage.TABLE + AnnexPage.TBODY + AnnexPage.TR + "[" + arg2 + "]" + AnnexPage.TH + "[" + arg1 + "]" + AnnexPage.AKNP + AnnexPage.SPAN + AnnexPage.LEOS_CONTENT_SOFT_NEW));
        for (WebElement element : elementList) {
            str = getElementAttributeInnerText(element).trim();
            if (str.contains(arg0)) {
                bool = true;
                break;
            }
        }
        assertTrue(bool, "the data is not added in header cell");
    }

    @And("the data {string} is added in data cell {int} of row {int} of table body in paragraph {int}")
    public void theDataIsAddedInDataCellOfRowOfTableBodyInParagraph(String arg0, int arg1, int arg2, int arg3) {
        String str;
        boolean bool = false;
        List<WebElement> elementList = driver.findElements(By.xpath(AnnexPage.PARAGRAPH + "[" + arg3 + "]" + AnnexPage.TABLE + AnnexPage.TBODY + AnnexPage.TR + "[" + arg2 + "]" + AnnexPage.TD + "[" + arg1 + "]" + AnnexPage.AKNP + AnnexPage.SPAN + AnnexPage.LEOS_CONTENT_SOFT_NEW));
        for (WebElement element : elementList) {
            str = getElementAttributeInnerText(element);
            if (str.contains(arg0)) {
                bool = true;
                break;
            }
        }
        assertTrue(bool, "the data is not added in data cell");
    }

    @Then("footnote with marker {int} is showing in data cell {int} of row {int} of table body in paragraph while ck editor is open")
    public void footnoteWithMarkerIsShowingInDataCellOfRowOfTableBodyInParagraphCKEditorOpen(int arg0, int arg1, int arg2) {
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath(AnnexPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI_TABLE_TBODY_TR + "[" + arg2 + "]" + AnnexPage.TD + "[" + arg1 + "]" + "//span[@marker='" + arg0 + "']"));
        assertTrue(bool, "footnote with marker is not showing in data cell");
    }

    @Then("footnote with marker {int} is showing in data cell {int} of row {int} of table body in paragraph {int}")
    public void footnoteWithMarkerIsShowingInDataCellOfRowOfTableBodyInParagraph(int arg0, int arg1, int arg2, int arg3) {
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath(AnnexPage.PARAGRAPH + "[" + arg3 + "]" + AnnexPage.TABLE + AnnexPage.TBODY + AnnexPage.TR + "[" + arg2 + "]" + AnnexPage.TD + "[" + arg1 + "]" + AnnexPage.AKNP + AnnexPage.SPAN + AnnexPage.LEOS_CONTENT_SOFT_NEW + AnnexPage.AUTHORIALNOTE + "[@marker='" + arg0 + "']"));
        assertTrue(bool, "footnote with marker is not showing in data cell");
    }

    @Then("footnote with marker {int} is showing in header cell {int} of row {int} of table header in paragraph while ck editor is open")
    public void footnoteWithMarkerIsShowingInHeaderCellOfRowOfTableHeaderInParagraph(int arg0, int arg1, int arg2) {
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath(AnnexPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI_TABLE_THEAD_TR + "[" + arg2 + "]" + AnnexPage.TH + "[" + arg1 + "]" + AnnexPage.CKE_WRAPPER_AUTHORIALNOTE + "//span[@marker='" + arg0 + "']"));
        assertTrue(bool, "footnote with marker is not showing in header cell");
    }

    @And("footnote with marker {int} is showing in header cell {int} of row {int} of table body in paragraph {int}")
    public void footnoteWithMarkerIsShowingInHeaderCellOfRowOfTableBodyInParagraph(int arg0, int arg1, int arg2, int arg3) {
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath(AnnexPage.PARAGRAPH + "[" + arg3 + "]" + AnnexPage.TABLE + AnnexPage.TBODY + AnnexPage.TR + "[" + arg2 + "]" + AnnexPage.TH + "[" + arg1 + "]" + AnnexPage.AKNP + AnnexPage.SPAN + AnnexPage.LEOS_CONTENT_SOFT_NEW + AnnexPage.AUTHORIALNOTE + "[@marker='" + arg0 + "']"));
        assertTrue(bool, "footnote with marker is not showing in header cell");
    }

    @When("click on the content of paragraph while ck editor is open")
    public void clickOnTheContentOfParagraph() {
        elementClick(driver, By.xpath(AnnexPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI));
    }

    @Then("footnote with marker {int} is showing in the content of paragraph while ck editor is open")
    public void footnoteWithMarkerIsShowingInTheContentOfParagraphCKEditor(int arg0) {
        WebElement element = waitForElementTobePresent(driver, By.xpath(AnnexPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI));
        assertNotNull(element);
        WebElement authNote = element.findElement(By.xpath(AnnexPage.CKE_WRAPPER_AUTHORIALNOTE + "//span[@marker='" + arg0 + "']"));
        assertNotNull(authNote, "footnote with marker " + arg0 + " is showing in the content of paragraph while ck editor is open");
    }

    @Then("footnote with marker {int} is showing in the content of paragraph {int}")
    public void footnoteWithMarkerIsShowingInTheContentOfParagraph(int arg0, int arg1) {
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath("//paragraph[1]//aknp//authorialnote[@marker='1']"));
        assertTrue(bool, "footnote with marker " + arg0 + " is showing in the content of paragraph " + arg1);
    }

    @When("delete the footnote present in the content of paragraph while ck editor is open")
    public void deleteFootnotePresentInTheContentOfParagraph() {
        WebElement ele = waitForElementTobePresent(driver, By.xpath(AnnexPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI + AnnexPage.AUTHORIALNOTEWIDGET));
        assertNotNull(ele);
        boolean bool = removeElementThroughJS(driver, ele);
        assertTrue(bool, "footnote is not deleted present in the content of paragraph while ck editor is open");
    }

    @And("footnote is not present in the content of paragraph {int}")
    public void footnoteIsNotPresentInTheContentOfParagraph(int arg0) {
        boolean bool = waitUnTillElementIsNotPresent(driver, By.xpath(AnnexPage.PARAGRAPH + "[" + arg0 + "]" + AnnexPage.AKNP + AnnexPage.AUTHORIALNOTE));
        assertTrue(bool, "footnote is not present in the content of paragraph " + arg0);
    }

    @When("click on footnote number {int}")
    public void clickOnFootnoteNumber(int arg0) {
        elementClick(driver, By.xpath(AnnexPage.AUTHORIALNOTE + "[@marker='" + arg0 + "']"));
    }

    @Then("the text {string} is showing in the footnote list")
    public void theTextIsShowingInTheFootnoteList(String arg0) {
        boolean bool = false;
        String text;
        List<WebElement> elementList = driver.findElements(AnnexPage.AUTHORIAL_NOTE_TEXT_LIST);
        for (WebElement element : elementList) {
            text = getElementAttributeInnerText(element);
            if (text.contains(arg0)) {
                bool = true;
                break;
            }
        }
        assertTrue(bool, "the text " + arg0 + " is not showing in the footnote list");
    }

    @When("mousehover on footnote number {int} and check footnote text is displayed")
    public void mousehoverOnFootnoteNumber(int arg0) {
        Actions actions = new Actions(driver);
        WebElement ele = waitForElementTobePresent(driver, By.xpath(AnnexPage.AUTHORIALNOTE + "[@marker='" + arg0 + "']"));
        actions.moveToElement(ele).build().perform();
        actions.release().build().perform();
    }

    @Then("footnote text is displayed for footnote number {int}")
    public void footnoteTextIsDisplayedForFootnoteNumber(int arg0) {
        boolean bool = verifyElement(driver, By.xpath(AnnexPage.AUTHORIALNOTE + "[@marker='" + arg0 + "']" + AnnexPage.AKNP));
        assertTrue(bool, "footnote text is not displayed for footnote number " + arg0);
    }

    @When("scroll to row {int} of table body in paragraph while ck editor is open")
    public void scrollToDataCellOfRowOfTableBodyInParagraph(int arg1) {
        scrollTo(driver, By.xpath(AnnexPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI_TABLE_TBODY_TR + "[" + arg1 + "]"));
    }

    @When("scroll to data cell {int} of row {int} of table body in paragraph {int}")
    public void scrollToDataCellOfRowOfTableBodyInParagraph(int arg0, int arg1, int arg2) {
        scrollTo(driver, By.xpath(AnnexPage.PARAGRAPH + "[" + arg2 + "]" + AnnexPage.TBODY + AnnexPage.TR + "[" + arg1 + "]" + AnnexPage.TD + "[" + arg0 + "]" + AnnexPage.AKNP));
    }

    @And("do right click on row {int} of table body in paragraph and mousehover on option {string} and click on {string} option in the submenu while ck editor is open")
    public void doRightClickOnDataCellOfRowOfTableBodyInParagraph(int arg1, String arg2, String arg3) {
        E2eUtil.wait(2000);
        Actions actions = new Actions(driver);
        actions.moveToElement(waitForElementTobePresent(driver, By.xpath(AnnexPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI_TABLE_TBODY_TR + "[" + arg1 + "]"))).contextClick().build().perform();
        E2eUtil.wait(2000);
        driver.switchTo().frame(waitForElementTobePresent(driver, AnnexPage.CK_PANEL_FRAME_1));
        actions.moveToElement(waitForElementTobePresent(driver, By.xpath(AnnexPage.CKE_MENUITEM + AnnexPage.CKE_MENUBUTTON_INNER + "//*[text()='" + arg2 + "']"))).build().perform();
        E2eUtil.wait(2000);
        driver.switchTo().defaultContent();
        driver.switchTo().frame(waitForElementTobePresent(driver, AnnexPage.CK_PANEL_FRAME_2));
        elementActionClick(driver, By.xpath(AnnexPage.CKE_MENUITEM + AnnexPage.CKE_MENUBUTTON_INNER + "//*[text()='" + arg3 + "']"));
        E2eUtil.wait(2000);
        driver.switchTo().defaultContent();
    }

    @When("do right click on data cell {int} of row {int} of table body in paragraph and mousehover on option {string} and click on {string} option in the submenu while ck editor is open")
    public void doRightClickOnDataCellOfRowOfTableBodyInParagraphAndMousehoverOnOptionAndClickOnOptionInTheSubmenuWhileCkEditorIsOpen(int arg0, int arg1, String arg2, String arg3) {
        E2eUtil.wait(2000);
        Actions actions = new Actions(driver);
        actions.moveToElement(waitForElementTobePresent(driver, By.xpath(AnnexPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI_TABLE_TBODY_TR + "[" + arg1 + "]" + AnnexPage.TD + "[" + arg0 + "]"))).contextClick().build().perform();
        E2eUtil.wait(2000);
        driver.switchTo().frame(waitForElementTobePresent(driver, AnnexPage.CK_PANEL_FRAME_1));
        actions.moveToElement(waitForElementTobePresent(driver, By.xpath(AnnexPage.CKE_MENUITEM + AnnexPage.CKE_MENUBUTTON_INNER + "//*[text()='" + arg2 + "']"))).build().perform();
        E2eUtil.wait(2000);
        driver.switchTo().defaultContent();
        driver.switchTo().frame(waitForElementTobePresent(driver, AnnexPage.CK_PANEL_FRAME_2));
        elementActionClick(driver, By.xpath(AnnexPage.CKE_MENUITEM + AnnexPage.CKE_MENUBUTTON_INNER + "//*[text()='" + arg3 + "']"));
        E2eUtil.wait(2000);
        driver.switchTo().defaultContent();
    }

    @Then("total row number of the table is {int} in paragraph {int}")
    public void totalRowNumberOfTheTableIsInParagraph(int arg0, int arg1) {
        int size = driver.findElements(By.xpath(AnnexPage.PARAGRAPH + "[" + arg1 + "]" + AnnexPage.TABLE + AnnexPage.TBODY + AnnexPage.TR)).size();
        assertEquals(size, arg0, "total number of rows in the table is not " + arg0);
    }

    @And("{string} is hyperlinked with title to EUR-Lex in data cell {int} of row {int} of table body in paragraph {int}")
    public void isHyperlinkedWithTitleInDataCellOfRowOfTableBodyInParagraph(String arg0, int arg1, int arg2, int arg3) {
        String text = getElementText(driver, By.xpath(AnnexPage.PARAGRAPH + "[" + arg3 + "]" + AnnexPage.TBODY + AnnexPage.TR + "[" + arg2 + "]" + AnnexPage.TD + "[" + arg1 + "]" + AnnexPage.AKNP + "//a[@title='to EUR-Lex']"));
        assertTrue(text.contains(arg0), arg0 + " is not hyperlinked with title to EUR-Lex in data cell");
    }

    @And("delete the footnote present in data cell {int} of row {int} of table body in paragraph while ck editor is open")
    public void deleteFootnotePresentInDataCellOfRowOfTableBodyInParagraph(int arg0, int arg1) {
        WebElement tdElement = waitForElementTobePresent(driver, By.xpath(AnnexPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI_TABLE_TBODY_TR + "[" + arg1 + "]" + AnnexPage.TD + "[" + arg0 + "]" + AnnexPage.AUTHORIALNOTEWIDGET));
        assertNotNull(tdElement);
        boolean bool = removeElementThroughJS(driver, tdElement);
        assertTrue(bool, "footnote is not deleted present in data cell");
    }

    @And("footnote is not present in data cell {int} of row {int} of table body in paragraph {int}")
    public void footnoteIsNotPresentInDataCellOfRowOfTableBodyInParagraph(int arg0, int arg1, int arg2) {
        boolean bool = waitUnTillElementIsNotPresent(driver, By.xpath(AnnexPage.PARAGRAPH + "[" + arg2 + "]" + AnnexPage.TBODY + AnnexPage.TR + "[" + arg1 + "]" + AnnexPage.TD + "[" + arg0 + "]" + AnnexPage.AKNP + AnnexPage.AUTHORIALNOTE));
        assertTrue(bool, "footnote is present in data cell");
    }

    @And("{string} is added to data cell {int} of row {int} of table body in paragraph {int}")
    public void isAddedToDataCellOfRowOfTableBodyInParagraph(String arg0, int arg1, int arg2, int arg3) {
        String text;
        boolean bool = false;
        List<WebElement> elementList = driver.findElements(By.xpath(AnnexPage.PARAGRAPH + "[" + arg3 + "]" + AnnexPage.TBODY + AnnexPage.TR + "[" + arg2 + "]" + AnnexPage.TD + "[" + arg1 + "]" + AnnexPage.AKNP + AnnexPage.SPAN + AnnexPage.LEOS_CONTENT_SOFT_NEW));
        for (WebElement element : elementList) {
            text = getElementAttributeInnerText(element);
            if (text.contains(arg0)) {
                bool = true;
                break;
            }
        }
        assertTrue(bool, arg0 + " is not added in data cell " + arg1 + " of row " + arg2 + " of table body in paragraph " + arg3);
    }

    @When("add open and close square bracket to the content of data cell {int} of row {int} of table body in paragraph")
    public void addOpenAndCloseSquareBracketToTheContentOfDataCellOfRowOfTableBodyInParagraph(int arg0, int arg1) {
        By by = By.xpath(AnnexPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI_TABLE_TBODY_TR + "[" + arg1 + "]" + AnnexPage.TD + "[" + arg0 + "]" + AnnexPage.P);
        WebElement element = waitForElementTobePresent(driver, by);
        assertNotNull(element);
        elementEcasSendkeys(element, "[" + getElementAttributeInnerText(driver, by) + "]");
    }

    @And("open and close square bracket is added to the content of data cell {int} of row {int} of table body in paragraph {int}")
    public void openAndCloseSquareBracketIsAddedToTheContentOfDataCellOfRowOfTableBodyInParagraph(int arg0, int arg1, int arg2) {
        String text;
        boolean bool = false;
        boolean bool1 = false;
        boolean bool2 = false;
        List<WebElement> elementList = driver.findElements(By.xpath(AnnexPage.PARAGRAPH + "[" + arg2 + "]" + AnnexPage.TBODY + AnnexPage.TR + "[" + arg1 + "]" + AnnexPage.TD + "[" + arg0 + "]" + AnnexPage.AKNP + AnnexPage.SPAN + AnnexPage.LEOS_CONTENT_SOFT_NEW));
        for (WebElement element : elementList) {
            text = getElementAttributeInnerText(element);
            if (text.contains("[")) {
                bool1 = true;
                break;
            }
        }
        for (WebElement element : elementList) {
            text = getElementAttributeInnerText(element);
            if (text.contains("[")) {
                bool2 = true;
                break;
            }
        }
        if (bool1 && bool2) {
            bool = true;
        }
        assertTrue(bool, "open and close square bracket is not added");
    }

    @And("number of rows in the table in paragraph is {int} while ck editor is open")
    public void numberOfRowsInTheTableOfParagraphIs(int arg0) {
        int size = driver.findElements(By.xpath(AnnexPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI_TABLE_TBODY_TR)).size();
        assertEquals(size, arg0, "number of rows in the table in paragraph is not " + arg0 + " while ck editor is open");
    }

    @And("row {int} of table in paragraph {int} is showing grey and strikethrough")
    public void rowOfTableInParagraphIsShowingGreyAndStrikethrough(int arg0, int arg1) {
        WebElement ele = waitForElementTobePresent(driver, By.xpath(AnnexPage.PARAGRAPH + "[" + arg1 + "]" + AnnexPage.TABLE + AnnexPage.TBODY + AnnexPage.TR + "[" + arg0 + "]" + AnnexPage.LEOS_CONTENT_SOFT_REMOVED));
        assertNotNull(ele, "row " + arg0 + " of table in paragraph " + arg1 + " is not showing grey and strikethrough");
    }

    @And("select content in data cell {int} of row {int} of table body in paragraph {int}")
    public void selectContentInDataCellOfRowOfTableBodyInParagraph(int arg0, int arg1, int arg2) {
        E2eUtil.wait(2000);
        selectText(driver, By.xpath(AnnexPage.PARAGRAPH + "[" + arg2 + "]" + AnnexPage.TBODY + AnnexPage.TR + "[" + arg1 + "]" + AnnexPage.TD + "[" + arg0 + "]" + AnnexPage.AKNP));
    }

    @Then("{string} is showing as bold in data cell {int} of row {int} of table body in paragraph {int}")
    public void isShowingAsBoldInDataCellOfRowOfTableBodyInParagraph(String arg0, int arg1, int arg2, int arg3) {
        String text;
        boolean bool = false;
        List<WebElement> elementList = driver.findElements(By.xpath(AnnexPage.PARAGRAPH + "[" + arg3 + "]" + AnnexPage.TBODY + AnnexPage.TR + "[" + arg2 + "]" + AnnexPage.TD + "[" + arg1 + "]" + AnnexPage.AKNP + AnnexPage.SPAN + AnnexPage.LEOS_CONTENT_SOFT_NEW));
        for (WebElement element : elementList) {
            text = getElementAttributeInnerText(element);
            if (text.contains(arg0)) {
                bool = true;
            }
        }
        assertTrue(bool, arg0 + " is not showing as bold in data cell " + arg1 + " of row " + arg2 + " of table body in paragraph " + arg3);
    }

    @And("double click on the content of paragraph {int} in annex page")
    public void doubleClickOnTheContentOfParagraphInAnnexPage(int arg0) {
        doubleClick(driver, By.xpath(AnnexPage.PARAGRAPH + "[" + arg0 + "]" + AnnexPage.CONTENT));
        E2eUtil.wait(2000);
    }

    @Then("select text in paragraph {int} of data cell {int} of row {int} in the table while ck editor is open")
    public void selectTextInParagraphOfDataCellOfRowInTheTableWhileCkEditorIsOpen(int arg0, int arg1, int arg2) {
        WebElement element = driver.findElement(By.xpath("//table[@data-akn-name='leosTable']//tbody//tr[" + arg2 + "]//td[" + arg1 + "]//p[" + arg0 + "]"));
        assertNotNull(element);
        element.sendKeys(Keys.chord(Keys.CONTROL, "a"));
    }

    @And("delete paragraph {int} present in data cell {int} of row {int} of table body in paragraph while ck editor is open")
    public void deleteParagraphPresentInDataCellOfRowOfTableBodyInParagraphWhileCkEditorIsOpen(int arg0, int arg1, int arg2) {
        WebElement element = waitForElementTobePresent(driver, By.xpath(AnnexPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI_TABLE_TBODY_TR + "[" + arg2 + "]" + AnnexPage.TD + "[" + arg1 + "]" + AnnexPage.P + "[" + arg0 + "]"));
        assertNotNull(element);
        scrollToElement(driver, element);
        boolean bool = removeElementThroughJS(driver, element);
        assertTrue(bool);
    }

    @Then("footnote with marker {int} is not displayed in data cell {int} of row {int} of table body in paragraph while ck editor is open")
    public void footnoteWithMarkerIsNotDisplayedInDataCellOfRowOfTableBodyInParagraphWhileCkEditorIsOpen(int arg0, int arg1, int arg2) {
        boolean bool = waitUnTillElementIsNotPresent(driver, By.xpath(AnnexPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI_TABLE_TBODY_TR + "[" + arg2 + "]" + AnnexPage.TD + "[" + arg1 + "]" + "//span[@marker='" + arg0 + "']"));
        assertTrue(bool, "footnote with marker " + arg0 + " is not displayed");
    }

    @And("double click on footnote with marker {int} present in data cell {int} of row {int} of table body in paragraph while ck editor is open")
    public void doubleClickOnFootnoteWithMarkerPresentInDataCellOfRowOfTableBodyInParagraphWhileCkEditorIsOpen(int arg0, int arg1, int arg2) {
        doubleClick(driver, By.xpath(AnnexPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI_TABLE_TBODY_TR + "[" + arg2 + "]" + AnnexPage.TD + "[" + arg1 + "]" + "//span[@marker='" + arg0 + "']"));
    }

    @Then("index {int} of authorial note table contains {string}")
    public void indexOfAuthorialNoteTableContains(int arg0, String arg1) {
        String text = getElementAttributeInnerText(driver, By.xpath(AnnexPage.LEOS_AUTH_NOTE_TABLE + AnnexPage.LEOS_AUTH_NOTE + "[" + arg0 + "]//text"));
        assertTrue(text.contains(arg1), "index " + arg0 + " of authorial note table doesn't contain " + arg1);
    }

    @And("replace {string} in paragraph of data cell {int} of row {int} of table body while ck editor is open")
    public void replaceInParagraphOfDataCellOfRowOfTableBodyWhileCkEditorIsOpen(String arg0, int arg1, int arg2) {
        elementEcasSendkeys(driver, By.xpath(AnnexPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI_TABLE_TBODY_TR + "[" + arg2 + "]" + AnnexPage.TD + "[" + arg1 + "]" + AnnexPage.P), arg0);
    }

    @When("scroll to row {int} of table body in paragraph {int}")
    public void scrollToRowOfTableBodyInParagraph(int arg0, int arg1) {
        scrollTo(driver, By.xpath(AnnexPage.PARAGRAPH + "[" + arg1 + "]" + AnnexPage.TBODY + AnnexPage.TR + "[" + arg0 + "]"));
    }

    @And("the data {string} is present in data cell {int} of row {int} of table body in paragraph {int}")
    public void theDataIsPresentInDataCellOfRowOfTableBodyInParagraph(String arg0, int arg1, int arg2, int arg3) {
        String text = getElementAttributeInnerText(driver, By.xpath(AnnexPage.PARAGRAPH + "[" + arg3 + "]" + AnnexPage.TABLE + AnnexPage.TBODY + AnnexPage.TR + "[" + arg2 + "]" + AnnexPage.TD + "[" + arg1 + "]" + "/aknp"));
        assertTrue(text.contains(arg0), "the data " + arg0 + " is not present in data cell ");
    }

    @And("the data {string} is showing in grey and strikethrough in data cell {int} of row {int} of table body in paragraph {int}")
    public void theDataIsShowingInGreyAndStrikethroughInDataCellOfRowOfTableBodyInParagraph(String arg0, int arg1, int arg2, int arg3) {
        String str;
        boolean bool = false;
        List<WebElement> elementList = driver.findElements(By.xpath(AnnexPage.PARAGRAPH + "[" + arg3 + "]" + AnnexPage.TABLE + AnnexPage.TBODY + AnnexPage.TR + "[" + arg2 + "]" + AnnexPage.TD + "[" + arg1 + "]" + AnnexPage.AKNP + AnnexPage.SPAN + AnnexPage.LEOS_CONTENT_SOFT_REMOVED));
        for (WebElement element : elementList) {
            str = getElementAttributeInnerText(element);
            if (str.contains(arg0)) {
                bool = true;
                break;
            }
        }
        assertTrue(bool, "the data is not showing in grey and strikethrough");
    }

    @And("the data {string} is showing in bold in data cell {int} of row {int} of table body in paragraph {int}")
    public void theDataIsShowingInBoldInDataCellOfRowOfTableBodyInParagraph(String arg0, int arg1, int arg2, int arg3) {
        String str;
        boolean bool = false;
        List<WebElement> elementList = driver.findElements(By.xpath(AnnexPage.PARAGRAPH + "[" + arg3 + "]" + AnnexPage.TABLE + AnnexPage.TBODY + AnnexPage.TR + "[" + arg2 + "]" + AnnexPage.TD + "[" + arg1 + "]" + AnnexPage.AKNP + AnnexPage.SPAN + AnnexPage.LEOS_CONTENT_SOFT_NEW));
        for (WebElement element : elementList) {
            str = getElementAttributeInnerText(element);
            if (str.contains(arg0)) {
                bool = true;
                break;
            }
        }
        assertTrue(bool, "the data is not showing in bold");
    }

    @And("aknp {int} is not present in data cell {int} of row {int} of table body in paragraph {int}")
    public void aknpIsNotPresentInDataCellOfRowOfTableBodyInParagraph(int arg0, int arg1, int arg2, int arg3) {
        boolean bool = waitUnTillElementIsNotPresent(driver, By.xpath(AnnexPage.PARAGRAPH + "[" + arg3 + "]" + AnnexPage.TABLE + AnnexPage.TBODY + AnnexPage.TR + "[" + arg2 + "]" + AnnexPage.TD + "[" + arg1 + "]" + "/aknp[" + arg0 + "]"));
        assertTrue(bool, "aknp " + arg0 + " is present in data cell");
    }

    @And("{string} is showing as bold and underlined in paragraph {int} in single diffing page of annex page")
    public void isShowingAsBoldAndUnderlinedInParagraphInSingleDiffingPageOfAnnexPage(String arg0, int arg1) {
        String text = getElementAttributeInnerText(driver, By.xpath(AnnexPage.PARAGRAPH + "[" + arg1 + "]" + AnnexPage.SPAN + AnnexPage.LEOS_CONTENT_NEW_CN));
        assertTrue(text.contains(arg0), arg0 + " is not showing as bold and underlined in paragraph " + arg1 + " in single diffing page of annex page");
    }

    @And("the data {string} is showing as bold and underlined in header cell {int} of row {int} of table body in paragraph {int} in single diffing page of annex page")
    public void theDataIsShowingAsBoldAndUnderlinedInHeaderCellOfRowOfTableBodyInParagraphInSingleDiffingPageOfAnnexPage(String arg0, int arg1, int arg2, int arg3) {
        String text = getElementAttributeInnerText(driver, By.xpath(AnnexPage.LEOS_DOUBLE_COMPARISON_CONTENT + AnnexPage.PARAGRAPH + "[" + arg3 + "]" + AnnexPage.TABLE + AnnexPage.TBODY + AnnexPage.TR + "[" + arg2 + "]" + AnnexPage.TH + "[" + arg1 + "]" + AnnexPage.SPAN + AnnexPage.LEOS_CONTENT_NEW_CN));
        assertTrue(text.contains(arg0), "the data " + arg0 + " is not showing as bold and underlined");
    }

    @And("the data {string} is showing as bold and underlined in data cell {int} of row {int} of table body in paragraph {int} in single diffing page of annex page")
    public void theDataIsShowingAsBoldAndUnderlinedInDataCellOfRowOfTableBodyInParagraphInSingleDiffingPageOfAnnexPage(String arg0, int arg1, int arg2, int arg3) {
        String text = getElementAttributeInnerText(driver, By.xpath(AnnexPage.LEOS_DOUBLE_COMPARISON_CONTENT + AnnexPage.PARAGRAPH + "[" + arg3 + "]" + AnnexPage.TABLE + AnnexPage.TBODY + AnnexPage.TR + "[" + arg2 + "]" + AnnexPage.TD + "[" + arg1 + "]" + AnnexPage.SPAN + AnnexPage.LEOS_CONTENT_NEW_CN));
        assertTrue(text.contains(arg0), "the data " + arg0 + " is not showing as bold and underlined");
    }

    @And("row {int} of table body in paragraph {int} is showing as bold and strikethrough in single diffing page of annex page")
    public void rowOfTableBodyInParagraphIsShowingAsBoldAndStrikethroughInSingleDiffingPageOfAnnexPage(int arg0, int arg1) {
        boolean bool = checkElementPresence(driver, By.xpath(AnnexPage.LEOS_DOUBLE_COMPARISON_CONTENT + AnnexPage.PARAGRAPH + "[" + arg1 + "]" + AnnexPage.TABLE + AnnexPage.TBODY + AnnexPage.TR + "[" + arg0 + "]" + AnnexPage.LEOS_CONTENT_REMOVED_CN));
        assertTrue(bool, "row " + arg0 + " of table body in paragraph " + arg1 + " is not showing as bold and strikethrough in single diffing page of annex page");
    }

    @And("the data {string} is showing as bold and strikethrough in data cell {int} of row {int} of table body in paragraph {int} in single diffing page of annex page")
    public void theDataIsShowingAsBoldAndStrikethroughInDataCellOfRowOfTableBodyInParagraphInSingleDiffingPageOfAnnexPage(String arg0, int arg1, int arg2, int arg3) {
        String text = getElementAttributeInnerText(driver, By.xpath(AnnexPage.LEOS_DOUBLE_COMPARISON_CONTENT + AnnexPage.PARAGRAPH + "[" + arg3 + "]" + AnnexPage.TABLE + AnnexPage.TBODY + AnnexPage.TR + "[" + arg2 + "]" + AnnexPage.TD + "[" + arg1 + "]" + AnnexPage.SPAN + AnnexPage.LEOS_CONTENT_REMOVED_CN));
        assertTrue(text.contains(arg0), "the data " + arg0 + " is not showing as bold and underlined");
    }

    @And("{string} is showing as bold and underlined in paragraph {int} in double diffing page of annex page")
    public void isShowingAsBoldAndUnderlinedInParagraphInDoubleDiffingPageOfAnnexPage(String arg0, int arg1) {
        String text = getElementAttributeInnerText(driver, By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + AnnexPage.PARAGRAPH + "[" + arg1 + "]" + AnnexPage.SPAN + LegalActPage.LEOS_DOUBLE_COMPARE_ADDED_INTERMEDIATE));
        assertEquals(text, arg0, arg0 + " is not showing as bold and underlined in paragraph " + arg1 + " in double diffing page of annex page");
    }

    @And("{string} is showing as bold and underlined in data cell {int} of row {int} of table body in paragraph {int} in double diffing page of annex page")
    public void isShowingAsBoldAndUnderlinedInDataCellOfRowOfTableBodyInParagraphInDoubleDiffingPageOfAnnexPage(String arg0, int arg1, int arg2, int arg3) {
        boolean bool = false;
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + AnnexPage.PARAGRAPH + "[" + arg3 + "]" + AnnexPage.TABLE + AnnexPage.TBODY + AnnexPage.TR + "[" + arg2 + "]" + AnnexPage.TD + "[" + arg1 + "]" + AnnexPage.SPAN + LegalActPage.LEOS_DOUBLE_COMPARE_ADDED_INTERMEDIATE));
        for (WebElement element : elementList) {
            if (getElementAttributeInnerText(element).contains(arg0)) {
                bool = true;
                break;
            }
        }
        assertTrue(bool, arg0 + " is not showing as bold and underlined in data cell");
    }

    @And("{string} is showing as strikethrough in data cell {int} of row {int} of table body in paragraph {int} in double diffing page of annex page")
    public void isShowingAsStrikethroughInDataCellOfRowOfTableBodyInParagraphInDoubleDiffingPageOfAnnexPage(String arg0, int arg1, int arg2, int arg3) {
        boolean bool = false;
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + AnnexPage.PARAGRAPH + "[" + arg3 + "]" + AnnexPage.TABLE + AnnexPage.TBODY + AnnexPage.TR + "[" + arg2 + "]" + AnnexPage.TD + "[" + arg1 + "]" + AnnexPage.SPAN + LegalActPage.LEOS_DOUBLE_COMPARE_REMOVED_ORIGINAL));
        for (WebElement element : elementList) {
            if (getElementAttributeInnerText(element).contains(arg0)) {
                bool = true;
                break;
            }
        }
        assertTrue(bool, arg0 + " is not showing as bold and strikethrough in data cell");
    }

    @And("{string} is showing as bold and strikethrough in data cell {int} of row {int} of table body in paragraph {int} in double diffing page of annex page")
    public void isShowingAsBoldAndStrikethroughInDataCellOfRowOfTableBodyInParagraphInDoubleDiffingPageOfAnnexPage(String arg0, int arg1, int arg2, int arg3) {
        boolean bool = false;
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + AnnexPage.PARAGRAPH + "[" + arg3 + "]" + AnnexPage.TABLE + AnnexPage.TBODY + AnnexPage.TR + "[" + arg2 + "]" + AnnexPage.TD + "[" + arg1 + "]" + AnnexPage.SPAN + LegalActPage.LEOS_DOUBLE_COMPARE_REMOVED_INTERMEDIATE));
        for (WebElement element : elementList) {
            if (getElementAttributeInnerText(element).contains(arg0)) {
                bool = true;
                break;
            }
        }
        assertTrue(bool, arg0 + " is not showing as bold and strikethrough in data cell");
    }

    @And("the data {string} is showing in bold, underlined and strikethrough in data cell {int} of row {int} of table body of paragraph {int} in single diffing comparision page")
    public void theDataIsShowingInBoldUnderlinedAndStrikethroughInDataCellOfRowOfTableBodyOfParagraphInSingleDiffingPage(String arg0, int arg1, int arg2, int arg3) {
        boolean bool = false;
        String text;
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + AnnexPage.PARAGRAPH + "[" + arg3 + "]" + AnnexPage.TABLE + AnnexPage.TBODY + AnnexPage.TR + "[" + arg2 + "]" + AnnexPage.TD + "[" + arg1 + "]" + AnnexPage.AKNP + AnnexPage.SPAN + AnnexPage.LEOS_CONTENT_REMOVED_CN));
        List<WebElement> elementList1 = driver.findElements(By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + AnnexPage.PARAGRAPH + "[" + arg3 + "]" + AnnexPage.TABLE + AnnexPage.TBODY + AnnexPage.TR + "[" + arg2 + "]" + AnnexPage.TD + "[" + arg1 + "]" + AnnexPage.AKNP + AnnexPage.LEOS_CONTENT_REMOVED_CN));
        for (WebElement element : elementList) {
            text = getElementAttributeInnerText(element);
            if (text.contains(arg0)) {
                bool = true;
                break;
            }
        }
        for (WebElement element : elementList1) {
            text = getElementAttributeInnerText(element);
            if (text.contains(arg0)) {
                bool = true;
                break;
            }
        }
        assertTrue(bool, "the data " + arg0 + " is not showing in bold, underlined and strikethrough in single diffing comparision page");
    }

    @And("the data {string} is showing in bold, underlined in data cell {int} of row {int} of table body of paragraph {int} in single diffing comparision page")
    public void theDataIsShowingInBoldUnderlinedInDataCellOfRowOfTableBodyOfParagraphInSingleDiffingPage(String arg0, int arg1, int arg2, int arg3) {
        boolean bool = false;
        String text;
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + AnnexPage.PARAGRAPH + "[" + arg3 + "]" + AnnexPage.TABLE + AnnexPage.TBODY + AnnexPage.TR + "[" + arg2 + "]" + AnnexPage.TD + "[" + arg1 + "]" + AnnexPage.AKNP + AnnexPage.SPAN + AnnexPage.LEOS_CONTENT_NEW_CN));
        for (WebElement element : elementList) {
            text = getElementAttributeInnerText(element);
            if (text.contains(arg0)) {
                bool = true;
                break;
            }
        }
        assertTrue(bool, "the data " + arg0 + " is not showing in bold, underlined in single diffing comparision page");
    }

    @And("the content is showing in bold, underlined and strikethrough in row {int} of table body of paragraph {int} in double diffing comparision page")
    public void theContentIsShowingInBoldUnderlinedAndStrikethroughInRowOfTableBodyOfParagraphInDoubleDiffingPage(int arg0, int arg1) {
        WebElement element = waitForElementTobePresent(driver, By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + AnnexPage.PARAGRAPH + "[" + arg1 + "]" + AnnexPage.TABLE + AnnexPage.TBODY + AnnexPage.TR + "[" + arg0 + "]" + LegalActPage.LEOS_DOUBLE_COMPARE_REMOVED_INTERMEDIATE));
        assertNotNull(element, "the content is not showing in bold, underlined and strikethrough in row " + arg0 + "in double diffing comparision page");
    }

    @And("the data {string} is showing in bold, underlined and strikethrough in data cell {int} of row {int} of table body of paragraph {int} in double diffing comparision page")
    public void theDataIsShowingInBoldUnderlinedAndStrikethroughInDataCellOfRowOfTableBodyOfParagraphInDoubleDiffingPage(String arg0, int arg1, int arg2, int arg3) {
        boolean bool = false;
        String text;
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + AnnexPage.PARAGRAPH + "[" + arg3 + "]" + AnnexPage.TABLE + AnnexPage.TBODY + AnnexPage.TR + "[" + arg2 + "]" + AnnexPage.TD + "[" + arg1 + "]" + AnnexPage.AKNP + AnnexPage.SPAN + LegalActPage.LEOS_DOUBLE_COMPARE_REMOVED_INTERMEDIATE));
        for (WebElement element : elementList) {
            text = getElementAttributeInnerText(element);
            if (text.contains(arg0)) {
                bool = true;
                break;
            }
        }
        assertTrue(bool, "the data " + arg0 + " is not showing in bold, underlined and strikethrough in double diffing comparision page");
    }

    @And("the data {string} is showing in bold, underlined in data cell {int} of row {int} of table body of paragraph {int} in double diffing comparision page")
    public void theDataIsShowingInBoldUnderlinedInDataCellOfRowOfTableBodyOfParagraphInDoubleDiffingPage(String arg0, int arg1, int arg2, int arg3) {
        boolean bool = false;
        String text;
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + AnnexPage.PARAGRAPH + "[" + arg3 + "]" + AnnexPage.TABLE + AnnexPage.TBODY + AnnexPage.TR + "[" + arg2 + "]" + AnnexPage.TD + "[" + arg1 + "]" + AnnexPage.AKNP + AnnexPage.SPAN + LegalActPage.LEOS_DOUBLE_COMPARE_ADDED_INTERMEDIATE));
        for (WebElement element : elementList) {
            text = getElementAttributeInnerText(element);
            if (text.contains(arg0)) {
                bool = true;
                break;
            }
        }
        assertTrue(bool, "the data " + arg0 + " is not showing in bold, underlined in double diffing comparision page");
    }

    @And("the data {string} is showing in bold and strikethrough in data cell {int} of row {int} of table body of paragraph {int} in double diffing comparision page")
    public void theDataIsShowingInBoldAndStrikethroughInDataCellOfRowOfTableBodyOfParagraphInDoubleDiffingPage(String arg0, int arg1, int arg2, int arg3) {
        boolean bool = false;
        String text;
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + AnnexPage.PARAGRAPH + "[" + arg3 + "]" + AnnexPage.TABLE + AnnexPage.TBODY + AnnexPage.TR + "[" + arg2 + "]" + AnnexPage.TD + "[" + arg1 + "]" + AnnexPage.AKNP + AnnexPage.SPAN + LegalActPage.LEOS_DOUBLE_COMPARE_REMOVED_ORIGINAL));
        for (WebElement element : elementList) {
            text = getElementAttributeInnerText(element);
            if (text.contains(arg0)) {
                bool = true;
                break;
            }
        }
        assertTrue(bool, "the data " + arg0 + " is not showing in bold and strikethrough in double diffing comparision page");
    }

    @And("the data {string} is showing in bold in data cell {int} of row {int} of table body of paragraph {int} in double diffing comparision page")
    public void theDataIsShowingInBoldInDataCellOfRowOfTableBodyOfParagraphInDoubleDiffingPage(String arg0, int arg1, int arg2, int arg3) {
        boolean bool = false;
        String text;
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + AnnexPage.PARAGRAPH + "[" + arg3 + "]" + AnnexPage.TABLE + AnnexPage.TBODY + AnnexPage.TR + "[" + arg2 + "]" + AnnexPage.TD + "[" + arg1 + "]" + AnnexPage.AKNP + AnnexPage.SPAN + LegalActPage.LEOS_DOUBLE_COMPARE_ADDED_ORIGINAL));
        for (WebElement element : elementList) {
            text = getElementAttributeInnerText(element);
            if (text.contains(arg0)) {
                bool = true;
                break;
            }
        }
        assertTrue(bool, "the data " + arg0 + " is not showing in bold in double diffing comparision page");
    }

    @And("the data {string} is showing in underlined in data cell {int} of row {int} of table body of paragraph {int} in double diffing comparision page")
    public void theDataIsShowingInUnderlinedInDataCellOfRowOfTableBodyOfParagraphInDoubleDiffingPage(String arg0, int arg1, int arg2, int arg3) {
        boolean bool = false;
        String text;
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + AnnexPage.PARAGRAPH + "[" + arg3 + "]" + AnnexPage.TABLE + AnnexPage.TBODY + AnnexPage.TR + "[" + arg2 + "]" + AnnexPage.TD + "[" + arg1 + "]" + AnnexPage.AKNP + AnnexPage.SPAN + LegalActPage.LEOS_DOUBLE_COMPARE_RETAINED_ORIGINAL));
        for (WebElement element : elementList) {
            text = getElementAttributeInnerText(element);
            if (text.contains(arg0)) {
                bool = true;
                break;
            }
        }
        assertTrue(bool, "the data " + arg0 + " is not showing in underlined in double diffing comparision page");
    }

    @And("the data {string} is showing in bold, underlined and strikethrough in aknp of data cell {int} of row {int} of table body of paragraph {int} in single diffing comparision page")
    public void theDataIsShowingInBoldUnderlinedAndStrikethroughInAknpOfDataCellOfRowOfTableBodyOfParagraphInSingleDiffingComparisionPage(String arg0, int arg1, int arg2, int arg3) {
        boolean bool = false;
        String text = null;
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + AnnexPage.PARAGRAPH + "[" + arg3 + "]" + AnnexPage.TABLE + AnnexPage.TBODY + AnnexPage.TR + "[" + arg2 + "]" + AnnexPage.TD + "[" + arg1 + "]" + AnnexPage.AKNP + AnnexPage.LEOS_CONTENT_REMOVED_CN));
        for (WebElement element : elementList) {
            text = getElementAttributeTextContent(element);
            if (text.contains(arg0)) {
                bool = true;
                break;
            }
        }
        assertTrue(bool, "the data " + arg0 + " is not showing in bold, underlined and strikethrough in single diffing comparision page " + text + ".");
    }

    @And("the data {string} is showing in bold and strikethrough in aknp of data cell {int} of row {int} of table body of paragraph {int} in double diffing comparision page")
    public void theDataIsShowingInBoldAndStrikethroughInAknpOfDataCellOfRowOfTableBodyOfParagraphInDoubleDiffingComparisionPage(String arg0, int arg1, int arg2, int arg3) {
        boolean bool = false;
        String text;
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.DOUBLE_COMPARISON_TOOL_BAR + LegalActPage.COMPARISION_SECOND_PAGE + AnnexPage.PARAGRAPH + "[" + arg3 + "]" + AnnexPage.TABLE + AnnexPage.TBODY + AnnexPage.TR + "[" + arg2 + "]" + AnnexPage.TD + "[" + arg1 + "]" + AnnexPage.AKNP + LegalActPage.LEOS_DOUBLE_COMPARE_REMOVED_ORIGINAL));
        for (WebElement element : elementList) {
            text = getElementAttributeTextContent(element);
            if (text.contains(arg0)) {
                bool = true;
                break;
            }
        }
        assertTrue(bool, "the data " + arg0 + " is not showing in bold and strikethrough in double diffing comparision page");
    }

    @And("annex title is {string}")
    public void annexTitleIs(String arg0) {
        String annexTitle = getElementAttributeInnerText(driver, AnnexPage.ANNEX_TITLE);
        assertEquals(annexTitle, arg0);
    }

    @Then("block name of the annex container is {string}")
    public void blockNameOfTheAnnexContainerIs(String arg0) {
        String annexContainerBlockName = getElementAttributeInnerText(driver, AnnexPage.ANNEX_PREFACE_CONTAINER_BLOCK);
        assertEquals(annexContainerBlockName, arg0);
    }

    @And("aknp tag {int} is showing in grey and strikethrough in data cell {int} of row {int} of table body in paragraph {int}")
    public void aknpTagIsShowingInGreyAndStrikethroughInDataCellOfRowOfTableBodyInParagraph(int arg0, int arg1, int arg2, int arg3) {
        WebElement ele = waitForElementTobePresent(driver, By.xpath(AnnexPage.PARAGRAPH + "[" + arg3 + "]" + AnnexPage.TABLE + AnnexPage.TBODY + AnnexPage.TR + "[" + arg2 + "]" + AnnexPage.TD + "[" + arg1 + "]" + AnnexPage.AKNP + "[" + arg0 + "]" + AnnexPage.LEOS_CONTENT_SOFT_REMOVED));
        assertNotNull(ele);
    }

    @And("authorial note with marker {int} is showing in strikethrough in aknp tag {int} in data cell {int} of row {int} of table body in paragraph {int}")
    public void authorialNoteWithMarkerIsShowingInStrikethroughInAknpTagInDataCellOfRowOfTableBodyInParagraph(int arg0, int arg1, int arg2, int arg3, int arg4) {
        WebElement ele = waitForElementTobePresent(driver, By.xpath(AnnexPage.PARAGRAPH + "[" + arg4 + "]" + AnnexPage.TABLE + AnnexPage.TBODY + AnnexPage.TR + "[" + arg3 + "]" + AnnexPage.TD + "[" + arg2 + "]" + AnnexPage.AKNP + "[" + arg1 + "]" + AnnexPage.SPAN + AnnexPage.LEOS_CONTENT_SOFT_REMOVED + AnnexPage.AUTHORIALNOTE + "[@marker='" + arg0 + "']"));
        assertNotNull(ele);
    }
}
