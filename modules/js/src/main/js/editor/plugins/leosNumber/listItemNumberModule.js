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
define(function listItemNumberModule(require) {
    "use strict";

    var UTILS = require("core/leosUtils");
    var leosPluginUtils = require("plugins/leosPluginUtils");
    var ckEditor;
    var defaultList = [];
    var listNumberConfig;
    var numberingConfigs;

    var sequenceMap = [
        {
            type: "ALPHA_LOWER_PARENTHESIS",
            inDefault: true,
            format: "x",
            prefix: "(",
            suffix: ")",
            name: 'Alphabets',
            generator: function generateSequenceForAlpha(list, item, idx) {
                return this.prefix+this.format.replace('x', generateAlpha(idx))+this.suffix;
            }
        }, {
            type: "ARABIC_PARENTHESIS",
            inDefault: true,
            format: "x",
            prefix: "(",
            suffix: ")",
            name: 'Arabic',
            generator: function generateSequenceForArabic(list, item, idx) {
                return this.prefix+this.format.replace('x', idx + 1)+this.suffix;
            }
        }, {
            type: "ARABIC_POSTFIXDOT",
            inDefault: true,
            format: "x",
            prefix: "",
            suffix: ".",
            name: 'Arabic',
            generator: function generateSequenceForArabic(list, item, idx) {
                return this.prefix+this.format.replace('x', idx + 1)+this.suffix;
            }
        }, {
            type: "ROMAN_LOWER_PARENTHESIS",
            inDefault: true,
            format: "x",
            prefix: "(",
            suffix: ")",
            name: 'Roman',
            generator: function generateSequenceForRoman(list, item, idx) {
                return this.prefix+this.format.replace('x', romanize(idx + 1))+this.suffix;
            }
        }, {
            type: "INDENT",
            inDefault: true,
            format: "-",
            prefix: "",
            suffix: "",
            name: 'IndentDash',
            generator: function generateSequenceForIndent(list, item, idx) {
                return this.prefix+this.format.replace('x', '-')+this.suffix;
            }
        }
    ];

    function _getSequences(seqName) {
        if (seqName && seqName === 'Paragraph') {
            var paragraphTocItem = ckEditor.LEOS.tocItemsList.find(function(e){return e.aknTag === 'paragraph'});
            var paraNumTypeName = paragraphTocItem.numberingType;
            var paragraphNumType = numberingConfigs.find(function(e){return e.type === paraNumTypeName});
            var paragraphSequence = sequenceMap.find(function(el){return el.type === paraNumTypeName});
            paragraphSequence.format = 'x';
            paragraphSequence.suffix = paragraphNumType.suffix;
            return paragraphSequence;
        }
        else if (seqName) {
            return defaultList.find(function(el){return el.name === seqName});
        }
        else {//return all sequences
            return sequenceMap;
        }
    }

    /*
     * For given num param return roman number literal
     */
    function romanize(num) {
        var digits = String(+num).split(""),
            key = ["", "c", "cc", "ccc", "cd", "d", "dc", "dcc", "dccc", "cm", "", "x", "xx", "xxx", "xl", "l", "lx", "lxx",
                "lxxx", "xc", "", "i", "ii", "iii", "iv", "v", "vi", "vii", "viii", "ix"], roman = "", i = 3;
        while (i--) {
            roman = (key[+digits.pop() + (i * 10)] || "") + roman;
        }
        return Array(+digits.join("") + 1).join("M") + roman;
    }

    /*
     * Returns the array containing literals for alpha points
     */
    function generateAlpha(sequenceNumber) {
        var startOfAlfaNumerical = 97;
        var endOfAlfaNumerical = 123;
        var sequenceBase = endOfAlfaNumerical - startOfAlfaNumerical;
        var currentSequenceBase = 0;
        var sequence = [];
        while (true) {
            var currentLetter;
            if (currentSequenceBase > 0) {
                var currentBaseCount = parseInt(sequenceNumber / (Math.pow(sequenceBase, currentSequenceBase)));
                if (currentBaseCount === 0) {
                    break;
                }
                currentLetter = String.fromCharCode(currentBaseCount + startOfAlfaNumerical - 1);
            }

            if (currentSequenceBase === 0) {
                currentLetter = String.fromCharCode((sequenceNumber % sequenceBase) + startOfAlfaNumerical);
            }
            currentSequenceBase++;
            sequence.unshift(currentLetter);
        }
        return sequence.join("");
    }

    /*
     * To identify the sequence first point in the list is used
     */
    function identifySequence(listItems, currentNestingLevel) {
        return getSequenceFromDefaultList(currentNestingLevel);
    }

    function _initialize(editor) {
        ckEditor = editor;
        _initializeDefaultList(editor);
    }

    /*
     * initialize default sequence list by sequence map
     */
    function _initializeDefaultList(editor) {
        var sequences = _getSequences();
        listNumberConfig = editor.LEOS.listNumberConfig;
        numberingConfigs = editor.LEOS.numberingConfigs;
        for (var i = 0; i < listNumberConfig.length; i++) {
            if (sequences[i].inDefault) {
                var numberType = numberingConfigs.find(function(e){return e.type === listNumberConfig[i].numberingType});
                var defaultListItem = sequences.find(function(e){return e.type ===  numberType.type});
                defaultListItem.prefix = numberType.prefix;
                defaultListItem.suffix = numberType.suffix;
                defaultList[listNumberConfig[i].depth-1] = defaultListItem;
            }
        }
    }

    /*
     * Get sequence type from default sequence list
     */
    function getSequenceFromDefaultList(currentNestingLevel) {
        var sequenceType = '';
        if (currentNestingLevel > defaultList.length || !defaultList[currentNestingLevel - 1]) {
            sequenceType = _getSequences('IndentDash');
            defaultList[currentNestingLevel - 1] = sequenceType;
        } else {
            sequenceType = defaultList[currentNestingLevel - 1];
        }
        return sequenceType;
    }

    /*
     * Returns the nesting level for given ol element
     */
    function getNestingLevelForOl(olElement) {
        var nestingLevel = -1;
        var currentOl = new CKEDITOR.dom.node(olElement);
        while (currentOl) {
            currentOl = currentOl.getAscendant(leosPluginUtils.ORDER_LIST_ELEMENT);
            nestingLevel++;
        }
        return nestingLevel;
    }

    /*
     * Called to update numbering, changed from indent plugin or context menu
     */
    function _updateNumbers(orderedLists, seqNum) {
        for (var ii = 0; ii < orderedLists.length; ii++) {
            var orderedList = orderedLists[ii];
            var currentNestingLevel = getNestingLevelForOl(orderedList);
            var listItems = _removeCrossHeadingsFromListItems(orderedList.children);
            var sequence = '';
            if (typeof seqNum === 'undefined' || !seqNum) {
                sequence = identifySequence(listItems, currentNestingLevel);
            } else {
                sequence = seqNum;
            }
            (UTILS.getElementOrigin(orderedList) && UTILS.getElementOrigin(orderedList) === 'ec' &&
              ckEditor.LEOS.instanceType === UTILS.COUNCIL_INSTANCE)
                ? _doMandateNum(listItems, sequence)
                : _doProposalNum(orderedList, listItems, sequence);
        }
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
    * default numbering
    */
    function _updateNumbersByDefault(orderedLists) {
        for (var ii = 0; ii < orderedLists.length; ii++) {
            var orderedList = orderedLists[ii];
            var listItems = orderedList.children;
            for (var jj = 0; jj < listItems.length; jj++) {
                if (_isPoint(listItems[jj])) {
                    listItems[jj].setAttribute(leosPluginUtils.DATA_AKN_NUM, '#') && listItems[jj].setAttribute(leosPluginUtils.DATA_AKN_ELEMENT, leosPluginUtils.POINT);
                }
            }
        }
    }

    function _doProposalNum(orderedList, listItems, sequence) {
        var newIdx = 0;
        var deleted = "deleted_";
        for (var idx = 0; idx < listItems.length; idx++) {
            if (!(listItems[idx].getAttribute('contenteditable') === "false")) {
                var numID = listItems[idx].getAttribute(leosPluginUtils.DATA_AKN_NUM_ID);
                if (numID && numID.startsWith(deleted)) {
                    listItems[idx].setAttribute(leosPluginUtils.DATA_AKN_NUM_ID, numID.replaceAll(deleted,''));
                    listItems[idx].removeAttribute("data-akn-num-attr-softaction");
                }
                sequence && listItems[idx].setAttribute(leosPluginUtils.DATA_AKN_NUM, sequence.generator(orderedList, listItems[idx], newIdx)) && listItems[idx].setAttribute(leosPluginUtils.DATA_AKN_ELEMENT, leosPluginUtils.POINT);
                newIdx++;
            }
        }
    }

    function _doMandateNum(listItems, sequence) {
        for (var jj = 0; jj < listItems.length; jj++) {
            if (!UTILS.getElementOrigin(listItems[jj])) {
                sequence && listItems[jj].setAttribute(leosPluginUtils.DATA_AKN_NUM, sequence.format.replace('x', '#')) && listItems[jj].setAttribute(leosPluginUtils.DATA_AKN_ELEMENT, leosPluginUtils.POINT);
            }
        }
    }

    return {
        init: _initialize,
        getSequences: _getSequences,
        updateNumbers: _updateNumbers,
        updateNumbersByDefault: _updateNumbersByDefault
    };
});