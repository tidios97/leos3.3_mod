package eu.europa.ec.leos.ui.event;

public class RevisionDoneEvent {
    private String legFileName;

    public RevisionDoneEvent(String legFileName) {
        this.legFileName = legFileName;
    }

    public String getLegFileName() {
        return legFileName;
    }
}
