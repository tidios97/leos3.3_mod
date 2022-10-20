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
package eu.europa.ec.leos.annotate.integration.services;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import eu.europa.ec.leos.annotate.helper.SpotBugsAnnotations;
import eu.europa.ec.leos.annotate.helper.TestDbHelper;
import eu.europa.ec.leos.annotate.model.entity.Group;
import eu.europa.ec.leos.annotate.repository.GroupRepository;
import eu.europa.ec.leos.annotate.services.GroupService;
import eu.europa.ec.leos.annotate.services.UUIDGeneratorService;
import eu.europa.ec.leos.annotate.services.exceptions.GroupAlreadyExistingException;
import eu.europa.ec.leos.annotate.services.impl.UUIDGeneratorServiceImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.config.name=anot")
@ActiveProfiles("test")
@SuppressWarnings("PMD.TooManyMethods")
public class GroupServiceTest {

    // -------------------------------------
    // Required services and repositories
    // -------------------------------------
    @Autowired
    private GroupService groupService;

    @Autowired
    private GroupRepository groupRepos;

    // -------------------------------------
    // Cleanup of database content
    // -------------------------------------
    @Before
    public void cleanDatabaseBeforeTests() {

        TestDbHelper.cleanupRepositories(this);
    }

    @After
    public void cleanDatabaseAfterTests() {

        TestDbHelper.cleanupRepositories(this);
    }

    // -------------------------------------
    // Tests
    // -------------------------------------

    /**
     * Test that group creation fails if no name is given
     */
    @Test(expected = IllegalArgumentException.class)
    @SuppressFBWarnings(value = SpotBugsAnnotations.ExceptionIgnored, justification = SpotBugsAnnotations.ExceptionIgnored)
    public void testCreateGroupFails() throws GroupAlreadyExistingException {

        groupService.createGroup("", true);
        Assert.fail("Group creation should throw exception when intended group name is missing; did not!");
    }

    /**
     * Test creation of random groups
     */
    @Test
    @SuppressWarnings("PMD.EmptyCatchBlock")
    @SuppressFBWarnings(value = SpotBugsAnnotations.ExceptionIgnored, justification = SpotBugsAnnotations.ExceptionIgnored)
    public void testCreateManyGroups() throws GroupAlreadyExistingException {

        final int numberOfGroups = 100;

        final List<String> groupNames = new ArrayList<String>();
        final UUIDGeneratorService uuidService = new UUIDGeneratorServiceImpl();

        // arrange
        for (int i = 0; i < numberOfGroups; i++) {
            groupNames.add(uuidService.generateUrlSafeUUID());
        }

        // verify before: no groups
        Assert.assertEquals(0, groupRepos.count());

        // act: create groups
        for (int i = 0; i < numberOfGroups; i++) {
            groupService.createGroup(groupNames.get(i), false);
        }

        // verify: <numberOfGroups> groups
        Assert.assertEquals(numberOfGroups, groupRepos.count());

        // trying to create again throws exceptions, but doesn't create supplementary items
        for (int i = 0; i < numberOfGroups; i++) {
            try {
                groupService.createGroup(groupNames.get(i), false);
                Assert.fail("Trying to create existing group again should throw exception - did not!");
            } catch (Exception e) {
                // OK
            }
        }
        Assert.assertEquals(numberOfGroups, groupRepos.count()); // number of groups did not change

        // verify details
        for (int i = 0; i < numberOfGroups; i++) {

            final Group foundGroup = groupService.findGroupByName(groupNames.get(i));
            Assert.assertNotNull(foundGroup);
            Assert.assertEquals(groupNames.get(i), foundGroup.getName());
            Assert.assertEquals(groupNames.get(i), foundGroup.getDisplayName());
            Assert.assertEquals(groupNames.get(i), foundGroup.getDescription());
        }
    }
}
