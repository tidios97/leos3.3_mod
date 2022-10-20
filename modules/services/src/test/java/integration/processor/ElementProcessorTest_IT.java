/*
 * Copyright 2022 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package integration.processor;


import eu.europa.ec.leos.i18n.LanguageHelper;
import eu.europa.ec.leos.i18n.MandateMessageHelper;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.services.clone.CloneContext;
import eu.europa.ec.leos.services.label.ReferenceLabelService;
import eu.europa.ec.leos.services.label.ReferenceLabelServiceImplMandate;
import eu.europa.ec.leos.services.label.ref.*;
import eu.europa.ec.leos.services.numbering.NumberProcessorHandler;
import eu.europa.ec.leos.services.numbering.NumberProcessorHandlerMandate;
import eu.europa.ec.leos.services.numbering.NumberService;
import eu.europa.ec.leos.services.numbering.NumberServiceMandate;
import eu.europa.ec.leos.services.numbering.config.NumberConfigFactory;
import eu.europa.ec.leos.services.numbering.depthBased.ParentChildConverter;
import eu.europa.ec.leos.services.numbering.processor.*;
import eu.europa.ec.leos.services.processor.content.TableOfContentProcessor;
import eu.europa.ec.leos.services.processor.content.TableOfContentProcessorImpl;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessorMandate;
import eu.europa.ec.leos.services.template.TemplateStructureService;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.services.toc.StructureServiceImpl;
import eu.europa.ec.leos.services.util.TestUtils;
import eu.europa.ec.leos.services.validation.handlers.AkomantosoXsdValidator;
import eu.europa.ec.leos.test.support.LeosTest;
import eu.europa.ec.leos.vo.toc.NumberingConfig;
import eu.europa.ec.leos.vo.toc.TocItem;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.inject.Provider;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static eu.europa.ec.leos.services.TestVOCreatorUtils.getJaneDigitUser;
import static eu.europa.ec.leos.services.util.TestUtils.squeezeXml;
import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.when;

public class ElementProcessorTest_IT extends LeosTest {

    @Mock
    protected LanguageHelper languageHelper;
    @Mock
    protected SecurityContext securityContext;
    @Mock
    protected eu.europa.ec.leos.security.SecurityContext leosSecurityContext;
    @Mock
    protected Authentication authentication;
    @Mock
    protected UserDetails userDetails;
    @Mock
    protected Provider<StructureContext> structureContextProvider;
    @Mock
    protected StructureContext structureContext;
    @Mock
    protected TemplateStructureService templateStructureService;
    @Mock
    protected CloneContext cloneContext;

    protected AkomantosoXsdValidator akomantosoXsdValidator = new AkomantosoXsdValidator();

    @InjectMocks
    protected StructureServiceImpl structureServiceImpl;
    @InjectMocks
    protected List<LabelHandler> labelHandlers = Mockito.spy(Stream.of(new LabelArticleElementsOnly(),
            new LabelArticlesOrRecitalsOnly(),
            new LabelCitationsOnly(),
            new LabelHigherOrderElementsOnly())
            .collect(Collectors.toList()));
    @InjectMocks
    protected MessageHelper messageHelper = Mockito.spy(getMessageHelper());

    protected MessageHelper getMessageHelper() {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("test-servicesContext.xml");
        MessageSource servicesMessageSource = (MessageSource) applicationContext.getBean("servicesMessageSource");
        MessageHelper messageHelper = new MandateMessageHelper(servicesMessageSource);
        return messageHelper;
    }

    protected List<TocItem> tocItems;
    protected String docTemplate;
    protected List<NumberingConfig> numberingConfigs;
    protected Map<TocItem, List<TocItem>> tocRules;

    @InjectMocks
    protected XmlContentProcessor xmlContentProcessor = Mockito.spy(new XmlContentProcessorMandate());
    @InjectMocks
    protected TableOfContentProcessor tableOfContentProcessor = Mockito.spy(new TableOfContentProcessorImpl());
    @InjectMocks
    protected ReferenceLabelService referenceLabelService = Mockito.spy(new ReferenceLabelServiceImplMandate());

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
    protected List<NumberProcessorDepthBased> numberProcessorsDepthBased = Mockito.spy(Stream.of(numberProcessorDepthBasedDefault, numberProcessorLevel).collect(Collectors.toList()));

    protected NumberService numberService ;

    protected final static String PREFIX_SAVE_TOC_BILL_CN = "/saveToc/bill/cn/";

    @Before
    public void onSetUp() throws Exception {
        getStructureFile();

        ReflectionTestUtils.setField(numberProcessorHandler, "numberConfigFactory", numberConfigFactory);
        ReflectionTestUtils.setField(numberProcessorHandler, "numberProcessorsDepthBased", numberProcessorsDepthBased);
        ReflectionTestUtils.setField(numberProcessorHandler, "numberProcessors", numberProcessors);
        numberService = new NumberServiceMandate(xmlContentProcessor, structureContextProvider, numberProcessorHandler, parentChildConverter);

        User user = getJaneDigitUser();
        when(languageHelper.getCurrentLocale()).thenReturn(new Locale("en"));
        when(leosSecurityContext.getUser()).thenReturn(user);
        when(leosSecurityContext.getUserName()).thenReturn(user.getName());
        SecurityContextHolder.setContext(securityContext);

        ReflectionTestUtils.setField(akomantosoXsdValidator, "SCHEMA_PATH", "eu/europa/ec/leos/xsd");
        ReflectionTestUtils.setField(akomantosoXsdValidator, "SCHEMA_NAME", "akomantoso30.xsd");
        akomantosoXsdValidator.initXSD();

        getStructureFile();

        ReflectionTestUtils.setField(structureServiceImpl, "structureSchema", "toc/schema/structure_1.xsd");
        numberingConfigs = structureServiceImpl.getNumberingConfigs(docTemplate);
        tocItems = structureServiceImpl.getTocItems(docTemplate);
        tocRules = structureServiceImpl.getTocRules(docTemplate);

        when(structureContextProvider.get()).thenReturn(structureContext);
        when(structureContext.getNumberingConfigs()).thenReturn(numberingConfigs);
        when(structureContext.getTocItems()).thenReturn(tocItems);
        when(structureContext.getTocRules()).thenReturn(tocRules);
    }

    protected void getStructureFile() {
        docTemplate = "BL-023";
        byte[] bytesFile = TestUtils.getFileContent("/structure-test-bill-CN.xml");
        when(templateStructureService.getStructure(docTemplate)).thenReturn(bytesFile);
    }

    @Test
    public void test_delete__recital_afterMoved() {
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__recital_afterMoved.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__recital_afterMoved_expected.xml");

        final String elementId = "_rec_1";

        byte[] resultXml = xmlContentProcessor.removeElementById(xmlInput, elementId);
        String result = new String(resultXml);
        String expectedStr = new String(xmlExpected);
        result = squeezeXml(result);
        expectedStr = squeezeXml(expectedStr);
        assertEquals(expectedStr, result);
    }

    @Test
    public void test_delete__paragraph_afterMoved() {
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__paragraph_afterMoved.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__paragraph_afterMoved_expected.xml");

        final String elementId = "par3_sub";

        byte[] resultXml = xmlContentProcessor.removeElementById(xmlInput, elementId);
        String result = new String(resultXml);
        String expectedStr = new String(xmlExpected);
        result = squeezeXml(result);
        expectedStr = squeezeXml(expectedStr);
        assertEquals(expectedStr, result);
    }

    /**
     * Given:
     * - Article 1(1) moved to Article 1(2a)
     * - Article 3(1), Point (a) moved to Article 1(2a), point (b)(ia)
     * - Delete Article 1(2a)
     * Expect:
     * - Article 1(1) set to softdelete
     * - Article 3(1) set to softdelete
     *
     * Full structure:
     *
     * Article 1
     *  Paragraph 1 MOVED to Article 1(2a)
     *      Point (a)
     *      Point (b)
     *          Point (i)
     *          Point (ii)
     *              Point (1)
     *                  Indent -
     *  Paragraph 2
     *  Paragraph 2a MOVED from Article 1(1)    ==> DELETE
     *      Point (a)
     *      Point (b)
     *          Point (i)
     *          point (ia) MOVED from Article 3(1), point (a)
     *          Point (ii)
     *              Point (1)
     *                  Indent -
     *  Paragraph 3
     * Article 2
     * Article 3
     *  Paragraph 1
     *      Point (a)  MOVED to Article 1(2a), point (b)(ia)
     *      Point (b)
     */
    @Test
    public void test_delete__paragraph_afterMoved_containingECPoint() {
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__paragraph_afterMoved_containingECPoint.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__paragraph_afterMoved_containingECPoint_expected.xml");

        final String elementId = "_art_1_0IxItO";

        byte[] resultXml = xmlContentProcessor.removeElementById(xmlInput, elementId);
        String result = new String(resultXml);
        String expectedStr = new String(xmlExpected);
        result = squeezeXml(result);
        expectedStr = squeezeXml(expectedStr);
        assertEquals(expectedStr, result);
    }

    /**
     * Article 1
     *  Paragraph 1
     *      Point (a)  MOVED to Article 1(1), point (-a)
     *      Point (b)
     *          Point (i)
     *          Point (ii)
     *              Point (1)
     *                  Indent -
     * Article 2
     * Article 3
     *  Paragraph 1    ===> DELETE
     *      Point (-a)  MOVED from Article 1(1), point (a)
     *      Point (a)
     */
    @Test
    public void test_delete__paragraph_containingMovedPoint() {
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__paragraph_containingMovedPoint.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__paragraph_containingMovedPoint_expected.xml");

        final String elementId = "_art_2_RaenZD";

        byte[] resultXml = xmlContentProcessor.removeElementById(xmlInput, elementId);
        String result = new String(resultXml);
        String expectedStr = new String(xmlExpected);
        result = squeezeXml(result);
        expectedStr = squeezeXml(expectedStr);
        assertEquals(expectedStr, result);
    }

    @Test
    public void test_delete__firstSubParagraph() {
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__subParagraph_firstEC_v0.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__subParagraph_firstEC_v1.xml");

        final String elementId = "_art_1_0IxItO";

        byte[] resultXml = xmlContentProcessor.removeElementById(xmlInput, elementId);
        String result = new String(resultXml);
        String expectedStr = new String(xmlExpected);
        result = squeezeXml(result);
        expectedStr = squeezeXml(expectedStr);
        assertEquals(expectedStr, result);
    }

    @Test
    public void test_delete__subParagraph_withList() {
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__subParagraph_withList_v1.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__subParagraph_withList_v2.xml");

        final String elementId = "transformed___akn_article_MSaviI-par1";

        byte[] resultXml = xmlContentProcessor.removeElementById(xmlInput, elementId);
        String result = new String(resultXml);
        String expectedStr = new String(xmlExpected);
        result = squeezeXml(result);
        expectedStr = squeezeXml(expectedStr);
        assertEquals(expectedStr, result);
    }

    @Test
    public void test_delete__point_afterMove() {
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__point_afterMove_v1.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__point_afterMove_v2.xml");

        final String elementId = "_art_1_9Xrkmo";

        byte[] resultXml = xmlContentProcessor.removeElementById(xmlInput, elementId);
        String result = new String(resultXml);
        String expectedStr = new String(xmlExpected);
        result = squeezeXml(result);
        expectedStr = squeezeXml(expectedStr);
        assertEquals(expectedStr, result);
    }

    @Test
    public void test_delete__point_afterMove_inParagraphWithNoPoints() {
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__point_afterMove_inParagraphWithNoPoints_v1.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__point_afterMove_inParagraphWithNoPoints_v2.xml");

        final String elementId = "_art_1_9Xrkmo";

        byte[] resultXml = xmlContentProcessor.removeElementById(xmlInput, elementId);
        String result = new String(resultXml);
        String expectedStr = new String(xmlExpected);
        result = squeezeXml(result);
        expectedStr = squeezeXml(expectedStr);
        assertEquals(expectedStr, result);
    }

    @Test
    public void test_delete__point_containingMovedPointFromDifferentSources() {
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__point_containingMovedPointFromDifferentSources.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__point_containingMovedPointFromDifferentSources_expected.xml");

        final String elementId = "_art_1_8YVMWK";

        byte[] resultXml = xmlContentProcessor.removeElementById(xmlInput, elementId);
        String result = new String(resultXml);
        String expectedStr = new String(xmlExpected);
        result = squeezeXml(result);
        expectedStr = squeezeXml(expectedStr);
        assertEquals(expectedStr, result);
    }

    @Test
    public void test_delete__point_containingMovedPointFromDifferentSources_deleteInner () {
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__point_containingMovedPointFromDifferentSources.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__point_containingMovedPointFromDifferentSources_deleteInner_expected.xml");

        final String elementId = "_art_2_tmKcQk";

        byte[] resultXml = xmlContentProcessor.removeElementById(xmlInput, elementId);
        String result = new String(resultXml);
        String expectedStr = new String(xmlExpected);
        result = squeezeXml(result);
        expectedStr = squeezeXml(expectedStr);
        assertEquals(expectedStr, result);
    }

    @Test
    public void test_delete__transformedPoint() {
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__transformedPoint_v1.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__transformedPoint_v2.xml");

        final String elementId = "transformed__art_2_SFQ4X0";

        byte[] resultXml = xmlContentProcessor.removeElementById(xmlInput, elementId);
        String result = new String(resultXml);
        String expectedStr = new String(xmlExpected);
        result = squeezeXml(result);
        expectedStr = squeezeXml(expectedStr);
        assertEquals(expectedStr, result);
    }

    @Test
    public void test_delete__transformedPoint_secondPart() {
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__transformedPoint_v1.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__transformedPoint_secondPart_excepted.xml");

        final String elementId = "_art_2_82v7Fg";

        byte[] resultXml = xmlContentProcessor.removeElementById(xmlInput, elementId);
        String result = new String(resultXml);
        String expectedStr = new String(xmlExpected);
        result = squeezeXml(result);
        expectedStr = squeezeXml(expectedStr);
        assertEquals(expectedStr, result);
    }

    @Test
    public void test_delete__anotherPoint() {
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__transformedPoint_v1.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__anotherPoint_excepted.xml");

        final String elementId = "_art_2_XZ1wQb";

        byte[] resultXml = xmlContentProcessor.removeElementById(xmlInput, elementId);
        String result = new String(resultXml);
        String expectedStr = new String(xmlExpected);
        result = squeezeXml(result);
        expectedStr = squeezeXml(expectedStr);
        assertEquals(expectedStr, result);
    }

    @Test
    public void test_delete__cnPoint_ofTransformedSubparagraph() {
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__cnPoint_ofTransformedSubparagraph.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__cnPoint_ofTransformedSubparagraph_expected.xml");

        final String elementId = "pointNew";

        byte[] resultXml = xmlContentProcessor.removeElementById(xmlInput, elementId);
        String result = new String(resultXml);
        String expectedStr = new String(xmlExpected);
        result = squeezeXml(result);
        expectedStr = squeezeXml(expectedStr);
        assertEquals(expectedStr, result);
    }

    @Test
    public void test_delete__transformedSubparagraph() {
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__transformedSubparagraph_v1.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__transformedSubparagraph_v2.xml");

        final String elementId = "transformed___akn_article_Vh2NWC-par1";

        byte[] resultXml = xmlContentProcessor.removeElementById(xmlInput, elementId);
        String result = new String(resultXml);
        String expectedStr = new String(xmlExpected);
        result = squeezeXml(result);
        expectedStr = squeezeXml(expectedStr);
        assertEquals(expectedStr, result);
    }

    @Test
    public void test_delete__transformedSubparagraph_secondPart() {
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__transformedSubparagraph_v1.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__transformedSubparagraph_secondPart_expected.xml");

        final String elementId = "__akn_article_Vh2NWC_nnPENY";

        byte[] resultXml = xmlContentProcessor.removeElementById(xmlInput, elementId);
        String result = new String(resultXml);
        String expectedStr = new String(xmlExpected);
        result = squeezeXml(result);
        expectedStr = squeezeXml(expectedStr);
        assertEquals(expectedStr, result);
    }

    @Test
    public void test_delete__level() {
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__level.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__level_expected.xml");

        final String elementId = "_body_level_2";

        byte[] resultXml = xmlContentProcessor.removeElementById(xmlInput, elementId);
        String result = new String(resultXml);
        String expectedStr = new String(xmlExpected);
        result = squeezeXml(result);
        expectedStr = squeezeXml(expectedStr);
        assertEquals(expectedStr, result);
    }

    @Test
    public void test_delete__level_singleChildInChapter() {
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__level.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__level_singleChildInChapter_expected.xml");

        final String elementId = "_akn_level_YuxqgR";

        byte[] resultXml = xmlContentProcessor.removeElementById(xmlInput, elementId);
        String result = new String(resultXml);
        String expectedStr = new String(xmlExpected);
        result = squeezeXml(result);
        expectedStr = squeezeXml(expectedStr);
        assertEquals(expectedStr, result);
    }

    @Test
    public void test_delete__paragraph_annex() {
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__level.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__level_annex_expected.xml");

        final String elementId = "akn_paragraph_6y0dox";

        byte[] resultXml = xmlContentProcessor.removeElementById(xmlInput, elementId);
        String result = new String(resultXml);
        String expectedStr = new String(xmlExpected);
        result = squeezeXml(result);
        expectedStr = squeezeXml(expectedStr);
        assertEquals(expectedStr, result);
    }

}
