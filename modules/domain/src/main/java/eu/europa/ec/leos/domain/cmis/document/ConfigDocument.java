package eu.europa.ec.leos.domain.cmis.document;

import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import io.atlassian.fugue.Option;

import java.time.Instant;

public final class ConfigDocument extends LeosDocument {
    public ConfigDocument(String id, String name, String createdBy, Instant creationInstant, String lastModifiedBy,
                          Instant lastModificationInstant, String versionSeriesId, String cmisVersionLabel, String versionLabel, String versionComment,
                          VersionType versionType, boolean isLatestVersion, Option<Content> content) {

        super(LeosCategory.CONFIG, id, name, createdBy, creationInstant, lastModifiedBy, lastModificationInstant,
                versionSeriesId, cmisVersionLabel, versionLabel, versionComment, versionType, isLatestVersion, content);
    }
}
