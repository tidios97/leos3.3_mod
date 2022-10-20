package eu.europa.ec.leos.services.label.ref;

import java.util.Objects;

public class Ref {
    private final String id;
    private final String href;
    private final String documentref;
    private final String origin;
    private String refValue;

    public Ref(String id, String href, String documentref, String origin) {
        this.id = id;
        this.href = href;
        this.documentref = documentref;
        this.origin = origin;
    }

    public Ref(String id, String href, String documentref, String origin, String refValue) {
        this.id = id;
        this.href = href;
        this.documentref = documentref;
        this.origin = origin;
        this.refValue = refValue;
    }

    public String getId() {
        return id;
    }

    public String getHref() {
        return href;
    }

    public String getDocumentref() {
        return documentref;
    }

    public String getOrigin() {
        return origin;
    }

    public String getRefVal() { return  refValue; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ref ref = (Ref) o;
        return Objects.equals(id, ref.id) &&
                Objects.equals(href, ref.href) &&
                Objects.equals(documentref, ref.documentref);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, href, documentref);
    }

    @Override
    public String toString() {
        return "Ref[" +
                "id=" + id +
                ", href=" + href +
                ", documentref=" + documentref +
                ", origin=" + origin +
                ", refValue=" + refValue + "]";
    }

}
