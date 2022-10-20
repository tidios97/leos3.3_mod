package eu.europa.ec.leos.integration.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * Provides authentication token for annotation
 *
 */
@Component
class AnnotationAuthProvider {

    @Autowired
    private RestTemplate restTemplate;

    @Value("#{integrationProperties['annotate.api.internal.host']}")
    private String annotationHost;

    /**
     * @param jwtToken
     * @param proposalRef
     * @return Access token
     */
    public TokenJson getToken(String jwtToken, String proposalRef) {
        String tokenURI = annotationHost + "token";
        MultiValueMap<String, String> requestPayload = new LinkedMultiValueMap<String, String>();
        requestPayload.add("grant_type", "jwt-bearer");
        requestPayload.add("assertion", jwtToken);
        requestPayload.add("context", proposalRef);
        return restTemplate.postForObject(tokenURI, requestPayload, TokenJson.class);
    }

}