package eu.europa.ec.leos.ui.event.view.collection;

public class SearchContextEvent {

    boolean showSearchContext;

    public SearchContextEvent(boolean showSearchContext){
        this.showSearchContext = showSearchContext;
    }

    public boolean isShowSearchContext() {
        return showSearchContext;
    }
}