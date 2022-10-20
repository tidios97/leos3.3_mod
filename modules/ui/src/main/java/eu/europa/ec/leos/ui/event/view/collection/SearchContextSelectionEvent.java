package eu.europa.ec.leos.ui.event.view.collection;

public class SearchContextSelectionEvent {

    private String context;

    public SearchContextSelectionEvent(String context) {
        this.context=context;
    }

    public String getContext(){
        return context;
    }

}