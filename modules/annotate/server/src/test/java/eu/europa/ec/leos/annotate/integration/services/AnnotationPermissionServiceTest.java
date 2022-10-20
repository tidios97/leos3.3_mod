/*
 * Copyright 2019 European Commission
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
import eu.europa.ec.leos.annotate.model.ResponseStatus;
import eu.europa.ec.leos.annotate.model.UserInformation;
import eu.europa.ec.leos.annotate.model.entity.Annotation;
import eu.europa.ec.leos.annotate.model.entity.Group;
import eu.europa.ec.leos.annotate.model.entity.Metadata;
import eu.europa.ec.leos.annotate.model.entity.User;
import eu.europa.ec.leos.annotate.model.helper.MetadataHandler;
import eu.europa.ec.leos.annotate.services.UserGroupService;
import eu.europa.ec.leos.annotate.services.exceptions.MissingPermissionException;
import eu.europa.ec.leos.annotate.services.impl.AnnotationPermissionServiceImpl;
import eu.europa.ec.leos.annotate.services.impl.UserServiceImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AnnotationPermissionServiceTest {

    // -------------------------------------
    // Required services and repositories
    // -------------------------------------
    @InjectMocks
    private AnnotationPermissionServiceImpl annotPermMockService;

    @Mock
    private UserServiceImpl userService;

    @Mock
    private UserGroupService userGroupService;

    // -------------------------------------
    // Tests
    // -------------------------------------

    @Test(expected = IllegalArgumentException.class)
    @SuppressFBWarnings(value = SpotBugsAnnotations.KnownNullValue, justification = SpotBugsAnnotations.KnownNullValueReason)
    public void testHasPermissionToUpdate_AnnotationNull() {

        final Annotation annot = null;
        final UserInformation userinfo = new UserInformation("itsme", null, Authorities.EdiT);
        annotPermMockService.hasUserPermissionToUpdateAnnotation(annot, userinfo);
    }

    @Test(expected = IllegalArgumentException.class)
    @SuppressFBWarnings(value = SpotBugsAnnotations.KnownNullValue, justification = SpotBugsAnnotations.KnownNullValueReason)
    public void testHasPermissionToUpdate_UserinfoNull() {

        final Annotation annot = new Annotation();
        final UserInformation userinfo = null;

        annotPermMockService.hasUserPermissionToUpdateAnnotation(annot, userinfo);
    }

    @Test
    public void testHasPermissionToUpdate_OtherMetadata_EditUser() {

        final String LOGIN = "theuser";

        final User user = new User(LOGIN);
        user.setId(2L);
        Mockito.when(userService.findByLoginAndContext(LOGIN, null)).thenReturn(user);

        final Annotation annot = new Annotation();
        final Metadata meta = new Metadata();
        annot.setMetadata(meta);
        annot.setUser(user);

        meta.setResponseStatus(ResponseStatus.IN_PREPARATION);

        // verify: status is not SENT, it is the user's annotation -> it can be updated
        Assert.assertTrue(annotPermMockService.hasUserPermissionToUpdateAnnotation(annot, new UserInformation(annot.getUser().getLogin(), null, Authorities.EdiT)));
    }

    // user wanting to update is not the annotation's creator -> refused
    @Test
    public void testHasPermissionToUpdate_OtherMetadata_OtherEditUser() {

        final Annotation annot = new Annotation();
        final Metadata meta = new Metadata();
        annot.setMetadata(meta);
        annot.setUser(new User("theuser"));

        final String login = "nottheuser";
        final User user = new User(login);
        user.setId(1L); // Use a different ID as the annotation's creator ID is 0.
        Mockito.when(userService.findByLoginAndContext(login, null)).thenReturn(user);

        meta.setResponseStatus(ResponseStatus.IN_PREPARATION);

        // verify: status is not SENT -> ok, but user is not the annotation's creator
        Assert.assertFalse(annotPermMockService.hasUserPermissionToUpdateAnnotation(annot, new UserInformation(login, null, Authorities.EdiT)));
    }

    /**
     * user wanting to update is:
     *  - from same group
     *  - not the annotation's creator
     *  - annotation is from CONTRIBUTION
     *  -> allowed
     */
    @Test
    public void testHasPermissionToUpdate_OtherMetadata_OtherIscUser_ContributionAnnot() {

        final String LOGIN_ANNOT = "annotUser";
        final String LOGIN_OTHER = "other";

        final User annotUser = new User(LOGIN_ANNOT);
        final User otherUser = new User(LOGIN_OTHER);
        final Group group = new Group("mygroup", true);

        Mockito.when(userService.findByLoginAndContext(LOGIN_OTHER, null)).thenReturn(otherUser);
        Mockito.when(userGroupService.isUserMemberOfGroup(otherUser, group)).thenReturn(true);

        final Annotation annot = new Annotation();
        final Metadata meta = new Metadata();
        //Test also ignoreCase comparison
        MetadataHandler.setOriginMode(meta, "pRiVaTe");
        meta.setGroup(group);
        annot.setMetadata(meta);
        annot.setUser(annotUser);

        otherUser.setId(1L); // Use a different ID as the annotation's creator ID is 0.
        Mockito.when(userService.findByLoginAndContext(LOGIN_OTHER, null)).thenReturn(otherUser);

        meta.setResponseStatus(ResponseStatus.IN_PREPARATION);

        // verify: status is not SENT -> ok
        Assert.assertTrue(annotPermMockService.hasUserPermissionToUpdateAnnotation(annot, new UserInformation(otherUser, null, Authorities.ISC)));
    }

    /**
     * user wanting to update is:
     *  - from same group
     *  - not the annotation's creator
     *  - annotation is NOT from CONTRIBUTION
     *  -> accepted
     */
    @Test
    public void testHasPermissionToUpdate_OtherMetadata_OtherIscUser_NOTContributionAnnot() {
        final Group group = new Group("mygroup", true);

        final Annotation annot = new Annotation();
        final Metadata meta = new Metadata();
        meta.setGroup(group);
        annot.setMetadata(meta);
        annot.setUser(new User("theuser"));

        final String login = "nottheuser";
        final User user = new User(login);
        user.setId(1L); // Use a different ID as the annotation's creator ID is 0.
        Mockito.when(userService.findByLoginAndContext(login, null)).thenReturn(user);
        Mockito.when(userGroupService.isUserMemberOfGroup(user, group)).thenReturn(true);

        meta.setResponseStatus(ResponseStatus.IN_PREPARATION);

        // verify: status is not SENT -> ok, user is not the annotation's creator but annotation is from CONTRIBUTION
        Assert.assertTrue(annotPermMockService.hasUserPermissionToUpdateAnnotation(annot, new UserInformation(user, Authorities.ISC)));
    }

    @Test
    public void testHasPermissionToUpdate_OtherMetadata_IscUser() {
        final Group group = new Group("mygroup", true);

        final String LOGIN = "dave";
        final User user = new User(LOGIN);
        user.setId(Long.valueOf(8));

        Mockito.when(userService.findByLoginAndContext(LOGIN, null)).thenReturn(user);
        Mockito.when(userGroupService.isUserMemberOfGroup(user, group)).thenReturn(true);

        final Annotation annot = new Annotation();
        final Metadata meta = new Metadata();
        meta.setGroup(group);
        annot.setMetadata(meta);
        annot.setUser(user);

        meta.setResponseStatus(ResponseStatus.IN_PREPARATION);

        // verify: status is not SENT -> it can be updated
        Assert.assertTrue(annotPermMockService.hasUserPermissionToUpdateAnnotation(annot, new UserInformation(user, Authorities.ISC)));
    }

    @Test
    public void testHasPermissionToUpdateNotSuccessful_Sent_EditUser() {

        final String login = "a";
        final User user = new User(login);
        Mockito.when(userService.findByLoginAndContext(login, null)).thenReturn(user);

        final Annotation annot = new Annotation();
        final Metadata meta = new Metadata();
        annot.setMetadata(meta);
        annot.setUser(user);

        meta.setResponseStatus(ResponseStatus.SENT);

        // verify: status is SENT -> it cannot be updated any more
        Assert.assertFalse(annotPermMockService.hasUserPermissionToUpdateAnnotation(annot, new UserInformation(annot.getUser().getLogin(), null, Authorities.EdiT)));
    }

    // an ISC user wants to update a SENT annotation; he belongs to the same group as the annotation
    @Test
    public void testHasPermissionToUpdateSuccessful_IscUserOfSameGroup() {

        final String LOGIN_ANNOT = "annotUser";
        final String LOGIN_OTHER = "other";

        final User annotUser = new User(LOGIN_ANNOT);
        final User otherUser = new User(LOGIN_OTHER);
        final Group group = new Group("mygroup", true);

        Mockito.when(userService.findByLoginAndContext(LOGIN_OTHER, null)).thenReturn(otherUser);
        Mockito.when(userGroupService.isUserMemberOfGroup(otherUser, group)).thenReturn(true);

        final Annotation annot = new Annotation();
        final Metadata meta = new Metadata();
        meta.setGroup(group);
        annot.setMetadata(meta);
        annot.setUser(annotUser);

        meta.setResponseStatus(ResponseStatus.SENT);

        // verify: status is SENT, but user belongs to same group -> it can be updated
        Assert.assertTrue(annotPermMockService.hasUserPermissionToUpdateAnnotation(annot, new UserInformation(otherUser, Authorities.ISC)));
    }

    // an ISC user wants to update a SENT annotation; he does not belong to the same group as the annotation
    @Test
    public void testHasPermissionToUpdateSuccessful_IscUserOfOtherGroup() {

        final String LOGIN_ANNOT = "annotUser";
        final String LOGIN_OTHER = "other";

        final User annotUser = new User(LOGIN_ANNOT);
        final User otherUser = new User(LOGIN_OTHER);
        final Group group = new Group("mygroup", true);

        Mockito.when(userService.findByLoginAndContext(LOGIN_OTHER, null)).thenReturn(otherUser);
        Mockito.when(userGroupService.isUserMemberOfGroup(otherUser, group)).thenReturn(false);

        final Annotation annot = new Annotation();
        final Metadata meta = new Metadata();
        meta.setGroup(group);
        annot.setMetadata(meta);
        annot.setUser(annotUser);

        meta.setResponseStatus(ResponseStatus.SENT);

        // verify: status is SENT, but user does not belong to same group -> it cannot be updated
        Assert.assertFalse(annotPermMockService.hasUserPermissionToUpdateAnnotation(annot, new UserInformation(otherUser, Authorities.ISC)));
    }

    @Test
    public void testValidateUserAnnotationApiPermission() {
        final UserInformation editUserInfo =  new UserInformation(new User("editUser"), Authorities.EdiT);
        final UserInformation iscUserInfo = new UserInformation(new User("iscUser"), Authorities.ISC);
        // No exception is expected here
        try {
            annotPermMockService.validateUserAnnotationApiPermission(editUserInfo);
            annotPermMockService.validateUserAnnotationApiPermission(iscUserInfo);
        } catch (Exception ex) {
            Assert.fail("Exception raised but not expected ('" + ex.getMessage() + "')");
        }

    }

    @Test(expected = MissingPermissionException.class)
    public void testValidateUserAnnotationApiPermissionWithError() throws Exception {
        final UserInformation supportUserInfo =  new UserInformation(new User("supportUser"), Authorities.Support);
        // No exception is expected here
        annotPermMockService.validateUserAnnotationApiPermission(supportUserInfo);
    }
}
