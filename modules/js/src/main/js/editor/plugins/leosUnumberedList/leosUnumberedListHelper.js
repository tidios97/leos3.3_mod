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
define(function leosUnumberedListHelper(require) {
    "use strict";

    let leosPluginUtils = require("plugins/leosPluginUtils");
    let DATA_AKN_NUM_ID = "data-akn-num-id";
    let DATA_AKN_NUM_ORIGIN = "data-num-origin";
    // Crossheading numbering configs
    let NONE_NUMBERING_CONFIG_TYPE = "NONE";
    let BULLET_NUMBERING_CONFIG_TYPE = "BULLET_BLACK_CIRCLE";
    let INDENT_NUMBERING_CONFIG_TYPE = "INDENT";

    function _getCrossheadingCurrentConfigFromNumAttribute(editor, element, numberingConfigs) {
        let currentConfig = numberingConfigs.find(numberingConfig => numberingConfig.type == NONE_NUMBERING_CONFIG_TYPE);
        if (!element || element === undefined) {
            return currentConfig;
        }
        if (element.hasAttribute(leosPluginUtils.DATA_AKN_NUM)) {
            let numValue = element.getAttribute(leosPluginUtils.DATA_AKN_NUM);
            if (numValue != null && numValue != "") {
                currentConfig = numberingConfigs.find(numberingConfig => numberingConfig.sequence != null && numberingConfig.sequence == numValue);
            }
        }
        return currentConfig;
    }

    function _updateCrossheadingNumAttributes(editor, currentConfig, element) {
        let id = element.getAttribute("data-akn-id");
        var rootElt = editor.element.find("*[data-akn-id='" + id + "']").$[0];
        if (!id) {
            id = element.getAttribute("id");
            rootElt = editor.element.find("*[id='" + id + "']").$[0];
        }
        if (!id) {
            rootElt = element.$;
        }
        if (currentConfig.type == NONE_NUMBERING_CONFIG_TYPE) {
            rootElt.removeAttribute(leosPluginUtils.DATA_AKN_NUM);
            rootElt.removeAttribute(DATA_AKN_NUM_ID);
            rootElt.removeAttribute(DATA_AKN_NUM_ORIGIN);
            leosPluginUtils.removeCrossheadingNumProperty(rootElt);
        } else {
            rootElt.setAttribute(leosPluginUtils.DATA_AKN_NUM, currentConfig.sequence);
            leosPluginUtils.setCrossheadingNumProperty(rootElt, currentConfig.sequence);
        }
    }

    return {
        getCrossheadingCurrentConfigFromNumAttribute: _getCrossheadingCurrentConfigFromNumAttribute,
        updateCrossheadingNumAttributes : _updateCrossheadingNumAttributes,
        NONE_NUMBERING_CONFIG_TYPE : NONE_NUMBERING_CONFIG_TYPE,
        BULLET_NUMBERING_CONFIG_TYPE : BULLET_NUMBERING_CONFIG_TYPE,
        INDENT_NUMBERING_CONFIG_TYPE : INDENT_NUMBERING_CONFIG_TYPE,
    };
});