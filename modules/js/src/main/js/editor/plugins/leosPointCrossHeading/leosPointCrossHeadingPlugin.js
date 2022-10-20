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
define(function leosPointCrossHeadingPluginModule(require) {
    "use strict";

    // load module dependencies
    var leosPluginUtils = require("plugins/leosPluginUtils");
    var pluginTools = require("plugins/pluginTools");
    var log = require("logger");
    var leosPointCrossHeadingHelper = require("./leosPointCrossHeadingHelper");
    var leosKeyHandler = require("plugins/leosKeyHandler/leosKeyHandler");
    var CKEDITOR = require("promise!ckEditor");
    var pluginName = "leosPointCrossHeading";
    var TRISTATE_DISABLED = CKEDITOR.TRISTATE_DISABLED;
    var POINT_CMD_NAME = "leosCrossHeadingPoint";

    var ENTER_KEY = 13;
    var SHIFT_ENTER = CKEDITOR.SHIFT + ENTER_KEY;

    var iconPointCrossheading = 'icons/point2crossheading.png';

    var pluginDefinition = {
        init : function init(editor) {
            log.debug("Initializing Crossheading <-> Point plugin...");

            editor.ui.addButton(POINT_CMD_NAME, {
                label: 'Crossheading <-> Point',
                command: POINT_CMD_NAME,
                toolbar: 'unumberedList',
                icon: this.path + iconPointCrossheading
            });

            editor.addCommand(POINT_CMD_NAME, {
                exec: function(editor) {
                    _execCmd(this, editor);
                },
                refresh: function( editor, path ) {
                    _refresh(this, editor, path);
                }
            });

            editor.on('selectionChange', _selectionChange);

            leosKeyHandler.on({
                editor : editor,
                eventType : 'key',
                key : ENTER_KEY,
                action : _onBlockKey
            });
            leosKeyHandler.on({
                editor : editor,
                eventType : 'key',
                key : SHIFT_ENTER,
                action : _onBlockKey
            });
        }
    };

    pluginTools.addPlugin(pluginName, pluginDefinition);

    function _onBlockKey(context) {
        let path = context.event.editor.elementPath();
        let element = getCrossHeading(path);
        if (isCrossHeadingInList(element)) {
            context.event.cancel();
        }
    }

    function _execCmd(cmd, editor) {
        cmd.refresh( editor, editor.elementPath() );

        let path = editor.elementPath();
        let elt = getCrossHeading(path);
        if (cmd.state != TRISTATE_DISABLED) {
            _updateEditor(cmd, editor, elt);
        }
    }

    function _refresh(cmd, editor, path) {
        let element = getCrossHeading(path);
        if (isCrossHeadingInList(element)) {
            let currentConfig = leosPointCrossHeadingHelper.getCurrentConfigFromAttributes(editor, element);
            if (currentConfig.rootEltCrossheadingType == leosPluginUtils.LIST) {
                cmd.setState(CKEDITOR.TRISTATE_OFF);
            } else {
                cmd.setState(CKEDITOR.TRISTATE_ON);
            }
        } else if (isPoint(element)) {
            cmd.setState(CKEDITOR.TRISTATE_ON);
        } else {
            cmd.setState(CKEDITOR.TRISTATE_DISABLED);
        }
    }

    function getCrossHeading(path) {
        return getCrossHeadingAttrInElement(path, 'li, p');
    }

    function getCrossHeadingAttrInElement(path, selector) {
        var element = path.lastElement.$.closest(selector);
        return element ? new CKEDITOR.dom.element(element) : null;
    }

    function isCrossHeadingInList(element) {
        return element && element.getName().toLowerCase() == leosPluginUtils.HTML_POINT && element.getAttribute(leosPluginUtils.DATA_AKN_ELEMENT) && element.getAttribute(leosPluginUtils.DATA_AKN_ELEMENT).toLowerCase() === leosPluginUtils.CROSSHEADING.toLowerCase();
    }

    function isPoint(element) {
        return element && element.getAttribute(leosPluginUtils.DATA_AKN_ELEMENT) && (element.getAttribute(leosPluginUtils.DATA_AKN_ELEMENT).toLowerCase() === leosPluginUtils.POINT
            ||  element.getAttribute(leosPluginUtils.DATA_AKN_ELEMENT).toLowerCase() === leosPluginUtils.INDENT);
    }

    function _updateEditor(cmd, editor, elt) {
        _updateContent(cmd, editor, elt);
    }

    function _updateContent(cmd, editor, elt) {
        var newConfig = {};
        if (cmd.state == CKEDITOR.TRISTATE_OFF) {
            newConfig.rootEltCrossheadingType = 'none';
        } else {
            newConfig.rootEltCrossheadingType = leosPluginUtils.LIST;
        }
        leosPointCrossHeadingHelper.updateRootEltAttributes(editor, newConfig, elt);
    }

    function _selectionChange(event) {
        event.editor.getCommand(POINT_CMD_NAME).refresh( event.editor, event.editor.elementPath() );
    }

    // return plugin module
    var pluginModule = {
        name : pluginName
    };

    return pluginModule;
});