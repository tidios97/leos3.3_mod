package eu.europa.ec.leos.ui.component.versions;

import com.google.common.eventbus.EventBus;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.model.action.VersionVO;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;

@SpringComponent
@ViewScope
@Instance(instances = {InstanceType.OS, InstanceType.COMMISSION})
public class VersionComparatorProposal extends VersionComparator {
    
    @Autowired
    public VersionComparatorProposal(EventBus eventBus) {
        super(eventBus);
    }
    
    @Override
    int getNumberVersionsForComparing() {
        return 2;
    }
    
    void doubleCompare(Collection<VersionVO> selectedCheckBoxes) {
        throw new IllegalStateException("Operation non implemented for this instance");
    }
}
