package eu.europa.ec.leos.services.collection;

public class CreateCollectionException extends Exception {

    private static final long serialVersionUID = -8129444997857365440L;

    public CreateCollectionException(String errorMessage) {
        super(errorMessage);
    }
}