package eu.europa.ec.leos.ui.view.collection;

import com.google.common.eventbus.EventBus;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FileResource;
import com.vaadin.server.Resource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.UI;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.i18n.LanguageHelper;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.security.LeosPermission;
import eu.europa.ec.leos.security.LeosPermissionAuthorityMapHelper;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.clone.CloneContext;
import eu.europa.ec.leos.services.export.ExportDW;
import eu.europa.ec.leos.services.export.ExportOptions;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.ui.event.view.collection.DeleteCollectionEvent;
import eu.europa.ec.leos.ui.event.view.collection.DownloadMandateEvent;
import eu.europa.ec.leos.ui.event.view.collection.ExportMandateEvent;
import eu.europa.ec.leos.ui.model.ExportPackageVO;
import eu.europa.ec.leos.ui.model.MilestonesVO;
import eu.europa.ec.leos.ui.wizard.document.CreateExplanatoryDocumentWizard;
import eu.europa.ec.leos.vo.catalog.CatalogItem;
import eu.europa.ec.leos.web.event.view.repository.ExplanatoryCreateWizardRequestEvent;
import eu.europa.ec.leos.web.support.UrlBuilder;
import eu.europa.ec.leos.web.support.cfg.ConfigurationHelper;
import eu.europa.ec.leos.web.support.user.UserHelper;
import eu.europa.ec.leos.web.support.xml.DownloadStreamResource;
import eu.europa.ec.leos.web.ui.component.SearchContextComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.web.context.WebApplicationContext;
import org.vaadin.dialogs.ConfirmDialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ViewScope
@SpringComponent
@Instance(InstanceType.COUNCIL)
public class MandateCollectionScreenImpl extends CollectionScreenImpl {
    private static final long serialVersionUID = 8863585776380925364L;

    private static final Logger LOG = LoggerFactory.getLogger(MandateCollectionScreenImpl.class);

    protected MenuBar.MenuItem exportCollectionToDocuwrite;

    private Button createExplanatoryButton = new Button(); // initialized to avoid unmapped field exception from design

    MandateCollectionScreenImpl(UserHelper userHelper, MessageHelper messageHelper, EventBus eventBus, LanguageHelper langHelper,
                                ConfigurationHelper cfgHelper, WebApplicationContext webApplicationContext,
                                SecurityContext securityContext, UrlBuilder urlBuilder,
                                LeosPermissionAuthorityMapHelper authorityMapHelper, CloneContext cloneContext,
                                XmlContentProcessor xmlContentProcessor) {
        super(userHelper, messageHelper, eventBus, langHelper, cfgHelper, webApplicationContext, securityContext,
                urlBuilder, authorityMapHelper, cloneContext, xmlContentProcessor);
        initMandateStaticData();
    }

