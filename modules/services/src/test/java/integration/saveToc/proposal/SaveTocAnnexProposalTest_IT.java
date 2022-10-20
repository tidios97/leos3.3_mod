package integration.saveToc.proposal;

import eu.europa.ec.leos.domain.common.TocMode;
import eu.europa.ec.leos.services.util.TestUtils;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;

import java.util.List;

import static eu.europa.ec.leos.services.support.XmlHelper.DOC;
import static eu.europa.ec.leos.services.TestVOCreatorUtils.getJaneTestUser;
import static org.mockito.Mockito.when;

public abstract class SaveTocAnnexProposalTest_IT extends SaveTocProposalTest_IT {

    protected void getStructureFile() {
        docTemplate = "SG-017";
        byte[] bytesFile = TestUtils.getFileContent("/structure-test-annex-EC.xml");
        when(templateStructureService.getStructure(docTemplate)).thenReturn(bytesFile);
    }

    protected List<TableOfContentItemVO> buildTableOfContentAnnex(byte[] xmlInput) {
        return tableOfContentProcessor.buildTableOfContent(DOC, xmlInput, TocMode.NOT_SIMPLIFIED);
    }

    protected byte[] processSaveTocAnnex(byte[] xmlInput, List<TableOfContentItemVO> toc) {
        byte[] xmlResult = xmlContentProcessor.createDocumentContentWithNewTocList(toc, xmlInput, getJaneTestUser());
        xmlResult = numberService.renumberLevel(xmlResult);
        xmlResult = numberService.renumberParagraph(xmlResult);
        return xmlResult;
    }
}
