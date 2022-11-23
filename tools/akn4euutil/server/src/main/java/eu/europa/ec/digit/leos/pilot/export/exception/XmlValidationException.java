package eu.europa.ec.digit.leos.pilot.export.exception;

public class XmlValidationException extends Exception {
    public XmlValidationException(String message) { super(message); }
    public XmlValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}