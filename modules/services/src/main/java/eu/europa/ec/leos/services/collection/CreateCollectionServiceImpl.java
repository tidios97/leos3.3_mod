package eu.europa.ec.leos.services.collection;

import com.google.common.base.Stopwatch;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.document.LegDocument;
import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.domain.common.Result;
import eu.europa.ec.leos.domain.vo.CloneProposalMetadataVO;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.notification.cloneProposal.ClonedProposalNotification;
import eu.europa.ec.leos.model.notification.cloneProposal.RevisionDoneNotification;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.clone.CloneContext;
import eu.europa.ec.leos.services.collection.document.ContextActionService;
import eu.europa.ec.leos.services.converter.ProposalConverterService;
import eu.europa.ec.leos.services.document.ContributionService;
import eu.europa.ec.leos.services.document.PostProcessingDocumentService;
import eu.europa.ec.leos.services.notification.NotificationService;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.support.url.CollectionIdsAndUrlsHolder;
import eu.europa.ec.leos.services.support.url.CollectionUrlBuilder;
import io.atlassian.fugue.Pair;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.inject.Provider;
import java.io.File;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static eu.europa.ec.leos.domain.cmis.LeosCategory.PROPOSAL;

@Service
public class CreateCollectionServiceImpl implements CreateCollectionService {
    private static final Logger LOG = LoggerFactory.getLogger(CreateCollectionServiceImpl.class);

    private final Provider<CollectionContextService> proposalContextProvider;
    private ProposalConverterService proposalConverterService;
    private ContributionService contributionService;
    private PostProcessingDocumentService postProcessingDocumentService;
    private final NotificationService notificationService;
    private SecurityContext securityContext;

    private CollectionUrlBuilder urlBuilder;
    private MessageHelper messageHelper;
    private XmlContentProcessor xmlContentProcessor;
    private CloneContext cloneContext;

    @Value("${notification.functional.mailbox}")
    private String notificationRecepient;

    @Autowired
    public CreateCollectionServiceImpl(
            Provider<CollectionContextService> proposalContextProvider,
            PostProcessingDocumentService postProcessingDocumentService,
            ContributionService contributionService,
            ProposalConverterService proposalConverterService,
            NotificationService notificationService,
            SecurityContext securityContext, CollectionUrlBuilder urlBuilder,
            MessageHelper messageHelper, XmlContentProcessor xmlContentProcessor,
            CloneContext cloneContext) {
        this.proposalContextProvider = proposalContextProvider;
        this.proposalConverterService = proposalConverterService;
        this.postProcessingDocumentService = postProcessingDocumentService;
        this.contributionService = contributionService;
        this.notificationService = notificationService;
        this.securityContext = securityContext;
        this.urlBuilder = urlBuilder;
        this.messageHelper = messageHelper;
        this.xmlContentProcessor = xmlContentProcessor;
        this.cloneContext = cloneContext;
    }

    private DocumentVO createDocumentVOFromLegfile(File legDocument) {
        Validate.notNull(legDocument, "Leg document is required");

        DocumentVO propDocument = proposalConverterService.createProposalFromLegFile(legDocument, new DocumentVO(PROPOSAL), true);

        CollectionContextService context = proposalContextProvider.get();
        context.useTemplate(propDocument.getMetadata().getDocTemplate());
        return propDocument;
    }

    private void addTemplateInContext(CollectionContextService context, DocumentVO documentVO) {
        context.useTemplate(documentVO.getMetadata().getDocTemplate());
        if (documentVO.getChildDocuments() != null) {
            for (DocumentVO docChild : documentVO.getChildDocuments()) {
                addTemplateInContext(context, docChild);
            }
        }
    }

