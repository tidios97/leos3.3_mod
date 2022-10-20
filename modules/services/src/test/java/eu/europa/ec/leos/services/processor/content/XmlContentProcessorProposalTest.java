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
package eu.europa.ec.leos.services.processor.content;

import eu.europa.ec.leos.domain.common.ErrorCode;
import eu.europa.ec.leos.domain.common.Result;
import eu.europa.ec.leos.model.annex.LevelItemVO;
import eu.europa.ec.leos.model.xml.Element;
import eu.europa.ec.leos.services.numbering.NumberProcessorHandler;
import eu.europa.ec.leos.services.numbering.NumberProcessorHandlerProposal;
import eu.europa.ec.leos.services.numbering.config.NumberConfigFactory;
import eu.europa.ec.leos.services.numbering.processor.NumberProcessor;
import eu.europa.ec.leos.services.numbering.processor.NumberProcessorParagraphAndPoint;
import eu.europa.ec.leos.services.support.XPathCatalog;
import eu.europa.ec.leos.services.support.XmlHelper;
import eu.europa.ec.leos.services.label.ref.Ref;
import eu.europa.ec.leos.services.util.TestUtils;
import io.atlassian.fugue.Pair;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static eu.europa.ec.leos.services.support.XmlHelper.ARTICLE;
import static eu.europa.ec.leos.services.support.XmlHelper.LEVEL;
import static eu.europa.ec.leos.services.support.XmlHelper.NUM;
import static eu.europa.ec.leos.services.support.XmlHelper.SUBPOINT;
import static eu.europa.ec.leos.services.support.XercesUtils.createXercesDocument;
import static eu.europa.ec.leos.services.support.XercesUtils.getId;
import static eu.europa.ec.leos.services.util.TestUtils.squeezeXmlAndRemoveAllNS;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.booleanThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

public class XmlContentProcessorProposalTest extends XmlContentProcessorTest {

    @Mock
    List<NumberProcessor> numberProcessors;
    @Mock
    NumberProcessor numberProcessor;
    @InjectMocks
    protected NumberConfigFactory numberConfigFactory = Mockito.spy(new NumberConfigFactory());
    @InjectMocks
    protected NumberProcessorHandler numberProcessorHandler = Mockito.spy(new NumberProcessorHandlerProposal());
    @InjectMocks
    XmlContentProcessorImpl xercesXmlContentProcessor = new XmlContentProcessorProposal();
    @InjectMocks
    private XPathCatalog xPathCatalog = spy(new XPathCatalog());

    @Override
    protected void getStructureFile() {
        docTemplate = "BL-023";
        configFile = "/structure-test-bill-EC.xml";
    }

    @Test
    public void test_getElementValue_should_return_elementValue_when_xpath_Is_Valid() {
        String elementValue = xercesXmlContentProcessor.getElementValue(docContent, "//heading[1]", true);
        String expected = "FINAL PROVISIONS";
        assertThat(elementValue, is(expected));
    }

    @Test
    public void test_getElementValue_should_return_null_when_xpath_Is_InValid() {
        String elementValue = xercesXmlContentProcessor.getElementValue(docContent, "//heading_[1]", true);
        assertThat(elementValue, is(nullValue()));
    }

    @Test
    public void test_getAncestorsIdsForElementId_should_returnEmptyArray_when_rootElementIdPassed() {
        List<String> ids = xercesXmlContentProcessor.getAncestorsIdsForElementId(docContent, "part11");
        assertThat(ids, is(Collections.EMPTY_LIST));
    }

