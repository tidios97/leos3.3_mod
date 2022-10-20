package europa.edit.stepdef;

import europa.edit.pages.LegalActPage;
import europa.edit.util.BaseDriver;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import static europa.edit.util.Common.*;
import static org.testng.Assert.*;

public class ExportToEConsiliumSteps extends BaseDriver {

    @And("{string} option is ticked in Export to eConsilium window")
    public void optionIsTickedInExportToEConsiliumWindow(String arg0) {
        boolean bool = verifyIsElementSelected(driver, By.xpath("//*[text()='" + arg0 + "']//preceding-sibling::input"));
        assertTrue(bool, arg0 + " option is not ticked in Export to eConsilium window");
    }

    @When("provide title {string} in Export to eConsilium window")
    public void provideTitleInExportToEConsiliumWindow(String arg0) {
        elementEcasSendkeys(driver, LegalActPage.TITLE_ECONSILIUM_WINDOW, arg0);
    }

    @And("tick {string} option in Export to eConsilium window")
    public void tickOptionInExportToEConsiliumWindow(String arg0) {
        elementClick(driver, By.xpath("//*[text()='" + arg0 + "']//preceding-sibling::input"));
    }

    @And("print style option {string} is disabled in Export to eConsilium window")
    public void printStyleOptionIsDisabledInExportToEConsiliumWindow(String arg0) {
        boolean bool = verifyElementIsEnabled(driver, By.xpath("//*[text()='" + arg0 + "']//preceding-sibling::input"));
        assertFalse(bool, arg0 + " option is not disabled in Export to eConsilium window");
    }

    @And("print style option {string} is checked in Export to eConsilium window")
    public void printStyleOptionIsCheckedInExportToEConsiliumWindow(String arg0) {
        boolean bool = verifyIsElementSelected(driver, By.xpath("//*[text()='" + arg0 + "']//preceding-sibling::input"));
        assertTrue(bool, arg0 + " option is not checked in Export to eConsilium window");
    }

    @And("print style option {string} is not checked in Export to eConsilium window")
    public void printStyleOptionIsNotCheckedInExportToEConsiliumWindow(String arg0) {
        boolean bool = verifyIsElementSelected(driver, By.xpath("//*[text()='" + arg0 + "']//preceding-sibling::input"));
        assertFalse(bool, arg0 + " option is checked in Export to eConsilium window");
    }

    @And("untick {string} option in Export to eConsilium window")
    public void untickOptionInExportToEConsiliumWindow(String arg0) {
        elementClick(driver, By.xpath("//*[text()='" + arg0 + "']//preceding-sibling::input[@checked]"));
    }

    @Then("{string} option is unticked in Export to eConsilium window")
    public void optionIsUntickedInExportToEConsiliumWindow(String arg0) {
        boolean bool = verifyIsElementSelected(driver, By.xpath("//*[text()='" + arg0 + "']//preceding-sibling::input"));
        assertFalse(bool, arg0 + " option is ticked in Export to eConsilium window");
    }

    @And("click on export button in Export to eConsilium window")
    public void clickOnExportButtonInExportToEConsiliumWindow() {
        elementClick(driver, LegalActPage.EXPORT_BUTTON_ECONSILIUM_WINDOW);
    }
}