    @Override
    public CreateCollectionResult createCollection(DocumentVO documentVO) throws CreateCollectionException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        LOG.debug("Handling create document request event... [category={}]", documentVO.getCategory());
        CollectionIdsAndUrlsHolder idsAndUrlsHolder = new CollectionIdsAndUrlsHolder();
        if (LeosCategory.PROPOSAL.equals(documentVO.getCategory())) {
            CollectionContextService context = proposalContextProvider.get();
            String template = documentVO.getMetadata().getDocTemplate();
            String[] templates = (template != null) ? template.split(";") : new String[0];
            for (String name : templates) {
                context.useTemplate(name);
            }
            context.usePurpose(documentVO.getMetadata().getDocPurpose());
            context.useEeaRelevance(documentVO.getMetadata().getEeaRelevance());
            context.useActionMessage(ContextActionService.METADATA_UPDATED, messageHelper.getMessage("operation.metadata.updated"));
            context.useActionMessage(ContextActionService.DOCUMENT_CREATED, messageHelper.getMessage("operation.document.created"));
            //create proposal
            Proposal proposal = context.executeCreateProposal();

            String proposalId = proposal.getMetadata().get().getRef();
            String proposalUrl = urlBuilder.buildProposalViewUrl(proposalId);
            idsAndUrlsHolder.setProposalId(proposalId);
            idsAndUrlsHolder.setProposalUrl(proposalUrl);
            LOG.info("New document of type {} created in {} milliseconds ({} sec)", documentVO.getCategory(),
                    stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
            return new CreateCollectionResult(idsAndUrlsHolder, true, null);
        }
        CreateCollectionError error = new CreateCollectionError(0,
                messageHelper.getMessage("repository.create.proposal.error"));
        throw new CreateCollectionException(error.getMessage());
    }

    @Override
    public CreateCollectionResult createCollectionFromLeg(File legDocument) throws CreateCollectionException {

        String proposalUrl;
        String proposalId;

        CollectionIdsAndUrlsHolder idsAndUrlsHolder = new CollectionIdsAndUrlsHolder();
        DocumentVO propDocument = createDocumentVOFromLegfile(legDocument);

        CollectionContextService context = proposalContextProvider.get();
        context.useDocument(propDocument);
        context.useIdsAndUrlsHolder(idsAndUrlsHolder);
        addTemplateInContext(context, propDocument);
        postProcessingDocumentService.processDocument(propDocument);
        Proposal proposal = context.executeImportProposal();

        proposalId = proposal.getMetadata().get().getRef();
        proposalUrl = urlBuilder.buildProposalViewUrl(proposalId);
        idsAndUrlsHolder.setProposalId(proposalId);
        idsAndUrlsHolder.setProposalUrl(proposalUrl);

        return new CreateCollectionResult(idsAndUrlsHolder, true, null);
    }

