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
define(function hContainerExtensionModule(require) {
    "use strict";

    var log = require("logger");
    var UTILS = require("core/leosUtils");

    var UNCHECKED;
    var CHECKED;
    var CHECKBOX_ATTR;
    var DIV = "div";
    var NAME = "name";
    var HCONTAINER = "hcontainer";
    var CHECKBOX = "checkbox";
    var divStyle = {'style':'width: 30px; height: 30px; display: inline-block; cursor: pointer;'};

    function _initExtension(connector) {
        log.debug("Initializing hcontainer extension...");

        // restrict scope to the extended target
        connector.target = UTILS.getParentElement(connector);

        log.debug("Registering hcontainer extension unregistration listener...");
        connector.onUnregister = _connectorUnregistrationListener;

        log.debug("Registering hcontainer extension state change listener...");
        connector.onStateChange = _connectorStateChangeListener;

        _initAndAddListenersToHContainer(connector);
    }

    // handle connector unregistration on client-side
    function _connectorUnregistrationListener() {
        var connector = this;
        log.debug("Unregistering hcontainer extension...");
        // clean connector
        connector.target = null;
    }

    // handle connector state change on client-side
    function _connectorStateChangeListener() {
        var connector = this;
        _initAndAddListenersToHContainer(connector);
        log.debug("hcontainer extension state changed...");
    }

    function doCheck(connector, event) {
        event.stopImmediatePropagation();
        var hContainerName = this.attr(CHECKBOX_ATTR);
        if (hContainerName == UNCHECKED) {
            this.attr(CHECKBOX_ATTR, CHECKED);
        } else {
            this.attr(CHECKBOX_ATTR, UNCHECKED);
        }
        var data = {
            elementId: this.attr("id"),
            elementType: this.prop("tagName").toLowerCase(),
            attributeName: CHECKBOX_ATTR,
            attributeValue: this.attr(CHECKBOX_ATTR)
        };
        connector.saveElement(data);
    }

    function _initAndAddListenersToHContainer(connector) {
        CHECKBOX_ATTR = connector.getState().checkBoxAttributeName;
        CHECKED = connector.getState().checkedBoxAttributeValue;
        UNCHECKED = connector.getState().uncheckedBoxAttributeValue;
        _addListeners(connector, CHECKBOX_ATTR, CHECKED);
        _addListeners(connector, CHECKBOX_ATTR, UNCHECKED);
    }

    function _addListeners(connector, attributeName, attributeValue) {
        var target = connector.target;
        $(target).find(HCONTAINER + "[" + attributeName + "='" + attributeValue + "']").each(function( index ) {
            var div = $($(this).children()[0]);
            if (div.prop("tagName").toLowerCase() != DIV || div.attr(NAME) != CHECKBOX) {
                div = $('<' + DIV + ' ' + NAME + '="' + CHECKBOX + '"/>').attr(divStyle);
                var cssCheckBoxWidth = window.getComputedStyle(this, '::before').width;
                div.get(0).style.marginLeft = '-' + cssCheckBoxWidth;
                $(this).prepend(div);
            }
            div.click(doCheck.bind($(this), connector));
        });
    }

    return {
        init: _initExtension
    };
});
