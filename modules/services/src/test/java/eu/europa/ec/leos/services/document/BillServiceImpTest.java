package eu.europa.ec.leos.services.document;


import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.document.Bill;
import eu.europa.ec.leos.domain.cmis.metadata.BillMetadata;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.repository.LeosRepository;
import eu.europa.ec.leos.repository.document.BillRepository;
import eu.europa.ec.leos.repository.document.BillRepositoryImpl;
import eu.europa.ec.leos.repository.store.PackageRepository;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.support.XPathCatalog;
import eu.europa.ec.leos.services.template.TemplateStructureService;
import eu.europa.ec.leos.services.processor.AttachmentProcessor;
import eu.europa.ec.leos.services.document.util.DocumentVOProvider;
import eu.europa.ec.leos.services.numbering.NumberService;
import eu.europa.ec.leos.services.search.SearchEngine;
import eu.europa.ec.leos.services.processor.node.XmlNodeConfigProcessor;
import eu.europa.ec.leos.services.processor.node.XmlNodeProcessor;
import eu.europa.ec.leos.services.processor.content.TableOfContentProcessor;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.services.toc.StructureServiceImpl;
import eu.europa.ec.leos.services.validation.ValidationService;
import eu.europa.ec.leos.vo.toc.NumberingConfig;
import eu.europa.ec.leos.vo.toc.TocItem;
import io.atlassian.fugue.Option;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.util.ReflectionTestUtils;

import javax.inject.Provider;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static eu.europa.ec.leos.services.TestVOCreatorUtils.getJohnTestUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class BillServiceImpTest {

    @Mock PackageRepository packageRepository;
    @Mock XmlNodeProcessor xmlNodeProcessor;
    @Mock
    XmlContentProcessor xmlContentProcessor;
    @Mock
    XmlNodeConfigProcessor xmlNodeConfigProcessor;
    @Mock AttachmentProcessor attachmentProcessor;
    @Mock ValidationService validationService;
    @Mock DocumentVOProvider documentVOProvider;
    @Mock
    NumberService numberService;
    @Mock LeosRepository leosRepository ;
    @Mock MessageHelper messageHelper;
    @Mock ObjectProvider<SearchEngine> searchEngineProvider;

    private BillRepository billRepository;
    private BillService billService;

    @Mock
    private TemplateStructureService templateStructureService;
    @Mock
    private Provider<StructureContext> structureContextProvider;
    @Mock
    private StructureContext structureContext;
    @Mock
    private TableOfContentProcessor tableOfContentProcessor;
    @Mock
    private XPathCatalog xPathCatalog;

    @InjectMocks
    private StructureServiceImpl structureServiceImpl = Mockito.spy(new StructureServiceImpl());

    private String docTemplate;
    private List<TocItem> tocItems;
    private List<NumberingConfig> numberingConfigs;

    @Before
   	public void onSetUp(){
        docTemplate = "BL-023";
        MockitoAnnotations.initMocks(this); //without this you will get NPE
        billRepository =  new BillRepositoryImpl(leosRepository);
        billService = new BillServiceProposalImpl(billRepository, packageRepository, xmlNodeProcessor, xmlContentProcessor, xmlNodeConfigProcessor
            ,attachmentProcessor, validationService, documentVOProvider, numberService, messageHelper,
                tableOfContentProcessor, xPathCatalog);
        byte[] bytesFile = getFileContent("/structure-test-bill-EC.xml");
        when(templateStructureService.getStructure(docTemplate)).thenReturn(bytesFile);
        ReflectionTestUtils.setField(structureServiceImpl, "structureSchema", "toc/schema/structure_1.xsd");

        tocItems = structureServiceImpl.getTocItems(docTemplate);
        numberingConfigs = structureServiceImpl.getNumberingConfigs(docTemplate);

        when(structureContextProvider.get()).thenReturn(structureContext);
        when(structureContext.getTocItems()).thenReturn(tocItems);
        when(structureContext.getNumberingConfigs()).thenReturn(numberingConfigs);
   	}

    @Test
    public void test_saveTableOfContent_shouldbe_calling_correctNumberOfProcessors() {
        // Given
        Content content = mock(Content.class);
        Content.Source source = mock(Content.Source.class);
        final byte[] byteContent = new byte[]{1, 2, 3};
        final BillMetadata billMetadata = new BillMetadata("", "REGULATION", "", "SJ-023", "EN", "BL-023", "bill-id", "", "0.1.0", false);
        final Bill bill = new Bill("1", "Legaltext", "login", Instant.now(), "login", Instant.now(),
                "", "", "Version 1.0.0", "", VersionType.MAJOR, true, "title",
                Collections.emptyList(), Arrays.asList(""), "", "", "", Option.some(content), Option.some(billMetadata));

        when(source.getBytes()).thenReturn(byteContent);
        when(content.getSource()).thenReturn(source);
        when(xmlContentProcessor.createDocumentContentWithNewTocList(any(), any(), any())).thenReturn(byteContent);
        when(numberService.renumberArticles(any(), eq(true))).thenReturn(byteContent);
        when(numberService.renumberRecitals(any())).thenReturn(byteContent);
        when(xmlContentProcessor.doXMLPostProcessing(any())).thenReturn(byteContent);

        //When
        billService.saveTableOfContent(bill, Collections.emptyList(), "test", getJohnTestUser());

        // Then
        verify(xmlContentProcessor, times(1)).createDocumentContentWithNewTocList(any(), any(), any());
        verify(numberService, times(1)).renumberArticles(any(), eq(true));
        verify(numberService, times(1)).renumberRecitals(any());
        verify(xmlContentProcessor, times(1)).doXMLPostProcessing(any());

        verifyNoMoreInteractions(xmlContentProcessor, numberService);
    }
    
    public byte[] getFileContent(String fileName) {
        try {
            InputStream inputStream = this.getClass().getResource(fileName).openStream();
            byte[] content = new byte[inputStream.available()];
            inputStream.read(content);
            inputStream.close();
            return content;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read bytes from file: " + fileName);
        }
    }

}
