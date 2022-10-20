package eu.europa.ec.leos.services.document;


import eu.europa.ec.leos.domain.vo.CloneProposalMetadataVO;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.repository.document.ProposalRepository;
import eu.europa.ec.leos.repository.store.PackageRepository;
import eu.europa.ec.leos.services.document.util.DocumentVOProvider;
import eu.europa.ec.leos.services.processor.content.TableOfContentProcessor;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessorProposal;
import eu.europa.ec.leos.services.processor.node.XmlNodeConfigProcessor;
import eu.europa.ec.leos.services.processor.node.XmlNodeProcessor;
import eu.europa.ec.leos.services.support.XPathCatalog;
import eu.europa.ec.leos.services.util.TestUtils;
import eu.europa.ec.leos.services.validation.ValidationService;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import java.util.Map;

public class ProposalServiceImplTest {

    private final static String CLONED_PROPOSAL_DOCUMENT = "/document/clonedProposals/";
    private final static String ORIGINAL_PROPOSAL_DOCUMENT = "/document/originalProposals/";

    @Mock
    ProposalRepository proposalRepository;
    @Mock
    XmlNodeProcessor xmlNodeProcessor;
    @Mock
    XmlNodeConfigProcessor xmlNodeConfigProcessor;
    @Mock
    PackageRepository packageRepository;
    @Mock TableOfContentProcessor tableOfContentProcessor;
    @Mock MessageHelper messageHelper;

    @InjectMocks
    private XPathCatalog xPathCatalog = spy(new XPathCatalog());

    private XmlContentProcessor xmlContentProcessor = new XmlContentProcessorProposal();
    private ProposalService proposalService;

    @Test
    public void test_getClonedProposalMetadata_should_return_clonedProposalMetadataVO() {
        byte[] xmlContent = TestUtils.getFileContent(CLONED_PROPOSAL_DOCUMENT, "proposal_cloned.xml");
        //Expected
        String expectedOriginRef = "proposal_ckmezv9qh0002yy56plb1dxhj";
        String expectedLegFileName = "leg_cko1gslfb0002ck56pt3pt5b0.leg";
        String expectedISCRef = "EdiT";
        String expectedObjectId = "afeba3f64617b7ef967c7cb6211be01cb2985a3e";

        proposalService = new ProposalServiceProposalImpl(proposalRepository, xmlNodeProcessor, xmlContentProcessor,
                xmlNodeConfigProcessor, packageRepository,
                xPathCatalog, tableOfContentProcessor, messageHelper);

        //DO the actual call
        CloneProposalMetadataVO cloneProposalMetadataVO = proposalService.getClonedProposalMetadata(xmlContent);

        //Assertions
        assertTrue(cloneProposalMetadataVO.isClonedProposal());
        assertEquals(expectedOriginRef, cloneProposalMetadataVO.getClonedFromRef());
        assertEquals(expectedLegFileName, cloneProposalMetadataVO.getLegFileName());
        assertEquals(expectedISCRef, cloneProposalMetadataVO.getOriginRef());
        assertEquals(expectedObjectId, cloneProposalMetadataVO.getClonedFromObjectId());
    }
    
    @Test
    public void test_getExplanatoryDocumentRef() {
    	byte[] xmlContent = TestUtils.getFileContent(ORIGINAL_PROPOSAL_DOCUMENT, "proposal_original.xml");

    	// Expected
        String expectedDocRefHref = "explanatory_cl43ykqyd0006k485zxvf53na.xml";
        String expectedDocRefId = "body_cmp_3__dref_1";
        
        proposalService = new ProposalServiceProposalImpl(proposalRepository, xmlNodeProcessor, xmlContentProcessor,
                xmlNodeConfigProcessor, packageRepository, xPathCatalog, tableOfContentProcessor, messageHelper);
        
        // Call
        Map<String, String> hrefIdMap = proposalService.getExplanatoryDocumentRef(xmlContent);
        
        // Assertions
        assertTrue(hrefIdMap.containsKey(expectedDocRefHref));
        assertTrue(hrefIdMap.containsValue(expectedDocRefId));
        
    }

}
