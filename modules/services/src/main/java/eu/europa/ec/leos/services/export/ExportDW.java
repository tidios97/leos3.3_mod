package eu.europa.ec.leos.services.export;

import eu.europa.ec.leos.domain.cmis.document.XmlDocument;

public class ExportDW extends ExportOptions {
    
    public static final String PREFIX_DOCUWRITE = "DW_";
    
    public ExportDW(Output exportOutput, boolean withAnnotations) {
        this.exportOutput = exportOutput;
        this.withAnnotations = withAnnotations;
    }
    
    public ExportDW(Output exportOutput) {
        this.exportOutput = exportOutput;
        this.withAnnotations = true;
    }
    
    public <T extends XmlDocument> ExportDW(Output exportOutput, Class<T> fileType, boolean withAnnotations) {
        this.exportOutput = exportOutput;
        this.fileType = fileType;
        this.withAnnotations = withAnnotations;
    }

    public <T extends XmlDocument> ExportDW(Output exportOutput, Class<T> fileType, boolean withAnnotations, boolean withCleanVersion) {
        this.exportOutput = exportOutput;
        this.fileType = fileType;
        this.withAnnotations = withAnnotations;
        this.isCleanVersion = withCleanVersion;
    }
    
    @Override
    public String getWordPrefix() {
        return PREFIX_DOCUWRITE;
    }
    
    @Override
    public String getExportOutputDescription() {
        switch (exportOutput){
            case PDF: return "Pdf";
            case WORD: return "DocuWrite";
            default: return "-";
        }
    }
    
}
