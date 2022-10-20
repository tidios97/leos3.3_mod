package europa.edit.stepdef;

import europa.edit.pages.*;
import europa.edit.util.BaseDriver;
import europa.edit.util.E2eUtil;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import static europa.edit.util.Common.*;
import static org.testng.Assert.*;

public class CouncilExplanatorySteps extends BaseDriver {

    private static final Logger logger = LoggerFactory.getLogger(CouncilExplanatorySteps.class);

    @Then("{string} council explanatory page is displayed")
    public void councilExplanatoryPageIsDisplayed(String arg0) {
        String title = getElementAttributeInnerText(driver, CouncilExplanatoryPage.LEOS_EXPLANATORY_VIEW_TITLE);
        assertEquals(title, arg0);
    }

    @When("click on close button in Council Explanatory page")
    public void clickOnCloseButtonInCouncilExplanatoryPage() {
        elementClick(driver, CouncilExplanatoryPage.CLOSE_BUTTON);
    }

    @And("below {int} templates are displayed under council explanatories section in council explanatory template selection window")
    public void belowTemplatesAreDisplayedUnderCouncilExplanatoriesSectionInCouncilExplanatoryTemplateSelectionWindow(int arg0, DataTable dataTable) {
        String templateName;
        List<String> headerDetails = dataTable.asList(String.class);
        List<WebElement> elements = driver.findElements(CouncilExplanatoryPage.V_TREE_NODE_LEAF_SPAN);
        assertNotNull(elements, "No templates found in the Council Explanatory template window");
        assertEquals(elements.size(), arg0);
        for (WebElement element : elements) {
            templateName = getElementAttributeInnerText(element);
            assertTrue(headerDetails.contains(templateName));
        }
    }

    @When("click on {string} template under council explanatories section in council explanatory template selection window")
    public void clickOnTemplateUnderCouncilExplanatoriesSectionInCouncilExplanatoryTemplateSelectionWindow(String templateName) {
        elementClick(driver, By.xpath(CouncilExplanatoryPage.V_TREE_NODE_LEAF + "//span[text()='" + templateName + "']"));
    }

    @And("click on create button in council explanatory template selection window")
    public void clickOnCreateButtonInCouncilExplanatoryTemplateSelectionWindow() {
        elementClick(driver, CouncilExplanatoryPage.CREATE_BUTTON);
    }

    @Then("number of council explanatory is {int}")
    public void numberOfCouncilExplanatoryIs(int arg0) {
        E2eUtil.wait(2000);
        WebElement ele = waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        assertNotNull(ele, "unable to load the page in the specified time duration");
        List<WebElement> element = driver.findElements(ProposalViewerPage.COUNCIL_EXPLANATORY_TABLE_TBODY_TR);
        assertEquals(element.size(), arg0);
    }

    @Then("level {int} is displayed")
    public void levelIsDisplayed(int arg0) {
        boolean bool = waitForElementTobeDisPlayed(driver, By.xpath(AnnexPage.LEVEL + "[" + arg0 + "]"));
        assertTrue(bool);
    }

    @And("num {string} is present in level {int}")
    public void numIsPresentInLevel(String arg0, int arg1) {
        String num = getElementAttributeInnerText(driver, By.xpath(AnnexPage.LEVEL + "[" + arg1 + "]" + LegalActPage.NUM));
        assertEquals(num, arg0);
    }

    @And("heading {string} is present in level {int}")
    public void headingIsPresentInLevel(String arg0, int arg1) {
        String heading = getElementAttributeInnerText(driver, By.xpath(AnnexPage.LEVEL + "[" + arg1 + "]" + LegalActPage.HEADING));
        assertEquals(heading, arg0);
    }

    @And("content {string} is present in level {int}")
    public void contentIsPresentInLevel(String arg0, int arg1) {
        String content = getElementAttributeInnerText(driver, By.xpath(AnnexPage.LEVEL + "[" + arg1 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP));
        assertEquals(content, arg0);
    }

