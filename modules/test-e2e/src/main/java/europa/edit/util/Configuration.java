package europa.edit.util;

import lombok.extern.slf4j.Slf4j;
import java.io.*;
import java.util.Properties;

/* 	Author: Satyabrata Das
 * 	Functionality: Configuration class to access the configuration properties file values
 */
@Slf4j
public class Configuration {

    private final Properties properties;
    private static final String MAIN_CONFIG_FILE_PATH = Constants.CONFIG + Constants.SLASH + Constants.CONFIG_PROERTIES;
    private static final String USERS_FILE_PATH = Constants.CONFIG + Constants.SLASH + Constants.USERS_PROERTIES;
    private static final String ENVIRONMENT_CONFIG_FILE_PATH = Constants.CONFIG + Constants.SLASH + Constants.ENV + Constants.SLASH + TestParameters.getInstance().getEnvironment() + ".properties";

    public Configuration() {
        try {
            FileInputStream configBaseFile = new FileInputStream("./src/test/resources/" + MAIN_CONFIG_FILE_PATH);
            FileInputStream configUserFile = new FileInputStream("./src/test/resources/" + USERS_FILE_PATH);
            FileInputStream environmentConfigFile = new FileInputStream("./src/test/resources/" + ENVIRONMENT_CONFIG_FILE_PATH);
            properties = new Properties();
            properties.load(configBaseFile);
            properties.load(configUserFile);
            properties.load(environmentConfigFile);
        } catch (Exception e) {
            throw new DecisionTestExceptions("The config file format is not as expected", e);
        }
    }

    public String getProperty(String value) {
        String propertyValue = getProperty(value, null);
        if (propertyValue != null) {
            return propertyValue;
        } else {
            throw new DecisionTestExceptions(value + " not specified in the config.properties file.");
        }
    }

    public String getProperty(String value, String defaultValue) {
        return properties.getProperty(value, defaultValue);
    }
}