    @Override
    public CreateCollectionResult cloneCollection(File legDocument, String iscRef, String targetUser, String connectedEntity) throws CreateCollectionException {
        String proposalUrl;
        String proposalId;
        String cmisObjectId;

        CollectionIdsAndUrlsHolder idsAndUrlsHolder = new CollectionIdsAndUrlsHolder();
        DocumentVO propDocument = createDocumentVOFromLegfile(legDocument);

        //set metadata to cloned proposal
        CloneProposalMetadataVO cloneProposalMetadataVO = new CloneProposalMetadataVO();
        cloneProposalMetadataVO.setClonedProposal(Boolean.TRUE);
        cloneProposalMetadataVO.setOriginRef(iscRef);
        cloneProposalMetadataVO.setClonedFromRef(propDocument.getRef());
        cloneProposalMetadataVO.setClonedFromObjectId(propDocument.getId());
        cloneProposalMetadataVO.setLegFileName(legDocument.getName());
        cloneProposalMetadataVO.setTargetUser(targetUser);
        cloneProposalMetadataVO.setRevisionStatus(messageHelper.getMessage("clone.proposal.status.sent"));

        CollectionContextService context = proposalContextProvider.get();
        context.useDocument(propDocument);
        context.useIdsAndUrlsHolder(idsAndUrlsHolder);
        context.useIscRef(iscRef);
        context.useCloneProposal(true);
        context.useConnectedEntity(connectedEntity);
        context.useClonedProposalMetadataVO(cloneProposalMetadataVO);
        addTemplateInContext(context, propDocument);

        Result<?> result = postProcessingDocumentService.saveOriginalProposalIdToClonedProposal(propDocument, legDocument.getName(), iscRef);
        if (result.isError()) {
            CreateCollectionError error = new CreateCollectionError(result.getErrorCode().get().ordinal(),
                    messageHelper.getMessage("clone.proposal.metadata.preserve.error"));
            return new CreateCollectionResult(idsAndUrlsHolder, false, error);
        }

        Proposal proposal = context.executeImportProposal();
        proposalId = proposal.getMetadata().get().getRef();
        cmisObjectId = proposal.getId();
        proposalUrl = urlBuilder.buildProposalViewUrl(proposalId);
        idsAndUrlsHolder.setProposalId(proposalId);
        idsAndUrlsHolder.setProposalUrl(proposalUrl);
        idsAndUrlsHolder.addDocCloneAndOriginIdMap(proposalId, propDocument.getRef());

        //set clone creation date to original proposal
        cloneProposalMetadataVO.setCreationDate(Date.from(proposal.getInitialCreationInstant()));

        result = postProcessingDocumentService.saveClonedProposalIdToOriginalProposal(propDocument, idsAndUrlsHolder, cloneProposalMetadataVO);
        if (result.isError()) {
            //In case of error delete the cloned proposal.
            context.useProposal(proposal);
            try {
                LOG.debug("Deleting cloned proposal as metadata update operation failed");
                context.executeDeleteProposal();
            } catch (Exception e) {
                LOG.error("Error deleting the cloned proposal", e);
            }
            CreateCollectionError error = new CreateCollectionError(result.getErrorCode().get().ordinal(),
                    messageHelper.getMessage("clone.proposal.metadata.preserve.error"));
            return new CreateCollectionResult(idsAndUrlsHolder, true, error);
        } else {
            postProcessingDocumentService.updatePostCloneMetadataProperties(cmisObjectId, cloneProposalMetadataVO);
        }
        try {
            //Send CNS notification
            notificationService.sendNotification(new ClonedProposalNotification(notificationRecepient,
                    messageHelper.getMessage("clone.proposal.notification.title.iscRef", iscRef),
                    legDocument.getName(), proposalUrl, iscRef));
        } catch (Exception e) {
            LOG.error("CNS notification exception. Service is not available at the moment.", e);
        }
        return new CreateCollectionResult(idsAndUrlsHolder, true, null);
    }

    @Override
    public Result<?> updateOriginalProposalAfterRevisionDone(String cloneProposalRef, String cloneLegFileName) {
        CloneProposalMetadataVO cloneProposalMetadataVO = cloneContext.getCloneProposalMetadataVO();
        if(cloneProposalMetadataVO == null) {
            cloneProposalMetadataVO = new CloneProposalMetadataVO();
        }
        cloneProposalMetadataVO.setRevisionStatus(messageHelper.getMessage("clone.proposal.status.contribution.done"));
        Result<?> result = contributionService.updateContributionStatusAfterContributionDone(cloneProposalRef,
                cloneLegFileName, cloneProposalMetadataVO);
        if(result.isOk()) {
            Proposal updatedProposal = (Proposal) ((Pair) result.get()).left();
            String proposalUrl = urlBuilder.buildProposalViewUrl(updatedProposal.getMetadata().get().getRef());
            try {
                //Send CNS notification
                notificationService.sendNotification(new RevisionDoneNotification(notificationRecepient,
                        messageHelper.getMessage("clone.proposal.contribution.done.notification.title",
                                securityContext.getUser().getDefaultEntity().getOrganizationName()),
                        proposalUrl, securityContext.getUser().getDefaultEntity().getOrganizationName()));
            } catch (Exception e) {
                LOG.error("CNS notification exception. Service is not available at the moment.", e);
            }
            return new Result<>(new Pair<String, LegDocument>(proposalUrl, (LegDocument) ((Pair) result.get()).right()), null);
        }
        return result;
    }
}