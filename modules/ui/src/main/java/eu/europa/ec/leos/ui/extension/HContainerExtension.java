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
import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.JavaScriptFunction;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import eu.europa.ec.leos.web.event.view.document.SaveElementAttributeRequestEvent;
import eu.europa.ec.leos.web.support.LeosCacheToken;

@JavaScript({"vaadin://../js/ui/extension/hContainerConnector.js"+ LeosCacheToken.TOKEN })
public class HContainerExtension<T extends AbstractField<V>, V> extends LeosJavaScriptExtension {

    private static final long serialVersionUID = 1L;

    private final EventBus eventBus;

    public HContainerExtension(T target, EventBus eventBus, String checkBoxAttributeName
            , String checkedBoxAttributeValue, String uncheckedBoxAttributeValue) {
        super();
        this.eventBus =  eventBus;
        registerServerSideAPI();
        // These values should be configurable (In structure file ?)
        // Be carefull of css (_hContainer.scss file)
        getState().checkBoxAttributeName = checkBoxAttributeName;
        getState().checkedBoxAttributeValue = checkedBoxAttributeValue;
        getState().uncheckedBoxAttributeValue = uncheckedBoxAttributeValue;
        extend(target);
    }

    private void registerServerSideAPI() {
        addFunction("saveElement", new JavaScriptFunction() {
            @Override
            public void call(JsonArray arguments) {
                LOG.trace("Saving element...");
                JsonObject data = arguments.get(0);
                String elementId = data.getString("elementId");
                String elementTagName = data.getString("elementType");
                String attributeName = data.getString("attributeName");
                String attributeValue = data.getString("attributeValue");
                eventBus.post(new SaveElementAttributeRequestEvent(elementId, elementTagName, attributeName, attributeValue));
            }
        });
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

    @Override
    protected HContainerState getState() {
        return (HContainerState)super.getState();
    }

    @Override
    protected HContainerState getState(boolean markAsDirty) {
        return (HContainerState)super.getState(markAsDirty);
    }
}
