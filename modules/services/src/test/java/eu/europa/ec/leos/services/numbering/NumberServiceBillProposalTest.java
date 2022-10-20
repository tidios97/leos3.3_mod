package eu.europa.ec.leos.services.numbering;

import eu.europa.ec.leos.services.util.TestUtils;
import org.junit.Test;

import static eu.europa.ec.leos.services.util.TestUtils.squeezeXmlAndRemoveAllNS;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class NumberServiceBillProposalTest extends NumberServiceProposalTest {

    @Test
    public void test_numbering_recitals() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_recitals.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_recitals_expected.xml");
        byte[] result = numberService.renumberRecitals(xmlInput);
        assertEquals(squeezeXmlAndRemoveAllNS(new String(xmlExpected)), squeezeXmlAndRemoveAllNS(new String(result)));
    }

    @Test
    public void test_numbering_recitals_clonedProposal() {
        when(cloneContext.isClonedProposal()).thenReturn(true);

        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_recitals_clonedProposal.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_recitals_clonedProposal_expected.xml");
        byte[] result = numberService.renumberRecitals(xmlInput);
        assertEquals(squeezeXmlAndRemoveAllNS(new String(xmlExpected)), squeezeXmlAndRemoveAllNS(new String(result)));
    }

    @Test
    public void test_numbering_articles() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_articles.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_articles_expected.xml");
        byte[] result = numberService.renumberArticles(xmlInput);
        assertEquals(squeezeXmlAndRemoveAllNS(new String(xmlExpected)), squeezeXmlAndRemoveAllNS(new String(result)));
    }

    @Test
    public void test_numbering_articles_with_soft_attributes() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_articles_with_soft_attr.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_articles_with_soft_attr_expected.xml");
        byte[] result = numberService.renumberArticles(xmlInput);
        assertEquals(squeezeXmlAndRemoveAllNS(new String(xmlExpected)), squeezeXmlAndRemoveAllNS(new String(result)));
    }

    @Test
    public void test_numbering_articles_with_soft_attributes_with_1st_element() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_articles_with_soft_attr_1st_element.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_articles_with_soft_attr_1st_element_expected.xml");
        byte[] result = numberService.renumberArticles(xmlInput);
        assertEquals(squeezeXmlAndRemoveAllNS(new String(xmlExpected)), squeezeXmlAndRemoveAllNS(new String(result)));
    }

    @Test
    public void test_numbering_article_importFromOJ_ec() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX_OJ, "test_numbering_article_importFromOJ_ec.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX_OJ, "test_numbering_article_importFromOJ_ec_expected.xml");
        String result = numberService.renumberImportedArticle(new String(xmlInput), null);
        assertEquals(squeezeXmlAndRemoveAllNS(new String(xmlExpected)), squeezeXmlAndRemoveAllNS(result));
    }

    @Test
    public void test_numbering_article_definition_importFromOJ_ec() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX_OJ, "test_numbering_article_definition_importFromOJ_ec.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX_OJ, "test_numbering_article_definition_importFromOJ_ec_expected.xml");
        String result = numberService.renumberImportedArticle(new String(xmlInput), null);
        assertEquals(squeezeXmlAndRemoveAllNS(new String(xmlExpected)), squeezeXmlAndRemoveAllNS(result));
    }
}
