package eu.europa.ec.leos.model.xml;

public class Element {

	private String elementId;
	private String elementTagName;
	private String elementFragment;

	public Element(String elementId, String elementTagName, String elementFragment) {
		this.elementId = elementId;
		this.elementTagName = elementTagName;
		this.elementFragment = elementFragment;
	}

	public String getElementId() {
		return elementId;
	}

	public String getElementTagName() {
		return elementTagName;
	}

	public String getElementFragment() {
		return elementFragment;
	}

}
