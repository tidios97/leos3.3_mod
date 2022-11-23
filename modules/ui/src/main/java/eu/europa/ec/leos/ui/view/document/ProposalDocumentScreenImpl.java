package eu.europa.ec.leos.ui.view.document;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.server.StreamResource;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.document.Bill;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.model.action.ContributionVO;
import eu.europa.ec.leos.security.LeosPermissionAuthorityMapHelper;
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
import eu.europa.ec.leos.ui.component.contributions.ContributionsTab;
import eu.europa.ec.leos.ui.component.markedText.MarkedTextComponent;
import eu.europa.ec.leos.ui.component.revision.RevisionComponent;
import eu.europa.ec.leos.ui.component.toc.TocEditor;
import eu.europa.ec.leos.ui.component.versions.VersionsTab;
import eu.europa.ec.leos.ui.extension.AnnotateExtension;
import eu.europa.ec.leos.ui.extension.SoftActionsExtension;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItem;
import eu.europa.ec.leos.web.event.NotificationEvent;
import eu.europa.ec.leos.web.event.component.LayoutChangeRequestEvent;
import eu.europa.ec.leos.web.event.component.ResetRevisionComponentEvent;
import eu.europa.ec.leos.web.event.view.AddChangeDetailsMenuEvent;
import eu.europa.ec.leos.web.event.view.document.CreateEventParameter;
import eu.europa.ec.leos.web.event.view.document.InstanceTypeResolver;
import eu.europa.ec.leos.web.support.LeosCacheToken;
import eu.europa.ec.leos.web.support.cfg.ConfigurationHelper;
import eu.europa.ec.leos.web.support.user.UserHelper;
import eu.europa.ec.leos.web.support.xml.DownloadStreamResource;
import eu.europa.ec.leos.web.ui.component.MenuBarComponent;
import eu.europa.ec.leos.web.ui.component.actions.LegalTextActionsMenuBar;
import eu.europa.ec.leos.web.ui.screen.document.ColumnPosition;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
@StyleSheet({"vaadin://../assets/css/bill.css" + LeosCacheToken.TOKEN})
// css not present in VAADIN folder so not published by server. Anything not published need to be served by static servlet.
public class ProposalDocumentScreenImpl extends DocumentScreenImpl {
    private static final long serialVersionUID = 3983015438446410548L;

    private static final Logger LOG = LoggerFactory.getLogger(ProposalDocumentScreenImpl.class);

    private MarkedTextComponent<Bill> markedTextComponent;
    private RevisionComponent<Bill> revisionComponent;
    private CloneContext cloneContext;

    @Autowired
    ProposalDocumentScreenImpl(UserHelper userHelper, SecurityContext securityContext, CloneContext cloneContext, EventBus eventBus, ConfigurationHelper cfgHelper,
                               MessageHelper messageHelper, TocEditor tocEditor, InstanceTypeResolver instanceTypeResolver,
                               MenuBarComponent menuBarComponent, LeosPermissionAuthorityMapHelper authorityMapHelper, LegalTextActionsMenuBar legalTextActionMenuBar,
                               ComparisonComponent<Bill> comparisonComponent, VersionsTab<Bill> versionsTab, ContributionsTab<Bill> contributionsTab, Provider<StructureContext> structureContextProvider,
                               MarkedTextComponent<Bill> markedTextComponent, TableOfContentProcessor tableOfContentProcessor, XmlContentProcessor xmlContentProcessor) {
        super(userHelper, securityContext, eventBus, cfgHelper, messageHelper, tocEditor, instanceTypeResolver, menuBarComponent, authorityMapHelper,
                legalTextActionMenuBar, comparisonComponent, versionsTab, contributionsTab, structureContextProvider,
                tableOfContentProcessor, xmlContentProcessor);
        ExportOptions exportOptions = new ExportLW(ExportOptions.Output.PDF, Bill.class, false);
        markedTextComponent.setExportOptions(exportOptions);
        this.markedTextComponent = markedTextComponent;
        this.cloneContext = cloneContext;
        init();
    }
    
    @Override
    public void init() {
        super.init();
        buildDocumentPane();
        legalTextActionMenuBar.setChildComponentClass(MarkedTextComponent.class);
        legalTextPaneComponent.addPaneToLayout(comparisonComponent, 2, false);
        legalTextPaneComponent.layoutChildComponents();
        legalTextActionMenuBar.setDownloadVersionVisible(true);
        legalTextActionMenuBar.setDownloadVersionWithAnnotationsVisible(true);
        new SoftActionsExtension(legalTextPaneComponent.getContent());
    }

