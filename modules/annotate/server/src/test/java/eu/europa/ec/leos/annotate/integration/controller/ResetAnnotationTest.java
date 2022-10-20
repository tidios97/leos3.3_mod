package eu.europa.ec.leos.annotate.integration.controller;

import java.time.LocalDateTime;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import eu.europa.ec.leos.annotate.Authorities;
import eu.europa.ec.leos.annotate.helper.SerialisationHelper;
import eu.europa.ec.leos.annotate.helper.SpotBugsAnnotations;
import eu.europa.ec.leos.annotate.helper.TestData;
import eu.europa.ec.leos.annotate.helper.TestDbHelper;
import eu.europa.ec.leos.annotate.helper.TestHelper;
import eu.europa.ec.leos.annotate.model.AnnotationStatus;
import eu.europa.ec.leos.annotate.model.AuthenticatedUserStore;
import eu.europa.ec.leos.annotate.model.UserInformation;
import eu.europa.ec.leos.annotate.model.entity.Annotation;
import eu.europa.ec.leos.annotate.model.entity.Group;
import eu.europa.ec.leos.annotate.model.entity.Token;
import eu.europa.ec.leos.annotate.model.entity.User;
import eu.europa.ec.leos.annotate.model.entity.UserGroup;
import eu.europa.ec.leos.annotate.model.web.JsonFailureResponse;
import eu.europa.ec.leos.annotate.model.web.annotation.JsonAnnotation;
import eu.europa.ec.leos.annotate.model.web.annotation.JsonResetSuccessResponse;
import eu.europa.ec.leos.annotate.repository.AnnotationTestRepository;
import eu.europa.ec.leos.annotate.repository.GroupRepository;
import eu.europa.ec.leos.annotate.repository.TokenRepository;
import eu.europa.ec.leos.annotate.repository.UserGroupRepository;
import eu.europa.ec.leos.annotate.repository.UserRepository;
import eu.europa.ec.leos.annotate.services.AnnotationService;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.config.name=anot")
@WebAppConfiguration
@ActiveProfiles("test")
public class ResetAnnotationTest {

    private static final String ACCESS_TOKEN = "demoaccesstoken";
    private static final String REFRESH_TOKEN = "refr";
    private static final String DEFAULT_AUTHORITY = "auth";
    private static final String API_PREFIX = "/api/annotations/reset/";

    // -------------------------------------
    // Required services and repositories
    // -------------------------------------

    @Autowired
    private AnnotationService annotService;

    @Autowired
    private AuthenticatedUserStore authUser;

    @Autowired
    @Qualifier("annotationTestRepos")
    private AnnotationTestRepository annotRepos;

    @Autowired
    private GroupRepository groupRepos;

    @Autowired
    private UserRepository userRepos;

    @Autowired
    private UserGroupRepository userGroupRepos;

    @Autowired
    private TokenRepository tokenRepos;

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    private User user;

    // -------------------------------------
    // Cleanup of database content
    // -------------------------------------
    @Before
    public void setupTests() {

        TestDbHelper.cleanupRepositories(this);
        final Group defaultGroup = TestDbHelper.insertDefaultGroup(groupRepos);

        user = new User("demo");
        userRepos.save(user);
        userGroupRepos.save(new UserGroup(user.getId(), defaultGroup.getId()));

        final DefaultMockMvcBuilder builder = MockMvcBuilders.webAppContextSetup(this.wac);
        this.mockMvc = builder.build();
    }

    @After
    public void cleanDatabaseAfterTests() {
        TestDbHelper.cleanupRepositories(this);
        authUser.clear();
    }

    // -------------------------------------
    // Tests
    // -------------------------------------

