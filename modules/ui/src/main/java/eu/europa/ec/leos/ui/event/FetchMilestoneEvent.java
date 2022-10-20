package eu.europa.ec.leos.ui.event;

public class FetchMilestoneEvent {

    private String legFileName;
    private String milestoneTitle;
    
    public FetchMilestoneEvent(String legFileName, String milestoneTitle) {
        this.legFileName = legFileName;
        this.milestoneTitle = milestoneTitle;
    }

    public String getLegFileName() {
        return legFileName;
    }

    public String getMilestoneTitle() {
        return milestoneTitle;
    }
}
