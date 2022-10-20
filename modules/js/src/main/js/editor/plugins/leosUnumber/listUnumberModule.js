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
define(function listUnumberModule(require) {
    "use strict";

    var leosPluginUtils = require("plugins/leosPluginUtils");
    var ckEditor;
    // Indent numbering configs
    var BULLET_NUM = 'bullet_num';
    var INDENT = 'indent';

    var unumberedListNumberConfig;
    var numberingConfigs;

    function initialize(editor) {
        ckEditor = editor;
        _initializeLists(editor);
    }

    /*
     * initialize numbering config lists
    */
    function _initializeLists(editor) {
        var tocItemsIdent = editor.LEOS.tocItemsList.filter(tocItem => tocItem.aknTag == leosPluginUtils.INDENT);
        numberingConfigs = editor.LEOS.numberingConfigs;
        unumberedListNumberConfig = tocItemsIdent.map(tocItem => numberingConfigs.find(numberingConfig => numberingConfig.type == tocItem.numberingType));
    }

    /*
     * Called to check if list if currently numbered or unumbered
     */
    function isNumbered(orderedList) {
        var currentNestingLevel = _getNestingLevelForOl(orderedList);
        var listItems = _removeCrossHeadingsFromListItems(orderedList.children);
        var numberingConfig = _identifyOverallNumberingConfig(listItems, currentNestingLevel);
        return !numberingConfig || numberingConfig.numbered;
    }

    /*
     * Called to update numbering, changed from indent plugin or context menu
     */
    function updateNumbers(orderedLists, numeringConfigName) {
        for (var ii = 0; ii < orderedLists.length; ii++) {
            var orderedList = orderedLists[ii];
            var currentNestingLevel = _getNestingLevelForOl(orderedList);
            var listItems = _removeCrossHeadingsFromListItems(orderedList.children);
            if (!!numeringConfigName) {
                var sequence = _getSequenceFromNumberingConfig(numeringConfigName, currentNestingLevel);
                if (!!sequence) {
                    _doNum(orderedList, listItems, sequence);
                }
            } else {
                var numberingConfig = _identifyNumberingConfig(listItems, currentNestingLevel);
                if (!!numberingConfig && !!numberingConfig.sequence) {
                    _doNum(orderedList, listItems, numberingConfig.sequence);
                }
            }
        }
    }

    /*
     * From global numbering config (bullet_num or indent), it gets the correct value for renumbering
     */
    function _getSequenceFromNumberingConfig(numeringConfigName, currentNestingLevel) {
        for (var numberingConfig of unumberedListNumberConfig) {
            if (!!numberingConfig.sequence && numeringConfigName.toLowerCase() == numberingConfig.type.toLowerCase()) {
                return numberingConfig.sequence;
            } else if (!!numberingConfig.levels && !!numberingConfig.levels.levels && numeringConfigName.toLowerCase() == numberingConfig.type.toLowerCase()) {
                var foundNumberingType = numberingConfig.levels.levels[currentNestingLevel-1];
                var foundNumberingConfig = numberingConfigs.find(n => n.type == foundNumberingType.numberingType);
                return foundNumberingConfig.sequence;
            }
        }
        return null;
    }

    /*
     * To identify the specific numbering config: first point of the list is used
     * From number value of the first indent of the list,
     * it gets the unumbered indent numbering config (bullet_black_circle, bullet_white_circle,... or indent)
     * This method is used to refresh numbering of lists
     */
    function _identifyNumberingConfig(listItems, currentNestingLevel) {
        if (!!listItems && listItems.length > 0) {
            var firstListItem = listItems[0];
            if (!!firstListItem.attributes[leosPluginUtils.DATA_AKN_NUM]) {
                var numValue = firstListItem.attributes[leosPluginUtils.DATA_AKN_NUM].value;
                if (!!numValue) {
                    return _getNumberingConfigFromSequence(numValue, currentNestingLevel);
                }
            } else if (currentNestingLevel > 0) {
                var parentIndent = _getParentIndent(firstListItem);
                if (!!parentIndent && !!parentIndent.attributes[leosPluginUtils.DATA_AKN_NUM]) {
                    var numValue = parentIndent.attributes[leosPluginUtils.DATA_AKN_NUM].value;
                    if (!!numValue) {
                        return _getNumberingConfigFromParentSequence(numValue, currentNestingLevel);
                    }
                }
            }
        }
        return null;
    }

    /*
     * From number value of the first indent of the list,
     * get the unumbered indent numbering config (bullet_black_circle, bullet_white_circle,... or indent)
     */
    function _getNumberingConfigFromSequence(numValue, currentNestingLevel) {
        for (var numberingConfig of unumberedListNumberConfig) {
            if (!!numberingConfig.sequence && numValue == numberingConfig.sequence) {
                return numberingConfig;
            } else if (!!numberingConfig.levels && !!numberingConfig.levels.levels) {
                var foundNumberingType = numberingConfig.levels.levels[currentNestingLevel-1];
                var foundNumberingConfig = numberingConfigs.find(n => n.type == foundNumberingType.numberingType);
                if (!!foundNumberingConfig.sequence && numValue == foundNumberingConfig.sequence) {
                    return foundNumberingConfig;
                }
            }
        }
        return null;
    }

    /*
     * From number value of the indent's parent,
     * get the unumbered indent numbering config (bullet_black_circle, bullet_white_circle,... or indent)
     */
    function _getNumberingConfigFromParentSequence(numValue, currentNestingLevel) {
        for (var numberingConfig of unumberedListNumberConfig) {
            if (!!numberingConfig.sequence && numValue == numberingConfig.sequence) {
                return numberingConfig;
            } else if (!!numberingConfig.levels && !!numberingConfig.levels.levels && currentNestingLevel>1) {
                var foundNumberingType = numberingConfig.levels.levels[currentNestingLevel-2];
                var foundNumberingConfig = numberingConfigs.find(n => n.type == foundNumberingType.numberingType);
                if (!!foundNumberingConfig.sequence && numValue == foundNumberingConfig.sequence) {
                    foundNumberingType = numberingConfig.levels.levels[currentNestingLevel-1];
                    foundNumberingConfig = numberingConfigs.find(n => n.type == foundNumberingType.numberingType);
                    return foundNumberingConfig;
                }
            }
        }
        return null;
    }

    function _doNum(orderedList, listItems, sequence) {
        for (var idx = 0; idx < listItems.length; idx++) {
            sequence && listItems[idx].setAttribute(leosPluginUtils.DATA_AKN_NUM, sequence) && listItems[idx].setAttribute(leosPluginUtils.DATA_AKN_ELEMENT, leosPluginUtils.INDENT);
        }
    }

    /*
     * Called to set "data-akn-element" to "point" or "indent" on all items of lists included in the edited text
     */
    function resetElementAttributeOnIndents(orderedList) {
        var currentNestingLevel = _getNestingLevelForOl(orderedList);
        var listItems = _removeCrossHeadingsFromListItems(orderedList.children);
        var numberingConfig = _identifyOverallNumberingConfig(listItems, currentNestingLevel);
        if (!!numberingConfig && !numberingConfig.numbered) {
            listItems.forEach(listItem => listItem.setAttribute(leosPluginUtils.DATA_AKN_ELEMENT, leosPluginUtils.INDENT));
        } else {
            listItems.forEach(listItem => listItem.setAttribute(leosPluginUtils.DATA_AKN_ELEMENT, leosPluginUtils.POINT));
        }
    }

    /*
     * To identify the global numbering config from one list
     */
    function identifyNumberingConfigFromList(orderedList) {
        var currentNestingLevel = _getNestingLevelForOl(orderedList);
        var listItems = _removeCrossHeadingsFromListItems(orderedList.children);
        return _identifyOverallNumberingConfig(listItems, currentNestingLevel);
    }

    /*
     * Returns the nesting level for given ol element
     */
    function _getNestingLevelForOl(olElement) {
        var nestingLevel = -1;
        var currentOl = new CKEDITOR.dom.node(olElement);
        while (currentOl) {
            currentOl = currentOl.getAscendant(leosPluginUtils.ORDER_LIST_ELEMENT);
            nestingLevel++;
        }
        return nestingLevel;
    }

    function _removeCrossHeadingsFromListItems(listItems) {
        var sortedListItems = [];
        for (var i=0; i<listItems.length; i++) {
            if (_isPoint(listItems[i])) {
                sortedListItems.push(listItems[i]);
            }
        }
        return sortedListItems;
    }

    function _isPoint(element) {
        var isLi = leosPluginUtils.getElementName(element) === leosPluginUtils.HTML_POINT;
        var crossheadingAttr = element.getAttribute(leosPluginUtils.CROSSHEADING_LIST_ATTR);

        if (element && isLi && (crossheadingAttr == null || crossheadingAttr != leosPluginUtils.LIST)) {
            return true;
        } else {
            return false;
        }
    }

    /*
     * To identify the global numbering config: first point of the list is used
     * From number value of the first indent of the list,
     * it gets the unumbered indent numbering config (point_num, bullet_num or indent)
     */
    function _identifyOverallNumberingConfig(listItems, currentNestingLevel) {
        if (!!listItems && listItems.length > 0) {
            var firstListItem = listItems[0];
            if (!!firstListItem.attributes[leosPluginUtils.DATA_AKN_NUM]) {
                var numValue = firstListItem.attributes[leosPluginUtils.DATA_AKN_NUM].value;
                if (!!numValue) {
                    return _getOverallNumberingConfigFromSequence(firstListItem, numValue, currentNestingLevel);
                }
            } else if (currentNestingLevel > 0) {
                var parentIndent = _getParentIndent(firstListItem);
                if (!!parentIndent && !!parentIndent.attributes[leosPluginUtils.DATA_AKN_NUM]) {
                    var numValue = parentIndent.attributes[leosPluginUtils.DATA_AKN_NUM].value;
                    if (!!numValue) {
                        return _getNumberingConfigFromParentSequence(numValue, currentNestingLevel);
                    }
                }
            }
        }
        return null;
    }

    /*
     * From number value of the first indent of the list,
     * it gets the unumbered indent numbering config (point_num, bullet_num or indent)
     */
    function _getOverallNumberingConfigFromSequence(firstListItem, numValue, currentNestingLevel) {
        for (var numberingConfig of unumberedListNumberConfig) {
            if (!!numberingConfig.sequence && numValue == numberingConfig.sequence) {
                if (currentNestingLevel == leosPluginUtils.MAX_LEVEL_LIST_DEPTH) {
                    var parentIndent = _getParentIndent(firstListItem);
                    if (!!parentIndent && !!parentIndent.attributes[leosPluginUtils.DATA_AKN_NUM]) {
                        var numValue = parentIndent.attributes[leosPluginUtils.DATA_AKN_NUM].value;
                        if (!!numValue && numValue == numberingConfig.sequence) {
                            return numberingConfig;
                        }
                    }
                } else {
                    return numberingConfig;
                }
            } else if (!!numberingConfig.levels && !!numberingConfig.levels.levels && currentNestingLevel>0) {
                var foundNumberingType = numberingConfig.levels.levels[currentNestingLevel-1];
                var foundNumberingConfig = numberingConfigs.find(n => n.type == foundNumberingType.numberingType);
                if (!!foundNumberingConfig.sequence && numValue == foundNumberingConfig.sequence) {
                    if (currentNestingLevel == leosPluginUtils.MAX_LEVEL_LIST_DEPTH) {
                        var parentIndent = _getParentIndent(firstListItem);
                        if (!!parentIndent && !!parentIndent.attributes[leosPluginUtils.DATA_AKN_NUM]) {
                            var numValue = parentIndent.attributes[leosPluginUtils.DATA_AKN_NUM].value;
                            if (!!numValue && numValue != foundNumberingConfig.sequence) {
                                return numberingConfig;
                            }
                        }
                    } else {
                        return numberingConfig;
                    }
                }
            }
        }
        return null;
    }

    function _getParentIndent(elt) {
        if ($(elt).parents(leosPluginUtils.HTML_POINT).length > 0) {
            return $(elt).parents(leosPluginUtils.HTML_POINT)[0];
        }
        return null;
    }

    function checkListsWithOnlyCrossheadings(list) {
        // case when there are only crossheadings in a list
        if (_containsOnlyCrossHeadings(list)) {
            var olParents = $(list).parents(leosPluginUtils.ORDER_LIST_ELEMENT);
            if (olParents.length > 0) {
                var olParent = olParents[0];
                if (olParent.getAttribute(leosPluginUtils.DATA_AKN_NAME).toLowerCase() != leosPluginUtils.AKN_ORDERED_ANNEX_LIST.toLowerCase()) {
                    olParent = list.parentElement;
                    for (var child of list.children) {
                        leosPluginUtils.convertToCrossheading(child, list);
                    }
                }
                var listChildren = Array.from(list.children);
                var indexInList = _getIndexInParentList(olParent, list);
                if (indexInList < olParent.children.length - 1) {
                    var nextSibling = olParent.children[indexInList + 1];
                    for (var child of listChildren) {
                        olParent.insertBefore(child, nextSibling);
                    }
                } else {
                    for (var child of listChildren) {
                        olParent.appendChild(child);
                    }
                }
                list.remove();
            }
        }
    }

    function _getIndexInParentList(olParent, list) {
        var parent = list;
        while (Array.prototype.indexOf.call(olParent.children, parent) == -1) {
            parent = parent.parentElement;
        }
        return Array.prototype.indexOf.call(olParent.children, parent);
    }

    function _containsOnlyCrossHeadings(list) {
        for (var child of list.children) {
            if (_isPoint(child)) {
                return false;
            }
        }
        return true;
    }

    return {
        init: initialize,
        updateNumbers: updateNumbers,
        isNumbered: isNumbered,
        resetElementAttributeOnIndents: resetElementAttributeOnIndents,
        identifyNumberingConfigFromList: identifyNumberingConfigFromList,
        checkListsWithOnlyCrossheadings: checkListsWithOnlyCrossheadings,
        BULLET_NUM: BULLET_NUM,
        INDENT: INDENT
    };
});