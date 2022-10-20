/*
 * Copyright 2022 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.europa.ec.leos.ui.extension;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractField;
import eu.europa.ec.leos.web.event.view.ChangeDetailsRequestEvent;
import eu.europa.ec.leos.web.support.LeosCacheToken;

@JavaScript({"vaadin://../js/ui/extension/changeDetailsConnector.js" + LeosCacheToken.TOKEN})
public class ChangeDetailsExtension<T extends AbstractField<V>, V> extends LeosJavaScriptExtension {

    private static final long serialVersionUID = 22081984L;

    private EventBus eventBus;

    public ChangeDetailsExtension(T target, EventBus eventBus) {
        super();
        this.eventBus = eventBus;
        extend(target);
    }

    @Override
    public void attach() {
        super.attach();
        eventBus.register(this);
    }

    @Override
    public void detach() {
        eventBus.unregister(this);
        super.detach();
    }

    protected void extend(T target) {
        super.extend(target);
        // handle target's value change
        target.addValueChangeListener(event -> {
            LOG.trace("Target's value changed...");
            // Mark that this connector's state might have changed.
            // There is no need to send new data to the client-side,
            // since we just want to trigger a state change event...
            forceDirty();
        });
    }

    @Subscribe
    public void setChangeDetailsState(ChangeDetailsRequestEvent event) {
        getState(false).changeDetailsEnabled = event.isEnable();
        callFunction("enableChangeDetails");
    }

    @Override
    protected ChangeDetailsState getState() {
        return (ChangeDetailsState)super.getState();
    }

    @Override
    protected ChangeDetailsState getState(boolean markAsDirty) {
        return (ChangeDetailsState)super.getState(markAsDirty);
    }
}
