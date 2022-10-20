package eu.europa.ec.leos.services.processor.content;

import eu.europa.ec.leos.i18n.LanguageHelper;
import eu.europa.ec.leos.i18n.MandateMessageHelper;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.user.Entity;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.clone.CloneContext;
import eu.europa.ec.leos.services.label.ReferenceLabelService;
import eu.europa.ec.leos.services.template.TemplateStructureService;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.services.toc.StructureServiceImpl;
import eu.europa.ec.leos.services.util.TestUtils;
import eu.europa.ec.leos.test.support.LeosTest;
import eu.europa.ec.leos.test.support.model.ModelHelper;
import eu.europa.ec.leos.vo.toc.NumberingConfig;
import eu.europa.ec.leos.vo.toc.StructureConfigUtils;
import eu.europa.ec.leos.vo.toc.TocItem;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static eu.europa.ec.leos.services.support.XmlHelper.ARTICLE;
import static eu.europa.ec.leos.services.support.XmlHelper.BODY;
import static eu.europa.ec.leos.services.support.XmlHelper.CHAPTER;
import static eu.europa.ec.leos.services.support.XmlHelper.CONCLUSIONS;
import static eu.europa.ec.leos.services.support.XmlHelper.PART;
import static eu.europa.ec.leos.services.support.XmlHelper.PREAMBLE;
import static eu.europa.ec.leos.services.support.XmlHelper.PREFACE;
import static eu.europa.ec.leos.services.support.XmlHelper.SECTION;
import static eu.europa.ec.leos.services.support.XmlHelper.TITLE;
import static org.mockito.Mockito.when;

public abstract class XmlContentProcessorTest extends LeosTest {
    @InjectMocks
    protected MessageHelper messageHelper = Mockito.spy(getMessageHelper());
    @Mock
    protected LanguageHelper languageHelper;
    @Mock
    protected ReferenceLabelService referenceLabelService;
    @Mock
    protected Provider<StructureContext> structureContextProvider;
    @Mock
    protected StructureContext structureContext;
    @Mock
    protected TemplateStructureService templateStructureService;
    @Mock
    protected CloneContext cloneContext;
    @Mock
    protected SecurityContext securityContext;

    @InjectMocks
    protected StructureServiceImpl structureServiceImpl;

    protected TocItem tocItemConclusions;
    protected TocItem tocItemArticle;
    protected TocItem tocItemBody;
    protected TocItem tocItemChapter;
    protected TocItem tocItemPart;
    protected TocItem tocItemPreface;
    protected TocItem tocItemPreamble;
    protected TocItem tocItemSection;
    protected TocItem tocItemTitle;

    protected List<TocItem> tocItems;
    protected List<NumberingConfig> numberingConfigs;
    protected Map<TocItem, List<TocItem>> tocRules;
    protected String docTemplate;
    protected String configFile;
    protected byte[] docContent;

    protected final static String FILE_PREFIX = "/contentProcessor";

    @Before
    public void setup() {
        super.setup();
        getStructureFile();

        byte[] bytesFile = TestUtils.getFileContent(configFile);
        when(templateStructureService.getStructure(docTemplate)).thenReturn(bytesFile);
        ReflectionTestUtils.setField(structureServiceImpl, "structureSchema", "toc/schema/structure_1.xsd");
        tocItems = structureServiceImpl.getTocItems(docTemplate);
        numberingConfigs = structureServiceImpl.getNumberingConfigs(docTemplate);
        tocRules = structureServiceImpl.getTocRules(docTemplate);

        tocItemConclusions = StructureConfigUtils.getTocItemByName(tocItems, CONCLUSIONS);
        tocItemArticle = StructureConfigUtils.getTocItemByName(tocItems, ARTICLE);
        tocItemBody = StructureConfigUtils.getTocItemByName(tocItems, BODY);
        tocItemChapter = StructureConfigUtils.getTocItemByName(tocItems, CHAPTER);
        tocItemPart = StructureConfigUtils.getTocItemByName(tocItems, PART);
        tocItemPreface = StructureConfigUtils.getTocItemByName(tocItems, PREFACE);
        tocItemPreamble = StructureConfigUtils.getTocItemByName(tocItems, PREAMBLE);
        tocItemSection = StructureConfigUtils.getTocItemByName(tocItems, SECTION);
        tocItemTitle = StructureConfigUtils.getTocItemByName(tocItems, TITLE);

        when(structureContextProvider.get()).thenReturn(structureContext);
        when(structureContext.getTocItems()).thenReturn(tocItems);
        when(structureContext.getNumberingConfigs()).thenReturn(numberingConfigs);
        when(structureContext.getTocRules()).thenReturn(tocRules);

        List<Entity> entities = new ArrayList<Entity>();
        entities.add(new Entity("1", "DIGIT.B2", "DIGIT"));
        User user = ModelHelper.buildUser(45L, "demo", "demo", entities);
        when(securityContext.getUser()).thenReturn(user);
        when(securityContext.getUserName()).thenReturn("demo");

        docContent = TestUtils.getFileContent(FILE_PREFIX + "/docContent.xml");
    }

    protected abstract void getStructureFile();

    private MessageHelper getMessageHelper() {
        try (ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("test-servicesContext.xml")) {
            MessageSource servicesMessageSource = (MessageSource) applicationContext.getBean("servicesMessageSource");
            MessageHelper messageHelper = new MandateMessageHelper(servicesMessageSource);
            return messageHelper;
        }
    }




}
