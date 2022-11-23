package eu.europa.ec.leos.domain.cmis.metadata;

import eu.europa.ec.leos.domain.cmis.LeosCategory;

import java.util.Objects;

public final class AnnexMetadata extends LeosMetadata {
    private final int index;
    private final String number;
    private final String title;
    private final String clonedRef; //Optional only required when adding new annex in clone proposal to original

    public AnnexMetadata(String stage, String type, String purpose, String template, String language, String docTemplate,
                         String ref, int index, String number, String title, String objectId, String docVersion,
                         boolean eeaRelevance, String clonedRef) {
        super(LeosCategory.ANNEX, stage, type, purpose, template, language, docTemplate, ref, objectId, docVersion, eeaRelevance);
        this.index = index;
        this.number = number;
        this.title = title;
        this.clonedRef = clonedRef;
    }

    public int getIndex() {
        return index;
    }

    public String getNumber() {
        return number;
    }

    public String getTitle() {
        return title;
    }

    public String getClonedRef() {
        return clonedRef;
    }

    public AnnexMetadata withPurpose(String purpose) {
        return new AnnexMetadata(stage, type, purpose, template, language, docTemplate, ref, index, number, title, objectId, docVersion, eeaRelevance, clonedRef);
    }

    public AnnexMetadata withIndex(int index) {
        return new AnnexMetadata(stage, type, purpose, template, language, docTemplate, ref, index, number, title, objectId, docVersion, eeaRelevance, clonedRef);
    }

    public AnnexMetadata withNumber(String number) {
        return new AnnexMetadata(stage, type, purpose, template, language, docTemplate, ref, index, number, title, objectId, docVersion, eeaRelevance, clonedRef);
    }

    public AnnexMetadata withTitle(String title) {
        return new AnnexMetadata(stage, type, purpose, template, language, docTemplate, ref, index, number, title, objectId, docVersion, eeaRelevance, clonedRef);
    }

    public AnnexMetadata withRef(String ref) {
        return new AnnexMetadata(stage, type, purpose, template, language, docTemplate, ref, index, number, title, objectId, docVersion, eeaRelevance, clonedRef);
    }

    public AnnexMetadata withObjectId(String objectId) {
        return new AnnexMetadata(stage, type, purpose, template, language, docTemplate, ref, index, number, title, objectId, docVersion, eeaRelevance, clonedRef);
    }

    public AnnexMetadata withType(String type) {
        return new AnnexMetadata(stage, type, purpose, template, language, docTemplate, ref, index, number, title, objectId, docVersion, eeaRelevance, clonedRef);
    }

    public AnnexMetadata withTemplate(String template) {
        return new AnnexMetadata(stage, type, purpose, template, language, docTemplate, ref, index, number, title, objectId, docVersion, eeaRelevance, clonedRef);
    }
    
    public AnnexMetadata withDocVersion(String docVersion) {
        return new AnnexMetadata(stage, type, purpose, template, language, docTemplate, ref, index, number, title, objectId, docVersion, eeaRelevance, clonedRef);
    }
    
    public AnnexMetadata withDocTemplate(String docTemplate) {
        return new AnnexMetadata(stage, type, purpose, template, language, docTemplate, ref, index, number, title, objectId, docVersion, eeaRelevance, clonedRef);
    }

    public AnnexMetadata withEeaRelevance(boolean eeaRelevance) {
        return new AnnexMetadata(stage, type, purpose, template, language, docTemplate, ref, index, number, title, objectId, docVersion, eeaRelevance, clonedRef);
    }

    public AnnexMetadata withClonedRef(String clonedRef) {
        return new AnnexMetadata(stage, type, purpose, template, language, docTemplate, ref, index, number, title, objectId, docVersion, eeaRelevance, clonedRef);
    }

    @Override
    public String toString() {
        return "AnnexMetadata{" +
                "index=" + index +
                ", number='" + number + '\'' +
                ", title='" + title + '\'' +
                ", clonedRef='" + clonedRef + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnnexMetadata that = (AnnexMetadata) o;
        return index == that.index &&
                Objects.equals(stage, that.stage) &&
                Objects.equals(type, that.type) &&
                Objects.equals(purpose, that.purpose) &&
                Objects.equals(template, that.template) &&
                Objects.equals(language, that.language) &&
                Objects.equals(docTemplate, that.docTemplate) &&
                Objects.equals(ref, that.ref) &&
                Objects.equals(number, that.number) &&
                Objects.equals(title, that.title) &&
                Objects.equals(objectId, that.objectId) &&
                Objects.equals(eeaRelevance, that.eeaRelevance) &&
                Objects.equals(docVersion, that.docVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), index, number, title);
    }
}
