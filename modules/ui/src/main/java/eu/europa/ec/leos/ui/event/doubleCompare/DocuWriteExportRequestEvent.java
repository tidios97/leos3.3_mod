package eu.europa.ec.leos.ui.event.doubleCompare;

import eu.europa.ec.leos.services.export.ExportOptions;

public class DocuWriteExportRequestEvent {

    private final ExportOptions exportOptions;

    public DocuWriteExportRequestEvent(ExportOptions exportOptions) {
        this.exportOptions = exportOptions;
    }

    public ExportOptions getExportOptions() {
        return exportOptions;
    }

}
