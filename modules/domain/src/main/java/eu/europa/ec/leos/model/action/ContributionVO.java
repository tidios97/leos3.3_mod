package eu.europa.ec.leos.model.action;

import eu.europa.ec.leos.model.user.Collaborator;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ContributionVO implements Comparable<ContributionVO> {
    private String documentId;
    private ContributionVO.VersionNumber versionNumber;
    private Instant updatedDate;
    private String versionedReference;
    private CheckinCommentVO checkinCommentVO;
    private String contributionCreator;
    private ContributionStatus contributionStatus;
    private List<Collaborator> collaborators;
    private byte[] xmlContent;
    private String legFileName;
    private String documentName;

    public enum ContributionStatus {
        RECEIVED("Contribution received"),
        CONTRIBUTION_DONE("Contribution done");

        String contributionStatus;

        ContributionStatus(String contributionStatus) {
            this.contributionStatus = contributionStatus;
        }

        public String getValue() {
            return contributionStatus;
        }

        public static ContributionStatus of(String contributionStatus) {
            return Arrays.asList(ContributionStatus.values()).stream()
                    .filter(x -> x.contributionStatus.equals(contributionStatus)).findFirst().orElse(RECEIVED);
        }
    }

    public ContributionStatus getContributionStatus() {
        return contributionStatus;
    }

    public void setContributionStatus(String contributionStatus) {
        this.contributionStatus = ContributionStatus.of(contributionStatus);
    }

    private final static SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public ContributionVO.VersionNumber getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(ContributionVO.VersionNumber versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getUpdatedDate() {
        return dateFormatter.format(Date.from(updatedDate));
    }

    public void setUpdatedDate(Instant updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getUsername() {
        Optional<Collaborator> owner = collaborators.stream().filter(c -> c.getRole().equals("OWNER")).findFirst();
        if (owner.isPresent()) {
            return owner.get().getEntity();
        }
        return "";
    }

    public String getVersionedReference() {
        return versionedReference;
    }

    public void setVersionedReference(String versionedReference) {
        this.versionedReference = versionedReference;
    }

    public CheckinCommentVO getCheckinCommentVO() {
        return checkinCommentVO;
    }

    public void setCheckinCommentVO(CheckinCommentVO checkinCommentVO) {
        this.checkinCommentVO = checkinCommentVO;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getContributionCreator() {
        return contributionCreator;
    }

    public void setContributionCreator(String contributionCreator) {
        this.contributionCreator = contributionCreator;
    }

    public List<Collaborator> getCollaborators() {
        return collaborators;
    }

    public void setCollaborators(List<Collaborator> collaborators) {
        this.collaborators = collaborators;
    }

    public byte[] getXmlContent() {
        return xmlContent;
    }

    public void setXmlContent(byte[] xmlContent) {
        this.xmlContent = xmlContent;
    }

    public String getLegFileName() {
        return legFileName;
    }

    public void setLegFileName(String legFileName) {
        this.legFileName = legFileName;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentId, versionNumber, updatedDate);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final ContributionVO other = (ContributionVO) obj;
        return Objects.equals(this.documentId, other.documentId) &&
                Objects.equals(this.versionNumber, other.versionNumber) &&
                Objects.equals(this.updatedDate, other.updatedDate) &&
                Objects.equals(this.getUsername(), other.getUsername());
    }

    @Override
    public String toString() {
        return "[documentId: " + documentId
                + ", versionNumber: " + versionNumber
                + ", updatedDate: " + updatedDate
                + ", checkinCommentVO: " + checkinCommentVO
                + "]" + "\n";
    }

    @Override
    public int compareTo(ContributionVO contributionVO) {
        int compareTo = versionNumber.compareTo(contributionVO.versionNumber);
        if (compareTo == 0) {
            if (ContributionStatus.CONTRIBUTION_DONE.equals(contributionStatus)
                    && ContributionStatus.RECEIVED.equals(contributionVO.getContributionStatus())) {
                return -1;
            } else {
                return 1;
            }
        } else {
            return compareTo;
        }
    }

    public static class VersionNumber implements Comparable<ContributionVO.VersionNumber> {
        private final int[] versions;
        public VersionNumber(String versionNumber) {
            versions = Stream.of(versionNumber.split("[.]")).mapToInt(Integer::parseInt).toArray();
        }

        @Override public String toString() {
            return Arrays.stream(versions).mapToObj(String::valueOf).collect(Collectors.joining("."));
        }

        private int getPosition(int position) {
            if (versions.length > position) {
                return versions[position];
            } else {
                return 0;
            }
        }

        public int getMajor() {
            return getPosition(0);
        }

        public int getIntermediate() {
            return getPosition(1);
        }

        public int getMinor() {
            return getPosition(2);
        }

        @Override public int compareTo(ContributionVO.VersionNumber versionNumber) {
            for (int i = 0; i < Math.min(versions.length, versionNumber.versions.length); i++) {
                int res = Integer.compare(versions[i], versionNumber.versions[i]);
                if (res != 0) {
                    return Integer.signum(res);
                }
            }
            return Integer.signum(Integer.compare(versions.length, versionNumber.versions.length));
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ContributionVO.VersionNumber that = (ContributionVO.VersionNumber) o;
            return Arrays.equals(versions, that.versions);
        }

        @Override public int hashCode() {
            return Arrays.hashCode(versions);
        }
    }
}
