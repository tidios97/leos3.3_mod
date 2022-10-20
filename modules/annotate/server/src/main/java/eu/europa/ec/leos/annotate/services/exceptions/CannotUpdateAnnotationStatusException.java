package eu.europa.ec.leos.annotate.services.exceptions;

/**
 * Exception that is thrown when it's not possible to update the status of annotations
 * The message gives more details about the reason
 */
public class CannotUpdateAnnotationStatusException extends Exception {

    private static final long serialVersionUID = 1L;

    public CannotUpdateAnnotationStatusException(final Throwable exc) {
        super(exc);
    }

    public CannotUpdateAnnotationStatusException(final String msg) {
        super(msg);
    }
    
    public CannotUpdateAnnotationStatusException(final String msg, final Throwable exc) {
        super(msg, exc);
    }
}
