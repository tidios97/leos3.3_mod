package integration.saveToc.proposal;

import eu.europa.ec.leos.services.util.TestUtils;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static eu.europa.ec.leos.services.util.TestUtils.squeezeXmlAndRemoveAllNS;
import static integration.saveToc.TocVOCreateLegalServiceUtils.createMoveFromElement;
import static integration.saveToc.TocVOCreateLegalServiceUtils.createMoveToElement;
import static integration.saveToc.TocVOCreateUtils.getElementById;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class SaveTocBillMoveClonedProposalTest_IT extends SaveTocBillProposalTest_IT {

    private static final Logger log = LoggerFactory.getLogger(SaveTocBillMoveClonedProposalTest_IT.class);

    @Before
    public void onSetUp() throws Exception {
        super.onSetUp();
        when(cloneContext.isClonedProposal()).thenReturn(true);
    }

    @Test
    public void test_clone_proposal_move__article_top_outsideChapter() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "bill_with2Article.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_clone_proposal_move__article_top__expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO body = getElementById(toc, "body");
        TableOfContentItemVO chapter = getElementById(toc, "chapter_1");

        TableOfContentItemVO originalArticle = getElementById(toc, "art_2");
        TableOfContentItemVO moveToArticle = createMoveToElement(originalArticle);
        TableOfContentItemVO moveFromArticle = createMoveFromElement(originalArticle);

        chapter.getChildItems().remove(originalArticle);
        chapter.getChildItems().add(moveToArticle);
        body.getChildItems().add(0, moveFromArticle);

        // When
        byte[] xmlResult = processSaveTocBill(xmlInput, toc);

        // Then
        String result = new String(xmlResult);
        String expected = new String(xmlExpected);
        result = squeezeXmlAndRemoveAllNS(result);
        expected = squeezeXmlAndRemoveAllNS(expected);
        assertEquals(expected, result);
    }

}