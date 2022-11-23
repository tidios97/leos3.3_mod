package eu.europa.ec.leos.services.dto.request;

public class ExportPdfRequest {
    private String proposalRef;
    private String exportOutput;

    public String getProposalRef() {
        return proposalRef;
    }

    public void setProposalRef(String proposalRef) {
        this.proposalRef = proposalRef;
    }

    public String getExportOutput() {
        return exportOutput;
    }

    public void setExportOutput(String exportOutput) {
        this.exportOutput = exportOutput;
    }

    @Override
    public String toString() {
        return "ExportPdfRequest{" +
                "proposalRef='" + proposalRef + '\'' +
                ", exportOutput='" + exportOutput + '\'' +
                '}';
    }
}
