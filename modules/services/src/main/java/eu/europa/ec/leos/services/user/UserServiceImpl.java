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

import eu.europa.ec.leos.integration.UsersProvider;
import eu.europa.ec.leos.integration.rest.UserJSON;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.security.AuthenticatedUser;
import eu.europa.ec.leos.security.SecurityUser;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

    private UsersProvider usersClient;

    @Autowired
    public UserServiceImpl(UsersProvider usersClient) {
        this.usersClient = usersClient;
    }

    @Override
    public User getUser(String login) {
        User result = usersClient.getUserByLogin(login);
        return result;
    }

    @Override
    public List<UserJSON> searchUsersByKey(String key) {
        return usersClient.searchUsers(key);
    }

    @Override
    public List<UserJSON> searchUsersInContextByKeyAndReference(String key, String searchContext, String searchReference) {
        List<UserJSON> result = usersClient.searchUsersInContext(key, searchContext, searchReference);
        return result;
    }

    @Override
    public void switchUser(String login) {
        Validate.notNull(login, "login is required!");
        LOG.debug("User switch request received for user -" + login);
        PreAuthenticatedAuthenticationToken preAuthRequest =
                new PreAuthenticatedAuthenticationToken(new AuthenticatedUser(usersClient.getUserByLogin(login)), "");
        preAuthRequest.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(preAuthRequest);
        LOG.debug("User switch request is successful. New user is -" + login);
    }

    @Override
    public void switchUserWithAuthorities(String login, Collection<? extends GrantedAuthority> authorities) {
        Validate.notNull(login, "login is required!");
        LOG.debug("User switch request received for user -" + login);
        PreAuthenticatedAuthenticationToken preAuthRequest =
                new PreAuthenticatedAuthenticationToken(new AuthenticatedUser(usersClient.getUserByLogin(login)), "", authorities);
        SecurityContextHolder.getContext().setAuthentication(preAuthRequest);
        LOG.debug("User switch request is successful. New user is -" + login);
    }

    @Override
    public Authentication createUserWithAuthorities(String login) {
        Validate.notNull(login, "login is required!");
        LOG.debug("Create user received for user -" + login);
        User user = usersClient.getUserByLogin(login);
        List<GrantedAuthority> allRoles = new ArrayList<GrantedAuthority>();
        if(user instanceof SecurityUser) {
            List<String> leosRoles = ((SecurityUser) user).getRoles();
            leosRoles.forEach(auth -> {
                allRoles.add(new SimpleGrantedAuthority(auth));
            });
        }
        PreAuthenticatedAuthenticationToken preAuthRequest = new PreAuthenticatedAuthenticationToken(new AuthenticatedUser(user), "", allRoles);
        preAuthRequest.setAuthenticated(true);
        LOG.debug("Create user is successful. User is -" + login);
        return preAuthRequest;
    }

}
