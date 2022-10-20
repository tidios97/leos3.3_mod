package europa.edit.stepdef;

import europa.edit.pages.*;
import europa.edit.util.BaseDriver;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import static europa.edit.util.Common.*;

public class CreateDraftSteps extends BaseDriver {

    @When("select option {string} from Council Explanatories section in template selection window")
    public void selectOptionFromCouncilExplanatoriesSectionInTemplateSelectionWindow(String arg0) {
        elementClick(driver, By.xpath(CreateDraftPage.V_TREE_NODE_CAPTION + CommonPage.XPATH_TEXT_1 + arg0 + CommonPage.XPATH_TEXT_2));
    }

    @When("provide draft title {string} in draft metadata page")
    public void provideDraftTitleInDraftMetadataPage(String title) {
        elementEcasSendkeys(driver, CreateDraftPage.DRAFT_TITLE, title);
    }
}
