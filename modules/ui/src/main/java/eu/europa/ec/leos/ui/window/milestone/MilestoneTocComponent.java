package eu.europa.ec.leos.ui.window.milestone;

import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import eu.europa.ec.leos.ui.component.LeosJavaScriptComponent;
import eu.europa.ec.leos.web.support.LeosCacheToken;

@StyleSheet({"vaadin://../lib/jqTree_1.4.9/css/jqtree.css" + LeosCacheToken.TOKEN, 
    "vaadin://../assets/css/leos-toc-rendition.css" + LeosCacheToken.TOKEN})
@JavaScript({"vaadin://../js/ui/component/milestoneTocConnector.js"+ LeosCacheToken.TOKEN,
    "vaadin://../lib/jqTree_1.4.9/jqtree.js" + LeosCacheToken.TOKEN})
public class MilestoneTocComponent extends LeosJavaScriptComponent {

    private static final long serialVersionUID = -6271664521480550491L;
    
    public void setTocData(String tocData) {
        getState().tocData = tocData;
    }

    @Override
    protected MilestoneTocState getState() {
        return (MilestoneTocState) super.getState();
    }
    
    @Override
    protected MilestoneTocState getState(boolean markAsDirty) {
        return (MilestoneTocState) super.getState(markAsDirty);
    }
}
