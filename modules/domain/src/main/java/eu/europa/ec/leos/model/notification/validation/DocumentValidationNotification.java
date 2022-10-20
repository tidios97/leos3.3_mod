package eu.europa.ec.leos.model.notification.validation;

import java.util.Date;
import java.util.List;

public class DocumentValidationNotification extends ValidationEmailNotification {

    public DocumentValidationNotification(String recipient, List<String> errors, String updatedBy, Date updatedOn, String title) {
        super(recipient, errors, updatedBy, updatedOn, title);
    }

    @Override
    public String getEmailSubjectKey() {
        return "notification.validation.document.subject";
    }

    @Override
    public byte[] getAttachmentContent() {
        return null;
    }

    @Override
    public boolean withAttachment() {
        return false;
    }

    @Override
    public String getAttachmentName() {
        return "";
    }

    @Override
    public String getMimeType() {
        return "";
    }

}
