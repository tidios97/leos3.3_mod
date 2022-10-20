package eu.europa.ec.leos.domain.cmis.metadata;

import eu.europa.ec.leos.domain.cmis.LeosCategory;

public final class BillMetadata extends LeosMetadata {

    public BillMetadata(String stage, String type, String purpose, String template, String language, String docTemplate, String ref, String objectId, String docVersion, boolean eeaRelevance) {
        super(LeosCategory.BILL, stage, type, purpose, template, language, docTemplate, ref, objectId, docVersion, eeaRelevance);
    }

    public final BillMetadata withPurpose(String purpose) {
        return new BillMetadata(stage, type, purpose, template, language, docTemplate, ref, objectId, docVersion, eeaRelevance);
    }

    public final BillMetadata withRef(String ref) {
        return new BillMetadata(stage, type, purpose, template, language, docTemplate, ref, objectId, docVersion, eeaRelevance);
    }

    public final BillMetadata withObjectId(String objectId) {
        return new BillMetadata(stage, type, purpose, template, language, docTemplate, ref, objectId, docVersion, eeaRelevance);
    }
    
    public final BillMetadata withDocVersion(String docVersion) {
        return new BillMetadata(stage, type, purpose, template, language, docTemplate, ref, objectId, docVersion, eeaRelevance);
    }

    public final BillMetadata withEeaRelevance(boolean eeaRelevance) {
        return new BillMetadata(stage, type, purpose, template, language, docTemplate, ref, objectId, docVersion, eeaRelevance);
    }
}
