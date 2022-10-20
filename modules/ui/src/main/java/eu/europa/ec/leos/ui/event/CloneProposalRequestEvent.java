package eu.europa.ec.leos.ui.event;

import eu.europa.ec.leos.ui.model.MilestonesVO;
import eu.europa.ec.leos.web.model.UserVO;

public class CloneProposalRequestEvent {
    MilestonesVO milestonesVO;
    UserVO userVO;

    public CloneProposalRequestEvent(MilestonesVO milestonesVO, UserVO userVO) {
        this.milestonesVO = milestonesVO;
        this.userVO = userVO;
    }

    public MilestonesVO getMilestonesVO() {
        return milestonesVO;
    }

    public UserVO getUserVO() {
        return userVO;
    }
}
