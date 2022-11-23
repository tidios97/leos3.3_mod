package eu.europa.ec.leos.services.exception;

public class SendNotificationException extends RuntimeException {

    public SendNotificationException(String message, Exception e) {
        super(message, e);
    }
}
