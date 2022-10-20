package eu.europa.ec.leos.services.processor.content;

import eu.europa.ec.leos.domain.common.TocMode;
import eu.europa.ec.leos.services.util.TestUtils;
import eu.europa.ec.leos.vo.toc.StructureConfigUtils;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItemVOBuilder;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static eu.europa.ec.leos.services.support.XmlHelper.DOC;
import static eu.europa.ec.leos.services.support.XmlHelper.LEVEL;
import static eu.europa.ec.leos.services.support.XmlHelper.MAIN_BODY;
import static eu.europa.ec.leos.services.support.XmlHelper.PARAGRAPH;
import static eu.europa.ec.leos.services.support.XmlHelper.PART;
import static eu.europa.ec.leos.services.support.XmlHelper.PREFACE;
import static eu.europa.ec.leos.services.processor.content.TableOfContentProcessor.updateDepthOfTocItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class TableOfContentHelperAnnexProposalTest extends TableOfXmlContentProcessorTest {

    @Override
    protected void getStructureFile() {
        docTemplate = "SG-017";
        configFile = "/structure-test-annex-EC.xml";
    }

    @Test
    public void test_numbering_level_wrongDepthStructure() {
        final byte[] xmlInput = TestUtils.getFileContent("/numbering/annex/", "test_numbering_level_wrongDepthStructure_fromToc.xml");
        TableOfContentItemVO mainBody = tableOfContentProcessor.buildTableOfContent(DOC, xmlInput, TocMode.SIMPLIFIED).get(0);
        List<TableOfContentItemVO> mainBodyChildren = mainBody.getChildItems();
        assertEquals(5, mainBodyChildren.get(1).getChildItems().get(1).getChildItems().get(0).getItemDepth());//mainBody/chapter/section/level
        updateDepthOfTocItems(mainBodyChildren);
        assertEquals(3, mainBodyChildren.get(1).getChildItems().get(1).getChildItems().get(0).getItemDepth());//correct depth to 3
    }

    @Test
    public void test_buildTableOfContent() {
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/annex_basic.xml");

        List<TableOfContentItemVO> xercesTOC = tableOfContentProcessor.buildTableOfContent(DOC, fileContent, TocMode.NOT_SIMPLIFIED);
        assertThat(xercesTOC, is(notNullValue()));
        assertThat(xercesTOC.size(), is(2));

        List<TableOfContentItemVO> expectedTOC = buildTOCProgrammatically();

        compareTOCs(expectedTOC, xercesTOC, false);
    }

    private List<TableOfContentItemVO> buildTOCProgrammatically() {
        TableOfContentItemVO preface = buildSingleTOCVo("preface", PREFACE, null, null, null, null, 0, null, "");
        TableOfContentItemVO mainBody = TocItemVOBuilder.getBuilder()
                .withId("mainBody")
                .withContent("")
                .withTocItem(
                        StructureConfigUtils.getTocItemByName(tocItems, MAIN_BODY)
                )
                .withChild(
                        buildSingleTOCVo("level_1", LEVEL, null, "1.", "level_1_num", null, 1, null, "Without heading - only short content")
                )
                .withChild(
                        TocItemVOBuilder.getBuilder()
                                .withId("level_2")
                                .withTocItem(
                                        StructureConfigUtils.getTocItemByName(tocItems, LEVEL)
                                )
                                .withHeading("Level with List")
                                .withNumber("2.")
                                .withElementNumberId("level_2_num")
                                .withList("(a) Point (a) (b) Point (b) (c) Point (c) (i) Point (i) (ii) Point (ii) (1) Point (1) (2) Point (2) - first indent - second indent - third indent (4) Point (4)")
                                .withItemDepth(1)
                                .withContent("Level 2 first sub")
                                .build()

                )
                .withChild(
                        TocItemVOBuilder.getBuilder()
                                .withTocItem(
                                        StructureConfigUtils.getTocItemByName(tocItems, PART)
                                )
                                .withId("part_1")
                                .withHeading("Part heading.")
                                .withNumber("I")
                                .withElementNumberId("part_1_num")
                                .withContent("Paragraph1, sub1 content Paragraph1, sub2 content (a) Point (a) content (i) Point (i) content")
                                .withItemDepth(1)
                                .withChild(
                                        buildSingleTOCVo("par_1", PARAGRAPH, null, null, null, null, 0, "(a) Point (a) content (i) Point (i) content", "Paragraph1, sub1 content")
                                )
                                .withChild(
                                        buildSingleTOCVo("level_3", LEVEL, "Heading for 3.", "3.", "level_3_num", null, 1, null, "Content for 3.")
                                )
                                .withChild(
                                        buildSingleTOCVo("level_3_1", LEVEL, null, "3.1.", "level_3_1_num", null, 2, null, "Content for 3.1")
                                )
                                .withChild(
                                        buildSingleTOCVo("level_3_1_1", LEVEL, null, "3.1.1.", "level_3_1_1_num", null, 3, null, "Content for 3.1.1")
                                )
                                .withChild(
                                        buildSingleTOCVo("level_3_1_2", LEVEL, null, "3.1.2.", "level_3_1_2_num", null, 3, null, "Content for 3.1.2")
                                )

                                .build()
                )
                .build();

        return Arrays.asList(preface, mainBody);
    }

    private TableOfContentItemVO buildSingleTOCVo(String id, String aknTag, String heading, String number, String numberId, String origin, int depth, String list, String content) {
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
                .withList(list)
                .withNode(null)
                .withItemDepth(depth)
                .build();
    }

}
