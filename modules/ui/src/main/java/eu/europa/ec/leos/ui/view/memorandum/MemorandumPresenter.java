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
package eu.europa.ec.leos.ui.view.memorandum;

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
import eu.europa.ec.leos.domain.cmis.document.Memorandum;
import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.domain.cmis.metadata.LeosMetadata;
import eu.europa.ec.leos.domain.cmis.metadata.MemorandumMetadata;
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
import eu.europa.ec.leos.security.LeosPermission;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.clone.CloneContext;
import eu.europa.ec.leos.services.document.ContributionService;
import eu.europa.ec.leos.services.document.DocumentContentService;
import eu.europa.ec.leos.services.document.MemorandumService;
import eu.europa.ec.leos.services.document.ProposalService;
import eu.europa.ec.leos.services.export.ExportLW;
import eu.europa.ec.leos.services.export.ExportOptions;
import eu.europa.ec.leos.services.export.ExportService;
import eu.europa.ec.leos.services.export.ExportVersions;
import eu.europa.ec.leos.services.export.ZipPackageUtil;
import eu.europa.ec.leos.services.processor.AttachmentProcessor;
import eu.europa.ec.leos.services.processor.ElementProcessor;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.search.SearchService;
import eu.europa.ec.leos.services.store.LegService;
import eu.europa.ec.leos.services.store.PackageService;
import eu.europa.ec.leos.services.store.WorkspaceService;
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
import eu.europa.ec.leos.ui.view.AbstractLeosPresenter;
import eu.europa.ec.leos.ui.view.CommonDelegate;
import eu.europa.ec.leos.ui.view.ComparisonDelegate;
import eu.europa.ec.leos.ui.view.ComparisonDisplayMode;
import eu.europa.ec.leos.ui.view.MergeContributionHelper;
import eu.europa.ec.leos.ui.window.milestone.MilestoneExplorer;
import eu.europa.ec.leos.usecases.document.BillContext;
import eu.europa.ec.leos.usecases.document.CollectionContext;
import eu.europa.ec.leos.vo.coedition.InfoType;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItem;
import eu.europa.ec.leos.web.event.NavigationRequestEvent;
import eu.europa.ec.leos.web.event.NotificationEvent;
import eu.europa.ec.leos.web.event.NotificationEvent.Type;
import eu.europa.ec.leos.web.event.component.CleanComparedContentEvent;
import eu.europa.ec.leos.web.event.component.CompareRequestEvent;
import eu.europa.ec.leos.web.event.component.CompareTimeLineRequestEvent;
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
import eu.europa.ec.leos.web.event.view.document.ComparisonEvent;
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
import eu.europa.ec.leos.web.event.window.ShowTimeLineWindowEvent;
import eu.europa.ec.leos.web.model.VersionInfoVO;
import eu.europa.ec.leos.web.support.SessionAttribute;
import eu.europa.ec.leos.web.support.UrlBuilder;
import eu.europa.ec.leos.web.support.UuidHelper;
import eu.europa.ec.leos.web.support.user.UserHelper;
import eu.europa.ec.leos.web.support.xml.DownloadStreamResource;
import eu.europa.ec.leos.web.ui.navigation.Target;
import eu.europa.ec.leos.web.ui.screen.document.ColumnPosition;
import io.atlassian.fugue.Option;
import org.apache.commons.io.FileUtils;
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
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Scope("prototype")
class MemorandumPresenter extends AbstractLeosPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(MemorandumPresenter.class);

    private final MemorandumScreen memorandumScreen;
    private final MemorandumService memorandumService;
    private final ElementProcessor<Memorandum> elementProcessor;
    private final DocumentContentService documentContentService;
    private final UrlBuilder urlBuilder;
    private final TemplateConfigurationService templateConfigurationService;
    private final ComparisonDelegate<Memorandum> comparisonDelegate;
    private final UserHelper userHelper;
    private final MessageHelper messageHelper;
    private final Provider<CollectionContext> proposalContextProvider;
    private final CoEditionHelper coEditionHelper;
    private final ExportService exportService;
    private final Provider<BillContext> billContextProvider;
    private final Provider<StructureContext> structureContextProvider;
    private final LegService legService;
    private final ProposalService proposalService;
    private final SearchService searchService;
    private final CommonDelegate<Memorandum> commonDelegate;
    private final CloneContext cloneContext;
    private CloneProposalMetadataVO cloneProposalMetadataVO;
    private final ContributionService contributionService;
    private final InstanceTypeResolver instanceTypeResolver;
    private final AttachmentProcessor attachmentProcessor;

    private String strDocumentVersionSeriesId;
    private String documentId;
    private String documentRef;
    private boolean comparisonMode;
    private String proposalRef;
    private String connectedEntity;
    private boolean milestoneExplorerOpened = false;
    private MergeContributionHelper mergeContributionHelper;
    private XmlContentProcessor xmlContentProcessor;

    private final static SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    @Autowired
    MemorandumPresenter(SecurityContext securityContext, HttpSession httpSession, EventBus eventBus,
                        MemorandumScreen memorandumScreen,
                        MemorandumService memorandumService,
                        ElementProcessor<Memorandum> elementProcessor,
                        DocumentContentService documentContentService,
                        UrlBuilder urlBuilder,
                        TemplateConfigurationService templateConfigurationService,
                        ComparisonDelegate<Memorandum> comparisonDelegate,
                        UserHelper userHelper, MessageHelper messageHelper,
                        Provider<CollectionContext> proposalContextProvider,
                        CoEditionHelper coEditionHelper, EventBus leosApplicationEventBus, UuidHelper uuidHelper,
                        Provider<StructureContext> structureContextProvider,
                        PackageService packageService,
                        ExportService exportService,
                        Provider<BillContext> billContextProvider,
                        WorkspaceService workspaceService, LegService legService,
                        ProposalService proposalService,
                        SearchService searchService, CommonDelegate<Memorandum> commonDelegate,
                        CloneContext cloneContext, ContributionService contributionService, InstanceTypeResolver instanceTypeResolver, AttachmentProcessor attachmentProcessor, MergeContributionHelper mergeContributionHelper, XmlContentProcessor xmlContentProcessor) {
        super(securityContext, httpSession, eventBus, leosApplicationEventBus, uuidHelper, packageService, workspaceService);
        this.instanceTypeResolver = instanceTypeResolver;
        this.attachmentProcessor = attachmentProcessor;
        this.mergeContributionHelper = mergeContributionHelper;
        this.xmlContentProcessor = xmlContentProcessor;
        LOG.trace("Initializing memorandum presenter...");
        this.contributionService = contributionService;
        this.memorandumScreen = memorandumScreen;
        this.memorandumService = memorandumService;
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
        this.proposalService = proposalService;
        this.searchService = searchService;
        this.commonDelegate = commonDelegate;
        this.cloneContext = cloneContext;
        this.exportService = exportService;
        this.billContextProvider = billContextProvider;
        this.xmlContentProcessor = xmlContentProcessor;
        this.mergeContributionHelper = mergeContributionHelper;
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
            Memorandum memorandum = getDocument();
            populateViewData(memorandum);
            populateVersionsData(memorandum);
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
        Proposal proposal = null;
        Memorandum memorandum = getDocument();
        if (memorandum != null) {
            LeosPackage leosPackage = packageService.findPackageByDocumentId(memorandum.getId());
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
        return (String) httpSession.getAttribute(id + "." + SessionAttribute.MEMORANDUM_REF.name());
    }

    private Memorandum getDocument() {
        documentRef = getDocumentRef();
        Memorandum memorandum = memorandumService.findMemorandumByRef(documentRef);
        setDocumentData(memorandum);
        return memorandum;
    }

    private void setDocumentData(Memorandum memorandum) {
        strDocumentVersionSeriesId = memorandum.getVersionSeriesId();
        documentId = memorandum.getId();
        structureContextProvider.get().useDocumentTemplate(memorandum.getMetadata().getOrError(() -> "Memorandum metadata is required!").getDocTemplate());
        cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);
    }

    private void populateViewData(Memorandum memorandum) {
        Validate.notNull(memorandum, "Memorandum document should not be null");
        try{
            memorandumScreen.setTitle(memorandum.getTitle());
            memorandumScreen.setDocumentVersionInfo(getVersionInfo(memorandum));
            String content = getEditableXml(memorandum);
            memorandumScreen.setContent(content);
            memorandumScreen.setToc(getTableOfContent(memorandum));
            DocumentVO memorandumVO = createMemorandumVO(memorandum);
            memorandumScreen.setPermissions(memorandumVO, isClonedProposal());
            memorandumScreen.initAnnotations(memorandumVO, proposalRef, connectedEntity);
            memorandumScreen.updateUserCoEditionInfo(coEditionHelper.getCurrentEditInfo(memorandum.getVersionSeriesId()), id);
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
        memorandumScreen.initLeosEditor(event.getDocument(), documentsMetadata);
    }
    
    private void populateVersionsData(Memorandum memorandum) {
        DocumentVO memorandumVO = createMemorandumVO(memorandum);
        final List<VersionVO> allVersions = getVersionVOS();
        final List<ContributionVO> allContributions = contributionService.getDocumentContributions(documentId, 0, Memorandum.class);
        memorandumScreen.setDataFunctions(
                memorandumVO,
                allVersions,
                allContributions,
                this::majorVersionsFn, this::countMajorVersionsFn,
                this::minorVersionsFn, this::countMinorVersionsFn,
                this::recentChangesFn, this::countRecentChangesFn);
        memorandumScreen.setContributionsData(allContributions);
    }

    private List<VersionVO> getVersionVOS() {
        return memorandumService.getAllVersions(documentId, documentRef);
    }

    @Subscribe
    public void updateVersionsTab(DocumentUpdatedEvent event) {
        final List<VersionVO> allVersions = getVersionVOS();
        memorandumScreen.refreshVersions(allVersions, comparisonMode);
    }
    
    private Integer countMinorVersionsFn(String currIntVersion) {
        return memorandumService.findAllMinorsCountForIntermediate(documentRef, currIntVersion);
    }
    
    private List<Memorandum> minorVersionsFn(String currIntVersion, int startIndex, int maxResults) {
        return memorandumService.findAllMinorsForIntermediate(documentRef, currIntVersion, startIndex, maxResults);
    }
    
    private Integer countMajorVersionsFn() {
        return memorandumService.findAllMajorsCount(documentRef);
    }
    
    private List<Memorandum> majorVersionsFn(int startIndex, int maxResults) {
        return memorandumService.findAllMajors(documentRef, startIndex, maxResults);
    }
    
    private Integer countRecentChangesFn() {
        return memorandumService.findRecentMinorVersionsCount(documentId, documentRef);
    }
    
    private List<Memorandum> recentChangesFn(int startIndex, int maxResults) {
        return memorandumService.findRecentMinorVersions(documentId, documentRef, startIndex, maxResults);
    }

    private List<TableOfContentItemVO> getTableOfContent(Memorandum memorandum) {
        return memorandumService.getTableOfContent(memorandum, TocMode.NOT_SIMPLIFIED);
    }

    @Subscribe
    void getDocumentVersionsList(VersionListRequestEvent<Memorandum> event) {
        List<Memorandum> memoVersions = memorandumService.findVersions(documentId);
        eventBus.post(new VersionListResponseEvent(new ArrayList<>(memoVersions)));
    }

    private String getEditableXml(Memorandum memorandum) {
        securityContext.getPermissions(memorandum);
        byte[] coverPageContent = new byte[0];
        byte[] memorandumContent = memorandum.getContent().get().getSource().getBytes();
        boolean isCoverPageExists = documentContentService.isCoverPageExists(memorandumContent);
        cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);
        if(memorandumScreen.isCoverPageVisible() && !isCoverPageExists) {
            Proposal proposal = getProposalFromPackage();
            byte[] xmlContent = proposal.getContent().get().getSource().getBytes();
            coverPageContent = documentContentService.getCoverPageContent(xmlContent);
        }
        String editableXml = documentContentService.toEditableContent(memorandum,
                urlBuilder.getWebAppPath(VaadinServletService.getCurrentServletRequest()), securityContext, coverPageContent);
        return StringEscapeUtils.unescapeXml(editableXml);
    }

    @Subscribe
    void handleCloseDocument(CloseDocumentEvent event) {
        LOG.trace("Handling close document request...");

        //if unsaved changes remain in the session, first ask for confirmation
        if(isMemorandumUnsaved()){
            eventBus.post(new ShowConfirmDialogEvent(event, null));
            return;
        }
        coEditionHelper.removeUserEditInfo(id, strDocumentVersionSeriesId, null, InfoType.DOCUMENT_INFO);
        resetCloneProposalMetadataVO();
        eventBus.post(new NavigationRequestEvent(Target.PREVIOUS));
    }

    private boolean isMemorandumUnsaved(){
        return getMemorandumFromSession() != null;
    }
    private Memorandum getMemorandumFromSession() {
        return (Memorandum) httpSession.getAttribute("memorandum#" + getDocumentRef());
    }

    @Subscribe
    void handleCloseBrowserRequest(CloseBrowserRequestEvent event) {
        coEditionHelper.removeUserEditInfo(id, strDocumentVersionSeriesId, null, InfoType.DOCUMENT_INFO);
        resetCloneProposalMetadataVO();
    }

    @Subscribe
    void handleCloseScreenRequest(CloseScreenRequestEvent event) {
        if (memorandumScreen.isTocEnabled()) {
            eventBus.post(new CloseTocAndDocumentEvent());
        } else {
            eventBus.post(new CloseDocumentEvent());
        }
    }

    @Subscribe
    void refreshDocument(RefreshDocumentEvent event){
        Memorandum memorandum = getDocument();
        populateViewData(memorandum);
    }

    @Subscribe
    void checkElementCoEdition(CheckElementCoEditionEvent event) {
        memorandumScreen.checkElementCoEdition(coEditionHelper.getCurrentEditInfo(strDocumentVersionSeriesId), user,
                event.getElementId(), event.getElementTagName(), event.getAction(), event.getActionEvent());
    }


    @Subscribe
    void cancelElementEditor(CancelElementEditorEvent event) {
        String elementId = event.getElementId();
        coEditionHelper.removeUserEditInfo(id, strDocumentVersionSeriesId, elementId, InfoType.ELEMENT_INFO);

        //load content from session if exists
        Memorandum memorandumFromSession = getMemorandumFromSession();
        if(memorandumFromSession != null) {
            memorandumScreen.setContent(getEditableXml(memorandumFromSession));
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
            if(isMemorandumUnsaved()){
                eventBus.post(new ShowConfirmDialogEvent(event, new CancelElementEditorEvent(event.getElementId(),event.getElementTagName())));
                return;
            }
            Memorandum memorandum = getDocument();
            String element = elementProcessor.getElement(memorandum, elementTagName, elementId);
            coEditionHelper.storeUserEditInfo(httpSession.getId(), id, user, strDocumentVersionSeriesId, elementId, InfoType.ELEMENT_INFO);
            memorandumScreen.showElementEditor(elementId, elementTagName, element);
        }
        catch (Exception ex){
            LOG.error("Exception while edit element operation for memorandum", ex);
            eventBus.post(new NotificationEvent(Type.INFO, "error.message", ex.getMessage()));
        }
    }

    @Subscribe
    void saveElement(SaveElementRequestEvent event) {
        String elementId = event.getElementId();
        String elementTagName = event.getElementTagName();
        LOG.trace("Handling save element request... for {},id={}",elementTagName , elementId );

        try {
            Memorandum memorandum = getDocument();
            byte[] newXmlContent = elementProcessor.updateElement(memorandum, event.getElementContent(), elementTagName, elementId);
            if (newXmlContent == null) {
                memorandumScreen.showAlertDialog("operation.element.not.performed");
                return;
            }

            memorandum = memorandumService.updateMemorandum(memorandum, newXmlContent, VersionType.MINOR, messageHelper.getMessage("operation." + elementTagName + ".updated"));

            if (memorandum != null) {
                String elementContent = elementProcessor.getElement(memorandum, elementTagName, elementId);
                memorandumScreen.refreshElementEditor(elementId, elementTagName, elementContent);
                eventBus.post(new DocumentUpdatedEvent()); //Document might be updated.
                memorandumScreen.scrollToMarkedChange(elementId);
                leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
            }
        } catch (Exception ex) {
            LOG.error("Exception while save  memorandum operation", ex);
            eventBus.post(new NotificationEvent(Type.INFO, "error.message", ex.getMessage()));
        }
    }

    @Subscribe
    void closeElementEditor(CloseElementEditorEvent event){
        String elementId = event.getElementId();
        coEditionHelper.removeUserEditInfo(id, strDocumentVersionSeriesId, elementId, InfoType.ELEMENT_INFO);
        LOG.debug("User edit information removed");
        eventBus.post(new RefreshDocumentEvent());
    }

    @Subscribe
    public void getUserGuidance(FetchUserGuidanceRequest event) {
        // KLUGE temporary hack for compatibility with new domain model
        Memorandum memorandum = memorandumService.findMemorandum(documentId, true);
        String jsonGuidance = templateConfigurationService.getTemplateConfiguration(memorandum.getMetadata().get().getDocTemplate(), "guidance");
        memorandumScreen.setUserGuidance(jsonGuidance);
    }

    @Subscribe
    void mergeSuggestion(MergeSuggestionRequest event) {
        Memorandum document = getDocument();
        byte[] resultXmlContent = elementProcessor.replaceTextInElement(document, event.getOrigText(), event.getNewText(), event.getElementId(), event.getStartOffset(), event.getEndOffset());
        if (resultXmlContent == null) {
            eventBus.post(new MergeSuggestionResponse(messageHelper.getMessage("document.merge.suggestion.failed"), MergeSuggestionResponse.Result.ERROR));
            return;
        }
        document = memorandumService.updateMemorandum(document, resultXmlContent, VersionType.MINOR, messageHelper.getMessage("operation.merge.suggestion"));
        if (document != null) {
            eventBus.post(new RefreshDocumentEvent());
            eventBus.post(new DocumentUpdatedEvent());
            leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
            eventBus.post(new MergeSuggestionResponse(messageHelper.getMessage("document.merge.suggestion.success"), MergeSuggestionResponse.Result.SUCCESS));
        }
        else {
            eventBus.post(new MergeSuggestionResponse(messageHelper.getMessage("document.merge.suggestion.failed"), MergeSuggestionResponse.Result.ERROR));
        }
    }

    @Subscribe
    public void mergeSuggestions(MergeSuggestionsRequest event) {
        Memorandum document = getDocument();
        commonDelegate.mergeSuggestions(document, event, elementProcessor, memorandumService::updateMemorandum);
    }

    @Subscribe
    public void getUserPermissions(FetchUserPermissionsRequest event) {
        Memorandum memorandum = getDocument();
        List<LeosPermission> userPermissions = securityContext.getPermissions(memorandum);
        memorandumScreen.sendUserPermissions(userPermissions);
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
        Memorandum memorandum = getDocument();
        metadata.setVersion(memorandum.getVersionLabel());
        metadata.setId(memorandum.getId());
        metadata.setTitle(memorandum.getTitle());
        eventBus.post(new DocumentMetadataResponse(metadata));
    }

    @Subscribe
    void showTimeLineWindow(ShowTimeLineWindowEvent event) {
        List documentVersions = memorandumService.findVersions(documentId);
        memorandumScreen.showTimeLineWindow(documentVersions);
    }
    
    @Subscribe
    void downloadXmlFiles(DownloadXmlFilesRequestEvent event) {
        final ExportVersions<Memorandum> exportVersions = event.getExportOptions().getExportVersions();
        final Memorandum current = exportVersions.getCurrent();
        final Memorandum original = exportVersions.getOriginal();
        final Memorandum intermediate = exportVersions.getIntermediate();

        final String leosComparedContent;
        final String docuWriteComparedContent;
        final String comparedInfo;
        String language = original.getMetadata().get().getLanguage();

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
        memorandumScreen.setDownloadStreamResourceForXmlFiles(original, intermediate, current, language, comparedInfo, leosComparedContent, docuWriteComparedContent);
    }
    
    @Subscribe
    void downloadXmlVersion(DownloadXmlVersionRequestEvent event) {
        try {
            cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);
            final Memorandum chosenDocument = memorandumService.findMemorandumVersion(event.getVersionId());
            final String fileName = chosenDocument.getMetadata().get().getRef() + "_v" + chosenDocument.getVersionLabel() + ".xml";
    
            DownloadStreamResource downloadStreamResource = new DownloadStreamResource(fileName, new ByteArrayInputStream(chosenDocument.getContent().get().getSource().getBytes()));
            memorandumScreen.setDownloadStreamResourceForVersion(downloadStreamResource, chosenDocument.getId());
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
            ExportOptions exportOptions = new ExportLW(ExportOptions.Output.PDF, Memorandum.class, false, true);
            exportOptions.setExportVersions(new ExportVersions(null, getDocument()));
            exportService.createDocumentPackage(jobFileName, proposalId, exportOptions, user);
            eventBus.post(new NotificationEvent("document.export.package.button.send", "document.export.message",
                    NotificationEvent.Type.TRAY, exportOptions.getExportOutputDescription(), user.getEmail()));
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while using ExportService", e);
            eventBus.post(new NotificationEvent(Type.ERROR, "export.legiswrite.error.message", e.getMessage()));
        }
        LOG.info("The actual version of CLEANED Memorandum for proposal {}, downloaded in {} milliseconds ({} sec)", proposalId, stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
    }

    @Subscribe
    void downloadActualVersion(DownloadActualVersionRequestEvent event) {
        requestFilteredAnnotationsForDownload(event.isWithFilteredAnnotations());
    }

    private void doDownloadActualVersion(Boolean isWithAnnotations, String annotations) {
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            final Memorandum currentDocument = getDocument();
            ExportOptions exportOptions = new ExportLW(ExportOptions.Output.PDF, Memorandum.class, false);
            exportOptions.setExportVersions(new ExportVersions(isClonedProposal() ?
                    documentContentService.getOriginalMemorandum(currentDocument) : null, currentDocument));
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
            LOG.info("The actual version of Memorandum {} downloaded in {} milliseconds ({} sec)", currentDocument.getName(),
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
        Memorandum version = memorandumService.findMemorandumVersion(versionId);
        byte[] resultXmlContent = getContent(version);
        memorandumService.updateMemorandum(getDocument(), resultXmlContent, VersionType.MINOR, messageHelper.getMessage("operation.restore.version", version.getVersionLabel()));

        List documentVersions = memorandumService.findVersions(documentId);
        memorandumScreen.updateTimeLineWindow(documentVersions);
        eventBus.post(new RefreshDocumentEvent());
        eventBus.post(new DocumentUpdatedEvent()); //Document might be updated.
        leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
    }
    
    @Subscribe
    void cleanComparedContent(CleanComparedContentEvent event) {
        memorandumScreen.cleanComparedContent();
    }
    
    @Subscribe
    void showVersion(ShowVersionRequestEvent event) {
        final Memorandum version = memorandumService.findMemorandumVersion(event.getVersionId());
        final String versionContent = documentContentService.getDocumentAsHtml(version, urlBuilder.getWebAppPath(VaadinServletService.getCurrentServletRequest()),
                securityContext.getPermissions(version));
        final String versionInfo = getVersionInfoAsString(version);
        memorandumScreen.showVersion(versionContent, versionInfo);
    }

    @Subscribe
    void showRevision(OpenRevisionDocumentEvent event) {
        compareAndShowRevision(event.getContributionVO());
        Memorandum memorandum = getDocument();
        setDocumentData(memorandum);
    }

    @Subscribe
    void showCompareAndRevision(CompareAndShowRevisionEvent event) {
        cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);
        compareAndShowRevision(event.getContributionVO());
    }

    private void compareAndShowRevision(ContributionVO contributionVO) {
        final Memorandum revision = memorandumService.findMemorandum(contributionVO.getDocumentId(), false);
        final String revisionContent = documentContentService.getDocumentForContributionAsHtml(contributionVO.getXmlContent(),
                urlBuilder.getWebAppPath(VaadinServletService.getCurrentServletRequest()),
                securityContext.getPermissions(revision));
        cloneContext.setContribution(Boolean.TRUE);
        memorandumScreen.refreshVersions(getVersionVOS(), false);
        Memorandum memorandum = getDocument();
        List<TocItem>  tocItemList = getTocITems(memorandum);
        memorandumScreen.showRevision(revisionContent, revision.getContributionStatus(), contributionVO, tocItemList);
    }

    private List<TocItem> getTocITems(Memorandum memorandum) {
        structureContextProvider.get().useDocumentTemplate(memorandum.getMetadata().getOrError(() -> "Bill metadata is required!").getDocTemplate());
        return structureContextProvider.get().getTocItems();
    }

    @Subscribe
    void declineRevision(DeclineRevisionDocumentEvent event) {
        cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);
        final Memorandum revision = memorandumService.findMemorandum(event.getContributionVO().getDocumentId(), false);
        Map<String, Object> properties = new HashMap<>();
        String contributionStatus = event.getContributionVO().getContributionStatus().getValue();
        String versionNumber = event.getContributionVO().getVersionNumber().toString();
        properties.put(CmisProperties.CONTRIBUTION_STATUS.getId(), contributionStatus);
        Memorandum updatedMemo = memorandumService.updateMemorandum(revision.getId(), properties, false);
        if(updatedMemo != null) {
            memorandumScreen.disableMergePane();
            event.getSelectedItem().setVisible(false);
        } else {
            throw new RuntimeException("Unable to update document revision status");
        }
    }

    void markRevisionAsProcessed(String revisionDocumentId, String msgKey) {
        cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);
        final Memorandum revision = memorandumService.findMemorandum(revisionDocumentId, false);

        memorandumScreen.disableMergePane();
        Map<String, Object> properties = new HashMap<>();
        properties.put(CmisProperties.CONTRIBUTION_STATUS.getId(), ContributionVO.ContributionStatus.CONTRIBUTION_DONE.getValue());
        memorandumService.updateMemorandum(revision.getId(), properties, false);
        final List<ContributionVO> allContributions = contributionService.getDocumentContributions(documentId, 0, Memorandum.class);
        memorandumScreen.populateContributions(allContributions);
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
        Memorandum memorandum = getDocument();
        List<TocItem> tocItemList = getTocITems(memorandum);
        byte[] xmlClonedContent = event.getMergeActionVOS().get(0).getContributionVO().getXmlContent();
        List<InternalRefMap> intRefMap = getInternalRefMaps(event, memorandum, xmlClonedContent);
        byte[] xmlContent = mergeContributionHelper.updateDocumentWithContributions(event, memorandum, tocItemList, intRefMap);
        xmlContent = xmlContentProcessor.doXMLPostProcessing(xmlContent);
        updateMemorandumContent(memorandum, xmlContent, messageHelper.getMessage("contribution.merge.operation.message"), mergeActionKey);
        if(event.isAllContributions()) {
            markRevisionAsProcessed(event.getMergeActionVOS().get(0).getContributionVO().getDocumentId(), "contribution.accept.all.notification.message");
        } else {
            eventBus.post(new RefreshContributionEvent());
        }
    }

    private List<InternalRefMap> getInternalRefMaps(ApplyContributionsRequestEvent event, Memorandum memorandum, byte[] xmlClonedContent) {
        byte[] xmlContent = memorandum.getContent().get().getSource().getBytes();
        Map<String, String> attachmentsClonedContent = attachmentProcessor.getAttachmentsHrefFromBill(xmlClonedContent);
        List<InternalRefMap> map = new ArrayList<>();
        String ref = memorandum.getName().replace(".xml", "");
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

    private void updateMemorandumContent( Memorandum memorandum, byte[] xmlContent, String operationMsg, String notificationMsg) {
        memorandum = memorandumService.updateMemorandum(memorandum, xmlContent, operationMsg);
        if (memorandum != null) {
            eventBus.post(new NotificationEvent(Type.INFO, notificationMsg));
            eventBus.post(new RefreshDocumentEvent());
            eventBus.post(new DocumentUpdatedEvent());
            leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
        }
    }

    @Subscribe
    void showCleanVersion(ShowCleanVersionRequestEvent event) {
        final Memorandum memorandum = getDocument();
        final String versionContent = documentContentService.getCleanDocumentAsHtml(memorandum, urlBuilder.getWebAppPath(VaadinServletService.getCurrentServletRequest()),
                securityContext.getPermissions(memorandum));
        final String versionInfo = getVersionInfoAsString(memorandum);
        memorandumScreen.showCleanVersion(versionContent, versionInfo);
    }

    @Subscribe
    void refreshCleanVersion(DocumentUpdatedEvent event) {
        if (memorandumScreen.isCleanVersionShowed()) {
            showCleanVersion(new ShowCleanVersionRequestEvent());
        }
    }
    
    @Subscribe
    public void fetchMilestoneByVersionedReference(FetchMilestoneByVersionedReferenceEvent event) {
        LeosPackage leosPackage = packageService.findPackageByDocumentId(documentId);
        LegDocument legDocument = legService.findLastLegByVersionedReference(leosPackage.getPath(), event.getVersionedReference());
        milestoneExplorerOpened = true;
        memorandumScreen.showMilestoneExplorer(legDocument, String.join(",", legDocument.getMilestoneComments()), proposalRef);
    }
    
    @Subscribe
    void compare(CompareRequestEvent event) {
        cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);
        final Memorandum oldVersion = memorandumService.findMemorandumVersion(event.getOldVersionId());
        final Memorandum newVersion = memorandumService.findMemorandumVersion(event.getNewVersionId());
        String comparedContent = comparisonDelegate.getMarkedContent(oldVersion, newVersion);
        final String comparedInfo = messageHelper.getMessage("version.compare.simple", oldVersion.getVersionLabel(), newVersion.getVersionLabel());
        memorandumScreen.populateComparisonContent(comparedContent, comparedInfo, oldVersion, newVersion);
    }
    
    @Subscribe
    void compareUpdateTimelineWindow(CompareTimeLineRequestEvent event) {
        String oldVersionId = event.getOldVersion();
        String newVersionId = event.getNewVersion();
        ComparisonDisplayMode displayMode = event.getDisplayMode();
        HashMap<ComparisonDisplayMode, Object> result = comparisonDelegate.versionCompare(memorandumService.findMemorandumVersion(oldVersionId), memorandumService.findMemorandumVersion(newVersionId), displayMode);
        memorandumScreen.displayComparison(result);        
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
            memorandumScreen.cleanComparedContent();
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
        memorandumScreen.showIntermediateVersionWindow();
    }

    @Subscribe
    public void saveIntermediateVersion(SaveIntermediateVersionEvent event) {
        Memorandum memorandum = memorandumService.createVersion(documentId, event.getVersionType(), event.getCheckinComment());
        setDocumentData(memorandum);
        eventBus.post(new NotificationEvent(NotificationEvent.Type.INFO, "document.major.version.saved"));
        eventBus.post(new DocumentUpdatedEvent());
        leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, memorandum.getVersionSeriesId(), id));
        populateViewData(memorandum);
    }
    
    private byte[] getContent(Memorandum memorandum) {
        final Content content = memorandum.getContent().getOrError(() -> "Memorandum content is required!");
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

    private DocumentVO createMemorandumVO(Memorandum memorandum) {
        DocumentVO memorandumVO = new DocumentVO(memorandum.getId(),
                memorandum.getMetadata().exists(m -> m.getLanguage() != null) ? memorandum.getMetadata().get().getLanguage() : "EN",
                LeosCategory.MEMORANDUM,
                memorandum.getLastModifiedBy(),
                Date.from(memorandum.getLastModificationInstant()));
        if (memorandum.getMetadata().isDefined()) {
            MemorandumMetadata metadata = memorandum.getMetadata().get();
            memorandumVO.getMetadata().setInternalRef(metadata.getRef());
        }
        if(!memorandum.getCollaborators().isEmpty()) {
            memorandumVO.addCollaborators(memorandum.getCollaborators());
        }
        
        return memorandumVO;
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
            LOG.debug("Memorandum Presenter updated the edit info -" + updateUserInfoEvent.getActionInfo().getOperation().name());
            memorandumScreen.updateUserCoEditionInfo(updateUserInfoEvent.getActionInfo().getCoEditionVos(), id);
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
            memorandumScreen.displayDocumentUpdatedByCoEditorWarning();
        }
    }

    @Subscribe
    void searchTextInDocument(SearchTextRequestEvent event) {
        Memorandum memorandum = (Memorandum) httpSession.getAttribute("memorandum#" + getDocumentRef());
        if (memorandum == null) {
            memorandum = getDocument();
        }
        List<SearchMatchVO> matches = Collections.emptyList();
        try {
            matches = searchService.searchText(getContent(memorandum), event.getSearchText(), event.matchCase, event.completeWords);
        } catch (Exception e) {
            eventBus.post(new NotificationEvent(Type.ERROR, "Error while searching{1}", e.getMessage()));
        }

        memorandumScreen.showMatchResults(event.searchID, matches);
    }

    @Subscribe
    void replaceAllTextInDocument(ReplaceAllMatchRequestEvent event) {
        Memorandum memorandumFromSession = getMemorandumFromSession();
        if (memorandumFromSession == null) {
            memorandumFromSession = getDocument();
        }

        byte[] updatedContent = searchService.replaceText(
                getContent(memorandumFromSession),
                event.getSearchText(),
                event.getReplaceText(),
                event.getSearchMatchVOs());

        Memorandum memorandumUpdated = copyIntoNew(memorandumFromSession, updatedContent);
        httpSession.setAttribute("memorandum#" + getDocumentRef(), memorandumUpdated);
        memorandumScreen.setContent(getEditableXml(memorandumUpdated));
        eventBus.post(new ReplaceAllMatchResponseEvent(true));
    }

    private Memorandum copyIntoNew(Memorandum source, byte[] updatedContent) {
        Content contentFromSession = source.getContent().get();
        Content.Source updatedSource = new SourceImpl(new ByteArrayInputStream(updatedContent));
        Content contentObj = new ContentImpl(
                contentFromSession.getFileName(),
                contentFromSession.getMimeType(),
                updatedContent.length,
                updatedSource
        );
        Option<Content> updatedContentOptionObj = Option.option(contentObj);
        return new Memorandum(
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
                updatedContentOptionObj,
                source.getContributionStatus(),
                source.getClonedFrom(),
                source.getMetadata()
        );
    }

    @Subscribe
    void saveAndCloseAfterReplace(SaveAndCloseAfterReplaceEvent event){
        // save document into repository
        Memorandum memorandum = getDocument();

        Memorandum memorandumFromSession = (Memorandum) httpSession.getAttribute("memorandum#" + getDocumentRef());
        httpSession.removeAttribute("memorandum#" + getDocumentRef());

        memorandum = memorandumService.updateMemorandum(memorandum, memorandumFromSession.getContent().get().getSource().getBytes(),
                VersionType.MINOR, messageHelper.getMessage("operation.search.replace.updated"));
        if (memorandum != null) {
            eventBus.post(new RefreshDocumentEvent());
            eventBus.post(new DocumentUpdatedEvent());
            leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
            eventBus.post(new NotificationEvent(Type.INFO, "document.replace.success"));
        }
    }

    @Subscribe
    void saveAfterReplace(SaveAfterReplaceEvent event){
        // save document into repository
        Memorandum memorandum = getDocument();

        Memorandum memorandumFromSession = (Memorandum) httpSession.getAttribute("memorandum#" + getDocumentRef());

        memorandum = memorandumService.updateMemorandum(memorandum, memorandumFromSession.getContent().get().getSource().getBytes(),
                VersionType.MINOR, messageHelper.getMessage("operation.search.replace.updated"));
        if (memorandum != null) {
            httpSession.setAttribute("memorandum#"+getDocumentRef(), memorandum);
            eventBus.post(new DocumentUpdatedEvent());
            leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
            eventBus.post(new NotificationEvent(Type.INFO, "document.replace.success"));
        }
    }

    @Subscribe
    void replaceOneTextInDocument(ReplaceMatchRequestEvent event) {
        if (event.getSearchMatchVO().isReplaceable()) {
            Memorandum memorandumFromSession = getMemorandumFromSession();
            if (memorandumFromSession == null) {
                memorandumFromSession = getDocument();
            }

            byte[] updatedContent = searchService.replaceText(
                    getContent(memorandumFromSession),
                    event.getSearchText(),
                    event.getReplaceText(),
                    Arrays.asList(event.getSearchMatchVO()));

            Memorandum memorandumUpdated = copyIntoNew(memorandumFromSession, updatedContent);
            httpSession.setAttribute("memorandum#" + getDocumentRef(), memorandumUpdated);
            memorandumScreen.setContent(getEditableXml(memorandumUpdated));
            memorandumScreen.refineSearch(event.getSearchId(), event.getMatchIndex(), true);
        } else {
            memorandumScreen.refineSearch(event.getSearchId(), event.getMatchIndex(), false);
        }
    }

    @Subscribe
    void closeSearchBar(SearchBarClosedEvent event) {
        //Cleanup the session etc
        memorandumScreen.closeSearchBar();
        httpSession.removeAttribute("memorandum#"+getDocumentRef());
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