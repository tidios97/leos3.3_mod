package eu.europa.ec.leos.integration;

import java.net.URI;


public interface AnnotationProvider {

	String searchAnnotations(URI uri, String jwtToken, String proposalRef);
}