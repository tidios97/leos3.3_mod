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

import static eu.europa.ec.leos.services.TestVOCreatorUtils.getJaneTestUser;
import static eu.europa.ec.leos.services.util.TestUtils.squeezeXml;
import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.when;

public class ElementProcessorClonedProposalTest_IT extends LeosTest {

    @Mock
    protected SecurityContext securityContext;
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
    protected LanguageHelper languageHelper;
    @Mock
    private eu.europa.ec.leos.security.SecurityContext leosSecurityContext;
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
    private NumberProcessorDepthBased numberProcessorLevel = new NumberProcessorLevel(messageHelper, numberProcessorHandler);
    @InjectMocks
    protected List<NumberProcessor> numberProcessors = Mockito.spy(Stream.of(numberProcessorArticle,
            numberProcessorPoint,
            numberProcessorDefault).collect(Collectors.toList()));
    @InjectMocks
    protected List<NumberProcessorDepthBased> numberProcessorsDepthBased = Mockito.spy(Stream.of(numberProcessorDepthBasedDefault, numberProcessorLevel).collect(Collectors.toList()));

    protected NumberService numberService ;

    protected final static String PREFIX_SAVE_TOC_BILL_CN = "/saveToc/bill/ls/";

    @Before
    public void onSetUp() throws Exception {
        getStructureFile();

        ReflectionTestUtils.setField(numberProcessorHandler, "numberConfigFactory", numberConfigFactory);
        ReflectionTestUtils.setField(numberProcessorHandler, "numberProcessorsDepthBased", numberProcessorsDepthBased);
        ReflectionTestUtils.setField(numberProcessorHandler, "numberProcessors", numberProcessors);
        numberService = new NumberServiceMandate(xmlContentProcessor, structureContextProvider, numberProcessorHandler, parentChildConverter);

        when(languageHelper.getCurrentLocale()).thenReturn(new Locale("en"));
        when(userDetails.getUsername()).thenReturn(getJaneTestUser().getLogin());
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
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
        when(leosSecurityContext.getUserName()).thenReturn("jane");
    }

    protected void getStructureFile() {
        docTemplate = "BL-023";
        byte[] bytesFile = TestUtils.getFileContent("/structure-test-bill-CN.xml");
        when(templateStructureService.getStructure(docTemplate)).thenReturn(bytesFile);
    }

    @Test
    public void test_delete__recital_afterMoved_ls() {
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__recital_afterMoved_ls.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__recital_afterMoved_ls_expected.xml");

//        final String elementId = "_rec_1";
//
//        byte[] resultXml = xmlContentProcessor.removeElementById(xmlInput, elementId);
//        String result = new String(resultXml);
//        String expectedStr = new String(xmlExpected);
//        result = squeezeXml(result);
//        expectedStr = squeezeXml(expectedStr);
//        assertEquals(expectedStr, result);
    }

}
