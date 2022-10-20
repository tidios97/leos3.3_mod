/*
 * Copyright 2021 European Commission
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
define(function aknDivisionNumPluginModule(require) {
    "use strict";

    // load module dependencies
    var pluginTools = require("plugins/pluginTools");
    var leosKeyHandler = require("plugins/leosKeyHandler/leosKeyHandler");
    var pluginName = "aknDivisionNum";
    var ENTER_KEY = 13;
    var SHIFT_ENTER = CKEDITOR.SHIFT + ENTER_KEY;
    var pluginDefinition = {
        requires : "widget,leosWidget",
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
            editor.on("instanceReady",_removeTextNodes);
            editor.on('afterCommandExec', _removeTextNodes, null, null, 1);
            editor.on("save", _setAutoNumOverwrite);
        }
    };

    function _onEnterKey(context) {
        context.event.cancel();
    }

    function _onShiftEnterKey(context) {
        context.event.cancel();
    }

    function _setAutoNumOverwrite(evt) {
        var parentDivision = evt.editor.editable().getAscendant('division');
        if (parentDivision) {
            parentDivision.setAttribute('leos:auto-num-overwrite', 'true');
        }
    }

    function _removeTextNodes(evt) {
        var editor = evt.editor;
        editor.editable().getChildren().toArray().forEach(function (element) {
            if (element.type === CKEDITOR.NODE_TEXT) {
                element.remove();
            }
        })
        editor.fire("focus");
    }


    pluginTools.addPlugin(pluginName, pluginDefinition);

    var transformationConfig = {
        akn : "num",
        html : "p[data-akn-name=aknDivisionNum]",
        attr : [  {
            akn : "leos:origin",
            html : "data-num-origin"
        }, {
            akn : "xml:id",
            html : "data-akn-num-id"
        }, {
            html : "contenteditable=true"
        }, {
            html: "data-akn-name=aknDivisionNum"
        }],
        sub : {
            akn : "text",
            html : "p/text"
        }

    };
    // return plugin module
    var pluginModule = {
        name : pluginName,
        transformationConfig : transformationConfig
    };

    pluginTools.addTransformationConfigForPlugin(transformationConfig, pluginName);

    return pluginModule;
});