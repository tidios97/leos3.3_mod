package eu.europa.ec.leos.ui.component.versions;


import com.google.common.eventbus.EventBus;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.model.action.VersionVO;
import eu.europa.ec.leos.ui.event.doubleCompare.DoubleCompareRequestEvent;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;

@SpringComponent
@ViewScope
@Instance(InstanceType.COUNCIL)
public class VersionComparatorMandate extends VersionComparator {
    
    @Autowired
    public VersionComparatorMandate(EventBus eventBus) {
        super(eventBus);
    }
    
    @Override
    int getNumberVersionsForComparing() {
        return 3;
    }
    
    @Override
    public boolean isCompareModeAvailable() {
        return false;
    }
    
    @Override
    void doubleCompare(Collection<VersionVO> selectedCheckBoxes) {
        List<String> orderedCheckboxes = getOrderedCheckboxes(selectedCheckBoxes);
        final String originalProposal = orderedCheckboxes.get(0);
        final String intermediateMajor = orderedCheckboxes.get(1);
        final String current = orderedCheckboxes.get(2);
        eventBus.post(new DoubleCompareRequestEvent(originalProposal, intermediateMajor, current));
    }
}
