package eu.europa.ec.leos.ui.extension;

import com.vaadin.shared.annotations.NoLayout;
import eu.europa.ec.leos.ui.shared.js.LeosJavaScriptExtensionState;

public class ActionManagerState extends LeosJavaScriptExtensionState {

    @NoLayout
    public String instanceType;

    public String tocItemsJsonArray;

}