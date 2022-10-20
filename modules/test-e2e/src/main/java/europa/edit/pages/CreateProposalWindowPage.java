package europa.edit.pages;

import org.openqa.selenium.By;

public class CreateProposalWindowPage {
    public static final By CREATE_BTN = By.xpath("//*[text()='Create']");
    public static final By PREVIOUS_BTN = By.xpath("//*[@role='button']//span[text()='Previous']");
    public static final By NEXTBTN = By.xpath("//span[text()='Next']");
    public static final By CANCELBTN = By.xpath("//span[text()='Cancel']");
    public static final By DOCUMENT_TITLE_INPUT = By.xpath("//span[text()='Document title:']/ancestor::tr[position() = 1]//input");
    public static final By UPLOAD_BTN_UPLOAD_WINDOW = By.xpath("//*[text()='Upload a legislative document - Upload a leg file (1/2)']//ancestor::div[@class='popupContent']//input[@type='file']");
    public static final By UPLOAD_WINDOW_FIRST_PAGE = By.xpath("//*[text()='Upload a legislative document - Upload a leg file (1/2)']");
    public static final By FILENAME_TXT = By.xpath("//span[text()='File name:']");
    public static final By VALID_ICON = By.xpath("//div[@class='v-label v-widget file-valid v-label-file-valid v-label-undef-w']");
    public static final String INTER_PROCEDURE = "//*[text()='Interinstitutional procedures - Law Initiative (COM/JOIN)']//ancestor::div[@role='treeitem']";
}