    @Test
    public void test_getAncestorsIdsForElementId_should_returnArrayWithAllAncestorsIds_when_nestedElementPassed() {
        List<String> ids = xercesXmlContentProcessor.getAncestorsIdsForElementId(docContent, "p2");
        assertThat(ids, is(Arrays.asList("part11", "art485", "art485-par2", "con")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_getAncestorsIdsForElementId_should_throwException_when_nonExistedElementPassed() {
        xercesXmlContentProcessor.getAncestorsIdsForElementId(docContent, "notExisted");
    }

    @Test
    public void test_getTagContentByNameAndId_should_returnTagContent_when_tagAndIdFound() {
        String tagContent = xercesXmlContentProcessor.getElementByNameAndId(docContent, SUBPOINT, "art486-aln1");
        String expected = "<alinea xml:id=\"art486-aln1\">" +
                "                        <content xml:id=\"c3\">" +
                "                            <p class=\"Paragraph(unnumbered)\" xml:id=\"p3\">This Regulation shall enter into force on the day following that of its publication in the <i xml:id=\"i2\">Official Journal of the European<authorialNote marker=\"8\" xml:id=\"a3\"><p>TestNote3</p></authorialNote> Union</i>.</p>" +
                "                        </content>" +
                "                    </alinea>";
        assertEquals(squeezeXmlAndRemoveAllNS(expected), squeezeXmlAndRemoveAllNS(tagContent));
    }

    @Test
    public void test_getTagContentByNameAndId_should_returnNull_when_tagAndIdNotFound() {
        String tagContent = xercesXmlContentProcessor.getElementByNameAndId(docContent, SUBPOINT, "art486-aln1123456789");
        assertThat(tagContent, is(nullValue()));
    }

    @Test
    public void test_getTagContentByNameAndId_should_returnFirstTag_when_IdNull() {
        String tagContent = xercesXmlContentProcessor.getElementByNameAndId(docContent, SUBPOINT, null);
        String expected = "<alinea xml:id=\"art486-aln1\">" +
                "                        <content xml:id=\"c3\">" +
                "                            <p class=\"Paragraph(unnumbered)\" xml:id=\"p3\">This Regulation shall enter into force on the day following that of its publication in the <i xml:id=\"i2\">Official Journal of the European<authorialNote marker=\"8\" xml:id=\"a3\"><p>TestNote3</p></authorialNote> Union</i>.</p>" +
                "                        </content>" +
                "                    </alinea>";
        assertEquals(squeezeXmlAndRemoveAllNS(expected), squeezeXmlAndRemoveAllNS(tagContent));
    }

    @Test(expected = RuntimeException.class)
    public void test_getTagContentByNameAndId_should_throwRuntimeException_when_illegalXmlFormat() {
        String xml = " <article xml:id=\"art486\">" +
                "                    <num class=\"ArticleNumber\">Article 486</num>";
        String tagContent = xercesXmlContentProcessor.getElementByNameAndId(xml.getBytes(UTF_8), SUBPOINT, "art486-aln1");
        assertThat(tagContent, is(nullValue()));
    }

    @Test
    public void test_replaceElement() {
        String newContent = "<article xml:id=\"artnew\">" +
                "                    <num class=\"ArticleNumber\" xml:id=\"num1\">Article new</num>" +
                "                    <alinea xml:id=\"artnew-aln1\">" +
                "                        <content xml:id=\"c\">" +
                "                            <p class=\"Paragraph(unnumbered)\" xml:id=\"p\">new content</p>" +
                "                        </content>" +
                "                    </alinea>" +
                "                </article>";

        byte[] returnedElement = xercesXmlContentProcessor.replaceElement(docContent, "//*[@xml:id = 'art486']", true, newContent);
        byte[] expected = TestUtils.getFileContent(FILE_PREFIX + "/test_replaceElement_expected.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected)), squeezeXmlAndRemoveAllNS(new String(returnedElement)));
    }

    @Test
    public void test_replaceElement_should_match_returnedContent_WithNamespaces() {
//        byte[] xmlContent = TestUtils.getFileContent(FILE_PREFIX + "/test_replaceElement.xml");
//        String newContent = "<leos:comments><leos:comment><![CDATA[test]]></leos:comment><leos:comment><![CDATA[Legal act without Annexes]]></leos:comment><leos:comment><![CDATA[0.1.0 vs 0.1.1]]></leos:comment></leos:comments>";
//
//        byte[] returnedElement = xercesXmlContentProcessor.replaceElement(xmlContent, "/akn:akomaNtoso//akn:meta/akn:proprietary/leos:comments", true, newContent);
//
//        byte[] expectedXmlContent = TestUtils.getFileContent(FILE_PREFIX + "/test_replaceElement_should_match_returnedContent_expected.xml");
//        assertThat(squeezeXmlAndRemoveAllNS(new String(returnedElement)), is(squeezeXmlAndRemoveAllNS(new String(expectedXmlContent))));
    }

    @Test
    public void test_replaceElement_should_match_returnedContent_WithoutNamespaces() {
//        byte[] xmlContent = TestUtils.getFileContent(FILE_PREFIX + "/test_replaceElement.xml");
//        String newContent = "<leos:comments><leos:comment><![CDATA[test]]></leos:comment><leos:comment><![CDATA[Legal act without Annexes]]></leos:comment><leos:comment><![CDATA[0.1.0 vs 0.1.1]]></leos:comment></leos:comments>";
//
//        byte[] returnedElement = xercesXmlContentProcessor.replaceElement(xmlContent, "/akomaNtoso//meta/proprietary/comments", false, newContent);
//
//        byte[] expectedXmlContent = TestUtils.getFileContent(FILE_PREFIX + "/test_replaceElement_should_match_returnedContent_expected.xml");
//        assertThat(squeezeXmlAndRemoveAllNSAndRemoveAllNS(new String(returnedElement)), is(squeezeXmlAndRemoveAllNSAndRemoveAllNS(new String(expectedXmlContent))));
    }

//    @Test(expected = IllegalArgumentException.class)
//    public void test_replaceElement_should_not_match_returnedContent_WithoutNamespaces() {
//        byte[] xmlContent = TestUtils.getFileContent(FILE_PREFIX + "/test_replaceElement.xml");
//        String newContent = "<leos:comments><leos:comment><![CDATA[test]]></leos:comment><leos:comment><![CDATA[Legal act without Annexes]]></leos:comment><leos:comment><![CDATA[0.1.0 vs 0.1.1]]></leos:comment></leos:comments>";
//
//        byte[] returnedElement = xercesXmlContentProcessor.replaceElement(xmlContent, "/akomaNtoso//meta/proprietary/leos:comments", false, newContent);
//
//        assertThat(returnedElement, is(nullValue()));
//    }

    @Test
    public void test_replaceElementByTagNameAndId_should_match_returnedTagContent() {
        String newContent = "<article xml:id=\"art486\">" +
                "                    <num class=\"ArticleNumber\" xml:id=\"num1\">Article 486</num>" +
                "                    <alinea xml:id=\"art486-aln1\">" +
                "                        <content xml:id=\"c\">" +
                "                            <p class=\"Paragraph(unnumbered)\" xml:id=\"p\">This text should appear in the main document after merge<authorialNote marker=\"1\" xml:id=\"a4\"><p xml:id=\"p1\">TestNoteX</p></authorialNote> with the updated Article <i xml:id=\"i1\">Official Journal of the European Union</i>.</p>" +
                "                        </content>" +
                "                    </alinea>" +
                "                </article>";

        byte[] returnedElement = xercesXmlContentProcessor.replaceElementById(docContent, newContent, "art486");
        byte[] expected = TestUtils.getFileContent(FILE_PREFIX + "/test_replaceElementByTagNameAndId_should_match_returnedTagContent.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected)), squeezeXmlAndRemoveAllNS(new String(returnedElement)));
    }

    @Test
    public void test_removeElementByTagNameAndId() {
        byte[] returnedElement = xercesXmlContentProcessor.removeElementById(docContent, "art486");
        byte[] expected = TestUtils.getFileContent(FILE_PREFIX + "/test_deleteElementByTagNameAndId.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected)), squeezeXmlAndRemoveAllNS(new String(returnedElement)));
    }

    @Test
    public void test_insertElement_withLeosAttribute() {
        byte[] xmlFragment = TestUtils.getFileContent(FILE_PREFIX + "/test_insertElement_withLeosAttribute.xml");
        byte[] returnedElement = xercesXmlContentProcessor.insertElement(docContent, "//*[@xml:id = 'art486']", true, new String(xmlFragment, UTF_8));
        byte[] expected = TestUtils.getFileContent(FILE_PREFIX + "/test_insertElement_withLeosAttribute_expected.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected)), squeezeXmlAndRemoveAllNS(new String(returnedElement)));
    }

    @Test
    public void test_insertElement() {
        String template = "                <article xml:id=\"art435\">" +
                "              <num xml:id=\"n4\">Article #</num>" +
                "              <heading xml:id=\"h4\">Article heading...</heading>" +
                "              <paragraph xml:id=\"art1-par1\">" +
                "                <num xml:id=\"n4p\">1.</num>" +
                "                <content xml:id=\"c4\">" +
                "                  <p xml:id=\"p4\">Text.<authorialNote marker=\"1\" xml:id=\"a1\"><p>TestNote4</p></authorialNote>..</p>" +
                "                </content>" +
                "              </paragraph>" +
                "             </article>";

        byte[] returnedElement = xercesXmlContentProcessor.insertElement(docContent, "//*[@xml:id = 'art486']", true, template);
        byte[] expected = TestUtils.getFileContent(FILE_PREFIX + "/test_insertElementByTagNameAndId_when_insert_after.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected)), squeezeXmlAndRemoveAllNS(new String(returnedElement)));
    }

    @Test
    public void test_insertElementByTagNameAndId_when_insert_after() {
        String template = "                <article xml:id=\"art435\">" +
                "              <num xml:id=\"n4\">Article #</num>" +
                "              <heading xml:id=\"h4\">Article heading...</heading>" +
                "              <paragraph xml:id=\"art1-par1\">" +
                "                <num xml:id=\"n4p\">1.</num>" +
                "                <content xml:id=\"c4\">" +
                "                  <p xml:id=\"p4\">Text.<authorialNote marker=\"1\" xml:id=\"a1\"><p>TestNote4</p></authorialNote>..</p>" +
                "                </content>" +
                "              </paragraph>" +
                "             </article>";

        byte[] returnedElement = xercesXmlContentProcessor.insertElementByTagNameAndId(docContent, template, ARTICLE, "art486", false);
        byte[] expected = TestUtils.getFileContent(FILE_PREFIX + "/test_insertElementByTagNameAndId_when_insert_after.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected)), squeezeXmlAndRemoveAllNS(new String(returnedElement)));
    }

    @Test
    public void test_insertElementByTagNameAndId_when_insert_before() {
        String template = "                <article xml:id=\"art435\">" +
                "              <num xml:id=\"n4\">Article #</num>" +
                "              <heading xml:id=\"h4\">Article heading...</heading>" +
                "              <paragraph xml:id=\"art1-par1\">" +
                "                <num xml:id=\"n4p\">1.</num>" +
                "                <content xml:id=\"c4\">" +
                "                  <p xml:id=\"p4\">Text.<authorialNote marker=\"1\" xml:id=\"a1\"><p>TestNote4</p></authorialNote>..</p>" +
                "                </content>" +
                "              </paragraph>" +
                "             </article>";

        byte[] returnedElement = xercesXmlContentProcessor.insertElementByTagNameAndId(docContent, template, ARTICLE, "art486", true);

        byte[] expected = TestUtils.getFileContent(FILE_PREFIX + "/test_insertElementByTagNameAndId_when_insert_before.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected)), squeezeXmlAndRemoveAllNS(new String(returnedElement)));
    }

    @Test
    public void test_removeElements_one() {
        // Given
        byte[] xml = TestUtils.getFileContent(FILE_PREFIX + "/test_removeElements_one.xml");
        byte[] expected = TestUtils.getFileContent(FILE_PREFIX + "/test_removeElements_expected.xml");

        // When
        byte[] result = xercesXmlContentProcessor.removeElements(xml, "//*[@refersTo=\"~leosComment\"]");

        // Then
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected, UTF_8)), squeezeXmlAndRemoveAllNS(new String(result, UTF_8)));
    }

    @Test
    public void test_removeElements_multiple() {
        // Given
        byte[] xml = TestUtils.getFileContent(FILE_PREFIX + "/test_removeElements_multiple.xml");
        byte[] expected = TestUtils.getFileContent(FILE_PREFIX + "/test_removeElements_expected.xml");

        // When
        byte[] result = xercesXmlContentProcessor.removeElements(xml, "//*[@refersTo=\"~leosComment\"]");

        // Then
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected, UTF_8)), squeezeXmlAndRemoveAllNS(new String(result, UTF_8)));
    }

    @Test
    public void test_removeElements_withOneParent() {
        // Given
        byte[] xml = TestUtils.getFileContent(FILE_PREFIX + "/test_removeElements_withOneParent.xml");
        byte[] expected = TestUtils.getFileContent(FILE_PREFIX + "/test_removeElements_expected.xml");

        // When
        byte[] result = xercesXmlContentProcessor.removeElements(xml, "//*[@refersTo=\"~leosComment\"]", 1);

        // Then
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected, UTF_8)), squeezeXmlAndRemoveAllNS(new String(result, UTF_8)));
    }

    @Test
    public void test_removeElements_withMultipleParent() {
        // Given
        byte[] xml = TestUtils.getFileContent(FILE_PREFIX + "/test_removeElements_withMultipleParent.xml");
        byte[] expected = TestUtils.getFileContent(FILE_PREFIX + "/test_removeElements_expected.xml");

        // When
        byte[] result = xercesXmlContentProcessor.removeElements(xml, "//*[@refersTo=\"~leosChild\"]", 2);

        // Then
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected, UTF_8)), squeezeXmlAndRemoveAllNS(new String(result, UTF_8)));
    }

    @Test
    public void test_getLastElementId() {
        String xPath = "/part/article[last()]";
        String elementId = xercesXmlContentProcessor.getElementIdByPath(docContent, xPath);

        // Then
        assertThat(elementId, equalTo("art486"));
    }

    @Test
    public void test_updateMultiRefs() {
        // Given
        byte[] xml = TestUtils.getFileContent(FILE_PREFIX + "/test_updateMultiRefs.xml");
        byte[] expectedXml = TestUtils.getFileContent(FILE_PREFIX + "/test_updateMultiRefs_expected.xml");

        when(referenceLabelService.generateLabel(
                (List<Ref>) argThat(containsInAnyOrder(new Ref("aid", "ref1", "bill", null), new Ref("bid", "ref2", "bill", null))),
                argThat(any(String.class)),
                argThat(any(String.class)),
                argThat(any(Document.class)),
                anyBoolean()))
                .thenReturn(new Result<String>(
                        "Article X<ref href=\"bill/ref1\" xml:id=\"aid\">updated ref for test onl</ref> and <ref href=\"bill/ref2\" xml:id=\"bid\">(b)</ref>",
                        null));
        when(referenceLabelService.generateLabel(
                (List<Ref>) argThat(containsInAnyOrder(new Ref("aid2", "ref21", "bill", null), new Ref("bid2", "ref22", "bill", null))), argThat(any(String.class)),
                argThat(any(String.class)),
                argThat(any(Document.class)),
                anyBoolean()))
                .thenReturn(new Result<String>(
                        "Article Y<ref href=\"bill/ref21\" xml:id=\"aid2\">updated ref for test only</ref> and <ref href=\"bill/ref22\" xml:id=\"bid2\">(b)</ref>",
                        null));

        // When
        byte[] result = xercesXmlContentProcessor.updateReferences(xml);

        // Then
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expectedXml)), squeezeXmlAndRemoveAllNS(new String(result)));
        verify(referenceLabelService, times(2)).generateLabel(ArgumentMatchers.any(List.class), ArgumentMatchers.any(String.class),
                ArgumentMatchers.any(String.class), ArgumentMatchers.any(Document.class), ArgumentMatchers.any(Boolean.class));
    }

    @Test
    public void test_updateMultiRefs_noChange() {
        // Given
        byte[] xml = TestUtils.getFileContent(FILE_PREFIX + "/test_updateMultiRefs.xml");

        when(referenceLabelService.generateLabel(
                (List<Ref>) argThat(containsInAnyOrder(new Ref("aid", "ref1", "bill", null), new Ref("bid", "ref2", "bill", null))),
                argThat(any(String.class)),
                argThat(any(String.class)),
                argThat(any(Document.class)),
                anyBoolean()))
                .thenReturn(new Result<>(
                        "Article 1<ref href=\"bill/ref1\" xml:id=\"aid\">(a)</ref> and <ref href=\"bill/ref2\" xml:id=\"bid\" >(b)</ref>",
                        null));
        when(referenceLabelService.generateLabel(
                (List<Ref>) argThat(containsInAnyOrder(new Ref("aid2", "ref21", "bill", null), new Ref("bid2", "ref22", "bill", null))), argThat(any(String.class)),
                argThat(any(String.class)),
                argThat(any(Document.class)),
                anyBoolean()))
                .thenReturn(new Result<String>(
                        "Article 2<ref href=\"bill/ref21\" xml:id=\"aid2\">(a)</ref> and <ref href=\"bill/ref22\" xml:id=\"bid2\">(b)</ref>",
                        null));

        // When
        byte[] actual = xercesXmlContentProcessor.updateReferences(xml);

        // Then
        assertNull(actual);
    }

    @Test
    public void test_updateMultiRefs_one_ref() {
        // Given
        byte[] xml = TestUtils.getFileContent(FILE_PREFIX + "/test_updateMultiRefs_one_ref.xml");
        byte[] expectedXml = TestUtils.getFileContent(FILE_PREFIX + "/test_updateMultiRefs_one_ref_expected.xml");

        when(referenceLabelService.generateLabel(
                (List<Ref>) argThat(containsInAnyOrder(new Ref("aid", "ref1", "bill", null))), argThat(any(String.class)),
                argThat(any(String.class)), argThat(any(Document.class)), anyBoolean()))
                .thenReturn(new Result<String>("Article 3<ref href=\"bill/ref1\" xml:id=\"aid\">(a)</ref>", null));

        // When
        byte[] result = xercesXmlContentProcessor.updateReferences(xml);

        // Then
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expectedXml)), squeezeXmlAndRemoveAllNS(new String(result)));
        verify(referenceLabelService, times(1)).generateLabel(ArgumentMatchers.any(List.class), ArgumentMatchers.any(String.class),
                ArgumentMatchers.any(String.class), ArgumentMatchers.any(Document.class), ArgumentMatchers.any(Boolean.class));
    }

    @Test
    public void test_updateMultiRefs_one_ref_noChange() {
        // Given
        byte[] xml = TestUtils.getFileContent(FILE_PREFIX + "/test_updateMultiRefs_one_ref.xml");

        when(referenceLabelService.generateLabel(
                (List<Ref>) argThat(containsInAnyOrder(new Ref("aid", "ref1", "bill", null))), argThat(any(String.class)),
                argThat(any(String.class)), argThat(any(Document.class)), anyBoolean()))
                .thenReturn(new Result<>("Article 1<ref href=\"bill/ref1\" xml:id=\"aid\">(1)</ref>", null));

        // When
        byte[] actual = xercesXmlContentProcessor.updateReferences(xml);

        // Then
        assertNull(actual);
    }

    @Test
    public void test_updateMultiRefs_broken() {
        // Given
        byte[] xml = TestUtils.getFileContent(FILE_PREFIX + "/test_updateMultiRefs_broken.xml");
        byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX + "/test_updateMultiRefs_broken_expected.xml");

        when(referenceLabelService.generateLabel(
                (List<Ref>) argThat(containsInAnyOrder(new Ref("aid", "ref1", "bill", null))), argThat(any(String.class)),
                argThat(any(String.class)), argThat(any(Document.class)), anyBoolean()))
                .thenReturn(new Result<>("", ErrorCode.DOCUMENT_REFERENCE_NOT_VALID));

        // When
        byte[] resultBytes = xercesXmlContentProcessor.doXMLPostProcessing(xml);

        // Then
        String result = new String(resultBytes, UTF_8);
        String expected = new String(xmlExpected, UTF_8);
        expected = squeezeXmlAndRemoveAllNS(expected);
        result = squeezeXmlAndRemoveAllNS(result);
        assertEquals(expected, result);
    }

    @Test
    public void test_merge_suggestion_found_text() {
        String xmlContent = "<bill>" +
                "<p xml:id=\"ElementId\">This is an example <i xml:id=\"testEltId\">of a replacement</i> text</p>" +
                "</bill>";
        String origText = "a";
        String newText = "the";
        String eltId = "testEltId";
        int start = 3;
        int end = 4;

        String expectedXmlContent = "<bill>" +
                "<p xml:id=\"ElementId\">This is an example <i xml:id=\"testEltId\">of the replacement</i> text</p>" +
                "</bill>";

        byte[] result = xercesXmlContentProcessor.replaceTextInElement(xmlContent.getBytes(), origText, newText, eltId, start, end);

        // Then
        assertEquals(squeezeXmlAndRemoveAllNS(expectedXmlContent), squeezeXmlAndRemoveAllNS(new String(result, UTF_8)));
    }

    @Test
    public void test_merge_suggestion_startingLine() {
        // Given
        byte[] xml = TestUtils.getFileContent(FILE_PREFIX + "/test_merge_suggestion.xml");
        byte[] expectedXml = TestUtils.getFileContent(FILE_PREFIX + "/test_merge_suggestion_startingLine_expected.xml");

        String origText = "Example";
        String newText = "This is an example";
        String eltId = "pId";
        int start = 0;
        int end = 7;

        byte[] result = xercesXmlContentProcessor.replaceTextInElement(xml, origText, newText, eltId, start, end);

        // Then
        assertNotNull(result);
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expectedXml)), squeezeXmlAndRemoveAllNS(new String(result, UTF_8)));
    }

    @Test
    public void test_merge_suggestion_endOfLine() {
        // Given
        byte[] xml = TestUtils.getFileContent(FILE_PREFIX + "/test_merge_suggestion.xml");
        byte[] expectedXml = TestUtils.getFileContent(FILE_PREFIX + "/test_merge_suggestion_endOfLine_expected.xml");

        String origText = "mref.";
        String newText = "of internal references";
        String eltId = "pId";
        int start = 76;
        int end = 81;

        byte[] result = xercesXmlContentProcessor.replaceTextInElement(xml, origText, newText, eltId, start, end);

        // Then
        assertNotNull(result);
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expectedXml)), squeezeXmlAndRemoveAllNS(new String(result, UTF_8)));
    }

    @Test
    public void test_merge_suggestion_endOfLine_formattedXml() {
        // Given
        byte[] xml = TestUtils.getFileContent(FILE_PREFIX + "/test_merge_suggestion_endOfLine_formattedXml.xml");
        byte[] expectedXml = TestUtils.getFileContent(FILE_PREFIX + "/test_merge_suggestion_endOfLine_expected.xml");

        String origText = "mref.";
        String newText = "of internal references";
        String eltId = "pId";
        int start = 80;
        int end = 85;

        byte[] result = xercesXmlContentProcessor.replaceTextInElement(xml, origText, newText, eltId, start, end);

        // Then
        assertNotNull(result);
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expectedXml)), squeezeXmlAndRemoveAllNS(new String(result, UTF_8)));
    }

    @Test
    public void test_merge_suggestion_two_tags_found_text() {
        String xmlContent = "<bill>" +
                "<p xml:id=\"cit_5__p\">Having regard to the opinion of the Committee of the Regions<authorialNote marker=\"1\" placement=\"bottom\" xml:id=\"authorialnote_2\"><p xml:id=\"authorialNote_2__p\">OJ C [...], [...], p. [...]</p></authorialNote>,</p>" +
                "</bill>";
        String origText = "the Committee of the Regions";
        String newText = "the Committee of the Countries";
        String eltId = "cit_5__p";
        int start = 32;
        int end = 60;

        String expectedXmlContent = "<bill>" +
                "<p xml:id=\"cit_5__p\">Having regard to the opinion of the Committee of the Countries<authorialNote marker=\"1\" placement=\"bottom\" xml:id=\"authorialnote_2\"><p xml:id=\"authorialNote_2__p\">OJ C [...], [...], p. [...]</p></authorialNote>,</p>" +
                "</bill>";

        byte[] result = xercesXmlContentProcessor.replaceTextInElement(xmlContent.getBytes(), origText, newText, eltId, start, end);

        // Then
        assertEquals(squeezeXmlAndRemoveAllNS(expectedXmlContent), squeezeXmlAndRemoveAllNS(new String(result, UTF_8)));
    }

    @Test
    public void test_merge_suggestion_two_tags_wrong_id() {
        String xmlContent = "<bill>" +
                "<p xml:id=\"cit_5__p\">Having regard to the opinion of the Committee of the Regions<authorialNote xml:id=\"authorialnote_2\" marker=\"1\" placement=\"bottom\"><p xml:id=\"authorialNote_2__p\">OJ C [...], [...], p. [...]</p></authorialNote>,</p>" +
                "</bill>";
        String origText = "the Committee of the Regions";
        String newText = "the Committee of the Countries";
        String eltId = "cit_5__";
        int start = 32;
        int end = 60;

        byte[] result = xercesXmlContentProcessor.replaceTextInElement(xmlContent.getBytes(), origText, newText, eltId, start, end);

        // Then
        assertNull(result);
    }

    @Test
    public void test_merge_suggestion_two_tags_wrong_text() {
        String xmlContent = "<bill>" +
                "<p xml:id=\"cit_5__p\">Having regard to the opinion of the Committee of the Regions<authorialNote xml:id=\"authorialnote_2\" marker=\"1\" placement=\"bottom\"><p xml:id=\"authorialNote_2__p\">OJ C [...], [...], p. [...]</p></authorialNote>,</p>" +
                "</bill>";
        String origText = "the Committee of the Region";
        String newText = "the Committee of the Countries";
        String eltId = "cit_5__";
        int start = 32;
        int end = 60;

        byte[] result = xercesXmlContentProcessor.replaceTextInElement(xmlContent.getBytes(), origText, newText, eltId, start, end);

        // Then
        assertTrue(result == null);
    }

    @Test
    public void test_getParentElement_should_returnParentElement_when_childIdFound() {
        Element element = xercesXmlContentProcessor.getParentElement(docContent, "c3");
        String expected = "<alinea xml:id=\"art486-aln1\">" +
                "                        <content xml:id=\"c3\">" +
                "                            <p class=\"Paragraph(unnumbered)\" xml:id=\"p3\">This Regulation shall enter into force on the day following that of its publication in the <i xml:id=\"i2\">Official Journal of the European<authorialNote marker=\"8\" xml:id=\"a3\"><p>TestNote3</p></authorialNote> Union</i>.</p>" +
                "                        </content>" +
                "                    </alinea>";

        assertEquals(squeezeXmlAndRemoveAllNS(expected), squeezeXmlAndRemoveAllNS(element.getElementFragment()));
    }

    @Test
    public void test_getParentElement_should_returnNull_when_childIdNotFound() {
        Element element = xercesXmlContentProcessor.getParentElement(docContent, "c3333333");
        assertThat(element, is(nullValue()));
    }

    @Test
    public void test_getLevelItemVO_should_return_valid_ItemVO_1() {
        byte[] xmlContent = TestUtils.getFileContent(FILE_PREFIX + "/test_getLevelItemVO_should_return_valid_ItemVO_1.xml");
        String elementId = "body_level_1"; //levelNum = 1.

        LevelItemVO levelItemVO = xercesXmlContentProcessor.getLevelItemVo(xmlContent, elementId, LEVEL);

        assertEquals(levelItemVO.getId(), elementId);
        assertEquals(levelItemVO.getLevelDepth(), 1);
        assertEquals(levelItemVO.getLevelNum(), "1.");
        assertEquals(levelItemVO.getChildren().size(), 2);
    }

    @Test
    public void test_getLevelItemVO_should_return_valid_ItemVO_1_parent() {
        byte[] xmlContent = TestUtils.getFileContent(FILE_PREFIX + "/test_getLevelItemVO_should_return_valid_ItemVO_1.xml");
        String elementId = "body_level_1_num"; //levelNum = 1.

        LevelItemVO levelItemVO = xercesXmlContentProcessor.getLevelItemVo(xmlContent, elementId, NUM);

        assertEquals(levelItemVO.getId(), elementId);
        assertEquals(levelItemVO.getLevelDepth(), 1);
        assertEquals(levelItemVO.getLevelNum(), "1.");
        assertEquals(levelItemVO.getChildren().size(), 2);
    }

    @Test
    public void test_getLevelItemVO_should_return_valid_ItemVO_2() {
        byte[] xmlContent = TestUtils.getFileContent(FILE_PREFIX + "/test_getLevelItemVO_should_return_valid_ItemVO_2.xml");
        String elementId = "body_level_1_1"; //levelNum = 1.1.

        LevelItemVO levelItemVO = xercesXmlContentProcessor.getLevelItemVo(xmlContent, elementId, LEVEL);

        assertEquals(levelItemVO.getId(), elementId);
        assertEquals(levelItemVO.getLevelDepth(), 2);
        assertEquals(levelItemVO.getLevelNum(), "1.1.");
        assertEquals(levelItemVO.getChildren().size(), 3);
    }

    @Test
    public void test_doImportedElementPreProcessing() {
        byte[] documentXml = TestUtils.getFileContent(FILE_PREFIX + "/test_doImportedElementPreProcessing_article.xml");
        String returnedElement = xercesXmlContentProcessor.doImportedElementPreProcessing(new String(documentXml, UTF_8), null);
        byte[] expected = TestUtils.getFileContent(FILE_PREFIX + "/test_doImportedElementPreProcessing_article_expected.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected, UTF_8)), squeezeXmlAndRemoveAllNS(returnedElement));

        Document document = createXercesDocument(returnedElement.getBytes(XmlHelper.UTF_8));
        Node node = document.getFirstChild();
        String id = getId(node);
        assertNotNull(id);
        assertTrue(id.startsWith("_imp_"));
        assertFalse(id.contains("null"));
    }

    @Test
    public void test_doXMLPostProcessing() {
        byte[] documentXml = TestUtils.getFileContent(FILE_PREFIX + "/docContent_noIds.xml");
        byte[] returnedElement = xercesXmlContentProcessor.doXMLPostProcessing(documentXml);
        byte[] expected = TestUtils.getFileContent(FILE_PREFIX + "/docContent_noIds_postProcessed.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected)), squeezeXmlAndRemoveAllNS(new String(returnedElement)));
    }

    @Test
    public void test_doXMLPostProcessing_clonedProposal_ECOrigin_shouldOnlyChangeLabel() {
        //Given
        byte[] documentXml = TestUtils.getFileContent(FILE_PREFIX + "/test_doXMLPostProcessing_clonedProposal_ECOrigin.xml");
        when(cloneContext.isClonedProposal()).thenReturn(true);
        when(referenceLabelService.generateSoftMoveLabel(
                argThat(equalTo(new Ref("moved_art_1__para_1", "art_1__para_1", null, "ec"))),
                argThat(any(String.class)),
                argThat(any(Node.class)),
                argThat(any(String.class)),
                argThat(any(String.class))))
                .thenReturn(new Result<String>(
                        "MOVED to art_1__para_1",
                        null));
        when(referenceLabelService.generateSoftMoveLabel(
                argThat(equalTo(new Ref("art_1__para_1", "moved_art_1__para_1", null, "ec"))),
                argThat(any(String.class)),
                argThat(any(Node.class)),
                argThat(any(String.class)),
                argThat(any(String.class))))
                .thenReturn(new Result<String>(
                        "MOVED from moved_art_1__para_1",
                        null));

        // When
        byte[] returnedElement = xercesXmlContentProcessor.doXMLPostProcessing(documentXml);

        //Then
        byte[] expected = TestUtils.getFileContent(FILE_PREFIX + "/test_doXMLPostProcessing_clonedProposal_ECOrigin_shouldOnlyChangeLabel_expected.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected)), squeezeXmlAndRemoveAllNS(new String(returnedElement)));
    }

    @Test
    public void test_doXMLPostProcessing_clonedProposal_noOriginAttribute_shouldAddSoftAttributes() {
        //Given
        byte[] documentXml = TestUtils.getFileContent(FILE_PREFIX + "/test_doXMLPostProcessing_clonedProposal_noOriginAttribute.xml");
        when(cloneContext.isClonedProposal()).thenReturn(true);
        when(referenceLabelService.generateSoftMoveLabel(
                argThat(equalTo(new Ref("moved_art_1__para_1", "art_1__para_1", null, null))),
                argThat(any(String.class)),
                argThat(any(Node.class)),
                argThat(any(String.class)),
                argThat(any(String.class))))
                .thenReturn(new Result<String>(
                        "MOVED to art_1__para_1",
                        null));
        when(referenceLabelService.generateSoftMoveLabel(
                argThat(equalTo(new Ref("art_1__para_1", "moved_art_1__para_1", null, null))),
                argThat(any(String.class)),
                argThat(any(Node.class)),
                argThat(any(String.class)),
                argThat(any(String.class))))
                .thenReturn(new Result<String>(
                        "MOVED from moved_art_1__para_1",
                        null));

        // When
        byte[] returnedElement = xercesXmlContentProcessor.doXMLPostProcessing(documentXml);

        //Then
        byte[] expected = TestUtils.getFileContent(FILE_PREFIX + "/test_doXMLPostProcessing_clonedProposal_noOriginAttribute_expected.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected)), squeezeXmlAndRemoveAllNS(new String(returnedElement)));
    }

    @Test
    public void test_removeEmptyHeading() {
        byte[] documentXml = TestUtils.getFileContent(FILE_PREFIX + "/test_removeEmptyHeading.xml");
        String returnedElement = xercesXmlContentProcessor.removeEmptyHeading(new String(documentXml, UTF_8));
        byte[] expected = TestUtils.getFileContent(FILE_PREFIX + "/test_removeEmptyHeading_ec_expected.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected, UTF_8)), squeezeXmlAndRemoveAllNS(returnedElement));
    }

    @Test
    public void test_updateSoftMovedElement_between_different_articles() {
        //Given
        byte[] xmlContent = TestUtils.getFileContent(FILE_PREFIX + "/test_bill_with_moved_para_between_articles.xml");
        byte[] elementFragment = TestUtils.getFileContent(FILE_PREFIX + "/test_different_article_fragment_with_moved_para.xml");

        // When
        when(cloneContext.isClonedProposal()).thenReturn(true);
        NumberProcessor numberProcessorPoint = new NumberProcessorParagraphAndPoint(messageHelper, numberProcessorHandler);
        ReflectionTestUtils.setField(numberProcessorHandler, "numberProcessors", Arrays.asList(numberProcessorPoint));
        Pair<byte[], String> result = xercesXmlContentProcessor.updateSoftMovedElement(xmlContent,
                new String(elementFragment, UTF_8));

        // Then
        byte[] expectedDocument = TestUtils.getFileContent(FILE_PREFIX + "/test_bill_with_updated_moved_element_expected.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expectedDocument, UTF_8)),
                squeezeXmlAndRemoveAllNS(new String(result.left(), UTF_8)));
    }

    @Test
    public void test_updateSoftMovedElement_within_articles() {
        //Given
        byte[] xmlContent = TestUtils.getFileContent(FILE_PREFIX + "/test_bill_with_moved_para_within_articles.xml");
        byte[] elementFragment = TestUtils.getFileContent(FILE_PREFIX + "/test_same_article_fragment_with_moved_para.xml");

        // When
        when(cloneContext.isClonedProposal()).thenReturn(true);
        NumberProcessor numberProcessorPoint = new NumberProcessorParagraphAndPoint(messageHelper, numberProcessorHandler);
        ReflectionTestUtils.setField(numberProcessorHandler, "numberProcessors", Arrays.asList(numberProcessorPoint));
        Pair<byte[], String> result = xercesXmlContentProcessor.updateSoftMovedElement(xmlContent, new String(elementFragment, UTF_8));

        // Then
        byte[] expectedDocument = TestUtils.getFileContent(FILE_PREFIX + "/test_bill_with_updated_moved_element_within_article_expected.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expectedDocument, UTF_8)),
                squeezeXmlAndRemoveAllNS(result.right()));
    }

    @Test
    public void test_getSplittedElement() {

    }

    @Test
    public void test_appendElementToTag() {

    }

    @Test
    public void test_insertDepthAttribute() {

    }

    @Test
    public void test_searchAndReplaceText() {

    }

    @Test
    public void test_updateRefsWithRefOrigin() {

    }

    @Test
    public void test_getElementsByTagName() {

    }

    @Test
    public void test_ignoreNotSelectedElements() {

    }

}
