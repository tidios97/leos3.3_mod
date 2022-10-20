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
import static integration.saveToc.TocVOCreateExplanatoryUtils.createPoint;
import static integration.saveToc.TocVOCreateExplanatoryUtils.createUnnumberedParagraph;
import static integration.saveToc.TocVOCreateUtils.getElementById;
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

public class SaveTocExplanatoryAddTest_IT extends SaveTocExplanatoryTest_IT {

    private static final Logger log = LoggerFactory.getLogger(SaveTocExplanatoryAddTest_IT.class);

    @Test
    public void test_add__unnumberedParagraphWithPoint_after_with2Levels() {

        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_ANNEX, "explanatory_with2Levels.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_ANNEX_CN, "test_add__unnumberedParagraphWithPoint_after_with2Levels__expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentExplanatory(xmlInput);

        TableOfContentItemVO body = getElementById(toc, "mainBody");
        TableOfContentItemVO paragraph = createUnnumberedParagraph("new_paragraph_id", 2);
        TableOfContentItemVO pointA = createPoint("new_point_a_id", 2);
        paragraph.addChildItem(pointA);
        body.addChildItem(paragraph);

        // When
        byte[] xmlResult = processSaveTocExplanatory(xmlInput, toc);
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
