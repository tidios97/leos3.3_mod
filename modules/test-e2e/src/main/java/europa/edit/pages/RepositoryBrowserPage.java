package europa.edit.pages;

import lombok.experimental.UtilityClass;
import org.openqa.selenium.By;

@UtilityClass
public class RepositoryBrowserPage {
    public static final By OPEN_BTN_1ST_PROPOSAL = By.xpath("(//*[text()='Open'])[1]");
    public static final By REPOSITORY_BROWSER_TEXT = By.xpath("//*[text()='Repository Browser']");
    public static final By SEARCHBAR = By.xpath("//input[@placeholder='Search by title']");
    public static final By RESET_BTN = By.xpath("//span[text()='Reset']");
    public static final By PROPOSAL_MANDATE_LIST_FIRST_TR = By.xpath("//table[@role='grid']/tbody/tr[1]");
    public static final By PROPOSAL_MANDATE_LIST_TR = By.xpath("//table[@role='grid']/tbody/tr");
    public static final By CREATE_PROPOSAL_BUTTON = By.xpath("//*[text()='Create proposal']");
    public static final By FILTER_SECTION = By.xpath("//*[text()='FILTERS']");
    public static final By UPLOAD_BUTTON = By.xpath("//*[text()='Upload']//ancestor::div[@role='button']");
    public static final By USERNAME_ICON = By.xpath("//*[@location='user']//*[@class='v-label v-widget v-label-undef-w']");
    public static final By FIRST_PROPOSAL = By.xpath("//table[@role='grid']/tbody/tr[1]/td//div[@class='leos-card-title v-widget v-has-width']");
    public static final By ROLE_BUTTON = By.cssSelector("[role='button']");
    public static final String LEOS_CARD_TITLE = "//div[contains(@class,'leos-card-title')]";
    public static final String PRECEDING_SIBLING_INPUT = "/preceding-sibling::input";
    public static final String CHECKBOX_NOT_CHECKED = "[@type='checkbox' and not(@checked)]";
    public static final String CHECKBOX_CHECKED = "[@type='checkbox' and (@checked)]";
    public static final String CLONED_PROPOSAL = "//div[contains(@class,'cloned-proposal')]";
    public static final String V_LABEL_CLONED_LABEL = "//*[contains(@class,'v-label-cloned-labels')]";
    public static final String PROPOSAL_MANDATE_LIST_TR_STRING = "//table[@role='grid']/tbody/tr";
}
