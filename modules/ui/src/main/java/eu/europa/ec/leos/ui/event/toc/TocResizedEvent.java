package eu.europa.ec.leos.ui.event.toc;

public class TocResizedEvent {

    private float paneSize;

    public TocResizedEvent(float paneSize) {
        this.paneSize = paneSize;
    }

    public float getPaneSize() {
        return paneSize;
    }
}
