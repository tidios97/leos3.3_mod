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
package eu.europa.ec.leos.annotate.integration;

import eu.europa.ec.leos.annotate.Authorities;
import eu.europa.ec.leos.annotate.helper.TestDbHelper;
import eu.europa.ec.leos.annotate.model.ResponseStatus;
import eu.europa.ec.leos.annotate.model.UserInformation;
import eu.europa.ec.leos.annotate.model.entity.*;
import eu.europa.ec.leos.annotate.model.helper.TagBuilder;
import eu.europa.ec.leos.annotate.model.search.AnnotationSearchResult;
import eu.europa.ec.leos.annotate.model.search.DocumentAnnotationsResult;
import eu.europa.ec.leos.annotate.model.search.LegDocumentAnnotationsResult;
import eu.europa.ec.leos.annotate.repository.*;
import eu.europa.ec.leos.annotate.services.AnnotationSearchService;
import eu.europa.ec.leos.annotate.services.impl.util.ZipContent;
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

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.config.name=anot")
@ActiveProfiles("test")
@SuppressWarnings("PMD.TooManyMethods")
public class DocumentAnnotationsTest {

    @Autowired
    private AnnotationSearchService annotationSearchService;

    @Autowired
    @Qualifier("annotationTestRepos")
    private AnnotationTestRepository annotationTestRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private MetadataRepository metadataRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserGroupRepository userGroupRepository;

    private static final String FIRST_DOC_NAME = "first_document";
    private static final String SECOND_DOC_NAME = "second_document";
    private static final String FIRST_DOC_URL = "uri://LEOS/" + FIRST_DOC_NAME;
    private static final String SECOND_DOC_URL = "uri://LEOS/" + SECOND_DOC_NAME;

    private static final String ID_EDIT_USER1_PUBLIC_ANNOTATION1 = "ID_EU1_A1";
    private static final String ID_EDIT_USER1_PUBLIC_ANNOTATION2 = "ID_EU1_A2";
    private static final String ID_EDIT_USER1_PUBLIC_SUGGESTION1 = "ID_EU1_S1";
    private static final String ID_EDIT_USER1_PUBLIC_SUGGESTION2 = "ID_EU1_S2";
    private static final String ID_EDIT_USER2_PRIVATE_ANNOTATION1 = "ID_EU2_A1";
    private static final String ID_EDIT_USER2_PRIVATE_ANNOTATION2 = "ID_EU2_A2";
    private static final String ID_EDIT_USER2_PRIVATE_REPLY1 = "ID_EU2_R1";
    private static final String ID_EDIT_USER2_PRIVATE_REPLY2 = "ID_EU2_R2";
    private static final String ID_ISC_USER1_PUBLIC_ANNOTATION1 = "ID_IU1_A1";
    private static final String ID_ISC_USER2_PUBLIC_ANNOTATION1 = "ID_IU2_A1";
    private static final String ID_ISC_USER2_PUBLIC_SUGGESTION1 = "ID_IU2_S1";
    private static final String ID_ISC_USER3_PUBLIC_REPLY1 = "ID_IU3_R1";
    private static final String ID_ISC_USER3_PUBLIC_SUGGESTION1 = "ID_IU3_S1";

    private final TestUser EDIT_USER_INFO_01 = new TestUser("editUserInfo01", Authorities.EdiT);
    private final TestUser EDIT_USER_INFO_02 = new TestUser("editUserInfo02", Authorities.EdiT);
    private final TestUser ISC_USER_INFO_01 = new TestUser("iscUserInfo01", Authorities.ISC);
    private final TestUser ISC_USER_INFO_02 = new TestUser("iscUserInfo02", Authorities.ISC);
    private final TestUser ISC_USER_INFO_03 = new TestUser("iscUserInfo03", Authorities.ISC);
    private final TestUser SUPPORT_USER_INFO = new TestUser("supportUserInfo", Authorities.Support);

