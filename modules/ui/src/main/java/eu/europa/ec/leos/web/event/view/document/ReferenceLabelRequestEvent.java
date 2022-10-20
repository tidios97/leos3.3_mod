package eu.europa.ec.leos.web.event.view.document;

import java.util.List;

public class ReferenceLabelRequestEvent {
    private final List<String> references;
    private final String currentElementID;
    private final String documentRef;
    private final boolean capital;
    
    public ReferenceLabelRequestEvent(List<String> references, String currentElementID, String documentRef, boolean capital) {
        this.references = references;
        this.currentElementID = currentElementID;
        this.documentRef = documentRef;
        this.capital = capital;
    }

    public List<String> getReferences() {
        return references;
    }
    
    public String getCurrentElementID() {
        return currentElementID;
    }

    public String getDocumentRef() {
        return documentRef;
    }

    public boolean isCapital() {
        return capital;
    }

}

