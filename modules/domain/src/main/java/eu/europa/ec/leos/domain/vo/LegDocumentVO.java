package eu.europa.ec.leos.domain.vo;

import java.util.List;
import java.util.Objects;

public class LegDocumentVO {


    private String proposalId;
    private String documentTitle;
    private String milestoneComments;
    private String legFileId;
    private String legFileName;
    private String legFileStatus;
    private String creationDate;
    private boolean clonedProposal;

    public String getProposalId() { return proposalId; }

    public void setProposalId(String proposalId) { this.proposalId = proposalId; }

    public String getDocumentTitle() {
        return documentTitle;
    }

    public void setDocumentTitle(String documentTitle) {
        this.documentTitle = documentTitle;
    }

    public String getMilestoneComments() {
        return milestoneComments;
    }

    public void setMilestoneComments(List<String> milestoneComments) {
        this.milestoneComments = String.join(":", milestoneComments);
    }

    public String getLegFileId() {
        return legFileId;
    }

    public void setLegFileId(String legFileId) {
        this.legFileId = legFileId;
    }

    public String getLegFileStatus() {
        return legFileStatus;
    }

    public void setLegFileStatus(String legFileStatus) {
        this.legFileStatus = legFileStatus;
    }

    public String getLegFileName() {
        return legFileName;
    }

    public void setLegFileName(String legFileName) {
        this.legFileName = legFileName;
    }

    public String getCreationDate() { return creationDate; }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public boolean isClonedProposal() { return clonedProposal; }

    public void setClonedProposal(boolean clonedProposal) { this.clonedProposal = clonedProposal; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LegDocumentVO that = (LegDocumentVO) o;
        return  Objects.equals(getProposalId(), that.getProposalId()) &&
                Objects.equals(getDocumentTitle(), that.getDocumentTitle()) &&
                Objects.equals(getMilestoneComments(), that.getMilestoneComments()) &&
                Objects.equals(getLegFileId(), that.getLegFileId()) &&
                Objects.equals(getLegFileName(), that.getLegFileName()) &&
                Objects.equals(getLegFileStatus(), that.getLegFileStatus());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProposalId(), getDocumentTitle(), getMilestoneComments(), getLegFileId(), getLegFileName(), getLegFileStatus());
    }
}
