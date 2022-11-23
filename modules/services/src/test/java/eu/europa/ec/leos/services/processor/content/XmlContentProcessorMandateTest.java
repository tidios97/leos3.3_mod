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

import eu.europa.ec.leos.model.xml.Element;
import eu.europa.ec.leos.services.support.XercesUtils;
import eu.europa.ec.leos.services.util.TestUtils;
import io.atlassian.fugue.Pair;
import org.junit.Test;
import org.junit.Ignore;
import org.mockito.InjectMocks;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static eu.europa.ec.leos.services.support.XercesUtils.getId;
import static eu.europa.ec.leos.services.support.XercesUtils.updateXMLIDAttribute;
import static eu.europa.ec.leos.services.support.XmlHelper.PARAGRAPH;
import static eu.europa.ec.leos.services.support.XmlHelper.POINT;
import static eu.europa.ec.leos.services.support.XmlHelper.SOFT_MOVE_PLACEHOLDER_ID_PREFIX;
import static eu.europa.ec.leos.services.support.XmlHelper.SUBPARAGRAPH;
import static eu.europa.ec.leos.services.support.XmlHelper.SUBPOINT;
import static eu.europa.ec.leos.services.util.TestUtils.squeezeXmlAndDummyDate;
import static eu.europa.ec.leos.services.util.TestUtils.trimAndRemoveNS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class XmlContentProcessorMandateTest extends XmlContentProcessorTest {

    @InjectMocks
    private XmlContentProcessorImpl xercesXmlContentProcessor = new XmlContentProcessorMandate();


    @Override
    protected void getStructureFile() {
        docTemplate = "BL-023";
        configFile = "/structure-test-bill-CN.xml";
    }

    @Test
    public void test_removeEmptyHeading() {
        byte[] documentXml = TestUtils.getFileContent(FILE_PREFIX + "/test_removeEmptyHeading.xml");
        String returnedElement = xercesXmlContentProcessor.removeEmptyHeading(new String(documentXml, UTF_8));
        byte[] expected = TestUtils.getFileContent(FILE_PREFIX + "/test_removeEmptyHeading_cn_expected.xml");
        assertEquals(squeezeXmlAndDummyDate(new String(expected, UTF_8)), squeezeXmlAndDummyDate(returnedElement));
    }

    @Test
    public void test_removeElementByTagNameAndId() {
        byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX + "/docContentCn.xml");
        byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX + "/test_removeElementByTagNameAndId_cn_expected.xml");
        byte[] returnedElement = xercesXmlContentProcessor.removeElementById(xmlInput, "art486");

        assertEquals(squeezeXmlAndDummyDate(new String(xmlExpected)), squeezeXmlAndDummyDate(new String(returnedElement)));
    }

    @Test
    public void test_insertAffectedAttributeIntoParentElements() {
        byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX + "/test_insertAffectedAttributeIntoParentElements.xml");
        byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX + "/test_insertAffectedAttributeIntoParentElements_expected.xml");

        byte[] resultXml = xercesXmlContentProcessor.insertAffectedAttributeIntoParentElements(xmlInput, "point_to_number");
        String expected = new String(xmlExpected);
        String result = new String(resultXml);
        assertEquals(squeezeXmlAndDummyDate(expected), squeezeXmlAndDummyDate(result));
    }

    @Ignore
    @Test
    public void test_doXMLPostProcessing_split_ec_paragraph() {
        byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX + "/test_doXMLPostProcessing_split_ec_paragraph.xml");
        byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX + "/test_doXMLPostProcessing_split_ec_paragraph_expected.xml");

        Node node = XercesUtils.createXercesDocument(xmlInput);
        xercesXmlContentProcessor.specificInstanceXMLPostProcessing(node);
        String result = XercesUtils.nodeToString(node);

        assertEquals(squeezeXmlAndDummyDate(new String(xmlExpected)), squeezeXmlAndDummyDate(result));
    }

    @Test
    public void test_doXMLPostProcessing_ec_paragraph_add_cn_subparagraph() {
        byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX + "/test_doXMLPostProcessing_ec_paragraph_add_cn_subparagraph.xml");
        byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX + "/test_doXMLPostProcessing_ec_paragraph_add_cn_subparagraph_expected.xml");

        Node node = XercesUtils.createXercesDocument(xmlInput);
        xercesXmlContentProcessor.specificInstanceXMLPostProcessing(node);
        String result = XercesUtils.nodeToString(node);

        assertEquals(squeezeXmlAndDummyDate(new String(xmlExpected)), squeezeXmlAndDummyDate(result));
    }

    @Ignore
    @Test
    public void test_doXMLPostProcessing_split_cn_paragraph() {
        byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX + "/test_doXMLPostProcessing_split_cn_paragraph.xml");
        byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX + "/test_doXMLPostProcessing_split_cn_paragraph_expected.xml");

        Node node = XercesUtils.createXercesDocument(xmlInput);
        xercesXmlContentProcessor.specificInstanceXMLPostProcessing(node);
        String result = XercesUtils.nodeToString(node);

        assertEquals(squeezeXmlAndDummyDate(new String(xmlExpected)), squeezeXmlAndDummyDate(result));
    }

    @Test
    public void test_doXMLPostProcessing_add_cn_paragraph() {
        byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX + "/test_doXMLPostProcessing_add_cn_paragraph.xml");
        byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX + "/test_doXMLPostProcessing_add_cn_paragraph_expected.xml");

        Node node = XercesUtils.createXercesDocument(xmlInput);
        xercesXmlContentProcessor.specificInstanceXMLPostProcessing(node);
        String result = XercesUtils.nodeToString(node);

        assertEquals(squeezeXmlAndDummyDate(new String(xmlExpected)), squeezeXmlAndDummyDate(result));
    }

    @Ignore
    @Test
    public void test_doXMLPostProcessing_split_ec_subparagraph() {
        byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX + "/test_doXMLPostProcessing_split_ec_subparagraph.xml");
        byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX + "/test_doXMLPostProcessing_split_ec_subparagraph_expected.xml");

        Node node = XercesUtils.createXercesDocument(xmlInput);
        xercesXmlContentProcessor.specificInstanceXMLPostProcessing(node);
        String result = XercesUtils.nodeToString(node);

        assertEquals(squeezeXmlAndDummyDate(new String(xmlExpected)), squeezeXmlAndDummyDate(result));
    }

    @Ignore
    @Test
    public void test_doXMLPostProcessing_ec_subparagraph_add_cn_subparagraph() {
        byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX + "/test_doXMLPostProcessing_ec_subparagraph_add_cn_subparagraph.xml");
        byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX + "/test_doXMLPostProcessing_ec_subparagraph_add_cn_subparagraph_expected.xml");

        Node node = XercesUtils.createXercesDocument(xmlInput);
        xercesXmlContentProcessor.specificInstanceXMLPostProcessing(node);
        String result = XercesUtils.nodeToString(node);

        assertEquals(squeezeXmlAndDummyDate(new String(xmlExpected)), squeezeXmlAndDummyDate(result));
    }

    @Ignore
    @Test
    public void test_doXMLPostProcessing_split_cn_subparagraph() {
        byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX + "/test_doXMLPostProcessing_split_cn_subparagraph.xml");
        byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX + "/test_doXMLPostProcessing_split_cn_subparagraph_expected.xml");

        Node node = XercesUtils.createXercesDocument(xmlInput);
        xercesXmlContentProcessor.specificInstanceXMLPostProcessing(node);
        String result = XercesUtils.nodeToString(node);

        assertEquals(squeezeXmlAndDummyDate(new String(xmlExpected)), squeezeXmlAndDummyDate(result));
    }

    @Test
    public void test_doXMLPostProcessing_add_cn_subparagraph() {
        byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX + "/test_doXMLPostProcessing_add_cn_subparagraph.xml");
        byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX + "/test_doXMLPostProcessing_add_cn_subparagraph_expected.xml");

        Node node = XercesUtils.createXercesDocument(xmlInput);
        xercesXmlContentProcessor.specificInstanceXMLPostProcessing(node);
        String result = XercesUtils.nodeToString(node);

        assertEquals(squeezeXmlAndDummyDate(new String(xmlExpected)), squeezeXmlAndDummyDate(result));
    }

    @Test
    public void test_doXMLPostProcessing_add_cn_paragraph_on_cn_article() {
        byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX + "/test_doXMLPostProcessing_add_cn_paragraph_on_cn_article.xml");
        byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX + "/test_doXMLPostProcessing_add_cn_paragraph_on_cn_article_expected.xml");

        Node node = XercesUtils.createXercesDocument(xmlInput);
        xercesXmlContentProcessor.specificInstanceXMLPostProcessing(node);
        String result = XercesUtils.nodeToString(node);

        assertEquals(squeezeXmlAndDummyDate(new String(xmlExpected)), squeezeXmlAndDummyDate(result));
    }

    @Ignore
    @Test
    public void test_doXMLPostProcessing_split_cn_paragraph2() {
        byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX + "/test_doXMLPostProcessing_split_cn_paragraph2.xml");
        byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX + "/test_doXMLPostProcessing_split_cn_paragraph2_expected.xml");

        Node node = XercesUtils.createXercesDocument(xmlInput);
        xercesXmlContentProcessor.specificInstanceXMLPostProcessing(node);
        String result = XercesUtils.nodeToString(node);

        assertEquals(squeezeXmlAndDummyDate(new String(xmlExpected)), squeezeXmlAndDummyDate(result));
    }

    @Test
    public void test_doXMLPostProcessing_add_cn_paragraph2() {
        byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX + "/test_doXMLPostProcessing_add_cn_paragraph2.xml");
        byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX + "/test_doXMLPostProcessing_add_cn_paragraph2_expected.xml");

        Node node = XercesUtils.createXercesDocument(xmlInput);
        xercesXmlContentProcessor.specificInstanceXMLPostProcessing(node);
        String result = XercesUtils.nodeToString(node);

        assertEquals(squeezeXmlAndDummyDate(new String(xmlExpected)), squeezeXmlAndDummyDate(result));
    }

    @Ignore
    @Test
    public void test_doXMLPostProcessing_split_ec_paragraph_empty() {
        byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX + "/test_doXMLPostProcessing_split_ec_paragraph_empty.xml");
        byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX + "/test_doXMLPostProcessing_split_ec_paragraph_empty_expected.xml");

        Node node = XercesUtils.createXercesDocument(xmlInput);
        xercesXmlContentProcessor.specificInstanceXMLPostProcessing(node);
        String result = XercesUtils.nodeToString(node);

        assertEquals(squeezeXmlAndDummyDate(new String(xmlExpected)), squeezeXmlAndDummyDate(result));
    }

    @Ignore
    @Test
    public void test_doXMLPostProcessing_split_cn_subparagraph_empty() {
        byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX + "/test_doXMLPostProcessing_split_cn_subparagraph_empty.xml");
        byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX + "/test_doXMLPostProcessing_split_cn_subparagraph_empty_expected.xml");

        Node node = XercesUtils.createXercesDocument(xmlInput);
        xercesXmlContentProcessor.specificInstanceXMLPostProcessing(node);
        String result = XercesUtils.nodeToString(node);

        assertEquals(squeezeXmlAndDummyDate(new String(xmlExpected)), squeezeXmlAndDummyDate(result));
    }

    @Test
    public void test_doXMLPostProcessing_add_cn_paragraphSingle_on_cn_article() {
        byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX + "/test_doXMLPostProcessing_add_cn_paragraphSingle_on_cn_article.xml");
        byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX + "/test_doXMLPostProcessing_add_cn_paragraphSingle_on_cn_article_expected.xml");

        Node node = XercesUtils.createXercesDocument(xmlInput);
        xercesXmlContentProcessor.specificInstanceXMLPostProcessing(node);
        String result = XercesUtils.nodeToString(node);

        assertEquals(squeezeXmlAndDummyDate(new String(xmlExpected)), squeezeXmlAndDummyDate(result));
    }

    @Test
    public void test_merge_paragraph_ec() {
        byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX + "/test_merge_paragraph_ec.xml");
        byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX + "/test_merge_paragraph_ec_expected.xml");

        byte[] elementToMergeByte = TestUtils.getFileContent(FILE_PREFIX + "/test_merge_paragraph_ec_elementToMerge.xml");
        String elementToMerge = new String(elementToMergeByte);
        byte[] result = xercesXmlContentProcessor.mergeElement(xmlInput, elementToMerge, PARAGRAPH, "imp_art_d1e1276_XAPlpX_OzDWGj");

        assertEquals(squeezeXmlAndDummyDate(new String(xmlExpected)), squeezeXmlAndDummyDate(new String(result, UTF_8)));
    }

    @Test
    public void test_merge_back_splitted_paragraph_ec() {
        byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX + "/test_merge_back_splitted_paragraph_ec.xml");
        byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX + "/test_merge_back_splitted_paragraph_ec_expected.xml");

        byte[] elementToMergeByte = TestUtils.getFileContent(FILE_PREFIX + "/test_merge_back_splitted_paragraph_ec_elementToMerge.xml");
        String elementToMerge = new String(elementToMergeByte);
        byte[] result = xercesXmlContentProcessor.mergeElement(xmlInput, elementToMerge, SUBPARAGRAPH, "imp_art_d1e1276_XAPlpX_to5uqp");

        assertEquals(squeezeXmlAndDummyDate(new String(xmlExpected)), squeezeXmlAndDummyDate(new String(result, UTF_8)));
    }

    @Test
    public void test_merge_points_ec() {
        byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX + "/test_merge_points_ec.xml");
        byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX + "/test_merge_points_ec_expected.xml");

        byte[] elementToMergeByte = TestUtils.getFileContent(FILE_PREFIX + "/test_merge_points_ec_elementToMerge.xml");
        String elementToMerge = new String(elementToMergeByte);
        byte[] result = xercesXmlContentProcessor.mergeElement(xmlInput, elementToMerge, POINT, "imp_art_d1e1221_qiqjdt_GEj1jJ");

        assertEquals(squeezeXmlAndDummyDate(new String(xmlExpected)), squeezeXmlAndDummyDate(new String(result, UTF_8)));
    }

    @Test
    public void test_merge_back_splitted_point_ec() {
        byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX + "/test_merge_back_splitted_point_ec.xml");
        byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX + "/test_merge_back_splitted_point_ec_expected.xml");

        byte[] elementToMergeByte = TestUtils.getFileContent(FILE_PREFIX + "/test_merge_back_splitted_point_ec_elementToMerge.xml");
        String elementToMerge = new String(elementToMergeByte);
        byte[] result = xercesXmlContentProcessor.mergeElement(xmlInput, elementToMerge, SUBPOINT, "imp_art_d1e1221_qiqjdt_ajnCHE");

        assertEquals(squeezeXmlAndDummyDate(new String(xmlExpected)), squeezeXmlAndDummyDate(new String(result, UTF_8)));
    }

    @Test
    public void test_merge_softmoved_point_into_ec_point() {
        byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX + "/test_merge_softmoved_point_into_ec_point.xml");
        byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX + "/test_merge_softmoved_point_into_ec_point_expected.xml");

        byte[] elementToMergeByte = TestUtils.getFileContent(FILE_PREFIX + "/test_merge_softmoved_point_into_ec_point_elementToMerge.xml");
        String elementToMerge = new String(elementToMergeByte);
        byte[] result = xercesXmlContentProcessor.mergeElement(xmlInput, elementToMerge, POINT, "imp_art_d1e1276_XAPlpX_4K2GdA");

        assertEquals(squeezeXmlAndDummyDate(new String(xmlExpected)), squeezeXmlAndDummyDate(new String(result, UTF_8)));
    }

    @Test
    public void test_getMergeOnElement_first_point_of_ec_list() throws Exception {
        byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX + "/test_getMergeOnElement_first_point_of_ec_list.xml");
        byte[] elementToMergeByte = TestUtils.getFileContent(FILE_PREFIX + "/test_getMergeOnElement_first_point_of_ec_list_elementToMerge.xml");
        String elementToMerge = new String(elementToMergeByte);
        Element result = xercesXmlContentProcessor.getMergeOnElement(xmlInput, elementToMerge, POINT, "imp_art_d1e1276_XAPlpX_VKPtnz");
        assertNull(result);
    }

    @Test
    public void test_getMergeOnElement_point_of_ec_list() throws Exception {
        byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX + "/test_getMergeOnElement_point_of_ec_list.xml");
        byte[] elementToMergeByte = TestUtils.getFileContent(FILE_PREFIX + "/test_getMergeOnElement_point_of_ec_list_elementToMerge.xml");
        String elementToMerge = new String(elementToMergeByte);
        Element result = xercesXmlContentProcessor.getMergeOnElement(xmlInput, elementToMerge, POINT, "imp_art_d1e1276_XAPlpX_VKPtnz");
        assertNull(result);
    }

    @Test
    public void test_getSplittedElement() {
        byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX + "/test_merge_back_splitted_paragraph_ec_expected.xml");
        Node node = XercesUtils.createNodeFromXmlFragment(xmlInput);
        Node splittedNode = XercesUtils.getElementById(node, "imp_art_d1e1276_XAPlpX_OzDWGj");
        String splittedElement = XercesUtils.nodeToString(splittedNode);
        Pair<byte[], Element> result = xercesXmlContentProcessor.getSplittedElement(xmlInput, splittedElement, PARAGRAPH, "imp_art_d1e1276_XAPlpX_w7XSAU");
        assertEquals(new String(xmlInput), new String(result.left()));
        assertEquals(trimAndRemoveNS(splittedElement), trimAndRemoveNS(new String(result.right().getElementFragment())));
    }

    @Test
    public void test_getSplittedElement_point() {
        byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX + "/test_merge_back_splitted_point_ec.xml");
        byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX + "/test_getSplittedElement_point_expected.xml");
        Node expectedNode = XercesUtils.createNodeFromXmlFragment(xmlExpected);
        String expectedElement = XercesUtils.nodeToString(expectedNode);
        Pair<byte[], Element> result = xercesXmlContentProcessor.getSplittedElement(xmlInput, expectedElement, SUBPOINT, "transformed_imp_art_d1e1221_qiqjdt_GEj1jJ");
        assertEquals(new String(xmlInput), new String(result.left()));
        assertEquals(trimAndRemoveNS(new String(xmlExpected)), trimAndRemoveNS(new String(result.right().getElementFragment())));
    }

    @Test
    public void test_needsToBeIndented() {
    }

    @Test
    public void test_indentElement() {
    }


    @Test
    public void test_softMovedLabel_moved_transformed_2prefix() {
        byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX + "/test_softMovedLabel_moved_transformed_2prefix.xml");
        Node expectedNode = XercesUtils.createNodeFromXmlFragment(xmlInput);
        NodeList nodeList = expectedNode.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node != null) {
                String id = getId(node);
                if(id != null) {
                    updateXMLIDAttribute(node, SOFT_MOVE_PLACEHOLDER_ID_PREFIX, false);
                    assertEquals(id, node.getAttributes().getNamedItem("xml:id").getNodeValue());
                }
            }
        }
    }

    @Test
    public void test_softMovedLabel_transformed_1prefix() {
        byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX + "/test_softMovedLabel_transformed_1prefix.xml");
        Node expectedNode = XercesUtils.createNodeFromXmlFragment(xmlInput);
        NodeList nodeList = expectedNode.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node != null) {
                String id = getId(node);
                if(id != null) {
                    updateXMLIDAttribute(node, SOFT_MOVE_PLACEHOLDER_ID_PREFIX, false);
                    assertEquals(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + id, node.getAttributes().getNamedItem("xml:id").getNodeValue());
                }
            }
        }
    }

    @Test
    public void test_softMovedLabel_noprefix() {
        byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX + "/test_softMovedLabel_noprefix.xml");
        Node expectedNode = XercesUtils.createNodeFromXmlFragment(xmlInput);
        NodeList nodeList = expectedNode.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node != null) {
                String id = getId(node);
                if(id != null) {
                    updateXMLIDAttribute(node, SOFT_MOVE_PLACEHOLDER_ID_PREFIX, true);
                    assertEquals(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + id, node.getAttributes().getNamedItem("xml:id").getNodeValue());
                }
            }
        }
    }

}
