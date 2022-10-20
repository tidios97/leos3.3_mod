package europa.edit.pages;

import lombok.experimental.UtilityClass;
import org.openqa.selenium.By;

@UtilityClass
public class CollaboratorPage {
    public static final By COLLABORATORS_SAVE_BUTTON = By.xpath("//*[@class='v-grid-editor-save']");
    public static final By COLLABORATORS_CANCEL_BUTTON = By.xpath("//*[@class='v-grid-editor-cancel']");
    public static final By COLLABORATORS_NAME_1ST_INPUT_BOX = By.xpath("(//div[contains(@class,'v-grid-editor-cells')]//input)[1]");
    public static final By COLLABORATORS_NAME_ROLE_INPUT_BOX = By.xpath("(//div[contains(@class,'v-grid-editor-cells')]//input)[3]");
    public static final By COLLABORATORS_ROLE_INPUT_DROPDOWN = By.cssSelector(".v-grid-editor.buffered > div:nth-child(2) > div:nth-child(3) .v-filterselect-button");
    public static final By ROLE_DROPDOWN_LIST = By.cssSelector("#VAADIN_COMBOBOX_OPTIONLIST table tbody tr td span");
    public static final String COLLABORATORS_SEARCH_RESULTS_TR = "//*[@id='VAADIN_COMBOBOX_OPTIONLIST']//table/tbody/tr";
    public static final String COLLABORATORS_TABLE_TBODY_TR = "//*[text()='Collaborators']//ancestor::div[contains(@class,'v-slot-collaborator-block')]//table/tbody//tr";
    public static final String COLLABORATORS_BLOCK_TABLE_TBODY_TR = ".collaborator-block table tbody tr";
}

