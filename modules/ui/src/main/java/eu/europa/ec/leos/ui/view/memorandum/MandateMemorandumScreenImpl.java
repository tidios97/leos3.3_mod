package eu.europa.ec.leos.ui.view.memorandum;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.server.StreamResource;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
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
import eu.europa.ec.leos.services.export.ExportDW;
import eu.europa.ec.leos.services.export.ExportOptions;
import eu.europa.ec.leos.services.export.ZipPackageUtil;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.processor.content.TableOfContentProcessor;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.ui.component.versions.VersionComparator;
import eu.europa.ec.leos.ui.component.versions.VersionsTab;
import eu.europa.ec.leos.ui.component.toc.TocEditor;
import eu.europa.ec.leos.vo.toc.TocItem;
import eu.europa.ec.leos.web.event.NotificationEvent;
import eu.europa.ec.leos.web.event.component.LayoutChangeRequestEvent;
import eu.europa.ec.leos.web.event.view.AddChangeDetailsMenuEvent;
import eu.europa.ec.leos.web.event.view.document.FetchUserPermissionsResponse;
import eu.europa.ec.leos.web.event.view.document.InstanceTypeResolver;
import eu.europa.ec.leos.web.support.cfg.ConfigurationHelper;
import eu.europa.ec.leos.web.support.user.UserHelper;
import eu.europa.ec.leos.web.support.xml.DownloadStreamResource;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import eu.europa.ec.leos.ui.component.markedText.MarkedTextComponent;

import javax.inject.Provider;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ViewScope
@SpringComponent
@Instance(InstanceType.COUNCIL)
class MandateMemorandumScreenImpl extends MemorandumScreenImpl {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(MandateMemorandumScreenImpl.class);

    protected MarkedTextComponent<Memorandum> markedTextComponent;

    @Autowired
    MandateMemorandumScreenImpl(SecurityContext securityContext, EventBus eventBus, MessageHelper messageHelper, ConfigurationHelper cfgHelper,
                                UserHelper userHelper, TocEditor tocEditor, InstanceTypeResolver instanceTypeResolver, VersionsTab versionsTab,
                                Provider<StructureContext> structureContext, VersionComparator versionComparator,
                                MarkedTextComponent<Memorandum> markedTextComponent,
                                TableOfContentProcessor tableOfContentProcessor, XmlContentProcessor xmlContentProcessor) {
        super(securityContext, eventBus, messageHelper, cfgHelper, userHelper, tocEditor, instanceTypeResolver,
                versionsTab, structureContext, versionComparator, tableOfContentProcessor, xmlContentProcessor);
        ExportOptions exportOptions = new ExportDW(ExportOptions.Output.WORD, Memorandum.class, false);
        markedTextComponent.setExportOptions(exportOptions);
        this.markedTextComponent = markedTextComponent;
        init();
    }

    @Override
    void init() {
        super.init();
    }

    @Override
    public void showVersion(String content, String versionInfo) {
        throw new IllegalArgumentException("Operation not valid");
    }

    @Override
    public void showRevision(String content, String contributionStatus, ContributionVO contributionVO, List<TocItem> tocItemList) {
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
    public boolean isCleanVersionShowed() {
        return false;
    }

    @Override
    public void cleanComparedContent() {
        throw new IllegalArgumentException("Operation not valid");
    }

    @Override
    public void populateComparisonContent(String comparedContent, String versionInfo, Memorandum original, Memorandum current) {
        throw new IllegalArgumentException("Operation not valid");
    }
    
    @Override
    public void populateDoubleComparisonContent(String comparedContent, String versionInfo) {
        throw new IllegalArgumentException("Operation not valid");
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
            contentToZip.put("comparedContent_export.xml", docuWriteComparedContent);

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
    public void setPermissions(DocumentVO memorandum, boolean isClonedProposal) {
        actionsMenuBar.setSaveVersionVisible(false);
        tableOfContentComponent.setPermissions(false);
        actionsMenuBar.setDownloadVersionVisible(false);
        actionsMenuBar.setDownloadVersionWithAnnotationsVisible(false);
        actionsMenuBar.setDownloadCleanVersionVisible(false);
        actionsMenuBar.setShowCleanVersionVisible(false);
    }

    @Override
    public void initLeosEditor(DocumentVO memorandum, List<LeosMetadata> documentsMetadata) {
        throw new IllegalArgumentException("Operation not valid");
    }

    @Override
    public void sendUserPermissions(List<LeosPermission> userPermissions) {
        userPermissions = userPermissions.stream().filter(permission -> permission.compareTo(LeosPermission.CAN_SUGGEST) != 0).collect(Collectors.toList());
        eventBus.post(new FetchUserPermissionsResponse(userPermissions));
    }
    
    @Subscribe
    void changePosition(LayoutChangeRequestEvent event) {
        screenLayoutHelper.changePosition(event.getPosition(), event.getOriginatingComponent());
    }

    @Override
    public void setContributionsData(List<ContributionVO> allContributions) {
    }

    @Override
    public boolean isCoverPageVisible() {
        return false;
    }
}
