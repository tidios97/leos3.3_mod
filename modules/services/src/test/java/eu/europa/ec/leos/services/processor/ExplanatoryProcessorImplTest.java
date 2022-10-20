package eu.europa.ec.leos.services.processor;

import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.document.Explanatory;
import eu.europa.ec.leos.domain.cmis.metadata.ExplanatoryMetadata;
import eu.europa.ec.leos.i18n.LanguageHelper;
import eu.europa.ec.leos.i18n.MandateMessageHelper;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.user.Collaborator;
import eu.europa.ec.leos.services.clone.CloneContext;
import eu.europa.ec.leos.services.compare.ContentComparatorService;
import eu.europa.ec.leos.services.document.DocumentContentService;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.support.XPathCatalog;
import eu.europa.ec.leos.services.template.TemplateStructureService;
import eu.europa.ec.leos.services.numbering.NumberProcessorHandler;
import eu.europa.ec.leos.services.numbering.NumberProcessorHandlerProposal;
import eu.europa.ec.leos.services.numbering.NumberService;
import eu.europa.ec.leos.services.numbering.NumberServiceMandate;
import eu.europa.ec.leos.services.numbering.config.NumberConfigFactory;
import eu.europa.ec.leos.services.numbering.depthBased.ParentChildConverter;
import eu.europa.ec.leos.services.numbering.processor.NumberProcessor;
import eu.europa.ec.leos.services.numbering.processor.NumberProcessorArticle;
import eu.europa.ec.leos.services.numbering.processor.NumberProcessorDepthBased;
import eu.europa.ec.leos.services.numbering.processor.NumberProcessorDepthBasedDefault;
import eu.europa.ec.leos.services.numbering.processor.NumberProcessorLevel;
import eu.europa.ec.leos.services.numbering.processor.NumberProcessorParagraphAndPoint;
import eu.europa.ec.leos.services.numbering.processor.NumberProcessorDefault;
import eu.europa.ec.leos.services.processor.content.TableOfContentProcessor;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessorMandate;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.services.toc.StructureServiceImpl;
import eu.europa.ec.leos.services.util.TestUtils;
import eu.europa.ec.leos.test.support.LeosTest;
import eu.europa.ec.leos.vo.toc.NumberingConfig;
import eu.europa.ec.leos.vo.toc.TocItem;
import io.atlassian.fugue.Option;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import javax.inject.Provider;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static eu.europa.ec.leos.services.util.TestUtils.squeezeXmlAndRemoveAllNS;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExplanatoryProcessorImplTest extends LeosTest {

    @Mock
    private CloneContext cloneContext;
    @Mock
    private Provider<StructureContext> structureContextProvider;
    @Mock
    private TableOfContentProcessor tableOfContentProcessor;
    @Mock
    private TemplateStructureService templateStructureService;
    @Mock
    protected LanguageHelper languageHelper;
    @InjectMocks
    protected MessageHelper messageHelper = Mockito.spy(getMessageHelper());

    protected MessageHelper getMessageHelper() {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("test-servicesContext.xml");
        MessageSource servicesMessageSource = (MessageSource) applicationContext.getBean("servicesMessageSource");
        MessageHelper messageHelper = new MandateMessageHelper(servicesMessageSource);
        return messageHelper;
    }
    @Mock
    private StructureContext structureContext;

    @Mock
    private XPathCatalog xPathCatalog;

    @Mock
    private DocumentContentService documentContentService;

    @Mock
    private ContentComparatorService compareService;

    @InjectMocks
    private StructureServiceImpl structureServiceImpl = Mockito.spy(new StructureServiceImpl());

    @InjectMocks
    private XmlContentProcessor xmlContentProcessor = Mockito.spy(new XmlContentProcessorMandate());

    @InjectMocks
    private ElementProcessor<Explanatory> elementProcessor = new ElementProcessorImpl<>(xmlContentProcessor,
            structureContextProvider, cloneContext, xPathCatalog, documentContentService, compareService);

    protected ParentChildConverter parentChildConverter = new ParentChildConverter();
    @InjectMocks
    protected NumberConfigFactory numberConfigFactory = Mockito.spy(new NumberConfigFactory());
    @InjectMocks
    protected NumberProcessorHandler numberProcessorHandler = new NumberProcessorHandlerProposal();
    private NumberProcessor numberProcessorArticle = new NumberProcessorArticle(messageHelper, numberProcessorHandler);
    private NumberProcessor numberProcessorPoint = new NumberProcessorParagraphAndPoint(messageHelper, numberProcessorHandler);
    private NumberProcessor numberProcessorDefault = new NumberProcessorDefault(messageHelper, numberProcessorHandler);
    private NumberProcessorDepthBased numberProcessorDepthBasedDefault = new NumberProcessorDepthBasedDefault(messageHelper, numberProcessorHandler);
    private NumberProcessorDepthBased numberProcessorLevel = new NumberProcessorLevel(messageHelper, numberProcessorHandler);
    @InjectMocks
    protected List<NumberProcessor> numberProcessors = Mockito.spy(Stream.of(numberProcessorArticle,
            numberProcessorPoint,
            numberProcessorDefault).collect(Collectors.toList()));
    @InjectMocks
    protected List<NumberProcessorDepthBased> numberProcessorsDepthBased = Mockito.spy(Stream.of(numberProcessorDepthBasedDefault, numberProcessorLevel).collect(Collectors.toList()));
    protected NumberService numberService;
    protected ExplanatoryProcessorImpl explanatoryProcessorImpl;

    private String docTemplate;
    private List<TocItem> tocItems;
    private List<NumberingConfig> numberingConfigs;

    protected final static String PREFIX_CONTENT_PROCESSOR = "/contentProcessor";

    @Before
    public void setUp() {
        super.setup();
        
        docTemplate = "CE-001";
        byte[] bytesFile = TestUtils.getFileContent("/structure-test-explanatory-CN.xml");
        when(templateStructureService.getStructure(docTemplate)).thenReturn(bytesFile);
        ReflectionTestUtils.setField(structureServiceImpl, "structureSchema", "toc/schema/structure_1.xsd");
        
        tocItems = structureServiceImpl.getTocItems(docTemplate);
        numberingConfigs = structureServiceImpl.getNumberingConfigs(docTemplate);

        numberService = new NumberServiceMandate(xmlContentProcessor, structureContextProvider, numberProcessorHandler, parentChildConverter);
        explanatoryProcessorImpl = new ExplanatoryProcessorImpl(xmlContentProcessor, numberService, elementProcessor, structureContextProvider, tableOfContentProcessor, messageHelper);
        ReflectionTestUtils.setField(numberProcessorHandler, "numberConfigFactory", numberConfigFactory);
        ReflectionTestUtils.setField(numberProcessorHandler, "numberProcessorsDepthBased", numberProcessorsDepthBased);
        ReflectionTestUtils.setField(numberProcessorHandler, "numberProcessors", numberProcessors);

        when(structureContextProvider.get()).thenReturn(structureContext);
        when(structureContext.getTocItems()).thenReturn(tocItems);
        when(structureContext.getNumberingConfigs()).thenReturn(numberingConfigs);
    }
    
    @Test
    public void test_explanatory_insertParagraph() {
        byte[] docContent = TestUtils.getFileContent(PREFIX_CONTENT_PROCESSOR + "/test_explanatory_insertParagraph.xml");
        byte[] docContentExpected = TestUtils.getFileContent(PREFIX_CONTENT_PROCESSOR + "/test_explanatory_insertParagraph_expected.xml");
        
        Content content = mock(Content.class);
        Content.Source source = mock(Content.Source.class);
        when(source.getBytes()).thenReturn(docContent);
        when(content.getSource()).thenReturn(source);
        Explanatory document = getMockedExplanatory(content);
        
        byte[] result = explanatoryProcessorImpl.insertNewElement(document, "body_para_2", "paragraph", false);
        
        assertEquals(squeezeXmlAndRemoveAllNS(squeezeXmlAndRemoveAllNS(new String(docContentExpected))), squeezeXmlAndRemoveAllNS(new String(result)));
    }
    
    @Test
    public void test_explanatory_deleteParagraph() throws Exception {
        byte[] docContent = TestUtils.getFileContent(PREFIX_CONTENT_PROCESSOR + "/test_explanatory_deleteParagraph.xml");
        byte[] docContentExpected = TestUtils.getFileContent(PREFIX_CONTENT_PROCESSOR + "/test_explanatory_deleteParagraph_expected.xml");
        
        Content content = mock(Content.class);
        Content.Source source = mock(Content.Source.class);
        when(source.getBytes()).thenReturn(docContent);
        when(content.getSource()).thenReturn(source);
        Explanatory document = getMockedExplanatory(content);
        
        byte[] result = explanatoryProcessorImpl.deleteElement(document, "akn_paragraph_im6PBK", "paragraph");
        
        assertEquals(squeezeXmlAndRemoveAllNS(new String(docContentExpected)), squeezeXmlAndRemoveAllNS(new String(result)));
    }
    
    @Test
    public void test_explanatory_insertLevel() {
        byte[] docContent = TestUtils.getFileContent(PREFIX_CONTENT_PROCESSOR + "/test_explanatory_insertLevel.xml");
        byte[] docContentExpected = TestUtils.getFileContent(PREFIX_CONTENT_PROCESSOR + "/test_explanatory_insertLevel_expected.xml");
        
        Content content = mock(Content.class);
        Content.Source source = mock(Content.Source.class);
        when(source.getBytes()).thenReturn(docContent);
        when(content.getSource()).thenReturn(source);
        Explanatory document = getMockedExplanatory(content);
        
        byte[] result = explanatoryProcessorImpl.insertNewElement(document, "body_level_26", "level", false);
        
        assertEquals(squeezeXmlAndRemoveAllNS(squeezeXmlAndRemoveAllNS(new String(docContentExpected))), squeezeXmlAndRemoveAllNS(new String(result)));
    }
    
    @Test
    public void test_explanatory_deleteLevel() throws Exception {
        byte[] docContent = TestUtils.getFileContent(PREFIX_CONTENT_PROCESSOR + "/test_explanatory_deleteLevel.xml");
        byte[] docContentExpected = TestUtils.getFileContent(PREFIX_CONTENT_PROCESSOR + "/test_explanatory_deleteLevel_expected.xml");
        
        Content content = mock(Content.class);
        Content.Source source = mock(Content.Source.class);
        when(source.getBytes()).thenReturn(docContent);
        when(content.getSource()).thenReturn(source);
        Explanatory document = getMockedExplanatory(content);
        
        byte[] result = explanatoryProcessorImpl.deleteElement(document, "body_level_26", "level");
        
        assertEquals(squeezeXmlAndRemoveAllNS(new String(docContentExpected)), squeezeXmlAndRemoveAllNS(new String(result)));
    }

    private Explanatory getMockedExplanatory(Content content) {
        ExplanatoryMetadata explanatoryMetadata = new ExplanatoryMetadata("... at this stage", "REGULATION OF THE EUROPEAN PARLIAMENT AND OF THE COUNCIL", "on ...",
                "CE-001", "EN", "CE-001", "explanatory", "Working Party cover page", "555", "0.1.0", false);
        List<Collaborator> collaborators = new ArrayList<>();
        collaborators.add(new Collaborator("test", "OWNER", "SG"));
        return new Explanatory("555", "EXPL_COUNCIL", "test", Instant.now(), "test", Instant.now(),
                "", "", "", "", VersionType.MINOR, false, "", collaborators, Arrays.asList(""),
                Option.some(content), Option.some(explanatoryMetadata));
    }
}