    @When("update the heading of level to {string} in ck editor")
    public void updateTheHeadingOfLevelToInCkEditor(String arg0) {
        setInnerTextToElementAttribute(driver, AnnexPage.CK_EDITOR_INLINE_OL_LI_AKN_ELEMENT_HEADING, arg0);
    }

    @And("append {string} to the content of level in ck editor")
    public void appendToTheContentOfLevelInCkEditor(String arg0) {
        String str = getElementAttributeInnerText(driver, AnnexPage.CK_EDITOR_INLINE_OL_LI_AKN_ELEMENT_SUBPARAGRAPH);
        elementEcasSendkeys(driver, AnnexPage.CK_EDITOR_INLINE_OL_LI_AKN_ELEMENT_SUBPARAGRAPH, str + arg0);
    }

    @And("{string} is present in heading of level while ck editor is open")
    public void isPresentInHeadingOfLevelWhileCkEditorIsOpen(String arg0) {
        String str = getElementAttributeInnerText(driver, AnnexPage.CK_EDITOR_INLINE_OL_LI_AKN_ELEMENT_HEADING);
        assertEquals(str, arg0);
    }

    @And("append {string} to the subparagraph p tag {int} of level while ck editor is open")
    public void appendToThePTagOfLevelWhileCkEditorIsOpen(String arg0, int arg1) {
        By by = By.xpath(AnnexPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI + AnnexPage.IMMEDIATE_P + AnnexPage.DATA_AKN_ELEMENT_SUBPARAGRAPH + "[" + arg1 + "]");
        try {
            WebElement element = waitForElementTobePresent(driver, by);
            WebElement brElement = element.findElement(By.tagName("br"));
            removeElementThroughJS(driver, brElement);
        }
        catch(Exception e){
            e.printStackTrace();
            logger.info("unable to find br element");
        }
        String str = getElementAttributeInnerText(driver, by);
        elementActionSendkeys(driver,str+arg0);
    }

    @And("{string} is present in subparagraph p tag {int} of level while ck editor is open")
    public void isPresentInSubparagraphPTagOfLevelWhileCkEditorIsOpen(String arg0, int arg1) {
        int pos = arg1 - 1;
        List<WebElement> elementList = driver.findElements(AnnexPage.CK_EDITOR_INLINE_OL_LI_AKN_ELEMENT_SUBPARAGRAPH);
        String str = getElementAttributeInnerText(elementList.get(pos));
        assertEquals(str, arg0);
    }

    @And("append {string} to point li tag {int} of ol tag {int} of level while ck editor is open")
    public void appendToTheLiTagOfOlTagOfLevelWhileCkEditorIsOpen(String arg0, int arg2, int arg3) {
        By by = By.xpath(AnnexPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI + AnnexPage.IMMEDIATE_OL + "[" + arg3 + "]" + AnnexPage.IMMEDIATE_LI + AnnexPage.DATA_AKN_ELEMENT_POINT + "[" + arg2 + "]");
        String str = getElementAttributeInnerText(driver, by);
        elementEcasSendkeys(driver, by, str + arg0);
    }

    @And("{string} is present in alinea p tag {int} of point li tag {int} of ol tag {int} of level while ck editor is open")
    public void isPresentInAlineaPTagOfPointLiTagOfOlTagOfLevelWhileCkEditorIsOpen(String arg0, int arg1, int arg2, int arg3) {
        String str = getElementAttributeInnerText(driver, By.xpath(AnnexPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI + AnnexPage.IMMEDIATE_OL + "[" + arg3 + "]" + AnnexPage.IMMEDIATE_LI + AnnexPage.DATA_AKN_ELEMENT_POINT + "[" + arg2 + "]" + AnnexPage.P + AnnexPage.DATA_AKN_ELEMENT_ALINEA + "[" + arg1 + "]"));
        assertEquals(str, arg0);
    }

