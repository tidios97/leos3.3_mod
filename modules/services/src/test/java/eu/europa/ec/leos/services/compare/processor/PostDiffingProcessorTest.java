package eu.europa.ec.leos.services.compare.processor;

import eu.europa.ec.leos.services.compare.processor.PostDiffingProcessor;
import junit.framework.TestCase;
import org.junit.Test;
import org.mockito.InjectMocks;

import static eu.europa.ec.leos.services.util.TestUtils.squeezeXml;

public class PostDiffingProcessorTest extends TestCase {

    @InjectMocks
    private PostDiffingProcessor postDiffingProcessor = new PostDiffingProcessor();

    @Test
    public void testAdjustSoftActionDiffing() {
        String expected = "<paragraph class=\"leos-double-compare-removed\" leos:softaction=\"del\">Due to the international nature of the postal and parcel sector</paragraph>";
        String diffResult = "<paragraph class=\"leos-double-compare-removed\" leos:softaction=\"add\">Due to the international nature of the postal and parcel sector</paragraph>";
        diffResult = postDiffingProcessor.adjustSoftActionDiffing(diffResult);
        assertEquals(squeezeXml(expected), squeezeXml(diffResult));
    }

    public void testAdjustSoftActionDoubleDiffing() {
        String expected = "<paragraph class=\"leos-double-compare-removed-original\" leos:softaction=\"del\">Due to the international nature of the postal and parcel sector</paragraph>";
        String diffResult = "<paragraph class=\"leos-double-compare-removed-original\" leos:softaction=\"add\">Due to the international nature of the postal and parcel sector</paragraph>";
        diffResult = postDiffingProcessor.adjustSoftActionDoubleDiffing(diffResult);
        assertEquals(squeezeXml(expected), squeezeXml(diffResult));
    }

    public void testAdjustResult() {
        String expected = "<authorialNoteid=\"dummyId\"leos:origin=\"ec\"marker=\"\"placement=\"bottom\">Thisisthecontentofthefootnote1</authorialNote><authorialNoteid=\"dummyId\"leos:origin=\"ec\"marker=\"1\"placement=\"bottom\">Thisisthecontentofthefootnote2</authorialNote>";
        String diffResult = "<authorialNote id=\"authorialnote_1\" leos:origin=\"ec\" marker=\"1\" placement=\"bottom\">This is the content of the footnote 1</authorialNote><authorialNote id=\"authorialnote_1\" leos:origin=\"ec\" marker=\"1\" placement=\"bottom\">This is the content of the footnote 2</authorialNote>";
        diffResult = postDiffingProcessor.adjustMarkersAuthorialNotes(diffResult);
        assertEquals(squeezeXml(expected), squeezeXml(diffResult));
    }

}