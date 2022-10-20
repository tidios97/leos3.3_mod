package eu.europa.ec.leos.web.event.view.document;

public class ReferenceLabelResponseEvent {
    private final String label;
    private final String documentRef;

    public ReferenceLabelResponseEvent(String label, String documentRef) {
        this.label = label;
        this.documentRef = documentRef;
    }

    public String getLabel() {
        return label;
    }

    public String getDocumentRef() {
        return documentRef;
    }
}

