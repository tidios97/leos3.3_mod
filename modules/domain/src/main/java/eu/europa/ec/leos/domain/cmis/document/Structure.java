package eu.europa.ec.leos.domain.cmis.document;

import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.metadata.StructureMetaData;
import io.atlassian.fugue.Option;

import java.time.Instant;

public final class Structure extends XmlDocument {

    private final Option<StructureMetaData> metadata;

    public Structure(String id, String name, String createdBy, Instant creationInstant, String lastModifiedBy,
                     Instant lastModificationInstant, String versionSeriesId, String cmisVersionLabel, String versionLabel, String versionComment,
                     VersionType versionType, boolean isLatestVersion, Option<Content> content, Option<StructureMetaData> metadata) {
        super(LeosCategory.STRUCTURE, id, name, createdBy, creationInstant, lastModifiedBy, lastModificationInstant,
                versionSeriesId, cmisVersionLabel, versionLabel, versionComment, versionType, isLatestVersion, content);
        this.metadata = metadata;
    }

    public final Option<StructureMetaData> getMetadata() {
        return this.metadata;
    }

    @Override
    public String getContributionStatus() {
        return null;
    }
}
