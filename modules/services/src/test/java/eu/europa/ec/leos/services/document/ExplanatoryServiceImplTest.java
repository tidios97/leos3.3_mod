package eu.europa.ec.leos.services.document;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.any;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import eu.europa.ec.leos.cmis.mapping.CmisProperties;
import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.document.Explanatory;
import eu.europa.ec.leos.domain.cmis.metadata.ExplanatoryMetadata;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.user.Collaborator;
import eu.europa.ec.leos.repository.LeosRepository;
import eu.europa.ec.leos.repository.document.ExplanatoryRepository;
import eu.europa.ec.leos.repository.document.ExplanatoryRepositoryImpl;
import eu.europa.ec.leos.repository.store.PackageRepository;
import eu.europa.ec.leos.services.document.util.DocumentVOProvider;
import eu.europa.ec.leos.services.numbering.NumberService;
import eu.europa.ec.leos.services.processor.content.TableOfContentProcessor;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.processor.node.XmlNodeConfigProcessor;
import eu.europa.ec.leos.services.processor.node.XmlNodeProcessor;
import eu.europa.ec.leos.services.support.XPathCatalog;
import eu.europa.ec.leos.services.validation.ValidationService;
import eu.europa.ec.leos.test.support.LeosTest;
import io.atlassian.fugue.Option;

public class ExplanatoryServiceImplTest extends LeosTest {

	private ExplanatoryService explanatoryService;
	private ExplanatoryRepository explanatoryRepository;
	
	@Mock
	private PackageRepository packageRepository;
	@Mock
	private XmlNodeProcessor xmlNodeProcessor;
	@Mock
	private XmlContentProcessor xmlContentProcessor;
	@Mock
	private NumberService numberService;
	@Mock
	private XmlNodeConfigProcessor xmlNodeConfigProcessor;
	@Mock
	private ValidationService validationService;
	@Mock
	private DocumentVOProvider documentVOProvider;
	@Mock
	private TableOfContentProcessor tableOfContentProcessor;
	@Mock
	private MessageHelper messageHelper;
	@Mock
	private XPathCatalog xPathCatalog;
	@Mock
	private LeosRepository leosRepository;;
	
	private String objectId = "555";
	private String baseVersionId = "210::0.1.0::Element Created";

	@Before
   	public void onSetUp(){
		super.setup();
		
		explanatoryRepository = new ExplanatoryRepositoryImpl(leosRepository);
		
		explanatoryService = new ExplanatoryServiceImpl(explanatoryRepository, 
				packageRepository, xmlNodeProcessor, xmlContentProcessor, numberService, 
				xmlNodeConfigProcessor, validationService, documentVOProvider, tableOfContentProcessor, messageHelper, xPathCatalog);
	}
	
	@Test
    public void test_updateBaseVersionId() {
		when(leosRepository.updateDocument(anyString(), anyMap(), any(), anyBoolean())).thenReturn(getMockedExplanatoryWithBaseVersionId());
		Map<String, Object> properties = new HashMap<>();
        properties.put(CmisProperties.BASE_REVISION_ID.getId(), baseVersionId);
		Explanatory explanatory = explanatoryService.updateExplanatory(objectId, properties, true);
		assertEquals(baseVersionId, explanatory.getBaseRevisionId());
		
	}
	
	@Test
    public void test_enableLiveDiffing() {
		when(leosRepository.updateDocument(anyString(), anyMap(), any(), anyBoolean())).thenReturn(getMockedExplanatoryWithLiveDiffing());
		Map<String, Object> properties = new HashMap<>();
        properties.put(CmisProperties.LIVE_DIFFING_REQUIRED.getId(), true);
		Explanatory explanatory = explanatoryService.updateExplanatory(objectId, properties, true);
		assertTrue(explanatory.isLiveDiffingRequired());
		
	}
	
	@Test
    public void test_disableLiveDiffing() {
		when(leosRepository.updateDocument(anyString(), anyMap(), any(), anyBoolean())).thenReturn(getMockedExplanatoryWithoutLiveDiffing());
		Map<String, Object> properties = new HashMap<>();
        properties.put(CmisProperties.LIVE_DIFFING_REQUIRED.getId(), false);
		Explanatory explanatory = explanatoryService.updateExplanatory(objectId, properties, true);
		assertFalse(explanatory.isLiveDiffingRequired());
		
	}
	
	private Explanatory getMockedExplanatoryWithBaseVersionId() {
		ExplanatoryMetadata explanatoryMetadata = getMockedMetadata();
		List<Collaborator> collaborators = Arrays.asList(new Collaborator("test", "OWNER", "SG"));
		Content content = mock(Content.class);
		return new Explanatory(objectId, "EXPL_COUNCIL", "test", Instant.now(), "test", Instant.now(), "", "", "", "", VersionType.MINOR, false, "", collaborators, Arrays.asList(""),
				Option.some(content), baseVersionId, true, Option.some(explanatoryMetadata));
	}
	
	private Explanatory getMockedExplanatoryWithLiveDiffing() {
		ExplanatoryMetadata explanatoryMetadata = getMockedMetadata();
		List<Collaborator> collaborators = Arrays.asList(new Collaborator("test", "OWNER", "SG"));
		Content content = mock(Content.class);
		return new Explanatory(objectId, "EXPL_COUNCIL", "test", Instant.now(), "test", Instant.now(), "", "", "", "", VersionType.MINOR, false, "", collaborators, Arrays.asList(""),
				Option.some(content), null, true, Option.some(explanatoryMetadata));
	}
	
	private Explanatory getMockedExplanatoryWithoutLiveDiffing() {
		ExplanatoryMetadata explanatoryMetadata = getMockedMetadata();
		List<Collaborator> collaborators = Arrays.asList(new Collaborator("test", "OWNER", "SG"));
		Content content = mock(Content.class);
		return new Explanatory(objectId, "EXPL_COUNCIL", "test", Instant.now(), "test", Instant.now(), "", "", "", "", VersionType.MINOR, false, "", collaborators, Arrays.asList(""),
				Option.some(content), null, false, Option.some(explanatoryMetadata));
	}
	
    private ExplanatoryMetadata getMockedMetadata() {
        return new ExplanatoryMetadata("... at this stage", "REGULATION OF THE EUROPEAN PARLIAMENT AND OF THE COUNCIL", "on ...",
                "CE-001", "EN", "CE-001", "explanatory", "Working Party cover page", "555", "0.1.0", false);
    }
}
