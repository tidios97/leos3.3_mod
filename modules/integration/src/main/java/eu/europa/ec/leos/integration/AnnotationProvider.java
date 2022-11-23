package eu.europa.ec.leos.integration;

import eu.europa.ec.leos.integration.rest.AnnotateStatusResponse;
import eu.europa.ec.leos.integration.rest.SendTemporaryAnnotationsResponse;
import eu.europa.ec.leos.security.LeosPermission;

import java.net.URI;
import java.util.List;


public interface AnnotationProvider {
	String searchAnnotations(URI uri, String jwtToken, String proposalRef);

	/**
	 * Send a leg file that will be temporarily stored on the annotation server
	 * <br><br>
	 * Will be deleted immediately after retrieval of the data
	 * @param legFile Log file content as bytes
	 * @param uri {@link URI} of the target path
	 * @param jwtToken JWT Token to use
	 * @param proposalRef Reference to load token data
	 * @return Object of {@link SendTemporaryAnnotationsResponse} providing the if of the stored data. This id will be used to
	 * 		load and identify the data.
	 * */
	SendTemporaryAnnotationsResponse sendTemporaryAnnotations(byte[] legFile, URI uri, String jwtToken, String proposalRef);

	AnnotateStatusResponse sendUserPermissions(List<LeosPermission> permissions, URI uri, String jwtToken);
}