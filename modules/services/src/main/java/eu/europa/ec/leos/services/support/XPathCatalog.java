package eu.europa.ec.leos.services.support;

import org.springframework.stereotype.Component;

@Component
public class XPathCatalog {

    public static final String NAMESPACE_AKN_NAME = "akn";
    public static final String NAMESPACE_AKN_URI = "http://docs.oasis-open.org/legaldocml/ns/akn/3.0";

    public static String getXPathElement(String element) {
        return "//akn:" + element;
    }

    public String getXPathRefOrigin() {
        return "/akn:akomaNtoso//akn:meta/akn:proprietary/leos:refOrigin";
    }

    public String getXPathRefOriginForCloneRefAttr() {
        return "/akn:akomaNtoso//akn:meta/akn:proprietary/leos:refOriginForClone/@ref";
    }

    public String getXPathRefOriginForCloneOriginalMilestone() {
        return "/akn:akomaNtoso//akn:meta/akn:proprietary/leos:refOriginForClone/akn:originMilestone";
    }

    public String getXPathRefOriginForCloneIscRef() {
        return "/akn:akomaNtoso//akn:meta/akn:proprietary/leos:refOriginForClone/akn:iscRef";
    }

    public String getXPathRefOriginForCloneObjectId() {
        return "/akn:akomaNtoso//akn:meta/akn:proprietary/leos:refOriginForClone/akn:objectId";
    }

    public String getXPathDocumentRefForExplanatory() {
        return "//akn:akomaNtoso/akn:documentCollection/akn:collectionBody/akn:component[@refersTo=\"#council_explanatory\" or @refersTo=\"#_council_explanatory\"]/akn:documentRef";
    }

    public String getXPathDocumentRefByHrefAttrFromProposal(String elementRef) {
        return String.format("//akn:akomaNtoso/akn:documentCollection/akn:collectionBody/akn:component/akn:documentRef[@href=\"%s\"]", elementRef);
    }

    public String getXPathAttachments() {
        return "//akn:attachments";
    }

    public String getXPathDocumentRef() {
        return "//akn:attachments/akn:attachment/akn:documentRef";
    }

    public String getXPathDocumentRefByHrefAttr(String elementRef) {
        return String.format("//akn:attachments/akn:attachment/akn:documentRef[@href=\"%s\"]", elementRef);
    }

    public String getXPathObjectId() {
        return "/akn:akomaNtoso//akn:meta/akn:proprietary/leos:objectId";
    }

    public String getXPathRef() {
        return "/akn:akomaNtoso//akn:meta/akn:proprietary/leos:ref";
    }

    public String getXPathLastElement(String tagName) {
        return "//akn:" + tagName + "[last()]";
    }

    public String getXPathHeading() {
        return "//akn:heading[1]";
    }

    public String getXPathDocTemplate() {
        return "/akn:akomaNtoso//akn:meta/akn:proprietary/leos:docTemplate";
    }

    public String getXPathRelevantElements() {
        return "/akn:akomaNtoso//akn:meta/akn:proprietary/leos:relevantElements";
    }

    public String getXPathComments() {
        return "/akn:akomaNtoso//akn:meta/akn:proprietary/leos:comments";
    }

    public String getXPathClonedProposals() {
        return "/akn:akomaNtoso//akn:meta/akn:proprietary/leos:clonedProposals";
    }

    public String getXPathClonedProposal() {
        return "/akn:akomaNtoso//akn:meta/akn:proprietary/leos:clonedProposal";
    }

    public String getXPathCPMilestoneRef() {
        return "/akn:akomaNtoso//akn:meta/akn:proprietary/leos:clonedProposals/leos:milestoneRef";
    }

    public String getXPathCPMilestoneRefByNameAttr(String legFileName) {
        return "/akn:akomaNtoso//akn:meta/akn:proprietary/leos:clonedProposals/leos:milestoneRef[@name=\"" + legFileName + "\"]";
    }

    public String getXPathCPMilestoneRefClonedProposalRef(String legFileName) {
        return "/akn:akomaNtoso//akn:meta/akn:proprietary/leos:clonedProposals/leos:milestoneRef[@name=\"" + legFileName + "\"]/akn:clonedProposalRef";
    }

    public String getXPathCPMilestoneRefClonedProposalRefByRefAttr(String legFileName, String clonedProposalId) {
        return "/akn:akomaNtoso//akn:meta/akn:proprietary/leos:clonedProposals/leos:milestoneRef[@name=\"" + legFileName + "\"]/akn:clonedProposalRef[@ref=\"" + clonedProposalId + "\"]";
    }

    public String getXPathStatusByClonedProposalRefAttr(String clonedProposalId) {
        return "/akn:akomaNtoso//akn:meta/akn:proprietary/leos:clonedProposals/leos:milestoneRef/akn:clonedProposalRef[@ref=\"" + clonedProposalId + "\"]/akn:status";
    }

    public String getXPathDocType() {
        return "/akn:akomaNtoso//akn:meta/akn:references/akn:TLCReference[@name=\"docType\"]";
    }

    public String getXPathCoverPage() {
        return "/akn:akomaNtoso//../akn:coverPage";
    }

    public String getXPathMeta() {
        return "/akn:akomaNtoso//akn:meta";
    }

    public String getXPathAkomaNtoso() {
        return "/akn:akomaNtoso";
    }

    public String getXPathAkomaNtosoFirstChild() {
        return "//akn:akomaNtoso/akn:*[1]";
    }

    public String getXPathProposalDocCollection() {
        return "//akn:documentCollection/@name";
    }

    public String getXPathElementWithSoftAction() {
        return "//akn:*[@leos:softaction]";
    }

    public static String removeNamespaceFromXml(String xml) {
        return xml.replaceAll(NAMESPACE_AKN_NAME + ":", "");
    }


}
