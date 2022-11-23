package eu.europa.ec.leos.ui.event.view.collection;

public class CreateSupportDocumentRequest {
    private String template;

    public CreateSupportDocumentRequest(String template) {
        this.template = template;
    }

    public String getTemplate() {
        return template;
    }
}
