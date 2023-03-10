/*
 * Copyright 2017 European Commission
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
package eu.europa.ec.leos.services.processor;


import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.Content.Source;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.document.Bill;
import eu.europa.ec.leos.domain.cmis.metadata.BillMetadata;
import eu.europa.ec.leos.model.user.Collaborator;
import eu.europa.ec.leos.services.clone.CloneContext;
import eu.europa.ec.leos.services.compare.ContentComparatorService;
import eu.europa.ec.leos.services.document.DocumentContentService;
import eu.europa.ec.leos.services.support.XPathCatalog;
import eu.europa.ec.leos.services.template.TemplateStructureService;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.services.toc.StructureServiceImpl;
import eu.europa.ec.leos.test.support.LeosTest;
import eu.europa.ec.leos.vo.toc.TocItem;
import io.atlassian.fugue.Option;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import javax.inject.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static eu.europa.ec.leos.services.support.XmlHelper.ARTICLE;
import static eu.europa.ec.leos.services.support.XmlHelper.CITATIONS;
import static eu.europa.ec.leos.services.support.XmlHelper.RECITALS;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

public class ElementProcessorImplTest extends LeosTest {

    @Mock
    private XmlContentProcessor xmlContentProcessor;

    @InjectMocks
    private StructureServiceImpl structureServiceImpl;

    @Mock
    private Provider<StructureContext> structureContextProvider;

    @Mock
    private StructureContext structureContext;

    @Mock
    private TemplateStructureService templateStructureService;

    @Mock
    private CloneContext cloneContext;

    @Mock
    private XPathCatalog xPathCatalog;

    @Mock
    private DocumentContentService documentContentService;

    @Mock
    private ContentComparatorService compareService;

    @InjectMocks
    private ElementProcessorImpl elementServiceImpl = new ElementProcessorImpl(xmlContentProcessor, structureContextProvider,
            cloneContext, xPathCatalog, documentContentService, compareService);

    private List<TocItem> tocItems;

    @Before
    public void setup() {
        super.setup();
        String docTemplate = "BL-023";
        byte[] bytesFile = getFileContent("/structure-test-bill-EC.xml");
        when(templateStructureService.getStructure(docTemplate)).thenReturn(bytesFile);
        ReflectionTestUtils.setField(structureServiceImpl, "structureSchema", "toc/schema/structure_1.xsd");
        tocItems = structureServiceImpl.getTocItems(docTemplate);

        when(structureContextProvider.get()).thenReturn(structureContext);
        when(structureContext.getTocItems()).thenReturn(tocItems);
    }

    @Test
    public void testGetArticle() {

        Content content = mock(Content.class);
        Source source = mock(Source.class);

        BillMetadata billMetadata = new BillMetadata("", "REGULATION", "", "SJ-023", "EN","", "bill-id", "", "0.1.0", false);
        List<Collaborator> collaborators = new ArrayList<>();
        collaborators.add(new Collaborator("login", "OWNER", "SG"));

        final byte[] byteContent = new byte[]{1, 2, 3};

        when(source.getBytes()).thenReturn(byteContent);
        when(content.getSource()).thenReturn(source);
    
        final Bill document = getMockedBill(content, billMetadata, collaborators, "1");
        final String articleTag = ARTICLE;
        final String articleId = "7474";
        final String articleContent = "article content";

        when(xmlContentProcessor.getElementByNameAndId(byteContent, articleTag, articleId)).thenReturn(articleContent);

        final String article = elementServiceImpl.getElement(document, articleTag, articleId);

        assertThat(article, is(articleContent));
    }

    @Test
    public void updateArticle() {

        Content content = mock(Content.class);
        Source source = mock(Source.class);

        BillMetadata billMetadata = new BillMetadata("", "REGULATION", "", "SJ-023", "EN","", "bill-id", "", "0.1.0", false);
        List<Collaborator> collaborators = new ArrayList<>();
        collaborators.add(new Collaborator("login", "OWNER", "SG"));

        String docId = "555";
        byte[] originalByteContent = new byte[]{1, 2, 3};
        byte[] updatedByteContent = new byte[]{4, 5, 6};

        when(source.getBytes()).thenReturn(originalByteContent);
        when(content.getSource()).thenReturn(source);
    
        final Bill originalDocument = getMockedBill(content, billMetadata, collaborators, docId);
    
        final String articleTag = ARTICLE;
        final String articleId = "486";
        final String newArticleText = "new article text";

        when(xmlContentProcessor.replaceElementById(originalByteContent, newArticleText, articleId)).thenReturn(updatedByteContent);
        when(xmlContentProcessor.removeEmptyHeading(newArticleText)).thenReturn(newArticleText);

        final byte[] result = elementServiceImpl.updateElement(originalDocument, newArticleText, articleTag, articleId);

        assertThat(result, is(updatedByteContent));
    }

    @Test
    public void saveArticle_when_articleNull_should_DeleteArticle() {

        Content content = mock(Content.class);
        Source source = mock(Source.class);

        BillMetadata billMetadata = new BillMetadata("", "REGULATION", "", "SJ-023", "EN","", "bill-id", "", "0.1.0", false);
        List<Collaborator> collaborators = new ArrayList<>();
        collaborators.add(new Collaborator("login", "OWNER", "SG"));

        String docId = "555";
        byte[] originalByteContent = new byte[]{1, 2, 3};
        byte[] updatedByteContent = new byte[]{4, 5, 6};

        when(source.getBytes()).thenReturn(originalByteContent);
        when(content.getSource()).thenReturn(source);
    
        final Bill originalDocument = getMockedBill(content, billMetadata, collaborators, docId);
    
        final String articleTag = ARTICLE;
        final String articleId = "486";

        when(xmlContentProcessor.replaceElementById(originalByteContent, null, articleId)).thenReturn(updatedByteContent);

        final byte[] result = elementServiceImpl.updateElement(originalDocument, null, articleTag, articleId);

        assertThat(result, is(updatedByteContent));
    }

    @Test
    public void testGetCitations() {

        Content content = mock(Content.class);
        Source source = mock(Source.class);

        BillMetadata billMetadata = new BillMetadata("", "REGULATION", "", "SJ-023", "EN","", "bill-id", "", "0.1.0", false);
        List<Collaborator> collaborators = new ArrayList<>();
        collaborators.add(new Collaborator("login", "OWNER", "SG"));

        byte[] byteContent = new byte[]{1, 2, 3, 4};
        final String tagName = CITATIONS;
        final String tagId = "cits";
        final String contentString = "citations content";

        when(source.getBytes()).thenReturn(byteContent);
        when(content.getSource()).thenReturn(source);
    
        final Bill document = getMockedBill(content, billMetadata, collaborators, "1");
    
        when(xmlContentProcessor.getElementByNameAndId(byteContent, tagName, tagId)).thenReturn(contentString);

        // DO THE ACTUAL CALL
        final String citations = elementServiceImpl.getElement(document, tagName, tagId);

        assertThat(citations, is(contentString));
        verify(xmlContentProcessor).getElementByNameAndId(byteContent, tagName, tagId);
        verifyNoMoreInteractions(xmlContentProcessor);

    }

    @Test
    public void testUpdateCitations() {

        Content content = mock(Content.class);
        Source source = mock(Source.class);

        BillMetadata billMetadata = new BillMetadata("", "REGULATION", "", "SJ-023", "EN","", "bill-id", "", "0.1.0", false);
        List<Collaborator> collaborators = new ArrayList<>();
        collaborators.add(new Collaborator("login", "OWNER", "SG"));

        String docId = "555";
        byte[] originalByteContent = new byte[]{1, 2, 3};
        byte[] updatedByteContent = new byte[]{4, 5, 6};

        when(source.getBytes()).thenReturn(originalByteContent);
        when(content.getSource()).thenReturn(source);
    
        final Bill originalDocument = getMockedBill(content, billMetadata, collaborators, docId);
    
        final String tagName = CITATIONS;
        final String tagId = "cits";
        final String updtedCitations = "Updated citations content";

        when(xmlContentProcessor.replaceElementById(originalByteContent, updtedCitations, tagId)).thenReturn(updatedByteContent);

        // DO THE ACTUAL CALL
        final byte[] result = elementServiceImpl.updateElement(originalDocument, updtedCitations, tagName, tagId);

        assertThat(result, is(updatedByteContent));
        verify(xmlContentProcessor).replaceElementById(originalByteContent, updtedCitations, tagId);
        verifyNoMoreInteractions(xmlContentProcessor);

    }

    @Test
    public void testGetRecitals() {

        Content content = mock(Content.class);
        Source source = mock(Source.class);

        BillMetadata billMetadata = new BillMetadata("", "REGULATION", "", "SJ-023", "EN","", "bill-id", "", "0.1.0", false);
        List<Collaborator> collaborators = new ArrayList<>();
        collaborators.add(new Collaborator("login", "OWNER", "SG"));

        byte[] byteContent = new byte[]{1, 2, 3, 4};
        final String tagName = RECITALS;
        final String tagId = "recs";
        final String contentString = "recitals content";

        when(source.getBytes()).thenReturn(byteContent);
        when(content.getSource()).thenReturn(source);
    
        final Bill document = getMockedBill(content, billMetadata, collaborators, "1");
    
        when(xmlContentProcessor.getElementByNameAndId(byteContent, tagName, tagId)).thenReturn(contentString);

        // DO THE ACTUAL CALL
        final String recitals = elementServiceImpl.getElement(document, tagName, tagId);

        assertThat(recitals, is(contentString));
        verify(xmlContentProcessor).getElementByNameAndId(byteContent, tagName, tagId);
        verifyNoMoreInteractions(xmlContentProcessor);
    }

    @Test
    public void testUpdateRecitals() {

        Content content = mock(Content.class);
        Source source = mock(Source.class);

        BillMetadata billMetadata = new BillMetadata("", "REGULATION", "", "SJ-023", "EN","", "bill-id", "", "0.1.0", false);
        List<Collaborator> collaborators = new ArrayList<>();
        collaborators.add(new Collaborator("login", "OWNER", "SG"));

        final String docId = "555";
        final byte[] originalByteContent = new byte[]{1, 2, 3};
        final byte[] updatedByteContent = new byte[]{4, 5, 6};

        when(source.getBytes()).thenReturn(originalByteContent);
        when(content.getSource()).thenReturn(source);
    
        final Bill originalDocument = getMockedBill(content, billMetadata, collaborators, docId);
    
        final String tagName = RECITALS;
        final String tagId = "recs";
        final String updtedRecitals = "Updated recitals content";

        when(xmlContentProcessor.replaceElementById(originalByteContent, updtedRecitals, tagId)).thenReturn(updatedByteContent);

        // DO THE ACTUAL CALL
        byte[] result = elementServiceImpl.updateElement(originalDocument, updtedRecitals, tagName, tagId);

        assertThat(result, is(updatedByteContent));
        verify(xmlContentProcessor).replaceElementById(originalByteContent, updtedRecitals, tagId);
        verifyNoMoreInteractions(xmlContentProcessor);

    }

    @Test
    public void deleteElement() throws Exception {

        Content content = mock(Content.class);
        Source source = mock(Source.class);

        BillMetadata billMetadata = new BillMetadata("", "REGULATION", "", "SJ-023", "EN","", "bill-id", "", "0.1.0", false);
        List<Collaborator> collaborators = new ArrayList<>();
        collaborators.add(new Collaborator("login", "OWNER", "SG"));

        final String docId = "555";
        final byte[] originalByteContent = new byte[]{1, 2, 3};
        final byte[] updatedByteContent = new byte[]{4, 5, 6};
        final byte[] renumberdContent = new byte[]{14, 15, 16};

        when(source.getBytes()).thenReturn(originalByteContent);
        when(content.getSource()).thenReturn(source);
    
        final Bill originalDocument = getMockedBill(content, billMetadata, collaborators, docId);
        final String elementTag = ARTICLE;
        final String elementId = "486";

        when(xmlContentProcessor.removeElementById(argThat(is(originalByteContent)), argThat(is(elementId))))
                .thenReturn(updatedByteContent);

        // DO THE ACTUAL CALL
        final byte[] result = elementServiceImpl.deleteElement(originalDocument, elementId, elementId);

        assertThat(result, is(updatedByteContent));
    }
    
    private Bill getMockedBill(Content content, BillMetadata billMetadata, List<Collaborator> collaborators, String docId) {
        return new Bill(docId, "Legaltext", "login", Instant.now(), "login", Instant.now(),
                    "", "", "Version 1.0.0", "", VersionType.MAJOR,
                true, "title", collaborators, Arrays.asList(""), "", "",
                "", Option.some(content), Option.some(billMetadata));
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
