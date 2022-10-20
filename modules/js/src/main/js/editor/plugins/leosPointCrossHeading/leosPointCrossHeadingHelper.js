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
define(function leosListCrossHeadingHelper(require) {
    "use strict";

    let leosPluginUtils = require("plugins/leosPluginUtils");

    let DATA_AKN_NUM_ID = "data-akn-num-id";
    let DATA_AKN_NUM_ORIGIN = "data-num-origin";

    function _getCurrentConfigFromAttributes(editor, element) {
        let currentConfig = {};
        if (!element || element === undefined) {
            return currentConfig;
        }
        if (element.hasAttribute(leosPluginUtils.CROSSHEADING_LIST_ATTR)) {
            currentConfig.rootEltCrossheadingType = element.getAttribute(leosPluginUtils.CROSSHEADING_LIST_ATTR);
        } else {
            currentConfig.rootEltCrossheadingType = 'none';
        }
        return currentConfig;
    }

    function _updateRootEltAttributes(editor, currentConfig, element) {
        let id = element.getAttribute("id");
        let rootElt = editor.element.find("li[id='" + id + "']").$[0];
        if (!id) {
            rootElt = element.$;
        }
        if (rootElt && currentConfig.rootEltCrossheadingType == "none") {
            rootElt.setAttribute(leosPluginUtils.DATA_AKN_ELEMENT, leosPluginUtils.POINT);
            rootElt.removeAttribute(leosPluginUtils.CROSSHEADING_LIST_ATTR);
            rootElt.removeAttribute(leosPluginUtils.DATA_AKN_NUM);
            rootElt.removeAttribute(leosPluginUtils.DATA_INDENT_LEVEL_ATTR);
            rootElt.removeAttribute(DATA_AKN_NUM_ID);
            rootElt.removeAttribute(DATA_AKN_NUM_ORIGIN);
        } else if (rootElt) {
            let olElement = rootElt.parentElement;
            let indentLevel = leosPluginUtils.getNestingLevelForOl(olElement);
            leosPluginUtils.setCrossheadingIndentAttribute(rootElt, indentLevel);
            rootElt.setAttribute(leosPluginUtils.CROSSHEADING_LIST_ATTR, currentConfig.rootEltCrossheadingType);
            rootElt.setAttribute(leosPluginUtils.DATA_AKN_ELEMENT, leosPluginUtils.CROSSHEADING);
            rootElt.removeAttribute(leosPluginUtils.DATA_AKN_NUM);
            rootElt.removeAttribute(DATA_AKN_NUM_ID);
            rootElt.removeAttribute(DATA_AKN_NUM_ORIGIN);
        }
    }

    return {
        getCurrentConfigFromAttributes: _getCurrentConfigFromAttributes,
        updateRootEltAttributes : _updateRootEltAttributes
    };
});