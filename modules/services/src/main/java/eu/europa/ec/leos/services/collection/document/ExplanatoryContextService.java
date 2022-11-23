package eu.europa.ec.leos.services.collection.document;

import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.LeosPackage;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.document.Explanatory;
import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.domain.cmis.metadata.ExplanatoryMetadata;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.model.user.Collaborator;
import eu.europa.ec.leos.services.document.ExplanatoryService;
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
public class ExplanatoryContextService {

    private static final Logger LOG = LoggerFactory.getLogger(ExplanatoryContextService.class);

    private final TemplateService templateService;
    private final ExplanatoryService explanatoryService;
    private final ProposalService proposalService;
    private final SecurityService securityService;

    private LeosPackage leosPackage;
    private Explanatory explanatory;
    private String purpose = null;
    private String title = "";
    private String type = null;
    private String template = null;
    private String explanatoryId = null;
    private List<Collaborator> collaborators = null;

    private DocumentVO explanatoryDocument;
    private final Map<ContextActionService, String> actionMsgMap;
    private String versionComment;
    private String milestoneComment;

    public ExplanatoryContextService(
            TemplateService templateService,
            ExplanatoryService explanatoryService,
            ProposalService proposalService, SecurityService securityService) {
        this.templateService = templateService;
        this.explanatoryService = explanatoryService;
        this.proposalService = proposalService;
        this.securityService = securityService;
        this.actionMsgMap = new HashMap<>();
    }

    public void useTemplate(String template) {
        Validate.notNull(template, "Template name is required!");

        this.explanatory = (Explanatory) templateService.getTemplate(template);
        Validate.notNull(explanatory, "Template not found! [name=%s]", template);
        this.template = template;
        LOG.trace("Using {} template... [id={}, name={}]", explanatory.getCategory(), explanatory.getId(), explanatory.getName());
    }

    public void useActionMessageMap(Map<ContextActionService, String> messages) {
        Validate.notNull(messages, "Action message map is required!");

        actionMsgMap.putAll(messages);
    }

    public void usePackage(LeosPackage leosPackage) {
        Validate.notNull(leosPackage, "Explanatory package is required!");
        LOG.trace("Using Explanatory package... [id={}, path={}]", leosPackage.getId(), leosPackage.getPath());
        this.leosPackage = leosPackage;
    }

    public void usePurpose(String purpose) {
        Validate.notNull(purpose, "Explanatory purpose is required!");
        LOG.trace("Using Explanatory purpose... [purpose={}]", purpose);
        this.purpose = purpose;
    }

    public void useType(String type) {
        Validate.notNull(type, "Explanatory type is required!");
        LOG.trace("Using Explanatory type... [type={}]", type);
        this.type = type;
    }

    public void usePackageTemplate(String template) {
        Validate.notNull(template, "template is required!");
        LOG.trace("Using template... [template={}]", template);
        this.template = template;
    }

    public void useTitle(String title) {
        Validate.notNull(title, "Explanatory title is required!");
        LOG.trace("Using Explanatory title... [title={}]", title);
        this.title = title;
    }

    public void useExplanatory(Explanatory explanatory) {
        Validate.notNull(explanatory, "Explanatory document is required!");
        LOG.trace("Using Explanatory document'... [explanatoryId={}]", explanatory.getId());
        this.explanatory = explanatory;
    }

    public void useExplanatoryId(String explanatoryId) {
        Validate.notNull(explanatoryId, "Explanatory 'explanatoryId' is required!");
        LOG.trace("Using ExplanatoryId'... [explanatoryId={}]", explanatoryId);
        this.explanatoryId = explanatoryId;
    }

    public void useDocument(DocumentVO document) {
        Validate.notNull(document, "Explanatory document is required!");
        explanatoryDocument = document;
    }

