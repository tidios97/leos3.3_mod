package eu.europa.ec.leos.domain.cmis.document;

import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.metadata.MemorandumMetadata;
import eu.europa.ec.leos.model.user.Collaborator;
import io.atlassian.fugue.Option;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class Memorandum extends XmlDocument {
    private final Option<MemorandumMetadata> metadata;
    private final String contributionStatus;
    private final String clonedFrom;

    public Memorandum(String id, String name, String createdBy, Instant creationInstant, String lastModifiedBy,
                      Instant lastModificationInstant, String versionSeriesId, String cmisVersionLabel, String versionLabel,
                      String versionComment, VersionType versionType, boolean isLatestVersion, String title,
                      List<Collaborator> collaborators, List<String> milestoneComments, Option<Content> content,
                       String contributionStatus, String clonedFrom, Option<MemorandumMetadata> metadata) {

        super(LeosCategory.MEMORANDUM, id, name, createdBy, creationInstant, lastModifiedBy, lastModificationInstant,
                versionSeriesId, cmisVersionLabel, versionLabel, versionComment, versionType, isLatestVersion, title, collaborators, milestoneComments, content);
        this.metadata = metadata;
        this.contributionStatus = contributionStatus;
        this.clonedFrom = clonedFrom;
    }

    public final Option<MemorandumMetadata> getMetadata() {
        return this.metadata;
    }

    public String getContributionStatus() {
        return contributionStatus;
    }

    public String getClonedFrom() {
        return clonedFrom;
    }
}
