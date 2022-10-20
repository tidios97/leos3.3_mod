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
@Instance(InstanceType.COUNCIL)
public class MandateMarkedTextComponent<T extends XmlDocument> extends MarkedTextComponent {

    private static final long serialVersionUID = 5322659426977921984L;

    @Autowired
    public MandateMarkedTextComponent(final EventBus eventBus, final MessageHelper messageHelper, final SecurityContext securityContext) {
        super(eventBus, messageHelper, securityContext);
    }
}