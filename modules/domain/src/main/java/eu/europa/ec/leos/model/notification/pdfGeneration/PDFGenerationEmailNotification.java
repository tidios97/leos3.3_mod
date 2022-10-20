package eu.europa.ec.leos.model.notification.pdfGeneration;

import eu.europa.ec.leos.model.notification.EmailNotification;

import java.util.ArrayList;
import java.util.List;

abstract public class PDFGenerationEmailNotification implements EmailNotification {

    private List<String> recipients = new ArrayList<String>();
    private String recipient;
    private String emailBody;
    private String emailSubject;
    private String title;
    private byte[] attachmentContent;
    private String legFileName;

    public PDFGenerationEmailNotification(String recipient, String title, byte[] attachmentContent, String legFileName) {
        this.recipient = recipient;
        addRecipients(recipient);
        this.title = title;
        this.attachmentContent = attachmentContent;
        this.legFileName = legFileName;
    }

    private void addRecipients(String recipient) {
        if(recipient.indexOf(";") != -1) {
           String[] recipientsList = recipient.split(";");
           for(String r : recipientsList) {
               recipients.add(r);
           }
        } else {
            recipients.add(recipient);
        }
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
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public byte[] getAttachmentContent() {
        return attachmentContent;
    }

    public void setAttachmentContent(byte[] attachmentContent) {
        this.attachmentContent = attachmentContent;
    }

    public String getLegFileName() {
        return legFileName;
    }

    public void setLegFileName(String legFileName) {
        this.legFileName = legFileName;
    }

    abstract public String getEmailSubjectKey();
}
