package eu.europa.ec.leos.model.notification.cloneProposal;

import eu.europa.ec.leos.model.notification.EmailNotification;

import java.util.ArrayList;
import java.util.List;

public class RevisionDoneNotification implements EmailNotification {

    private List<String> recipients = new ArrayList<String>();
    private String recipient;
    private String emailBody;
    private String emailSubject;
    private String title;
    private String proposalUrl;
    private String revisionEntity;

    public RevisionDoneNotification(String recipient, String title, String proposalUrl, String revisionEntity) {
        this.recipient = recipient;
        addRecipients(recipient);
        this.title = title;
        this.proposalUrl = proposalUrl;
        this.revisionEntity = revisionEntity;
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

    @Override
    public byte[] getAttachmentContent() { return new byte[0]; }

    @Override
    public String getAttachmentName() { return null; }

    @Override
    public String getMimeType() { return null; }

    @Override
    public boolean withAttachment() { return false; }

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

    public String getEmailSubjectKey() { return "notification.clone.proposal.subject"; };

    public String getProposalUrl() { return proposalUrl; }

    public void setProposalUrl(String proposalUrl) { this.proposalUrl = proposalUrl; }

    public String getRevisionEntity() {
        return revisionEntity;
    }

    public void setRevisionEntity(String revisionEntity) {
        this.revisionEntity = revisionEntity;
    }
}
