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
package eu.europa.ec.leos.ui.view.annex;

import com.google.common.base.Stopwatch;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.server.VaadinServletService;
import eu.europa.ec.leos.cmis.domain.ContentImpl;
import eu.europa.ec.leos.cmis.domain.SourceImpl;
import eu.europa.ec.leos.cmis.mapping.CmisProperties;
import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.LeosExportStatus;
import eu.europa.ec.leos.domain.cmis.LeosPackage;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.document.Annex;
import eu.europa.ec.leos.domain.cmis.document.ExportDocument;
import eu.europa.ec.leos.domain.cmis.document.LegDocument;
import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.domain.cmis.metadata.AnnexMetadata;
import eu.europa.ec.leos.domain.cmis.metadata.LeosMetadata;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.domain.common.Result;
import eu.europa.ec.leos.domain.common.TocMode;
import eu.europa.ec.leos.domain.vo.CloneProposalMetadataVO;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.domain.vo.SearchMatchVO;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.action.*;
import eu.europa.ec.leos.model.annex.AnnexStructureType;
import eu.europa.ec.leos.model.annex.LevelItemVO;
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
import eu.europa.ec.leos.services.document.AnnexService;
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
import eu.europa.ec.leos.services.label.ReferenceLabelService;
import eu.europa.ec.leos.services.messaging.UpdateInternalReferencesProducer;
import eu.europa.ec.leos.services.notification.NotificationService;
import eu.europa.ec.leos.services.numbering.NumberService;
import eu.europa.ec.leos.services.processor.AnnexProcessor;
import eu.europa.ec.leos.services.processor.AttachmentProcessor;
import eu.europa.ec.leos.services.processor.ElementProcessor;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.search.SearchService;
import eu.europa.ec.leos.services.store.ExportPackageService;
import eu.europa.ec.leos.services.store.LegService;
import eu.europa.ec.leos.services.store.PackageService;
import eu.europa.ec.leos.services.store.WorkspaceService;
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
import eu.europa.ec.leos.ui.event.view.AnnexStructureChangeEvent;
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
import eu.europa.ec.leos.usecases.document.AnnexContext;
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
import eu.europa.ec.leos.web.event.view.document.CloseDocumentConfirmationEvent;
import eu.europa.ec.leos.web.event.view.document.CloseElementEvent;
import eu.europa.ec.leos.web.event.view.document.ComparisonEvent;
import eu.europa.ec.leos.web.event.view.document.ConfirmRenumberingEvent;
import eu.europa.ec.leos.web.event.view.document.DeleteElementRequestEvent;
import eu.europa.ec.leos.web.event.view.document.DocumentNavigationRequest;
import eu.europa.ec.leos.web.event.view.document.DocumentUpdatedEvent;
import eu.europa.ec.leos.web.event.view.document.EditElementRequestEvent;
import eu.europa.ec.leos.web.event.view.document.FetchCrossRefTocRequestEvent;
import eu.europa.ec.leos.web.event.view.document.FetchCrossRefTocResponseEvent;
import eu.europa.ec.leos.web.event.view.document.FetchElementRequestEvent;
import eu.europa.ec.leos.web.event.view.document.FetchElementResponseEvent;
import eu.europa.ec.leos.web.event.view.document.FetchUserPermissionsRequest;
import eu.europa.ec.leos.web.event.view.document.InsertElementRequestEvent;
import eu.europa.ec.leos.web.event.view.document.InstanceTypeResolver;
import eu.europa.ec.leos.web.event.view.document.MergeSuggestionRequest;
import eu.europa.ec.leos.web.event.view.document.MergeSuggestionResponse;
import eu.europa.ec.leos.web.event.view.document.MergeSuggestionsRequest;
import eu.europa.ec.leos.web.event.view.document.ReferenceLabelRequestEvent;
import eu.europa.ec.leos.web.event.view.document.ReferenceLabelResponseEvent;
import eu.europa.ec.leos.web.event.view.document.RefreshContributionEvent;
import eu.europa.ec.leos.web.event.view.document.RefreshDocumentEvent;
import eu.europa.ec.leos.web.event.view.document.RenumberingEvent;
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
import eu.europa.ec.leos.web.model.TocAndAncestorsVO;
import eu.europa.ec.leos.web.model.VersionInfoVO;
import eu.europa.ec.leos.web.support.SessionAttribute;
import eu.europa.ec.leos.web.support.UrlBuilder;
import eu.europa.ec.leos.web.support.UuidHelper;
import eu.europa.ec.leos.web.support.cfg.ConfigurationHelper;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static eu.europa.ec.leos.model.annex.AnnexStructureType.ARTICLE;
import static eu.europa.ec.leos.services.support.XmlHelper.CONTENT;
import static eu.europa.ec.leos.services.support.XmlHelper.INDENT;
import static eu.europa.ec.leos.services.support.XmlHelper.LEVEL;
import static eu.europa.ec.leos.services.support.XmlHelper.NUM;
import static eu.europa.ec.leos.services.support.XmlHelper.PARAGRAPH;
import static eu.europa.ec.leos.services.support.XmlHelper.POINT;
import static eu.europa.ec.leos.services.support.XmlHelper.SUBPARAGRAPH;
import static eu.europa.ec.leos.services.support.XmlHelper.SUBPOINT;
import static eu.europa.ec.leos.util.LeosDomainUtil.CMIS_PROPERTY_SPLITTER;

