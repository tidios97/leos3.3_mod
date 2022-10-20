package eu.europa.ec.leos.model.notification.validation;

import eu.europa.ec.leos.model.notification.EmailNotification;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

abstract public class ValidationEmailNotification implements EmailNotification {

    private List<String> recipients = new ArrayList<String>();
    private List<String> errors;
    private String title;
    private String updatedBy;
    private Date updatedOn;
    private String recipient;
    private String emailBody;
    private String emailSubject;

    public ValidationEmailNotification(String recipient, List<String> errors, String updatedBy, Date updatedOn, String title) {
        this.errors = errors;
        this.updatedBy = updatedBy;
        this.updatedOn = updatedOn;
        this.recipient = recipient;
        this.title = title;
        recipients.add(recipient);

    }

    @Override
    public List<String> getRecipients() {
        return recipients;
    }

    @Override
    public String getNotificationName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getEmailSubject() {
        return emailSubject;
    }

    @Override
    public String getEmailBody() {
        return emailBody;
    }

    public List<String> getErrors() {
        return errors;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }

    public void setEmailBody(String emailBody) {
        this.emailBody = emailBody;
    }

    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }

    public String getTitle() {
        return title;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    abstract public String getEmailSubjectKey();

}
