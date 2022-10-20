package eu.europa.ec.leos.ui.event.view;

import eu.europa.ec.leos.services.export.ExportOptions;

public class ToolBoxExportRequestEvent {
    
    final ExportOptions exportOptions;
    
    public ToolBoxExportRequestEvent(ExportOptions exportOptions) {
        this.exportOptions = exportOptions;
    }
    
    public ExportOptions getExportOptions() {
        return exportOptions;
    }
}