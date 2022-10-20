package eu.europa.ec.leos.ui.component.markedText;

import com.google.common.eventbus.EventBus;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import org.springframework.beans.factory.annotation.Autowired;

@ViewScope
@SpringComponent
@Instance(InstanceType.COMMISSION)
public class ProposalMarkedTextComponent<T extends XmlDocument> extends MarkedTextComponent {

    private static final long serialVersionUID = -3151653919278829335L;

    @Autowired
    public ProposalMarkedTextComponent(final EventBus eventBus, final MessageHelper messageHelper, final SecurityContext securityContext) {
        super(eventBus, messageHelper, securityContext);
        this.setAlwaysHideToolBoxExportButton(false);
    }
}
