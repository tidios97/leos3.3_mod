package eu.europa.ec.leos.services.collection;

import eu.europa.ec.leos.domain.cmis.LeosPackage;
import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.notification.collaborators.AddCollaborator;
import eu.europa.ec.leos.model.user.Collaborator;
import eu.europa.ec.leos.model.user.Entity;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.permissions.Role;
import eu.europa.ec.leos.security.LeosPermissionAuthorityMap;
import eu.europa.ec.leos.security.LeosPermissionAuthorityMapHelper;
import eu.europa.ec.leos.services.document.SecurityService;
import eu.europa.ec.leos.services.dto.collaborator.CollaboratorDTO;
import eu.europa.ec.leos.services.exception.CollaboratorException;
import eu.europa.ec.leos.services.exception.SendNotificationException;
import eu.europa.ec.leos.services.notification.NotificationService;
import eu.europa.ec.leos.services.store.PackageService;
import eu.europa.ec.leos.services.user.UserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CollaboratorServiceImpl implements CollaboratorService {
    private static final Logger LOG = LoggerFactory.getLogger(CollaboratorServiceImpl.class);

    private final NotificationService notificationService;
    private final MessageHelper messageHelper;
    private final PackageService packageService;
    private final SecurityService securityService;
    private final UserService userService;
    private final LeosPermissionAuthorityMap leosPermissionAuthorityMap;
    private final LeosPermissionAuthorityMapHelper authorityMapHelper;

    @Autowired
    public CollaboratorServiceImpl(NotificationService notificationService, MessageHelper messageHelper, PackageService packageService,
            SecurityService securityService, UserService userService, LeosPermissionAuthorityMap leosPermissionAuthorityMap, LeosPermissionAuthorityMapHelper authorityMapHelper) {
        this.notificationService = notificationService;
        this.messageHelper = messageHelper;
        this.packageService = packageService;
        this.securityService = securityService;
        this.userService = userService;
        this.leosPermissionAuthorityMap = leosPermissionAuthorityMap;
        this.authorityMapHelper = authorityMapHelper;
    }

    @Override
    public List<CollaboratorDTO> getCollaborators(Proposal proposal) {
        LOG.trace("Getting collaborators for proposal {}", proposal);
        return Collections.unmodifiableList(proposal.getCollaborators().stream()
                .map(collaborator -> createCollaboratorDTO(collaborator.getLogin(), collaborator.getRole(), this::getUser, collaborator.getEntity()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList()));
    }

    private Optional<CollaboratorDTO> createCollaboratorDTO(String login, String roleName, Function<String, User> converter, String entityName) {
        try {
            User user = converter.apply(login);
            return Optional.of(new CollaboratorDTO(login, user.getName(), roleName, pickFromUserEntitiesByName(user, entityName)));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Entity pickFromUserEntitiesByName(final User user, final String entityName) {
        return user != null ? user.getEntities().stream()
                .filter(entity -> entity.getName().equals(entityName))
                .findFirst()
                .orElse(null) : null;
    }

    @Override
    public String addCollaborator(Proposal proposal, String userId, String roleName, String selectedEntity, String proposalUrl) {
        LOG.trace("Adding collaborator...{}, with authority {}", userId, roleName);
        final User user = getUser(userId);
        final Role role = getRole(roleName);
        final String entity = getEntity(selectedEntity, user);

        List<XmlDocument> documents = getXmlDocumentsForProposal(proposal.getId());
        if (isCollaboratorPresent(documents, user, role, entity)) {
            throw new CollaboratorException(messageHelper.getMessage("collaborator.message.user.present", user.getLogin(), role.getName(), entity));
        }

        documents.forEach(doc -> updateCollaborators(user, role, entity, doc, false));

        sendNotification(user, entity, role, proposal.getId(), proposalUrl);
        LOG.info("Collaborator '{}', role '{}', entity '{}' inserted to proposal id {}", user.getLogin(), role.getName(), entity, proposal.getId());
        return entity;
    }

    @Override
    public String removeCollaborator(Proposal proposal, String userId, String roleName, String selectedEntity, String proposalUrl) {
        LOG.trace("Removing collaborator...{}, with authority {}", userId, roleName);
        final User user = getUser(userId);
        final Role role = getRole(roleName);
        final String entity = getEntity(selectedEntity, user);

        List<XmlDocument> documents = getXmlDocumentsForProposal(proposal.getId());
        if (!isCollaboratorPresent(documents, user, role, entity)) {
            throw new CollaboratorException(messageHelper.getMessage("collaborator.message.user.notPresent", user.getLogin(), role.getName(), entity));
        }
        if (!hasCollaboratorDifferentRole(documents, user, role)) {
            throw new CollaboratorException(messageHelper.getMessage("collaborator.message.role.different", user.getLogin(), role.getName()));
        }
        if (isCollaboratorLastOwner(documents, user, entity)) {
            throw new CollaboratorException(messageHelper.getMessage("collaborator.message.last.owner.removed", role.getName()));
        }

        documents.forEach(doc -> updateCollaborators(user, role, entity, doc, true));

        sendNotification(user, entity, role, proposal.getId(), proposalUrl);
        LOG.info("Collaborator '{}', role '{}', entity '{}' removed from proposal id {}", user.getLogin(), role.getName(), entity, proposal.getId());
        return entity;
    }

    @Override
    public String editCollaborator(Proposal proposal, String userId, String newRoleName, String selectedEntity, String proposalUrl) {
        final User user = getUser(userId);
        final Role newRole = getRole(newRoleName);
        final String entity = getEntity(selectedEntity, user);
        List<XmlDocument> documents = getXmlDocumentsForProposal(proposal.getId());
        String collaboratorRole = documents.get(0).getCollaborators().stream()
                .filter(c -> user.getLogin().equals(c.getLogin()) && c.getEntity().equals(entity))
                .map(Collaborator::getRole)
                .findFirst()
                .orElse(null);
        if (collaboratorRole == null) {
            LOG.warn("User '{}' does not have any role for Entity '{}'", userId, selectedEntity);
            throw new CollaboratorException(messageHelper.getMessage("collaborator.message.role.notFoundForEntity", messageHelper.getMessage(userId, selectedEntity)));
        }
        Role oldRole = authorityMapHelper.getRoleFromListOfRoles(collaboratorRole);
        LOG.trace("Updating collaborator {}, role {} with new role {}", userId, oldRole.getName(), newRoleName);

        if (isCollaboratorLastOwner(documents, user, entity)) {
            LOG.warn("Should be at least one user with role {}", oldRole);
            throw new CollaboratorException(messageHelper.getMessage("collaborator.message.last.owner.edited", messageHelper.getMessage(oldRole.getMessageKey())));
        }

        documents.forEach(doc -> updateCollaborators(user, newRole, entity, doc, false));
        sendNotification(user, entity, newRole, proposal.getId(), proposalUrl);
        LOG.info("Collaborator '{}', oldRole '{}', entity '{}' updated new role to '{}' for proposal id {}", user.getLogin(), oldRole.getName(), entity, newRole.getName(), proposal.getId());
        return entity;
    }

    private User getUser(String userId) {
        if (StringUtils.isEmpty(userId)) {
            throw new CollaboratorException(messageHelper.getMessage("collaborator.message.user.noId"));
        }
        final String DOMAIN_SEPARATOR = "/";
        if (userId.contains(DOMAIN_SEPARATOR)) {
            userId = userId.substring(userId.lastIndexOf(DOMAIN_SEPARATOR) + 1);
        }

        User user = userService.getUser(userId);
        if (user == null) {
            LOG.warn("User '{}' not found!", userId);
            throw new CollaboratorException(messageHelper.getMessage("collaborator.message.user.notFound", userId));
        }
        return user;
    }

    private String getEntity(String connectedDG, User user) {
        String entity;
        if (connectedDG == null) {
            Entity firstEntity = user.getEntities().get(0);
            if (firstEntity == null) {
                LOG.error("User '{}' has no Entity associated", user.getLogin());
                throw new CollaboratorException(messageHelper.getMessage("collaborator.message.user.noEntity", user.getLogin()));
            }
            entity = firstEntity.getName();
        } else {
            Entity userEntity = user.getEntities().stream()
                    .filter(e -> e.getOrganizationName().equalsIgnoreCase(connectedDG))
                    .findFirst()
                    .orElse(null);
            if (userEntity == null) {
                LOG.error("User '{}' has no Entity with name '{}'", user.getLogin(), connectedDG);
                throw new CollaboratorException(messageHelper.getMessage("collaborator.message.user.unknownEntity", user.getLogin(), connectedDG));
            }
            entity = userEntity.getName();
        }
        return entity;
    }

    private Role getRole(String roleName) {
        if (StringUtils.isEmpty(roleName)) {
            throw new CollaboratorException(messageHelper.getMessage("collaborator.message.role.null"));
        }
        Role role = leosPermissionAuthorityMap.getAllRoles()
                .stream()
                .filter(r -> roleName.equals(r.getName()))
                .findFirst()
                .orElse(null);
        if (role == null) {
            LOG.warn("Role '{}' not found", roleName);
            throw new CollaboratorException(messageHelper.getMessage("collaborator.message.role.notFound", roleName));
        }

        if (!role.isCollaborator()) {
            LOG.warn("Role {}, is not a contributor", roleName);
            throw new CollaboratorException(messageHelper.getMessage("collaborator.message.role.notContributor", roleName));
        }
        return role;
    }

    private boolean isCollaboratorPresent(List<XmlDocument> documents, User user, Role role, String selectedEntity) {
        List<Collaborator> collaborators = documents.get(0).getCollaborators();
        return collaborators.stream()
                .anyMatch(collaborator -> collaborator.getLogin().equals(user.getLogin())
                        && collaborator.getRole().equals(role.getName())
                        && collaborator.getEntity().equals(selectedEntity)
                );
    }

    private boolean hasCollaboratorDifferentRole(List<XmlDocument> documents, User user, Role role) {
        List<Collaborator> collaborators = documents.get(0).getCollaborators();
        return collaborators.stream()
                .filter(collaborator -> collaborator.getLogin().equals(user.getLogin()))
                .anyMatch(collaborator -> role.getName().equals(collaborator.getRole()));
    }

    private boolean isCollaboratorLastOwner(List<XmlDocument> documents, User user, String entity) {
        boolean isLastOwner = false;
        List<Collaborator> collaborators = documents.get(0).getCollaborators();
        collaborators = collaborators.stream()
                .filter(c -> c.getRole().equals("OWNER"))
                .collect(Collectors.toList());
        if (collaborators.size() == 1
                && collaborators.get(0).getLogin().equals(user.getLogin())
                && collaborators.get(0).getEntity().equals(entity)) {
            isLastOwner = true;
        }
        return isLastOwner;
    }

    private void sendNotification(User user, String selectedEntity, Role role, String proposalId, String proposalUrl) {
        try {
            LOG.trace("Sending email to new collaborator user {}", user.getLogin());
            notificationService.sendNotification(new AddCollaborator(user, selectedEntity, role.getName(), proposalId, proposalUrl));
        } catch (Exception e) {
            LOG.warn("Unexpected error occurred while sending notification to user {}", user.getLogin(), e);
            throw new SendNotificationException("Unexpected error occurred while sending notification to user " + user.getLogin(), e);
        }
    }

    private List<XmlDocument> getXmlDocumentsForProposal(String proposalId) {
        LeosPackage leosPackage = packageService.findPackageByDocumentId(proposalId);
        return packageService.findDocumentsByPackagePath(leosPackage.getPath(), XmlDocument.class, false);
    }

    //Update document based on action(add/edit/remove)
    private void updateCollaborators(User user, Role role, String selectedEntity, XmlDocument doc, boolean isRemoveAction) {
        Validate.notNull(doc, "The document must not be null!");
        Validate.notNull(user, "The user must not be null!");
        List<Collaborator> collaborators = doc.getCollaborators();

        if (collaborators != null) {
            collaborators.removeIf(c -> c == null || c.getLogin() == null || (c.getLogin().equals(user.getLogin()) &&
                    (c.getEntity() == null || selectedEntity == null || c.getEntity().equals(selectedEntity))));
            if (!isRemoveAction) {
                //pick selectedEntity or first found entity if no selectedEntity defined
                String newEntity = selectedEntity;
                if (newEntity == null) {
                    newEntity = user.getEntities().get(0) != null ? user.getEntities().get(0).getName() : null;
                }
                collaborators.add(new Collaborator(user.getLogin(), role.getName(), newEntity));
            }
            securityService.updateCollaborators(doc.getId(), collaborators, doc.getClass());
        }
    }
}