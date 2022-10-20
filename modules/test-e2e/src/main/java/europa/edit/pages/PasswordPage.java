package europa.edit.pages;

import lombok.experimental.UtilityClass;
import org.openqa.selenium.By;

@UtilityClass
public class PasswordPage {
    public static final By PASSWORD = By.id("password");
    public static final By SIGN_IN_BUTTON = By.name("_submit");
}
