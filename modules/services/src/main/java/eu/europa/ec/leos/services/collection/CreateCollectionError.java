package eu.europa.ec.leos.services.collection;

public class CreateCollectionError {
    private int code;
    private String message;

    //For Jackson
    public CreateCollectionError() {
    }

    public CreateCollectionError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}