    @Before
    public void beforeTest() throws URISyntaxException {
        TestDbHelper.cleanupRepositories(this);

        final List<Group> groups = Arrays.asList(
                TestDbHelper.insertDefaultGroup(groupRepository),
                insertGroup("__GROUP_01__", "Group01", "Description for Group 01", true),
                insertGroup("__GROUP_02__", "Group02", "Description for Group 02", true),
                insertGroup("__GROUP_03__", "Group03", "Description for Group 03", true),
                insertGroup("__GROUP_04__", "Group04", "Description for Group 04", true));

        userRepository.saveAll(Arrays.asList(EDIT_USER_INFO_01.getUser(), EDIT_USER_INFO_02.getUser(),
                ISC_USER_INFO_01.getUser(), ISC_USER_INFO_02.getUser(), ISC_USER_INFO_03.getUser(),
                SUPPORT_USER_INFO.getUser()));

        addUserToGroups(EDIT_USER_INFO_01, groups);
        addUserToGroups(EDIT_USER_INFO_02, groups);
        addUserToGroups(ISC_USER_INFO_01, groups);
        addUserToGroups(ISC_USER_INFO_02, groups);
        addUserToGroups(ISC_USER_INFO_03, groups);

        final List<Document> documents = new ArrayList<>();
        documents.add(new Document(new URI(FIRST_DOC_URL), "First document title"));
        documents.add(new Document(new URI(SECOND_DOC_URL), "Second document title"));
        documentRepository.saveAll(documents);

        final List<Metadata> firstDocumentMetadata = new ArrayList<>();
        firstDocumentMetadata.add(new Metadata(documents.get(0), groups.get(0), Authorities.EdiT));
        firstDocumentMetadata.add(new Metadata(documents.get(0), groups.get(1), Authorities.EdiT));

        Metadata iscMetadata = new Metadata(documents.get(0), groups.get(2), Authorities.ISC);
        iscMetadata.setResponseStatus(ResponseStatus.SENT);
        firstDocumentMetadata.add(iscMetadata);

        iscMetadata = new Metadata(documents.get(0), groups.get(2), Authorities.ISC);
        iscMetadata.setResponseStatus(ResponseStatus.IN_PREPARATION);
        firstDocumentMetadata.add(iscMetadata);

        iscMetadata = new Metadata(documents.get(0), groups.get(3), Authorities.ISC);
        iscMetadata.setResponseStatus(ResponseStatus.SENT);
        firstDocumentMetadata.add(iscMetadata);

        metadataRepository.saveAll(firstDocumentMetadata);

        final List<Metadata> secondDocumentMetadata = new ArrayList<>();
        secondDocumentMetadata.add(new Metadata(documents.get(1), groups.get(0), Authorities.EdiT));
        secondDocumentMetadata.add(new Metadata(documents.get(1), groups.get(1), Authorities.EdiT));
        metadataRepository.saveAll(secondDocumentMetadata);

        createFirstDocumentTestData(documents.get(0), firstDocumentMetadata);
        createSecondDocumentTestData(documents.get(1), secondDocumentMetadata);
    }

