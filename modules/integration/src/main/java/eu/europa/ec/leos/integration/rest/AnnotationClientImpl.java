package eu.europa.ec.leos.integration.rest;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import eu.europa.ec.leos.integration.AnnotationProvider;

@Component
public class AnnotationClientImpl implements AnnotationProvider {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private AnnotationAuthProvider authenticationProvider;

	@Override
	public String searchAnnotations(URI uri, String jwtToken, String proposalRef) {
		HttpHeaders headers = new HttpHeaders();
		TokenJson tokenJson = authenticationProvider.getToken(jwtToken, proposalRef);
		// FIXME In ticket LEOS-2862 Annotations: improve authentication provider
		headers.set("Authorization", "Bearer " + tokenJson.getAccessToken());
		headers.set("Accept", "application/json");
		HttpEntity<?> request = new HttpEntity<>(headers);
		return restTemplate.exchange(uri, HttpMethod.GET, request, String.class).getBody();
	}

}