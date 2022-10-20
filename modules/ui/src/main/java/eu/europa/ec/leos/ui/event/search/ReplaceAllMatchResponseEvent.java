package eu.europa.ec.leos.ui.event.search;

public class ReplaceAllMatchResponseEvent {

    private final boolean isReplaced;

    public ReplaceAllMatchResponseEvent(boolean isReplaced) {
        this.isReplaced = isReplaced;
    }

    public boolean isReplaced() {
        return isReplaced;
    }
}
