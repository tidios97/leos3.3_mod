package eu.europa.ec.leos.annotate.integration;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import eu.europa.ec.leos.annotate.Authorities;
import eu.europa.ec.leos.annotate.helper.SpotBugsAnnotations;
import eu.europa.ec.leos.annotate.helper.TestData;
import eu.europa.ec.leos.annotate.helper.TestDbHelper;
import eu.europa.ec.leos.annotate.model.UserInformation;
import eu.europa.ec.leos.annotate.model.entity.*;
import eu.europa.ec.leos.annotate.model.web.annotation.JsonAnnotation;
import eu.europa.ec.leos.annotate.repository.*;
import eu.europa.ec.leos.annotate.services.*;
import eu.europa.ec.leos.annotate.services.exceptions.CannotCreateMetadataException;
import eu.europa.ec.leos.annotate.services.exceptions.CannotUpdateAnnotationException;
import eu.europa.ec.leos.annotate.services.impl.AnnotationServiceImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.config.name=anot")
@ActiveProfiles("test")
public class AnnotationUpdateWithMetadataMockTest {

    private Group defaultGroup;

    // -------------------------------------
    // Required services and repositories
    // -------------------------------------
    @InjectMocks
    private AnnotationServiceImpl annotService; // mocked MetadataService is injected

    @Autowired
    private AnnotationConversionService conversionService;

    @Autowired
    private AnnotationService realAnnotService;

    @Mock
    @Qualifier("annotationTestRepos")
    private AnnotationTestRepository annotRepos;

    @Autowired
    private DocumentRepository documentRepos;

    @Autowired
    private UserRepository userRepos;

    @Autowired
    private GroupRepository groupRepos;

    @Autowired
    private UserGroupRepository userGroupRepos;

    @Mock
    private MetadataService metadataService;

    @Mock
    private UserService userService;

    @Mock
    private GroupService groupService;

    @Mock
    private DocumentService documentService;

    private AutoCloseable mocks;

    // -------------------------------------
    // Cleanup of database content before running new test
    // -------------------------------------
    @Before
    public void cleanDatabaseBeforeTests() {

        TestDbHelper.cleanupRepositories(this);
        defaultGroup = TestDbHelper.insertDefaultGroup(groupRepos);

        mocks = MockitoAnnotations.openMocks(this);
    }

    @After
    public void cleanDatabaseAfterTests() throws Exception {

        TestDbHelper.cleanupRepositories(this);
        mocks.close();
    }

    // -------------------------------------
    // Tests
    // -------------------------------------

    // test updating of an annotation's group when metadata service has internal failures - annotation update should throw expected exception
    @SuppressFBWarnings(value = SpotBugsAnnotations.FieldNotInitialized, justification = SpotBugsAnnotations.FieldNotInitializedReason)
    @SuppressWarnings("PMD.EmptyCatchBlock")
    @Test
    public void testCreateAnnotation_MetadataService_Failure() throws Exception {

        final String login = "demo";
        final String authority = Authorities.ISC;
        final String username = "acct:" + login + "@" + authority;
        final UserInformation userInfo = new UserInformation(login, null, authority);
        final String newGroupName = "newGroup";

        // add user to default group
        final User theUser = userRepos.save(new User(login));
        userGroupRepos.save(new UserGroup(theUser.getId(), defaultGroup.getId()));
        userInfo.setUser(theUser);

        // save annotation in database
        final JsonAnnotation jsAnnot = TestData.getTestAnnotationObject(username);
        final Annotation annot = realAnnotService.createAnnotation(jsAnnot, userInfo);

        // retrieve objects from real DB
        final Document doc = documentRepos.findByUri(annot.getDocument().getUri());
        final Annotation savedAnnot = realAnnotService.findAnnotationById(annot.getId());

        // configure mocks; especially MetadataService should throw a custom exception
        Mockito.when(userService.findByLoginAndContext(Mockito.anyString(), Mockito.anyString())).thenReturn(theUser);
        Mockito.when(groupService.findGroupByName("__world__")).thenReturn(defaultGroup);
        Mockito.when(groupService.findGroupByName(newGroupName)).thenReturn(new Group(newGroupName, true));
        Mockito.when(documentService.findDocumentByUri(Mockito.any(URI.class))).thenReturn(doc);
        Mockito.when(metadataService.saveMetadata(Mockito.any(Metadata.class)))
                .thenThrow(new CannotCreateMetadataException("error"))
                .thenThrow(new RuntimeException("error"));
        Mockito.when(annotRepos.findById(Mockito.anyString())).thenReturn(Optional.of(savedAnnot));

        // update annotation
        final JsonAnnotation jsAnnotCreated = conversionService.convertToJsonAnnotation(annot, userInfo);
        jsAnnotCreated.setGroup(newGroupName);

        // let the annotation be created - should not be successful
        try {
            annotService.updateAnnotation(annot.getId(), jsAnnotCreated, userInfo);
            Assert.fail("Expected exception due to metadata service failure not received");
        } catch (CannotUpdateAnnotationException ccae) {
            // OK
        } catch (Exception e) {
            Assert.fail("Received unexpected exception");
        }

        // test again, but now MetadataService returns non-custom exception
        try {
            annotService.updateAnnotation(annot.getId(), jsAnnotCreated, userInfo);
            Assert.fail("Expected exception due to metadata service failure not received");
        } catch (CannotUpdateAnnotationException ccae) {
            // OK
        } catch (Exception e) {
            Assert.fail("Received unexpected exception");
        }
    }
}
