package eu.europa.ec.leos.api.auth;

import org.springframework.security.core.AuthenticationException;

public class LeosApiAuthenticationException extends AuthenticationException {
    public LeosApiAuthenticationException(String msg, Throwable t) {
        super(msg, t);
    }

    public LeosApiAuthenticationException(String msg) {
        super(msg);
    }
}