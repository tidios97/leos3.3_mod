package eu.europa.ec.leos.ui.event;

public class FetchMilestoneByVersionedReferenceEvent {

    private String versionedReference;

    public FetchMilestoneByVersionedReferenceEvent(String versionedReference) {
        this.versionedReference = versionedReference;
    }

    public String getVersionedReference() {
        return versionedReference;
    }

}
