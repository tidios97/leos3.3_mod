package eu.europa.ec.leos.ui.view.annex;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.StreamResource;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.document.Annex;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.model.action.ContributionVO;
import eu.europa.ec.leos.model.annex.AnnexStructureType;
import eu.europa.ec.leos.model.annex.LevelItemVO;
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
import eu.europa.ec.leos.ui.component.revision.RevisionComponent;
import eu.europa.ec.leos.ui.component.toc.TableOfContentItemConverter;
import eu.europa.ec.leos.ui.component.contributions.ContributionsTab;
import eu.europa.ec.leos.ui.component.versions.VersionsTab;
import eu.europa.ec.leos.ui.event.view.AddStructureChangeMenuEvent;
import eu.europa.ec.leos.ui.component.toc.TocEditor;
import eu.europa.ec.leos.ui.extension.AnnotateExtension;
import eu.europa.ec.leos.ui.extension.SoftActionsExtension;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItem;
import eu.europa.ec.leos.web.event.NotificationEvent;
import eu.europa.ec.leos.web.event.component.LayoutChangeRequestEvent;
import eu.europa.ec.leos.web.event.component.ResetRevisionComponentEvent;
import eu.europa.ec.leos.web.event.view.document.CreateEventParameter;
import eu.europa.ec.leos.web.event.view.document.InstanceTypeResolver;
import eu.europa.ec.leos.web.support.LeosCacheToken;
import eu.europa.ec.leos.web.support.cfg.ConfigurationHelper;
import eu.europa.ec.leos.web.support.user.UserHelper;
import eu.europa.ec.leos.web.support.xml.DownloadStreamResource;
import eu.europa.ec.leos.web.ui.screen.document.ColumnPosition;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.inject.Provider;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ViewScope
@SpringComponent
@Instance(instances = {InstanceType.COMMISSION, InstanceType.OS})
@StyleSheet({"vaadin://../assets/css/annex.css" + LeosCacheToken.TOKEN})
public class ProposalAnnexScreenImpl extends AnnexScreenImpl {
    private static final long serialVersionUID = -6719257516608653344L;

    private static final Logger LOG = LoggerFactory.getLogger(AnnexScreenImpl.class);

    private MarkedTextComponent<Annex> markedTextComponent;
    private RevisionComponent<Annex> revisionComponent;
    protected ContributionsTab<Annex> contributionsTab;
    private CloneContext cloneContext;

    @Value("${leos.coverpage.separated}")
    private boolean coverPageSeparated;

    @Autowired
    ProposalAnnexScreenImpl(MessageHelper messageHelper, EventBus eventBus, SecurityContext securityContext, UserHelper userHelper,
                            ConfigurationHelper cfgHelper, TocEditor numberEditor, InstanceTypeResolver instanceTypeResolver,
                            VersionsTab<Annex> versionsTab, ContributionsTab<Annex> contributionsTab, Provider<StructureContext> structureContextProvider,
                            MarkedTextComponent<Annex> markedTextComponent, TableOfContentProcessor tableOfContentProcessor,
                            CloneContext cloneContext, XmlContentProcessor xmlContentProcessor) {
        super(messageHelper, eventBus, securityContext, userHelper, cfgHelper, numberEditor, instanceTypeResolver,
                versionsTab, structureContextProvider, tableOfContentProcessor, xmlContentProcessor);
        ExportOptions exportOptions = new ExportLW(ExportOptions.Output.PDF, Annex.class, false);
        markedTextComponent.setExportOptions(exportOptions);
        this.markedTextComponent = markedTextComponent;
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
        actionsMenuBar.setDownloadVersionVisible(true);
        actionsMenuBar.setDownloadVersionWithAnnotationsVisible(true);
        new SoftActionsExtension<>(annexContent);
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
        markedTextComponent.populateMarkedContent("", LeosCategory.ANNEX, versionInfo, null);
        markedTextComponent.hideCompareButtons();
    }

    @Override
    public void showRevisionWithSidebar(String versionContent, ContributionVO contributionVO, List<TocItem> tocItemList, String temporaryAnnotationsId) {
        this.showRevision(versionContent, contributionVO, tocItemList);
        final LeosDisplayField revisionContent =  revisionComponent.getRevisionContent();
        final String temporaryDocument = contributionVO.getDocumentName().replace(".xml", "");
        new AnnotateExtension(revisionContent, eventBus, cfgHelper, "leos-revision-content", AnnotateExtension.OperationMode.READ_ONLY,
                ConfigurationHelper.isAnnotateAuthorityEquals(cfgHelper, "LEOS"), true, null,
                null, "revision-01", temporaryAnnotationsId, temporaryDocument);
    }

