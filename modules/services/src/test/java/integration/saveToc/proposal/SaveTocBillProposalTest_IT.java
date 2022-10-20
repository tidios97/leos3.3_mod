package integration.saveToc.proposal;

import eu.europa.ec.leos.domain.common.TocMode;
import eu.europa.ec.leos.services.util.TestUtils;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;

import java.util.List;

import static eu.europa.ec.leos.services.support.XmlHelper.BILL;
import static eu.europa.ec.leos.services.TestVOCreatorUtils.getJaneTestUser;
import static org.mockito.Mockito.when;

public abstract class SaveTocBillProposalTest_IT extends SaveTocProposalTest_IT {

    protected List<TableOfContentItemVO> buildTableOfContentBill(byte[] xmlInput) {
        return tableOfContentProcessor.buildTableOfContent(BILL, xmlInput, TocMode.NOT_SIMPLIFIED);
    }

    protected void getStructureFile() {
        docTemplate = "BL-023";
        byte[] bytesFile = TestUtils.getFileContent("/structure-test-bill-EC.xml");
        when(templateStructureService.getStructure(docTemplate)).thenReturn(bytesFile);
    }

    protected byte[] processSaveTocBill(byte[] xmlInput, List<TableOfContentItemVO> toc) {
        byte[] xmlResult = xmlContentProcessor.createDocumentContentWithNewTocList(toc, xmlInput, getJaneTestUser());
        xmlResult = numberService.renumberArticles(xmlResult);
        xmlResult = numberService.renumberRecitals(xmlResult);
        xmlResult = xmlContentProcessor.doXMLPostProcessing(xmlResult);
        return xmlResult;
    }

}
