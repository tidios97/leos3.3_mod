package eu.europa.ec.leos.ui.event.toc;

import eu.europa.ec.leos.model.action.CheckinElement;

import java.util.List;

public class TocChangedEvent {

    private String message;
    private Result result;
    private List<CheckinElement> checkinElements;
    
    public enum Result {
        SUCCESSFUL,ERROR;
    }

    public TocChangedEvent(String message, Result result, List<CheckinElement> checkinElements) {
        this.message = message;
        this.result = result;
        this.checkinElements = checkinElements;
    }
    
    public List<CheckinElement> getCheckinElements() {
        return checkinElements;
    }
    
    public String getMessage() {
        return message;
    }

    public Result getResult() {
        return result;
    }
}
