/*
 * Copyright 2018 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.europa.ec.leos.usecases.document;

import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.LeosPackage;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.document.Bill;
import eu.europa.ec.leos.domain.cmis.document.Explanatory;
import eu.europa.ec.leos.domain.cmis.document.FinancialStatement;
import eu.europa.ec.leos.domain.cmis.document.Memorandum;
import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.domain.cmis.metadata.ProposalMetadata;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.domain.vo.MetadataVO;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.services.document.ProposalService;
import eu.europa.ec.leos.services.store.PackageService;
import eu.europa.ec.leos.services.store.TemplateService;
import io.atlassian.fugue.Option;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static eu.europa.ec.leos.domain.cmis.LeosCategory.BILL;
import static eu.europa.ec.leos.domain.cmis.LeosCategory.FINANCIAL_STATEMENT;
import static eu.europa.ec.leos.domain.cmis.LeosCategory.MEMORANDUM;
import static eu.europa.ec.leos.domain.cmis.LeosCategory.PROPOSAL;

public abstract class CollectionContext {

    private static final Logger LOG = LoggerFactory.getLogger(CollectionContext.class);

    protected final TemplateService templateService;
    protected final PackageService packageService;
    protected final ProposalService proposalService;

    protected final Provider<MemorandumContext> memorandumContextProvider;
    protected final Provider<BillContext> billContextProvider;

    protected final Map<LeosCategory, XmlDocument> categoryTemplateMap;

    protected final Map<ContextAction, String> actionMsgMap;

    protected Proposal proposal = null;
    protected String purpose;
    protected boolean eeaRelevance;
    protected String versionComment;
    protected String milestoneComment;

    protected DocumentVO propDocument;
    private String propChildDocument;
    private String proposalComment;

    protected LeosPackage leosPackage = null;

    CollectionContext(TemplateService templateService,
            PackageService packageService,
            ProposalService proposalService,
            Provider<MemorandumContext> memorandumContextProvider,
            Provider<BillContext> billContextProvider) {
        this.templateService = templateService;
        this.packageService = packageService;
        this.proposalService = proposalService;
        this.memorandumContextProvider = memorandumContextProvider;
        this.billContextProvider = billContextProvider;
        this.categoryTemplateMap = new HashMap<>();
        this.actionMsgMap = new HashMap<>();
    }

    public void useTemplate(String name) {
        Validate.notNull(name, "Template name is required!");
        XmlDocument template = templateService.getTemplate(name);
        Validate.notNull(template, "Template not found! [name=%s]", name);

        LOG.trace("Using {} template... [id={}, name={}]", template.getCategory(), template.getId(), template.getName());
        categoryTemplateMap.put(template.getCategory(), template);
    }

    public void useActionMessage(ContextAction action, String actionMsg) {
        Validate.notNull(actionMsg, "Action message is required!");
        Validate.notNull(action, "Context Action not found! [name=%s]", action);

        LOG.trace("Using action message... [action={}, name={}]", action, actionMsg);
        actionMsgMap.put(action, actionMsg);
    }

    public void useProposal(String id) {
        Validate.notNull(id, "Proposal identifier is required!");
        LOG.trace("Using Proposal... [id={}]", id);
        proposal = proposalService.findProposal(id);
        Validate.notNull(proposal, "Proposal not found! [id=%s]", id);
    }

    public void usePurpose(String purpose) {
        Validate.notNull(purpose, "Proposal purpose is required!");
        LOG.trace("Using Proposal purpose... [purpose={}]", purpose);
        this.purpose = purpose;
    }

    public void useEeaRelevance(boolean eeaRelevance) {
        LOG.trace("Using EEA Relevance... [eeaRelevance={}]", eeaRelevance);
        this.eeaRelevance = eeaRelevance;
    }

    public void useDocument(DocumentVO document) {
        Validate.notNull(document, "Proposal document is required!");
        propDocument = document;
    }

    public void useVersionComment(String comment) {
        Validate.notNull(comment, "Version comment is required!");
        this.versionComment = comment;
    }

    public void useMilestoneComment(String milestoneComment) {
        Validate.notNull(milestoneComment, "milestoneComment is required!");
        this.milestoneComment = milestoneComment;
    }

    public void useChildDocument(String documentId) {
        Validate.notNull(documentId, "Proposal child document is required!");
        propChildDocument = documentId;
    }

    public void useActionComment(String comment){
        Validate.notNull(comment, "Proposal comment is required!");
        proposalComment = comment;
    }

    public void executeImportProposal() {
        LOG.trace("Executing 'Import Proposal' use case...");
        MetadataVO propMeta = propDocument.getMetadata();
        Validate.notNull(propMeta, "Proposal metadata is required!");
        Validate.notNull(propDocument.getChildDocuments(), "Proposal must contain child documents to import!");
        // create package
        LeosPackage leosPackage = packageService.createPackage();

        // use template
        Proposal proposalTemplate = cast(categoryTemplateMap.get(PROPOSAL));
        Validate.notNull(proposalTemplate, "Proposal template is required!");

        // get metadata from template
        Option<ProposalMetadata> metadataOption = proposalTemplate.getMetadata();
        Validate.isTrue(metadataOption.isDefined(), "Proposal metadata is required!");
        purpose = propMeta.getDocPurpose();
        Validate.notNull(purpose, "Proposal purpose is required!");
        eeaRelevance = propMeta.getEeaRelevance();
        ProposalMetadata metadata = metadataOption.get().withPurpose(purpose).withEeaRelevance(eeaRelevance);

        Validate.notNull(propDocument.getSource(), "Proposal xml is required!");
        proposal = proposalService.createProposalFromContent(leosPackage.getPath(), metadata, propDocument.getSource());

        // create child element
        for (DocumentVO docChild : propDocument.getChildDocuments()) {
            if ((docChild.getCategory() == MEMORANDUM) && (cast(categoryTemplateMap.get(MEMORANDUM)) != null)) {
                MemorandumContext memorandumContext = memorandumContextProvider.get();
                memorandumContext.usePackage(leosPackage);
                // use template
                memorandumContext.useTemplate(cast(categoryTemplateMap.get(MEMORANDUM)));
                // We want to use the same purpose that was set in the wizard for all the documents.
                memorandumContext.usePurpose(purpose);
                memorandumContext.useDocument(docChild);
                memorandumContext.useActionMessageMap(actionMsgMap);
                memorandumContext.useType(metadata.getType());
                memorandumContext.usePackageTemplate(metadata.getTemplate());
                memorandumContext.useEeaRelevance(eeaRelevance);
                Memorandum memorandum = memorandumContext.executeImportMemorandum();
                proposal = proposalService.addComponentRef(proposal, memorandum.getName(), LeosCategory.MEMORANDUM);
            } else if (docChild.getCategory() == BILL) {
                BillContext billContext = billContextProvider.get();
                billContext.usePackage(leosPackage);
                // use template
                billContext.useTemplate(cast(categoryTemplateMap.get(BILL)));
                billContext.usePurpose(purpose);
                billContext.useDocument(docChild);
                billContext.useActionMessageMap(actionMsgMap);
                billContext.useEeaRelevance(eeaRelevance);
                Bill bill = billContext.executeImportBill();
                proposal = proposalService.addComponentRef(proposal, bill.getName(), LeosCategory.BILL);
            }
        }
        proposal = proposalService.createVersion(proposal.getId(), VersionType.INTERMEDIATE, actionMsgMap.get(ContextAction.DOCUMENT_CREATED));
    };

    public void executeUpdateProposal() {
        LOG.trace("Executing 'Update Proposal' use case...");

        Validate.notNull(proposal, "Proposal is required!");
        Validate.notNull(proposalComment, "Proposal comment is required!");

        Option<ProposalMetadata> metadataOption = proposal.getMetadata();
        Validate.isTrue(metadataOption.isDefined(), "Proposal metadata is required!");

        Validate.notNull(purpose, "Proposal purpose is required!");
        ProposalMetadata metadata = metadataOption.get().withPurpose(purpose).withEeaRelevance(eeaRelevance);

        proposal = proposalService.updateProposal(proposal, metadata, VersionType.MINOR, proposalComment);

        executeUpdateDocumentsAssociatedToProposal();
    }

    public void executeUpdateDocumentsAssociatedToProposal() {
        LOG.trace("Executing 'Update Docucuments Associated to Proposal' use case...");

        Validate.notNull(proposal, "Proposal is required!");
        Validate.notNull(proposalComment, "Proposal comment is required!");

        Option<ProposalMetadata> metadataOption = proposal.getMetadata();
        Validate.isTrue(metadataOption.isDefined(), "Proposal metadata is required!");

        Validate.notNull(purpose, "Proposal purpose is required!");

        LeosPackage leosPackage = packageService.findPackageByDocumentId(proposal.getId());
        List<XmlDocument> documents = packageService.findDocumentsByPackagePath(leosPackage.getPath(),
                XmlDocument.class, false);

        for (XmlDocument document : documents) {
            switch (document.getCategory()) {
                case COUNCIL_EXPLANATORY: {
                    executeUpdateExplanatory(leosPackage, purpose, actionMsgMap);
                    break;
                }
                case MEMORANDUM: {
                    MemorandumContext memorandumContext = memorandumContextProvider.get();
                    memorandumContext.usePackage(leosPackage);
                    memorandumContext.usePurpose(purpose);
                    memorandumContext.useEeaRelevance(eeaRelevance);
                    memorandumContext.useActionMessageMap(actionMsgMap);
                    memorandumContext.executeUpdateMemorandum();
                    break;
                }
                case BILL: {
                    BillContext billContext = billContextProvider.get();
                    billContext.usePackage(leosPackage);
                    billContext.usePurpose(purpose);
                    billContext.useEeaRelevance(eeaRelevance);
                    billContext.useActionMessageMap(actionMsgMap);
                    billContext.executeUpdateBill();
                    break;
                }
                default:
                    LOG.debug("Do nothing for rest of the categories like MEDIA, CONFIG & LEG");
                    break;
            }
        }
    }

    public void executeDeleteProposal() {
        LOG.trace("Executing 'Delete Proposal' use case...");
        LeosPackage leosPackage = packageService.findPackageByDocumentId(proposal.getId());
        packageService.deletePackage(leosPackage);
    }

    public void executeUpdateProposalAsync() {
        Validate.notNull(propChildDocument, "Proposal child document is required!");
        proposalService.updateProposalAsync(propChildDocument, proposalComment);
    }

    /**
     * Given a proposalId, fetch all documents related from CMIS and update them to the next major version.
     * The document version is not updated if it is already a major version.
     */
    public void executeCreateMilestone() {
        LOG.info("Creating major versions for all documents of [proposal={}", proposal.getId());

        // 1. Proposal
        List<String> milestoneComments = proposal.getMilestoneComments();
        milestoneComments.add(milestoneComment);
        if (proposal.getVersionType().equals(VersionType.MAJOR)) {
            proposal = proposalService.updateProposalWithMilestoneComments(proposal.getId(), milestoneComments);
            LOG.info("Major version {} already present. Updated only milestoneComment for [proposal={}]", proposal.getVersionLabel(), proposal.getId());
        } else {
            proposal = proposalService.updateProposalWithMilestoneComments(proposal, milestoneComments, VersionType.MAJOR, versionComment);
            LOG.info("Created major version {} for [proposal={}]", proposal.getVersionLabel(), proposal.getId());
        }

        // Update the last structure
        final LeosPackage leosPackage = packageService.findPackageByDocumentId(proposal.getId());

        // 2. Memorandum
        final MemorandumContext memorandumContext = memorandumContextProvider.get();
        memorandumContext.usePackage(leosPackage);
        memorandumContext.useVersionComment(versionComment);
        memorandumContext.useMilestoneComment(milestoneComment);
        memorandumContext.executeCreateMilestone();

        // 2. Bill + Annexes
        final BillContext billContext = billContextProvider.get();
        billContext.usePackage(leosPackage);
        billContext.useVersionComment(versionComment);
        billContext.useMilestoneComment(milestoneComment);
        billContext.executeCreateMilestone();

        createExplanatoryMilestones(leosPackage);
    }

    public String getUpdatedProposalId() {
        return proposal.getId();
    }

    @SuppressWarnings("unchecked")
    protected static <T> T cast(Object obj) {
        return (T) obj;
    }

    public void usePackage(LeosPackage leosPackage) {
        Validate.notNull(leosPackage, "Bill package is required!");
        LOG.trace("Using Bill package... [id={}, path={}]", leosPackage.getId(), leosPackage.getPath());
        this.leosPackage = leosPackage;
    }

    protected abstract void createExplanatoryMilestones(LeosPackage leosPackage);
    public abstract void executeRemoveExplanatory();
    public abstract void useExplanatory(String explanatoryId);
    public abstract Explanatory getExplanatory(ProposalMetadata metadata, ExplanatoryContext explanatoryContext, String template, boolean createProposal) ;
    public abstract void executeCreateExplanatory();
    public abstract void executeCreateProposal();
    protected abstract void executeUpdateExplanatory(LeosPackage leosPackage, String purpose, Map<ContextAction, String> actionMsgMap);
}
