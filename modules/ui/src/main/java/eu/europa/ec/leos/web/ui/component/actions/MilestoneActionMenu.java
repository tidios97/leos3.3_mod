package eu.europa.ec.leos.web.ui.component.actions;

import com.google.common.eventbus.EventBus;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.web.ui.themes.LeosTheme;

public class MilestoneActionMenu extends ActionsMenuBarComponent {

    private static final long serialVersionUID = 1L;

    public MilestoneActionMenu(MessageHelper messageHelper, EventBus eventBus) {
        super(messageHelper, eventBus, LeosTheme.LEOS_HAMBURGUER_VERSIONS_16);
    }

    @Override
    void initDropDownMenu() {

    }
}
