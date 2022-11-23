/*
 * Copyright 2021 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.europa.ec.leos.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PasswordConfigurator extends PropertyPlaceholderConfigurer {

    private static final String propFileName = "config.properties";
    private static final InputStream inputStream = PasswordConfigurator.class.getClassLoader().getResourceAsStream(propFileName);
    private static final Properties properties = new Properties();
    private static final Logger log = LoggerFactory.getLogger(PasswordConfigurator.class);

    public char[] getProperty(String propertyName) {
        String encryptedValue = getPropertyValue(propertyName);
        return encryptedValue != null ? encryptedValue.toCharArray() : null;
    }

    @Override
    protected String convertProperty(String propertyName, String originalValue) {
        return originalValue;
    }

    private String getPropertyValue(String propertyName) {
        if (properties.isEmpty()) {
            try {
                properties.load(inputStream);
            } catch (IOException e) {
                log.error("Please check with the app administrator. Error: {}",
                        e.getMessage());
            }
        }
        return properties.getProperty(propertyName);
    }
}