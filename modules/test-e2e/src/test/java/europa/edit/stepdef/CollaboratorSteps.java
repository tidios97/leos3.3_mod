package europa.edit.stepdef;

import europa.edit.pages.CollaboratorPage;
import europa.edit.pages.CommonPage;
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

public class CollaboratorSteps extends BaseDriver {

    @When("search {string} in the role input field")
    public void searchInTheRoleInputField(String arg0) {
        elementEcasSendkeys(driver, CollaboratorPage.COLLABORATORS_NAME_ROLE_INPUT_BOX, arg0);
    }

    @Then("{string} role is showing in the list")
    public void roleIsShowingInTheList(String arg0) {
        String role = null;
        List<WebElement> elementList = driver.findElements(By.xpath(CollaboratorPage.COLLABORATORS_SEARCH_RESULTS_TR));
        assertTrue(elementList.size() > 0, "No lists found in the search results");
        for (int i = 1; i <= elementList.size(); i++) {
            role = driver.findElement(By.xpath(CollaboratorPage.COLLABORATORS_SEARCH_RESULTS_TR + "[" + i + "]/td/span")).getText();
        }
        assertTrue(role.equalsIgnoreCase(arg0), arg0 + " is not present in the search results");
    }

    @When("click on first role showing in the list")
    public void clickOnFirstRoleShowingInTheList() {
        elementClick(driver, By.xpath(CollaboratorPage.COLLABORATORS_SEARCH_RESULTS_TR + "[1]/td/span"));
    }

    @Then("{string} role is selected in the role input field")
    public void roleIsSelectedInTheRoleInputField(String arg0) {
        String str = getElementAttributeValue(driver, CollaboratorPage.COLLABORATORS_NAME_ROLE_INPUT_BOX);
        assertTrue(str.contains(arg0), arg0 + " role is not selected in the role input field");
    }

    @And("{string} role is showing in the collaborator list")
    public void roleIsShowingInTheCollaboratorList(String arg0) {
        String role;
        boolean bool = false;
        WebElement ele = waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        assertNotNull(ele, "unable to load the page in the specified time duration");
        List<WebElement> elementList = driver.findElements(By.xpath(CollaboratorPage.COLLABORATORS_TABLE_TBODY_TR));
        assertTrue(elementList.size() > 0, "No rows present in the collaborator table");
        for (int i = 1; i <= elementList.size(); i++) {
            role = driver.findElement(By.xpath(CollaboratorPage.COLLABORATORS_TABLE_TBODY_TR + "[" + i + "]/td[3]")).getText();
            if (role.contains(arg0)) {
                bool = true;
                break;
            }
        }
        assertTrue(bool, arg0 + " role is not showing in the collaborator list");
    }

    @Then("collaborator save button is displayed")
    public void collaboratorSaveButtonIsDisplayed() {
        boolean bool = verifyElement(driver, CollaboratorPage.COLLABORATORS_SAVE_BUTTON);
        assertTrue(bool, "collaborator save button is not displayed");
    }

    @And("collaborator cancel button is displayed")
    public void collaboratorCancelButtonIsDisplayed() {
        boolean bool = verifyElement(driver, CollaboratorPage.COLLABORATORS_CANCEL_BUTTON);
        assertTrue(bool, "collaborator cancel button is not displayed");
    }

    @And("search input box is enabled for name column in Collaborator section")
    public void searchInputBoxIsEnabledForNameColumnInCollaboratorSection() {
        boolean bool = verifyElement(driver, CollaboratorPage.COLLABORATORS_NAME_1ST_INPUT_BOX);
        assertTrue(bool, "search input box is not displayed for name column in Collaborator section");
    }

    @When("search {string} in the name input field")
    public void searchInTheNameInputField(String arg0) {
        elementEcasSendkeys(driver, CollaboratorPage.COLLABORATORS_NAME_1ST_INPUT_BOX, arg0);
        E2eUtil.wait(2000);
    }

    @Then("{string} user is showing in the list")
    public void stringUserIsShowingInTheList(String arg0) {
        String name;
        String[] nameList = new String[3];
        List<WebElement> elementList = driver.findElements(By.xpath(CollaboratorPage.COLLABORATORS_SEARCH_RESULTS_TR));
        assertTrue(elementList.size() > 0, "No lists found in the search results");
        for (int i = 1; i <= elementList.size(); i++) {
            name = driver.findElement(By.xpath(CollaboratorPage.COLLABORATORS_SEARCH_RESULTS_TR + "[" + i + "]/td/span")).getText();
            nameList = name.split(" ");
        }
        assertTrue(nameList[0].contains(arg0), arg0 + " is not present in the search results");
    }

    @When("click on first user showing in the list")
    public void clickOnUser() {
        elementClick(driver, By.xpath(CollaboratorPage.COLLABORATORS_SEARCH_RESULTS_TR + "[1]/td/span"));
    }

    @Then("{string} user is selected in the name input field")
    public void userIsSelectedInTheNameInputField(String arg0) {
        String str = getElementAttributeValue(driver, CollaboratorPage.COLLABORATORS_NAME_1ST_INPUT_BOX);
        assertTrue(str.contains(arg0), arg0 + " user is not selected in the name input field");
    }

