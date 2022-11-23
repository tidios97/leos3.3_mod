package eu.europa.ec.leos.web.event.view.document;

import eu.europa.ec.leos.web.event.NavigationRequestEvent;

public class DocumentNavigationRequest {
    private final NavigationRequestEvent navigationEvent;

    public DocumentNavigationRequest() {
        this(null);
    }

    public DocumentNavigationRequest(final NavigationRequestEvent navigationEvent) {
        this.navigationEvent = navigationEvent;
    }

    public NavigationRequestEvent getNavigationEvent() {
        return this.navigationEvent;
    }
}
