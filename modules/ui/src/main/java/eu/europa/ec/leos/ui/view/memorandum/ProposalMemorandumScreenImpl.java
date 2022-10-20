package eu.europa.ec.leos.ui.view.memorandum;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.StreamResource;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.document.Memorandum;
import eu.europa.ec.leos.domain.cmis.metadata.LeosMetadata;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.domain.vo.CloneProposalMetadataVO;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.model.action.ContributionVO;
import eu.europa.ec.leos.security.LeosPermission;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.clone.CloneContext;
import eu.europa.ec.leos.services.export.ExportLW;
import eu.europa.ec.leos.services.export.ExportOptions;
import eu.europa.ec.leos.services.export.ExportVersions;
import eu.europa.ec.leos.services.export.ZipPackageUtil;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.processor.content.TableOfContentProcessor;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.ui.component.ComparisonComponent;
import eu.europa.ec.leos.ui.component.LeosDisplayField;
import eu.europa.ec.leos.ui.component.markedText.MarkedTextComponent;
import eu.europa.ec.leos.ui.component.contributions.ContributionsTab;
import eu.europa.ec.leos.ui.component.revision.RevisionComponent;
import eu.europa.ec.leos.ui.component.versions.VersionComparator;
import eu.europa.ec.leos.ui.component.versions.VersionsTab;
import eu.europa.ec.leos.ui.event.InitLeosEditorEvent;
import eu.europa.ec.leos.ui.extension.ActionManagerExtension;
import eu.europa.ec.leos.ui.extension.LeosEditorExtension;
import eu.europa.ec.leos.ui.component.toc.TocEditor;
import eu.europa.ec.leos.vo.toc.TocItem;
import eu.europa.ec.leos.web.event.NotificationEvent;
import eu.europa.ec.leos.web.event.component.LayoutChangeRequestEvent;
import eu.europa.ec.leos.web.event.component.ResetRevisionComponentEvent;
import eu.europa.ec.leos.web.event.view.AddChangeDetailsMenuEvent;
import eu.europa.ec.leos.web.event.view.document.CreateEventParameter;
import eu.europa.ec.leos.web.event.view.document.FetchUserPermissionsResponse;
import eu.europa.ec.leos.web.event.view.document.InstanceTypeResolver;
import eu.europa.ec.leos.web.support.cfg.ConfigurationHelper;
import eu.europa.ec.leos.web.support.user.UserHelper;
import eu.europa.ec.leos.web.support.xml.DownloadStreamResource;
import eu.europa.ec.leos.web.ui.screen.document.ColumnPosition;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Provider;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ViewScope
@SpringComponent
@Instance(instances = {InstanceType.COMMISSION, InstanceType.OS})
class ProposalMemorandumScreenImpl extends MemorandumScreenImpl {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(ProposalMemorandumScreenImpl.class);

    protected LeosEditorExtension<LeosDisplayField> leosEditorExtension;
    protected ActionManagerExtension<LeosDisplayField> actionManagerExtension;
    protected MarkedTextComponent<Memorandum> markedTextComponent;
    protected RevisionComponent<Memorandum> revisionComponent;
    protected ContributionsTab<Memorandum> contributionsTab;
    private CloneContext cloneContext;

    @Autowired
    ProposalMemorandumScreenImpl(SecurityContext securityContext, EventBus eventBus, MessageHelper messageHelper, ConfigurationHelper cfgHelper,
                                 UserHelper userHelper, TocEditor tocEditor, InstanceTypeResolver editElementResponseEventCreator,
                                 VersionsTab<Memorandum> versionsTab, ContributionsTab<Memorandum> contributionsTab,
                                 Provider<StructureContext> structureContextProvider, VersionComparator versionComparator,
                                 MarkedTextComponent<Memorandum> markedTextComponent, TableOfContentProcessor tableOfContentProcessor,
                                 CloneContext cloneContext, XmlContentProcessor xmlContentProcessor) {
        super(securityContext, eventBus, messageHelper, cfgHelper, userHelper, tocEditor, editElementResponseEventCreator, versionsTab,
                structureContextProvider, versionComparator, tableOfContentProcessor, xmlContentProcessor);
        ExportOptions exportOptions = new ExportLW(ExportOptions.Output.PDF, Memorandum.class, false);
        markedTextComponent.setExportOptions(exportOptions);
        this.markedTextComponent = markedTextComponent;
        Validate.notNull(contributionsTab, "contributionsTab must not be null!");
        this.contributionsTab = contributionsTab;
        this.cloneContext = cloneContext;
        init();
    }
    
