package europa.edit.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ISuite;
import org.testng.ISuiteListener;

public class SuiteListener implements ISuiteListener {
	
	private static final Logger logger = LoggerFactory.getLogger(SuiteListener.class);

    @Override
    public void onStart(ISuite suite) {
        logger.debug("onStart");
        String browser = suite.getParameter("browser");
        String environment = suite.getParameter("environment");
        String mode = suite.getParameter("mode");
        TestParameters.getInstance().setEnvironment(environment); //Set environment
        TestParameters.getInstance().setBrowser(browser); //Set browser
        TestParameters.getInstance().setMode(mode); //Set mode
    }

    @Override
    public void onFinish(ISuite suite) {
        logger.debug("onFinish");
    }
}