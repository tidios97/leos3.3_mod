package eu.europa.ec.leos.services.api;

import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.document.LeosDocument;
import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.domain.cmis.metadata.ProposalMetadata;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.integration.rest.UserJSON;
import eu.europa.ec.leos.security.LeosPermissionAuthorityMap;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.collection.CollectionContextService;
import eu.europa.ec.leos.services.collection.CreateCollectionException;
import eu.europa.ec.leos.services.collection.CreateCollectionResult;
import eu.europa.ec.leos.services.collection.CreateCollectionService;
import eu.europa.ec.leos.services.collection.document.ContextActionService;
import eu.europa.ec.leos.services.document.ProposalService;
import eu.europa.ec.leos.services.dto.request.FilterProposalsRequest;
import eu.europa.ec.leos.services.dto.request.UpdateProposalRequest;
import eu.europa.ec.leos.services.dto.response.WorkspaceProposalResponse;
import eu.europa.ec.leos.services.export.ExportLW;
import eu.europa.ec.leos.services.export.ExportOptions;
import eu.europa.ec.leos.services.export.ExportService;
import eu.europa.ec.leos.services.store.TemplateService;
import eu.europa.ec.leos.services.store.WorkspaceService;
import eu.europa.ec.leos.services.user.UserService;
import eu.europa.ec.leos.vo.catalog.CatalogItem;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Provider;
import javax.xml.ws.WebServiceException;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class ApiServiceImpl implements ApiService {
    private static final Logger LOG = LoggerFactory.getLogger(ApiServiceImpl.class);

    private final TemplateService templateService;
    private final WorkspaceService workspaceService;
    private final UserService userService;
    private final CreateCollectionService createCollectionService;
    private final SecurityContext securityContext;
    private final LeosPermissionAuthorityMap authorityMap;
    private final ProposalService proposalService;

    private final ExportService exportService;
    private final Provider<CollectionContextService> collectionContextProvider;
    private final MessageHelper messageHelper;

    @Autowired
    public ApiServiceImpl(TemplateService templateService, WorkspaceService workspaceService, UserService userService,
                          CreateCollectionService createCollectionService, ProposalService proposalService,
                          SecurityContext securityContext, LeosPermissionAuthorityMap authorityMap, ExportService exportService,
                          Provider<CollectionContextService> collectionContextProvider, MessageHelper messageHelper) {
        this.templateService = templateService;
        this.workspaceService = workspaceService;
        this.userService = userService;
        this.proposalService = proposalService;
        this.createCollectionService = createCollectionService;
        this.securityContext = securityContext;
        this.authorityMap = authorityMap;
        this.exportService = exportService;
        this.collectionContextProvider = collectionContextProvider;
        this.messageHelper = messageHelper;
    }

    @Override
    public <T extends LeosDocument> WorkspaceProposalResponse listDocumentsWithFilter(FilterProposalsRequest request) {
        return workspaceService.listDocumentsWithFilter(request, securityContext, authorityMap);
    }

    @Override
    public List<CatalogItem> getTemplates() throws IOException {
        return templateService.getTemplatesCatalog();
    }

    @Override
    public CreateCollectionResult createProposal(String templateId, String templateName, String langCode,
                                                 String docPurpose, boolean eeaRelevance) throws CreateCollectionException {
        DocumentVO documentVO = new DocumentVO(LeosCategory.PROPOSAL);
        documentVO.getMetadata().setDocTemplate(templateId);
        documentVO.getMetadata().setTemplateName(templateName);
        documentVO.getMetadata().setLanguage(langCode);
        documentVO.getMetadata().setDocPurpose(docPurpose);
        documentVO.getMetadata().setEeaRelevance(eeaRelevance);
        return createCollectionService.createCollection(documentVO);
    }

    @Override
    public CreateCollectionResult uploadProposal(File legDocument) throws CreateCollectionException {
        return createCollectionService.createCollectionFromLeg(legDocument);
    }

    @Override
    public DocumentVO updateProposalMetadata(String proposalRef, UpdateProposalRequest request) {
        LOG.trace("Saving proposal metadata...");
        try {
            CollectionContextService context = collectionContextProvider.get();
            Proposal proposal = proposalService.findProposalByRef(proposalRef);
            context.useProposal(proposal);
            if (request.getDocPurpose() != null) {
                context.usePurpose(request.getDocPurpose());
            } else {
                context.usePurpose(proposal.getMetadata().get().getPurpose());
            }
            if (request.isEeaRelevance() != null) {
                context.useEeaRelevance(request.isEeaRelevance());
            } else {
                context.useEeaRelevance(proposal.getMetadata().get().getEeaRelevance());
            }
            String comment = messageHelper.getMessage("operation.metadata.updated");
            context.useActionMessage(ContextActionService.METADATA_UPDATED, comment);
            context.useActionComment(comment);
            return new DocumentVO(context.executeUpdateProposal());
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while updating proposal metadata ", e);
            throw e;
        }
    }
    @Override
    public void deleteCollection(String proposalRef) {
        CollectionContextService context = collectionContextProvider.get();
        Proposal proposal = proposalService.findProposalByRef(proposalRef);
        context.useProposal(proposal);
        context.executeDeleteProposal();
    }

    @Override
    public List<UserJSON> searchUser(String searchKey) {
        return userService.searchUsersByKey(searchKey);
    }
    @Override
    public void createExplanatoryDocument(String proposalRef, String template) {
        try {
            Proposal proposal = proposalService.findProposalByRef(proposalRef);
            ProposalMetadata metadata = proposal.getMetadata().getOrError(() -> "Proposal metadata is required!");

            CollectionContextService context = collectionContextProvider.get();
            String selectedTemplate = !StringUtils.isEmpty(template) ? template : "CE-003";

            context.useTemplate(selectedTemplate);
            context.usePurpose(metadata.getPurpose());
            context.useProposal(proposal);
            context.useActionMessage(ContextActionService.EXPLANATORY_ADDED, messageHelper.getMessage("collection.block.explanatory.added"));
            context.executeCreateExplanatory();
        } catch(Exception e) {
            LOG.error("Unexpected error occurred while creating the explanatory", e);
            throw e;
        }
    }

    @Override
    public String exportProposal(String proposalRef, String outputType) {
        try {
            Proposal proposal = proposalService.findProposalByRef(proposalRef);
            ExportOptions.Output output;
            switch (outputType) {
                case "PDF":
                    output = ExportOptions.Output.PDF;
                    break;
                case "WORD":
                    output = ExportOptions.Output.WORD;
                    break;
                default:
                    throw new RuntimeException("Invalid output type provided");
            }
            ExportOptions exportOptions = new ExportLW(output);
            String jobId = exportService.exportToToolboxCoDe(proposal.getId(), exportOptions);
            return jobId;
        } catch (WebServiceException wse) {
            LOG.error("External system not available due to WebServiceException: {}", wse.getMessage());
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while sending job to ToolBox: {}", e.getMessage());
        }
        return null;
    }
}
