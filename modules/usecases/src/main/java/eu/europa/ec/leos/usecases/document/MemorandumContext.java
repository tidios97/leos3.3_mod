/*
 * Copyright 2017 European Commission
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
import eu.europa.ec.leos.domain.cmis.document.Memorandum;
import eu.europa.ec.leos.domain.cmis.metadata.MemorandumMetadata;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.services.document.MemorandumService;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.processor.node.XmlNodeConfigProcessor;
import eu.europa.ec.leos.services.processor.node.XmlNodeProcessor;
import io.atlassian.fugue.Option;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static eu.europa.ec.leos.services.processor.node.XmlNodeConfigProcessor.createValueMap;
import static eu.europa.ec.leos.services.support.XmlHelper.XML_DOC_EXT;

@Component
@Scope("prototype")
public class MemorandumContext {

    private static final Logger LOG = LoggerFactory.getLogger(MemorandumContext.class);

    private final MemorandumService memorandumService;
    private final XmlContentProcessor xmlContentProcessor;
    private final XmlNodeProcessor xmlNodeProcessor;
    private final XmlNodeConfigProcessor xmlNodeConfigProcessor;

    private LeosPackage leosPackage = null;
    private Memorandum memorandum = null;
    private String purpose = null;
    private String versionComment;
    private String milestoneComment;
    private String type = null;
    private String template = null;
    private boolean eeaRelevance;

    private DocumentVO memoDocument;

    private final Map<ContextAction, String> actionMsgMap;

    @Autowired
    MemorandumContext(MemorandumService memorandumService, XmlContentProcessor xmlContentProcessor, XmlNodeProcessor xmlNodeProcessor, XmlNodeConfigProcessor xmlNodeConfigProcessor) {
        this.memorandumService = memorandumService;
        this.xmlContentProcessor = xmlContentProcessor;
        this.xmlNodeProcessor = xmlNodeProcessor;
        this.xmlNodeConfigProcessor = xmlNodeConfigProcessor;
        this.actionMsgMap = new HashMap<>();
    }

    public void usePackage(LeosPackage leosPackage) {
        Validate.notNull(leosPackage, "Memorandum package is required!");
        LOG.trace("Using Memorandum package... [id={}, path={}]", leosPackage.getId(), leosPackage.getPath());
        this.leosPackage = leosPackage;
    }

    public void useTemplate(Memorandum memorandum) {
        Validate.notNull(memorandum, "Memorandum template is required!");
        LOG.trace("Using Memorandum template... [id={}, name={}]", memorandum.getId(), memorandum.getName());
        this.memorandum = memorandum;
    }

    public void useActionMessageMap(Map<ContextAction, String> messages) {
        Validate.notNull(messages, "Action message map is required!");

        actionMsgMap.putAll(messages);
    }

    public void usePurpose(String purpose) {
        Validate.notNull(purpose, "Memorandum purpose is required!");
        LOG.trace("Using Memorandum purpose: {}", purpose);
        this.purpose = purpose;
    }

    public void useDocument(DocumentVO document) {
        Validate.notNull(document, "Memorandum document is required!");
        memoDocument = document;
    }

    public void useVersionComment(String comment) {
        Validate.notNull(comment, "Version comment is required!");
        this.versionComment = comment;
    }

    public void useMilestoneComment(String milestoneComment) {
        Validate.notNull(milestoneComment, "milestoneComment is required!");
        this.milestoneComment = milestoneComment;
    }
    
    public void useType(String type) {
        Validate.notNull(type, "type is required!");
        LOG.trace("Using type... [type={}]", type);
        this.type = type;
    }

    public void usePackageTemplate(String template) {
        Validate.notNull(template, "template is required!");
        LOG.trace("Using template... [template={}]", template);
        this.template = template;
    }

    public void useEeaRelevance(boolean eeaRelevance) {
        LOG.trace("Using EEA Relevance... [eeaRelevance={}]", eeaRelevance);
        this.eeaRelevance = eeaRelevance;
    }
    
    public Memorandum executeCreateMemorandum() {
        LOG.trace("Executing 'Create Memorandum' use case...");
        Validate.notNull(leosPackage, "Memorandum package is required!");
        Validate.notNull(memorandum, "Memorandum template is required!");

        Option<MemorandumMetadata> metadataOption = memorandum.getMetadata();
        Validate.isTrue(metadataOption.isDefined(), "Memorandum metadata is required!");

        Validate.notNull(purpose, "Memorandum purpose is required!");
        MemorandumMetadata metadata = metadataOption.get().withPurpose(purpose).withType(type).withTemplate(template).withEeaRelevance(eeaRelevance);

        Memorandum memorandumCreated = memorandumService.createMemorandum(memorandum.getId(), leosPackage.getPath(), metadata, actionMsgMap.get(ContextAction.METADATA_UPDATED), null);
        return memorandumService.createVersion(memorandumCreated.getId(), VersionType.INTERMEDIATE, actionMsgMap.get(ContextAction.DOCUMENT_CREATED));
    }

    public void executeUpdateMemorandum() {
        LOG.trace("Executing 'Update Memorandum' use case...");

        Validate.notNull(leosPackage, "Memorandum package is required!");
        Memorandum memorandum = memorandumService.findMemorandumByPackagePath(leosPackage.getPath());
        if (memorandum != null) {
            Option<MemorandumMetadata> metadataOption = memorandum.getMetadata();
            Validate.isTrue(metadataOption.isDefined(), "Memorandum metadata is required!");

            Validate.notNull(purpose, "Memorandum purpose is required!");
            MemorandumMetadata metadata = metadataOption.get().withPurpose(purpose).withEeaRelevance(eeaRelevance);

            memorandumService.updateMemorandum(memorandum, metadata, VersionType.MINOR, actionMsgMap.get(ContextAction.METADATA_UPDATED));
        }
    }

    public Memorandum executeImportMemorandum() {
        LOG.trace("Executing 'Import Memorandum' use case...");
        Validate.notNull(leosPackage, "Memorandum package is required!");
        Validate.notNull(memorandum, "Memorandum template is required!");
        Option<MemorandumMetadata> metadataOption = memorandum.getMetadata();
        Validate.isTrue(metadataOption.isDefined(), "Memorandum metadata is required!");
        Validate.notNull(purpose, "Memorandum purpose is required!");
        Validate.notNull(memoDocument.getSource(), "Memorandum xml is required!");

        createRefForMemorandum();

        Memorandum memorandumCreated = memorandumService.createMemorandumFromContent(leosPackage.getPath(), getMemoMetadata(),
                actionMsgMap.get(ContextAction.METADATA_UPDATED), memoDocument.getSource(), memoDocument.getName());
        return memorandumService.createVersion(memorandumCreated.getId(), VersionType.INTERMEDIATE, actionMsgMap.get(ContextAction.DOCUMENT_CREATED));
    }

    private MemorandumMetadata getMemoMetadata() {
        return (MemorandumMetadata)memoDocument.getMetadataDocument();
    }

    private String createRefForMemorandum() {
        Validate.notNull(memoDocument.getSource(), "Memorandum xml is required!");
        Validate.isTrue(memorandum.getMetadata().isDefined(), "Memorandum metadata is required!");

        final String ref = memorandumService.generateMemorandumReference(memorandum.getContent().get().getSource().getBytes(), memorandum.getMetadata().get().getLanguage());
        final MemorandumMetadata updatedMemorandumMetadata = memorandum.getMetadata().get()
                .withPurpose(purpose)
                .withRef(ref);
        final byte[] updatedSource = xmlNodeProcessor.setValuesInXml(memoDocument.getSource(), createValueMap(updatedMemorandumMetadata),
                xmlNodeConfigProcessor.getConfig(updatedMemorandumMetadata.getCategory()));

        memoDocument.setName(ref + XML_DOC_EXT);
        memoDocument.setMetadataDocument(updatedMemorandumMetadata);
        memoDocument.setSource(updatedSource);

        return ref;
    }

    public void executeCreateMilestone() {
        Memorandum memorandum = memorandumService.findMemorandumByPackagePath(leosPackage.getPath());
        if (memorandum != null) {
            List<String> milestoneComments = memorandum.getMilestoneComments();
            milestoneComments.add(milestoneComment);
            if (memorandum.getVersionType().equals(VersionType.MAJOR)) {
                memorandum = memorandumService.updateMemorandumWithMilestoneComments(memorandum.getId(), milestoneComments);
                LOG.info("Major version {} already present. Updated only milestoneComment for [memorandum={}]", memorandum.getVersionLabel(), memorandum.getId());
            } else {
                memorandum = memorandumService.updateMemorandumWithMilestoneComments(memorandum, milestoneComments, VersionType.MAJOR, versionComment);
                LOG.info("Created major version {} for [memorandum={}]", memorandum.getVersionLabel(), memorandum.getId());
            }
        }
    }
}
