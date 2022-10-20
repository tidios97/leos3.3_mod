package eu.europa.ec.leos.web.ui.component.actions;

import org.apache.cxf.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.vaadin.dialogs.ConfirmDialog;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.ui.UI;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.annex.AnnexStructureType;
import eu.europa.ec.leos.ui.event.view.AddStructureChangeMenuEvent;
import eu.europa.ec.leos.ui.event.view.AnnexStructureChangeEvent;
import eu.europa.ec.leos.ui.window.export.AnnexExportPackageWindow;
import eu.europa.ec.leos.ui.window.export.ExportPackageWindow;

@Component
@Scope("prototype")
public class AnnexActionsMenuBar extends CommonActionsMenuBar {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(AnnexActionsMenuBar.class);
    
    private MenuItem annexActionSeparator;
    private MenuItem switchStructure;
    private MenuItem createExportPackageItem;

    @Autowired
    public AnnexActionsMenuBar(MessageHelper messageHelper, EventBus eventBus) {
        super(messageHelper, eventBus);
    }

    @Override
    protected void buildViewActions() {
        LOG.debug("Building View actions group...");
        addCustomSeparator(messageHelper.getMessage("menu.actions.separator.view"));
    }
    
    @Subscribe
    public void buildStructureChangeAction(AddStructureChangeMenuEvent event) {
        LOG.debug("Building annex actions menu item...");
        mainMenuItem.removeChild(annexActionSeparator);
        mainMenuItem.removeChild(switchStructure);
        annexActionSeparator = addCustomSeparator(messageHelper.getMessage("menu.annex.action"));
        AnnexStructureType switchStructureType = getSwitchStructureType(event.getStructureType());
        //Structure change
        switchStructure = createMenuItem(messageHelper.getMessage("menu.actions.separator.structure.change." + switchStructureType.getType()),
                selectedItem -> switchStructure(switchStructureType));
    }
    
    private void switchStructure(AnnexStructureType structureType) {
        ConfirmDialog confirmDialog = ConfirmDialog.getFactory().create(
                messageHelper.getMessage("annex.structure.switch.window.title"),
                messageHelper.getMessage("annex.structure.switch.caption", StringUtils.capitalize(structureType.getType())),
                messageHelper.getMessage("annex.structure.switch.button.switch"),
                messageHelper.getMessage("annex.structure.switch.button.cancel"),
                null
        );
        confirmDialog.setContentMode(ConfirmDialog.ContentMode.HTML);
        confirmDialog.show(getUI(), dialog -> {
            if (dialog.isConfirmed()) {
                eventBus.post(new AnnexStructureChangeEvent(structureType));
            }
        }, true);
    }
    
    private AnnexStructureType getSwitchStructureType(AnnexStructureType structureType) {
        return (structureType.getType() == AnnexStructureType.LEVEL.getType()) ? AnnexStructureType.ARTICLE : AnnexStructureType.LEVEL;
    }
    
    @Override
    protected void initDropDownMenu() {
        buildVersionActions();
        buildExportPackageActions();
        //buildStructureChangeAction();
        buildViewActions();
    }

    private void buildExportPackageActions() {
        LOG.debug("Building Export Package actions group...");
        createExportPackageItem = createMenuItem(messageHelper.getMessage("menu.actions.create.export.package"),
                selectedItem -> {
                    ExportPackageWindow exportPackageWindow = new AnnexExportPackageWindow(messageHelper, eventBus);
                    UI.getCurrent().addWindow(exportPackageWindow);
                    exportPackageWindow.center();
                    exportPackageWindow.focus();    
                });
        createExportPackageItem.setVisible(false);
    }

    public void setExportPackageVisible(boolean visible) {
        createExportPackageItem.setVisible(visible);
    }

}