    @Override
    public void refreshContent(final String documentContent) {
        if(isClonedProposal()) {
            eventBus.post(new AddChangeDetailsMenuEvent());
        }
        legalTextPaneComponent.populateContent(documentContent);
    }

    @Override
    public void showElementEditor(final String elementId, final String elementTagName, final String elementFragment, String alternatives) {
        CreateEventParameter eventParameterObjet = new CreateEventParameter(elementId, elementTagName, elementFragment,
                LeosCategory.BILL.name(), securityContext.getUser(),
                authorityMapHelper.getPermissionsForRoles(securityContext.getUser().getRoles()));
        eventParameterObjet.setAlternative(alternatives);
        eventParameterObjet.setCloneProposal(cloneContext.isClonedProposal());
        eventBus.post(instanceTypeResolver.createEvent(eventParameterObjet));
    }

    @Override
    public void enableTocEdition(List<TableOfContentItemVO> tocItemVoList) {
        legalTextPaneComponent.handleTocEditRequestEvent(tocItemVoList, tocEditor);
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
    public void showVersion(String content, String versionInfo){
        changePosition(new LayoutChangeRequestEvent(ColumnPosition.DEFAULT, ComparisonComponent.class, markedTextComponent));
        markedTextComponent.populateMarkedContent(content, LeosCategory.BILL, versionInfo, null);
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
    public void showRevision(String content, ContributionVO contributionVO, List<TocItem> tocItemList) {
        initRevisionComponent();
        revisionComponent.populateRevisionContent(content, LeosCategory.BILL, contributionVO);
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
    public void showCleanVersion(String content, String versionInfo) {
        changePosition(new LayoutChangeRequestEvent(ColumnPosition.DEFAULT, ComparisonComponent.class, markedTextComponent));
        markedTextComponent.populateMarkedWithCleanContent(content, LeosCategory.BILL, versionInfo);
        markedTextComponent.hideCompareButtons();
    }

    @Override
    public boolean isCleanVersionShowed() {
        return markedTextComponent != null && markedTextComponent.isCleanVersion();
    }

    @Override
    public void setPermissions(DocumentVO bill, boolean isClonedProposal) {
        legalTextPaneComponent.setPermissions(bill, instanceTypeResolver.getInstanceType());
        legalTextActionMenuBar.setDownloadVersionVisible(true);
        legalTextActionMenuBar.setDownloadVersionWithAnnotationsVisible(true);
        legalTextActionMenuBar.setDownloadCleanVersionVisible(isClonedProposal);
        legalTextActionMenuBar.setShowCleanVersionVisible(isClonedProposal);
        legalTextActionMenuBar.setRenumberingVisible(false);
        legalTextActionMenuBar.setRenumberingGroupVisible(false);
    }

    @Override
    public void populateMarkedContent(String comparedContent, String comparedInfo, Bill original, Bill current) {
        ExportVersions<Bill> exportVersions = new ExportVersions<>(original, current);
        changePosition(new LayoutChangeRequestEvent(ColumnPosition.DEFAULT, ComparisonComponent.class, markedTextComponent));
        markedTextComponent.populateMarkedContent(comparedContent, LeosCategory.BILL, comparedInfo, exportVersions);
        markedTextComponent.showCompareButtons();
    }
    
    @Override
    public void cleanComparedContent() {
        final String versionInfo = messageHelper.getMessage("document.compare.version.caption.simple");
        markedTextComponent.populateMarkedContent("", LeosCategory.BILL, versionInfo, null);
        markedTextComponent.hideCompareButtons();
    }
    
    @Override
    public void populateDoubleComparisonContent(String comparedContent, String versionInfo, Bill original, Bill intermediate, Bill current) {
        throw new IllegalArgumentException("Operation not valid");
    }

    @Override
    public void setDownloadStreamResourceForXmlFiles(Bill original, Bill intermediate, Bill current, String language, String comparedInfo,
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
    public void scrollToMarkedChange(String elementId) {
        markedTextComponent.scrollToMarkedChange(elementId);
    }

    @Override
    public void setDownloadStreamResourceForExport(StreamResource streamResource) {
        markedTextComponent.setDownloadStreamResourceForExport(streamResource);
    }

    @Override
    public Optional<ContributionVO> findContributionAndShowTab(String versionedReference) {
        legalTextPaneComponent.selectTab(contributionsTab);
        return contributionsTab.findContribution(versionedReference);
    }

    private boolean isClonedProposal() {
        return cloneContext != null && cloneContext.isClonedProposal();
    }
}
