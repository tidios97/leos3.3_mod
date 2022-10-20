package europa.edit.pages;

import lombok.experimental.UtilityClass;
import org.openqa.selenium.By;

@UtilityClass
public class CoverPage {
    public static final By COVER_PAGE_LEOS_HEADER = By.xpath("//*[@class='leos-header-breadcrumb-caption' and text()='Cover Page']");
    public static final By COVER_PAGE_LONG_TITLE = By.cssSelector("#_coverpage__longtitle__p__docpurpose");
    public static final By COVER_PAGE_LONG_TITLE_EDIT_BUTTON = By.cssSelector(".leos-actions span[data-widget-type='edit']");
    public static final By COVER_PAGE_MAIN_DOC_LOCATION = By.cssSelector("#_coverpage__container_mainDoc__block_1__location");
    public static final By COVER_PAGE_MAIN_LONG_TITLE_DOCSTAGE = By.cssSelector("#_coverpage__longtitle #_coverpage__longtitle__p__docstage");
    public static final By COVER_PAGE_MAIN_LONG_TITLE_DOCTYPE = By.cssSelector("#_coverpage__longtitle #_coverpage__longtitle__p__doctype");
    public static final By COVER_PAGE_MAIN_LONG_TITLE_DOCPURPOSE = By.cssSelector("#_coverpage__longtitle #_coverpage__longtitle__p__docpurpose");
    public static final By COVER_PAGE_MAIN_LONG_TITLE_DOCPURPOSE_HIGHLIGHT_ANNOTATOR_HL = By.cssSelector("#_coverpage__longtitle #_coverpage__longtitle__p__docpurpose hypothesis-highlight.annotator-hl");
}

