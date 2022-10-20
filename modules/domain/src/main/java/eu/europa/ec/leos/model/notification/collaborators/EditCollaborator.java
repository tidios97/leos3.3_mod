package eu.europa.ec.leos.model.notification.collaborators;

import eu.europa.ec.leos.model.user.User;

public class EditCollaborator extends CollaboratorEmailNotification {

    public EditCollaborator(User recipient, String selectedEntity, String leosAuthority, String documentId, String link) {
        super(recipient, selectedEntity, leosAuthority, documentId, link);
    }

    @Override
    public String getEmailSubjectKey() {
        return "notification.collaborator.edited.subject";
    }

    @Override
    public boolean withAttachment() {
        return false;
    }

    @Override
    public byte[] getAttachmentContent() {
        return null;
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