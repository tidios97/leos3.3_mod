/*
 * Copyright 2022 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package eu.europa.ec.leos.services.controllers;

import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.services.collection.CollaboratorService;
import eu.europa.ec.leos.services.document.ProposalService;
import eu.europa.ec.leos.services.dto.collaborator.CollaboratorDTO;
import eu.europa.ec.leos.services.exception.CollaboratorException;
import eu.europa.ec.leos.services.exception.SendNotificationException;
import eu.europa.ec.leos.services.request.CollaboratorRequest;
import eu.europa.ec.leos.services.support.url.CollectionUrlBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/secured/proposal")
public class CollaboratorController {

    private static final Logger LOG = LoggerFactory.getLogger(CollaboratorController.class);

    private final CollaboratorService collaboratorService;
    private final ProposalService proposalService;
    private final CollectionUrlBuilder urlBuilder;

    @Autowired
    public CollaboratorController(CollaboratorService collaboratorService, ProposalService proposalService, CollectionUrlBuilder urlBuilder) {
        this.collaboratorService = collaboratorService;
        this.proposalService = proposalService;
        this.urlBuilder = urlBuilder;
    }

    @RequestMapping(value = "/{proposalRef}/collaborators", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Object> getAllCollaboratorFromProposal(@PathVariable("proposalRef") String proposalRef) {
        try {
            Proposal proposal = proposalService.findProposalByRef(proposalRef);
            List<CollaboratorDTO> collaborators = collaboratorService.getCollaborators(proposal);
            return new ResponseEntity<>(collaborators, HttpStatus.OK);
        } catch (CollaboratorException e) {
            String msg = "Error occurred while getting collaborators for Proposal '" + proposalRef + "'";
            LOG.error(msg);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            String msg = "Error occurred while getting collaborators for Proposal '" + proposalRef + "'";
            LOG.error(msg, e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/{proposalRef}/collaborators", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Object> addCollaboratorToProposal(@PathVariable("proposalRef") String proposalRef, @RequestBody CollaboratorRequest collaboratorRequest) {
        final String userId = collaboratorRequest.getUserId();
        final String roleName = collaboratorRequest.getRoleName();
        final String connectedDG = collaboratorRequest.getConnectedDG();
        try {
            Proposal proposal = proposalService.findProposalByRef(proposalRef);
            String proposalUrl = urlBuilder.buildProposalViewUrl(proposalRef);
            String entity = collaboratorService.addCollaborator(proposal, userId, roleName, connectedDG, proposalUrl);
            return new ResponseEntity<>("User '" + userId + "' [entity: '" + entity + "'], added successfully to document '" + proposalRef + "' with role '" + roleName + "'", HttpStatus.OK);
        } catch (CollaboratorException | SendNotificationException e) {
            String msg = "Error occurred while adding User '" + userId + "' [entity '" + connectedDG + "'], as role '" + roleName + "' to proposal '" + proposalRef + "'";
            LOG.error(msg);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            String msg = "Error occurred while adding User '" + userId + "' [entity '" + connectedDG + "'], as role '" + roleName + "' to proposal '" + proposalRef + "'";
            LOG.error(msg, e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/{proposalRef}/collaborators", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<Object> editCollaboratorFromProposal(@PathVariable("proposalRef") String proposalRef, @RequestBody CollaboratorRequest collaboratorRequest) {
        final String userId = collaboratorRequest.getUserId();
        final String newRoleName = collaboratorRequest.getRoleName();
        final String connectedDG = collaboratorRequest.getConnectedDG();
        try {
            LOG.info("Updating new Role '{}' of User '{}' [entity '{}'], for proposal '{}'", newRoleName, userId, connectedDG, proposalRef);
            Proposal proposal = proposalService.findProposalByRef(proposalRef);
            String proposalUrl = urlBuilder.buildProposalViewUrl(proposalRef);
            String entity = collaboratorService.editCollaborator(proposal, userId, newRoleName, connectedDG, proposalUrl);
            return new ResponseEntity<>("User '" + userId + "' [entity: '" + entity + "'], updated successfully for proposal '" + proposalRef + "' with new role '" + newRoleName + "'", HttpStatus.OK);
        } catch (CollaboratorException | SendNotificationException e) {
            String msg = "Error occurred while updating new Role '" + newRoleName + "' of User '" + userId + "' [entity '" + connectedDG + "'], for proposal '" + proposalRef + "'";
            LOG.error(msg);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            String msg = "Error occurred while updating new Role '" + newRoleName + "' of User '" + userId + "' [entity '" + connectedDG + "'], for proposal '" + proposalRef + "'";
            LOG.error(msg, ex);
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/{proposalRef}/collaborators", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Object> removeCollaboratorFromProposal(@PathVariable("proposalRef") String proposalRef, @RequestBody CollaboratorRequest collaboratorRequest) {
        final String userId = collaboratorRequest.getUserId();
        final String roleName = collaboratorRequest.getRoleName();
        final String connectedDG = collaboratorRequest.getConnectedDG();
        try {
            LOG.info("Removing User '{}' [entity '{}'], from proposal '{}' with role '{}'", userId, connectedDG, proposalRef, roleName);
            Proposal proposal = proposalService.findProposalByRef(proposalRef);
            String proposalUrl = urlBuilder.buildProposalViewUrl(proposalRef);
            String entity = collaboratorService.removeCollaborator(proposal, userId, roleName, connectedDG, proposalUrl);
            return new ResponseEntity<>("User '" + userId + "' [entity: '" + entity + "'], removed successfully from proposal '" + proposalRef + "' with role '" + roleName + "'", HttpStatus.OK);
        } catch (CollaboratorException | SendNotificationException e) {
            String msg = "Error occurred while removing User '" + userId + "' [entity '" + connectedDG + "'], as role '" + roleName + "' from proposal '" + proposalRef + "'";
            LOG.error(msg);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            String msg = "Error occurred while removing User '" + userId + "' [entity '" + connectedDG + "'], as role '" + roleName + "' from proposal '" + proposalRef + "'";
            LOG.error(msg, ex);
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
