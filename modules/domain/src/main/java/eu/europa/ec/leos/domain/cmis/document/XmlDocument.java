package eu.europa.ec.leos.domain.cmis.document;

import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.common.Securable;
import eu.europa.ec.leos.domain.cmis.common.SecurityData;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.metadata.LeosMetadata;
import eu.europa.ec.leos.model.user.Collaborator;
import io.atlassian.fugue.Option;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public abstract class XmlDocument extends LeosDocument implements Securable {
    private final String title;
    private final List<String> milestoneComments;
    private final SecurityData securityData;


    public final String getTitle() {
        return this.title;
    }


    public final List<String> getMilestoneComments() {
        return this.milestoneComments;
    }

    protected XmlDocument(LeosCategory category, String id, String name, String createdBy,
                          Instant creationInstant, String lastModifiedBy, Instant lastModificationInstant,
                          String versionSeriesId, String cmisVersionLabel, String versionLabel, String versionComment,
                          VersionType versionType, boolean isLatestVersion, String title,
                          List<Collaborator> collaborators, List<String> milestoneComments, Option<Content> content) {
        super(category, id, name, createdBy, creationInstant, lastModifiedBy, lastModificationInstant, versionSeriesId,
                cmisVersionLabel, versionLabel, versionComment, versionType, isLatestVersion, content);
        this.securityData = new SecurityData(collaborators);
        this.title = title;
        this.milestoneComments = milestoneComments;
    }

    protected XmlDocument(LeosCategory category, String id, String name, String createdBy,
                          Instant creationInstant, String lastModifiedBy, Instant lastModificationInstant,
                          String versionSeriesId, String cmisVersionLabel, String versionLabel, String versionComment,
                          VersionType versionType, boolean isLatestVersion, Option<Content> content) {
        super(category, id, name, createdBy, creationInstant, lastModifiedBy, lastModificationInstant, versionSeriesId,
                cmisVersionLabel, versionLabel, versionComment, versionType, isLatestVersion, content);
        this.securityData = null;
        this.title = null;
        this.milestoneComments = null;
    }


    public List<Collaborator> getCollaborators() {
        return this.securityData.getCollaborators();
    }

    public abstract Option<? extends LeosMetadata> getMetadata();

    public String getVersionedReference() {
        return this.getMetadata().get().getRef() + "_" + this.getVersionLabel();
    }

    public abstract String getContributionStatus();
}
