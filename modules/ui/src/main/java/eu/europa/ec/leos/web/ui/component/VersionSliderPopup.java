package eu.europa.ec.leos.web.ui.component;

import com.vaadin.ui.HorizontalLayout;
import eu.europa.ec.leos.ui.component.RangeSliderStepVO;

import java.util.List;
import java.util.Set;

public interface VersionSliderPopup<T> {

    Set<String> getVersionLabels(List<T> displayedVersions);
    
    void updateMinimizedRepresentation();
    
    HorizontalLayout populateItemDetails(HorizontalLayout horizontalLayout, T documentVersion);
    
    void selectionChanged();
    
    Set<RangeSliderStepVO> getVersionSteps(Set<String> displayedVersions);
    
    boolean isDisableInitialVersion();
    
    String getPopupCaption();
}
