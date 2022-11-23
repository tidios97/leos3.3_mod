package eu.europa.ec.leos.services.collection;

import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.services.dto.collaborator.CollaboratorDTO;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface CollaboratorService {

    @PreAuthorize("hasPermission(#proposal, 'CAN_ADD_REMOVE_COLLABORATOR')")
    String addCollaborator(Proposal proposal, String userId, String proposalRef, String roleName, String proposalUrl);

    @PreAuthorize("hasPermission(#proposal, 'CAN_ADD_REMOVE_COLLABORATOR')")
    String removeCollaborator(Proposal proposal, String userId, String proposalRef, String roleName, String proposalUrl);

    @PreAuthorize("hasPermission(#proposal, 'CAN_ADD_REMOVE_COLLABORATOR')")
    String editCollaborator(Proposal proposal, String userId, String proposalRef, String roleName, String proposalUrl);

    @PreAuthorize("hasPermission(#proposal, 'CAN_ADD_REMOVE_COLLABORATOR')")
    List<CollaboratorDTO> getCollaborators(Proposal proposal);
}
