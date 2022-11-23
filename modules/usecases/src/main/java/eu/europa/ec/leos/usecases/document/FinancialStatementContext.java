package eu.europa.ec.leos.usecases.document;

import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.LeosPackage;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.document.Explanatory;
import eu.europa.ec.leos.domain.cmis.document.FinancialStatement;
import eu.europa.ec.leos.domain.cmis.document.FinancialStatement;
import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.domain.cmis.metadata.FinancialStatementMetadata;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.model.user.Collaborator;
import eu.europa.ec.leos.services.document.FinancialStatementService;
import eu.europa.ec.leos.services.document.FinancialStatementService;
import eu.europa.ec.leos.services.document.ProposalService;
import eu.europa.ec.leos.services.document.SecurityService;
import eu.europa.ec.leos.services.store.TemplateService;
import io.atlassian.fugue.Option;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
public class FinancialStatementContext {

    private static final Logger LOG = LoggerFactory.getLogger(FinancialStatementContext.class);

    private final TemplateService templateService;
    private final FinancialStatementService financialStatementService;
    private final ProposalService proposalService;
    private final SecurityService securityService;

    private LeosPackage leosPackage;
    private FinancialStatement financialStatement;
    private String purpose = null;
    private String title = "";
    private String type = null;
    private String template = null;
    private String FinancialStatementId = null;
    private List<Collaborator> collaborators = null;

    private DocumentVO FinancialStatementDocument;
    private final Map<ContextAction, String> actionMsgMap;
    private String versionComment;
    private String milestoneComment;
    private String financialStatementId;

    public FinancialStatementContext(
            TemplateService templateService,
            FinancialStatementService financialStatementService,
            ProposalService proposalService, SecurityService securityService) {
        this.templateService = templateService;
        this.financialStatementService = financialStatementService;
        this.proposalService = proposalService;
        this.securityService = securityService;
        this.actionMsgMap = new HashMap<>();
    }

    public void useTemplate(String template) {
        Validate.notNull(template, "Template name is required!");

        this.financialStatement = (FinancialStatement) templateService.getTemplate(template);
        Validate.notNull(financialStatement, "Template not found! [name=%s]", template);
        this.template = template;
        LOG.trace("Using {} template... [id={}, name={}]", financialStatement.getCategory(), financialStatement.getId(), financialStatement.getName());
    }

    public void useActionMessageMap(Map<ContextAction, String> messages) {
        Validate.notNull(messages, "Action message map is required!");

        actionMsgMap.putAll(messages);
    }

    public void usePackage(LeosPackage leosPackage) {
        Validate.notNull(leosPackage, "FinancialStatement package is required!");
        LOG.trace("Using FinancialStatement package... [id={}, path={}]", leosPackage.getId(), leosPackage.getPath());
        this.leosPackage = leosPackage;
    }

    public void usePurpose(String purpose) {
        Validate.notNull(purpose, "FinancialStatement purpose is required!");
        LOG.trace("Using FinancialStatement purpose... [purpose={}]", purpose);
        this.purpose = purpose;
    }

    public void useType(String type) {
        Validate.notNull(type, "FinancialStatement type is required!");
        LOG.trace("Using FinancialStatement type... [type={}]", type);
        this.type = type;
    }

    public void usePackageTemplate(String template) {
        Validate.notNull(template, "template is required!");
        LOG.trace("Using template... [template={}]", template);
        this.template = template;
    }

    public void useTitle(String title) {
        Validate.notNull(title, "FinancialStatement title is required!");
        LOG.trace("Using FinancialStatement title... [title={}]", title);
        this.title = title;
    }

    public void useFinancialStatement(FinancialStatement financialStatement) {
        Validate.notNull(financialStatement, "FinancialStatement document is required!");
        LOG.trace("Using FinancialStatement document'... [FinancialStatementId={}]", financialStatement.getId());
        this.financialStatement = financialStatement;
    }

