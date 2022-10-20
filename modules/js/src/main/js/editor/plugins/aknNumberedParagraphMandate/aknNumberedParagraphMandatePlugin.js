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
define(function aknNumberedParagraphMandatePluginModule(require) {
    "use strict";

    // load module dependencies
    var pluginTools = require("plugins/pluginTools");
    var leosKeyHandler = require("plugins/leosKeyHandler/leosKeyHandler");
    var leosHierarchicalElementTransformerStamp = require("plugins/leosHierarchicalElementTransformer/hierarchicalElementTransformer");

    var ENTER_KEY = 13;
    var SHIFT_ENTER = CKEDITOR.SHIFT + ENTER_KEY;
    var UNDERLINE = CKEDITOR.CTRL + 85;

    var pluginName = "aknNumberedParagraphMandate";


    var pluginDefinition = {
        icons: pluginName.toLowerCase(),
        init : function init(editor) {

            leosKeyHandler.on({
                editor : editor,
                eventType : 'key',
                key : ENTER_KEY,
                action : _onEnterKey
            });

            leosKeyHandler.on({
                editor : editor,
                eventType : 'key',
                key : SHIFT_ENTER,
                action : _onShiftEnterKey
            });

            leosKeyHandler.on({
                editor : editor,
                eventType : 'key',
                key : UNDERLINE,
                action : _onCtrlUKey
            });

            editor.on("toDataFormat", _transformParagraph, null, null, 15);
        }
    };

    function _onEnterKey(context) {
        context.event.cancel();
    }

    function _onShiftEnterKey(context) {
        context.event.cancel();
    }

    function _onCtrlUKey(context) {
        context.event.cancel();
    }

    function _transformParagraph(event) {
        if (!event.data.dataValue.includes("</num>") && event.data.dataValue.includes("<subparagraph>")) {
            event.data.dataValue = event.data.dataValue.replace("<subparagraph>", "").replace("</paragraph>", "").replace(/subparagraph>/g, "paragraph>").replace(/<subparagraph /g, "<paragraph ");
        }
    }

    pluginTools.addPlugin(pluginName, pluginDefinition);

    var leosHierarchicalElementTransformer = leosHierarchicalElementTransformerStamp({

        firstLevelConfig: {
            akn: 'paragraph',
            html: 'ol[data-akn-name=aknNumberedParagraphMandate]',
            attr: [{
                html: "data-akn-name=aknNumberedParagraphMandate"
            }]
        },
        rootElementsForFrom: ["paragraph"],
        contentWrapperForFrom: "subparagraph",
        rootElementsForTo: ["ol","li"]
    });

    var transformationConfig = leosHierarchicalElementTransformer.getTransformationConfig();

    // return plugin module
    var pluginModule = {
        name : pluginName,
        transformationConfig : transformationConfig
    };

    pluginTools.addTransformationConfigForPlugin(transformationConfig, pluginName);

    return pluginModule;
});