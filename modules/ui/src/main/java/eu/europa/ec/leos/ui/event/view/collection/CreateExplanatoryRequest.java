package eu.europa.ec.leos.ui.event.view.collection;

public class CreateExplanatoryRequest {
    private String template;

    public CreateExplanatoryRequest(String template) {
        this.template = template;
    }

    public String getTemplate() {
        return template;
    }
}
