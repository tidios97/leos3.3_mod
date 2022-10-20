package eu.europa.ec.leos.model.notification.collaborators;

import java.util.*;

import eu.europa.ec.leos.model.notification.EmailNotification;
import eu.europa.ec.leos.model.user.User;

abstract public class CollaboratorEmailNotification implements EmailNotification {
    private List<String> recipients = new ArrayList<>();
    private String leosAuthority;
    private String leosAuthorityName;
    private String link;
    private String documentId;
    private User recipient;
    private String title;
    private String emailBody;
    private String emailSubject;
    private String collaboratorPlural;
    private Map<String, String> collaboratorsMap = new LinkedHashMap<>();
    private Map<String, String> collaboratorNoteMap = new HashMap<>();
    private String selectedEntity;

    public CollaboratorEmailNotification(User recipient, String selectedEntity, String leosAuthority, String documentId, String link) {
        this.documentId = documentId;
        this.link = link;
        this.leosAuthority = leosAuthority;
        this.recipient = recipient;
        recipients.add(recipient.getEmail());
        this.selectedEntity = selectedEntity;
    }
    
    public String getEmailBody() {
        return emailBody;
    }

    public String getEmailSubject() {
        return emailSubject;
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public String getNotificationName() {
        return this.getClass().getSimpleName();
    }


    public String getDocumentId() {
        return documentId;
    }

    public User getRecipient() {
        return recipient;
    }

    public String getLink() {
        return link;
    }

    public String getLeosAuthority() {
        return leosAuthority;
    }

    public String getLeosAuthorityName() {
        return leosAuthorityName;
    }

    public void setLeosAuthorityName(String leosAuthorityName) {
        this.leosAuthorityName = leosAuthorityName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setEmailBody(String emailBody) {
        this.emailBody = emailBody;
    }
    
    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }

    public Map<String, String> getCollaboratorsMap() {
        return collaboratorsMap;
    }

    public Map<String, String> getCollaboratorNoteMap() {
        return collaboratorNoteMap;
    }

    public String getCollaboratorPlural() {
        return collaboratorPlural;
    }

    public void setCollaboratorPlural(String collaboratorPlural) {
        this.collaboratorPlural = collaboratorPlural;
    }

    abstract public String getEmailSubjectKey();

    public String getSelectedEntity() {
        return selectedEntity;
    }

    public void setSelectedEntity(String selectedEntity) {
        this.selectedEntity = selectedEntity;
    }
}
