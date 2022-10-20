package eu.europa.ec.leos.ui.component;

import com.vaadin.ui.VerticalLayout;
import eu.europa.ec.leos.web.ui.component.ContentPane;

public class AccordionPane extends VerticalLayout implements ContentPane {
    
    @Override
    public float getDefaultPaneWidth(int numberOfFeatures, boolean tocPresent) {
        final float featureWidth;
        switch (numberOfFeatures) {
            case 1:
                featureWidth = 100f;
                break;
            case 2:
                featureWidth = 20f;
                break;
            default:
                featureWidth = 30f;
                break;
        }
        return featureWidth;
    }
}
