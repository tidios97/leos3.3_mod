package europa.edit.util;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.Reporter;

public class Common {

    private static final Logger logger = LoggerFactory.getLogger(Common.class);
    private final static int TIME_OUT = 120;
    private final static int POLLING_TIME = 1;
    private final static String IS_NOT_DISPLAYED = "The following element is NOT displayed: ";
    private final static String ELEMENT_NOTPRESENT = "The following element is NOT present: ";
    private final static String IS_NOT_DISPLAYED_VERIFIED = "The following element is NOT displayed and NOT verified: ";
    private final static String IS_DISPLAYED_AND_CLICKED = "The following element is displayed and clicked: ";
    private final static String DOUBLE_CLICKED = "The following element is double clicked: ";
    private final static String EXCEPTION_MESSGAE = "The exception occured in finding the following element ";
    private final static String EXCEPTION_MESSGAE_ON_FAILURE = "The exception occured during test execution";
    private final static String SCROLL_ELEMENT = "arguments[0].scrollIntoView(true);";
    private final static String CLICK_ELEMENT = "arguments[0].click();";
    private final static String selectJsScript = "function selectText(element, start, end) {\n" +
            "    selection = window.getSelection();        \n" +
            "    range = new Range();\n" +
            "    range.setStart(element.firstChild, start);\n" +
            "    range.setEnd(element.firstChild, end);\n" +
            "    selection.removeAllRanges();\n" +
            "    selection.addRange(range);\n" +
            "}\n" +
            "\n" +
            "selectText(arguments[0], arguments[1], arguments[2]);";

    // Scroll to the element till the element is visible
    public static void scrollTo(WebDriver driver, By by) {
        try {
            waitForElement(driver, by);
            if (elementExists(driver, by)) {
                WebElement element = driver.findElement(by);
                ((JavascriptExecutor) driver).executeScript(SCROLL_ELEMENT, element);
                logger.info("Page is scrolled to the element {}", element);
            }
        } catch (Exception e) {
            exceptionReport(driver, by, e);
        }
    }

    public static void scrollToElement(WebDriver driver, WebElement ele) {
        try {
            ((JavascriptExecutor) driver).executeScript(SCROLL_ELEMENT, ele);
            logger.info("Page is scrolled to the element {}", ele);
        } catch (Exception e) {
            exceptionReport(driver, e);
        }
    }

/*    public void scrollToSpecificOffset(WebDriver driver, int x, int y) {
        try {
            ((JavascriptExecutor) driver).executeScript("window.scrollBy(arguments[0], arguments[1]);", x, y);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }*/

    public static void waitForPageLoadComplete(WebDriver driver, int specifiedTimeout) {
        try {
            //driver.manage().timeouts().implicitlyWait(TIME_OUT, TimeUnit.SECONDS);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(specifiedTimeout));
            //Wait for Javascript to load
            ExpectedCondition<Boolean> jsLoad = driver1 -> "complete".equals(((JavascriptExecutor) driver)
                    .executeScript("return document.readyState").toString());
            wait.until(jsLoad);
        } catch (Exception e) {
            exceptionReport(driver, e);
        }
    }

