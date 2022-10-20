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
package eu.europa.ec.leos.annotate.integration;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import eu.europa.ec.leos.annotate.Authorities;
import eu.europa.ec.leos.annotate.helper.SpotBugsAnnotations;
import eu.europa.ec.leos.annotate.helper.TestData;
import eu.europa.ec.leos.annotate.helper.TestDbHelper;
import eu.europa.ec.leos.annotate.model.AnnotationStatus;
import eu.europa.ec.leos.annotate.model.SimpleMetadata;
import eu.europa.ec.leos.annotate.model.UserInformation;
import eu.europa.ec.leos.annotate.model.entity.*;
import eu.europa.ec.leos.annotate.model.web.annotation.JsonAnnotation;
import eu.europa.ec.leos.annotate.repository.*;
import eu.europa.ec.leos.annotate.services.AnnotationConversionService;
import eu.europa.ec.leos.annotate.services.AnnotationService;
import eu.europa.ec.leos.annotate.services.TagsService;
import eu.europa.ec.leos.annotate.services.exceptions.CannotCreateAnnotationException;

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
import org.springframework.util.StringUtils;

import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("PMD.TooManyMethods")
@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.config.name=anot")
@ActiveProfiles("test")
public class AnnotationSaveTest {

    private Group defaultGroup;
    private Group defaultGroup2;
    private static final String LOGIN = "demo";
    private static final String PREFIX = "acct:";

    // -------------------------------------
    // Required services and repositories
    // -------------------------------------
    @Autowired
    private AnnotationService annotService;

    @Autowired
    private AnnotationConversionService conversionService;

    @Autowired
    private DocumentRepository documentRepos;

    @Autowired
    @Qualifier("annotationTestRepos")
    private AnnotationTestRepository annotRepos;

    @Autowired
    private UserRepository userRepos;

    @Autowired
    private GroupRepository groupRepos;

    @Autowired
    private UserGroupRepository userGroupRepos;

    @Autowired
    private TagRepository tagRepos;

    @Autowired
    private MetadataRepository metadataRepos;

    @Autowired
    private TagsService tagService;

    // -------------------------------------
    // Cleanup of database content before running new test
    // -------------------------------------
    @Before
    public void cleanDatabaseBeforeTests() {

        TestDbHelper.cleanupRepositories(this);
        defaultGroup = TestDbHelper.insertDefaultGroup(groupRepos);
    }

    @After
    public void cleanDatabaseAfterTests() {

        TestDbHelper.cleanupRepositories(this);
    }

