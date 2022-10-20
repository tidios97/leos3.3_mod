package eu.europa.ec.leos.services.document;

import eu.europa.ec.leos.cmis.mapping.CmisProperties;
import eu.europa.ec.leos.domain.cmis.LeosLegStatus;
import eu.europa.ec.leos.domain.cmis.LeosPackage;
import eu.europa.ec.leos.domain.cmis.document.Annex;
import eu.europa.ec.leos.domain.cmis.document.Bill;
import eu.europa.ec.leos.domain.cmis.document.LegDocument;
import eu.europa.ec.leos.domain.cmis.document.LeosDocument;
import eu.europa.ec.leos.domain.cmis.document.Memorandum;
import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.domain.common.ErrorCode;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.domain.common.Result;
import eu.europa.ec.leos.domain.vo.CloneProposalMetadataVO;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.model.action.ContributionVO;
import eu.europa.ec.leos.repository.LeosRepository;
import eu.europa.ec.leos.services.export.ZipPackageUtil;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.store.LegService;
import eu.europa.ec.leos.services.store.PackageService;
import eu.europa.ec.leos.services.support.ContributionsUtil;
import eu.europa.ec.leos.services.support.XPathCatalog;
import eu.europa.ec.leos.util.LeosDomainUtil;
import eu.europa.ec.leos.vo.contribution.ContributionLegDocumentVO;
import io.atlassian.fugue.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static eu.europa.ec.leos.services.support.XmlHelper.ANNEX;
import static eu.europa.ec.leos.services.support.XmlHelper.BILL;
import static eu.europa.ec.leos.services.support.XmlHelper.MEMORANDUM;
import static eu.europa.ec.leos.services.support.XmlHelper.PROPOSAL;
import static eu.europa.ec.leos.util.LeosDomainUtil.CMIS_PROPERTY_SPLITTER;

@Service
@Instance(instances = {InstanceType.COMMISSION, InstanceType.OS})
public class ContributionServiceProposalImpl<T> implements ContributionService {

    private static final Logger LOG = LoggerFactory.getLogger(ContributionServiceProposalImpl.class);

    private final ProposalService proposalService;
    private PackageService packageService;
    private BillService billService;
    private AnnexService annexService;
    private MemorandumService memorandumService;
    private LegService legService;
    private final LeosRepository leosRepository;
    private final MessageHelper messageHelper;
    private final XmlContentProcessor xmlContentProcessor;

    List<String> BILL_DOC_TYPES = new ArrayList(Arrays.asList("REG", "DIR", "DEC"));
    private static final String ANNEX_DOC_TYPE = "ANNEX";
    private static final String MEMORANDUM_DOC_TYPE = "EXPL_MEMORANDUM";


    @Autowired
    public ContributionServiceProposalImpl(LeosRepository leosRepository, MessageHelper messageHelper,
                                           ProposalService proposalService, PackageService packageService,
                                           BillService billService, AnnexService annexService,
                                           MemorandumService memorandumService, LegService legService, XmlContentProcessor xmlContentProcessor) {
        this.leosRepository = leosRepository;
        this.messageHelper = messageHelper;
        this.proposalService = proposalService;
        this.packageService = packageService;
        this.billService = billService;
        this.annexService = annexService;
        this.memorandumService = memorandumService;
        this.legService = legService;
        this.xmlContentProcessor = xmlContentProcessor;
    }

    private <T extends LeosDocument> T findVersionByVersionedReference(String versionedReference, Class<T> filterType) {
        return this.findVersionByVersionedReference(versionedReference, filterType, true);
    }

    private <T extends LeosDocument> T findVersionByVersionedReference(String versionedReference, Class<T> filterType, boolean filterVersion) {
        int lastIndex = versionedReference.lastIndexOf(filterVersion? "_" : ".");
        if (lastIndex != -1) {
            String ref = versionedReference.substring(0, lastIndex);
            String versionLabel = versionedReference.substring(lastIndex + 1);
            return leosRepository.findDocumentByVersion(filterType, ref, versionLabel);
        }
        throw new RuntimeException("Unable to retrieve the version's document");
    }

    private String getDocumentName(String versionedReference) {
        int lastIndex = versionedReference.lastIndexOf("-");
        if (lastIndex != -1) {
            return versionedReference.substring(0, lastIndex).concat(".xml");
        }
        throw new RuntimeException("Unable to retrieve the document name");
    }

