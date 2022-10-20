package eu.europa.ec.leos.services.processor.content;

import eu.europa.ec.leos.i18n.LanguageHelper;
import eu.europa.ec.leos.i18n.MandateMessageHelper;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.services.template.TemplateStructureService;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.services.toc.StructureServiceImpl;
import eu.europa.ec.leos.services.util.TestUtils;
import eu.europa.ec.leos.test.support.LeosTest;
import eu.europa.ec.leos.vo.toc.NumberingConfig;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItem;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import javax.inject.Provider;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static eu.europa.ec.leos.services.support.XmlHelper.ELEMENTS_TO_HIDE_CONTENT;
import static eu.europa.ec.leos.services.support.XmlHelper.removeTag;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

public abstract class TableOfXmlContentProcessorTest extends LeosTest {

    @Mock
    private eu.europa.ec.leos.security.SecurityContext leosSecurityContext;
    @Mock
    protected Provider<StructureContext> structureContextProvider;
    @Mock
    protected StructureContext structureContext;
    @Mock
    protected TemplateStructureService templateStructureService;
    @Mock
    protected LanguageHelper languageHelper;
    @InjectMocks
    protected MessageHelper messageHelper = Mockito.spy(getMessageHelper());
    @InjectMocks
    protected StructureServiceImpl structureServiceImpl;
    @InjectMocks
    protected TableOfContentProcessor tableOfContentProcessor = Mockito.spy(new TableOfContentProcessorImpl());
    
    protected MessageHelper getMessageHelper() {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("test-servicesContext.xml");
        MessageSource servicesMessageSource = (MessageSource) applicationContext.getBean("servicesMessageSource");
        MessageHelper messageHelper = new MandateMessageHelper(servicesMessageSource);
        return messageHelper;
    }
    
    protected List<TocItem> tocItems;
    protected List<NumberingConfig> numberingConfigs;
    protected Map<TocItem, List<TocItem>> tocRules;
    protected String docTemplate;
    protected String configFile;
    
    protected final static String FILE_PREFIX = "/xml-files";
    
    @Before
    public void onSetUp() {
        super.setup();
        getStructureFile();
        
        byte[] bytesFile = TestUtils.getFileContent(configFile);
        when(templateStructureService.getStructure(docTemplate)).thenReturn(bytesFile);
        when(languageHelper.getCurrentLocale()).thenReturn(new Locale("en"));
        
        ReflectionTestUtils.setField(structureServiceImpl, "structureSchema", "toc/schema/structure_1.xsd");
        numberingConfigs = structureServiceImpl.getNumberingConfigs(docTemplate);
        tocItems = structureServiceImpl.getTocItems(docTemplate);
        tocRules = structureServiceImpl.getTocRules(docTemplate);
        
        when(structureContextProvider.get()).thenReturn(structureContext);
        when(structureContext.getNumberingConfigs()).thenReturn(numberingConfigs);
        when(structureContext.getTocItems()).thenReturn(tocItems);
        when(structureContext.getTocRules()).thenReturn(tocRules);
        when(leosSecurityContext.getUserName()).thenReturn("jane");
    }
    
    protected abstract void getStructureFile();
    
    protected void compareTOCs(List<TableOfContentItemVO> expectedTOC, List<TableOfContentItemVO> actualTOC, boolean compare) {
        assertNotNull(expectedTOC);
        assertNotNull(actualTOC);
        assertEquals(expectedTOC.size(), actualTOC.size());
        
        for (int i = 0; i < expectedTOC.size(); i++) {
            TableOfContentItemVO expectedElement = expectedTOC.get(i);
            TableOfContentItemVO actualElement = actualTOC.get(i);
            
            compareTOCItem(expectedElement, actualElement, compare);
//            compareTOCItem(expectedElement.getParentItem(), actualElement.getParentItem());
            
            assertNotNull(expectedElement.getChildItems());
            assertNotNull(actualElement.getChildItems());
            assertEquals(expectedElement.getChildItems().size(), actualElement.getChildItems().size());
            compareTOCs(expectedElement.getChildItems(), actualElement.getChildItems(), compare);
        }
    }
    
