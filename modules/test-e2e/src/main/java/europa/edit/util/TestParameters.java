package europa.edit.util;

import io.cucumber.java.Scenario;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class TestParameters {

    private static TestParameters instance;
    private String screenshotPath;
    private String environment;
    private String browser;
    private String mode;
//    private XSSFWorkbook testDataFile;
    private Scenario scenario;
    
    public static TestParameters getInstance(){
    	if(instance == null) {
    		instance = new TestParameters();
    	}
    	return instance;
    }
    
    public String getScreenshotPath() {
		return screenshotPath;
	}

	public void setScreenshotPath(String screenshotPath) {
		this.screenshotPath = screenshotPath;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public String getBrowser() {
		return browser;
	}

	public void setBrowser(String browser) {
		this.browser = browser;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

/*
	public XSSFWorkbook getTestDataFile() {
		return testDataFile;
	}

	public void setTestDataFile(XSSFWorkbook testDataFile) {
		this.testDataFile = testDataFile;
	}
*/

	public Scenario getScenario() {
		return scenario;
	}

	public void setScenario(Scenario scenario) {
		this.scenario = scenario;
	}

	public void reset() {
        screenshotPath = null;
        scenario = null;
    }
}