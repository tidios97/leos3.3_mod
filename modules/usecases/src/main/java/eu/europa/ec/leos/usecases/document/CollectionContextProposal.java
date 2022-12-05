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
import eu.europa.ec.leos.domain.cmis.document.*;
import eu.europa.ec.leos.domain.cmis.metadata.ProposalMetadata;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.instance.Instance;
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
import java.util.Map;

import static eu.europa.ec.leos.domain.cmis.LeosCategory.*;
import static eu.europa.ec.leos.domain.cmis.LeosCategory.COUNCIL_EXPLANATORY;

@Component
@Scope("prototype")
@Instance(instances = {InstanceType.COMMISSION, InstanceType.OS})
public class CollectionContextProposal extends CollectionContext {

    private static final Logger LOG = LoggerFactory.getLogger(CollectionContextProposal.class);

    private final MessageHelper messageHelper;

    @Autowired
    CollectionContextProposal(TemplateService templateService,
                              PackageService packageService,
                              ProposalService proposalService,
                              Provider<MemorandumContext> memorandumContextProvider,
                              Provider<BillContext> billContextProvider, MessageHelper messageHelper) {
        super(templateService, packageService, proposalService, memorandumContextProvider, billContextProvider);
        this.messageHelper = messageHelper;
    }
    @Override
    protected void createExplanatoryMilestones(LeosPackage leosPackage) {
    }
    @Override
    public void executeCreateExplanatory() {
    }
    @Override
    public void useExplanatory(String explanatoryId) {
    }
    @Override
    public void executeRemoveExplanatory() {
    }
    @Override
    public Explanatory getExplanatory(ProposalMetadata metadata, ExplanatoryContext explanatoryContext, String template, boolean createProposal) {
        return null;
    }
    @Override
    protected void executeUpdateExplanatory(LeosPackage leosPackage, String purpose, Map<ContextAction, String> actionMsgMap) {
    }

    public void executeCreateProposal() {
        LOG.trace("Executing 'Create Proposal' use case...");

        LeosPackage leosPackage = packageService.createPackage();

        Proposal proposalTemplate = cast(categoryTemplateMap.get(PROPOSAL));
        Validate.notNull(proposalTemplate, "Proposal template is required!");

        Option<ProposalMetadata> metadataOption = proposalTemplate.getMetadata();
        Validate.isTrue(metadataOption.isDefined(), "Proposal metadata is required!");

        Validate.notNull(purpose, "Proposal purpose is required!");
        ProposalMetadata metadata = metadataOption.get().withPurpose(purpose).withEeaRelevance(eeaRelevance);

        Proposal proposal = proposalService.createProposal(proposalTemplate.getId(), leosPackage.getPath(), metadata, null);

        // TODO: To have other structure proposal
        if (cast(categoryTemplateMap.get(MEMORANDUM)) != null) {
            MemorandumContext memorandumContext = memorandumContextProvider.get();
            memorandumContext.usePackage(leosPackage);
            memorandumContext.useTemplate(cast(categoryTemplateMap.get(MEMORANDUM)));
            memorandumContext.usePurpose(purpose);
            memorandumContext.useActionMessageMap(actionMsgMap);
            memorandumContext.useType(metadata.getType());
            memorandumContext.usePackageTemplate(metadata.getTemplate());
            memorandumContext.useEeaRelevance(eeaRelevance);
            Memorandum memorandum = memorandumContext.executeCreateMemorandum();
            proposal = proposalService.addComponentRef(proposal, memorandum.getName(), LeosCategory.MEMORANDUM);
        }

        BillContext billContext = billContextProvider.get();
        billContext.usePackage(leosPackage);
        billContext.useTemplate(cast(categoryTemplateMap.get(BILL)));
        billContext.usePurpose(purpose);
        billContext.useActionMessageMap(actionMsgMap);
        billContext.useEeaRelevance(eeaRelevance);
        Bill bill = billContext.executeCreateBill();
        proposalService.addComponentRef(proposal, bill.getName(), LeosCategory.BILL);
        proposalService.createVersion(proposal.getId(), VersionType.INTERMEDIATE, actionMsgMap.get(ContextAction.DOCUMENT_CREATED));
    }
}
