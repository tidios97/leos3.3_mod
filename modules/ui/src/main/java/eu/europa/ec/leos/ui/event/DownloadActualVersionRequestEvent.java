package eu.europa.ec.leos.ui.event;

public class DownloadActualVersionRequestEvent {
    Boolean withFilteredAnnotations;

    public DownloadActualVersionRequestEvent() {
        withFilteredAnnotations = false;
    }

    public DownloadActualVersionRequestEvent(Boolean withFilteredAnnotations) {
        this.withFilteredAnnotations = withFilteredAnnotations;
    }

    public Boolean isWithFilteredAnnotations() {
        return withFilteredAnnotations;
    }
}
