package eu.europa.ec.leos.domain.cmis.document;

import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.metadata.ProposalMetadata;
import eu.europa.ec.leos.model.user.Collaborator;
import io.atlassian.fugue.Option;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class Proposal extends XmlDocument {
    private final String initialCreatedBy;
    private final Instant initialCreationInstant;
    private final Option<ProposalMetadata> metadata;
    private final boolean clonedProposal;
    private final String originRef;
    private final String clonedFrom;
    private final String revisionStatus;
    private final List<String> clonedMilestoneIds;

    private final String contributionStatus;

    public Proposal(String id, String name, String createdBy, Instant creationInstant, String lastModifiedBy, Instant lastModificationInstant,
                    String versionSeriesId, String cmisVersionLabel, String versionLabel, String versionComment, VersionType versionType,
                    boolean isLatestVersion, String title, List<Collaborator> collaborators, List<String> milestoneComments,
                    String initialCreatedBy, Instant initialCreationInstant, Option<Content> content, Option<ProposalMetadata> metadata,
                    boolean clonedProposal, String originRef, String clonedFrom, String revisionStatus,
                    List<String> clonedMilestoneIds, String contributionStatus) {
        super(LeosCategory.PROPOSAL, id, name, createdBy, creationInstant, lastModifiedBy, lastModificationInstant,
                versionSeriesId, cmisVersionLabel, versionLabel, versionComment, versionType, isLatestVersion, title, collaborators, milestoneComments, content);
        this.initialCreatedBy = initialCreatedBy;
        this.initialCreationInstant = initialCreationInstant;
        this.metadata = metadata;
        this.clonedProposal = clonedProposal;
        this.originRef = originRef;
        this.clonedFrom = clonedFrom;
        this.revisionStatus = revisionStatus;
        this.clonedMilestoneIds = clonedMilestoneIds;
        this.contributionStatus = contributionStatus;
    }

    public final String getInitialCreatedBy() {
        return this.initialCreatedBy;
    }

    public final Instant getInitialCreationInstant() {
        return this.initialCreationInstant;
    }

    public final Option<ProposalMetadata> getMetadata() {
        return this.metadata;
    }

    public boolean isClonedProposal() {
        return clonedProposal;
    }

    public String getOriginRef() {
        return originRef;
    }

    public String getRevisionStatus() {
        return revisionStatus;
    }

    public String getClonedFrom() {
        return clonedFrom;
    }

    public List<String> getClonedMilestoneIds() {
        return clonedMilestoneIds != null ? clonedMilestoneIds : new ArrayList<>();
    }

    public String getContributionStatus() {
        return contributionStatus;
    }
}
