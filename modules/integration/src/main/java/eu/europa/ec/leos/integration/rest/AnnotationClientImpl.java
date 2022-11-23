package eu.europa.ec.leos.integration.rest;

import java.net.URI;
import java.util.List;

import eu.europa.ec.leos.security.LeosPermission;
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
		HttpHeaders headers = this.getDefaultHttpHeaders(jwtToken, proposalRef);
		HttpEntity<?> request = new HttpEntity<>(headers);
		return restTemplate.exchange(uri, HttpMethod.GET, request, String.class).getBody();
	}

	@Override
	public SendTemporaryAnnotationsResponse sendTemporaryAnnotations(final byte[] legFile, final URI uri, final String jwtToken, String proposalRef) {
		final HttpHeaders headers = this.getDefaultHttpHeaders(jwtToken, proposalRef);
		headers.set("Content-Type", "application/octet-stream");
		final HttpEntity<byte[]> request = new HttpEntity<>(legFile, headers);
		return restTemplate.exchange(uri, HttpMethod.POST, request, SendTemporaryAnnotationsResponse.class).getBody();
	}

	@Override
	public AnnotateStatusResponse sendUserPermissions(List<LeosPermission> permissions, URI uri, String jwtToken) {
		final HttpHeaders headers = this.getDefaultHttpHeaders(jwtToken, null);
		headers.set("Content-Type", "application/json");
		final HttpEntity<AnnotatePermissionsJson> request = new HttpEntity<>(new AnnotatePermissionsJson(permissions), headers);
		return restTemplate.exchange(uri, HttpMethod.POST, request, AnnotateStatusResponse.class).getBody();
	}

	private HttpHeaders getDefaultHttpHeaders(final String jwtToken, final String proposalRef) {
		HttpHeaders headers = new HttpHeaders();
		TokenJson tokenJson = authenticationProvider.getToken(jwtToken, proposalRef);
		// FIXME In ticket LEOS-2862 Annotations: improve authentication provider
		headers.set("Authorization", "Bearer " + tokenJson.getAccessToken());
		headers.set("Accept", "application/json");
		return headers;
	}

}