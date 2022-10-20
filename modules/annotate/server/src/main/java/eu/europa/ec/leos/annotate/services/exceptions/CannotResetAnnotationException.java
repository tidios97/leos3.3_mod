package eu.europa.ec.leos.annotate.services.exceptions;

/**
 * Exception that is thrown when resetting an annotation to normal fails
 * The inner exception gives more details
 */
public class CannotResetAnnotationException extends Exception {

	private static final long serialVersionUID = 1L;

    public CannotResetAnnotationException(final Throwable exc) {
        super("The annotation status could not be reset to normal", exc);
    }
    
    public CannotResetAnnotationException(final String message) {
        super(message);
    }
}
