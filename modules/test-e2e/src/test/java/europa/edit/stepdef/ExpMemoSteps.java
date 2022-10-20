package europa.edit.stepdef;

import europa.edit.pages.CommonPage;
import europa.edit.pages.ExpMemoPage;
import europa.edit.pages.ProposalViewerPage;
import europa.edit.util.BaseDriver;
import europa.edit.util.E2eUtil;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import java.util.ArrayList;
import java.util.List;

import static europa.edit.util.Common.*;
import static org.testng.Assert.*;

public class ExpMemoSteps extends BaseDriver {

    @When("click on open button for explanatory memorandum")
    public void clickOnOpenButtonForExplanatoryMemorandum() {
        elementClick(driver, ProposalViewerPage.EXP_MEMO_OPEN_BUTTON);
    }

    @Then("explanatory memorandum page is displayed")
    public void explanatoryMemorandumPageIsDisplayed() {
        boolean bool = verifyElement(driver, ExpMemoPage.EXP_MEMO_TEXT);
        assertTrue(bool);
    }

    @And("toc editing button is not displayed")
    public void tocEditingButtonIsNotDisplayed() {
        boolean bool = waitUnTillElementIsNotPresent(driver, ExpMemoPage.TOC_EDIT_BUTON);
        assertTrue(bool,"toc editing button is displayed");
    }

    @Then("page is redirected to {string}")
    public void pageIsRedirectedTo(String arg0) {
        boolean bool = verifyElement(driver,By.xpath(CommonPage.XPATH_TEXT_1+arg0+ CommonPage.XPATH_TEXT_2));
        assertTrue(bool);
    }

    @When("select {string} in the page")
    public void selectInThePage(String arg0) {
        boolean bool = selectTextThroughDoubleClick(driver,By.xpath(CommonPage.XPATH_TEXT_1+arg0+ CommonPage.XPATH_TEXT_2));
        assertTrue(bool, "Text is not selected");
    }

    @When("click on close button in explanatory memorandum page")
    public void clickOnCloseButtonInExplanatoryMemorandumPage() {
        elementClick(driver, ExpMemoPage.CLOSE_BUTON);
    }

    @And("navigation pane is displayed")
    public void navigationPaneIsDisplayed() {
        boolean bool = waitForElementTobeDisPlayed(driver, ExpMemoPage.NAVIGATION_PANE);
        assertTrue(bool, "navigation pane is not displayed");
    }

    @And("explanatory memorandum content is displayed")
    public void explanatoryMemorandumContentIsDisplayed() {
        boolean bool = verifyElement(driver, ExpMemoPage.EXP_MEMO_CONTENT);
        assertTrue(bool);
    }

    @Then("below sentences are present for user guidance")
    public void belowSentencesArePresentForUserGuidance(DataTable dataTable) {
        E2eUtil.wait(500);
        WebElement ele = waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        assertNotNull(ele);
        String text;
        List<String> actualOptionList = new ArrayList<>();
        List<String> givenOptionList = dataTable.asList(String.class);
        List<WebElement> elements = driver.findElements(ExpMemoPage.GUIDANCE_SPAN);
        assertTrue(null != elements && !elements.isEmpty(), "no element present for user guidance");
        for (WebElement element : elements) {
            text = getElementAttributeInnerText(element);
            actualOptionList.add(text);
        }
        for(String str : givenOptionList){
            System.out.println("givenOptionList " + str);
        }

        for(String str : actualOptionList){
            System.out.println("actualOptionList " + str);
        }
        assertEquals(actualOptionList.size(), givenOptionList.size());
        assertTrue(actualOptionList.containsAll(givenOptionList), "given sentences are not present in the actual sentences list");
    }
}