package eu.europa.ec.leos.ui.event.search;

public class SeachSelectionChanedEvent {
    private final int selectedMatch;

    public SeachSelectionChanedEvent(int selectedMatch) {
        this.selectedMatch = selectedMatch;
    }

    public int getSelectedMatch() {
        return selectedMatch;
    }
}
