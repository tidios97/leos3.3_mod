package eu.europa.ec.leos.ui.component.search;

import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.TextField;
import eu.europa.ec.leos.ui.shared.search.MessageOverlayState;

public class MessageOverlayExtension extends AbstractExtension {
    protected MessageOverlayExtension(TextField field) {
        extend(field);
    }
    @Override
    protected MessageOverlayState getState() {
        return (MessageOverlayState) super.getState();
    }

    @Override
    protected MessageOverlayState getState(boolean markAsDirty) {
        return (MessageOverlayState) super.getState(markAsDirty);
    }

    public void setMessage(String message) {
        getState().message = message;
    }

}
