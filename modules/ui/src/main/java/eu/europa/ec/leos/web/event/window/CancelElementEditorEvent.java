package eu.europa.ec.leos.web.event.window;

public class CancelElementEditorEvent {

	private String elementId;
	private String elementTagName;

	public String getElementId() {
		return elementId;
	}

	public String getElementTagName() {
		return elementTagName;
	}

	public CancelElementEditorEvent(String elementId, String elementTagName) {
		this.elementId = elementId;
		this.elementTagName = elementTagName;
	}

}
