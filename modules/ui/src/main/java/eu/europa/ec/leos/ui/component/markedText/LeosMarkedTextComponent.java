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

import java.util.HashMap;
import java.util.Map;

@ViewScope
@SpringComponent
@Instance(InstanceType.OS)
public class LeosMarkedTextComponent<T extends XmlDocument> extends MarkedTextComponent {

    private static final long serialVersionUID = 2444295781989179408L;

    @Autowired
    public LeosMarkedTextComponent(final EventBus eventBus, final MessageHelper messageHelper, final SecurityContext securityContext) {
        super(eventBus, messageHelper, securityContext);
    }

    @Override
    protected Map<String, String> getSelectorStyleMap() {
        Map<String, String> selectorStyleMap = new HashMap<>();
        selectorStyleMap.put(".leos-marker-content-removed", "pin-leos-marker-content-removed");
        selectorStyleMap.put(".leos-marker-content-added", "pin-leos-marker-content-added");
        selectorStyleMap.put(".leos-content-removed", "pin-leos-content-removed");
        selectorStyleMap.put(".leos-content-new", "pin-leos-content-new");
        return selectorStyleMap;
    }
}
