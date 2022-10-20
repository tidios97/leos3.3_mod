package eu.europa.ec.leos.ui.event.toc;

import eu.europa.ec.leos.domain.common.TocMode;

public class RefreshTocEvent {

    private TocMode mode;

    public RefreshTocEvent(TocMode mode) {
        this.mode = mode;
    }
    
    public TocMode getTocMode() {
        return this.mode;
    }
}
