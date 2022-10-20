package eu.europa.ec.leos.ui.event;

import eu.europa.ec.leos.services.export.RelevantElements;

public class CreateExportPackageCleanVersionRequestEvent {

    private final String title;
    private final RelevantElements relevantElements;
    private final Boolean isWithAnnotations;
    private final String printStyle;

    public CreateExportPackageCleanVersionRequestEvent(String title, RelevantElements relevantElements, Boolean isWithAnnotations, String printStyle) {
        this.title = title;
        this.relevantElements = relevantElements;
        this.isWithAnnotations = isWithAnnotations;
        this.printStyle = printStyle;
    }

    public CreateExportPackageCleanVersionRequestEvent(String title, RelevantElements relevantElements, Boolean isWithAnnotations) {
        this.title = title;
        this.relevantElements = relevantElements;
        this.isWithAnnotations = isWithAnnotations;
        this.printStyle = null;
    }

    public String getTitle() { return this.title; }

    public RelevantElements getRelevantElements() { return this.relevantElements; }

    public Boolean isWithAnnotations() {
        return isWithAnnotations;
    }

    public String getPrintStyle() {
        return printStyle;
    }
}