    public void useFinancialStatementId(String FinancialStatementId) {
        Validate.notNull(FinancialStatementId, "FinancialStatement 'FinancialStatementId' is required!");
        LOG.trace("Using FinancialStatementId'... [FinancialStatementId={}]", FinancialStatementId);
        this.FinancialStatementId = FinancialStatementId;
    }

    public void useDocument(DocumentVO document) {
        Validate.notNull(document, "FinancialStatement document is required!");
        FinancialStatementDocument = document;
    }

    public void useCollaborators(List<Collaborator> collaborators) {
        Validate.notNull(collaborators, "FinancialStatement 'collaborators' are required!");
        LOG.trace("Using collaborators'... [collaborators={}]", collaborators);
        this.collaborators = Collections.unmodifiableList(collaborators);
    }

    public void useVersionComment(String comment) {
        Validate.notNull(comment, "Version comment is required!");
        this.versionComment = comment;
    }

    public void useMilestoneComment(String milestoneComment) {
        Validate.notNull(milestoneComment, "milestoneComment is required!");
        this.milestoneComment = milestoneComment;
    }

    public void useActionMessage(ContextAction action, String actionMsg) {
        Validate.notNull(actionMsg, "Action message is required!");
        Validate.notNull(action, "Context Action not found! [name=%s]", action);

        LOG.trace("Using action message... [action={}, name={}]", action, actionMsg);
        actionMsgMap.put(action, actionMsg);
    }

    public void useFinancialStatement(String financialStatementId) {
        Validate.notNull(financialStatementId, "Financial statementId is required!");
        LOG.trace("Using Proposal explanatory id [explId={}]", financialStatementId);
        this.financialStatementId = financialStatementId;
    }

    public FinancialStatement executeCreateFinancialStatement() {
        LOG.trace("Executing 'Create FinancialStatement' use case...");

        Validate.notNull(leosPackage, "FinancialStatement package is required!");
        Validate.notNull(financialStatement, "FinancialStatement template is required!");
        Validate.notNull(collaborators, "FinancialStatement collaborators are required!");

        Option<FinancialStatementMetadata> metadataOption = financialStatement.getMetadata();
        Validate.isTrue(metadataOption.isDefined(), "FinancialStatement metadata is required!");

        Validate.notNull(purpose, "FinancialStatement purpose is required!");
        FinancialStatementMetadata metadata = metadataOption.get().withPurpose(purpose).withType(type).withTemplate(template).withTitle(title);

        financialStatement = financialStatementService.createFinancialStatement(financialStatement.getId(), leosPackage.getPath(), metadata, actionMsgMap.get(ContextAction.FINANCIAL_STATEMENT_METADATA_UPDATED), null);
        financialStatement = securityService.updateCollaborators(financialStatement.getId(), collaborators, FinancialStatement.class);

        return financialStatementService.createVersion(financialStatement.getId(), VersionType.INTERMEDIATE, actionMsgMap.get(ContextAction.DOCUMENT_CREATED));
    }

    public FinancialStatement executeImportFinancialStatement() {
        LOG.trace("Executing 'Import FinancialStatement' use case...");
        Validate.notNull(leosPackage, "FinancialStatement package is required!");
        Validate.notNull(financialStatement, "FinancialStatement template is required!");
        Validate.notNull(collaborators, "FinancialStatement collaborators are required!");
        Validate.notNull(purpose, "FinancialStatement purpose is required!");
        Validate.notNull(type, "FinancialStatement type is required!");

        final String actionMessage = actionMsgMap.get(ContextAction.ANNEX_BLOCK_UPDATED);
        final FinancialStatementMetadata metadataDocument = (FinancialStatementMetadata) FinancialStatementDocument.getMetadataDocument();
        financialStatement = financialStatementService.createFinancialStatementFromContent(leosPackage.getPath(), metadataDocument, actionMessage, FinancialStatementDocument.getSource(), FinancialStatementDocument.getName());
        financialStatement = securityService.updateCollaborators(financialStatement.getId(), collaborators, FinancialStatement.class);

        return financialStatementService.createVersion(financialStatement.getId(), VersionType.INTERMEDIATE, actionMsgMap.get(ContextAction.DOCUMENT_CREATED));
    }

