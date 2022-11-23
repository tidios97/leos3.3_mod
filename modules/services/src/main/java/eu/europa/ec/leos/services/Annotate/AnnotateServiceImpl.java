package eu.europa.ec.leos.services.Annotate;

import java.net.URI;
import java.util.List;

import eu.europa.ec.leos.integration.rest.AnnotateStatusResponse;
import eu.europa.ec.leos.integration.rest.SendTemporaryAnnotationsResponse;
import eu.europa.ec.leos.security.LeosPermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import eu.europa.ec.leos.integration.AnnotationProvider;
import eu.europa.ec.leos.security.SecurityContext;

@Service
public class AnnotateServiceImpl implements AnnotateService {

    private static final Logger LOG = LoggerFactory.getLogger(AnnotateServiceImpl.class);

    private final SecurityContext securityContext;
    private final AnnotationProvider annotationProvider;

    @Value("${annotate.server.internal.url}")
    private String annotationHost;

    @Autowired
    AnnotateServiceImpl(SecurityContext securityContext, AnnotationProvider annotationProvider) {
        this.securityContext = securityContext;
        this.annotationProvider = annotationProvider;
    }

    @Override
    public String getAnnotations(String docName, String proposalRef) {

        URI uri = UriComponentsBuilder.fromHttpUrl(annotationHost + "/api/search")
                .queryParam("_separate_replies", true)
                .queryParam("group", "__world__")
                .queryParam("limit", -1)
                .queryParam("offset", 0)
                .queryParam("order", "asc")
                .queryParam("sort", "created")
                .queryParam("uri", "uri://LEOS/" + docName).build().encode().toUri();

        try {
            return annotationProvider.searchAnnotations(uri, this.getAnnotateToken(), proposalRef);
        } catch (Exception exception) {
            LOG.error("Error getting annotations: ", exception);
            throw new RuntimeException("Error Occurred While Getting Annotation");
        }
    }

    @Override
    public String createTemporaryAnnotations(final byte[] legFile, final String proposalRef) {
        URI uri = UriComponentsBuilder.fromHttpUrl(annotationHost + "/api/annotations/temporary").build().encode().toUri();
        try {
            final SendTemporaryAnnotationsResponse response = annotationProvider
                    .sendTemporaryAnnotations(legFile, uri, this.getAnnotateToken(), proposalRef);
            return response.getCreatedId();
        } catch (Exception exception) {
            LOG.error("Error creating temporary annotations: ", exception);
            throw new RuntimeException("Error occurred while creating temporary annotations");
        }
    }

    @Override
    public boolean sendUserPermissions(List<LeosPermission> permissions) {
        URI uri = UriComponentsBuilder.fromHttpUrl(annotationHost + "/api/user/permissions").build().encode().toUri();
        try {
            final AnnotateStatusResponse response = annotationProvider.sendUserPermissions(permissions, uri, this.getAnnotateToken());
            return response.getStatus().equalsIgnoreCase("ok");
        } catch (Exception exception) {
            LOG.error("Error sending user permissions to annotate: ", exception);
            return false;
        }
    }

    private String getAnnotateToken() {
        return this.securityContext.getAnnotateToken(annotationHost + "/api/token");
    }

}