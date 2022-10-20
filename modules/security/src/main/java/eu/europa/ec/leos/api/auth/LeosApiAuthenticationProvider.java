package eu.europa.ec.leos.api.auth;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class LeosApiAuthenticationProvider implements AuthenticationProvider {


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if(!authentication.isAuthenticated()){
            throw new LeosApiAuthenticationException("Unauthorized");
        }
        return authentication;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (JwtAuthenticationToken.class.isAssignableFrom(authentication) || PreAuthenticatedAuthenticationToken.class.isAssignableFrom(authentication));
    }
}