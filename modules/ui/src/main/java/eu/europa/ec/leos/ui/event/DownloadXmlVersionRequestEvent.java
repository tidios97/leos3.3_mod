package eu.europa.ec.leos.ui.event;

public class DownloadXmlVersionRequestEvent {
    private String versionId;
    
    public DownloadXmlVersionRequestEvent(String versionId) {
        this.versionId = versionId;
    }
    
    public String getVersionId() {
        return versionId;
    }
}