    @SuppressWarnings("PMD.ExcessiveMethodLength")
    private void createFirstDocumentTestData(final Document document, final List<Metadata> metadata) {
        // two dummy selectors
        final String documentSelector = "[{\"selector\":null,\"source\":\"" + document.getUri() + "\"}]";
        final List<String> suggestionTags = Arrays.asList(Annotation.ANNOTATION_SUGGESTION);

        // EditUser1 public annotation A1 in group G1 (EdiT)
        insertAnnotation(ID_EDIT_USER1_PUBLIC_ANNOTATION1, "", "EditUser1 annotation",
                documentSelector, Collections.emptyList(), metadata.get(0), EDIT_USER_INFO_01.getUser(),
                true);

        // EditUser1 creates a public suggestion S1 in group G1 (EdiT)
        insertAnnotation(ID_EDIT_USER1_PUBLIC_SUGGESTION1, "", "EditUser1 Suggestion",
                documentSelector, suggestionTags, metadata.get(0), EDIT_USER_INFO_01.getUser(),
                true);

        // EditUser2 creates a private reply to A1 in group G1 (EdiT)
        insertAnnotation(ID_EDIT_USER2_PRIVATE_REPLY1, ID_EDIT_USER1_PUBLIC_ANNOTATION1, "EditUser2 Reply",
                documentSelector, Collections.emptyList(), metadata.get(0), EDIT_USER_INFO_02.getUser(),
                false);

        // EditUser2 creates private annotation A2 in group G2 (EdiT)
        insertAnnotation(ID_EDIT_USER2_PRIVATE_ANNOTATION1, "", "EditUser2 private annotation",
                documentSelector, Collections.emptyList(), metadata.get(1), EDIT_USER_INFO_02.getUser(),
                false);

        // IscUser1 creates a public ISC annotation A3 in group G3, status: IN_PREPARATION
        insertAnnotation(ID_ISC_USER1_PUBLIC_ANNOTATION1, "", "IscUser1 Annotation",
                documentSelector, Collections.emptyList(), metadata.get(3), ISC_USER_INFO_01.getUser(),
                true);

        // IscUser2 creates a public ISC annotation A4 in group G3, status: SENT
        insertAnnotation(ID_ISC_USER2_PUBLIC_ANNOTATION1, "", "IscUser2 Annotation",
                documentSelector, Collections.emptyList(), metadata.get(2), ISC_USER_INFO_02.getUser(),
                true);

        // IscUser3 creates a public reply to A4 in group G3, status: SENT
        insertAnnotation(ID_ISC_USER3_PUBLIC_REPLY1, ID_ISC_USER2_PUBLIC_ANNOTATION1, "IscUser3 Reply",
                documentSelector, Collections.emptyList(), metadata.get(2), ISC_USER_INFO_03.getUser(),
                true);

        // IscUser2 creates a public ISC suggestion S2 in group G3, status: IN_PREPARATION
        insertAnnotation(ID_ISC_USER2_PUBLIC_SUGGESTION1, "", "IscUser2 suggestion",
                documentSelector, Collections.emptyList(), metadata.get(3), ISC_USER_INFO_02.getUser(),
                true);

        // IscUser3 creates a public ISC suggestion S1 in group G4, status: SENT
        insertAnnotation(ID_ISC_USER3_PUBLIC_SUGGESTION1, "", "IscUser3 suggestion",
                documentSelector, Collections.emptyList(), metadata.get(4), ISC_USER_INFO_03.getUser(),
                true);
    }

    @SuppressWarnings("PMD.ExcessiveMethodLength")
    private void createSecondDocumentTestData(
            final Document document, final List<Metadata> metadata) {

        // two dummy selectors
        final String documentSelector = "[{\"selector\":null,\"source\":\"" + document.getUri() + "\"}]";

        // EditUser1 public annotation A1 in group G1 (EdiT)
        insertAnnotation(ID_EDIT_USER1_PUBLIC_ANNOTATION2, "", "EditUser1 annotation",
                documentSelector, Collections.emptyList(), metadata.get(0), EDIT_USER_INFO_01.getUser(),
                true);

        // EditUser1 creates a public suggestion S1 in group G1 (EdiT)
        insertAnnotation(ID_EDIT_USER1_PUBLIC_SUGGESTION2, "", "EditUser1 Suggestion",
                documentSelector, Arrays.asList(Annotation.ANNOTATION_SUGGESTION), metadata.get(0), EDIT_USER_INFO_01.getUser(),
                true);

        // EditUser2 creates a private reply to A1 in group G1 (EdiT)
        insertAnnotation(ID_EDIT_USER2_PRIVATE_REPLY2, ID_EDIT_USER1_PUBLIC_ANNOTATION2, "EditUser2 Reply",
                documentSelector, Collections.emptyList(), metadata.get(0), EDIT_USER_INFO_02.getUser(),
                false);

        // EditUser2 creates private annotation A2 in group G2 (EdiT)
        insertAnnotation(ID_EDIT_USER2_PRIVATE_ANNOTATION2, "", "EditUser2 private annotation",
                documentSelector, Collections.emptyList(), metadata.get(1), EDIT_USER_INFO_02.getUser(),
                false);
    }