    public void useCollaborators(List<Collaborator> collaborators) {
        Validate.notNull(collaborators, "Explanatory 'collaborators' are required!");
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

    public void useActionMessage(ContextActionService action, String actionMsg) {
        Validate.notNull(actionMsg, "Action message is required!");
        Validate.notNull(action, "Context Action not found! [name=%s]", action);

        LOG.trace("Using action message... [action={}, name={}]", action, actionMsg);
        actionMsgMap.put(action, actionMsg);
    }

    public Explanatory executeCreateExplanatory() {
        LOG.trace("Executing 'Create Explanatory' use case...");

        Validate.notNull(leosPackage, "Explanatory package is required!");
        Validate.notNull(explanatory, "Explanatory template is required!");
        Validate.notNull(collaborators, "Explanatory collaborators are required!");

        Option<ExplanatoryMetadata> metadataOption = explanatory.getMetadata();
        Validate.isTrue(metadataOption.isDefined(), "Explanatory metadata is required!");

        Validate.notNull(purpose, "Explanatory purpose is required!");
        ExplanatoryMetadata metadata = metadataOption.get().withPurpose(purpose).withType(type).withTemplate(template).withTitle(title);

        explanatory = explanatoryService.createExplanatory(explanatory.getId(), leosPackage.getPath(), metadata, actionMsgMap.get(ContextActionService.ANNEX_METADATA_UPDATED), null);
        explanatory = securityService.updateCollaborators(explanatory.getId(), collaborators, Explanatory.class);

        return explanatoryService.createVersion(explanatory.getId(), VersionType.INTERMEDIATE, actionMsgMap.get(ContextActionService.DOCUMENT_CREATED));
    }

    public Explanatory executeImportExplanatory() {
        LOG.trace("Executing 'Import Explanatory' use case...");
        Validate.notNull(leosPackage, "Explanatory package is required!");
        Validate.notNull(explanatory, "Explanatory template is required!");
        Validate.notNull(collaborators, "Explanatory collaborators are required!");
        Validate.notNull(purpose, "Explanatory purpose is required!");
        Validate.notNull(type, "Explanatory type is required!");

        final String actionMessage = actionMsgMap.get(ContextActionService.ANNEX_BLOCK_UPDATED);
        final ExplanatoryMetadata metadataDocument = (ExplanatoryMetadata) explanatoryDocument.getMetadataDocument();
        explanatory = explanatoryService.createExplanatoryFromContent(leosPackage.getPath(), metadataDocument, actionMessage, explanatoryDocument.getSource(), explanatoryDocument.getName());
        explanatory = securityService.updateCollaborators(explanatory.getId(), collaborators, Explanatory.class);

        return explanatoryService.createVersion(explanatory.getId(), VersionType.INTERMEDIATE, actionMsgMap.get(ContextActionService.DOCUMENT_CREATED));
    }

    public void executeUpdateExplanatory() {
        LOG.trace("Executing 'Update explanatory metadata' use case...");
        Validate.notNull(purpose, "Explanatory purpose is required!");
        Validate.notNull(explanatory, "Explanatory document is required!");

        Option<ExplanatoryMetadata> metadataOption = explanatory.getMetadata();
        Validate.isTrue(metadataOption.isDefined(), "Explanatory metadata is required!");

        // Updating only purpose at this time. other metadata needs to be set, if needed
        ExplanatoryMetadata explanatoryMetadata = metadataOption.get().withPurpose(purpose);
        explanatoryService.updateExplanatory(explanatory, explanatoryMetadata, VersionType.MINOR, actionMsgMap.get(ContextActionService.METADATA_UPDATED));
    }

    public void executeUpdateExplanatoryStructure() {
        byte[] xmlContent = getContent(explanatory); //Use the content from template
        explanatory = explanatoryService.findExplanatory(explanatoryId); //Get the existing explanatory document

        Option<ExplanatoryMetadata> metadataOption = explanatory.getMetadata();
        Validate.isTrue(metadataOption.isDefined(), "Explanatory metadata is required!");
        ExplanatoryMetadata metadata = metadataOption.get();
        ExplanatoryMetadata explanatoryMetadata = metadata
                .withPurpose(metadata.getPurpose())
                .withType(metadata.getType())
                .withTemplate(template)
                .withDocVersion(metadata.getDocVersion())
                .withDocTemplate(template);

        explanatory = explanatoryService.updateExplanatory(explanatory, xmlContent, explanatoryMetadata, VersionType.INTERMEDIATE, actionMsgMap.get(ContextActionService.ANNEX_STRUCTURE_UPDATED));
    }

    private byte[] getContent(Explanatory explanatory) {
        final Content content = explanatory.getContent().getOrError(() -> "Explanatory content is required!");
        return content.getSource().getBytes();
    }

    public void executeCreateMilestone() {
        explanatory = explanatoryService.findExplanatory(explanatoryId);
        List<String> milestoneComments = explanatory.getMilestoneComments();
        milestoneComments.add(milestoneComment);
        if (explanatory.getVersionType().equals(VersionType.MAJOR)) {
            explanatory = explanatoryService.updateExplanatoryWithMilestoneComments(explanatory.getId(), milestoneComments);
            LOG.info("Major version {} already present. Updated only milestoneComment for [explanatory={}]", explanatory.getVersionLabel(), explanatory.getId());
        } else {
            explanatory = explanatoryService.updateExplanatoryWithMilestoneComments(explanatory, milestoneComments, VersionType.MAJOR, versionComment);
            LOG.info("Created major version {} for [explanatory={}]", explanatory.getVersionLabel(), explanatory.getId());
        }
    }

    public String getUpdatedExplanatoryId() {
        return explanatory.getId();
    }

    public String getProposalId() {
        Proposal proposal = proposalService.findProposalByPackagePath(leosPackage.getPath());
        return proposal != null ? proposal.getId() : null;
    }
}
