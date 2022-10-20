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

import static eu.europa.ec.leos.services.TestVOCreatorUtils.getJaneTestUser;
import static eu.europa.ec.leos.services.support.XmlHelper.BILL;
import static org.mockito.Mockito.when;

import java.util.List;

import eu.europa.ec.leos.domain.common.TocMode;
import eu.europa.ec.leos.services.util.TestUtils;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;

public abstract class SaveTocBillMandateTest_IT extends SaveTocMandateTest_IT {

    protected List<TableOfContentItemVO> buildTableOfContentBill(byte[] xmlInput) {
        return tableOfContentProcessor.buildTableOfContent(BILL, xmlInput, TocMode.NOT_SIMPLIFIED);
    }

    protected void getStructureFile() {
        docTemplate = "BL-023";
        byte[] bytesFile = TestUtils.getFileContent("/structure-test-bill-CN.xml");
        when(templateStructureService.getStructure(docTemplate)).thenReturn(bytesFile);
    }

    protected byte[] processSaveTocBill(byte[] xmlInput, List<TableOfContentItemVO> toc) {
        byte[] xmlResult = xmlContentProcessor.createDocumentContentWithNewTocList(toc, xmlInput, getJaneTestUser());
        //xmlResult = numberService.renumberArticles(xmlResult);
        //xmlResult = numberService.renumberRecitals(xmlResult);
        //xmlResult = xmlContentProcessor.doXMLPostProcessing(xmlResult);
        return xmlResult;
    }

}
