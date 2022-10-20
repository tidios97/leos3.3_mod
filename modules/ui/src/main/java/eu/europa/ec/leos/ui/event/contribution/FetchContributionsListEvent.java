package eu.europa.ec.leos.ui.event.contribution;

import eu.europa.ec.leos.model.action.ContributionVO;

public class FetchContributionsListEvent {

    ContributionVO contributionVO;
    boolean selectAll = false;

    public FetchContributionsListEvent(ContributionVO contributionVO, boolean selectAll) {
        this.contributionVO = contributionVO;
        this.selectAll = selectAll;
    }

    public ContributionVO getContributionVO() {
        return contributionVO;
    }

    public boolean isSelectAll() {
        return selectAll;
    }
}