    // Function to wait for element to appear
    public static void waitForElement(WebDriver driver, By element) {
        try {
            waitForPageLoadComplete(driver, TIME_OUT);
            logger.info("Waiting for {} to display", element);
            E2eUtil.wait(1000);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIME_OUT));
            wait.until(ExpectedConditions.presenceOfElementLocated(element));
        } catch (StaleElementReferenceException staleExcption) {
            E2eUtil.wait(2000);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIME_OUT));
            wait.until(ExpectedConditions.presenceOfElementLocated(element));
            logger.info("There was a stale element exception, but waited");
        } catch (NoSuchElementException | TimeoutException e) {
            exceptionReport(driver, element, e);
        }
    }

    // Function to wait for element to appear
    public static void waitForElementClickable(WebDriver driver, By element) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIME_OUT));
            wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(element)));
        } catch (StaleElementReferenceException staleException) {
            E2eUtil.wait(2000);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIME_OUT));
            wait.until(ExpectedConditions.presenceOfElementLocated(element));
            logger.info("There was a stale element exception, but waited");
        } catch (Exception otherexceptions) {
            exceptionReport(driver, element, otherexceptions);
        }
    }

    public static void elementClick(WebDriver driver, By by) {
        try {
            waitForElementClickable(driver, by);
            if (elementExists(driver, by)) {
                driver.findElement(by).click();
                logger.info(IS_DISPLAYED_AND_CLICKED + by);
            } else {
                throw new NoSuchElementException(IS_NOT_DISPLAYED + by);
            }

        } catch (NoSuchElementException e) {
            exceptionReport(driver, by, e);
        } catch (StaleElementReferenceException staleExcption) {
            E2eUtil.wait(2000);
            WebElement webelement = driver.findElement(by);
            webelement.click();
            logger.info("There was a stale element exception, but clicked");
        } catch (ElementClickInterceptedException elementClickInterceptedException) {
            E2eUtil.scrollandClick(driver, by);
        }
    }

    public static void elementClick(WebElement ele) {
        try {
            ele.click();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static void elementClickJS(WebDriver driver, WebElement ele) {
        try {
            ((JavascriptExecutor) driver).executeScript(CLICK_ELEMENT, ele);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static void elementClickJS(WebDriver driver, By by) {
        try {
            WebElement ele = Common.waitForElementTobePresent(driver, by);
            ((JavascriptExecutor) driver).executeScript(CLICK_ELEMENT, ele);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    // Function to verify the element is loaded before entering data
    public static void elementEcasSendkeys(WebDriver driver, By by, String data) {
        try {
            if (elementExists(driver, by)) {
                WebElement element = driver.findElement(by);
                element.clear();
                element.sendKeys(data);
//                logger.info("The data entered at {}", element);
//                E2eUtil.takeSnapShot(driver, "PASS");
            } else {
                throw new NoSuchElementException(IS_NOT_DISPLAYED + by);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static void elementEcasSendkeys(WebElement element, String data) {
        try {
            element.clear();
            element.sendKeys(data);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static void elementActionSendkeys(WebDriver driver, String data) {
        try {
            Actions action = new Actions(driver);
            action.sendKeys(data).perform();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Function to keep assertions on particular element.
    public static boolean verifyElement(WebDriver driver, By by) {
        waitForElement(driver, by);
        return verifyUIElement(driver, by);
    }

    public boolean verifyElement(WebElement element) {
        return element.isDisplayed();
    }

    public static boolean verifyUIElement(WebDriver driver, By element) {
        try {
            scrollTo(driver, element);
            if (elementDisplays(driver, element)) {
                E2eUtil.highlightElement(driver, driver.findElement(element));
                return true;
            } else {
                Reporter.getCurrentTestResult().setStatus(ITestResult.FAILURE);
                logger.info(IS_NOT_DISPLAYED_VERIFIED + element);
                E2eUtil.takeSnapShot(driver, "FAIL");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    // Element exists or not
    public static boolean elementExists(WebDriver driver, By element) {
        return elementExistsWithOutwait(driver, element);
    }

    public void click(WebDriver driver, By by) {
        try {
            driver.findElement(by).click();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    // Element exists or not
    public static boolean elementExistsWithOutwait(WebDriver driver, By element) {
        // Tries 3 times in case of StaleElementReferenceException
        for (int i = 0; true; i++) {
            try {
                return driver.findElement(element).isEnabled();
            } catch (StaleElementReferenceException e) {
                if (i > 2) {
                    throw e;
                }
            } catch (NoSuchElementException e) {
                return false;
            }
        }
    }

    // Element displayed or not
    public static Boolean elementDisplays(WebDriver driver, By element) {
        try {
            return driver.findElement(element).isDisplayed();
        } catch (Exception e) {
            return false;
        }

    }

    public static String getElementText(WebDriver driver, By by) {
        try {
            return driver.findElement(by).getText();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    //Get Text of the element
    public static String getElementText(WebElement element) {
        try {
            return element.getText();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    //Get Element Attribute Value
    public static String getElementAttributeValue(WebDriver driver, By by) {
        try {
            return driver.findElement(by).getAttribute("value");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    //Get Element Attribute InnerText
    public static String getElementAttributeInnerText(WebDriver driver, By by) {
        try {
            return driver.findElement(by).getAttribute("innerText");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static String getElementAttributeInnerText(WebElement ele) {
        try {
            return ele.getAttribute("innerText");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static String getElementAttributeTextContent(WebElement ele) {
        try {
            return ele.getAttribute("textContent");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static String getElementAttributeTextContent(WebDriver driver,By by) {
        try {
            return driver.findElement(by).getAttribute("textContent");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

/*    public static void setTextContentToElementAttribute(WebDriver driver, By by, String val) {
        try {
            WebElement ele = driver.findElement(by);
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].textContent=arguments[1];", ele, val);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    // Take screenshot if scenario fails and stop execution
    private static void exceptionReport(WebDriver driver, By element, Exception e) {
        E2eUtil.takeSnapShot(driver, "FAIL");
        Reporter.getCurrentTestResult().setStatus(ITestResult.FAILURE);
        logger.info(EXCEPTION_MESSGAE + element);
        logger.error(e.getMessage(), e);
        throw new AssertionError(EXCEPTION_MESSGAE, e);
    }

    // Take screenshot if scenario fails and stop execution
    private static void exceptionReport(WebDriver driver, Exception e) {
        E2eUtil.takeSnapShot(driver, "FAIL");
        logger.info(EXCEPTION_MESSGAE_ON_FAILURE);
        logger.error(e.getMessage(), e);
        throw new AssertionError(EXCEPTION_MESSGAE_ON_FAILURE, e);
    }

    public static boolean verifyIsElementSelected(WebDriver driver, By by) {
        return driver.findElement(by).isSelected();
    }

    public static boolean verifyElementIsEnabled(WebDriver driver, By by) {
        return driver.findElement(by).isEnabled();
    }

/*    public static boolean verifyElementIsEnabled(WebElement element) {
        return element.isEnabled();
    }*/

    public static void isElementDisabled(WebDriver driver, By element) {
        for (int i = 0; true; i++) {
            try {
                driver.findElement(element).isEnabled();
                return;
            } catch (StaleElementReferenceException e) {
                if (i > 2) {
                    throw e;
                }
            } catch (NoSuchElementException e) {
                return;
            }
        }
    }

    public static void verifyStringContainsText(WebDriver driver, By element) {
        for (int i = 0; true; i++) {
            try {
                driver.findElement(element).getText();
                return;
            } catch (StaleElementReferenceException e) {
                if (i > 2) {
                    throw e;
                }
            } catch (NoSuchElementException e) {
                return;
            }
        }
    }

    public static void doubleClick(WebDriver driver, By by) {
        try {
            waitForElementClickable(driver, by);
            if (elementExists(driver, by)) {
                Actions actions = new Actions(driver);
                WebElement elementLocator = driver.findElement(by);
                actions.doubleClick(elementLocator).build().perform();
                logger.info(DOUBLE_CLICKED + by);
            } else {
                E2eUtil.takeSnapShot(driver, "FAIL");
                throw new NoSuchElementException(ELEMENT_NOTPRESENT + by);
            }

        } catch (NoSuchElementException e) {
            exceptionReport(driver, by, e);
        } catch (StaleElementReferenceException staleExcption) {
            E2eUtil.wait(2000);
            Actions actions = new Actions(driver);
            WebElement elementLocator = driver.findElement(by);
            actions.moveToElement(elementLocator).doubleClick().build().perform();
            logger.info("There was a stale element exception, but clicked");
        } catch (ElementClickInterceptedException elementClickInterceptedException) {
            E2eUtil.scrollandDoubleClick(driver, by);
        }
    }

    public static void elementActionClick(WebDriver driver, By locator) {
        WebElement element;
        Actions act;
        Wait<WebDriver> wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(TIME_OUT))
                .pollingEvery(Duration.ofSeconds(POLLING_TIME))
                .ignoring(NoSuchElementException.class);
        try {
            element = wait.until(driver1 -> driver1.findElement(locator));
        } catch (NoSuchElementException | TimeoutException e) {
            e.printStackTrace();
            throw e;
        }
        if (null != element) {
            try {
                act = new Actions(driver);
                act.moveToElement(element).click().release().build().perform();
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    public static boolean selectTextThroughDoubleClick(WebDriver driver, By contextOfTheProposalText) {
        try {
            WebElement element = driver.findElement(contextOfTheProposalText);
            /*Integer width = element.getSize().getWidth();
            Actions act = new Actions(driver);
            act.moveByOffset(element.getLocation().getX() + width,
                    element.getLocation().getY() + width).click();
            act.build().perform();
            act.moveByOffset(width/2,0).clickAndHold().moveByOffset(width,0).release().build().perform();*/
            Actions act = new Actions(driver);
            act.doubleClick(element).build().perform();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static void selectText(WebDriver driver, By locator) {
        try {
            WebElement element = driver.findElement(locator);
            Dimension size = element.getSize();
            int width = size.getWidth();
            Actions action = new Actions(driver);
            action.clickAndHold(element)
                    .moveToElement(element, -width / 2, 0)
                    .build().perform();
            action.release().build().perform();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static void selectTextFromElement(WebDriver driver, By locator, String partialText) {
        try {
            WebElement textContainer = Common.waitForElementTobePresent(driver, locator);
            String fullText = textContainer.getText();
            int start = fullText.indexOf(partialText);
            int end = fullText.lastIndexOf(partialText) + partialText.length();
            ((JavascriptExecutor) driver).executeScript(selectJsScript, textContainer, start, end);
            Actions actions = new Actions(driver);
            actions.release().build().perform();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

/*    public static void selectAllText(WebDriver driver, By locator) {
        try {
            WebElement element = driver.findElement(locator);
            element.sendKeys(Keys.CONTROL + "a");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }*/

    public static WebElement waitForElementTobePresent(WebDriver driver, By by) {
        Wait<WebDriver> wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(TIME_OUT))
                .pollingEvery(Duration.ofSeconds(POLLING_TIME))
                .ignoring(Exception.class);
        return wait.until(ExpectedConditions.presenceOfElementLocated(by));
    }

    public static boolean checkElementPresence(WebDriver driver, By by) {
        Wait<WebDriver> wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(TIME_OUT))
                .pollingEvery(Duration.ofSeconds(POLLING_TIME))
                .ignoring(Exception.class);
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(by));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean waitForElementTobeDisPlayed(WebDriver driver, By by) {
        Wait<WebDriver> wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(TIME_OUT))
                .pollingEvery(Duration.ofSeconds(POLLING_TIME))
                .ignoring(Exception.class);
        //return wait.until(driver1 -> driver1.findElement(by).isDisplayed());
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by)).isDisplayed();
    }

    public static boolean waitUnTillElementIsNotPresent(WebDriver driver, By by) {
        try {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(POLLING_TIME));
            Wait<WebDriver> wait = new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(TIME_OUT))
                    .pollingEvery(Duration.ofSeconds(POLLING_TIME));
            Boolean found = false;
            long totalTime = 0;
            long startTime;
            long endTime;
            while (!found && (totalTime / 1000) < TIME_OUT) {
                startTime = System.currentTimeMillis();
                found = wait.until(ExpectedConditions.invisibilityOfElementLocated(by));
                endTime = System.currentTimeMillis();
                totalTime = totalTime + (endTime - startTime);
            }
            return found;
        } catch (Exception e) {
            return false;
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(TIME_OUT));
        }
    }

    public static boolean waitElementToBeDisplayedWithInSpecifiedTime(WebDriver driver, By by, int timeOut) {
        boolean bool = false;
        try {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(timeOut));
            WebElement ele = driver.findElement(by);
            if (null != ele) {
                bool = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(TIME_OUT));
        }
        return bool;
    }

    public static List<WebElement> getElementListForComparision(WebDriver driver, By by, int timeOut) {
        List<WebElement> elementList = new ArrayList<>();
        try {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(timeOut));
            elementList = driver.findElements(by);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(TIME_OUT));
        }
        return elementList;
    }

    public static void setValueToElementAttribute(WebDriver driver, By by, String val) {
        try {
            WebElement ele = driver.findElement(by);
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].value=arguments[1];", ele, val);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static void setInnerTextToElementAttribute(WebDriver driver, By by, String val) {
        try {
            WebElement ele = driver.findElement(by);
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].innerText=arguments[1];", ele, val);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static boolean removeElementThroughJS(WebDriver driver, WebElement ele) {
        boolean bool = false;
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].remove();", ele);
            bool = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bool;
    }

    public static void waitForOneSecond() {
        E2eUtil.wait(1000);
    }

    public static void waitForTwoSecond() {
        E2eUtil.wait(2000);
    }
/*    public String getElementAttributeInnerHTML(WebDriver driver, By by) {
        try {
            return driver.findElement(by).getAttribute("innerHTML");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }*/

    public static String getAttributeValueFromElement(WebDriver driver, By by, String attribute) {
        try {
            return driver.findElement(by).getAttribute(attribute);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static String getAttributeValueFromElement(WebElement element, String attribute) {
        try {
            return element.getAttribute(attribute);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static String getNodeTextFromElement(WebDriver driver, WebElement element) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            return (String)js.executeScript("return arguments[0].childNodes[0].textContent;", element);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}