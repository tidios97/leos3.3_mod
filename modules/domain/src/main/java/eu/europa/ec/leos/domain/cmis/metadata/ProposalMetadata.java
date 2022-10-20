package eu.europa.ec.leos.domain.cmis.metadata;

import eu.europa.ec.leos.domain.cmis.LeosCategory;

public final class ProposalMetadata extends LeosMetadata {

    public ProposalMetadata(String stage, String type, String purpose, String template, String language, String docTemplate, String ref, String objectId, String docVersion, boolean eeaRelevance) {
        super(LeosCategory.PROPOSAL, stage, type, purpose, template, language, docTemplate, ref, objectId, docVersion, eeaRelevance);
    }

    public final ProposalMetadata withPurpose(String p) {
        return new ProposalMetadata(stage, type, p, template, language, docTemplate, ref, objectId, docVersion, eeaRelevance);
    }

    public final ProposalMetadata withRef(String ref) {
        return new ProposalMetadata(stage, type, purpose, template, language, docTemplate, ref, objectId, docVersion, eeaRelevance);
    }

    public final ProposalMetadata withObjectId(String objectId) {
        return new ProposalMetadata(stage, type, purpose, template, language, docTemplate, ref, objectId, docVersion, eeaRelevance);
    }

    public final ProposalMetadata withDocVersion(String docVersion) {
        return new ProposalMetadata(stage, type, purpose, template, language, docTemplate, ref, objectId, docVersion, eeaRelevance);
    }

    public final ProposalMetadata withEeaRelevance(boolean eeaRelevance) {
        return new ProposalMetadata(stage, type, purpose, template, language, docTemplate, ref, objectId, docVersion, eeaRelevance);
    }
}
