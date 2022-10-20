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
define(function aknBlockHeadingPluginModule(require) {
    "use strict";

    // load module dependencies
    var pluginTools = require("plugins/pluginTools");
    var leosKeyHandler = require("plugins/leosKeyHandler/leosKeyHandler");
    var pluginName = "aknBlockHeading";

    var ENTER_KEY = 13;
    var SHIFT_ENTER = CKEDITOR.SHIFT + ENTER_KEY;
    var UNDERLINE = CKEDITOR.CTRL + 85;
    var BOLD = CKEDITOR.CTRL + 66;
    var ITALIC = CKEDITOR.CTRL + 73;
    var DATA_AKN_NAME = "data-akn-name";
    var CROSS_HEADING = "crossHeading";

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

            leosKeyHandler.on({
                editor : editor,
                eventType : 'key',
                key : UNDERLINE,
                action : _onCtrlUKey
            });

            leosKeyHandler.on({
                editor : editor,
                eventType : 'key',
                key : BOLD,
                action : _onCtrlBKey
            });

            leosKeyHandler.on({
                editor : editor,
                eventType : 'key',
                key : ITALIC,
                action : _onCtrlIKey
            });
        }
    };

    function _onEnterKey(context) {
        var selection = context.event.editor.getSelection();
        var startElement = leosKeyHandler.getSelectedElement(selection);
        if(startElement.getAttribute(DATA_AKN_NAME) === CROSS_HEADING) {
            context.event.cancel();
        }
    }

    function _onShiftEnterKey(context) {
        var selection = context.event.editor.getSelection();
        var startElement = leosKeyHandler.getSelectedElement(selection);
        if(startElement.getAttribute(DATA_AKN_NAME) === CROSS_HEADING) {
            context.event.cancel();
        }
    }

    function _onCtrlUKey(context) {
        context.event.cancel();
    }

    function _onCtrlBKey(context) {
        context.event.cancel();
    }

    function _onCtrlIKey(context) {
        context.event.cancel();
    }

    pluginTools.addPlugin(pluginName, pluginDefinition);

    var transformationConfig = {
        akn: "block",
        html: "p",
        attr : [ {
            akn: "leos:name",
            html : "data-akn-name"
        }, {
            html : "data-akn-element=crossHeading"
        }, {
            akn : "leos:origin",
            html : "data-origin"
        }, {
            akn : "leos:indent-level",
            html : "data-indent-level"
        }, {
            akn : "style",
            html : "style"
        }, {
            akn : "xml:id",
            html : "data-akn-id"
        }, {
            akn : "leos:softuser",
            html : "data-akn-attr-softuser"
        }, {
            akn : "leos:softdate",
            html : "data-akn-attr-softdate"
        }],
        sub: [{
            akn: "inline[name=crossHnum]",
            html: "p",
            attr: [{
                akn: "name=crossHnum"
            },{
                akn: "xml:id",
                html: "data-akn-num-id"
            }, {
                akn : "leos:origin",
                html : "data-num-origin"
            }],
            sub: {
                akn: "text",
                html: "p[data-akn-num]"
            }
        }, {
            akn: "text",
            html: "p/text"
        }]
    };

    pluginTools.addTransformationConfigForPlugin(transformationConfig, pluginName);

    // return plugin module
    var pluginModule = {
        name: pluginName,
        transformationConfig: transformationConfig
    };

    return pluginModule;
});