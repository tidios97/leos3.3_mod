package eu.europa.ec.leos.web.ui.component.actions;

import com.google.common.eventbus.EventBus;
import com.vaadin.ui.UI;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.ui.window.export.FinancialStatementExportPackageWindow;
import eu.europa.ec.leos.ui.window.export.ExportPackageWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class FinancialstatementActionsMenuBar extends CommonActionsMenuBar {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(FinancialstatementActionsMenuBar.class);
    
    private MenuItem financialStatementActionSeparator;
    private MenuItem createExportPackageItem;

    @Autowired
    public FinancialstatementActionsMenuBar(MessageHelper messageHelper, EventBus eventBus) {
        super(messageHelper, eventBus);
    }

    @Override
    protected void buildViewActions() {
        LOG.debug("Building View actions group...");
        addCustomSeparator(messageHelper.getMessage("menu.actions.separator.view"));
    }
    
    @Override
    protected void initDropDownMenu() {
        buildVersionActions();
        buildExportPackageActions();
        buildViewActions();
    }

    private void buildExportPackageActions() {
        LOG.debug("Building Export Package actions group...");
        createExportPackageItem = createMenuItem(messageHelper.getMessage("menu.actions.create.export.package"),
                selectedItem -> {
                    ExportPackageWindow exportPackageWindow = new FinancialStatementExportPackageWindow(messageHelper, eventBus);
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
