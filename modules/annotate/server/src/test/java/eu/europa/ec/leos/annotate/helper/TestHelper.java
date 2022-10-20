package eu.europa.ec.leos.annotate.helper;

import eu.europa.ec.leos.annotate.model.AnnotationStatus;
import eu.europa.ec.leos.annotate.model.entity.Annotation;
import eu.europa.ec.leos.annotate.repository.AnnotationTestRepository;
import org.junit.Assert;

import java.time.LocalDateTime;

public final class TestHelper {

    public static final String AUTH_HEADER = "authorization";
    public static final String AUTH_BEARER = "Bearer ";
    
    private TestHelper() {
        // utility class -> private constructor
    }
    
    // check that an annotation specified by its ID has a given status 
    // and/or was recently modified by a given user 
    public static void assertHasStatus(
            final AnnotationTestRepository annotRepos, 
            final String annotId, final AnnotationStatus status, final Long userId, final Long groupId) {
        
        final Annotation foundAnnot = annotRepos.findById(annotId).get();
        Assert.assertEquals("Status of annotation unexpected", status, foundAnnot.getStatus());
        Assert.assertEquals("User that updated status unexpected", userId, foundAnnot.getStatusUpdatedByUser());
        Assert.assertEquals("Group that updated status unexpected", groupId, foundAnnot.getStatusUpdatedByGroup());
        
        if(userId != null) {
            Assert.assertNotNull(foundAnnot.getStatusUpdated());
            // set timestamp must be within the last about ten seconds
            Assert.assertTrue("'updated' timestamp not within 10 seconds before now", withinLastSeconds(foundAnnot.getStatusUpdated(), 10));
        }
    }
    
    public static boolean withinLastSeconds(final LocalDateTime dateToCheck, final long tolerance) {

        final LocalDateTime currentTime = LocalDateTime.now(java.time.ZoneOffset.UTC);
        return (dateToCheck.isBefore(currentTime) || dateToCheck.isEqual(currentTime)) 
                && dateToCheck.isAfter(currentTime.minusSeconds(tolerance));

    }
}
