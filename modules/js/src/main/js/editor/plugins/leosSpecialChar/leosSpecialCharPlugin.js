/*
 * Copyright 2018 European Commission
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
define(function leosSpecialCharPluginModule(require) {
    "use strict";

    // load module dependencies
    var pluginTools = require("plugins/pluginTools");
    var leosCommandStateHandler = require("plugins/leosCommandStateHandler/leosCommandStateHandler");

    var pluginName = "leosSpecialChar";

    var changeStateElements = {
        crossHeading: {
            elementName: 'p',
            selector: '[data-akn-name=crossHeading]'
        }
    };

    var pluginDefinition = {
        requires: "specialchar",
        init: function init(editor) {
            editor.on('selectionChange', _onSelectionChange);
        }
    };

    function _onSelectionChange(event) {
        leosCommandStateHandler.changeCommandState(event, 'specialchar', changeStateElements, true);
    }

    pluginTools.addPlugin(pluginName, pluginDefinition);

    // return plugin module
    var pluginModule = {
        name: pluginName
    };

    return pluginModule;
});