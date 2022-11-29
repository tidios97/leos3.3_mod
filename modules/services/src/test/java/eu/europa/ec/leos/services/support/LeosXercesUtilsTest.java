package eu.europa.ec.leos.services.support;

import eu.europa.ec.leos.services.util.TestUtils;
import eu.europa.ec.leos.test.support.LeosTest;
import org.junit.Test;
import org.w3c.dom.Document;

import static eu.europa.ec.leos.services.util.TestUtils.squeezeXml;
import static org.junit.Assert.assertEquals;

public class LeosXercesUtilsTest extends LeosTest {

    protected final static String FILE_PREFIX = "/leosXercesUtil";

    @Test
    public void test_pageOrientationWrapper() {
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/test_pageOrientationWrapper.xml");
        byte[] fileContentExpected = TestUtils.getFileContent(FILE_PREFIX + "/test_pageOrientationWrapper_expected.xml");

        Document document = XercesUtils.createXercesDocument(fileContent);
        byte[] nodeActual = LeosXercesUtils.wrapWithPageOrientationDivs(document);

        String expected = new String(fileContentExpected, UTF_8);
        String nodeActualAsString = new String(nodeActual, UTF_8);
        expected = squeezeXml(expected);
        nodeActualAsString = squeezeXml(nodeActualAsString);

        assertEquals(expected, nodeActualAsString);
    }

}
