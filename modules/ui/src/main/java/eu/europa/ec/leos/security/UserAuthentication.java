package eu.europa.ec.leos.security;

import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.services.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class UserAuthentication {

    private Authentication authentication;

    @Autowired
    public UserAuthentication(SecurityContext securityContext, UserService userService) {
        User user = securityContext.getUser();
        this.authentication = userService.createUserWithAuthorities(user.getLogin());
    }

    public Authentication getAuthentication() {
        return this.authentication;
    }

}