    @Override
    public <T extends XmlDocument> List<ContributionVO> getDocumentContributions(String documentId, int annexIndex, Class<T> filterType) {
        List<ContributionLegDocumentVO<T>> documentVersions = new ArrayList<>();
        LeosPackage leosPackage = packageService.findPackageByDocumentId(documentId);
        Proposal proposal = proposalService.findProposalByPackagePath(leosPackage.getPath());
        final List<String> clonedMilestoneIds = proposal.getClonedMilestoneIds();
        for (String clonedMilestoneId : clonedMilestoneIds) {
            String proposalRef = clonedMilestoneId.split(LeosDomainUtil.CMIS_PROPERTY_SPLITTER)[0];
            String legName = clonedMilestoneId.split(LeosDomainUtil.CMIS_PROPERTY_SPLITTER)[1];
            Proposal clonedProposal;
            try {
                clonedProposal = leosRepository.findDocumentByRef(proposalRef, Proposal.class);
            } catch (Exception e) {
                LOG.error("Error retrieving cloned proposal with reference " + proposalRef, e);
                continue;
            }
            LeosPackage clonedPackage = packageService.findPackageByDocumentId(clonedProposal.getId());
            LegDocument legDocument = packageService.findDocumentByPackagePathAndName(clonedPackage.getPath(), legName, LegDocument.class);
            List<String> containedDocuments = legDocument.getContainedDocuments();
            Map<String, Object> legContent;
            try {
                legContent = ZipPackageUtil.unzipByteArray(legDocument.getContent().getOrNull().getSource().getBytes());
            } catch (IOException e) {
                LOG.error("Error unzipping leg file " + legName + " for cloned proposal with reference " + proposalRef, e);
                continue;
            }
            if (filterType.getSimpleName().equalsIgnoreCase(ANNEX)) {
                Stream<String> filesToFind = containedDocuments.stream()
                        .filter(containedFile -> containedFile.startsWith(ANNEX_DOC_TYPE + "-"));
                filesToFind.forEach(annexVersionAndName -> {
                    Annex annex = (Annex) findVersionByVersionedReference(annexVersionAndName, filterType);
                    if (annex != null && annex.getMetadata().get().getIndex() == annexIndex) {
                        String annexName = annex.getName();
                        byte[] annexContent = (byte[]) legContent.get(annexName);
                        documentVersions.add(new ContributionLegDocumentVO(clonedProposal.getOriginRef(), annex,
                                annexContent, legName, annexName));
                    }
                });
            } else if(filterType.getSimpleName().equalsIgnoreCase(BILL)) {
                Optional<String> fileToFind = containedDocuments.stream()
                        .filter(containedFile-> {
                            String fileType = containedFile.substring(0, containedFile.indexOf("-"));
                            return BILL_DOC_TYPES.contains(fileType);
                        }).findFirst();
                if (fileToFind.isPresent()) {
                    Bill doc = (Bill) findVersionByVersionedReference(fileToFind.get(), filterType);
                    if (doc != null) {
                        String documentName = doc.getName();
                        byte[] docContent = (byte[])legContent.get(documentName);
                        documentVersions.add(new ContributionLegDocumentVO(clonedProposal.getOriginRef(), doc,
                                docContent, legName, documentName));
                    }
                }
            } else if(filterType.getSimpleName().equalsIgnoreCase(MEMORANDUM)) {
                Optional<String> fileToFind = containedDocuments.stream()
                        .filter(containedFile -> containedFile.startsWith(MEMORANDUM_DOC_TYPE + "-"))
                        .findFirst();
                if (fileToFind.isPresent()) {
                    Memorandum doc = (Memorandum) findVersionByVersionedReference(fileToFind.get(), filterType);
                    if (doc != null) {
                        String documentName = doc.getName();
                        byte[] docContent = (byte[])legContent.get(documentName);
                        documentVersions.add(new ContributionLegDocumentVO(clonedProposal.getOriginRef(), doc,
                                docContent, legName, documentName));
                    }
                }
            } else if(filterType.getSimpleName().equalsIgnoreCase(PROPOSAL)) {
                Proposal doc = clonedProposal;
                if (doc != null) {
                    String documentName = doc.getName();
                    byte[] docContent = (byte[]) legContent.get(documentName);
                    XPathCatalog catalog = new XPathCatalog();
                    doc = proposalService.findProposalVersion(xmlContentProcessor.getElementValue(docContent, catalog.getXPathObjectId(), true));
                    documentVersions.add(new ContributionLegDocumentVO(clonedProposal.getOriginRef(), doc,
                            docContent, legName, documentName));
                }
            }
        }
        return ContributionsUtil.buildContributionsVO(documentVersions, messageHelper);
    }

