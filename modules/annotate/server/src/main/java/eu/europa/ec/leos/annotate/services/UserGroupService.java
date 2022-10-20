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
package eu.europa.ec.leos.annotate.services;

import eu.europa.ec.leos.annotate.model.UserInformation;
import eu.europa.ec.leos.annotate.model.entity.Group;
import eu.europa.ec.leos.annotate.model.entity.User;
import eu.europa.ec.leos.annotate.model.web.user.JsonGroupWithDetails;
import eu.europa.ec.leos.annotate.services.exceptions.DefaultGroupNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserGroupService {

    /**
     * determine the {@link User} running the search - and check if he is member of the requested group at all
     *
     * @param userInfo
     *        {@link UserInformation} containing user details
     * @param group
     *        {@link Group} for which the user requested to retrieve information
     * @return {@link User} object being member of given group, or {@literal null}
     */
    User getExecutingUser(final UserInformation userInfo, final Group group);

    /**
     * add a user as a member to the given group
     *
     * @param user
     *        {@link User} to be assigned to a group
     * @param entityGroup
     *        {@link Group} to which the user is to be assigned to
     * @return flag indicating success, i.e. whether the user is a group member in the end
     */
    boolean assignUserToGroup(User user, Group entityGroup);

    // possibility to inject a custom group service - USED FOR TESTING, NOT CONTAINED IN PUBLIC SERVICE INTERFACE
    void setGroupService(GroupService grpServ);

    /**
     * add a user as member to the default group
     *
     * @param user
     *        {@link User} to be assigned to the default group
     * @throws DefaultGroupNotFoundException
     *         thrown when the default group can be found in the database
     */
    void assignUserToDefaultGroup(User user) throws DefaultGroupNotFoundException;

    /**
     * add a user to a group if it has its associate entity property set
     *
     *  @param userInfo the {@link UserInformation} collected about the user, contains info from our database and from UD repo
     *
     *  @return flag indicating success or nothing to do; FALSE indicates a problem
     */
    @Transactional
    boolean addUserToEntityGroup(UserInformation userInfo);

    /**
     * remove a user as member from the given group
     *
     * @param user
     *        {@link User} to be removed from a group
     * @param group
     *        {@link Group} from which the user is to be removed
     * @return flag indicating success, i.e. whether the user was a member of the group and removed
     */
    @Transactional
    boolean removeUserFromGroup(User user, Group group);

    /**
     * check if a given user is member of a given group
     *
     * @param user
     *        {@link User} to be checked for group membership
     * @param group
     *        {@link Group} which needs to be checked if the user is a member
     * @return flag indicating if the user is member of the given group
     */
    boolean isUserMemberOfGroup(User user, Group group);

    /**
     * check if a given user is member of a given group
     *
     * @param user
     *        {@link User} to be checked for group membership
     * @param group
     *        {@link Group} which needs to be checked if the user is a member
     * @param authority
     *        authority of the requesting user
     * @return flag indicating if the user is member of the given group
     */
    boolean isUserMemberOfGroup(final User user, final Group group, String authority);

    /**
     * retrieve all groups a user is member of
     *
     * @param user
     *        {@link User} for which all group memberships are wanted
     * @return list of {@link Group}s the user is member of, or {@literal null} when none are found
     */
    List<Group> getGroupsOfUser(User user);

    /**
     * retrieve all IDs of the groups that the user belongs to
     *
     * @param user
     *        {@link User} for which all group memberships are wanted
     * @return list of IDs of the {@link Group} the user is member of, or {@literal null}
     */
    List<Long> getGroupIdsOfUser(User user);

    /**
     * retrieve all IDs of the groups that the user belongs to
     *
     * @param user
     *        {@link User} for which all group memberships are wanted
     * @param authority
     *        authority of the requesting user
     * @return list of IDs of the {@link Group} the user is member of, or {@literal null}
     */
    List<Long> getGroupIdsOfUser(User user, String authority);

    /**
     * retrieve all Names of the groups that the user belongs to
     *
     * @param user
     *        {@link User} for which all group memberships are wanted
     * @param authority
     *        authority of the requesting user
     * @return list of IDs of the {@link Group} the user is member of, or {@literal null}
     */
    List<String> getGroupNamesOfUser(User user, String authority);


    /**
     * retrieve all IDs of the users belonging to a given group
     *
     * @param group
     *        {@link Group} whose members are wanted
     * @return list of IDs of the users being member of the given group, or {@literal null}
     */
    List<Long> getUserIdsOfGroup(Group group);

    /**
     * retrieve all IDs of the users belonging to a given group
     *
     * @param groupName
     *        name of the group whose members are wanted
     * @return list of IDs of the users being member of the given group, or {@literal null}
     */
    List<Long> getUserIdsOfGroup(String groupName);

    /**
     * find all groups of which a user is member and provide their details in the JSON format; ISC users are treated slightly different than others
     *
     * @param userinfo
     *        {@link UserInformation} of the user for whom all group information is wanted
     * @return list of {@link JsonGroupWithDetails} objects containing all group information
     *         if no user is given (=not logged in), only the default group is returned
     */
    List<JsonGroupWithDetails> getUserGroupsAsJson(UserInformation userinfo);

    /**
     * returns the group of the user's connected entity, if specified and found in DB
     *
     * @param userInfo
     *        information about the user and especially his connected entity
     * @return found group, or {@literal null}
     */
    Group getConnectedEntityGroup(UserInformation userInfo);

}
