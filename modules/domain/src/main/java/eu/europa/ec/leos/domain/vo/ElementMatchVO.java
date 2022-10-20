package eu.europa.ec.leos.domain.vo;

import java.util.Objects;

public class ElementMatchVO {
    private String elementId;
    private int matchStartIndex;
    private int matchEndIndex;
    private boolean isEditable;

    public ElementMatchVO() {
    }

    public ElementMatchVO(String elementId, int matchStartIndex) {
        this.elementId = elementId;
        this.matchStartIndex = matchStartIndex;
    }

    public ElementMatchVO(String elementId, int matchStartIndex, int matchEndIndex) {
        this.elementId = elementId;
        this.matchStartIndex = matchStartIndex;
        this.matchEndIndex = matchEndIndex;
    }

    public ElementMatchVO(String elementId, int matchStartIndex, boolean isEditable) {
        this.elementId = elementId;
        this.matchStartIndex = matchStartIndex;
        this.isEditable = isEditable;
    }
    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {// method needed for vaadin reflection to create JSON
        throw new UnsupportedOperationException();
    }

    public int getMatchStartIndex() {
        return matchStartIndex;
    }

    public void setMatchStartIndex(int matchStartIndex) {
        this.matchStartIndex = matchStartIndex;
    }

    public int getMatchEndIndex() {
        return matchEndIndex;
    }

    public void setMatchEndIndex(int matchEndIndex) {
        this.matchEndIndex = matchEndIndex;
    }

    public boolean isEditable() {
        return isEditable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElementMatchVO that = (ElementMatchVO) o;
        return matchStartIndex == that.matchStartIndex &&
                matchEndIndex == that.matchEndIndex &&
                Objects.equals(elementId, that.elementId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementId, matchStartIndex, matchEndIndex);
    }

    @Override
    public String toString() {
        return "ElementMatchVO{" +
                "elementId='" + elementId + '\'' +
                ", matchStartIndex=" + matchStartIndex +
                ", matchEndIndex=" + matchEndIndex +
                '}';
    }
}