    /**
     * successfully reset an existing annotation status to normal, expected HTTP 200 and ID
     * of the annotation
     */
    @SuppressFBWarnings(value = SpotBugsAnnotations.FieldNotInitialized, justification = SpotBugsAnnotations.FieldNotInitializedReason)
    @Test
    public void testResetAnnotationOk() throws Exception {
        final UserInformation userInfo = new UserInformation(user, Authorities.EdiT);

        tokenRepos.save(new Token(user, Authorities.EdiT, ACCESS_TOKEN, LocalDateTime.now().plusMinutes(5),
                REFRESH_TOKEN, LocalDateTime.now().plusMinutes(5)));

        final String hypothesisUserAccount = "acct:user@domain.eu";

        // preparation: save an annotation that can be reset as normal later on
        final JsonAnnotation jsAnnot = TestData.getTestAnnotationObject(hypothesisUserAccount);
        final Annotation annot = annotService.createAnnotation(jsAnnot, userInfo);
        final String annotId = annot.getId();
        annotService.treatAnnotationById(annotId, userInfo);
        Assert.assertEquals(AnnotationStatus.TREATED, annotRepos.findById(annotId).get().getStatus()); // check: annotation is
        // existing

        // send mark as treated request
        final MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.patch(API_PREFIX + annotId)
                .header(TestHelper.AUTH_HEADER, TestHelper.AUTH_BEARER + ACCESS_TOKEN);

        final ResultActions result = this.mockMvc.perform(builder);

        // expected: Http 200
        result.andExpect(MockMvcResultMatchers.status().isOk());

        final MvcResult resultContent = result.andReturn();
        final String responseString = resultContent.getResponse().getContentAsString();

        // ID must have been set
        final JsonResetSuccessResponse jsResponse = SerialisationHelper
                .deserializeJsonResetSuccessResponse(responseString);
        Assert.assertNotNull(jsResponse);
        Assert.assertEquals(annotId, jsResponse.getId());

        // the annotation was "treated"
        Assert.assertEquals(1, annotRepos.count());
        TestHelper.assertHasStatus(annotRepos, annotId, AnnotationStatus.NORMAL, user.getId(), null);
    }

    /**
     * failure resetting a non-existing annotation, expected HTTP 400 and failure response
     */
    @SuppressFBWarnings(value = SpotBugsAnnotations.FieldNotInitialized, justification = SpotBugsAnnotations.FieldNotInitializedReason)
    @Test
    public void testResetAnnotationFailureNotFound() throws Exception {
        tokenRepos.save(new Token(user, DEFAULT_AUTHORITY, ACCESS_TOKEN, LocalDateTime.now().plusMinutes(5),
                REFRESH_TOKEN, LocalDateTime.now().plusMinutes(5)));

        // send reset request for non-existing ID
        final MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.patch(API_PREFIX + "theid")
                .header(TestHelper.AUTH_HEADER, TestHelper.AUTH_BEARER + ACCESS_TOKEN);

        final ResultActions result = this.mockMvc.perform(builder);

        // expected: Http 400
        result.andExpect(MockMvcResultMatchers.status().isBadRequest());

        final MvcResult resultContent = result.andReturn();
        final String responseString = resultContent.getResponse().getContentAsString();

        // failure response is received
        final JsonFailureResponse jsResponse = SerialisationHelper.deserializeJsonFailureResponse(responseString);
        Assert.assertNotNull(jsResponse);
        Assert.assertFalse(jsResponse.getReason().isEmpty());
    }

    /**
     * failure resetting an annotation in ISC, expected HTTP 400 and failure response
     */
    @SuppressFBWarnings(value = SpotBugsAnnotations.FieldNotInitialized, justification = SpotBugsAnnotations.FieldNotInitializedReason)
    @Test
    public void testResetAnnotationFailureNotInLeos() throws Exception {

        final UserInformation userInfo = new UserInformation(user, Authorities.ISC);

        tokenRepos.save(new Token(user, Authorities.ISC, ACCESS_TOKEN, LocalDateTime.now().plusMinutes(5),
                REFRESH_TOKEN, LocalDateTime.now().plusMinutes(5)));

        final String hypothesisUserAccount = "acct:user@domain.eu";

        // preparation: save an annotation that can be deleted later on
        final JsonAnnotation jsAnnot = TestData.getTestAnnotationObject(hypothesisUserAccount);
        final Annotation annot = annotService.createAnnotation(jsAnnot, userInfo);
        final String annotId = annot.getId();
        Assert.assertEquals(AnnotationStatus.NORMAL, annotRepos.findById(annotId).get().getStatus()); // check: annotation is existing

        // send mark as treated request
        final MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.patch(API_PREFIX + annotId)
                .header(TestHelper.AUTH_HEADER, TestHelper.AUTH_BEARER + ACCESS_TOKEN);

        final ResultActions result = this.mockMvc.perform(builder);

        // expected: Http 400
        result.andExpect(MockMvcResultMatchers.status().isBadRequest());

        final MvcResult resultContent = result.andReturn();
        final String responseString = resultContent.getResponse().getContentAsString();

        // failure response is received
        final JsonFailureResponse jsResponse = SerialisationHelper.deserializeJsonFailureResponse(responseString);
        Assert.assertNotNull(jsResponse);
        Assert.assertFalse(jsResponse.getReason().isEmpty());
    }
}
