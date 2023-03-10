/*
 * Copyright 2017 European Commission
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
define(function leosEditorExtensionModule(require) {
    "use strict";

    // load module dependencies
    var log = require("logger");
    var postal = require("postal");
    var DiagnosticsWireTap = require("postal.diagnostics");
    var CONFIG = require("core/leosConfig");
    var UTILS = require("core/leosUtils");
    var actionHandler = require("./core/actionHandler");
    var toolbarPositionAdapter = require("./core/toolbarPositionAdapter");
    var elementEditor = require("./core/elementEditor");
    var bookmarkHandler = require("./core/bookmarkHandler");
    var $ = require("jquery");

    // configuration
    var EDITOR_CHANNEL_CFG = CONFIG.channels.editor;

    // handle extension initialization
    function _initExtension(connector) {
        log.debug("Initializing Leos Editor extension...");
        _setupEditorChannel(connector);
        actionHandler.setup(connector);
        toolbarPositionAdapter.setup(connector);
        elementEditor.setup(connector);
        bookmarkHandler.setup(connector);
        _registerUnLoadHandlers(connector);

        log.debug("Registering Leos Editor extension unregistration listener...");
        connector.onUnregister = _connectorUnregistrationListener;
    }

    function _setupEditorChannel(connector) {
        // create channel for editor
        connector.editorChannel = postal.channel(EDITOR_CHANNEL_CFG.name);
        // create wire tap for diagnostics
        if (EDITOR_CHANNEL_CFG.diagnostics) {
            connector.editorWireTap = new DiagnosticsWireTap({
                name: EDITOR_CHANNEL_CFG.name + "Logger",
                filters: [
                    {channel: EDITOR_CHANNEL_CFG.name}
                ],
                writer: function logMessage(msg) {
                    log.debug(EDITOR_CHANNEL_CFG.name, "channel message:\n", msg);
                }
            });
        }
    }

    // handle connector unregistration from server-side
    function _connectorUnregistrationListener() {
        var connector = this;
        log.debug("Unregistering Leos Editor extension...");
        bookmarkHandler.teardown(connector);
        elementEditor.teardown(connector);
        toolbarPositionAdapter.teardown(connector);
        actionHandler.teardown(connector);
        _teardownEditorChannel(connector);
        _unregisterUnLoadHandlers();
    }

    function _teardownEditorChannel(connector) {
        // remove editor wire tap
        if (connector.editorWireTap) {
            connector.editorWireTap.removeWireTap();
            connector.editorWireTap = null;
        }
        // clear editor channel
        connector.editorChannel = null;
        UTILS.clearItemStorage();
    }

    function _registerUnLoadHandlers(connector) {
        log.debug("Registering beforeunload handler...");
        $(window).on("beforeunload", function() {
            if (_isTextEditorOpen()) {
                return "Changes you made may not be saved."; // Browser shows it's own message and not this
            }
        });

        _registerOnPopStateHandler();
        log.debug("Registering unload handler...");
        $(window).on("unload", _closeBrowser.bind(undefined, connector));
    }

    function _registerOnPopStateHandler() {
        log.debug("Registering popstate handler...");
        // Add additional history state to prevent user from leaving the current page in case back button is pressed
        // and user would like to stay if open editors are detected.
        window.leosEditorState = { 'checkForOpenEditors': true };
        window.history.pushState(null, "", window.location.href);
        $(window).on('popstate', (jqueryEvent) => { _onPopState(jqueryEvent.originalEvent) });
    }

    function _onPopState(event) {
        event.preventDefault();
        if (!window.leosEditorState) {
            return;
        }
        if (!window.leosEditorState.checkForOpenEditors) {
            _goBack();
            return;
        }
        _checkForOpenEditors();
    }

    function _checkForOpenEditors() {
        log.debug('Check for open editor');
        if (!_isTextEditorOpen()) {
            _goBack();
            return;
        }

        log.debug('Open editor detected');
        let proceed = confirm('Changes you made may not be saved. Would you like to proceed?');
        log.debug(`Proceed: ${proceed}`);
        if (proceed) {
            _goBack();
            return;
        }
        window.history.pushState(null, "", window.location.href);
    }

    function _goBack() {
        window.leosEditorState = null;
        window.history.back();
    }

    function _isTextEditorOpen() {
        return $("akomantoso .cke_editable").is("[data-wrapped-id]")
            || $("div.leos-toc-tree-editable").length;
    }

    function _unregisterUnLoadHandlers() {
        log.debug("Unregistering unload handlers...");
        $(window).off("beforeunload");
        $(window).off("unload");
        $(window).off("popstate");
    }

    function _closeBrowser(connector) {
        // After the call to "closeBrowser" a delay it is needed to be added to allow
        // the call be launched. If this delay is not added the browser is closed without calling.
        if(connector && connector.closeBrowser) {
            connector.closeBrowser();
            _sleepFor(1000);
        }
        UTILS.clearItemStorage();
    }

    function _sleepFor(sleepDuration) {
        // Previously implemented with JS setTimeout, promises, etc. but it does not work when it is
        // called from browser "unload" event. Seems that JS native functions are aborted.
        var now = new Date().getTime();
        while (new Date().getTime() < now + sleepDuration) { /* do nothing */ }
    }

    return {
        init: _initExtension
    };
});
