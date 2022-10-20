package eu.europa.ec.leos.annotate.services.exceptions;

/**
 * Exception that is thrown when treating an annotation fails
 * The inner exception gives more details
 */
public class CannotTreatAnnotationException extends Exception {

	private static final long serialVersionUID = 1L;

    public CannotTreatAnnotationException(final Throwable exc) {
        super("The annotation could not be treated", exc);
    }
    
    public CannotTreatAnnotationException(final String message) {
        super(message);
    }
}
