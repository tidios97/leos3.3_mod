package europa.edit.util;

import org.openqa.selenium.WebDriver;

public class BaseDriver{
    public WebDriver driver = WebDriverFactory.getInstance().getWebdriver();
    public Configuration config = new Configuration();
    public Cryptor td = new Cryptor();
}
