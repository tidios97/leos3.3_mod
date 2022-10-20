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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import eu.europa.ec.leos.services.support.XercesUtils;
import eu.europa.ec.leos.services.util.TestUtils;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import integration.saveToc.TocVOCreateMandateUtils;

public class SaveTocBillMoveMandateTest_IT extends SaveTocBillMandateTest_IT {

    private static final Logger log = LoggerFactory.getLogger(SaveTocBillMoveMandateTest_IT.class);

    @Ignore
    @Test
    public void test_move__article_top_outsideChapter() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "bill_with2Article.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_mandate_move__article_top__expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO body = getElementById(toc, "body");
        TableOfContentItemVO chapter = getElementById(toc, "chapter_1");

        TableOfContentItemVO originalArticle = getElementById(toc, "art_2");
        TableOfContentItemVO moveToArticle = TocVOCreateMandateUtils.createMoveToElement(originalArticle);
        TableOfContentItemVO moveFromArticle = TocVOCreateMandateUtils.createMoveFromElement(originalArticle);

        chapter.removeChildItem(originalArticle);
        chapter.addChildItem(moveToArticle);
        body.addChildItem(0, moveFromArticle);

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
    public void test_move__article_bottom() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "bill_with2ArticlesWithPoints.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_move__article_bottom__expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO body = getElementById(toc, "body");
        TableOfContentItemVO originalArticle = getElementById(toc, "art_1");
        TableOfContentItemVO moveToArticle = TocVOCreateMandateUtils.createMoveToElement(originalArticle);
        TableOfContentItemVO moveFromArticle = TocVOCreateMandateUtils.createMoveFromElement(originalArticle);

        body.removeChildItem(originalArticle);
        body.addChildItem(0, moveToArticle);
        body.addChildItem(moveFromArticle);

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

    // we loose the attribute leos:origin="ec" on heading
    @Test
    public void test_move__article_over_partChapter() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "bill_with2Article.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_move__article_over_partChapter__expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO body = getElementById(toc, "body");
        TableOfContentItemVO chapter = getElementById(toc, "chapter_1");
        TableOfContentItemVO originalArticle = getElementById(toc, "art_1");
        TableOfContentItemVO moveToArticle = TocVOCreateMandateUtils.createMoveToElement(originalArticle);
        TableOfContentItemVO moveFromArticle = TocVOCreateMandateUtils.createMoveFromElement(originalArticle);

        body.getChildItems().remove(originalArticle);
        body.getChildItems().add(0, moveToArticle);
        chapter.getChildItems().add(moveFromArticle);

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
    public void test_move__point_top_sameDepth() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "bill_with2Points.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_move__point_top_sameDepth__expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO list = getElementById(toc, "list");
        TableOfContentItemVO pointB = getElementById(toc, "point_b"); //move it on top of the same depth
        TableOfContentItemVO moveToPoint = TocVOCreateMandateUtils.createMoveToPoint(pointB);
        TableOfContentItemVO moveFromPoint = TocVOCreateMandateUtils.createMoveFromElement(pointB);

        list.removeChildItem(pointB);
        list.addChildItem(0, moveFromPoint);
        list.getParentItem().setAffected(true); //paragraph
        list.getParentItem().getParentItem().setAffected(true); //article
        list.addChildItem(moveToPoint);

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
    public void test_move__point_thanMoveBackAgain() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_move__point_top_sameDepth__expected.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "bill_with2Points_moveBack.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);
        TableOfContentItemVO list = getElementById(toc, "list");

        TableOfContentItemVO moveFromB = getElementById(toc, "point_b");
        list.removeChildItem(moveFromB);
        TableOfContentItemVO moveToB = getElementById(toc, "moved_point_b");
        list.removeChildItem(moveToB);

        moveFromB = TocVOCreateMandateUtils.restorePointToPreviousPosition(moveFromB, moveToB);
        list.addChildItem(moveFromB);

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
    public void test_move__point_asChild_ofSibling() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "bill_with2Points.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_move__point_asChildOfSibling__expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO list = getElementById(toc, "list");
        TableOfContentItemVO pointA = getElementById(toc, "point_a");
        TableOfContentItemVO pointB = getElementById(toc, "point_b");

        TableOfContentItemVO moveToPoint = TocVOCreateMandateUtils.createMoveToPoint(pointA);
        TableOfContentItemVO moveFromPoint = TocVOCreateMandateUtils.createMoveFromElement(pointA);

        list.removeChildItem(pointA); //remove original point A
        list.addChildItem(0, moveToPoint); //put movedTo point A
        pointB.addChildItem(moveFromPoint);

        list.setAffected(true);
        list.getParentItem().setAffected(true);//paragraph
        list.getParentItem().getParentItem().setAffected(true); //article

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
    public void test_move__paragraph_bottom_sameArticle() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "bill_with2Points.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_move__paragraph_bottom_sameArticle__expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO article = getElementById(toc, "art_1");
        TableOfContentItemVO par1 = getElementById(toc, "art_1__para_1"); //move it on top of the same depth
        TableOfContentItemVO moveToPar = TocVOCreateMandateUtils.createMoveToPoint(par1);
        TableOfContentItemVO moveFromPar = TocVOCreateMandateUtils.createMoveFromElement(par1);

        article.removeChildItem(par1);
        article.addChildItem(0, moveToPar);
        article.setAffected(true);
        article.addChildItem(moveFromPar);

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
    public void test_move__paragraph_down_differentChapterArticle() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "bill_with2Article.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_move__paragraph_down_differentChapterArticle__expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO article1Source = getElementById(toc, "art_1");
        TableOfContentItemVO article2Target = getElementById(toc, "art_2");
        TableOfContentItemVO par1 = getElementById(toc, "art_1__para_1");
        TableOfContentItemVO moveToPar = TocVOCreateMandateUtils.createMoveToPoint(par1);
        TableOfContentItemVO moveFromPar = TocVOCreateMandateUtils.createMoveFromElement(par1);

        //source
        article1Source.removeChildItem(par1); //chapter
        article1Source.addChildItem(0, moveToPar);
        //target
        article2Target.setAffected(true);
        article2Target.addChildItem(moveFromPar);

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
    public void test_move__alinea_over_moved_point() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "bill_with2ArticlesWithAlinea.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_move__alinea_onMovedPoint__expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO list = getElementById(toc, "art_1_xzP7r7");
        TableOfContentItemVO destinationPoint = getElementById(toc, "art_1_1OdGZW");
        TableOfContentItemVO originalAlinea = getElementById(toc, "art_1_g9zUKf");
        TableOfContentItemVO moveToPoint = TocVOCreateMandateUtils.createMoveToPoint(originalAlinea);
        TableOfContentItemVO moveFromPoint = TocVOCreateMandateUtils.createMoveFromElement(originalAlinea);

        list.removeChildItem(originalAlinea);
        list.addChildItem(1, moveToPoint);
        destinationPoint.getParentItem().getParentItem().getParentItem().getParentItem().getParentItem().setAffected(true); //article
        destinationPoint.getParentItem().getParentItem().getParentItem().getParentItem().setAffected(true); //paragraph
        destinationPoint.addChildItem(0, moveFromPoint);

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
}