    // -------------------------------------
    // Tests
    // -------------------------------------
    /**
     * tests saving an incoming annotation to the database using hibernate
     * -> checks that related objects are persisted into their proper repository by re-reading object from DB 
     *    (coarsely only; we do not want to test hibernate framework as such, just principle setup of our model) 
     * 
     * @throws URISyntaxException
     */
    @Test
    public void testSaveAnnotationWithRepository() {

        final Document doc = new Document();
        doc.setTitle("title");
        doc.setUri("http://www.a.com");
        documentRepos.save(doc);

        final User user = new User();
        user.setLogin("login");
        userRepos.save(user);

        final Group group = new Group();
        group.setName("groupname");
        group.setDisplayName("display");
        group.setDescription("description");
        groupRepos.save(group);

        final Tag tag = new Tag();
        tag.setName("thetag");

        final Metadata meta = new Metadata(doc, group, "sys");
        metadataRepos.save(meta);

        final LocalDateTime timestampUtc = LocalDateTime.now(java.time.ZoneOffset.UTC);
        final LocalDateTime timestampLocal = LocalDateTime.now();

        final Annotation annot = new Annotation();
        annot.setCreated(timestampUtc.minusMinutes(1));
        annot.setUpdated(timestampUtc);
        annot.setId("theid");
        annot.setUser(user);
        annot.setTargetSelectors("a");
        annot.setMetadata(meta);

        tag.setAnnotation(annot);
        annot.getTags().add(tag);

        annotRepos.save(annot);

        Assert.assertEquals(1, annotRepos.count());

        final Annotation foundAnnotation = annotRepos.findById("theid").orElse(null);
        Assert.assertNotNull(foundAnnotation);
        Assert.assertNotNull(foundAnnotation.getDocument());
        Assert.assertNotNull(foundAnnotation.getUser());
        Assert.assertNotNull(foundAnnotation.getGroup());
        Assert.assertNotNull(foundAnnotation.getTags());
        Assert.assertNull(foundAnnotation.getLinkedAnnotationId());
        Assert.assertEquals(1, foundAnnotation.getTags().size());
        Assert.assertEquals(AnnotationStatus.NORMAL, foundAnnotation.getStatus());
        Assert.assertFalse(foundAnnotation.isSentDeleted());

        // verify that the timestamps are returned in UTC again (ANOT-124)
        Assert.assertEquals(timestampUtc.minusMinutes(1), foundAnnotation.getCreated());
        Assert.assertEquals(timestampUtc, foundAnnotation.getUpdated());
        Assert.assertNotEquals(timestampLocal, foundAnnotation.getCreated());

        // now we delete the tag again (only the tag) using our custom delete function
        // (hibernate does not do it due to the way our model is configured; the Annotation is the master for the Annotation-Tag relation)
        tagService.removeTags(foundAnnotation.getTags());

        Assert.assertEquals(0, tagRepos.count());
        Assert.assertEquals(1, annotRepos.count()); // the annotation was not deleted, only its tag
    }

    /**
     * tests saving an incoming annotation to the database using the annotation service
     */
    @SuppressFBWarnings(value = SpotBugsAnnotations.FieldNotInitialized, justification = SpotBugsAnnotations.FieldNotInitializedReason)
    @Test
    public void testSaveAnnotationWithService() throws Exception {

        final String username = "acct:myusername@domain";
        final String authority = "domain";

        // add user to default group
        final User theUser = userRepos.save(new User(LOGIN));
        userGroupRepos.save(new UserGroup(theUser.getId(), defaultGroup.getId()));
        final UserInformation userInfo = new UserInformation(theUser, authority);

        // save the annotation using the annotation service
        Annotation returnAnnot = null;
        try {
            returnAnnot = annotService.createAnnotation(TestData.getTestAnnotationObject(username), userInfo);
        } catch (CannotCreateAnnotationException e) {
            Assert.fail("Unexpected exception received: " + e);
        }

        // verify properties that should have been filled
        Assert.assertNotNull(returnAnnot);
        Assert.assertFalse(returnAnnot.getId().isEmpty());
        Assert.assertNotNull(returnAnnot.getUpdated());

        // verify that the annotation has NORMAL status and no update info yet
        Assert.assertNotNull(returnAnnot.getStatus());
        Assert.assertEquals(AnnotationStatus.NORMAL, returnAnnot.getStatus());
        final JsonAnnotation jsReturnAnnot = conversionService.convertToJsonAnnotation(returnAnnot, userInfo);
        Assert.assertNull(jsReturnAnnot.getStatus().getUpdated_by());
        Assert.assertNull(jsReturnAnnot.getStatus().getUpdated());
        Assert.assertNull(jsReturnAnnot.getStatus().getUser_info());
        Assert.assertFalse(jsReturnAnnot.getStatus().isSentDeleted());

        // no replies or linked annotation
        Assert.assertNull(returnAnnot.getReferences());
        Assert.assertNull(returnAnnot.getLinkedAnnotationId());
        Assert.assertEquals("p", returnAnnot.getPrecedingText());
        Assert.assertEquals("s", returnAnnot.getSucceedingText());

        Assert.assertEquals(1, annotRepos.count());

        // verify that dependent objects are saved
        Annotation readAnnotation = null;
        readAnnotation = annotService.findAnnotationById(returnAnnot.getId(), LOGIN, null);

        Assert.assertNotNull(readAnnotation);
        Assert.assertEquals(1, documentRepos.count());
        Assert.assertEquals(1, userRepos.count());
        Assert.assertEquals(1, groupRepos.count());
        Assert.assertEquals(2, tagRepos.count());
        Assert.assertEquals(1, metadataRepos.count());
    }

