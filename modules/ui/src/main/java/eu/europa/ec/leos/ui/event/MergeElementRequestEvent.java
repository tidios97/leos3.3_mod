package eu.europa.ec.leos.ui.event;

public class MergeElementRequestEvent {

    private String elementId;
    private String elementTagName;
    private String elementContent;

    public MergeElementRequestEvent(String elementId, String elementTagName, String elementContent) {
        this.elementId = elementId;
        this.elementTagName = elementTagName;
        this.elementContent = elementContent;
    }

    public String getElementId() {
        return elementId;
    }

    public String getElementTagName() {
        return elementTagName;
    }

    public String getElementContent() {
        return elementContent;
    }

}
