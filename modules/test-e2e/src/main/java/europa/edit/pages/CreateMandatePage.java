package europa.edit.pages;

import lombok.experimental.UtilityClass;
import org.openqa.selenium.By;

@UtilityClass
public class CreateMandatePage {
    public static final By UPLOAD_ICON_INPUT = By.xpath("//*[text()='Create new mandate - Upload a leg file (1/2)']//ancestor::div[@class='popupContent']//input[@type='file']");
    public static final By CREATE_NEW_MANDATE_PAGE_2 = By.xpath("//*[text()='Create new mandate - Draft metadata (2/2)']");
}