    @Test
    public void testCreateAnnotationWithLongPreceedingAndSucceedingTexts() {

        final JsonAnnotation jsAnnot = TestData.getTestAnnotationObject("a");
        final char[] arr = new char[310];
        Arrays.fill(arr, '0');
        jsAnnot.setPrecedingText(new String(arr));
        jsAnnot.setSucceedingText(new String(arr));

        final User theUser = userRepos.save(new User(LOGIN));
        final UserInformation userInfo = new UserInformation(theUser, Authorities.ISC);

        Annotation returnAnnot = null;
        try {
            returnAnnot = annotService.createAnnotation(jsAnnot, userInfo);
        } catch (CannotCreateAnnotationException e) {
            Assert.fail("Unexpected exception received: " + e);
        }

        Assert.assertEquals(300, returnAnnot.getPrecedingText().length());
        Assert.assertEquals(300, returnAnnot.getSucceedingText().length());
    }

    // test creation of an annotation when no metadata is given - default entry should be created using configured default systemId
    @SuppressFBWarnings(value = SpotBugsAnnotations.FieldNotInitialized, justification = SpotBugsAnnotations.FieldNotInitializedReason)
    @Test
    public void testCreateAnnotationWithoutMetadata() throws CannotCreateAnnotationException {

        final String authority = Authorities.ISC;
        final String username = PREFIX + LOGIN + "@" + authority;

        // add user to default group
        final User theUser = userRepos.save(new User(LOGIN));
        userGroupRepos.save(new UserGroup(theUser.getId(), defaultGroup.getId()));
        final UserInformation userInfo = new UserInformation(theUser, authority);

        // let the annotation be created, but without providing any metadata
        final JsonAnnotation jsAnnot = TestData.getTestAnnotationObject(username);
        jsAnnot.getDocument().setMetadata(null);

        annotService.createAnnotation(jsAnnot, userInfo);

        // verify the annotation and a metadata record were saved
        Assert.assertEquals(1, annotRepos.count());
        Assert.assertEquals(1, metadataRepos.count());

        // verify that some system Id was supplied, namely the user's authority
        final List<Metadata> allMetas = (List<Metadata>) metadataRepos.findAll();
        final Metadata singleMeta = allMetas.get(0);
        Assert.assertTrue(StringUtils.hasLength(singleMeta.getSystemId()));
        Assert.assertEquals(authority, singleMeta.getSystemId());
    }

    // test creation of an annotation when a connected entity is set
    @SuppressFBWarnings(value = SpotBugsAnnotations.FieldNotInitialized, justification = SpotBugsAnnotations.FieldNotInitializedReason)
    @Test
    public void testCreateAnnotationWithConnectedEntity() throws CannotCreateAnnotationException {

        final String authority = Authorities.ISC;
        final String username = PREFIX + LOGIN + "@" + authority;
        final String ENTITY = "AGRI.H.3";

        // add user to default group; create a group for the connected entity as well
        final User theUser = userRepos.save(new User(LOGIN));
        userGroupRepos.save(new UserGroup(theUser.getId(), defaultGroup.getId()));

        final Group entityGroup = new Group(ENTITY, true);
        groupRepos.save(entityGroup);
        userGroupRepos.save(new UserGroup(theUser.getId(), entityGroup.getId()));

        final UserInformation userInfo = new UserInformation(theUser, authority);
        userInfo.setConnectedEntity(ENTITY);

        // let the annotation be created
        final JsonAnnotation jsAnnot = TestData.getTestAnnotationObject(username);
        jsAnnot.getDocument().setMetadata(null);

        final Annotation annot = annotService.createAnnotation(jsAnnot, userInfo);

        // verify the annotation and a metadata record were saved
        Assert.assertEquals(1, annotRepos.count());

        // verify that the connected entity was identified and stored in the annotation
        final Annotation readAnnot = annotRepos.findById(annot.getId()).get();
        Assert.assertNotNull(readAnnot.getConnectedEntityId());
        Assert.assertEquals(entityGroup.getId(), readAnnot.getConnectedEntityId());
    }

