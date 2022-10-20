package eu.europa.ec.leos.ui.view.explanatory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.server.StreamResource;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.document.Explanatory;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.security.LeosPermission;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.export.ExportDW;
import eu.europa.ec.leos.services.export.ExportOptions;
import eu.europa.ec.leos.services.export.ExportVersions;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.processor.content.TableOfContentProcessor;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.ui.component.ComparisonComponent;
import eu.europa.ec.leos.ui.component.doubleCompare.DoubleComparisonComponent;
import eu.europa.ec.leos.ui.component.toc.TableOfContentItemConverter;
import eu.europa.ec.leos.ui.component.versions.VersionsTab;
import eu.europa.ec.leos.ui.extension.SoftActionsExtension;
import eu.europa.ec.leos.ui.component.toc.TocEditor;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.web.event.component.LayoutChangeRequestEvent;
import eu.europa.ec.leos.web.event.view.document.InstanceTypeResolver;
import eu.europa.ec.leos.web.support.cfg.ConfigurationHelper;
import eu.europa.ec.leos.web.support.user.UserHelper;
import eu.europa.ec.leos.web.support.xml.DownloadStreamResource;
import eu.europa.ec.leos.web.ui.screen.document.ColumnPosition;

import javax.inject.Provider;
import java.util.List;

@SpringComponent
@ViewScope
@Instance(InstanceType.COUNCIL)
public class MandateExplanatoryScreenImpl extends ExplanatoryScreenImpl {
    private static final long serialVersionUID = 934198605326069948L;

    private final DoubleComparisonComponent<Explanatory> doubleComparisonComponent;
    
    MandateExplanatoryScreenImpl(MessageHelper messageHelper, EventBus eventBus, SecurityContext securityContext, UserHelper userHelper,
                                 ConfigurationHelper cfgHelper, TocEditor numberEditor, InstanceTypeResolver instanceTypeResolver,
                                 VersionsTab<Explanatory> versionsTab, Provider<StructureContext> structureContextProvider,
                                 TableOfContentProcessor tableOfContentProcessor, XmlContentProcessor xmlContentProcessor) {
        super(messageHelper, eventBus, securityContext, userHelper, cfgHelper, numberEditor, instanceTypeResolver,
                versionsTab, structureContextProvider, tableOfContentProcessor, xmlContentProcessor);
        ExportOptions exportOptions = new ExportDW(ExportOptions.Output.WORD, Explanatory.class, false);
        doubleComparisonComponent = new DoubleComparisonComponent<>(exportOptions, eventBus, messageHelper, securityContext);
    }

    @Override
    public void init() {
        super.init();
        explanatoryActionsMenuBar.setChildComponentClass(DoubleComparisonComponent.class);
        screenLayoutHelper.addPane(comparisonComponent, 2, false);
        screenLayoutHelper.layoutComponents();
        new SoftActionsExtension<>(explanatoryContent);
    }

    @Subscribe
    void changePosition(LayoutChangeRequestEvent event) {
        changeLayout(event, doubleComparisonComponent);
    }

    @Override
    public void populateComparisonContent(String comparedContent, String comparedInfo, Explanatory original, Explanatory current) {
        ExportVersions<Explanatory> exportVersions = new ExportVersions<>(original, current);
        doubleComparisonComponent.populateMarkedContent(comparedContent, LeosCategory.COUNCIL_EXPLANATORY, comparedInfo, exportVersions);
        doubleComparisonComponent.setSimpleComparison();
    }
    
    @Override
    public void populateDoubleComparisonContent(String comparedContent, String comparedInfo, Explanatory original, Explanatory intermediate, Explanatory current) {
        ExportVersions<Explanatory> exportVersions = new ExportVersions<>(original, intermediate, current);
        doubleComparisonComponent.populateDoubleComparisonContent(comparedContent, LeosCategory.COUNCIL_EXPLANATORY, comparedInfo, exportVersions);
        doubleComparisonComponent.setDoubleComparison();
    }
    
    @Override
    public void showVersion(String content, String versionInfo) {
        changePosition(new LayoutChangeRequestEvent(ColumnPosition.DEFAULT, ComparisonComponent.class, doubleComparisonComponent));
        doubleComparisonComponent.populateDoubleComparisonContent(content.replaceAll("(?i) id=\"", " id=\"doubleCompare-"), LeosCategory.COUNCIL_EXPLANATORY, versionInfo, null);
        doubleComparisonComponent.removeComparison();
    }
    
    @Override
    public void cleanComparedContent() {
        final String versionInfo = messageHelper.getMessage("document.compare.version.caption.double");
        doubleComparisonComponent.populateDoubleComparisonContent("", LeosCategory.COUNCIL_EXPLANATORY, versionInfo, null);
        doubleComparisonComponent.removeComparison();
    }

    @Override
    public void setDownloadStreamResourceForExport(StreamResource streamResource) {
        doubleComparisonComponent.setDownloadStreamResourceForExport(streamResource);
    }
    
    @Override
    public void setDownloadStreamResourceForMenu(DownloadStreamResource streamResource){
        explanatoryActionsMenuBar.setDownloadStreamResource(streamResource);
    }
    
    @Override
    public void setDownloadStreamResourceForXmlFiles(StreamResource streamResource) {
        doubleComparisonComponent.setDownloadStreamResourceForXmlFiles(streamResource);
    }
    
    @Override
    public void enableTocEdition(List<TableOfContentItemVO> tableOfContent) {
        tableOfContentComponent.handleEditTocRequest(tocEditor);
        tableOfContentComponent.setTableOfContent(TableOfContentItemConverter.buildTocData(tableOfContent));
    }

    @Override
    public boolean isComparisonComponentVisible() {
        return comparisonComponent != null && comparisonComponent.getParent() != null;
    }

    @Override
    public void setPermissions(DocumentVO explanatory) {
        super.setPermissions(explanatory);
        explanatoryActionsMenuBar.setDownloadCleanVersionVisible(true);
        explanatoryActionsMenuBar.setDownloadVersionVisible(false);
        explanatoryActionsMenuBar.setDownloadVersionWithAnnotationsVisible(true);
        boolean enableExportPackage = securityContext.hasPermission(explanatory, LeosPermission.CAN_WORK_WITH_EXPORT_PACKAGE);
        doubleComparisonComponent.enableExportPackage(enableExportPackage);
    }

    @Override
    public boolean isCoverPageVisible() {
        return false;
    }

}
