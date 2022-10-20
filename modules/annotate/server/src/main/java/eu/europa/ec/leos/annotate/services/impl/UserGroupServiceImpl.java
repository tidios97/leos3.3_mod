package eu.europa.ec.leos.annotate.services.impl;

import eu.europa.ec.leos.annotate.Authorities;
import eu.europa.ec.leos.annotate.model.GroupComparator;
import eu.europa.ec.leos.annotate.model.UserDetails;
import eu.europa.ec.leos.annotate.model.UserEntity;
import eu.europa.ec.leos.annotate.model.UserInformation;
import eu.europa.ec.leos.annotate.model.entity.Group;
import eu.europa.ec.leos.annotate.model.entity.User;
import eu.europa.ec.leos.annotate.model.entity.UserGroup;
import eu.europa.ec.leos.annotate.model.web.helper.JsonConverter;
import eu.europa.ec.leos.annotate.model.web.user.JsonGroupWithDetails;
import eu.europa.ec.leos.annotate.repository.GroupRepository;
import eu.europa.ec.leos.annotate.repository.UserGroupRepository;
import eu.europa.ec.leos.annotate.repository.UserRepository;
import eu.europa.ec.leos.annotate.services.GroupService;
import eu.europa.ec.leos.annotate.services.UserGroupService;
import eu.europa.ec.leos.annotate.services.exceptions.DefaultGroupNotFoundException;
import eu.europa.ec.leos.annotate.services.impl.util.InternalGroupName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserGroupServiceImpl implements UserGroupService {

    private static final Logger LOG = LoggerFactory.getLogger(UserGroupServiceImpl.class);

    /**
     * default group name injected from configuration
     */
    @Value("${defaultgroup.name}")
    private String defaultGroupName;

    // -------------------------------------
    // Required services and repositories
    // -------------------------------------

    private GroupService groupService;
    private GroupRepository groupRepos;
    private UserRepository userRepository;
    private UserGroupRepository userGroupRepos;

    /*
    // custom constructor in order to ease testability by benefiting from mock object injection via Mockito
    public UserGroupServiceImpl(final UserRepository userRepos, final GroupService groupService,
                           @Value("${feature.client.forwardAnnotations}") boolean canForwardAnnotations) {
        this.userRepository = userRepos;
        this.groupService = groupService;
    }*/

    // -------------------------------------
    // Constructors & Setters
    // -------------------------------------

    public UserGroupServiceImpl() {
        // default
    }

    @Autowired
    public UserGroupServiceImpl(final GroupService groupService, final GroupRepository groupRepos,
            final UserRepository userRepository, final UserGroupRepository userGroupRepos) {

        this.groupService = groupService;
        this.groupRepos = groupRepos;
        this.userRepository = userRepository;
        this.userGroupRepos = userGroupRepos;
    }

    // possibility to inject a custom group service - USED FOR TESTING, NOT CONTAINED IN PUBLIC SERVICE INTERFACE
    @Override
    public void setGroupService(final GroupService grpServ) {
        this.groupService = grpServ;
    }

    public void setGroupRepos(final GroupRepository groupRepos) {
        this.groupRepos = groupRepos;
    }

    public void setUserRepository(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void setUserGroupRepos(final UserGroupRepository userGroupRepos) {
        this.userGroupRepos = userGroupRepos;
    }

    // -------------------------------------
    // Service functionality
    // -------------------------------------

    @Override
    public void assignUserToDefaultGroup(final User user) throws DefaultGroupNotFoundException {

        final Group defaultGroup = groupService.findDefaultGroup();
        if (defaultGroup == null) {
            LOG.error("Cannot assign user to the default group; seems not to be configured in database");
            throw new DefaultGroupNotFoundException();
        }

        assignUserToGroup(user, defaultGroup);
    }

    @Override
    public boolean assignUserToGroup(final User user, final Group group) {

        Assert.notNull(user, "User must be defined to assign user to group");
        Assert.notNull(group, "Group must be defined to assign user to group");

        // check if already assigned
        if (isUserMemberOfGroup(user, group)) {
            LOG.info("User '{}' is already member of group '{}' - nothing to do", user.getLogin(), group.getName());
            return true;
        }

        final long userId = user.getId();
        final long groupId = group.getId();
        final UserGroup foundUserGroup = new UserGroup(userId, groupId);
        userGroupRepos.save(foundUserGroup);
        LOG.info("Saved user '{}' (id {}) as member of group '{}' (id {})", user.getLogin(), userId, group.getName(), groupId);

        return true;
    }

    @Override
    @Transactional
    public boolean removeUserFromGroup(final User user, final Group group) {

        Assert.notNull(user, "User must be defined to remove user to group");
        Assert.notNull(group, "Group must be defined to remove user to group");

        // check if already assigned
        if (!isUserMemberOfGroup(user, group)) {
            LOG.info("User '{}' is no member of group '{}' - nothing to do", user.getLogin(), group.getName());
            return false;
        }

        final long userId = user.getId();
        final long groupId = group.getId();
        userGroupRepos.deleteByUserIdAndGroupId(userId, groupId);
        LOG.info("Removed user '{}' (id {}) as member of group '{}' (id {})", user.getLogin(), userId, group.getName(), groupId);

        return true;
    }

    @Override
    public boolean isUserMemberOfGroup(final User user, final Group group) {

        Assert.notNull(user, "Cannot check if user is group member when no user is given");
        Assert.notNull(group, "Cannot check if user is group member when no group is given");

        final UserGroup membership = userGroupRepos.findByUserIdAndGroupId(user.getId(), group.getId());
        LOG.debug("User '{}' (id {}) is member of group '{}' (id {}): {}", user.getLogin(), user.getId(), group.getName(), group.getId(), membership != null);
        return membership != null;
    }

    @Override
    public boolean isUserMemberOfGroup(final User user, final Group group, final String authority) {
        if (!StringUtils.hasLength(authority) || !Authorities.isSupport(authority)) {
            return isUserMemberOfGroup(user, group);
        }

        // User with Support authority are members of any group
        LOG.debug("User '{}' (id {}) requested membership for group '{}' (id {})", user.getLogin(), user.getId(), group.getName(), group.getId());
        return true;
    }

    @Override
    public List<Group> getGroupsOfUser(final User user) {

        Assert.notNull(user, "Cannot search for groups of undefined User (null)");

        final List<UserGroup> foundUserGroups = userGroupRepos.findByUserId(user.getId());
        LOG.debug("Found {} groups in which user '{}' is member", foundUserGroups == null ? 0 : foundUserGroups.size(), user.getLogin());

        if (foundUserGroups == null) {
            return null;
        }

        // extract groupIds of found assignments and get corresponding groups
        return groupRepos.findByIdIn(foundUserGroups.stream().map(UserGroup::getGroupId).collect(Collectors.toList()));
    }

    @Override
    public List<Long> getGroupIdsOfUser(final User user) {

        if (user == null) {
            LOG.warn("Cannot retrieve group IDs from undefined user");
            return null;
        }

        final List<UserGroup> userGroups = userGroupRepos.findByUserId(user.getId());
        if (userGroups == null) return null;

        // extract the groupIds
        return userGroups.stream().map(UserGroup::getGroupId).distinct().collect(Collectors.toList());
    }

    @Override
    public List<Long> getGroupIdsOfUser(final User user, final String authority) {
        final List<Group> allGroups = getGroupsOfUser(user, authority);
        // extract the groupIds
        return allGroups == null ? null : allGroups.stream().map(Group::getId).distinct().collect(Collectors.toList());
    }

    @Override
    public List<String> getGroupNamesOfUser(final User user, final String authority) {
        final List<Group> allGroups = getGroupsOfUser(user, authority);
        // extract the groupIds
        return allGroups == null ? null : allGroups.stream().map(Group::getName).distinct().collect(Collectors.toList());
    }

    private List<Group> getGroupsOfUser(final User user, final String authority) {
        if (!StringUtils.hasLength(authority) || !Authorities.isSupport(authority)) {
            return getGroupsOfUser(user);
        }

        if (user == null) {
            LOG.warn("Cannot retrieve group IDs from undefined user");
            return null;
        }

        // for the support users, we pretend they are members of every group
        // note: there will be at least one group, namely the default group
        return (List<Group>) groupRepos.findAll();
    }

    @Override
    public List<Long> getUserIdsOfGroup(final Group group) {

        if (group == null) {
            LOG.warn("Cannot retrieve user IDs from undefined group");
            return null;
        }

        final List<UserGroup> userGroups = userGroupRepos.findByGroupId(group.getId());
        if (userGroups == null) return null;

        // extract the userId
        return userGroups.stream().map(UserGroup::getUserId).collect(Collectors.toList());
    }

    @Override
    public List<Long> getUserIdsOfGroup(final String groupName) {

        final Group group = groupService.findGroupByName(groupName);
        return getUserIdsOfGroup(group);
    }

    @Override
    public List<JsonGroupWithDetails> getUserGroupsAsJson(final UserInformation userinfo) {

        List<Group> allGroups;

        if (userinfo == null || userinfo.getUser() == null || !StringUtils.hasLength(userinfo.getAuthority())) {
            LOG.info("Groups retrieval request received without user - return default group only");
            allGroups = new ArrayList<>();
            allGroups.add(groupService.findDefaultGroup());
        } else {

            final User user = userinfo.getUser();
            allGroups = getGroupsOfUser(user);
            if (allGroups == null) {
                LOG.warn("Did not receive a valid result from querying groups of user");
                return null;
            }
            LOG.debug("Found {} groups for user '{}'", allGroups.size(), user.getLogin());

            if (Authorities.isIsc(userinfo.getAuthority())) {
                // for ISC, we should not return the default group -> filter out
                LOG.debug("Remove default group for ISC user {}", user.getLogin());
                allGroups.remove(groupService.findDefaultGroup());
            }
        }

        // sort the groups as desired
        allGroups.sort(new GroupComparator(defaultGroupName));

        return JsonConverter.convertToJsonGroupWithDetailsList(allGroups);
    }

    @Override
    public Group getConnectedEntityGroup(final UserInformation userInfo) {

        if (userInfo == null) {
            return null;
        }

        if (StringUtils.hasLength(userInfo.getConnectedEntity())) {
            return groupService.findGroupByName(userInfo.getConnectedEntity());
        }

        return null;
    }

    @Override
    @Transactional
    public boolean addUserToEntityGroup(final UserInformation userInfo) {

        Assert.notNull(userInfo, "Received invalid user object when trying to assign user to entity-based group");

        final User user = userInfo.getUser();
        Assert.notNull(user, "Received invalid user object when trying to assign user to entity-based group");

        final UserDetails userDetails = userInfo.getUserDetails();
        Assert.notNull(userDetails, "Received invalid user details object when trying to assign user to entity-based group");

        if (CollectionUtils.isEmpty(userDetails.getEntities())) {
            LOG.debug("User '{}' does not have any entity assigned; won't assign to any further group though", userDetails.getLogin());
            return false;
        }

        boolean result = true;
        final List<UserEntity> entitiesToAssign = Collections.synchronizedList(userDetails.getEntities());

        synchronized (entitiesToAssign) {
            if (!CollectionUtils.isEmpty(userDetails.getAllEntities())) {
                entitiesToAssign.addAll(userDetails.getAllEntities());
            }

            // if we reach this point, the user is associated to at least one entity
            // -> for each entity: retrieve or create the corresponding group, and assign
            for (final UserEntity entity : entitiesToAssign) {
                Group entityGroup = groupService.findGroupByName(entity.getName());
                if (entityGroup == null) {

                    // group not yet defined, so this must be the first user associated to the entity logging in -> create non-public group
                    try {
                        entityGroup = groupService.createGroup(entity.getName(), false);
                    } catch (Exception e) {
                        LOG.error("Received error creating group:", e);
                    }
                    if (entityGroup == null) {
                        LOG.warn("It seems the new group with name '{}' could not be created! Cannot assign user '{}' to it.", entity.getName(),
                                userDetails.getLogin());
                        return false;
                    }
                }

                // now the group is available - either was already, or has newly been created -> assign user
                result = this.assignUserToGroup(user, entityGroup) && result;
            }
        }

        // remove all group memberships not covered by the given entities any more
        // to identify these, get all groups and filter out the ones given from UD-repo and the default group
        final List<Group> groupsOfUser = this.getGroupsOfUser(user);

        // note: as the group name might have been modified in order to be URL-compliant, we need to map these group names in the same way
        final List<String> entityNames = userDetails.getEntities().stream()
                .map(ent -> InternalGroupName.getInternalGroupName(ent.getName()))
                .collect(Collectors.toList());

        final String defGroupName = groupService.getDefaultGroupName();
        final List<Group> superfluousGroups = groupsOfUser.stream().filter(
                grp -> !grp.getName().equals(defGroupName) && !entityNames.contains(grp.getName()))
                .collect(Collectors.toList());

        superfluousGroups.forEach(group -> this.removeUserFromGroup(user, group));

        return result;
    }

    @Override
    public User getExecutingUser(final UserInformation userInfo, final Group group) {

        User executingUser = userInfo.getUser();
        if (executingUser == null) {
            executingUser = userRepository.findByLoginAndContext(userInfo.getLogin(), userInfo.getContext());
        }

        if (!this.isUserMemberOfGroup(executingUser, group, userInfo.getAuthority())) {
            LOG.info("User {} - {} is not member of group {}, so he may not see any content", executingUser.getLogin(), executingUser.getContext(),
                    group.getName());
            return null;
        }

        return executingUser;
    }
}
