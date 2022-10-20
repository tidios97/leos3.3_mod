package eu.europa.ec.leos.ui.event;

import eu.europa.ec.leos.services.export.ExportOptions;

public class CreateExportPackageRequestEvent {

    private final String title;
    private final ExportOptions exportOptions;

    public CreateExportPackageRequestEvent(String title, ExportOptions exportOptions) {
        this.title = title;
        this.exportOptions = exportOptions;
    }

    public String getTitle() { return this.title; }

    public ExportOptions getExportOptions() { return this.exportOptions; }
}