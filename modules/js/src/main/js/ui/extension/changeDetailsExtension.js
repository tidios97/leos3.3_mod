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
define(function changeDetailsExtensionModule(require) {
    "use strict";

    // load module dependencies
    var log = require("logger");
    var $ = require("jquery");
    var UTILS = require("core/leosUtils");

    function _init(connector) {
        log.debug("Initializing Change details extension...");
        // restrict scope to the extended target
        connector.target = UTILS.getParentElement(connector);

        log.debug("Registering Change details unregistration listener...");
        connector.onUnregister = _connectorUnregistrationListener;

        log.debug("Registering Chnage details state change listener...");
        connector.onStateChange = _connectorStateChangeListener;

        connector.enableChangeDetails = _enableChangeDetails;
    }

    // handle connector state change on client-side
    function _connectorStateChangeListener() {
        var connector = this;
        log.debug("Change details extension state changed...");
        // KLUGE delay execution due to sync issues with target update
        setTimeout(_enableChangeDetails, 500, connector);
    }

    function _enableChangeDetails(connector) {
        if (connector) {
            const enable = connector.getState(false).changeDetailsEnabled;
            log.debug("Change details request with value: " + enable);
            _removeChangeDetails();
            if (enable) {
                _registerActionTriggers();
            }
        }
    }

    function _registerActionTriggers() {
        const changedElements = $('#docContainer [' + $.escapeSelector('leos:softuser') + ']');
        changedElements.each(function(index) {
            const $element = $(changedElements[index]);
            _injectChangeDetails($element);
        });
    }

    function _injectChangeDetails($target) {
        const target = $target[0];
        if (target) {
            const $children = $target.children(':not(num, aknp, content, div, span)');
            const skipElements = ['content'];
            if ((!$children.length || $children.filter('heading').length) && !skipElements.includes(target.localName)) {
                const softUser = target.getAttribute('leos:softuser');
                const softDate = target.getAttribute('leos:softdate');
                //TODO: Use i18n
                const content = "Changed by: ".concat(softUser ? softUser : 'Unknown').concat(" on ")
                    .concat(UTILS.formatDate(softDate ? new Date(softDate) : new Date()));
                const wrapper = "<changedetails class='change-details-block'><span>[" + content + "]</span></changedetails>";
                $target.after($(wrapper).css('paddingLeft', $target.css('paddingLeft')));
            }
        }
    }

    function _removeChangeDetails() {
        log.debug("Removing all change details..");
        $("changedetails").remove();
    }

    // handle connector unregistration on client-side
    function _connectorUnregistrationListener() {
        var connector = this;
        log.debug("Unregistering change details extension...");
        // clean connector
        connector.changeDetailsEnabled = null;
    }

    return {
        init: _init
    };
});