    @Override
    public void confirmCollectionDeletion() {
        // ask confirmation before delete
        ConfirmDialog confirmDialog = ConfirmDialog.getFactory().create(
                messageHelper.getMessage("collection.mandate.delete.confirmation.title"),
                messageHelper.getMessage("collection.mandate.delete.confirmation.message"),
                messageHelper.getMessage("collection.delete.confirmation.confirm"),
                messageHelper.getMessage("collection.delete.confirmation.cancel"), null);
        confirmDialog.setContentMode(ConfirmDialog.ContentMode.HTML);

        confirmDialog.show(getUI(),
                new ConfirmDialog.Listener() {
                    private static final long serialVersionUID = 144198814274639L;

                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {
                            eventBus.post(new DeleteCollectionEvent());
                        }
                    }
                }, true);
    }

    private class ExportToDocuwriteCommand implements MenuBar.Command {

        private static final long serialVersionUID = -8216998585945188964L;

        @Override
        public void menuSelected(MenuBar.MenuItem selectedItem) {
            eventBus.post(new ExportMandateEvent(new ExportDW(ExportOptions.Output.WORD)));
        }
    }

    private void initMandateStaticData() {
        exportCollection.setDescription(messageHelper.getMessage("collection.description.menuitem.export.docuwrite"));
        MenuBar.MenuItem exportMenu = exportCollection.addItem(messageHelper.getMessage("collection.caption.menuitem.export"), null);
        exportCollectionToDocuwrite = exportMenu.addItem(messageHelper.getMessage("collection.caption.menuitem.export.docuwrite"),
                new ExportToDocuwriteCommand());
        exportCollectionToDocuwrite.setDescription(messageHelper.getMessage("collection.description.menuitem.export.document.docuwrite"));

        //Disabled for first implementation. Further analysis needed
        exportCollection.setVisible(false);

        downloadCollection.setDescription(messageHelper.getMessage("collection.description.button.download.docuwrite"));

        exportPackageBlockHeading.setCaption(messageHelper.getMessage("collection.block.export.package.caption"));

        searchContextHolder.addStyleName("search-context");
        searchContextHolder.addComponent(new SearchContextComponent(messageHelper, eventBus));

        explanatoryBlockHeading.setCaption(messageHelper.getMessage("collection.block.explanatory.caption"));
        explanatoryBlockHeading.addRightButton(addCreateExplanatoryButton());

        createSupportingDocumentsButton.setVisible(false);
        explanatoryBlockHeading.setCaption(messageHelper.getMessage("collection.block.explanatory.caption"));
    }

    private Button addCreateExplanatoryButton() {
        createExplanatoryButton.setIcon(VaadinIcons.PLUS_CIRCLE);
        createExplanatoryButton.setDescription(messageHelper.getMessage("collection.block.explanatory.description.button.create"));
        createExplanatoryButton.addStyleName("create-button");
        createExplanatoryButton.addClickListener(clickEvent -> eventBus.post(new ExplanatoryCreateWizardRequestEvent()));
        createExplanatoryButton.setDisableOnClick(true);
        return createExplanatoryButton;
    }

    @Override
    public void showCreateDocumentWizard(List<CatalogItem> templates) {
        CreateExplanatoryDocumentWizard createDocumentWizard = new CreateExplanatoryDocumentWizard(templates, messageHelper, langHelper, eventBus);
        UI.getCurrent().addWindow(createDocumentWizard);
        createDocumentWizard.focus();
    }

    @Override
    public void setSearchContextHolder(boolean isShowSearchContext) {
        searchContextHolder.setVisible(isShowSearchContext);
    }

    @Override
    protected void resetBasedOnPermissions(DocumentVO proposalVO, Authentication authentication) {
        super.resetBasedOnPermissions(proposalVO, authentication);

        boolean enableExportToDocuwrite = securityContext.hasPermission(proposalVO, LeosPermission.CAN_EXPORT_DW, authentication);
        exportCollectionToDocuwrite.setVisible(enableExportToDocuwrite);

        boolean enableExportPackage = securityContext.hasPermission(proposalVO, LeosPermission.CAN_WORK_WITH_EXPORT_PACKAGE, authentication);
        exportPackageBlock.setVisible(enableExportPackage);

        boolean enableCloseButton = securityContext.hasPermission(proposalVO, LeosPermission.CAN_CLOSE_PROPOSAL, authentication);
        closeButton.setVisible(enableCloseButton);
    }

    @Override
    protected void initDownloader() {
        // Resource cannot be null at instantiation time of the FileDownloader, creating a dummy one
        FileResource downloadStreamResource = new FileResource(new File(""));
        fileDownloader = new FileDownloader(downloadStreamResource) {
            private static final long serialVersionUID = -4584979099145066535L;

            @Override
            public boolean handleConnectorRequest(VaadinRequest request, VaadinResponse response, String path) throws IOException {
                boolean result = false;
                try {
                    eventBus.post(new DownloadMandateEvent());
                    result = super.handleConnectorRequest(request, response, path);
                } catch (Exception exception) {
                    LOG.error("Error occurred in download", exception.getMessage());
                }

                return result;
            }
        };
        fileDownloader.extend(downloadCollection);
    }
    
    @Override
    public void setDownloadStreamResource(Resource downloadResource) {
        fileDownloader.setFileDownloadResource(downloadResource);
    }

    @Override
    public void setExportPdfStreamResource(Resource exportPdfStreamResource) {
    }

    @Override
    public void openCreateRevisionWindow(MilestonesVO milesstoneVo) {
    }

    @Override
    public void populateExportPackages(Set<ExportPackageVO> exportPackages) {
        exportPackageComponent.populateData(exportPackages);
    }

    @Override
    public void setExportPackageStreamResource(DownloadStreamResource exportPackageStreamResource) {
        exportPackageComponent.setDownloadStreamResource(exportPackageStreamResource);
    }

    @Override
    public void populateExplanatory(DocumentVO proposalVO) {
        createExplanatoryButton.setEnabled(true);
        explanatoryBlock.setVisible(true);
        List<DocumentVO> explanatories = new ArrayList<>();
        proposalVO.getChildDocuments().forEach(documentVO -> {
            if(LeosCategory.COUNCIL_EXPLANATORY.equals(documentVO.getCategory())){
                explanatories.add(documentVO);
            }
        });
        explanatoryComponent.populateData(explanatories);
    }

    @Override
    public void reset() {
        createExplanatoryButton.setEnabled(true);
    }

    @Override
    public boolean isCoverPageVisible() {
        return false;
    }
}
