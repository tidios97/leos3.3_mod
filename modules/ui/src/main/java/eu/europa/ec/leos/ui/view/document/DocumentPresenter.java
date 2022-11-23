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
package eu.europa.ec.leos.ui.view.document;

import com.google.common.base.Stopwatch;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.server.VaadinServletService;
import eu.europa.ec.leos.cmis.domain.ContentImpl;
import eu.europa.ec.leos.cmis.domain.SourceImpl;
import eu.europa.ec.leos.cmis.mapping.CmisProperties;
import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.Content.Source;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.LeosExportStatus;
import eu.europa.ec.leos.domain.cmis.LeosPackage;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.document.Bill;
import eu.europa.ec.leos.domain.cmis.document.ExportDocument;
import eu.europa.ec.leos.domain.cmis.document.LegDocument;
import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.domain.cmis.metadata.BillMetadata;
import eu.europa.ec.leos.domain.cmis.metadata.LeosMetadata;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.domain.common.Result;
import eu.europa.ec.leos.domain.common.TocMode;
import eu.europa.ec.leos.domain.vo.CloneProposalMetadataVO;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.domain.vo.SearchMatchVO;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.action.ActionType;
import eu.europa.ec.leos.model.action.CheckinCommentVO;
import eu.europa.ec.leos.model.action.CheckinElement;
import eu.europa.ec.leos.model.action.ContributionVO;
import eu.europa.ec.leos.model.action.VersionVO;
import eu.europa.ec.leos.model.event.DocumentUpdatedByCoEditorEvent;
import eu.europa.ec.leos.model.event.ExportPackageCreatedEvent;
import eu.europa.ec.leos.model.event.UpdateUserInfoEvent;
import eu.europa.ec.leos.model.messaging.UpdateInternalReferencesMessage;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.model.xml.Element;
import eu.europa.ec.leos.security.LeosPermission;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.Annotate.AnnotateService;
import eu.europa.ec.leos.services.clone.CloneContext;
import eu.europa.ec.leos.services.document.BillService;
import eu.europa.ec.leos.services.document.ContributionService;
import eu.europa.ec.leos.services.document.DocumentContentService;
import eu.europa.ec.leos.services.document.ProposalService;
import eu.europa.ec.leos.services.document.TransformationService;
import eu.europa.ec.leos.services.document.util.CheckinCommentUtil;
import eu.europa.ec.leos.services.export.ExportDW;
import eu.europa.ec.leos.services.export.ExportLW;
import eu.europa.ec.leos.services.export.ExportOptions;
import eu.europa.ec.leos.services.export.ExportService;
import eu.europa.ec.leos.services.export.ExportVersions;
import eu.europa.ec.leos.services.export.FileHelper;
import eu.europa.ec.leos.services.importoj.ImportService;
import eu.europa.ec.leos.services.label.ReferenceLabelService;
import eu.europa.ec.leos.services.messaging.UpdateInternalReferencesProducer;
import eu.europa.ec.leos.services.notification.NotificationService;
import eu.europa.ec.leos.services.numbering.NumberService;
import eu.europa.ec.leos.services.processor.AttachmentProcessor;
import eu.europa.ec.leos.services.processor.BillProcessor;
import eu.europa.ec.leos.services.processor.ElementProcessor;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.search.SearchService;
import eu.europa.ec.leos.services.store.ExportPackageService;
import eu.europa.ec.leos.services.store.LegService;
import eu.europa.ec.leos.services.store.PackageService;
import eu.europa.ec.leos.services.store.WorkspaceService;
import eu.europa.ec.leos.services.template.TemplateConfigurationService;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.ui.component.ComparisonComponent;
import eu.europa.ec.leos.ui.event.ChangeBaseVersionEvent;
import eu.europa.ec.leos.ui.event.CloseBrowserRequestEvent;
import eu.europa.ec.leos.ui.event.CloseScreenRequestEvent;
import eu.europa.ec.leos.ui.event.CreateExportPackageActualVersionRequestEvent;
import eu.europa.ec.leos.ui.event.CreateExportPackageCleanVersionRequestEvent;
import eu.europa.ec.leos.ui.event.CreateExportPackageRequestEvent;
import eu.europa.ec.leos.ui.event.DownloadActualVersionRequestEvent;
import eu.europa.ec.leos.ui.event.DownloadCleanVersion;
import eu.europa.ec.leos.ui.event.DownloadXmlVersionRequestEvent;
import eu.europa.ec.leos.ui.event.FetchMilestoneByVersionedReferenceEvent;
import eu.europa.ec.leos.ui.event.InitLeosEditorEvent;
import eu.europa.ec.leos.ui.event.MergeElementRequestEvent;
import eu.europa.ec.leos.ui.event.contribution.ApplyContributionsRequestEvent;
import eu.europa.ec.leos.ui.event.contribution.CompareAndShowRevisionEvent;
import eu.europa.ec.leos.ui.event.doubleCompare.DocuWriteExportRequestEvent;
import eu.europa.ec.leos.ui.event.doubleCompare.DoubleCompareRequestEvent;
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
import eu.europa.ec.leos.ui.event.toc.InlineTocCloseRequestEvent;
import eu.europa.ec.leos.ui.event.toc.InlineTocEditRequestEvent;
import eu.europa.ec.leos.ui.event.toc.RefreshTocEvent;
import eu.europa.ec.leos.ui.event.toc.SaveTocRequestEvent;
import eu.europa.ec.leos.ui.event.view.DownloadXmlFilesRequestEvent;
import eu.europa.ec.leos.ui.event.view.ToolBoxExportRequestEvent;
import eu.europa.ec.leos.ui.model.AnnotateMetadata;
import eu.europa.ec.leos.ui.model.AnnotationStatus;
import eu.europa.ec.leos.services.clone.InternalRefMap;
import eu.europa.ec.leos.ui.support.CoEditionHelper;
import eu.europa.ec.leos.ui.support.ConfirmDialogHelper;
import eu.europa.ec.leos.ui.support.DownloadExportRequest;
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
import eu.europa.ec.leos.web.event.view.document.CheckElementCoEditionEvent;
import eu.europa.ec.leos.web.event.view.document.CheckElementCoEditionEvent.Action;
import eu.europa.ec.leos.web.event.view.document.CloseDocumentEvent;
import eu.europa.ec.leos.web.event.view.document.CloseDocumentConfirmationEvent;
import eu.europa.ec.leos.web.event.view.document.CloseElementEvent;
import eu.europa.ec.leos.web.event.view.document.ComparisonEvent;
import eu.europa.ec.leos.web.event.view.document.ConfirmRenumberingEvent;
import eu.europa.ec.leos.web.event.view.document.DeleteElementRequestEvent;
import eu.europa.ec.leos.web.event.view.document.DocumentNavigationRequest;
import eu.europa.ec.leos.web.event.view.document.DocumentUpdatedEvent;
import eu.europa.ec.leos.web.event.view.document.EditElementRequestEvent;
import eu.europa.ec.leos.web.event.view.document.FetchCrossRefTocRequestEvent;
import eu.europa.ec.leos.web.event.view.document.FetchElementRequestEvent;
import eu.europa.ec.leos.web.event.view.document.FetchUserGuidanceRequest;
import eu.europa.ec.leos.web.event.view.document.FetchUserPermissionsRequest;
import eu.europa.ec.leos.web.event.view.document.ImportElementRequestEvent;
import eu.europa.ec.leos.web.event.view.document.InsertElementRequestEvent;
import eu.europa.ec.leos.web.event.view.document.InstanceTypeResolver;
import eu.europa.ec.leos.web.event.view.document.MergeSuggestionRequest;
import eu.europa.ec.leos.web.event.view.document.MergeSuggestionsRequest;
import eu.europa.ec.leos.web.event.view.document.ReferenceLabelRequestEvent;
import eu.europa.ec.leos.web.event.view.document.RefreshContributionEvent;
import eu.europa.ec.leos.web.event.view.document.RefreshDocumentEvent;
import eu.europa.ec.leos.web.event.view.document.RenumberingEvent;
import eu.europa.ec.leos.web.event.view.document.RequestFilteredAnnotations;
import eu.europa.ec.leos.web.event.view.document.ResponseFilteredAnnotations;
import eu.europa.ec.leos.web.event.view.document.SaveElementRequestEvent;
import eu.europa.ec.leos.web.event.view.document.SaveIntermediateVersionEvent;
import eu.europa.ec.leos.web.event.view.document.SearchActRequestEvent;
import eu.europa.ec.leos.web.event.view.document.ShowCleanVersionRequestEvent;
import eu.europa.ec.leos.web.event.view.document.ShowImportWindowEvent;
import eu.europa.ec.leos.web.event.view.document.ShowIntermediateVersionWindowEvent;
import eu.europa.ec.leos.web.event.view.document.TocItemListRequestEvent;
import eu.europa.ec.leos.web.event.view.document.TocItemListResponseEvent;
import eu.europa.ec.leos.web.event.window.CancelElementEditorEvent;
import eu.europa.ec.leos.web.event.window.CloseElementEditorEvent;
import eu.europa.ec.leos.web.event.window.ShowTimeLineWindowEvent;
import eu.europa.ec.leos.web.model.SearchCriteriaVO;
import eu.europa.ec.leos.web.model.VersionInfoVO;
import eu.europa.ec.leos.web.support.SessionAttribute;
import eu.europa.ec.leos.web.support.UrlBuilder;
import eu.europa.ec.leos.web.support.UuidHelper;
import eu.europa.ec.leos.web.support.log.LogUtil;
import eu.europa.ec.leos.web.support.user.UserHelper;
import eu.europa.ec.leos.web.support.xml.DownloadStreamResource;
import eu.europa.ec.leos.web.ui.navigation.Target;
import eu.europa.ec.leos.web.ui.screen.document.ColumnPosition;
import io.atlassian.fugue.Option;
import io.atlassian.fugue.Pair;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static eu.europa.ec.leos.services.support.XmlHelper.INDENT;
import static eu.europa.ec.leos.services.support.XmlHelper.PARAGRAPH;
import static eu.europa.ec.leos.services.support.XmlHelper.POINT;
import static eu.europa.ec.leos.services.support.XmlHelper.SUBPARAGRAPH;
import static eu.europa.ec.leos.services.support.XmlHelper.SUBPOINT;
import static eu.europa.ec.leos.services.support.XmlHelper.UTF_8;
import static eu.europa.ec.leos.util.LeosDomainUtil.CMIS_PROPERTY_SPLITTER;

