package eu.europa.ec.leos.web.event.view.document;

public class CollaboratorsUpdatedEvent {

    public static enum Operation {
        ADDED, EDITED, REMOVED
    }

    private final String presenterId;
    private final String documentId;
    private final Operation operation;

    public CollaboratorsUpdatedEvent(String presenterId, String documentId,
            Operation operation) {
        this.presenterId = presenterId;
        this.documentId = documentId;
        this.operation = operation;
    }

    public String getPresenterId() {
        return presenterId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public Operation getOperation() {
        return operation;
    }
}
