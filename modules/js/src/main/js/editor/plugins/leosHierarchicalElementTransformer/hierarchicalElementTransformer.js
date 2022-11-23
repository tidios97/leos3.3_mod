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
define(function hierarchicalElementTransformer(require) {
    "use strict";
    var STAMPIT = require("stampit");
    var LOG = require("logger");

    var INLINE_FROM_MATCH = /^(text|span|strong|em|u|sup|sub|br|a|img|mref|del)$/;
    var TABLE_ELEMENT_MATCH = /^(table)$/; 
    
    var DATA_AKN_NUM = "data-akn-num";
    var DATA_AKN_NUM_ID = "data-akn-num-id";
    var DATA_AKN_CONTENT_ID = "data-akn-content-id";
    var DATA_AKN_WRAPPED_CONTENT_ID = "data-akn-wrapped-content-id";
    var DATA_AKN_MP_ID = "data-akn-mp-id";
    var DATA_NUM_ORIGIN = "data-num-origin";
    var DATA_CONTENT_ORIGIN = "data-content-origin";
    var DATA_WRAPPED_CONTENT_ORIGIN = "data-wrapped-content-origin";
    var DATA_MP_ORIGIN = "data-mp-origin";
    var DATA_ORIGIN = "data-origin";
    var CONTENT_EDITABLE = "contenteditable";
    var DATA_AKN_EDITABLE = "data-akn-attr-editable";
    var DATA_AKN_SOFTACTION = "data-akn-attr-softaction";
    var DATA_AKN_SOFTACTION_ROOT = "data-akn-attr-softactionroot";
    var DATA_AKN_SOFTUSER = "data-akn-attr-softuser";
    var DATA_AKN_SOFTDATE = "data-akn-attr-softdate";    
    var DATA_AKN_SOFTMOVE_TO = "data-akn-attr-softmove_to";
    var DATA_AKN_SOFTMOVE_FROM = "data-akn-attr-softmove_from";
    var DATA_AKN_SOFTMOVE_LABEL = "data-akn-attr-softmove_label";
    var DATA_AKN_SOFTTRANS_FROM = "data-akn-attr-softtrans_from";
    var DATA_AKN_NUM_SOFTACTION = "data-akn-num-attr-softaction";
    var DATA_AKN_NUM_SOFTACTION_ROOT = "data-akn-num-attr-softactionroot";
    var DATA_AKN_NUM_SOFTUSER = "data-akn-num-attr-softuser";
    var DATA_AKN_NUM_SOFTDATE = "data-akn-num-attr-softdate";
    var LEOS_ORIGINAL_DEPTH_ATTR = "leos:originaldepth";
    var DATA_INDENT_LEVEL = "data-indent-level";
    var DATA_INDENT_NUMBERED = "data-indent-numbered";
    var LEOS_INDENT_LEVEL = "leos:indent-level";
    var LEOS_INDENT_NUMBERED = "leos:indent-numbered";
    var DATA_INDENT_ORIGIN_LEVEL = "data-indent-origin-indent-level";
    var DATA_INDENT_ORIGIN_NUMBER = "data-indent-origin-num";
    var DATA_INDENT_ORIGIN_NUMBER_ID = "data-indent-origin-num-id";
    var DATA_INDENT_ORIGIN_NUMBER_ORIGIN = "data-indent-origin-num-origin";
    var DATA_INDENT_ORIGIN_TYPE = "data-indent-origin-type";
    var DATA_INDENT_UNUMBERED_PARAGRAPH = "data-indent-unumbered-paragraph";
    var DATA_AKN_CROSS_HEADING_TYPE = "data-akn-crossheading-type";
    var DATA_AKN_ATTR_RENUMBERED = "data-akn-attr-renumbered";
    var LEOS_CROSS_HEADING_TYPE= "leos:crossheading-type";
    var DATA_LIST_CROSS_HEADING_PATH = "list/crossheading";
    var LEOS_INDENT_ORIGIN_LEVEL = "leos:indent-origin-indent-level";
    var LEOS_INDENT_ORIGIN_NUMBER = "leos:indent-origin-num";
    var LEOS_INDENT_ORIGIN_NUMBER_ID = "leos:indent-origin-num-id";
    var LEOS_INDENT_ORIGIN_NUMBER_ORIGIN = "leos:indent-origin-num-origin";
    var LEOS_INDENT_ORIGIN_TYPE = "leos:indent-origin-type";
    var LEOS_INDENT_UNUMBERED_PARAGRAPH = "leos:indent-unumbered-paragraph";
    var STYLE = "style";
    var INLINE_NUM = "crossHnum";
    var PARAGRAPH = 'paragraph';
    var AKN_NUMBERED_PARAGRAPH = 'aknNumberedParagraph';

    /*
     * Create content elements with wrapping element(e.g.: alinea, subparagraph)  
     */
    function createContentWrapper(element, rootPath, wrapper) {
        var inlineGroup = [];
        var that = this;
        var isFirstInlineChildElement = true;
        element.children.forEach(function(childElement) {
            var elementName = that._getElementName(childElement);
            var wrapperId = "data-akn-" + wrapper + "-id";
            var wrapperOrigin = "data-" + wrapper + "-origin";
            // KLUG: temporarily fix for in-line elements in text node
            if (elementName === "p" && childElement.attributes['data-akn-heading-content'] === undefined) {
                that.mapToChildProducts(childElement, {
                    toPath: rootPath,
                    toChild: wrapper,
                    attrs: [{
                        from: "id",
                        to: "xml:id",
                        action: "passAttributeTransformer"
                    },{
                        from: DATA_ORIGIN,
                        to: "leos:origin",
                        action: "passAttributeTransformer"
                    },{
                        from: DATA_AKN_SOFTACTION,
                        to: "leos:softaction",
                        action: "passAttributeTransformer"
                    },{
                        from: DATA_AKN_SOFTACTION_ROOT,
                        to: "leos:softactionroot",
                        action: "passAttributeTransformer"
                    },{
                        from: DATA_AKN_SOFTUSER,
                        to: "leos:softuser",
                        action: "passAttributeTransformer"
                    },{
                        from: DATA_AKN_SOFTDATE,
                        to: "leos:softdate",
                        action: "passAttributeTransformer"
                    },{
                        from: DATA_AKN_SOFTMOVE_TO,
                        to: "leos:softmove_to",
                        action: "passAttributeTransformer"
                    },{
                        from: DATA_AKN_SOFTMOVE_FROM,
                        to: "leos:softmove_from",
                        action: "passAttributeTransformer"
                    },{
                        from: DATA_AKN_SOFTMOVE_LABEL,
                        to: "leos:softmove_label",
                        action: "passAttributeTransformer"
                    },{
                        from: DATA_AKN_SOFTTRANS_FROM,
                        to: "leos:softtrans_from",
                        action: "passAttributeTransformer"
                    },{
                        from: CONTENT_EDITABLE,
                        to: "leos:editable",
                        action: "passAttributeTransformer"
                    },{
                        from: DATA_INDENT_ORIGIN_TYPE,
                        to: "leos:indent-origin-type",
                        action: "passAttributeTransformer"
                    },{
                        from: DATA_INDENT_ORIGIN_NUMBER,
                        to: "leos:indent-origin-num",
                        action: "passAttributeTransformer"
                    },{
                        from: DATA_INDENT_ORIGIN_NUMBER_ID,
                        to: "leos:indent-origin-num-id",
                        action: "passAttributeTransformer"
                    },{
                        from: DATA_INDENT_ORIGIN_NUMBER_ORIGIN,
                        to: "leos:indent-origin-num-origin",
                        action: "passAttributeTransformer"
                    }]
                });
                var contentId = childElement.attributes[DATA_AKN_WRAPPED_CONTENT_ID] ?
                    DATA_AKN_WRAPPED_CONTENT_ID : DATA_AKN_CONTENT_ID;

                var contentOrigin = childElement.attributes[DATA_WRAPPED_CONTENT_ORIGIN] ?
                    DATA_WRAPPED_CONTENT_ORIGIN : DATA_CONTENT_ORIGIN;
                createContent.call(that, childElement, [rootPath, wrapper].join("/"), childElement.children, contentId, contentOrigin);
            } else if (INLINE_FROM_MATCH.test(elementName)) {
                inlineGroup.push(childElement);
                var nextElementName = childElement.next ? that._getElementName(childElement.next) : null;
                if (!nextElementName || !INLINE_FROM_MATCH.test(nextElementName)) {
                    that.mapToChildProducts(childElement.parent, {
                        toPath: rootPath,
                        toChild: wrapper,
                        attrs: [{
                            from: wrapperId,
                            to: "xml:id",
                            action: "passAttributeTransformer"
                        },{
                            from: wrapperOrigin,
                            to: "leos:origin",
                            action: "passAttributeTransformer"
                        }]
                    });
                    if (isFirstInlineChildElement) { //LEOS-3384
                        var contentId = childElement.parent.attributes[DATA_AKN_WRAPPED_CONTENT_ID] ?
                            DATA_AKN_WRAPPED_CONTENT_ID : DATA_AKN_CONTENT_ID;

                        var contentOrigin = childElement.parent.attributes[DATA_WRAPPED_CONTENT_ORIGIN] ?
                            DATA_WRAPPED_CONTENT_ORIGIN : DATA_CONTENT_ORIGIN;
                    	createContent.call(that, childElement.parent, [rootPath, wrapper].join("/"), inlineGroup, contentId, contentOrigin);
                    	isFirstInlineChildElement = false;
                    } else {
                    	createContentWithNoAttributes.call(that, childElement.parent, [rootPath, wrapper].join("/"), inlineGroup);
                    }
                	inlineGroup = [];
                }
            } else if (TABLE_ELEMENT_MATCH.test(elementName)) {
                that.mapToChildProducts(childElement, {
                    toPath: rootPath,
                    toChild: wrapper,
                    attrs: [{
                        from: wrapperId,
                        to: "xml:id",
                        action: "passAttributeTransformer"
                    },{
                        from: wrapperOrigin,
                        to: "leos:origin",
                        action: "passAttributeTransformer"
                    }]
                });
                wrapElementWithContent.call(that, childElement, [rootPath, wrapper].join("/"), DATA_AKN_WRAPPED_CONTENT_ID, DATA_WRAPPED_CONTENT_ORIGIN);
            } else {
                // for other elements eg.: list, etc
                that.mapToNestedChildProduct(childElement, {
                    toPath: rootPath
                });
            }
        });
    }
    
    /*
     * Create content elements without wrapping element(e.g.: alinea, subparagraph)  
     */
    function createContentDirectly(element, rootPath) {
        var inlineGroup = [];
        var that = this;
        element.children.forEach(function(childElement) {
            var childElementName = that._getElementName(childElement);
            if ((typeof childElement.parent.attributes[DATA_AKN_CROSS_HEADING_TYPE] !== 'undefined') && childElement.parent.attributes[DATA_AKN_CROSS_HEADING_TYPE] == "list") {
                if (childElementName === "text") {
                    that.mapToChildProducts(element, {
                        toPath: rootPath,
                        toChild: "text",
                        toChildTextValue: childElement.value
                    });
                } else {
                    that.mapToNestedChildProduct(childElement, {
                        toPath: rootPath
                    });
                }
            } else if (INLINE_FROM_MATCH.test(childElementName)) {
                inlineGroup.push(childElement);
                var nextElementName = childElement.next ? that._getElementName(childElement.next) : null;
                if (!nextElementName || !INLINE_FROM_MATCH.test(nextElementName)) {
                    var contentId = childElement.parent.attributes[DATA_AKN_WRAPPED_CONTENT_ID] ?
                        DATA_AKN_WRAPPED_CONTENT_ID : DATA_AKN_CONTENT_ID;

                    var contentOrigin = childElement.parent.attributes[DATA_WRAPPED_CONTENT_ORIGIN] ?
                        DATA_WRAPPED_CONTENT_ORIGIN : DATA_CONTENT_ORIGIN;

                    createContent.call(that, childElement.parent, rootPath, inlineGroup, contentId, contentOrigin);
                    inlineGroup = [];
                }
            } else if (childElementName === "p") {
                createContent.call(that, childElement, rootPath, childElement.children, DATA_AKN_WRAPPED_CONTENT_ID, DATA_CONTENT_ORIGIN);
            } else if (TABLE_ELEMENT_MATCH.test(childElementName)) {
                wrapElementWithContent.call(that, childElement, rootPath, DATA_AKN_CONTENT_ID, DATA_CONTENT_ORIGIN);
            } else {
                that.mapToNestedChildProduct(childElement, {
                    toPath: rootPath
                });
            }
        });
    }
    
    function createContent(element, rootPath, contentChildren, contentId, contentOrigin) {
        this.mapToChildProducts(element, {
            toPath: rootPath,
            toChild: "content",
            attrs: [{
                from: contentId,
                to: "xml:id",
                action: "passAttributeTransformer"
            },{
                from: contentOrigin,
                to: "leos:origin",
                action: "passAttributeTransformer"
            }]
        });
        var contentPath = rootPath + "/content";
        this.mapToChildProducts(element, {
            toPath: contentPath,
            toChild: "mp",
            attrs: [{
                from: DATA_AKN_MP_ID,
                to: "xml:id",
                action: "passAttributeTransformer"
            },{
                from: DATA_MP_ORIGIN,
                to: "leos:origin",
                action: "passAttributeTransformer"
            }]
        });
        createContentChildren.call(this, element, contentPath + "/mp", contentChildren);
    }
    
    function createContentWithNoAttributes(element, rootPath, contentChildren) { //LEOS-3384
        this.mapToChildProducts(element, {
            toPath: rootPath,
            toChild: "content",
            attrs: []
        });
        var contentPath = rootPath + "/content";
        this.mapToChildProducts(element, {
            toPath: contentPath,
            toChild: "mp",
            attrs: []
        });
        createContentChildren.call(this, element, contentPath + "/mp", contentChildren);
    }
    
    function wrapElementWithContent(element, rootPath, contentId, contentOrigin) {
        this.mapToChildProducts(element, {
            toPath: rootPath,
            toChild: "content",
            attrs: [{
                from: contentId,
                to: "xml:id",
                action: "passAttributeTransformer"
            },{
                from: contentOrigin,
                to: "leos:origin",
                action: "passAttributeTransformer"
            }]
        });
        var contentPath = rootPath + "/content";
        this.mapToNestedChildProduct(element, {
            toPath: contentPath
        });
    }

    function shouldContentBeWrapped(element) {
        var that = this;
        var numOfNonSpaceElements = 0;
        var childElement;
        var isBlockElementPresent = false;
        for (var ii = 0; ii < element.children.length; ii++) {
            childElement = element.children[ii];
            var childElementName = this._getElementName(childElement);
            if (childElementName === "text" && childElement.value.trim() === "") {
                continue;
            } else {
                numOfNonSpaceElements++;
            }
            if (!INLINE_FROM_MATCH.test(childElementName)) {
                isBlockElementPresent = true;
            }
            if (numOfNonSpaceElements > 1 && isBlockElementPresent) {
                return true;
            }
        }
        return false;
    }
    
    var getElementName = function getElementName(element) {
        var elementName = null;
        if (element instanceof CKEDITOR.htmlParser.element) {
            elementName = element.name;
        } else if (element instanceof CKEDITOR.htmlParser.text) {
            elementName = "text";
        } else {
            elementName = "unknown";
        }

        return elementName;
    };

    var getElementPath = function getElementPath(element, fragment) {
        var hierarchicalPartsOfPath = [];
        var currentElement = element;
        do {
            hierarchicalPartsOfPath.unshift(getElementName(element).toLowerCase());
            if (currentElement === fragment) {
                break;
            } else {
                currentElement = currentElement.parent;
            }
            // check if the path is calculated for current element's parent if yes use it
            if (currentElement.hierarchicalPartsOfPath) {
                hierarchicalPartsOfPath = currentElement.hierarchicalPartsOfPath.concat(hierarchicalPartsOfPath);
                break;
            }
        } while (currentElement !== fragment);
        element.hierarchicalPartsOfPath = hierarchicalPartsOfPath;
        return hierarchicalPartsOfPath.join("/");
    };

    function createContentChildren(element, rootPath, children) {
        var that = this;
        children.forEach(function(childElement) {
            var childElementName = that._getElementName(childElement);
            if (childElementName === "text") {
                that.mapToChildProducts(element, {
                    toPath: rootPath,
                    toChild: "text",
                    toChildTextValue: childElement.value
                });
            } else {
                that.mapToNestedChildProduct(childElement, {
                    toPath: rootPath
                });
            }

        });
    }

    function containsPath(element, pathToBeMatched) {
        var matched = false;
        element.forEach(function(el) {
            var path = getElementPath(el, element);
            if (pathToBeMatched === path) {
                matched = true;
                return false;
            }
        });
        return matched;
    }
    
    /* Get the value of attribute for the element whose elementName is passed as parameter 
     * if found returns the value else returns empty string
     */
    function getElementAttrVal(elementName, attrName, currentElement) {
        if(!elementName || !attrName || !currentElement) {
            return "";
        }
        var element = currentElement.getAscendant(elementName);
        var attrVal = element && element.attributes[attrName.toLowerCase()];
        return attrVal ? attrVal : "";
    }
    
    var anchor = function(content) {
        return "^" + content + "$";
    };

    function getRootElementsForFromRegExpString(rootElementsForFrom) {
        var result = "";
        rootElementsForFrom.forEach(function(item) {
            result = result
                + (result === "" ? "" : "\/")
                + (Array.isArray(item.elementTags) ? ("(" + item.elementTags.join("|") + ")") : item);
        });
        return result;
    }

    function getRootsElementsPathForFrom(element, rootElementsForFrom){
        var result = "";
        rootElementsForFrom.forEach(function(item){
            result = result
                + (result === "" ? "" : "/")
                + (Array.isArray(item.elementTags) ? (item.elementTags[item.elementTagIndexProvider ? item.elementTagIndexProvider(element) : 0]) : item);
        });
        return result;
    }

    function getChildElementFromRootElement(element, rootElementsForFrom) {
        var result = getRootsElementsPathForFrom(element, rootElementsForFrom);
        if(result && result.indexOf("\/") != -1) {
            return result.substring(result.lastIndexOf("\/") + 1, result.length);
        }
        return result;
    }

    function getElementNameFromRootElement(element, rootElementsForFrom) {
        var result = getChildElementFromRootElement(element, rootElementsForFrom);
        if(result && result === PARAGRAPH) {
            return AKN_NUMBERED_PARAGRAPH;
        }
        return result;
    }

    var hierarchicalElementTransformerStamp = STAMPIT().enclose(
            //executed on the instance creation
            function init() {
                var rootElementsForFrom = this.rootElementsForFrom;
                var contentWrapperForFrom = this.contentWrapperForFrom;
                var rootElementsForTo = this.rootElementsForTo;
              //Regular expressions from AKN to HTML side
                var PSR = "\/";
                var rootElementsForFromRegExpString = getRootElementsForFromRegExpString(rootElementsForFrom);
                var rootElementsForFromRegExp = new RegExp(anchor(rootElementsForFromRegExpString));
                var rootElementsWithNumForFromRegExp = new RegExp(anchor([rootElementsForFromRegExpString, "\/num"].join("")));
                var rootElementsWithCrossHeadingInlineForFromRegExp = new RegExp(anchor([rootElementsForFromRegExpString, "\/inline"].join("")));
                var rootElementsWithTextForFromRegExp = new RegExp(anchor([rootElementsForFromRegExpString, "\/text"].join("")));
                var rootElementsWithNumAndTextForFromRegExp = new RegExp(anchor([rootElementsForFromRegExpString, "\/num\/text"].join("")));
                var rootElementsWithCrossHeadingInlineAndTextForFromRegExp = new RegExp(anchor([rootElementsForFromRegExpString, "\/inline\/text"].join("")));
                var rootElementsWithContentForFromRegExp = new RegExp(anchor([rootElementsForFromRegExpString, "\/content"].join("")));
                var rootElementsWithContentAndMpForFromRegExp = new RegExp(anchor([rootElementsForFromRegExpString, "\/content\/mp"].join("")));
                // path = paragraph/subparagraph
                var rootElementsWithContentWrapperForFromRegExp = new RegExp(anchor([rootElementsForFromRegExpString, PSR, contentWrapperForFrom].join("")));
                //path = paragraph/subparagraph/content
                var rootElementsWithContentWrapperAndContentForFromRegExp = new RegExp(anchor([rootElementsForFromRegExpString, "\/(", contentWrapperForFrom,
                        "\/)?content"].join("")));
                //path = paragraph/subparagraph/content/? (any nested element e.g. table except mp|text|num|subparagraph)
                var rootElementsWithContentWrapperAndContentAndNestedElementForFromRegExp = new RegExp(anchor([rootElementsForFromRegExpString, "\/(", contentWrapperForFrom,
                        "\/)?content", "\/((?!text|num|mp|", contentWrapperForFrom, ").)+"].join("")));
                //path = paragraph/subparagraph/content/mp
                var rootElementsWithContentWrapperAndContentAndMpForFromRegExp = new RegExp(anchor([rootElementsForFromRegExpString, "\/(", contentWrapperForFrom,
                        "\/)?content\/mp"].join("")));
                //path = paragraph/subparagraph/content/mp/text
                var rootElementsWithContentWrapperAndTextForFromRegExp = new RegExp(anchor([rootElementsForFromRegExpString, "\/(", contentWrapperForFrom,
                        "\/)?content\/mp\/text"].join("")));
                //path = paragraph/subparagraph/content/mp/? (anything except text)
                var rootElementsWithContentWrapperAndNestedForFromRegExp = new RegExp(anchor([rootElementsForFromRegExpString, "\/(", contentWrapperForFrom,
                        "\/)?content\/mp\/((?!text).)+"].join("")));
                //path = paragraph/content? (anything after paragraph/content e.g. table except text|num|subparagraph)
                var rootElementsWithContentAndNestedElementForFromRegExp = new RegExp(anchor([rootElementsForFromRegExpString, "\/((?!text|num|", contentWrapperForFrom,
                        ").)+"].join("")));
                //path = paragraph/? (anything after paragraph e.g. list except text|num|subparagraph|content)
                var rootElementsWithNestedElementForFromRegExp = new RegExp(anchor([rootElementsForFromRegExpString, "\/((?!text|num|", contentWrapperForFrom,
                        "|content).)+"].join("")));

                //Regular expressions from HTML to AKN side
                var rootElementForToRegExp = new RegExp(anchor(rootElementsForTo.join(PSR)));
                // <= end of regular expression section
                //path section
                var rootsElementsPathForTo = rootElementsForTo.join("/");
                var rootsElementsWithPPathForTo = [rootsElementsPathForTo, "p"].join("/");
                // <=end of path section

                //content wrapper id (for e.g. data-akn-subparagraph-id)
                var contentWrapperId = "data-akn-" + contentWrapperForFrom + "-id";
                var contentWrapperOrigin = "data-" + contentWrapperForFrom + "-origin";
                
                var transformationConfig = {
                    akn: this.firstLevelConfig.akn,
                    html: this.firstLevelConfig.html,
                    attr: this.firstLevelConfig.attr,
                    transformer: {
                        to: {
                            action: function(element) {
                                var path = element.transformationContext.elementPath;
                                this._.isContentWrapperPresent = this._.isContentWrapperPresent === undefined ? false : this._.isContentWrapperPresent;

                                if (rootElementsForFromRegExp.test(path)) {
                                    //For rootElement with 2 elements e.g. rootElementsForFrom : [ "list", "point" ]
                                    if (rootElementsForTo.length === 2) {
                                        var that = this;
                                        this.mapToChildProducts(element, {
                                            toPath: rootElementsForTo[0],
                                            toChild: rootElementsForTo[1],
                                            attrs: [{
                                                from: "xml:id",
                                                to: "id",
                                                action: "passAttributeTransformer"
                                            },{
                                                from  : LEOS_CROSS_HEADING_TYPE,
                                                to : DATA_AKN_CROSS_HEADING_TYPE,
                                                action: "passAttributeTransformer"
                                            },{
                                                from: STYLE,
                                                to: STYLE,
                                                action: "passAttributeTransformer"
                                            },{
                                                from: "leos:origin",
                                                to: DATA_ORIGIN,
                                                action: "passAttributeTransformer"
                                            },{
                                                from: "leos:editable",
                                                to: DATA_AKN_EDITABLE,
                                                action: "passAttributeTransformer"
                                            },{
                                                from: "leos:editable",
                                                to: CONTENT_EDITABLE,
                                                action: "passAttributeTransformer"
                                            },{
                                                from: LEOS_ORIGINAL_DEPTH_ATTR,
                                                to: LEOS_ORIGINAL_DEPTH_ATTR,
                                                action: "passAttributeTransformer"
                                            },{
                                                from: LEOS_INDENT_LEVEL,
                                                to: DATA_INDENT_LEVEL,
                                                action: "passAttributeTransformer"
                                            },{
                                                from: LEOS_INDENT_NUMBERED,
                                                to: DATA_INDENT_NUMBERED,
                                                action: "passAttributeTransformer"
                                            },{
                                                from: LEOS_INDENT_ORIGIN_LEVEL,
                                                to: DATA_INDENT_ORIGIN_LEVEL,
                                                action: "passAttributeTransformer"
                                            },{
                                                from: LEOS_INDENT_ORIGIN_NUMBER,
                                                to: DATA_INDENT_ORIGIN_NUMBER,
                                                action: "passAttributeTransformer"
                                            },{
                                                from: LEOS_INDENT_ORIGIN_NUMBER_ID,
                                                to: DATA_INDENT_ORIGIN_NUMBER_ID,
                                                action: "passAttributeTransformer"
                                            },{
                                                from: LEOS_INDENT_ORIGIN_NUMBER_ORIGIN,
                                                to: DATA_INDENT_ORIGIN_NUMBER_ORIGIN,
                                                action: "passAttributeTransformer"
                                            },{
                                                from: LEOS_INDENT_ORIGIN_TYPE,
                                                to: DATA_INDENT_ORIGIN_TYPE,
                                                action: "passAttributeTransformer"
                                            },{
                                                from: LEOS_INDENT_UNUMBERED_PARAGRAPH,
                                                to: DATA_INDENT_UNUMBERED_PARAGRAPH,
                                                action: "passAttributeTransformer"
                                            },{
                                                from: "leos:softaction",
                                                to: DATA_AKN_SOFTACTION,
                                                action: "passAttributeTransformer"
                                            },{
                                                from: "leos:softactionroot",
                                                to: DATA_AKN_SOFTACTION_ROOT,
                                                action: "passAttributeTransformer"
                                            },{
                                                from: "leos:softuser",
                                                to: DATA_AKN_SOFTUSER,
                                                action: "passAttributeTransformer"
                                            },{
                                                from: "leos:softdate",
                                                to: DATA_AKN_SOFTDATE,
                                                action: "passAttributeTransformer"
                                            },{
                                                from: "leos:softmove_to",
                                                to: DATA_AKN_SOFTMOVE_TO,
                                                action: "passAttributeTransformer"
                                            },{
                                                from: "leos:softmove_from",
                                                to: DATA_AKN_SOFTMOVE_FROM,
                                                action: "passAttributeTransformer"
                                            },{
                                                from: "leos:softmove_label",
                                                to: DATA_AKN_SOFTMOVE_LABEL,
                                                action: "passAttributeTransformer"
                                            },{
                                                from: "leos:softtrans_from",
                                                to: DATA_AKN_SOFTTRANS_FROM,
                                                action: "passAttributeTransformer"
                                            },{
                                                to: "data-akn-element",
                                                toValue: getChildElementFromRootElement.call(that, element, rootElementsForFrom),
                                                action: "passAttributeTransformer"
                                            },{
                                                to: "data-akn-name",
                                                toValue: getElementNameFromRootElement.call(that, element, rootElementsForFrom),
                                                action: "passAttributeTransformer"
                                            },{
                                                from: "leos:renumbered",
                                                to: DATA_AKN_ATTR_RENUMBERED,
                                                action: "passAttributeTransformer"
                                            }]
                                        });
                                        this._.isContentWrapperPresent = false;
                                    } else if (rootElementsForTo.length > 2) {
                                        LOG.error("Invalid rootElement configuration found: '", rootElementsForTo);
                                    }
                                } else if(rootElementsWithNumForFromRegExp.test(path)
                                    || (rootElementsWithCrossHeadingInlineForFromRegExp.test(path)
                                        && !!element.attributes["name"] && element.attributes["name"].toLowerCase() == INLINE_NUM.toLowerCase())) {
                                    this.mapToProducts(element, {
                                        toPath: rootsElementsPathForTo,
                                        attrs: [{
                                            from: "xml:id",
                                            to: DATA_AKN_NUM_ID,
                                            action: "passAttributeTransformer"
                                        },{
                                            from: "leos:origin",
                                            to: DATA_NUM_ORIGIN,
                                            action: "passAttributeTransformer"
                                        },{
                                            from: "leos:softaction",
                                            to: DATA_AKN_NUM_SOFTACTION,
                                            action: "passAttributeTransformer"
                                        },{
                                            from: "leos:softactionroot",
                                            to: DATA_AKN_NUM_SOFTACTION_ROOT,
                                            action: "passAttributeTransformer"
                                        },{
                                            from: "leos:softuser",
                                            to: DATA_AKN_NUM_SOFTUSER,
                                            action: "passAttributeTransformer"
                                        },{
                                            from: "leos:softdate",
                                            to: DATA_AKN_NUM_SOFTDATE,
                                            action: "passAttributeTransformer"
                                        }]
                                    });
                                } else if(rootElementsWithTextForFromRegExp.test(path)) {
                                    this.mapToChildProducts(element, {
                                        toPath: rootsElementsPathForTo,
                                        toChild: "text",
                                        toChildTextValue: element.value

                                    });
                                } else if (rootElementsWithNumAndTextForFromRegExp.test(path)
                                    || (rootElementsWithCrossHeadingInlineAndTextForFromRegExp.test(path)
                                        && !!element.parent.attributes["name"] && element.parent.attributes["name"].toLowerCase() == INLINE_NUM.toLowerCase())) {
                                    this.mapToProducts(element, {
                                        toPath: rootsElementsPathForTo,
                                        toAttribute: DATA_AKN_NUM
                                    });
                                } else if(rootElementsWithContentForFromRegExp.test(path)) {
                                    this.mapToProducts(element, {
                                        toPath: rootsElementsPathForTo,
                                        attrs: [{
                                            from: "xml:id",
                                            to: DATA_AKN_CONTENT_ID,
                                            action: "passAttributeTransformer"
                                        },{
                                            from: "leos:origin",
                                            to: DATA_CONTENT_ORIGIN,
                                            action: "passAttributeTransformer"
                                        }]
                                    });
                                } else if(rootElementsWithContentAndMpForFromRegExp.test(path)) {
                                    this.mapToProducts(element, {
                                        toPath: rootsElementsPathForTo,
                                        attrs: [{
                                            from: "xml:id",
                                            to: DATA_AKN_MP_ID,
                                            action: "passAttributeTransformer"
                                        },{
                                            from: "leos:origin",
                                            to: DATA_MP_ORIGIN,
                                            action: "passAttributeTransformer"
                                        }]
                                    });
                                } else if(rootElementsWithContentAndNestedElementForFromRegExp.test(path)) {
                                    this.mapToNestedChildProduct(element, {
                                        toPath: rootsElementsPathForTo,
                                        attrs: [{
                                            to: DATA_AKN_CONTENT_ID,
                                            toValue: getElementAttrVal.call(that, "content", "xml:id", element),
                                            action: "passAttributeTransformer"
                                        },{
                                            to: DATA_CONTENT_ORIGIN,
                                            toValue: getElementAttrVal.call(that, "content", "leos:origin", element),
                                            action: "passAttributeTransformer"
                                        }]
                                    });
                                }
                                else if (rootElementsWithContentWrapperForFromRegExp.test(path)) {
                                    this.mapToProducts(element, {
                                        toPath: rootsElementsPathForTo
                                    });
                                    this._.isContentWrapperPresent = true;
                                } else if(rootElementsWithContentWrapperAndContentForFromRegExp.test(path)) {
                                    this.mapToProducts(element, {
                                        toPath: rootsElementsPathForTo
                                    });
                                } else if(rootElementsWithContentWrapperAndContentAndMpForFromRegExp.test(path)) {
                                    var that = this;
                                    this.mapToChildProducts(element, {
                                        toPath: rootsElementsPathForTo,
                                        toChild: "p",
                                        attrs: [{
                                            to: "id",
                                            toValue: getElementAttrVal.call(that, contentWrapperForFrom, "xml:id", element), //LEOS-2899
                                            action: "passAttributeTransformer"
                                        },{
                                            to: DATA_ORIGIN,
                                            toValue: getElementAttrVal.call(that, contentWrapperForFrom, "leos:origin", element), //LEOS-2899
                                            action: "passAttributeTransformer"
                                        },{
                                            to: DATA_AKN_SOFTACTION,
                                            toValue: getElementAttrVal.call(that, contentWrapperForFrom, "leos:softaction", element),
                                            action: "passAttributeTransformer"
                                        },{
                                            to: DATA_AKN_SOFTACTION_ROOT,
                                            toValue: getElementAttrVal.call(that, contentWrapperForFrom, "leos:softactionroot", element),
                                            action: "passAttributeTransformer"
                                        },{
                                            to: DATA_AKN_SOFTUSER,
                                            toValue: getElementAttrVal.call(that, contentWrapperForFrom, "leos:softuser", element),
                                            action: "passAttributeTransformer"
                                        },{
                                            to: DATA_AKN_SOFTDATE,
                                            toValue: getElementAttrVal.call(that, contentWrapperForFrom, "leos:softdate", element),
                                            action: "passAttributeTransformer"
                                        },{
                                            to: DATA_AKN_SOFTMOVE_TO,
                                            toValue: getElementAttrVal.call(that, contentWrapperForFrom, "leos:softmove_to", element),
                                            action: "passAttributeTransformer"
                                        },{
                                            to: DATA_AKN_SOFTMOVE_FROM,
                                            toValue: getElementAttrVal.call(that, contentWrapperForFrom, "leos:softmove_from", element),
                                            action: "passAttributeTransformer"
                                        },{
                                            to: DATA_AKN_SOFTMOVE_LABEL,
                                            toValue: getElementAttrVal.call(that, contentWrapperForFrom, "leos:softmove_label", element),
                                            action: "passAttributeTransformer"
                                        },{
                                            to: DATA_AKN_SOFTTRANS_FROM,
                                            toValue: getElementAttrVal.call(that, contentWrapperForFrom, "leos:softtrans_from", element),
                                            action: "passAttributeTransformer"
                                        },{
                                            to: CONTENT_EDITABLE,
                                            toValue: getElementAttrVal.call(that, contentWrapperForFrom, "leos:editable", element),
                                            action: "passAttributeTransformer"
                                        },{
                                            to: DATA_AKN_WRAPPED_CONTENT_ID,
                                            toValue: getElementAttrVal.call(that, "content", "xml:id", element),  //LEOS-2899
                                            action: "passAttributeTransformer"
                                        },{
                                            to: DATA_WRAPPED_CONTENT_ORIGIN,
                                            toValue: getElementAttrVal.call(that, "content", "leos:origin", element),  //LEOS-2899
                                            action: "passAttributeTransformer"
                                        },{
                                            from: "xml:id",
                                            to: DATA_AKN_MP_ID,
                                            action: "passAttributeTransformer"
                                        },{
                                            from: "leos:origin",
                                            to: DATA_MP_ORIGIN,
                                            action: "passAttributeTransformer"
                                        },{
                                            to: DATA_INDENT_ORIGIN_TYPE,
                                            toValue: getElementAttrVal.call(that, contentWrapperForFrom, LEOS_INDENT_ORIGIN_TYPE, element),
                                            action: "passAttributeTransformer"
                                        },{
                                            to: DATA_INDENT_ORIGIN_NUMBER,
                                            toValue: getElementAttrVal.call(that, contentWrapperForFrom, LEOS_INDENT_ORIGIN_NUMBER, element),
                                            action: "passAttributeTransformer"
                                        },{
                                            to: DATA_INDENT_ORIGIN_NUMBER_ID,
                                            toValue: getElementAttrVal.call(that, contentWrapperForFrom, LEOS_INDENT_ORIGIN_NUMBER_ID, element),
                                            action: "passAttributeTransformer"
                                        },{
                                            to: DATA_INDENT_ORIGIN_NUMBER_ORIGIN,
                                            toValue: getElementAttrVal.call(that, contentWrapperForFrom, LEOS_INDENT_ORIGIN_NUMBER_ORIGIN, element),
                                            action: "passAttributeTransformer"
                                        },{
                                            to: "data-akn-element",
                                            toValue: contentWrapperForFrom,
                                            action: "passAttributeTransformer"
                                        }]
                                    });
                                } else if (rootElementsWithContentWrapperAndTextForFromRegExp.test(path)) {
                                    var toPath = this._.isContentWrapperPresent ? rootsElementsWithPPathForTo : rootsElementsPathForTo;
                                    this.mapToChildProducts(element, {
                                        toPath: toPath,
                                        toChild: "text",
                                        toChildTextValue: element.value
                                    });
                                } else if(rootElementsWithContentWrapperAndContentAndNestedElementForFromRegExp.test(path)) {
                                    var that = this;
                                    this.mapToNestedChildProduct(element, {
                                        toPath: rootsElementsPathForTo,
                                        attrs: [{
                                            to: contentWrapperId,
                                            toValue: getElementAttrVal.call(that, contentWrapperForFrom, "xml:id", element), //LEOS-2899
                                            action: "passAttributeTransformer"
                                        },{
                                            to: contentWrapperOrigin,
                                            toValue: getElementAttrVal.call(that, contentWrapperForFrom, "leos:origin", element), //LEOS-2899
                                            action: "passAttributeTransformer"
                                        },{
                                            to: DATA_WRAPPED_CONTENT_ORIGIN,
                                            toValue: getElementAttrVal.call(that, "content", "leos:origin", element),  //LEOS-2899
                                            action: "passAttributeTransformer"
                                        },{
                                            to: DATA_AKN_WRAPPED_CONTENT_ID,
                                            toValue: getElementAttrVal.call(that, "content", "xml:id", element),  //LEOS-2899
                                            action: "passAttributeTransformer"
                                        },{
                                            to: "data-akn-element",
                                            toValue: contentWrapperForFrom,
                                            action: "passAttributeTransformer"
                                        }]
                                    });
                                } else if (rootElementsWithContentWrapperAndNestedForFromRegExp.test(path)) {
                                    var toPath = this._.isContentWrapperPresent ? rootsElementsWithPPathForTo : rootsElementsPathForTo;
                                    this.mapToNestedChildProduct(element, {
                                        toPath: toPath
                                    });
                                } else if (rootElementsWithNestedElementForFromRegExp.test(path)) {
                                    this.mapToNestedChildProduct(element, {
                                        toPath: rootsElementsPathForTo
                                    });
                                }
                            },
                            supports: function supports(element) {
                                return containsPath(element, getRootsElementsPathForFrom(element, rootElementsForFrom));
                            }
                        },

                        from: {
                            action: function action(element) {
                                var path = element.transformationContext.elementPath;
                                if (rootElementForToRegExp.test(path)) {
                                    var rootsElementsPathForFrom = getRootsElementsPathForFrom(element, rootElementsForFrom);
                                    if((typeof element.attributes[DATA_AKN_CROSS_HEADING_TYPE] !== 'undefined')
                                        && element.attributes[DATA_AKN_CROSS_HEADING_TYPE] === 'list') {
                                        this.mapToProducts(element, [{
                                            toPath: DATA_LIST_CROSS_HEADING_PATH,
                                            attrs: [{
                                                from: "id",
                                                to: "xml:id",
                                                action: "passAttributeTransformer"
                                            }, {
                                                from: DATA_AKN_CROSS_HEADING_TYPE,
                                                to: LEOS_CROSS_HEADING_TYPE,
                                                action: "passAttributeTransformer"
                                            },{
                                                from: DATA_INDENT_LEVEL,
                                                to: LEOS_INDENT_LEVEL,
                                                action: "passAttributeTransformer"
                                            }, {
                                                from: STYLE,
                                                to: STYLE,
                                                action: "passAttributeTransformer"
                                            }]
                                        }, {
                                            toPath: [rootsElementsPathForFrom, "inline"].join("/"),
                                            attrs: [{
                                                to: "name",
                                                toValue: INLINE_NUM,
                                                action: "passAttributeTransformer"
                                            }, {
                                                from: DATA_AKN_NUM_ID,
                                                to: "xml:id",
                                                action: "passAttributeTransformer"
                                            }, {
                                                from: DATA_NUM_ORIGIN,
                                                to: "leos:origin",
                                                action: "passAttributeTransformer"
                                            }]
                                        }, {
                                            toPath: [rootsElementsPathForFrom, "inline", "text"].join("/"),
                                            fromAttribute: DATA_AKN_NUM
                                        }]);
                                    } else if(element.attributes[DATA_AKN_NUM]) {
                                       this.mapToProducts(element, [{
                                           toPath: rootsElementsPathForFrom,
                                           attrs: [{
                                               from: "id",
                                               to: "xml:id",
                                               action: "passAttributeTransformer"
                                           },{
                                               from: DATA_ORIGIN,
                                               to: "leos:origin",
                                               action: "passAttributeTransformer"
                                           },{
                                               from: DATA_AKN_EDITABLE,
                                               to: "leos:editable",
                                               action: "passAttributeTransformer"
                                           },{
                                               from: LEOS_ORIGINAL_DEPTH_ATTR,
                                               to: LEOS_ORIGINAL_DEPTH_ATTR,
                                               action: "passAttributeTransformer"
                                           },{
                                               from: DATA_INDENT_LEVEL,
                                               to: LEOS_INDENT_LEVEL,
                                               action: "passAttributeTransformer"
                                           },{
                                               from: DATA_INDENT_NUMBERED,
                                               to: LEOS_INDENT_NUMBERED,
                                               action: "passAttributeTransformer"
                                           },{
                                               from: DATA_INDENT_ORIGIN_LEVEL,
                                               to: LEOS_INDENT_ORIGIN_LEVEL,
                                               action: "passAttributeTransformer"
                                           },{
                                               from: DATA_INDENT_ORIGIN_NUMBER,
                                               to: LEOS_INDENT_ORIGIN_NUMBER,
                                               action: "passAttributeTransformer"
                                           },{
                                               from: DATA_INDENT_ORIGIN_NUMBER_ID,
                                               to: LEOS_INDENT_ORIGIN_NUMBER_ID,
                                               action: "passAttributeTransformer"
                                           },{
                                               from: DATA_INDENT_ORIGIN_NUMBER_ORIGIN,
                                               to: LEOS_INDENT_ORIGIN_NUMBER_ORIGIN,
                                               action: "passAttributeTransformer"
                                           },{
                                               from: DATA_INDENT_ORIGIN_TYPE,
                                               to: LEOS_INDENT_ORIGIN_TYPE,
                                               action: "passAttributeTransformer"
                                           },{
                                               from: DATA_INDENT_UNUMBERED_PARAGRAPH,
                                               to: LEOS_INDENT_UNUMBERED_PARAGRAPH,
                                               action: "passAttributeTransformer"
                                           },{
                                               from: DATA_AKN_SOFTACTION,
                                               to: "leos:softaction",
                                               action: "passAttributeTransformer"
                                           },{
                                               from: DATA_AKN_SOFTACTION_ROOT,
                                               to: "leos:softactionroot",
                                               action: "passAttributeTransformer"
                                           },{
                                               from: DATA_AKN_SOFTUSER,
                                               to: "leos:softuser",
                                               action: "passAttributeTransformer"
                                           },{
                                               from: DATA_AKN_SOFTDATE,
                                               to: "leos:softdate",
                                               action: "passAttributeTransformer"
                                           },{
                                               from: DATA_AKN_SOFTMOVE_TO,
                                               to: "leos:softmove_to",
                                               action: "passAttributeTransformer"
                                           },{
                                               from: DATA_AKN_SOFTMOVE_FROM,
                                               to: "leos:softmove_from",
                                               action: "passAttributeTransformer"
                                           },{
                                               from: DATA_AKN_SOFTMOVE_LABEL,
                                               to: "leos:softmove_label",
                                               action: "passAttributeTransformer"
                                           },{
                                               from: DATA_AKN_SOFTTRANS_FROM,
                                               to: "leos:softtrans_from",
                                               action: "passAttributeTransformer"
                                           },{
                                               from: DATA_AKN_ATTR_RENUMBERED,
                                               to: "leos:renumbered",
                                               action: "passAttributeTransformer"
                                           }]
                                       }, {
                                           toPath: [rootsElementsPathForFrom, "num"].join("/"),
                                           attrs: [{
                                               from: DATA_AKN_NUM_ID,
                                               to: "xml:id",
                                               action: "passAttributeTransformer"
                                           },{
                                               from: DATA_NUM_ORIGIN,
                                               to: "leos:origin",
                                               action: "passAttributeTransformer"
                                           },{
                                               from: DATA_AKN_NUM_SOFTACTION,
                                               to: "leos:softaction",
                                               action: "passAttributeTransformer"
                                           },{
                                               from: DATA_AKN_NUM_SOFTACTION_ROOT,
                                               to: "leos:softactionroot",
                                               action: "passAttributeTransformer"
                                           },{
                                               from: DATA_AKN_NUM_SOFTUSER,
                                               to: "leos:softuser",
                                               action: "passAttributeTransformer"
                                           },{
                                               from: DATA_AKN_NUM_SOFTDATE,
                                               to: "leos:softdate",
                                               action: "passAttributeTransformer"
                                           }]
                                       }, {
                                           toPath: [rootsElementsPathForFrom, "num", "text"].join("/"),
                                           fromAttribute: DATA_AKN_NUM
                                       }]);
                                    } else {
                                        this.mapToProducts(element, [{
                                            toPath: rootsElementsPathForFrom,
                                            attrs: [{
                                                from: "id",
                                                to: "xml:id",
                                                action: "passAttributeTransformer"
                                            },{
                                                from: DATA_ORIGIN,
                                                to: "leos:origin",
                                                action: "passAttributeTransformer"
                                            },{
                                                from: DATA_AKN_EDITABLE,
                                                to: "leos:editable",
                                                action: "passAttributeTransformer"
                                            },{
                                                from: DATA_INDENT_LEVEL,
                                                to: LEOS_INDENT_LEVEL,
                                                action: "passAttributeTransformer"
                                            },{
                                                from: DATA_INDENT_NUMBERED,
                                                to: LEOS_INDENT_NUMBERED,
                                                action: "passAttributeTransformer"
                                            },{
                                                from: DATA_INDENT_NUMBERED,
                                                to: LEOS_INDENT_NUMBERED,
                                                action: "passAttributeTransformer"
                                            },{
                                                from: DATA_INDENT_ORIGIN_LEVEL,
                                                to: LEOS_INDENT_ORIGIN_LEVEL,
                                                action: "passAttributeTransformer"
                                            },{
                                                from: DATA_INDENT_ORIGIN_NUMBER,
                                                to: LEOS_INDENT_ORIGIN_NUMBER,
                                                action: "passAttributeTransformer"
                                            },{
                                                from: DATA_INDENT_ORIGIN_NUMBER_ID,
                                                to: LEOS_INDENT_ORIGIN_NUMBER_ID,
                                                action: "passAttributeTransformer"
                                            },{
                                                from: DATA_INDENT_ORIGIN_NUMBER_ORIGIN,
                                                to: LEOS_INDENT_ORIGIN_NUMBER_ORIGIN,
                                                action: "passAttributeTransformer"
                                            },{
                                                from: DATA_INDENT_ORIGIN_TYPE,
                                                to: LEOS_INDENT_ORIGIN_TYPE,
                                                action: "passAttributeTransformer"
                                            },{
                                            	from: DATA_AKN_SOFTACTION,
                                            	to: "leos:softaction",
                                            	action: "passAttributeTransformer"
                                            },{
                                                from: DATA_AKN_SOFTACTION_ROOT,
                                                to: "leos:softactionroot",
                                                action: "passAttributeTransformer"
                                            },{
                                            	from: DATA_AKN_SOFTUSER,
                                            	to: "leos:softuser",
                                            	action: "passAttributeTransformer"
                                            },{
                                            	from: DATA_AKN_SOFTDATE,
                                            	to: "leos:softdate",
                                            	action: "passAttributeTransformer"
                                           },{
                                               from: DATA_AKN_SOFTMOVE_TO,
                                               to: "leos:softmove_to",
                                               action: "passAttributeTransformer"
                                           },{
                                               from: DATA_AKN_SOFTMOVE_FROM,
                                               to: "leos:softmove_from",
                                               action: "passAttributeTransformer"
                                           },{
                                               from: DATA_AKN_SOFTMOVE_LABEL,
                                               to: "leos:softmove_label",
                                               action: "passAttributeTransformer"
                                           },{
                                               from: DATA_AKN_SOFTTRANS_FROM,
                                               to: "leos:softtrans_from",
                                               action: "passAttributeTransformer"
                                           },{
                                                from: DATA_AKN_ATTR_RENUMBERED,
                                                to: "leos:renumbered",
                                                action: "passAttributeTransformer"
                                            }]
                                        }]);
                                    }
                                    var isContentWrapperPresent = shouldContentBeWrapped.call(this, element);
                                    if (isContentWrapperPresent) {
                                        createContentWrapper.call(this, element, rootsElementsPathForFrom, contentWrapperForFrom);
                                    } else {
                                        createContentDirectly.call(this, element, rootsElementsPathForFrom);
                                    }
                                }
                            },
                            supports: function supports(element) {
                                return containsPath(element, rootsElementsPathForTo);
                            }
                        }

                    }
                };

                this.getTransformationConfig = function() {
                    return transformationConfig;
                };
            });

    return hierarchicalElementTransformerStamp;
});