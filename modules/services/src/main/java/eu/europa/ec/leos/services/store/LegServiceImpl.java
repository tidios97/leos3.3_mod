package eu.europa.ec.leos.services.store;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cool.graph.cuid.Cuid;
import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.LeosLegStatus;
import eu.europa.ec.leos.domain.cmis.LeosPackage;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.document.Annex;
import eu.europa.ec.leos.domain.cmis.document.Bill;
import eu.europa.ec.leos.domain.cmis.document.LegDocument;
import eu.europa.ec.leos.domain.cmis.document.MediaDocument;
import eu.europa.ec.leos.domain.cmis.document.Memorandum;
import eu.europa.ec.leos.domain.cmis.document.Explanatory;
import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.domain.cmis.metadata.AnnexMetadata;
import eu.europa.ec.leos.domain.cmis.metadata.BillMetadata;
import eu.europa.ec.leos.domain.cmis.metadata.MemorandumMetadata;
import eu.europa.ec.leos.domain.cmis.metadata.ProposalMetadata;
import eu.europa.ec.leos.domain.cmis.metadata.ExplanatoryMetadata;
import eu.europa.ec.leos.domain.common.TocMode;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.domain.vo.LegDocumentVO;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.rendition.RenderedDocument;
import eu.europa.ec.leos.model.user.Collaborator;
import eu.europa.ec.leos.model.xml.Element;
import eu.europa.ec.leos.repository.store.PackageRepository;
import eu.europa.ec.leos.repository.store.WorkspaceRepository;
import eu.europa.ec.leos.security.LeosPermissionAuthorityMapHelper;
import eu.europa.ec.leos.services.Annotate.AnnotateService;
import eu.europa.ec.leos.services.compare.ContentComparatorContext;
import eu.europa.ec.leos.services.compare.ContentComparatorService;
import eu.europa.ec.leos.services.processor.AttachmentProcessor;
import eu.europa.ec.leos.services.document.DocumentContentService;
import eu.europa.ec.leos.services.converter.ProposalConverterService;
import eu.europa.ec.leos.services.document.AnnexService;
import eu.europa.ec.leos.services.document.BillService;
import eu.europa.ec.leos.services.document.MemorandumService;
import eu.europa.ec.leos.services.document.ProposalService;
import eu.europa.ec.leos.services.document.ExplanatoryService;
import eu.europa.ec.leos.services.export.ExportOptions;
import eu.europa.ec.leos.services.export.ExportResource;
import eu.europa.ec.leos.services.export.ExportVersions;
import eu.europa.ec.leos.services.export.LegPackage;
import eu.europa.ec.leos.services.export.RelevantElements;
import eu.europa.ec.leos.services.export.ZipPackageUtil;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.processor.rendition.HtmlRenditionProcessor;
import eu.europa.ec.leos.services.compare.processor.PostDiffingProcessor;
import eu.europa.ec.leos.services.processor.content.TableOfContentHelper;
import eu.europa.ec.leos.services.support.XPathCatalog;
import eu.europa.ec.leos.services.support.XmlHelper;
import eu.europa.ec.leos.services.processor.node.XmlNodeConfig;
import eu.europa.ec.leos.services.processor.node.XmlNodeConfigProcessor;
import eu.europa.ec.leos.services.processor.node.XmlNodeProcessor;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.vo.toc.TableOfContentItemHtmlVO;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.inject.Provider;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static eu.europa.ec.leos.services.compare.ContentComparatorService.ATTR_NAME;
import static eu.europa.ec.leos.services.compare.ContentComparatorService.CONTENT_ADDED_CLASS;
import static eu.europa.ec.leos.services.compare.ContentComparatorService.CONTENT_REMOVED_CLASS;
import static eu.europa.ec.leos.services.compare.ContentComparatorService.DOUBLE_COMPARE_ADDED_CLASS;
import static eu.europa.ec.leos.services.compare.ContentComparatorService.DOUBLE_COMPARE_INTERMEDIATE_STYLE;
import static eu.europa.ec.leos.services.compare.ContentComparatorService.DOUBLE_COMPARE_ORIGINAL_STYLE;
import static eu.europa.ec.leos.services.compare.ContentComparatorService.DOUBLE_COMPARE_REMOVED_CLASS;
import static eu.europa.ec.leos.services.compare.ContentComparatorService.DOUBLE_COMPARE_RETAIN_CLASS;
import static eu.europa.ec.leos.services.processor.node.XmlNodeConfigProcessor.createValueMap;
import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class LegServiceImpl implements LegService {
    private static final Logger LOG = LoggerFactory.getLogger(LegServiceImpl.class);
    
    private final PackageRepository packageRepository;
    private final WorkspaceRepository workspaceRepository;
    private final AttachmentProcessor attachmentProcessor;
    private final XmlNodeProcessor xmlNodeProcessor;
    private final XmlNodeConfigProcessor xmlNodeConfigProcessor;
    private final AnnotateService annotateService;
    private final HtmlRenditionProcessor htmlRenditionProcessor;
    private final ProposalConverterService proposalConverterService;
    private final LeosPermissionAuthorityMapHelper authorityMapHelper;
    private final ContentComparatorService compareService;
    private final MessageHelper messageHelper;
    private final Provider<StructureContext> structureContextProvider;
    private final DocumentContentService documentContentService;
    private final BillService billService;
    private final AnnexService annexService;
    private final MemorandumService memorandumService;
    private final ExplanatoryService explanatoryService;
    private final XmlContentProcessor xmlContentProcessor;
    private final ProposalService proposalService;
    private final XPathCatalog xPathCatalog;

    private static final String MEDIA_DIR = "media/";
    private static final String ANNOT_FILE_EXT = ".json";
    private static final String ANNOT_FILE_PREFIX = "annot_";
    private static final String LEG_FILE_PREFIX = "leg_";
    private static final String LEG_FILE_EXTENSION = ".leg";
    private static final String STYLE_SHEET_EXT = ".css";
    private static final String JS_EXT = ".js";
    private static final String STYLE_DEST_DIR = "renditions/html/css/";
    private static final String JS_DEST_DIR = "renditions/html/js/";
    private static final String STYLES_SOURCE_PATH = "META-INF/resources/assets/css/";
    private static final String JS_SOURCE_PATH = "META-INF/resources/js/";
    private static final String JQUERY_SOURCE_PATH = "META-INF/resources/lib/jquery_3.2.1/";
    private static final String JQTREE_SOURCE_PATH = "META-INF/resources/lib/jqTree_1.4.9/";
    private static final String HTML_RENDITION = "renditions/html/";
    private static final String PDF_RENDITION = "renditions/pdf/";
    private static final String WORD_RENDITION = "renditions/word/";
    
    private static final String annexStyleSheet = LeosCategory.ANNEX.name().toLowerCase() + STYLE_SHEET_EXT;
    private static final String memoStyleSheet = LeosCategory.MEMORANDUM.name().toLowerCase() + STYLE_SHEET_EXT;
    private static final String billStyleSheet = LeosCategory.BILL.name().toLowerCase() + STYLE_SHEET_EXT;
    private static final String coverPageStyleSheet = LeosCategory.COVERPAGE.name().toLowerCase() + STYLE_SHEET_EXT;
    private static final String explanatoryStyleSheet = "explanatory" + STYLE_SHEET_EXT;

    public static final String FORMAT_DATE_TIME_ISO_8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    public static final String SUGGESTION = "suggestion";

    @Autowired
    public LegServiceImpl(PackageRepository packageRepository,
                   WorkspaceRepository workspaceRepository,
                   AttachmentProcessor attachmentProcessor,
                   XmlNodeProcessor xmlNodeProcessor,
                   XmlNodeConfigProcessor xmlNodeConfigProcessor,
                   AnnotateService annotateService,
                   HtmlRenditionProcessor htmlRenditionProcessor,
                   ProposalConverterService proposalConverterService,
                   LeosPermissionAuthorityMapHelper authorityMapHelper,
                   ContentComparatorService compareService,
                   MessageHelper messageHelper,
                   Provider<StructureContext> structureContextProvider,
                   DocumentContentService documentContentService, BillService billService,
                   MemorandumService memorandumService,
                   AnnexService annexService, XmlContentProcessor xmlContentProcessor,
                   ProposalService proposalService,
                   XPathCatalog xPathCatalog,
                   ExplanatoryService explanatoryService) {
        this.packageRepository = packageRepository;
        this.workspaceRepository = workspaceRepository;
        this.attachmentProcessor = attachmentProcessor;
        this.xmlNodeProcessor = xmlNodeProcessor;
        this.xmlNodeConfigProcessor = xmlNodeConfigProcessor;
        this.annotateService = annotateService;
        this.htmlRenditionProcessor = htmlRenditionProcessor;
        this.proposalConverterService = proposalConverterService;
        this.authorityMapHelper = authorityMapHelper;
        this.messageHelper = messageHelper;
        this.compareService = compareService;
        this.structureContextProvider = structureContextProvider;
        this.documentContentService = documentContentService;
        this.billService = billService;
        this.memorandumService = memorandumService;
        this.annexService = annexService;
        this.xmlContentProcessor = xmlContentProcessor;
        this.proposalService = proposalService;
        this.xPathCatalog = xPathCatalog;
        this.explanatoryService = explanatoryService;
    }

    @Override
    public LegDocument findLastLegByVersionedReference(String path, String versionedReference) {
        return packageRepository.findLastLegByVersionedReference(path, versionedReference);
    }
    
    @Override
    public List<LegDocumentVO> getLegDocumentDetailsByUserId(String userId, String proposalId, String legStatus) {
        List<LegDocumentVO> legDocumentVOs = new ArrayList<>();
        if(!StringUtils.isEmpty(proposalId)) {
            Proposal proposal = proposalService.findProposalByRef(proposalId);
            Optional<Collaborator> userAsCollaborator = proposal.getCollaborators().stream()
                    .filter(x -> x.getLogin().equalsIgnoreCase(userId)).findAny();
            if(userAsCollaborator.isPresent()) {
                LegDocumentVO legDocumentVO = getLegDocumentVO(proposal, legStatus);
                if(legDocumentVO != null) {
                    legDocumentVOs.add(legDocumentVO);
                }
            }
        } else {
            //TODO:Improve performance by searching for leg files using userId and legFileStatus only.
            //TODO:CMIS properties needs to be added in leg file (leos:collaborator,docTitle, proposalId)
            List<Proposal> proposals = packageRepository.findDocumentsByUserId(userId, Proposal.class,
                    authorityMapHelper.getRoleForDocCreation());
            for (Proposal proposal : proposals) {
                LegDocumentVO legDocumentVO = getLegDocumentVO(proposal, legStatus);
                if(legDocumentVO != null) {
                    legDocumentVOs.add(legDocumentVO);
                }
            }
        }
        return legDocumentVOs;
    }

    private LegDocumentVO getLegDocumentVO(Proposal proposal, String legStatus) {
        LegDocumentVO legDocumentVO = null;
        List<LegDocument> legDocuments = findLegDocumentByProposal(proposal.getId());
        if (!legDocuments.isEmpty()) {
            legDocuments.sort(Comparator.comparing(LegDocument::getLastModificationInstant).reversed());
            LegDocument leg = legDocuments.get(0);
            if(StringUtils.isEmpty(legStatus)) {
                legDocumentVO = populateLegDocumentVO(proposal, leg);
            } else {
                if(leg.getStatus().name().equals(legStatus)) {
                    legDocumentVO = populateLegDocumentVO(proposal, leg);
                }
            }
        }
        return legDocumentVO;
    }

    private LegDocumentVO populateLegDocumentVO(Proposal proposal, LegDocument leg) {
        LegDocumentVO legDocumentVO = new LegDocumentVO();
        legDocumentVO.setProposalId(proposal.getMetadata().getOrError(() -> "Proposal metadata is not available!").getRef());
        legDocumentVO.setDocumentTitle(proposal.getTitle());
        legDocumentVO.setLegFileId(leg.getId());
        legDocumentVO.setLegFileName(leg.getName());
        legDocumentVO.setLegFileStatus(leg.getStatus().name());
        legDocumentVO.setMilestoneComments(leg.getMilestoneComments());
        legDocumentVO.setCreationDate(new SimpleDateFormat(FORMAT_DATE_TIME_ISO_8601).
                format(Date.from(leg.getInitialCreationInstant())));
        legDocumentVO.setClonedProposal(proposal.isClonedProposal());
        return legDocumentVO;
    }

    private String generateLegName(String proposalId) {
        String docCollectionXPath = xPathCatalog.getXPathProposalDocCollection();
        Proposal proposal = proposalService.findProposal(proposalId);
        final Content content = proposal.getContent().getOrError(() -> "Proposal content is required!");
        byte[] xmlContent = content.getSource().getBytes();
        String language = proposal.getMetadata().get().getLanguage().toLowerCase();
        String docCollectionName = xmlContentProcessor.getElementValue(xmlContent, docCollectionXPath, true);
        return docCollectionName.concat("-").concat(Cuid.createCuid()).concat("-").concat(language).concat(LEG_FILE_EXTENSION);
    }
    
    private byte[] addMetadataToProposal(Proposal proposal, byte[] xmlContent) {
        ProposalMetadata metadata = proposal.getMetadata().get();
        metadata = metadata.withObjectId(proposal.getId()).withDocVersion(proposal.getVersionLabel());
        xmlContent = xmlNodeProcessor.setValuesInXml(xmlContent, createValueMap(metadata), xmlNodeConfigProcessor.getConfig(metadata.getCategory()));
        return xmlContent;
    }
    
    private byte[] addMetadataToMemorandum(Memorandum memorandum, byte[] xmlContent) {
        MemorandumMetadata metadata = memorandum.getMetadata().get();
        metadata = metadata.withObjectId(memorandum.getId()).withDocVersion(memorandum.getVersionLabel());
        xmlContent = xmlNodeProcessor.setValuesInXml(xmlContent, createValueMap(metadata), xmlNodeConfigProcessor.getConfig(metadata.getCategory()));
        return xmlContent;
    }
    
    private byte[] addMetadataToBill(Bill bill, byte[] xmlContent) {
        BillMetadata metadata = bill.getMetadata().get();
        metadata = metadata.withObjectId(bill.getId()).withDocVersion(bill.getVersionLabel());
        xmlContent = xmlNodeProcessor.setValuesInXml(xmlContent, createValueMap(metadata), xmlNodeConfigProcessor.getConfig(metadata.getCategory()));
        return xmlContent;
    }
    
    private byte[] addMetadataToAnnex(Annex annex, byte[] xmlContent) {
        AnnexMetadata metadata = annex.getMetadata().get();
        metadata = metadata.withObjectId(annex.getId()).withDocVersion(annex.getVersionLabel());
        xmlContent = xmlNodeProcessor.setValuesInXml(xmlContent, createValueMap(metadata), xmlNodeConfigProcessor.getConfig(metadata.getCategory()),
                xmlNodeConfigProcessor.getOldPrefaceOfAnnexConfig());
        return xmlContent;
    }

    private byte[] addMetadataToExplanatory(Explanatory explanatory, byte[] xmlContent) {
        ExplanatoryMetadata metadata = explanatory.getMetadata().get();
        metadata = metadata.withObjectId(explanatory.getId()).withDocVersion(explanatory.getVersionLabel());
        xmlContent = xmlNodeProcessor.setValuesInXml(xmlContent, createValueMap(metadata), xmlNodeConfigProcessor.getConfig(metadata.getCategory()));
        return xmlContent;
    }
    
    /**
     * Creates the LegPackage for the given leg file.
     *
     * @param legFile legFile for which we need to create the LegPackage
     * @param exportOptions
     * @return LegPackage used to be sent to Toolbox for PDF/LegisWrite generation.
     */
    @Override
    public LegPackage createLegPackage(File legFile, ExportOptions exportOptions) throws IOException {
        // legFile will be deleted after createProposalFromLegFile(), so we save the bytes in a temporary file
        File legFileTemp = File.createTempFile("RENDITION_", ".leg");
        FileUtils.copyFile(legFile, legFileTemp);
        
        final DocumentVO proposalVO = proposalConverterService.createProposalFromLegFile(legFile, new DocumentVO(LeosCategory.PROPOSAL), false);
        final byte[] proposalXmlContent = proposalVO.getSource();
        ExportResource proposalExportResource = new ExportResource(LeosCategory.PROPOSAL);
        final Map<String, String> proposalRefsMap = buildProposalExportResource(proposalExportResource, proposalXmlContent);
        proposalExportResource.setExportOptions(exportOptions);
        final DocumentVO memorandumVO = proposalVO.getChildDocument(LeosCategory.MEMORANDUM);
        final byte[] memorandumXmlContent = memorandumVO.getSource();
        final ExportResource memorandumExportResource = buildExportResourceMemorandum(proposalRefsMap, memorandumXmlContent);
        proposalExportResource.addChildResource(memorandumExportResource);
        
        final DocumentVO billVO = proposalVO.getChildDocument(LeosCategory.BILL);
        final byte[] billXmlContent = billVO.getSource();
        final ExportResource billExportResource = buildExportResourceBill(proposalRefsMap, billXmlContent);
        
        // add annexes to billExportResource
        final Map<String, String> attachmentIds = attachmentProcessor.getAttachmentsIdFromBill(billXmlContent);
        final List<DocumentVO> annexesVO = billVO.getChildDocuments(LeosCategory.ANNEX);
        annexesVO.forEach((annexVO) -> {
            final byte[] annexXmlContent = annexVO.getSource();
            final int docNumber = Integer.parseInt(annexVO.getMetadata().getIndex());
            final String resourceId = attachmentIds.entrySet()
                    .stream()
                    .filter(e -> e.getKey().equals(annexVO.getRef()))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .get();
            final ExportResource annexExportResource = buildExportResourceAnnex(docNumber, resourceId, annexXmlContent);
            billExportResource.addChildResource(annexExportResource);
        });

        final List<DocumentVO> explanatoriesVO = billVO.getChildDocuments(LeosCategory.COUNCIL_EXPLANATORY);
        explanatoriesVO.forEach((explanatoryVO) -> {
            final byte[] explanatoryXmlContent = explanatoryVO.getSource();
            final int docNumber = Integer.parseInt(explanatoryVO.getMetadata().getIndex());
            final String resourceId = attachmentIds.entrySet()
                    .stream()
                    .filter(e -> e.getKey().equals(explanatoryVO.getRef()))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .get();
            final ExportResource explanatoryExportResource = buildExportResourceExplanatory(docNumber, resourceId, explanatoryXmlContent);
            billExportResource.addChildResource(explanatoryExportResource);
        });
        proposalExportResource.addChildResource(billExportResource);
        LegPackage legPackage = new LegPackage();
        legPackage.setFile(legFileTemp);
        legPackage.setExportResource(proposalExportResource);
        return legPackage;
    }
    
    /**
     * Creates the LegPackage, which is the logical representation of the leg file, for the given proposalId.
     *
     * @param proposalId       proposalId for which we need to create the LegPackage
     * @param exportOptions
     * @return LegPackage used to be sent to Toolbox for PDF/LegisWrite generation.
     */
    @Override
    public LegPackage createLegPackage(String proposalId, ExportOptions exportOptions) throws IOException {
        LOG.trace("Creating Leg Package... [documentId={}]", proposalId);
        final LegPackage legPackage = new LegPackage();
        final LeosPackage leosPackage = packageRepository.findPackageByDocumentId(proposalId);
        final Map<String, Object> contentToZip = new HashMap<>();
        final ExportResource exportProposalResource = new ExportResource(LeosCategory.PROPOSAL);
        exportProposalResource.setExportOptions(exportOptions);
        
        // 1. Add Proposal to package
        final Proposal proposal = workspaceRepository.findDocumentById(proposalId, Proposal.class, true);
        final Map<String, String> proposalRefsMap = enrichZipWithProposal(contentToZip, exportProposalResource, proposal);
        legPackage.addContainedFile(proposal.getVersionedReference());
        byte[] proposalContent = proposal.getContent().get().getSource().getBytes();
        String language = proposal.getMetadata().get().getLanguage();

        // 2. Depending on ExportOptions FileType add documents to package
        if (exportOptions.isComparisonMode() || exportOptions.isCleanVersion()) {
            if (Memorandum.class.equals(exportOptions.getFileType())) {
                Memorandum memorandum = packageRepository.findDocumentByPackagePathAndName(leosPackage.getPath(),
                        proposalRefsMap.get(LeosCategory.MEMORANDUM.name() + "_href"), Memorandum.class);
                byte[] xmlContent;
                if (exportOptions.isComparisonMode()) {
                    xmlContent = getComparedContent(exportOptions);
                } else {
                    xmlContent = xmlContentProcessor.cleanSoftActions(memorandum.getContent().get().getSource().getBytes());
                }
                enrichZipWithMemorandum(contentToZip, exportProposalResource, proposalRefsMap, memorandum, proposal.getMetadata().getOrNull().getRef(), xmlContent);                                
            } else if (Bill.class.equals(exportOptions.getFileType())) {
                final Bill bill = packageRepository.findDocumentByPackagePathAndName(leosPackage.getPath(),
                        proposalRefsMap.get(LeosCategory.BILL.name() + "_href"), Bill.class);
                byte[] xmlContent;
                if (exportOptions.isComparisonMode()) {
                    xmlContent = getComparedContent(exportOptions);
                } else {
                    xmlContent = xmlContentProcessor.cleanSoftActions(bill.getContent().get().getSource().getBytes());
                }
                if (exportOptions.isWithRelevantElements()) {
                    structureContextProvider.get().useDocumentTemplate(bill.getMetadata().get().getDocTemplate());
                    xmlContent = addRelevantElements(exportOptions, bill.getVersionLabel(), xmlContent);
                    xmlContent = addCommentsMetadata(exportOptions.getComments(), xmlContent);
                }
                enrichZipWithBill(contentToZip, exportProposalResource, proposalRefsMap, bill, proposal.getMetadata().getOrNull().getRef(), xmlContent);
                legPackage.addContainedFile(bill.getVersionedReference());
            } else if (Annex.class.equals(exportOptions.getFileType())) {
                final Bill bill = packageRepository.findDocumentByPackagePathAndName(leosPackage.getPath(),
                        proposalRefsMap.get(LeosCategory.BILL.name() + "_href"), Bill.class);
                byte[] billXmlContent = bill.getContent().get().getSource().getBytes();
                ExportResource exportBillResource = buildExportResourceBill(proposalRefsMap, billXmlContent);
                exportBillResource.setExportOptions(exportOptions);
                exportProposalResource.addChildResource(exportBillResource);
                addAnnexToPackage(leosPackage, contentToZip, exportOptions, exportBillResource, legPackage, proposal.getMetadata().getOrNull().getRef(), billXmlContent);
                legPackage.addContainedFile(bill.getVersionedReference());
            } else if (Explanatory.class.equals(exportOptions.getFileType())) {
            	addExplanatoryToPackage(leosPackage, contentToZip, exportOptions, exportProposalResource, legPackage, proposal);
            } else if (Proposal.class.equals(exportOptions.getFileType())) {
                addCoverPageHtmlRendition(contentToZip, proposalContent, coverPageStyleSheet, proposal);
            } else {
                throw new IllegalStateException("Not implemented for type: " + exportOptions.getFileType());
            }
        } else {
            addMemorandumToPackage(leosPackage, contentToZip, exportProposalResource, proposalRefsMap, legPackage, proposal.getMetadata().getOrNull().getRef());
            addExplanatoryToPackage(leosPackage, contentToZip, exportOptions, exportProposalResource, legPackage, proposal);
            addBillToPackage(leosPackage, contentToZip, exportOptions, exportProposalResource, proposalRefsMap, legPackage, proposal.getMetadata().getOrNull().getRef());
            addCoverPageHtmlRendition(contentToZip, proposalContent, coverPageStyleSheet, proposal);
            enrichZipWithToc(contentToZip);
            enrichZipWithMedia(contentToZip, leosPackage);
        }
        String legPackageName = proposalRefsMap.get(XmlNodeConfigProcessor.PROPOSAL_DOC_COLLECTION).concat(LEG_FILE_EXTENSION);
        legPackage.setFile(ZipPackageUtil.zipFiles(legPackageName, contentToZip, language));
        legPackage.setExportResource(exportProposalResource);
        return legPackage;
    }

    private void addMemorandumToPackage(final LeosPackage leosPackage, final Map<String, Object> contentToZip,
                                        ExportResource exportProposalResource, final Map<String, String> proposalRefsMap,
                                        LegPackage legPackage, String proposalRef) {
        final String memorandumRef = proposalRefsMap.get(LeosCategory.MEMORANDUM.name() + "_href");
        if (!StringUtils.isEmpty(memorandumRef) && !memorandumRef.equals("#")) {
            final Memorandum memorandum = packageRepository.findDocumentByPackagePathAndName(leosPackage.getPath(), memorandumRef, Memorandum.class);
            enrichZipWithMemorandum(contentToZip, exportProposalResource, proposalRefsMap, memorandum, proposalRef);
            legPackage.addContainedFile(memorandum.getVersionedReference());
        }
    }
    
    private void addExplanatoryToPackage(final LeosPackage leosPackage, final Map<String, Object> contentToZip,
                                   ExportOptions exportOptions, ExportResource exportProposalResource, LegPackage legPackage,
                                   Proposal proposal) {
    	// if we are in comparison mode, we don't need to fetch the document from CMIS, is already present in exportVersions
        final String explanatoryId = exportOptions.isComparisonMode() || exportOptions.isCleanVersion() ? exportOptions.getExportVersions().getCurrent().getMetadata().get().getRef() : null;
        
        byte[] proposalContent = proposal.getContent().get().getSource().getBytes();
        final Map<String, String> documentRefMap = proposalService.getExplanatoryDocumentRef(proposalContent);
                
        if (!documentRefMap.isEmpty()){
            for (Map.Entry<String, String> entry : documentRefMap.entrySet()) {
                String href = entry.getKey();
                String id = entry.getValue();
                Explanatory explanatory;
                if (explanatoryId == null) {
                	explanatory = packageRepository.findDocumentByPackagePathAndName(leosPackage.getPath(), href, Explanatory.class);
                } else if(href.contains(explanatoryId)){
                	explanatory = (Explanatory) exportOptions.getExportVersions().getCurrent();
                } else {
                    continue;
                }
    
                String proposalRef = proposal.getMetadata().getOrNull().getRef();
            	enrichZipWithExplanatory(contentToZip, exportProposalResource, explanatory, exportOptions, id, href, proposalRef);
                legPackage.addContainedFile(explanatory.getVersionedReference());
            }
            
            if (explanatoryId != null) { // only if we are not in comparison mode
                addResourceToZipContent(contentToZip, explanatoryStyleSheet, STYLES_SOURCE_PATH, STYLE_DEST_DIR);
            }
        }
    }

    private void addBillToPackage(final LeosPackage leosPackage, final Map<String, Object> contentToZip,
                                    ExportOptions exportOptions, ExportResource exportProposalResource, final Map<String, String> proposalRefsMap,
                                    LegPackage legPackage, String proposalRef) {
        final String billRef = proposalRefsMap.get(LeosCategory.BILL.name() + "_href");
        if (!StringUtils.isEmpty(billRef) && !billRef.equals("#")) {
            final Bill bill = packageRepository.findDocumentByPackagePathAndName(leosPackage.getPath(), billRef, Bill.class);
            byte[] billXmlContent;
            if (exportOptions.isComparisonMode()) {
                billXmlContent = getComparedContent(exportOptions);
            } else {
                billXmlContent = bill.getContent().get().getSource().getBytes();
                billXmlContent = addMetadataToBill(bill, billXmlContent);
            }
            ExportResource exportBillResource = enrichZipWithBill(contentToZip, exportProposalResource, proposalRefsMap, bill, proposalRef, billXmlContent);
            legPackage.addContainedFile(bill.getVersionedReference());

            addAnnexToPackage(leosPackage, contentToZip, exportOptions, exportBillResource, legPackage, proposalRef, billXmlContent);
        }
    }
    
    private void addAnnexToPackage(final LeosPackage leosPackage, final Map<String, Object> contentToZip,
                                   ExportOptions exportOptions, ExportResource exportProposalResource, LegPackage legPackage,
                                   String proposalRef, byte[] xmlContent) {
        // if we are in comparison mode, we don't need to fetch the document from CMIS, is already present in exportVersions
        final String annexId = exportOptions.isComparisonMode() || exportOptions.isCleanVersion() ?
                exportOptions.getExportVersions().getCurrent().getMetadata().get().getRef()
                : null;
        
        final Map<String, String> attachmentIds = attachmentProcessor.getAttachmentsIdFromBill(xmlContent);
        if (!attachmentIds.isEmpty()){
            for (Map.Entry<String, String> entry : attachmentIds.entrySet()) {
                String href = entry.getKey();
                String id = entry.getValue();
                Annex annex;
                if (annexId == null) {
                    annex = packageRepository.findDocumentByPackagePathAndName(leosPackage.getPath(), href, Annex.class);
                } else if(href.contains(annexId)){
                    annex = (Annex) exportOptions.getExportVersions().getCurrent();
                } else {
                    continue;
                }
    
                enrichZipWithAnnex(contentToZip, exportProposalResource, annex, exportOptions, id, href, proposalRef);
                legPackage.addContainedFile(annex.getVersionedReference());
            }
            
            if (annexId != null) { // only if we are not in comparison mode
                addResourceToZipContent(contentToZip, annexStyleSheet, STYLES_SOURCE_PATH, STYLE_DEST_DIR);
            }
        }
    }

    private Map<String, String> enrichZipWithProposal(final Map<String, Object> contentToZip, ExportResource exportProposalResource, Proposal proposal) {
        byte[] xmlContent = proposal.getContent().get().getSource().getBytes();
        xmlContent = addMetadataToProposal(proposal, xmlContent);
        contentToZip.put(proposalService.generateProposalName(proposal.getMetadata().get().getRef(),
                proposal.getMetadata().get().getLanguage()), xmlContent);

        ExportOptions exportOptions = exportProposalResource.getExportOptions();
        addAnnotateToZipContent(contentToZip, proposal.getMetadata().get().getRef(), proposal.getName(), exportOptions, proposal.getMetadata().getOrNull().getRef());
        if (exportOptions.getFileType().equals(Proposal.class)) {
            addFilteredAnnotationsToZipContent(contentToZip, proposal.getName(), exportOptions);
        }
        return buildProposalExportResource(exportProposalResource, xmlContent);
    }

    private Map<String, String> enrichZipWithProposalForClone(final Map<String, Object> contentToZip, ExportResource exportProposalResource, Proposal proposal) {
        ExportOptions exportOptions = exportProposalResource.getExportOptions();
        byte[] xmlContent = proposal.getContent().get().getSource().getBytes();
        if(exportOptions.isComparisonMode()) {
            XmlDocument originalProposal = documentContentService.getOriginalProposal(proposal);
            xmlContent = simpleCompareXmlContentsForClone(originalProposal, proposal).getBytes(UTF_8);
        }
        List<Element> docPurposeElements = xmlContentProcessor.getElementsByTagName(xmlContent, Arrays.asList("docPurpose"), true);
        String title = !docPurposeElements.isEmpty() ?
                xmlContentProcessor.getElementByNameAndId(xmlContent, docPurposeElements.get(0).getElementTagName(), docPurposeElements.get(0).getElementId()) :
                null;
        xmlContent = addMetadataToProposal(proposal, xmlContent);
        if (title != null) {
            xmlContent = xmlContentProcessor.replaceElementById(xmlContent, title, docPurposeElements.get(0).getElementId());
        }

        contentToZip.put(proposalService.generateProposalName(proposal.getMetadata().get().getRef(),
                proposal.getMetadata().get().getLanguage()), xmlContent);

        addAnnotateToZipContent(contentToZip, proposal.getMetadata().get().getRef(), proposal.getName(), exportOptions, proposal.getMetadata().getOrNull().getRef());
        if (exportOptions.getFileType().equals(Proposal.class)) {
            addFilteredAnnotationsToZipContent(contentToZip, proposal.getName(), exportOptions);
        }

        return buildProposalExportResource(exportProposalResource, xmlContent);
    }

    private void enrichZipWithToc(final Map<String, Object> contentToZip) {
        addResourceToZipContent(contentToZip, "jquery" + JS_EXT, JQUERY_SOURCE_PATH, JS_DEST_DIR);
        addResourceToZipContent(contentToZip, "jqtree" + JS_EXT, JQTREE_SOURCE_PATH, JS_DEST_DIR);
        addResourceToZipContent(contentToZip, "jqtree" + STYLE_SHEET_EXT, JQTREE_SOURCE_PATH + "css/", STYLE_DEST_DIR);
        addResourceToZipContent(contentToZip, "leos-toc-rendition" + JS_EXT, JS_SOURCE_PATH + "rendition/", JS_DEST_DIR);
        addResourceToZipContent(contentToZip, "leos-toc-rendition" + STYLE_SHEET_EXT, STYLES_SOURCE_PATH, STYLE_DEST_DIR);
    }
    
    private void enrichZipWithMemorandum(final Map<String, Object> contentToZip, ExportResource exportProposalResource,
                                         Map<String, String> proposalRefsMap, Memorandum memorandum,
                                         String proposalRef) {
        ExportOptions exportOptions = exportProposalResource.getExportOptions();

        byte[] xmlContent = memorandum.getContent().get().getSource().getBytes();
        xmlContent = addMetadataToMemorandum(memorandum, xmlContent);
        contentToZip.put(memorandum.getName(), xmlContent);
        
        addAnnotateToZipContent(contentToZip, memorandum.getMetadata().get().getRef(), memorandum.getName(), exportOptions, proposalRef);
        addFilteredAnnotationsToZipContent(contentToZip, memorandum.getName(), exportOptions);

        addResourceToZipContent(contentToZip, memoStyleSheet, STYLES_SOURCE_PATH, STYLE_DEST_DIR);
        structureContextProvider.get().useDocumentTemplate(memorandum.getMetadata().get().getDocTemplate());
        final String memoTocJson = getTocAsJson(memorandumService.getTableOfContent(memorandum, TocMode.SIMPLIFIED_CLEAN));
        addHtmlRendition(contentToZip, memorandum.getName(), xmlContent, memoStyleSheet, memoTocJson);
        
        final ExportResource memorandumExportResource = buildExportResourceMemorandum(proposalRefsMap, xmlContent);
        exportProposalResource.addChildResource(memorandumExportResource);
    }

    private ExportResource enrichZipWithMemorandum(final Map<String, Object> contentToZip, ExportResource exportProposalResource, Map<String, String> proposalRefsMap,
            Memorandum memorandum, String proposalRef, byte[] xmlContent) {
        ExportOptions exportOptions = exportProposalResource.getExportOptions();
        contentToZip.put(memorandum.getName(), xmlContent);

        addAnnotateToZipContent(contentToZip, memorandum.getMetadata().get().getRef(), memorandum.getName(), exportOptions, proposalRef);
        if (exportOptions.getFileType().equals(Memorandum.class)) {
            addFilteredAnnotationsToZipContent(contentToZip, memorandum.getName(), exportOptions);
        }

        if (!exportOptions.isComparisonMode()) {
            addResourceToZipContent(contentToZip, memoStyleSheet, STYLES_SOURCE_PATH, STYLE_DEST_DIR);
            structureContextProvider.get().useDocumentTemplate(memorandum.getMetadata().get().getDocTemplate());
            final String memorandumTocJson = getTocAsJson(memorandumService.getTableOfContent(memorandum, TocMode.SIMPLIFIED_CLEAN));
            addHtmlRendition(contentToZip, memorandum.getName(), xmlContent, memoStyleSheet, memorandumTocJson);
        }

        final ExportResource exportMemorandumResource = buildExportResourceMemorandum(proposalRefsMap, xmlContent);
        exportMemorandumResource.setExportOptions(exportOptions);
        exportProposalResource.addChildResource(exportMemorandumResource);
        return exportMemorandumResource;
    }

    private ExportResource enrichZipWithBill(final Map<String, Object> contentToZip, ExportResource exportProposalResource, Map<String, String> proposalRefsMap,
                                             Bill bill, String proposalRef, byte[] xmlContent) {
        ExportOptions exportOptions = exportProposalResource.getExportOptions();
        contentToZip.put(bill.getName(), xmlContent);

        addAnnotateToZipContent(contentToZip, bill.getMetadata().get().getRef(), bill.getName(), exportOptions, proposalRef);
        if (exportOptions.getFileType().equals(Bill.class)) {
            addFilteredAnnotationsToZipContent(contentToZip, bill.getName(), exportOptions);
        }

        if(!exportOptions.isComparisonMode()) {
            addResourceToZipContent(contentToZip, billStyleSheet, STYLES_SOURCE_PATH, STYLE_DEST_DIR);
            structureContextProvider.get().useDocumentTemplate(bill.getMetadata().get().getDocTemplate());
            final String billTocJson = getTocAsJson(billService.getTableOfContent(bill, TocMode.SIMPLIFIED_CLEAN));
            final String coverPage = exportProposalResource.getComponentId(XmlNodeConfigProcessor.DOC_REF_COVER);
            addHtmlRendition(contentToZip, bill.getName(), xmlContent, billStyleSheet, billTocJson);
        }
        
        final ExportResource exportBillResource = buildExportResourceBill(proposalRefsMap, xmlContent);
        exportBillResource.setExportOptions(exportOptions);
        exportProposalResource.addChildResource(exportBillResource);
        return exportBillResource;
    }

    private byte[] getComparedContent(ExportOptions exportOptions) {
        ExportVersions exportVersions = exportOptions.getExportVersions();
        String resultContent;
        switch (exportOptions.getComparisonType()) {
            case DOUBLE:
                resultContent = doubleCompareXmlContents(exportVersions.getOriginal(), exportVersions.getIntermediate(),
                        exportVersions.getCurrent(), exportOptions.isDocuwrite());
                break;
            case SIMPLE:
                resultContent = simpleCompareXmlContents(exportVersions.getOriginal(), exportVersions.getCurrent(), exportOptions.isDocuwrite());
                break;
            default:
                throw new IllegalStateException("Shouldn't happen!!! ExportVersions: " + exportVersions);
        }
        return resultContent.getBytes(UTF_8);
    }

    public String doubleCompareXmlContents(XmlDocument originalVersion, XmlDocument intermediateMajor, XmlDocument current, boolean isDocuwrite) {
        byte[] currentXmlContent = current.getContent().get().getSource().getBytes();
        String originalXml = originalVersion.getContent().getOrError(() -> "Original document content is required!")
                .getSource().toString();
        String intermediateMajorXml = intermediateMajor.getContent().getOrError(() -> "Intermadiate Major Version document content is required!")
                .getSource().toString();
        String currentXml = new String(currentXmlContent, UTF_8);

        String diffResult =  compareService.compareContents(new ContentComparatorContext.Builder(originalXml, currentXml, intermediateMajorXml)   .withAttrName(ATTR_NAME)
                .withRemovedValue(DOUBLE_COMPARE_REMOVED_CLASS)
                .withAddedValue(DOUBLE_COMPARE_ADDED_CLASS)
                .withRemovedIntermediateValue(DOUBLE_COMPARE_REMOVED_CLASS + DOUBLE_COMPARE_INTERMEDIATE_STYLE)
                .withAddedIntermediateValue(DOUBLE_COMPARE_ADDED_CLASS + DOUBLE_COMPARE_INTERMEDIATE_STYLE)
                .withRemovedOriginalValue(DOUBLE_COMPARE_REMOVED_CLASS + DOUBLE_COMPARE_ORIGINAL_STYLE)
                .withAddedOriginalValue(DOUBLE_COMPARE_ADDED_CLASS + DOUBLE_COMPARE_ORIGINAL_STYLE)
                .withRetainOriginalValue(DOUBLE_COMPARE_RETAIN_CLASS + DOUBLE_COMPARE_ORIGINAL_STYLE)
                .withDisplayRemovedContentAsReadOnly(Boolean.TRUE)
                .withThreeWayDiff(true)
                .withDocuwriteExport(isDocuwrite)
                .build());

        PostDiffingProcessor postDiffingProcessor = new PostDiffingProcessor();
        diffResult = postDiffingProcessor.adjustSoftActionDoubleDiffing(diffResult);
        diffResult = postDiffingProcessor.adjustTagsDiffing(diffResult);
        return diffResult;
    }

    public String simpleCompareXmlContents(XmlDocument versionToCompare, XmlDocument currentXmlContent, boolean isDocuwrite) {
        String versionToCompareXml = versionToCompare.getContent().get().getSource().toString();
        String currentXmlContentXml = currentXmlContent.getContent().get().getSource().toString();

        String diffResult =  compareService.compareContents(new ContentComparatorContext.Builder(versionToCompareXml, currentXmlContentXml)
                .withAttrName(ATTR_NAME)
                .withRemovedValue((isDocuwrite) ? DOUBLE_COMPARE_REMOVED_CLASS : CONTENT_REMOVED_CLASS)
                .withAddedValue((isDocuwrite) ? DOUBLE_COMPARE_ADDED_CLASS : CONTENT_ADDED_CLASS)
                .withDisplayRemovedContentAsReadOnly(Boolean.TRUE)
                .withThreeWayDiff(false)
                .withDocuwriteExport(isDocuwrite)
                .build());
        PostDiffingProcessor postDiffingProcessor = new PostDiffingProcessor();
        diffResult = postDiffingProcessor.adjustSoftActionDiffing(diffResult);
        diffResult = postDiffingProcessor.adjustTagsDiffing(diffResult);
        diffResult = postDiffingProcessor.adjustSoftRootSubParagraph(diffResult);
        return diffResult;
    }

    private void enrichZipWithAnnex(final Map<String, Object> contentToZip, ExportResource exportBillResource,
                                    Annex annex, ExportOptions exportOptions, String resourceId, String href,
                                    String proposalRef) {
        byte[] xmlContent;
        if(exportOptions.isComparisonMode()){
            xmlContent = getComparedContent(exportOptions);
        } else if(exportOptions.isCleanVersion()){
            xmlContent = xmlContentProcessor.cleanSoftActions(annex.getContent().get().getSource().getBytes());
        } else {
            xmlContent = annex.getContent().get().getSource().getBytes();
            xmlContent = addMetadataToAnnex(annex, xmlContent);
        }
        if (exportOptions.isWithRelevantElements()) {
            structureContextProvider.get().useDocumentTemplate(annex.getMetadata().get().getDocTemplate());
            xmlContent = addRelevantElements(exportOptions, annex.getVersionLabel(), xmlContent);
            xmlContent = addCommentsMetadata(exportOptions.getComments(), xmlContent);
        }
        contentToZip.put(annex.getName(), xmlContent);
        
        addAnnotateToZipContent(contentToZip, annex.getMetadata().get().getRef(), annex.getName(), exportOptions, proposalRef);
        if (exportOptions.getFileType().equals(Annex.class)) {
            addFilteredAnnotationsToZipContent(contentToZip, annex.getName(), exportOptions);
        }

        if (!exportOptions.isComparisonMode()) {
            addResourceToZipContent(contentToZip, annexStyleSheet, STYLES_SOURCE_PATH, STYLE_DEST_DIR);
            structureContextProvider.get().useDocumentTemplate(annex.getMetadata().get().getDocTemplate());
            final String annexTocJson = getTocAsJson(annexService.getTableOfContent(annex, TocMode.SIMPLIFIED_CLEAN));
            addHtmlRendition(contentToZip, annex.getName(), xmlContent, annexStyleSheet, annexTocJson);
        }
        
        int docNumber = annex.getMetadata().get().getIndex();
        final ExportResource annexExportResource = buildExportResourceAnnex(docNumber, resourceId, href, xmlContent);
        exportBillResource.addChildResource(annexExportResource);
    }

    private void enrichZipWithExplanatory(final Map<String, Object> contentToZip, ExportResource exportBillResource,
                                    Explanatory explanatory, ExportOptions exportOptions, String resourceId, String href,
                                    String proposalRef) {
        byte[] xmlContent;
        if(exportOptions.isComparisonMode()){
            xmlContent = getComparedContent(exportOptions);
        } else if(exportOptions.isCleanVersion()){
            xmlContent = xmlContentProcessor.cleanSoftActions(explanatory.getContent().get().getSource().getBytes());
        } else {
            xmlContent = explanatory.getContent().get().getSource().getBytes();
            xmlContent = addMetadataToExplanatory(explanatory, xmlContent);
        }
        if (exportOptions.isWithRelevantElements()) {
            structureContextProvider.get().useDocumentTemplate(explanatory.getMetadata().get().getDocTemplate());
            xmlContent = addRelevantElements(exportOptions, explanatory.getVersionLabel(), xmlContent);
            xmlContent = addCommentsMetadata(exportOptions.getComments(), xmlContent);
        }
        contentToZip.put(explanatory.getName(), xmlContent);

        addAnnotateToZipContent(contentToZip, explanatory.getMetadata().get().getRef(), explanatory.getName(), exportOptions, proposalRef);
        if (exportOptions.getFileType().equals(Explanatory.class)) {
            addFilteredAnnotationsToZipContent(contentToZip, explanatory.getName(), exportOptions);
        }

        if (!exportOptions.isComparisonMode()) {
            addResourceToZipContent(contentToZip, explanatoryStyleSheet, STYLES_SOURCE_PATH, STYLE_DEST_DIR);
            structureContextProvider.get().useDocumentTemplate(explanatory.getMetadata().get().getDocTemplate());
            final String explanatoryTocJson = getTocAsJson(explanatoryService.getTableOfContent(explanatory, TocMode.SIMPLIFIED_CLEAN));
            addHtmlRendition(contentToZip, explanatory.getName(), xmlContent, explanatoryStyleSheet, explanatoryTocJson);
        }

        int docNumber = 0;
        final ExportResource explanatoryExportResource = buildExportResourceExplanatory(docNumber, resourceId, href, xmlContent);
        exportBillResource.addChildResource(explanatoryExportResource);
    }
    
    private List<TableOfContentItemHtmlVO> buildTocHtml(List<TableOfContentItemVO> tableOfContents) {
        List<TableOfContentItemHtmlVO> tocHtml = new ArrayList<>();
        for (TableOfContentItemVO item : tableOfContents) {
            String name = TableOfContentHelper.buildItemCaption(item, TableOfContentHelper.DEFAULT_CAPTION_MAX_SIZE, messageHelper);
            TableOfContentItemHtmlVO itemHtml = new TableOfContentItemHtmlVO(name, "#" + item.getId());
            if (item.getChildItems().size() > 0) {
                itemHtml.setChildren(buildTocHtml(item.getChildItems()));
            }
            tocHtml.add(itemHtml);
        }
        return tocHtml;
    }
    
    private String getTocAsJson(List<TableOfContentItemVO> tableOfContent) {
        final String json;
        try {
            List<TableOfContentItemHtmlVO> tocHtml = buildTocHtml(tableOfContent);
            json = new ObjectMapper().writeValueAsString(tocHtml);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Exception while converting 'tableOfContent' in json format.", e);
        }
        return json;
    }

    private Map<String, String> buildProposalExportResource(ExportResource exportResource, byte[] xmlContent) {
        Map<String, XmlNodeConfig> config = new HashMap<>();
        config.putAll(xmlNodeConfigProcessor.getProposalComponentsConfig(LeosCategory.MEMORANDUM, "xml:id"));
        config.putAll(xmlNodeConfigProcessor.getProposalComponentsConfig(LeosCategory.MEMORANDUM, "href"));
        config.putAll(xmlNodeConfigProcessor.getProposalComponentsConfig(LeosCategory.BILL, "xml:id"));
        config.putAll(xmlNodeConfigProcessor.getProposalComponentsConfig(LeosCategory.BILL, "href"));
        config.putAll(xmlNodeConfigProcessor.getProposalComponentsConfig(LeosCategory.COUNCIL_EXPLANATORY, "xml:id"));
        config.putAll(xmlNodeConfigProcessor.getProposalComponentsConfig(LeosCategory.COUNCIL_EXPLANATORY, "href"));
        config.putAll(xmlNodeConfigProcessor.getConfig(LeosCategory.PROPOSAL));

        Map<String, String> proposalRefsMap = xmlNodeProcessor.getValuesFromXml(xmlContent,
                new String[]{XmlNodeConfigProcessor.PROPOSAL_DOC_COLLECTION, XmlNodeConfigProcessor.DOC_REF_COVER,
                        LeosCategory.MEMORANDUM.name() + "_xml:id",
                        LeosCategory.MEMORANDUM.name() + "_href",
                        LeosCategory.BILL.name() + "_xml:id",
                        LeosCategory.BILL.name() + "_href"
                },
                config);
        
        exportResource.setResourceId(proposalRefsMap.get(XmlNodeConfigProcessor.PROPOSAL_DOC_COLLECTION));
        exportResource.setComponentsIdsMap(Collections.singletonMap(XmlNodeConfigProcessor.DOC_REF_COVER,
                proposalRefsMap.get(XmlNodeConfigProcessor.DOC_REF_COVER)));
        return proposalRefsMap;
    }
    
    private ExportResource buildExportResourceMemorandum(Map<String, String> proposalRefsMap, byte[] xmlContent) {
        ExportResource memorandumExportResource = new ExportResource(LeosCategory.MEMORANDUM);
        memorandumExportResource.setResourceId(proposalRefsMap.get(LeosCategory.MEMORANDUM.name() + "_xml:id"));
        memorandumExportResource.setHref(proposalRefsMap.get(LeosCategory.MEMORANDUM.name() + "_href"));
        memorandumExportResource.setComponentsIdsMap(getCoverPage(LeosCategory.MEMORANDUM, xmlContent));
        return memorandumExportResource;
    }

    private ExportResource buildExportResourceExplanatory(int docNumber, String resourceId, String href, byte[] xmlContent) {
        ExportResource explanatoryExportResource = new ExportResource(LeosCategory.COUNCIL_EXPLANATORY);
        explanatoryExportResource.setResourceId(resourceId);
        explanatoryExportResource.setHref(href);
        explanatoryExportResource.setDocNumber(docNumber);
        return explanatoryExportResource;
    }

    private ExportResource buildExportResourceExplanatory(int docNumber, String resourceId, byte[] xmlContent) {
        return buildExportResourceExplanatory(docNumber, resourceId, null, xmlContent);
    }
    
    private ExportResource buildExportResourceBill(Map<String, String> proposalRefsMap, byte[] xmlContent) {
        ExportResource billExportResource = new ExportResource(LeosCategory.BILL);
        billExportResource.setResourceId(proposalRefsMap.get(LeosCategory.BILL.name() + "_xml:id"));
        billExportResource.setHref(proposalRefsMap.get(LeosCategory.BILL.name() + "_href"));
        billExportResource.setComponentsIdsMap(getCoverPage(LeosCategory.BILL, xmlContent));
        return billExportResource;
    }
    
    private ExportResource buildExportResourceAnnex(int docNumber, String resourceId, String href, byte[] xmlContent) {
        ExportResource annexExportResource = new ExportResource(LeosCategory.ANNEX);
        annexExportResource.setResourceId(resourceId);
        annexExportResource.setHref(href);
        annexExportResource.setDocNumber(docNumber);
        annexExportResource.setComponentsIdsMap(getCoverPage(LeosCategory.ANNEX, xmlContent));
        return annexExportResource;
    }

    private ExportResource buildExportResourceAnnex(int docNumber, String resourceId, byte[] xmlContent) {
        //TODO : FIXME : populate href for Proposal export
        return buildExportResourceAnnex(docNumber, resourceId, null, xmlContent);
    }

    private  Map<String, String> getCoverPage(LeosCategory leosCategory, byte[] xmlContent) {
        Map<String, XmlNodeConfig> config = new HashMap<>();
        config.putAll(xmlNodeConfigProcessor.getConfig(leosCategory));

        Map<String, String> refsMap = xmlNodeProcessor.getValuesFromXml(xmlContent,
                new String[]{XmlNodeConfigProcessor.DOC_REF_COVER}, config);

        return Collections.singletonMap(XmlNodeConfigProcessor.DOC_REF_COVER,
                refsMap.get(XmlNodeConfigProcessor.DOC_REF_COVER));
    }
    
    private void enrichZipWithMedia(final Map<String, Object> contentToZip, LeosPackage leosPackage) {
        final List<MediaDocument> mediaDocs = packageRepository.findDocumentsByPackagePath(leosPackage.getPath(), MediaDocument.class, true);
        for (MediaDocument mediaDoc : mediaDocs) {
            byte[] byteContent = mediaDoc.getContent().getOrError(() -> "Document content is required!").getSource().getBytes();
            contentToZip.put(MEDIA_DIR + mediaDoc.getName(), byteContent);
        }
    }
    
    @Override
    public LegDocument createLegDocument(String proposalId, String jobId, LegPackage legPackage, LeosLegStatus status) throws IOException {
        LOG.trace("Creating Leg Document for Package... [documentId={}]", proposalId);
        return packageRepository.createLegDocumentFromContent(packageRepository.findPackageByDocumentId(proposalId).getPath(), generateLegName(proposalId),
                jobId, legPackage.getMilestoneComments(), getFileContent(legPackage.getFile()), status, legPackage.getContainedFiles());
    }

    @Override
    public LegDocument updateLegDocument(String id, byte[] content) {
        return packageRepository.updateLegDocument(id, LeosLegStatus.FILE_READY, content, VersionType.INTERMEDIATE,
                "Milestone is updated");
    }

    @Override
    public LegDocument updateLegDocument(String id, LeosLegStatus status) {
        LOG.trace("Updating Leg document status... [id={}, status={}]", id, status.name());
        return packageRepository.updateLegDocument(id, status);
    }
    
    @Override
    public LegDocument updateLegDocument(String id, byte[] pdfJobZip, byte[] wordJobZip) {
        LOG.trace("Updating Leg document with id={} status to {} and content with pdf and word renditions", id, LeosLegStatus.FILE_READY.name());
        LegDocument document = findLegDocumentById(id);
        try {
            byte[] content = updateContentWithPdfAndWordRenditions(pdfJobZip, wordJobZip, document.getContent().getOrNull());
            return packageRepository.updateLegDocument(document.getId(), LeosLegStatus.FILE_READY, content, VersionType.INTERMEDIATE, "Milestone is now validated");
        } catch (Exception e) {
            LOG.error("Error while updating the content of the Leg Document with id=" + id, e);
            return packageRepository.updateLegDocument(document.getId(), LeosLegStatus.FILE_ERROR);
        }
    }
    
    @Override
    public LegDocument findLegDocumentById(String id) {
        LOG.trace("Finding Leg Document by id... [documentId={}]", id);
        return packageRepository.findLegDocumentById(id, true);
    }
    
    @Override
    public LegDocument findLegDocumentByAnyDocumentIdAndJobId(String documentId, String jobId) {
        LOG.trace("Finding Leg Document by proposal id and job id... [proposalId={}, jobId={}]", documentId, jobId);
        LeosPackage leosPackage = packageRepository.findPackageByDocumentId(documentId);
        List<LegDocument> legDocuments = packageRepository.findDocumentsByPackageId(leosPackage.getId(), LegDocument.class, false, false);
        return legDocuments.stream()
                .filter(legDocument -> jobId.equals(legDocument.getJobId()))
                .findAny()
                .orElse(null);
    }
    
    @Override
    public List<LegDocument> findLegDocumentByStatus(LeosLegStatus leosLegStatus) {
        return packageRepository.findDocumentsByStatus(leosLegStatus, LegDocument.class);
    }
    
    @Override
    public List<LegDocument> findLegDocumentByProposal(String proposalId) {
        LeosPackage leosPackage = packageRepository.findPackageByDocumentId(proposalId);
        return packageRepository.findDocumentsByPackageId(leosPackage.getId(), LegDocument.class, false, false);
    }
    
    private byte[] updateContentWithPdfAndWordRenditions(byte[] pdfJobZip, byte[] wordJobZip, Content content) throws IOException {
        Map<String, Object> legContent = ZipPackageUtil.unzipByteArray(content.getSource().getBytes());
        addPdfRendition(pdfJobZip, legContent);
        addWordRenditions(wordJobZip, legContent);
        return ZipPackageUtil.zipByteArray(legContent);
    }
    
    private void addPdfRendition(byte[] pdfJobZip, Map<String, Object> legContent) throws IOException {
        Map.Entry<String, Object> neededEntry = unzipJobResult(pdfJobZip).entrySet().stream()
                .filter(pdfEntry -> !pdfEntry.getKey().endsWith("_pdfa.pdf"))
                .findAny()
                .orElseThrow(() -> new FileNotFoundException("Pdf rendition not found in the pdf document job file"));
        legContent.put(PDF_RENDITION + neededEntry.getKey(), neededEntry.getValue());
    }
    
    private void addWordRenditions(byte[] wordJobZip, Map<String, Object> legContent) throws IOException {
        List<String> wordEntries = new ArrayList<>();
        unzipJobResult(wordJobZip).entrySet().stream()
                .filter(wordEntity -> !wordEntity.getKey().isEmpty())
                .forEach(wordEntry -> {
                    legContent.put(WORD_RENDITION + wordEntry.getKey(), wordEntry.getValue());
                    wordEntries.add(wordEntry.getKey());
                });
        if (wordEntries.isEmpty()) {
            throw new FileNotFoundException("No word rendition found in the word document job file");
        }
    }
    
    private Map<String, Object> unzipJobResult(byte[] jobZip) throws IOException {
        Map<String, Object> jobContent = ZipPackageUtil.unzipByteArray(jobZip);
        for (Map.Entry<String, Object> entry : jobContent.entrySet()) {
            if (entry.getKey().endsWith("_out.zip")) {
                return ZipPackageUtil.unzipByteArray((byte[]) entry.getValue());
            }
        }
        throw new FileNotFoundException("The job result zip file is not present in the job file");
    }
    
    private byte[] getFileContent(File file) throws IOException {
        try (InputStream is = new FileInputStream(file)) {
            byte[] content = new byte[(int) file.length()];
            is.read(content);
            return content;
        }
    }

    private void addCoverPageHtmlRendition(Map<String, Object> contentToZip, byte[] proposalContent, String styleSheetName, Proposal proposal) {
        if(!documentContentService.isCoverPageExists(proposalContent)) {
            // No cover page to add
            return;
        }
        byte[] coverPageContent = xmlContentProcessor.getCoverPageContentForRendition(proposalContent);
        addResourceToZipContent(contentToZip, coverPageStyleSheet, STYLES_SOURCE_PATH, STYLE_DEST_DIR);
        try {
            RenderedDocument htmlDocument = new RenderedDocument();
            htmlDocument.setContent(new ByteArrayInputStream(coverPageContent));
            htmlDocument.setStyleSheetName(styleSheetName);
            String htmlName = HTML_RENDITION + XmlNodeConfigProcessor.DOC_REF_COVER + ".html";
            contentToZip.put(htmlName, htmlRenditionProcessor.processCoverPage(htmlDocument).getBytes("UTF-8"));

            structureContextProvider.get().useDocumentTemplate(proposal.getMetadata().get().getDocTemplate());
            final String coverPageTocJson = getTocAsJson(proposalService.getCoverPageTableOfContent(proposal, TocMode.SIMPLIFIED_CLEAN));
            String xmlDocumentName = XmlNodeConfigProcessor.DOC_REF_COVER + ".xml";
            addCoverPageTocHtmlRendition(contentToZip, styleSheetName, coverPageContent, coverPageTocJson, xmlDocumentName);
        } catch (UnsupportedEncodingException exception) {
            throw new IllegalStateException("UnsupportedEncodingException while processing cover page ", exception);
        }
    }

    private void addCoverPageTocHtmlRendition(Map<String, Object> contentToZip, String styleSheetName, byte[] coverPageContent, String coverPageTocJson, String xmlDocumentName) {
        try {
            // Build toc_docName.js file
            RenderedDocument tocHtmlDocument = new RenderedDocument();
            tocHtmlDocument.setContent(new ByteArrayInputStream(coverPageContent));
            tocHtmlDocument.setStyleSheetName(styleSheetName);
            final String tocJsName = xmlDocumentName.substring(0, xmlDocumentName.indexOf(".xml")) + "_toc" + ".js";
            final String tocJsFile = JS_DEST_DIR + tocJsName;
            contentToZip.put(tocJsFile, htmlRenditionProcessor.processJsTemplate(coverPageTocJson).getBytes("UTF-8"));

            //build html_docName_toc.html
            tocHtmlDocument = new RenderedDocument();
            tocHtmlDocument.setContent(new ByteArrayInputStream(coverPageContent));
            tocHtmlDocument.setStyleSheetName(styleSheetName);
            String tocHtmlFile = HTML_RENDITION + xmlDocumentName;
            tocHtmlFile = tocHtmlFile.substring(0, tocHtmlFile.indexOf(".xml")) + "_toc" + ".html";
            contentToZip.put(tocHtmlFile, htmlRenditionProcessor.processCoverPageTocTemplate(tocHtmlDocument, tocJsName).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException exception) {
            throw new IllegalStateException("UnsupportedEncodingException while processing document " + xmlDocumentName, exception);
        }
    }

    private void addHtmlRendition(Map<String, Object> contentToZip, String xmlDocumentName, byte[] xmlContent, String styleSheetName, String tocJson) {
        try {
            RenderedDocument htmlDocument = new RenderedDocument();
            htmlDocument.setContent(new ByteArrayInputStream(xmlContent));
            htmlDocument.setStyleSheetName(styleSheetName);
            String htmlName = HTML_RENDITION + xmlDocumentName.replaceAll(".xml", ".html");
            contentToZip.put(htmlName, htmlRenditionProcessor.processTemplate(htmlDocument).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException exception) {
            throw new IllegalStateException("UnsupportedEncodingException while processing document " + xmlDocumentName, exception);
        }
        
        try {
            // Build toc_docName.js file
            RenderedDocument tocHtmlDocument = new RenderedDocument();
            tocHtmlDocument.setContent(new ByteArrayInputStream(xmlContent));
            tocHtmlDocument.setStyleSheetName(styleSheetName);
            final String tocJsName = xmlDocumentName.substring(0, xmlDocumentName.indexOf(".xml")) + "_toc" + ".js";
            final String tocJsFile = JS_DEST_DIR + tocJsName;
            contentToZip.put(tocJsFile, htmlRenditionProcessor.processJsTemplate(tocJson).getBytes("UTF-8"));
            
            //build html_docName_toc.html
            tocHtmlDocument = new RenderedDocument();
            tocHtmlDocument.setContent(new ByteArrayInputStream(xmlContent));
            tocHtmlDocument.setStyleSheetName(styleSheetName);
            String tocHtmlFile = HTML_RENDITION + xmlDocumentName;
            tocHtmlFile = tocHtmlFile.substring(0, tocHtmlFile.indexOf(".xml")) + "_toc" + ".html";
            contentToZip.put(tocHtmlFile, htmlRenditionProcessor.processTocTemplate(tocHtmlDocument, tocJsName).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException exception) {
            throw new IllegalStateException("UnsupportedEncodingException while processing document " + xmlDocumentName, exception);
        }
    }
    
    /**
     * Add resource to exported .leg file in renditions/html/css or js folder
     */
    private void addResourceToZipContent(Map<String, Object> contentToZip, String resourceName, String sourcePath, String destPath) {
        try {
            Resource resource = new ClassPathResource(sourcePath + resourceName);
            contentToZip.put(destPath + resourceName, IOUtils.toByteArray(resource.getInputStream()));
        } catch (IOException io) {
            LOG.error("Error occurred while getting styles ", io);
        }
    }
    
    /**
     * Calls service to get Annotations per document
     */
    private void addAnnotateToZipContent(Map<String, Object> contentToZip, String ref, String docName, ExportOptions exportOptions, String proposalRef) {
        if (exportOptions.isWithAnnotations()) {
            try {
                String annotations = annotateService.getAnnotations(ref, proposalRef);
                annotations = processAnnotations(annotations, exportOptions);
                final byte[] xmlAnnotationContent = annotations.getBytes(UTF_8);
                contentToZip.put(creatAnnotationFileName(docName), xmlAnnotationContent);
            } catch(Exception e) {
                LOG.error("Exception occurred", e);
            }
        }
    }

    public void addFilteredAnnotationsToZipContent(Map<String, Object> contentToZip, String docName, ExportOptions exportOptions) {
        if (exportOptions.isWithFilteredAnnotations()) {
            try {
                String annotations = exportOptions.getFilteredAnnotations();
                annotations = processAnnotations(annotations, exportOptions);
                final byte[] xmlAnnotationContent = annotations.getBytes(UTF_8);
                contentToZip.put(creatAnnotationFileName(docName), xmlAnnotationContent);
            } catch(Exception e) {
                LOG.error("Exception occurred", e);
            }
        }
    }

    private String creatAnnotationFileName(String docName) {
        return MEDIA_DIR + ANNOT_FILE_PREFIX + docName + ANNOT_FILE_EXT;
    }
    
    private void setComponentsRefs(LeosCategory leosCategory, final ExportResource exportResource, byte[] xmlContent) {
        Map<String, String> componentMap = xmlNodeProcessor.getValuesFromXml(xmlContent,
                new String[]{XmlNodeConfigProcessor.DOC_REF_COVER},
                xmlNodeConfigProcessor.getConfig(leosCategory));
        exportResource.setComponentsIdsMap(componentMap);
    }
    
    private byte[] getContent(XmlDocument xmlDocument) {
        final Content content = xmlDocument.getContent().getOrError(() -> "xml content is required!");
        return content.getSource().getBytes();
    }

    private byte[] addRelevantElements(ExportOptions exportOptions, String currentVersion, byte[] xmlContent) {
        final List<String> rootElements = structureContextProvider.get().getTocItems().stream().filter(x -> x.isRoot() && x.getProfiles() == null).map(x -> x.getAknTag().value()).collect(Collectors.toList());
        final List<Element> relevantXmlElements = getRelevantElementsFromXml(exportOptions.getRelevantElements(), rootElements, xmlContent);
        xmlContent = addRelevantElementsMetadata(exportOptions, relevantXmlElements, currentVersion, xmlContent);
        xmlContent = xmlContentProcessor.ignoreNotSelectedElements(xmlContent, rootElements, relevantXmlElements.stream().map(x -> x.getElementId()).collect(Collectors.toList()));
        return xmlContent;
    }

    private List<Element> getRelevantElementsFromXml(RelevantElements relevantElements, List<String> rootElements, byte[] xmlContent) {
        List<Element> relevantXmlElements;
        switch (relevantElements) {
            case RECITALS:
                relevantXmlElements = xmlContentProcessor.getElementsByTagName(xmlContent, Arrays.asList(XmlHelper.RECITALS), false);
                break;
            case ENACTING_TERMS:
                relevantXmlElements = xmlContentProcessor.getElementsByTagName(xmlContent, Arrays.asList(XmlHelper.BODY), false);
                break;
            case RECITALS_AND_ENACTING_TERMS:
                relevantXmlElements = xmlContentProcessor.getElementsByTagName(xmlContent, Arrays.asList(XmlHelper.RECITALS, XmlHelper.BODY), false);
                break;
            case ALL:
                relevantXmlElements = xmlContentProcessor.getElementsByTagName(xmlContent, rootElements, false);
                break;
            default:
                throw new IllegalArgumentException("No supported element " + relevantElements);
        }
        return relevantXmlElements;
    }

    private byte[] addRelevantElementsMetadata(ExportOptions exportOptions, List<Element> relevantXmlElements, String currentVersion, byte[] xmlContent) {
        StringBuilder relevantElementsBuilder = new StringBuilder("<leos:relevantElements");
        if (exportOptions.isComparisonMode()) {
            relevantElementsBuilder.append((exportOptions.getExportVersions().getOriginal() != null) ? " leos:originalVersion=\"" + exportOptions.getExportVersions().getOriginal().getVersionLabel() + "\"" : "");
            relevantElementsBuilder.append((exportOptions.getExportVersions().getIntermediate() != null) ? " leos:intermediateVersion=\"" + exportOptions.getExportVersions().getIntermediate().getVersionLabel() + "\"" : "");
            relevantElementsBuilder.append((exportOptions.getExportVersions().getCurrent() != null) ? " leos:currentVersion=\"" + exportOptions.getExportVersions().getCurrent().getVersionLabel() + "\"" : "");
        } else {
            relevantElementsBuilder.append(" leos:currentVersion=\"" + currentVersion + "\"");
        }
        relevantElementsBuilder.append(">");
        relevantXmlElements.forEach(element -> {
            relevantElementsBuilder.append("<leos:relevantElement leos:ref=\"" + element.getElementId() + "\" leos:tagName=\"" + element.getElementTagName() + "\"/>");
        });
        relevantElementsBuilder.append("</leos:relevantElements>");
        return xmlContentProcessor.insertElement(xmlContent, xPathCatalog.getXPathDocTemplate(), true, relevantElementsBuilder.toString());
    }

    @Override
    public byte[] updateLegPackageContentWithComments(byte[] legPackageContent, List<String> comments) throws IOException {
        File legPackageZipFile = null;
        try {
            Map<String, Object> legPackageZipContent = ZipPackageUtil.unzipByteArray(legPackageContent);
            Map.Entry<String, Object> legPackageXmlDocument = legPackageZipContent.entrySet().stream()
                    .filter(x -> x.getKey().startsWith(LeosCategory.BILL.name().toLowerCase()) || x.getKey().startsWith(LeosCategory.ANNEX.name().toLowerCase()))
                    .findAny().orElseThrow(() -> new RuntimeException("No document file inside leg package!"));
            byte[] xmlContentUpdated = replaceCommentsMetadata(comments, (byte[])legPackageXmlDocument.getValue());
            legPackageZipContent.put(legPackageXmlDocument.getKey(), xmlContentUpdated);
            legPackageZipFile = ZipPackageUtil.zipFiles(System.currentTimeMillis() + ".zip", legPackageZipContent, "");
            return FileUtils.readFileToByteArray(legPackageZipFile);
        } finally {
            if ((legPackageZipFile != null) && (legPackageZipFile.exists())) {
                legPackageZipFile.delete();
            }
        }
    }

    private byte[] addCommentsMetadata(List<String> comments, byte[] xmlContent) {
        StringBuilder commentsBuilder = new StringBuilder("<leos:comments>");
        comments.forEach(comment -> commentsBuilder.append("<leos:comment><![CDATA[" + comment + "]]></leos:comment>"));
        commentsBuilder.append("</leos:comments>");
        return xmlContentProcessor.insertElement(xmlContent, xPathCatalog.getXPathRelevantElements(), true, commentsBuilder.toString());
    }

    private byte[] replaceCommentsMetadata(List<String> comments, byte[] xmlContent) {
        StringBuilder commentsBuilder = new StringBuilder("<leos:comments>");
        comments.forEach(comment -> commentsBuilder.append("<leos:comment><![CDATA[" + comment + "]]></leos:comment>"));
        commentsBuilder.append("</leos:comments>");
        return xmlContentProcessor.replaceElement(xmlContent, xPathCatalog.getXPathComments(), true, commentsBuilder.toString());
    }

    @Override
    public LegPackage createLegPackageForClone(String proposalId, ExportOptions exportOptions) throws IOException {
        LOG.trace("Creating Leg Package for cloned proposal... [documentId={}]", proposalId);
        final LegPackage legPackage = new LegPackage();
        final LeosPackage leosPackage = packageRepository.findPackageByDocumentId(proposalId);
        final Map<String, Object> contentToZip = new HashMap<>();
        final ExportResource exportProposalResource = new ExportResource(LeosCategory.PROPOSAL);
        exportProposalResource.setExportOptions(exportOptions);

        //1. Add Proposal to package
        final Proposal proposal = workspaceRepository.findDocumentById(proposalId, Proposal.class, true);
        final Map<String, String> proposalRefsMap = enrichZipWithProposalForClone(contentToZip, exportProposalResource, proposal);
        byte[] proposalContent = proposal.getContent().get().getSource().getBytes();
        String language = proposal.getMetadata().get().getLanguage();
        //2. Add Bill to pacakge

        //2. Add Bill to package
        Bill bill = packageRepository.findDocumentByPackagePathAndName(leosPackage.getPath(),
                proposalRefsMap.get(LeosCategory.BILL.name() + "_href"), Bill.class);

        byte[] xmlContent = bill.getContent().get().getSource().getBytes();
        if(exportOptions.isComparisonMode()) {
            XmlDocument originalBill = documentContentService.getOriginalBill(bill);
            xmlContent = simpleCompareXmlContentsForClone(originalBill, bill).getBytes(UTF_8);
        } else if (exportOptions.isCleanVersion()) {
            xmlContent = xmlContentProcessor.cleanSoftActions(xmlContent);
        }

        xmlContent = addMetadataToBill(bill, xmlContent);

        ExportResource exportBillResource = enrichZipWithBillForClone(contentToZip, exportProposalResource,
                proposalRefsMap, bill, proposal.getMetadata().getOrNull().getRef(), xmlContent);

        //3. Add annex to package
        byte[] billXmlContent = bill.getContent().get().getSource().getBytes();
        addAnnexToPackageForClone(leosPackage, contentToZip, exportOptions, exportBillResource, legPackage,
                proposal.getMetadata().getOrNull().getRef(), billXmlContent);

        //4. Add memorandum to package
        addMemorandumToPackageForClone(leosPackage, contentToZip, exportProposalResource, proposalRefsMap, legPackage,
                proposal.getMetadata().getOrNull().getRef());

        //5. Add toc and media
        enrichZipWithToc(contentToZip);
        enrichZipWithMedia(contentToZip, leosPackage);

        //6. Add Cover page rendition
        addCoverPageHtmlRendition(contentToZip, proposalContent, coverPageStyleSheet, proposal);

        legPackage.setFile(ZipPackageUtil.zipFiles(proposalRefsMap.get(XmlNodeConfigProcessor.PROPOSAL_DOC_COLLECTION) + ".leg",
                contentToZip, language));
        legPackage.addContainedFile(bill.getVersionedReference());
        legPackage.setExportResource(exportProposalResource);
        return legPackage;
    }

    private String simpleCompareXmlContentsForClone(XmlDocument originalContent, XmlDocument currentContent) {
        String originalCompareXml = originalContent.getContent().get().getSource().toString();
        String currentCompareXml = currentContent.getContent().get().getSource().toString();

        return compareService.compareContents(new ContentComparatorContext.Builder(originalCompareXml, currentCompareXml)
                .withAttrName(ATTR_NAME)
                .withRemovedValue(CONTENT_REMOVED_CLASS)
                .withAddedValue(CONTENT_ADDED_CLASS)
                .withDisplayRemovedContentAsReadOnly(Boolean.TRUE)
                .build());
    }

    private ExportResource enrichZipWithBillForClone(final Map<String, Object> contentToZip, ExportResource exportProposalResource,
                                                     Map<String, String> proposalRefsMap, Bill bill, String proposalRef,
                                                     byte[] xmlContent) {
        ExportOptions exportOptions = exportProposalResource.getExportOptions();
        contentToZip.put(bill.getName(), xmlContent);
        if (exportOptions.isWithAnnotations()) {
            addAnnotateToZipContentForClone(contentToZip, bill.getMetadata().get().getRef(), bill.getName(), proposalRef, exportOptions);
            if (exportOptions.getFileType().equals(Bill.class)) {
                addFilteredAnnotationsToZipContent(contentToZip, bill.getName(), exportOptions);
            }
        }

        addResourceToZipContent(contentToZip, billStyleSheet, STYLES_SOURCE_PATH, STYLE_DEST_DIR);
        structureContextProvider.get().useDocumentTemplate(bill.getMetadata().get().getDocTemplate());
        final String billTocJson = getTocAsJson(billService.getTableOfContent(bill, TocMode.SIMPLIFIED_CLEAN));
        addHtmlRendition(contentToZip, bill.getName(), xmlContent, billStyleSheet, billTocJson);

        final ExportResource exportBillResource = buildExportResourceBill(proposalRefsMap, xmlContent);
        exportBillResource.setExportOptions(exportOptions);
        exportProposalResource.addChildResource(exportBillResource);
        return exportBillResource;
    }

    private void addAnnexToPackageForClone(final LeosPackage leosPackage, final Map<String, Object> contentToZip,
                                   ExportOptions exportOptions, ExportResource exportProposalResource, LegPackage legPackage,
                                   String proposalRef, byte[] xmlContent) {

        final Map<String, String> attachmentIds = attachmentProcessor.getAttachmentsIdFromBill(xmlContent);
        if (!attachmentIds.isEmpty()) {
            for (Map.Entry<String, String> entry : attachmentIds.entrySet()) {
                String href = entry.getKey();
                String id = entry.getValue();
                Annex annex = packageRepository.findDocumentByPackagePathAndName(leosPackage.getPath(), href, Annex.class);;

                enrichZipWithAnnexForClone(contentToZip, exportProposalResource, annex, exportOptions, id, href, proposalRef);
                legPackage.addContainedFile(annex.getVersionedReference());
            }
            addResourceToZipContent(contentToZip, annexStyleSheet, STYLES_SOURCE_PATH, STYLE_DEST_DIR);
        }
    }

    private void enrichZipWithAnnexForClone(final Map<String, Object> contentToZip, ExportResource exportBillResource,
                                    Annex annex, ExportOptions exportOptions, String resourceId, String href,
                                    String proposalRef) {
        byte[] xmlContent = annex.getContent().get().getSource().getBytes();
        if(exportOptions.isComparisonMode()) {
            XmlDocument originalAnnex = documentContentService.getOriginalAnnex(annex);
            xmlContent = simpleCompareXmlContentsForClone(originalAnnex, annex).getBytes(UTF_8);
        } else if (exportOptions.isCleanVersion()) {
            xmlContent = xmlContentProcessor.cleanSoftActions(xmlContent);
        }
        xmlContent = addMetadataToAnnex(annex, xmlContent);
        contentToZip.put(annex.getName(), xmlContent);
        if(exportOptions.isWithAnnotations()) {
            addAnnotateToZipContentForClone(contentToZip, annex.getMetadata().get().getRef(), annex.getName(), proposalRef, exportOptions);
            if (exportOptions.getFileType().equals(Annex.class)) {
                addFilteredAnnotationsToZipContent(contentToZip, annex.getName(), exportOptions);
            }
        }

        addResourceToZipContent(contentToZip, annexStyleSheet, STYLES_SOURCE_PATH, STYLE_DEST_DIR);
        structureContextProvider.get().useDocumentTemplate(annex.getMetadata().get().getDocTemplate());
        final String annexTocJson = getTocAsJson(annexService.getTableOfContent(annex, TocMode.SIMPLIFIED_CLEAN));
        addHtmlRendition(contentToZip, annex.getName(), xmlContent, annexStyleSheet, annexTocJson);

        int docNumber = annex.getMetadata().get().getIndex();
        final ExportResource annexExportResource = buildExportResourceAnnex(docNumber, resourceId, href, xmlContent);
        exportBillResource.addChildResource(annexExportResource);
    }

    private void addMemorandumToPackageForClone(final LeosPackage leosPackage, final Map<String, Object> contentToZip,
                                        ExportResource exportProposalResource, final Map<String, String> proposalRefsMap,
                                        LegPackage legPackage, String proposalRef) {
        final Memorandum memorandum = packageRepository.findDocumentByPackagePathAndName(leosPackage.getPath(), proposalRefsMap.get(LeosCategory.MEMORANDUM.name() + "_href"), Memorandum.class);
        enrichZipWithMemorandumForClone(contentToZip, exportProposalResource, proposalRefsMap, memorandum, proposalRef);
        legPackage.addContainedFile(memorandum.getVersionedReference());
    }

    private void enrichZipWithMemorandumForClone(final Map<String, Object> contentToZip, ExportResource exportProposalResource,
                                         Map<String, String> proposalRefsMap, Memorandum memorandum,
                                         String proposalRef) {
        ExportOptions exportOptions = exportProposalResource.getExportOptions();

        byte[] xmlContent = memorandum.getContent().get().getSource().getBytes();
        if(exportOptions.isComparisonMode()) {
            XmlDocument originalMemorandum= documentContentService.getOriginalMemorandum(memorandum);
            xmlContent = simpleCompareXmlContentsForClone(originalMemorandum, memorandum).getBytes(UTF_8);
        } else if (exportOptions.isCleanVersion()) {
            xmlContent = xmlContentProcessor.cleanSoftActions(xmlContent);
        }

        xmlContent = addMetadataToMemorandum(memorandum, xmlContent);
        contentToZip.put(memorandum.getName(), xmlContent);
        if (exportOptions.isWithAnnotations()) {
            addAnnotateToZipContentForClone(contentToZip, memorandum.getMetadata().get().getRef(), memorandum.getName(), proposalRef, exportOptions);
            if (exportOptions.getFileType().equals(Memorandum.class)) {
                addFilteredAnnotationsToZipContent(contentToZip, memorandum.getName(), exportOptions);
            }
        }

        addResourceToZipContent(contentToZip, memoStyleSheet, STYLES_SOURCE_PATH, STYLE_DEST_DIR);
        structureContextProvider.get().useDocumentTemplate(memorandum.getMetadata().get().getDocTemplate());
        final String memoTocJson = getTocAsJson(memorandumService.getTableOfContent(memorandum, TocMode.SIMPLIFIED_CLEAN));
        addHtmlRendition(contentToZip, memorandum.getName(), xmlContent, memoStyleSheet, memoTocJson);

        final ExportResource memorandumExportResource = buildExportResourceMemorandum(proposalRefsMap, xmlContent);
        exportProposalResource.addChildResource(memorandumExportResource);
    }

    private void addAnnotateToZipContentForClone(Map<String, Object> contentToZip, String ref, String docName, String proposalRef, ExportOptions exportOptions) {
        try {
            String annotations = annotateService.getAnnotations(ref, proposalRef);
            annotations = processAnnotations(annotations, exportOptions);
            final byte[] xmlAnnotationContent = annotations.getBytes(UTF_8);
            contentToZip.put(creatAnnotationFileName(docName), xmlAnnotationContent);
        } catch (Exception e) {
            LOG.error("Exception occurred while adding annotations to leg file", e);
        }
    }

    private String processAnnotations(String annotations, ExportOptions exportOptions) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(annotations);
        JsonNode rootNode = json.get("rows");
        Iterator<JsonNode> itr = rootNode.elements();
        List<JsonNode> modifiedList = new ArrayList<JsonNode>();
        itr.forEachRemaining(node -> {
            if (!exportOptions.isWithSuggestions() && node.findValue("tags").get(0).textValue().equalsIgnoreCase(SUGGESTION)) {
                // Skip suggestions
                return;
            }

            String entityText = "";
            JsonNode userInfo = node.get("user_info");
            if (userInfo != null && exportOptions.isWithAnonymization()) {
                JsonNode entityName = userInfo.get("entity_name");
                entityText = entityName.textValue();
                int firstEntitySeparator = entityText.indexOf(".");
                entityText = firstEntitySeparator >= 0 ? entityText.substring(0, firstEntitySeparator) : entityText;
                ((ObjectNode) userInfo).put("display_name", entityText);
                ((ObjectNode) userInfo).put("entity_name", entityText);
                ((ObjectNode) node).put("user", entityText);
                JsonNode permissions = node.get("permissions");
                if(permissions != null) {
                    anonymizePermission(permissions,"admin", entityText);
                    anonymizePermission(permissions,"update", entityText);
                    anonymizePermission(permissions,"delete", entityText);
                }
            }
            modifiedList.add(node);

        });
        ((ObjectNode) json).putArray("rows").removeAll().addAll(modifiedList);
        ((ObjectNode) json).put("total", modifiedList.size());
        return mapper.writeValueAsString(json);
    }

    private void anonymizePermission(JsonNode permissions, String permissionName, String entityText) {
        try {
            ArrayNode arrayNode = ((ArrayNode) (permissions.get(permissionName)));
            if(arrayNode!=null) {
                arrayNode.remove(0);
                arrayNode.add(entityText);
            }
        } catch (Exception e) {
            LOG.error("Error while anonymize a permission with name" + permissionName, e);
        }
    }
}
