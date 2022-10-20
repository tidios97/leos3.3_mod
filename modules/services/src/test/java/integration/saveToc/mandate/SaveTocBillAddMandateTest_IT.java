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
import static integration.saveToc.TocVOCreateMandateUtils.createArticle;
import static integration.saveToc.TocVOCreateMandateUtils.createChapter;
import static integration.saveToc.TocVOCreateMandateUtils.createParagraph;
import static integration.saveToc.TocVOCreateMandateUtils.createParagraphs;
import static integration.saveToc.TocVOCreateMandateUtils.createPart;
import static integration.saveToc.TocVOCreateMandateUtils.createPoint;
import static integration.saveToc.TocVOCreateMandateUtils.createSection;
import static integration.saveToc.TocVOCreateMandateUtils.createSubParagraph;
import static integration.saveToc.TocVOCreateMandateUtils.createTitle;
import static integration.saveToc.TocVOCreateUtils.getElementById;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import eu.europa.ec.leos.services.support.XercesUtils;
import eu.europa.ec.leos.services.util.TestUtils;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;

public class SaveTocBillAddMandateTest_IT extends SaveTocBillMandateTest_IT {

    private static final Logger log = LoggerFactory.getLogger(SaveTocBillAddMandateTest_IT.class);

    @Test
    public void test_buildTocFromXML() {
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "bill_with1Article.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);
        TableOfContentItemVO article = getElementById(toc, "art_1");
        assertNotNull(article);
    }

