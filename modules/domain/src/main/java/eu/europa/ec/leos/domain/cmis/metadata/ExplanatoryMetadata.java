package eu.europa.ec.leos.domain.cmis.metadata;

import eu.europa.ec.leos.domain.cmis.LeosCategory;

public class ExplanatoryMetadata extends LeosMetadata{
    private final String title;

    public String getTitle() {
        return title;
    }

    public ExplanatoryMetadata(String stage, String type, String purpose, String template, String language, String docTemplate, String ref, String title, String objectId, String docVersion, boolean eeaRelevance) {
        super(LeosCategory.COUNCIL_EXPLANATORY, stage, type, purpose, template, language, docTemplate, ref, objectId, docVersion, eeaRelevance);
        this.title = title;
    }

    public final ExplanatoryMetadata withPurpose(String purpose) {
        return new ExplanatoryMetadata(stage, type, purpose, template, language, docTemplate, ref, title, objectId, docVersion, eeaRelevance);
    }

    public final ExplanatoryMetadata withRef(String ref) {
        return new ExplanatoryMetadata(stage, type, purpose, template, language, docTemplate, ref, title, objectId, docVersion, eeaRelevance);
    }

    public final ExplanatoryMetadata withObjectId(String objectId) {
        return new ExplanatoryMetadata(stage, type, purpose, template, language, docTemplate, ref, title, objectId, docVersion, eeaRelevance);
    }

    public final ExplanatoryMetadata withType(String type) {
        return new ExplanatoryMetadata(stage, type, purpose, template, language, docTemplate, ref, title, objectId, docVersion, eeaRelevance);
    }

    public final ExplanatoryMetadata withTemplate(String template) {
        return new ExplanatoryMetadata(stage, type, purpose, template, language, docTemplate, ref, title, objectId, docVersion, eeaRelevance);
    }

    public final ExplanatoryMetadata withDocVersion(String docVersion) {
        return new ExplanatoryMetadata(stage, type, purpose, template, language, docTemplate, ref, title, objectId, docVersion, eeaRelevance);
    }

    public final ExplanatoryMetadata withDocTemplate(String docTemplate) {
        return new ExplanatoryMetadata(stage, type, purpose, template, language, docTemplate, ref, title, objectId, docVersion, eeaRelevance);
    }

    public final ExplanatoryMetadata withTitle(String title) {
        return new ExplanatoryMetadata(stage, type, purpose, template, language, docTemplate, ref, title, objectId, docVersion, eeaRelevance);
    }

    public final ExplanatoryMetadata withEeaRelevance(boolean eeaRelevance) {
        return new ExplanatoryMetadata(stage, type, purpose, template, language, docTemplate, ref, title, objectId, docVersion, eeaRelevance);
    }
}