    @And("{string} is present in point li tag {int} of ol tag {int} of point li tag {int} of ol tag {int} of level while ck editor is open")
    public void isPresentInPointLiTagOfOlTagOfPointLiTagOfOlTagOfLevelWhileCkEditorIsOpen(String arg0, int arg1, int arg2, int arg3, int arg4) {
        String str = getElementAttributeInnerText(driver, By.xpath(AnnexPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI + AnnexPage.IMMEDIATE_OL + "[" + arg4 + "]" + AnnexPage.IMMEDIATE_LI + AnnexPage.DATA_AKN_ELEMENT_POINT + "[" + arg3 + "]" + AnnexPage.IMMEDIATE_OL + "[" + arg2 + "]" + AnnexPage.IMMEDIATE_LI + AnnexPage.DATA_AKN_ELEMENT_POINT + "[" + arg1 + "]"));
        assertEquals(str, arg0);
    }

    @Then("{string} is present in subparagraph {int} of level {int}")
    public void isPresentInSubparagraphOfLevel(String arg0, int arg1, int arg2) {
        String str = getElementAttributeInnerText(driver, By.xpath(AnnexPage.LEVEL + "[" + arg2 + "]" + AnnexPage.SUBPARAGRAPH + "[" + arg1 + "]" + AnnexPage.CONTENT + AnnexPage.AKNP));
        assertEquals(str, arg0);
    }

    @And("{string} is present in alinea {int} of point {int} of list {int} of level {int}")
    public void isPresentInAlineaOfPointOfListOfLevel(String arg0, int arg1, int arg2, int arg3, int arg4) {
        String str = getElementAttributeInnerText(driver, By.xpath(AnnexPage.LEVEL + "[" + arg4 + "]" + LegalActPage.LIST + "[" + arg3 + "]" + LegalActPage.POINT + "[" + arg2 + "]" + AnnexPage.ALINEA + "[" + arg1 + "]" + AnnexPage.CONTENT + AnnexPage.AKNP));
        assertEquals(str, arg0);
    }

    @And("{string} is present in point {int} of list {int} of point {int} of list {int} of level {int}")
    public void isPresentInPointOfListOfPointOfListOfLevel(String arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
        String str = getElementAttributeInnerText(driver, By.xpath(AnnexPage.LEVEL + "[" + arg5 + "]" + LegalActPage.LIST + "[" + arg4 + "]" + LegalActPage.POINT + "[" + arg3 + "]" + LegalActPage.LIST + "[" + arg2 + "]" + AnnexPage.POINT + "[" + arg1 + "]" + AnnexPage.CONTENT + AnnexPage.AKNP));
        assertEquals(str, arg0);
    }

    @And("replace {string} to the content of subparagraph p tag {int} of level while ck editor is open")
    public void replaceToTheContentOfSubParagraphCkEditorIsOpen(String arg0, int arg1) {
        By by = By.xpath(AnnexPage.CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI + AnnexPage.IMMEDIATE_P + AnnexPage.DATA_AKN_ELEMENT_SUBPARAGRAPH + "[" + arg1 + "]");
        setInnerTextToElementAttribute(driver, by, arg0);
    }

    @And("select {string} from heading of division {int}")
    public void selectFromHeadingOfDivision(String arg0, int arg1) {
        E2eUtil.wait(1000);
        selectTextFromElement(driver, By.xpath(CouncilExplanatoryPage.DIVISION + "[" + arg1 + "]" + CouncilExplanatoryPage.HEADING), arg0);
    }

    @And("select {string} from content of level {int}")
    public void selectFromContentOfLevel(String arg0, int arg1) {
        E2eUtil.wait(1000);
        selectTextFromElement(driver, By.xpath(CouncilExplanatoryPage.LEVEL + "[" + arg1 + "]" + CouncilExplanatoryPage.CONTENT + CouncilExplanatoryPage.AKNP), arg0);
    }

    @And("select {string} from content of paragraph {int}")
    public void selectFromContentOfParagraph(String arg0, int arg1) {
        E2eUtil.wait(1000);
        selectTextFromElement(driver, By.xpath(CouncilExplanatoryPage.PARAGRAPH + "[" + arg1 + "]" + CouncilExplanatoryPage.CONTENT + CouncilExplanatoryPage.AKNP), arg0);
    }
}
