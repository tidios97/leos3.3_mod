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
package eu.europa.ec.digit.userdata.controllers;

import eu.europa.ec.digit.userdata.entities.Entity;
import eu.europa.ec.digit.userdata.entities.SpecialEntity;
import eu.europa.ec.digit.userdata.entities.SpecialUser;
import eu.europa.ec.digit.userdata.entities.User;
import eu.europa.ec.digit.userdata.repositories.EntityRepository;
import eu.europa.ec.digit.userdata.repositories.SpecialEntityRepository;
import eu.europa.ec.digit.userdata.repositories.SpecialUserRepository;
import eu.europa.ec.digit.userdata.repositories.UserRepository;
import eu.europa.ec.digit.userdata.request.SpecialEntityRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Random;
import java.util.stream.Collectors;

@RestController
public class UserController {

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);
    private static int MAX_RECORDS = 100;

    @Autowired
    UserRepository userRepository;
    @Autowired
    EntityRepository entityRepository;
    @Autowired
    SpecialEntityRepository specialEntityRepository;
    @Autowired
    SpecialUserRepository specialUserRepository;

    @RequestMapping(method = RequestMethod.GET, path = "/users")
    @Transactional(readOnly = true)
    public Collection<User> searchUsers(
            @RequestParam(value = "searchKey") String searchKey,
            @RequestParam(value = "searchContext", required = false) String searchContext,
            @RequestParam(value = "searchReference", required = false) String searchReference) {
        return userRepository
                .findUsersByKey(searchKey.trim().replace(" ", "%").concat("%"))
                .limit(MAX_RECORDS).collect(Collectors.toList());
    }

    @RequestMapping(method = RequestMethod.GET, path = "/users/{userId}")
    @Transactional(readOnly = true)
    public User getUser(@PathVariable(value = "userId") String userId) {
        return userRepository.findByLogin(userId);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/entities")
    @Transactional(readOnly = true)
    public Collection<String> getAllOrganizations() {
        return entityRepository.findAllOrganizations()
                .collect(Collectors.toList());
    }

    @RequestMapping(method = RequestMethod.GET, path = "/entities/{org}/users")
    @Transactional(readOnly = true)
    public Collection<User> searchUsersByOrganizationAndKey(
            @PathVariable(value = "org", required = false) String organization,
            @RequestParam(value = "searchKey", required = true) String searchKey) {
        return userRepository
                .findUsersByKeyAndOrganization(
                        searchKey.trim().replace(" ", "%").concat("%"),
                        organization)
                .limit(MAX_RECORDS).collect(Collectors.toList());
    }

    @RequestMapping(method = RequestMethod.POST, path = "/users/connectedEntity")
    @Transactional
    public Boolean addSpecialEntityForUser(@RequestBody SpecialEntityRequest request) {
        LOG.debug("Adding special entity to LEOS_SPECIAL_ENTITY table in ud-repo ---Started");
        SpecialUser specialUser = specialUserRepository.findByLogin(request.getUserId());
        if (specialUser == null) {
            LOG.debug("Special user does not exists, adding to the special user table");
            User user = getUser(request.getUserId());
            specialUser = new SpecialUser(user.getLogin(), user.getPerId(), user.getLastName(), user.getFirstName(),
                    user.getEmail());
            specialUser = specialUserRepository.save(specialUser);
            LOG.debug("Added special user to LEOS_SPECIAL_USER table in ud-repo");
        }
        if (specialUser != null) {
            LOG.debug("Searching special entity in LEOS_SPECIAL_ENTITY table");
            SpecialEntity specialEntity = specialEntityRepository.findByName(request.getEntity());
            if (specialEntity == null) {
                LOG.debug("Special entity in LEOS_SPECIAL_ENTITY table not found, adding to the table");
                Random random = new Random();
                // generate random number from 0 to 10000
                Integer number = random.nextInt(10000);
                specialEntity = new SpecialEntity(String.valueOf(number), request.getEntity(), null,
                        request.getEntity());
                SpecialEntity insertedEntity = specialEntityRepository.save(specialEntity);
                LOG.debug("Adding special entity to LEOS_SPECIAL_ENTITY table in ud-repo ---Finished");
                return insertedEntity != null;
            } else {
                return true;
            }
        }
        return false;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/entities/{userId}")
    @Transactional(readOnly = true)
    public Collection<Entity> getAllFullPathEntitiesForUser(@PathVariable(value = "userId") String userId) {
        User user = getUser(userId);
        Collection<Entity> entities = entityRepository
                .findAllFullPathEntities(user.getEntities().stream()
                        .map(e -> e.getId()).collect(Collectors.toList()))
                .collect(Collectors.toList());
        return entities;
    }
}