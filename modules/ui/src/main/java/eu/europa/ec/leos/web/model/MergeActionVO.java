package eu.europa.ec.leos.web.model;

import eu.europa.ec.leos.model.action.ContributionVO;
import eu.europa.ec.leos.ui.event.contribution.MergeActionRequestEvent;

public class MergeActionVO {

    private final MergeActionRequestEvent.MergeAction action;
    private final MergeActionRequestEvent.ElementState elementState;
    private final String elementId;
    private final String elementTagName;
    private final ContributionVO contributionVO;


    public MergeActionVO(MergeActionRequestEvent.MergeAction action, MergeActionRequestEvent.ElementState elementState, String elementId, String elementTagName, ContributionVO contributionVO) {
        this.action = action;
        this.elementState = elementState;
        this.elementId = elementId;
        this.elementTagName = elementTagName;
        this.contributionVO = contributionVO;
    }

    public MergeActionRequestEvent.MergeAction getAction() {
        return action;
    }
    public MergeActionRequestEvent.ElementState getElementState() { return elementState; }
    public String getElementId() {
        return elementId;
    }
    public String getElementTagName() {
        return elementTagName;
    }
    public ContributionVO getContributionVO() {
        return contributionVO;
    }
}
