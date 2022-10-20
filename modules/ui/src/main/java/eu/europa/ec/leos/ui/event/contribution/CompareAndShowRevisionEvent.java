package eu.europa.ec.leos.ui.event.contribution;

import eu.europa.ec.leos.model.action.ContributionVO;

public class CompareAndShowRevisionEvent {

    ContributionVO contributionVO;

    public CompareAndShowRevisionEvent(ContributionVO contributionVO) {
        this.contributionVO = contributionVO;
    }

    public ContributionVO getContributionVO() {
        return contributionVO;
    }
}
