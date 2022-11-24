package eu.europa.ec.leos.ui.event;

public class ToggleLiveDiffingRequiredEvent {
	private boolean liveDiffingRequired;

	public ToggleLiveDiffingRequiredEvent(boolean liveDiffingRequired) {
		super();
		this.liveDiffingRequired = liveDiffingRequired;
	}
	public boolean isLiveDiffingRequired() {
		return liveDiffingRequired;
	}
}