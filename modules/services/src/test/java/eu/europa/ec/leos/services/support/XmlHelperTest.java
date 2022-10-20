package eu.europa.ec.leos.services.support;

import eu.europa.ec.leos.services.support.XmlHelper;
import eu.europa.ec.leos.test.support.LeosTest;
import org.junit.Test;

import static eu.europa.ec.leos.services.util.TestUtils.squeezeXml;
import static org.junit.Assert.assertEquals;

public class XmlHelperTest extends LeosTest {

    @Test
    public void test_removeEnclosingTags() {
        String str = "<aknP id=\"cit_5__p\">Having Regions<authorialNote id=\"authorialnote_2\" marker=\"2\" placement=\"bottom\"><aknP id=\"authorialNote_2__p\">OJ C [...], [...], p. [...]</aknP></authorialNote>,</aknP>";
        String expected = "Having Regions<authorialNote id=\"authorialnote_2\" marker=\"2\" placement=\"bottom\"><aknP id=\"authorialNote_2__p\">OJ C [...], [...], p. [...]</aknP></authorialNote>,";

        str = XmlHelper.removeEnclosingTags(str);
        assertEquals(expected, str);
    }

    @Test
    public void test_removeEnclosingTags_multiLines() {
        String str = "<aknP\n id=\"cit_5__p\"> Having regard to\n\n the Regions\r\n\n   <authorialNote \nid=\"authorialnote_2\"\n marker=\"2\" placement=\"bottom\"\n> \n     <aknP \n id=\"authorialNote_2__p\">OJ C [...], [...], p. \n[...]\n    </aknP> \n\r </authorialNote>,  \n\n</aknP>";
        String expected = "Having regard to the Regions<authorialNote id=\"authorialnote_2\" marker=\"2\" placement=\"bottom\"><aknP id=\"authorialNote_2__p\">OJ C [...], [...], p. [...]</aknP></authorialNote>,";

        str = XmlHelper.removeEnclosingTags(str);
        assertEquals(squeezeXml(expected), squeezeXml(str));
    }

}
