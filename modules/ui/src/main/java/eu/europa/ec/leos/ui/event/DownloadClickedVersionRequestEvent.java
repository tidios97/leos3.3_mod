package eu.europa.ec.leos.ui.event;

public class DownloadClickedVersionRequestEvent {
    private String versionId;
    
    public DownloadClickedVersionRequestEvent(String versionId) {
        this.versionId = versionId;
    }
    
    public String getVersionId() {
        return versionId;
    }
}