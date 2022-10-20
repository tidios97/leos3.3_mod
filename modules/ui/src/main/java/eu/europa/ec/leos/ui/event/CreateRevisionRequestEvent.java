package eu.europa.ec.leos.ui.event;

import eu.europa.ec.leos.ui.model.MilestonesVO;

public class CreateRevisionRequestEvent {

    MilestonesVO vo;

    public CreateRevisionRequestEvent(MilestonesVO vo) {
        this.vo = vo;
    }

    public MilestonesVO getVo() {
        return vo;
    }
}
