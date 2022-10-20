package eu.europa.ec.leos.services.processor.node;

import junit.framework.TestCase;
import org.junit.Test;

public class XmlNodeConfigTest extends TestCase {

    private final String REGULAR_EXPRESSION = "(.+?)([a-zA-Z]+?)\\[@(.+?)='(.+?)'\\](.*?)";
    private final String X_PATH_1 = "//akn:coverPage/akn:container[@name='language']/akn:p";
    private final String X_PATH_2 = "//akn:coverPage/akn:container[@name='language']";

    @Test
    public void testDifferentXPathValues() {

        assertTrue(X_PATH_1.matches(REGULAR_EXPRESSION));
        assertTrue(X_PATH_2.matches(REGULAR_EXPRESSION));

    }

}