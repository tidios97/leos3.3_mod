package eu.europa.ec.leos.ui.event;

import eu.europa.ec.leos.services.export.RelevantElements;

public class CreateExportPackageActualVersionRequestEvent {

    private final String title;
    private final RelevantElements relevantElements;
    private final Boolean isWithAnnotations;

    public CreateExportPackageActualVersionRequestEvent(String title, RelevantElements relevantElements, Boolean isWithAnnotations) {
        this.title = title;
        this.relevantElements = relevantElements;
        this.isWithAnnotations = isWithAnnotations;
    }

    public String getTitle() { return this.title; }

    public RelevantElements getRelevantElements() { return this.relevantElements; }

    public Boolean isWithAnnotations() {
        return isWithAnnotations;
    }
}