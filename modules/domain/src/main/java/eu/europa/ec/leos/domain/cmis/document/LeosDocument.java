package eu.europa.ec.leos.domain.cmis.document;

import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.common.AuditData;
import eu.europa.ec.leos.domain.cmis.common.Auditable;
import eu.europa.ec.leos.domain.cmis.common.VersionData;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.common.Versionable;
import io.atlassian.fugue.Option;

import java.time.Instant;

public abstract class LeosDocument implements Auditable, Versionable {

    private final LeosCategory category;
    private final String id;
    private final String name;
    private final Option<Content> content;
    private final AuditData auditData;
    private final VersionData versionData;

    protected LeosDocument(LeosCategory category, String id, String name, String createdBy, Instant creationInstant,
                           String lastModifiedBy, Instant lastModificationInstant, String versionSeriesId,
                           String cmisVersionLabel, String versionLabel, String versionComment, VersionType versionType,
                           boolean isLatestVersion, Option<Content> content) {
        this.auditData = new AuditData(createdBy, creationInstant, lastModifiedBy, lastModificationInstant);
        this.versionData = new VersionData(versionSeriesId, cmisVersionLabel, versionLabel, versionComment, versionType, isLatestVersion);
        this.category = category;
        this.id = id;
        this.name = name;
        this.content = content;
    }

    public LeosCategory getCategory() {
        return category;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Option<Content> getContent() {
        return content;
    }

    public String getCreatedBy() {
        return auditData.getCreatedBy();
    }

    public Instant getCreationInstant() {
        return auditData.getCreationInstant();
    }

    public Instant getLastModificationInstant() {
        return auditData.getLastModificationInstant();
    }

    public String getLastModifiedBy() {
        return auditData.getLastModifiedBy();
    }

    public String getVersionComment() {
        return versionData.getVersionComment();
    }

    public String getCmisVersionLabel() {
            return versionData.getCmisVersionLabel();
        }
        
    public String getVersionLabel() {
        return versionData.getVersionLabel();
    }

    public String getVersionSeriesId() {
        return versionData.getVersionSeriesId();
    }

    public boolean isLatestVersion() {
        return versionData.isLatestVersion();
    }

    public VersionType getVersionType() {
        return versionData.getVersionType();
    }

}
