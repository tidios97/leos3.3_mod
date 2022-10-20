package eu.europa.ec.leos.ui.view.annex;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.server.StreamResource;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.document.Annex;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.domain.vo.CloneProposalMetadataVO;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.model.action.VersionVO;
import eu.europa.ec.leos.model.action.ContributionVO;
import eu.europa.ec.leos.model.annex.AnnexStructureType;
import eu.europa.ec.leos.security.LeosPermission;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.export.ExportDW;
import eu.europa.ec.leos.services.export.ExportOptions;
import eu.europa.ec.leos.services.export.ExportVersions;
import eu.europa.ec.leos.services.export.ZipPackageUtil;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.store.PackageService;
import eu.europa.ec.leos.services.processor.content.TableOfContentProcessor;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.ui.component.ComparisonComponent;
import eu.europa.ec.leos.ui.component.doubleCompare.DoubleComparisonComponent;
import eu.europa.ec.leos.ui.component.toc.TableOfContentItemConverter;
import eu.europa.ec.leos.ui.component.toc.TocEditor;
import eu.europa.ec.leos.ui.component.versions.VersionsTab;
import eu.europa.ec.leos.ui.event.view.AddStructureChangeMenuEvent;
import eu.europa.ec.leos.ui.extension.SoftActionsExtension;
import eu.europa.ec.leos.ui.view.TriFunction;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItem;
import eu.europa.ec.leos.web.event.NotificationEvent;
import eu.europa.ec.leos.web.event.component.LayoutChangeRequestEvent;
import eu.europa.ec.leos.web.event.view.AddChangeDetailsMenuEvent;
import eu.europa.ec.leos.web.event.view.document.InstanceTypeResolver;
import eu.europa.ec.leos.web.support.LeosCacheToken;
import eu.europa.ec.leos.web.support.cfg.ConfigurationHelper;
import eu.europa.ec.leos.web.support.user.UserHelper;
import eu.europa.ec.leos.web.support.xml.DownloadStreamResource;
import eu.europa.ec.leos.web.ui.screen.document.ColumnPosition;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@SpringComponent
@ViewScope
@Instance(InstanceType.COUNCIL)
@StyleSheet({"vaadin://../assets/css/annex.css" + LeosCacheToken.TOKEN})
@StyleSheet({"vaadin://../assets/css/annex-wo-diffing-style.css" + LeosCacheToken.TOKEN})
public class MandateAnnexScreenImpl extends AnnexScreenImpl {
    private static final long serialVersionUID = 934198605326069948L;

    private static final Logger LOG = LoggerFactory.getLogger(MandateAnnexScreenImpl.class);

    private final DoubleComparisonComponent<Annex> doubleComparisonComponent;
    
    MandateAnnexScreenImpl(MessageHelper messageHelper, EventBus eventBus, SecurityContext securityContext, UserHelper userHelper,
                           ConfigurationHelper cfgHelper, TocEditor numberEditor, InstanceTypeResolver instanceTypeResolver,
                           VersionsTab<Annex> versionsTab, Provider<StructureContext> structureContextProvider,
                           TableOfContentProcessor tableOfContentProcessor,
                           XmlContentProcessor xmlContentProcessor) {
        super(messageHelper, eventBus, securityContext, userHelper, cfgHelper, numberEditor, instanceTypeResolver,
                versionsTab, structureContextProvider, tableOfContentProcessor, xmlContentProcessor);
        ExportOptions exportOptions = new ExportDW(ExportOptions.Output.WORD, Annex.class, false);
        doubleComparisonComponent = new DoubleComparisonComponent<>(exportOptions, eventBus, messageHelper, securityContext);
        init();
    }

    @Override
    public void init() {
        super.init();
        actionsMenuBar.setChildComponentClass(DoubleComparisonComponent.class);
        screenLayoutHelper.addPane(comparisonComponent, 2, false);
        screenLayoutHelper.layoutComponents();
        new SoftActionsExtension<>(annexContent);
    }

    @Subscribe
    void changePosition(LayoutChangeRequestEvent event) {
        changeLayout(event, event.getChildComponent() != null ? event.getChildComponent() : doubleComparisonComponent);
    }

    @Override
    public void populateComparisonContent(String comparedContent, String comparedInfo, Annex original, Annex current) {
        ExportVersions<Annex> exportVersions = new ExportVersions<>(original, current);
        doubleComparisonComponent.populateMarkedContent(comparedContent, LeosCategory.ANNEX, comparedInfo, exportVersions);
        doubleComparisonComponent.setSimpleComparison();
    }
    
    @Override
    public void populateDoubleComparisonContent(String comparedContent, String comparedInfo, Annex original, Annex intermediate, Annex current) {
        ExportVersions<Annex> exportVersions = new ExportVersions<>(original, intermediate, current);
        doubleComparisonComponent.populateDoubleComparisonContent(comparedContent, LeosCategory.ANNEX, comparedInfo, exportVersions);
        doubleComparisonComponent.setDoubleComparison();
    }
    