    @Override
    public Result<?> updateContributionStatusAfterContributionDone(String cloneProposalRef, String cloneLegFileName,
                                                                   CloneProposalMetadataVO cloneProposalMetadataVO) {
        Proposal updatedProposal;
        LegDocument updatedLegDocument;
        try {
            Proposal clonedProposal = leosRepository.findDocumentByRef(cloneProposalRef, Proposal.class);
            String originalProposalId = clonedProposal.getClonedFrom();
            Proposal originalProposal = proposalService.findProposal(originalProposalId);
            LeosPackage clonedPackage = packageService.findPackageByDocumentId(clonedProposal.getId());
            LegDocument legDocument = packageService.findDocumentByPackagePathAndName(clonedPackage.getPath(), cloneLegFileName,
                    LegDocument.class);
            List<String> containedDocuments = legDocument.getContainedDocuments();

            //update cloned proposal properties
            Map<String, Object> clonedProperties = new HashMap<>();
            clonedProperties.put(CmisProperties.REVISION_STATUS.getId(), cloneProposalMetadataVO.getRevisionStatus());
            proposalService.updateProposal(clonedProposal.getId(), clonedProperties);

            //update Bill metadata
            clonedProperties = new HashMap<>();
            Optional<String> billFile = containedDocuments.stream()
                    .filter(containedFile-> {
                        String fileType = containedFile.substring(0, containedFile.indexOf("-"));
                        return BILL_DOC_TYPES.contains(fileType);
                    }).findFirst();
            Bill clonedBill = findVersionByVersionedReference(billFile.get(), Bill.class);
            clonedProperties.put(CmisProperties.CONTRIBUTION_STATUS.getId(),
                    ContributionVO.ContributionStatus.RECEIVED.getValue());
            billService.updateBill(clonedBill.getId(), clonedProperties, true);

            //update Memorandum metadata
            clonedProperties = new HashMap<>();
            Optional<String> memorandumFile = containedDocuments.stream()
                    .filter(containedFile-> containedFile.startsWith(MEMORANDUM_DOC_TYPE)).findFirst();
            Memorandum clonedMemo = findVersionByVersionedReference(memorandumFile.get(), Memorandum.class);
            clonedProperties.put(CmisProperties.CONTRIBUTION_STATUS.getId(),
                    ContributionVO.ContributionStatus.RECEIVED.getValue());
            memorandumService.updateMemorandum(clonedMemo.getId(), clonedProperties, true);

            //update Annex metadata
            Stream<String> annexFile = containedDocuments.stream()
                    .filter(containedFile -> containedFile.startsWith(ANNEX_DOC_TYPE));
            annexFile.forEach(annexVersionAndName -> {
                Annex clonedAnnex = findVersionByVersionedReference(annexVersionAndName, Annex.class);
                Map<String, Object>  annexProperties = new HashMap<>();
                annexProperties.put(CmisProperties.CONTRIBUTION_STATUS.getId(),
                        ContributionVO.ContributionStatus.RECEIVED.getValue());
                annexService.updateAnnex(clonedAnnex.getId(), annexProperties, true);
            });

            // Update cloned proposal
            proposalService.updateProposal(clonedProposal.getId(), clonedProperties, true);

            //update original proposal properties
            Map<String, Object> properties = new HashMap<>();
            List<String> clonedMilestoneIds = originalProposal.getClonedMilestoneIds();
            clonedMilestoneIds.add(getClonedMilestoneId(cloneProposalRef, cloneLegFileName));
            properties.put(CmisProperties.CLONED_MILESTONE_ID.getId(), clonedMilestoneIds);
            updatedProposal = proposalService.updateProposal(originalProposal.getId(), properties);
            updatedLegDocument = legService.updateLegDocument(legDocument.getId(), LeosLegStatus.CONTRIBUTION_SENT);
        } catch(Exception e) {
            LOG.error("Unexpected error occurred while updating the proposal after revision", e);
            return new Result<>(e.getMessage(), ErrorCode.EXCEPTION);
        }
        return new Result<>(new Pair(updatedProposal, updatedLegDocument), null);
    }

    @Override
    public void updateContributionMergeActions(String cloneDocumentId, String legFileName, String documentName,
                                               byte[] xmlContent)
            throws IOException {
        LeosPackage clonedPackage = packageService.findPackageByDocumentId(cloneDocumentId);
        LegDocument legDocument = packageService.findDocumentByPackagePathAndName(clonedPackage.getPath(), legFileName,
                LegDocument.class);
        Map<String, Object> legContent = ZipPackageUtil.unzipByteArray(legDocument.getContent().get().
                getSource().getBytes());
        legContent.put(documentName, xmlContent);
        byte[] updatedLegContent = ZipPackageUtil.zipByteArray(legContent);
        legService.updateLegDocument(legDocument.getId(), updatedLegContent);
    }

    private String getClonedMilestoneId(String proposalRef, String legDocumentName) {
        return proposalRef + CMIS_PROPERTY_SPLITTER + legDocumentName;
    }
}
