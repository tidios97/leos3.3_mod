package eu.europa.ec.leos.model.action;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class CheckinElement {
    
    private ActionType actionType;
    private String elementId;
    private String elementTagName;
    private String elementLabel;
    
    // Set object because we want the uniquity in the couple (ActionType, elementId)
    private Set<CheckinElement> childElements = new HashSet<>(); // not empty only for STRUCTURAL save
    
    // Empty constructor needed for jackson ObjectMapper
    public CheckinElement(){}

    public CheckinElement(ActionType actionType) {
        this.actionType = actionType;
    }

    public CheckinElement(ActionType actionType, Set<CheckinElement> childElements) {
        this.actionType = actionType;
        this.childElements = childElements;
    }
    
    public CheckinElement(ActionType actionType, String elementId, String elementTagName) {
        this.actionType = actionType;
        this.elementId = elementId;
        this.elementTagName = elementTagName;
    }
    
    public CheckinElement(ActionType actionType, String elementId, String elementTagName, String elementLabel) {
        this.actionType = actionType;
        this.elementId = elementId;
        this.elementTagName = elementTagName;
        this.elementLabel = elementLabel;
    }
    
    public ActionType getActionType() {
        return actionType;
    }
    
    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }
    
    public String getElementId() {
        return elementId;
    }
    
    public void setElementId(String elementId) {
        this.elementId = elementId;
    }
    
    public String getElementTagName() {
        return elementTagName;
    }
    
    public void setElementTagName(String elementTagName) {
        this.elementTagName = elementTagName;
    }
    
    public String getElementLabel() {
        return elementLabel;
    }
    
    public void setElementLabel(String elementLabel) {
        this.elementLabel = elementLabel;
    }
    
    public Set<CheckinElement> getChildElements() {
        return childElements;
    }
    
    public void setChildElements(Set<CheckinElement> childElements) {
        this.childElements = childElements;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CheckinElement)) return false;
        CheckinElement item = (CheckinElement) o;
        return Objects.equals(actionType, item.actionType) &&
                Objects.equals(elementId, item.elementId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(actionType, elementId);
    }
    
    @Override
    public String toString() {
        return "[elementId: " + elementId + ", elementTagName: " + elementTagName + ", elementLabel: " + elementLabel
                + ", actionType: " + actionType + ", childElements: " + childElements + "]";
    }
    
}
