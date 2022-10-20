package eu.europa.ec.leos.ui.event.revision;

import com.vaadin.ui.MenuBar.MenuItem;
import eu.europa.ec.leos.model.action.ContributionVO;

public class DeclineRevisionDocumentEvent {

    private ContributionVO contributionVO;
    private MenuItem selectedItem;

    public DeclineRevisionDocumentEvent(ContributionVO contributionVO, MenuItem selectedItem) {
        this.contributionVO = contributionVO;
        this.selectedItem = selectedItem;
    }

    public ContributionVO getContributionVO() {
        return contributionVO;
    }

    public MenuItem getSelectedItem() { return selectedItem; }
}
