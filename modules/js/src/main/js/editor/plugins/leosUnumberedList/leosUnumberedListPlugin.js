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
define(function leosListCrossHeadingPluginModule(require) {
    "use strict";

    // load module dependencies
    var pluginTools = require("plugins/pluginTools");
    var leosPluginUtils = require("plugins/leosPluginUtils");
    var log = require("logger");
    var unumberModule = require("plugins/leosUnumber/listUnumberModule");
    var numberModule = require("plugins/leosNumber/listItemNumberModule");
    var leosUnumberedListHelper = require("./leosUnumberedListHelper");
    var CKEDITOR = require("promise!ckEditor");
    var pluginName = "leosUnumberedList";
    var TRISTATE_DISABLED = CKEDITOR.TRISTATE_DISABLED;
    var BULLET_LIST_CMD_NAME = "leosBulletList";
    var INDENT_LIST_CMD_NAME = "leosIndentList";
    var NUMBERED_LIST_CMD_NAME = "leosNumberedList";

    var iconBulletList = 'icons/bulletedlist.png';
    var iconIndentList = 'icons/indentedlist.png';
    var iconNumberedList = 'icons/numberedlist.png';

    var ORDERED_LIST_SELECTOR = "ol[data-akn-name='aknAnnexOrderedList']";

    var pluginDefinition = {
        init : function init(editor) {
            log.debug("Initializing List plugin...");

            editor.ui.addButton(BULLET_LIST_CMD_NAME, {
                label: 'Set to Bullet List',
                command: BULLET_LIST_CMD_NAME,
                toolbar: 'unumberedList',
                icon: this.path + iconBulletList
            });

            editor.addCommand(BULLET_LIST_CMD_NAME, {
                exec: function(editor) {
                    _execCmd(this, editor);
                },
                refresh: function( editor, path ) {
                    _refresh(this, editor, path);
                }
            });

            editor.ui.addButton(INDENT_LIST_CMD_NAME, {
                label: 'Set to Indent List',
                command: INDENT_LIST_CMD_NAME,
                toolbar: 'unumberedList',
                icon: this.path + iconIndentList
            });

            editor.addCommand(INDENT_LIST_CMD_NAME, {
                exec: function(editor) {
                    _execCmd(this, editor);
                },
                refresh: function( editor, path ) {
                    _refresh(this, editor, path);
                }
            });

            editor.ui.addButton(NUMBERED_LIST_CMD_NAME, {
                label: 'Set to Numbered List',
                command: NUMBERED_LIST_CMD_NAME,
                toolbar: 'unumberedList',
                icon: this.path + iconNumberedList
            });

            editor.addCommand(NUMBERED_LIST_CMD_NAME, {
                exec: function(editor) {
                    _execCmd(this, editor);
                },
                refresh: function( editor, path ) {
                    _refresh(this, editor, path);
                }
            });

            editor.on('selectionChange', _selectionChange);
            editor.on('change', _selectionChange);
        }
    };

    pluginTools.addPlugin(pluginName, pluginDefinition);

    function _execCmd(cmd, editor) {
        let path = editor.elementPath();
        let elt = getPointOrCrossHeading(path);
        if (cmd.state != TRISTATE_DISABLED) {
            _updateEditor(cmd, editor, elt);
        }
        cmd.refresh( editor, editor.elementPath() );

        // Clean up, restore selection and update toolbar button states.
        editor.focus();
        editor.forceNextSelectionCheck();
    }

    function _refresh(cmd, editor, path) {
        let element = getPointOrCrossHeading(path);
        if (!isCrossHeading(element) && isIndent(element)) {
            var currentList = getCurrentList(element);
            if (currentList.length > 0) {
                var numberingConfig = unumberModule.identifyNumberingConfigFromList(currentList[0]);
                if ((!numberingConfig || numberingConfig.numbered) && cmd.name.toLowerCase() == NUMBERED_LIST_CMD_NAME.toLowerCase()) {
                    cmd.setState(CKEDITOR.TRISTATE_ON);
                } else if (numberingConfig != null && numberingConfig.type.toLowerCase() === unumberModule.INDENT
                    && cmd.name.toLowerCase() == INDENT_LIST_CMD_NAME.toLowerCase()) {
                    cmd.setState(CKEDITOR.TRISTATE_ON);
                } else if (numberingConfig != null && numberingConfig.type.toLowerCase() === unumberModule.BULLET_NUM
                    && cmd.name.toLowerCase() == BULLET_LIST_CMD_NAME.toLowerCase()) {
                    cmd.setState(CKEDITOR.TRISTATE_ON);
                } else {
                    cmd.setState(CKEDITOR.TRISTATE_OFF);
                }
            } else {
                cmd.setState(CKEDITOR.TRISTATE_OFF);
            }
      } else if (isCrossHeading(element) && !isIndent(element)) {
            editor.getCommand(NUMBERED_LIST_CMD_NAME).setState(CKEDITOR.TRISTATE_DISABLED);
            let currentConfig = leosUnumberedListHelper.getCrossheadingCurrentConfigFromNumAttribute(editor, element, editor.LEOS.numberingConfigs);
            if (currentConfig.type == leosUnumberedListHelper.BULLET_NUMBERING_CONFIG_TYPE && cmd.name.toLowerCase() == BULLET_LIST_CMD_NAME.toLowerCase()) {
                cmd.setState(CKEDITOR.TRISTATE_ON);
            } else if (currentConfig.type == leosUnumberedListHelper.INDENT_NUMBERING_CONFIG_TYPE && cmd.name.toLowerCase() == INDENT_LIST_CMD_NAME.toLowerCase()) {
                cmd.setState(CKEDITOR.TRISTATE_ON);
            } else if (cmd.name.toLowerCase() == NUMBERED_LIST_CMD_NAME.toLowerCase()) {
                cmd.setState(CKEDITOR.TRISTATE_DISABLED);
            } else {
                cmd.setState(CKEDITOR.TRISTATE_OFF);
            }
        } else {
            cmd.setState(CKEDITOR.TRISTATE_DISABLED);
        }
    }

    function getPointOrCrossHeading(path) {
        return getPointOrCrossHeadingFromSelector(path, 'p, li');
    }

    function getPointOrCrossHeadingFromSelector(path, selector) {
        if (!!path.lastElement) {
            var element = path.lastElement.$.closest(selector);
            return element ? new CKEDITOR.dom.element(element) : null;
        }
        return null;
    }

    function isCrossHeading(element) {
        return element && element.getAttribute(leosPluginUtils.DATA_AKN_ELEMENT) && element.getAttribute(leosPluginUtils.DATA_AKN_ELEMENT).toLowerCase() === leosPluginUtils.CROSSHEADING.toLowerCase();
    }

    function isIndent(element) {
        if (element && element.getAttribute(leosPluginUtils.DATA_AKN_ELEMENT)) {
            if (element.getAttribute(leosPluginUtils.DATA_AKN_ELEMENT).toLowerCase() === leosPluginUtils.ALINEA.toLowerCase()) {
                element = element.getAscendant(leosPluginUtils.HTML_POINT);
            }
            return (element.getAttribute(leosPluginUtils.DATA_AKN_ELEMENT).toLowerCase() === leosPluginUtils.INDENT.toLowerCase()
                || element.getAttribute(leosPluginUtils.DATA_AKN_ELEMENT).toLowerCase() === leosPluginUtils.POINT.toLowerCase());
        }
        return false;
    }

    function getCurrentList(elt) {
        return $(elt.$).closest(ORDERED_LIST_SELECTOR);
    }

    function _updateEditor(cmd, editor, elt) {
        _updateContent(cmd, editor, elt);
    }

    function _updateContent(cmd, editor, elt) {
        if (isCrossHeading(elt)) {
            var numberingConfigs = editor.LEOS.numberingConfigs;
            var newConfig = numberingConfigs.find(numberingConfig => numberingConfig.type == leosUnumberedListHelper.NONE_NUMBERING_CONFIG_TYPE);
            if (cmd.name == BULLET_LIST_CMD_NAME.toLowerCase() && cmd.state == CKEDITOR.TRISTATE_OFF) {
                newConfig = numberingConfigs.find(numberingConfig => numberingConfig.type == leosUnumberedListHelper.BULLET_NUMBERING_CONFIG_TYPE);
            } else if (cmd.name == INDENT_LIST_CMD_NAME.toLowerCase() && cmd.state == CKEDITOR.TRISTATE_OFF) {
                newConfig = numberingConfigs.find(numberingConfig => numberingConfig.type == leosUnumberedListHelper.INDENT_NUMBERING_CONFIG_TYPE);
            }
            editor.getCommand(NUMBERED_LIST_CMD_NAME).setState(CKEDITOR.TRISTATE_DISABLED);
            leosUnumberedListHelper.updateCrossheadingNumAttributes(editor, newConfig, elt);
        } else if (isIndent(elt)) {
            var impactedLists = getImpactedLists(elt);
            if (cmd.name == INDENT_LIST_CMD_NAME.toLowerCase()) {
                unumberModule.updateNumbers(impactedLists, unumberModule.INDENT);
            } else if (cmd.name == BULLET_LIST_CMD_NAME.toLowerCase()) {
                unumberModule.updateNumbers(impactedLists, unumberModule.BULLET_NUM);
            } else {
                numberModule.updateNumbers(impactedLists);
            }
        }
        _updateButtonState(cmd, editor);
    }

    function getImpactedLists(elt) {
        var allLists = [];
        var rootList = getRootList(elt.$);
        allLists.push(rootList);
        var childrenLists = $(rootList).find(ORDERED_LIST_SELECTOR);
        for (var ii = 0; ii < childrenLists.length; ii++) {
            allLists.push(childrenLists[ii]);
        }
        return allLists;
    }

    function getRootList(elt) {
        while ($(elt).parents(ORDERED_LIST_SELECTOR).length > 0) {
            elt = $(elt).parents(ORDERED_LIST_SELECTOR)[0];
        }
        return elt;
    }

    function _updateButtonState(cmd, editor) {
        if (cmd.name != BULLET_LIST_CMD_NAME.toLowerCase()) {
            editor.getCommand(BULLET_LIST_CMD_NAME).setState(CKEDITOR.TRISTATE_OFF);
        }
        if (cmd.name != INDENT_LIST_CMD_NAME.toLowerCase()) {
            editor.getCommand(INDENT_LIST_CMD_NAME).setState(CKEDITOR.TRISTATE_OFF);
        }
        if (cmd.name != NUMBERED_LIST_CMD_NAME.toLowerCase()) {
            editor.getCommand(NUMBERED_LIST_CMD_NAME).setState(CKEDITOR.TRISTATE_OFF);
        }
    }

    function _selectionChange(event) {
        event.editor.getCommand(BULLET_LIST_CMD_NAME).refresh( event.editor, event.editor.elementPath() );
        event.editor.getCommand(INDENT_LIST_CMD_NAME).refresh( event.editor, event.editor.elementPath() );
        event.editor.getCommand(NUMBERED_LIST_CMD_NAME).refresh( event.editor, event.editor.elementPath() );
    }

    // return plugin module
    var pluginModule = {
        name : pluginName
    };

    return pluginModule;
});