package eu.europa.ec.leos.services.numbering;

import eu.europa.ec.leos.services.processor.content.XmlContentProcessorMandate;
import eu.europa.ec.leos.services.util.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static eu.europa.ec.leos.services.util.TestUtils.squeezeXmlAndRemoveAllNS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NumberServiceExplanatoryCouncilTest extends NumberServiceMandateTest {

    private static final Logger LOG = LoggerFactory.getLogger(NumberServiceExplanatoryCouncilTest.class);
    protected final static String FILE_PREFIX = "/numbering/explanatory/";

    @InjectMocks
    private XmlContentProcessorMandate xmlContentProcessor = new XmlContentProcessorMandate();

    private NumberServiceMandate numberService;

    @Before
    public void setup() {
        super.setup();
        numberService = new NumberServiceMandate(xmlContentProcessor, structureContextProvider, numberProcessorHandler, parentChildConverter);
    }

    @Override
    protected void getStructureFile() {
        docTemplate = "SG-017";
        configFile = "/structure-test-explanatory-CN.xml";
    }

    @Test
    public void test_level_crossheading_added_in_list() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_level_crossheading_added_in_list.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_level_crossheading_added_in_list_expected.xml");
        byte[] result = numberService.renumberLevel(xmlInput);
        assertEquals(squeezeXmlAndRemoveAllNS(new String(xmlExpected)), squeezeXmlAndRemoveAllNS(new String(result)));
    }

    @Test
    public void test_renumbering_divisions() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_division.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_division_expected.xml");
        byte[] result = numberService.renumberDivisions(xmlInput);
        assertEquals(squeezeXmlAndRemoveAllNS(new String(xmlExpected)), squeezeXmlAndRemoveAllNS(new String(result)));
    }

    @Test
    public void test_renumbering_divisions_withCustomNumber() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_division_custom.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_division_custom_expected.xml");
        byte[] result = numberService.renumberDivisions(xmlInput);
        assertEquals(squeezeXmlAndRemoveAllNS(new String(xmlExpected)), squeezeXmlAndRemoveAllNS(new String(result)));
    }

    @Test
    public void test_division_withChapterLevelAndParagraphs() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_division_withChapterLevelAndParagraphs.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_division_withChapterLevelAndParagraphs_expected.xml");
        byte[] result = numberService.renumberDivisions(xmlInput);
        result = numberService.renumberLevel(result);
        assertEquals(squeezeXmlAndRemoveAllNS(new String(xmlExpected)), squeezeXmlAndRemoveAllNS(new String(result)));
    }

    @Test
    public void test_renumbering_divisions_stressTest() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_division_stressTest.xml");
        long start = System.currentTimeMillis();
        byte[] result = numberService.renumberDivisions(xmlInput);
        long end = System.currentTimeMillis();
        LOG.debug("Numbering done in {} ms ({} secs) ", end-start, (end-start)/1000);
        assertTrue(end-start < 15_000);
    }

    @Test
    public void test_level_indents_added_in_list() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_level_indents_added_in_list.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_level_indents_added_in_list_expected.xml");
        byte[] result = numberService.renumberLevel(xmlInput);
        assertEquals(squeezeXmlAndRemoveAllNS(new String(xmlExpected)), squeezeXmlAndRemoveAllNS(new String(result)));
    }
    
    @Test
    public void test_division_when_num_overwritten() {
    	final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_division_when_num_overwritten.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_division_when_num_overwritten_expected.xml");
        byte[] result = numberService.renumberDivisions(xmlInput);
        assertEquals(squeezeXmlAndRemoveAllNS(new String(xmlExpected)), squeezeXmlAndRemoveAllNS(new String(result)));
    }
}
