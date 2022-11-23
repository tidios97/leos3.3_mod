package eu.europa.ec.leos.ui.event.revision;

public class OpenAndViewContibutionEvent {
    private String document;
    private String version;

    public OpenAndViewContibutionEvent(String selectedDocument, String version) {
        this.document = selectedDocument;
        this.version = version;
    }

    public String getDocument() {
        return document;
    }

    public String getVersion() {
        return version;
    }
}