    @Override
    public void init() {
        super.init();
        actionsMenuBar.setChildComponentClass(MarkedTextComponent.class);
        screenLayoutHelper.addPane(comparisonComponent, 2, false);
        screenLayoutHelper.layoutComponents();
    }

    @Subscribe
    void changePosition(LayoutChangeRequestEvent event) {
        changeLayout(event, event.getChildComponent() != null ? event.getChildComponent() : markedTextComponent);
    }

    @Subscribe
    void resetRevisionComponent(ResetRevisionComponentEvent event) {
        revisionComponent = null;
    }

    @Override
    public void cleanComparedContent() {
        final String versionInfo = messageHelper.getMessage("document.compare.version.caption.simple");
        markedTextComponent.populateMarkedContent("", LeosCategory.MEMORANDUM, versionInfo, null);
    }

    @Override
    public void showVersion(String content, String versionInfo) {
        changePosition(new LayoutChangeRequestEvent(ColumnPosition.DEFAULT, ComparisonComponent.class, markedTextComponent));
        markedTextComponent.populateMarkedContent(content, LeosCategory.MEMORANDUM, versionInfo, null);
        markedTextComponent.hideCompareButtons();
    }

    @Override
    public void showRevision(String versionContent, String contributionStatus, ContributionVO contributionVO, List<TocItem> tocItemList) {
        initRevisionComponent();
        revisionComponent.populateRevisionContent(versionContent, LeosCategory.MEMORANDUM, contributionStatus, contributionVO);
        changePosition(new LayoutChangeRequestEvent(ColumnPosition.DEFAULT, ComparisonComponent.class, revisionComponent));
    }

    @Override
    public void disableMergePane() {
        initRevisionComponent();
        revisionComponent.disableMergePane();
    }

    private void initRevisionComponent() {
        if (revisionComponent == null) {
            revisionComponent = new RevisionComponent<>(eventBus, messageHelper, securityContext);
        }
    }

    @Override
    public void populateContributions(List<ContributionVO> allContributions) {
        contributionsTab.populateContributionsData(allContributions);
    }

    @Override
    public void showCleanVersion(String content, String versionInfo) {
        changePosition(new LayoutChangeRequestEvent(ColumnPosition.DEFAULT, ComparisonComponent.class, markedTextComponent));
        markedTextComponent.populateMarkedWithCleanContent(content, LeosCategory.MEMORANDUM, versionInfo);
        markedTextComponent.hideCompareButtons();
    }

    @Override
    public boolean isCleanVersionShowed() {
        return markedTextComponent != null && markedTextComponent.isCleanVersion();
    }

    @Override
    public void populateComparisonContent(String comparedContent, String comparedInfo, Memorandum original, Memorandum current) {
        ExportVersions<Memorandum> exportVersions = new ExportVersions<>(original, current);
        changePosition(new LayoutChangeRequestEvent(ColumnPosition.DEFAULT, ComparisonComponent.class, markedTextComponent));
        markedTextComponent.populateMarkedContent(comparedContent, LeosCategory.MEMORANDUM, comparedInfo, exportVersions);
        markedTextComponent.showCompareButtons();
    }

    @Override
    public void populateDoubleComparisonContent(String comparedContent, String versionInfo) {
        throw new IllegalArgumentException("Operation not valid");
    }

    @Override
    public void scrollToMarkedChange(String elementId) {
        markedTextComponent.scrollToMarkedChange(elementId);
    }