    public void executeUpdateFinancialStatement() {
        LOG.trace("Executing 'Update FinancialStatement metadata' use case...");
        Validate.notNull(purpose, "FinancialStatement purpose is required!");
        Validate.notNull(financialStatement, "FinancialStatement document is required!");

        Option<FinancialStatementMetadata> metadataOption = financialStatement.getMetadata();
        Validate.isTrue(metadataOption.isDefined(), "FinancialStatement metadata is required!");

        // Updating only purpose at this time. other metadata needs to be set, if needed
        FinancialStatementMetadata FinancialStatementMetadata = metadataOption.get().withPurpose(purpose);
        financialStatementService.updateFinancialStatement(financialStatement, FinancialStatementMetadata, VersionType.MINOR, actionMsgMap.get(ContextAction.METADATA_UPDATED));
    }

    public void executeUpdateFinancialStatementStructure() {
        byte[] xmlContent = getContent(financialStatement); //Use the content from template
        financialStatement = financialStatementService.findFinancialStatement(FinancialStatementId); //Get the existing FinancialStatement document

        Option<FinancialStatementMetadata> metadataOption = financialStatement.getMetadata();
        Validate.isTrue(metadataOption.isDefined(), "FinancialStatement metadata is required!");
        FinancialStatementMetadata metadata = metadataOption.get();
        FinancialStatementMetadata financialStatementMetadata = metadata
                .withPurpose(metadata.getPurpose())
                .withType(metadata.getType())
                .withTemplate(template)
                .withDocVersion(metadata.getDocVersion())
                .withDocTemplate(template);

        financialStatement = financialStatementService.updateFinancialStatement(financialStatement, xmlContent, financialStatementMetadata, VersionType.INTERMEDIATE, actionMsgMap.get(ContextAction.ANNEX_STRUCTURE_UPDATED));
    }

    private byte[] getContent(FinancialStatement FinancialStatement) {
        final Content content = FinancialStatement.getContent().getOrError(() -> "FinancialStatement content is required!");
        return content.getSource().getBytes();
    }

    public void executeCreateMilestone() {
        financialStatement = financialStatementService.findFinancialStatement(FinancialStatementId);
        List<String> milestoneComments = financialStatement.getMilestoneComments();
        milestoneComments.add(milestoneComment);
        if (financialStatement.getVersionType().equals(VersionType.MAJOR)) {
            financialStatement = financialStatementService.updateFinancialStatementWithMilestoneComments(financialStatement.getId(), milestoneComments);
            LOG.info("Major version {} already present. Updated only milestoneComment for [FinancialStatement={}]", financialStatement.getVersionLabel(), financialStatement.getId());
        } else {
            financialStatement = financialStatementService.updateFinancialStatementWithMilestoneComments(financialStatement, milestoneComments, VersionType.MAJOR, versionComment);
            LOG.info("Created major version {} for [FinancialStatement={}]", financialStatement.getVersionLabel(), financialStatement.getId());
        }
    }

    public String getUpdatedFinancialStatementId() {
        return financialStatement.getId();
    }

    public void executeDeleteFinancialStatement() {
        LOG.trace("Executing 'FinancialStatement' use case...");
        Validate.notNull(leosPackage, "Leos package is required!");
        FinancialStatement financialStatement = financialStatementService.findFinancialStatement(financialStatementId);
        financialStatementService.deleteFinancialStatement(financialStatement);
        Proposal proposal = proposalService.findProposalByPackagePath(leosPackage.getPath());
        proposal = proposalService.removeComponentRef(proposal, financialStatement.getName());
        proposalService.updateProposal(proposal.getId(), proposal.getContent().get().getSource().getBytes());
    }

    public String getProposalId() {
        Proposal proposal = proposalService.findProposalByPackagePath(leosPackage.getPath());
        return proposal != null ? proposal.getId() : null;
    }
}
