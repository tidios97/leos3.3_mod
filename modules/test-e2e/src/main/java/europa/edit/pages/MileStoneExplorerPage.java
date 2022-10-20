package europa.edit.pages;

import lombok.experimental.UtilityClass;
import org.openqa.selenium.By;

@UtilityClass
public class MileStoneExplorerPage {
    public static final By EXP_MEMO_TEXT = By.xpath("//table[@role='presentation']//div[contains(text(),'Explanatory Memorandum')]");
    public static final By LEGAL_ACT_TEXT = By.xpath("//table[@role='presentation']//div[contains(text(),'Legal Act')]");
    public static final By COVER_PAGE_TAB = By.xpath("//table[@role='presentation']//div[contains(text(),'Cover Page')]");
    public static final By MILESTONE_EXPLORER_TEXT = By.xpath("//*[text()='Milestone explorer']");
    public static final By CLOSE_BUTTON = By.xpath("//div[@class='v-slot v-slot-window-buttons-area']//*[text()='Close']");
    public static final By CITATIONS_TEXT = By.xpath("//*[text()='Citations ']");
    public static final By RECITALS_TEXT = By.xpath("//*[text()='Recitals ']");
    public static final By TABITEM_ANNEX_TEXT = By.xpath(MileStoneExplorerPage.V_TABSHEET_TABITEM + "//*[contains(text(),'Annex')]");
    public static final By PREFACE_CONTAINER_BLOCK_NUM = By.xpath("//*[@id='_preface__container__block__num']");
    public static final By ANNOTATION_SIDE_BAR_RIGHT_SIDE = By.xpath("//*[@class='annotator-frame-button annotator-frame-button--sidebar_toggle h-icon-chevron-right']");
    public static final By ORPHANS_LINK_IN_ANNOTATION_WINDOW = By.xpath("//div[@class='selection-tabs']//*[contains(text(),'Orphans')]//ancestor::a");
    public static final By EXPORT_BUTTON = By.xpath("//*[@class='v-button-caption' and text()='Export']");
    public static final By ROLE_TAB_LIST_TABLE_TR_TD = By.cssSelector("div[role='tablist'] table tbody tr td[role='tab']");
    public static final By V_CAPTION_TEXT = By.cssSelector(".v-captiontext");
    public static final String V_TABSHEET_TABITEM = "//*[@class='v-tabsheet-tabitem']";
}

