package  eu.europa.ec.leos.ui.event.contribution;

import eu.europa.ec.leos.web.model.MergeActionVO;

import java.util.List;

public class ApplyContributionsRequestEvent {

    List<MergeActionVO> mergeActionVOS;

    boolean allContributions = false;

    public ApplyContributionsRequestEvent(List<MergeActionVO> mergeActionVOS, boolean allContributions) {
        this.mergeActionVOS = mergeActionVOS;
        this.allContributions = allContributions;
    }

    public List<MergeActionVO> getMergeActionVOS() {
        return mergeActionVOS;
    }

    public boolean isAllContributions() {
        return allContributions;
    }
}
