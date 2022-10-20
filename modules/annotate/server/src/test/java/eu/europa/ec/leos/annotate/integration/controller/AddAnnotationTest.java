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
package eu.europa.ec.leos.annotate.integration.controller;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import eu.europa.ec.leos.annotate.helper.*;
import eu.europa.ec.leos.annotate.model.SimpleMetadata;
import eu.europa.ec.leos.annotate.model.entity.*;
import eu.europa.ec.leos.annotate.model.web.JsonFailureResponse;
import eu.europa.ec.leos.annotate.model.web.annotation.JsonAnnotation;
import eu.europa.ec.leos.annotate.repository.*;
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

import java.time.LocalDateTime;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.config.name=anot")
@WebAppConfiguration
@ActiveProfiles("test")
public class AddAnnotationTest {

    private static final String ACCESS_TOKEN = "demoaccesstoken", REFRESH_TOKEN = "helloRefresh";
    private static final String ACCT = "acct:user@domain.eu";
    
    private User theUser;
    
    // -------------------------------------
    // Required services and repositories
    // -------------------------------------
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

    // -------------------------------------
    // Cleanup of database content
    // -------------------------------------
    @Before
    public void setupTests() {

        TestDbHelper.cleanupRepositories(this);
        TestDbHelper.insertDefaultGroup(groupRepos);

        theUser = new User("demo");
        userRepos.save(theUser);
        tokenRepos.save(new Token(theUser, "auth", ACCESS_TOKEN, LocalDateTime.now().plusMinutes(5), REFRESH_TOKEN, LocalDateTime.now().plusMinutes(5)));

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
     * successfully create an annotation, expected HTTP 200 and returned annotation
     */
    @SuppressFBWarnings(value = SpotBugsAnnotations.FieldNotInitialized, justification = SpotBugsAnnotations.FieldNotInitializedReason)
    @Test
    public void testAddAnnotationOk() throws Exception {

        final JsonAnnotation jsAnnot = TestData.getTestAnnotationObject(ACCT);
        final String serializedAnnotation = SerialisationHelper.serialize(jsAnnot);

        final MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/api/annotations")
                .header(TestHelper.AUTH_HEADER, TestHelper.AUTH_BEARER + ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(serializedAnnotation);

        final ResultActions result = this.mockMvc.perform(builder);

        // expected: Http 200
        result.andExpect(MockMvcResultMatchers.status().isOk());

        final MvcResult resultContent = result.andReturn();
        final String responseString = resultContent.getResponse().getContentAsString();

        // ID must have been set
        final JsonAnnotation jsResponse = SerialisationHelper.deserializeJsonAnnotation(responseString);
        Assert.assertNotNull(jsResponse);
        Assert.assertFalse(jsResponse.getId().isEmpty());

        // the annotation was saved
        Assert.assertEquals(1, annotRepos.count());
    }

    /**
     * successfully create an annotation with a connected entity, 
     * expected HTTP 200 and returned annotation
     */
    @SuppressFBWarnings(value = SpotBugsAnnotations.FieldNotInitialized, justification = SpotBugsAnnotations.FieldNotInitializedReason)
    @Test
    public void testAddAnnotationOkWithConnectedEntity() throws Exception {

        // add the user to a group
        final String ENTITY = "SomeDg";
        final Group entGroup = new Group(ENTITY, true);
        groupRepos.save(entGroup);
        userGroupRepos.save(new UserGroup(theUser.getId(), entGroup.getId()));
        
        final JsonAnnotation jsAnnot = TestData.getTestAnnotationObject(ACCT);
        final String serializedAnnotation = SerialisationHelper.serialize(jsAnnot);

        final MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .post("/api/annotations?connectedEntity=" + ENTITY)
                .header(TestHelper.AUTH_HEADER, TestHelper.AUTH_BEARER + ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(serializedAnnotation);

        final ResultActions result = this.mockMvc.perform(builder);

        // expected: Http 200
        result.andExpect(MockMvcResultMatchers.status().isOk());

        final MvcResult resultContent = result.andReturn();
        final String responseString = resultContent.getResponse().getContentAsString();

        // ID must have been set
        final JsonAnnotation jsResponse = SerialisationHelper.deserializeJsonAnnotation(responseString);
        Assert.assertNotNull(jsResponse);
        Assert.assertFalse(jsResponse.getId().isEmpty());

        // the annotation was saved with the connected entity set properly
        Assert.assertEquals(1, annotRepos.count());
        final Annotation readAnnot = ((List<Annotation>)annotRepos.findAll()).get(0);
        Assert.assertEquals(entGroup.getId(), readAnnot.getConnectedEntityId());
    }

    /**
     * successfully create an annotation with an unknown connected entity, 
     * expected HTTP 200 and returned annotation
     */
    @SuppressFBWarnings(value = SpotBugsAnnotations.FieldNotInitialized, justification = SpotBugsAnnotations.FieldNotInitializedReason)
    @Test
    public void testAddAnnotationOkWithUnknownConnectedEntity() throws Exception {

        // add the user to a group
        final String ENTITY = "SomeDg";
        
        final JsonAnnotation jsAnnot = TestData.getTestAnnotationObject(ACCT);
        final String serializedAnnotation = SerialisationHelper.serialize(jsAnnot);

        final MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .post("/api/annotations?connectedEntity=" + ENTITY)
                .header(TestHelper.AUTH_HEADER, TestHelper.AUTH_BEARER + ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(serializedAnnotation);

        final ResultActions result = this.mockMvc.perform(builder);

        // expected: Http 200
        result.andExpect(MockMvcResultMatchers.status().isOk());

        final MvcResult resultContent = result.andReturn();
        final String responseString = resultContent.getResponse().getContentAsString();

        // ID must have been set
        final JsonAnnotation jsResponse = SerialisationHelper.deserializeJsonAnnotation(responseString);
        Assert.assertNotNull(jsResponse);
        Assert.assertFalse(jsResponse.getId().isEmpty());

        // the annotation was saved with the connected entity set properly
        Assert.assertEquals(1, annotRepos.count());
        final Annotation readAnnot = ((List<Annotation>)annotRepos.findAll()).get(0);
        Assert.assertNull(readAnnot.getConnectedEntityId());
        Assert.assertNull(readAnnot.getConnectedEntity());
    }
    
    /**
     * create an annotation having its response status set to SENT already
     * expected HTTP 400
     */
    @SuppressFBWarnings(value = SpotBugsAnnotations.FieldNotInitialized, justification = SpotBugsAnnotations.FieldNotInitializedReason)
    @Test
    public void testAddAnnotationWithResponseStatusSentRefused() throws Exception {

        final JsonAnnotation jsAnnot = TestData.getTestAnnotationObject(ACCT);
        final SimpleMetadata metadata = new SimpleMetadata();
        metadata.put("responseStatus", "SENT");
        jsAnnot.getDocument().setMetadata(metadata);
        final String serializedAnnotation = SerialisationHelper.serialize(jsAnnot);

        final MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/api/annotations")
                .header(TestHelper.AUTH_HEADER, TestHelper.AUTH_BEARER + ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(serializedAnnotation);

        final ResultActions result = this.mockMvc.perform(builder);

        // expected: Http 200
        result.andExpect(MockMvcResultMatchers.status().isBadRequest());

        final MvcResult resultContent = result.andReturn();
        final String responseString = resultContent.getResponse().getContentAsString();

        // the response must be a failure response
        final JsonFailureResponse jsResponse = SerialisationHelper.deserializeJsonFailureResponse(responseString);
        Assert.assertNotNull(jsResponse);
        Assert.assertFalse(jsResponse.getReason().isEmpty());

        // the annotation was not saved
        Assert.assertEquals(0, annotRepos.count());
    }

    /**
     * annotation not created, expect HTTP 400 and failure response
     */
    @SuppressFBWarnings(value = SpotBugsAnnotations.FieldNotInitialized, justification = SpotBugsAnnotations.FieldNotInitializedReason)
    @Test
    public void testAddAnnotationFail() throws Exception {

        // reset the group repository, by which the required default group is missing
        groupRepos.deleteAll();
        userGroupRepos.deleteAll();

        final JsonAnnotation jsAnnot = TestData.getTestAnnotationObject(ACCT);
        final String serializedAnnotation = SerialisationHelper.serialize(jsAnnot);

        final MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/api/annotations")
                .header(TestHelper.AUTH_HEADER, TestHelper.AUTH_BEARER + ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(serializedAnnotation);

        final ResultActions result = this.mockMvc.perform(builder);

        // expected: Http 400
        result.andExpect(MockMvcResultMatchers.status().isBadRequest());

        final MvcResult resultContent = result.andReturn();
        final String responseString = resultContent.getResponse().getContentAsString();

        // the response must be a failure response
        final JsonFailureResponse jsResponse = SerialisationHelper.deserializeJsonFailureResponse(responseString);
        Assert.assertNotNull(jsResponse);
        Assert.assertFalse(jsResponse.getReason().isEmpty());

        // the annotation was not saved
        Assert.assertEquals(0, annotRepos.count());
    }
    
    /**
     * successfully create a forwarded annotation (without forwarding justification), 
     * expected HTTP 200 and returned annotation
     */
    @SuppressFBWarnings(value = SpotBugsAnnotations.FieldNotInitialized, justification = SpotBugsAnnotations.FieldNotInitializedReason)
    @Test
    public void testAddForwardedAnnotationWithoutJustificationOk() throws Exception {

        final JsonAnnotation jsAnnot = TestData.getTestAnnotationObject(ACCT);
        groupRepos.save(new Group("Test", true));
        jsAnnot.setOriginGroup("Test");
        jsAnnot.setForwarded(true);
        jsAnnot.setForwardedJustification(null); // no further comment
        final String serializedAnnotation = SerialisationHelper.serialize(jsAnnot);

        final MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/api/annotations")
                .header(TestHelper.AUTH_HEADER, TestHelper.AUTH_BEARER + ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(serializedAnnotation);

        final ResultActions result = this.mockMvc.perform(builder);

        // expected: Http 200
        result.andExpect(MockMvcResultMatchers.status().isOk());

        final MvcResult resultContent = result.andReturn();
        final String responseString = resultContent.getResponse().getContentAsString();

        // forward properties must be echoed
        final JsonAnnotation jsResponse = SerialisationHelper.deserializeJsonAnnotation(responseString);
        Assert.assertNotNull(jsResponse);
        Assert.assertFalse(jsResponse.getId().isEmpty());
        Assert.assertTrue(jsResponse.isForwarded());
        Assert.assertNull(jsResponse.getForwardJustification());

        // the annotation was saved
        Assert.assertEquals(1, annotRepos.count());
        
        final Annotation readAnnot = annotRepos.findById(jsResponse.getId()).get();
        Assert.assertEquals(readAnnot.getOriginGroup().getName(), jsAnnot.getOriginGroup());
        Assert.assertTrue(readAnnot.isForwarded());
        Assert.assertNull(readAnnot.getForwardJustification());
    }
    
    /**
     * successfully create a forwarded annotation (containing a forwarding justification), 
     * expected HTTP 200 and returned annotation
     */
    @SuppressFBWarnings(value = SpotBugsAnnotations.FieldNotInitialized, justification = SpotBugsAnnotations.FieldNotInitializedReason)
    @Test
    public void testAddForwardedAnnotationWithJustificationOk() throws Exception {

        final String forwardingJustification = "FYI";

        final JsonAnnotation jsAnnot = TestData.getTestAnnotationObject(ACCT);
        jsAnnot.setForwarded(true);
        jsAnnot.setForwardedJustification(forwardingJustification);
        groupRepos.save(new Group("Test", true));
        jsAnnot.setOriginGroup("Test");
        final String serializedAnnotation = SerialisationHelper.serialize(jsAnnot);

        final MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/api/annotations")
                .header(TestHelper.AUTH_HEADER, TestHelper.AUTH_BEARER + ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(serializedAnnotation);

        final ResultActions result = this.mockMvc.perform(builder);

        // expected: Http 200
        result.andExpect(MockMvcResultMatchers.status().isOk());

        final MvcResult resultContent = result.andReturn();
        final String responseString = resultContent.getResponse().getContentAsString();

        // forward properties must be echoed
        final JsonAnnotation jsResponse = SerialisationHelper.deserializeJsonAnnotation(responseString);
        Assert.assertNotNull(jsResponse);
        Assert.assertFalse(jsResponse.getId().isEmpty());
        Assert.assertTrue(jsResponse.isForwarded());
        Assert.assertEquals(forwardingJustification, jsResponse.getForwardJustification());

        // the annotation was saved
        Assert.assertEquals(1, annotRepos.count());
        
        final Annotation readAnnot = annotRepos.findById(jsResponse.getId()).get();
        Assert.assertEquals(readAnnot.getOriginGroup().getName(), jsAnnot.getOriginGroup());
        Assert.assertTrue(readAnnot.isForwarded());
        Assert.assertEquals(forwardingJustification, readAnnot.getForwardJustification());
    }
}
