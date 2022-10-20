package eu.europa.ec.leos.services.export;

import eu.europa.ec.leos.domain.cmis.document.XmlDocument;

public class ExportVersions<T extends XmlDocument> {
    
    private T original;
    private T intermediate;
    private T current;
    
    public ExportVersions(T original, T intermediate, T current) {
        this.original = original;
        this.intermediate = intermediate;
        this.current = current;
    }

    public ExportVersions(T original, T current) {
        this.original = original;
        this.intermediate = null;
        this.current = current;
    }

    public T getOriginal() {
        return original;
    }

    public T getIntermediate() {
        return intermediate;
    }

    public T getCurrent() {
        return current;
    }
    
    public Class getVersionClass(){
        return current.getClass();
    }

    public int getVersionsPresent() {
        int count = 0;
        if (original != null) {
            count++;
        }
        if (intermediate != null) {
            count++;
        }
        if (current != null) {
            count++;
        }
        return count;
    }
    
}
