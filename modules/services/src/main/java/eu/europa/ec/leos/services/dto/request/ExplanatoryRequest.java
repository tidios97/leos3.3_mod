package eu.europa.ec.leos.services.dto.request;

public class ExplanatoryRequest {
    private String proposalRef;
    private String template;

    public String getProposalRef() {
        return proposalRef;
    }

    public void setProposalRef(String proposalRef) {
        this.proposalRef = proposalRef;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    @Override
    public String toString() {
        return "ExplanatoryRequest{" +
                "proposalRef='" + proposalRef + '\'' +
                ", template='" + template + '\'' +
                '}';
    }
}
