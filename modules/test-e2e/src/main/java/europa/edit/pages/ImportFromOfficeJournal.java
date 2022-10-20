package europa.edit.pages;

import lombok.experimental.UtilityClass;
import org.openqa.selenium.By;

@UtilityClass
public class ImportFromOfficeJournal {
    public static final By SEARCH_BUTTON = By.xpath("//*[text()='Search']//ancestor::div[@role='button']");
    public static final By I_BUTTON = By.xpath("//*[contains(@class,'v-icon-info_circle')]");
    public static final By IMPORT_BUTTON = By.xpath("//*[text()='Import']//ancestor::div[@role='button']");
    public static final By IMPORT_BUTTON_DISABLED = By.xpath("//*[text()='Import']//ancestor::div[@role='button' and @aria-disabled='true']");
    public static final By SELECT_ALL_RECITALS_BUTTON = By.xpath("//*[text()='Select all recitals']//ancestor::div[@role='button']");
    public static final By SELECT_ALL_RECITALS_BUTTON_DISABLED = By.xpath("//*[text()='Select all recitals']//ancestor::div[@role='button' and @aria-disabled='true']");
    public static final By SELECT_ALL_ARTICLES_BUTTON = By.xpath("//*[text()='Select all articles']//ancestor::div[@role='button']");
    public static final By SELECT_ALL_ARTICLES_BUTTON_DISABLED = By.xpath("//*[text()='Select all articles']//ancestor::div[@role='button' and @aria-disabled='true']");
    public static final By CLOSE_BUTTON = By.xpath("//*[text()='Close']//ancestor::div[@role='button']");
    public static final By I_MOUSE_HOVER_TEXT = By.xpath("//*[@class='v-tooltip-text']");
    public static final By NR_INPUT = By.xpath("//*[text()='Nr.']//ancestor::div[contains(@class,'v-caption-on-top')]//input");
    public static final By ERROR_INDICATOR = By.xpath("//*[contains(@class,'v-errorindicator-error')]");
    public static final By NR_INPUT_ERROR_INDICATOR = By.xpath("//input[contains(@class,'v-textfield-error-error')]");
    public static final By TYPE_SELECT_CLASS = By.xpath("//*[text()='Type']//ancestor::div[contains(@class,'v-caption-on-top')]//select");
    public static final By TYPE_SELECT_CLASS_OPTION = By.xpath("//*[text()='Type']//ancestor::div[contains(@class,'v-caption-on-top')]//select/option");
    public static final By YEAR_SELECT_CLASS = By.xpath("//*[text()='Year']//ancestor::div[contains(@class,'v-caption-on-top')]//select");
    public static final String RECITAL = "//div[contains(@class,'v-window')]//recital";
    public static final String ARTICLE = "//div[contains(@class,'v-window')]//article";
    public static final String BILL = "//*[@id='akomaNtoso']/bill";
    public static final String LEOS_IMPORT_WRAPPER = "//div[@class='leos-import-wrapper']";
    public static final String RECITALS = "//recitals";
    public static final String AKNBODY = "//aknbody";
}
