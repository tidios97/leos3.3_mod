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

import static eu.europa.ec.leos.services.support.XmlHelper.SOFT_DELETE_PLACEHOLDER_ID_PREFIX;
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

import eu.europa.ec.leos.model.action.SoftActionType;
import eu.europa.ec.leos.services.support.XercesUtils;
import eu.europa.ec.leos.services.util.TestUtils;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;

public class SaveTocBillSoftDeleteMandateTest_IT extends SaveTocBillMandateTest_IT {

    private static final Logger log = LoggerFactory.getLogger(SaveTocBillSoftDeleteMandateTest_IT.class);

    @Test
    public void test_softdelete() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "bill_with2ArticlesWithPoints.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_softdelete__articleWithPoints__expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO articleToSoftDelete = getElementById(toc, "art_1");
        softDeleteElement(articleToSoftDelete);
        TableOfContentItemVO citationToSoftDelete = getElementById(toc, "cit_2");
        citationToSoftDelete.setNumber("");
        softDeleteElement(citationToSoftDelete);

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

    @Ignore // to be implemented. should be restored to initial state bill_with2Points.xml
    @Test
    public void test_softdelete__movedPoint() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_move__point_top_sameDepth__expected.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "bill_with2Points.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO elementToSoftDelete = getElementById(toc, "point_b");
        softDeleteElement(elementToSoftDelete);

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
    public void test_delete_Paragraph_splitted() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete_Paragraph_splitted.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_delete_Paragraph_splitted_expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO body = getElementById(toc, "body");
        softDeleteElement(getElementById(toc, "__akn_article_Vh2NWC"));
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

    private void softDeleteElement(TableOfContentItemVO tocItemVO) {
        tocItemVO.setId(SOFT_DELETE_PLACEHOLDER_ID_PREFIX + tocItemVO.getId());
        tocItemVO.setSoftActionAttr(SoftActionType.DELETE);
        tocItemVO.setSoftActionRoot(true);

        for (TableOfContentItemVO child : tocItemVO.getChildItems()) {
            softDeleteElement(child);
        }
    }

}
