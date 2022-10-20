package eu.europa.ec.leos.domain.vo;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

import static eu.europa.ec.leos.util.LeosDomainUtil.getLeosDateAsString;

public class CloneProposalMetadataVO {

    private String legFileName;
    private String targetUser;
    private Date creationDate;
    private String revisionStatus;

    private boolean clonedProposal;
    private String clonedFromRef;
    private String clonedFromObjectId;
    private String originRef;

    public String getLegFileName() {
        return legFileName;
    }

    public void setLegFileName(String legFileName) {
        this.legFileName = legFileName;
    }

    public String getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(String targetUser) { this.targetUser = targetUser; }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getRevisionStatus() {
        return revisionStatus;
    }

    public void setRevisionStatus(String revisionStatus) {
        this.revisionStatus = revisionStatus;
    }

    public boolean isClonedProposal() {
        return clonedProposal;
    }

    public void setClonedProposal(boolean clonedProposal) {
        this.clonedProposal = clonedProposal;
    }

    public String getClonedFromRef() {
        return clonedFromRef;
    }

    public void setClonedFromRef(String clonedFromRef) {
        this.clonedFromRef = clonedFromRef;
    }

    public String getClonedFromObjectId() {
        return clonedFromObjectId;
    }

    public void setClonedFromObjectId(String clonedFromObjectId) {
        this.clonedFromObjectId = clonedFromObjectId;
    }

    public String getOriginRef() {
        return originRef;
    }

    public void setOriginRef(String originRef) {
        this.originRef = originRef;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof CloneProposalMetadataVO)) {
            return false;
        }
        CloneProposalMetadataVO castOther = (CloneProposalMetadataVO) other;
        return new EqualsBuilder()
                .append(legFileName, castOther.legFileName)
                .append(targetUser, castOther.targetUser)
                .append(getLeosDateAsString(creationDate), getLeosDateAsString(castOther.creationDate))
                .append(revisionStatus, castOther.revisionStatus)
                .append(clonedProposal, castOther.clonedProposal)
                .append(clonedFromRef, castOther.clonedFromRef)
                .append(clonedFromObjectId, castOther.clonedFromObjectId)
                .append(originRef, castOther.originRef)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(legFileName)
                .append(targetUser)
                .append(getLeosDateAsString(creationDate))
                .append(revisionStatus)
                .append(clonedProposal)
                .append(clonedFromRef)
                .append(clonedFromObjectId)
                .append(originRef)
                .toHashCode();
    }

    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("legFileName", legFileName)
                .append("targetUser", targetUser)
                .append("creationDate", creationDate)
                .append("revisionStatus", revisionStatus)
                .append("clonedProposal", clonedProposal)
                .append("clonedFromRef", clonedFromRef)
                .append("clonedFromObjectId", clonedFromObjectId)
                .append("originRef", originRef)
                .toString();
    }
}