    @After
    public void afterTest() {
        TestDbHelper.cleanupRepositories(this);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDocumentAnnotationsWithoutUri() {
        annotationSearchService.getDocumentAnnotations(null, EDIT_USER_INFO_01.getUserInformation());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDocumentAnnotationsNoSupportUsers() {
        try {
            annotationSearchService.getDocumentAnnotations(FIRST_DOC_URL, EDIT_USER_INFO_01.getUserInformation());
            Assert.fail("Missing exception from method getDocumentAnnotations");
        } catch (Exception ex) {
        }
        annotationSearchService.getDocumentAnnotations(FIRST_DOC_URL, ISC_USER_INFO_01.getUserInformation());
    }

    @Test
    public void testGetDocumentAnnotations() {
        final DocumentAnnotationsResult documentAnnotationsResult = annotationSearchService.getDocumentAnnotations(FIRST_DOC_URL,
                SUPPORT_USER_INFO.getUserInformation());
        validateFirstDocumentAnnotations(documentAnnotationsResult);
    }

    @Test
    public void testPostDocumentAnnotations() {

        final List<ZipContent> postZipContent = buildPostTestZipContent();
        final List<LegDocumentAnnotationsResult> searchResults = annotationSearchService.getLegDocumentAnnotations(postZipContent,
                SUPPORT_USER_INFO.getUserInformation());

        Assert.assertEquals(2, searchResults.size());
        assertDocumentResultContained(searchResults, getDocumentXmlName(FIRST_DOC_NAME));
        assertDocumentResultContained(searchResults, getDocumentXmlName(SECOND_DOC_NAME));

        validateFirstDocumentAnnotations(searchResults.get(0).getAnnotationsResult());
        validateSecondDocumentAnnotations(searchResults.get(1).getAnnotationsResult());
    }

    private void validateFirstDocumentAnnotations(
            final DocumentAnnotationsResult documentAnnotationsResult) {

        Assert.assertNotNull(documentAnnotationsResult);
        Assert.assertNotNull(documentAnnotationsResult.getSearchResult());
        Assert.assertNotNull(documentAnnotationsResult.getReplies());

        final AnnotationSearchResult annotationSearchResult = documentAnnotationsResult.getSearchResult();
        final List<Annotation> replies = documentAnnotationsResult.getReplies();

        Assert.assertEquals("Unexpected number of Annotations", 5, annotationSearchResult.size());
        assertAnnotationContained(annotationSearchResult.getItems(), ID_EDIT_USER1_PUBLIC_ANNOTATION1);
        assertAnnotationContained(annotationSearchResult.getItems(), ID_EDIT_USER1_PUBLIC_SUGGESTION1);
        assertAnnotationContained(annotationSearchResult.getItems(), ID_EDIT_USER2_PRIVATE_ANNOTATION1);
        assertAnnotationContained(annotationSearchResult.getItems(), ID_ISC_USER2_PUBLIC_ANNOTATION1);
        assertAnnotationContained(annotationSearchResult.getItems(), ID_ISC_USER3_PUBLIC_SUGGESTION1);

        Assert.assertEquals("Unexpected number of Replies", 2, replies.size());
        assertAnnotationContained(replies, ID_EDIT_USER2_PRIVATE_REPLY1);
        assertAnnotationContained(replies, ID_ISC_USER3_PUBLIC_REPLY1);
    }

    private void validateSecondDocumentAnnotations(
            final DocumentAnnotationsResult documentAnnotationsResult) {
        Assert.assertNotNull(documentAnnotationsResult);
        Assert.assertNotNull(documentAnnotationsResult.getSearchResult());
        Assert.assertNotNull(documentAnnotationsResult.getReplies());

        final AnnotationSearchResult annotationSearchResult = documentAnnotationsResult.getSearchResult();
        final List<Annotation> replies = documentAnnotationsResult.getReplies();

        Assert.assertEquals("Unexpected number of Annotations", 3, annotationSearchResult.size());
        assertAnnotationContained(annotationSearchResult.getItems(), ID_EDIT_USER1_PUBLIC_ANNOTATION2);
        assertAnnotationContained(annotationSearchResult.getItems(), ID_EDIT_USER1_PUBLIC_SUGGESTION2);
        assertAnnotationContained(annotationSearchResult.getItems(), ID_EDIT_USER2_PRIVATE_ANNOTATION2);

        Assert.assertEquals("Unexpected number of Replies", 1, replies.size());
        assertAnnotationContained(replies, ID_EDIT_USER2_PRIVATE_REPLY2);
    }

    private void addUserToGroups(final TestUser user, final List<Group> groups) {
        groups.forEach(group -> {
            userGroupRepository.save(new UserGroup(user.getId(), group.getId()));
        });
    }

    private String getDocumentXmlName(final String documentUri) {
        return String.format("%s.xml", documentUri);
    }

    private List<ZipContent> buildPostTestZipContent() {
        
        final List<ZipContent> result = new ArrayList<>();
        result.add(new ZipContent(getDocumentXmlName(FIRST_DOC_NAME), "".getBytes(StandardCharsets.UTF_8)));
        result.add(new ZipContent(getDocumentXmlName(SECOND_DOC_NAME), "".getBytes(StandardCharsets.UTF_8)));
        return result;
    }

    private void assertDocumentResultContained(final List<LegDocumentAnnotationsResult> items, 
            final String documentName) {
        Assert.assertTrue(String.format("Document '%s' is missing in result", documentName),
                items.stream().anyMatch(ann -> ann.getDocumentName().equals(documentName)));
    }

    private void assertAnnotationContained(final List<Annotation> items, final String annotId) {
        Assert.assertTrue(String.format("Missing expected annotation '%s'", annotId),
                items.stream().anyMatch(ann -> ann.getId().equals(annotId)));
    }

    private Group insertGroup(final String name, final String displayName, final String description, final Boolean isPublic) {
        final Group newGroup = new Group(name, displayName, description, isPublic);
        groupRepository.save(newGroup);
        return newGroup;
    }

    private Annotation insertAnnotation(
            final String annotId, final String reference,
            final String text, final String selector,
            final List<String> tags, final Metadata metadata,
            final User user, final Boolean isShared) {
        
        final Annotation annotation = new Annotation();
        annotation.setId(annotId);
        annotation.setCreated(LocalDateTime.of(2012, 12, 21, 12, 0));
        annotation.setUpdated(LocalDateTime.of(2012, 12, 22, 12, 0));
        annotation.setMetadata(metadata);
        annotation.setReferences(reference);
        annotation.setShared(isShared);
        annotation.setTags(TagBuilder.getTagList(tags, annotation));
        annotation.setTargetSelectors(selector);
        annotation.setText(text);
        annotation.setUser(user);
        annotationTestRepository.save(annotation);

        return annotation;
    }

    private class TestUser {
        private final String authority;
        private final User user;

        public TestUser(final String login, final String authority) {
            this.authority = authority;
            this.user = new User(login);
        }

        public User getUser() {
            return user;
        }

        public Long getId() {
            return user.getId();
        }

        public UserInformation getUserInformation() {
            final Token token = new Token(user, authority, "accessToken1", LocalDateTime.now().plusMinutes(5),
                    "refreshToken1", LocalDateTime.now().plusMinutes(5));
            return new UserInformation(token);
        }
    }
}
