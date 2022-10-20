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
package eu.europa.ec.leos.annotate.services.impl;

import eu.europa.ec.leos.annotate.model.entity.Group;
import eu.europa.ec.leos.annotate.repository.GroupRepository;
import eu.europa.ec.leos.annotate.services.GroupService;
import eu.europa.ec.leos.annotate.services.exceptions.DefaultGroupNotFoundException;
import eu.europa.ec.leos.annotate.services.exceptions.GroupAlreadyExistingException;
import eu.europa.ec.leos.annotate.services.impl.util.InternalGroupName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Service responsible for managing user groups 
 */
@Service
public class GroupServiceImpl implements GroupService {

    private static final Logger LOG = LoggerFactory.getLogger(GroupServiceImpl.class);

    /**
     * default group name injected from configuration
     */
    @Value("${defaultgroup.name}")
    private String defaultGroupName;

    // -------------------------------------
    // Required services and repositories
    // -------------------------------------


    private GroupRepository groupRepos;

    // -------------------------------------
    // Constructors & Setters
    // -------------------------------------

    public GroupServiceImpl() {
        // default
    }

    @Autowired
    public GroupServiceImpl(final GroupRepository groupRepos) {
        this.groupRepos = groupRepos;
    }

    // -------------------------------------
    // Service functionality
    // -------------------------------------

    @Override
    public String getDefaultGroupName() {
        return defaultGroupName;
    }

    @Override
    public Group createGroup(final String name, final boolean isPublic) throws GroupAlreadyExistingException {

        Assert.isTrue(StringUtils.hasLength(name), "Cannot create group without a given name!");

        LOG.info("Save group with name '{}' in the database", name);

        final Group newGroup = new Group(InternalGroupName.getInternalGroupName(name), name, isPublic);
        try {
            groupRepos.save(newGroup);
            LOG.debug("Group '{}' created with id {}", name, newGroup.getId());
        } catch (DataIntegrityViolationException dive) {
            LOG.error("The group '{}' already exists", name);
            throw new GroupAlreadyExistingException(dive);
        } catch (Exception ex) {
            LOG.error("Exception while creating group", ex);
            throw new GroupAlreadyExistingException(ex);
        }
        return newGroup;
    }

    @Override
    public Group findGroupByName(final String groupName) {

        Group foundGroup = groupRepos.findByName(groupName);
        LOG.debug("Found group based on group name: {}", foundGroup != null);
        if(foundGroup == null) {
            // try again with URL-conform internal name
            foundGroup = groupRepos.findByName(InternalGroupName.getInternalGroupName(groupName));
            LOG.debug("Found group based on internal group name: {}", foundGroup != null);
        }
        return foundGroup;
    }

    @Override
    public Group findDefaultGroup() {
        return findGroupByName(defaultGroupName);
    }

    @Override
    public void throwIfNotExistsDefaultGroup() throws DefaultGroupNotFoundException {

        if (findDefaultGroup() == null) {
            LOG.error("Default group seems not to be configured in database; throw DefaultGroupNotFoundException");
            throw new DefaultGroupNotFoundException();
        }
    }

    @Override
    public String getGroupName(final long groupId) {
        
        return groupRepos.findById(groupId).get().getName();
    }
}
