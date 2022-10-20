package eu.europa.ec.leos.ui.event.view;

import eu.europa.ec.leos.services.export.ExportOptions;

public class DownloadXmlFilesRequestEvent {
    
    final ExportOptions exportOptions;
    
    public DownloadXmlFilesRequestEvent(ExportOptions exportOptions) {
        this.exportOptions = exportOptions;
    }
    
    public ExportOptions getExportOptions() {
        return exportOptions;
    }
}
