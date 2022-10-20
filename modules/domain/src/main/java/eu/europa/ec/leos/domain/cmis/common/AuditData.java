package eu.europa.ec.leos.domain.cmis.common;

import java.time.Instant;
import java.util.Objects;

public class AuditData implements Auditable {

    private final String createdBy;
    private final Instant creationInstant;
    private final String lastModifiedBy;
    private final Instant lastModificationInstant;

    public AuditData(String createdBy, Instant creationInstant, String lastModifiedBy, Instant lastModificationInstant) {
        this.createdBy = createdBy;
        this.creationInstant = creationInstant;
        this.lastModifiedBy = lastModifiedBy;
        this.lastModificationInstant = lastModificationInstant;
    }

    @Override
    public String getCreatedBy() {
        return this.createdBy;
    }

    @Override
    public Instant getCreationInstant() {
        return this.creationInstant;
    }

    @Override
    public String getLastModifiedBy() {
        return this.lastModifiedBy;
    }

    @Override
    public Instant getLastModificationInstant() {
        return this.lastModificationInstant;
    }

    @Override
    public String toString() {
        return "AuditData{" +
                "createdBy='" + createdBy + '\'' +
                ", creationInstant=" + creationInstant +
                ", lastModifiedBy='" + lastModifiedBy + '\'' +
                ", lastModificationInstant=" + lastModificationInstant +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditData auditData = (AuditData) o;
        return Objects.equals(createdBy, auditData.createdBy) &&
                Objects.equals(creationInstant, auditData.creationInstant) &&
                Objects.equals(lastModifiedBy, auditData.lastModifiedBy) &&
                Objects.equals(lastModificationInstant, auditData.lastModificationInstant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(createdBy, creationInstant, lastModifiedBy, lastModificationInstant);
    }
}
