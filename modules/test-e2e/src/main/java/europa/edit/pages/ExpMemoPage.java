package europa.edit.pages;

import lombok.experimental.UtilityClass;
import org.openqa.selenium.By;

@UtilityClass
public class ExpMemoPage {
    public static final By EXP_MEMO_TEXT = By.xpath("//span[text()='Explanatory Memorandum']");
    public static final By TOC_EDIT_BUTON = By.xpath("//img[contains(@src,'toc-edit.png')]");
    public static final By ENABLE_ANNOTATION_POPUP = By.xpath("//*[@title=\"Enable Annotations' Popup\"]");
    public static final By CLOSE_BUTON = By.xpath("//*[text()='Close']");
    public static final By NAVIGATION_PANE = By.xpath("//*[text()='Navigation pane']");
    public static final By EXP_MEMO_CONTENT = By.xpath("//*[@id='docContainer']");
    public static final By GUIDANCE_SPAN = By.cssSelector("guidance span");
}