    @Test
    public void test_add__article() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "bill_with1Article.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_add__article__expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO newArticle = createArticle("new_art_1", 1);
        TableOfContentItemVO body = getElementById(toc, "body");
        body.addChildItem(newArticle);

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
//        assertTrue(akomantosoXsdValidator.validate(xmlResult));
    }

    @Test
    public void test_add__3Articles() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "bill_with1Article.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_add__3articles__expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO body = getElementById(toc, "body");
        body.addChildItem(0, createArticle("new_art_1", 1)); //new -2
        body.addChildItem(0, createArticle("new_art_2", 1)); //new -1
        body.addChildItem(createArticle("new_art_3", 1)); //new 1a

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
    public void test_add__article_paragraph() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "bill_with1Article.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_add__article_paragraph__expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO body = getElementById(toc, "body");
        TableOfContentItemVO newArticle = createArticle("new_art_1", 1);
        TableOfContentItemVO par = createParagraph("par_1", 2);

        newArticle.addChildItem(par);
        body.addChildItem(newArticle);

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

    @Test
    public void test_add__article_paragraph_point() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "bill_with1Article.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_add__article_paragraph_point__expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO body = getElementById(toc, "body");
        TableOfContentItemVO newArticle = createArticle("new_art_1", 1);
        TableOfContentItemVO par = createParagraph("par_1", 2);
        TableOfContentItemVO point = createPoint("new_point_1", 3);

        newArticle.addChildItem(par);
        par.addChildItem(point);
        body.addChildItem(newArticle);

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

    @Test
    public void test_add__article_paragraph_point_point_point_indent() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "bill_with1Article.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_add__article_paragraph_point_point_point_indent__expected2.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO body = getElementById(toc, "body");
        TableOfContentItemVO newArticle = createArticle("new_art_1", 1);
        TableOfContentItemVO par = createParagraph("par_1", 2);
        TableOfContentItemVO point = createPoint("new_point_1", 3);
        TableOfContentItemVO point2 = createPoint("point_2", 3);
        TableOfContentItemVO point3 = createPoint("point_3", 3);
        TableOfContentItemVO indent = createPoint("point_4", 3);

        newArticle.addChildItem(par);
        par.addChildItem(point);
        point.addChildItem(point2);
        point2.addChildItem(point3);
        point3.addChildItem(indent);
        body.addChildItem(newArticle);

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
    public void test_add__article_3Paragraphs() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "bill_with1Article.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_add__article_3paragraphs__expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO body = getElementById(toc, "body");
        TableOfContentItemVO newArticle = createArticle("new_art_1", 1);
        List<TableOfContentItemVO> pars = createParagraphs("new_par_", 3, 2);

        newArticle.addAllChildItems(pars);
        body.addChildItem(newArticle);

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

    @Test
    public void test_add__article_paragraph_subpar() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "bill_with1Article.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_add__article_paragraph_subpar__expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);
        TableOfContentItemVO body = getElementById(toc, "body");

        TableOfContentItemVO newArticle = createArticle("new_art_1", 1);
        TableOfContentItemVO par = createParagraph("par_1", 2);
        TableOfContentItemVO sub = createSubParagraph("subpar_1", 3);

        newArticle.addChildItem(par);
        par.addChildItem(sub);
        body.addChildItem(newArticle);

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
    public void test_add__article_paragraph_subpar_3points() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "bill_with1Article.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_add__article_paragraph_subpar_3points__expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO body = getElementById(toc, "body");
        TableOfContentItemVO newArticle = createArticle("new_art_1", 1);
        TableOfContentItemVO par = createParagraph("par_1", 2);
        TableOfContentItemVO point1 = createPoint("point_1", 3);
        TableOfContentItemVO point2 = createPoint("point_2", 3);
        TableOfContentItemVO point3 = createPoint("point_3", 3);

        newArticle.addChildItem(par);
        par.addChildItem(point1);
        par.addChildItem(point2);
        par.addChildItem(point3);
        body.addChildItem(newArticle);

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

    @Test
    public void test_add__onCNParagraph_point() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_add__onCNParagraph_point.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_add__onCNParagraph_point_expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO par = getElementById(toc, "par_1");
        TableOfContentItemVO point1 = createPoint("point_1", 1);

        par.addChildItem(point1);

        // When
        byte[] xmlResult = processSaveTocBill(xmlInput, toc);

        // Then
        String result = squeezeXml(new String(xmlResult, UTF_8));
        String expectedStr = squeezeXml(new String(xmlExpected, UTF_8));
        assertEquals(expectedStr, result);
    }

    @Test
    public void test_add__section_article_paragraph_point_point_point_indent() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "bill_with1Article.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_add__section_article_paragraph_point_point_point_indent__expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO body = getElementById(toc, "body");
        TableOfContentItemVO newSection = createSection("new_section_1", 1);
        TableOfContentItemVO newArticle = createArticle("new_art_1", 2);
        TableOfContentItemVO par = createParagraph("par_1", 3);
        TableOfContentItemVO point = createPoint("new_point_1", 4);
        TableOfContentItemVO point2 = createPoint("point_2", 5);
        TableOfContentItemVO point3 = createPoint("point_3", 6);
        TableOfContentItemVO indent = createPoint("point_4", 7);

        newSection.addChildItem(newArticle);
        newArticle.addChildItem(par);
        par.addChildItem(point);
        point.addChildItem(point2);
        point2.addChildItem(point3);
        point3.addChildItem(indent);
        body.addChildItem(newSection);

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

    @Test
    public void test_add__part_title_chapter_section_article_paragraph_pointUntilIndent() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "bill_with1Article.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_add__part_title_chapter_section_article_paragraph_pointUntilIndent__expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO body = getElementById(toc, "body");
        TableOfContentItemVO newPart = createPart("new_part_1", 1);
        TableOfContentItemVO newTitle = createTitle("new_title_1", 2);
        TableOfContentItemVO newChapter = createChapter("new_chapter_1", 3);
        TableOfContentItemVO newSection = createSection("new_section_1", 4);
        TableOfContentItemVO newArticle = createArticle("new_art_1", 5);
        TableOfContentItemVO par = createParagraph("new_par_1", 6);
        TableOfContentItemVO point = createPoint("new_point_1", 7);
        TableOfContentItemVO point2 = createPoint("new_point_2", 8);
        TableOfContentItemVO point3 = createPoint("new_point_3", 9);
        TableOfContentItemVO indent = createPoint("new_point_4", 10);

        newPart.addChildItem(newTitle);
        newTitle.addChildItem(newChapter);
        newChapter.addChildItem(newSection);
        newSection.addChildItem(newArticle);
        newArticle.addChildItem(par);
        par.addChildItem(point);
        point.addChildItem(point2);
        point2.addChildItem(point3);
        point3.addChildItem(indent);
        body.addChildItem(newPart);

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

    @Test
    public void test_add__onExistingParagraph__point() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "bill_with1Article.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_add__onExistingParagraph__point__expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO existingParagraph = getElementById(toc, "art_1__para_1");
        TableOfContentItemVO newPoint = createPoint("point_1", 3);
        existingParagraph.addChildItem(newPoint);
        existingParagraph.setAffected(true);
        existingParagraph.getParentItem().setAffected(true);

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

    @Test
    public void test_add__onExistingParagraph__point_point_point_indent() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "bill_with1Article.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_add__onExistingParagraph__point_point_point_indent__expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO existingParagraph = getElementById(toc, "art_1__para_1");
        TableOfContentItemVO point = createPoint("point_1", 3);
        TableOfContentItemVO point2 = createPoint("point_2", 3);
        TableOfContentItemVO point3 = createPoint("point_3", 3);
        TableOfContentItemVO indent = createPoint("point_4", 3);

        point.addChildItem(point2);
        point2.addChildItem(point3);
        point3.addChildItem(indent);
        existingParagraph.addChildItem(point);
        existingParagraph.setAffected(true);
        existingParagraph.getParentItem().setAffected(true);

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
    public void test_add__onExistingParagraph__3points() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "bill_with1Article.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_add__onExistingParagraph__3points__expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO existingParagraph = getElementById(toc, "art_1__para_1");
        TableOfContentItemVO point = createPoint("point_1", 3);
        TableOfContentItemVO point2 = createPoint("point_2", 3);
        TableOfContentItemVO point3 = createPoint("point_3", 3);

        existingParagraph.addChildItem(point);
        existingParagraph.addChildItem(point2);
        existingParagraph.addChildItem(point3);
        existingParagraph.setAffected(true);
        existingParagraph.getParentItem().setAffected(true);

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

    @Test
    public void test_add__onExistingPartChapter__section() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "bill_with1Part1Chapter1Article.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_add__onExistingPartChapter__section__expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO chapter = getElementById(toc, "chapter_1");
        TableOfContentItemVO newSection = createSection("new_section_1", 1);//first to be inserted
        chapter.addChildItem(newSection);

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

    @Test
    public void test_add__onExistingPartChapter__section_article_paragraph_pointUntilIndent() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "bill_with1Part1Chapter1Article.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_add__onExistingPartChapter__section_article_paragraph_pointUntilIndent__expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO chapter = getElementById(toc, "chapter_1");

        TableOfContentItemVO newSection = createSection("new_section_1", 1);
        TableOfContentItemVO newArticle = createArticle("new_art_1", 2);
        TableOfContentItemVO par = createParagraph("new_par_1", 3);
        TableOfContentItemVO point = createPoint("new_point_1", 4);
        TableOfContentItemVO point2 = createPoint("new_point_2", 5);
        TableOfContentItemVO point3 = createPoint("new_point_3", 6);
        TableOfContentItemVO indent = createPoint("new_point_4", 7);

        chapter.addChildItem(newSection);
        newSection.addChildItem(newArticle);
        newArticle.addChildItem(par);
        par.addChildItem(point);
        point.addChildItem(point2);
        point2.addChildItem(point3);
        point3.addChildItem(indent);

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
