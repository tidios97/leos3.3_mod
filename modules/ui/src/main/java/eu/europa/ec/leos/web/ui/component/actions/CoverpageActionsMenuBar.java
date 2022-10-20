package eu.europa.ec.leos.web.ui.component.actions;

import com.google.common.eventbus.EventBus;
import eu.europa.ec.leos.i18n.MessageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class CoverpageActionsMenuBar extends CommonActionsMenuBar {

    private static final long serialVersionUID = 1L;

    @Autowired
    public CoverpageActionsMenuBar(MessageHelper messageHelper, EventBus eventBus) {
        super(messageHelper, eventBus);
    }

    @Override
    protected void initDropDownMenu() {
        buildVersionActions();
        buildViewActions();
    }

}