    @Override
    public void showVersion(String content, String versionInfo) {
        changePosition(new LayoutChangeRequestEvent(ColumnPosition.DEFAULT, ComparisonComponent.class, markedTextComponent));
        markedTextComponent.populateMarkedContent(content, LeosCategory.ANNEX, versionInfo, null);
        markedTextComponent.hideCompareButtons();

    }

    @Override
    public void showRevision(String content, ContributionVO contributionVO, List<TocItem> tocItemList) {
        initRevisionComponent();
        revisionComponent.populateRevisionContent(content, LeosCategory.ANNEX, contributionVO);
        changePosition(new LayoutChangeRequestEvent(ColumnPosition.DEFAULT, ComparisonComponent.class, revisionComponent));
    }

    @Override
    public void showElementEditor(final String elementId, final String elementTagName, final String elementFragment, LevelItemVO levelItemVO) {
        CreateEventParameter eventParameterObject = new CreateEventParameter(elementId, elementTagName, elementFragment, LeosCategory.ANNEX.name(), securityContext.getUser(),
                authorityMapHelper.getPermissionsForRoles(securityContext.getUser().getRoles()));
        eventParameterObject.setLevelItemVo(levelItemVO);
        eventParameterObject.setCloneProposal(cloneContext.isClonedProposal());
        eventBus.post(instanceTypeResolver.createEvent(eventParameterObject));
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
        markedTextComponent.populateMarkedWithCleanContent(content, LeosCategory.ANNEX, versionInfo);
        markedTextComponent.hideCompareButtons();
    }

    @Override
    public boolean isCleanVersionVisible() {
        return markedTextComponent != null && markedTextComponent.isCleanVersion();
    }

    @Override
    public void populateComparisonContent(String comparedContent, String comparedInfo, Annex original, Annex current) {
        ExportVersions<Annex> exportVersions = new ExportVersions<>(original, current);
        changePosition(new LayoutChangeRequestEvent(ColumnPosition.DEFAULT, ComparisonComponent.class, markedTextComponent));
        markedTextComponent.populateMarkedContent(comparedContent, LeosCategory.ANNEX, comparedInfo, exportVersions);
        markedTextComponent.showCompareButtons();
    }

    @Override
    public void populateDoubleComparisonContent(String comparedContent, String versionInfo, Annex original, Annex intermediate, Annex current) {
        throw new IllegalArgumentException("Operation not valid");
    }

    @Override
    public void scrollToMarkedChange(String elementId) {
        markedTextComponent.scrollToMarkedChange(elementId);
    }
    
    @Override
    public void enableTocEdition(List<TableOfContentItemVO> tableOfContent) {
        tableOfContentComponent.handleEditTocRequest(tocEditor);
        tableOfContentComponent.setTableOfContent(TableOfContentItemConverter.buildTocData(tableOfContent));
    }
    
    @Override
    public void setDownloadStreamResourceForExport(StreamResource streamResource) {
        markedTextComponent.setDownloadStreamResourceForExport(streamResource);
    }

    @Override
    public void setStructureChangeMenuItem() {
        AnnexStructureType structureType = getStructureType();
        eventBus.post(new AddStructureChangeMenuEvent(structureType));
    }

    @Override
    public boolean isComparisonComponentVisible() {
        return comparisonComponent != null && comparisonComponent.getParent() != null;
    }

    @Override
    public void setDownloadStreamResourceForMenu(DownloadStreamResource streamResource) {
    }
    
    @Override
    public void setDownloadStreamResourceForXmlFiles(Annex original, Annex intermediate, Annex current, String language, String comparedInfo,
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
    public void setPermissions(DocumentVO annex, boolean isClonedProposal) {
        super.setPermissions(annex, isClonedProposal);
        actionsMenuBar.setDownloadVersionVisible(true);
        actionsMenuBar.setDownloadVersionWithAnnotationsVisible(true);
        actionsMenuBar.setDownloadCleanVersionVisible(isClonedProposal);
        actionsMenuBar.setShowCleanVersionVisible(isClonedProposal);
    }

    @Override
    public void setContributionsData(List<ContributionVO> allContributions) {
        if (!allContributions.isEmpty()) {
            accordion.addTab(contributionsTab, messageHelper.getMessage("document.accordion.contribution"), VaadinIcons.CHEVRON_RIGHT);
            contributionsTab.populateContributionsData(allContributions);
        }
    }

    @Override
    public boolean isCoverPageVisible() {
        return !coverPageSeparated;
    }

    @Override
    public Optional<ContributionVO> findContributionAndShowTab(String versionedReference) {
        accordion.setSelectedTab(contributionsTab);
        return contributionsTab.findContribution(versionedReference);
    }

    private boolean isClonedProposal() {
        return cloneContext != null && cloneContext.isClonedProposal();
    }
}
