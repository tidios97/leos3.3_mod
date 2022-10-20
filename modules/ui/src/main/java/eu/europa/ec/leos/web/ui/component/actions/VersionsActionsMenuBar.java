package eu.europa.ec.leos.web.ui.component.actions;

import com.google.common.eventbus.EventBus;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.web.ui.themes.LeosTheme;

public class VersionsActionsMenuBar extends ActionsMenuBarComponent {
    
    private static final long serialVersionUID = 1L;
    
    public VersionsActionsMenuBar(EventBus eventBus, MessageHelper messageHelper) {
        super(messageHelper, eventBus, LeosTheme.LEOS_HAMBURGUER_VERSIONS_16);
    }
    
    @Override
    protected void initDropDownMenu() {
    
    }
    
}
