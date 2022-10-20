package eu.europa.ec.leos.services.export;

public class ExportLeos extends ExportOptions {
    
    public ExportLeos(Output exportOutput) {
        this.exportOutput = exportOutput;
        this.withAnnotations = false;
    }

    @Override
    public String getWordPrefix() {
        throw new IllegalStateException("Not supported for this implementation");
    }
    
    @Override
    public String getExportOutputDescription() {
        switch (exportOutput){
            case PDF: return "Pdf";
            case WORD: return "Word";
            default: return "-";
        }
    }
}

