package eu.europa.ec.leos.services.numbering;

import eu.europa.ec.leos.services.util.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static eu.europa.ec.leos.services.util.TestUtils.squeezeXmlAndRemoveAllNS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NumberServiceAnnexProposalTest extends NumberServiceProposalTest {

    private static final Logger LOG = LoggerFactory.getLogger(NumberServiceAnnexProposalTest.class);
    protected final static String FILE_PREFIX = "/numbering/annex/";

    private NumberServiceProposal numberService;

    @Before
    public void setup() {
        super.setup();
        numberService = new NumberServiceProposal(structureContextProvider, numberProcessorHandler, parentChildConverter, contentProcessor);
    }

    @Override
    protected void getStructureFile() {
        docTemplate = "SG-017";
        configFile = "/structure-test-annex-EC.xml";
    }

    @Test
    public void test_renumbering_new_level_added_as_sibling() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_renumbering_new_level_added_as_sibling.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_renumbering_new_level_added_as_sibling_expected.xml");
        byte[] result = numberService.renumberLevel(xmlInput);
        assertEquals(squeezeXmlAndRemoveAllNS(new String(xmlExpected)), squeezeXmlAndRemoveAllNS(new String(result)));
    }

    @Test
    public void test_renumbering_new_level_added_as_child() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_renumbering_new_level_added_as_child.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_renumbering_new_level_added_as_child_expected.xml");
        byte[] result = numberService.renumberLevel(xmlInput);
        assertEquals(squeezeXmlAndRemoveAllNS(new String(xmlExpected)), squeezeXmlAndRemoveAllNS(new String(result)));
    }

    @Test
    public void test_renumbering_new_level_added_at_multiple_sublevel() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_renumbering_new_level_added_at_multiple_sublevel.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_renumbering_new_level_added_at_multiple_sublevel_expected.xml");
        byte[] result = numberService.renumberLevel(xmlInput);
        assertEquals(squeezeXmlAndRemoveAllNS(new String(xmlExpected)), squeezeXmlAndRemoveAllNS(new String(result)));
    }

    @Test
    public void test_renumbering_level_with_soft_attributes() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_renumbering_level_with_soft_attr.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_renumbering_level_with_soft_attr_expected.xml");
        byte[] result = numberService.renumberLevel(xmlInput);
        assertEquals(squeezeXmlAndRemoveAllNS(new String(xmlExpected)), squeezeXmlAndRemoveAllNS(new String(result)));
    }

    @Test
    public void test_numbering_level_ec_withHigherElements() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_level_cn_withHigherElements.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_level_ec_withHigherElements_expected.xml");
        byte[] result = numberService.renumberLevel(xmlInput);
        assertEquals(squeezeXmlAndRemoveAllNS(new String(xmlExpected)), squeezeXmlAndRemoveAllNS(new String(result)));
    }

    @Test
    public void test_numbering_level_wrongDepthStructure() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_level_wrongDepthStructure.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_level_wrongDepthStructure_expected.xml");
        byte[] result = numberService.renumberLevel(xmlInput);
        assertEquals(squeezeXmlAndRemoveAllNS(new String(xmlExpected)), squeezeXmlAndRemoveAllNS(new String(result)));
    }

    @Test
    public void test_numbering_level_ec_stressTest() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_level_ec_stressTest.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_level_ec_stressTest_expected.xml");
        long start = System.currentTimeMillis();
        byte[] result = numberService.renumberLevel(xmlInput);
        long end = System.currentTimeMillis();
        LOG.debug("Numbering done in {} ms ({} secs) ", end - start, (end - start) / 1000);
        assertEquals(squeezeXmlAndRemoveAllNS(new String(xmlExpected)), squeezeXmlAndRemoveAllNS(new String(result)));
        assertTrue(end - start < 25_000);
    }
}
