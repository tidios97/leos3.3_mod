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

import eu.europa.ec.leos.domain.cmis.LeosPackage;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.document.Explanatory;
import eu.europa.ec.leos.domain.cmis.document.FinancialStatement;
import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.domain.cmis.metadata.ProposalMetadata;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.services.document.ExplanatoryService;
import eu.europa.ec.leos.services.document.ProposalService;
import eu.europa.ec.leos.services.store.PackageService;
import eu.europa.ec.leos.services.store.TemplateService;
import io.atlassian.fugue.Option;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Provider;
import java.util.List;
import java.util.Map;

import static eu.europa.ec.leos.domain.cmis.LeosCategory.*;

@Component
@Scope("prototype")
@Instance(InstanceType.COUNCIL)
public class CollectionContextMandate extends CollectionContext {

    private static final Logger LOG = LoggerFactory.getLogger(CollectionContextMandate.class);

    private final ExplanatoryService explanatoryService;
    private final Provider<ExplanatoryContext> explanatoryContextProvider;
    private final MessageHelper messageHelper;
    private String explanatoryId;

    @Autowired
    CollectionContextMandate(TemplateService templateService,
                             PackageService packageService,
                             ProposalService proposalService, Provider<ExplanatoryContext> explanatoryContextProvider,
                             Provider<MemorandumContext> memorandumContextProvider,
                             Provider<BillContext> billContextProvider,
                             ExplanatoryService explanatoryService, MessageHelper messageHelper) {
        super(templateService, packageService, proposalService, memorandumContextProvider, billContextProvider);
        this.explanatoryContextProvider = explanatoryContextProvider;
        this.explanatoryService = explanatoryService;
        this.messageHelper = messageHelper;
    }

    @Override
    protected void createExplanatoryMilestones(LeosPackage leosPackage) {
        final List<Explanatory> explanatories = packageService.findDocumentsByPackagePath(leosPackage.getPath(), Explanatory.class, false);
        explanatories.forEach(explanatory -> {
            ExplanatoryContext explanatoryContext = explanatoryContextProvider.get();
            explanatoryContext.useExplanatoryId(explanatory.getId());
            explanatoryContext.useVersionComment(versionComment);
            explanatoryContext.useMilestoneComment(milestoneComment);
            explanatoryContext.executeCreateMilestone();
        });
    }

    @Override
    public void executeCreateExplanatory() {
        LeosPackage leosPackage = packageService.findPackageByDocumentId(proposal.getId());
        ExplanatoryContext explanatoryContext = explanatoryContextProvider.get();
        explanatoryContext.usePackage(leosPackage);
        String template = categoryTemplateMap.get(COUNCIL_EXPLANATORY).getName();
        explanatoryContext.useTemplate(template);
        explanatoryContext.usePurpose(purpose);
        explanatoryContext.useTitle(messageHelper.getMessage("document.default.explanatory.title.default." + template));
        Option<ProposalMetadata> metadataOption = proposal.getMetadata();
        Validate.isTrue(metadataOption.isDefined(), "Proposal metadata is required!");
        ProposalMetadata metadata = metadataOption.get();
        explanatoryContext.useType(metadata.getType());
        explanatoryContext.useActionMessageMap(actionMsgMap);
        explanatoryContext.useCollaborators(proposal.getCollaborators());
        Explanatory explanatory = explanatoryContext.executeCreateExplanatory();
        proposalService.addComponentRef(proposal, explanatory.getName(), COUNCIL_EXPLANATORY);
        proposalService.createVersion(proposal.getId(), VersionType.INTERMEDIATE, actionMsgMap.get(ContextAction.DOCUMENT_CREATED));
    }

    @Override
    public void useExplanatory(String explanatoryId) {
        Validate.notNull(explanatoryId, "Proposal 'explId' is required!");
        LOG.trace("Using Proposal explanatory id [explId={}]", explanatoryId);
        this.explanatoryId = explanatoryId;
    }

    @Override
    public void executeRemoveExplanatory() {
        LOG.trace("Executing 'Remove council explanatory' use case...");
        Validate.notNull(leosPackage, "Leos package is required!");
        Explanatory explanatory = explanatoryService.findExplanatory(explanatoryId);
        explanatoryService.deleteExplanatory(explanatory);
        Proposal proposal = proposalService.findProposalByPackagePath(leosPackage.getPath());
        proposal = proposalService.removeComponentRef(proposal, explanatory.getName());
        proposalService.updateProposal(proposal.getId(), proposal.getContent().get().getSource().getBytes());
    }

    @Override
    public Explanatory getExplanatory(ProposalMetadata metadata, ExplanatoryContext explanatoryContext, String template,
                                      boolean createProposal) {
        explanatoryContext.useTemplate(template);
        explanatoryContext.usePurpose(purpose);
        explanatoryContext.useType(metadata.getType());
        explanatoryContext.useTitle(messageHelper.getMessage("document.default.explanatory.title.default." + template));
        explanatoryContext.useActionMessageMap(actionMsgMap);
        explanatoryContext.useCollaborators(proposal.getCollaborators());
        Explanatory explanatory = explanatoryContext.executeCreateExplanatory();
        return explanatory;
    }

    @Override
    public void executeCreateProposal() {
        LOG.trace("Executing 'Create Proposal' use case...");
        LeosPackage leosPackage = packageService.createPackage();
        Proposal proposalTemplate = cast(categoryTemplateMap.get(PROPOSAL));
        proposal = proposal == null ?  proposalTemplate : proposal;
        Validate.notNull(proposalTemplate, "Proposal template is required!");
        Option<ProposalMetadata> metadataOption = proposalTemplate.getMetadata();
        Validate.isTrue(metadataOption.isDefined(), "Proposal metadata is required!");
        Validate.notNull(purpose, "Proposal purpose is required!");
        ProposalMetadata metadata = metadataOption.get().withPurpose(purpose).withEeaRelevance(eeaRelevance);

        String explanatoryTemplate = categoryTemplateMap.get(COUNCIL_EXPLANATORY).getName();
        ExplanatoryContext explanatoryContext = explanatoryContextProvider.get();
        explanatoryContext.usePackage(leosPackage);
        explanatoryContext.useCollaborators(proposalTemplate.getCollaborators());
        Explanatory explanatory = getExplanatory(metadata, explanatoryContext, explanatoryTemplate, true);

        Proposal proposal = proposalService.createProposal(proposalTemplate.getId(), leosPackage.getPath(), metadata, null);
        proposalService.addComponentRef(proposal, explanatory.getName(), COUNCIL_EXPLANATORY);
        proposalService.createVersion(proposal.getId(), VersionType.INTERMEDIATE, actionMsgMap.get(ContextAction.DOCUMENT_CREATED));
    }

    @Override
    protected void executeUpdateExplanatory(LeosPackage leosPackage, String purpose, Map<ContextAction, String> actionMsgMap) {
        List<Explanatory> explanatories = explanatoryService.findCouncilExplanatoryByPackagePath(leosPackage.getPath());
        explanatories.forEach(explanatory -> {
            ExplanatoryContext explanatoryContext = explanatoryContextProvider.get();
            explanatoryContext.useExplanatory(explanatory);
            explanatoryContext.usePurpose(purpose);
            explanatoryContext.useActionMessageMap(actionMsgMap);
            explanatoryContext.executeUpdateExplanatory();
        });

    }

    @Override
    public void executeCreateFinancialStatement() {
    }
}
