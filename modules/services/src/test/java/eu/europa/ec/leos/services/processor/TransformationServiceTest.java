package eu.europa.ec.leos.services.processor;

import eu.europa.ec.leos.services.document.TransformationServiceImpl;
import eu.europa.ec.leos.services.util.TestUtils;
import eu.europa.ec.leos.test.support.LeosTest;
import freemarker.template.Configuration;
import freemarker.template.TemplateHashModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class TransformationServiceTest extends LeosTest {

    @InjectMocks
    private Configuration freemarkerConfiguration = new Configuration();
    @Mock
    private TemplateHashModel enumModels;
    @InjectMocks
    private TransformationServiceImpl transformationService = new TransformationServiceImpl(freemarkerConfiguration, enumModels);

    private final String FILE_PREFIX = "/transformation/";

    @Before
    public void setUp() {
        super.setup();
        String path = "/src/main/resources/eu/europa/ec/leos/freemarker/templates/";
        String templateName = "legalText/akn_fragment_xml_wrapper.ftl";
        ReflectionTestUtils.setField(transformationService, "editableXHtmlTemplate", path + templateName);
    }

    @Test
    public void test_transformation_citation_elementWithoutNamespace() {
        byte[] documentXml = TestUtils.getFileContent(FILE_PREFIX + "test_transformation_citation_elementWithoutNamespace.xml");
        InputStream contentStream = new ByteArrayInputStream(documentXml);
        transformationService.formatToHtml(contentStream, "", null);
    }

    /**
     *  For now this error is being avoided calling removeAllNameSpaces(byte[]).
     *  @See XmlContentProcessor.getElementByNameAndId(byte[], String, String)
     */
    @Test(expected = RuntimeException.class)
    public void test_transformation_citation_elementWithNamespace() {
        byte[] documentXml = TestUtils.getFileContent(FILE_PREFIX + "test_transformation_citation_elementWithNamespace.xml");
        InputStream contentStream = new ByteArrayInputStream(documentXml);
        transformationService.formatToHtml(contentStream, "", null);
    }
}
