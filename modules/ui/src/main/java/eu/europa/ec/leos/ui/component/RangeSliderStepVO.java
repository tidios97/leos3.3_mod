package eu.europa.ec.leos.ui.component;

public class RangeSliderStepVO {
    
    private String stepValue;
    private String mileStoneComments;
    private boolean milestoneVersion;
    
    
    public RangeSliderStepVO(String stepValue, String mileStoneComments, boolean milestoneVersion) {
        this.stepValue = stepValue;
        this.mileStoneComments = mileStoneComments;
        this.milestoneVersion = milestoneVersion;
    }
    
    public String getStepValue() {
        return stepValue;
    }
    public void setStepValue(String stepValue) {
        this.stepValue = stepValue;
    }
    public boolean isMilestoneVersion() {
        return milestoneVersion;
    }
    public void setMilestoneVersion(boolean milestoneVersion) {
        this.milestoneVersion = milestoneVersion;
    }

    public String getMileStoneComments() {
        return mileStoneComments;
    }

    public void setMileStoneComments(String mileStoneComments) {
        this.mileStoneComments = mileStoneComments;
    }
}