    // test creation of an annotation when a connected entity is set,
    // however there is no corresponding group stored in the database
    @SuppressFBWarnings(value = SpotBugsAnnotations.FieldNotInitialized, justification = SpotBugsAnnotations.FieldNotInitializedReason)
    @Test
    public void testCreateAnnotationWithUnknownConnectedEntity() throws CannotCreateAnnotationException {

        final String authority = Authorities.ISC;
        final String username = PREFIX + LOGIN + "@" + authority;
        final String ENTITY = "AGRI.H.3";

        // add user to default group; create a group for the connected entity as well
        final User theUser = userRepos.save(new User(LOGIN));
        userGroupRepos.save(new UserGroup(theUser.getId(), defaultGroup.getId()));

        final UserInformation userInfo = new UserInformation(theUser, authority);
        userInfo.setConnectedEntity(ENTITY);

        // let the annotation be created
        final JsonAnnotation jsAnnot = TestData.getTestAnnotationObject(username);
        jsAnnot.getDocument().setMetadata(null);

        final Annotation annot = annotService.createAnnotation(jsAnnot, userInfo);

        // verify the annotation and a metadata record were saved
        Assert.assertEquals(1, annotRepos.count());

        // verify that the connected entity was identified and stored in the annotation
        final Annotation readAnnot = annotRepos.findById(annot.getId()).get();
        Assert.assertNull(readAnnot.getConnectedEntityId()); // entity was unknown -> remains null
    }

    // test creation of an annotation with metadata containing systemId and version - should be saved to individual database columns
    @Test
    public void testCreateAnnotationWithSystemidAndVersion() throws CannotCreateAnnotationException {

        final String authority = Authorities.EdiT;
        final String username = PREFIX + LOGIN + "@" + authority;
        final String version = "2.0";

        // add user to default group
        final User theUser = userRepos.save(new User(LOGIN));
        userGroupRepos.save(new UserGroup(theUser.getId(), defaultGroup.getId()));
        final UserInformation userInfo = new UserInformation(theUser, authority);

        // let the annotation be created, but without providing any metadata
        final JsonAnnotation jsAnnot = TestData.getTestAnnotationObject(username);
        final SimpleMetadata meta = jsAnnot.getDocument().getMetadata();
        meta.put("version", version);
        meta.put("ISCRef", "ISC/1");

        annotService.createAnnotation(jsAnnot, userInfo);

        // verify the annotation and a metadata record were saved
        Assert.assertEquals(1, annotRepos.count());
        Assert.assertEquals(1, metadataRepos.count());

        // verify that some system Id was supplied, namely the user's authority
        final List<Metadata> allMetas = (List<Metadata>) metadataRepos.findAll();
        final Metadata singleMeta = allMetas.get(0);
        Assert.assertTrue(StringUtils.hasLength(singleMeta.getSystemId()));
        Assert.assertEquals(authority, singleMeta.getSystemId());
        Assert.assertEquals(version, singleMeta.getVersion());
    }