@Component
@Scope("prototype")
class AnnexPresenter extends AbstractLeosPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(AnnexPresenter.class);

    private final AnnexScreen annexScreen;
    private final AnnexService annexService;
    private final ContributionService contributionService;
    private final ElementProcessor<Annex> elementProcessor;
    private final AnnexProcessor annexProcessor;
    private final DocumentContentService documentContentService;
    private final UrlBuilder urlBuilder;
    private final ComparisonDelegate<Annex> comparisonDelegate;
    private final UserHelper userHelper;
    private final MessageHelper messageHelper;
    private final ConfigurationHelper cfgHelper;
    private final Provider<CollectionContext> proposalContextProvider;
    private final CoEditionHelper coEditionHelper;
    private final ExportService exportService;
    private final Provider<BillContext> billContextProvider;
    private final Provider<StructureContext> structureContextProvider;
    private final ReferenceLabelService referenceLabelService;
    private final Provider<AnnexContext> annexContextProvider;
    private final UpdateInternalReferencesProducer updateInternalReferencesProducer;
    private final TransformationService transformationService;
    private final LegService legService;
    private final ProposalService proposalService;
    private final SearchService searchService;
    private final ExportPackageService exportPackageService;
    private final NotificationService notificationService;
    private final CloneContext cloneContext;
    private CloneProposalMetadataVO cloneProposalMetadataVO;
    private DownloadExportRequest downloadExportRequest;
    protected final AttachmentProcessor attachmentProcessor;

    private String strDocumentVersionSeriesId;
    private String documentId;
    private String documentRef;
    private Element elementToEditAfterClose;
    private boolean comparisonMode;
    private String proposalRef;
    private String connectedEntity;
    private final CommonDelegate<Annex> commonDelegate;
    private boolean milestoneExplorerOpened = false;
    private InstanceTypeResolver instanceTypeResolver;

    private final static SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private MergeContributionHelper mergeContributionHelper;
    private NumberService numberService;
    private XmlContentProcessor xmlContentProcessor;
    private final AnnotateService annotateService;
    private final List<String> openElementEditors;

    @Autowired
    AnnexPresenter(SecurityContext securityContext, HttpSession httpSession, EventBus eventBus,
                   AnnexScreen annexScreen,
                   AnnexService annexService, PackageService packageService, ContributionService contributionService, ExportService exportService,
                   Provider<BillContext> billContextProvider, Provider<AnnexContext> annexContextProvider, ElementProcessor<Annex> elementProcessor,
                   AnnexProcessor annexProcessor, DocumentContentService documentContentService, UrlBuilder urlBuilder,
                   ComparisonDelegate<Annex> comparisonDelegate, UserHelper userHelper,
                   MessageHelper messageHelper, ConfigurationHelper cfgHelper, Provider<CollectionContext> proposalContextProvider,
                   CoEditionHelper coEditionHelper, EventBus leosApplicationEventBus, UuidHelper uuidHelper,
                   Provider<StructureContext> structureContextProvider, ReferenceLabelService referenceLabelService, WorkspaceService workspaceService,
                   UpdateInternalReferencesProducer updateInternalReferencesProducer, TransformationService transformationService, LegService legService,
                   ProposalService proposalService, SearchService searchService, ExportPackageService exportPackageService, AnnotateService annotateService,
                   NotificationService notificationService, CommonDelegate<Annex> commonDelegate, CloneContext cloneContext, AttachmentProcessor attachmentProcessor, InstanceTypeResolver instanceTypeResolver, NumberService numberService, MergeContributionHelper mergeContributionHelper, XmlContentProcessor xmlContentProcessor) {
        super(securityContext, httpSession, eventBus, leosApplicationEventBus, uuidHelper, packageService, workspaceService);
        this.attachmentProcessor = attachmentProcessor;
        LOG.trace("Initializing annex presenter...");
        this.annexScreen = annexScreen;
        this.annexService = annexService;
        this.contributionService = contributionService;
        this.elementProcessor = elementProcessor;
        this.annexProcessor = annexProcessor;
        this.documentContentService = documentContentService;
        this.urlBuilder = urlBuilder;
        this.comparisonDelegate = comparisonDelegate;
        this.userHelper = userHelper;
        this.messageHelper = messageHelper;
        this.cfgHelper = cfgHelper;
        this.proposalContextProvider = proposalContextProvider;
        this.coEditionHelper = coEditionHelper;
        this.exportService = exportService;
        this.billContextProvider = billContextProvider;
        this.annexContextProvider = annexContextProvider;
        this.structureContextProvider = structureContextProvider;
        this.referenceLabelService = referenceLabelService;
        this.updateInternalReferencesProducer = updateInternalReferencesProducer;
        this.transformationService = transformationService;
        this.legService = legService;
        this.proposalService = proposalService;
        this.searchService = searchService;
        this.exportPackageService = exportPackageService;
        this.notificationService = notificationService;
        this.commonDelegate = commonDelegate;
        this.cloneContext = cloneContext;
        this.instanceTypeResolver = instanceTypeResolver;
        this.numberService = numberService;
        this.mergeContributionHelper = mergeContributionHelper;
        this.xmlContentProcessor = xmlContentProcessor;
        this.openElementEditors = new ArrayList<>();
        this.annotateService = annotateService;
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
            Annex annex = getDocument();
            populateViewData(annex, TocMode.SIMPLIFIED);
            populateVersionsData(annex);
            String revisionReference = getRevisionRef();
            if (revisionReference != null) {
                Optional<ContributionVO> contributionVO = annexScreen.findContributionAndShowTab(revisionReference);
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
        Proposal proposal = null;
        Annex annex = getDocument();
        if (annex != null) {
            LeosPackage leosPackage = packageService.findPackageByDocumentId(annex.getId());
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
        return (String) httpSession.getAttribute(id + "." + SessionAttribute.ANNEX_REF.name());
    }

    private String getRevisionRef() {
        return (String) httpSession.getAttribute(id + "." + SessionAttribute.REVISION_VERSION.name());
    }

    private Annex getDocument() {
        documentRef = getDocumentRef();
        Annex annex = annexService.findAnnexByRef(documentRef);
        setDocumentData(annex);
        return annex;
    }

    private void setDocumentData(Annex annex) {
        strDocumentVersionSeriesId = annex.getVersionSeriesId();
        documentId = annex.getId();
        structureContextProvider.get().useDocumentTemplate(annex.getMetadata().getOrError(() -> "Annex metadata is required!").getDocTemplate());
        cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);
    }

    private void populateViewData(Annex annex, TocMode mode) {
        Validate.notNull(annex, "Annex document should not be null");
        try{
            Option<AnnexMetadata> annexMetadata = annex.getMetadata();
            if (annexMetadata.isDefined()) {
                annexScreen.setTitle(annexMetadata.get().getTitle(), annexMetadata.get().getNumber());
            }
            annexScreen.setContent(getEditableXml(annex));
            annexScreen.setToc(getTableOfContent(annex, mode));
            annexScreen.setStructureChangeMenuItem();
            DocumentVO annexVO = createAnnexVO(annex);
            annexScreen.updateUserCoEditionInfo(coEditionHelper.getCurrentEditInfo(annex.getVersionSeriesId()), id);
            annexScreen.setPermissions(annexVO, isClonedProposal());
            annexScreen.initAnnotations(annexVO, proposalRef, connectedEntity);
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
        annexScreen.initLeosEditor(event.getDocument(), documentsMetadata);
    }

    private void populateVersionsData(Annex annex) {
        DocumentVO annexVO = createAnnexVO(annex);
        final List<VersionVO> allVersions = getVersionVOS();
        final List<ContributionVO> allContributions = contributionService.getDocumentContributions(documentId, getDocument().getMetadata().get().getIndex(), Annex.class);
        annexScreen.setDataFunctions(
                annexVO,
                allVersions,
                allContributions,
                this::majorVersionsFn, this::countMajorVersionsFn,
                this::minorVersionsFn, this::countMinorVersionsFn,
                this::recentChangesFn, this::countRecentChangesFn);
    }

    private List<VersionVO> getVersionVOS() {
        return annexService.getAllVersions(documentId, documentRef);
    }

    @Subscribe
    public void updateVersionsTab(DocumentUpdatedEvent event) {
        final List<VersionVO> allVersions = getVersionVOS();
        annexScreen.refreshVersions(allVersions, comparisonMode);
    }

    private Integer countMinorVersionsFn(String currIntVersion) {
        return annexService.findAllMinorsCountForIntermediate(documentRef, currIntVersion);
    }

    private List<Annex> minorVersionsFn(String currIntVersion, int startIndex, int maxResults) {
        return annexService.findAllMinorsForIntermediate(documentRef, currIntVersion, startIndex, maxResults);
    }

    private Integer countMajorVersionsFn() {
        return annexService.findAllMajorsCount(documentRef);
    }

    private List<Annex> majorVersionsFn(int startIndex, int maxResults) {
        return annexService.findAllMajors(documentRef, startIndex, maxResults);
    }

    private Integer countRecentChangesFn() {
        return annexService.findRecentMinorVersionsCount(documentId, documentRef);
    }

    private List<Annex> recentChangesFn(int startIndex, int maxResults) {
        return annexService.findRecentMinorVersions(documentId, documentRef, startIndex, maxResults);
    }

    private List<TableOfContentItemVO> getTableOfContent(Annex annex, TocMode mode) {
        return annexService.getTableOfContent(annex, mode);
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
    void downloadActualVersion(DownloadActualVersionRequestEvent event) {
        requestFilteredAnnotationsForDownload(event.isWithFilteredAnnotations());
    }

    private void doDownloadActualVersion(Boolean isWithAnnotations, String annotations) {
        try {
            XmlDocument original = documentContentService.getOriginalAnnex(getDocument());
            ExportOptions exportOptions;
            if (InstanceType.COMMISSION.toString().equals(instanceTypeResolver.getInstanceType())) {
                exportOptions = new ExportLW(ExportOptions.Output.PDF, Annex.class, false);
                exportOptions.setExportVersions(new ExportVersions<>(isClonedProposal() ? original : null, getDocument()));

            } else {
                exportOptions = new ExportDW(ExportOptions.Output.WORD, Annex.class, false);
                exportOptions.setExportVersions(new ExportVersions<>(original, getDocument()));
            }
            exportOptions.setWithFilteredAnnotations(isWithAnnotations);
            exportOptions.setFilteredAnnotations(annotations);
            exportOptions.setWithCoverPage(false);
            LeosPackage leosPackage = packageService.findPackageByDocumentId(documentId);
            BillContext context = billContextProvider.get();
            context.usePackage(leosPackage);
            String proposalId = context.getProposalId();

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
                    LOG.error("Unexpected error occurred while using LegisWriteExportService", e);
                    eventBus.post(new NotificationEvent(Type.ERROR, "export.package.error.message", e.getMessage()));
                }
            } else {
                byte[] exportedBytes = exportService.createDocuWritePackage(jobFileName, proposalId, exportOptions);
                DownloadStreamResource downloadStreamResource = new DownloadStreamResource(jobFileName, new ByteArrayInputStream(exportedBytes));
                annexScreen.setDownloadStreamResourceForMenu(downloadStreamResource);
            }
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while using ExportService", e);
            eventBus.post(new NotificationEvent(Type.ERROR, "export.docuwrite.error.message", e.getMessage()));
        }
    }

    @Subscribe
    void downloadXmlFiles(DownloadXmlFilesRequestEvent event) {
        Stopwatch stopwatch = Stopwatch.createStarted();

        final ExportVersions<Annex> exportVersions = event.getExportOptions().getExportVersions();
        final Annex current = exportVersions.getCurrent();
        final Annex original = exportVersions.getOriginal();
        final Annex intermediate = exportVersions.getIntermediate();

        final String leosComparedContent;
        final String docuWriteComparedContent;
        final String comparedInfo;
        cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);

        String language = original.getMetadata().get().getLanguage();

        if(intermediate != null){
            comparedInfo = messageHelper.getMessage("version.compare.double", original.getVersionLabel(), intermediate.getVersionLabel(), current.getVersionLabel());
            leosComparedContent = comparisonDelegate.doubleCompareHtmlContents(original, intermediate, current, true);
            docuWriteComparedContent = legService.doubleCompareXmlContents(original, intermediate, current, false);
        } else {
            comparedInfo = messageHelper.getMessage("version.compare.simple", original.getVersionLabel(), current.getVersionLabel());
            leosComparedContent = comparisonDelegate.getMarkedContent(original, current);
            docuWriteComparedContent = legService.simpleCompareXmlContents(original, current, true);
        }
        annexScreen.setDownloadStreamResourceForXmlFiles(original, intermediate, current, language, comparedInfo, leosComparedContent, docuWriteComparedContent);
        LOG.info("Xml files for Annex {}, downloaded in {} milliseconds ({} sec)", current.getName(), stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
    }

    @Subscribe
    void downloadXmlVersion(DownloadXmlVersionRequestEvent event) {
        try {
            cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);
            final Annex chosenDocument = annexService.findAnnexVersion(event.getVersionId());
            final String fileName = chosenDocument.getMetadata().get().getRef() + "_v" + chosenDocument.getVersionLabel() + ".xml";

            DownloadStreamResource downloadStreamResource = new DownloadStreamResource(fileName, new ByteArrayInputStream(chosenDocument.getContent().get().getSource().getBytes()));
            annexScreen.setDownloadStreamResourceForVersion(downloadStreamResource, chosenDocument.getId());
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while downloadXmlVersion", e);
            eventBus.post(new NotificationEvent(Type.ERROR, "error.message", e.getMessage()));
        }
    }

    @Subscribe
    void exportToDocuWrite(DocuWriteExportRequestEvent event){
        try {
            this.createDocuWritePackageForExport(event.getExportOptions());
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while using DocuWriteExportService", e);
            eventBus.post(new NotificationEvent(Type.ERROR, "export.docuwrite.error.message", e.getMessage()));
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
            eventBus.post(new NotificationEvent(Type.ERROR, "export.legiswrite.error.message", e.getMessage()));
        }
    }

    @Subscribe
    void confirmRenumberDocument(ConfirmRenumberingEvent event) {
        annexScreen.confirmRenumberDocument();
    }

    @Subscribe
    void renumberDocument(RenumberingEvent event) {

        Stopwatch stopwatch = Stopwatch.createStarted();
        final Annex annex = getDocument();

        AnnexStructureType structureType = getStructureType();
        final byte[] newXmlContent = annexProcessor.renumberDocument(annex, structureType);

        final String title = messageHelper.getMessage("operation.element.document_renumbered");
        final String description = messageHelper.getMessage("operation.checkin.minor");
        final CheckinCommentVO checkinComment = new CheckinCommentVO(title, description, new CheckinElement(ActionType.DOCUMENT_RENUMBERED));
        final String checkinCommentJson = CheckinCommentUtil.getJsonObject(checkinComment);

        updateAnnexContent(annex, newXmlContent, checkinCommentJson, "document.renumbered");
        updateInternalReferencesProducer.send(new UpdateInternalReferencesMessage(annex.getId(), annex.getMetadata().get().getRef(), id));
        LOG.info("Renumbering document executed, in {} milliseconds ({} sec)", stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));

    }

    private void createDocuWritePackageForExport(ExportOptions exportOptions) throws Exception {
        final String proposalId = this.getContextProposalId();
        exportOptions.setDocuwrite(true);
        if (proposalId != null){
            final String jobFileName = "Proposal_" + proposalId + "_AKN2DW_" + System.currentTimeMillis() + ".docx";
            final byte[] exportedBytes = exportService.createDocuWritePackage(jobFileName, proposalId, exportOptions);
            this.setAnnexScreenDownloadStreamResourceForExport(jobFileName, exportedBytes);
        }

    }

    private void createDocumentPackageForExport(ExportOptions exportOptions) throws Exception {
        final String proposalId = this.getContextProposalId();
        if (proposalId != null){
            final String jobFileName = "Proposal_" + proposalId + "_AKN2DW_" + System.currentTimeMillis() + ".zip";
            exportService.createDocumentPackage(jobFileName, proposalId, exportOptions, user);
        }
    }

    private void setAnnexScreenDownloadStreamResourceForExport(String jobFileName, byte[] exportedBytes){
        DownloadStreamResource downloadStreamResource = new DownloadStreamResource(jobFileName, new ByteArrayInputStream(exportedBytes));
        annexScreen.setDownloadStreamResourceForExport(downloadStreamResource);
    }

    private String getContextProposalId(){
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
                final String jobFileName = "Proposal_" + proposalId + "_AKN2LW_CLEAN_" + System.currentTimeMillis() + ".docx";
                ExportOptions exportOptions = new ExportLW(ExportOptions.Output.PDF, Annex.class, false, true);
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
                ExportOptions exportOptions = new ExportDW(ExportOptions.Output.WORD, Annex.class, false, true);
                exportOptions.setExportVersions(new ExportVersions<Annex>(null, getDocument()));
                byte[] exportedBytes = exportService.createDocuWritePackage(FileHelper.getReplacedExtensionFilename(jobFileName,"zip"), proposalId, exportOptions);
                DownloadStreamResource downloadStreamResource = new DownloadStreamResource(jobFileName, new ByteArrayInputStream(exportedBytes));
                annexScreen.setDownloadStreamResourceForMenu(downloadStreamResource);
            } catch (Exception e) {
                LOG.error("Unexpected error occurred while using ExportService", e);
                eventBus.post(new NotificationEvent(Type.ERROR, "export.docuwrite.error.message", e.getMessage()));
            }
        }
        LOG.info("The actual version of CLEANED Annex for proposal {}, downloaded in {} milliseconds ({} sec)", proposalId, stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
    }

    @Subscribe
    void createExportPackageForActualVersion(CreateExportPackageActualVersionRequestEvent event) {
        final Annex currentDocument = getDocument();
        XmlDocument original = documentContentService.getOriginalAnnex(currentDocument);
        ExportOptions exportOptions = new ExportDW(ExportOptions.Output.WORD, Annex.class, false);
        exportOptions.setExportVersions(new ExportVersions(original, currentDocument));
        exportOptions.setRelevantElements(event.getRelevantElements());
        exportOptions.setWithFilteredAnnotations(event.isWithAnnotations());

        requestFilteredAnnotationsForExport(event.getTitle(), false, exportOptions);
    }

    @Subscribe
    void createExportPackageCleanVersion(CreateExportPackageCleanVersionRequestEvent event) {
        ExportOptions exportOptions = new ExportDW(ExportOptions.Output.WORD, Annex.class, false, true);
        exportOptions.setRelevantElements(event.getRelevantElements());
        exportOptions.setWithFilteredAnnotations(event.isWithAnnotations());

        requestFilteredAnnotationsForExport(event.getTitle(), true, exportOptions);
    }

    @Subscribe
    void createExportPackage(CreateExportPackageRequestEvent event) {
        requestFilteredAnnotationsForExport(event.getTitle(), false, event.getExportOptions());
    }

    private void doExportPackage(final String filteredAnnotations) {
        final String title = this.downloadExportRequest.getTitle();
        ExportOptions exportOptions = this.downloadExportRequest.getExportOptions();
        final String proposalId = getContextProposalId();
        final String jobFileName = this.downloadExportRequest.getRequestType().equals(DownloadExportRequest.RequestType.EXPORT_CLEAN)
                ? "Proposal_" + proposalId + "_AKN2DW_CLEAN_" + System.currentTimeMillis() + ".zip"
                : "Proposal_" + proposalId + "_AKN2DW_" + System.currentTimeMillis() + ".zip";

        ExportDocument exportDocument = null;
        LeosExportStatus processedStatus = LeosExportStatus.PROCESSED_ERROR;
        if (exportOptions.isWithFilteredAnnotations()) {
            exportOptions.setFilteredAnnotations(filteredAnnotations);
        }
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            exportOptions.setComments(getCommentsForExportPackage(title, exportOptions));

            byte[] exportedBytes = exportService.createExportPackage(jobFileName, proposalId, exportOptions);
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
        comments.add(messageHelper.getMessage("document.export.package.creation.comment.annex"));
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
    void getDocumentVersionsList(VersionListRequestEvent<Annex> event) {
        List<Annex> annexVersions = annexService.findVersions(documentId);
        eventBus.post(new VersionListResponseEvent<Annex>(new ArrayList<>(annexVersions)));
    }

    private String getEditableXml(Annex document) {
        VersionInfoVO versionInfoVO = getVersionInfo(document);
        String baseRevisionId = document.getBaseRevisionId();
        cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);

        if(!StringUtils.isEmpty(baseRevisionId) && baseRevisionId.split(CMIS_PROPERTY_SPLITTER).length >= 3) {
            String versionLabel = baseRevisionId.split(CMIS_PROPERTY_SPLITTER)[1];
            String versionComment = baseRevisionId.split(CMIS_PROPERTY_SPLITTER)[2];
            versionInfoVO.setRevisedBaseVersion(versionLabel);
            versionInfoVO.setBaseVersionTitle(versionComment);
        }
        annexScreen.setDocumentVersionInfo(versionInfoVO);
        byte[] coverPageContent = new byte[0];
        byte[] annexContent = document.getContent().get().getSource().getBytes();
        boolean isCoverPageExists = documentContentService.isCoverPageExists(annexContent);
        if(annexScreen.isCoverPageVisible() && !isCoverPageExists) {
            Proposal proposal = getProposalFromPackage();
            byte[] xmlContent = proposal.getContent().get().getSource().getBytes();
            coverPageContent = documentContentService.getCoverPageContent(xmlContent);
        }
        String editableXml = documentContentService.toEditableContent(document,
                urlBuilder.getWebAppPath(VaadinServletService.getCurrentServletRequest()), securityContext, coverPageContent);
        return StringEscapeUtils.unescapeXml(editableXml);
    }

    private byte[] getContent(Annex annex) {
        final Content content = annex.getContent().getOrError(() -> "Annex content is required!");
        return content.getSource().getBytes();
    }


    @Subscribe
    void handleCloseDocument(CloseDocumentEvent event) {
        LOG.trace("Handling close document request...");

        //if unsaved changes remain in the session, first ask for confirmation
        if(this.isAnnexUnsaved() || this.isHasOpenElementEditors()){
            eventBus.post(new ShowConfirmDialogEvent(new CloseDocumentConfirmationEvent(), null));
            return;
        }
        this.closeDocument();
    }

    @Subscribe
    void handleNavigationRequest(DocumentNavigationRequest event) {
        LOG.trace("Handling document navigation request...");
        if (event.getNavigationEvent() != null) event.getNavigationEvent().setForwardToDocument(false);
        if(isAnnexUnsaved() || this.isHasOpenElementEditors()) {
            eventBus.post(new ShowConfirmDialogEvent(event.getNavigationEvent(), null));
            return;
        }
        eventBus.post(event.getNavigationEvent());
    }

    private boolean isAnnexUnsaved(){
        return getAnnexFromSession() != null;
    }

    private Annex getAnnexFromSession() {
        return (Annex) httpSession.getAttribute("annex#" + getDocumentRef());
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
        if (annexScreen.isTocEnabled()) {
            eventBus.post(new CloseTocAndDocumentEvent());
        } else {
            eventBus.post(new CloseDocumentEvent());
        }
    }

    @Subscribe
    void refreshDocument(RefreshDocumentEvent event) {
        Annex annex = getDocument();
        populateViewData(annex, event.getTocMode());
    }

    @Subscribe
    void refreshToc(RefreshTocEvent event) {
        try {
            Annex annex = getDocument();
            annexScreen.setToc(getTableOfContent(annex, event.getTocMode()));
        } catch (Exception ex) {
            LOG.error("Error while refreshing TOC", ex);
            eventBus.post(new NotificationEvent(Type.INFO, "error.message", ex.getMessage()));
        }
    }

    @Subscribe
    void deleteElement(DeleteElementRequestEvent event){
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            Annex annex = getDocument();
            String tagName = event.getElementTagName();
            byte[] updatedXmlContent = annexProcessor.deleteAnnexBlock(annex, event.getElementId(), tagName);

            // save document into repository
            annex = annexService.updateAnnex(annex, updatedXmlContent, VersionType.MINOR, messageHelper.getMessage("operation.annex.block.deleted"));
            if (annex != null) {
                eventBus.post(new NotificationEvent(Type.INFO, "document.annex.block.deleted", tagName.equalsIgnoreCase(LEVEL) ? StringUtils.capitalize(POINT) : StringUtils.capitalize(tagName)));
                eventBus.post(new RefreshDocumentEvent());
                eventBus.post(new DocumentUpdatedEvent());
                leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
                updateInternalReferencesProducer.send(new UpdateInternalReferencesMessage(annex.getId(), annex.getMetadata().get().getRef(), id));
            }
            LOG.info("Element '{}' in Annex {} id {}, deleted in {} milliseconds ({} sec)", event.getElementId(), annex.getName(), annex.getId(), stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
        }
        catch (Exception ex){
            LOG.error("Exception while deleting element operation for ", ex);
            eventBus.post(new NotificationEvent(Type.INFO, "error.message", ex.getMessage()));
        }
    }

    @Subscribe
    void insertElement(InsertElementRequestEvent event){
        Stopwatch stopwatch = Stopwatch.createStarted();
        String tagName = event.getElementTagName();
        Annex annex = getDocument();
        byte[] updatedXmlContent = annexProcessor.insertAnnexBlock(annex, event.getElementId(), tagName, InsertElementRequestEvent.POSITION.BEFORE.equals(event.getPosition()));

        annex = annexService.updateAnnex(annex, updatedXmlContent, VersionType.MINOR, messageHelper.getMessage("operation.annex.block.inserted"));
        if (annex != null) {
            eventBus.post(new NotificationEvent(Type.INFO, "document.annex.block.inserted",  tagName.equalsIgnoreCase(LEVEL) ? StringUtils.capitalize(POINT) : StringUtils.capitalize(tagName)));
            eventBus.post(new RefreshDocumentEvent());
            eventBus.post(new DocumentUpdatedEvent());
            leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
            updateInternalReferencesProducer.send(new UpdateInternalReferencesMessage(annex.getId(), annex.getMetadata().get().getRef(), id));
        }
        LOG.info("New Element of type '{}' inserted in Annex {} id {}, in {} milliseconds ({} sec)", tagName, annex.getName(), annex.getId(), stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
    }

    @Subscribe
    void mergeElement(MergeElementRequestEvent event) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            String elementId = event.getElementId();
            String tagName = event.getElementTagName();
            String elementContent = event.getElementContent();

            Annex annex = getDocument();
            Element mergeOnElement = annexProcessor.getMergeOnElement(annex, elementContent, tagName, elementId);
            if (mergeOnElement != null) {
                byte[] newXmlContent = annexProcessor.mergeElement(annex, elementContent, tagName, elementId);
                annex = annexService.updateAnnex(annex, newXmlContent, VersionType.MINOR, messageHelper.getMessage("operation.element.updated", org.apache.commons.lang3.StringUtils.capitalize(tagName)));
                if (annex != null) {
                    elementToEditAfterClose = mergeOnElement;
                    eventBus.post(new CloseElementEvent());
                    eventBus.post(new DocumentUpdatedEvent());
                    leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
                }
            } else {
                annexScreen.showAlertDialog("operation.element.not.performed");
            }
            LOG.info("Element '{}' merged into '{}' in Annex {} id {}, in {} milliseconds ({} sec)", elementId, mergeOnElement.getElementId(), annex.getName(), annex.getId(), stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
        } catch (Exception e) {
            LOG.error("Unexpected error in mergeElement", e);
            eventBus.post(new NotificationEvent(Type.ERROR, "unknown.error.message"));
        }
    }

    @Subscribe
    void checkElementCoEdition(CheckElementCoEditionEvent event) {
        try {
            if (event.getAction().equals(CheckElementCoEditionEvent.Action.MERGE)) {
                Annex annex = getDocument();
                Element mergeOnElement = annexProcessor.getMergeOnElement(annex, event.getElementContent(), event.getElementTagName(), event.getElementId());
                if (mergeOnElement != null) {
                    annexScreen.checkElementCoEdition(coEditionHelper.getCurrentEditInfo(strDocumentVersionSeriesId), user,
                            mergeOnElement.getElementId(), mergeOnElement.getElementTagName(), event.getAction(), event.getActionEvent());
                } else {
                    annexScreen.showAlertDialog("operation.element.not.performed");
                }
            } else {
                Annex annex = getDocument();
                Element tocElement = annexProcessor.getTocElement(annex, event.getElementId(), getTableOfContent(annex, TocMode.SIMPLIFIED));
                annexScreen.checkElementCoEdition(coEditionHelper.getCurrentEditInfo(strDocumentVersionSeriesId), user,
                        tocElement.getElementId(), tocElement.getElementTagName(), event.getAction(), event.getActionEvent());
            }
        } catch (Exception e) {
            LOG.error("Unexpected error in checkElementCoEdition", e);
            eventBus.post(new NotificationEvent(Type.ERROR, "unknown.error.message"));
        }
    }

    @Subscribe
    void cancelElementEditor(CancelElementEditorEvent event) {
        String elementId = event.getElementId();
        coEditionHelper.removeUserEditInfo(id, strDocumentVersionSeriesId, elementId, InfoType.ELEMENT_INFO);

        //load content from session if exists
        Annex annexFromSession = getAnnexFromSession();
        if(annexFromSession != null) {
            annexScreen.setContent(getEditableXml(annexFromSession));
        }else{
            eventBus.post(new RefreshDocumentEvent());
        }
        LOG.debug("User edit information removed");
    }


    @Subscribe
    void editElement(EditElementRequestEvent event){
        String elementId = event.getElementId();
        String elementTagName = event.getElementTagName();
        elementToEditAfterClose = null;
        LOG.trace("Handling edit element request... for {},id={}",elementTagName , elementId );
        try {
            //show confirm dialog if there is any unsaved replaced text
            //it can be detected from the session attribute
            if(isAnnexUnsaved()){
                eventBus.post(new ShowConfirmDialogEvent(event, new CancelElementEditorEvent(event.getElementId(),event.getElementTagName())));
                return;
            }

            Annex annex = getDocument();
            LevelItemVO levelItemVO = new LevelItemVO();
            String element = elementProcessor.getElement(annex, elementTagName, elementId);
            if (AnnexStructureType.LEVEL.getType().equalsIgnoreCase(elementTagName)
                    || NUM.equalsIgnoreCase(elementTagName)) {
                levelItemVO = annexProcessor.getLevelItemVO(annex, elementId, elementTagName);
            }
            coEditionHelper.storeUserEditInfo(httpSession.getId(), id, user, strDocumentVersionSeriesId, elementId, InfoType.ELEMENT_INFO);
            annexScreen.showElementEditor(elementId, elementTagName, element, levelItemVO);
            openElementEditors.add(elementId);
        }
        catch (Exception ex){
            LOG.error("Exception while edit element operation for ", ex);
            eventBus.post(new NotificationEvent(Type.INFO, "error.message", ex.getMessage()));
        }
    }

    @Subscribe
    void saveElement(SaveElementRequestEvent event){
        Stopwatch stopwatch = Stopwatch.createStarted();
        String elementId = event.getElementId();
        String elementTagName = event.getElementTagName();
        String elementContent = event.getElementContent();
        elementToEditAfterClose = null;
        LOG.trace("Handling save element request... for {},id={}",elementTagName , elementId );

        try {
            Annex annex = getDocument();
            byte[] updatedXmlContent = annexProcessor.updateAnnexBlock(annex, elementId, elementTagName, elementContent);
            if (updatedXmlContent == null) {
                annexScreen.showAlertDialog("operation.element.not.performed");
                return;
            }

            if (annex != null) {
                Pair<byte[], Element> splittedContent = null;
                if (event.isSplit() && checkIfCloseElementEditor(elementTagName, event.getElementContent())) {
                    splittedContent = annexProcessor.getSplittedElement(updatedXmlContent, event.getElementContent(), elementTagName, elementId);
                    if (splittedContent != null) {
                        elementToEditAfterClose = splittedContent.right();
                        if(splittedContent.left() != null){
                            updatedXmlContent = splittedContent.left();
                        }
                        eventBus.post(new CloseElementEvent());
                    }
                }
                annex = annexService.updateAnnex(annex, updatedXmlContent, VersionType.MINOR, messageHelper.getMessage("operation.annex.block.updated"));
                if (splittedContent == null) {
                    String newElementContent = elementProcessor.getElement(annex, elementTagName, elementId);
                    annexScreen.refreshElementEditor(elementId, elementTagName, newElementContent);
                }
                eventBus.post(new DocumentUpdatedEvent());
                annexScreen.scrollToMarkedChange(elementId);
                leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
            }
            LOG.info("Element '{}' in Annex {} id {}, saved in {} milliseconds ({} sec)", elementId, annex.getName(), annex.getId(), stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
        } catch (Exception ex) {
            LOG.error("Exception while save annex operation", ex);
            eventBus.post(new NotificationEvent(Type.INFO, "error.message", ex.getMessage()));
        }
    }

    boolean checkIfCloseElementEditor(String elementTagName, String elementContent) {
        switch (elementTagName) {
            case SUBPARAGRAPH:
            case CONTENT:
            case SUBPOINT:
                return elementContent.contains("<" + elementTagName);
            case PARAGRAPH:
                return elementContent.contains("<paragraph") || elementContent.contains("<subparagraph");
            case LEVEL:
                return elementContent.contains("<level") || elementContent.contains("<subparagraph");
            case POINT:
                return elementContent.contains("<alinea");
            case INDENT:
                return elementContent.contains("<alinea");
            default:
                return false;
        }
    }

    @Subscribe
    void closeAnnexBlock(CloseElementEditorEvent event){
        String elementId = event.getElementId();
        coEditionHelper.removeUserEditInfo(id, strDocumentVersionSeriesId, elementId, InfoType.ELEMENT_INFO);
        openElementEditors.remove(elementId);
        LOG.debug("User edit information removed");
        eventBus.post(new RefreshDocumentEvent());
        if (elementToEditAfterClose != null) {
            annexScreen.scrollTo(elementToEditAfterClose.getElementId());
            eventBus.post(new EditElementRequestEvent(elementToEditAfterClose.getElementId(), elementToEditAfterClose.getElementTagName()));
        }
    }

    @Subscribe
    void showTimeLineWindow(ShowTimeLineWindowEvent event) {
        List<Annex> documentVersions = annexService.findVersions(documentId);
        annexScreen.showTimeLineWindow(documentVersions);
    }

    @Subscribe
    void cleanComparedContent(CleanComparedContentEvent event) {
        annexScreen.cleanComparedContent();
    }

    @Subscribe
    void showVersion(ShowVersionRequestEvent event) {
        final Annex version = annexService.findAnnexVersion(event.getVersionId());
        final String versionContent = documentContentService.getDocumentAsHtml(version,
                urlBuilder.getWebAppPath(VaadinServletService.getCurrentServletRequest()),
                securityContext.getPermissions(version));
        final String versionInfo = getVersionInfoAsString(version);
        annexScreen.showVersion(versionContent, versionInfo);
    }

    @Subscribe
    void showRevision(OpenRevisionDocumentEvent event) {
        compareAndShowRevision(event.getContributionVO());
        Annex annex = getDocument();
        setDocumentData(annex);
    }

    @Subscribe
    void showCompareAndRevision(CompareAndShowRevisionEvent event) {
        cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);
        compareAndShowRevision(event.getContributionVO());
    }


    private void compareAndShowRevision(ContributionVO contributionVO) {
        final Annex revision = annexService.findAnnex(contributionVO.getDocumentId(), false);
        final String revisionContent = documentContentService.getDocumentForContributionAsHtml(contributionVO.getXmlContent(),
                urlBuilder.getWebAppPath(VaadinServletService.getCurrentServletRequest()),
                securityContext.getPermissions(revision));

        cloneContext.setContribution(Boolean.TRUE);
        annexScreen.refreshVersions(getVersionVOS(), false);
        Annex annex = getDocument();
        List<TocItem>  tocItemList = getTocITems(annex);

        final String temporaryAnnotationsId = this.storeRevisionAnnotationsTemporary(contributionVO.getDocumentId(), contributionVO.getLegFileName(), contributionVO.getVersionedReference());
        annexScreen.showRevisionWithSidebar(revisionContent, contributionVO, tocItemList, temporaryAnnotationsId);
    }

    private String storeRevisionAnnotationsTemporary(final String documentId, final String legFileName, final String versionedReference) {
        final LeosPackage leosPackage = this.packageService.findPackageByDocumentId(documentId);
        //final LegDocument referenceDocument = this.legService.findLastLegByVersionedReference(leosPackage.getPath(), versionedReference);
        final LegDocument legDocument = this.packageService.findDocumentByPackagePathAndName(leosPackage.getPath(), legFileName, LegDocument.class);
        return this.legService.storeLegDocumentTemporary(legDocument);
    }

    private List<TocItem> getTocITems(Annex annex) {
        structureContextProvider.get().useDocumentTemplate(annex.getMetadata().getOrError(() -> "Annex metadata is required!").getDocTemplate());
        return structureContextProvider.get().getTocItems();
    }

    @Subscribe
    void declineRevision(DeclineRevisionDocumentEvent event) {
        cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);
        final Annex revision = annexService.findAnnex(event.getContributionVO().getDocumentId(), false);
        Map<String, Object> properties = new HashMap<>();
        String contributionStatus = event.getContributionVO().getContributionStatus().getValue();
        properties.put(CmisProperties.CONTRIBUTION_STATUS.getId(), contributionStatus);
        Annex updatedAnnex = annexService.updateAnnex(revision.getId(), properties, false);
        if(updatedAnnex != null) {
            annexScreen.disableMergePane();
            event.getSelectedItem().setVisible(false);
        } else {
            throw new RuntimeException("Unable to update document revision status");
        }
    }

    void markRevisionAsProcessed(String revisionDocumentId, String msgKey) {
        cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);
        final Annex revision = annexService.findAnnex(revisionDocumentId, false);

        annexScreen.disableMergePane();
        Map<String, Object> properties = new HashMap<>();
        properties.put(CmisProperties.CONTRIBUTION_STATUS.getId(), ContributionVO.ContributionStatus.CONTRIBUTION_DONE.getValue());
        annexService.updateAnnex(revision.getId(), properties, false);
        final List<ContributionVO> allContributions = contributionService.getDocumentContributions(documentId, getDocument().getMetadata().get().getIndex(), Annex.class);
        annexScreen.populateContributions(allContributions);
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
        cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);
        String mergeActionKey = "contribution.merge.action.accepted.notification";
        Annex annex = getDocument();
        List<TocItem> tocItemList = getTocITems(annex);
        byte[] xmlClonedContent = event.getMergeActionVOS().get(0).getContributionVO().getXmlContent();
        List<InternalRefMap> intRefMap = getInternalRefMaps(event, annex, xmlClonedContent);

        byte[] xmlContent = mergeContributionHelper.updateDocumentWithContributions(event, annex, tocItemList, intRefMap);
        AnnexStructureType annexStructureType = getStructureType();
        switch (annexStructureType) {
            case ARTICLE:
                xmlContent = numberService.renumberArticles(xmlContent);
                break;
            case LEVEL:
                xmlContent = numberService.renumberLevel(xmlContent);
                xmlContent = numberService.renumberParagraph(xmlContent);
                break;
        }
        xmlContent = xmlContentProcessor.doXMLPostProcessing(xmlContent);
        updateAnnexContent(annex, xmlContent, messageHelper.getMessage("contribution.merge.operation.message"), mergeActionKey);
        if(event.isAllContributions()) {
            markRevisionAsProcessed(event.getMergeActionVOS().get(0).getContributionVO().getDocumentId(), "contribution.accept.all.notification.message");
        } else {
            eventBus.post(new RefreshContributionEvent());
        }
    }


    private List<InternalRefMap> getInternalRefMaps(ApplyContributionsRequestEvent event, Annex annex, byte[] xmlClonedContent) {
        byte[] xmlContent = annex.getContent().get().getSource().getBytes();
        Map<String, String> attachmentsClonedContent = attachmentProcessor.getAttachmentsHrefFromBill(xmlClonedContent);
        List<InternalRefMap> map = new ArrayList<>();
        String ref = annex.getName().replace(".xml", "");
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

    private void updateAnnexContent(Annex annex, byte[] xmlContent, String operationMsg, String notificationMsg) {
        annex = annexService.updateAnnex(annex, xmlContent, operationMsg);
        if (annex != null) {
            eventBus.post(new NotificationEvent(Type.INFO, notificationMsg));
            eventBus.post(new RefreshDocumentEvent());
            eventBus.post(new DocumentUpdatedEvent());
            leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
        }
    }

    @Subscribe
    void showCleanVersion(ShowCleanVersionRequestEvent event) {
        final Annex annex = getDocument();
        final String versionContent = documentContentService.getCleanDocumentAsHtml(annex,
                urlBuilder.getWebAppPath(VaadinServletService.getCurrentServletRequest()),
                securityContext.getPermissions(annex));
        final String versionInfo = getVersionInfoAsString(annex);
        annexScreen.showCleanVersion(versionContent, versionInfo);
    }

    @Subscribe
    void refreshCleanVersion(DocumentUpdatedEvent event) {
        if (annexScreen.isCleanVersionVisible()) {
            showCleanVersion(new ShowCleanVersionRequestEvent());
        }
    }

    @Subscribe
    public void fetchMilestoneByVersionedReference(FetchMilestoneByVersionedReferenceEvent event) {
        LeosPackage leosPackage = packageService.findPackageByDocumentId(documentId);
        LegDocument legDocument = legService.findLastLegByVersionedReference(leosPackage.getPath(), event.getVersionedReference());
        milestoneExplorerOpened = true;
        annexScreen.showMilestoneExplorer(legDocument, String.join(",", legDocument.getMilestoneComments()), proposalRef);
    }

    @Subscribe
    void getCompareContentForTimeLine(CompareTimeLineRequestEvent event) {
        final Annex oldVersion = annexService.findAnnexVersion(event.getOldVersion());
        final Annex newVersion = annexService.findAnnexVersion(event.getNewVersion());
        final ComparisonDisplayMode displayMode = event.getDisplayMode();
        HashMap<ComparisonDisplayMode, Object> result = comparisonDelegate.versionCompare(oldVersion, newVersion, displayMode);
        annexScreen.displayComparison(result);
    }

    @Subscribe
    void compare(CompareRequestEvent event) {
        cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);
        final Annex oldVersion = annexService.findAnnexVersion(event.getOldVersionId());
        final Annex newVersion = annexService.findAnnexVersion(event.getNewVersionId());
        String comparedContent = comparisonDelegate.getMarkedContent(oldVersion, newVersion);
        final String comparedInfo = messageHelper.getMessage("version.compare.simple", oldVersion.getVersionLabel(), newVersion.getVersionLabel());
        annexScreen.populateComparisonContent(comparedContent, comparedInfo, oldVersion, newVersion);
    }

    @Subscribe
    void doubleCompare(DoubleCompareRequestEvent event) {
        final Annex original = annexService.findAnnexVersion(event.getOriginalProposalId());
        final Annex intermediate = annexService.findAnnexVersion(event.getIntermediateMajorId());
        final Annex current = annexService.findAnnexVersion(event.getCurrentId());
        String resultContent = comparisonDelegate.doubleCompareHtmlContents(original, intermediate, current, true);
        final String comparedInfo = messageHelper.getMessage("version.compare.double", original.getVersionLabel(), intermediate.getVersionLabel(), current.getVersionLabel());
        annexScreen.populateDoubleComparisonContent(resultContent, comparedInfo, original, intermediate, current);
    }

    @Subscribe
    void versionRestore(RestoreVersionRequestEvent event) {
        final Annex version = annexService.findAnnexVersion(event.getVersionId());
        final byte[] resultXmlContent = getContent(version);
        annexService.updateAnnex(getDocument(), resultXmlContent, VersionType.MINOR, messageHelper.getMessage("operation.restore.version", version.getVersionLabel()));

        List<Annex> documentVersions = annexService.findVersions(documentId);
        annexScreen.updateTimeLineWindow(documentVersions);
        eventBus.post(new RefreshDocumentEvent());
        eventBus.post(new DocumentUpdatedEvent()); //Document might be updated.
        leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
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
    public void changeComparisonMode(ComparisonEvent event) {
        comparisonMode = event.isComparsionMode();
        if (comparisonMode) {
            annexScreen.cleanComparedContent();
            if (!annexScreen.isComparisonComponentVisible()) {
                LayoutChangeRequestEvent layoutEvent = new LayoutChangeRequestEvent(ColumnPosition.DEFAULT, ComparisonComponent.class, null);
                eventBus.post(layoutEvent);
            }
        } else {
            LayoutChangeRequestEvent layoutEvent = new LayoutChangeRequestEvent(ColumnPosition.OFF, ComparisonComponent.class, null);
            eventBus.post(layoutEvent);
            eventBus.post(new ResetRevisionComponentEvent());
        }
        updateVersionsTab(new DocumentUpdatedEvent());
    }

    @Subscribe
    public void showIntermediateVersionWindow(ShowIntermediateVersionWindowEvent event) {
        annexScreen.showIntermediateVersionWindow();
    }

    @Subscribe
    public void saveIntermediateVersion(SaveIntermediateVersionEvent event) {
        final Annex annex = annexService.createVersion(documentId, event.getVersionType(), event.getCheckinComment());
        setDocumentData(annex);
        eventBus.post(new NotificationEvent(NotificationEvent.Type.INFO, "document.major.version.saved"));
        eventBus.post(new DocumentUpdatedEvent());
        leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, annex.getVersionSeriesId(), id));
        populateViewData(annex, TocMode.SIMPLIFIED);
    }

    @Subscribe
    void saveToc(SaveTocRequestEvent event) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Annex annex = getDocument();
        AnnexStructureType structureType = getStructureType();
        annex = annexService.saveTableOfContent(annex, event.getTableOfContentItemVOs(), structureType, messageHelper.getMessage("operation.toc.updated"), user);

        eventBus.post(new NotificationEvent(Type.INFO, "toc.edit.saved"));
        eventBus.post(new DocumentUpdatedEvent());
        leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
        updateInternalReferencesProducer.send(new UpdateInternalReferencesMessage(annex.getId(), annex.getMetadata().get().getRef(), id));
        LOG.info("Toc saved in Annex {} id {}, in {} milliseconds ({} sec)", annex.getName(), annex.getId(), stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
    }

    private AnnexStructureType getStructureType() {
        List<TocItem> tocItems = structureContextProvider.get().getTocItems().stream().
                filter(tocItem -> (tocItem.getAknTag().value().equalsIgnoreCase(AnnexStructureType.LEVEL.getType()) ||
                        tocItem.getAknTag().value().equalsIgnoreCase(ARTICLE.getType()))).collect(Collectors.toList());
        return AnnexStructureType.valueOf(tocItems.get(0).getAknTag().value().toUpperCase());
    }

    @Subscribe
    public void getUserPermissions(FetchUserPermissionsRequest event) {
        Annex annex = getDocument();
        List<LeosPermission> userPermissions = securityContext.getPermissions(annex);
        annexScreen.sendUserPermissions(userPermissions);
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
        Annex annex = getDocument();
        metadata.setVersion(annex.getVersionLabel());
        metadata.setId(annex.getId());
        metadata.setTitle(annex.getTitle());
        eventBus.post(new DocumentMetadataResponse(metadata));
    }

    @Subscribe
    void mergeSuggestion(MergeSuggestionRequest event) {
        Annex document = getDocument();
        byte[] resultXmlContent = elementProcessor.replaceTextInElement(document, event.getOrigText(), event.getNewText(), event.getElementId(), event.getStartOffset(), event.getEndOffset());
        if (resultXmlContent == null) {
            eventBus.post(new MergeSuggestionResponse(messageHelper.getMessage("document.merge.suggestion.failed"), MergeSuggestionResponse.Result.ERROR));
            return;
        }
        document = annexService.updateAnnex(document, resultXmlContent, VersionType.MINOR, messageHelper.getMessage("operation.merge.suggestion"));
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
    void mergeSuggestions(MergeSuggestionsRequest event) {
        Annex annex = getDocument();
        commonDelegate.mergeSuggestions(annex, event, elementProcessor, annexService::updateAnnex);
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

    private DocumentVO createAnnexVO(Annex annex) {
        DocumentVO annexVO =
                new DocumentVO(annex.getId(),
                        annex.getMetadata().exists(m -> m.getLanguage() != null) ? annex.getMetadata().get().getLanguage() : "EN",
                        LeosCategory.ANNEX,
                        annex.getLastModifiedBy(),
                        Date.from(annex.getLastModificationInstant()));

        if (annex.getMetadata().isDefined()) {
            AnnexMetadata metadata = annex.getMetadata().get();
            annexVO.setDocNumber(metadata.getIndex());
            annexVO.setTitle(metadata.getTitle());
            annexVO.getMetadata().setInternalRef(metadata.getRef());
        }
        if(!annex.getCollaborators().isEmpty()) {
            annexVO.addCollaborators(annex.getCollaborators());
        }
        return annexVO;
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
            LOG.debug("Annex Presenter updated the edit info -" + updateUserInfoEvent.getActionInfo().getOperation().name());
            annexScreen.updateUserCoEditionInfo(updateUserInfoEvent.getActionInfo().getCoEditionVos(), id);
        }
    }

    private boolean isCurrentInfoId(String versionSeriesId) {
        return versionSeriesId.equals(strDocumentVersionSeriesId);
    }

    @Subscribe
    public void documentUpdatedByCoEditor(DocumentUpdatedByCoEditorEvent documentUpdatedByCoEditorEvent) {
        if (isCurrentInfoId(documentUpdatedByCoEditorEvent.getDocumentId()) &&
                !id.equals(documentUpdatedByCoEditorEvent.getPresenterId())) {
            eventBus.post(new NotificationEvent(leosUI, "coedition.caption", "coedition.operation.update", NotificationEvent.Type.TRAY,
                    documentUpdatedByCoEditorEvent.getUser().getName()));
            annexScreen.displayDocumentUpdatedByCoEditorWarning();
        }
    }

    @Subscribe
    void editInlineToc(InlineTocEditRequestEvent event) {
        Annex annex = getDocument();
        coEditionHelper.storeUserEditInfo(httpSession.getId(), id, user, strDocumentVersionSeriesId, null, InfoType.TOC_INFO);
        annexScreen.enableTocEdition(getTableOfContent(annex, TocMode.NOT_SIMPLIFIED));
    }

    @Subscribe
    void closeInlineToc(InlineTocCloseRequestEvent event) {
        coEditionHelper.removeUserEditInfo(id, strDocumentVersionSeriesId, null, InfoType.TOC_INFO);
        LOG.debug("User edit information removed");
    }

    @Subscribe
    void fetchTocAndAncestors(FetchCrossRefTocRequestEvent event) {
        Annex annex = getDocument();
        List<String> elementAncestorsIds = null;
        if (event.getElementIds() != null && event.getElementIds().size() > 0) {
            try {
                elementAncestorsIds = annexService.getAncestorsIdsForElementId(annex, event.getElementIds());
            } catch (Exception e) {
                LOG.warn("Could not get ancestors Ids", e);
            }
        }
        // we are combining two operations (get toc + get selected element ancestors)
        final Map<String, List<TableOfContentItemVO>> tocItemList = packageService.getTableOfContent(annex.getId(), TocMode.SIMPLIFIED_CLEAN);
        eventBus.post(new FetchCrossRefTocResponseEvent(new TocAndAncestorsVO(tocItemList, elementAncestorsIds, messageHelper, structureContextProvider.get().getNumberingConfigs())));
    }

    @Subscribe
    void fetchElement(FetchElementRequestEvent event) {
        XmlDocument document = workspaceService.findDocumentByRef(event.getDocumentRef(), XmlDocument.class);
        String contentForType = elementProcessor.getElement(document, event.getElementTagName(), event.getElementId());
        String wrappedContentXml = wrapXmlFragment(contentForType != null ? contentForType : "");
        InputStream contentStream = new ByteArrayInputStream(wrappedContentXml.getBytes(StandardCharsets.UTF_8));
        contentForType = transformationService.toXmlFragmentWrapper(contentStream, urlBuilder.getWebAppPath(VaadinServletService.getCurrentServletRequest()),
                securityContext.getPermissions(document));

        eventBus.post(new FetchElementResponseEvent(event.getElementId(), event.getElementTagName(), contentForType, event.getDocumentRef()));
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
        eventBus.post(new ReferenceLabelResponseEvent(updatedLabel.get(), event.getDocumentRef()));
    }


    @Subscribe
    void structureChangeHandler(AnnexStructureChangeEvent event) {
        AnnexContext annexContext = annexContextProvider.get();
        String elementType = event.getStructureType().getType();
        String template = cfgHelper.getProperty("leos.annex." + elementType + ".template");
        structureContextProvider.get().useDocumentTemplate(template);
        annexContext.useTemplate(template);
        annexContext.useAnnexId(documentId);
        annexContext.useActionMessage(ContextAction.ANNEX_STRUCTURE_UPDATED, messageHelper.getMessage("operation.annex.switch."+ elementType +".structure"));
        annexContext.executeUpdateAnnexStructure();
        refreshView(elementType);
    }

    private void refreshView(String elementType) {
        eventBus.post(new NavigationRequestEvent(Target.ANNEX, getDocumentRef()));
        eventBus.post(new NotificationEvent(Type.INFO, "annex.structure.changed.message." + elementType));
    }

    @Subscribe
    void searchTextInDocument(SearchTextRequestEvent event) {
        Annex annex = (Annex) httpSession.getAttribute("annex#" + getDocumentRef());
        if (annex == null) {
            annex = getDocument();
        }
        List<SearchMatchVO> matches = Collections.emptyList();
        try {
            matches = searchService.searchText(getContent(annex), event.getSearchText(), event.matchCase, event.completeWords);
        } catch (Exception e) {
            eventBus.post(new NotificationEvent(Type.ERROR, "Error while searching{1}", e.getMessage()));
        }

        //Do we reset session etc if there was partial replace earlier.
        annexScreen.showMatchResults(event.searchID, matches);
    }

    @Subscribe
    void saveAndCloseAfterReplace(SaveAndCloseAfterReplaceEvent event){
        // save document into repository
        Annex annex = getDocument();

        Annex annexFromSession = (Annex) httpSession.getAttribute("annex#" + getDocumentRef());
        httpSession.removeAttribute("annex#" + getDocumentRef());

        annex = annexService.updateAnnex(annex, annexFromSession.getContent().get().getSource().getBytes(),
                VersionType.MINOR, messageHelper.getMessage("operation.search.replace.updated"));
        if (annex != null) {
            eventBus.post(new RefreshDocumentEvent());
            eventBus.post(new DocumentUpdatedEvent());
            leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
            eventBus.post(new NotificationEvent(Type.INFO, "document.replace.success"));
        }
    }

    @Subscribe
    void replaceAllTextInDocument(ReplaceAllMatchRequestEvent event) {
        Annex annexFromSession = getAnnexFromSession();
        if (annexFromSession == null) {
            annexFromSession = getDocument();
        }

        byte[] updatedContent = searchService.replaceText(
                getContent(annexFromSession),
                event.getSearchText(),
                event.getReplaceText(),
                event.getSearchMatchVOs());

        Annex annexUpdated = copyIntoNew(annexFromSession, updatedContent);
        httpSession.setAttribute("annex#" + getDocumentRef(), annexUpdated);
        annexScreen.setContent(getEditableXml(annexUpdated));
        eventBus.post(new ReplaceAllMatchResponseEvent(true));
    }

    private Annex copyIntoNew(Annex source, byte[] updatedContent) {
        Content contentFromSession = source.getContent().get();
        Content.Source updatedSource = new SourceImpl(new ByteArrayInputStream(updatedContent));
        Content contentObj = new ContentImpl(
                contentFromSession.getFileName(),
                contentFromSession.getMimeType(),
                updatedContent.length,
                updatedSource
        );
        Option<Content> updatedContentOptionObj = Option.option(contentObj);
        return new Annex(
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
                source.getBaseRevisionId(),
                source.getContributionStatus(),
                source.getClonedFrom(),
                updatedContentOptionObj,
                source.getMetadata()
        );
    }

    @Subscribe
    void saveAfterReplace(SaveAfterReplaceEvent event){
        // save document into repository
        Annex annex = getDocument();

        Annex annexFromSession = (Annex) httpSession.getAttribute("annex#" + getDocumentRef());

        annex = annexService.updateAnnex(annex, annexFromSession.getContent().get().getSource().getBytes(),
                VersionType.MINOR, messageHelper.getMessage("operation.search.replace.updated"));
        if (annex != null) {
            httpSession.setAttribute("annex#"+getDocumentRef(), annex);
            eventBus.post(new DocumentUpdatedEvent());
            leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
            eventBus.post(new NotificationEvent(Type.INFO, "document.replace.success"));
        }
    }

    @Subscribe
    void replaceOneTextInDocument(ReplaceMatchRequestEvent event) {
        if (event.getSearchMatchVO().isReplaceable()) {
            Annex annexFromSession = getAnnexFromSession();
            if (annexFromSession == null) {
                annexFromSession = getDocument();
            }

            byte[] updatedContent = searchService.replaceText(
                    getContent(annexFromSession),
                    event.getSearchText(),
                    event.getReplaceText(),
                    Arrays.asList(event.getSearchMatchVO()));

            Annex annexUpdated = copyIntoNew(annexFromSession, updatedContent);
            httpSession.setAttribute("annex#" + getDocumentRef(), annexUpdated);
            annexScreen.setContent(getEditableXml(annexUpdated));
            annexScreen.refineSearch(event.getSearchId(), event.getMatchIndex(), true);
        } else {
            annexScreen.refineSearch(event.getSearchId(), event.getMatchIndex(), false);
        }
    }

    @Subscribe
    void closeSearchBar(SearchBarClosedEvent event) {
        //Cleanup the session etc
        annexScreen.closeSearchBar();
        httpSession.removeAttribute("annex#"+getDocumentRef());
        eventBus.post(new RefreshDocumentEvent());
    }

    @Subscribe
    void changeBaseVersion(ChangeBaseVersionEvent event) {
        String documentId = event.getVersionId();
        String versionLabel = event.getVersionLabel();
        String versionTitle = event.getBaseVersionTitle();
        Map<String, Object> properties = new HashMap<>();
        properties.put(CmisProperties.BASE_REVISION_ID.getId(), documentId + CMIS_PROPERTY_SPLITTER + versionLabel + CMIS_PROPERTY_SPLITTER + versionTitle);
        Annex updatedAnnex = annexService.updateAnnex(documentId, properties, true);
        VersionInfoVO versionInfoVO = getVersionInfo(updatedAnnex);
        versionInfoVO.setRevisedBaseVersion(versionLabel);
        versionInfoVO.setBaseVersionTitle(event.getBaseVersionTitle());
        annexScreen.setContent(getEditableXml(updatedAnnex));
        annexScreen.setDocumentVersionInfo(versionInfoVO);
        eventBus.post(new NotificationEvent(NotificationEvent.Type.INFO, "document.base.version.changed.info",
                versionLabel));
    }

    @Subscribe
    public void afterClosedWindow(WindowClosedEvent<MilestoneExplorer> windowClosedEvent) {
        milestoneExplorerOpened = false;
    }

    private boolean isClonedProposal() {
        cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);
        return cloneContext != null && cloneContext.isClonedProposal();
    }
}
