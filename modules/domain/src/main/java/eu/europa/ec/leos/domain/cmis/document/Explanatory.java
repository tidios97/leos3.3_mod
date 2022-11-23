package eu.europa.ec.leos.domain.cmis.document;

import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.metadata.ExplanatoryMetadata;
import eu.europa.ec.leos.model.user.Collaborator;
import io.atlassian.fugue.Option;

import java.time.Instant;
import java.util.List;

public final class Explanatory extends XmlDocument {
    private final Option<ExplanatoryMetadata> metadata;

    public Explanatory(String id, String name, String createdBy, Instant creationInstant, String lastModifiedBy,
                      Instant lastModificationInstant, String versionSeriesId, String cmisVersionLabel, String versionLabel,
                      String versionComment, VersionType versionType, boolean isLatestVersion, String title,
                      List<Collaborator> collaborators, List<String> milestoneComments, Option<Content> content,
                      Option<ExplanatoryMetadata> metadata) {

        super(LeosCategory.COUNCIL_EXPLANATORY, id, name, createdBy, creationInstant, lastModifiedBy, lastModificationInstant,
                versionSeriesId, cmisVersionLabel, versionLabel, versionComment, versionType, isLatestVersion, title, collaborators, milestoneComments, content);
        this.metadata = metadata;
    }

    public final Option<ExplanatoryMetadata> getMetadata() {
        return this.metadata;
    }

    @Override
    public String getContributionStatus() {
        return null;
    }
}
