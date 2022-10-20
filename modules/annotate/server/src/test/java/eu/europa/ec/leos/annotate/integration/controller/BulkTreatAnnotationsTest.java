package eu.europa.ec.leos.annotate.integration.controller;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
import eu.europa.ec.leos.annotate.model.UserInformation;
import eu.europa.ec.leos.annotate.model.entity.Annotation;
import eu.europa.ec.leos.annotate.model.entity.Group;
import eu.europa.ec.leos.annotate.model.entity.Token;
import eu.europa.ec.leos.annotate.model.entity.User;
import eu.europa.ec.leos.annotate.model.entity.UserGroup;
import eu.europa.ec.leos.annotate.model.web.annotation.JsonAnnotation;
import eu.europa.ec.leos.annotate.model.web.annotation.JsonBulkTreatSuccessResponse;
import eu.europa.ec.leos.annotate.model.web.annotation.JsonIdList;
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
public class BulkTreatAnnotationsTest {

    private static final String ACCESS_TOKEN = "demoaccesstoken";
    private static final String REFRESH_TOKEN = "refr";
    private static final String API_PREFIX = "/api/annotations/treat/";

    // -------------------------------------
    // Required services and repositories
    // -------------------------------------

    @Autowired
    private AnnotationService annotService;

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
    }

    // -------------------------------------
    // Tests
    // -------------------------------------

    /**
     * successfully mark existing annotations as treated, expected HTTP 200 and success response
     */
    @SuppressFBWarnings(value = SpotBugsAnnotations.FieldNotInitialized, justification = SpotBugsAnnotations.FieldNotInitializedReason)
    @Test
    public void testBulkTreatAnnotationsOk() throws Exception {
        final UserInformation userInfo = new UserInformation(user, Authorities.EdiT);

        tokenRepos.save(new Token(user, Authorities.EdiT, ACCESS_TOKEN, LocalDateTime.now().plusMinutes(5),
                REFRESH_TOKEN, LocalDateTime.now().plusMinutes(5)));

        final String hypothesisUserAccount = "acct:user@domain.eu";

        // preparation: save annotations that can be marked as treated later on
        final JsonAnnotation jsAnnot = TestData.getTestAnnotationObject(hypothesisUserAccount);
        final Annotation annot = annotService.createAnnotation(jsAnnot, userInfo);
        final JsonAnnotation jsAnnot2 = TestData.getTestAnnotationObject(hypothesisUserAccount);
        final Annotation annot2 = annotService.createAnnotation(jsAnnot2, userInfo);

        final JsonIdList idList = new JsonIdList();
        idList.setIds(Arrays.asList(annot.getId(), annot2.getId()));
        final String serializedList = SerialisationHelper.serialize(idList);

        // send treat request
        final MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.patch(API_PREFIX)
                .header(TestHelper.AUTH_HEADER, TestHelper.AUTH_BEARER + ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(serializedList);

        final ResultActions result = this.mockMvc.perform(builder);

        // expected: Http 200
        result.andExpect(MockMvcResultMatchers.status().isOk());

        final MvcResult resultContent = result.andReturn();
        final String responseString = resultContent.getResponse().getContentAsString();

        // ID must have been set
        final JsonBulkTreatSuccessResponse jsResponse = SerialisationHelper.deserializeJsonBulkTreatSuccessResponse(responseString);
        Assert.assertNotNull(jsResponse);

        // the annotation was "treated"
        Assert.assertEquals(2, annotRepos.count());
        TestHelper.assertHasStatus(annotRepos, annot.getId(), AnnotationStatus.TREATED, user.getId(), null);
        TestHelper.assertHasStatus(annotRepos, annot2.getId(), AnnotationStatus.TREATED, user.getId(), null);
    }

    /**
     * failure to mark existing annotations as treated as they are not found, expected HTTP 400 and success response
     */
    @SuppressFBWarnings(value = SpotBugsAnnotations.FieldNotInitialized, justification = SpotBugsAnnotations.FieldNotInitializedReason)
    @Test
    public void testBulkTreatAnnotationsFailureNotFound() throws Exception {

        tokenRepos.save(new Token(user, Authorities.EdiT, ACCESS_TOKEN, LocalDateTime.now().plusMinutes(5),
                REFRESH_TOKEN, LocalDateTime.now().plusMinutes(5)));

        // preparation: save annotations that can be marked as treated later on
        final JsonIdList idList = new JsonIdList();
        idList.setIds(Arrays.asList("theId"));
        final String serializedList = SerialisationHelper.serialize(idList);

        // send treat request
        final MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.patch(API_PREFIX)
                .header(TestHelper.AUTH_HEADER, TestHelper.AUTH_BEARER + ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(serializedList);

        final ResultActions result = this.mockMvc.perform(builder);

        // expected: Http 200
        result.andExpect(MockMvcResultMatchers.status().isOk());

        final MvcResult resultContent = result.andReturn();
        final String responseString = resultContent.getResponse().getContentAsString();

        // ID must have been set
        final JsonBulkTreatSuccessResponse jsResponse = SerialisationHelper.deserializeJsonBulkTreatSuccessResponse(responseString);
        Assert.assertNotNull(jsResponse);

        // the annotation was "treated"
        Assert.assertEquals(0, annotRepos.count());
    }
}
