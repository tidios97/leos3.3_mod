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
package integration.saveToc.mandate;

import static eu.europa.ec.leos.services.support.XercesUtils.createXercesDocument;
import static eu.europa.ec.leos.services.util.TestUtils.squeezeXml;
import static integration.saveToc.TocVOCreateUtils.getElementById;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import eu.europa.ec.leos.services.support.XercesUtils;
import eu.europa.ec.leos.services.util.TestUtils;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;

/**
 * This class is the inverse of SaveTocBillAddMandateTest_IT
 * For each method in save, we need a method in delete.
 * Example
 * test_add__article => starting from a initial state, add an article, the output will be an XML with a CN article.
 * test_delete__article => from the output of previous method, delete the CN element, the output will be the initial state.
 */
public class SaveTocBillDeleteMandateTest_IT extends SaveTocBillMandateTest_IT {

    private static final Logger log = LoggerFactory.getLogger(SaveTocBillDeleteMandateTest_IT.class);

    @Test
    public void test_delete__article() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_add__article__expected.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__article_expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO body = getElementById(toc, "body");
        body.removeChildItem(getElementById(toc, "art_1"));

        // When
        byte[] xmlResult = processSaveTocBill(xmlInput, toc);

        // Then
        Document documentExpected = createXercesDocument(xmlExpected);
        String expectedStr = XercesUtils.nodeToString(documentExpected);
        Document documentResult = createXercesDocument(xmlResult);
        String result = XercesUtils.nodeToString(documentResult);
        result = squeezeXml(result);
        expectedStr = squeezeXml(expectedStr);
        assertEquals(expectedStr, result);
    }

    @Test
    public void test_delete__3Articles() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__3articles.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__3Articles_expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO body = getElementById(toc, "body");
        body.removeChildItem(getElementById(toc, "art_2"));
        body.removeChildItem(getElementById(toc, "art_3"));
        body.removeChildItem(getElementById(toc, "art_4"));

        // When
        byte[] xmlResult = processSaveTocBill(xmlInput, toc);

        // Then
        Document documentExpected = createXercesDocument(xmlExpected);
        String expectedStr = XercesUtils.nodeToString(documentExpected);
        Document documentResult = createXercesDocument(xmlResult);
        String result = XercesUtils.nodeToString(documentResult);
        result = squeezeXml(result);
        expectedStr = squeezeXml(expectedStr);
        assertThat(result, is(expectedStr));
    }

    //Deleting article (root) all the hierarchy (paragraph, points) should be removed.
    @Test
    public void test_delete__article_paragraph_point_point_point_indent() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__article_paragraph_point_point_point_indent.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__article_paragraph_point_point_point_indent_expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO body = getElementById(toc, "body");
        body.removeChildItem(getElementById(toc, "art_2"));

        // When
        byte[] xmlResult = processSaveTocBill(xmlInput, toc);

        // Then
        Document documentExpected = createXercesDocument(xmlExpected);
        String expectedStr = XercesUtils.nodeToString(documentExpected);
        Document documentResult = createXercesDocument(xmlResult);
        String result = XercesUtils.nodeToString(documentResult);
        result = squeezeXml(result);
        expectedStr = squeezeXml(expectedStr);
        assertThat(result, is(expectedStr));
    }

    // Deleting point(a) all sub-points should be removed; article and paragraph should remain
    // The ideal expectation should be "test_add__article_paragraph__expected.xml". Check why leos:origin="cn" is missing.
    @Test
    public void test_delete__point1__from_article_paragraph_point_point_point_indent() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__point1__from_article_paragraph_point_point_point_indent.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__point1__from_article_paragraph_point_point_point_indent_expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO listElement = getElementById(toc, "art_2_par_1_list_1");
        listElement.removeChildItem(getElementById(toc, "art_2_par_1_new_point_1"));

        // When
        byte[] xmlResult = processSaveTocBill(xmlInput, toc);

        // Then
        Document documentExpected = createXercesDocument(xmlExpected);
        String expectedStr = XercesUtils.nodeToString(documentExpected);
        Document documentResult = createXercesDocument(xmlResult);
        String result = XercesUtils.nodeToString(documentResult);
        result = squeezeXml(result);
        expectedStr = squeezeXml(expectedStr);
        assertThat(result, is(expectedStr));
    }

    //Deleting list/point(a) all sub-points should be removed; article and paragraph should remain
    @Test
    public void test_delete__list1__from_article_paragraph_point_point_point_indent() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_add__article_paragraph_point_point_point_indent__expected.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_add__article_paragraph__expected_OriginCNPresent.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO paragraph = getElementById(toc, "art_2_par_1");
        paragraph.removeChildItem(getElementById(toc, "art_2_par_1_list_1"));

        // When
        byte[] xmlResult = processSaveTocBill(xmlInput, toc);

        // Then
        Document documentExpected = createXercesDocument(xmlExpected);
        String expectedStr = XercesUtils.nodeToString(documentExpected);
        Document documentResult = createXercesDocument(xmlResult);
        String result = XercesUtils.nodeToString(documentResult);
        result = squeezeXml(result);
        expectedStr = squeezeXml(expectedStr);
        assertThat(result, is(expectedStr));
    }

    // Eventual BUG: The ideal expectation should be "bill_with1Article.xml". Check why we have "softtrans_from" attribute
    @Test
    public void test_delete__onExistingParagraph__addedPoint_thanDelete() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__onExistingParagraph__addedPoint_thanDelete.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete__onExistingParagraph__addedPoint_thanDelete__expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO listElement = getElementById(toc, "art_1_par_1_list_1");
        listElement.removeChildItem(getElementById(toc, "art_1_par_1_point_1"));

        // When
        byte[] xmlResult = processSaveTocBill(xmlInput, toc);

        // Then
        Document documentExpected = createXercesDocument(xmlExpected);
        String expectedStr = XercesUtils.nodeToString(documentExpected);
        Document documentResult = createXercesDocument(xmlResult);
        String result = XercesUtils.nodeToString(documentResult);
        result = squeezeXml(result);
        expectedStr = squeezeXml(expectedStr);
        assertThat(result, is(expectedStr));
    }

}
