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
package eu.europa.ec.leos.services.processor.content;

import eu.europa.ec.leos.domain.common.TocMode;
import eu.europa.ec.leos.services.util.TestUtils;
import eu.europa.ec.leos.vo.toc.NumberingType;
import eu.europa.ec.leos.vo.toc.StructureConfigUtils;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItemVOBuilder;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static eu.europa.ec.leos.services.support.XmlHelper.DOC;
import static eu.europa.ec.leos.services.support.XmlHelper.PREFACE;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class TableOfContentHelperExplanatoryMandateTest extends TableOfXmlContentProcessorTest {

    @Override
    protected void getStructureFile() {
        docTemplate = "SG-017";
        configFile = "/structure-test-explanatory-CN.xml";
    }

    @Test
    public void test_buildTableOfContent() {
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/explanatory_basic.xml");

        List<TableOfContentItemVO> xercesTOC = tableOfContentProcessor.buildTableOfContent(DOC, fileContent, TocMode.NOT_SIMPLIFIED);
        assertThat(xercesTOC, is(notNullValue()));
        assertThat(xercesTOC.size(), is(2));

        List<TableOfContentItemVO> expectedTOC = buildTOCProgrammatically();

        compareTOCs(expectedTOC, xercesTOC, false);
    }

    // Mandate logic with more children
    private List<TableOfContentItemVO> buildTOCProgrammatically() {
        TableOfContentItemVO preface = buildSingleTOCVo("_preface", PREFACE, null, null, null, null, "", 0);

        TableOfContentItemVO mainBody = TocItemVOBuilder.getBuilder()
                .withId("_body")
                .withTocItem(
                        StructureConfigUtils.getTocItemByName(tocItems, "mainBody")
                )
                .withContent("")
                .withChild(TocItemVOBuilder.getBuilder()
                        .withId("_body_para_1")
                        .withTocItem(
                                StructureConfigUtils.getTocItemByName(tocItems, "paragraph")
                        )
                        .withHeading(null)
                        .withContent("With a view to the meeting of the Working Party on … that will take place on …, delegations will find attached a Presidency compromise text on the above-mentioned proposal.")
                        .withChild(TocItemVOBuilder.getBuilder()
                                .withId("_body_para_1__subparagraph")
                                .withTocItem(
                                        StructureConfigUtils.getTocItemByName(tocItems, "subparagraph")
                                )
                                .withHeading(null)
                                .withContent("With a view to the meeting of the Working Party on … that will take place on …, delegations will find attached a Presidency compromise text on the above-mentioned proposal.")
                                .withNumber(null)
                                .withNode(null)
                                .withItemDepth(0)
                                .withIndentLevel(0)
                                .withElementNumberId(null)
                                .build()
                        )
                        .withChild(TocItemVOBuilder.getBuilder()
                                .withId("_body_para_1__crossheading")
                                .withTocItem(
                                        StructureConfigUtils.getTocItemByNumberingConfig(tocItems, NumberingType.BULLET_BLACK_CIRCLE, "CROSS_HEADING")
                                )
                                .withHeading(null)
                                .withContent("The request of the EP and the EU Agency for Fundamental Rights[the European Data Protection Officer] Comment....delivered an opinion: \\(x = {-b \\pm \\sqrt{b^2-4ac} \\over 2a}\\)")
                                .withNumber("•")
                                .withNode(null).withItemDepth(0)
                                .withIndentLevel(1)
                                .withElementNumberId("_akn_oa6e0J")
                                .build()
                        )
                        .withNumber(null)
                        .withNode(null)
                        .withItemDepth(0)
                        .withElementNumberId(null)
                        .build()
                )
                .withNode(null)
                .withParentItem(null)
                .withItemDepth(0)
                .build();
        return Arrays.asList(preface, mainBody);
    }

    private TableOfContentItemVO buildSingleTOCVo(String id, String aknTag, String heading, String number, String numberId, String origin, String content, int indentLevel) {
        return TocItemVOBuilder.getBuilder()
                .withId(id)
                .withTocItem(
                        StructureConfigUtils.getTocItemByName(tocItems, aknTag)
                )
                .withContent(content)
                .withHeading(heading)
                .withNumber(number)
                .withElementNumberId(numberId)
                .withOriginAttr(origin)
                .withIndentLevel(indentLevel)
                .withNode(null)
                .withItemDepth(0)
                .build();
    }
}
