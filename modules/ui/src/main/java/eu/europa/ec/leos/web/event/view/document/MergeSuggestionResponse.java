package eu.europa.ec.leos.web.event.view.document;

public class MergeSuggestionResponse {
    private String message;
    private Result result;
    
    public enum Result {
        SUCCESS,ERROR;
    }

    public MergeSuggestionResponse(String message, Result result) {
        this.message = message;
        this.result = result;
    }

    /**
     * @return the result
     */
    public Result getResult() {
        return result;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }
}
