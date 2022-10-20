package eu.europa.ec.leos.ui.event.revision;

import eu.europa.ec.leos.model.action.ContributionVO;

public class OpenRevisionDocumentEvent {

    private ContributionVO contributionVO;

    public OpenRevisionDocumentEvent(ContributionVO contributionVO) {
        this.contributionVO = contributionVO;
    }

    public ContributionVO getContributionVO() {
        return contributionVO;
    }
}
