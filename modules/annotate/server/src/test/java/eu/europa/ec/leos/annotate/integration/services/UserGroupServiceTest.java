/*
 * Copyright 2018-2020 European Commission
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
package eu.europa.ec.leos.annotate.integration.services;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import eu.europa.ec.leos.annotate.Authorities;
import eu.europa.ec.leos.annotate.helper.SpotBugsAnnotations;
import eu.europa.ec.leos.annotate.helper.TestDbHelper;
import eu.europa.ec.leos.annotate.model.UserDetails;
import eu.europa.ec.leos.annotate.model.UserEntity;
import eu.europa.ec.leos.annotate.model.UserInformation;
import eu.europa.ec.leos.annotate.model.entity.Group;
import eu.europa.ec.leos.annotate.model.entity.User;
import eu.europa.ec.leos.annotate.model.entity.UserGroup;
import eu.europa.ec.leos.annotate.model.web.user.JsonGroupWithDetails;
import eu.europa.ec.leos.annotate.repository.GroupRepository;
import eu.europa.ec.leos.annotate.repository.UserGroupRepository;
import eu.europa.ec.leos.annotate.repository.UserRepository;
import eu.europa.ec.leos.annotate.services.GroupService;
import eu.europa.ec.leos.annotate.services.UUIDGeneratorService;
import eu.europa.ec.leos.annotate.services.UserGroupService;
import eu.europa.ec.leos.annotate.services.UserService;
import eu.europa.ec.leos.annotate.services.exceptions.DefaultGroupNotFoundException;
import eu.europa.ec.leos.annotate.services.exceptions.UserAlreadyExistingException;
import eu.europa.ec.leos.annotate.services.impl.UUIDGeneratorServiceImpl;
import eu.europa.ec.leos.annotate.services.impl.util.UserDetailsCache;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.config.name=anot")
@ActiveProfiles("test")
@SuppressWarnings("PMD.TooManyMethods")
public class UserGroupServiceTest {

    private static final String OPEN = "open";
    private static final String GROUPDESC = "Group description";
    
    // -------------------------------------
    // Required services and repositories
    // -------------------------------------
    @Autowired
    private UserGroupService userGroupService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepos;

    @Autowired
    private GroupRepository groupRepos;

    @Autowired
    private UserGroupRepository userGroupRepos;

    @Autowired
    private GroupService groupService;

    @Autowired
    UserDetailsCache userDetailsCache;

    private Group defaultGroup;

    // -------------------------------------
    // Cleanup of database content
    // -------------------------------------
    @Before
    public void cleanDatabaseBeforeTests() {
        TestDbHelper.cleanupRepositories(this);
        userDetailsCache.clear();
    }

    @After
    public void cleanDatabaseAfterTests() {

        TestDbHelper.cleanupRepositories(this);
    }

    // -------------------------------------
    // Tests
    // -------------------------------------

    /**
     * Test that assigning user to default group fails when default group is not configured
     */
    @Test(expected = DefaultGroupNotFoundException.class)
    public void testDefaultGroupRequired() throws UserAlreadyExistingException, DefaultGroupNotFoundException {

        final User user = new User("username");
        userRepos.save(user);

        // in test setup, all groups were removed - i.e. that there is no default group entry available
        userGroupService.assignUserToDefaultGroup(user);
        Assert.fail("Expected exception about missing default group entries not received");
    }

    /**
     * test that assigning user twice to the default group doesn't actually do anything
     * (and does not throw exceptions either) 
     */
    @Test
    public void testNoDoubleAssignmentToDefaultGroup() throws DefaultGroupNotFoundException {

        final User theUser = userRepos.save(new User("me"));
        TestDbHelper.insertDefaultGroup(groupRepos);
        Assert.assertEquals(0, userGroupRepos.count()); // empty

        userGroupService.assignUserToDefaultGroup(theUser);
        Assert.assertEquals(1, userGroupRepos.count()); // assignment entry was created

        // doing it a second time should still not throw any exception
        userGroupService.assignUserToDefaultGroup(theUser);

        Assert.assertEquals(1, userGroupRepos.count()); // and there is still only one assignment entry
    }

    /**
     * check that assignment of users to groups throws exceptions when required parameters are missing
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAssignUserToGroupInvalidParameters1() {

        userGroupService.assignUserToGroup(null, null);
    }

    /**
     * check that assignment of users to groups throws exceptions when required parameters are missing
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAssignUserToGroupInvalidParameters2() {

        final User dummyUser = new User("me");
        userGroupService.assignUserToGroup(dummyUser, null);
    }

    /**
     * test assigning user to a group works, and does not complain when called a second time (but doesn't do anything)
     */
    @Test
    public void testAssignUserToGroup_New_And_AlreadyAssigned() {

        // prepare: create group and user
        final User theUser = userRepos.save(new User("myname"));
        Assert.assertEquals(0, userGroupRepos.count()); // empty

        final Group testGroup = new Group("testgroup", false);
        groupRepos.save(testGroup);

        // test
        Assert.assertEquals(0, userGroupRepos.count()); // no assignments before
        Assert.assertTrue(userGroupService.assignUserToGroup(theUser, testGroup));
        Assert.assertEquals(1, userGroupRepos.count()); // assignment entry was created
        Assert.assertTrue(userGroupService.isUserMemberOfGroup(theUser, testGroup));

        // doing it a second time should work without protest
        Assert.assertTrue(userGroupService.assignUserToGroup(theUser, testGroup));
    }

    /**
     * test that a user's group membership is found correctly
     */
    @Test
    public void testGroupMembership() {

        final User theUser = userRepos.save(new User("someuser"));
        final Group defaultGroup = TestDbHelper.insertDefaultGroup(groupRepos);

        Assert.assertFalse(userGroupService.isUserMemberOfGroup(theUser, defaultGroup));

        // assign user to group now
        userGroupRepos.save(new UserGroup(theUser.getId(), defaultGroup.getId()));

        Assert.assertTrue(userGroupService.isUserMemberOfGroup(theUser, defaultGroup));
    }

    /**
     * test that a user's group membership throws exceptions if required parameters are missing
     */
    @Test(expected = IllegalArgumentException.class)
    @SuppressFBWarnings(value = SpotBugsAnnotations.ExceptionIgnored, justification = SpotBugsAnnotations.ExceptionIgnored)
    public void testGroupMembershipCheckFailures1() {

        userGroupService.isUserMemberOfGroup(null, new Group());
    }

    /**
     * test that a user's group membership throws exceptions if required parameters are missing
     */
    @Test(expected = IllegalArgumentException.class)
    @SuppressFBWarnings(value = SpotBugsAnnotations.ExceptionIgnored, justification = SpotBugsAnnotations.ExceptionIgnored)
    public void testGroupMembershipCheckFailures2() {

        userGroupService.isUserMemberOfGroup(new User(), null);
    }

    /**
     * test search for user's group memberships
     */
    @Test
    public void testGroupsOfUser() {

        final User theUser = userRepos.save(new User("demo2"));
        final User anotherUser = userRepos.save(new User("other"));
        final User grouplessUser = userRepos.save(new User("lonesomeuser"));
        final Group defaultGroup = TestDbHelper.insertDefaultGroup(groupRepos);
        final Group anotherGroup = groupRepos.save(new Group("internalGroupId", "nice group name", "Group desc", false));

        // empty lists received so far as users are not associated to groups yet
        Assert.assertEquals(0, userGroupService.getGroupsOfUser(theUser).size());
        Assert.assertEquals(0, userGroupService.getGroupsOfUser(anotherUser).size());

        // assign users to groups now
        userGroupRepos.save(new UserGroup(theUser.getId(), defaultGroup.getId()));
        userGroupRepos.save(new UserGroup(theUser.getId(), anotherGroup.getId()));
        userGroupRepos.save(new UserGroup(anotherUser.getId(), defaultGroup.getId()));

        // retrieve
        final List<Group> groupsOfTheUser = userGroupService.getGroupsOfUser(theUser);
        final List<Group> groupsOfAnotherUser = userGroupService.getGroupsOfUser(anotherUser);
        final List<Group> groupsOfLonesomeUser = userGroupService.getGroupsOfUser(grouplessUser);

        // verify assignments
        Assert.assertEquals(2, groupsOfTheUser.size());
        Assert.assertEquals(1, groupsOfAnotherUser.size());
        Assert.assertEquals(0, groupsOfLonesomeUser.size());

        Assert.assertTrue(groupsOfTheUser.stream().anyMatch(group -> group.equals(defaultGroup)));
        Assert.assertTrue(groupsOfTheUser.stream().anyMatch(group -> group.equals(anotherGroup)));
        Assert.assertTrue(groupsOfAnotherUser.stream().anyMatch(group -> group.equals(defaultGroup)));

        // make sure that group visibility is correct - check the "groupsOfTheUser": one is public, other one isn't
        Assert.assertTrue(groupsOfTheUser.stream().filter(group -> group.equals(defaultGroup)).collect(Collectors.toList()).get(0).isPublicGroup());
        Assert.assertFalse(groupsOfTheUser.stream().filter(group -> group.equals(anotherGroup)).collect(Collectors.toList()).get(0).isPublicGroup());

        // check retrieval of the user's group IDs
        final List<Long> groupIdsOfTheUser = userGroupService.getGroupIdsOfUser(theUser);
        final List<Long> groupIdsOfAnotherUser = userGroupService.getGroupIdsOfUser(anotherUser);
        final List<Long> groupIdsOfLonesomeUser = userGroupService.getGroupIdsOfUser(grouplessUser);

        // verify received IDs
        Assert.assertTrue(groupIdsOfTheUser.stream().anyMatch(groupId -> groupId.equals(defaultGroup.getId())));
        Assert.assertTrue(groupIdsOfTheUser.stream().anyMatch(groupId -> groupId.equals(anotherGroup.getId())));
        Assert.assertTrue(groupIdsOfAnotherUser.stream().anyMatch(groupId -> groupId.equals(defaultGroup.getId())));
        Assert.assertEquals(0, groupIdsOfLonesomeUser.size());
    }

    /**
     * test the retrieval of a user's group as JSON result for a LEOS/EdiT user
     * (used by groups API)
     */
    @Test
    public void testGroupsOfLeosUserAsJson() {

        final java.util.Random rand = new java.util.Random();

        final String privateGroupName1 = "group nicename";
        final String privateGroupName2 = "other private group nicename";
        final String publicGroupName1 = "the public group";
        final String publicGroupName2 = "a public group";

        // no groups set for the user
        final User theUser = userRepos.save(new User("demo"));
        final UserInformation userinfo = new UserInformation(theUser, Authorities.EdiT);
        Assert.assertEquals(0, userGroupService.getUserGroupsAsJson(userinfo).size());

        // assign user to groups: the public default group, two private ones, two public ones
        final Group defaultGroup = TestDbHelper.insertDefaultGroup(groupRepos);
        final Group firstPrivateGroup = groupRepos.save(new Group(Integer.toString(rand.nextInt()), privateGroupName1, GROUPDESC, false));
        final Group secondPrivateGroup = groupRepos.save(new Group(Integer.toString(rand.nextInt()), privateGroupName2, GROUPDESC, false));
        final Group firstPublicGroup = groupRepos.save(new Group(Integer.toString(rand.nextInt()), publicGroupName1, GROUPDESC, true));
        final Group secondPublicGroup = groupRepos.save(new Group(Integer.toString(rand.nextInt()), publicGroupName2, GROUPDESC, true));

        userGroupRepos.save(new UserGroup(theUser.getId(), defaultGroup.getId()));
        userGroupRepos.save(new UserGroup(theUser.getId(), firstPrivateGroup.getId()));
        userGroupRepos.save(new UserGroup(theUser.getId(), secondPrivateGroup.getId()));
        userGroupRepos.save(new UserGroup(theUser.getId(), firstPublicGroup.getId()));
        userGroupRepos.save(new UserGroup(theUser.getId(), secondPublicGroup.getId()));

        // retrieve
        final List<JsonGroupWithDetails> userGroupsLeos = userGroupService.getUserGroupsAsJson(userinfo);
        Assert.assertNotNull(userGroupsLeos);
        Assert.assertEquals(5, userGroupsLeos.size());

        // verify
        // world public group first
        final JsonGroupWithDetails extractedDefaultGroup = userGroupsLeos.get(0);
        Assert.assertNotNull(extractedDefaultGroup);
        Assert.assertEquals(OPEN, extractedDefaultGroup.getType());
        Assert.assertTrue(extractedDefaultGroup.isPublic());
        Assert.assertFalse(extractedDefaultGroup.isScoped());

        // two public groups next, sorted by display name
        Assert.assertEquals(userGroupsLeos.get(1).getName(), publicGroupName2);
        Assert.assertEquals(userGroupsLeos.get(2).getName(), publicGroupName1);

        // two private groups at the end, sorted by display name
        Assert.assertEquals(userGroupsLeos.get(3).getName(), privateGroupName1);
        Assert.assertEquals(userGroupsLeos.get(4).getName(), privateGroupName2);

        final JsonGroupWithDetails extractedPrivateGroup = userGroupsLeos.get(3);
        Assert.assertNotNull(extractedPrivateGroup);
        Assert.assertEquals("private", extractedPrivateGroup.getType());
        Assert.assertFalse(extractedPrivateGroup.isPublic());
        Assert.assertFalse(extractedPrivateGroup.isScoped());
    }

    /**
     * test the retrieval of a user's group as JSON result for an ISC user (should not show the default group)
     * (used by groups API)
     */
    @Test
    public void testGroupsOfIscUserAsJson() {

        final java.util.Random rand = new java.util.Random();

        final String privateGroupName1 = "group nicename";
        final String privateGroupName2 = "other private group nicename";
        final String publicGroupName1 = "the public group";
        final String publicGroupName2 = "a public group";

        // no groups set for the user
        final User theUser = userRepos.save(new User("demo"));
        final UserInformation userinfo = new UserInformation(theUser, Authorities.ISC);
        Assert.assertEquals(0, userGroupService.getUserGroupsAsJson(userinfo).size());

        // assign user to groups: the public default group, two private ones, two public ones
        final Group defaultGroup = TestDbHelper.insertDefaultGroup(groupRepos);
        final Group firstPrivateGroup = groupRepos.save(new Group(Integer.toString(rand.nextInt()), privateGroupName1, GROUPDESC, false));
        final Group secondPrivateGroup = groupRepos.save(new Group(Integer.toString(rand.nextInt()), privateGroupName2, GROUPDESC, false));
        final Group firstPublicGroup = groupRepos.save(new Group(Integer.toString(rand.nextInt()), publicGroupName1, GROUPDESC, true));
        final Group secondPublicGroup = groupRepos.save(new Group(Integer.toString(rand.nextInt()), publicGroupName2, GROUPDESC, true));

        userGroupRepos.save(new UserGroup(theUser.getId(), defaultGroup.getId()));
        userGroupRepos.save(new UserGroup(theUser.getId(), firstPrivateGroup.getId()));
        userGroupRepos.save(new UserGroup(theUser.getId(), secondPrivateGroup.getId()));
        userGroupRepos.save(new UserGroup(theUser.getId(), firstPublicGroup.getId()));
        userGroupRepos.save(new UserGroup(theUser.getId(), secondPublicGroup.getId()));

        // retrieve - the default group should not be among the returned groups
        final List<JsonGroupWithDetails> userGroupsIsc = userGroupService.getUserGroupsAsJson(userinfo);
        Assert.assertNotNull(userGroupsIsc);
        Assert.assertEquals(4, userGroupsIsc.size());

        // verify
        // two public groups first, sorted by display name
        Assert.assertEquals(userGroupsIsc.get(0).getName(), publicGroupName2);
        Assert.assertEquals(userGroupsIsc.get(1).getName(), publicGroupName1);

        // two private groups at the end, sorted by display name
        Assert.assertEquals(userGroupsIsc.get(2).getName(), privateGroupName1);
        Assert.assertEquals(userGroupsIsc.get(3).getName(), privateGroupName2);

        // check that the default group is not contained
        Assert.assertEquals(0, userGroupsIsc.stream().filter(group -> group.getName().equals(defaultGroup.getName())).count());
    }

    /**
     * test that an exception is thrown when groups of undefined user are requested and not default group is defined
     */
    @Test(expected = NullPointerException.class)
    @SuppressFBWarnings(value = SpotBugsAnnotations.ExceptionIgnored, justification = SpotBugsAnnotations.ExceptionIgnored)
    public void testGroupsOfNullIsNull() {

        // when no user given, we expect the default group to be returned
        // if no default group is defined, there may be an exception
        userGroupService.getUserGroupsAsJson(null);
    }

    /**
     * test that the default group is returned if no user is specified, but default group is defined
     */
    @Test
    @SuppressFBWarnings(value = SpotBugsAnnotations.ExceptionIgnored, justification = SpotBugsAnnotations.ExceptionIgnored)
    public void testGroupsOfNullUserinfo() {

        // define default group, but don't assign any user to it
        // ask -> default group should be returned anyway
        final Group defaultGroup = TestDbHelper.insertDefaultGroup(groupRepos);
        final List<JsonGroupWithDetails> details = userGroupService.getUserGroupsAsJson(null);
        Assert.assertNotNull(details);
        Assert.assertEquals(1, details.size());

        // verify
        // world public group (=default group) is returned
        final JsonGroupWithDetails extractedDefaultGroup = details.get(0);
        Assert.assertNotNull(extractedDefaultGroup);
        Assert.assertEquals(defaultGroup.getDisplayName(), extractedDefaultGroup.getName());
        Assert.assertEquals(defaultGroup.getName(), extractedDefaultGroup.getId());
        Assert.assertEquals(OPEN, extractedDefaultGroup.getType());
        Assert.assertTrue(extractedDefaultGroup.isPublic());
        Assert.assertFalse(extractedDefaultGroup.isScoped());
    }

    /**
     * test that the default group is returned if no particular user is specified, but default group is defined
     */
    @Test
    @SuppressFBWarnings(value = {SpotBugsAnnotations.ExceptionIgnored,
            SpotBugsAnnotations.KnownNullValue}, justification = SpotBugsAnnotations.ExceptionIgnored)
    public void testGroupsOfEmptyUserinfo() {

        // define default group, but don't assign any user to it
        // ask -> default group should be returned anyway
        final Group defaultGroup = TestDbHelper.insertDefaultGroup(groupRepos);
        final User nullUser = null;
        final List<JsonGroupWithDetails> details = userGroupService.getUserGroupsAsJson(new UserInformation(nullUser, ""));
        Assert.assertNotNull(details);
        Assert.assertEquals(1, details.size());

        // verify
        // world public group (=default group) is returned
        final JsonGroupWithDetails extractedDefaultGroup = details.get(0);
        Assert.assertNotNull(extractedDefaultGroup);
        Assert.assertEquals(defaultGroup.getDisplayName(), extractedDefaultGroup.getName());
        Assert.assertEquals(defaultGroup.getName(), extractedDefaultGroup.getId());
        Assert.assertEquals(OPEN, extractedDefaultGroup.getType());
        Assert.assertTrue(extractedDefaultGroup.isPublic());
        Assert.assertFalse(extractedDefaultGroup.isScoped());
    }

    /**
     * test that the default group is returned when no authority is specified
     */
    @Test
    @SuppressFBWarnings(value = SpotBugsAnnotations.ExceptionIgnored, justification = SpotBugsAnnotations.ExceptionIgnored)
    public void testGroupsOfUndefinedAuthority() {

        // define default group, but don't assign any user to it
        // ask -> default group should be returned anyway
        final Group defaultGroup = TestDbHelper.insertDefaultGroup(groupRepos);
        final List<JsonGroupWithDetails> details = userGroupService.getUserGroupsAsJson(new UserInformation(new User("me"), ""));
        Assert.assertNotNull(details);
        Assert.assertEquals(1, details.size());

        // verify
        // world public group (=default group) is returned
        final JsonGroupWithDetails extractedDefaultGroup = details.get(0);
        Assert.assertNotNull(extractedDefaultGroup);
        Assert.assertEquals(defaultGroup.getDisplayName(), extractedDefaultGroup.getName());
        Assert.assertEquals(defaultGroup.getName(), extractedDefaultGroup.getId());
        Assert.assertEquals(OPEN, extractedDefaultGroup.getType());
        Assert.assertTrue(extractedDefaultGroup.isPublic());
        Assert.assertFalse(extractedDefaultGroup.isScoped());
    }

    /**
     * test that no group are returned if user is unknown
     */
    @Test
    @SuppressFBWarnings(value = SpotBugsAnnotations.ExceptionIgnored, justification = SpotBugsAnnotations.ExceptionIgnored)
    public void testGroupsOfUnknownUser() {

        // define default group, but don't assign any user to it
        TestDbHelper.insertDefaultGroup(groupRepos);

        // check for an unknown user - empty result
        final List<JsonGroupWithDetails> details = userGroupService.getUserGroupsAsJson(new UserInformation(new User("unknownUser"), "someauthority"));
        Assert.assertNotNull(details);
        Assert.assertEquals(0, details.size());
    }

    /**
     * test that no group IDs are received when no user is given
     */
    @Test
    public void testGroupIdsOfNullUser() {

        // when no user is given, we expect null to be returned
        Assert.assertNull(userGroupService.getGroupIdsOfUser(null));
    }

    /**
     * test search for users of a group
     */
    @Test
    public void testUsersOfGroup() {

        final User theUser = userRepos.save(new User("demo"));
        final User anotherUser = userRepos.save(new User("other"));
        final Group defaultGroup = TestDbHelper.insertDefaultGroup(groupRepos);
        final Group anotherGroup = groupRepos.save(new Group("internalGroupId", "group nicename", GROUPDESC, false));

        Assert.assertNull(userGroupService.getUserIdsOfGroup(""));
        Assert.assertEquals(0, userGroupService.getUserIdsOfGroup(new Group()).size()); // test with undefined group
        Assert.assertEquals(0, userGroupService.getUserIdsOfGroup(defaultGroup).size());
        Assert.assertEquals(0, userGroupService.getUserIdsOfGroup(anotherGroup).size());

        // assign users to groups now
        userGroupRepos.save(new UserGroup(theUser.getId(), defaultGroup.getId()));
        userGroupRepos.save(new UserGroup(theUser.getId(), anotherGroup.getId()));
        userGroupRepos.save(new UserGroup(anotherUser.getId(), defaultGroup.getId()));

        // retrieve
        final List<Long> usersOfDefaultGroup = userGroupService.getUserIdsOfGroup(defaultGroup);
        final List<Long> usersOfAnotherGroup = userGroupService.getUserIdsOfGroup(anotherGroup.getName()); // test alternative interface

        // verify assignments
        Assert.assertEquals(2, usersOfDefaultGroup.size());
        Assert.assertEquals(1, usersOfAnotherGroup.size());

        Assert.assertTrue(usersOfDefaultGroup.stream().anyMatch(userId -> userId.equals(theUser.getId())));
        Assert.assertTrue(usersOfDefaultGroup.stream().anyMatch(userId -> userId.equals(anotherUser.getId())));
        Assert.assertTrue(usersOfAnotherGroup.stream().anyMatch(userId -> userId.equals(theUser.getId())));
    }

    /**
     * test that a user's group membership search throws exceptions if required parameters are missing
     */
    @Test(expected = IllegalArgumentException.class)
    @SuppressFBWarnings(value = SpotBugsAnnotations.ExceptionIgnored, justification = SpotBugsAnnotations.ExceptionIgnored)
    public void testGroupsOfUserFailure() {

        userGroupService.getGroupsOfUser(null);
        Assert.fail("Expected exception about invalid argument not received!");
    }

    /**
     * test removing a user from a group - but user is undefined
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRemovalOfGroup_UserUndefined() {
        
        userGroupService.removeUserFromGroup(null, null);
    }
    
    /**
     * test removing a user from a group - but group is undefined
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRemovalOfGroup_GroupUndefined() {
        
        final User user = new User("somebody");
        userGroupService.removeUserFromGroup(user, null);
    }
    
    /**
     * test successfully removing a user from a group
     */
    @Test
    public void testRemovalOfGroup() {
        
        final User user = new User("somebody");
        final Group group = new Group("someGroup", false);
        
        userRepos.save(user);
        groupRepos.save(group);
        userGroupRepos.save(new UserGroup(user.getId(), group.getId()));
        Assert.assertEquals(1, userGroupRepos.count());
        
        // act
        Assert.assertTrue(userGroupService.removeUserFromGroup(user, group));
        
        // verify
        Assert.assertEquals(0, userGroupRepos.count());
    }
    
    /**
     * test removing a user from a group - but user is not a member of the group
     */
    @Test
    public void testRemovalOfGroup_UserNotMember() {
        
        final User user = new User("somebody");
        final Group group = new Group("someGroup", false);
        Assert.assertFalse(userGroupService.removeUserFromGroup(user, group));
    }
    
    /**
     * test the retrieval of the connected entity of a user - when there is no user specified
     */
    @Test
    public void testFindConnectedEntity_noUserInfo() {
        
        Assert.assertNull(userGroupService.getConnectedEntityGroup(null));
    }
    
    /**
     * test the retrieval of the connected entity of a user - when there is no entity specified
     */
    @Test
    public void testFindConnectedEntity_noEntitySet() {
        
        final UserInformation userInfo = new UserInformation("jane", null, Authorities.EdiT);
        
        Assert.assertNull(userGroupService.getConnectedEntityGroup(userInfo));
    }
    
    /**
     * test the retrieval of the connected entity of a user - when the entity is unknown
     */
    @Test
    public void testFindConnectedEntity_entityUnknown() {
        
        final UserInformation userInfo = new UserInformation("jane", null, Authorities.EdiT);
        userInfo.setConnectedEntity("someentity");
        Assert.assertNull(userGroupService.getConnectedEntityGroup(userInfo));
    }
    
    /**
     * test the retrieval of the connected entity of a user - when the entity is unknown
     */
    @Test
    public void testFindConnectedEntity_entityKnown() {
        
        final String ENTITY = "ent";
        final String USERNAME = "mary";
        
        final Group group = new Group(ENTITY, false);
        groupRepos.save(group);

        final User user = userRepos.save(new User(USERNAME));
        TestDbHelper.insertDefaultGroup(groupRepos);
        userRepos.save(user);
        
        final UserInformation userInfo = new UserInformation(USERNAME, null, Authorities.EdiT);
        userInfo.setConnectedEntity(ENTITY);
        
        // act
        final Group entGroup = userGroupService.getConnectedEntityGroup(userInfo);
        
        // verify
        Assert.assertNotNull(entGroup);
        Assert.assertEquals(group, entGroup);
    }

    /**
     * test adding users to groups with invalid parameters
     */
    @SuppressFBWarnings(value = SpotBugsAnnotations.KnownNullValue, justification = SpotBugsAnnotations.KnownNullValueReason)
    @Test(expected = IllegalArgumentException.class)
    public void testAssignUserToGroup_InvalidParameter2() {

        final UserDetails userDetails = null;

        userGroupService.addUserToEntityGroup(new UserInformation(null, userDetails, null)); // should throw IllegalArgumentException
    }

    /**
     * test adding users to groups with invalid parameters
     */
    @SuppressFBWarnings(value = SpotBugsAnnotations.KnownNullValue, justification = SpotBugsAnnotations.KnownNullValueReason)
    @Test(expected = IllegalArgumentException.class)
    public void testAssignUserToGroup_InvalidParameter3() {

        final UserDetails userDetails = null;

        final User dummyUser = new User("somelogin");
        userGroupService.addUserToEntityGroup(new UserInformation(dummyUser, userDetails, null)); // should throw IllegalArgumentException
    }

    /**
     * test adding users to groups with invalid parameters
     */
    @SuppressFBWarnings(value = SpotBugsAnnotations.KnownNullValue, justification = SpotBugsAnnotations.KnownNullValueReason)
    @Test
    public void testAssignUserToGroup_InvalidParameter4() {

        final User dummyUser = new User("login2");

        Assert.assertFalse(userGroupService.addUserToEntityGroup(
                new UserInformation(dummyUser, new UserDetails("login2", Long.valueOf(1), "first2", "last2", null, "", null), null)));
    }

    /**
     * test creation of new group for a user and assignment of user to the group
     */
    @Test
    public void testAssignUserToGroup_createNewGroupAndAssign() {

        final String login = "alogin";
        final List<UserEntity> entities = new ArrayList<UserEntity>();
        entities.add(new UserEntity("4", "COMM.D.1", "COMM"));

        // arrange
        final UserDetails details = new UserDetails(login, Long.valueOf(4), "Sledge", "Hammer", entities, "a@c.eu", null);

        final User theUser = new User(login);
        userRepos.save(theUser);

        // only default group defined before
        Assert.assertEquals(0, groupRepos.count());
        Assert.assertEquals(0, userGroupRepos.count());

        // act
        Assert.assertTrue(userGroupService.addUserToEntityGroup(new UserInformation(theUser, details, null)));

        // verify
        // new group
        Assert.assertEquals(1, groupRepos.count());
        final Group createdGroup = groupRepos.findByName(entities.get(0).getName());
        Assert.assertNotNull(createdGroup);

        // user is member of group
        Assert.assertEquals(1, userGroupRepos.count());
        Assert.assertNotNull(userGroupRepos.findByUserIdAndGroupId(theUser.getId(), createdGroup.getId()));
    }

    /**
     * since ANOT-85, the UD-repo may report several entities, and the user is assigned to all those entities
     * but: exactly to those, meaning that old group memberships are removed (ANOT-86)
     * @throws DefaultGroupNotFoundException
     * @throws UserAlreadyExistingException
     */
    @Test
    public void testAssignAndDeleteUserFromGroups() throws UserAlreadyExistingException, DefaultGroupNotFoundException {

        Assert.assertEquals(0, groupRepos.count());
        defaultGroup = TestDbHelper.insertDefaultGroup(groupRepos);
        Assert.assertEquals(1, groupRepos.count());

        userGroupService.setGroupService(groupService);

        final String login = "itsme";
        final String entity = "AGRI";
        final String entity2name = entity + ".2";
        final String entity4name = entity + ".4";

        final List<UserEntity> entities = new ArrayList<UserEntity>();
        entities.add(new UserEntity("1", entity + ".1", entity));
        entities.add(new UserEntity("2", entity2name, entity));

        // arrange
        final UserDetails details = new UserDetails(login, Long.valueOf(4), "Johnny", "Cash", entities, "a@c.eu", null);
        final User theUser = userService.createUser(login, null);

        // only default group defined before, user is member
        Assert.assertEquals(1, groupRepos.count());
        Assert.assertEquals(1, userGroupRepos.count());

        // act
        Assert.assertTrue(userGroupService.addUserToEntityGroup(new UserInformation(theUser, details, null)));

        // verify
        // two new groups
        Assert.assertEquals(3, groupRepos.count());
        Group createdGroup = groupRepos.findByName(entities.get(0).getName());
        Assert.assertNotNull(createdGroup);
        createdGroup = groupRepos.findByName(entities.get(1).getName());
        Assert.assertNotNull(createdGroup);

        // now we want to call the method a second time, this time with entities 2+3+4
        // -> membership of entity group 1 should be removed
        // -> group and membership in group for group 3 should be created
        // -> membership in group 4 should be created (group exists already)

        // create group for entity 4 already
        groupRepos.save(new Group(entity4name, false));

        final List<UserEntity> newEntities = new ArrayList<UserEntity>();
        newEntities.add(new UserEntity("2", entity2name, entity));
        newEntities.add(new UserEntity("2", entity + ".3", entity));
        newEntities.add(new UserEntity("4", entity4name, entity));

        // act
        details.setEntities(newEntities);
        Assert.assertTrue(userGroupService.addUserToEntityGroup(new UserInformation(theUser, details, null)));

        // verify
        // two more new groups - five in total; the two new groups are tested
        Assert.assertEquals(5, groupRepos.count());
        Assert.assertNotNull(groupRepos.findByName(newEntities.get(1).getName()));
        Assert.assertNotNull(groupRepos.findByName(newEntities.get(2).getName()));

        // user is member of four groups now: 2+3+4 and default group
        Assert.assertEquals(4, userGroupRepos.count());
        Assert.assertNotNull(userGroupRepos.findByUserIdAndGroupId(theUser.getId(), groupRepos.findByName(newEntities.get(0).getName()).getId()));
        Assert.assertNotNull(userGroupRepos.findByUserIdAndGroupId(theUser.getId(), groupRepos.findByName(newEntities.get(1).getName()).getId()));
        Assert.assertNotNull(userGroupRepos.findByUserIdAndGroupId(theUser.getId(), groupRepos.findByName(newEntities.get(2).getName()).getId()));
        Assert.assertNotNull(userGroupRepos.findByUserIdAndGroupId(theUser.getId(), defaultGroup.getId()));
    }

    /**
     * test adding users to groups with invalid parameters
     */
    @SuppressFBWarnings(value = SpotBugsAnnotations.KnownNullValue, justification = SpotBugsAnnotations.KnownNullValueReason)
    @Test(expected = IllegalArgumentException.class)
    public void testAssignUserToGroup_InvalidParameter1() {

        userGroupService.addUserToEntityGroup(null);
    }

    /**
     * since ANOT-85, the UD-repo may report several entities, and the user is assigned to all those entities
     * here we check that not only the "entities" property is considered, but also the "allEntities" property
     */
    @Test
    public void testAssignAndDeleteUserFromGroups_AllEntities() throws UserAlreadyExistingException, DefaultGroupNotFoundException {

        Assert.assertEquals(0, groupRepos.count());
        defaultGroup = TestDbHelper.insertDefaultGroup(groupRepos);

        final String login = "itsme";
        final String entity = "AGRI";

        final List<UserEntity> entities = new ArrayList<UserEntity>();
        entities.add(new UserEntity("1", "AGRI.something", entity));

        final List<UserEntity> newEntities = new ArrayList<UserEntity>();
        newEntities.add(new UserEntity("1", entity, entity));
        newEntities.add(new UserEntity("2", entity + ".I", entity));
        newEntities.add(new UserEntity("4", entity + ".I.1", entity));

        // arrange
        final UserDetails details = new UserDetails(login, Long.valueOf(4), "John", "Doe", entities, "a@c.eu", null);
        details.setAllEntities(newEntities); // these are the entities that are primarily tested here
        final User theUser = userService.createUser(login, null);

        // only default group defined before, user is member
        Assert.assertEquals(1, groupRepos.count());
        Assert.assertEquals(1, userGroupRepos.count());

        // act
        Assert.assertTrue(userGroupService.addUserToEntityGroup(new UserInformation(theUser, details, null)));

        // verify
        // four new groups
        Assert.assertEquals(5, groupRepos.count()); // default group and the four new ones
        Assert.assertNotNull(groupRepos.findByName(entity + ".something"));
        Assert.assertNotNull(groupRepos.findByName(entity));
        Assert.assertNotNull(groupRepos.findByName(entity + ".I"));
        Assert.assertNotNull(groupRepos.findByName(entity + ".I.1"));

        // user is member of four groups now: four AGRI groups and default group
        Assert.assertEquals(5, userGroupRepos.count());
        Assert.assertNotNull(userGroupRepos.findByUserIdAndGroupId(theUser.getId(), groupRepos.findByName(entity + ".something").getId()));
        Assert.assertNotNull(userGroupRepos.findByUserIdAndGroupId(theUser.getId(), groupRepos.findByName(entity).getId()));
        Assert.assertNotNull(userGroupRepos.findByUserIdAndGroupId(theUser.getId(), groupRepos.findByName(entity + ".I").getId()));
        Assert.assertNotNull(userGroupRepos.findByUserIdAndGroupId(theUser.getId(), groupRepos.findByName(entity + ".I.1").getId()));
    }

    /**
     * test assignment of a user to an existing group
     */
    @Test
    public void testAssignUserToGroup_groupExistsAlready() {

        Assert.assertEquals(0, groupRepos.count());
        defaultGroup = TestDbHelper.insertDefaultGroup(groupRepos);
        Assert.assertEquals(1, groupRepos.count());

        final String login = "login";
        final String entityName = "AGRI.D.8";
        final List<UserEntity> entities = new ArrayList<UserEntity>();
        entities.add(new UserEntity("4", entityName, "AGRI"));

        // arrange
        final UserDetails details = new UserDetails(login, Long.valueOf(4), "John", "Doe", entities, "a@b.eu", null);

        final User theUser = new User(login);
        userRepos.save(theUser);

        final Group newGroup = new Group(entityName, true);
        groupRepos.save(newGroup);

        // groups defined before, but no memberships
        Assert.assertEquals(2, groupRepos.count());
        Assert.assertEquals(0, userGroupRepos.count());

        // act
        Assert.assertTrue(userGroupService.addUserToEntityGroup(new UserInformation(theUser, details, null)));

        // verify
        Assert.assertEquals(2, groupRepos.count()); // no additional groups, existing was used

        // user is member of group
        Assert.assertEquals(1, userGroupRepos.count());
        Assert.assertNotNull(userGroupRepos.findByUserIdAndGroupId(theUser.getId(), newGroup.getId()));
    }

    /**
     * test assignment of a user to a new group (user's first login), and the group contains a blank
     */
    @Test
    public void testAssignUserToEntityGroup_GroupNameWithBlanks() {

        Assert.assertEquals(0, groupRepos.count());
        defaultGroup = TestDbHelper.insertDefaultGroup(groupRepos);
        Assert.assertEquals(1, groupRepos.count());

        final String login = "demouser";
        final String COMP = "DG COMP";
        final String COMP2 = "DG COMP I3";
        final List<UserEntity> entities = new ArrayList<UserEntity>();
        entities.add(new UserEntity("2", COMP, COMP));
        entities.add(new UserEntity("3", COMP2, COMP));

        // arrange
        final UserDetails details = new UserDetails(login, Long.valueOf(4), "Demo", "User", entities, "demouser@LEOS", null);

        final User theUser = new User(login);
        userRepos.save(theUser);

        // act
        Assert.assertTrue(userGroupService.addUserToEntityGroup(new UserInformation(theUser, details, null)));

        // verify: two groups must have been created, without spaces in their names, but still with spaces in their display names
        // third group is the default group
        Assert.assertEquals(3, groupRepos.count());

        // group is found again, although its internal name is different!
        final Group grpComp = groupRepos.findByName("DGCOMP");
        Assert.assertNotNull(grpComp);
        Assert.assertEquals("DGCOMP", grpComp.getName());
        Assert.assertEquals(COMP, grpComp.getDisplayName()); // display name remains original


        final Group grpComp2 = groupRepos.findByName("DGCOMPI3");
        Assert.assertNotNull(grpComp2);
        Assert.assertEquals("DGCOMPI3", grpComp2.getName());
        Assert.assertEquals(COMP2, grpComp2.getDisplayName());

        // there must be two group memberships - this implicitly checks that groups have been identified superfluous and deleted, which was the case previously
        Assert.assertEquals(2, userGroupRepos.count());
        Assert.assertNotNull(userGroupRepos.findByUserIdAndGroupId(theUser.getId(), grpComp.getId()));
        Assert.assertNotNull(userGroupRepos.findByUserIdAndGroupId(theUser.getId(), grpComp2.getId()));
    }

    /**
     * test assignment of a user to a group does not do anything as the user details don't contain an entity
     */
    @Test
    public void testAssignUserToGroup_noUserEntityDefined() {

        Assert.assertEquals(0, groupRepos.count());
        defaultGroup = TestDbHelper.insertDefaultGroup(groupRepos);

        final String login = "login";

        // arrange
        final UserDetails details = new UserDetails(login, Long.valueOf(4), "John", "Doe", null, "a@b.eu", null); // entity empty

        final User theUser = new User(login);
        userRepos.save(theUser);

        // only default group defined before
        Assert.assertEquals(1, groupRepos.count());
        Assert.assertEquals(0, userGroupRepos.count());

        // act
        Assert.assertFalse(userGroupService.addUserToEntityGroup(new UserInformation(theUser, details, null)));

        // verify
        Assert.assertEquals(1, groupRepos.count()); // no additional group was created

        // user did not get any group membership
        Assert.assertEquals(0, userGroupRepos.count());
    }

    /**
     * test randomized creation of user details and thus group memberships
     */
    @Test
    @SuppressWarnings({"PMD.AvoidLiteralsInIfCondition", "PMD.CyclomaticComplexity",
            "PMD.ModifiedCyclomaticComplexity", "PMD.NPathComplexity"})
    public void testAssignUserToGroup_randomized() {

        Assert.assertEquals(0, groupRepos.count());
        defaultGroup = TestDbHelper.insertDefaultGroup(groupRepos);
        Assert.assertEquals(1, groupRepos.count());

        final int numberOfUsers = 1000;
        final int numberOfEntities = 200;

        final List<User> users = new ArrayList<User>();
        final List<UserDetails> details = new ArrayList<UserDetails>();
        final List<String> entities = new ArrayList<String>();
        final List<Integer> entityStats = new ArrayList<Integer>();

        // prepare user accounts
        for (int i = 0; i < numberOfUsers; i++) {
            final User user = new User("login" + i);
            userRepos.save(user);
            users.add(user);
        }

        // generate entities - use our UUID generator for simplicity
        final UUIDGeneratorService uuidService = new UUIDGeneratorServiceImpl();
        for (int i = 0; i < numberOfEntities; i++) {
            entities.add(uuidService.generateUrlSafeUUID().replaceAll("-", ""));

            // create entry in entity statistics
            entityStats.add(Integer.valueOf(0));
        }

        final java.util.Random rand = new java.util.Random();

        // generate user details
        for (int i = 0; i < numberOfUsers; i++) {

            final UserDetails detail = new UserDetails(users.get(i).getLogin(), users.get(i).getId(), "firstname", "lastname", null, "a@b.eu", null);
            final int generatedValue = rand.nextInt(100) + 1;
            if (generatedValue < 80) { // about 80% of users should have an entity assigned in our test
                final int entityToUse = rand.nextInt(numberOfEntities);
                final UserEntity newEntity = new UserEntity(String.valueOf(i), entities.get(entityToUse), "somename");
                detail.setEntities(Arrays.asList(newEntity));

                // increase statistics counter
                entityStats.set(entityToUse, entityStats.get(entityToUse) + 1);
            }

            details.add(detail);
        }

        // now let all users to assigned to their groups
        for (int i = 0; i < numberOfUsers; i++) {

            // expected result depends on whether an entity is set
            final boolean addResult = userGroupService.addUserToEntityGroup(new UserInformation(users.get(i), details.get(i), null));
            final boolean expectedAddResult = !CollectionUtils.isEmpty(details.get(i).getEntities()) &&
                    StringUtils.hasLength(details.get(i).getEntities().get(0).getName());
            Assert.assertEquals(expectedAddResult, addResult);
        }

        // verify that all groups have been created: default group + new entity groups
        int entityGroupsUsed = 1; // note: the number of actually used entities could be less than the ones defined - depends on randomness...
        for (int i = 0; i < numberOfEntities; i++) {
            if (entityStats.get(i) > 0) {
                entityGroupsUsed++;
            }
        }

        // check that number of groups is correct
        Assert.assertEquals(entityGroupsUsed, groupRepos.count());

        // check that total number of group memberships is correct
        for (int i = 0; i < numberOfEntities; i++) {

            if (entityStats.get(i) == 0) {

                // the entity was not used - thus there shouldn't be a corresponding group in the database
                final Group groupNotToFind = groupRepos.findByName(entities.get(i));
                Assert.assertNull(groupNotToFind);
            } else {
                // the entity was assigned in the randomized procedure above
                // -> find the group in the database...
                final Group groupToFind = groupRepos.findByName(entities.get(i));

                // ... and count the number of users assigned to it - must correspond to the statistics
                Assert.assertEquals(entityStats.get(i).intValue(), userGroupRepos.findByGroupId(groupToFind.getId()).size());
            }
        }
    }
}
