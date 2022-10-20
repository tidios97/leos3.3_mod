package eu.europa.ec.leos.services.export;

import eu.europa.ec.leos.domain.cmis.document.XmlDocument;

public class ExportLW extends ExportOptions {
    
    public static final String PREFIX_LEGISWRITE = "LW_";

    public ExportLW(Output exportOutput) {
        this.exportOutput = exportOutput;
        this.withAnnotations = true;
    }

    public ExportLW(Output exportOutput, boolean withAnnotations, ComparisonType comparisonType) {
        this.exportOutput = exportOutput;
        this.comparisonType = comparisonType;
        this.withAnnotations = withAnnotations;
    }
    
    public ExportLW(Output exportOutput, boolean withAnnotations) {
        this.exportOutput = exportOutput;
        this.withAnnotations = withAnnotations;
    }

    public <T extends XmlDocument> ExportLW(Output exportOutput, Class<T> fileType, boolean withAnnotations) {
        this.exportOutput = exportOutput;
        this.fileType = fileType;
        this.withAnnotations = withAnnotations;
    }

    public <T extends XmlDocument> ExportLW(Output exportOutput, Class<T> fileType, boolean withAnnotations, boolean withCleanVersion) {
        this.exportOutput = exportOutput;
        this.fileType = fileType;
        this.withAnnotations = withAnnotations;
        this.isCleanVersion = withCleanVersion;
    }

    public ExportLW(String output) {
        switch (output.toLowerCase()) {
            case "pdf":
                exportOutput = Output.PDF;
                break;
            case "lw":
                exportOutput = Output.WORD;
                break;
            default:
                throw new IllegalArgumentException("Wrong value on parameter output type '" + output + "'");
        }
    }
    
    @Override
    public String getWordPrefix() {
        return PREFIX_LEGISWRITE;
    }
    
    @Override
    public String getExportOutputDescription() {
        switch (exportOutput){
            case PDF: return "Pdf";
            case WORD: return "Legiswrite";
            default: return "-";
        }
    }
}