    @When("click on save button in Collaborator section")
    public void clickOnSaveButtonInCollaboratorSection() {
        elementClick(driver, CollaboratorPage.COLLABORATORS_SAVE_BUTTON);
        E2eUtil.wait(5000);
    }

    @Then("{string} user is showing in the collaborator list")
    public void userIsShowingInTheCollaboratorList(String arg0) {
        String name;
        boolean bool = false;
        WebElement ele = waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        assertNotNull(ele, "unable to load the page in the specified time duration");
        List<WebElement> elementList = driver.findElements(By.xpath(CollaboratorPage.COLLABORATORS_TABLE_TBODY_TR));
        assertTrue(elementList.size() > 0, "No rows present in the collaborator table");
        for (int i = 1; i <= elementList.size(); i++) {
            name = driver.findElement(By.xpath(CollaboratorPage.COLLABORATORS_TABLE_TBODY_TR + "[" + i + "]/td[1]")).getText();
            if (name.contains(arg0)) {
                bool = true;
                break;
            }
        }
        assertTrue(bool, arg0 + " user is not showing in the collaborator list");
    }

    @Then("user {string} is showing in the list")
    public void userStringIsShowingInTheList(String arg0) {
        String name;
        boolean result = false;
        List<WebElement> elementList = driver.findElements(By.xpath(CollaboratorPage.COLLABORATORS_SEARCH_RESULTS_TR));
        assertTrue(elementList.size() > 0, "No lists found in the search results");
        for (int i = 1; i <= elementList.size(); i++) {
            name = driver.findElement(By.xpath(CollaboratorPage.COLLABORATORS_SEARCH_RESULTS_TR + "[" + i + "]/td/span")).getText();
            if (name.contains(arg0)) {
                result = true;
                break;
            }
        }
        assertTrue(result, arg0 + " is not present in the search results");
    }

    @And("{string} is added as {string} in collaborators section")
    public void isAddedAsInCollaboratorsSection(String arg0, String arg1) {
        List<WebElement> elements = driver.findElements(By.xpath(CollaboratorPage.COLLABORATORS_TABLE_TBODY_TR));
        assertTrue(elements.size() > 0, "No rows found in collaborators section");
        String name = waitForElementTobePresent(driver, By.xpath(CollaboratorPage.COLLABORATORS_TABLE_TBODY_TR + "[1]/td[1]")).getText();
        assertEquals(name, arg0, "Name is not " + arg0);
        String role = waitForElementTobePresent(driver, By.xpath(CollaboratorPage.COLLABORATORS_TABLE_TBODY_TR + "[1]/td[3]//input")).getAttribute("value");
        assertEquals(role, arg1, "Role is not " + arg1);
    }

    @When("click on down arrow button present for the role input field")
    public void clickOnDownArrowButtonPresentForTheRoleInputField() {
        elementClick(driver, CollaboratorPage.COLLABORATORS_ROLE_INPUT_DROPDOWN);
    }

    @When("click on {string} from role dropdown list")
    public void clickOnFromRoleDropdownList(String arg0) {
        boolean bool = false;
        List<WebElement> roleList = driver.findElements(CollaboratorPage.ROLE_DROPDOWN_LIST);
        for(WebElement role : roleList){
            if(arg0.equalsIgnoreCase(role.getText())){
                bool = true;
                elementClick(role);
                break;
            }
        }
        assertTrue(bool, arg0 + " role is not present in the role dropdown list");
   }

    @Then("{string} user is showing in row {int} of the collaborator list")
    public void userIsShowingInRowOfTheCollaboratorList(String arg0, int arg1) {
        String user = getElementAttributeInnerText(driver, By.cssSelector(CollaboratorPage.COLLABORATORS_BLOCK_TABLE_TBODY_TR + ":nth-child("+arg1+") td:nth-child(1)"));
        assertTrue(user.contains(arg0));
    }

    @And("{string} role is showing in row {int} of the collaborator list")
    public void roleIsShowingInRowOfTheCollaboratorList(String arg0, int arg1) {
        String role = getElementAttributeValue(driver, By.cssSelector(CollaboratorPage.COLLABORATORS_BLOCK_TABLE_TBODY_TR + ":nth-child("+arg1+") td:nth-child(3) input"));
        assertEquals(role, arg0);
    }

    @Then("below roles are shown in role dropdown")
    public void belowRolesAreShownInRoleDropdown(DataTable dataTable) {
        List<String> actualRoleList = new ArrayList<>();
        List<String> givenRoleList = dataTable.asList(String.class);
        List<WebElement> roleList = driver.findElements(CollaboratorPage.ROLE_DROPDOWN_LIST);
        assertTrue(null != roleList && !roleList.isEmpty(), "no role is present");
        for (WebElement role : roleList) {
            actualRoleList.add(getElementAttributeInnerText(role).trim());
        }
        assertTrue(actualRoleList.containsAll(givenRoleList), "mentioned roles are not present in the actual role list");
    }
}