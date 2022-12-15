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
define(function leosPluginUtilsModule(require) {
    "use strict";

    var CKEDITOR = require("promise!ckEditor");
    var TEXT = "text";
    var BOGUS = "br";
    var UNKNOWN = "unknown";
    var DATA_AKN_NUM = "data-akn-num";
    var DATA_AKN_NUM_ID = "data-akn-num-id";
    var DATA_INDENT_ORIGIN_NUM_ID = "data-indent-origin-num-id";
    var DATA_NUM_ORIGIN = "data-num-origin";
    var DATA_ORIGIN = "data-origin";
    var DATA_AKN_ELEMENT = "data-akn-element";
    var DATA_AKN_NAME = "data-akn-name";
    var DATA_AKN_ID = "data-akn-id";
    var ID = "id";
    var REGULAR = "REGULAR";
    var CROSSHEADING_LIST_ATTR = "data-akn-crossheading-type";
    var DATA_INDENT_LEVEL_ATTR = "data-indent-level";
    var AKN_ORDERED_ANNEX_LIST = "aknAnnexOrderedList";
    var INDENT_LEVEL_ATTR = "--indent-level"
    var INLINE_NUM_ATTR = "--inline-num"
    var ORDER_LIST_ELEMENT = "ol";
    var HTML_POINT = "li";
    var HTML_SUB_POINT = "p";
    var MAX_LEVEL_DEPTH = 7;
    var MAX_LIST_LEVEL = 5;
    var MAX_LEVEL_LIST_DEPTH = 4;
    var MAINBODY = "mainbody";
    var INDENT = "indent";
    var POINT = "point";
    var LIST = "list";
    var ALINEA = "alinea";
    var NUM = "num";
    var SUBPARAGRAPH = "subparagraph";
    var PARAGRAPH = "paragraph";
    var ARTICLE = "article";
    var LEVEL = "level";
    var CROSSHEADING = "crossHeading";
    var EC = "ec";
    var CN = "cn";

    var NUMBERED_ITEM = "point, indent, paragraph";
    var UNUMBERED_ITEM = "alinea, subparagraph";
    var NUMBERED_LEVEL_ITEM = "point, indent, paragraph, level";
    var ITEMS_SELECTOR = LIST + "," + NUMBERED_ITEM + "," + UNUMBERED_ITEM;
    var ATTR_INDENT_LEVEL = "data-indent-level";
    var ATTR_INDENT_NUMBERED = "data-indent-numbered";
    var DATA_AKN_WRAPPED_CONTENT_ID = "data-akn-wrapped-content-id";
    var DATA_AKN_CONTENT_ID = "data-akn-content-id";
    var DATA_CONTENT_ORIGIN = "data-content-origin";
    var DATA_WRAPPED_CONTENT_ORIGIN = "data-wrapped-content-origin";
    var DATA_AKN_MP_ID = "data-akn-mp-id";
    var DATA_MP_ORIGIN = "data-mp-origin";

    var INLINE_FROM_MATCH = /^(text|span|strong|em|u|sup|sub|br|a|img|mref|del)$/;

    var DATA_INDENT_ORIGIN_NUMBER = "data-indent-origin-num";
    var DATA_INDENT_ORIGIN_NUMBER_ID = "data-indent-origin-num-id";
    var DATA_INDENT_ORIGIN_NUMBER_ORIGIN = "data-indent-origin-num-origin";
    var DATA_INDENT_ORIGIN_TYPE = "data-indent-origin-type";

    var LEOS_SOFTACTION = "leos:softaction";
    var DATA_AKN_NUM_SOFTACTION = "data-akn-num-attr-softaction";
    var DEL = "del";
    var MOVETO = "move_to";

    var COUNCIL_INSTANCE = "COUNCIL";


    function _hasTextOrBogusAsNextSibling(element){
        return (element instanceof CKEDITOR.dom.element) && element.hasNext()
            && (_getElementName(element.getNext()) === TEXT || _getElementName(element.getNext()) === BOGUS);
    }

    function _getElementName(element) {
        var elementName = UNKNOWN;
        if (element instanceof CKEDITOR.dom.element) {
            elementName = element.getName();
        } else if (element instanceof CKEDITOR.dom.text
                || (element && element.nodeName && element.nodeName === "#text")) {
            elementName = TEXT;
        } else if(element && element.localName){
            elementName = element.localName;
        }
        return elementName;
    }

    function _setFocus(element, editor){
        if(element){
            var range = editor.createRange();
            range.selectNodeContents(element);
            range.collapse(true);
            range.select();
            range.scrollIntoView();
        }
    }

    /**
     * Calculates the depth level of the selected element inside the list (ol block).
     * It counts how many ol elements are present in the hierarchy.
     */
    function _calculateListLevel(selected) {
        var level = 0;
        var actualEL = selected;
        while (_isListElement(actualEL)) {
            level++;
            actualEL = actualEL.getAscendant(ORDER_LIST_ELEMENT);
        }
        return level;
    }

    /**
     * Returns true if the selected element is child of an li or ol
     */
    function _isListElement(el) {
        return (el && (el.getAscendant(ORDER_LIST_ELEMENT) || el.getAscendant(HTML_POINT)));
    }
    
    function _isAnnexList(element) {
        return !!element && 'aknAnnexList' === element.getAttribute(DATA_AKN_NAME);
    }

    function _isOrderedAnnexList(element) {
        return !!element && AKN_ORDERED_ANNEX_LIST === element.getAttribute(DATA_AKN_NAME);
    }

	function _isUnnumberedCNParagraph(el) {
		return el && (!el.getAttribute(DATA_ORIGIN) || el.getAttribute(DATA_ORIGIN).toLowerCase() === 'cn') 
			&& el.getAttribute(DATA_AKN_ELEMENT) && el.getAttribute(DATA_AKN_ELEMENT).toLowerCase() === PARAGRAPH 
			&& !el.getAttribute(DATA_AKN_NUM);
	}

	function _isAnnexUnnumberedCNParagraph(el) {
		return el && _isAnnexList(el.getAscendant(ORDER_LIST_ELEMENT, true)) && _isUnnumberedCNParagraph(el);
	}

    function _isAnnexSubparagraphElement(element) {
        var elementName = element.getName && element.getName();
        if (elementName === 'p' && 'subparagraph' === element.getAttribute('data-akn-element')) {
            var parentElement = element.getAscendant('ol');
            if (parentElement && 'aknAnnexList' === parentElement.getAttribute('data-akn-name')
                && 'paragraph' === parentElement.getAttribute('data-akn-element')) {
                return true;
            }
        }
        return false;
    }

    function _isSubparagraph(element) {
        return (!!element && element.type == CKEDITOR.NODE_ELEMENT
            && !!element.getAttribute(DATA_AKN_ELEMENT)
            && element.getAttribute(DATA_AKN_ELEMENT) == SUBPARAGRAPH);
    }

    function _isPointOrIndent(element) {
        return (!!element && element.type == CKEDITOR.NODE_ELEMENT && !!element.getAttribute(DATA_AKN_ELEMENT)
            && (element.getAttribute(DATA_AKN_ELEMENT) == POINT || element.getAttribute(DATA_AKN_ELEMENT) == INDENT));
    }

    function _isListIntro(element) {
        if (!!element
            && _isOrderedAnnexList(element.getParent())
            && element.getParent().getFirst().equals(element)) {
            return _isSubparagraph(element);
        }
    }

    function _isListEnding(element) {
        if (!!element
            && _isOrderedAnnexList(element.getParent())
            && element.getParent().getLast().equals(element)
            && !element.getParent().getFirst().equals(element)) {
            return _isSubparagraph(element);
        }
    }

    function _moveChildren(source, target) {
        if ( !source || !target )
            return;
        var $ = source.$;
        var targetHtml = target.$;
        var child;

        while ( ( child = $.firstChild ) ) {
            if ($.parentElement.isEqualNode(targetHtml)) {
                targetHtml.insertBefore($.removeChild(child), $);
            } else {
                targetHtml.appendChild($.removeChild(child));
            }
        }
        $.parentElement.removeChild($);
    }

    function _moveElementChildren(source, target) {
        if ( !source || !target )
            return;
        var $ = source.$;
        var targetHtml = target.$;
        var i = $.children.length - 1;

        while ( i > -1 ) {
            var child = $.children.item(i);
            if (child.nodeType === CKEDITOR.NODE_ELEMENT && (child.nodeName.toLowerCase() == 'li' || child.nodeName.toLowerCase() == 'ol' || child.nodeName.toLowerCase() == 'p')) {
                if (!!$.nextSibling) {
                    targetHtml.insertBefore($.removeChild(child), $.nextSibling);
                } else {
                    targetHtml.appendChild($.removeChild(child));
                }
            }
            i--;
        }
    }

    function _isSelectionInFirstLevelList(element) {
        var listDepth = 0;
        var olElement = element.getAscendant(ORDER_LIST_ELEMENT);
        while (!_isAnnexList(olElement)) {
            if (olElement) {
                listDepth = listDepth + 1;
                olElement = olElement.getAscendant(ORDER_LIST_ELEMENT);
            } else {
                break;
            }
        }
        return listDepth === 1;
    }
    
    function _getAnnexList(element) {
        var olElement = element.getAscendant(ORDER_LIST_ELEMENT);
        while (olElement && !_isAnnexList(olElement)) {
            olElement = olElement.getAscendant(ORDER_LIST_ELEMENT);
        }
        return olElement;
    }

    /*
    * Returns the nesting level for given ol element
    */
    function _getNestingLevelForOl(olElement) {
        var nestingLevel = -1;
        var currentOl = new CKEDITOR.dom.node(olElement);
        while (currentOl) {
            currentOl = currentOl.getAscendant(ORDER_LIST_ELEMENT);
            nestingLevel++;
        }
        return nestingLevel;
    }

    function _setCrossheadingIndentAttribute(element, indentLevel) {
        if (!!element.style) {
            element.style.setProperty(INDENT_LEVEL_ATTR, indentLevel);
        } else {
            element.style = INDENT_LEVEL_ATTR + ":" + indentLevel + ";";
        }
        return element.setAttribute(DATA_INDENT_LEVEL_ATTR, indentLevel);
    }

    function _setCrossheadingNumProperty(element, num) {
        if (!!element.style && !!num) {
            element.style.setProperty(INLINE_NUM_ATTR, "1");
        } else if (!!num) {
            element.style = INLINE_NUM_ATTR + ":" + num + ";";
        }
    }

    function _removeCrossheadingNumProperty(element, num) {
        if (!!element.style) {
            element.style.removeProperty(INLINE_NUM_ATTR);
        }
    }

    function _convertToCrossheading(element, olElement) {
        var crossheading = document.createElement(HTML_SUB_POINT);
        var indentLevel = _getNestingLevelForOl(olElement);
        _setCrossheadingIndentAttribute(crossheading, indentLevel);
        crossheading.setAttribute(DATA_AKN_ELEMENT, CROSSHEADING);
        crossheading.setAttribute(DATA_AKN_NAME, CROSSHEADING);
        crossheading.setAttribute("data-akn-heading-content", "");
        crossheading.setAttribute(DATA_AKN_ID, element.getAttribute("id"));
        crossheading.innerHTML = element.innerHTML;
        element.parentNode.replaceChild(crossheading, element);
        return crossheading;
    }

    function _isCrossHeading(element) {
        return element && element.type == CKEDITOR.NODE_ELEMENT && element.getAttribute(DATA_AKN_ELEMENT) && element.getAttribute(DATA_AKN_ELEMENT).toLowerCase() === CROSSHEADING.toLowerCase();
    }

    function _isCrossHeadingInList(element) {
        return _isCrossHeading(element) && element.getAttribute(CROSSHEADING_LIST_ATTR) && element.getAttribute(CROSSHEADING_LIST_ATTR).toLowerCase() === LIST.toLowerCase();
    }

    // Check lists: is list only contains subparagraphs and crossheadings,
    // this should be removed and children moved to the parent
    function _manageEmptyLists(editor) {
        var lists = editor.element.find('ol');
        for (var i = 0; i < lists.count(); i++) {
            var list = lists.getItem(i);
            if (_isOrderedAnnexList(list) && _isListContainsOnlySubparagraphsCrossheadingsOrEmpty(list)) {
                _moveChildrenToParent(list);
            }
        }
    }

    // Check lists: if two lists are siblings,,
    // they should be merged if possible
    function _manageSiblingLists(editor) {
        var lists = editor.element.find('ol');
        for (var i = 0; i < lists.count(); i++) {
            var list = lists.getItem(i);
            var sibling = list.getNext();
            while (list.is('ol') && !!sibling && !!sibling.is && sibling.is('ol')
            && !_isListEnding(list.getLast()) && !_isListIntro(sibling.getFirst())) {
                _moveChildren(sibling, list);
                sibling = list.getNext();
            }
        }
    }

    // Check subparagraphs, if there is a subparagraph without text, it should be removed
    function _manageEmptySubparagraphs(editor) {
        var subparagraphs = editor.element.find('li');
        for (var i = 0; i < subparagraphs.count(); i++) {
            var subparagraph = subparagraphs.getItem(i);
            if (_isSubparagraph(subparagraph)
                && subparagraph.getChildren().count() == 0
                && !!subparagraph.getNext()
                && !!subparagraph.getNext().is
                && subparagraph.getNext().is('ol')) {
                if (!!subparagraph.getNext().getFirst() && _isSubparagraph(subparagraph.getNext().getFirst())) {
                    subparagraph.remove();
                }
            }
        }
        subparagraphs = editor.element.find('p');
        for (var i = 0; i < subparagraphs.count(); i++) {
            var subparagraph = subparagraphs.getItem(i);
            if (_isSubparagraph(subparagraph)
                && subparagraph.getChildren().count() == 0
                && !!subparagraph.getNext()
                && !!subparagraph.getNext().is
                && subparagraph.getNext().is('ol')) {
                if (!!subparagraph.getNext().getFirst() && _isSubparagraph(subparagraph.getNext().getFirst())) {
                    subparagraph.remove();
                }
            }
        }
    }

    // Check crossheadings:
    // 1. A crossheading should not contain a list -> crossheadings should be moved inside the list
    // and list should be moved to the previous point, indent
    // 2. List crossheadings should be part of a list
    function _manageCrossheadings(editor) {
        var crossheadings = editor.element.find('li');
        for (var i = 0; i < crossheadings.count(); i++) {
            var crossheading = crossheadings.getItem(i);
            // Crossheadings should not contain list
            if (_isCrossHeading(crossheading)
                && !!crossheading.getLast()
                && !!crossheading.getLast().is
                && crossheading.getLast().is('ol')
                && !_isListIntro(crossheading.getLast().getFirst())) {
                crossheading.getParent().$.insertBefore(crossheading.getLast().$, crossheading.$);
                var ol = crossheading.getPrevious();
                do {
                    ol.$.insertBefore(crossheading.$, ol.getFirst().$);
                } while ((crossheading = ol.getPrevious()) && _isCrossHeadingInList(crossheading));
                if (_isPointOrIndent(ol.getPrevious())) {
                    ol.getPrevious().$.appendChild(ol.$);
                }
            }
            // List Crossheadings should stay in a list
            if (_isCrossHeadingInList(crossheading)
                && !!crossheading.getParent()
                && !!crossheading.getParent().is
                && crossheading.getParent().is('li')) {
                if (!!crossheading.getParent().getAttribute(DATA_AKN_ELEMENT)
                    && crossheading.getParent().getAttribute(DATA_AKN_ELEMENT) == LEVEL) {
                    crossheading.renameNode('p');
                    crossheading.removeAttribute(CROSSHEADING_LIST_ATTR);
                } else {
                    var foundSubparagraph = false;
                    var prev = crossheading;
                    while ((prev = prev.getPrevious()) && !foundSubparagraph) {
                        if (_isSubparagraph(prev)) {
                            foundSubparagraph = true;
                        }
                    }
                    if (foundSubparagraph || _isContainsOnlyCrossheadingsOrEmpty(crossheading.getParent())) {
                        if (!!crossheading.getParent().getNext()) {
                            var nextParent = crossheading.getParent().getNext();
                            var next;
                            do {
                                next = crossheading.getNext();
                                crossheading.getParent().getParent().$.insertBefore(crossheading.$, nextParent.$);
                            } while (_isCrossHeading(next) && (crossheading = next))
                        } else {
                            crossheading.getParent().getParent().$.appendChild(crossheading.$);
                        }
                    } else {
                        crossheading.getParent().getParent().$.insertBefore(crossheading.$, crossheading.getParent().$);
                    }
                }
            }
        }
    }

    // Check points,
    // 1. If it contains only one subparagraph, content of subparagraph should be moved to point
    // 2. Points should be converted to subparagraph when they not anymore in a list
    function _managePoints(editor) {
        var points = editor.element.find('li');
        for (var i = 0; i < points.count(); i++) {
            var point = points.getItem(i);
            if (_isPointOrIndent(point) && point.getChildren().count() == 1 && _isSubparagraph(point.getFirst())) {
                _moveChildrenToParent(point.getFirst());
            }
            if (_isAnnexList(point.getParent()) && _isPointOrIndent(point)) {
                var level = $(point.getParent().$).find("li[data-akn-element='" + LEVEL + "']");
                if (level.length > 0) {
                    level[0].appendChild(point.$);
                    point.setAttribute(DATA_AKN_ELEMENT, SUBPARAGRAPH);
                    if (!!point.is && point.is('li') && !point.getParent().is('ol')) {
                        point.renameNode('p');
                    } else if (!!point.is && point.is('p') && point.getParent().is('ol')) {
                        point.renameNode('li');
                    }
                    _moveElementChildren(point, point.getParent());
                }
            }
            if (point.getParent().is('li')
                && !!point.getParent().getAttribute(DATA_AKN_ELEMENT)
                && (point.getParent().getAttribute(DATA_AKN_ELEMENT) == LEVEL || point.getParent().getAttribute(DATA_AKN_ELEMENT) == PARAGRAPH)
                && _isPointOrIndent(point)) {
                point.setAttribute(DATA_AKN_ELEMENT, SUBPARAGRAPH);
                if (!!point.is && point.is('li') && !point.getParent().is('ol')) {
                    point.renameNode('p');
					point.removeAttribute('data-akn-name');
                } else if (!!point.is && point.is('p') && point.getParent().is('ol')) {
                    point.renameNode('li');
                }
            }
        }
    }

    function _moveChildrenToParent( element ) {
        var parent = element.getParent();
        var children = element.getChildren().toArray();
        _moveChildren(element, element.getParent());
        if (!!parent.is &&  parent.is('li')) {
            for (var i = 0; i < children.length; i++) {
                var child = children[i];
                if (child.type == CKEDITOR.NODE_ELEMENT && !!child.is && child.is('li') && !child.getParent().is('ol') && _isSubparagraph(child)) {
                    child.renameNode('p');
                } else if (child.type == CKEDITOR.NODE_ELEMENT && !!child.is && child.is('p') && child.getParent().is('ol') && _isSubparagraph(child)) {
                    child.renameNode('li');
                }
            }
        }
    }

    function _isListContainsOnlySubparagraphsCrossheadingsOrEmpty(list) {
        var children = list.getChildren();
        for (var i = 0; i < children.count(); i++) {
            var child = children.getItem(i);
            if (!_isSubparagraph(child) && !_isCrossHeading(child)) {
                return false;
            }
        }
        return true;
    }

    function _isContainsOnlyCrossheadingsOrEmpty(list) {
        var children = list.getChildren();
        for (var i = 0; i < children.count(); i++) {
            var child = children.getItem(i);
            if (child.type == CKEDITOR.NODE_ELEMENT && child.getName() != 'br' && !_isCrossHeading(child)) {
                return false;
            }
        }
        return true;
    }

    function _isFirstList(indentationStatus) {
        return (indentationStatus.current.level == 0);
    }

    function _isListDepthMoreThanThreshold(indentationStatus, maxLevel) {
        return ((indentationStatus.current.level > maxLevel) || (indentationStatus.current.level == maxLevel + 1 && !indentationStatus.current.numbered));
    }

    function _getIndentLevel(editor) {
        var currentPoint = _getPoint(editor);
        var $points = currentPoint.parents(NUMBERED_LEVEL_ITEM);
        return $points.length;
    }

    function _getPoint(editor) {
        var rootElt = $(editor.element.$);
        return _isSubpoint(editor) && _isFirstChild(editor) ? rootElt.parents(NUMBERED_ITEM).first() : rootElt;
    }

    function _isSubpoint(editor) {
        var div = $(editor.element.$);
        if (div.length) {
            return (div.nextAll(ITEMS_SELECTOR).length > 0 && div.nextAll(NUMBERED_ITEM).length == 0) ||
                (div.prevAll(ITEMS_SELECTOR).length > 0 && div.prevAll(NUMBERED_ITEM).length == 0) ||
                (div.parent(NUMBERED_ITEM).length > 0);
        }
        return false;
    }

    function _isFirstChild(editor) {
        return ($(editor.element.$).prevAll(ITEMS_SELECTOR).length == 0 && $(editor.element.$).parent().prop("tagName").toLowerCase() != LEVEL);
    }

    function _getCurrentNumValue(editor) {
        var rootElt = $(editor.element.$);
        var pointInHtml = (_isSubpoint(editor) && _isFirstChild(editor)) ? rootElt.parents(NUMBERED_ITEM).first() : rootElt.find(HTML_POINT).first();
        var currentNum = pointInHtml.children(NUM).first();
        if (currentNum.length) {
            return currentNum.html();
        } else {
            return pointInHtml.attr(DATA_AKN_NUM);
        }
    }

    function _getDepth(item) {
        if (item.prop("tagName").toLowerCase() == PARAGRAPH) {
            var children = item.children(ITEMS_SELECTOR);
            if (children.length) {
                item = children.eq(children.length-1);
            } else {
                return 1;
            }
        }
        if (item.prop("tagName").toLowerCase() == SUBPARAGRAPH) {
            var children = item.children(ITEMS_SELECTOR);
            if (children.length) {
                item = children.eq(children.length-1);
            } else {
                return 1;
            }
        }
        if (item.prop("tagName").toLowerCase() == POINT) {
            var children = item.children(ITEMS_SELECTOR);
            if (children.length) {
                item = children.eq(children.length-1);
            }
        }
        if (item.prop("tagName").toLowerCase() == LIST) {
            var children = item.children(ITEMS_SELECTOR);
            if (children.length) {
                item = children.eq(children.length-1);
            }
        }
        var currentLevel = item.parents(NUMBERED_LEVEL_ITEM).length;
        var depth = currentLevel;
        var descendantPoints = item.find(NUMBERED_ITEM);
        for (var i = 0; i < descendantPoints.length; i++) {
            var currentChild = descendantPoints.eq(i);
            var level = currentChild.parents(NUMBERED_LEVEL_ITEM).length;
            if (level > depth) {
                depth = level;
            }
        }
        if (item.prop("tagName").toLowerCase() != ALINEA  &&
            item.prop("tagName").toLowerCase() != SUBPARAGRAPH) {
            depth++;
        }

        return depth;
    }

    function _setCurrentNumValue(newValue, editor, indentationStatus) {
        var rootElt = $(editor.element.$);
        var pointInHtml = (_isSubpoint(editor) && _isFirstChild(editor)) ? rootElt.parents(NUMBERED_ITEM).first() : rootElt.find(HTML_POINT).first();
        if (_isUnumberedparagraph(pointInHtml)) {
            pointInHtml = rootElt.find(HTML_POINT).first();
        }
        if (pointInHtml.parents(PARAGRAPH).length && _isUnumberedparagraph(pointInHtml.parents(PARAGRAPH)) && indentationStatus.current.level == 0) {
            newValue = null;
        }

        var currentNum = pointInHtml.children(NUM).first();
        if (currentNum.length) {
            if (typeof newValue === 'undefined') {
                currentNum.html("");
            } else {
                currentNum.html(newValue);
            }
        } else {
            if (typeof newValue === 'undefined') {
                pointInHtml.removeAttr(DATA_AKN_NUM);
            } else {
                pointInHtml.attr(DATA_AKN_NUM, newValue);
            }
        }
    }

    function _isUnumberedHtmlParagraph(editor, indentationStatus) {
        var paragraph = $(editor.element.$).find(HTML_POINT).first()
        if (!!indentationStatus) {
            return (!indentationStatus.original.numbered && paragraph && paragraph.attr(DATA_AKN_ELEMENT) && paragraph.attr(DATA_AKN_ELEMENT).toLocaleLowerCase() == PARAGRAPH && !_hasHtmlNum(paragraph));
        } else {
            return (paragraph && paragraph.attr(DATA_AKN_ELEMENT) && paragraph.attr(DATA_AKN_ELEMENT).toLocaleLowerCase() == PARAGRAPH && !_hasHtmlNum(paragraph));
        }
    }

    function _isUnumberedparagraph(paragraph) {
        return (paragraph && paragraph.prop("tagName").toLocaleLowerCase() == PARAGRAPH && !_hasNum(paragraph));
    }

    function _hasHtmlNum(element) {
        if (element.attr(DATA_AKN_NUM)) {
            var dataAknNumAttrSoftAction = element.attr(DATA_AKN_NUM_SOFTACTION);
            if (!!dataAknNumAttrSoftAction && dataAknNumAttrSoftAction == DEL) {
                return false;
            }
            return true;
        }
        return false;
    }

    function _hasNum(element) {
        var childNum = element.children(NUM);
        if (childNum.length > 0) {
            var dataAknNumAttrSoftAction = childNum.attr(LEOS_SOFTACTION);
            if (!!dataAknNumAttrSoftAction && dataAknNumAttrSoftAction == DEL) {
                return false;
            }
            return true;
        }
        return false;
    }

    function _resetIndent(editor, isIndent) {
        editor.fire("resetIndent");

        var source = $(editor.element.$);
        if (_isSubpoint(editor) && _isFirstChild(editor)) {
            source = $(editor.element.$).parents(NUMBERED_ITEM).first();
        }
        source.css({'margin-left': -5});
        _resetIndentOtherItems(editor, source, isIndent);
    }

    function _resetIndentOtherItems(editor, source, isIndent) {
        // if point with alinea and list, the children should not be indented
        if (_isSubpoint(editor) && _isFirstChild(editor) && !isIndent) {
            var list = source.children(LIST).first();
            if (list.length) {
                var children = list.children(NUMBERED_ITEM);
                $.each(children, function(key, child){
                    $(child).css({'margin-left': -5});
                });
            }
            var subpoints = source.children(UNUMBERED_ITEM);
            if (subpoints.length) {
                $.each(subpoints, function(key, child){
                    $(child).css({'margin-left': -5});
                });
            }
        }
        // If second, third, ... alinea, siblings alinea should be outdented
        else if (_isSubpoint(editor) && isIndent) {
            var alineaNextSiblings = source.nextAll(UNUMBERED_ITEM);
            $.each(alineaNextSiblings, function(key, sibling){
                $(sibling).css({'margin-left': -5});
            });
            var list = source.nextAll(LIST);
            if (list.length) {
                var children = list.children(NUMBERED_ITEM);
                $.each(children, function(key, child){
                    $(child).css({'margin-left': -5});
                });
            }
        }
    }

    function _setIndentAttributes(editor, indentationStatus) {
        var li = $(editor.element.$).find(HTML_POINT).first();
        if (li.length) {
            li.attr(ATTR_INDENT_LEVEL, indentationStatus.current.level);
            li.attr(ATTR_INDENT_NUMBERED, indentationStatus.current.numbered);
        }
    }

    function _resetIndentAttributes(editor, indentationStatus) {
        var li = $(editor.element.$).find(HTML_POINT).first();
        if (li.length) {
            li.removeAttr(ATTR_INDENT_LEVEL);
            li.removeAttr(ATTR_INDENT_NUMBERED);
            indentationStatus.current.numbered = indentationStatus.original.numbered;
            indentationStatus.current.prevNumbered = [];
        }
    }

    function _doIndent(editor, indentationStatus) {
        var source = $(editor.element.$);

        editor.fire("indent");

        // If first alinea, should be considered as the point itself
        if (_isFirstChild(editor) && _isSubpoint(editor)) {
            source = $(editor.element.$).parents(NUMBERED_ITEM).first();
        }

        var diff = indentationStatus.current.level - indentationStatus.original.level;
        if (!indentationStatus.current.numbered
            && indentationStatus.original.numbered
            && (!source.parents(LEVEL).length || (_isSubpoint(editor) && _isFirstChild(editor)))) {
            source.css({'margin-left': 40 * (diff - 1) - 5});
        } else if (indentationStatus.original.level == 1 && !indentationStatus.original.numbered
            && !source.parents(LEVEL).length) {
            source.css({'margin-left': 40 * (diff + 1) - 5});
        } else if (source.parents(PARAGRAPH).length
            && _isUnumberedparagraph(source.parents(PARAGRAPH))
            && indentationStatus.current.level == 0
            && indentationStatus.current.numbered
            && indentationStatus.original.numbered) {
            source.css({'margin-left': 40 * (diff - 1) - 5});
        } else {
            source.css({'margin-left': 40 * diff - 5});
        }
        _doIndentOtherItems(editor, source, indentationStatus);
    }

    function _doIndentOtherItems(editor, source, indentationStatus) {
        var diff = indentationStatus.current.level - indentationStatus.original.level;

        // For indent, children should not be indented
        if (_isSubpoint(editor) && _isFirstChild(editor) && indentationStatus.current.move > 0) {
            var childrenElts = source.children(ITEMS_SELECTOR);
            if (childrenElts.length) {
                var levelDiff = indentationStatus.current.numbered ? -diff : -diff + 1;
                $.each(childrenElts, function(key, child){
                    if ($(child).prop("tagName").toLowerCase() == LIST) {
                        var children = $(child).children(NUMBERED_ITEM);
                        $.each(children, function (key, listChild) {
                            if (diff != 0) {
                                $(listChild).css({'margin-left': 40 * levelDiff - 5});
                            } else {
                                $(listChild).css({'margin-left': - 5});
                            }
                        });
                    } else if ($(child).prop("tagName").toLowerCase() == ALINEA ||
                        $(child).prop("tagName").toLowerCase() == SUBPARAGRAPH) {
                        if (diff != 0) {
                            $(child).css({'margin-left': 40 * levelDiff - 5});
                        } else {
                            $(child).css({'margin-left': - 5});
                        }
                    }
                });
            }
        }
        // If outdenting, next siblings should be outdented only when there is a change of level
        if (indentationStatus.current.move < 0) {
            var nextElts = source.nextAll(ITEMS_SELECTOR);
            if (nextElts.length) {
                $.each(nextElts, function(key, next){
                    var levelDiff = indentationStatus.current.numbered ? 1 : 0;
                    if ($(next).prop("tagName").toLowerCase() == LIST) {
                        var children = $(next).children(NUMBERED_ITEM);
                        $.each(children, function(key, child){
                            if (diff != 0) {
                                $(child).css({'margin-left': 40 * (diff+levelDiff) - 5});
                            } else {
                                $(child).css({'margin-left': - 5});
                            }
                        });
                    } else {
                        if (diff != 0) {
                            $(next).css({'margin-left': 40 * (diff+levelDiff) - 5});
                        } else {
                            $(next).css({'margin-left': - 5});
                        }
                    }
                });
            }
        }
    }

    function _popSingleSubElement(singleSubElements) {
        singleSubElements.forEach(function (subElement) {
            var parent = subElement.getParent();
            if (parent) {
                while (subElement.getChildCount() > 0) {
                    subElement.getChild(0).insertBefore(subElement);
                    _copyContentAndMpAttributeToElement(subElement, parent);
                }
                subElement.remove();
            }
        });
    }

    function _copyContentAndMpAttributeToElement(from, to) {
        if (!!from.getAttribute(DATA_AKN_WRAPPED_CONTENT_ID)) {
            to.setAttribute(DATA_AKN_WRAPPED_CONTENT_ID, from.getAttribute(DATA_AKN_WRAPPED_CONTENT_ID));
            to.setAttribute(DATA_AKN_CONTENT_ID, from.getAttribute(DATA_AKN_WRAPPED_CONTENT_ID));
        }
        if (!!from.getAttribute(DATA_WRAPPED_CONTENT_ORIGIN)) {
            to.setAttribute(DATA_WRAPPED_CONTENT_ORIGIN, from.getAttribute(DATA_WRAPPED_CONTENT_ORIGIN));
            to.setAttribute(DATA_CONTENT_ORIGIN, from.getAttribute(DATA_WRAPPED_CONTENT_ORIGIN));
        }
        if (!!from.getAttribute(DATA_AKN_CONTENT_ID)) {
            to.setAttribute(DATA_AKN_WRAPPED_CONTENT_ID, from.getAttribute(DATA_AKN_CONTENT_ID));
            to.setAttribute(DATA_AKN_CONTENT_ID, from.getAttribute(DATA_AKN_CONTENT_ID));
        }
        if (!!from.getAttribute(DATA_CONTENT_ORIGIN)) {
            to.setAttribute(DATA_WRAPPED_CONTENT_ORIGIN, from.getAttribute(DATA_CONTENT_ORIGIN));
            to.setAttribute(DATA_CONTENT_ORIGIN, from.getAttribute(DATA_CONTENT_ORIGIN));
        }
        if (!!from.getAttribute(DATA_AKN_MP_ID)) {
            to.setAttribute(DATA_AKN_MP_ID, from.getAttribute(DATA_AKN_MP_ID));
        } else {
            to.setAttribute(DATA_AKN_MP_ID, "");
        }
        if (!!from.getAttribute(DATA_MP_ORIGIN)) {
            to.setAttribute(DATA_MP_ORIGIN, from.getAttribute(DATA_MP_ORIGIN));
        } else {
            to.setAttribute(DATA_MP_ORIGIN, "");
        }
    }

    function _handleIndentAttributes(node, isIndentAttributesToBeSet) {
        if (isIndentAttributesToBeSet && !INLINE_FROM_MATCH.test(node.getName()) && node.getAttribute(DATA_AKN_ELEMENT)) {
            var elementName = node.getAttribute(DATA_AKN_ELEMENT).toUpperCase();
            switch(elementName) {
                case "ALINEA":
                    elementName = "OTHER_SUBPOINT";
                    break;
                case "SUBPARAGRAPH":
                    var parentName = node.getParent().getName().toLowerCase();
                    var grandParentName = node.getParent().getParent().getAttribute(DATA_AKN_ELEMENT).toUpperCase();
                    if (parentName == "ol" && grandParentName == "PARAGRAPH") {
                        elementName = "OTHER_SUBPARAGRAPH";
                    } else {
                        elementName = "OTHER_SUBPOINT";
                    }
                    break;
                case "INDENT":
                    elementName = "POINT";
                    break;
            }
            if (elementName == 'PARAGRAPH'
                || elementName == 'OTHER_SUBPARAGRAPH'
                || elementName == 'OTHER_SUBPOINT'
                || !!node.getAttribute(DATA_AKN_NUM_ID)) {
                node.setAttribute(DATA_INDENT_ORIGIN_TYPE, elementName);
                node.getAttribute(DATA_AKN_NUM) ? node.setAttribute(DATA_INDENT_ORIGIN_NUMBER, node.getAttribute(DATA_AKN_NUM)) : null;
            }
            if (!!node.getAttribute(DATA_AKN_NUM_ID)) {
                node.setAttribute(DATA_INDENT_ORIGIN_NUMBER_ID, node.getAttribute(DATA_AKN_NUM_ID));
            }
            if (!!node.getAttribute(DATA_NUM_ORIGIN)) {
                node.setAttribute(DATA_INDENT_ORIGIN_NUMBER_ORIGIN, node.getAttribute(DATA_NUM_ORIGIN));
            }
        }
    }

    function _hasPointAttribute(element) {
        return (!!element.attributes[DATA_AKN_ELEMENT]
            && element.attributes[DATA_AKN_ELEMENT].value == POINT);
    }

    function _getArticleType(element, articleTypesConfig) {
        var type = REGULAR;
        var article = $(element.$).parents(ARTICLE);
        if (article.length == 0) {
            article = $(element.$).find(ARTICLE);
        }
        if (article.length > 0) {
            article = article.get(0);
            for (var key in articleTypesConfig) {
                var attribute = articleTypesConfig[key];
                if (!!attribute.attributeName && attribute.attributeName === "") {
                    type = key;
                } else if (!!attribute.attributeName
                    && (article.hasAttribute(attribute.attributeName)
                        || article.hasAttribute(attribute.attributeName.toLowerCase()))) {
                    if (article.getAttribute(attribute.attributeName) == attribute.attributeValue
                        || article.getAttribute(attribute.attributeName.toLowerCase()) == attribute.attributeValue) {
                        return key;
                    }
                }
            }
        }
        return type;
    }

    return {
        hasTextOrBogusAsNextSibling: _hasTextOrBogusAsNextSibling,
        getElementName: _getElementName,
        setFocus: _setFocus,
        calculateListLevel: _calculateListLevel,
        getAnnexList: _getAnnexList,
        isSelectionInFirstLevelList: _isSelectionInFirstLevelList,
        isAnnexList: _isAnnexList,
        isOrderedAnnexList: _isOrderedAnnexList,
		isUnnumberedCNParagraph: _isUnnumberedCNParagraph,
		isAnnexUnnumberedCNParagraph: _isAnnexUnnumberedCNParagraph,
		isAnnexSubparagraphElement: _isAnnexSubparagraphElement,
        isSubparagraph: _isSubparagraph,
        isPointOrIndent: _isPointOrIndent,
        isListIntro: _isListIntro,
        isListEnding: _isListEnding,
        getNestingLevelForOl: _getNestingLevelForOl,
        convertToCrossheading: _convertToCrossheading,
        setCrossheadingIndentAttribute: _setCrossheadingIndentAttribute,
        isCrossHeading: _isCrossHeading,
        isCrossHeadingInList: _isCrossHeadingInList,
        setCrossheadingNumProperty: _setCrossheadingNumProperty,
        removeCrossheadingNumProperty: _removeCrossheadingNumProperty,
        isFirstList: _isFirstList,
        isListDepthMoreThanThreshold: _isListDepthMoreThanThreshold,
        getIndentLevel: _getIndentLevel,
        isSubpoint: _isSubpoint,
        isFirstChild: _isFirstChild,
        getCurrentNumValue: _getCurrentNumValue,
        getDepth: _getDepth,
        setCurrentNumValue: _setCurrentNumValue,
        isUnumberedHtmlParagraph: _isUnumberedHtmlParagraph,
        isUnumberedparagraph: _isUnumberedparagraph,
        resetIndent: _resetIndent,
        setIndentAttributes: _setIndentAttributes,
        resetIndentAttributes: _resetIndentAttributes,
        doIndent: _doIndent,
        popSingleSubElement : _popSingleSubElement,
        handleIndentAttributes : _handleIndentAttributes,
        moveChildren: _moveChildren,
        moveElementChildren: _moveElementChildren,
        managePoints: _managePoints,
        manageEmptyLists: _manageEmptyLists,
        manageEmptySubparagraphs: _manageEmptySubparagraphs,
        manageSiblingLists: _manageSiblingLists,
        manageCrossheadings: _manageCrossheadings,
        copyContentAndMpAttributeToElement: _copyContentAndMpAttributeToElement,
        hasPointAttribute: _hasPointAttribute,
        getArticleType: _getArticleType,
        MAX_LEVEL_DEPTH: MAX_LEVEL_DEPTH,
        MAX_LIST_LEVEL: MAX_LIST_LEVEL,
        MAX_LEVEL_LIST_DEPTH: MAX_LEVEL_LIST_DEPTH,
        HTML_POINT: HTML_POINT,
        HTML_SUB_POINT: HTML_SUB_POINT,
        DATA_ORIGIN: DATA_ORIGIN,
        MAINBODY: MAINBODY,
        ARTICLE: ARTICLE,
        POINT: POINT,
        INDENT: INDENT,
        CROSSHEADING: CROSSHEADING,
        LIST: LIST,
        ALINEA: ALINEA,
        SUBPARAGRAPH: SUBPARAGRAPH,
        PARAGRAPH: PARAGRAPH,
        LEVEL: LEVEL,
        ORDER_LIST_ELEMENT: ORDER_LIST_ELEMENT,
        DATA_AKN_NAME: DATA_AKN_NAME,
        DATA_AKN_ELEMENT: DATA_AKN_ELEMENT,
        DATA_AKN_ID: DATA_AKN_ID,
        ID: ID,
        EC: EC,
        CN: CN,
        DATA_AKN_NUM: DATA_AKN_NUM,
        DATA_AKN_NUM_ID: DATA_AKN_NUM_ID,
        DATA_INDENT_ORIGIN_NUM_ID: DATA_INDENT_ORIGIN_NUM_ID,
        DATA_AKN_WRAPPED_CONTENT_ID: DATA_AKN_WRAPPED_CONTENT_ID,
        DATA_AKN_MP_ID: DATA_AKN_MP_ID,
        LEOS_SOFTACTION: LEOS_SOFTACTION,
        DEL: DEL,
        MOVETO: MOVETO,
        CROSSHEADING_LIST_ATTR: CROSSHEADING_LIST_ATTR,
        DATA_INDENT_LEVEL_ATTR: DATA_INDENT_LEVEL_ATTR,
        INDENT_LEVEL_ATTR: INDENT_LEVEL_ATTR,
        AKN_ORDERED_ANNEX_LIST: AKN_ORDERED_ANNEX_LIST,
        COUNCIL_INSTANCE: COUNCIL_INSTANCE
    };
});
