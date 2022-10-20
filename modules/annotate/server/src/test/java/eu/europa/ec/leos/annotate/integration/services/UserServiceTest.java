/*
 * Copyright 2018-2022 European Commission
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

import eu.europa.ec.leos.annotate.Authorities;
import eu.europa.ec.leos.annotate.helper.TestDbHelper;
import eu.europa.ec.leos.annotate.model.UserDetails;
import eu.europa.ec.leos.annotate.model.UserEntity;
import eu.europa.ec.leos.annotate.model.UserInformation;
import eu.europa.ec.leos.annotate.model.entity.Group;
import eu.europa.ec.leos.annotate.model.entity.User;
import eu.europa.ec.leos.annotate.model.entity.UserGroup;
import eu.europa.ec.leos.annotate.model.web.user.JsonUserProfile;
import eu.europa.ec.leos.annotate.repository.GroupRepository;
import eu.europa.ec.leos.annotate.repository.UserGroupRepository;
import eu.europa.ec.leos.annotate.repository.UserRepository;
import eu.europa.ec.leos.annotate.services.UserGroupService;
import eu.europa.ec.leos.annotate.services.UserService;
import eu.europa.ec.leos.annotate.services.UserServiceWithTestFunctions;
import eu.europa.ec.leos.annotate.services.exceptions.DefaultGroupNotFoundException;
import eu.europa.ec.leos.annotate.services.exceptions.UserAlreadyExistingException;
import eu.europa.ec.leos.annotate.services.exceptions.UserNotFoundException;
import eu.europa.ec.leos.annotate.services.impl.UserGroupServiceImpl;
import eu.europa.ec.leos.annotate.services.impl.UserServiceImpl;
import eu.europa.ec.leos.annotate.services.impl.util.UserDetailsCache;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.config.name=anot")
@ActiveProfiles("test")
@SuppressWarnings("PMD.TooManyMethods")
// it should also be possible to run these tests with:
// @ActiveProfiles("testUserService")
// the associate test configuration scripts are still available; this should demonstrate that several different test scripts can be used, if needed
public class UserServiceTest {

    /**
     * NOTE: This test class demonstrates how to use a specific profile that does not use the default scripts for creation of the database 
     */

    // -------------------------------------
    // Required services and repositories
    // -------------------------------------
    @Autowired
    private UserService userService;

    @Autowired
    private UserGroupService userGroupService;

    @Autowired
    private UserRepository userRepos;

    @Autowired
    private GroupRepository groupRepos;

    @Autowired
    private UserGroupRepository userGroupRepos;
    
    @Autowired
    private UserDetailsCache userCache;

    private static final Logger Log = LoggerFactory.getLogger(UserServiceTest.class);

    private Group defaultGroup;

    // -------------------------------------
    // Cleanup of database content
    // -------------------------------------
    @Before
    public void cleanDatabaseBeforeTests() {

        TestDbHelper.cleanupRepositories(this);
        defaultGroup = TestDbHelper.insertDefaultGroup(groupRepos);
        
        userCache.clear();
    }

    @After
    public void cleanDatabaseAfterTests() {

        TestDbHelper.cleanupRepositories(this);
    }

    // -------------------------------------
    // Tests
    // -------------------------------------

    /**
     * test that a new user is created based on its login and assigned to the default group
     */
    @Test
    public void testCreateNewUser() {

        final String userLogin = "myuserlogin";

        // initially no users are registered; default group is available (by DB initialization script)
        Assert.assertEquals(0, userRepos.count());
        Assert.assertEquals(0, userGroupRepos.count());
        Assert.assertEquals(1, groupRepos.count());

        User foundUser = userService.findByLoginAndContext(userLogin, null);
        Assert.assertNull(foundUser);

        User newUser = null;

        try {
            newUser = userService.createUser(new User(userLogin));
        } catch (UserAlreadyExistingException | DefaultGroupNotFoundException e) {
            Assert.fail("Unexpected exception when creating new user: " + e);
        }

        Assert.assertNotNull(newUser);
        Assert.assertEquals(1, userRepos.count());
        Assert.assertEquals(1, groupRepos.count()); // still 1, i.e. no additional groups have been created
        Assert.assertEquals(1, userGroupRepos.count());

        // retrieve group ID
        final Group theGroup = groupRepos.findByName("__world__");

        // verify user has been added to default group
        final UserGroup userInGroup = userGroupRepos.findByUserIdAndGroupId(newUser.getId(), theGroup.getId());
        Assert.assertNotNull(userInGroup);

        // verify that user can be found using login
        foundUser = userService.findByLoginAndContext(userLogin, null);
        Assert.assertNotNull(foundUser);
    }

    @Test
    public void testCreateUser_WithContext() {

        final String userLogin = "a";
        final String userContext = "b";

        User userCreated = null;
        try {
            userCreated = userService.createUser(userLogin, userContext);
            Assert.assertNotNull(userCreated);
        } catch (Exception e) {
            Assert.fail("Unexpected exception received: " + e);
        }

        final User userRetrieved = userService.getUserById(userCreated.getId());
        Assert.assertNotNull(userRetrieved);
        Assert.assertEquals(userContext, userRetrieved.getContext());
    }

    @Test
    public void testFindByLoginAndContext() {

        final String userLogin = "a";
        final String userContext = "b";

        User userCreated = null;
        try {
            userCreated = userService.createUser(userLogin, userContext);
            Assert.assertNotNull(userCreated);
        } catch (Exception e) {
            Assert.fail("Unexpected exception received: " + e);
        }

        final User userRetrieved = userService.findByLoginAndContext(userLogin, userContext);
        Assert.assertNotNull(userRetrieved);
    }

    /**
     * test that no user is registered twice (based on its login)
     */
    @Test
    public void testDontCreateDuplicateUser() {

        final String userLogin = "theuserlogin";

        // initially no users are registered; default group is available (by test initialization)
        Assert.assertEquals(0, userRepos.count());
        Assert.assertEquals(0, userGroupRepos.count());
        Assert.assertEquals(1, groupRepos.count());

        User newUser = null;

        try {
            newUser = userService.createUser(userLogin, null);
        } catch (UserAlreadyExistingException | DefaultGroupNotFoundException e) {
            Assert.fail("Unexpected exception received during user creation: " + e);
        }
        Assert.assertNotNull(newUser);

        Assert.assertNotNull(userService.getUserById(newUser.getId())); // can be found by ID
        Assert.assertNull(userService.getUserById(newUser.getId() + 1)); // different ID is not found

        try {
            userService.createUser(userLogin, null);
            Assert.fail("Expected exception not thrown when trying to recreate existing user");
        } catch (UserAlreadyExistingException | DefaultGroupNotFoundException ex) {
            Log.info("Expected exception for duplicate user received.");
        }

        // user was not created, but exception was thrown instead due to duplicate user
    }

    /**
     * tests that no new user can be registered if default group is not configured in database 
     */
    @Test
    public void testDontCreateUserWithoutDefaultGroup() {

        final String userLogin = "theuserlogin";

        // remove the default group initialized by test setup
        groupRepos.deleteAll();

        try {
            userService.createUser(new User(userLogin));
            Assert.fail("Expected exception about missing default user group not received!");
        } catch (UserAlreadyExistingException e) {
            Assert.fail("Unexpected exception received: " + e);
        } catch (DefaultGroupNotFoundException e) {
            Log.info("Expected exception about missing default group received");
        }

        Assert.assertNull(userRepos.findByLoginAndContext(userLogin, null));
    }

    /**
     * test that user creation throws an exception if required user name is missing
     * @throws DefaultGroupNotFoundException 
     * @throws UserAlreadyExistingException 
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCreateUser_InvalidParameters() throws Exception {

        userService.createUserIfNotExists("", null);
        Assert.fail("User creation should throw an error if user name is missing; did not!");
    }

    /**
     * test that a user is created if not existing, 
     * but don't throw exception or do anything if already existing
     */
    @Test
    public void testCreateUserIfNotExists() {

        final String userLogin = "theuserlogin";

        User theUser = null;

        Assert.assertEquals(0, userRepos.count());

        // let the user be created
        try {
            theUser = userService.createUserIfNotExists(userLogin, null);
            Assert.assertNotNull(theUser);
        } catch (Exception e) {
            Assert.fail("Unexpected exception received: " + e);
        }
        Assert.assertEquals(1, userRepos.count()); // user exists now

        // call the creation a second time - should also return existing user without exception
        try {
            theUser = userService.createUserIfNotExists(userLogin, null);
            Assert.assertNotNull(theUser);
        } catch (Exception e) {
            Assert.fail("Unexpected exception received: " + e);
        }
        Assert.assertEquals(1, userRepos.count()); // user still exists, but no further users
    }

    /**
     * test that an exception is thrown when preferences are to be updated for an unknown user
     */
    @Test(expected = UserNotFoundException.class)
    public void testUpdatePreferenceForUnknownUser() throws UserNotFoundException {

        userService.updateSidebarTutorialVisible("unknown", null, true);// should throw UserNotFoundException
    }

    /**
     * test that an exception is thrown when preferences are to be updated for an invalid user
     */
    @Test(expected = IllegalArgumentException.class)
    public void testUpdatePreferenceForInvalidUser() throws UserNotFoundException {

        userService.updateSidebarTutorialVisible("", null, true); // should throw IllegalArgumentException
    }

    /**
     * test that user preferences can be properly saved
     */
    @Test
    public void testUpdatePreferenceForUser()
            throws UserAlreadyExistingException, DefaultGroupNotFoundException, UserNotFoundException {

        // register a user
        final String userLogin = "mylogin";
        userService.createUser(userLogin, null);

        // and update its preference and verify
        userService.updateSidebarTutorialVisible(userLogin, null, false);

        User readUser = userRepos.findByLoginAndContext(userLogin, null);
        Assert.assertTrue(readUser.isSidebarTutorialDismissed());

        // update the other way and verify again
        userService.updateSidebarTutorialVisible(userLogin, null, true);

        readUser = userRepos.findByLoginAndContext(userLogin, null);
        Assert.assertFalse(readUser.isSidebarTutorialDismissed());
    }

    /**
     * test retrieval of user profile for known user of LEOS/EdiT
     */
    @Test
    public void testGetUserProfileOfKnownLeosUser() throws UserNotFoundException {

        final String login = "johndoe";
        final String authority = Authorities.EdiT;

        final User theUser = userRepos.save(new User(login));
        userGroupRepos.save(new UserGroup(theUser.getId(), defaultGroup.getId()));

        // assign user to a second group
        final Group otherGroup = new Group("thisGroup", true);
        groupRepos.save(otherGroup);
        userGroupRepos.save(new UserGroup(theUser.getId(), otherGroup.getId()));

        final UserInformation userInfo = new UserInformation(theUser, authority);
        final JsonUserProfile profile = userService.getUserProfile(userInfo);

        // verify
        Assert.assertNotNull(profile);
        Assert.assertTrue(profile.getUserid() != null && !profile.getUserid().isEmpty());
        Assert.assertEquals(2, profile.getGroups().size());
        Assert.assertEquals(defaultGroup.getName(), profile.getGroups().get(0).getId());
        Assert.assertEquals(otherGroup.getName(), profile.getGroups().get(1).getId());

        Assert.assertNotNull(profile.getFeatures());
        Assert.assertNotNull(profile.getFlash());
        Assert.assertNotNull(profile.getUser_info()); // user_info object is available, although its content might be null
        Assert.assertEquals(authority, profile.getAuthority());
    }

    /**
     * test retrieval of user profile for known user of ISC
     * -> should not report the default group
     */
    @Test
    public void testGetUserProfileOfKnownIscUser() throws UserNotFoundException {

        final String login = "demo";
        final String authority = Authorities.ISC;
        final String ENTITY = "CNECT.DDG2.G.3"; 

        final User theUser = userRepos.save(new User(login));
        // user is member of the default group, but it shouldn't be reported!
        userGroupRepos.save(new UserGroup(theUser.getId(), defaultGroup.getId()));

        // assign user to a second group
        final Group otherGroup = new Group("otherGroup", true);
        groupRepos.save(otherGroup);
        userGroupRepos.save(new UserGroup(theUser.getId(), otherGroup.getId()));

        // cache some user information to assure that the user's entity is assigned below
        userCache.cache(login, null, new UserDetails(login, (long)9, "some", "body",
            Arrays.asList(new UserEntity("8", ENTITY, "CNECT")), "some.body@ec.eu",
            null));
        final UserInformation userInfo = new UserInformation(theUser, authority);
        final JsonUserProfile profile = userService.getUserProfile(userInfo);

        // verify
        Assert.assertNotNull(profile);
        Assert.assertTrue(profile.getUserid() != null && !profile.getUserid().isEmpty());
        Assert.assertEquals(1, profile.getGroups().size());
        Assert.assertEquals(otherGroup.getName(), profile.getGroups().get(0).getId());

        Assert.assertNotNull(profile.getFeatures());
        Assert.assertNotNull(profile.getFlash());
        Assert.assertNotNull(profile.getUser_info()); // user_info object is always available, although its content might be null
        Assert.assertEquals(ENTITY, profile.getUser_info().getEntity_name());
        Assert.assertEquals(authority, profile.getAuthority());
    }

    /**
     * test that the entity with which the user is currently connected is shown in profile
     * in case it was also read in UD repo for the user
     */
    @Test
    public void testGetUserProfileOfWithValidConnectedEntity() throws UserNotFoundException {

        final String ENTITY1 = "CNECT.DDG2.G";
        final String connectedEntity = "CNECT.DDG2.G.3";
        final String login = "demo";
        final String authority = Authorities.EdiT;

        final User theUser = userRepos.save(new User(login));
        userGroupRepos.save(new UserGroup(theUser.getId(), defaultGroup.getId()));

        // assign user to a second group
        final Group otherGroup = new Group("otherGroup", true);
        groupRepos.save(otherGroup);
        userGroupRepos.save(new UserGroup(theUser.getId(), otherGroup.getId()));

        final UserServiceWithTestFunctions myUserService = new UserServiceImpl(null, false);

        final UserEntity entity1 = new UserEntity("1", ENTITY1, ENTITY1);
        final UserEntity entity2 = new UserEntity("2", connectedEntity, connectedEntity);
        final UserDetails details = new UserDetails(login, (long) 4712, "first", "last", Arrays.asList(entity1, entity2), login + "@domain.eu", null);
        myUserService.cacheUserDetails(login, details); // test function of extended interface
        myUserService.setUserGroupService(userGroupService); // inject non-mocked group service
        
        final UserInformation userInfo = new UserInformation(theUser, authority);
        userInfo.setConnectedEntity(connectedEntity);
        final JsonUserProfile profile = myUserService.getUserProfile(userInfo);

        // verify
        Assert.assertNotNull(profile);
        Assert.assertEquals(connectedEntity, profile.getUser_info().getEntity_name());
    }

    /**
     * test that the entity read from UD repo is used if user is currently connected with unknown entity
     */
    @Test
    public void testGetUserProfileOfWithNotValidConnectedEntity() throws UserNotFoundException {

        final String invalidEntity = "NOT_VALID_ENTITY_NAME";
        final String defaultEntity = "CNECT.DDG2.G.3";
        final String login = "demo";
        final String authority = Authorities.EdiT;

        final User theUser = userRepos.save(new User(login));
        userGroupRepos.save(new UserGroup(theUser.getId(), defaultGroup.getId()));

        // assign user to a second group
        final Group otherGroup = new Group("otherGroup", true);
        groupRepos.save(otherGroup);
        userGroupRepos.save(new UserGroup(theUser.getId(), otherGroup.getId()));

        final UserServiceWithTestFunctions myUserService = new UserServiceImpl(null, false);

        final UserEntity entity1 = new UserEntity("1", defaultEntity, defaultEntity);
        final UserDetails details = new UserDetails(login, (long) 4712, "first", "last", Arrays.asList(entity1), login + "@domain.eu", null);
        myUserService.cacheUserDetails(login, details); // test function of extended interface
        myUserService.setUserGroupService(userGroupService); // inject non-mocked group service
        
        final UserInformation userInfo = new UserInformation(theUser, authority);
        userInfo.setConnectedEntity(invalidEntity);
        final JsonUserProfile profile = myUserService.getUserProfile(userInfo);

        // verify
        Assert.assertNotNull(profile);
        Assert.assertEquals(defaultEntity, profile.getUser_info().getEntity_name());
    }

    /**
     * test retrieval of user profile without specifying user -> exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetUserProfileWithoutUser() throws Exception {

        userService.getUserProfile(null); // should throw IllegalArgumentException
    }

    /**
     * test retrieval of user profile throws exception when user is not known
     */
    @Test(expected = UserNotFoundException.class)
    public void testGetUserProfileOfUnknownUser() throws UserNotFoundException {

        userService.getUserProfile(new UserInformation("unknownlogin", null, "someauthority")); // should throw UserNotFoundException
    }

    /**
     * test that no exception is thrown when UD-repo cannot be contacted (and display_name remains empty)
     */
    @Test
    public void testGetUserProfile_RestException() throws UserNotFoundException {

        // mock the RestTemplate and inject it into the UserService
        final RestTemplate restOperations = Mockito.mock(RestTemplate.class);
        Mockito.when(restOperations.getForObject(Mockito.anyString(), Mockito.any(), Mockito.anyMap())).thenThrow(new RestClientException("error"));

        // mock the GroupService also to cover other branches
        final UserServiceWithTestFunctions userServiceExtended = new UserServiceImpl(restOperations, false);
        final UserGroupService userGrpServ = Mockito.mock(UserGroupServiceImpl.class);
        Mockito.when(userGrpServ.getGroupsOfUser(Mockito.any(User.class))).thenReturn(null);

        userServiceExtended.setUserGroupService(userGrpServ);

        final UserInformation userInfo = new UserInformation(new User("someuser"), "auth");

        final JsonUserProfile prof = userServiceExtended.getUserProfile(userInfo);
        Assert.assertNotNull(prof);// result received, no exception
        Assert.assertNull(prof.getUser_info().getDisplay_name()); // but no display name
    }

    @Test
    public void testIsContextEnabled() {
        // Both properties user.repository.url and user.repository.url.entities in the current test configuration do not contain the context.
        Assert.assertFalse(userService.isContextEnabled());
    }

}
