package eu.europa.ec.leos.ui.event.view.collection;

import eu.europa.ec.leos.domain.vo.DocumentVO;

public class DeleteExplanatoryEvent {
    private DocumentVO explanatory;

    public DeleteExplanatoryEvent(DocumentVO explanatory) {
        this.explanatory = explanatory;
    }

    public DocumentVO getExplanatory(){
        return explanatory;
    }
}
