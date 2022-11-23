package eu.europa.ec.leos.services.dto.request;

public class UpdateProposalRequest {

    private String docPurpose;
    private Boolean eeaRelevance;

    public String getDocPurpose() {
        return docPurpose;
    }

    public Boolean isEeaRelevance() {
        return eeaRelevance;
    }

    public void setDocPurpose(String docPurpose) {
        this.docPurpose = docPurpose;
    }

    public void setEeaRelevance(Boolean eeaRelevance) {
        this.eeaRelevance = eeaRelevance;
    }

    @Override
    public String toString() {
        return "UpdateProposalRequest{" +
                "docPurpose='" + docPurpose + '\'' +
                ", eeaRelevance=" + eeaRelevance +
                '}';
    }
}
