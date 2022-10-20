package eu.europa.ec.leos.ui.component.versions;

import com.google.common.eventbus.EventBus;
import eu.europa.ec.leos.model.action.VersionVO;
import eu.europa.ec.leos.web.event.component.CompareRequestEvent;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public abstract class VersionComparator {
    
    protected EventBus eventBus;
    
    @Autowired
    public VersionComparator(EventBus eventBus){
        this.eventBus = eventBus;
    }
    
    abstract int getNumberVersionsForComparing();
    
    abstract void doubleCompare(Collection<VersionVO> selectedCheckBoxes);
    
    public boolean isCompareModeAvailable() {
        return true;
    }

    List<String> getOrderedCheckboxes(Collection<VersionVO> checkboxes) {
        return checkboxes
                .stream()
                .sorted(Comparator.comparing(VersionVO::getVersionNumber))
                .map(VersionVO::getDocumentId)
                .collect(Collectors.toList());
    }

    void compare(Collection<VersionVO> selectedCheckBoxes) {
        List<String> orderedCheckboxes = getOrderedCheckboxes(selectedCheckBoxes);
        final String oldVersion = orderedCheckboxes.get(0);
        final String newVersion = orderedCheckboxes.get(1);
        eventBus.post(new CompareRequestEvent(oldVersion, newVersion));
    }
}
