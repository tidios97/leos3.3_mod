package eu.europa.ec.leos.services.document;

import com.google.common.base.Strings;
import eu.europa.ec.leos.domain.common.Result;
import eu.europa.ec.leos.domain.vo.CloneProposalMetadataVO;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.support.XPathCatalog;
import eu.europa.ec.leos.services.support.url.CollectionIdsAndUrlsHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class PostProcessingDocumentService {

    private static final Logger LOG = LoggerFactory.getLogger(PostProcessingDocumentService.class);
    protected final XmlContentProcessor xmlContentProcessor;
    protected final XPathCatalog xPathCatalog;

    @Autowired
    protected PostProcessingDocumentService(XmlContentProcessor xmlContentProcessor, XPathCatalog xPathCatalog) {
        this.xmlContentProcessor = xmlContentProcessor;
        this.xPathCatalog = xPathCatalog;
    }

    public abstract Result<?> processDocument(DocumentVO documentVO);
    public abstract Result<?> saveOriginalProposalIdToClonedProposal(DocumentVO documentVO, String legFileName, String iscRef);
    public abstract Result<?> saveClonedProposalIdToOriginalProposal(DocumentVO documentVO, CollectionIdsAndUrlsHolder idsAndUrlsHolder, CloneProposalMetadataVO cloneProposalMetadataVO);

    public byte[] preserveDocumentReference(byte[] xmlContent) {
        byte[] updatedDocContent = xmlContentProcessor.removeElement(xmlContent, xPathCatalog.getXPathRefOrigin(), true);
        String documentReference = Strings.nullToEmpty(xmlContentProcessor.getElementValue(updatedDocContent, xPathCatalog.getXPathRef(), true));
        updatedDocContent = xmlContentProcessor.replaceElement(updatedDocContent, xPathCatalog.getXPathRef(), true, "<leos:ref></leos:ref>");
        updatedDocContent = xmlContentProcessor.replaceElement(updatedDocContent, xPathCatalog.getXPathRef(), true, "<leos:refOrigin>" + documentReference + "</leos:refOrigin>");
        LOG.info("Moved value '{}' of the filed <leos:ref> to <leos:refOrigin>", documentReference);
        return updatedDocContent;
    }

    public byte[] preserveOriginalDocumentProperties(byte[] xmlContent, String legFileName, String iscRef) {
        String clonedProposalXPath = xPathCatalog.getXPathClonedProposal();
        boolean clonedProposalsPresent = xmlContentProcessor.evalXPath(xmlContent, clonedProposalXPath, true);
        if(clonedProposalsPresent) {
            xmlContent = xmlContentProcessor.removeElement(xmlContent, clonedProposalXPath, true);
        }

        xmlContent = xmlContentProcessor.insertElement(xmlContent, xPathCatalog.getXPathRef(), true
                , "<leos:clonedProposal>true</leos:clonedProposal>");

        String documentReference = Strings.nullToEmpty(xmlContentProcessor.getElementValue(xmlContent, xPathCatalog.getXPathRef(), true));
        String objectId = Strings.nullToEmpty(xmlContentProcessor.getElementValue(xmlContent, xPathCatalog.getXPathObjectId(), true));
        StringBuilder originRefBuilder = buildRefOriginForCloneNode(legFileName, iscRef, documentReference, objectId);
        return xmlContentProcessor.insertElement(xmlContent, xPathCatalog.getXPathRef(), true, originRefBuilder.toString());
    }

    private StringBuilder buildRefOriginForCloneNode(String legFileName, String iscRef, String documentReference, String objectId) {
        StringBuilder originRefBuilder = new StringBuilder();
        originRefBuilder.append("<leos:refOriginForClone ref=\"").append(documentReference).append("\">")
                .append("<originMilestone>").append(legFileName).append("</originMilestone>")
                .append("<iscRef>").append(iscRef).append("</iscRef>")
                .append("<objectId>").append(objectId).append("</objectId>")
                .append("</leos:refOriginForClone>");
        return originRefBuilder;
    }

    public byte[] preserveClonedDocumentProperties(byte[] xmlContent, String clonedDocumentId,
                                                   CloneProposalMetadataVO cloneProposalMetadataVO) {
        boolean clonedProposalsPresent = xmlContentProcessor.evalXPath(xmlContent, xPathCatalog.getXPathClonedProposals(), true);
        String legFileName = cloneProposalMetadataVO.getLegFileName();
        String newClonedRef = getNewClonedRef(clonedDocumentId, cloneProposalMetadataVO, legFileName);
        if (clonedProposalsPresent) {
            boolean milestoneRefPresent = xmlContentProcessor.evalXPath(xmlContent, xPathCatalog.getXPathCPMilestoneRefByNameAttr(legFileName), true);
            if (milestoneRefPresent) {
                return xmlContentProcessor.insertElement(xmlContent, xPathCatalog.getXPathCPMilestoneRefClonedProposalRef(legFileName), true,
                        addClonedProposalRef(clonedDocumentId, cloneProposalMetadataVO));
            } else {
                return xmlContentProcessor.insertElement(xmlContent, xPathCatalog.getXPathCPMilestoneRef(), true, newClonedRef);
            }
        } else {
            return xmlContentProcessor.insertElement(xmlContent, xPathCatalog.getXPathRef(), true,
                    "<leos:clonedProposals>" + newClonedRef + "</leos:clonedProposals>");
        }
    }

    private String getNewClonedRef(String clonedDocumentId, CloneProposalMetadataVO cloneProposalMetadataVO, String legFileName) {
        StringBuilder newClonedRefBuilder = new StringBuilder();
        return newClonedRefBuilder.append("<leos:milestoneRef name=\"").append(legFileName).append("\">")
        .append(addClonedProposalRef(clonedDocumentId, cloneProposalMetadataVO))
        .append("</leos:milestoneRef>").toString();
    }

    private String addClonedProposalRef(String clonedDocumentId, CloneProposalMetadataVO cloneProposalMetadataVO) {
        StringBuilder clonedProposalRef = new StringBuilder();
        return clonedProposalRef.append("<clonedProposalRef ref=\"").append(clonedDocumentId).append("\">")
                .append("<targetUser>").append(cloneProposalMetadataVO.getTargetUser()).append("</targetUser>")
                .append("<creationDate>").append(cloneProposalMetadataVO.getCreationDate()).append("</creationDate>")
                .append("<status>").append(cloneProposalMetadataVO.getRevisionStatus()).append("</status>")
                .append("</clonedProposalRef>").toString();
    }

    public byte[] updateRevisionStatus(byte[] xmlContent, String clonedDocumentId, String revisionStatus) {
        StringBuilder newStatusBuilder = new StringBuilder("<status>");
        newStatusBuilder.append(revisionStatus).append("</status>");
        return xmlContentProcessor.replaceElement(xmlContent,
                xPathCatalog.getXPathStatusByClonedProposalRefAttr(clonedDocumentId),true,
                newStatusBuilder.toString());
    }

    public void updatePostCloneMetadataProperties(String id, CloneProposalMetadataVO cloneProposalMetadataVO) {
    }

    public Result<?> updateOriginalProposalAfterRevisionDone(String cloneProposalRef, String cloneLegFileId,
                                                        CloneProposalMetadataVO cloneProposalMetadataVO) {
        return null;
    }
}
