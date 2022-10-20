package eu.europa.ec.leos.services.clone;

public class InternalRefMap {

    private String docType;
    private String ref;
    private String clonedRef;

    public InternalRefMap(String docType, String ref, String clonedRef) {
        this.docType = docType;
        this.ref = ref;
        this.clonedRef = clonedRef;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getClonedRef() {
        return clonedRef;
    }

    public void setClonedRef(String clonedRef) {
        this.clonedRef = clonedRef;
    }

}
