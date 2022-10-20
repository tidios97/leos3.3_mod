package eu.europa.ec.leos.model.event;

import eu.europa.ec.leos.vo.coedition.CoEditionActionInfo;

public class UpdateUserInfoEvent {

    private CoEditionActionInfo actionInfo;

    public UpdateUserInfoEvent(CoEditionActionInfo actionInfo) {
        this.actionInfo = actionInfo;
    }

    public CoEditionActionInfo getActionInfo() {
        return actionInfo;
    }
}
