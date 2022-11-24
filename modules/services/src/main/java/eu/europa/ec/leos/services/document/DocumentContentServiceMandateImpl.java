/*
 * Copyright 2019 European Commission
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
package eu.europa.ec.leos.services.document;

import eu.europa.ec.leos.domain.cmis.document.Annex;
import eu.europa.ec.leos.domain.cmis.document.Bill;
import eu.europa.ec.leos.domain.cmis.document.Explanatory;
import eu.europa.ec.leos.domain.cmis.document.Memorandum;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.compare.ContentComparatorContext;
import eu.europa.ec.leos.services.compare.ContentComparatorService;
import eu.europa.ec.leos.services.compare.processor.LeosPostDiffingProcessor;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.support.XPathCatalog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static eu.europa.ec.leos.services.compare.ContentComparatorService.ATTR_NAME;
import static eu.europa.ec.leos.services.compare.ContentComparatorService.CONTENT_SOFT_ADDED_CLASS;
import static eu.europa.ec.leos.services.compare.ContentComparatorService.CONTENT_SOFT_REMOVED_CLASS;
import static eu.europa.ec.leos.util.LeosDomainUtil.CMIS_PROPERTY_SPLITTER;

@Service
@Instance(InstanceType.COUNCIL)
public class DocumentContentServiceMandateImpl extends DocumentContentServiceImpl {

	private static final String BASE_EC_VERSION = "0.1.0";
	
    @Autowired
    public DocumentContentServiceMandateImpl(TransformationService transformationService,
                                             ContentComparatorService compareService, AnnexService annexService, BillService billService,
                                             MemorandumService memorandumService, ExplanatoryService explanatoryService,
                                             ProposalService proposalService, XmlContentProcessor xmlContentProcessor, XPathCatalog xPathCatalog) {
        super(transformationService, compareService, annexService, billService, memorandumService, explanatoryService, proposalService,
                xmlContentProcessor, xPathCatalog);
    }

    @Override
    public String toEditableContent(XmlDocument xmlDocument, String contextPath, SecurityContext securityContext, byte[] coverPageContent) {
    	String currentDocumentEditableXml = getEditableXml(xmlDocument, contextPath, securityContext, coverPageContent);
    	
    	if(!isComparisonRequired(xmlDocument)) {
        	return currentDocumentEditableXml;
        }
    	
    	XmlDocument originalDocument = getOriginalDocument(xmlDocument);
    	if(originalDocument == null) {
    		return currentDocumentEditableXml;
    	}
    	
    	String originalDocumentEditableXml = getEditableXml(originalDocument, contextPath, securityContext,
                coverPageContent != null && coverPageContent.length > 0 ? getCoverPageContent(originalDocument.getContent().get().getSource().getBytes()) : coverPageContent);
    	
        LeosPostDiffingProcessor postDiffingProcessor = new LeosPostDiffingProcessor();
        currentDocumentEditableXml = postDiffingProcessor.adjustSoftActionDiffing(currentDocumentEditableXml);
        String result =  compareService.compareContents(new ContentComparatorContext.Builder(originalDocumentEditableXml, currentDocumentEditableXml)
                .withAttrName(ATTR_NAME)
                .withRemovedValue(CONTENT_SOFT_REMOVED_CLASS)
                .withAddedValue(CONTENT_SOFT_ADDED_CLASS)
                .withDisplayRemovedContentAsReadOnly(Boolean.TRUE)
                .build());
        result = postDiffingProcessor.adjustMarkersAuthorialNotes(result);
        result = postDiffingProcessor.adjustSoftRootSubParagraph(result);
        return result;
    }

    @Override
    public boolean isMemorandumComparisonRequired(byte[] contentBytes) {
        return false;
    }

    @Override
    public boolean isProposalComparisonRequired(byte[] contentBytes) {
        return false;
    }

    @Override
    public XmlDocument getOriginalMemorandum(Memorandum memorandum) {
        return memorandumService.findFirstVersion(memorandum.getMetadata().get().getRef());
    }

    @Override
    public XmlDocument getOriginalAnnex(Annex annex) {
        if(StringUtils.isEmpty(annex.getBaseRevisionId())) {
            return annexService.findFirstVersion(annex.getMetadata().get().getRef());
        } else {
            return annexService.findAnnex(annex.getBaseRevisionId().split(CMIS_PROPERTY_SPLITTER)[0], false);
        }
    }

    @Override
    public XmlDocument getOriginalBill(Bill bill) {
        if(StringUtils.isEmpty(bill.getBaseRevisionId())) {
            return billService.findFirstVersion(bill.getMetadata().get().getRef());
        } else {
            return billService.findBill(bill.getBaseRevisionId().split(CMIS_PROPERTY_SPLITTER)[0], false);
        }
    }

    @Override
    public XmlDocument getOriginalExplanatory(Explanatory explanatory) {
    	if(StringUtils.isEmpty(explanatory.getBaseRevisionId())) {
    		return explanatoryService.findFirstVersion(explanatory.getMetadata().get().getRef());
    	} else {
    		return explanatoryService.findExplanatoryVersion(explanatory.getBaseRevisionId().split(CMIS_PROPERTY_SPLITTER)[0]);
    	}
    }
}
