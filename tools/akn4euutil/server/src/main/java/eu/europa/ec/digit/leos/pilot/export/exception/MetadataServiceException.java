package eu.europa.ec.digit.leos.pilot.export.exception;

public class MetadataServiceException extends Exception {

    private static final long serialVersionUID = 3979802808087284045L;

    public MetadataServiceException(String errorMessage) {
        super(errorMessage);
    }

    public MetadataServiceException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }
}
