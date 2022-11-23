/*
 * Copyright 2017 European Commission
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
package eu.europa.ec.leos.services.user;

import eu.europa.ec.leos.integration.rest.UserJSON;
import eu.europa.ec.leos.model.user.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

public interface UserService {
    /**
     * Get the user with the given login.
     *
     * @param login the user login.
     * @return an user object or <code>null</code> if the user is not found.
     */
    public User getUser(String login);
    /**
     * Get list of users with a given key.
     *
     * @param key
     * @return a list of users and an empty list if no user is found.
     */
    public List<UserJSON> searchUsersByKey(String key);

    /**
     * Get list of users with a given key, context and reference.
     *
     * @param key
     * @return a list of users and an empty list if no user is found.
     */
    public List<UserJSON> searchUsersInContextByKeyAndReference(String key, String searchContext, String searchReference);

    /**
     * Switch logged-in user with the given login.
     * This implementation is loosely based on the Spring's SwitchUserFilter
     * but should be used with care.
     * @param login the user login
     */
    public void switchUser(String login);

    /**
     * Switch logged-in user with the given login and authorities.
     * This implementation is loosely based on the Spring's SwitchUserFilter
     * but should be used with care.
     * @param login the user login
     */
    public void switchUserWithAuthorities(String login, Collection<? extends GrantedAuthority> authorities);

    /**
     * Create user with the given login with authorities
     * to be used in SecurityContext to check permissions
     * @param login the user login
     */
    public Authentication createUserWithAuthorities(String login);

}

