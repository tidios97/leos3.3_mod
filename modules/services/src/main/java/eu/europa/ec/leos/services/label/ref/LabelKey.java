package eu.europa.ec.leos.services.label.ref;

public class LabelKey {

    private String labelName; // name to be shown
    private String labelNumber; // number can be letters or numbers
    private boolean unNumbered; // in case of unnumbered, the number has to be expressed in letters rather than in number.
    private String documentRef;

    public LabelKey(String labelName, String labelNumber, boolean unNumbered, String documentRef) {
        this.labelName = labelName;
        this.labelNumber = labelNumber;
        this.unNumbered = unNumbered;
        this.documentRef = documentRef;
    }

    public String getLabelName() {
        return labelName;
    }

    public String getLabelNumber() {
        return labelNumber;
    }

    public String getDocumentRef() {
        return documentRef;
    }

    public boolean isUnNumbered() {
        return unNumbered;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + labelName.hashCode() + labelNumber.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        LabelKey other = (LabelKey) obj;
        if (other.labelName.equals(labelName)
                && other.labelNumber.equals(labelNumber)) {
            return true;
        }
        return true;
    }

    @Override
    public String toString() {
        return "LabelKey [labelName=" + labelName + ", labelNumber=" + labelNumber + ", unNumbered=" + unNumbered + "]";
    }

}
