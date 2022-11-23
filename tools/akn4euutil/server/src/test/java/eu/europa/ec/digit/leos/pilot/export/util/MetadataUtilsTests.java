package eu.europa.ec.digit.leos.pilot.export.util;

import eu.europa.ec.digit.leos.pilot.export.exception.MetadataUtilsException;
import eu.europa.ec.digit.leos.pilot.export.model.metadata.MetadataFieldType;
import eu.europa.ec.digit.leos.pilot.export.model.metadata.fieldInfo.MetadataFieldInfo;
import eu.europa.ec.digit.leos.pilot.export.model.metadata.fieldInfo.MultipleReferencesFieldInfo;
import eu.europa.ec.digit.leos.pilot.export.model.metadata.fieldInfo.ReferenceFieldInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * tests on parsing the linked documents
 */
public class MetadataUtilsTests {

    @Test
    public void testLinkedDocumentsSwdWithDraft() throws MetadataUtilsException {
     
        final MetadataFieldInfo actual = MetadataUtil.parseLinkedDocuments("{SWD(2012) 1234 draft}");

        Assertions.assertNotNull(actual);

        final MultipleReferencesFieldInfo typedResult = (MultipleReferencesFieldInfo) actual;
        Assertions.assertEquals(1, typedResult.getReferences().size());

        final ReferenceFieldInfo refField = typedResult.getReferences().get(0);
        
        Assertions.assertEquals("SWD(2012) 1234 draft", refField.getDisplayValue());
        Assertions.assertEquals("http://data.europa.eu/eli/swd/2012/1234", refField.getHref());
    }
    
    @Test
    public void testLinkedDocumentsComWithoutSuffixAndBrackets() throws MetadataUtilsException {
     
        final MetadataFieldInfo actual = MetadataUtil.parseLinkedDocuments("COM(2014) 4");

        Assertions.assertNotNull(actual);

        final MultipleReferencesFieldInfo typedResult = (MultipleReferencesFieldInfo) actual;
        Assertions.assertEquals(1, typedResult.getReferences().size());

        final ReferenceFieldInfo refField = typedResult.getReferences().get(0);
        
        Assertions.assertEquals("COM(2014) 4", refField.getDisplayValue());
        Assertions.assertEquals("http://data.europa.eu/eli/com/2014/4", refField.getHref());
    }
    
    @Test
    public void testLinkedDocumentsSecIsConvertedToSwdInHref() throws MetadataUtilsException {

        final MetadataFieldInfo actual = MetadataUtil.parseLinkedDocuments("{SEC(2011) 12 final}");

        Assertions.assertNotNull(actual);
        Assertions.assertTrue(actual instanceof MultipleReferencesFieldInfo);

        final MultipleReferencesFieldInfo typedResult = (MultipleReferencesFieldInfo) actual;
        Assertions.assertEquals(MetadataFieldType.LINKED_DOCUMENTS, actual.getFieldType());

        Assertions.assertEquals(1, typedResult.getReferences().size());
        final ReferenceFieldInfo refField = typedResult.getReferences().get(0);
        
        Assertions.assertEquals("SEC(2011) 12 final", refField.getDisplayValue());
        Assertions.assertEquals("http://data.europa.eu/eli/swd/2011/12", refField.getHref());
    }
    
    @Test
    public void testParseMultipleLinkedDocuments() throws MetadataUtilsException {
        
        final MetadataFieldInfo actual = MetadataUtil.parseLinkedDocuments(
                "{COM(2014) 4 final}-{SWD(2012) 1111}-{SEC(2016) 248 final}");

        Assertions.assertNotNull(actual);

        final MultipleReferencesFieldInfo typedResult = (MultipleReferencesFieldInfo) actual;
        Assertions.assertEquals(3, typedResult.getReferences().size());

        ReferenceFieldInfo refField = typedResult.getReferences().get(0);
        Assertions.assertEquals("COM(2014) 4 final", refField.getDisplayValue());
        Assertions.assertEquals("http://data.europa.eu/eli/com/2014/4", refField.getHref());
        
        refField = typedResult.getReferences().get(1);
        Assertions.assertEquals("SWD(2012) 1111", refField.getDisplayValue());
        Assertions.assertEquals("http://data.europa.eu/eli/swd/2012/1111", refField.getHref());
        
        refField = typedResult.getReferences().get(2);
        Assertions.assertEquals("SEC(2016) 248 final", refField.getDisplayValue());
        Assertions.assertEquals("http://data.europa.eu/eli/swd/2016/248", refField.getHref());
    }
}
