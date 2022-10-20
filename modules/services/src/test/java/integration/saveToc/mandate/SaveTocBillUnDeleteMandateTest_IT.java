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
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import eu.europa.ec.leos.model.action.SoftActionType;
import eu.europa.ec.leos.services.support.XercesUtils;
import eu.europa.ec.leos.services.util.TestUtils;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;

public class SaveTocBillUnDeleteMandateTest_IT extends SaveTocBillMandateTest_IT {

    private static final Logger log = LoggerFactory.getLogger(SaveTocBillUnDeleteMandateTest_IT.class);

    @Test
    public void test_undelete__article() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_softdelete__articleWithPoints__expected.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_undelete__articleWithPoints__expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO articleSoftDeleted = getElementById(toc, "deleted_art_1");
        unDeleteElement(articleSoftDeleted);

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
    public void test_undelete__paragraphWithPoints() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_undelete__paragraphWithPoints.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_undelete__paragraphWithPoints__expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO parSoftDeleted = getElementById(toc, "deleted__art_2_num_par2");
        unDeleteElement(parSoftDeleted);

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

    private void unDeleteElement(TableOfContentItemVO tocItemVO) {
        tocItemVO.setId(tocItemVO.getId().replace(SOFT_DELETE_PLACEHOLDER_ID_PREFIX, ""));
        tocItemVO.setSoftActionAttr(SoftActionType.UNDELETE);
        tocItemVO.setSoftActionRoot(null);
        tocItemVO.setUndeleted(true);

        for (TableOfContentItemVO child : tocItemVO.getChildItems()) {
            unDeleteElement(child);
        }
    }
}
