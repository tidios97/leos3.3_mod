package eu.europa.ec.leos.services.numbering;

import eu.europa.ec.leos.services.numbering.config.NumberConfigFactory;
import eu.europa.ec.leos.services.numbering.depthBased.ParentChildConverter;
import eu.europa.ec.leos.services.numbering.processor.NumberProcessor;
import eu.europa.ec.leos.services.numbering.processor.NumberProcessorArticle;
import eu.europa.ec.leos.services.numbering.processor.NumberProcessorDefault;
import eu.europa.ec.leos.services.numbering.processor.NumberProcessorDepthBased;
import eu.europa.ec.leos.services.numbering.processor.NumberProcessorDepthBasedDefault;
import eu.europa.ec.leos.services.numbering.processor.NumberProcessorParagraphAndPoint;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessorMandate;
import eu.europa.ec.leos.services.util.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static eu.europa.ec.leos.services.util.TestUtils.squeezeXml;
import static eu.europa.ec.leos.services.util.TestUtils.squeezeXmlAndRemoveAllNS;
import static org.junit.Assert.assertEquals;

public class NumberServiceAnnexMandateTest extends NumberServiceTest {

    protected final static String FILE_PREFIX = "/numbering/annex/";

    @InjectMocks
    protected XmlContentProcessor xmlContentProcessor = new XmlContentProcessorMandate();

    protected ParentChildConverter parentChildConverter = new ParentChildConverter();
    @InjectMocks
    protected NumberConfigFactory numberConfigFactory = Mockito.spy(new NumberConfigFactory());
    @InjectMocks
    protected NumberProcessorHandler numberProcessorHandler = new NumberProcessorHandlerMandate();
    private NumberProcessor numberProcessorArticle = new NumberProcessorArticle(messageHelper, numberProcessorHandler);
    private NumberProcessor numberProcessorPoint = new NumberProcessorParagraphAndPoint(messageHelper, numberProcessorHandler);
    private NumberProcessor numberProcessorDefault = new NumberProcessorDefault(messageHelper, numberProcessorHandler);
    private NumberProcessorDepthBased numberProcessorDepthBasedDefault = new NumberProcessorDepthBasedDefault(messageHelper, numberProcessorHandler);
    private NumberProcessorDepthBased numberProcessorLevel = new eu.europa.ec.leos.services.numbering.processor.NumberProcessorLevel(messageHelper, numberProcessorHandler);
    @InjectMocks
    protected List<NumberProcessor> numberProcessors = Mockito.spy(Stream.of(numberProcessorArticle,
            numberProcessorPoint,
            numberProcessorDefault).collect(Collectors.toList()));
    @InjectMocks
    protected List<NumberProcessorDepthBased> numberProcessorsDepthBased = Mockito.spy(Stream.of(numberProcessorDepthBasedDefault, numberProcessorLevel)
            .collect(Collectors.toList()));

    protected NumberService numberService;

    @Before
    public void setup() {
        super.setup();
        numberService = new NumberServiceMandate(xmlContentProcessor, structureContextProvider, numberProcessorHandler, parentChildConverter);
        ReflectionTestUtils.setField(numberProcessorHandler, "numberConfigFactory", numberConfigFactory);
        ReflectionTestUtils.setField(numberProcessorHandler, "numberProcessorsDepthBased", numberProcessorsDepthBased);
        ReflectionTestUtils.setField(numberProcessorHandler, "numberProcessors", numberProcessors);
    }

    @Override
    protected void getStructureFile() {
        docTemplate = "SG-017";
        configFile = "/structure-test-annex-CN.xml";
    }

    @Test
    public void test_numbering_level_cn_withNegatives() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_level_cn_withNegatives.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_level_cn_withNegatives_expected.xml");
        byte[] result = numberService.renumberLevel(xmlInput);
        assertEquals(squeezeXmlAndRemoveAllNS(new String(xmlExpected)), squeezeXmlAndRemoveAllNS(new String(result)));
    }

    @Test
    public void test_numbering_level_cn_withPointsAndLevels() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_level_cn_withPointsAndLevels.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_level_cn_withPointsAndLevels_expected.xml");
        byte[] result = numberService.renumberLevel(xmlInput);
        assertEquals(squeezeXmlAndRemoveAllNS(new String(xmlExpected)), squeezeXmlAndRemoveAllNS(new String(result)));
    }

    @Test
    public void test_numbering_level_cn_withPoints() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_level_cn_withPoints.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_level_cn_withPoints_expected.xml");
        byte[] result = numberService.renumberLevel(xmlInput);
        assertEquals(squeezeXmlAndRemoveAllNS(new String(xmlExpected)), squeezeXmlAndRemoveAllNS(new String(result)));
    }

    @Test
    public void test_numbering_level_cn_withHigherElements() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_level_cn_withHigherElements.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_level_cn_withHigherElements_expected.xml");
        byte[] result = numberService.renumberLevel(xmlInput);
        assertEquals(squeezeXmlAndRemoveAllNS(new String(xmlExpected)), squeezeXmlAndRemoveAllNS(new String(result)));
    }

    @Test
    public void test_renumbering_level_cn_deeperElementAfterSoftDeleted() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_renumbering_level_cn_deeperElementAfterSoftDeleted.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_renumbering_level_cn_deeperElementAfterSoftDeleted_expected.xml");
        byte[] result = numberService.renumberLevel(xmlInput);
        assertEquals(squeezeXmlAndRemoveAllNS(new String(xmlExpected)), squeezeXmlAndRemoveAllNS(new String(result)));
    }

    @Test
    public void test_numbering_levels_with_deep_numbering_in_annex_cn() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_levels_with_deep_numbering_in_annex_cn.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_levels_with_deep_numbering_in_annex_cn_expected.xml");
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

}
