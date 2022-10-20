/*
 * Copyright 2018 European Commission
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
package eu.europa.ec.leos.annotate.integration.controller;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import eu.europa.ec.leos.annotate.Authorities;
import eu.europa.ec.leos.annotate.helper.*;
import eu.europa.ec.leos.annotate.model.AnnotationStatus;
import eu.europa.ec.leos.annotate.model.AuthenticatedUserStore;
import eu.europa.ec.leos.annotate.model.UserInformation;
import eu.europa.ec.leos.annotate.model.entity.*;
import eu.europa.ec.leos.annotate.model.web.JsonFailureResponse;
import eu.europa.ec.leos.annotate.model.web.annotation.JsonAnnotation;
import eu.europa.ec.leos.annotate.model.web.annotation.JsonTreatSuccessResponse;
import eu.europa.ec.leos.annotate.repository.*;
import eu.europa.ec.leos.annotate.services.AnnotationService;
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

import java.time.LocalDateTime;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.config.name=anot")
@WebAppConfiguration
@ActiveProfiles("test")
public class TreatAnnotationTest {

    private static final String ACCESS_TOKEN = "demoaccesstoken";
    private static final String REFRESH_TOKEN = "refr";
    private static final String DEFAULT_AUTHORITY = "auth";
    private static final String API_PREFIX = "/api/annotations/treat/";
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
     * successfully mark an existing annotation as treated, expected HTTP 200 and ID of the treated annotation
     */
    @SuppressFBWarnings(value = SpotBugsAnnotations.FieldNotInitialized, justification = SpotBugsAnnotations.FieldNotInitializedReason)
    @Test
    public void testTreatAnnotationOk() throws Exception {
        
        final UserInformation userInfo = new UserInformation(user, Authorities.EdiT);

        tokenRepos.save(new Token(user, Authorities.EdiT, ACCESS_TOKEN, LocalDateTime.now().plusMinutes(5),
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

        // expected: Http 200
        result.andExpect(MockMvcResultMatchers.status().isOk());

        final MvcResult resultContent = result.andReturn();
        final String responseString = resultContent.getResponse().getContentAsString();

        // ID must have been set
        final JsonTreatSuccessResponse jsResponse = SerialisationHelper.deserializeJsonTreatSuccessResponse(responseString);
        Assert.assertNotNull(jsResponse);
        Assert.assertEquals(annotId, jsResponse.getId());

        // the annotation was "treated"
        Assert.assertEquals(1, annotRepos.count());
        TestHelper.assertHasStatus(annotRepos, annotId, AnnotationStatus.TREATED, user.getId(), null);
    }

    /**
     * failure treating a non-existing annotation, expected HTTP 400 and failure response
     */
    @SuppressFBWarnings(value = SpotBugsAnnotations.FieldNotInitialized, justification = SpotBugsAnnotations.FieldNotInitializedReason)
    @Test
    public void testTreatAnnotationFailureNotFound() throws Exception {
        tokenRepos.save(new Token(user, DEFAULT_AUTHORITY, ACCESS_TOKEN, LocalDateTime.now().plusMinutes(5),
                REFRESH_TOKEN, LocalDateTime.now().plusMinutes(5)));

        // send mark as treat request for non-existing ID
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
     * failure treating an annotation in ISC, expected HTTP 400 and failure response
     */
    @SuppressFBWarnings(value = SpotBugsAnnotations.FieldNotInitialized, justification = SpotBugsAnnotations.FieldNotInitializedReason)
    @Test
    public void testTreatAnnotationFailureNotInLeos() throws Exception {

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
