package eu.europa.ec.leos.domain.cmis.common;

import java.time.Instant;

public interface Auditable {
    String getCreatedBy();

    Instant getCreationInstant();

    String getLastModifiedBy();

    Instant getLastModificationInstant();
}
