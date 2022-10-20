package eu.europa.ec.leos.annotate.services.impl.util;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import eu.europa.ec.leos.annotate.model.entity.AuthClient;

/**
 * internal class to ease access from a given configured client to its algorithm and token verifier
 */
public final class RegisteredClient {

    private final AuthClient client;
    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    // -------------------------------------
    // Constructor
    // -------------------------------------
    public RegisteredClient(final AuthClient client, final Algorithm algorithm, final JWTVerifier verifier) {
        this.client = client;
        this.algorithm = algorithm;
        this.verifier = verifier;
    }

    // -------------------------------------
    // Getter
    // -------------------------------------
    public AuthClient getClient() {
        return client;
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public JWTVerifier getVerifier() {
        return verifier;
    }

}