    private void compareTOCItem(TableOfContentItemVO expectedElement, TableOfContentItemVO actualElement, boolean compare) {
        if (expectedElement == null && actualElement == null) {
            return;
        }
//        System.out.println("comparing: " + expectedElement.getId());
        assertEquals(expectedElement.getId(), actualElement.getId());
        assertEquals(expectedElement.getTocItem(), actualElement.getTocItem());
        assertEquals(expectedElement.getOriginAttr(), actualElement.getOriginAttr());
        assertEquals(expectedElement.getNumber(), actualElement.getNumber());
        assertEquals(expectedElement.getOriginNumAttr(), actualElement.getOriginNumAttr());
        assertEquals(expectedElement.getHeading(), actualElement.getHeading());
        assertEquals(expectedElement.getOriginHeadingAttr(), actualElement.getOriginHeadingAttr());
        if (compare) {
            if (!ELEMENTS_TO_HIDE_CONTENT.contains(expectedElement.getTocItem().getAknTag().value())) {
                assertEquals(escapeHtml(removeTag(expectedElement.getContent())), escapeHtml(actualElement.getContent()));
            }
        } else {
            assertEquals(expectedElement.getContent(), actualElement.getContent());
        }

        assertEquals(expectedElement.getList(), actualElement.getList());
        assertEquals(expectedElement.isMovedOnEmptyParent(), actualElement.isMovedOnEmptyParent());
        assertEquals(expectedElement.isUndeleted(), actualElement.isUndeleted());
        assertEquals(expectedElement.getSoftActionAttr(), actualElement.getSoftActionAttr());
        assertEquals(expectedElement.isSoftActionRoot(), actualElement.isSoftActionRoot());
        
        assertEquals(expectedElement.getSoftMoveTo(), actualElement.getSoftMoveTo());
        assertEquals(expectedElement.getSoftMoveFrom(), actualElement.getSoftMoveFrom());
        assertEquals(expectedElement.getSoftTransFrom(), actualElement.getSoftTransFrom());
        assertEquals(expectedElement.getSoftUserAttr(), actualElement.getSoftUserAttr());
        assertEquals(expectedElement.getSoftDateAttr(), actualElement.getSoftDateAttr());
        assertEquals(expectedElement.isAffected(), actualElement.isAffected());
        assertEquals(expectedElement.isNumberingToggled(), actualElement.isNumberingToggled());
        assertEquals(expectedElement.getNumSoftActionAttr(), actualElement.getNumSoftActionAttr());
        assertEquals(expectedElement.getHeadingSoftActionAttr(), actualElement.getHeadingSoftActionAttr());
        
        assertEquals(expectedElement.isRestored(), actualElement.isRestored());
        assertEquals(expectedElement.getItemDepth(), actualElement.getItemDepth());
        assertEquals(expectedElement.getOriginalDepth(), actualElement.getOriginalDepth());
        assertEquals(expectedElement.getElementNumberId(), actualElement.getElementNumberId());
        assertEquals(expectedElement.getIndentOriginType(), actualElement.getIndentOriginType());
        assertEquals(expectedElement.getIndentOriginIndentLevel(), actualElement.getIndentOriginIndentLevel());
        assertEquals(expectedElement.getIndentOriginNumId(), actualElement.getIndentOriginNumId());
        assertEquals(expectedElement.getIndentOriginNumValue(), actualElement.getIndentOriginNumValue());
        assertEquals(expectedElement.getIndentOriginNumOrigin(), actualElement.getIndentOriginNumOrigin());
        
        String expectedLabel = TableOfContentHelper.buildItemCaption(expectedElement, TableOfContentHelper.DEFAULT_CAPTION_MAX_SIZE, messageHelper);
        String actualLabel = TableOfContentHelper.buildItemCaption(actualElement, TableOfContentHelper.DEFAULT_CAPTION_MAX_SIZE, messageHelper);
//        System.out.println("label: " + expectedLabel);
        assertEquals(expectedLabel.trim(), actualLabel.trim());
    }
    
}