@Component
@Scope("prototype")
class DocumentPresenter extends AbstractLeosPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentPresenter.class);

    private final DocumentScreen documentScreen;
    private final BillService billService;
    private final BillProcessor billProcessor;
    private final ElementProcessor<Bill> elementProcessor;
    private final XmlContentProcessor xmlContentProcessor;
    private final ReferenceLabelService referenceLabelService;
    private final TransformationService transformationService;
    private final UrlBuilder urlBuilder;
    private final ComparisonDelegate<Bill> comparisonDelegate;
    private final DocumentContentService documentContentService;
    private final ExportService exportService;
    private final NumberService numberService;
    private final CommonDelegate<Bill> commonDelegate;
    private final TemplateConfigurationService templateConfigurationService;
    private final ImportService importService;
    private final UserHelper userHelper;
    private final MessageHelper messageHelper;
    private final Provider<CollectionContext> proposalContextProvider;
    private final Provider<BillContext> billContextProvider;
    private final CoEditionHelper coEditionHelper;
    private final Provider<StructureContext> structureContextProvider;
    private final LegService legService;
    private final UpdateInternalReferencesProducer updateInternalReferencesProducer;
    private final ProposalService proposalService;
    private final ContributionService contributionService;
    private final SearchService searchService;
    private final ExportPackageService exportPackageService;
    private final NotificationService notificationService;
    private final CloneContext cloneContext;
    private DownloadExportRequest downloadExportRequest;
    private final MergeContributionHelper mergeContributionHelper;
    private CloneProposalMetadataVO cloneProposalMetadataVO;
    protected final AttachmentProcessor attachmentProcessor;

    private String strDocumentVersionSeriesId;
    private String documentId;
    private String documentRef;
    private Element elementToEditAfterClose;
    private boolean comparisonMode;
    private String proposalRef;
    private String connectedEntity;

    private final static SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private static final String LEOS_ALTERNATIVE_ATTR = "leos:alternative";
    private boolean milestoneExplorerOpened = false;
    private InstanceTypeResolver instanceTypeResolver;
    private final AnnotateService annotateService;
    private final List<String> openElementEditors;

    @Autowired
    DocumentPresenter(SecurityContext securityContext, HttpSession httpSession, EventBus eventBus,
                      DocumentScreen documentScreen, BillService billService,
                      CommonDelegate<Bill> commonDelegate,
                      ComparisonDelegate<Bill> comparisonDelegate,
                      DocumentContentService documentContentService, PackageService packageService, ExportService exportService,
                      BillProcessor billProcessor, ReferenceLabelService referenceLabelService,
                      ElementProcessor<Bill> elementProcessor, TransformationService transformationService,
                      UrlBuilder urlBuilder, TemplateConfigurationService templateConfigurationService,
                      ImportService importService, UserHelper userHelper, MessageHelper messageHelper,
                      Provider<CollectionContext> proposalContextProvider, Provider<BillContext> billContextProvider, CoEditionHelper coEditionHelper,
                      EventBus leosApplicationEventBus, UuidHelper uuidHelper, Provider<StructureContext> structureContextProvider,
                      WorkspaceService workspaceService, LegService legService, UpdateInternalReferencesProducer updateInternalReferencesProducer,
                      ProposalService proposalService, ContributionService contributionService, SearchService searchService, ExportPackageService exportPackageService,
                      NotificationService notificationService, CloneContext cloneContext, InstanceTypeResolver instanceTypeResolver, XmlContentProcessor xmlContentProcessor,
                      NumberService numberService, MergeContributionHelper mergeContributionHelper, AttachmentProcessor attachmentProcessor,
                      AnnotateService annotateService) {

        super(securityContext, httpSession, eventBus, leosApplicationEventBus, uuidHelper, packageService, workspaceService);
        this.contributionService = contributionService;
        this.attachmentProcessor = attachmentProcessor;
        LOG.trace("Initializing document presenter...");
        this.documentScreen = documentScreen;
        this.billService = billService;
        this.comparisonDelegate = comparisonDelegate;
        this.documentContentService = documentContentService;
        this.exportService = exportService;
        this.commonDelegate = commonDelegate;
        this.billProcessor = billProcessor;
        this.elementProcessor = elementProcessor;
        this.referenceLabelService = referenceLabelService;
        this.transformationService = transformationService;
        this.urlBuilder = urlBuilder;
        this.templateConfigurationService = templateConfigurationService;
        this.importService = importService;
        this.userHelper = userHelper;
        this.messageHelper = messageHelper;
        this.proposalContextProvider = proposalContextProvider;
        this.billContextProvider = billContextProvider;
        this.coEditionHelper = coEditionHelper;
        this.structureContextProvider = structureContextProvider;
        this.legService = legService;
        this.updateInternalReferencesProducer = updateInternalReferencesProducer;
        this.proposalService = proposalService;
        this.searchService = searchService;
        this.exportPackageService = exportPackageService;
        this.notificationService = notificationService;
        this.cloneContext = cloneContext;
        this.instanceTypeResolver = instanceTypeResolver;
        this.xmlContentProcessor = xmlContentProcessor;
        this.numberService = numberService;
        this.mergeContributionHelper = mergeContributionHelper;
        this.annotateService = annotateService;
        this.openElementEditors = new ArrayList<>();
    }

    private byte[] getContent(Bill bill) {
        final Content content = bill.getContent().getOrError(() -> "Document content is required!");
        return content.getSource().getBytes();
    }

    @Override
    public void enter() {
        super.enter();
        init();
    }

    private void init() {
        try {
            populateWithProposalRefAndConnectedEntity();
            Bill bill = getDocument();
            populateViewWithDocumentDetails(bill, TocMode.SIMPLIFIED);
            populateVersionAndContributionData(bill);
            String revisionReference = getRevisionRef();
            if (revisionReference != null) {
                Optional<ContributionVO> contributionVO = documentScreen.findContributionAndShowTab(revisionReference);
                if (contributionVO.isPresent()) {
                    eventBus.post(new OpenRevisionDocumentEvent(contributionVO.get()));
                }
            }
        } catch (Exception exception) {
            LOG.error("Exception occurred in init(): ", exception);
            eventBus.post(new NotificationEvent(Type.ERROR, "unknown.error.message"));
        }
    }

    private void populateWithProposalRefAndConnectedEntity() {
        Proposal proposal = getProposalFromPackage();
        if (proposal != null) {
            proposalRef = proposal.getMetadata().get().getRef();
            connectedEntity = userHelper.getCollaboratorConnectedEntityByLoggedUser(proposal.getCollaborators());
            byte[] xmlContent = proposal.getContent().get().getSource().getBytes();
            if (proposal != null && proposal.isClonedProposal()) {
                populateCloneProposalMetadataVO(xmlContent);
            }
        }
    }

    private Proposal getProposalFromPackage() {
        Proposal proposal = null;
        Bill bill = getDocument();
        if (bill != null) {
            LeosPackage leosPackage = packageService.findPackageByDocumentId(bill.getId());
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

    private void populateVersionAndContributionData(Bill bill) {
        DocumentVO billVO = createLegalTextVO(bill);
        final List<VersionVO> allVersions = getVersionVOS();
        final List<ContributionVO> allContributions = contributionService.getDocumentContributions(documentId, 0, Bill.class);
        documentScreen.setDataFunctions(
                billVO,
                allVersions,
                allContributions,
                this::majorVersionsFn, this::countMajorVersionsFn,
                this::minorVersionsFn, this::countMinorVersionsFn,
                this::recentChangesFn, this::countRecentChangesFn);
    }

    private List<VersionVO> getVersionVOS() {
        return billService.getAllVersions(documentId, documentRef);
    }

    private Integer countMinorVersionsFn(String currIntVersion) {
        return billService.findAllMinorsCountForIntermediate(documentRef, currIntVersion);
    }

    private List<Bill> minorVersionsFn(String currIntVersion, int startIndex, int maxResults) {
        return billService.findAllMinorsForIntermediate(documentRef, currIntVersion, startIndex, maxResults);
    }

    private Integer countMajorVersionsFn() {
        return billService.findAllMajorsCount(documentRef);
    }

    private List<Bill> majorVersionsFn(int startIndex, int maxResults) {
        return billService.findAllMajors(documentRef, startIndex, maxResults);
    }

    private Integer countRecentChangesFn() {
        return billService.findRecentMinorVersionsCount(documentId, documentRef);
    }

    private List<Bill> recentChangesFn(int startIndex, int maxResults) {
        return billService.findRecentMinorVersions(documentId, documentRef, startIndex, maxResults);
    }

    @Override
    public void detach() {
        super.detach();
        coEditionHelper.removeUserEditInfo(id, strDocumentVersionSeriesId, null, InfoType.DOCUMENT_INFO);
        resetCloneProposalMetadataVO();
    }

    @Subscribe
    void closeDocument(CloseDocumentEvent event) {
        if (this.isBillUnsaved() || this.isHasOpenElementEditors()) {
            eventBus.post(new ShowConfirmDialogEvent(new CloseDocumentConfirmationEvent(), null));
            return;
        }
        this.closeDocument();
    }

    @Subscribe
    void handleNavigationRequest(DocumentNavigationRequest event) {
        LOG.trace("Handling document navigation request...");
        if (event.getNavigationEvent() != null) event.getNavigationEvent().setForwardToDocument(false);
        if(isBillUnsaved() || this.isHasOpenElementEditors()) {
            eventBus.post(new ShowConfirmDialogEvent(event.getNavigationEvent(), null));
            return;
        }
        eventBus.post(event.getNavigationEvent());
    }

    private boolean isBillUnsaved() {
        return getBillFromSession() != null;
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
    void closeDocument(CloseBrowserRequestEvent event) {
        coEditionHelper.removeUserEditInfo(id, strDocumentVersionSeriesId, null, InfoType.DOCUMENT_INFO);
        resetCloneProposalMetadataVO();
    }

    @Subscribe
    void closeScreen(CloseScreenRequestEvent event) {
        if (documentScreen.isTocEnabled()) {
            eventBus.post(new CloseTocAndDocumentEvent());
        } else {
            eventBus.post(new CloseDocumentEvent());
        }
    }

    @Subscribe
    void refreshDocument(RefreshDocumentEvent event) {
        Bill bill = getDocument();
        populateViewWithDocumentDetails(bill, event.getTocMode());
    }

    @Subscribe
    void refreshToc(RefreshTocEvent event) {
        Bill bill = getDocument();
        if (bill != null) {
            documentScreen.setToc(getListOfTableOfContent(bill, event.getTocMode()));
        }
    }

    @Subscribe
    void checkElementCoEdition(CheckElementCoEditionEvent event) {
        try {
            if (event.getAction().equals(Action.MERGE)) {
                Bill bill = getDocument();
                Element mergeOnElement = billProcessor.getMergeOnElement(bill, event.getElementContent(), event.getElementTagName(), event.getElementId());
                if (mergeOnElement != null) {
                    documentScreen.checkElementCoEdition(coEditionHelper.getCurrentEditInfo(strDocumentVersionSeriesId), user,
                            mergeOnElement.getElementId(), mergeOnElement.getElementTagName(), event.getAction(), event.getActionEvent());
                } else {
                    documentScreen.showAlertDialog("operation.element.not.performed");
                }
            } else {
                Bill bill = getDocument();
                Element tocElement = billProcessor.getTocElement(bill, event.getElementId(), getListOfTableOfContent(bill, TocMode.SIMPLIFIED));
                documentScreen.checkElementCoEdition(coEditionHelper.getCurrentEditInfo(strDocumentVersionSeriesId), user,
                        tocElement.getElementId(), tocElement.getElementTagName(), event.getAction(), event.getActionEvent());
            }
        } catch (Exception e) {
            LOG.error("Unexpected error in checkElementCoEdition", e);
            eventBus.post(new NotificationEvent(Type.ERROR, "unknown.error.message"));
        }
    }


    @Subscribe
    void editElement(EditElementRequestEvent event) {

        //show confirm dialog if there is any unsaved replaced text
        //it can be detected from the session attribute
        if (isBillUnsaved()) {
            eventBus.post(new ShowConfirmDialogEvent(event, new CancelElementEditorEvent(event.getElementId(), event.getElementTagName())));
            return;
        }
        String elementId = event.getElementId();
        String elementTagName = event.getElementTagName();
        elementToEditAfterClose = null;
        Bill bill = getDocument();
        String jsonAlternatives = "";

        String element = elementProcessor.getElement(bill, elementTagName, elementId);
        String alternateAttrVal = elementProcessor.getElementAttributeValueByNameAndId(bill, LEOS_ALTERNATIVE_ATTR, elementTagName, elementId);
        if (alternateAttrVal != null && alternateAttrVal.equalsIgnoreCase("true")) {
            jsonAlternatives = templateConfigurationService.getTemplateConfiguration(bill.getMetadata().get().getDocTemplate(), "alternatives");
        }

        coEditionHelper.storeUserEditInfo(httpSession.getId(), id, user, strDocumentVersionSeriesId, elementId, InfoType.ELEMENT_INFO);
        documentScreen.showElementEditor(event.getElementId(), elementTagName, element, jsonAlternatives);
        this.openElementEditors.add(elementId);
    }

    @Subscribe
    void saveElement(SaveElementRequestEvent event) {
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            final String elementId = event.getElementId();
            final String elementTagName = event.getElementTagName();
            final String xmlFragment = event.getElementContent();
            elementToEditAfterClose = null;

            Bill bill = getDocument();
            byte[] newXmlContent = billProcessor.updateElement(bill, elementTagName, elementId, xmlFragment);
            if (newXmlContent == null) {
                documentScreen.showAlertDialog("operation.element.not.performed");
                return;
            }

            final String title = messageHelper.getMessage("operation.element.updated", StringUtils.capitalize(elementTagName));
            final String description = messageHelper.getMessage("operation.checkin.minor");
            final String elementLabel = generateLabel(elementId, bill);
            final CheckinCommentVO checkinComment = new CheckinCommentVO(title, description, new CheckinElement(ActionType.UPDATED, elementId, elementTagName, elementLabel));
            final String checkinCommentJson = CheckinCommentUtil.getJsonObject(checkinComment);

            if (bill != null) {
                Pair<byte[], Element> splittedContent = null;
                if (event.isSplit() && checkIfCloseElementEditor(elementTagName, event.getElementContent())) {
                    splittedContent = billProcessor.getSplittedElement(newXmlContent, event.getElementContent(), elementTagName, elementId);
                    if (splittedContent != null) {
                        elementToEditAfterClose = splittedContent.right();
                        if (splittedContent.left() != null) {
                            newXmlContent = splittedContent.left();
                        }
                        eventBus.post(new CloseElementEvent());
                    }
                }
                bill = billService.updateBill(bill, newXmlContent, checkinCommentJson);
                if (splittedContent == null) {
                    String elementContent = elementProcessor.getElement(bill, elementTagName, elementId);
                    documentScreen.refreshElementEditor(elementId, elementTagName, elementContent);
                }
                eventBus.post(new DocumentUpdatedEvent());
                documentScreen.scrollToMarkedChange(elementId);
                leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
            }
            LOG.info("Element '{}' in Bill {} id {}, saved in {} milliseconds ({} sec)", elementId, bill.getName(), bill.getId(), stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
        } catch (Exception ex) {
            LOG.error("Exception while saving element operation for ", ex);
            eventBus.post(new NotificationEvent(Type.ERROR, "error.message", ex.getMessage()));
        }
    }

    boolean checkIfCloseElementEditor(String elementTagName, String elementContent) {
        switch (elementTagName) {
            case SUBPARAGRAPH:
            case SUBPOINT:
                return elementContent.contains("<" + elementTagName);
            case PARAGRAPH:
                return elementContent.contains("<paragraph") || elementContent.contains("<subparagraph");
            case POINT:
            case INDENT:
                return elementContent.contains("<alinea");
            default:
                return false;
        }
    }

    @Subscribe
    void closeElementEditor(CloseElementEditorEvent event) {
        String elementId = event.getElementId();
        coEditionHelper.removeUserEditInfo(id, strDocumentVersionSeriesId, elementId, InfoType.ELEMENT_INFO);
        LOG.debug("User edit information removed");
        this.openElementEditors.remove(elementId);
        eventBus.post(new RefreshDocumentEvent());
        if (elementToEditAfterClose != null) {
            documentScreen.scrollTo(elementToEditAfterClose.getElementId());
            eventBus.post(new EditElementRequestEvent(elementToEditAfterClose.getElementId(), elementToEditAfterClose.getElementTagName()));
        }
    }

    @Subscribe
    void cancelElementEditor(CancelElementEditorEvent event) {
        String elementId = event.getElementId();
        coEditionHelper.removeUserEditInfo(id, strDocumentVersionSeriesId, elementId, InfoType.ELEMENT_INFO);

        //load content from session if exists
        Bill billFromSession = getBillFromSession();
        if (billFromSession != null) {
            documentScreen.refreshContent(getEditableXml(billFromSession));
        } else {
            eventBus.post(new RefreshDocumentEvent());
        }
        LOG.debug("User edit information removed");
    }

    private Bill getBillFromSession() {
        return (Bill) httpSession.getAttribute("bill#" + getDocumentRef());
    }

    @Subscribe
    void deleteElement(DeleteElementRequestEvent event) {
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            final String tagName = event.getElementTagName();
            final Bill bill = getDocument();
            final byte[] newXmlContent = billProcessor.deleteElement(bill, event.getElementId(), tagName, user);

            final String updatedLabel = generateLabel(event.getElementId(), bill);
            final String comment = messageHelper.getMessage("operation.element.deleted", updatedLabel);

            updateBillContent(bill, newXmlContent, comment, "document." + tagName + ".deleted");
            updateInternalReferencesProducer.send(new UpdateInternalReferencesMessage(bill.getId(), bill.getMetadata().get().getRef(), id));
            LOG.info("Element '{}' in Bill {} id {}, deleted in {} milliseconds ({} sec)", event.getElementId(), bill.getName(), bill.getId(), stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
        } catch (Exception ex) {
            LOG.error("Exception while deleting element operation for ", ex);
            eventBus.post(new NotificationEvent(Type.ERROR, "error.message", ex.getMessage()));
        }
    }

    public String generateLabel(String reference, XmlDocument sourceDocument) {
        final byte[] sourceXmlContent = sourceDocument.getContent().get().getSource().getBytes();
        Result<String> updatedLabel = referenceLabelService.generateLabelStringRef(Arrays.asList(reference), sourceDocument.getMetadata().get().getRef(), sourceXmlContent);
        return updatedLabel.get();
    }

    @Subscribe
    void insertElement(InsertElementRequestEvent event) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        final String tagName = event.getElementTagName();
        final Bill bill = getDocument();

        final boolean before = InsertElementRequestEvent.POSITION.BEFORE.equals(event.getPosition());
        final byte[] newXmlContent = billProcessor.insertNewElement(bill, event.getElementId(), before, tagName);

        final String title = messageHelper.getMessage("operation.element.inserted", StringUtils.capitalize(tagName));
        final String description = messageHelper.getMessage("operation.checkin.minor");
        final String elementLabel = "";
        final CheckinCommentVO checkinComment = new CheckinCommentVO(title, description, new CheckinElement(ActionType.INSERTED, event.getElementId(), tagName, elementLabel));
        final String checkinCommentJson = CheckinCommentUtil.getJsonObject(checkinComment);

        updateBillContent(bill, newXmlContent, checkinCommentJson, "document." + tagName + ".inserted");
        updateInternalReferencesProducer.send(new UpdateInternalReferencesMessage(bill.getId(), bill.getMetadata().get().getRef(), id));
        LOG.info("New Element of type '{}' inserted in Bill {} id {}, in {} milliseconds ({} sec)", tagName, bill.getName(), bill.getId(), stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
    }

    @Subscribe
    void confirmRenumberDocument(ConfirmRenumberingEvent event) {
        documentScreen.confirmRenumberDocument();
    }

    @Subscribe
    void renumberDocument(RenumberingEvent event) {

        Stopwatch stopwatch = Stopwatch.createStarted();
        final Bill bill = getDocument();

        final byte[] newXmlContent = billProcessor.renumberDocument(bill);

        final String title = messageHelper.getMessage("operation.element.document_renumbered");
        final String description = messageHelper.getMessage("operation.checkin.minor");
        final CheckinCommentVO checkinComment = new CheckinCommentVO(title, description, new CheckinElement(ActionType.DOCUMENT_RENUMBERED));
        final String checkinCommentJson = CheckinCommentUtil.getJsonObject(checkinComment);

        updateBillContent(bill, newXmlContent, checkinCommentJson, "document.renumbered");
        updateInternalReferencesProducer.send(new UpdateInternalReferencesMessage(bill.getId(), bill.getMetadata().get().getRef(), id));
        LOG.info("Renumbering document executed, in {} milliseconds ({} sec)", stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));

    }

    @Subscribe
    void mergeElement(MergeElementRequestEvent event) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            String elementId = event.getElementId();
            String tagName = event.getElementTagName();
            String elementContent = event.getElementContent();

            Bill bill = getDocument();
            Element mergeOnElement = billProcessor.getMergeOnElement(bill, elementContent, tagName, elementId);
            if (mergeOnElement != null) {
                byte[] newXmlContent = billProcessor.mergeElement(bill, elementContent, tagName, elementId);
                bill = billService.updateBill(bill, newXmlContent, messageHelper.getMessage("operation.element.updated", StringUtils.capitalize(tagName)));
                if (bill != null) {
                    elementToEditAfterClose = mergeOnElement;
                    eventBus.post(new CloseElementEvent());
                    eventBus.post(new DocumentUpdatedEvent());
                    leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
                }
            } else {
                documentScreen.showAlertDialog("operation.element.not.performed");
            }
            LOG.info("Element '{}' merged into '{}' in Bill {} id {}, in {} milliseconds ({} sec)", elementId, mergeOnElement.getElementId(), bill.getName(), bill.getId(), stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
        } catch (Exception e) {
            LOG.error("Unexpected error in mergeElement", e);
            eventBus.post(new NotificationEvent(Type.ERROR, "unknown.error.message"));
        }
    }

    @Subscribe
    void editInlineToc(InlineTocEditRequestEvent event) {
        Bill bill = getDocument();
        coEditionHelper.storeUserEditInfo(httpSession.getId(), id, user, strDocumentVersionSeriesId, null, InfoType.TOC_INFO);
        documentScreen.enableTocEdition(getListOfTableOfContent(bill, TocMode.NOT_SIMPLIFIED));
    }

    @Subscribe
    void closeInlineToc(InlineTocCloseRequestEvent event) {
        coEditionHelper.removeUserEditInfo(id, strDocumentVersionSeriesId, null, InfoType.TOC_INFO);
        LOG.debug("User edit information removed");
    }

    @Subscribe
    void saveToc(SaveTocRequestEvent event) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Bill bill = getDocument();

        final String title = messageHelper.getMessage("operation.toc.updated");
        final String description = messageHelper.getMessage("operation.checkin.minor");
        Set<CheckinElement> updatedElements = event.getSaveElements();
        updatedElements.forEach(item -> {
            final String label = generateLabel(item.getElementId(), bill);
            item.setElementLabel(label);
        });


        final CheckinCommentVO checkinComment = new CheckinCommentVO(title, description, new CheckinElement(ActionType.STRUCTURAL, updatedElements));
        final String checkinCommentJson = CheckinCommentUtil.getJsonObject(checkinComment);

        Bill updatedBill = billService.saveTableOfContent(bill, event.getTableOfContentItemVOs(), checkinCommentJson, user);

        eventBus.post(new NotificationEvent(Type.INFO, "toc.edit.saved"));
        eventBus.post(new DocumentUpdatedEvent());
        leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
        updateInternalReferencesProducer.send(new UpdateInternalReferencesMessage(bill.getId(), bill.getMetadata().get().getRef(), id));
        LOG.info("Toc saved in Bill {} id {}, in {} milliseconds ({} sec)", bill.getName(), bill.getId(), stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
    }

    @Subscribe
    void fetchTocAndAncestors(FetchCrossRefTocRequestEvent event) {
        Bill bill = getDocument();
        List<String> elementAncestorsIds = null;
        if (event.getElementIds() != null && event.getElementIds().size() > 0) {
            try {
                elementAncestorsIds = billService.getAncestorsIdsForElementId(bill, event.getElementIds());
            } catch (Exception e) {
                LOG.warn("Could not get ancestors Ids", e);
            }
        }
        // we are combining two operations (get toc + get selected element ancestors)
        documentScreen.setTocAndAncestors(packageService.getTableOfContent(bill.getId(), TocMode.SIMPLIFIED_CLEAN), elementAncestorsIds);
    }

    @Subscribe
    void fetchElement(FetchElementRequestEvent event) {
        XmlDocument document = workspaceService.findDocumentByRef(event.getDocumentRef(), XmlDocument.class);
        String contentForType = elementProcessor.getElement(document, event.getElementTagName(), event.getElementId());
        String wrappedContentXml = wrapXmlFragment(contentForType != null ? contentForType : "");
        InputStream contentStream = new ByteArrayInputStream(wrappedContentXml.getBytes(StandardCharsets.UTF_8));
        contentForType = transformationService.toXmlFragmentWrapper(contentStream, urlBuilder.getWebAppPath(VaadinServletService.getCurrentServletRequest()),
                securityContext.getPermissions(document));

        documentScreen.setElement(event.getElementId(), event.getElementTagName(), contentForType, event.getDocumentRef());
    }

    @Subscribe
    void fetchReferenceLabel(ReferenceLabelRequestEvent event) {
        // Validate
        if (event.getReferences().size() < 1) {
            eventBus.post(new NotificationEvent(Type.ERROR, "unknown.error.message"));
            LOG.error("No reference found in the request from client");
            return;
        }
        final byte[] sourceXmlContent = getDocument().getContent().get().getSource().getBytes();
        Result<String> updatedLabel = referenceLabelService.generateLabelStringRef(event.getReferences(), getDocumentRef(), event.getCurrentElementID(), sourceXmlContent, event.getDocumentRef(), event.isCapital());
        documentScreen.setReferenceLabel(updatedLabel.get(), event.getDocumentRef());
    }

    @Subscribe
    void mergeSuggestion(MergeSuggestionRequest event) {
        Bill bill = getDocument();
        commonDelegate.mergeSuggestion(bill, event, elementProcessor, billService::updateBill);
    }

    @Subscribe
    void mergeBulkSuggestions(MergeSuggestionsRequest event) {
        Bill bill = getDocument();
        commonDelegate.mergeSuggestions(bill, event, elementProcessor, billService::updateBill);
    }

    @Subscribe
    void changeBaseVersion(ChangeBaseVersionEvent event) {
        String documentId = event.getVersionId();
        String versionLabel = event.getVersionLabel();
        String versionComment = event.getBaseVersionTitle();
        Map<String, Object> properties = new HashMap<>();
        properties.put(CmisProperties.BASE_REVISION_ID.getId(), documentId + CMIS_PROPERTY_SPLITTER + versionLabel +
                CMIS_PROPERTY_SPLITTER + versionComment);
        Bill updatedBill = billService.updateBill(documentId, properties, true);
        documentScreen.refreshContent(getEditableXml(updatedBill));
        eventBus.post(new NotificationEvent(NotificationEvent.Type.INFO, "document.base.version.changed.info",
                versionLabel));
    }

    @Subscribe
    public void fetchMetadata(DocumentMetadataRequest event) {
        AnnotateMetadata metadata = new AnnotateMetadata();
        Bill bill = getDocument();
        metadata.setVersion(bill.getVersionLabel());
        metadata.setId(bill.getId());
        metadata.setTitle(bill.getTitle());
        eventBus.post(new DocumentMetadataResponse(metadata));
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

    private String getEditableXml(Bill document) {
        VersionInfoVO versionInfoVO = getVersionInfo(document);
        String baseRevisionId = document.getBaseRevisionId();
        cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);

        if (!StringUtils.isEmpty(baseRevisionId) && baseRevisionId.split(CMIS_PROPERTY_SPLITTER).length >= 3) {
            String versionLabel = baseRevisionId.split(CMIS_PROPERTY_SPLITTER)[1];
            String versionComment = baseRevisionId.split(CMIS_PROPERTY_SPLITTER)[2];
            versionInfoVO.setRevisedBaseVersion(versionLabel);
            versionInfoVO.setBaseVersionTitle(versionComment);
        }
        documentScreen.setDocumentVersionInfo(versionInfoVO);
        byte[] coverPageContent = new byte[0];
        byte[] documentContent = document.getContent().get().getSource().getBytes();
        boolean isCoverPageExists = documentContentService.isCoverPageExists(documentContent);
        if (documentScreen.isCoverPageVisible() && !isCoverPageExists) {
            Proposal proposal = getProposalFromPackage();
            byte[] xmlContent = proposal.getContent().get().getSource().getBytes();
            coverPageContent = documentContentService.getCoverPageContent(xmlContent);
        }

        String editableXml = documentContentService.toEditableContent(document,
                urlBuilder.getWebAppPath(VaadinServletService.getCurrentServletRequest()), securityContext, coverPageContent);
        return StringEscapeUtils.unescapeXml(editableXml);
    }

    private String getImportXml(String content) {
        return transformationService.toImportXml(
                new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)),
                urlBuilder.getWebAppPath(VaadinServletService.getCurrentServletRequest()), securityContext.getPermissions(content));
    }

    private Bill getDocument() {
        documentRef = getDocumentRef();
        Bill bill;
        if (documentRef != null) {
            bill = billService.findBillByRef(documentRef);
            if (bill != null) {
                setDocumentData(bill);
            } else {
                throw new IllegalStateException("Bill document retrieved is null, for documentRef: " + documentRef);
            }
        } else {
            throw new IllegalStateException("Cannot retrieve Bill document, documentRef is null");
        }
        return bill;
    }

    private void setDocumentData(Bill bill) {
        strDocumentVersionSeriesId = bill.getVersionSeriesId();
        documentId = bill.getId();
        structureContextProvider.get().useDocumentTemplate(bill.getMetadata().getOrError(() -> "Bill metadata is required!").getDocTemplate());
        cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);
    }

    private String getDocumentRef() {
        return (String) httpSession.getAttribute(id + "." + SessionAttribute.BILL_REF.name());
    }

    private String getRevisionRef() {
        return (String) httpSession.getAttribute(id + "." + SessionAttribute.REVISION_VERSION.name());
    }

    private List<TableOfContentItemVO> getListOfTableOfContent(Bill bill, TocMode mode) {
        return billService.getTableOfContent(bill, mode);
    }

    private Bill populateViewWithDocumentDetails(Bill bill, TocMode mode) {
        Validate.notNull(bill, "Bill document should not be null");
        documentScreen.setDocumentTitle(bill.getTitle());
        documentScreen.refreshContent(getEditableXml(bill));
        documentScreen.setToc(getListOfTableOfContent(bill, mode));
        DocumentVO billVO = createLegalTextVO(bill);
        documentScreen.updateUserCoEditionInfo(coEditionHelper.getCurrentEditInfo(bill.getVersionSeriesId()), id);
        documentScreen.setPermissions(billVO, isClonedProposal());
        documentScreen.initAnnotations(billVO, proposalRef, connectedEntity);
        return bill;
    }

    @Subscribe
    public void initLeosEditor(InitLeosEditorEvent event) {
        List<LeosMetadata> documentsMetadata = packageService.getDocumentsMetadata(event.getDocument().getId());
        documentScreen.initLeosEditor(event.getDocument(), documentsMetadata);
    }

    @Subscribe
    void showTimeLineWindow(ShowTimeLineWindowEvent event) {
        List<Bill> documentVersions = billService.findVersions(documentId);
        documentScreen.showTimeLineWindow(documentVersions);
    }

    @Subscribe
    void showIntermediateVersionWindow(ShowIntermediateVersionWindowEvent event) {
        documentScreen.showIntermediateVersionWindow();
    }

    @Subscribe
    void showImportWindow(ShowImportWindowEvent event) {
        documentScreen.showImportWindow();
    }

    @Subscribe
    void saveIntermediateVersion(SaveIntermediateVersionEvent event) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        final String checkinCommentJson = event.getCheckinComment();
        final Bill bill = billService.createVersion(documentId, event.getVersionType(), checkinCommentJson);
        setDocumentData(bill);
        eventBus.post(new NotificationEvent(NotificationEvent.Type.INFO, "document.major.version.saved"));
        eventBus.post(new DocumentUpdatedEvent());
        leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, bill.getVersionSeriesId(), id));
        populateViewWithDocumentDetails(bill, TocMode.SIMPLIFIED);
        LOG.info("Created intermediate version for Bill {}, in {} milliseconds ({} sec)", bill.getId(), stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
    }

    @Subscribe
    void importElements(ImportElementRequestEvent event) {
        try {
            SearchCriteriaVO searchCriteria = event.getSearchCriteria();
            List<String> elementIds = event.getElementIds();
            String aknDocument = importService.getAknDocument(searchCriteria.getType().getValue(), Integer.parseInt(searchCriteria.getYear()),
                    Integer.parseInt(searchCriteria.getNumber()));
            if (aknDocument != null) {
                Bill bill = getDocument();
                BillMetadata metadata = bill.getMetadata().getOrError(() -> "Bill metadata is required");
                byte[] newXmlContent = importService.insertSelectedElements(bill, aknDocument.getBytes(StandardCharsets.UTF_8), elementIds,
                        metadata.getLanguage());
                String notificationMsg = "document.import.element.inserted" + (elementIds.stream().anyMatch((s) -> s.startsWith("rec_")) ? ".recitals" : "") +
                        (elementIds.stream().anyMatch((s) -> s.startsWith("art_")) ? ".articles" : "");
                updateBillContent(bill, newXmlContent, messageHelper.getMessage("operation.import.element.inserted"), notificationMsg);
                documentScreen.closeImportWindow();
            } else {
                eventBus.post(new NotificationEvent(Type.INFO, "document.import.no.result"));
            }
        } catch (Exception e) {
            LOG.error("Unable to perform the importElements operation", e);
            eventBus.post(new NotificationEvent(Type.INFO, "document.import.failed"));
        }
    }

    private void updateBillContent(Bill bill, byte[] xmlContent, String operationMsg, String notificationMsg) {
        bill = billService.updateBill(bill, xmlContent, operationMsg);
        if (bill != null) {
            eventBus.post(new NotificationEvent(Type.INFO, notificationMsg));
            eventBus.post(new RefreshDocumentEvent());
            eventBus.post(new DocumentUpdatedEvent());
            leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
        }
    }

    @Subscribe
    void searchAct(SearchActRequestEvent event) {
        try {
            SearchCriteriaVO searchCriteria = event.getSearchCriteria();
            String aknDocument = importService.getAknDocument(searchCriteria.getType().getValue(), Integer.parseInt(searchCriteria.getYear()),
                    Integer.parseInt(searchCriteria.getNumber()));
            if (aknDocument != null) {
                String transformedAknDocument = getImportXml(aknDocument);
                documentScreen.displaySearchedContent(transformedAknDocument);
            } else {
                documentScreen.displaySearchedContent(null);
                eventBus.post(new NotificationEvent(Type.INFO, "document.import.no.result"));
            }
        } catch (Exception e) {
            LOG.error("Unable to perform searchAct operation", e);
            documentScreen.displaySearchedContent(null);
            eventBus.post(new NotificationEvent(Type.INFO, "document.import.failed"));
        }
    }

    @Subscribe
    void replaceAllTextInDocument(ReplaceAllMatchRequestEvent event) {
        Bill billFromSession = getBillFromSession();
        if (billFromSession == null) {
            billFromSession = getDocument();
        }

        byte[] updatedContent = searchService.replaceText(getContent(billFromSession),
                event.getSearchText(), event.getReplaceText(), event.getSearchMatchVOs());

        Bill billUpdated = copyIntoNew(billFromSession, updatedContent);
        httpSession.setAttribute("bill#" + getDocumentRef(), billUpdated);
        documentScreen.refreshContent(getEditableXml(billUpdated));
        eventBus.post(new ReplaceAllMatchResponseEvent(true));
    }

    @Subscribe
    void saveAndCloseAfterReplace(SaveAndCloseAfterReplaceEvent event) {
        // save document into repository
        Bill bill = getDocument();

        Bill billFromSession = getBillFromSession();

        bill = billService.updateBill(bill, billFromSession.getContent().get().getSource().getBytes(),
                messageHelper.getMessage("operation.search.replace.updated"));
        if (bill != null) {
            eventBus.post(new RefreshDocumentEvent());
            eventBus.post(new DocumentUpdatedEvent());
            leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
            eventBus.post(new NotificationEvent(Type.INFO, "document.replace.success"));
        }
        httpSession.removeAttribute("bill#" + getDocumentRef());
    }

    @Subscribe
    void saveAfterReplace(SaveAfterReplaceEvent event) {
        // save document into repository
        Bill bill = getDocument();

        Bill billFromSession = getBillFromSession();

        bill = billService.updateBill(bill, billFromSession.getContent().get().getSource().getBytes(),
                messageHelper.getMessage("operation.search.replace.updated"));
        if (bill != null) {
            eventBus.post(new DocumentUpdatedEvent());
            leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
            eventBus.post(new NotificationEvent(Type.INFO, "document.replace.success"));
        }
        httpSession.removeAttribute("bill#" + getDocumentRef());
    }

    private Bill copyIntoNew(Bill source, byte[] updatedContent) {
        //make new bill with new content
        Content contentFromSession = source.getContent().get();
        Source updatedSource = new SourceImpl(new ByteArrayInputStream(updatedContent));
        Content contentObj = new ContentImpl(
                contentFromSession.getFileName(),
                contentFromSession.getMimeType(),
                updatedContent.length,
                updatedSource
        );
        Option<Content> updatedContentOptionObj = Option.option(contentObj);
        Bill billUpdated = new Bill(
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
                null,
                source.getContributionStatus(),
                source.getClonedFrom(),
                updatedContentOptionObj,
                source.getMetadata()
        );

        return billUpdated;
    }

    @Subscribe
    void replaceOneTextInDocument(ReplaceMatchRequestEvent event) {
        if (event.getSearchMatchVO().isReplaceable()) {
            Bill billFromSession = getBillFromSession();
            if (billFromSession == null) {
                billFromSession = getDocument();
            }

            byte[] updatedContent = searchService.replaceText(getContent(billFromSession),
                    event.getSearchText(), event.getReplaceText(), Arrays.asList(event.getSearchMatchVO()));

            Bill billUpdated = copyIntoNew(billFromSession, updatedContent);
            httpSession.setAttribute("bill#" + getDocumentRef(), billUpdated);
            documentScreen.refreshContent(getEditableXml(billUpdated));
            documentScreen.refineSearch(event.getSearchId(), event.getMatchIndex(), true);
        } else {
            documentScreen.refineSearch(event.getSearchId(), event.getMatchIndex(), false);
        }
    }

    @Subscribe
    void searchTextInDocument(SearchTextRequestEvent event) {
        Bill bill = getBillFromSession();
        if (bill == null) {
            bill = getDocument();
        }
        List<SearchMatchVO> matches = Collections.emptyList();
        try {
            matches = searchService.searchText(getContent(bill), event.getSearchText(), event.matchCase, event.completeWords);
        } catch (Exception e) {
            eventBus.post(new NotificationEvent(Type.ERROR, "Error while searching{1}", e.getMessage()));
        }

        //Do we reset session etc if there was partial replace earlier.
        documentScreen.showMatchResults(event.searchID, matches);
    }

    @Subscribe
    void closeSearchBar(SearchBarClosedEvent event) {
        //Cleanup the session etc
        documentScreen.closeSearchBar();
        httpSession.removeAttribute("bill#" + getDocumentRef());
        eventBus.post(new RefreshDocumentEvent());
    }

    @Subscribe
    void cleanComparedContent(CleanComparedContentEvent event) {
        documentScreen.cleanComparedContent();
    }

    @Subscribe
    void showVersion(ShowVersionRequestEvent event) {
        final Bill version = billService.findBillVersion(event.getVersionId());
        final String versionContent = documentContentService.getDocumentAsHtml(version, urlBuilder.getWebAppPath(VaadinServletService.getCurrentServletRequest()),
                securityContext.getPermissions(version));

        byte[] updatedContents = billProcessor.removeDeletedElements(versionContent.getBytes(UTF_8));
        final String versionInfo = getVersionInfoAsString(version);
        documentScreen.showVersion(new String(updatedContents, UTF_8), versionInfo);
    }

    @Subscribe
    void showRevision(OpenRevisionDocumentEvent event) {
        compareAndShowRevision(event.getContributionVO());
        Bill bill = getDocument();
        setDocumentData(bill);
    }

    @Subscribe
    void compareAndShowRevision(CompareAndShowRevisionEvent event) {
        cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);
        compareAndShowRevision(event.getContributionVO());
    }

    private void compareAndShowRevision(ContributionVO contributionVO) {
        final Bill revision = billService.findBill(contributionVO.getDocumentId(), false);
        final String revisionContent = documentContentService.getDocumentForContributionAsHtml(
                contributionVO.getXmlContent(), urlBuilder.getWebAppPath(VaadinServletService.getCurrentServletRequest()),
                securityContext.getPermissions(revision));

        cloneContext.setContribution(Boolean.TRUE);
        documentScreen.refreshVersions(getVersionVOS(), false);
        Bill bill = getDocument();
        List<TocItem> tocItemList = getTocITems(bill);

        final String temporaryAnnotationsId = this.storeRevisionAnnotationsTemporary(contributionVO.getDocumentId(), contributionVO.getLegFileName(), contributionVO.getVersionedReference());
        documentScreen.showRevisionWithSidebar(revisionContent, contributionVO, tocItemList, temporaryAnnotationsId);
    }

    private String storeRevisionAnnotationsTemporary(final String documentId, final String legFileName, final String versionedReference) {
        final LeosPackage leosPackage = this.packageService.findPackageByDocumentId(documentId);
        //final LegDocument referenceDocument = this.legService.findLastLegByVersionedReference(leosPackage.getPath(), versionedReference);
        final LegDocument legDocument = this.packageService.findDocumentByPackagePathAndName(leosPackage.getPath(), legFileName, LegDocument.class);
        return this.legService.storeLegDocumentTemporary(legDocument);
    }

    private List<TocItem> getTocITems(Bill bill) {
        structureContextProvider.get().useDocumentTemplate(bill.getMetadata().getOrError(() -> "Bill metadata is required!").getDocTemplate());
        return structureContextProvider.get().getTocItems();
    }

    @Subscribe
    void declineRevision(DeclineRevisionDocumentEvent event) {
        cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);
        final Bill revision = billService.findBill(event.getContributionVO().getDocumentId(), false);
        Map<String, Object> properties = new HashMap<>();
        String contributionStatus = event.getContributionVO().getContributionStatus().getValue();
        properties.put(CmisProperties.CONTRIBUTION_STATUS.getId(), contributionStatus);
        Bill updatedBill = billService.updateBill(revision.getId(), properties, false);
        if (updatedBill != null) {
            documentScreen.disableMergePane();
            event.getSelectedItem().setVisible(false);
        } else {
            throw new RuntimeException("Unable to update document revision status");
        }
    }

    void markRevisionAsProcessed(String revisionDocumentId, String msgKey) {
        cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);
        final Bill revision = billService.findBill(revisionDocumentId, false);

        documentScreen.disableMergePane();
        Map<String, Object> properties = new HashMap<>();
        properties.put(CmisProperties.CONTRIBUTION_STATUS.getId(), ContributionVO.ContributionStatus.CONTRIBUTION_DONE.getValue());
        billService.updateBill(revision.getId(), properties, false);
        final List<ContributionVO> allContributions = contributionService.getDocumentContributions(documentId, 0, Bill.class);
        documentScreen.populateContributions(allContributions);
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
        Bill bill = getDocument();
        List<TocItem> tocItemList = getTocITems(bill);
        byte[] xmlClonedContent = event.getMergeActionVOS().get(0).getContributionVO().getXmlContent();
        List<InternalRefMap> intRefMap = getInternalRefMaps(event, bill, xmlClonedContent);
        byte[] xmlContent = mergeContributionHelper.updateDocumentWithContributions(event, bill, tocItemList, intRefMap);
        xmlContent = numberService.renumberArticles(xmlContent, true);
        xmlContent = numberService.renumberRecitals(xmlContent);
        xmlContent = xmlContentProcessor.doXMLPostProcessing(xmlContent);
        bill = billService.updateBill(bill, xmlContent, messageHelper.getMessage("contribution.merge.operation.message"));
        if (bill != null) {
            eventBus.post(new RefreshDocumentEvent());
            eventBus.post(new DocumentUpdatedEvent());
            leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
        }
        if(event.isAllContributions()) {
            markRevisionAsProcessed(event.getMergeActionVOS().get(0).getContributionVO().getDocumentId(), "contribution.accept.all.notification.message");
        } else {
            eventBus.post(new RefreshContributionEvent());
        }
    }

    private List<InternalRefMap> getInternalRefMaps(ApplyContributionsRequestEvent event, Bill bill, byte[] xmlClonedContent) {
        byte[] xmlContent = bill.getContent().get().getSource().getBytes();
        Map<String, String> attachmentsClonedContent = attachmentProcessor.getAttachmentsHrefFromBill(xmlClonedContent);
        List<InternalRefMap> map = new ArrayList<>();
        String ref = bill.getName().replace(".xml", "");
        String clonedRef  = event.getMergeActionVOS().get(0).getContributionVO().getDocumentName().replace(".xml", "");
        map.add(new InternalRefMap("BILL", ref, clonedRef));
        Map<String, String> attachments = attachmentProcessor.getAttachmentsHrefFromBill(xmlContent);
        attachments.forEach((docType, href) -> {
            String cloned = attachmentsClonedContent.get(docType);
            map.add(new InternalRefMap(docType, href, cloned));
        });
        return map;
    }

    @Subscribe
    void showCleanVersion(ShowCleanVersionRequestEvent event) {
        final Bill bill = getDocument();
        final String versionContent = documentContentService.getCleanDocumentAsHtml(bill,
                urlBuilder.getWebAppPath(VaadinServletService.getCurrentServletRequest()),
                securityContext.getPermissions(bill));
        final String versionInfo = getVersionInfoAsString(bill);
        documentScreen.showCleanVersion(versionContent, versionInfo);
    }

    @Subscribe
    void compareUpdateDocumentView(CompareRequestEvent event) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);
        final Bill oldVersion = billService.findBillVersion(event.getOldVersionId());
        final Bill newVersion = billService.findBillVersion(event.getNewVersionId());
        final String comparedContent = comparisonDelegate.getMarkedContent(oldVersion, newVersion);
        final String comparedInfo = messageHelper.getMessage("version.compare.simple", oldVersion.getVersionLabel(), newVersion.getVersionLabel());
        documentScreen.populateMarkedContent(comparedContent, comparedInfo, oldVersion, newVersion);
        LOG.info("Bill comparison between v{} and v{} in {} milliseconds ({} sec)", oldVersion.getVersionLabel(), newVersion.getVersionLabel(), stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
    }

    @Subscribe
    void comparePopulateTimeLine(CompareTimeLineRequestEvent event) {
        final Bill oldVersion = billService.findBillVersion(event.getOldVersion());
        final Bill newVersion = billService.findBillVersion(event.getNewVersion());
        final ComparisonDisplayMode displayMode = event.getDisplayMode();
        HashMap<ComparisonDisplayMode, Object> result = comparisonDelegate.versionCompare(oldVersion, newVersion, displayMode);
        documentScreen.displayComparison(result);
    }

    @Subscribe
    void doubleCompare(DoubleCompareRequestEvent event) {
        final Bill original = billService.findBillVersion(event.getOriginalProposalId());
        final Bill intermediate = billService.findBillVersion(event.getIntermediateMajorId());
        final Bill current = billService.findBillVersion(event.getCurrentId());

        final String comparedContent = comparisonDelegate.doubleCompareHtmlContents(original, intermediate, current, true);
        final String comparedInfo = messageHelper.getMessage("version.compare.double", original.getVersionLabel(), intermediate.getVersionLabel(), current.getVersionLabel());
        documentScreen.populateDoubleComparisonContent(comparedContent, comparedInfo, original, intermediate, current);
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
    void getDocumentVersionsList(VersionListRequestEvent<Bill> event) {
        List<Bill> billVersions = billService.findVersions(documentId);
        eventBus.post(new VersionListResponseEvent<>(new ArrayList<>(billVersions)));
    }

    @Subscribe
    void downloadActualVersion(DownloadActualVersionRequestEvent event) {
        requestFilteredAnnotationsForDownload(event.isWithFilteredAnnotations());
    }

    private void doDownloadActualVersion(Boolean isWithAnnotations, String annotations) {
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            final Bill currentDocument = getDocument();
            ExportOptions exportOptions;
            XmlDocument original = documentContentService.getOriginalBill(currentDocument);
            if (InstanceType.COMMISSION.toString().equals(instanceTypeResolver.getInstanceType())) {
                exportOptions = new ExportLW(ExportOptions.Output.PDF, Bill.class, false);
                exportOptions.setExportVersions(new ExportVersions(isClonedProposal() ? original : null, currentDocument));
            } else {
                exportOptions = new ExportDW(ExportOptions.Output.WORD, Bill.class, false);
                exportOptions.setExportVersions(new ExportVersions(original, currentDocument));
            }
            exportOptions.setWithFilteredAnnotations(isWithAnnotations);
            exportOptions.setFilteredAnnotations(annotations);
            exportOptions.setWithCoverPage(false);
            LeosPackage leosPackage = packageService.findPackageByDocumentId(documentId);
            BillContext context = billContextProvider.get();
            context.usePackage(leosPackage);
            String proposalId = context.getProposalId();
            if (proposalId != null) {
                final String jobFileName = "Proposal_" + proposalId + "_AKN2DW_" + System.currentTimeMillis() + ".docx";
                if (isClonedProposal() || InstanceType.COMMISSION.toString().equals(instanceTypeResolver.getInstanceType())) {
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
                } else {
                    byte[] exportedBytes = exportService.createDocuWritePackage(FileHelper.getReplacedExtensionFilename(jobFileName, "zip"), proposalId, exportOptions);
                    DownloadStreamResource downloadStreamResource = new DownloadStreamResource(FileHelper.getReplacedExtensionFilename(jobFileName, "docx"), new ByteArrayInputStream(exportedBytes));
                    documentScreen.setDownloadStreamResourceForMenu(downloadStreamResource);
                }
            }
            LOG.info("The actual version of Bill {} downloaded in {} milliseconds ({} sec)", currentDocument.getName(), stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while using ExportService", e);
            eventBus.post(new NotificationEvent(Type.ERROR, "export.docuwrite.error.message", e.getMessage()));
        }
    }

    private void requestFilteredAnnotationsForDownload(final Boolean isWithAnnotations) {
        if (isWithAnnotations) {
            this.downloadExportRequest = new DownloadExportRequest(DownloadExportRequest.RequestType.DOWNLOAD, null, null);
            eventBus.post(new RequestFilteredAnnotations());
        } else {
            doDownloadActualVersion(false, null);
        }
    }

    private void requestFilteredAnnotationsForExport(final String title, final Boolean isExportCleanVersion, ExportOptions exportOptions) {
        if (isExportCleanVersion) {
            this.downloadExportRequest = new DownloadExportRequest(DownloadExportRequest.RequestType.EXPORT_CLEAN, title, exportOptions);
        } else {
            this.downloadExportRequest = new DownloadExportRequest(DownloadExportRequest.RequestType.EXPORT, title, exportOptions);
        }
        if (exportOptions.isWithFilteredAnnotations()) {
            eventBus.post(new RequestFilteredAnnotations());
        } else {
            doExportPackage(null);
        }
    }

    @Subscribe
    void responseFilteredAnnotations(ResponseFilteredAnnotations event) {
        String filteredAnnotations = event.getAnnotations();
        if (this.downloadExportRequest.getRequestType().equals(DownloadExportRequest.RequestType.DOWNLOAD)) {
            doDownloadActualVersion(true, filteredAnnotations);
        } else {
            doExportPackage(filteredAnnotations);
        }
    }

    @Subscribe
    void downloadXmlFiles(DownloadXmlFilesRequestEvent event) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        final Map<String, Object> contentToZip = new HashMap<>();

        final ExportVersions<Bill> exportVersions = event.getExportOptions().getExportVersions();
        final Bill current = exportVersions.getCurrent();
        final Bill original = exportVersions.getOriginal();
        final Bill intermediate = exportVersions.getIntermediate();

        final String leosComparedContent;
        final String docuWriteComparedContent;
        final String comparedInfo;
        cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);
        String language = original.getMetadata().get().getLanguage();

        if (intermediate != null) {
            comparedInfo = messageHelper.getMessage("version.compare.double", original.getVersionLabel(), intermediate.getVersionLabel(), current.getVersionLabel());
            leosComparedContent = comparisonDelegate.doubleCompareHtmlContents(original, intermediate, current, true);
            docuWriteComparedContent = legService.doubleCompareXmlContents(original, intermediate, current, false);
        } else {
            comparedInfo = messageHelper.getMessage("version.compare.simple", original.getVersionLabel(), current.getVersionLabel());
            leosComparedContent = comparisonDelegate.getMarkedContent(original, current);
            docuWriteComparedContent = legService.simpleCompareXmlContents(original, current, true);
        }
        documentScreen.setDownloadStreamResourceForXmlFiles(original, intermediate, current, language, comparedInfo, leosComparedContent, docuWriteComparedContent);
        LOG.info("Xml files for Bill {}, downloaded in {} milliseconds ({} sec)", current.getName(), stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
    }

    @Subscribe
    void downloadXmlVersion(DownloadXmlVersionRequestEvent event) {
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);
            final Bill chosenDocument = billService.findBillVersion(event.getVersionId());
            final String fileName = chosenDocument.getMetadata().get().getRef() + "_v" + chosenDocument.getVersionLabel() + ".xml";

            DownloadStreamResource downloadStreamResource = new DownloadStreamResource(fileName, new ByteArrayInputStream(chosenDocument.getContent().get().getSource().getBytes()));
            documentScreen.setDownloadStreamResourceForVersion(downloadStreamResource, chosenDocument.getId());
            LOG.info("Downloaded file {}, in {} milliseconds ({} sec)", fileName, stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while downloadXmlVersion", e);
            eventBus.post(new NotificationEvent(Type.ERROR, "error.message", e.getMessage()));
        }
    }

    @Subscribe
    void exportToDocuWrite(DocuWriteExportRequestEvent event) {
        try {
            this.createDocuWritePackageForExport(event.getExportOptions());
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while using DocuWriteExportService", e);
        }
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

    private void createDocuWritePackageForExport(ExportOptions exportOptions) throws Exception {
        Stopwatch stopwatch = Stopwatch.createStarted();
        exportOptions.setDocuwrite(true);
        final String proposalId = this.getContextProposalId();

        if (proposalId != null) {
            final String jobFileName = "Proposal_" + proposalId + "_AKN2DW_" + System.currentTimeMillis() + ".docx";
            final byte[] exportedBytes = exportService.createDocuWritePackage(FileHelper.getReplacedExtensionFilename(jobFileName, "zip"), proposalId, exportOptions);
            this.setDocumentScreenDownloadStreamResourceForExport(jobFileName, exportedBytes);
            LOG.info("Exported to DocuWrite and downloaded file {}, in {} milliseconds ({} sec)", jobFileName,
                    stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
        }
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

    private void setDocumentScreenDownloadStreamResourceForExport(String jobFileName, byte[] exportedBytes) {
        DownloadStreamResource downloadStreamResource = new DownloadStreamResource(jobFileName, new ByteArrayInputStream(exportedBytes));
        documentScreen.setDownloadStreamResourceForExport(downloadStreamResource);
    }

    private String getContextProposalId() {
        LeosPackage leosPackage = packageService.findPackageByDocumentId(documentId);
        BillContext context = billContextProvider.get();
        context.usePackage(leosPackage);
        return context.getProposalId();
    }

    @Subscribe
    void downloadCleanVersion(DownloadCleanVersion event) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        LeosPackage leosPackage = packageService.findPackageByDocumentId(documentId);
        BillContext context = billContextProvider.get();
        context.usePackage(leosPackage);
        String proposalId = context.getProposalId();
        if (isClonedProposal()) {
            try {
                final String jobFileName = "Proposal_" + proposalId + "_AKN2LW_CLEAN_" + System.currentTimeMillis() + ".zip";
                ExportOptions exportOptions = new ExportLW(ExportOptions.Output.PDF, Bill.class, false, true);
                exportOptions.setExportVersions(new ExportVersions(null, getDocument()));
                exportService.createDocumentPackage(jobFileName, proposalId, exportOptions, user);
                eventBus.post(new NotificationEvent("document.export.package.button.send", "document.export.message",
                        NotificationEvent.Type.TRAY, exportOptions.getExportOutputDescription(), user.getEmail()));
            } catch (Exception e) {
                LOG.error("Unexpected error occurred while using ExportService", e);
                eventBus.post(new NotificationEvent(Type.ERROR, "export.legiswrite.error.message", e.getMessage()));
            }
        } else {
            try {
                final String jobFileName = "Proposal_" + proposalId + "_AKN2DW_CLEAN_" + System.currentTimeMillis() + ".docx";
                ExportOptions exportOptions = new ExportDW(ExportOptions.Output.WORD, Bill.class, false, true);
                byte[] exportedBytes = exportService.createDocuWritePackage(FileHelper.getReplacedExtensionFilename(jobFileName, "zip"), proposalId, exportOptions);
                DownloadStreamResource downloadStreamResource = new DownloadStreamResource(jobFileName, new ByteArrayInputStream(exportedBytes));
                documentScreen.setDownloadStreamResourceForMenu(downloadStreamResource);
            } catch (Exception e) {
                LOG.error("Unexpected error occurred while using ExportService", e);
                eventBus.post(new NotificationEvent(Type.ERROR, "export.docuwrite.error.message", e.getMessage()));
            }
        }
        LOG.info("The actual version of CLEANED Bill for proposal {}, downloaded in {} milliseconds ({} sec)", proposalId, stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
    }

    @Subscribe
    void createExportPackageForActualVersion(CreateExportPackageActualVersionRequestEvent event) {
        final Bill currentDocument = getDocument();
        XmlDocument original = documentContentService.getOriginalBill(currentDocument);
        ExportOptions exportOptions = new ExportDW(ExportOptions.Output.WORD, Bill.class, false);
        exportOptions.setExportVersions(new ExportVersions(original, currentDocument));
        exportOptions.setRelevantElements(event.getRelevantElements());
        exportOptions.setWithFilteredAnnotations(event.isWithAnnotations());

        requestFilteredAnnotationsForExport(event.getTitle(), false, exportOptions);
    }

    @Subscribe
    void createExportPackageCleanVersion(CreateExportPackageCleanVersionRequestEvent event) {
        ExportOptions exportOptions = new ExportDW(ExportOptions.Output.WORD, Bill.class, false, true);
        exportOptions.setRelevantElements(event.getRelevantElements());
        exportOptions.setWithFilteredAnnotations(event.isWithAnnotations());

        requestFilteredAnnotationsForExport(event.getTitle(), true, exportOptions);
    }

    @Subscribe
    void createExportPackage(CreateExportPackageRequestEvent event) {
        ExportOptions exportOptions = event.getExportOptions();
        requestFilteredAnnotationsForExport(event.getTitle(), false, exportOptions);
    }

    private void doExportPackage(final String filteredAnnotations) {
        final String title = this.downloadExportRequest.getTitle();
        ExportOptions exportOptions = this.downloadExportRequest.getExportOptions();
        final String proposalId = getContextProposalId();
        final String jobFileName = this.downloadExportRequest.getRequestType().equals(DownloadExportRequest.RequestType.EXPORT_CLEAN)
                ? "Proposal_" + proposalId + "_AKN2DW_CLEAN_" + System.currentTimeMillis() + ".docx"
                : "Proposal_" + proposalId + "_AKN2DW_" + System.currentTimeMillis() + ".docx";

        ExportDocument exportDocument = null;
        LeosExportStatus processedStatus = LeosExportStatus.PROCESSED_ERROR;
        if (exportOptions.isWithFilteredAnnotations()) {
            exportOptions.setFilteredAnnotations(filteredAnnotations);
        }
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            exportOptions.setComments(getCommentsForExportPackage(title, exportOptions));

            byte[] exportedBytes = exportService.createExportPackage(FileHelper.getReplacedExtensionFilename(jobFileName, "zip"), proposalId, exportOptions);
            exportDocument = exportPackageService.createExportDocument(proposalId, exportOptions.getComments(), exportedBytes);
            exportPackageService.updateExportDocument(exportDocument.getId(), LeosExportStatus.NOTIFIED);
            notificationService.sendNotification(proposalRef, exportDocument.getId());
            processedStatus = LeosExportStatus.PROCESSED_OK;
            eventBus.post(new NotificationEvent("document.export.package.window.title", "document.export.package.creation.success", NotificationEvent.Type.TRAY));
            LOG.info("Export Package {} for proposal {} created in {} milliseconds ({} sec)", exportDocument.getName(), proposalRef, stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
        } catch (Exception e) {
            LogUtil.logError(LOG, eventBus, "Unexpected error occurred while generating Export Package", e);
        } finally {
            if (exportDocument != null) {
                exportDocument = exportPackageService.findExportDocumentById(exportDocument.getId(), false);
                if ((exportDocument != null) && (!exportDocument.getStatus().equals(LeosExportStatus.FILE_READY))) {
                    exportDocument = exportPackageService.updateExportDocument(exportDocument.getId(), processedStatus);
                    leosApplicationEventBus.post(new ExportPackageCreatedEvent(proposalRef, exportDocument));
                }
            }
        }
    }

    private List<String> getCommentsForExportPackage(String title, ExportOptions exportOptions) {
        List<String> comments = new ArrayList<>();
        comments.add(title);
        comments.add(messageHelper.getMessage("document.export.package.creation.comment.legal.act"));
        if (exportOptions.isComparisonMode()) {
            StringBuilder versionsComment = new StringBuilder();
            versionsComment.append(exportOptions.getExportVersions().getOriginal() != null ? getCommentForVersion(exportOptions.getExportVersions().getOriginal()) : "");
            versionsComment.append(exportOptions.getExportVersions().getIntermediate() != null ? " vs " + getCommentForVersion(exportOptions.getExportVersions().getIntermediate()) : "");
            versionsComment.append(exportOptions.getExportVersions().getCurrent() != null ? " vs " + getCommentForVersion(exportOptions.getExportVersions().getCurrent()) : "");
            comments.add(versionsComment.toString());
        } else {
            comments.add(getCommentForVersion(getDocument()));
        }
        return comments;
    }

    private String getCommentForVersion(XmlDocument document) {
        StringBuilder versionsComment = new StringBuilder(document.getVersionLabel());
        if (document.getVersionType().equals(VersionType.INTERMEDIATE) && document.getVersionComment() != null) {
            final CheckinCommentVO checkinCommentVO = CheckinCommentUtil.getJavaObjectFromJson(document.getVersionComment());
            versionsComment.append(" (").append(checkinCommentVO.getTitle()).append(") ");
        }
        return versionsComment.toString();
    }

    @Subscribe
    void versionRestore(RestoreVersionRequestEvent event) {
        String versionId = event.getVersionId();
        Bill version = billService.findBillVersion(versionId);
        byte[] resultXmlContent = getContent(version);
        billService.updateBill(getDocument(), resultXmlContent, messageHelper.getMessage("operation.restore.version", version.getVersionLabel()));

        List<Bill> documentVersions = billService.findVersions(documentId);
        documentScreen.updateTimeLineWindow(documentVersions);
        eventBus.post(new RefreshDocumentEvent());
        eventBus.post(new DocumentUpdatedEvent()); // Document might be updated.
        leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
    }

    @Subscribe
    public void getUserGuidance(FetchUserGuidanceRequest event) {
        Bill bill = billService.findBill(documentId, true);
        String jsonGuidance = templateConfigurationService.getTemplateConfiguration(bill.getMetadata().get().getDocTemplate(), "guidance");
        documentScreen.setUserGuidance(jsonGuidance);
    }

    @Subscribe
    public void getUserPermissions(FetchUserPermissionsRequest event) {
        Bill bill = getDocument();
        List<LeosPermission> userPermissions = securityContext.getPermissions(bill);
        documentScreen.sendUserPermissions(userPermissions);
        annotateService.sendUserPermissions(userPermissions);
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

    private DocumentVO createLegalTextVO(Bill bill) {
        DocumentVO billVO = new DocumentVO(bill.getId(),
                bill.getMetadata().exists(m -> m.getLanguage() != null) ? bill.getMetadata().get().getLanguage() : "EN",
                LeosCategory.BILL,
                bill.getLastModifiedBy(),
                Date.from(bill.getLastModificationInstant()));
        if (bill.getMetadata().isDefined()) {
            BillMetadata metadata = bill.getMetadata().get();
            billVO.getMetadata().setInternalRef(metadata.getRef());
        }
        if (!bill.getCollaborators().isEmpty()) {
            billVO.addCollaborators(bill.getCollaborators());
        }
        return billVO;
    }

    @Subscribe
    void updateDocument(DocumentUpdatedEvent event) {
        if (event.isModified()) {
            // 1. Refresh versions
            final List<VersionVO> allVersions = billService.getAllVersions(documentId, documentRef);
            documentScreen.refreshVersions(allVersions, comparisonMode);
            // 2. show clean version on right pane
            if (documentScreen.isCleanVersionShowed()) {
                showCleanVersion(new ShowCleanVersionRequestEvent());
            }
            //3. Update proposalMetadata async
            CollectionContext context = proposalContextProvider.get();
            context.useChildDocument(documentId);
            context.executeUpdateProposalAsync();
        }
    }

    @Subscribe
    public void onInfoUpdate(UpdateUserInfoEvent updateUserInfoEvent) {
        if (isCurrentInfoId(updateUserInfoEvent.getActionInfo().getInfo().getDocumentId())) {
            if (!id.equals(updateUserInfoEvent.getActionInfo().getInfo().getPresenterId())) {
                eventBus.post(new NotificationEvent(leosUI, "coedition.caption",
                        "coedition.operation." + updateUserInfoEvent.getActionInfo().getOperation().getValue(),
                        NotificationEvent.Type.TRAY, updateUserInfoEvent.getActionInfo().getInfo().getUserName()));
            }
            LOG.debug("Document Presenter updated the edit info -" + updateUserInfoEvent.getActionInfo().getOperation().name());
            documentScreen.updateUserCoEditionInfo(updateUserInfoEvent.getActionInfo().getCoEditionVos(), id);
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
            documentScreen.displayDocumentUpdatedByCoEditorWarning();
        }
    }

    @Subscribe
    public void fetchMilestoneByVersionedReference(FetchMilestoneByVersionedReferenceEvent event) {
        LeosPackage leosPackage = packageService.findPackageByDocumentId(documentId);
        LegDocument legDocument = legService.findLastLegByVersionedReference(leosPackage.getPath(), event.getVersionedReference());
        milestoneExplorerOpened = true;
        documentScreen.showMilestoneExplorer(legDocument, String.join(",", legDocument.getMilestoneComments()), proposalRef);
    }

    @Subscribe
    public void changeComparisonMode(ComparisonEvent event) {
        comparisonMode = event.isComparsionMode();
        if (comparisonMode) {
            documentScreen.cleanComparedContent();
            if (!documentScreen.isComparisonComponentVisible()) {
                LayoutChangeRequestEvent layoutEvent = new LayoutChangeRequestEvent(ColumnPosition.DEFAULT, ComparisonComponent.class, null);
                eventBus.post(layoutEvent);
            }
        } else {
            LayoutChangeRequestEvent layoutEvent = new LayoutChangeRequestEvent(ColumnPosition.OFF, ComparisonComponent.class, null);
            eventBus.post(layoutEvent);
            eventBus.post(new ResetRevisionComponentEvent());
        }
        eventBus.post(new DocumentUpdatedEvent());
    }

    /**
     * Reloads the document after the embedded milestone explorer is closed
     * Solves LEOS-3923
     *
     * @param windowClosedEvent not used
     */
    @Subscribe
    public void afterClosedWindow(WindowClosedEvent<MilestoneExplorer> windowClosedEvent) {
        milestoneExplorerOpened = false;
        eventBus.post(new NavigationRequestEvent(Target.LEGALTEXT, getDocumentRef()));
    }

    private boolean isClonedProposal() {
        cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);
        return cloneContext != null && cloneContext.isClonedProposal();
    }
}