    @Override
    public void setPermissions(DocumentVO memorandum, boolean isClonedProposal) {
        boolean enableUpdate = this.securityContext.hasPermission(memorandum, LeosPermission.CAN_UPDATE);
        actionsMenuBar.setSaveVersionVisible(enableUpdate);
        actionsMenuBar.setDownloadVersionVisible(true);
        actionsMenuBar.setDownloadVersionWithAnnotationsVisible(true);
        actionsMenuBar.setDownloadCleanVersionVisible(isClonedProposal);
        actionsMenuBar.setShowCleanVersionVisible(isClonedProposal);
        tableOfContentComponent.setPermissions(false);
        // add extensions only if the user has the permission.
        if(enableUpdate) {
            if(leosEditorExtension == null) {
                eventBus.post(new InitLeosEditorEvent(memorandum));
            }
            if(actionManagerExtension == null) {
                actionManagerExtension = new ActionManagerExtension<>(memorandumContent, instanceTypeResolver.getInstanceType(), eventBus, structureContextProvider.get().getTocItems());
            }
        }
    }

    @Override
    public void initLeosEditor(DocumentVO memorandum, List<LeosMetadata> documentsMetadata) {
        leosEditorExtension = new LeosEditorExtension<>(memorandumContent, eventBus, cfgHelper, structureContextProvider.get().getTocItems(),
                null, null, documentsMetadata, memorandum.getMetadata().getInternalRef());
    }

    @Override
    public void showElementEditor(final String elementId, final String  elementTagName, final String elementFragment) {
        CreateEventParameter eventParameterObject = new CreateEventParameter(elementId, elementTagName, elementFragment, LeosCategory.MEMORANDUM.name(), securityContext.getUser(),
                authorityMapHelper.getPermissionsForRoles(securityContext.getUser().getRoles()));
        eventParameterObject.setCloneProposal(cloneContext.isClonedProposal());
        eventBus.post(instanceTypeResolver.createEvent(eventParameterObject));
    }

    @Override
    public void sendUserPermissions(List<LeosPermission> userPermissions) {
        eventBus.post(new FetchUserPermissionsResponse(userPermissions));
    }
    
    @Override
    public void setDownloadStreamResourceForXmlFiles(Memorandum original, Memorandum intermediate, Memorandum current, String language, String comparedInfo,
            String leosComparedContent, String docuWriteComparedContent) {
        File zipFile = null;
        try {
            final Map<String, Object> contentToZip = new HashMap<>();
            if (intermediate != null) {
                contentToZip.put(intermediate.getMetadata().get().getRef() + "_v" + intermediate.getVersionLabel() + ".xml",
                        intermediate.getContent().get().getSource().getBytes());
            }
            contentToZip.put(current.getMetadata().get().getRef() + "_v" + current.getVersionLabel() + ".xml", current.getContent().get().getSource().getBytes());
            contentToZip.put(original.getMetadata().get().getRef() + "_v" + original.getVersionLabel() + ".xml", original.getContent().get().getSource().getBytes());
            contentToZip.put("comparedContent_leos.xml", leosComparedContent);
            contentToZip.put("comparedContent_docuwrite.xml", docuWriteComparedContent);
            contentToZip.put("comparedContent_export.xml", leosComparedContent);

            final String zipFileName = original.getMetadata().get().getRef().concat("-").concat(comparedInfo).
                    concat(original.getMetadata().get().getLanguage().toLowerCase()).concat(".zip");
            zipFile = ZipPackageUtil.zipFiles(zipFileName, contentToZip, language);

            final byte[] zipBytes = FileUtils.readFileToByteArray(zipFile);
            DownloadStreamResource downloadStreamResource = new DownloadStreamResource(zipFileName, new ByteArrayInputStream(zipBytes));
            markedTextComponent.setDownloadStreamResourceForXmlFiles(downloadStreamResource);
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while setDownloadStreamResourceForXmlFiles", e);
            eventBus.post(new NotificationEvent(NotificationEvent.Type.ERROR, "error.message", e.getMessage()));
        } finally {
            if (zipFile != null) {
                zipFile.delete();
            }
        }
    }

    @Override
    public void setContributionsData(List<ContributionVO> allContributions) {
        if (!allContributions.isEmpty()) {
            accordion.addTab(contributionsTab, messageHelper.getMessage("document.accordion.contribution"), VaadinIcons.CHEVRON_RIGHT);
            contributionsTab.populateContributionsData(allContributions);
        }
    }

    private boolean isClonedProposal() {
        return cloneContext != null && cloneContext.isClonedProposal();
    }
}
