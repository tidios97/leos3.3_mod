package eu.europa.ec.leos.annotate.unit.model;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.europa.ec.leos.annotate.model.AnnotationStatus;

public class AnnotationStatusTest {

    // check that the retrieval of all statuses actually returns them all
    // (small help for consistency checks in case the enum is enlarged)
    @Test
    public void checkAllStatusEnums() {
        
        final List<AnnotationStatus> all = AnnotationStatus.getAllValues();
        Assert.assertEquals(all.size(), AnnotationStatus.values().length - 1); // contains all enum values except "ALL"
        Assert.assertTrue(all.contains(AnnotationStatus.NORMAL));
        Assert.assertTrue(all.contains(AnnotationStatus.DELETED));
        Assert.assertTrue(all.contains(AnnotationStatus.TREATED));
        Assert.assertTrue(all.contains(AnnotationStatus.ACCEPTED));
        Assert.assertTrue(all.contains(AnnotationStatus.REJECTED));

        // compute value sum
        int sum = 0;
        for(int i = 0; i < all.size(); i++) {
            sum += all.get(i).getEnumValue();
        }
        
        Assert.assertEquals(AnnotationStatus.ALL.getEnumValue(), sum);
    }
    
    // check that the default item is still the expected one
    @Test
    public void checkDefaultStatus() {
        
        final List<AnnotationStatus> defaultList = AnnotationStatus.getDefaultStatus();
        Assert.assertEquals(1, defaultList.size());
        Assert.assertTrue(defaultList.contains(AnnotationStatus.NORMAL));
    }
}
