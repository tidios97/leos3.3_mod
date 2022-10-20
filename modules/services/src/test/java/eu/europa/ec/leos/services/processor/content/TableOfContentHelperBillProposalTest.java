package eu.europa.ec.leos.services.processor.content;

import eu.europa.ec.leos.domain.common.TocMode;
import eu.europa.ec.leos.services.util.TestUtils;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.StructureConfigUtils;
import eu.europa.ec.leos.vo.toc.TocItemVOBuilder;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static eu.europa.ec.leos.services.support.XmlHelper.ARTICLE;
import static eu.europa.ec.leos.services.support.XmlHelper.BILL;
import static eu.europa.ec.leos.services.support.XmlHelper.CITATION;
import static eu.europa.ec.leos.services.support.XmlHelper.CN;
import static eu.europa.ec.leos.services.support.XmlHelper.CONCLUSIONS;
import static eu.europa.ec.leos.services.support.XmlHelper.EC;
import static eu.europa.ec.leos.services.support.XmlHelper.FORMULA;
import static eu.europa.ec.leos.services.support.XmlHelper.PREFACE;
import static eu.europa.ec.leos.services.support.XmlHelper.RECITAL;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class TableOfContentHelperBillProposalTest extends TableOfXmlContentProcessorTest {

    @Override
    protected void getStructureFile() {
        docTemplate = "BL-023";
        configFile = "/structure-test-bill-EC.xml";
    }

    @Test
    public void test_buildTableOfContent() {
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/bill_basic.xml");

        List<TableOfContentItemVO> xercesTOC = tableOfContentProcessor.buildTableOfContent(BILL, fileContent, TocMode.NOT_SIMPLIFIED);
        assertThat(xercesTOC, is(notNullValue()));
        assertThat(xercesTOC.size(), is(4));

        List<TableOfContentItemVO> expectedTOC = buildTOCProgrammatically();

        compareTOCs(expectedTOC, xercesTOC, false);
    }

    private List<TableOfContentItemVO> buildTOCProgrammatically() {
        TableOfContentItemVO preface = buildSingleTOCVo("preface", PREFACE, null, null, null, null, "");

        TableOfContentItemVO preamble = TocItemVOBuilder.getBuilder()
                .withId("preamble")
                .withTocItem(
                        StructureConfigUtils.getTocItemByName(tocItems, "preamble")
                )
                .withContent("")
                .withChild(
                        buildSingleTOCVo("preamble__formula_1", FORMULA, null, null, null, null,
                                "THE EUROPEAN PARLIAMENT AND THE COUNCIL OF THE EUROPEAN UNION,")
                )
                .withChild(TocItemVOBuilder.getBuilder()
                        .withId("cits")
                        .withTocItem(
                                StructureConfigUtils.getTocItemByName(tocItems, "citations")
                        )
                        .withChild(
                                buildSingleTOCVo("cit_1", CITATION, null, null, null, null, "Citation 1 content with newLine")
                        )
                        .withChild(
                                buildSingleTOCVo("cit_2", CITATION, null, null, null, null, "Citation 2 content with AuthorialNote Inside Authorial note ,")
                        )
                        .withContent("")
                        .withNode(null)
                        .withItemDepth(0)
                        .build()
                )
                .withChild(TocItemVOBuilder.getBuilder()
                        .withId("recs")
                        .withTocItem(
                                StructureConfigUtils.getTocItemByName(tocItems, "recitals")
                        )
                        .withChild(
                                buildSingleTOCVo("rec_1", RECITAL, null, "(1)", "rec_1_num", null, "Recital 1 content")
                        )
                        .withChild(
                                buildSingleTOCVo("rec_2", RECITAL, null, "(2)", "rec_2_num", null, "Recital 2 content with special character Larosière")
                        )
                        .withContent("")
                        .withNode(null)
                        .withItemDepth(0)
                        .build()
                )
                .withChild(
                        buildSingleTOCVo("preamble__formula_2", FORMULA, null, null, null, null, "HAVE ADOPTED THIS REGULATION:")
                )
                .withNode(null)
                .withParentItem(null)
                .withItemDepth(0)
                .build();

        TableOfContentItemVO body = TocItemVOBuilder.getBuilder()
                .withId("body")
                .withTocItem(
                        StructureConfigUtils.getTocItemByName(tocItems, "body")
                )
                .withContent("")
                .withChild(TocItemVOBuilder.getBuilder()
                        .withId("akn_part_htJBP6")
                        .withTocItem(
                                StructureConfigUtils.getTocItemByName(tocItems, "part")
                        )
                        .withHeading("100 [...] Articles(bold Article) text in header")
                        .withContent("Article 1 Article 1 Heading 1. Article 1 paragraph 1 content 2. Article 1 paragraph 2 content")
                        .withChild(
                                buildSingleTOCVo("art_1", ARTICLE, "Article 1 Heading", "1", "art_1_num", CN, "1. Article 1 paragraph 1 content")
                        )
                        .withChild(
                                buildSingleTOCVo("art_2", ARTICLE, "Article 2 Heading", "2", "art_2_num", EC,
                                        "1. Sub paragraph -- of Paragraph 1 Article 2 (a) point (a) (b) point (b) (c) point (c) content. This is alinea (i) point (i) content (ii) point (i) content (1) point (1) content. This is alinea (2) point (2) content - point - (first indent) content. this is alinea point - (first indent) content. this is alinea (3) point (3) content (iii) point (iii) content")
                        )
                        .withChild(
                                buildSingleTOCVo("art_3", ARTICLE, "Article 3 Heading", "3", "art_3_num", null, "Article 3 first paragraph (unnumbered) content")
                        )
                        .withNumber("I")
                        .withNode(null)
                        .withItemDepth(1)
                        .withElementNumberId("akn_BE55oX")
                        .build()
                )
                .withNode(null)
                .withParentItem(null)
                .withItemDepth(0)
                .build();

        TableOfContentItemVO conclusions = buildSingleTOCVo("conclusions", CONCLUSIONS, null, null, null, null,
                "Done at Brussels, For the European Parliament The President [...] For the Council The President [...]");

        return Arrays.asList(preface, preamble, body, conclusions);
    }

    private TableOfContentItemVO buildSingleTOCVo(String id, String aknTag, String heading, String number, String numberId, String origin, String content) {
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
                .withNode(null)
                .withItemDepth(0)
                .build();
    }

}
