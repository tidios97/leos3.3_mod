package eu.europa.ec.leos.domain.cmis.metadata;

import eu.europa.ec.leos.domain.cmis.LeosCategory;

public class FinancialStatementMetadata extends LeosMetadata{
    private final String title;

    public String getTitle() {
        return title;
    }

    public FinancialStatementMetadata(String stage, String type, String purpose, String template, String language, String docTemplate, String ref, String title, String objectId, String docVersion, boolean eeaRelevance) {
        super(LeosCategory.FINANCIAL_STATEMENT, stage, type, purpose, template, language, docTemplate, ref, objectId, docVersion, eeaRelevance);
        this.title = title;
    }

    public final FinancialStatementMetadata withPurpose(String purpose) {
        return new FinancialStatementMetadata(stage, type, purpose, template, language, docTemplate, ref, title, objectId, docVersion, eeaRelevance);
    }

    public final FinancialStatementMetadata withRef(String ref) {
        return new FinancialStatementMetadata(stage, type, purpose, template, language, docTemplate, ref, title, objectId, docVersion, eeaRelevance);
    }

    public final FinancialStatementMetadata withObjectId(String objectId) {
        return new FinancialStatementMetadata(stage, type, purpose, template, language, docTemplate, ref, title, objectId, docVersion, eeaRelevance);
    }

    public final FinancialStatementMetadata withType(String type) {
        return new FinancialStatementMetadata(stage, type, purpose, template, language, docTemplate, ref, title, objectId, docVersion, eeaRelevance);
    }

    public final FinancialStatementMetadata withTemplate(String template) {
        return new FinancialStatementMetadata(stage, type, purpose, template, language, docTemplate, ref, title, objectId, docVersion, eeaRelevance);
    }

    public final FinancialStatementMetadata withDocVersion(String docVersion) {
        return new FinancialStatementMetadata(stage, type, purpose, template, language, docTemplate, ref, title, objectId, docVersion, eeaRelevance);
    }

    public final FinancialStatementMetadata withDocTemplate(String docTemplate) {
        return new FinancialStatementMetadata(stage, type, purpose, template, language, docTemplate, ref, title, objectId, docVersion, eeaRelevance);
    }

    public final FinancialStatementMetadata withTitle(String title) {
        return new FinancialStatementMetadata(stage, type, purpose, template, language, docTemplate, ref, title, objectId, docVersion, eeaRelevance);
    }

    public final FinancialStatementMetadata withEeaRelevance(boolean eeaRelevance) {
        return new FinancialStatementMetadata(stage, type, purpose, template, language, docTemplate, ref, title, objectId, docVersion, eeaRelevance);
    }
}