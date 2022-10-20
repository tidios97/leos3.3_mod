/*
 * Copyright 2019-2022 European Commission
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
package eu.europa.ec.leos.annotate.integration.model.web.user;

import org.junit.Assert;
import org.junit.Test;

import eu.europa.ec.leos.annotate.model.web.user.JsonGroup;
import eu.europa.ec.leos.annotate.model.web.user.JsonUserProfile;

/**
 * simple tests for helper functions of {@link JsonUserProfile} class
 */
public class JsonUserProfileTest {

    /**
     * test that adding a group is working without exception, 
     * even when intermediate layers are reset
     */
    @Test
    public void testAddGroup() {

        final JsonUserProfile prof = new JsonUserProfile(false);

        // act
        prof.setGroups(null);
        prof.addGroup(new JsonGroup("name", "newId", true));

        // verify
        Assert.assertNotNull(prof.getGroups());
        Assert.assertEquals(1, prof.getGroups().size());
    }

    /**
     * test that setting the display name is working without exception, 
     * even when intermediate layers are reset
     */
    @Test
    public void testSetDisplayname() {

        final String newName = "dispname";
        final JsonUserProfile prof = new JsonUserProfile(false);

        // act
        prof.setUser_info(null);
        prof.setDisplayName(newName);

        // verify
        Assert.assertNotNull(prof.getUser_info());
        Assert.assertEquals(newName, prof.getUser_info().getDisplay_name());
    }

    /**
     * test that setting the entity name is working without exception, 
     * even when intermediate layers are reset
     */
    @Test
    public void testSetEntityname() {

        final String newName = "entityname";
        final JsonUserProfile prof = new JsonUserProfile(false);

        // act
        prof.setUser_info(null);
        prof.setEntityName(newName);

        // verify
        Assert.assertNotNull(prof.getUser_info());
        Assert.assertEquals(newName, prof.getUser_info().getEntity_name());
    }
    
    /**
     * check that propagation of the "forward annotations" feature setting is correct
     */
    @Test
    public void testSetForwardingAnnotationFeature() {
        
        final JsonUserProfile prof = new JsonUserProfile(true);
        
        // verify
        Assert.assertTrue(prof.getFeatures().isForward_annotations());
    }
}