    // test creation of an annotation when given metadata has response status set to SENT - should be refused
    @SuppressFBWarnings(value = SpotBugsAnnotations.FieldNotInitialized, justification = SpotBugsAnnotations.FieldNotInitializedReason)
    @SuppressWarnings("PMD.EmptyCatchBlock")
    @Test
    public void testCannotCreateAnnotationWithSentResponseStatus() {

        final String authority = Authorities.ISC;
        final String username = PREFIX + LOGIN + "@" + authority;

        // add user to default group
        final User theUser = userRepos.save(new User(LOGIN));
        userGroupRepos.save(new UserGroup(theUser.getId(), defaultGroup.getId()));
        final UserInformation userInfo = new UserInformation(theUser, authority);

        // let the annotation be created, but without providing any metadata
        final JsonAnnotation jsAnnot = TestData.getTestAnnotationObject(username);
        final SimpleMetadata metadata = new SimpleMetadata();
        metadata.put("responseStatus", "SENT");
        jsAnnot.getDocument().setMetadata(metadata);

        try {
            annotService.createAnnotation(jsAnnot, userInfo);
            Assert.fail("Did not receive expected exception");
        } catch (CannotCreateAnnotationException ccae) {
            // OK
        }

        // verify that no annotation was created
        Assert.assertEquals(0, annotRepos.count());
        Assert.assertEquals(0, metadataRepos.count());
    }

    // test creation of an annotation when metadata is given, but systemId is missing - systemId from user token should be used
    @SuppressFBWarnings(value = SpotBugsAnnotations.FieldNotInitialized, justification = SpotBugsAnnotations.FieldNotInitializedReason)
    @Test
    public void testCreateAnnotationWithoutSystemIdMetadata() throws CannotCreateAnnotationException {

        final String authority = Authorities.ISC;
        final String username = PREFIX + LOGIN + "@" + authority;

        // add user to default group
        final User theUser = userRepos.save(new User(LOGIN));
        userGroupRepos.save(new UserGroup(theUser.getId(), defaultGroup.getId()));
        final UserInformation userInfo = new UserInformation(theUser, authority);

        // let the annotation be created, but without providing any metadata
        final JsonAnnotation jsAnnot = TestData.getTestAnnotationObject(username);
        jsAnnot.getDocument().getMetadata().remove("systemId");

        annotService.createAnnotation(jsAnnot, userInfo);

        // verify the annotation and a metadata record were saved
        Assert.assertEquals(1, annotRepos.count());
        Assert.assertEquals(1, metadataRepos.count());

        // verify that some system Id was supplied, namely the user's authority
        final List<Metadata> allMetas = (List<Metadata>) metadataRepos.findAll();
        final Metadata singleMeta = allMetas.get(0);
        Assert.assertTrue(StringUtils.hasLength(singleMeta.getSystemId()));
        Assert.assertEquals(authority, singleMeta.getSystemId());
    }

    /**
     * tests saving an annotation fails when no user is specified
     */
    @Test
    @SuppressWarnings("PMD.EmptyCatchBlock")
    public void testCannotCreateAnnotationWithoutUser() {

        final String username = "acct:myusername@domain.com";

        // save the annotation, but let user's login be missing
        try {
            annotService.createAnnotation(TestData.getTestAnnotationObject(username), null);
            Assert.fail("Expected exception about missing user not received");
        } catch (CannotCreateAnnotationException e) {
            // OK
        }

        Assert.assertEquals(0, annotRepos.count());
    }

