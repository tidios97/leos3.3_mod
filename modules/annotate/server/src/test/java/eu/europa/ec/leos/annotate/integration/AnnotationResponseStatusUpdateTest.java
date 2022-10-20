/*
 * Copyright 2019-2021 European Commission
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
package eu.europa.ec.leos.annotate.integration;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import eu.europa.ec.leos.annotate.Authorities;
import eu.europa.ec.leos.annotate.helper.SpotBugsAnnotations;
import eu.europa.ec.leos.annotate.helper.TestData;
import eu.europa.ec.leos.annotate.helper.TestDbHelper;
import eu.europa.ec.leos.annotate.model.*;
import eu.europa.ec.leos.annotate.model.entity.Annotation;
import eu.europa.ec.leos.annotate.model.entity.Group;
import eu.europa.ec.leos.annotate.model.entity.Metadata;
import eu.europa.ec.leos.annotate.model.entity.User;
import eu.europa.ec.leos.annotate.model.helper.AnnotationChecker;
import eu.europa.ec.leos.annotate.model.helper.MetadataHandler;
import eu.europa.ec.leos.annotate.model.web.StatusUpdateRequest;
import eu.europa.ec.leos.annotate.model.web.annotation.JsonAnnotation;
import eu.europa.ec.leos.annotate.repository.AnnotationRepository;
import eu.europa.ec.leos.annotate.repository.GroupRepository;
import eu.europa.ec.leos.annotate.repository.MetadataRepository;
import eu.europa.ec.leos.annotate.repository.UserRepository;
import eu.europa.ec.leos.annotate.services.AnnotationService;
import eu.europa.ec.leos.annotate.services.StatusUpdateService;
import eu.europa.ec.leos.annotate.services.UserGroupService;
import eu.europa.ec.leos.annotate.services.exceptions.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.time.LocalDateTime;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.config.name=anot")
@WebAppConfiguration
@ActiveProfiles("test")
public class AnnotationResponseStatusUpdateTest {

    /**
     * tests on updating the "response status"
     */
    private User user;
    private Group groupDigit;
    private static final String ACCOUNT_PREFIX = "acct:user@";
    private static final String ENTITY = "entity";

    // -------------------------------------
    // Required services and repositories
    // -------------------------------------
    @Autowired
    private AnnotationService annotService;

    @Autowired
    private StatusUpdateService statusService;

    @Autowired
    private UserGroupService userGroupService;

    @Autowired
    @Qualifier("annotationTestRepos")
    private AnnotationRepository annotRepos;

    @Autowired
    private GroupRepository groupRepos;

    @Autowired
    private UserRepository userRepos;

    @Autowired
    private MetadataRepository metadataRepos;

    // -------------------------------------
    // Cleanup of database content
    // -------------------------------------
    @Before
    public void setupTests() {

        TestDbHelper.cleanupRepositories(this);
        TestDbHelper.insertDefaultGroup(groupRepos);

        user = new User("demo");
        userRepos.save(user);

        groupDigit = new Group("DIGIT", true);
        groupRepos.save(groupDigit);
    }

    @After
    public void cleanDatabaseAfterTests() {
        TestDbHelper.cleanupRepositories(this);
    }

    // -------------------------------------
    // Tests
    // -------------------------------------

    /**
     * successful update of some annotations' response status; others are ignored since they don't match
     * also verifies that linked annotations are deleted and link is removed in one direction
     */
    @Test
    @SuppressFBWarnings(value = SpotBugsAnnotations.FieldNotInitialized, justification = SpotBugsAnnotations.FieldNotInitializedReason)
    public void testUpdateResponseStatusOk()
            throws CannotCreateAnnotationException,
            MissingPermissionException, CannotUpdateAnnotationStatusException {

        final String authority = Authorities.ISC;
        final String hypothesisUserAccount = ACCOUNT_PREFIX + authority;
        final String agriEntity = "AGRI";

        final UserInformation userInfo = new UserInformation(user, authority);

        // A (in prep) <-> B (sent)
        // create annotation A: in preparation
        final JsonAnnotation jsAnnotA = TestData.getTestAnnotationObject(hypothesisUserAccount);
        jsAnnotA.getDocument().getMetadata().put(Metadata.PROP_RESPONSE_STATUS, ResponseStatus.IN_PREPARATION.toString());
        jsAnnotA.getDocument().getMetadata().put(ENTITY, agriEntity);
        final Annotation annotA = annotService.createAnnotation(jsAnnotA, userInfo);

        // create annotation B: SENT already (must be adjusted in the DB)
        final JsonAnnotation jsAnnotB = TestData.getTestAnnotationObject(hypothesisUserAccount);
        jsAnnotB.getDocument().getMetadata().put(Metadata.PROP_RESPONSE_STATUS, ResponseStatus.UNKNOWN.toString());
        jsAnnotB.getDocument().getMetadata().put(ENTITY, agriEntity);
        final Annotation annotB = annotService.createAnnotation(jsAnnotB, userInfo);

        Annotation annHelp = annotService.findAnnotationById(annotB.getId());
        Metadata metaHelp = metadataRepos.findById(annHelp.getMetadataId()).get();
        metaHelp.setResponseStatus(ResponseStatus.SENT);
        final LocalDateTime oldUpdateTime = LocalDateTime.now(java.time.ZoneOffset.UTC).plusMinutes(-1);
        metaHelp.setResponseStatusUpdated(oldUpdateTime);
        metaHelp.setResponseStatusUpdatedByUser(user.getId() + 1); // set a different ID than the one of the user
        metaHelp.setResponseStatusUpdatedByGroup(annotB.getGroup().getId());
        metadataRepos.save(metaHelp);

        // link both items
        annHelp.setLinkedAnnotationId(annotA.getId());
        annotRepos.save(annHelp);

        annHelp = annotService.findAnnotationById(annotA.getId());
        annHelp.setLinkedAnnotationId(annotB.getId());
        annotRepos.save(annHelp);

        // create annotation C (in_prep), but different "entity" metadata than other annotations -> should not be affected by response status update
        final JsonAnnotation jsAnnotC = TestData.getTestAnnotationObject(hypothesisUserAccount);
        jsAnnotC.getDocument().getMetadata().put(Metadata.PROP_RESPONSE_STATUS, ResponseStatus.IN_PREPARATION.toString());
        jsAnnotC.getDocument().getMetadata().put(ENTITY, "DIGIT"); // different entity
        final Annotation annotC = annotService.createAnnotation(jsAnnotC, userInfo);

        // create annotation D: also associated to same metadata as annotation A, but not linked
        final JsonAnnotation jsAnnotD = TestData.getTestAnnotationObject(hypothesisUserAccount);
        final Annotation annotD = annotService.createAnnotation(jsAnnotD, userInfo);
        annHelp = annotService.findAnnotationById(annotD.getId());
        metaHelp = annotService.findAnnotationById(annotA.getId()).getMetadata();
        annHelp.setMetadata(metaHelp);
        annotRepos.save(annHelp);

        // now send the response status update request
        final StatusUpdateRequest updateRequest = new StatusUpdateRequest(annotA.getGroup().getName(), annotA.getDocument().getUri().toString(),
                ResponseStatus.SENT);
        updateRequest.setMetadataToMatch(new SimpleMetadata(ENTITY, agriEntity));
        final ResponseStatusUpdateResult rsue = statusService.updateAnnotationResponseStatus(updateRequest, userInfo);

        Assert.assertNotNull(rsue);
        final List<String> updatedAnnotIds = rsue.getUpdatedAnnotIds();
        final List<String> deletedAnnotIds = rsue.getDeletedAnnotIds();

        // check: A has changed status and is no longer linked to B
        Assert.assertEquals(2, updatedAnnotIds.size());
        Assert.assertEquals(annotA.getId(), updatedAnnotIds.get(0));
        annHelp = annotRepos.findByIdAndStatus(annotA.getId(), AnnotationStatus.NORMAL);
        Assert.assertEquals(ResponseStatus.SENT, annHelp.getMetadata().getResponseStatus());
        Assert.assertNull(annHelp.getLinkedAnnotationId());

        // check: B still has link to A, but is DELETED; response status has not been touched
        Assert.assertEquals(1, deletedAnnotIds.size());
        annHelp = annotRepos.findByIdAndStatus(annotB.getId(), AnnotationStatus.DELETED);
        Assert.assertEquals(annotA.getId(), annHelp.getLinkedAnnotationId());
        Assert.assertEquals(AnnotationStatus.DELETED, annHelp.getStatus());
        Assert.assertEquals(annHelp.getMetadata().getResponseStatusUpdated(), oldUpdateTime); // was not updated again!
        Assert.assertNotEquals(user.getId(), annHelp.getMetadata().getResponseStatusUpdatedByUser());
        Assert.assertEquals(annotB.getGroup().getId().longValue(), annHelp.getMetadata().getResponseStatusUpdatedByGroup().longValue());

        // check: C was not changed, still has response status IN_PREPARATION
        annHelp = annotRepos.findByIdAndStatus(annotC.getId(), AnnotationStatus.NORMAL);
        Assert.assertEquals(ResponseStatus.IN_PREPARATION, annHelp.getMetadata().getResponseStatus());

        // check: D has changed status, is not linked
        Assert.assertEquals(annotD.getId(), updatedAnnotIds.get(1));
        annHelp = annotRepos.findByIdAndStatus(annotD.getId(), AnnotationStatus.NORMAL);
        Assert.assertEquals(ResponseStatus.SENT, annHelp.getMetadata().getResponseStatus());
        Assert.assertNull(annHelp.getLinkedAnnotationId());
    }

    /**
     * successful migrate the metadata of some annotations' to a newer version; 
     * others are ignored since they don't match
     */
    @Test
    @SuppressFBWarnings(value = SpotBugsAnnotations.FieldNotInitialized, justification = SpotBugsAnnotations.FieldNotInitializedReason)
    public void testUpdateResponseStatusMigrateOk()
            throws CannotCreateAnnotationException,
            MissingPermissionException, CannotUpdateAnnotationStatusException {

        final String authority = Authorities.ISC;
        final String hypothesisUserAccount = ACCOUNT_PREFIX + authority;
        final String agriEntity = "AGRI";

        final UserInformation userInfo = new UserInformation(user, authority);

        // A (in prep) <-> B (sent)
        // create annotation A: in preparation
        final JsonAnnotation jsAnnotA = TestData.getTestAnnotationObject(hypothesisUserAccount);
        jsAnnotA.getDocument().getMetadata().put(Metadata.PROP_RESPONSE_STATUS, ResponseStatus.IN_PREPARATION.toString());
        jsAnnotA.getDocument().getMetadata().put(ENTITY, agriEntity);
        jsAnnotA.getDocument().getMetadata().put(Metadata.PROP_RESPONSE_VERSION, "2");
        final Annotation annotA = annotService.createAnnotation(jsAnnotA, userInfo);

        // create annotation B: SENT already (must be adjusted in the DB)
        final JsonAnnotation jsAnnotB = TestData.getTestAnnotationObject(hypothesisUserAccount);
        jsAnnotB.getDocument().getMetadata().put(Metadata.PROP_RESPONSE_STATUS, ResponseStatus.UNKNOWN.toString());
        jsAnnotB.getDocument().getMetadata().put(ENTITY, agriEntity);
        jsAnnotB.getDocument().getMetadata().put(Metadata.PROP_RESPONSE_VERSION, "1");
        final Annotation annotB = annotService.createAnnotation(jsAnnotB, userInfo);

        Annotation annHelp = annotService.findAnnotationById(annotB.getId());
        final Metadata metaHelp = metadataRepos.findById(annHelp.getMetadataId()).get();
        metaHelp.setResponseStatus(ResponseStatus.SENT);
        final LocalDateTime oldUpdateTime = LocalDateTime.now(java.time.ZoneOffset.UTC).plusMinutes(-1);
        metaHelp.setResponseStatusUpdated(oldUpdateTime);
        metaHelp.setResponseStatusUpdatedByUser(user.getId() + 1); // set a different ID than the one of the user
        metaHelp.setResponseStatusUpdatedByGroup(123L);
        metadataRepos.save(metaHelp);

        // link both items
        annHelp.setLinkedAnnotationId(annotA.getId());
        annotRepos.save(annHelp);

        annHelp = annotService.findAnnotationById(annotA.getId());
        annHelp.setLinkedAnnotationId(annotB.getId());
        annotRepos.save(annHelp);

        // create annotation C (in_prep), but different "entity" metadata than other annotations -> should not be affected by migration
        final JsonAnnotation jsAnnotC = TestData.getTestAnnotationObject(hypothesisUserAccount);
        jsAnnotC.getDocument().getMetadata().put(Metadata.PROP_RESPONSE_STATUS, ResponseStatus.IN_PREPARATION.toString());
        jsAnnotC.getDocument().getMetadata().put(ENTITY, "DIGIT"); // different entity
        jsAnnotC.getDocument().getMetadata().put(Metadata.PROP_RESPONSE_VERSION, "1");
        final Annotation annotC = annotService.createAnnotation(jsAnnotC, userInfo);

        // now send the response status update (migration) request
        // note: target status remains "IN_PREPARATION"
        final StatusUpdateRequest updateRequest = new StatusUpdateRequest(annotA.getGroup().getName(), annotA.getDocument().getUri().toString(),
                ResponseStatus.IN_PREPARATION, true);

        final SimpleMetadata metaMatch = new SimpleMetadata(ENTITY, agriEntity);
        metaMatch.put(Metadata.PROP_RESPONSE_VERSION, "2");
        updateRequest.setMetadataToMatch(metaMatch);
        final ResponseStatusUpdateResult rsue = statusService.updateAnnotationResponseStatus(updateRequest, userInfo);

        Assert.assertNotNull(rsue);
        final List<String> updatedAnnotIds = rsue.getUpdatedAnnotIds();
        final List<String> deletedAnnotIds = rsue.getDeletedAnnotIds();

        // check: A has kept status and other properties, but now has version 3 instead of 2
        Assert.assertEquals(1, updatedAnnotIds.size());
        Assert.assertEquals(annotA.getId(), updatedAnnotIds.get(0));
        annHelp = annotRepos.findByIdAndStatus(annotA.getId(), AnnotationStatus.NORMAL);
        Assert.assertEquals(ResponseStatus.IN_PREPARATION, annHelp.getMetadata().getResponseStatus());
        Assert.assertEquals(annotB.getId(), annHelp.getLinkedAnnotationId());
        Assert.assertEquals(3, MetadataHandler.getResponseVersion(annHelp.getMetadata()));

        // check: B still has link to A, was NOT deleted, still has response version 1
        Assert.assertEquals(0, deletedAnnotIds.size());
        annHelp = annotRepos.findByIdAndStatus(annotB.getId(), AnnotationStatus.NORMAL);
        Assert.assertEquals(annotA.getId(), annHelp.getLinkedAnnotationId());
        Assert.assertEquals(AnnotationStatus.NORMAL, annHelp.getStatus());
        Assert.assertEquals(1, MetadataHandler.getResponseVersion(annHelp.getMetadata()));
        Assert.assertEquals(123L, annHelp.getMetadata().getResponseStatusUpdatedByGroup().longValue());

        // check: C was not changed, still has response status IN_PREPARATION
        annHelp = annotRepos.findByIdAndStatus(annotC.getId(), AnnotationStatus.NORMAL);
        Assert.assertEquals(ResponseStatus.IN_PREPARATION, annHelp.getMetadata().getResponseStatus());
    }

    /**
     * update of response status - nothing to update (i.e. metadata defined, but no annotations assigned to it)
     */
    @Test
    public void testUpdateResponseStatus_nothingToUpdate()
            throws CannotCreateAnnotationException,
            MissingPermissionException, CannotUpdateAnnotationStatusException {

        final String authority = Authorities.ISC;
        final String hypothesisUserAccount = ACCOUNT_PREFIX + authority;

        final UserInformation userInfo = new UserInformation(user, authority);

        // create annotation A: in preparation
        final JsonAnnotation jsAnnotA = TestData.getTestAnnotationObject(hypothesisUserAccount);
        jsAnnotA.getDocument().getMetadata().put(Metadata.PROP_RESPONSE_STATUS, ResponseStatus.IN_PREPARATION.toString());
        jsAnnotA.getDocument().getMetadata().put(ENTITY, "AGRI");
        final Annotation annotA = annotService.createAnnotation(jsAnnotA, userInfo);

        // but assign the annotation to different metadata that's newly created
        final Metadata metaDb = metadataRepos.findAll().iterator().next();
        final Metadata replacementMetadata = new Metadata(metaDb.getDocument(), metaDb.getGroup(), metaDb.getSystemId());
        metadataRepos.save(replacementMetadata);
        Annotation annHelp = annotService.findAnnotationById(annotA.getId());
        annHelp.setMetadata(replacementMetadata);
        annotRepos.save(annHelp);

        // now send the response status update request - there should be matching metadata, but no annotation is assigned to this metadata
        final StatusUpdateRequest updateRequest = new StatusUpdateRequest(annotA.getGroup().getName(), annotA.getDocument().getUri().toString(),
                ResponseStatus.SENT);
        updateRequest.setMetadataToMatch(new SimpleMetadata(ENTITY, "AGRI"));
        final ResponseStatusUpdateResult rsur = statusService.updateAnnotationResponseStatus(updateRequest, userInfo);

        // check: there were no updates, A has its previous status
        Assert.assertNotNull(rsur);
        Assert.assertEquals(0, rsur.getUpdatedAnnotIds().size());
        Assert.assertEquals(0, rsur.getDeletedAnnotIds().size());
        annHelp = annotRepos.findByIdAndStatus(annotA.getId(), AnnotationStatus.NORMAL);
        Assert.assertNull(annHelp.getMetadata().getResponseStatus()); // replacementMetadata doesn't have a response status set
    }

    /**
     * testing the entire workflow:
     * - annotations with version 1.0 are created, response status IN_PREPARATION
     * - they are SENT -> version 1.0, response status SENT
     * - they are deleted -> version 1.0, response status SENT, sentDeleted=true
     * - new annotations are created with version 2.0, response status IN_PREPARATION
     * - they are SENT -> now upon sending, we have to identify older annotations being sentDeleted=true and delete them
     *  
     * @throws CannotCreateAnnotationException
     * @throws CannotUpdateAnnotationStatusException
     * @throws MissingPermissionException
     * @throws CannotDeleteAnnotationException
     * @throws CannotDeleteSentAnnotationException
     */
    @Test
    @SuppressFBWarnings(value = SpotBugsAnnotations.FieldNotInitialized, justification = SpotBugsAnnotations.FieldNotInitializedReason)
    public void testUpdateResponseStatusWithDeletedSentAnnotations() throws CannotCreateAnnotationException, CannotUpdateAnnotationStatusException,
            MissingPermissionException, CannotDeleteAnnotationException, CannotDeleteSentAnnotationException {

        // create an annotation with response status IN_PREPARATION, then SENT it -> must have sentDeleted flag set
        // delete it, and then SENT again -> must be deleted then

        final String authority = Authorities.ISC;
        final String hypothesisUserAccount = ACCOUNT_PREFIX + authority;
        final String version = "version";
        final String version1 = "1.0";
        final String version2 = "2.0";

        // add user to DIGIT group
        userGroupService.assignUserToGroup(user, groupDigit);
        final UserInformation userInfo = new UserInformation(user, authority);

        // create annotation: in preparation
        final JsonAnnotation jsAnnotV1 = TestData.getTestAnnotationObject(hypothesisUserAccount);
        jsAnnotV1.getDocument().getMetadata().put(Metadata.PROP_RESPONSE_STATUS, ResponseStatus.IN_PREPARATION.toString());
        jsAnnotV1.getDocument().getMetadata().put(version, version1);
        jsAnnotV1.setGroup(groupDigit.getName());
        final Annotation annotV1 = annotService.createAnnotation(jsAnnotV1, userInfo);

        // now send the response status update request - annotation should get different response status now (SENT)
        final StatusUpdateRequest updateRequest = new StatusUpdateRequest(annotV1.getGroup().getName(), annotV1.getDocument().getUri().toString(),
                ResponseStatus.SENT);
        updateRequest.setMetadataToMatch(new SimpleMetadata(version, version1));
        final ResponseStatusUpdateResult rsur = statusService.updateAnnotationResponseStatus(updateRequest, userInfo);

        // check: there should have been one update
        Assert.assertNotNull(rsur);
        Assert.assertEquals(1, rsur.getUpdatedAnnotIds().size());
        Assert.assertEquals(0, rsur.getDeletedAnnotIds().size());

        // delete the annotation
        annotService.deleteAnnotationById(annotV1.getId(), userInfo);

        // check: should have sentDeleted=true
        Annotation readAnnot = annotService.findAnnotationById(annotV1.getId());
        Assert.assertTrue(readAnnot.isSentDeleted());
        Assert.assertTrue(AnnotationChecker.isResponseStatusSent(readAnnot));
        Assert.assertEquals(AnnotationStatus.NORMAL, readAnnot.getStatus());

        // create another annotation in IN_PREPARATION status for version 2.0 -> will be SENT afterwards
        final JsonAnnotation jsAnnotV2 = TestData.getTestAnnotationObject(hypothesisUserAccount);
        jsAnnotV2.getDocument().getMetadata().put(Metadata.PROP_RESPONSE_STATUS, ResponseStatus.IN_PREPARATION.toString());
        jsAnnotV2.getDocument().getMetadata().put(version, version2);
        jsAnnotV2.setGroup(groupDigit.getName());
        final Annotation annotV2 = annotService.createAnnotation(jsAnnotV2, userInfo);

        // now send again a response status update request - should really delete the annotation now
        updateRequest.setMetadataToMatch(new SimpleMetadata(version, version2)); // send a newer version now!

        final ResponseStatusUpdateResult rsur2 = statusService.updateAnnotationResponseStatus(updateRequest, userInfo);

        // check: there should have been one deletion (annotV1), and one update (annotV2)
        Assert.assertNotNull(rsur2);
        Assert.assertEquals(1, rsur2.getUpdatedAnnotIds().size());
        Assert.assertEquals(1, rsur2.getDeletedAnnotIds().size());
        Assert.assertEquals(rsur2.getUpdatedAnnotIds().get(0), annotV2.getId());
        Assert.assertEquals(rsur2.getDeletedAnnotIds().get(0), annotV1.getId());

        // check: annotV1 is DELETED now, but still has the SENT response status and the sentDeleted flags
        readAnnot = annotRepos.findByIdAndStatus(annotV1.getId(), AnnotationStatus.DELETED);
        Assert.assertTrue(readAnnot.isSentDeleted());
        Assert.assertTrue(AnnotationChecker.isResponseStatusSent(readAnnot));
        Assert.assertEquals(AnnotationStatus.DELETED, readAnnot.getStatus());
    }

    /**
     * cannot migrate the metadata of some annotations' to a newer version since the wanted version 
     * does not exist
     */
    @Test
    @SuppressFBWarnings(value = SpotBugsAnnotations.FieldNotInitialized, justification = SpotBugsAnnotations.FieldNotInitializedReason)
    public void testUpdateResponseStatusCannotMigrate()
            throws CannotCreateAnnotationException,
            MissingPermissionException, CannotUpdateAnnotationStatusException {

        final String authority = Authorities.ISC;
        final String hypothesisUserAccount = ACCOUNT_PREFIX + authority;
        final String agriEntity = "AGRI";

        final UserInformation userInfo = new UserInformation(user, authority);

        // A (in prep)
        // create annotation A: in preparation
        final JsonAnnotation jsAnnotA = TestData.getTestAnnotationObject(hypothesisUserAccount);
        jsAnnotA.getDocument().getMetadata().put(Metadata.PROP_RESPONSE_STATUS, ResponseStatus.IN_PREPARATION.toString());
        jsAnnotA.getDocument().getMetadata().put(ENTITY, agriEntity);
        jsAnnotA.getDocument().getMetadata().put(Metadata.PROP_RESPONSE_VERSION, "1");
        final Annotation annotA = annotService.createAnnotation(jsAnnotA, userInfo);

        // now send the response status update (migration) request
        // note: target status remains "IN_PREPARATION"
        final StatusUpdateRequest updateRequest = new StatusUpdateRequest(annotA.getGroup().getName(), annotA.getDocument().getUri().toString(),
                ResponseStatus.IN_PREPARATION, true);

        // ask for response version 2 to be migrated (which does not exist)
        final SimpleMetadata metaMatch = new SimpleMetadata(ENTITY, agriEntity);
        metaMatch.put(Metadata.PROP_RESPONSE_VERSION, "2");
        updateRequest.setMetadataToMatch(metaMatch);
        final ResponseStatusUpdateResult rsue = statusService.updateAnnotationResponseStatus(updateRequest, userInfo);

        Assert.assertNotNull(rsue);
        final List<String> updatedAnnotIds = rsue.getUpdatedAnnotIds();
        final List<String> deletedAnnotIds = rsue.getDeletedAnnotIds();

        // check: nothing was updated nor deleted
        Assert.assertEquals(0, updatedAnnotIds.size());
        Assert.assertEquals(0, deletedAnnotIds.size());

        // thus the annotation should still have response version 1
        final Annotation annHelp = annotRepos.findByIdAndStatus(annotA.getId(), AnnotationStatus.NORMAL);
        Assert.assertNotNull(annHelp);
        Assert.assertEquals(1, MetadataHandler.getResponseVersion(annHelp.getMetadata()));
    }
}
