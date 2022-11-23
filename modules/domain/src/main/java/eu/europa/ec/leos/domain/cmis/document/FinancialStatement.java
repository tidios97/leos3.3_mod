package eu.europa.ec.leos.domain.cmis.document;

import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.metadata.FinancialStatementMetadata;
import eu.europa.ec.leos.model.user.Collaborator;
import io.atlassian.fugue.Option;

import java.time.Instant;
import java.util.List;

public final class FinancialStatement extends XmlDocument {
    private final Option<FinancialStatementMetadata> metadata;
    private final String baseRevisionId;

    public FinancialStatement(String id, String name, String createdBy, Instant creationInstant, String lastModifiedBy,
                              Instant lastModificationInstant, String versionSeriesId, String cmisVersionLabel, String versionLabel,
                              String versionComment, VersionType versionType, boolean isLatestVersion, String title,
                              List<Collaborator> collaborators, List<String> milestoneComments, Option<Content> content,
                              Option<FinancialStatementMetadata> metadata, String baseRevisionId) {

        super(LeosCategory.FINANCIAL_STATEMENT, id, name, createdBy, creationInstant, lastModifiedBy, lastModificationInstant,
                versionSeriesId, cmisVersionLabel, versionLabel, versionComment, versionType, isLatestVersion, title, collaborators, milestoneComments, content);
        this.metadata = metadata;
        this.baseRevisionId = baseRevisionId;
    }

    public final Option<FinancialStatementMetadata> getMetadata() {
        return this.metadata;
    }

    public String getBaseRevisionId() { return baseRevisionId; }

    @Override
    public String getContributionStatus() {
        return null;
    }
}
