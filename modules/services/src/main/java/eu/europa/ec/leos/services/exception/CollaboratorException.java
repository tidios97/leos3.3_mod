package eu.europa.ec.leos.services.exception;

public class CollaboratorException extends RuntimeException {

    public CollaboratorException(String message, Exception e) {
        super(message, e);
    }

    public CollaboratorException(String message) {
        super(message);
    }
}
