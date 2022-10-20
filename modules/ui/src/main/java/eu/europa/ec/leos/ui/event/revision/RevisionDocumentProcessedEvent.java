package eu.europa.ec.leos.ui.event.revision;

public class RevisionDocumentProcessedEvent {

    private String documentId;

    public RevisionDocumentProcessedEvent(String documentId) {
        this.documentId = documentId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
