package eu.europa.ec.leos.domain.cmis.metadata;

import eu.europa.ec.leos.domain.cmis.LeosCategory;

public final class MemorandumMetadata extends LeosMetadata {
    public MemorandumMetadata(String stage, String type, String purpose, String template, String language, String docTemplate, String ref, String objectId, String docVersion, boolean eeaRelevance) {
        super(LeosCategory.MEMORANDUM, stage, type, purpose, template, language, docTemplate, ref, objectId, docVersion, eeaRelevance);
    }

    public final MemorandumMetadata withPurpose(String purpose) {
        return new MemorandumMetadata(stage, type, purpose, template, language, docTemplate, ref, objectId, docVersion, eeaRelevance);
    }

    public final MemorandumMetadata withRef(String ref) {
        return new MemorandumMetadata(stage, type, purpose, template, language, docTemplate, ref, objectId, docVersion, eeaRelevance);
    }

    public final MemorandumMetadata withObjectId(String objectId) {
        return new MemorandumMetadata(stage, type, purpose, template, language, docTemplate, ref, objectId, docVersion, eeaRelevance);
    }

    public final MemorandumMetadata withType(String type) {
        return new MemorandumMetadata(stage, type, purpose, template, language, docTemplate, ref, objectId, docVersion, eeaRelevance);
    }

    public final MemorandumMetadata withTemplate(String template) {
        return new MemorandumMetadata(stage, type, purpose, template, language, docTemplate, ref, objectId, docVersion, eeaRelevance);
    }
    
    public final MemorandumMetadata withDocVersion(String docVersion) {
        return new MemorandumMetadata(stage, type, purpose, template, language, docTemplate, ref, objectId, docVersion, eeaRelevance);
    }

    public final MemorandumMetadata withEeaRelevance(boolean eeaRelevance) {
        return new MemorandumMetadata(stage, type, purpose, template, language, docTemplate, ref, objectId, docVersion, eeaRelevance);
    }
}
