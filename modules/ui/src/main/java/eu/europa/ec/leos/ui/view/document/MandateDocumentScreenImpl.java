/*
* Copyright 2019 European Commission
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
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
import eu.europa.ec.leos.model.action.VersionVO;
import eu.europa.ec.leos.security.LeosPermission;
import eu.europa.ec.leos.security.LeosPermissionAuthorityMapHelper;
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
import eu.europa.ec.leos.ui.component.contributions.ContributionsTab;
import eu.europa.ec.leos.ui.component.doubleCompare.DoubleComparisonComponent;
import eu.europa.ec.leos.ui.component.toc.TocEditor;
import eu.europa.ec.leos.ui.component.versions.VersionsTab;
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
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@ViewScope
@SpringComponent
@Instance(InstanceType.COUNCIL)
@StyleSheet({"vaadin://../assets/css/bill.css" + LeosCacheToken.TOKEN})
@StyleSheet({"vaadin://../assets/css/bill-wo-diffing-style.css" + LeosCacheToken.TOKEN})
// css not present in VAADIN folder so not published by server. Anything not published need to be served by static servlet.
public class MandateDocumentScreenImpl extends DocumentScreenImpl {
    private static final long serialVersionUID = 6711728542602337765L;

    private static final Logger LOG = LoggerFactory.getLogger(MandateDocumentScreenImpl.class);

    private final DoubleComparisonComponent<Bill> doubleComparisonComponent;

    @Autowired
    MandateDocumentScreenImpl(UserHelper userHelper, SecurityContext securityContext, EventBus eventBus, ConfigurationHelper cfgHelper,
                              MessageHelper messageHelper, TocEditor tocEditor, InstanceTypeResolver instanceTypeResolver,
                              MenuBarComponent menuBarComponent, LeosPermissionAuthorityMapHelper authorityMapHelper, LegalTextActionsMenuBar legalTextActionMenuBar, ComparisonComponent<Bill> comparisonComponent,
                              VersionsTab<Bill> versionsTab, ContributionsTab<Bill> contributionsTab, Provider<StructureContext> structureContextProvider, TableOfContentProcessor tableOfContentProcessor,
                              XmlContentProcessor xmlContentProcessor) {
        super(userHelper, securityContext, eventBus, cfgHelper, messageHelper, tocEditor, instanceTypeResolver, menuBarComponent, authorityMapHelper,
                legalTextActionMenuBar, comparisonComponent, versionsTab, contributionsTab, structureContextProvider,
                tableOfContentProcessor, xmlContentProcessor);

        ExportOptions exportOptions = new ExportDW(ExportOptions.Output.WORD, Bill.class, false);
        doubleComparisonComponent = new DoubleComparisonComponent<>(exportOptions, eventBus, messageHelper, securityContext);
        init();
    }

    @Override
    public void init() {
        super.init();
        buildDocumentPane();
        legalTextActionMenuBar.setChildComponentClass(DoubleComparisonComponent.class);
        legalTextPaneComponent.addPaneToLayout(comparisonComponent, 2, false);
        legalTextPaneComponent.layoutChildComponents();
        new SoftActionsExtension(legalTextPaneComponent.getContent());
    }

    @Override
    public void enableTocEdition(List<TableOfContentItemVO> tableOfContentItemVoList) {
        legalTextPaneComponent.handleTocEditRequestEvent(tableOfContentItemVoList, tocEditor);
    }

    @Subscribe
    void changePosition(LayoutChangeRequestEvent event) {
        changeLayout(event, event.getChildComponent() != null ? event.getChildComponent() : doubleComparisonComponent);
    }

    @Override
    public void refreshContent(final String documentContent) {
        eventBus.post(new AddChangeDetailsMenuEvent());
        legalTextPaneComponent.populateContent(documentContent);
    }

    @Override
    public void populateMarkedContent(String comparedContent, String comparedInfo, Bill original, Bill current) {
        ExportVersions<Bill> exportVersions = new ExportVersions<>(original, current);
        doubleComparisonComponent.populateMarkedContent(comparedContent, LeosCategory.BILL, comparedInfo, exportVersions);
        doubleComparisonComponent.setSimpleComparison();
    }
    
    @Override
    public void populateDoubleComparisonContent(String comparedContent, String comparedInfo, Bill original, Bill intermediate, Bill current) {
        ExportVersions<Bill> exportVersions = new ExportVersions<>(original, intermediate, current);
        doubleComparisonComponent.populateDoubleComparisonContent(comparedContent, LeosCategory.BILL, comparedInfo, exportVersions);
        doubleComparisonComponent.setDoubleComparison();
    }

    @Override
    public void showVersion(String content, String versionInfo){
        changePosition(new LayoutChangeRequestEvent(ColumnPosition.DEFAULT, ComparisonComponent.class, doubleComparisonComponent));
        doubleComparisonComponent.populateDoubleComparisonContent(content.replaceAll("(?i) id=\"", " id=\"doubleCompare-"), LeosCategory.BILL, versionInfo, null);
        doubleComparisonComponent.removeComparison();
    }

    @Override
    public void showRevision(String content, String contributionStatus, ContributionVO contributionVO,
                             List<TocItem> tocItemList){
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
                                 BiFunction<Integer, Integer, List<Bill>> majorVersionsFn, Supplier<Integer> countMajorVersionsFn,
                                 TriFunction<String, Integer, Integer, List<Bill>> minorVersionsFn, Function<String, Integer> countMinorVersionsFn,
                                 BiFunction<Integer, Integer, List<Bill>> recentChangesFn, Supplier<Integer> countRecentChangesFn) {

        boolean canRestorePreviousVersion = securityContext.hasPermission(annexVO, LeosPermission.CAN_RESTORE_PREVIOUS_VERSION);
        boolean canDownload = securityContext.hasPermission(annexVO, LeosPermission.CAN_DOWNLOAD_XML_COMPARISON);

        legalTextPaneComponent.setDataFunctions(allVersions, allContributions, majorVersionsFn, countMajorVersionsFn, minorVersionsFn,
                countMinorVersionsFn, recentChangesFn, countRecentChangesFn, true, true,
                canRestorePreviousVersion, canDownload);

    }

    @Override
    public boolean isCleanVersionShowed() {
        return false;
    }

    @Override
    public void cleanComparedContent() {
        final String versionInfo = messageHelper.getMessage("document.compare.version.caption.double");
        doubleComparisonComponent.populateDoubleComparisonContent("", LeosCategory.BILL, versionInfo, null);
        doubleComparisonComponent.removeComparison();
    }
    
    @Override
    public void setDownloadStreamResourceForExport(StreamResource streamResource) {
        doubleComparisonComponent.setDownloadStreamResourceForExport(streamResource);
    }

    @Override
    public void setDownloadStreamResourceForMenu(DownloadStreamResource streamResource) {
        legalTextActionMenuBar.setDownloadStreamResource(streamResource);
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
    public void scrollToMarkedChange(String elementId) {
    }

    @Override
    public void setPermissions(DocumentVO bill, boolean isClonedProposal) {
        legalTextPaneComponent.setPermissions(bill, instanceTypeResolver.getInstanceType());
        boolean enableExportPackage = securityContext.hasPermission(bill, LeosPermission.CAN_WORK_WITH_EXPORT_PACKAGE);
        legalTextPaneComponent.enableExportPackage(enableExportPackage);
        doubleComparisonComponent.enableExportPackage(enableExportPackage);
        legalTextActionMenuBar.setDownloadVersionVisible(true);
        legalTextActionMenuBar.setDownloadVersionWithAnnotationsVisible(true);
        legalTextActionMenuBar.setDownloadCleanVersionVisible(true);
        legalTextActionMenuBar.setShowCleanVersionVisible(false);
    }

    @Override
    public boolean isCoverPageVisible() {
        return false;
    }

}
