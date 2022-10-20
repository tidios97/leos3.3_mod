package europa.edit.pages;

import lombok.experimental.UtilityClass;
import org.openqa.selenium.By;

@UtilityClass
public class CKEditorPage {
    public static final By CKE_BUTTON = By.cssSelector(".cke_button");
    public static final By CLOSE_BUTTON = By.cssSelector(".cke_button__leosinlinecancel");
    public static final By SAVE_BUTTON = By.cssSelector(".cke_button__leosinlinesave");
    public static final By SAVE_CLOSE_BUTTON = By.cssSelector(".cke_button__leosinlinesaveclose");
    public static final By CUT_BUTTON = By.cssSelector(".cke_button__cut");
    public static final By COPY_BUTTON = By.cssSelector(".cke_button__copy");
    public static final By PASTE_BUTTON = By.cssSelector(".cke_button__paste");
    public static final By UNDO_BUTTON = By.cssSelector(".cke_button__undo");
    public static final By REDO_BUTTON = By.cssSelector(".cke_button__redo");
    public static final By SUBSCRIPT_BUTTON = By.cssSelector(".cke_button__subscript");
    public static final By SUPERSCRIPT_BUTTON = By.cssSelector(".cke_button__superscript");
    public static final By SPECIAL_CHARACTER_BUTTON = By.cssSelector(".cke_button__specialchar");
    public static final By SHOW_BLOCKS_BUTTON = By.cssSelector(".cke_button__leosshowblocks");
    public static final By SOURCE_DIALOG_BUTTON = By.cssSelector(".cke_button__sourcedialog");
    public static final By TABLE_ICON = By.cssSelector(".cke_button__table_icon");
    public static final By INSERT_FOOTNOTE_ICON = By.cssSelector(".cke_button__authorialnotewidget_icon");
    public static final By BOLD_ICON = By.cssSelector(".cke_button__bold_icon");
    public static final By DECREASE_INTEND_ICON = By.cssSelector(".cke_button__outdent");
    public static final By INCREASE_INTEND_ICON = By.cssSelector(".cke_button__indent");
    public static final By INTERNAL_REFERENCE_ICON = By.cssSelector(".cke_button__leoscrossreference");
    public static final By INTERNAL_REFERENCE_WINDOW = By.cssSelector("table.cke_single_page tbody div.cke_dialog_title");
    public static final By SOFT_ENTER = By.cssSelector(".cke_button__leoshierarchicalelementshiftenterhandler");
}

