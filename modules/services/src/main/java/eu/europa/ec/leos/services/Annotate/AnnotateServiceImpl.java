package eu.europa.ec.leos.services.Annotate;

import java.net.URI;

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
            return annotationProvider.searchAnnotations(uri, securityContext.getAnnotateToken(annotationHost + "/api/token"), proposalRef);
        } catch (Exception exception) {
            LOG.error("Error getting annotations: ", exception);
            throw new RuntimeException("Error Occured While Getting Annotation");
        }
    }

}