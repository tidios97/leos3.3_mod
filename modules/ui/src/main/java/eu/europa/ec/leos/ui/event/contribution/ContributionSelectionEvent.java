package eu.europa.ec.leos.ui.event.contribution;

public class ContributionSelectionEvent {

    Boolean selected;

    public ContributionSelectionEvent(Boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }
}
