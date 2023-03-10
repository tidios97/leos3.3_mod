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
define(function aknHtmlSuperScriptPluginModule(require) {
    "use strict";
    // load module dependencies
    var pluginTools = require("plugins/pluginTools");
    var pluginName = "aknHtmlSuperScript";
    var leosCommandStateHandler = require("plugins/leosCommandStateHandler/leosCommandStateHandler");

    var pluginDefinition = {
        init: function init(editor) {
            editor.on('selectionChange', _onSelectionChange, null, null, 11);
        }
    };

    pluginTools.addPlugin(pluginName, pluginDefinition);
    var transformationConfig = {
        akn: "sup",
        html: "sup",
        attr: [{
            akn: "xml:id",
            html: "id"
        }, {
            akn : "leos:origin",
            html : "data-origin"
        }],
        sub: {
            akn: "text",
            html: "sup/text"
        }
    };
    pluginTools.addTransformationConfigForPlugin(transformationConfig, pluginName);

    function _onSelectionChange(event) {
        leosCommandStateHandler.changeCommandState(event, "superscript");
    }

    // return plugin module
    var pluginModule = {
        name: pluginName,
        transformationConfig: transformationConfig,
        specificConfig: {
            coreStyles_superscript : { element: 'sup' ,alwaysRemoveElement: true }
        }
    };
    return pluginModule;
});