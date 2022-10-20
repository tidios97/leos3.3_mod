package eu.europa.ec.leos.domain.cmis.document;

import java.time.Instant;
import java.util.List;

import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.LeosExportStatus;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import io.atlassian.fugue.Option;

public final class ExportDocument extends LeosDocument {
    private final String initialCreatedBy;
    private final Instant initialCreationInstant;
    private final LeosExportStatus status;
    private final List<String> comments;

    public ExportDocument(String id, String name, String createdBy, Instant creationInstant, String lastModifiedBy,
                          Instant lastModificationInstant, String versionSeriesId, String cmisVersionLabel, String versionLabel, String versionComment,
                          VersionType versionType, boolean isLatestVersion, String initialCreatedBy, Instant initialCreationInstant, Option<Content> content,
                          LeosExportStatus status, List<String> comments) {

        super(LeosCategory.EXPORT, id, name, createdBy, creationInstant, lastModifiedBy, lastModificationInstant,
                versionSeriesId, cmisVersionLabel, versionLabel, versionComment, versionType, isLatestVersion, content);
        this.initialCreatedBy = initialCreatedBy;
        this.initialCreationInstant = initialCreationInstant;
        this.status = status;
        this.comments = comments;
    }

    public final String getInitialCreatedBy() {
        return this.initialCreatedBy;
    }

    public final Instant getInitialCreationInstant() {
        return this.initialCreationInstant;
    }

    public final LeosExportStatus getStatus() { return status; }

    public final List<String> getComments() { return comments; }
}
