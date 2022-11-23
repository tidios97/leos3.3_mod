package eu.europa.ec.leos.services.numbering;

import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessorMandate;
import eu.europa.ec.leos.services.util.TestUtils;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;

import static eu.europa.ec.leos.services.util.TestUtils.squeezeXml;
import static org.junit.Assert.assertEquals;

public class NumberServiceBillMandateTest extends NumberServiceMandateTest {

    @InjectMocks
    protected XmlContentProcessor xmlContentProcessor = Mockito.spy(new XmlContentProcessorMandate());

    @Test
    public void test_numbering_recitals_cn() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_recitals_cn.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_recitals_cn_expected.xml");
        byte[] result = numberService.renumberRecitals(xmlInput);
        assertEquals(squeezeXml(new String(xmlExpected)), squeezeXml(new String(result)));
    }

    @Test
    public void test_numbering_recitals_onlyCn() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_recitals_onlyCn.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_recitals_onlyCn_expected.xml");
        byte[] result = numberService.renumberRecitals(xmlInput);
        assertEquals(squeezeXml(new String(xmlExpected)), squeezeXml(new String(result)));
    }

    @Test
    public void test_numbering_recitals_cn_withNegatives() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_recitals_cn_withNegatives.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_recitals_cn_withNegatives_expected.xml");
        byte[] result = numberService.renumberRecitals(xmlInput);
        assertEquals(squeezeXml(new String(xmlExpected)), squeezeXml(new String(result)));
    }

    @Test
    public void test_numbering_articles_cn() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_articles_cn.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_articles_cn_expected.xml");
        byte[] result = numberService.renumberArticles(xmlInput);
        assertEquals(squeezeXml(new String(xmlExpected)), squeezeXml(new String(result)));
    }

    @Test
    public void test_numbering_articles_cn_many() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_articles_cn_many.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_articles_cn_many_expected.xml");
        byte[] result = numberService.renumberArticles(xmlInput);
        assertEquals(squeezeXml(new String(xmlExpected)), squeezeXml(new String(result)));
    }

    @Test
    public void test_numbering_article_cn_2Lists() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_article_cn_2Lists.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_article_cn_2Lists_expected.xml");
        byte[] result = numberService.renumberArticles(xmlInput);
        assertEquals(squeezeXml(new String(xmlExpected)), squeezeXml(new String(result)));
    }

    @Test
    public void test_numbering_article_cn_2Lists_withNegatives() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_article_cn_2Lists_withNegatives.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_article_cn_2Lists_withNegatives_expected.xml");
        byte[] result = numberService.renumberArticles(xmlInput);
        assertEquals(squeezeXml(new String(xmlExpected)), squeezeXml(new String(result)));
    }

    @Test
    public void test_numbering_article_cn_2Lists_withNegatives_fullStructure() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_article_cn_2Lists_withNegatives_fullStructure.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_article_cn_2Lists_withNegatives_fullStructure_expected.xml");
        byte[] result = numberService.renumberArticles(xmlInput);
        assertEquals(squeezeXml(new String(xmlExpected)), squeezeXml(new String(result)));
    }

    @Test
    public void test_numbering_article_cn_singleCnPoint_siblingECList() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_article_cn_singleCnPoint_siblingEcList.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_article_cn_singleCnPoint_siblingEcList_expected.xml");
        byte[] result = numberService.renumberArticles(xmlInput);
        assertEquals(squeezeXml(new String(xmlExpected)), squeezeXml(new String(result)));
    }

    @Test
    public void test_numbering_articles_cn_move_paragraphWithList() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_articles_cn_move_paragraphWithList.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_articles_cn_move_paragraphWithList_expected.xml");
        byte[] result = numberService.renumberArticles(xmlInput);
        assertEquals(squeezeXml(new String(xmlExpected)), squeezeXml(new String(result)));
    }

    @Test
    public void test_numbering_paragraph_cn() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_paragraph_cn.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_paragraph_cn_expected.xml");
        byte[] result = numberService.renumberArticles(xmlInput);
        assertEquals(squeezeXml(new String(xmlExpected)), squeezeXml(new String(result)));
    }

    @Test
    public void test_numbering_paragraph_whenPreviousParagraphIndented_cn() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_paragraph_whenPreviousParagraphIndented_cn.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_paragraph_whenPreviousParagraphIndented_cn_expected.xml");
        byte[] result = numberService.renumberArticles(xmlInput);
        assertEquals(squeezeXml(new String(xmlExpected)), squeezeXml(new String(result)));
    }

    @Test
    public void test_outdentPointToParagraph_then_move_bottom_cn() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_outdentPointToParagraph_then_move_bottom_cn.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_outdentPointToParagraph_then_move_bottom_cn_expected.xml");
        byte[] result = numberService.renumberArticles(xmlInput);
        assertEquals(squeezeXml(new String(xmlExpected)), squeezeXml(new String(result)));
    }

    @Test
    public void test_numbering_point_whenPreviousPointIndented_cn() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_point_whenPreviousPointIndented_cn.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_point_whenPreviousPointIndented_cn_expected.xml");
        byte[] result = numberService.renumberArticles(xmlInput);
        assertEquals(squeezeXml(new String(xmlExpected)), squeezeXml(new String(result)));
    }

    @Test
    public void test_outdentPointToUpperLevel_then_move_bottom_cn() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_outdentPointToUpperLevel_then_move_bottom_cn.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_outdentPointToUpperLevel_then_move_bottom_cn_expected.xml");
        byte[] result = numberService.renumberArticles(xmlInput);
        assertEquals(squeezeXml(new String(xmlExpected)), squeezeXml(new String(result)));
    }

    @Test
    public void test_move_differentPointLevels_cn() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_move_differentPointLevels_cn.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_move_differentPointLevels_cn_expected.xml");
        byte[] result = numberService.renumberArticles(xmlInput);
        assertEquals(squeezeXml(new String(xmlExpected)), squeezeXml(new String(result)));
    }

    @Test
    public void test_point_alphaAndRomanNumbers_withBigValues_cn() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_point_alphaAndRomanNumbers_withBigValues_cn.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_point_alphaAndRomanNumbers_withBigValues_cn_expected.xml");
        byte[] result = numberService.renumberArticles(xmlInput);
        assertEquals(squeezeXml(new String(xmlExpected)), squeezeXml(new String(result)));
    }

    @Test
    public void test_numbering_cn_list_added_to_ec_paragraph() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_cn_list_added_to_ec_paragraph.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_cn_list_added_to_ec_paragraph_expected.xml");
        byte[] result = numberService.renumberArticles(xmlInput);
        assertEquals(squeezeXml(new String(xmlExpected)), squeezeXml(new String(result)));
    }

    @Test
    public void test_numbering_nested_list_added() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_nested_list_added.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_nested_list_added_expected.xml");
        byte[] result = numberService.renumberArticles(xmlInput);
        assertEquals(squeezeXml(new String(xmlExpected)), squeezeXml(new String(result)));
    }

    @Test
    public void test_numbering_point_having_single_level_nested_list() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_point_having_single_level_nested_list.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_point_having_single_level_nested_list_expected.xml");
        byte[] result = numberService.renumberArticles(xmlInput);
        assertEquals(squeezeXml(new String(xmlExpected)), squeezeXml(new String(result)));
    }

    @Test
    public void test_numbering_point_having_multi_level_nested_list_1() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_point_having_multi_level_nested_list_1.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_point_having_multi_level_nested_list_1_expected.xml");
        byte[] result = numberService.renumberArticles(xmlInput);
        assertEquals(squeezeXml(new String(xmlExpected)), squeezeXml(new String(result)));
    }

    @Test
    public void test_numbering_point_having_multi_level_nested_list_2() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_point_having_multi_level_nested_list_2.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_point_having_multi_level_nested_list_2_expected.xml");
        byte[] result = numberService.renumberArticles(xmlInput);
        assertEquals(squeezeXml(new String(xmlExpected)), squeezeXml(new String(result)));
    }

    @Test
    public void test_numbering_nested_lists() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_nested_lists.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_nested_lists_expected.xml");
        byte[] result = numberService.renumberArticles(xmlInput);
        assertEquals(squeezeXml(new String(xmlExpected)), squeezeXml(new String(result)));
    }

    @Test
    public void test_numbering_point_added_on_negative_side() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_point_added_on_negative_side.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_point_added_on_negative_side_expected.xml");
        byte[] result = numberService.renumberArticles(xmlInput);
        assertEquals(squeezeXml(new String(xmlExpected)), squeezeXml(new String(result)));
    }

    @Test
    public void test_numbering_point_added_between_ec_point() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_point_added_between_ec_point.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_point_added_between_ec_point_expected.xml");
        byte[] result = numberService.renumberArticles(xmlInput);
        assertEquals(squeezeXml(new String(xmlExpected)), squeezeXml(new String(result)));
    }

    @Test
    public void test_numbering_nested_point_added_to_ec_point() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_nested_point_added_to_ec_point.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_nested_point_added_to_ec_point_expected.xml");
        byte[] result = numberService.renumberArticles(xmlInput);
        assertEquals(squeezeXml(new String(xmlExpected)), squeezeXml(new String(result)));
    }

    @Test
    public void test_numbering_nested_point_added_to_cn_point() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_nested_point_added_to_cn_point.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_nested_point_added_to_cn_point_expected.xml");
        byte[] result = numberService.renumberArticles(xmlInput);
        assertEquals(squeezeXml(new String(xmlExpected)), squeezeXml(new String(result)));
    }

    @Test
    public void test_numbering_point_added_to_unnumbered_paragraph() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_point_added_to_unnumbered_paragraph.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_point_added_to_unnumbered_paragraph_expected.xml");
        byte[] result = numberService.renumberArticles(xmlInput);
        assertEquals(squeezeXml(new String(xmlExpected)), squeezeXml(new String(result)));
    }

    @Test
    public void test_when_point_added_to_unnum_cn_paragraph_input() {
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_CN, "test_when_point_added_to_unnum_cn_paragraph_input.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_CN, "test_when_point_added_to_unnum_cn_paragraph_expected.xml");
        byte[] xmlResult = numberService.renumberArticles(xmlInput);

        String result = squeezeXml(new String(xmlResult));
        String expected = squeezeXml(new String(xmlExpected));
        assertEquals(expected, result);
    }

    @Test
    public void test_numbering_points_are_moved_from_inside_ec() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_points_are_moved_from_inside_ec.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_points_are_moved_from_inside_ec_expected.xml");
        byte[] xmlResult = numberService.renumberArticles(xmlInput);

        String result = squeezeXml(new String(xmlResult));
        String expected = squeezeXml(new String(xmlExpected));
        assertEquals(expected, result);
    }

    @Test
    public void test_auto_renumbering_for_cn() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_renumbering_recitals_articles_paragraph_pointa.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_renumbering_recitals_articles_paragraph_pointa_expected.xml");
        byte[] result = xmlContentProcessor.prepareForRenumber(xmlInput);
        result = numberService.renumberRecitals(result);
        result = numberService.renumberArticles(result);
        assertEquals(squeezeXml(new String(xmlExpected)), squeezeXml(new String(result)));
    }

}
