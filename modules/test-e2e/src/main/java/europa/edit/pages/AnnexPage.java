package europa.edit.pages;

import lombok.experimental.UtilityClass;
import org.openqa.selenium.By;

@UtilityClass
public class AnnexPage {
    public static final By PREFACE = By.xpath("//*[text()='Preface']");
    public static final By BODY = By.xpath("//*[text()='Body']");
    public static final By ANNEX_TITLE = By.cssSelector(".leos-annexview-title");
    public static final By ANNEX_PREFACE_CONTAINER_BLOCK = By.cssSelector("preface container block[name='num']");
    public static final By TOC_CANCEL_BUTTON = By.xpath("//img[contains(@src,'toc-cancel.png')]");
    public static final By TOC_SAVE_BUTTON = By.xpath("//img[contains(@src,'toc-save.png')]");
    public static final By TOC_SAVE_AND_CLOSE_BUTTON = By.xpath("//img[contains(@src,'toc-save-close.png')]");
    public static final By SHOW_ALL_ACTIONS = By.xpath("//*[@title='Show all actions' and @style='display: inline-block;']");
    public static final By SHOW_ALL_ACTIONS_INSERT_BEFORE = By.xpath("//*[@data-widget-type='insert.before' and @style='display: inline-block;']");
    public static final By SHOW_ALL_ACTIONS_INSERT_AFTER = By.xpath("//*[@data-widget-type='insert.after' and @style='transform: rotate(180deg); display: inline-block;']");
    public static final By SHOW_ALL_ACTIONS_EDIT = By.xpath("//*[@data-widget-type='edit' and @style='display: inline-block;']");
    public static final By SHOW_ALL_ACTIONS_DELETE = By.xpath("//*[@data-widget-type='delete' and @style='display: inline-block;']");
    public static final By CLOSE_BUTTON = By.xpath("//span[text()='Close']");
    public static final By ANNEX = By.xpath("//span[@class='leos-header-breadcrumb-wrap']//span[text()='Annex']");
    public static final By ANNEX_DELETION_BUTTON = By.xpath("//*[text()='Annex deletion: confirmation']//ancestor::div[@class='popupContent']//*[text()='Delete']");
    public static final By AUTHORIAL_NOTE_TEXT_LIST = By.xpath("//span[@class='leos-authnote-table']//span[contains(@id,'endNote')]//text");
    public static final By CK_PANEL_FRAME_1 = By.xpath("(//*[@class='cke_panel_frame'])[1]");
    public static final By CK_PANEL_FRAME_2 = By.xpath("(//*[@class='cke_panel_frame'])[2]");
    public static final By SAVE_THIS_VERSION_WINDOW = By.xpath("//*[@class='v-window-header' and text()='Save this version']");
    public static final By TITLE_SAVE_THIS_VERSION_WINDOW = By.xpath("//*[@class='v-window-contents']//input[@placeholder='e.g.: this is my title']");
    public static final By RESTORE_VERSION_WINDOW = By.xpath("//*[@class='v-window-header' and text()='Restore version']");
    public static final By REVERT_BUTTON_IN_RESTORE_VERSION_WINDOW = By.xpath("//*[@id='confirmdialog-ok-button']//*[text()='Revert']");
    public static final By CK_EDITOR_INLINE_OL_LI_AKN_ELEMENT_HEADING = By.cssSelector(".cke_editable_inline ol li .akn-element-heading");
    public static final By CK_EDITOR_INLINE_OL_LI_AKN_ELEMENT_SUBPARAGRAPH = By.cssSelector(".cke_editable_inline ol li p[data-akn-element='subparagraph']");



    public static final String LEOS_AUTH_NOTE_TABLE = "//span[@class='leos-authnote-table']";
    public static final String LEOS_AUTH_NOTE = "//span[contains(@class,'leos-authnote')]";
    public static final String CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI = "//*[contains(@class,cke_editable_inline) and @role='textbox']/ol/li";
    public static final String CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI_TABLE_TBODY_TR = "//*[contains(@class,cke_editable_inline) and @role='textbox']//ol//li//table//tbody//tr";
    public static final String CK_EDITOR_PARAGRAPH_INNERTEXT_OL_LI_TABLE_THEAD_TR = "//*[contains(@class,cke_editable_inline) and @role='textbox']//ol//li//table//thead//tr";
    public static final String CKE_WRAPPER_AUTHORIALNOTE = "//span[contains(@class,'cke_widget_wrapper_authorialnote')]";
    public static final String CKE_MENUITEM = "//span[@class='cke_menuitem']";
    public static final String CKE_MENUBUTTON_INNER = "//span[@class='cke_menubutton_inner']";
    public static final String SHOW_ALL_ACTIONS_ICON = "//following-sibling::div[1][@class='leos-actions Vaadin-Icons']/span[@title='Show all actions']";
    public static final String TOC_TABLE_TR = "//table[@role='treegrid']//tbody//tr";
    public static final String LEVEL = "//level";
    public static final String AKNP = "//aknp";
    public static final String PARAGRAPH = "//paragraph";
    public static final String SUBPARAGRAPH = "//subparagraph";
    public static final String POINT = "//point";
    public static final String ALINEA = "//alinea";
    public static final String CONTENT = "//content";
    public static final String SPAN = "//span";
    public static final String TABLE = "//table";
    public static final String TBODY = "//tbody";
    public static final String TR = "//tr";
    public static final String TD = "//td";
    public static final String TH = "//th";
    public static final String P = "//p";
    public static final String IMMEDIATE_OL = "/ol";
    public static final String IMMEDIATE_LI = "/li";
    public static final String IMMEDIATE_P = "/p";
    public static final String AUTHORIALNOTE = "//authorialnote";
    public static final String DATA_AKN_ELEMENT_ALINEA = "[@data-akn-element='alinea']";
    public static final String DATA_AKN_ELEMENT_POINT = "[@data-akn-element='point']";
    public static final String DATA_AKN_ELEMENT_SUBPARAGRAPH = "[@data-akn-element='subparagraph']";
    public static final String LEOS_DOUBLE_COMPARISON_CONTENT = "//*[@id='leos-double-comparison-content']";
    public static final String AUTHORIALNOTEWIDGET = "//span[contains(@class,'cke_widget_authorialNoteWidget')]";
    public static final String LEOS_CONTENT_SOFT_NEW = "[@class='leos-content-soft-new']";
    public static final String LEOS_CONTENT_SOFT_REMOVED = "[@class='leos-content-soft-removed']";
    public static final String LEOS_CONTENT_NEW_CN = "[@class='leos-content-new-cn']";
    public static final String LEOS_CONTENT_REMOVED_CN = "[@class='leos-content-removed-cn']";
    public static final String LEOS_DOUBLE_COMPARE_ADDED_ORIGINAL = "[@class='leos-double-compare-added-original']";
    public static final String LEOS_DOUBLE_COMPARE_REMOVED_ORIGINAL = "[@class='leos-double-compare-removed-original']";
    public static final String LEOS_DOUBLE_COMPARE_ADDED_INTERMEDIATE = "[@class='leos-double-compare-added-intermediate']";
    public static final String LEOS_DOUBLE_COMPARE_REMOVED_INTERMEDIATE = "[@class='leos-double-compare-removed-intermediate']";
}