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
import static integration.saveToc.TocVOCreateMandateUtils.createChapter;
import static integration.saveToc.TocVOCreateMandateUtils.createLevel;
import static integration.saveToc.TocVOCreateMandateUtils.createPart;
import static integration.saveToc.TocVOCreateMandateUtils.createPoint;
import static integration.saveToc.TocVOCreateMandateUtils.createSection;
import static integration.saveToc.TocVOCreateMandateUtils.createTitle;
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

public class SaveTocAnnexAddMandateTest_IT extends SaveTocAnnexMandateTest_IT {

    private static final Logger log = LoggerFactory.getLogger(SaveTocAnnexAddMandateTest_IT.class);

    @Test
    public void test_add__level() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_ANNEX, "annex_with1Level.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_ANNEX_CN, "test_add__level__expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentAnnex(xmlInput);

        TableOfContentItemVO body = getElementById(toc, "mainBody");
        TableOfContentItemVO level = createLevel("new_level_1", 1);
        body.addChildItem(level);

        // When
        byte[] xmlResult = processSaveTocAnnex(xmlInput, toc);
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
    public void test_add__part_title_chapter_section_level_pointUntilIndent() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_ANNEX, "annex_with1Level.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_ANNEX_CN, "test_add__part_title_chapter_section_level_pointUntilIndent__expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentAnnex(xmlInput);

        TableOfContentItemVO body = getElementById(toc, "mainBody");
        TableOfContentItemVO newPart = createPart("new_part_1", 1);
        TableOfContentItemVO newTitle = createTitle("new_title_1", 2);
        TableOfContentItemVO newChapter = createChapter("new_chapter_1", 3);
        TableOfContentItemVO newSection = createSection("new_section_1", 4);
        TableOfContentItemVO level = createLevel("new_level_1", 5);
        TableOfContentItemVO point = createPoint("new_point_1", 6);
        TableOfContentItemVO point2 = createPoint("new_point_2", 7);
        TableOfContentItemVO point3 = createPoint("new_point_3", 8);
        TableOfContentItemVO indent = createPoint("new_point_4", 9);

        newPart.addChildItem(newTitle);
        newTitle.addChildItem(newChapter);
        newChapter.addChildItem(newSection);
        newSection.addChildItem(level);
        level.addChildItem(point);
        point.addChildItem(point2);
        point2.addChildItem(point3);
        point3.addChildItem(indent);
        body.addChildItem(newPart);

        // When
        byte[] xmlResult = processSaveTocAnnex(xmlInput, toc);

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
    public void test_add__onExistingLevelWithOnlyContent__point() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_ANNEX, "annex_with1Level.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_ANNEX_CN, "test_add__onExistingLevelWithOnlyContent__point__expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentAnnex(xmlInput);

        TableOfContentItemVO level = getElementById(toc, "level_1");
        TableOfContentItemVO point = createPoint("new_point_1", 2);
        level.addChildItem(point);

        // When
        byte[] xmlResult = processSaveTocAnnex(xmlInput, toc);

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
