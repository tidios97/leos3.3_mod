package eu.europa.ec.leos.vo.contribution;

import eu.europa.ec.leos.domain.cmis.document.XmlDocument;

public class ContributionLegDocumentVO<T extends XmlDocument> {

    private String originRef;
    private T document;
    private byte[] content;
    private String legFileName;
    private String documentName;

    public ContributionLegDocumentVO(String originRef, T document, byte[] content, String legFileName,
                                     String documentName) {
        this.originRef = originRef;
        this.document = document;
        this.content = content;
        this.legFileName = legFileName;
        this.documentName = documentName;
    }

    public String getOriginRef() {
        return originRef;
    }

    public void setOriginRef(String originRef) {
        this.originRef = originRef;
    }

    public T getDocument() {
        return document;
    }

    public void setDocument(T document) {
        this.document = document;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getLegFileName() {
        return legFileName;
    }

    public void setLegFileName(String legFileName) {
        this.legFileName = legFileName;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }
}
