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
package eu.europa.ec.leos.ui.view.coverpage;

import com.google.common.base.Stopwatch;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.server.VaadinServletService;
import eu.europa.ec.leos.cmis.domain.ContentImpl;
import eu.europa.ec.leos.cmis.domain.SourceImpl;
import eu.europa.ec.leos.cmis.mapping.CmisProperties;
import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.LeosPackage;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.document.LegDocument;
import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.domain.cmis.metadata.LeosMetadata;
import eu.europa.ec.leos.domain.cmis.metadata.ProposalMetadata;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.domain.common.TocMode;
import eu.europa.ec.leos.domain.vo.CloneProposalMetadataVO;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.domain.vo.SearchMatchVO;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.action.ContributionVO;
import eu.europa.ec.leos.model.action.VersionVO;
import eu.europa.ec.leos.model.event.DocumentUpdatedByCoEditorEvent;
import eu.europa.ec.leos.model.event.UpdateUserInfoEvent;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.model.xml.Element;
import eu.europa.ec.leos.security.LeosPermission;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.Annotate.AnnotateService;
import eu.europa.ec.leos.services.clone.CloneContext;
import eu.europa.ec.leos.services.document.ContributionService;
import eu.europa.ec.leos.services.document.DocumentContentService;
import eu.europa.ec.leos.services.document.ProposalService;
import eu.europa.ec.leos.services.document.TransformationService;
import eu.europa.ec.leos.services.export.ExportLW;
import eu.europa.ec.leos.services.export.ExportOptions;
import eu.europa.ec.leos.services.export.ExportService;
import eu.europa.ec.leos.services.export.ExportVersions;
import eu.europa.ec.leos.services.processor.AttachmentProcessor;
import eu.europa.ec.leos.services.processor.ElementProcessor;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.search.SearchService;
import eu.europa.ec.leos.services.store.LegService;
import eu.europa.ec.leos.services.store.PackageService;
import eu.europa.ec.leos.services.store.WorkspaceService;
import eu.europa.ec.leos.services.support.XmlHelper;
import eu.europa.ec.leos.services.template.TemplateConfigurationService;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.ui.component.ComparisonComponent;
import eu.europa.ec.leos.ui.event.CloseBrowserRequestEvent;
import eu.europa.ec.leos.ui.event.CloseScreenRequestEvent;
import eu.europa.ec.leos.ui.event.DownloadActualVersionRequestEvent;
import eu.europa.ec.leos.ui.event.DownloadCleanVersion;
import eu.europa.ec.leos.ui.event.DownloadXmlVersionRequestEvent;
import eu.europa.ec.leos.ui.event.FetchMilestoneByVersionedReferenceEvent;
import eu.europa.ec.leos.ui.event.InitLeosEditorEvent;
import eu.europa.ec.leos.ui.event.contribution.ApplyContributionsRequestEvent;
import eu.europa.ec.leos.ui.event.contribution.CompareAndShowRevisionEvent;
import eu.europa.ec.leos.ui.event.contribution.MergeActionRequestEvent;
import eu.europa.ec.leos.ui.event.metadata.DocumentMetadataRequest;
import eu.europa.ec.leos.ui.event.metadata.DocumentMetadataResponse;
import eu.europa.ec.leos.ui.event.metadata.SearchMetadataRequest;
import eu.europa.ec.leos.ui.event.metadata.SearchMetadataResponse;
import eu.europa.ec.leos.ui.event.revision.DeclineRevisionDocumentEvent;
import eu.europa.ec.leos.ui.event.revision.OpenRevisionDocumentEvent;
import eu.europa.ec.leos.ui.event.revision.RevisionDocumentProcessedEvent;
import eu.europa.ec.leos.ui.event.search.ReplaceAllMatchRequestEvent;
import eu.europa.ec.leos.ui.event.search.ReplaceAllMatchResponseEvent;
import eu.europa.ec.leos.ui.event.search.ReplaceMatchRequestEvent;
import eu.europa.ec.leos.ui.event.search.SaveAfterReplaceEvent;
import eu.europa.ec.leos.ui.event.search.SaveAndCloseAfterReplaceEvent;
import eu.europa.ec.leos.ui.event.search.SearchBarClosedEvent;
import eu.europa.ec.leos.ui.event.search.SearchTextRequestEvent;
import eu.europa.ec.leos.ui.event.search.ShowConfirmDialogEvent;
import eu.europa.ec.leos.ui.event.toc.CloseTocAndDocumentEvent;
import eu.europa.ec.leos.ui.event.view.DownloadXmlFilesRequestEvent;
import eu.europa.ec.leos.ui.event.view.ToolBoxExportRequestEvent;
import eu.europa.ec.leos.ui.model.AnnotateMetadata;
import eu.europa.ec.leos.ui.model.AnnotationStatus;
import eu.europa.ec.leos.services.clone.InternalRefMap;
import eu.europa.ec.leos.ui.support.CoEditionHelper;
import eu.europa.ec.leos.ui.support.ConfirmDialogHelper;
import eu.europa.ec.leos.ui.view.AbstractLeosPresenter;
import eu.europa.ec.leos.ui.view.CommonDelegate;
import eu.europa.ec.leos.ui.view.ComparisonDelegate;
import eu.europa.ec.leos.ui.view.MergeContributionHelper;
import eu.europa.ec.leos.ui.window.milestone.MilestoneExplorer;
import eu.europa.ec.leos.usecases.document.BillContext;
import eu.europa.ec.leos.usecases.document.CollectionContext;
import eu.europa.ec.leos.usecases.document.ContextAction;
import eu.europa.ec.leos.vo.coedition.InfoType;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItem;
import eu.europa.ec.leos.web.event.NavigationRequestEvent;
import eu.europa.ec.leos.web.event.NotificationEvent;
import eu.europa.ec.leos.web.event.NotificationEvent.Type;
import eu.europa.ec.leos.web.event.component.CleanComparedContentEvent;
import eu.europa.ec.leos.web.event.component.CompareRequestEvent;
import eu.europa.ec.leos.web.event.component.LayoutChangeRequestEvent;
import eu.europa.ec.leos.web.event.component.ResetRevisionComponentEvent;
import eu.europa.ec.leos.web.event.component.RestoreVersionRequestEvent;
import eu.europa.ec.leos.web.event.component.ShowVersionRequestEvent;
import eu.europa.ec.leos.web.event.component.VersionListRequestEvent;
import eu.europa.ec.leos.web.event.component.VersionListResponseEvent;
import eu.europa.ec.leos.web.event.component.WindowClosedEvent;
import eu.europa.ec.leos.web.event.view.AddChangeDetailsMenuEvent;
import eu.europa.ec.leos.web.event.view.document.CheckElementCoEditionEvent;
import eu.europa.ec.leos.web.event.view.document.CloseDocumentEvent;
import eu.europa.ec.leos.web.event.view.document.CloseDocumentConfirmationEvent;
import eu.europa.ec.leos.web.event.view.document.ComparisonEvent;
import eu.europa.ec.leos.web.event.view.document.DocumentNavigationRequest;
import eu.europa.ec.leos.web.event.view.document.DocumentUpdatedEvent;
import eu.europa.ec.leos.web.event.view.document.EditElementRequestEvent;
import eu.europa.ec.leos.web.event.view.document.FetchUserGuidanceRequest;
import eu.europa.ec.leos.web.event.view.document.FetchUserPermissionsRequest;
import eu.europa.ec.leos.web.event.view.document.InstanceTypeResolver;
import eu.europa.ec.leos.web.event.view.document.MergeSuggestionRequest;
import eu.europa.ec.leos.web.event.view.document.MergeSuggestionResponse;
import eu.europa.ec.leos.web.event.view.document.MergeSuggestionsRequest;
import eu.europa.ec.leos.web.event.view.document.RefreshContributionEvent;
import eu.europa.ec.leos.web.event.view.document.RefreshDocumentEvent;
import eu.europa.ec.leos.web.event.view.document.RequestFilteredAnnotations;
import eu.europa.ec.leos.web.event.view.document.ResponseFilteredAnnotations;
import eu.europa.ec.leos.web.event.view.document.SaveElementRequestEvent;
import eu.europa.ec.leos.web.event.view.document.SaveIntermediateVersionEvent;
import eu.europa.ec.leos.web.event.view.document.ShowCleanVersionRequestEvent;
import eu.europa.ec.leos.web.event.view.document.ShowIntermediateVersionWindowEvent;
import eu.europa.ec.leos.web.event.view.document.TocItemListRequestEvent;
import eu.europa.ec.leos.web.event.view.document.TocItemListResponseEvent;
import eu.europa.ec.leos.web.event.window.CancelElementEditorEvent;
import eu.europa.ec.leos.web.event.window.CloseElementEditorEvent;
import eu.europa.ec.leos.web.model.MergeActionVO;
import eu.europa.ec.leos.web.model.VersionInfoVO;
import eu.europa.ec.leos.web.support.SessionAttribute;
import eu.europa.ec.leos.web.support.UrlBuilder;
import eu.europa.ec.leos.web.support.UuidHelper;
import eu.europa.ec.leos.web.support.user.UserHelper;
import eu.europa.ec.leos.web.support.xml.DownloadStreamResource;
import eu.europa.ec.leos.web.ui.navigation.Target;
import eu.europa.ec.leos.web.ui.screen.document.ColumnPosition;
import io.atlassian.fugue.Option;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Provider;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
class CoverPagePresenter extends AbstractLeosPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(CoverPagePresenter.class);

    private final CoverPageScreen coverPageScreen;
    private final ProposalService proposalService;
    private final ElementProcessor<Proposal> elementProcessor;
    private final DocumentContentService documentContentService;
    private final UrlBuilder urlBuilder;
    private final TemplateConfigurationService templateConfigurationService;
    private final ComparisonDelegate<Proposal> comparisonDelegate;
    private final UserHelper userHelper;
    private final MessageHelper messageHelper;
    private final Provider<CollectionContext> proposalContextProvider;
    private final CoEditionHelper coEditionHelper;
    private final ExportService exportService;
    private final Provider<BillContext> billContextProvider;
    private final Provider<StructureContext> structureContextProvider;
    private final LegService legService;
    private final SearchService searchService;
    private final CommonDelegate<Proposal> commonDelegate;
    private final CloneContext cloneContext;
    private CloneProposalMetadataVO cloneProposalMetadataVO;
    private final ContributionService contributionService;
    private final InstanceTypeResolver instanceTypeResolver;
    protected final AttachmentProcessor attachmentProcessor;

    private String strDocumentVersionSeriesId;
    private String documentId;
    private String documentRef;
    private boolean comparisonMode;
    private String proposalRef;
    private String connectedEntity;
    private boolean milestoneExplorerOpened = false;
    private MergeContributionHelper mergeContributionHelper;
    private XmlContentProcessor xmlContentProcessor;
    private TransformationService transformationService;
    private final List<String> openElementEditors;
    private final AnnotateService annotateService;

    private final static SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    @Autowired
    CoverPagePresenter(SecurityContext securityContext, HttpSession httpSession, EventBus eventBus,
            CoverPageScreen coverPageScreen,
            ProposalService proposalService,
            ElementProcessor<Proposal> elementProcessor,
            DocumentContentService documentContentService,
            UrlBuilder urlBuilder,
            TemplateConfigurationService templateConfigurationService,
            ComparisonDelegate<Proposal> comparisonDelegate,
            UserHelper userHelper, MessageHelper messageHelper,
            Provider<CollectionContext> proposalContextProvider,
            CoEditionHelper coEditionHelper, EventBus leosApplicationEventBus, UuidHelper uuidHelper,
            Provider<StructureContext> structureContextProvider,
            PackageService packageService,
            ExportService exportService,
            Provider<BillContext> billContextProvider,
            WorkspaceService workspaceService, LegService legService,
            SearchService searchService, CommonDelegate<Proposal> commonDelegate, AnnotateService annotateService,
            CloneContext cloneContext, ContributionService contributionService, InstanceTypeResolver instanceTypeResolver,
            AttachmentProcessor attachmentProcessor, MergeContributionHelper mergeContributionHelper, XmlContentProcessor xmlContentProcessor, TransformationService transformationService) {
        super(securityContext, httpSession, eventBus, leosApplicationEventBus, uuidHelper, packageService, workspaceService);
        this.instanceTypeResolver = instanceTypeResolver;
        this.attachmentProcessor = attachmentProcessor;
        this.mergeContributionHelper = mergeContributionHelper;
        this.xmlContentProcessor = xmlContentProcessor;
        LOG.trace("Initializing coverpage presenter...");
        this.contributionService = contributionService;
        this.coverPageScreen = coverPageScreen;
        this.proposalService = proposalService;
        this.elementProcessor = elementProcessor;
        this.documentContentService = documentContentService;
        this.urlBuilder = urlBuilder;
        this.templateConfigurationService = templateConfigurationService;
        this.comparisonDelegate = comparisonDelegate;
        this.userHelper = userHelper;
        this.messageHelper = messageHelper;
        this.proposalContextProvider = proposalContextProvider;
        this.coEditionHelper = coEditionHelper;
        this.structureContextProvider = structureContextProvider;
        this.legService = legService;
        this.searchService = searchService;
        this.commonDelegate = commonDelegate;
        this.cloneContext = cloneContext;
        this.exportService = exportService;
        this.billContextProvider = billContextProvider;
        this.xmlContentProcessor = xmlContentProcessor;
        this.mergeContributionHelper = mergeContributionHelper;
        this.transformationService = transformationService;
        this.annotateService = annotateService;
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

    private void init() {
        try {
            populateWithProposalRefAndConnectedEntity();
            Proposal proposal = getDocument();
            populateViewData(proposal);
            populateVersionsData(proposal);
            String revisionReference = getRevisionRef();
            if (revisionReference != null) {
                Optional<ContributionVO> contributionVO = coverPageScreen.findContributionAndShowTab(revisionReference);
                if (contributionVO.isPresent()) {
                    eventBus.post(new OpenRevisionDocumentEvent(contributionVO.get()));
                }
            }
        } catch (Exception exception) {
            LOG.error("Exception occurred in init(): ", exception);
            eventBus.post(new NotificationEvent(Type.INFO, "unknown.error.message"));
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
        Proposal proposal = getDocument();
        if (proposal != null) {
            LeosPackage leosPackage = packageService.findPackageByDocumentId(proposal.getId());
            proposal = proposalService.findProposalByPackagePath(leosPackage.getPath());
        }
        return proposal;
    }

    private void populateCloneProposalMetadataVO(byte[] xmlContent) {
        cloneProposalMetadataVO = proposalService.getClonedProposalMetadata(xmlContent);
        cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);
    }

    private void resetCloneProposalMetadataVO() {
        cloneContext.setCloneProposalMetadataVO(null);
    }

    private String getDocumentRef() {
        return (String) httpSession.getAttribute(id + "." + SessionAttribute.PROPOSAL_REF.name());
    }

    private String getRevisionRef() {
        return (String) httpSession.getAttribute(id + "." + SessionAttribute.REVISION_VERSION.name());
    }

    private Proposal getDocument() {
        documentRef = getDocumentRef();
        Proposal proposal = proposalService.findProposalByRef(documentRef);
        setDocumentData(proposal);
        return proposal;
    }

    private void setDocumentData(Proposal proposal) {
        strDocumentVersionSeriesId = proposal.getVersionSeriesId();
        documentId = proposal.getId();
        structureContextProvider.get().useDocumentTemplate(proposal.getMetadata().getOrError(() -> "Proposal metadata is required!").getDocTemplate());
        cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);
    }

    private void populateViewData(Proposal proposal) {
        Validate.notNull(proposal, "Proposal document should not be null");
        try{
            coverPageScreen.setTitle(proposal.getTitle());
            coverPageScreen.setDocumentVersionInfo(getVersionInfo(proposal));
            String content = getEditableXml(proposal);
            coverPageScreen.setContent(content);
            coverPageScreen.setToc(getTableOfContent(proposal));
            DocumentVO proposalVO = createProposalVO(proposal);
            coverPageScreen.setPermissions(proposalVO, isClonedProposal());
            coverPageScreen.initAnnotations(proposalVO, proposalRef, connectedEntity);
            coverPageScreen.updateUserCoEditionInfo(coEditionHelper.getCurrentEditInfo(proposal.getVersionSeriesId()), id);
            if(isClonedProposal()) {
                eventBus.post(new AddChangeDetailsMenuEvent());
            }
        }
        catch (Exception ex) {
            LOG.error("Error while processing document", ex);
            eventBus.post(new NotificationEvent(Type.INFO, "error.message", ex.getMessage()));
        }
    }

    @Subscribe
    public void initLeosEditor(InitLeosEditorEvent event) {
        List<LeosMetadata> documentsMetadata = packageService.getDocumentsMetadata(event.getDocument().getId());
        coverPageScreen.initLeosEditor(event.getDocument(), documentsMetadata);
    }

    private void populateVersionsData(Proposal proposal) {
        DocumentVO proposalVO = createProposalVO(proposal);
        final List<VersionVO> allVersions = getVersionVOS();
        final List<ContributionVO> allContributions = contributionService.getDocumentContributions(documentId, 0, Proposal.class);
        coverPageScreen.setDataFunctions(
                proposalVO,
                allVersions,
                allContributions,
                this::majorVersionsFn, this::countMajorVersionsFn,
                this::minorVersionsFn, this::countMinorVersionsFn,
                this::recentChangesFn, this::countRecentChangesFn);
        coverPageScreen.setContributionsData(allContributions);
    }

    private List<VersionVO> getVersionVOS() {
        return proposalService.getAllVersions(documentId, documentRef);
    }

    @Subscribe
    public void updateVersionsTab(DocumentUpdatedEvent event) {
        final List<VersionVO> allVersions = getVersionVOS();
        coverPageScreen.refreshVersions(allVersions, comparisonMode);
    }

    private Integer countMinorVersionsFn(String currIntVersion) {
        return proposalService.findAllMinorsCountForIntermediate(documentRef, currIntVersion);
    }

    private List<Proposal> minorVersionsFn(String currIntVersion, int startIndex, int maxResults) {
        return proposalService.findAllMinorsForIntermediate(documentRef, currIntVersion, startIndex, maxResults);
    }

    private Integer countMajorVersionsFn() {
        return proposalService.findAllMajorsCount(documentRef);
    }

    private List<Proposal> majorVersionsFn(int startIndex, int maxResults) {
        return proposalService.findAllMajors(documentRef, startIndex, maxResults);
    }

    private Integer countRecentChangesFn() {
        return proposalService.findRecentMinorVersionsCount(documentId, documentRef);
    }

    private List<Proposal> recentChangesFn(int startIndex, int maxResults) {
        return proposalService.findRecentMinorVersions(documentId, documentRef, startIndex, maxResults);
    }

    private List<TableOfContentItemVO> getTableOfContent(Proposal proposal) {
        return proposalService.getCoverPageTableOfContent(proposal, TocMode.NOT_SIMPLIFIED);
    }

    @Subscribe
    void getDocumentVersionsList(VersionListRequestEvent<Proposal> event) {
        List<Proposal> proposalVersions = proposalService.findVersions(documentId);
        eventBus.post(new VersionListResponseEvent(new ArrayList<>(proposalVersions)));
    }

    private String getEditableXml(Proposal proposal) {
        securityContext.getPermissions(proposal);
        byte[] coverPageContent = new byte[0];
        byte[] proposalContent = proposal.getContent().get().getSource().getBytes();
        boolean isCoverPageExists = documentContentService.isCoverPageExists(proposalContent);
        cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);
        if(coverPageScreen.isCoverPageVisible() && isCoverPageExists) {
            byte[] xmlContent = proposal.getContent().get().getSource().getBytes();
            coverPageContent = documentContentService.getCoverPageContent(xmlContent);
        }
        String editableXml = documentContentService.toEditableContent(proposal,
                urlBuilder.getWebAppPath(VaadinServletService.getCurrentServletRequest()), securityContext, coverPageContent);
        editableXml = XmlHelper.removeSelfClosingElements(editableXml);
        return StringEscapeUtils.unescapeXml(editableXml);
    }

    @Subscribe
    void handleCloseDocument(CloseDocumentEvent event) {
        LOG.trace("Handling close document request...");

        //if unsaved changes remain in the session, first ask for confirmation
        if(isProposalUnsaved() || this.isHasOpenElementEditors()){
            eventBus.post(new ShowConfirmDialogEvent(new CloseDocumentConfirmationEvent(), null));
            return;
        }
        this.closeDocument();
    }

    @Subscribe
    void handleNavigationRequest(DocumentNavigationRequest event) {
        LOG.trace("Handling document navigation request...");
        if (event.getNavigationEvent() != null) event.getNavigationEvent().setForwardToDocument(false);
        if(isProposalUnsaved() || this.isHasOpenElementEditors()) {
            eventBus.post(new ShowConfirmDialogEvent(event.getNavigationEvent(), null));
            return;
        }
        eventBus.post(event.getNavigationEvent());
    }

    private boolean isProposalUnsaved(){
        return getProposalFromSession() != null;
    }

    private Proposal getProposalFromSession() {
        return (Proposal) httpSession.getAttribute("proposal#" + getDocumentRef());
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
        if (coverPageScreen.isTocEnabled()) {
            eventBus.post(new CloseTocAndDocumentEvent());
        } else {
            eventBus.post(new CloseDocumentEvent());
        }
    }

    @Subscribe
    void refreshDocument(RefreshDocumentEvent event){
        Proposal proposal = getDocument();
        populateViewData(proposal);
    }

    @Subscribe
    void checkElementCoEdition(CheckElementCoEditionEvent event) {
        coverPageScreen.checkElementCoEdition(coEditionHelper.getCurrentEditInfo(strDocumentVersionSeriesId), user,
                event.getElementId(), event.getElementTagName(), event.getAction(), event.getActionEvent());
    }


    @Subscribe
    void cancelElementEditor(CancelElementEditorEvent event) {
        String elementId = event.getElementId();
        coEditionHelper.removeUserEditInfo(id, strDocumentVersionSeriesId, elementId, InfoType.ELEMENT_INFO);

        //load content from session if exists
        Proposal proposalFromSession = getProposalFromSession();
        if(proposalFromSession != null) {
            coverPageScreen.setContent(getEditableXml(proposalFromSession));
        }else{
            eventBus.post(new RefreshDocumentEvent());
        }
        LOG.debug("User edit information removed");
    }


    @Subscribe
    void editElement(EditElementRequestEvent event){
        String elementId = event.getElementId();
        String elementTagName = event.getElementTagName();

        LOG.trace("Handling edit element request... for {},id={}",elementTagName , elementId );

        try {
            //show confirm dialog if there is any unsaved replaced text
            //it can be detected from the session attribute
            if(isProposalUnsaved()){
                eventBus.post(new ShowConfirmDialogEvent(event, new CancelElementEditorEvent(event.getElementId(),event.getElementTagName())));
                return;
            }
            Proposal proposal = getDocument();
            String element = elementProcessor.getElement(proposal, elementTagName, elementId);
            coEditionHelper.storeUserEditInfo(httpSession.getId(), id, user, strDocumentVersionSeriesId, elementId, InfoType.ELEMENT_INFO);
            coverPageScreen.showElementEditor(elementId, elementTagName, element);
            openElementEditors.add(elementId);
        }
        catch (Exception ex){
            LOG.error("Exception while edit element operation for coverpage", ex);
            eventBus.post(new NotificationEvent(Type.INFO, "error.message", ex.getMessage()));
        }
    }

    @Subscribe
    void saveElement(SaveElementRequestEvent event) {
        String elementId = event.getElementId();
        String elementTagName = event.getElementTagName();
        String elementContent = event.getElementContent();
        String docPurpose = proposalService.getPurposeFromXml(elementContent.getBytes());
        LOG.trace("Handling save element request... for {},id={}",elementTagName , elementId );

        try {
            Proposal proposal = getDocument();
            byte[] proposalContent = proposal.getContent().get().getSource().getBytes();
            List<Element> docPurposeElements = xmlContentProcessor.getElementsByTagName(proposalContent, Arrays.asList("docPurpose"), false);
            // Check if new doc purpose is not empty
            if (docPurpose != null && docPurpose.trim().replaceAll("(^\\h*)|(\\h*$)", "").length() > 0) {

                byte[] newXmlContent = !docPurposeElements.isEmpty() ?  xmlContentProcessor.replaceElementById(proposalContent, elementContent,
                        docPurposeElements.get(0).getElementId()) : null;

                if (newXmlContent == null) {
                    coverPageScreen.showAlertDialog("operation.element.not.performed");
                    return;
                }

                proposal = proposalService.updateProposal(proposal, newXmlContent, VersionType.MINOR, messageHelper.getMessage("operation.docpurpose.updated"));

                CollectionContext context = proposalContextProvider.get();
                context.useProposal(proposal.getId());
                context.usePurpose(docPurpose);
                context.useEeaRelevance(proposal.getMetadata().get().getEeaRelevance());
                String comment = messageHelper.getMessage("operation.docpurpose.updated");
                context.useActionMessage(ContextAction.METADATA_UPDATED, comment);
                context.useActionComment(comment);
                context.executeUpdateDocumentsAssociatedToProposal();
            }

            if (proposal != null) {
                if (!docPurposeElements.isEmpty()) {
                    String elemContent = elementProcessor.getElement(proposal, docPurposeElements.get(0).getElementTagName(),
                            docPurposeElements.get(0).getElementId());
                    coverPageScreen.refreshElementEditor(elementId, elementTagName, elemContent);
                }
                eventBus.post(new DocumentUpdatedEvent()); //Document might be updated.
                coverPageScreen.scrollToMarkedChange(elementId);
                leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
            }
        } catch (Exception ex) {
            LOG.error("Exception while save  proposal operation", ex);
            eventBus.post(new NotificationEvent(Type.INFO, "error.message", ex.getMessage()));
        }
    }

    @Subscribe
    void closeElementEditor(CloseElementEditorEvent event){
        String elementId = event.getElementId();
        coEditionHelper.removeUserEditInfo(id, strDocumentVersionSeriesId, elementId, InfoType.ELEMENT_INFO);
        LOG.debug("User edit information removed");
        openElementEditors.remove(elementId);
        eventBus.post(new RefreshDocumentEvent());
    }

    @Subscribe
    public void getUserGuidance(FetchUserGuidanceRequest event) {
        // KLUGE temporary hack for compatibility with new domain model
        Proposal proposal = proposalService.findProposal(documentId, true);
        String jsonGuidance = templateConfigurationService.getTemplateConfiguration(proposal.getMetadata().get().getDocTemplate(), "guidance");
        coverPageScreen.setUserGuidance(jsonGuidance);
    }

    @Subscribe
    void mergeSuggestion(MergeSuggestionRequest event) {
        Proposal document = getDocument();
        byte[] resultXmlContent = elementProcessor.replaceTextInElement(document, event.getOrigText(), event.getNewText(), event.getElementId(), event.getStartOffset(), event.getEndOffset());
        if (resultXmlContent == null) {
            eventBus.post(new MergeSuggestionResponse(messageHelper.getMessage("document.merge.suggestion.failed"), MergeSuggestionResponse.Result.ERROR));
            return;
        }
        document = proposalService.updateProposal(document, resultXmlContent, VersionType.MINOR, messageHelper.getMessage("operation.merge.suggestion"));
        if (document != null) {
            eventBus.post(new RefreshDocumentEvent());
            eventBus.post(new DocumentUpdatedEvent());
            leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
            eventBus.post(new MergeSuggestionResponse(messageHelper.getMessage("document.merge.suggestion.success"), MergeSuggestionResponse.Result.SUCCESS));
        } else {
            eventBus.post(new MergeSuggestionResponse(messageHelper.getMessage("document.merge.suggestion.failed"), MergeSuggestionResponse.Result.ERROR));
        }
    }

    @Subscribe
    public void mergeSuggestions(MergeSuggestionsRequest event) {
        Proposal document = getDocument();
        commonDelegate.mergeSuggestions(document, event, elementProcessor, proposalService::updateProposal);
    }

    @Subscribe
    public void getUserPermissions(FetchUserPermissionsRequest event) {
        Proposal proposal = getDocument();
        List<LeosPermission> userPermissions = securityContext.getPermissions(proposal);
        coverPageScreen.sendUserPermissions(userPermissions);
        annotateService.sendUserPermissions(userPermissions);
    }

    @Subscribe
    public void fetchSearchMetadata(SearchMetadataRequest event){
        if (!milestoneExplorerOpened) {
            List<AnnotateMetadata> metadataList = new ArrayList<>();
            if (instanceTypeResolver.getInstanceType().equals(InstanceType.COMMISSION.toString()) || instanceTypeResolver.getInstanceType().equals(InstanceType.COUNCIL.toString())) {
                AnnotateMetadata metadata = new AnnotateMetadata();
                List<String> statusList = new ArrayList<String>();
                statusList.add(AnnotationStatus.ALL.name());
                metadata.setStatus(statusList);
                metadataList.add(metadata);
            }
            eventBus.post(new SearchMetadataResponse(metadataList));
        }
    }

    @Subscribe
    public void fetchMetadata(DocumentMetadataRequest event){
        AnnotateMetadata metadata = new AnnotateMetadata();
        Proposal proposal = getDocument();
        metadata.setVersion(proposal.getVersionLabel());
        metadata.setId(proposal.getId());
        metadata.setTitle(proposal.getTitle());
        eventBus.post(new DocumentMetadataResponse(metadata));
    }

    @Subscribe
    void downloadXmlFiles(DownloadXmlFilesRequestEvent event) {
        final ExportVersions<Proposal> exportVersions = event.getExportOptions().getExportVersions();
        final Proposal current = exportVersions.getCurrent();
        final Proposal original = exportVersions.getOriginal();
        final Proposal intermediate = exportVersions.getIntermediate();

        String language = original.getMetadata().get().getLanguage();

        final String leosComparedContent;
        final String docuWriteComparedContent;
        final String comparedInfo;
        cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);
        if(intermediate != null){
            comparedInfo = messageHelper.getMessage("version.compare.double", original.getVersionLabel(), intermediate.getVersionLabel(), current.getVersionLabel());
            leosComparedContent = comparisonDelegate.doubleCompareHtmlContents(original, intermediate, current, true);
            docuWriteComparedContent = legService.doubleCompareXmlContents(original, intermediate, current, false);
        } else {
            comparedInfo = messageHelper.getMessage("version.compare.simple", original.getVersionLabel(), current.getVersionLabel());
            leosComparedContent = comparisonDelegate.getMarkedContent(original, current);
            docuWriteComparedContent = legService.simpleCompareXmlContents(original, current, true);
        }

        coverPageScreen.setDownloadStreamResourceForXmlFiles(original, intermediate, current, language, comparedInfo, leosComparedContent, docuWriteComparedContent);
    }

    @Subscribe
    void downloadXmlVersion(DownloadXmlVersionRequestEvent event) {
        try {
            cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);
            final Proposal chosenDocument = proposalService.findProposalVersion(event.getVersionId());
            final String fileName = chosenDocument.getMetadata().get().getRef() + "_v" + chosenDocument.getVersionLabel() + ".xml";

            DownloadStreamResource downloadStreamResource = new DownloadStreamResource(fileName, new ByteArrayInputStream(chosenDocument.getContent().get().getSource().getBytes()));
            coverPageScreen.setDownloadStreamResourceForVersion(downloadStreamResource, chosenDocument.getId());
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while downloadXmlVersion", e);
            eventBus.post(new NotificationEvent(Type.ERROR, "error.message", e.getMessage()));
        }
    }

    @Subscribe
    void downloadCleanVersion(DownloadCleanVersion event) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        LeosPackage leosPackage = packageService.findPackageByDocumentId(documentId);
        BillContext context = billContextProvider.get();
        context.usePackage(leosPackage);
        String proposalId = context.getProposalId();
        try {
            final String jobFileName = "Proposal_" + proposalId + "_AKN2LW_CLEAN_" + System.currentTimeMillis() + ".zip";
            ExportOptions exportOptions = new ExportLW(ExportOptions.Output.PDF, Proposal.class, false, true);
            exportOptions.setExportVersions(new ExportVersions(null, getDocument()));
            exportService.createDocumentPackage(jobFileName, proposalId, exportOptions, user);
            eventBus.post(new NotificationEvent("document.export.package.button.send", "document.export.message",
                    NotificationEvent.Type.TRAY, exportOptions.getExportOutputDescription(), user.getEmail()));
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while using ExportService", e);
            eventBus.post(new NotificationEvent(Type.ERROR, "export.legiswrite.error.message", e.getMessage()));
        }
        LOG.info("The actual version of CLEANED Coverpage for proposal {}, downloaded in {} milliseconds ({} sec)", proposalId, stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
    }

    @Subscribe
    void downloadActualVersion(DownloadActualVersionRequestEvent event) {
        requestFilteredAnnotationsForDownload(event.isWithFilteredAnnotations());
    }

    private void doDownloadActualVersion(Boolean isWithAnnotations, String annotations) {
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            final Proposal currentDocument = getDocument();
            ExportOptions exportOptions = new ExportLW(ExportOptions.Output.PDF, Proposal.class, false);
            exportOptions.setExportVersions(new ExportVersions(isClonedProposal() ?
                    proposalService.findFirstVersion(getDocument().getMetadata().get().getRef()) : null, currentDocument));
            exportOptions.setWithFilteredAnnotations(isWithAnnotations);
            exportOptions.setFilteredAnnotations(annotations);
            LeosPackage leosPackage = packageService.findPackageByDocumentId(documentId);
            BillContext context = billContextProvider.get();
            context.usePackage(leosPackage);
            String proposalId = context.getProposalId();
            if (proposalId != null) {
                try {
                    this.createDocumentPackageForExport(exportOptions);
                    eventBus.post(new NotificationEvent("document.export.package.button.send",
                            "document.export.message",
                            NotificationEvent.Type.TRAY,
                            exportOptions.getExportOutputDescription(),
                            user.getEmail()));
                } catch (Exception e) {
                    LOG.error("Unexpected error occurred while using ExportService", e);
                    eventBus.post(new NotificationEvent(Type.ERROR, "export.package.error.message", e.getMessage()));
                }
            }
            LOG.info("The actual version of Coverpage {} downloaded in {} milliseconds ({} sec)", currentDocument.getName(),
                    stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while using ExportService", e);
            eventBus.post(new NotificationEvent(Type.ERROR, "export.docuwrite.error.message", e.getMessage()));
        }
    }

    private void requestFilteredAnnotationsForDownload(final Boolean isWithAnnotations) {
        if (isWithAnnotations) {
            eventBus.post(new RequestFilteredAnnotations());
        } else {
            doDownloadActualVersion(false, null);
        }
    }

    @Subscribe
    void responseFilteredAnnotations(ResponseFilteredAnnotations event) {
        String filteredAnnotations = event.getAnnotations();
        doDownloadActualVersion(true, filteredAnnotations);
    }

    @Subscribe
    void versionRestore(RestoreVersionRequestEvent event) {
        String versionId = event.getVersionId();
        Proposal version = proposalService.findProposalVersion(versionId);
        byte[] resultXmlContent = getContent(version);
        proposalService.updateProposal(getDocument(), resultXmlContent,  VersionType.MINOR, messageHelper.getMessage("operation.restore.version", version.getVersionLabel()));

        List documentVersions = proposalService.findVersions(documentId);
        eventBus.post(new RefreshDocumentEvent());
        eventBus.post(new DocumentUpdatedEvent()); //Document might be updated.
        leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
    }

    @Subscribe
    void cleanComparedContent(CleanComparedContentEvent event) {
        coverPageScreen.cleanComparedContent();
    }

    @Subscribe
    void showVersion(ShowVersionRequestEvent event) {
        final Proposal version = proposalService.findProposalVersion(event.getVersionId());
        final String versionContent = documentContentService.getDocumentAsHtml(version, urlBuilder.getWebAppPath(VaadinServletService.getCurrentServletRequest()),
                securityContext.getPermissions(version), true);
        final String versionInfo = getVersionInfoAsString(version);
        coverPageScreen.showVersion(versionContent, versionInfo);
    }

    @Subscribe
    void showRevision(OpenRevisionDocumentEvent event) {
        compareAndShowRevision(event.getContributionVO());
        Proposal proposal = getDocument();
        setDocumentData(proposal);
    }

    @Subscribe
    void showCompareAndRevision(CompareAndShowRevisionEvent event) {
        cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);
        compareAndShowRevision(event.getContributionVO());
    }

    private void compareAndShowRevision(ContributionVO contributionVO) {
        final Proposal revision = proposalService.findProposal(contributionVO.getDocumentId(), false);
        byte[] contributionContent = contributionVO.getXmlContent();
        final String revisionContent = documentContentService.getDocumentForContributionAsHtml(contributionContent,
                urlBuilder.getWebAppPath(VaadinServletService.getCurrentServletRequest()),
                securityContext.getPermissions(revision), true);
        cloneContext.setContribution(Boolean.TRUE);
        coverPageScreen.refreshVersions(getVersionVOS(), false);
        Proposal proposal = getDocument();
        List<TocItem>  tocItemList = getTocITems(proposal);
        coverPageScreen.showRevision(revisionContent, contributionVO, tocItemList);
    }

    private List<TocItem> getTocITems(Proposal proposal) {
        structureContextProvider.get().useDocumentTemplate(proposal.getMetadata().getOrError(() -> "Proposal metadata is required!").getDocTemplate());
        return structureContextProvider.get().getTocItems();
    }

    @Subscribe
    void declineRevision(DeclineRevisionDocumentEvent event) {
        cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);
        final Proposal revision = proposalService.findProposalVersion(event.getContributionVO().getDocumentId());
        Map<String, Object> properties = new HashMap<>();
        String contributionStatus = event.getContributionVO().getContributionStatus().getValue();
        properties.put(CmisProperties.CONTRIBUTION_STATUS.getId(), contributionStatus);
        Proposal updatedProposal = proposalService.updateProposal(revision.getId(), properties, false);
        if(updatedProposal != null) {
            coverPageScreen.disableMergePane();
            event.getSelectedItem().setVisible(false);
        } else {
            throw new RuntimeException("Unable to update document revision status");
        }
    }

    void markRevisionAsProcessed(String revisionDocumentId, String msgKey) {
        cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);
        final Proposal revision = proposalService.findProposalVersion(revisionDocumentId);

        coverPageScreen.disableMergePane();
        Map<String, Object> properties = new HashMap<>();
        properties.put(CmisProperties.CONTRIBUTION_STATUS.getId(), ContributionVO.ContributionStatus.CONTRIBUTION_DONE.getValue());
        proposalService.updateProposal(revision.getId(), properties, false);
        final List<ContributionVO> allContributions = contributionService.getDocumentContributions(documentId, 0, Proposal.class);
        coverPageScreen.populateContributions(allContributions);
        eventBus.post(new RefreshDocumentEvent());
        eventBus.post(new NotificationEvent(Type.INFO, msgKey));
    }

    @Subscribe
    public void processRevision(RevisionDocumentProcessedEvent event) {
        markRevisionAsProcessed(event.getDocumentId(), "contribution.process.revision.notification.message");
        eventBus.post(new DocumentUpdatedEvent());
    }

    @Subscribe
    public void fetchList(TocItemListRequestEvent tocItemListRequestEvent) {
        eventBus.post(new TocItemListResponseEvent(getTocITems(getDocument())));
    }

    @Subscribe
    public void acceptSelectedContributions(ApplyContributionsRequestEvent event) throws IOException {
        if(event.getMergeActionVOS() == null || event.getMergeActionVOS().isEmpty()) {
            // Nothing to be done
            eventBus.post(new NotificationEvent(Type.INFO,
                    "contribution.no.contribution.available.notification.message"));
            return;
        }
        String mergeActionKey = "contribution.merge.action.accepted.notification";
        Proposal proposal = getDocument();
        List<TocItem> tocItemList = getTocITems(proposal);
        byte[] xmlClonedContent = event.getMergeActionVOS().get(0).getContributionVO().getXmlContent();
        List<InternalRefMap> intRefMap = getInternalRefMaps(event, proposal, xmlClonedContent);
        byte[] xmlContent = mergeContributionHelper.updateDocumentWithContributions(event, proposal, tocItemList, intRefMap);
        proposal = updateEEARelevance(proposal, event);
        xmlContent = xmlContentProcessor.doXMLPostProcessing(xmlContent);
        updateProposalContent(proposal, xmlContent, messageHelper.getMessage("contribution.merge.operation.message"), mergeActionKey);
        if(event.isAllContributions()) {
            markRevisionAsProcessed(event.getMergeActionVOS().get(0).getContributionVO().getDocumentId(), "contribution.accept.all.notification.message");
        } else {
            eventBus.post(new RefreshContributionEvent());
        }
    }

    private List<MergeActionVO> getEEARelevanceMergeActions(List<MergeActionVO> mergeActions) {
        return mergeActions.stream()
                .filter(mergeAction -> mergeAction.getElementId().equals(XmlHelper.COVERPAGE_EEA_RELEVANCE_ID))
                .collect(Collectors.toList());
    }

    private Proposal updateEEARelevance(Proposal proposal, ApplyContributionsRequestEvent event) {
        List<MergeActionVO> eeaRelevanceMergeActions = getEEARelevanceMergeActions(event.getMergeActionVOS());
        if (!eeaRelevanceMergeActions.isEmpty()) {
            ProposalMetadata proposalMetadata = proposal.getMetadata().get();
            for (MergeActionVO mergeActionVO : eeaRelevanceMergeActions) {
                switch (mergeActionVO.getElementState()) {
                    case ADD:
                        if (mergeActionVO.getAction().equals(MergeActionRequestEvent.MergeAction.UNDO)) {
                            proposalMetadata = proposalMetadata.withEeaRelevance(false);
                        } else {
                            proposalMetadata = proposalMetadata.withEeaRelevance(true);
                        }
                        break;
                    case DELETE:
                        if (mergeActionVO.getAction().equals(MergeActionRequestEvent.MergeAction.UNDO)) {
                            proposalMetadata = proposalMetadata.withEeaRelevance(true);
                        } else {
                            proposalMetadata = proposalMetadata.withEeaRelevance(false);
                        }
                        break;
                }
            }
            proposal = proposalService.updateProposal(proposal, proposalMetadata);
            CollectionContext context = proposalContextProvider.get();
            context.useProposal(proposal.getId());
            context.usePurpose(proposalMetadata.getPurpose());
            context.useEeaRelevance(proposalMetadata.getEeaRelevance());
            String comment = messageHelper.getMessage("operation.metadata.updated");
            context.useActionMessage(ContextAction.METADATA_UPDATED, comment);
            context.useActionComment(comment);
            context.executeUpdateDocumentsAssociatedToProposal();
        }
        return proposal;
    }

    private List<InternalRefMap> getInternalRefMaps(ApplyContributionsRequestEvent event, Proposal proposal, byte[] xmlClonedContent) {
        byte[] xmlContent = proposal.getContent().get().getSource().getBytes();
        Map<String, String> attachmentsClonedContent = attachmentProcessor.getAttachmentsHrefFromBill(xmlClonedContent);
        List<InternalRefMap> map = new ArrayList<>();
        String ref = proposal.getName().replace(".xml", "");
        String clonedRef  = event.getMergeActionVOS().get(0).getContributionVO().getDocumentName().replace(".xml", "");
        map.add(new InternalRefMap("BILL", ref, clonedRef));
        Map<String, String> attachments = attachmentProcessor.getAttachmentsHrefFromBill(xmlContent);
        attachments.forEach((docType, href) -> {
            if(docType.equals("ANNEX")) {
                docType = docType + " I";
            }
            String cloned = attachmentsClonedContent.get(docType);
            map.add(new InternalRefMap(docType, href, cloned));
        });
        return map;
    }

    private void updateProposalContent(Proposal proposal, byte[] xmlContent, String operationMsg, String notificationMsg) {
        proposal = proposalService.updateProposal(proposal, xmlContent, operationMsg);
        if (proposal != null) {
            eventBus.post(new NotificationEvent(Type.INFO, notificationMsg));
            eventBus.post(new RefreshDocumentEvent());
            eventBus.post(new DocumentUpdatedEvent());
            leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
        }
    }

    @Subscribe
    void showCleanVersion(ShowCleanVersionRequestEvent event) {
        final Proposal proposal = getDocument();
        final String versionContent = documentContentService.getCleanDocumentAsHtml(proposal, urlBuilder.getWebAppPath(VaadinServletService.getCurrentServletRequest()),
                securityContext.getPermissions(proposal), true);
        final String versionInfo = getVersionInfoAsString(proposal);
        coverPageScreen.showCleanVersion(versionContent, versionInfo);
    }

    @Subscribe
    void refreshCleanVersion(DocumentUpdatedEvent event) {
        if (coverPageScreen.isCleanVersionShowed()) {
            showCleanVersion(new ShowCleanVersionRequestEvent());
        }
    }

    @Subscribe
    public void fetchMilestoneByVersionedReference(FetchMilestoneByVersionedReferenceEvent event) {
        LeosPackage leosPackage = packageService.findPackageByDocumentId(documentId);
        LegDocument legDocument = legService.findLastLegByVersionedReference(leosPackage.getPath(), event.getVersionedReference());
        milestoneExplorerOpened = true;
        coverPageScreen.showMilestoneExplorer(legDocument, String.join(",", legDocument.getMilestoneComments()), proposalRef);
    }

    @Subscribe
    void compare(CompareRequestEvent event) {
        cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);
        final Proposal oldVersion = proposalService.findProposalVersion(event.getOldVersionId());
        final Proposal newVersion = proposalService.findProposalVersion(event.getNewVersionId());
        String comparedContent = comparisonDelegate.getMarkedContent(oldVersion, newVersion, true);
        final String comparedInfo = messageHelper.getMessage("version.compare.simple", oldVersion.getVersionLabel(), newVersion.getVersionLabel());
        coverPageScreen.populateComparisonContent(comparedContent, comparedInfo, oldVersion, newVersion);
    }

    private String getVersionInfoAsString(XmlDocument document) {
        final VersionInfoVO versionInfo = getVersionInfo(document);
        final String versionInfoString = messageHelper.getMessage(
                "document.version.caption",
                versionInfo.getDocumentVersion(),
                versionInfo.getLastModifiedBy(),
                versionInfo.getEntity(),
                versionInfo.getLastModificationInstant()
        );
        return versionInfoString;
    }

    @Subscribe
    public void changeComparisionMode(ComparisonEvent event) {
        comparisonMode = event.isComparsionMode();
        LayoutChangeRequestEvent layoutEvent;
        if (comparisonMode) {
            coverPageScreen.cleanComparedContent();
            layoutEvent = new LayoutChangeRequestEvent(ColumnPosition.DEFAULT, ComparisonComponent.class, null);
            eventBus.post(layoutEvent);
        } else {
            layoutEvent = new LayoutChangeRequestEvent(ColumnPosition.OFF, ComparisonComponent.class, null);
            eventBus.post(layoutEvent);
            eventBus.post(new ResetRevisionComponentEvent());
        }
        updateVersionsTab(new DocumentUpdatedEvent());
    }

    @Subscribe
    public void showIntermediateVersionWindow(ShowIntermediateVersionWindowEvent event) {
        coverPageScreen.showIntermediateVersionWindow();
    }

    @Subscribe
    public void saveIntermediateVersion(SaveIntermediateVersionEvent event) {
        Proposal proposal = proposalService.createVersion(documentId, event.getVersionType(), event.getCheckinComment());
        setDocumentData(proposal);
        eventBus.post(new NotificationEvent(NotificationEvent.Type.INFO, "document.major.version.saved"));
        eventBus.post(new DocumentUpdatedEvent());
        leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, proposal.getVersionSeriesId(), id));
        populateViewData(proposal);
    }

    private byte[] getContent(Proposal proposal) {
        final Content content = proposal.getContent().getOrError(() -> "Coverpage content is required!");
        return content.getSource().getBytes();
    }

    private VersionInfoVO getVersionInfo(XmlDocument document){
        String userId = document.getLastModifiedBy();
        User user = userHelper.getUser(userId);

        return new VersionInfoVO(
                document.getVersionLabel(),
                user.getName(), user.getDefaultEntity() != null ? user.getDefaultEntity().getOrganizationName(): "",
                dateFormatter.format(Date.from(document.getLastModificationInstant())),
                document.getVersionType());
    }

    private DocumentVO createProposalVO(Proposal proposal) {
        DocumentVO proposalVO = new DocumentVO(proposal.getId(),
                proposal.getMetadata().exists(m -> m.getLanguage() != null) ? proposal.getMetadata().get().getLanguage() : "EN",
                LeosCategory.PROPOSAL,
                proposal.getLastModifiedBy(),
                Date.from(proposal.getLastModificationInstant()));
        if (proposal.getMetadata().isDefined()) {
            ProposalMetadata metadata = proposal.getMetadata().get();
            proposalVO.getMetadata().setInternalRef(metadata.getRef());
        }
        if(!proposal.getCollaborators().isEmpty()) {
            proposalVO.addCollaborators(proposal.getCollaborators());
        }

        return proposalVO;
    }

    @Subscribe
    void updateProposalMetadata(DocumentUpdatedEvent event) {
        if (event.isModified()) {
            CollectionContext context = proposalContextProvider.get();
            context.useChildDocument(documentId);
            context.executeUpdateProposalAsync();
        }
    }

    @Subscribe
    public void onInfoUpdate(UpdateUserInfoEvent updateUserInfoEvent) {
        if(isCurrentInfoId(updateUserInfoEvent.getActionInfo().getInfo().getDocumentId())) {
            if (!id.equals(updateUserInfoEvent.getActionInfo().getInfo().getPresenterId())) {
                eventBus.post(new NotificationEvent(leosUI, "coedition.caption", "coedition.operation." + updateUserInfoEvent.getActionInfo().getOperation().getValue(),
                        NotificationEvent.Type.TRAY, updateUserInfoEvent.getActionInfo().getInfo().getUserName()));
            }
            LOG.debug("Coverpage Presenter updated the edit info -" + updateUserInfoEvent.getActionInfo().getOperation().name());
            coverPageScreen.updateUserCoEditionInfo(updateUserInfoEvent.getActionInfo().getCoEditionVos(), id);
        }
    }

    private boolean isCurrentInfoId(String versionSeriesId) {
        return versionSeriesId.equals(strDocumentVersionSeriesId);
    }

    @Subscribe
    private void documentUpdatedByCoEditor(DocumentUpdatedByCoEditorEvent documentUpdatedByCoEditorEvent) {
        if (isCurrentInfoId(documentUpdatedByCoEditorEvent.getDocumentId()) &&
                !id.equals(documentUpdatedByCoEditorEvent.getPresenterId())) {
            eventBus.post(new NotificationEvent(leosUI, "coedition.caption", "coedition.operation.update", NotificationEvent.Type.TRAY,
                    documentUpdatedByCoEditorEvent.getUser().getName()));
            coverPageScreen.displayDocumentUpdatedByCoEditorWarning();
        }
    }

    @Subscribe
    void searchTextInDocument(SearchTextRequestEvent event) {
        Proposal coverpage = (Proposal) httpSession.getAttribute("proposal#" + getDocumentRef());
        if (coverpage == null) {
            coverpage = getDocument();
        }
        List<SearchMatchVO> matches = Collections.emptyList();
        try {
            matches = searchService.searchText(getContent(coverpage), event.getSearchText(), event.matchCase, event.completeWords);
        } catch (Exception e) {
            eventBus.post(new NotificationEvent(Type.ERROR, "Error while searching{1}", e.getMessage()));
        }

        coverPageScreen.showMatchResults(event.searchID, matches);
    }

    @Subscribe
    void replaceAllTextInDocument(ReplaceAllMatchRequestEvent event) {
        Proposal proposalFromSession = getProposalFromSession();
        if (proposalFromSession == null) {
            proposalFromSession = getDocument();
        }

        byte[] updatedContent = searchService.replaceText(
                getContent(proposalFromSession),
                event.getSearchText(),
                event.getReplaceText(),
                event.getSearchMatchVOs());

        Proposal proposalUpdated = copyIntoNew(proposalFromSession, updatedContent);
        httpSession.setAttribute("proposal#" + getDocumentRef(), proposalUpdated);
        coverPageScreen.setContent(getEditableXml(proposalUpdated));
        eventBus.post(new ReplaceAllMatchResponseEvent(true));
    }

    private Proposal copyIntoNew(Proposal source, byte[] updatedContent) {
        Content contentFromSession = source.getContent().get();
        Content.Source updatedSource = new SourceImpl(new ByteArrayInputStream(updatedContent));
        Content contentObj = new ContentImpl(
                contentFromSession.getFileName(),
                contentFromSession.getMimeType(),
                updatedContent.length,
                updatedSource
        );
        Option<Content> updatedContentOptionObj = Option.option(contentObj);
        return new Proposal(
                source.getId(),
                source.getName(),
                source.getCreatedBy(),
                source.getCreationInstant(),
                source.getLastModifiedBy(),
                source.getLastModificationInstant(),
                source.getVersionSeriesId(),
                source.getCmisVersionLabel(),
                source.getVersionLabel(),
                source.getVersionComment(),
                source.getVersionType(),
                source.isLatestVersion(),
                source.getTitle(),
                source.getCollaborators(),
                source.getMilestoneComments(),
                source.getInitialCreatedBy(),
                source.getInitialCreationInstant(),
                updatedContentOptionObj,
                source.getMetadata(),
                source.isClonedProposal(),
                source.getOriginRef(),
                source.getClonedFrom(),
                source.getRevisionStatus(),
                source.getClonedMilestoneIds(),
                source.getContributionStatus()
        );
    }

    @Subscribe
    void saveAndCloseAfterReplace(SaveAndCloseAfterReplaceEvent event){
        // save document into repository
        Proposal proposal = getDocument();

        Proposal proposalFromSession = (Proposal) httpSession.getAttribute("proposal#" + getDocumentRef());
        httpSession.removeAttribute("proposal#" + getDocumentRef());
        byte[] updatedContent = proposalFromSession.getContent().get().getSource().getBytes();
        proposal = proposalService.updateProposal(proposal, updatedContent, VersionType.MINOR,
                messageHelper.getMessage("operation.search.replace.updated"));
        if (proposal != null) {
            eventBus.post(new RefreshDocumentEvent());
            eventBus.post(new DocumentUpdatedEvent());
            leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
            eventBus.post(new NotificationEvent(Type.INFO, "document.replace.success"));
        }
    }

    @Subscribe
    void saveAfterReplace(SaveAfterReplaceEvent event){
        // save document into repository
        Proposal proposal = getDocument();

        Proposal proposalFromSession = (Proposal) httpSession.getAttribute("proposal#" + getDocumentRef());

        byte[] updatedContent = proposalFromSession.getContent().get().getSource().getBytes();
        proposal = proposalService.updateProposal(proposal, updatedContent,
                VersionType.MINOR, messageHelper.getMessage("operation.search.replace.updated"));
        if (proposal != null) {
            httpSession.setAttribute("proposal#"+getDocumentRef(), proposal);
            eventBus.post(new DocumentUpdatedEvent());
            leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
            eventBus.post(new NotificationEvent(Type.INFO, "document.replace.success"));
        }
    }

    @Subscribe
    void replaceOneTextInDocument(ReplaceMatchRequestEvent event) {
        if (event.getSearchMatchVO().isReplaceable()) {
            Proposal proposalFromSession = getProposalFromSession();
            if (proposalFromSession == null) {
                proposalFromSession = getDocument();
            }

            byte[] updatedContent = searchService.replaceText(
                    getContent(proposalFromSession),
                    event.getSearchText(),
                    event.getReplaceText(),
                    Arrays.asList(event.getSearchMatchVO()));

            Proposal proposalUpdated = copyIntoNew(proposalFromSession, updatedContent);
            httpSession.setAttribute("proposal#" + getDocumentRef(), proposalUpdated);
            coverPageScreen.setContent(getEditableXml(proposalUpdated));
            coverPageScreen.refineSearch(event.getSearchId(), event.getMatchIndex(), true);
        } else {
            coverPageScreen.refineSearch(event.getSearchId(), event.getMatchIndex(), false);
        }
    }

    @Subscribe
    void closeSearchBar(SearchBarClosedEvent event) {
        //Cleanup the session etc
        coverPageScreen.closeSearchBar();
        httpSession.removeAttribute("proposal#"+getDocumentRef());
        eventBus.post(new RefreshDocumentEvent());
    }

    @Subscribe
    void exportToToolBox(ToolBoxExportRequestEvent event) {
        try {
            cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);
            this.createDocumentPackageForExport(event.getExportOptions());
            eventBus.post(new NotificationEvent("document.export.package.button.send", "document.export.message",
                    NotificationEvent.Type.TRAY, event.getExportOptions().getExportOutputDescription(), user.getEmail()));
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while using ToolBoxExportService", e);
            eventBus.post(new NotificationEvent(Type.ERROR, "export.package.error.message", e.getMessage()));
        }
    }

    @Subscribe
    public void afterClosedWindow(WindowClosedEvent<MilestoneExplorer> windowClosedEvent) {
        milestoneExplorerOpened = false;
    }

    private void createDocumentPackageForExport(ExportOptions exportOptions) throws Exception {
        Stopwatch stopwatch = Stopwatch.createStarted();
        final String proposalId = this.getContextProposalId();

        if (proposalId != null) {
            final String jobFileName = "Proposal_" + proposalId + "_AKN2DW_" + System.currentTimeMillis() + ".zip";
            exportService.createDocumentPackage(jobFileName, proposalId, exportOptions, user);
            LOG.info("Exported to LegisWrite and downloaded file {}, in {} milliseconds ({} sec)", jobFileName,
                    stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
        }
    }

    private String getContextProposalId(){
        LeosPackage leosPackage = packageService.findPackageByDocumentId(documentId);
        BillContext context = billContextProvider.get();
        context.usePackage(leosPackage);
        return context.getProposalId();
    }

    private boolean isClonedProposal() {
        return cloneContext != null && cloneContext.isClonedProposal();
    }
}