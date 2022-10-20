package europa.edit.util;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.Platform;
import org.openqa.selenium.UsernameAndPassword;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.http.ClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* 	Author: Satyabrata Das
 * 	Functionality: Webdriver class to create the webdriver object
 */

public class WebDriverFactory {

    private String testBrowser;
    private WebDriver driver;
    private static WebDriverFactory instance;
    public static final String CHROME = "chrome";
    public static final String FIREFOX = "firefox";
    public static final String IE = "internetexplorer";
    public static final String EDGE = "edge";
    private static final int TIMEOUTDELAY = 120;
    private static final Configuration config = new Configuration();
    private static final Logger logger = LoggerFactory.getLogger(WebDriverFactory.class);

    private WebDriverFactory() {
    }

    public static WebDriverFactory getInstance() {
        if (instance == null) {
            instance = new WebDriverFactory();
            instance.setTestBrowser(TestParameters.getInstance().getBrowser());
        }
        return instance;
    }

    public void setTestBrowser(String testBrowser) {
        this.testBrowser = testBrowser;
    }

    public WebDriver getWebdriver() {
        return driver;
    }

    public void setWebDriver() {
        String exeMode = TestParameters.getInstance().getMode();
        String gridUrl = config.getProperty("grid.url");
        switch (exeMode) {
            case "local":
                localDriver(testBrowser);
                return;
            case "remote":
                remoteDriver(testBrowser, gridUrl);
                return;
            default:
        }
    }

    public void remoteDriver(String browser, String gridUrl) {

        DesiredCapabilities capability;
        switch (browser) {

            case FIREFOX:
                try {
                    FirefoxOptions options = new FirefoxOptions();
                    options.addArguments("--incognito");
                    options.addArguments("--disable-gpu");
                    options.addArguments("--no-sandbox");
                    options.addArguments("--disable-browser-side-navigation");
                    options.setAcceptInsecureCerts(true);
                    options.setPageLoadStrategy(PageLoadStrategy.EAGER);
                    capability = new DesiredCapabilities();
                    capability.setCapability(FirefoxOptions.FIREFOX_OPTIONS, options);
                    capability.setBrowserName(FIREFOX);
                    capability.setPlatform(Platform.ANY);
                    capability.setCapability("ignoreZoomSetting", true);
                    options.merge(capability);
                    driver = new RemoteWebDriver(new URL(gridUrl), options);
                    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(TIMEOUTDELAY));
                    driver.manage().window().maximize();
                    driver.manage().deleteAllCookies();
                } catch (MalformedURLException e) {
                    logger.error(e.getMessage(), e);
                }
                break;

            case IE:
                try {
                    InternetExplorerOptions options = new InternetExplorerOptions();
                    options.setPageLoadStrategy(PageLoadStrategy.EAGER);
                    capability = new DesiredCapabilities();
                    capability.setBrowserName(IE);
                    capability.setPlatform(Platform.ANY);
                    capability.setCapability("ignoreZoomSetting", true);
                    options.merge(capability);
                    driver = new RemoteWebDriver(new URL(gridUrl), options);
                    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(TIMEOUTDELAY));
                    driver.manage().window().maximize();
                    driver.manage().deleteAllCookies();
                } catch (MalformedURLException e) {
                    logger.error(e.getMessage(), e);
                }
                break;

            case EDGE:
                try {
                    EdgeOptions options = new EdgeOptions();
                    capability = new DesiredCapabilities();
                    capability.setBrowserName(EDGE);
                    capability.setPlatform(Platform.ANY);
                    capability.setCapability("ignoreZoomSetting", true);
                    options.merge(capability);
                    driver = new RemoteWebDriver(new URL(gridUrl), options);
                    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(TIMEOUTDELAY));
                    driver.manage().window().maximize();
                    driver.manage().deleteAllCookies();
                } catch (MalformedURLException e) {
                    logger.error(e.getMessage(), e);
                }
                break;

            default:
                try {
                    ChromeOptions options = new ChromeOptions();
                    options.addArguments("--incognito");
                    options.addArguments("--disable-gpu");
                    options.addArguments("--no-sandbox");
                    options.addArguments("--disable-browser-side-navigation");
                    options.setAcceptInsecureCerts(true);
                    options.setPageLoadStrategy(PageLoadStrategy.EAGER);
                    Map<String, Object> prefs = new HashMap<>();
                    prefs.put("download.default_directory", config.getProperty("path.remote.download"));
                    options.setExperimentalOption("prefs", prefs);
                    options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
                    capability = new DesiredCapabilities();
                    capability.setCapability(ChromeOptions.CAPABILITY, options);
                    capability.setBrowserName(CHROME);
                    capability.setPlatform(Platform.ANY);
                    capability.setCapability("ignoreZoomSetting", true);
                    options.merge(capability);
                    driver = new RemoteWebDriver(new URL(gridUrl), options);
/*                    Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy-t2-lu.welcome.ec.europa.eu", 8012));
                    ClientConfig config = ClientConfig.defaultConfig()
                            .baseUrl(new URL(gridUrl))
                            .authenticateAs(new UsernameAndPassword("dasatya", "London1234"))
                            .proxy(proxy);
                    driver = RemoteWebDriver.builder()
                            .oneOf(options)
                            .config(config)
                            .build();*/
                    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(TIMEOUTDELAY));
                    driver.manage().window().maximize();
                    driver.manage().deleteAllCookies();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                break;
        }
    }

    public void localDriver(String browser) {
        DesiredCapabilities capability;
        switch (browser) {
            case FIREFOX: // If user choose Firefox driver has been changed to Firefox
                driver = new FirefoxDriver();
                break;
            case IE: // If user choose InternetExplorer driver has been changed to IE
                driver = new InternetExplorerDriver();
                break;
            case EDGE: // If user choose edge driver has been changed to EDGE
                driver = new EdgeDriver();
                break;
            default:// If user choose Chrome driver has been changed to Chrome
                //WebDriverManager.chromedriver().setup();
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--disable-gpu");
                options.addArguments("--no-sandbox");
                options.addArguments("--disable-browser-side-navigation");
                options.addArguments("--incognito");
                options.setAcceptInsecureCerts(true);
                options.setPageLoadStrategy(PageLoadStrategy.EAGER);
                Map<String, Object> prefs = new HashMap<>();
                prefs.put("download.default_directory", System.getProperty("user.dir") + config.getProperty("relative.download.path.local"));
                options.setExperimentalOption("prefs", prefs);
                options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
                capability = new DesiredCapabilities();
                capability.setCapability(ChromeOptions.CAPABILITY, options);
                capability.setBrowserName(CHROME);
                capability.setPlatform(Platform.ANY);
                capability.setCapability("ignoreZoomSetting", true);
                capability.setCapability(ChromeOptions.CAPABILITY, options);
                driver = WebDriverManager.chromedriver().capabilities(capability).create();
/*                File configBaseFile = new File(Objects.requireNonNull(Configuration.class.getClassLoader().getResource("drivers/chromedriver.exe")).getFile());
                String absolutePath = configBaseFile.getAbsolutePath();
                System.setProperty("webdriver.chrome.driver", absolutePath);*/
                driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(TIMEOUTDELAY));
                driver.manage().window().maximize();
                driver.manage().deleteAllCookies();
        }
    }
}