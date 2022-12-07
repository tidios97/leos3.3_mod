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
; // jshint ignore:line
define(function checkBoxesExtensionModule(require) {
    "use strict";

    let log = require("logger");
    let UTILS = require("core/leosUtils");

    let UNCHECKED;
    let CHECKED;
    let CHECKBOX_TAGNAME;
    const NUM = "num";

    function _initExtension(connector) {
        log.debug("Initializing checkBoxes extension...");

        // restrict scope to the extended target
        connector.target = UTILS.getParentElement(connector);

        log.debug("Registering checkBoxes extension unregistration listener...");
        connector.onUnregister = _connectorUnregistrationListener;

        log.debug("Registering checkBoxes extension state change listener...");
        connector.onStateChange = _connectorStateChangeListener;

        _initAndAddListenersToCheckBoxes(connector);
    }

    // handle connector unregistration on client-side
    function _connectorUnregistrationListener() {
        let connector = this;
        log.debug("Unregistering checkBoxes extension...");
        // clean connector
        connector.target = null;
    }

    // handle connector state change on client-side
    function _connectorStateChangeListener() {
        let connector = this;
        _initAndAddListenersToCheckBoxes(connector);
        log.debug("checkBoxes extension state changed...");
    }

    function _toggleCheckBox(connector, event) {
        event.stopImmediatePropagation();
        let checkBoxValue = this.text();
        checkBoxValue == UNCHECKED ? this.text(CHECKED) : this.text(UNCHECKED);
        let data = {
            elementId: this.attr("id"),
            elementType: this.prop("tagName").toLowerCase(),
            elementFragment: this.prop('outerHTML').replace(" id=", " xml:id="),
        };
        connector.saveElement(data);
    }

    function _initAndAddListenersToCheckBoxes(connector) {
        CHECKBOX_TAGNAME = connector.getState().checkBoxTagName;
        CHECKED = connector.getState().checkedBoxValue;
        UNCHECKED = connector.getState().uncheckedBoxValue;
        _addListeners(connector, CHECKBOX_TAGNAME, CHECKED);
        _addListeners(connector, CHECKBOX_TAGNAME, UNCHECKED);
    }

    function _addListeners(connector, tagName, value) {
        let target = connector.target;
        $(target).find(tagName).each(function( index ) {
            if ($(this).children(NUM).length > 0) {
                let num = $($(this).children(NUM)[0]);
                if (!!num && num.text() == value) {
                    num[0].addEventListener("click", _toggleCheckBox.bind(num, connector));
                }
            }
        });
    }

    return {
        init: _initExtension
    };
});
