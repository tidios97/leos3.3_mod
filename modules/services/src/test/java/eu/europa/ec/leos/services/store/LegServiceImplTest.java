package eu.europa.ec.leos.services.store;

import eu.europa.ec.leos.cmis.domain.ContentImpl;
import eu.europa.ec.leos.cmis.domain.SourceImpl;
import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.Content.Source;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.LeosPackage;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.document.Annex;
import eu.europa.ec.leos.domain.cmis.document.Bill;
import eu.europa.ec.leos.domain.cmis.document.Explanatory;
import eu.europa.ec.leos.domain.cmis.document.Memorandum;
import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.domain.cmis.metadata.AnnexMetadata;
import eu.europa.ec.leos.domain.cmis.metadata.BillMetadata;
import eu.europa.ec.leos.domain.cmis.metadata.ExplanatoryMetadata;
import eu.europa.ec.leos.domain.cmis.metadata.MemorandumMetadata;
import eu.europa.ec.leos.domain.cmis.metadata.ProposalMetadata;
import eu.europa.ec.leos.i18n.LanguageHelper;
import eu.europa.ec.leos.i18n.MandateMessageHelper;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.action.ContributionVO;
import eu.europa.ec.leos.model.user.Collaborator;
import eu.europa.ec.leos.model.user.Entity;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.repository.document.ProposalRepository;
import eu.europa.ec.leos.repository.store.PackageRepository;
import eu.europa.ec.leos.repository.store.WorkspaceRepository;
import eu.europa.ec.leos.security.LeosPermissionAuthorityMapHelper;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.Annotate.AnnotateService;
import eu.europa.ec.leos.services.clone.CloneContext;
import eu.europa.ec.leos.services.compare.ContentComparatorService;
import eu.europa.ec.leos.services.compare.LeosTextComparatorImpl;
import eu.europa.ec.leos.services.compare.TextComparator;
import eu.europa.ec.leos.services.compare.XMLContentComparatorServiceImplMandate;
import eu.europa.ec.leos.services.converter.ProposalConverterService;
import eu.europa.ec.leos.services.document.AnnexService;
import eu.europa.ec.leos.services.document.BillService;
import eu.europa.ec.leos.services.document.DocumentContentService;
import eu.europa.ec.leos.services.document.ExplanatoryService;
import eu.europa.ec.leos.services.document.MemorandumService;
import eu.europa.ec.leos.services.document.ProposalService;
import eu.europa.ec.leos.services.document.ProposalServiceProposalImpl;
import eu.europa.ec.leos.services.export.ExportDW;
import eu.europa.ec.leos.services.export.ExportOptions;
import eu.europa.ec.leos.services.export.ExportVersions;
import eu.europa.ec.leos.services.export.LegPackage;
import eu.europa.ec.leos.services.label.ReferenceLabelService;
import eu.europa.ec.leos.services.processor.AttachmentProcessor;
import eu.europa.ec.leos.services.processor.AttachmentProcessorImpl;
import eu.europa.ec.leos.services.processor.content.TableOfContentProcessor;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessorMandate;
import eu.europa.ec.leos.services.processor.content.indent.IndentHelper;
import eu.europa.ec.leos.services.processor.node.XmlNodeConfigProcessor;
import eu.europa.ec.leos.services.processor.node.XmlNodeConfigProcessorImpl;
import eu.europa.ec.leos.services.processor.node.XmlNodeProcessor;
import eu.europa.ec.leos.services.processor.node.XmlNodeProcessorImpl;
import eu.europa.ec.leos.services.processor.rendition.HtmlRenditionProcessor;
import eu.europa.ec.leos.services.support.XPathCatalog;
import eu.europa.ec.leos.services.template.TemplateStructureService;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.services.toc.StructureService;
import eu.europa.ec.leos.services.toc.StructureServiceImpl;
import eu.europa.ec.leos.services.util.TestUtils;
import eu.europa.ec.leos.test.support.model.ModelHelper;
import eu.europa.ec.leos.vo.toc.NumberingConfig;
import eu.europa.ec.leos.vo.toc.TocItem;
import io.atlassian.fugue.Option;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import javax.inject.Provider;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class LegServiceImplTest {

	private final static String STORE_DIR = "/store/";

	private ApplicationContext applicationContext;
	private List<TocItem> tocItems;
    private List<NumberingConfig> numberingConfigs;

	@Mock
	private PackageRepository packageRepository;
	@Mock
	private WorkspaceRepository workspaceRepository;
	@Mock
	private AnnotateService annotateService;
	@Mock
	private HtmlRenditionProcessor htmlRenditionProcessor;
	@Mock
	private ProposalConverterService proposalConverterService;
	@Mock
	private LeosPermissionAuthorityMapHelper authorityMapHelper;
	@Mock
	private Provider<StructureContext> structureContextProvider;
	@Mock
	private DocumentContentService documentContentService;
	@Mock
	private BillService billService;
	@Mock
	private AnnexService annexService;
	@Mock
	private MemorandumService memorandumService;
	@Mock
	private ExplanatoryService explanatoryService;
	@Mock
	private ProposalRepository proposalRepository;
	@Mock
	private LanguageHelper languageHelper;
	@Mock
	private ReferenceLabelService referenceLabelService;
	@Mock
	private StructureContext structureContext;
	@Mock
	private TemplateStructureService templateStructureService;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private CloneContext cloneContext;
	@Mock
	private IndentHelper indentHelper;
	@Mock
	private TableOfContentProcessor tableOfContentProcessor;

	@InjectMocks
	private XPathCatalog xPathCatalog = spy(new XPathCatalog());
	
	@InjectMocks
	private MessageHelper messageHelper = spy(getMessageHelper());
	
	@InjectMocks
	private TextComparator textComparator = spy(new LeosTextComparatorImpl(messageHelper));
	
	@InjectMocks
	private StructureService structureService = spy(new StructureServiceImpl());
	
	@InjectMocks
	private XmlNodeProcessor xmlNodeProcessor = spy(new XmlNodeProcessorImpl());
	
	@InjectMocks
	private XmlNodeConfigProcessor xmlNodeConfigProcessor = spy(new XmlNodeConfigProcessorImpl());
	
	@InjectMocks
	private XmlContentProcessor xmlContentProcessor = spy(new XmlContentProcessorMandate());
	
	@InjectMocks
	private AttachmentProcessor attachmentProcessor = spy(new AttachmentProcessorImpl(xmlContentProcessor, xPathCatalog));
	
	@InjectMocks
	private ContentComparatorService contentComparatorService = spy(new XMLContentComparatorServiceImplMandate(messageHelper, textComparator, securityContext, xmlContentProcessor));
	
	@InjectMocks
	private ProposalService proposalService = spy(new ProposalServiceProposalImpl(proposalRepository, xmlNodeProcessor, xmlContentProcessor, xmlNodeConfigProcessor, packageRepository, xPathCatalog,
									tableOfContentProcessor, messageHelper));

	@InjectMocks
	private LegServiceImpl legService;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		
		String docTemplate = "CE-001";
        
		byte[] bytesFile = TestUtils.getFileContent("/structure-test-explanatory-CN.xml");
        when(templateStructureService.getStructure(docTemplate)).thenReturn(bytesFile);
		
        ReflectionTestUtils.setField(structureService, "structureSchema", "toc/schema/structure_1.xsd");
        tocItems = structureService.getTocItems(docTemplate);
        numberingConfigs = structureService.getNumberingConfigs(docTemplate);
        
        List<Entity> entities = new ArrayList<Entity>();
        entities.add(new Entity("1", "DIGIT.B2", "DIGIT"));
        User user = ModelHelper.buildUser(45L, "demo", "demo", entities);
        when(securityContext.getUser()).thenReturn(user);
        when(securityContext.getUserName()).thenReturn("demo");
	}

	@After
	public void tearDown() {
		if (applicationContext != null)
			((ConfigurableApplicationContext) applicationContext).close();
	}

	@Test
	public void test_createLegPackage_withMemorandumActualVersion() throws IOException {
		String proposalId = "555";
		List<Collaborator> collaborators = new ArrayList<>();
		collaborators.add(new Collaborator("login", "OWNER", "SG"));

		Proposal proposal = getMockedProposal(proposalId, collaborators);
		Memorandum memorandum = getMockedMemorandum(collaborators);

		// Memorandum with original & current version
		ExportOptions exportOptions = new ExportDW(ExportOptions.Output.WORD, Memorandum.class, false);
		exportOptions.setExportVersions(new ExportVersions<Memorandum>(memorandum, memorandum));

		LeosPackage leosPackage = new LeosPackage(proposalId, "Proposal", "");
		
		when(packageRepository.findPackageByDocumentId(proposalId)).thenReturn(leosPackage);
		when(workspaceRepository.findDocumentById(proposalId, Proposal.class, true)).thenReturn(proposal);
		when(packageRepository.findDocumentByPackagePathAndName(eq(leosPackage.getPath()), anyString(), eq(Memorandum.class))).thenReturn(memorandum);

		// Expected
		String hrefExpected = "memorandum_ckn9773is000ywn567lsopipc.xml";
		
		// Call
		LegPackage legPackage = legService.createLegPackage(proposalId, exportOptions);
		
		// Actual
		assertEquals(hrefExpected, legPackage.getExportResource().getChildResource(LeosCategory.MEMORANDUM.name()).getHref());
	}

	@Test
	public void test_createLegPackage_withMemorandumCleanVersion() throws IOException {
		String proposalId = "555";

		List<Collaborator> collaborators = new ArrayList<>();
		collaborators.add(new Collaborator("login", "OWNER", "SG"));

		Proposal proposal = getMockedProposal(proposalId, collaborators);
		Memorandum memorandum = getMockedMemorandum(collaborators);

		// Memorandum with only current version
		ExportOptions exportOptions = new ExportDW(ExportOptions.Output.WORD, Memorandum.class, false, true);
		exportOptions.setExportVersions(new ExportVersions<Memorandum>(null, memorandum));

		LeosPackage leosPackage = new LeosPackage(proposalId, "Proposal", "");
		
		when(packageRepository.findPackageByDocumentId(proposalId)).thenReturn(leosPackage);
		when(workspaceRepository.findDocumentById(proposalId, Proposal.class, true)).thenReturn(proposal);
		when(packageRepository.findDocumentByPackagePathAndName(eq(leosPackage.getPath()), anyString(), eq(Memorandum.class))).thenReturn(memorandum);
		when(structureContextProvider.get()).thenReturn(structureContext);
        when(structureContext.getTocItems()).thenReturn(tocItems);
        when(structureContext.getNumberingConfigs()).thenReturn(numberingConfigs);
        when(htmlRenditionProcessor.processTemplate(any())).thenReturn(StringUtils.EMPTY);
        when(htmlRenditionProcessor.processJsTemplate(any())).thenReturn(StringUtils.EMPTY);
        when(htmlRenditionProcessor.processTocTemplate(any(), any())).thenReturn(StringUtils.EMPTY);
        
		// Expected
        String hrefExpected = "memorandum_ckn9773is000ywn567lsopipc.xml";
		
        // Call
        LegPackage legPackage = legService.createLegPackage(proposalId, exportOptions);
		
        // Actual
		assertEquals(hrefExpected, legPackage.getExportResource().getChildResource(LeosCategory.MEMORANDUM.name()).getHref());
	}

	@Test
	public void test_createLegPackage_withExplanatoryActualVersion() throws IOException {
		String proposalId = "555";

		List<Collaborator> collaborators = new ArrayList<>();
		collaborators.add(new Collaborator("login", "OWNER", "SG"));

		Proposal proposal = getMockedProposal(proposalId, collaborators);
		Explanatory explanatory = getMockedExplanatory(collaborators);

		// Explanatory with original & current version
		ExportOptions exportOptions = new ExportDW(ExportOptions.Output.WORD, Explanatory.class, false);
		exportOptions.setExportVersions(new ExportVersions<Explanatory>(explanatory, explanatory));

		LeosPackage leosPackage = new LeosPackage(proposalId, "Proposal", "");
		
		when(packageRepository.findPackageByDocumentId(proposalId)).thenReturn(leosPackage);
		when(workspaceRepository.findDocumentById(proposalId, Proposal.class, true)).thenReturn(proposal);

		// Expected
		String hrefExpected = "explanatory_cl43ykqyd0006k485zxvf53na.xml";
		
		// Call
		LegPackage legPackage = legService.createLegPackage(proposalId, exportOptions);
		
		// Actual
		assertEquals(hrefExpected, legPackage.getExportResource().getChildResource(LeosCategory.COUNCIL_EXPLANATORY.name()).getHref());
	}

	@Test
	public void test_createLegPackage_withExplanatoryCleanVersion() throws IOException {
		String proposalId = "555";

		List<Collaborator> collaborators = new ArrayList<>();
		collaborators.add(new Collaborator("login", "OWNER", "SG"));

		Proposal proposal = getMockedProposal(proposalId, collaborators);
		Explanatory explanatory = getMockedExplanatory(collaborators);

		// Explanatory with only current version
		ExportOptions exportOptions = new ExportDW(ExportOptions.Output.WORD, Explanatory.class, false, true);
		exportOptions.setExportVersions(new ExportVersions<Explanatory>(null, explanatory));

		LeosPackage leosPackage = new LeosPackage(proposalId, "Proposal", "");
		
		when(packageRepository.findPackageByDocumentId(proposalId)).thenReturn(leosPackage);
		when(workspaceRepository.findDocumentById(proposalId, Proposal.class, true)).thenReturn(proposal);
        when(structureContextProvider.get()).thenReturn(structureContext);
        when(structureContext.getTocItems()).thenReturn(tocItems);
        when(structureContext.getNumberingConfigs()).thenReturn(numberingConfigs);
        when(htmlRenditionProcessor.processTemplate(any())).thenReturn(StringUtils.EMPTY);
        when(htmlRenditionProcessor.processJsTemplate(any())).thenReturn(StringUtils.EMPTY);
        when(htmlRenditionProcessor.processTocTemplate(any(), any())).thenReturn(StringUtils.EMPTY);
        
		// Expected
        String hrefExpected = "explanatory_cl43ykqyd0006k485zxvf53na.xml";
		
        // Call
        LegPackage legPackage = legService.createLegPackage(proposalId, exportOptions);
		
        // Actual
		assertEquals(hrefExpected, legPackage.getExportResource().getChildResource(LeosCategory.COUNCIL_EXPLANATORY.name()).getHref());
	}
	
	@Test
	public void test_createLegPackage_withBillActualVersion() throws IOException {
		String proposalId = "555";

		List<Collaborator> collaborators = new ArrayList<>();
		collaborators.add(new Collaborator("login", "OWNER", "SG"));

		Proposal proposal = getMockedProposal(proposalId, collaborators);
		Bill bill = getMockedBill(collaborators);

		// Bill with original & current version
		ExportOptions exportOptions = new ExportDW(ExportOptions.Output.WORD, Bill.class, false);
		exportOptions.setExportVersions(new ExportVersions<Bill>(bill, bill));

		LeosPackage leosPackage = new LeosPackage(proposalId, "Proposal", "");
		
		when(packageRepository.findPackageByDocumentId(proposalId)).thenReturn(leosPackage);
		when(workspaceRepository.findDocumentById(proposalId, Proposal.class, true)).thenReturn(proposal);
		when(packageRepository.findDocumentByPackagePathAndName(eq(leosPackage.getPath()), anyString(), eq(Bill.class))).thenReturn(bill);
		
		// Expected
		String hrefExpected = "bill_ckn97778i000zwn56esq96qet.xml";
		
		// Call
		LegPackage legPackage = legService.createLegPackage(proposalId, exportOptions);
		
		// Actual
		assertEquals(hrefExpected, legPackage.getExportResource().getChildResource(LeosCategory.BILL.name()).getHref());
	}
	
	@Test
	public void test_createLegPackage_withBillCleanVersion() throws IOException {
		String proposalId = "555";
		
		List<Collaborator> collaborators = new ArrayList<>();
		collaborators.add(new Collaborator("login", "OWNER", "SG"));

		Proposal proposal = getMockedProposal(proposalId, collaborators);
		Bill bill = getMockedBill(collaborators);

		// Bill with only current version
		ExportOptions exportOptions = new ExportDW(ExportOptions.Output.WORD, Bill.class, false, true);
		exportOptions.setExportVersions(new ExportVersions<Bill>(null, bill));

		LeosPackage leosPackage = new LeosPackage(proposalId, "Proposal", "");
		
		when(packageRepository.findPackageByDocumentId(proposalId)).thenReturn(leosPackage);
		when(workspaceRepository.findDocumentById(proposalId, Proposal.class, true)).thenReturn(proposal);
		when(packageRepository.findDocumentByPackagePathAndName(eq(leosPackage.getPath()), anyString(), eq(Bill.class))).thenReturn(bill);
		when(structureContextProvider.get()).thenReturn(structureContext);
        when(structureContext.getTocItems()).thenReturn(tocItems);
        when(structureContext.getNumberingConfigs()).thenReturn(numberingConfigs);
        when(htmlRenditionProcessor.processTemplate(any())).thenReturn(StringUtils.EMPTY);
        when(htmlRenditionProcessor.processJsTemplate(any())).thenReturn(StringUtils.EMPTY);
        when(htmlRenditionProcessor.processTocTemplate(any(), any())).thenReturn(StringUtils.EMPTY);

        // Expected
        String hrefExpected = "bill_ckn97778i000zwn56esq96qet.xml";
		
        // Call
		LegPackage legPackage = legService.createLegPackage(proposalId, exportOptions);
		
		// Actual
		assertEquals(hrefExpected, legPackage.getExportResource().getChildResource(LeosCategory.BILL.name()).getHref());
	}
	
	@Test
	public void test_createLegPackage_withAnnexActualVersion() throws IOException {
		String proposalId = "555";

		List<Collaborator> collaborators = new ArrayList<>();
		collaborators.add(new Collaborator("login", "OWNER", "SG"));

		Proposal proposal = getMockedProposal(proposalId, collaborators);
		Bill bill = getMockedBill(collaborators);
		Annex annex = getMockedAnnex(collaborators);

		// Annex with original & current version
		ExportOptions exportOptions = new ExportDW(ExportOptions.Output.WORD, Annex.class, false);
		exportOptions.setExportVersions(new ExportVersions<Annex>(annex, annex));

		LeosPackage leosPackage = new LeosPackage(proposalId, "Proposal", "");
		
		when(packageRepository.findPackageByDocumentId(proposalId)).thenReturn(leosPackage);
		when(workspaceRepository.findDocumentById(proposalId, Proposal.class, true)).thenReturn(proposal);
		when(packageRepository.findDocumentByPackagePathAndName(eq(leosPackage.getPath()), anyString(), eq(Bill.class))).thenReturn(bill);
		
		// Expected
		String hrefExpected = "annex_cl3yjnpcz0007k485t5p989mq.xml";
		
		// Call
		LegPackage legPackage = legService.createLegPackage(proposalId, exportOptions);
		
		// Actual
		assertEquals(hrefExpected, legPackage.getExportResource().getChildResource(LeosCategory.BILL.name()).getChildResource(LeosCategory.ANNEX.name()).getHref());
	}
	
	@Test
	public void test_createLegPackage_withAnnexCleanVersion() throws IOException {
		String proposalId = "555";

		List<Collaborator> collaborators = new ArrayList<>();
		collaborators.add(new Collaborator("login", "OWNER", "SG"));

		Proposal proposal = getMockedProposal(proposalId, collaborators);
		Bill bill = getMockedBill(collaborators);
		Annex annex = getMockedAnnex(collaborators);

		// Annex with only current version
		ExportOptions exportOptions = new ExportDW(ExportOptions.Output.WORD, Annex.class, false, true);
		exportOptions.setExportVersions(new ExportVersions<Annex>(null, annex));

		LeosPackage leosPackage = new LeosPackage(proposalId, "Proposal", "");
		
		when(packageRepository.findPackageByDocumentId(proposalId)).thenReturn(leosPackage);
		when(workspaceRepository.findDocumentById(proposalId, Proposal.class, true)).thenReturn(proposal);
		when(packageRepository.findDocumentByPackagePathAndName(eq(leosPackage.getPath()), anyString(), eq(Bill.class))).thenReturn(bill);
		when(structureContextProvider.get()).thenReturn(structureContext);
        when(structureContext.getTocItems()).thenReturn(tocItems);
        when(structureContext.getNumberingConfigs()).thenReturn(numberingConfigs);
        when(htmlRenditionProcessor.processTemplate(any())).thenReturn(StringUtils.EMPTY);
        when(htmlRenditionProcessor.processJsTemplate(any())).thenReturn(StringUtils.EMPTY);
        when(htmlRenditionProcessor.processTocTemplate(any(), any())).thenReturn(StringUtils.EMPTY);
		
        // Expected
        String hrefExpected = "annex_cl3yjnpcz0007k485t5p989mq.xml";
		
        // Call
		LegPackage legPackage = legService.createLegPackage(proposalId, exportOptions);
		
		// Actual
		assertEquals(hrefExpected, legPackage.getExportResource().getChildResource(LeosCategory.BILL.name()).getChildResource(LeosCategory.ANNEX.name()).getHref());
	}

	@Test
	public void test_createLegPackage_withAllDocs() throws IOException {
		String proposalId = "555";

		List<Collaborator> collaborators = new ArrayList<>();
		collaborators.add(new Collaborator("login", "OWNER", "SG"));

		Proposal proposal = getMockedProposal(proposalId, collaborators);
		Explanatory explanatory = getMockedExplanatory(collaborators);
		Bill bill = getMockedBill(collaborators);
		Memorandum memorandum = getMockedMemorandum(collaborators);
		Annex annex = getMockedAnnex(collaborators);

		ExportOptions exportOptions = new ExportDW(ExportOptions.Output.WORD);

		LeosPackage leosPackage = new LeosPackage(proposalId, "Proposal", "");
		
		when(packageRepository.findPackageByDocumentId(proposalId)).thenReturn(leosPackage);
		when(workspaceRepository.findDocumentById(proposalId, Proposal.class, true)).thenReturn(proposal);
		when(packageRepository.findDocumentByPackagePathAndName(eq(leosPackage.getPath()), anyString(), eq(Bill.class))).thenReturn(bill);
		when(packageRepository.findDocumentByPackagePathAndName(eq(leosPackage.getPath()), anyString(), eq(Memorandum.class))).thenReturn(memorandum);
		when(packageRepository.findDocumentByPackagePathAndName(eq(leosPackage.getPath()), anyString(), eq(Annex.class))).thenReturn(annex);
		when(packageRepository.findDocumentByPackagePathAndName(eq(leosPackage.getPath()), anyString(), eq(Explanatory.class))).thenReturn(explanatory);
        when(structureContextProvider.get()).thenReturn(structureContext);
        when(structureContext.getTocItems()).thenReturn(tocItems);
        when(structureContext.getNumberingConfigs()).thenReturn(numberingConfigs);
        when(htmlRenditionProcessor.processTemplate(any())).thenReturn(StringUtils.EMPTY);
        when(htmlRenditionProcessor.processJsTemplate(any())).thenReturn(StringUtils.EMPTY);
        when(htmlRenditionProcessor.processTocTemplate(any(), any())).thenReturn(StringUtils.EMPTY);
        when(htmlRenditionProcessor.processCoverPage(any())).thenReturn(StringUtils.EMPTY);
        
		// Expected
		String hrefMemorandon = "memorandum_ckn9773is000ywn567lsopipc.xml";
		String hrefExplanatory = "explanatory_cl43ykqyd0006k485zxvf53na.xml";
		String hrefBill = "bill_ckn97778i000zwn56esq96qet.xml";
		String hrefAnnex = "annex_cl3yjnpcz0007k485t5p989mq.xml";
		
		// Call
		LegPackage legPackage = legService.createLegPackage(proposalId, exportOptions);
		
		// Actual
		assertEquals(hrefMemorandon, legPackage.getExportResource().getChildResource(LeosCategory.MEMORANDUM.name()).getHref());
		assertEquals(hrefExplanatory, legPackage.getExportResource().getChildResource(LeosCategory.COUNCIL_EXPLANATORY.name()).getHref());
		assertEquals(hrefBill, legPackage.getExportResource().getChildResource(LeosCategory.BILL.name()).getHref());
		assertEquals(hrefAnnex, legPackage.getExportResource().getChildResource(LeosCategory.BILL.name()).getChildResource(LeosCategory.ANNEX.name()).getHref());
	}

	private Proposal getMockedProposal(String proposalId, List<Collaborator> collaborators) {
		byte[] proposalXmlContent = TestUtils.getFileContent(STORE_DIR, "proposal.xml");
		Source proposalSource = new SourceImpl(new ByteArrayInputStream(proposalXmlContent));
		Content proposalContent = new ContentImpl("PR-00.xml", "mime type", proposalXmlContent.length, proposalSource);
		ProposalMetadata proposalMetadata = new ProposalMetadata("", "REGULATION for EC", "", "PR-00.xml", "EN", "", "proposal-id", "", "0.1.0", false);
		Proposal leosProposal = new Proposal(proposalId, "Proposal", "login", Instant.now(), "login", Instant.now(), "", "", "", "", VersionType.MAJOR, true, "REGULATION for EC", collaborators,
				Arrays.asList(""), "login", Instant.now(), Option.some(proposalContent), Option.some(proposalMetadata), true, "", "", "", null,
										ContributionVO.ContributionStatus.CONTRIBUTION_DONE.name());
		return leosProposal;
	}

	private Explanatory getMockedExplanatory(List<Collaborator> collaborators) {
		byte[] xmlContent = TestUtils.getFileContent(STORE_DIR, "explanatory.xml");
		Source source = new SourceImpl(new ByteArrayInputStream(xmlContent));
		Content content = new ContentImpl("CE-001.xml", "mime type", xmlContent.length, source);
		ExplanatoryMetadata explanatoryMetadata = new ExplanatoryMetadata("", "REGULATION", "", "CE-001", "EN", "CE-001",
				"explanatory_cl43ykqyd0006k485zxvf53na.xml", "Working Party cover page", "555", "0.1.0", false);
		return new Explanatory("555", "explanatory_cl43ykqyd0006k485zxvf53na.xml", "test", Instant.now(), "test", Instant.now(), "", "", "", "", VersionType.MINOR, false, "", collaborators, Arrays.asList(""),
				Option.some(content), Option.some(explanatoryMetadata));
	}
	
	private Bill getMockedBill(List<Collaborator> collaborators) {
		byte[] xmlContent = TestUtils.getFileContent(STORE_DIR, "bill.xml");
		Source source = new SourceImpl(new ByteArrayInputStream(xmlContent));
		Content content = new ContentImpl("BL-023.xml", "mime type", xmlContent.length, source);
		BillMetadata billMetadata = new BillMetadata("", "REGULATION", "", "SJ-023", "EN", "BL-023", 
				"bill_ckn97778i000zwn56esq96qet.xml", "", "0.1.0", false);
		return new Bill("555", "bill_ckn97778i000zwn56esq96qet.xml", "login", Instant.now(), "login", Instant.now(),
                "", "", "Version 1.0.0", "", VersionType.MAJOR,
            true, "title", collaborators, Arrays.asList(""), "", "",
            "", Option.some(content), Option.some(billMetadata));
	}
	
	private Memorandum getMockedMemorandum(List<Collaborator> collaborators) {
		byte[] xmlContent = TestUtils.getFileContent(STORE_DIR, "memorandum.xml");
		Source source = new SourceImpl(new ByteArrayInputStream(xmlContent));
		Content content = new ContentImpl("EM-LP01.xml", "mime type", xmlContent.length, source);
		MemorandumMetadata metadata = new MemorandumMetadata("555", "REGULATION", "", "EM-LP01", "EN", "EM-LP01", 
				"memorandum_ckn9773is000ywn567lsopipc.xml", "", "0.1.0", false);
		return new Memorandum("555", "memorandum_ckn9773is000ywn567lsopipc.xml", "login", Instant.now(), "login", Instant.now(), 
				"", "", "Version 1.0.0", "", VersionType.MAJOR, true, "title", 
				collaborators, Arrays.asList(""), Option.some(content), "", null, Option.some(metadata));
	}
	
	private Annex getMockedAnnex(List<Collaborator> collaborators) {
		byte[] xmlContent = TestUtils.getFileContent(STORE_DIR, "annex.xml");
		Source source = new SourceImpl(new ByteArrayInputStream(xmlContent));
		Content content = new ContentImpl("AN-000.xml", "mime type", xmlContent.length, source);
		AnnexMetadata annexMetadata = new AnnexMetadata("", "REGULATION", "", "AN-000.xml", "EN", "AN-000.xml", 
				"annex_cl3yjnpcz0007k485t5p989mq.xml", 1, "Annex 1", "title", "", "0.0.1", false, STORE_DIR);
		return new Annex("555", "annex_cl3yjnpcz0007k485t5p989mq.xml", "login", Instant.now(), "login", Instant.now(),
				"0.1.0", "", "0.1.0", "", VersionType.MINOR, true,
                "title", Collections.emptyList(), Arrays.asList(""), "", "", "",
                Option.some(content), Option.some(annexMetadata));
	}

	private MessageHelper getMessageHelper() {
		applicationContext = new ClassPathXmlApplicationContext("test-servicesContext.xml");
		MessageSource servicesMessageSource = (MessageSource) applicationContext.getBean("servicesMessageSource");
		MessageHelper messageHelper = new MandateMessageHelper(servicesMessageSource);
		return messageHelper;
	}
}