    @Override
    public void showVersion(String content, String versionInfo) {
        changePosition(new LayoutChangeRequestEvent(ColumnPosition.DEFAULT, ComparisonComponent.class, doubleComparisonComponent));
        doubleComparisonComponent.populateDoubleComparisonContent(content.replaceAll("(?i) id=\"", " id=\"doubleCompare-"), LeosCategory.ANNEX, versionInfo, null);
        doubleComparisonComponent.removeComparison();
    }

    @Override
    public void showRevision(String content, String contributionStatus, ContributionVO contributionVO, List<TocItem> tocItemList){
        throw new IllegalArgumentException("Operation not valid");
    }

    @Override
    public void disableMergePane() {
        throw new IllegalArgumentException("Operation not valid");
    }

    @Override
    public void populateContributions(List<ContributionVO> allContributions) {
        throw new IllegalArgumentException("Operation not valid");
    }

    @Override
    public void showCleanVersion(String content, String versionInfo) {
        throw new IllegalArgumentException("Operation not valid");
    }

    @Override
    public void setDataFunctions(DocumentVO annexVO, List<VersionVO> allVersions,
                                 List<ContributionVO> allContributions,
                                 BiFunction<Integer, Integer, List<Annex>> majorVersionsFn, Supplier<Integer> countMajorVersionsFn,
                                 TriFunction<String, Integer, Integer, List<Annex>> minorVersionsFn, Function<String, Integer> countMinorVersionsFn,
                                 BiFunction<Integer, Integer, List<Annex>> recentChangesFn, Supplier<Integer> countRecentChangesFn) {

        boolean canRestorePreviousVersion = securityContext.hasPermission(annexVO, LeosPermission.CAN_RESTORE_PREVIOUS_VERSION);
        boolean canDownload = securityContext.hasPermission(annexVO, LeosPermission.CAN_DOWNLOAD_XML_COMPARISON);

        versionsTab.setDataFunctions(allVersions, minorVersionsFn, countMinorVersionsFn,
                recentChangesFn, countRecentChangesFn, true, true,
                canRestorePreviousVersion, canDownload);

    }

    @Override
    public boolean isCleanVersionVisible() {
        return false;
    }

    @Override
    public void cleanComparedContent() {
        final String versionInfo = messageHelper.getMessage("document.compare.version.caption.double");
        doubleComparisonComponent.populateDoubleComparisonContent("", LeosCategory.ANNEX, versionInfo, null);
        doubleComparisonComponent.removeComparison();
    }

    @Override
    public void setDownloadStreamResourceForExport(StreamResource streamResource) {
        doubleComparisonComponent.setDownloadStreamResourceForExport(streamResource);
    }
    
    @Override
    public void setDownloadStreamResourceForMenu(DownloadStreamResource streamResource){
        actionsMenuBar.setDownloadStreamResource(streamResource);
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
            contentToZip.put("comparedContent_export.xml", docuWriteComparedContent);

            final String zipFileName = original.getMetadata().get().getRef().concat("-").concat(comparedInfo).
                    concat(original.getMetadata().get().getLanguage().toLowerCase()).concat(".zip");
            zipFile = ZipPackageUtil.zipFiles(zipFileName, contentToZip, language);

            final byte[] zipBytes = FileUtils.readFileToByteArray(zipFile);
            DownloadStreamResource downloadStreamResource = new DownloadStreamResource(zipFileName, new ByteArrayInputStream(zipBytes));
            doubleComparisonComponent.setDownloadStreamResourceForXmlFiles(downloadStreamResource);
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
    public void enableTocEdition(List<TableOfContentItemVO> tableOfContent) {
        tableOfContentComponent.handleEditTocRequest(tocEditor);
        tableOfContentComponent.setTableOfContent(TableOfContentItemConverter.buildTocData(tableOfContent));
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
    public void setPermissions(DocumentVO annex, boolean isClonedProposal) {
        super.setPermissions(annex, isClonedProposal);
        boolean enableExportPackage = securityContext.hasPermission(annex, LeosPermission.CAN_WORK_WITH_EXPORT_PACKAGE);
        actionsMenuBar.setExportPackageVisible(enableExportPackage);
        doubleComparisonComponent.enableExportPackage(enableExportPackage);
        actionsMenuBar.setDownloadVersionVisible(true);
        actionsMenuBar.setDownloadVersionWithAnnotationsVisible(true);
        actionsMenuBar.setDownloadCleanVersionVisible(true);
        actionsMenuBar.setShowCleanVersionVisible(false);
    }

    @Override
    public void setContributionsData(List<ContributionVO> allContributions) {
    }

    @Override
    public boolean isCoverPageVisible() { return false; }

}
