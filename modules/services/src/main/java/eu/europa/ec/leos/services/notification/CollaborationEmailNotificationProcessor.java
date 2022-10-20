package eu.europa.ec.leos.services.notification;

import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.domain.cmis.metadata.ProposalMetadata;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.notification.EmailNotification;
import eu.europa.ec.leos.model.notification.collaborators.CollaboratorEmailNotification;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.permissions.Role;
import eu.europa.ec.leos.security.LeosPermissionAuthorityMapHelper;
import eu.europa.ec.leos.services.document.ProposalService;
import eu.europa.ec.leos.services.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.StringJoiner;

@Component
public class CollaborationEmailNotificationProcessor implements EmailNotificationProcessor<CollaboratorEmailNotification> {
    
    private MessageHelper messageHelper;
    private LeosPermissionAuthorityMapHelper authorityMapHelper;

    private final ProposalService proposalService;
    private final UserService userService;
    private final FreemarkerNotificationProcessor processor;

    @Autowired
    public CollaborationEmailNotificationProcessor(ProposalService proposalService, UserService userService, FreemarkerNotificationProcessor processor,
                                                   MessageHelper messageHelper, LeosPermissionAuthorityMapHelper authorityMapHelper) {
        this.proposalService = proposalService;
        this.userService = userService;
        this.processor = processor;
        this.messageHelper = messageHelper;
        this.authorityMapHelper = authorityMapHelper;
    }

    @Override
    public boolean canProcess(EmailNotification emailNotification) {
        if (CollaboratorEmailNotification.class.isAssignableFrom(emailNotification.getClass())) {
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    public void process(CollaboratorEmailNotification emailNotification) {
        buildEmailBody(emailNotification);
        buildEmailSubject(emailNotification);
    }

    private void buildEmailBody(CollaboratorEmailNotification collaborationEmailNotification) {
        Proposal proposal = proposalService.findProposal(collaborationEmailNotification.getDocumentId());
        collaborationEmailNotification.setLeosAuthorityName(messageHelper.getMessage("notification.collaborator.leosAuthority." + collaborationEmailNotification.getLeosAuthority()));

        collaborationEmailNotification.setTitle(getProposalTitle(proposal));

        authorityMapHelper.getCollaboratorRoles().forEach(role -> {
            String collaboratorTitle = messageHelper.getMessage(role.getMessageKey());
            collaborationEmailNotification.getCollaboratorsMap().put(collaboratorTitle, buildCollaboratorList(proposal, role));
            if(role.isDefaultDocCreationRole()){
                collaborationEmailNotification.getCollaboratorNoteMap().put(collaboratorTitle, messageHelper.getMessage("notification.collaborator.doc.owner.note"));
            } else{
                collaborationEmailNotification.getCollaboratorNoteMap().put(collaboratorTitle, "");
            }
        });
        setCollaboratorPlural(collaborationEmailNotification);
        collaborationEmailNotification.setEmailBody(processor.processTemplate(collaborationEmailNotification));
    }

    private void buildEmailSubject(CollaboratorEmailNotification collaborationEmailNotification) {
        String proposalId = collaborationEmailNotification.getDocumentId();
        Proposal proposal = proposalService.findProposal(proposalId);
        String title = getProposalTitle(proposal);
        String entity = collaborationEmailNotification.getSelectedEntity() != null ? collaborationEmailNotification.getSelectedEntity() : "";
        String role = messageHelper.getMessage("notification.collaborator.leosAuthority." + collaborationEmailNotification.getLeosAuthority());
        collaborationEmailNotification.setLeosAuthorityName(role);
        
        collaborationEmailNotification
                .setEmailSubject(messageHelper.getMessage(collaborationEmailNotification.getEmailSubjectKey(), new Object[]{role, entity, title}));
    }

    private String buildCollaboratorList(Proposal proposal, Role collaboratorAuthority) {
        StringJoiner collaborators = new StringJoiner(", ");
        proposal.getCollaborators().forEach((collaborator) -> {
            User user = userService.getUser(collaborator.getLogin());
            if (collaboratorAuthority != null && collaboratorAuthority.getName().equals(collaborator.getRole())) {
                collaborators.add(user.getName());
            }
        });
        return collaborators.toString();
    }

    private void setCollaboratorPlural(CollaboratorEmailNotification collaborationEmailNotification){
        collaborationEmailNotification.setCollaboratorPlural(messageHelper.getMessage("notification.collaborator.plural"));
    }

    private String getProposalTitle(Proposal proposal) {
        ProposalMetadata proposalMetadata = proposal.getMetadata().get();
        StringBuilder proposalTitle = new StringBuilder(proposalMetadata.getStage()).append(" ");
        proposalTitle.append(proposalMetadata.getType()).append(" ");
        proposalTitle.append(proposalMetadata.getPurpose()).append(" ");
        return proposalTitle.toString();
    }
}
