package eu.europa.ec.leos.ui.event.search;


public class ShowConfirmDialogEvent {
    private final Object postConfirmEvent;
    private final Object postCancelEvent;

    public ShowConfirmDialogEvent(){
        this(null,null);
    }
    public ShowConfirmDialogEvent(Object postConfirmEvent, Object postCancelEvent) {
        this.postConfirmEvent = postConfirmEvent;
        this.postCancelEvent = postCancelEvent;
    }

    public Object getPostConfirmEvent() {
        return postConfirmEvent;
    }

    public Object getPostCancelEvent() {
        return postCancelEvent;
    }
}
