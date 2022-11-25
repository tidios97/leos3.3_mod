/*
 * Copyright 2018 European Commission
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

import com.google.common.base.Strings;
import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.document.Annex;
import eu.europa.ec.leos.domain.cmis.document.Bill;
import eu.europa.ec.leos.domain.cmis.document.Explanatory;
import eu.europa.ec.leos.domain.cmis.document.Memorandum;
import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.security.LeosPermission;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.compare.ContentComparatorService;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.support.XPathCatalog;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public abstract class DocumentContentServiceImpl implements DocumentContentService {

    protected static final Logger LOG = LoggerFactory.getLogger(DocumentContentServiceImpl.class);

    protected TransformationService transformationService;
    protected ContentComparatorService compareService;
    protected AnnexService annexService;
    protected BillService billService;
    protected MemorandumService memorandumService;
    protected ExplanatoryService explanatoryService;
    protected ProposalService proposalService;
    protected XmlContentProcessor xmlContentProcessor;
    protected final XPathCatalog xPathCatalog;

    @Autowired
    public DocumentContentServiceImpl(TransformationService transformationService,
                                      ContentComparatorService compareService, AnnexService annexService,
                                      BillService billService, MemorandumService memorandumService, ExplanatoryService explanatoryService,
                                      ProposalService proposalService, XmlContentProcessor xmlContentProcessor, XPathCatalog xPathCatalog) {
        this.transformationService = transformationService;
        this.compareService = compareService;
        this.annexService = annexService;
        this.billService = billService;
        this.memorandumService = memorandumService;
        this.explanatoryService = explanatoryService;
        this.proposalService = proposalService;
        this.xmlContentProcessor = xmlContentProcessor;
        this.xPathCatalog = xPathCatalog;
    }
    
    protected boolean isComparisonRequired(XmlDocument xmlDocument, SecurityContext securityContext) {
    	byte[] contentBytes = xmlDocument.getContent().get().getSource().getBytes();
    	switch (xmlDocument.getCategory()) {
	        case MEMORANDUM:
	        	return isMemorandumComparisonRequired(contentBytes);
	        case COUNCIL_EXPLANATORY:
	            return isCouncilExplanatoryComparisonRequired((Explanatory) xmlDocument, securityContext);
	        case ANNEX:
	            return isAnnexComparisonRequired(contentBytes);
	        case BILL:
	            return true;
	        case PROPOSAL:
	        	return true;
	        default:
	            throw new UnsupportedOperationException("No transformation supported for this category");
	    }
    }
    
    protected XmlDocument getOriginalDocument(XmlDocument xmlDocument) {
    	switch (xmlDocument.getCategory()) {
	        case MEMORANDUM:
	        	return getOriginalMemorandum((Memorandum) xmlDocument);
	        case COUNCIL_EXPLANATORY:
	        	return getOriginalExplanatory((Explanatory) xmlDocument);
	        case ANNEX:
	        	return getOriginalAnnex((Annex) xmlDocument);
	        case BILL:
	        	return getOriginalBill((Bill) xmlDocument);
	        case PROPOSAL:
	        	return getOriginalProposal((Proposal) xmlDocument);
	        default:
	            throw new UnsupportedOperationException("No transformation supported for this category");
	    }
    }
    
    protected boolean isSameDocument(XmlDocument xmlDocument, XmlDocument originalDocument) {
    	return originalDocument != null 
    			&& StringUtils.equals(originalDocument.getId(), xmlDocument.getId()) 
    			&& StringUtils.equals(originalDocument.getVersionLabel(), xmlDocument.getVersionLabel());
    }

    protected String[] getContentsToCompare(XmlDocument xmlDocument, String contextPath, SecurityContext securityContext,
                                            byte[] coverPageContent) {
        String currentDocumentEditableXml = getEditableXml(xmlDocument, contextPath, securityContext, coverPageContent);
        XmlDocument originalDocument;
        byte[] contentBytes;

        switch (xmlDocument.getCategory()) {
            case MEMORANDUM:
                contentBytes = xmlDocument.getContent().get().getSource().getBytes();
                if (isMemorandumComparisonRequired(contentBytes)) {
                    originalDocument = getOriginalMemorandum((Memorandum) xmlDocument);
                } else {
                    return new String[]{currentDocumentEditableXml};
                }
                break;
            case COUNCIL_EXPLANATORY:
                contentBytes = xmlDocument.getContent().get().getSource().getBytes();
                if (isCouncilExplanatoryComparisonRequired((Explanatory) xmlDocument, securityContext)) {
                    originalDocument = getOriginalExplanatory((Explanatory) xmlDocument);
                } else {
                    return new String[]{currentDocumentEditableXml};
                }
                break;
            case ANNEX:
                contentBytes = xmlDocument.getContent().get().getSource().getBytes();
                if (isAnnexComparisonRequired(contentBytes)) {
                    originalDocument = getOriginalAnnex((Annex) xmlDocument);
                } else {
                    return new String[]{currentDocumentEditableXml};
                }
                break;
            case BILL:
                originalDocument = getOriginalBill((Bill) xmlDocument);
                break;
            case PROPOSAL:
                originalDocument = getOriginalProposal((Proposal) xmlDocument);
                break;
            default:
                throw new UnsupportedOperationException("No transformation supported for this category");
        }
        String originalDocumentEditableXml = getEditableXml(originalDocument, contextPath, securityContext,
                coverPageContent != null && coverPageContent.length > 0 ? getCoverPageContent(originalDocument.getContent().get().getSource().getBytes()) : coverPageContent);
        return new String[]{currentDocumentEditableXml, originalDocumentEditableXml};
    }

    protected String getEditableXml(XmlDocument xmlDocument, String contextPath, SecurityContext securityContext,
                                    byte[] coverPageContent) {
        return transformationService.toEditableXml(getContentInputStream(xmlDocument), contextPath, xmlDocument.getCategory(),
                securityContext.getPermissions(xmlDocument), getContentInputStream(coverPageContent));
    }

    @Override
    public XmlDocument getOriginalMemorandum(Memorandum memorandum) {
        return memorandumService.findFirstVersion(memorandum.getMetadata().get().getRef());
    }

    @Override
    public XmlDocument getOriginalProposal(Proposal proposal) {
        return proposalService.findFirstVersion(proposal.getMetadata().get().getRef());
    }

    @Override
    public XmlDocument getOriginalExplanatory(Explanatory explanatory) {
        return explanatoryService.findFirstVersion(explanatory.getMetadata().get().getRef());
    }

    @Override
    public XmlDocument getOriginalAnnex(Annex annex) {
        return annexService.findFirstVersion(annex.getMetadata().get().getRef());
    }

    @Override
    public XmlDocument getOriginalBill(Bill bill) {
        return billService.findFirstVersion(bill.getMetadata().get().getRef());
    }

    @Override
    public boolean isAnnexComparisonRequired(byte[] contentBytes) {
        return xmlContentProcessor.isAnnexComparisonRequired(contentBytes);
    }

    @Override
    public String getDocumentAsHtml(XmlDocument xmlDocument, String contextPath, List<LeosPermission> permissions) {
        return this.getDocumentAsHtml(xmlDocument, contextPath, permissions, false);
    }

    @Override
    public String getDocumentAsHtml(XmlDocument xmlDocument, String contextPath, List<LeosPermission> permissions, boolean includeCoverPage) {
        return transformationService.formatToHtml(xmlDocument, contextPath, permissions,
                        includeCoverPage ? new ByteArrayInputStream(getCoverPageContent(xmlDocument.getContent().get().getSource().getBytes()))
                                : null)
                .replaceAll("(?i)(href|onClick)=\".*?\"", "");
    }

    @Override
    public String getCleanDocumentAsHtml(XmlDocument xmlDocument, String contextPath, List<LeosPermission> permissions) {
        return this.getCleanDocumentAsHtml(xmlDocument, contextPath, permissions, false);
    }

    @Override
    public String getCleanDocumentAsHtml(XmlDocument xmlDocument, String contextPath, List<LeosPermission> permissions, boolean includeCoverPage) {
        byte[] xmlContent = xmlContentProcessor.cleanSoftActions(xmlDocument.getContent().get().getSource().getBytes());
        return transformationService.formatToHtml(new ByteArrayInputStream(xmlContent), contextPath, permissions,
                        includeCoverPage ? new ByteArrayInputStream(getCoverPageContent(xmlDocument.getContent().get().getSource().getBytes()))
                                : null)
                .replaceAll("(?i)(href|onClick)=\".*?\"", "");
    }

    @Override
    public String getDocumentForContributionAsHtml(byte[] content, String contextPath, List<LeosPermission> permissions) {
        return getDocumentForContributionAsHtml(content, contextPath, permissions, false);
    }

    @Override
    public String getDocumentForContributionAsHtml(byte[] content, String contextPath, List<LeosPermission> permissions, boolean includeCoverPage) {
        content = xmlContentProcessor.cleanMiscAttributes(content);
        return transformationService.formatToHtml(new ByteArrayInputStream(content), contextPath, permissions,
                        includeCoverPage ? new ByteArrayInputStream(getCoverPageContent(content))
                                : null)
                .replaceAll("(?i)(href|onClick)=\".*?\"", "");
    }

    @Override
    public byte[] getCoverPageContent(byte[] xmlContent) {
        String coverPageContent = "";
        String coverPageXPath = xPathCatalog.getXPathCoverPage();
        boolean coverPagePresent = xmlContentProcessor.evalXPath(xmlContent, coverPageXPath, true);
        if (coverPagePresent) {
            coverPageContent = Strings.nullToEmpty(xmlContentProcessor.getElementFragmentByPath(xmlContent,
                    coverPageXPath, true));
        }
        return coverPageContent.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public boolean isCoverPageExists(byte[] xmlContent) {
        String coverPageXPath = xPathCatalog.getXPathCoverPage();
        boolean coverPagePresent = xmlContentProcessor.evalXPath(xmlContent, coverPageXPath, true);
        return coverPagePresent;
    }

    @Override
    public byte[] getOriginalContentToCompare(XmlDocument xmlDocument) {
        byte[] contentBytes;
        XmlDocument originalDocument;
        switch (xmlDocument.getCategory()) {
            case MEMORANDUM:
                contentBytes = xmlDocument.getContent().get().getSource().getBytes();
                if (isMemorandumComparisonRequired(contentBytes)) {
                    originalDocument = getOriginalMemorandum((Memorandum) xmlDocument);
                    contentBytes = getContent(originalDocument);
                }
                break;
            case ANNEX:
                contentBytes = xmlDocument.getContent().get().getSource().getBytes();
                if (isAnnexComparisonRequired(contentBytes)) {
                    originalDocument = getOriginalAnnex((Annex) xmlDocument);
                    contentBytes = getContent(originalDocument);
                }
                break;
            case BILL:
                originalDocument = getOriginalBill((Bill) xmlDocument);
                contentBytes = getContent(originalDocument);
                break;
            case PROPOSAL:
                contentBytes = xmlDocument.getContent().get().getSource().getBytes();
                if (isProposalComparisonRequired(contentBytes)) {
                    originalDocument = getOriginalProposal((Proposal) xmlDocument);
                    contentBytes = getCoverPageContent(getContent(originalDocument));
                }
                break;
            default:
                throw new UnsupportedOperationException("Category not supported");
        }
        return contentBytes;
    }

    private byte[] getContent(XmlDocument xmlDocument) {
        final Content content = xmlDocument.getContent().getOrError(() -> "Document content is required!");
        return content.getSource().getBytes();
    }

    @Override
    public boolean isCouncilExplanatoryComparisonRequired(Explanatory explanatory, SecurityContext securityContext) {
        return securityContext.hasPermission(explanatory, LeosPermission.CAN_TOGGLE_LIVE_DIFFING) && explanatory.isLiveDiffingRequired();
    }

}
