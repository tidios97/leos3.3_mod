package eu.europa.ec.leos.ui.event.view.collection;

public class SearchUserInContextEvent {

    private String searchKey;
    private String searchContext;

    public SearchUserInContextEvent(String searchKey, String searchContext) {
        this.searchKey = searchKey;
        this.searchContext = searchContext;
    }

    public String getSearchKey() {
        return searchKey;
    }

    public String getSearchContext() {
        return searchContext;
    }
}