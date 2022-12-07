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
import eu.europa.ec.leos.web.event.view.document.SaveElementRequestEvent;
import eu.europa.ec.leos.web.support.LeosCacheToken;
import org.apache.commons.lang3.Validate;

@JavaScript({"vaadin://../js/ui/extension/checkBoxesConnector.js"+ LeosCacheToken.TOKEN })
public class CheckBoxesExtension<T extends AbstractField<V>, V> extends LeosJavaScriptExtension {

    private static final long serialVersionUID = 1L;

    private final EventBus eventBus;

    public CheckBoxesExtension(T target, EventBus eventBus, String checkBoxTagName
            , String checkedBoxValue, String uncheckedBoxValue) {
        super();
        this.eventBus =  eventBus;
        registerServerSideAPI();
        // These values should be configurable (In structure file ?)
        getState().checkBoxTagName = checkBoxTagName;
        getState().checkedBoxValue = checkedBoxValue;
        getState().uncheckedBoxValue = uncheckedBoxValue;
        extend(target);
    }

    private void registerServerSideAPI() {
        addFunction("saveElement", new JavaScriptFunction() {
            @Override
            public void call(JsonArray arguments) {
                LOG.trace("Saving element...");
                JsonObject data = arguments.get(0);
                String elementId = data.getString("elementId");
                String elementType = data.getString("elementType");
                String elementFragment = data.getString("elementFragment");
                eventBus.post(new SaveElementRequestEvent(elementId, elementType, elementFragment, false));
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
    protected CheckBoxesState getState() {
        return (CheckBoxesState)super.getState();
    }

    @Override
    protected CheckBoxesState getState(boolean markAsDirty) {
        return (CheckBoxesState)super.getState(markAsDirty);
    }
}