    /**
     * test that different metadata sets are created and assigned to annotations if different requests were sent
     */
    @Test
    public void testCreatingAnnotationsWithDifferentMetadata() throws Exception {

        final String login = "somebody";
        final String authority = Authorities.EdiT;
        final String username = PREFIX + login + "@" + authority;

        // add user to default group
        final User theUser = userRepos.save(new User(login));
        userGroupRepos.save(new UserGroup(theUser.getId(), defaultGroup.getId()));
        final UserInformation userInfo = new UserInformation(theUser, authority);

        // let the annotation be created
        final JsonAnnotation jsAnnot = TestData.getTestAnnotationObject(username);
        jsAnnot.getDocument().getMetadata().put("someprop", "someval");

        final Annotation annot = annotService.createAnnotation(jsAnnot, userInfo);

        // verify the annotation and a metadata record were saved
        Assert.assertEquals(1, annotRepos.count());
        Assert.assertEquals(1, metadataRepos.count());

        // create a second annotation with different metadata, but for same document/group/system
        // -> new metadata set should be created
        final JsonAnnotation jsAnnotSecond = TestData.getTestAnnotationObject(username);
        jsAnnotSecond.getDocument().getMetadata().put("someprop", "someval");
        jsAnnotSecond.getDocument().getMetadata().put("anotherprop", "anotherval");// additional property, which was not set for first annotation

        final Annotation annotSecond = annotService.createAnnotation(jsAnnotSecond, userInfo);

        // verify that two annotations and metadata are present, and but annotations are assigned to different metadata sets
        Assert.assertEquals(2, annotRepos.count());
        Assert.assertEquals(2, metadataRepos.count());

        final long firstMetaId = annotRepos.findById(annot.getId()).get().getMetadata().getId();
        final long secondMetaId = annotRepos.findById(annotSecond.getId()).get().getMetadata().getId();
        Assert.assertNotEquals(firstMetaId, secondMetaId);
    }

    // check that creation of a forwarded annotation is feasible and the appropriate properties are saved
    @Test
    public void testCreateForwardedAnnotation() throws CannotCreateAnnotationException {

        final String login = "john";
        final String ForwardText = "I wanted to forward this";
        final String authority = Authorities.EdiT;
        final String username = PREFIX + login + "@" + authority;

        // add user to default group
        final User theUser = userRepos.save(new User(login));
        Group originGroup = groupRepos.save(new Group("Test", false));
        userGroupRepos.save(new UserGroup(theUser.getId(), defaultGroup.getId()));
        userGroupRepos.save(new UserGroup(theUser.getId(), originGroup.getId()));
        final UserInformation userInfo = new UserInformation(theUser, authority);
        // let the annotation be created
        final JsonAnnotation jsAnnot = TestData.getTestAnnotationObject(username);
        jsAnnot.getDocument().getMetadata().put("oneprop", "oneval");

        // set it to be forwarded
        jsAnnot.setForwarded(true);
        jsAnnot.setForwardedJustification(ForwardText);
        jsAnnot.setOriginGroup(originGroup.getName());

        // act
        final Annotation annot = annotService.createAnnotation(jsAnnot, userInfo);

        // verify the annotation's forward properties
        Assert.assertEquals(1, annotRepos.count());
        Assert.assertTrue(annot.isForwarded());
        Assert.assertEquals(jsAnnot.getForwardJustification(), annot.getForwardJustification());
        Assert.assertEquals(jsAnnot.getOriginGroup(), annot.getOriginGroup().getName());
    }

    @Test
    public void createForwardedAnnotationNok() throws Exception{
        final String login = "john";
        final String ForwardText = "I wanted to forward this";
        final String authority = Authorities.EdiT;
        final String username = PREFIX + login + "@" + authority;

        // add user to default group
        final User theUser = userRepos.save(new User(login));
        userGroupRepos.save(new UserGroup(theUser.getId(), defaultGroup.getId()));
        final UserInformation userInfo = new UserInformation(theUser, authority);
        // let the annotation be created
        final JsonAnnotation jsAnnot = TestData.getTestAnnotationObject(username);
        jsAnnot.getDocument().getMetadata().put("oneprop", "oneval");

        // set it to be forwarded
        jsAnnot.setForwarded(true);
        jsAnnot.setForwardedJustification(ForwardText);
        jsAnnot.setOriginGroup(defaultGroup.getName());

        try {
            annotService.createAnnotation(jsAnnot, userInfo);
        } catch (CannotCreateAnnotationException e) {
            Assert.assertEquals("Group equal to group of original annotation", e.getMessage());
            return;
        }

        Assert.fail();
    }
}
