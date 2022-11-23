package eu.europa.ec.leos.ui.event.contribution;

public class ViewContributionEvent {

    private String legFileName;
    private String propsalRef;
    private String contributionTitle;

    public ViewContributionEvent(String legFileName, String propsalRef, String contributionTitle) {
        this.legFileName = legFileName;
        this.propsalRef = propsalRef;
        this.contributionTitle = contributionTitle;
    }

    public String getLegFileName() {
        return legFileName;
    }

    public String getContributionTitle() {
        return contributionTitle;
    }

    public String getPropsalRef() {
        return propsalRef;
    }
}
