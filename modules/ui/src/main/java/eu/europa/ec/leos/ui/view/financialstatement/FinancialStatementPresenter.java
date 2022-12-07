/*
 * Copyright 2022 European Commission
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
package eu.europa.ec.leos.ui.view.financialstatement;

import com.google.common.base.Stopwatch;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.server.VaadinServletService;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.LeosPackage;
import eu.europa.ec.leos.domain.cmis.document.FinancialStatement;
import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.domain.cmis.metadata.FinancialStatementMetadata;
import eu.europa.ec.leos.domain.common.Result;
import eu.europa.ec.leos.domain.common.TocMode;
import eu.europa.ec.leos.domain.vo.CloneProposalMetadataVO;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.action.ActionType;
import eu.europa.ec.leos.model.action.CheckinCommentVO;
import eu.europa.ec.leos.model.action.CheckinElement;
import eu.europa.ec.leos.model.action.ContributionVO;
import eu.europa.ec.leos.model.action.VersionVO;
import eu.europa.ec.leos.model.event.DocumentUpdatedByCoEditorEvent;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.clone.CloneContext;
import eu.europa.ec.leos.services.collection.document.FinancialStatementContextService;
import eu.europa.ec.leos.services.document.ContributionService;
import eu.europa.ec.leos.services.document.DocumentContentService;
import eu.europa.ec.leos.services.document.FinancialStatementService;
import eu.europa.ec.leos.services.document.ProposalService;
import eu.europa.ec.leos.services.document.TransformationService;
import eu.europa.ec.leos.services.document.util.CheckinCommentUtil;
import eu.europa.ec.leos.services.export.ExportService;
import eu.europa.ec.leos.services.label.ReferenceLabelService;
import eu.europa.ec.leos.services.messaging.UpdateInternalReferencesProducer;
import eu.europa.ec.leos.services.notification.NotificationService;
import eu.europa.ec.leos.services.processor.AttachmentProcessor;
import eu.europa.ec.leos.services.processor.ElementProcessor;
import eu.europa.ec.leos.services.processor.FinancialStatementProcessor;
import eu.europa.ec.leos.services.search.SearchService;
import eu.europa.ec.leos.services.store.ExportPackageService;
import eu.europa.ec.leos.services.store.LegService;
import eu.europa.ec.leos.services.store.PackageService;
import eu.europa.ec.leos.services.store.WorkspaceService;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.ui.event.CloseBrowserRequestEvent;
import eu.europa.ec.leos.ui.event.CloseScreenRequestEvent;
import eu.europa.ec.leos.ui.event.revision.OpenRevisionDocumentEvent;
import eu.europa.ec.leos.ui.event.search.ShowConfirmDialogEvent;
import eu.europa.ec.leos.ui.event.toc.CloseTocAndDocumentEvent;
import eu.europa.ec.leos.ui.event.toc.InlineTocEditRequestEvent;
import eu.europa.ec.leos.ui.support.CoEditionHelper;
import eu.europa.ec.leos.ui.support.ConfirmDialogHelper;
import eu.europa.ec.leos.ui.view.AbstractLeosPresenter;
import eu.europa.ec.leos.ui.view.ComparisonDelegate;
import eu.europa.ec.leos.usecases.document.CollectionContext;;
import eu.europa.ec.leos.vo.coedition.InfoType;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.web.event.NavigationRequestEvent;
import eu.europa.ec.leos.web.event.NotificationEvent;
import eu.europa.ec.leos.web.event.view.AddChangeDetailsMenuEvent;
import eu.europa.ec.leos.web.event.view.document.CloseDocumentConfirmationEvent;
import eu.europa.ec.leos.web.event.view.document.CloseDocumentEvent;
import eu.europa.ec.leos.web.event.view.document.DocumentUpdatedEvent;
import eu.europa.ec.leos.web.event.view.document.SaveElementRequestEvent;
import eu.europa.ec.leos.web.model.VersionInfoVO;
import eu.europa.ec.leos.web.support.SessionAttribute;
import eu.europa.ec.leos.web.support.UrlBuilder;
import eu.europa.ec.leos.web.support.UuidHelper;
import eu.europa.ec.leos.web.support.cfg.ConfigurationHelper;
import eu.europa.ec.leos.web.support.user.UserHelper;
import eu.europa.ec.leos.web.ui.navigation.Target;
import io.atlassian.fugue.Option;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Provider;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static eu.europa.ec.leos.util.LeosDomainUtil.CMIS_PROPERTY_SPLITTER;

@Component
@Scope("prototype")
public class FinancialStatementPresenter extends AbstractLeosPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(FinancialStatementPresenter.class);

    private final FinancialStatementScreen financialStatementScreen;
    private final FinancialStatementService financialStatementService;
    private final ContributionService contributionService;
    private final ElementProcessor<FinancialStatement> elementProcessor;
    private final FinancialStatementProcessor financialStatementProcessor;
    private final DocumentContentService documentContentService;
    private final UrlBuilder urlBuilder;
    private final ComparisonDelegate<FinancialStatement> comparisonDelegate;
    private final UserHelper userHelper;
    private final MessageHelper messageHelper;
    private final ConfigurationHelper cfgHelper;
    private final Provider<CollectionContext> proposalContextProvider;
    private final CoEditionHelper coEditionHelper;
    private final ExportService exportService;
    private final Provider<StructureContext> structureContextProvider;
    private final ReferenceLabelService referenceLabelService;
    private final Provider<FinancialStatementContextService> financialStatementContextProvider;
    private final UpdateInternalReferencesProducer updateInternalReferencesProducer;
    private final TransformationService transformationService;
    private final LegService legService;
    private final ProposalService proposalService;
    private final SearchService searchService;
    private final ExportPackageService exportPackageService;
    private final NotificationService notificationService;
    private final CloneContext cloneContext;
    private CloneProposalMetadataVO cloneProposalMetadataVO;
    protected final AttachmentProcessor attachmentProcessor;

    private final static SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private String documentRef;
    private String strDocumentVersionSeriesId;
    private String documentId;
    private String proposalRef;
    private String connectedEntity;
    private final List<String> openElementEditors;

    protected FinancialStatementPresenter(SecurityContext securityContext, HttpSession httpSession, EventBus eventBus, EventBus leosApplicationEventBus, UuidHelper uuidHelper, PackageService packageService, WorkspaceService workspaceService, FinancialStatementScreen financialStatementScreen, FinancialStatementService financialStatementService, ContributionService contributionService, ElementProcessor<FinancialStatement> elementProcessor, FinancialStatementProcessor financialStatementProcessor, DocumentContentService documentContentService, UrlBuilder urlBuilder, ComparisonDelegate<FinancialStatement> comparisonDelegate, UserHelper userHelper, MessageHelper messageHelper, ConfigurationHelper cfgHelper, Provider<CollectionContext> proposalContextProvider, CoEditionHelper coEditionHelper, ExportService exportService, Provider<StructureContext> structureContextProvider, ReferenceLabelService referenceLabelService, Provider<FinancialStatementContextService> financialStatementContextProvider, UpdateInternalReferencesProducer updateInternalReferencesProducer, TransformationService transformationService, LegService legService, ProposalService proposalService, SearchService searchService, ExportPackageService exportPackageService, NotificationService notificationService, CloneContext cloneContext, AttachmentProcessor attachmentProcessor) {
        super(securityContext, httpSession, eventBus, leosApplicationEventBus, uuidHelper, packageService, workspaceService);
        this.financialStatementScreen = financialStatementScreen;
        this.financialStatementService = financialStatementService;
        this.contributionService = contributionService;
        this.elementProcessor = elementProcessor;
        this.financialStatementProcessor = financialStatementProcessor;
        this.documentContentService = documentContentService;
        this.urlBuilder = urlBuilder;
        this.comparisonDelegate = comparisonDelegate;
        this.userHelper = userHelper;
        this.messageHelper = messageHelper;
        this.cfgHelper = cfgHelper;
        this.proposalContextProvider = proposalContextProvider;
        this.coEditionHelper = coEditionHelper;
        this.exportService = exportService;
        this.structureContextProvider = structureContextProvider;
        this.referenceLabelService = referenceLabelService;
        this.financialStatementContextProvider = financialStatementContextProvider;
        this.updateInternalReferencesProducer = updateInternalReferencesProducer;
        this.transformationService = transformationService;
        this.legService = legService;
        this.proposalService = proposalService;
        this.searchService = searchService;
        this.exportPackageService = exportPackageService;
        this.notificationService = notificationService;
        this.cloneContext = cloneContext;
        this.attachmentProcessor = attachmentProcessor;
        this.openElementEditors = new ArrayList<>();
    }

    @Override
    public void enter() {
        super.enter();
        init();
    }

    @Override
    public void detach() {
        super.detach();
        coEditionHelper.removeUserEditInfo(id, strDocumentVersionSeriesId, null, InfoType.DOCUMENT_INFO);
        resetCloneProposalMetadataVO();
    }

    private boolean isFinancialStatementUnsaved(){
        return getFinancialStatementFromSession() != null;
    }

    private FinancialStatement getFinancialStatementFromSession() {
        return (FinancialStatement) httpSession.getAttribute("financialstatement#" + getDocumentRef());
    }

    private boolean isHasOpenElementEditors() {
        return this.openElementEditors.size() > 0;
    }

    @Subscribe
    void handleCloseDocumentConfirmation(CloseDocumentConfirmationEvent event) {
        this.closeDocument();
    }

    private void closeDocument() {
        coEditionHelper.removeUserEditInfo(id, strDocumentVersionSeriesId, null, InfoType.DOCUMENT_INFO);
        resetCloneProposalMetadataVO();
        eventBus.post(new NavigationRequestEvent(Target.PREVIOUS, false));
    }

    @Subscribe
    void handleShowConfirmDialog(ShowConfirmDialogEvent event) {
        ConfirmDialogHelper.showOpenEditorDialog(this.leosUI, event, this.eventBus, this.messageHelper);
    }

    @Subscribe
    void handleCloseBrowserRequest(CloseBrowserRequestEvent event) {
        coEditionHelper.removeUserEditInfo(id, strDocumentVersionSeriesId, null, InfoType.DOCUMENT_INFO);
        resetCloneProposalMetadataVO();
    }

    @Subscribe
    void handleCloseScreenRequest(CloseScreenRequestEvent event) {
        if (financialStatementScreen.isTocEnabled()) {
            eventBus.post(new CloseTocAndDocumentEvent());
        } else {
            eventBus.post(new CloseDocumentEvent());
        }
    }

    @Subscribe
    void handleCloseDocument(CloseDocumentEvent event) {
        LOG.trace("Handling close document request...");

        //if unsaved changes remain in the session, first ask for confirmation
        if(this.isFinancialStatementUnsaved() || this.isHasOpenElementEditors()){
            eventBus.post(new ShowConfirmDialogEvent(new CloseDocumentConfirmationEvent(), null));
            return;
        }
        this.closeDocument();
    }

    private void init() {
        try {
            populateWithProposalRefAndConnectedEntity();
            FinancialStatement financialStatement = getDocument();
            populateViewData(financialStatement, TocMode.SIMPLIFIED);
            populateVersionsData(financialStatement);
            String revisionReference = getRevisionRef();
            if (revisionReference != null) {
                Optional<ContributionVO> contributionVO = financialStatementScreen.findContributionAndShowTab(revisionReference);
                if (contributionVO.isPresent()) {
                    eventBus.post(new OpenRevisionDocumentEvent(contributionVO.get()));
                }
            }
        } catch (Exception exception) {
            LOG.error("Exception occurred in init(): ", exception);
            eventBus.post(new NotificationEvent(NotificationEvent.Type.INFO, "unknown.error.message"));
        }
    }

    private void populateWithProposalRefAndConnectedEntity() {
        Proposal proposal = getProposalFromPackage();
        if (proposal != null) {
            proposalRef = proposal.getMetadata().get().getRef();
            connectedEntity = userHelper.getCollaboratorConnectedEntityByLoggedUser(proposal.getCollaborators());
            byte[] xmlContent = proposal.getContent().get().getSource().getBytes();
            if(proposal != null && proposal.isClonedProposal()) {
                populateCloneProposalMetadataVO(xmlContent);
            }
        }
    }

    private Proposal getProposalFromPackage() {
        Proposal proposal = null;
        FinancialStatement financialStatement = getDocument();
        if (financialStatement != null) {
            LeosPackage leosPackage = packageService.findPackageByDocumentId(financialStatement.getId());
            proposal = proposalService.findProposalByPackagePath(leosPackage.getPath());
        }
        return proposal;
    }

    private String getDocumentRef() {
        return (String) httpSession.getAttribute(id + "." + SessionAttribute.FINANCIAL_STATEMENT.name());
    }

    private String getRevisionRef() {
        return (String) httpSession.getAttribute(id + "." + SessionAttribute.REVISION_VERSION.name());
    }

    private FinancialStatement getDocument() {
        documentRef = getDocumentRef();
        FinancialStatement financialStatement = financialStatementService.findFinancialStatementByRef(documentRef);
        setDocumentData(financialStatement);
        return financialStatement;
    }

    private void setDocumentData(FinancialStatement financialStatement) {
        strDocumentVersionSeriesId = financialStatement.getVersionSeriesId();
        documentId = financialStatement.getId();
        structureContextProvider.get().useDocumentTemplate(financialStatement.getMetadata().getOrError(() -> "Financial statement metadata is required!").getDocTemplate());
        cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);
    }

    private List<VersionVO> getVersionVOS() {
        return financialStatementService.getAllVersions(documentId, documentRef);
    }

    private void populateViewData(FinancialStatement financialStatement, TocMode mode) {
        Validate.notNull(financialStatement, "Financial statement document should not be null");
        try{
            Option<FinancialStatementMetadata> financialStatementMetadata = financialStatement.getMetadata();
            if (financialStatementMetadata.isDefined()) {
                financialStatementScreen.setTitle(financialStatementMetadata.get().getTitle());
            }
            financialStatementScreen.setContent(getEditableXml(financialStatement));
            financialStatementScreen.setToc(getTableOfContent(financialStatement, mode));
            DocumentVO financialStatementVO = createFinancialStatementVO(financialStatement);
            financialStatementScreen.updateUserCoEditionInfo(coEditionHelper.getCurrentEditInfo(financialStatement.getVersionSeriesId()), id);
            financialStatementScreen.setPermissions(financialStatementVO, isClonedProposal());
            financialStatementScreen.initAnnotations(financialStatementVO, proposalRef, connectedEntity);
            if(isClonedProposal()) {
                eventBus.post(new AddChangeDetailsMenuEvent());
            }
        }
        catch (Exception ex) {
            LOG.error("Error while processing document", ex);
            eventBus.post(new NotificationEvent(NotificationEvent.Type.INFO, "error.message", ex.getMessage()));
        }
    }

    private String getEditableXml(FinancialStatement financialStatement) {
        VersionInfoVO versionInfoVO = getVersionInfo(financialStatement);
        String baseRevisionId = financialStatement.getBaseRevisionId();
        cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);

        if(!StringUtils.isEmpty(baseRevisionId) && baseRevisionId.split(CMIS_PROPERTY_SPLITTER).length >= 3) {
            String versionLabel = baseRevisionId.split(CMIS_PROPERTY_SPLITTER)[1];
            String versionComment = baseRevisionId.split(CMIS_PROPERTY_SPLITTER)[2];
            versionInfoVO.setRevisedBaseVersion(versionLabel);
            versionInfoVO.setBaseVersionTitle(versionComment);
        }
        financialStatementScreen.setDocumentVersionInfo(versionInfoVO);
        byte[] coverPageContent = new byte[0];
        byte[] financialStatementContent = financialStatement.getContent().get().getSource().getBytes();
        boolean isCoverPageExists = documentContentService.isCoverPageExists(financialStatementContent);
        if(financialStatementScreen.isCoverPageVisible() && !isCoverPageExists) {
            Proposal proposal = getProposalFromPackage();
            byte[] xmlContent = proposal.getContent().get().getSource().getBytes();
            coverPageContent = documentContentService.getCoverPageContent(xmlContent);
        }
        String editableXml = documentContentService.toEditableContent(financialStatement,
                urlBuilder.getWebAppPath(VaadinServletService.getCurrentServletRequest()), securityContext, coverPageContent);
        return StringEscapeUtils.unescapeXml(editableXml);
    }

    @Subscribe
    void editInlineToc(InlineTocEditRequestEvent event) {
        FinancialStatement financialStatement = getDocument();
        coEditionHelper.storeUserEditInfo(httpSession.getId(), id, user, strDocumentVersionSeriesId, null, InfoType.TOC_INFO);
        financialStatementScreen.enableTocEdition(getTableOfContent(financialStatement, TocMode.NOT_SIMPLIFIED));
    }


    @Subscribe
    void saveElement(SaveElementRequestEvent event) {
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            final String elementId = event.getElementId();
            final String elementType = event.getElementTagName();
            final String elementFragment = event.getElementContent();

            FinancialStatement financialStatement = getDocument();
            byte[] newXmlContent = financialStatementProcessor.updateElement(financialStatement, elementType, elementId, elementFragment);
            if (newXmlContent == null) {
                financialStatementScreen.showAlertDialog("operation.element.not.performed");
                return;
            }

            final String title = messageHelper.getMessage("operation.element.updated", StringUtils.capitalize(elementType));
            final String description = messageHelper.getMessage("operation.checkin.minor");
            final String elementLabel = generateLabel(elementId, financialStatement);
            final CheckinCommentVO checkinComment = new CheckinCommentVO(title, description, new CheckinElement(ActionType.UPDATED, elementId, elementType,
                    elementLabel));
            final String checkinCommentJson = CheckinCommentUtil.getJsonObject(checkinComment);

            if (financialStatement != null) {
                financialStatement = financialStatementService.updateFinancialStatement(financialStatement, newXmlContent, checkinCommentJson);
                eventBus.post(new DocumentUpdatedEvent());
                financialStatementScreen.scrollToMarkedChange(elementId);
                leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
            }
            LOG.info("Element '{}' in Financial Statement {} id {}, saved in {} milliseconds ({} sec)", elementId, financialStatement.getName(),
                    financialStatement.getId(), stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
        } catch (Exception ex) {
            LOG.error("Exception while saving element operation for ", ex);
            eventBus.post(new NotificationEvent(NotificationEvent.Type.ERROR, "error.message", ex.getMessage()));
        }
    }

    private VersionInfoVO getVersionInfo(XmlDocument document) {
        String userId = document.getLastModifiedBy();
        User user = userHelper.getUser(userId);

        return new VersionInfoVO(
                document.getVersionLabel(),
                user.getName(), user.getDefaultEntity() != null ? user.getDefaultEntity().getOrganizationName() : "",
                dateFormatter.format(Date.from(document.getLastModificationInstant())),
                document.getVersionType());
    }

    private List<TableOfContentItemVO> getTableOfContent(FinancialStatement financialStatement, TocMode mode) {
        return financialStatementService.getTableOfContent(financialStatement, mode);
    }

    private DocumentVO createFinancialStatementVO(FinancialStatement financialStatement) {
        DocumentVO financialStatementVO =
                new DocumentVO(financialStatement.getId(),
                        financialStatement.getMetadata().exists(m -> m.getLanguage() != null) ? financialStatement.getMetadata().get().getLanguage() : "EN",
                        LeosCategory.FINANCIAL_STATEMENT,
                        financialStatement.getLastModifiedBy(),
                        Date.from(financialStatement.getLastModificationInstant()));

        if (financialStatement.getMetadata().isDefined()) {
            FinancialStatementMetadata financialStatementMetadata = financialStatement.getMetadata().get();
            financialStatementVO.setTitle(financialStatementMetadata.getTitle());
            financialStatementVO.getMetadata().setInternalRef(financialStatementMetadata.getRef());
        }
        if(!financialStatement.getCollaborators().isEmpty()) {
            financialStatementVO.addCollaborators(financialStatement.getCollaborators());
        }
        return financialStatementVO;
    }

    private void populateVersionsData(FinancialStatement financialStatement) {
        DocumentVO financialStatementVO = createFinancialStatementVO(financialStatement);
        final List<VersionVO> allVersions = getVersionVOS();
        final List<ContributionVO> allContributions = contributionService.getDocumentContributions(documentId, 0, FinancialStatement.class);
        financialStatementScreen.setDataFunctions(
                financialStatementVO,
                allVersions,
                allContributions,
                this::majorVersionsFn, this::countMajorVersionsFn,
                this::minorVersionsFn, this::countMinorVersionsFn,
                this::recentChangesFn, this::countRecentChangesFn);
    }

    private Integer countMinorVersionsFn(String currIntVersion) {
        return financialStatementService.findAllMinorsCountForIntermediate(documentRef, currIntVersion);
    }

    private List<FinancialStatement> minorVersionsFn(String currIntVersion, int startIndex, int maxResults) {
        return financialStatementService.findAllMinorsForIntermediate(documentRef, currIntVersion, startIndex, maxResults);
    }

    private Integer countMajorVersionsFn() {
        return financialStatementService.findAllMajorsCount(documentRef);
    }

    private List<FinancialStatement> majorVersionsFn(int startIndex, int maxResults) {
        return financialStatementService.findAllMajors(documentRef, startIndex, maxResults);
    }

    private Integer countRecentChangesFn() {
        return financialStatementService.findRecentMinorVersionsCount(documentId, documentRef);
    }

    private List<FinancialStatement> recentChangesFn(int startIndex, int maxResults) {
        return financialStatementService.findRecentMinorVersions(documentId, documentRef, startIndex, maxResults);
    }

    private void resetCloneProposalMetadataVO() {
        cloneContext.setCloneProposalMetadataVO(null);
    }

    private void populateCloneProposalMetadataVO(byte[] xmlContent) {
        cloneProposalMetadataVO = proposalService.getClonedProposalMetadata(xmlContent);
        cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);
    }

    private boolean isClonedProposal() {
        cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);
        return cloneContext != null && cloneContext.isClonedProposal();
    }

    private String generateLabel(String reference, XmlDocument sourceDocument) {
        final byte[] sourceXmlContent = sourceDocument.getContent().get().getSource().getBytes();
        Result<String> updatedLabel = referenceLabelService.generateLabelStringRef(Arrays.asList(reference), sourceDocument.getMetadata().get().getRef(), sourceXmlContent);
        return updatedLabel.get();
    }
}
