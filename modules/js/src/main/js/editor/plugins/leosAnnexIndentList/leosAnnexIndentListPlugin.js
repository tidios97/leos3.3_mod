/*
 * Copyright 2020 European Commission
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
define(function leosAnnexIndentListPluginModule(require) {
    "use strict";

    // load module dependencies
    var $ = require("jquery");
    var CKEDITOR = require("promise!ckEditor");
    var LOG = require("logger");
    var pluginTools = require("plugins/pluginTools");
    var leosPluginUtils = require("plugins/leosPluginUtils");

    var AKN_ANNEX_LIST = "aknAnnexList";
    var DATA_AKN_NAME_ATTR = "data-akn-name";
    var DATA_AKN_NAME_ELEMENT = "data-akn-element";
    var NUM = "num";
    var LEVEL = "level";
    var LEOS_ORIGINAL_DEPTH_ATTR = "leos:originaldepth";
    var DATA_AKN_ORIGIN_DEPTH_ATTR = "data-akn-origin-depth";
    var DATA_AKN_ORIGIN_NUM_ATTR = "data-akn-origin-num";
    var SOFT_REMOVED_STYLE = "leos-content-soft-removed";
    var MAX_INDENT_FOR_CROSSHEADING = 11;

    var NUMBERED_ITEM = "point, indent, paragraph";
    var UNUMBERED_ITEM = "alinea, subparagraph";
    var ITEMS_SELECTOR = leosPluginUtils.LIST + "," + NUMBERED_ITEM + "," + UNUMBERED_ITEM;
    var DIV = "div";
    var PARAGRAPH = "paragraph";
    var LEVEL = "level";
    var SUBPARAGRAPH = "subparagraph";

    var originalDepth = 0;
    var originalNum;
    var originalOrigin;
    var levelItemVo;

    var pluginName = "leosAnnexIndentListPlugin";

    var indentationStatus = {
        original: {
            num: undefined,
            level: -1,
            parent: '',
            realPosition: -1,
            position: -1,
            numbered: true
        },
        current: {
            level: -1,
            prevNumbered: [],
            move: 0,
            numbered: true
        }
    };

    const CN = "cn";
    const EC = "ec";
    const DATA_NUM_ORIGIN = "data-num-origin";

    var isNotWhitespaces = CKEDITOR.dom.walker.whitespaces( true ),
        isNotBookmark = CKEDITOR.dom.walker.bookmark( false, true ),
        TRISTATE_DISABLED = CKEDITOR.TRISTATE_DISABLED,
        TRISTATE_OFF = CKEDITOR.TRISTATE_OFF;

    var pluginDefinition = {
        requires: 'indent',
        init: function init(editor) {
            var globalHelpers = CKEDITOR.plugins.indent;
            editor.on("receiveLevelItemVo", _getLevelItemVo);

            resetIndentStatus();

            // Register commands.
            globalHelpers.registerCommands( editor, {
                aknindentsubpararaph: new commandDefinition( editor, 'aknindentsubparagraph', true ),
                aknoutdentsubpararaph: new commandDefinition( editor, 'aknoutdentsubparagraph' )
            } );

            function commandDefinition(editor) {
                globalHelpers.specificDefinition.apply( this, arguments );

                // Require ul OR ol list.
                this.requiredContent = ['ul', 'ol', 'p'];

                // Indent and outdent lists with TAB/SHIFT+TAB key. Indenting can
                // be done for any list item that isn't the first child of the parent.
                editor.on('key', function(evt) {
                    var path = editor.elementPath();

                    if (editor.mode != 'wysiwyg')
                        return;

                    if (evt.data.keyCode == this.indentKey) {
                        // Prevent of getting context of empty path (#424)(https://dev.ckeditor.com/ticket/17028).
                        if (!path) {
                            return;
                        }

                        var list = this.getContext(path);

                        if (list) {
                            // Don't indent if in first list item of the parent.
                            // Outdent, however, can always be done to collapse
                            // the list into a paragraph (div).
                            if (this.isIndent && firstItemInPath(this.context, path, list))
                                return;

                            // Exec related global indentation command. Global
                            // commands take care of bookmarks and selection,
                            // so it's much easier to use them instead of
                            // content-specific commands.
                            editor.execCommand(this.relatedGlobal);

                            // Cancel the key event so editor doesn't lose focus.
                            evt.cancel();
                        }
                    }
                }, this);

                // There are two different jobs for this plugin:
                //
                //	* Indent job (priority=10), before indentblock.
                //
                //	  This job is before indentblock because, if this plugin is
                //	  loaded it has higher priority over indentblock. It means that,
                //	  if possible, nesting is performed, and then block manipulation,
                //	  if necessary.
                //
                //	* Outdent job (priority=30), after outdentblock.
                //
                //	  This job got to be after outdentblock because in some cases
                //	  (margin, config#indentClass on list) outdent must be done on
                //	  block-level.

                this.jobs[this.isIndent ? 10 : 30] = {
                    refresh: this.isIndent ?
                        function(editor, path) {
                            var range = getSelectedRange(editor);
                            path = _manageSubparagraphs(range, path);
                            var list = this.getContext(path);
                            var crossheading = _getCrossHeading(path);
                            if (!!crossheading && leosPluginUtils.isCrossHeading(crossheading)) {
                                var indentLevel = _getCrossheadingIndentAttribute(crossheading) ? _getCrossheadingIndentAttribute(crossheading): 0;
                                if (indentLevel == MAX_INDENT_FOR_CROSSHEADING) {
                                    return TRISTATE_DISABLED;
                                }
                                return TRISTATE_OFF;
                            } else {
                                var type = list ? list.getAttribute(DATA_AKN_NAME_ELEMENT) : null;
                                var isLevelNumberIndentable = list
                                    && list.getAttribute(DATA_AKN_NAME_ATTR) === AKN_ANNEX_LIST
                                    && !!list.getAttribute(DATA_AKN_NAME_ELEMENT)
                                    && list.getAttribute(DATA_AKN_NAME_ELEMENT) === LEVEL
                                    && !_isFirstLevelListAtLastIndentPosition(list, path)
                                    && !_isListDepthMoreThanThreshold(getEnclosedLiElement(range.startContainer), getEnclosedLiElement(range.endContainer), leosPluginUtils.MAX_LIST_LEVEL);
                                var isListIntro = leosPluginUtils.isListIntro(path.lastElement);
                                var isListEnding = leosPluginUtils.isListEnding(path.lastElement);
                                var isNotFirstLevelElement = _isNotFirstLevelElement(path);

                                if (_shouldUseCouncilIndentation(list, editor) && LEVEL === type) {
                                    var isFirstLevel = list && isFirstItemInLevel(path, list);
                                    var currLvlNum = list ? _getListNumber(list) : null;
                                    if (_isMaxDepthLevelAchieved(levelItemVo, currLvlNum)) {
                                        return TRISTATE_DISABLED;
                                    } else if (isLevelNumberIndentable || isNotFirstLevelElement) {
                                        return TRISTATE_OFF;
                                    } else if (isFirstLevel) {
                                        return TRISTATE_DISABLED;
                                    }
                                } else if (_shouldUseCouncilIndentation(list, editor) && PARAGRAPH === type) {
                                    _initIndentStatus(editor);
                                    var ol = $(this.getContext(path).$);
                                    if (!_shouldIndent(ol)) {
                                        return TRISTATE_DISABLED;
                                    } else {
                                        return TRISTATE_OFF;
                                    }
                                } else if (isListEnding) {
                                    return TRISTATE_OFF;
                                } else if (_isIndentableInNumberEditor(range)) {
                                    return TRISTATE_OFF;
                                } else if (_isSubparagraphInsideAnnexLevel(range, path, list)) {
                                    return TRISTATE_OFF;
                                } else if ((isLevelNumberIndentable && !isListIntro) || (isNotFirstLevelElement && isFirstLevel)) {
                                    return TRISTATE_OFF;
                                } else if (_isHeadingSelected(range) || _isLevelDepthDiffGreaterThanOne(list) || _isSubparagraphInsideAnnexParagraph(range)) {
                                    return TRISTATE_DISABLED;
                                } else if (_checkLevelListDepthMoreThanThreshold(list, range) && !_isOnlyLevelElementSelected(range.startContainer, range.endContainer)) {
                                    return TRISTATE_OFF;
                                } else if (!list || firstItemInPath(this.context, path, list)
                                    || _isListDepthMoreThanThreshold(getEnclosedLiElement(range.startContainer), getEnclosedLiElement(range.endContainer), leosPluginUtils.MAX_LIST_LEVEL)
                                    || _isFirstPointOrIndentInList(getEnclosedLiElement(range.startContainer), getEnclosedLiElement(range.endContainer))) {
                                    return TRISTATE_DISABLED;
                                } else {
                                    return TRISTATE_OFF;
                                }
                            }
                        } : function(editor, path) {
                            var range = getSelectedRange(editor);
                            path = _manageSubparagraphs(range, path);
                            var list = this.getContext(path);
                            var isSubparagraph = _isSubparagraph(path);
                            var crossheading = _getCrossHeading(path);
                            if (!!crossheading && leosPluginUtils.isCrossHeading(crossheading)) {
                                var indentLevel = _getCrossheadingIndentAttribute(crossheading) ? _getCrossheadingIndentAttribute(crossheading): 0;
                                if (indentLevel == 0) {
                                    return TRISTATE_DISABLED;
                                }
                                return TRISTATE_OFF;
                            } else {
                                var type = list ? list.getAttribute(DATA_AKN_NAME_ELEMENT) : null;
                                //var firstLevelList = isFirstLevelList(editor, list);
                                var firstLevelList = !!list && !list.getAscendant('li');
                                if (_shouldUseCouncilIndentation(list, editor) && PARAGRAPH === type) {
                                    _initIndentStatus(editor);
                                    var ol = $(this.getContext(path).$);
                                    if (!_shouldOutdent(ol)) {
                                        return TRISTATE_DISABLED;
                                    } else {
                                        return TRISTATE_OFF;
                                    }
                                } else if (isSubparagraph) {
                                    return TRISTATE_DISABLED;
                                } else if (list && firstLevelList) {
                                    var listNum = _getListNumber(list);
                                    var depth = _getLevelDepth(listNum);
                                    return (depth <= 1) ? TRISTATE_DISABLED : TRISTATE_OFF;
                                } else if (list && _isSelectionInFirstLevelListInsideAnnexParagraph(range.startContainer)) {
                                    return TRISTATE_DISABLED;
                                } else if (_isOutdentableInNumberEditor(range)) {
                                    return TRISTATE_OFF;
                                } else {
                                    return !list ? TRISTATE_DISABLED : TRISTATE_OFF;
                                }
                            }
                        },

                    exec: CKEDITOR.tools.bind(aknindentList, this)
                };
            }

            CKEDITOR.tools.extend(commandDefinition.prototype, globalHelpers.specificDefinition.prototype, {
                // Elements that, if in an elementpath, will be handled by this
                // command. They restrict the scope of the plugin.
                context: {ol: 1, ul: 1}
            });
        }
    };

    function _shouldIndent(ol) {
        if (indentationStatus.current.move < 0) {
            return true;
        }
        if (!ol.length
            || !(_checkParentAndPosition())
            || (leosPluginUtils.isListDepthMoreThanThreshold(indentationStatus, leosPluginUtils.MAX_LEVEL_LIST_DEPTH))) {
            return false;
        } else {
            return true;
        }
    }

    function _shouldOutdent(ol) {
        if (indentationStatus.current.move > 0) {
            return true;
        }
        if (!ol.length || (leosPluginUtils.isFirstList(indentationStatus) && indentationStatus.current.numbered)
            || (indentationStatus.current.level == 1 && !indentationStatus.current.numbered && !ol.parents(PARAGRAPH).length)) {
            return false;
        } else {
            return true;
        }
    }

    function _checkParentAndPosition() {
        var prevSibling = undefined;

        var currentParent = $('#' + indentationStatus.original.parent);
        if (!indentationStatus.original.position && currentParent.prop("tagName").toLowerCase() != LEVEL) {
            return false;
        }
        if (indentationStatus.original.previous.prop("tagName") && indentationStatus.original.previous.prop("tagName").toLowerCase() != PARAGRAPH) {
            return false;
        }

        var parentChildren = currentParent.children(NUMBERED_ITEM);
        if (!indentationStatus.original.numbered) {
            parentChildren = currentParent.children(ITEMS_SELECTOR);
        }
        prevSibling = $(parentChildren[indentationStatus.original.position - 1]);

        var currentParentDepth = 0;
        if (!prevSibling.length && currentParent.prop("tagName").toLowerCase() == LEVEL) {
            currentParentDepth = 1;
        } else if (!prevSibling.length) {
            return false;
        } else {
            currentParentDepth = leosPluginUtils.getDepth(prevSibling);
        }

        if (currentParentDepth > indentationStatus.current.level) {
            return true;
        } else if (currentParentDepth == indentationStatus.current.level && !indentationStatus.current.numbered) {
            return true;
        } else {
            return false;
        }
    }

    function resetIndentStatus() {
        indentationStatus.original.num = undefined;
        indentationStatus.original.level = -1;
        indentationStatus.original.parent = '';
        indentationStatus.original.previous = undefined;
        indentationStatus.original.position = -1;
        indentationStatus.original.realPosition = -1;
        indentationStatus.original.numbered = true;
        indentationStatus.current.prevNumbered = [];
        indentationStatus.current.level = indentationStatus.original.level;
        indentationStatus.current.move = 0;
        indentationStatus.current.numbered = true;
    }

    function _initIndentStatus(editor) {
        if (indentationStatus.original.level == -1) {
            indentationStatus.original.level = leosPluginUtils.getIndentLevel(editor);
            var source = $(editor.element.$);
            if (leosPluginUtils.isSubpoint(editor) && leosPluginUtils.isFirstChild(editor)) {
                source = $(editor.element.$).parents(NUMBERED_ITEM).first();
            }
            indentationStatus.original.realPosition = source.index();
            indentationStatus.original.position = source.prevAll(ITEMS_SELECTOR).length;
            indentationStatus.original.parent = source.parent().attr('id');
            indentationStatus.original.previous = source.prev();
            if (indentationStatus.original.previous.prop("tagName") && indentationStatus.original.previous.prop("tagName").toLowerCase() == DIV) {
                indentationStatus.original.previous = indentationStatus.original.previous.prev();
            }
            indentationStatus.current.level = indentationStatus.original.level;
            indentationStatus.original.num = leosPluginUtils.getCurrentNumValue(editor);
            indentationStatus.original.numbered = !(leosPluginUtils.isSubpoint(editor) && !leosPluginUtils.isFirstChild(editor));
            indentationStatus.current.numbered = indentationStatus.original.numbered;
        }
    }

    function _isIndentableInNumberEditor(range) {
        return _isPathElementIndentable(numElementInNumberEditor(range));
    }

    function _isOutdentableInNumberEditor(range) {
        return _isPathElementOutdentable(numElementInNumberEditor(range));
    }

    var numElementInNumberEditor = function _getNumElementInNumberEditor(range) {
        var numElement;
        if (_isPlaceHolderOfNumElement(range.startContainer)) {
            numElement = _findNumElement(range.startContainer);
        } else {
            numElement = range.startContainer;
        }
        return numElement;
    }

    function _isPlaceHolderOfNumElement(element) {
        if (element && element.type === CKEDITOR.NODE_ELEMENT && element.getName() === 'div') {
            var numElement = _findNumElement(element);
            return numElement && _isLevelNumElement(numElement);
        }
    }

    function _isPathElementIndentable(numElement) {
        var isNumIndentable = _isLevelNumElement(numElement.type === CKEDITOR.NODE_TEXT ? numElement.getParent() : numElement)
            && !numElement.getText().endsWith('1.')
            && !_isLevelDepthMoreThanThreshold(levelItemVo, _getLevelDepth(numElement.getText()));
        return isNumIndentable;
    }

    function _isPathElementOutdentable(numElement) {
        var isNumOutdentable =  _isLevelNumElement(numElement.type === CKEDITOR.NODE_TEXT ? numElement.getParent() : numElement)
            && _getLevelDepth(numElement.getText()) > 1
        return isNumOutdentable;
    }

    function _findNumElement(element){
        return element.getChildren().toArray().find(getNumElement);
    }

    function getNumElement(element){
        return element.type === CKEDITOR.NODE_ELEMENT && element.getName() === 'p';
    }

    function _checkLevelListDepthMoreThanThreshold(list, range) {
        var isDepthMoreThanThreshold = list && list.getName() === 'ol' && leosPluginUtils.isAnnexList(list)
            && !_isLevelListDepthMoreThanThreshold(getEnclosedLevelElement(range.startContainer),
                getEnclosedLevelElement(range.endContainer), leosPluginUtils.MAX_LEVEL_LIST_DEPTH);
        return isDepthMoreThanThreshold;
    }

    function _getLevelItemVo(evt) {
        levelItemVo = evt.data;
        if(levelItemVo) {
            originalDepth = levelItemVo.levelDepth;
            originalNum = levelItemVo.levelNum;
            originalOrigin = levelItemVo.origin;
        }
    }

    function _getLevelDepth(listNum) {
        let depth = 0;
        if(listNum && listNum.includes(".")) {
            const arr = listNum.split(".");
            depth = arr.length - 1;
        }
        return depth;
    }

    function _getListNumber(list) {
        const listItem = _getListItem(list);
        return listItem? listItem.getAttribute('data-akn-num') : null;
    }

    function _getListItem(list) {
        let index = 0;
        const children = list.getChildren();
        if(children && children.count() > 1) {
            for(let i = 0; i < children.count(); i++) {
                if(children.getItem(i).type == CKEDITOR.NODE_ELEMENT && children.getItem(i).getName() === "li") {
                    index = i;
                    break;
                }
            }
            return children.getItem(index);
        }
    }

    function _getNextLevels(listNode) {
        if(listNode.$.parentNode) {
            return $(listNode.$.parentNode).nextAll(LEVEL);
        }
    }

    function _getNextLevelNum(listNode) {
        const $nextLevels = _getNextLevels(listNode);
        return _getLevelNum($nextLevels, true);
    }

    function _getPrevLevels(listNode) {
        if(listNode.$.parentNode) {
            return $(listNode.$.parentNode).prevAll(LEVEL);
        }
    }

    function _getPrevLevelNum(listNode) {
        const $prevLevels = _getPrevLevels(listNode);
        return _getLevelNum($prevLevels, true);
    }

    function _getLevelNum($levels, asc) {
        if ($levels && $levels.length > 0) {
            var $num;
            if (asc) {
                $num = $($levels.first().find(NUM)[0]);
            } else {
                $num = $($levels.last().find(NUM)[0]);
            }
            var $content = $num.contents()
                .filter(function () {
                    return (this.nodeType === CKEDITOR.NODE_TEXT ||
                        !(this.nodeType === CKEDITOR.NODE_ELEMENT && this.classList.contains(SOFT_REMOVED_STYLE)));
                });
            return $content.text();
        }
    }

    function _renumberOnIndent(editor, listNode) {
        const data = _getData(listNode);

        const indentData = editor.fire("levelIndent", data);

        data.listItem.setAttribute("data-akn-num", indentData.nextNum);
        data.listItem.setAttribute(LEOS_ORIGINAL_DEPTH_ATTR, originalDepth);
        levelItemVo.levelDepth++;
        levelItemVo.levelNum = indentData.nextNum;
    }

    function _renumberOnOutdent(editor, listNode) {
        const data = _getData(listNode);

        const outdentData = editor.fire("levelOutdent", data);

        data.listItem.setAttribute("data-akn-num", outdentData.nextNum);
        data.listItem.setAttribute(LEOS_ORIGINAL_DEPTH_ATTR, originalDepth);
        levelItemVo.levelDepth--;
        levelItemVo.levelNum = outdentData.nextNum;
    }

    function _getData(listNode) {
        const listItem = _getListItem(listNode);
        const prevLevels = _getPrevLevels(listNode);
        const prevLvlNum = _getPrevLevelNum(listNode);
        const prevLvlDepth = _getLevelDepth(prevLvlNum);
        const currLvlNum = listItem.getAttribute("data-akn-num");
        const currLvlDepth = _getLevelDepth(currLvlNum);
        const nextLevels = _getNextLevels(listNode);
        const nextLevelNum = _getNextLevelNum(listNode);

        const data = {
            prevLevels: prevLevels,
            prevLvlDepth: prevLvlDepth,
            prevLvlNum: prevLvlNum,
            currLvlDepth: currLvlDepth,
            currLvlNum: currLvlNum,
            nextLevelNum: nextLevelNum,
            nextLevels: nextLevels,
            listItem: listItem,
            levelItemVo: levelItemVo
        };
        return data;
    }

    function _getDataOnNumberIndentOutdent(numNode) {
        const level = _getLevel(numNode);
        const prevLevels = _getPrevLevelsOnNumberIndentOutdent(numNode);
        const prevLvlNum = _getPrevLevelNumOnNumberIndentOutdent(numNode);
        const prevLvlDepth = _getLevelDepth(prevLvlNum);
        const currLvlNum = numNode.$.innerText;
        const currLvlDepth = _getLevelDepth(currLvlNum);
        const nextLevels = _getNextLevelsOnNumberIndentOutdent(numNode);
        const nextLevelNum = _getNextLevelNumOnNumberIndentOutdent(numNode);
        const originalECDepth = _getLevelOriginalECDepth(numNode);
        const originalECNum = _getLevelOriginalECNumber(numNode);

        let data = Object.assign({
            prevLevels: prevLevels,
            prevLvlDepth: prevLvlDepth,
            prevLvlNum: prevLvlNum,
            currLvlDepth: currLvlDepth,
            currLvlNum: currLvlNum,
            nextLevelNum: nextLevelNum,
            nextLevels: nextLevels,
            level: level,
            originalECDepth : originalECDepth === null ? null : originalECDepth,
            originalECNum: originalECNum === null ? null : originalECNum
        });
        return data;
    }

    function _getLevelOriginalECDepth(numNode){
        if(numNode.getAttribute(DATA_AKN_ORIGIN_DEPTH_ATTR)){
            return numNode.getAttribute(DATA_AKN_ORIGIN_DEPTH_ATTR);
        }
    }

    function _getLevelOriginalECNumber(numNode){
        if(numNode.getAttribute(DATA_AKN_ORIGIN_NUM_ATTR)){
            return numNode.getAttribute(DATA_AKN_ORIGIN_NUM_ATTR);
        }
    }

    function _getLevel(numNode){
        return numNode.getAscendant(LEVEL);
    }

    function _getNextLevelsOnNumberIndentOutdent(numNode) {
        if(numNode.getAscendant(LEVEL)) {
            return $(numNode.getAscendant(LEVEL).$).nextAll(LEVEL);
        }
    }

    function _getNextLevelNumOnNumberIndentOutdent(numNode) {
        const $nextLevels = _getNextLevelsOnNumberIndentOutdent(numNode);
        return _getLevelNum($nextLevels, true);
    }

    function _getPrevLevelsOnNumberIndentOutdent(numNode) {
        if(numNode.getAscendant(leosPluginUtils.MAINBODY) && numNode.getAscendant(LEVEL)) {
            let levels = Array.from(numNode.getAscendant(leosPluginUtils.MAINBODY).find(LEVEL).$);
            let indexOfLevel = levels.indexOf(numNode.getAscendant(LEVEL).$);
            if (indexOfLevel > -1) {
                return $(levels.splice(0, indexOfLevel));
            }
        }
    }

    function _getPrevLevelNumOnNumberIndentOutdent(numNode) {
        const $prevLevels = _getPrevLevelsOnNumberIndentOutdent(numNode);
        return _getLevelNum($prevLevels, false);
    }

    function _onNumberIndent(editor, listNode) {
        const data = _getDataOnNumberIndentOutdent(listNode);
        const indentData = editor.fire("levelIndent", data);
        setLevelNumData(listNode, indentData, 1);
    }

    function _onNumberOutdent(editor, listNode) {
        const data = _getDataOnNumberIndentOutdent(listNode);
        const outdentData = editor.fire("levelOutdent", data);
        setLevelNumData(listNode, outdentData, -1);
    }

    function setLevelNumData(listNode, data, command) {
        data.level.setAttribute(LEOS_ORIGINAL_DEPTH_ATTR, originalDepth);
        command > 0 ? levelItemVo.levelDepth++ : levelItemVo.levelDepth--;
        if (parseInt(data.originalECDepth) === levelItemVo.levelDepth) {
            listNode.setAttribute(DATA_NUM_ORIGIN, EC);
            levelItemVo.levelNum = data.originalECNum;
            listNode.setHtml(data.originalECNum);
        } else if (originalDepth === levelItemVo.levelDepth) {
            listNode.setAttribute(DATA_NUM_ORIGIN, originalOrigin);
            levelItemVo.levelNum = originalNum;
            listNode.setHtml(originalNum);
        } else {
            listNode.setAttribute(DATA_NUM_ORIGIN, CN);
            levelItemVo.levelNum = data.nextNum;
            listNode.setHtml(data.nextNum);
        }
    }

    function _isListIntroAndFirstSubelement(element) {
        if (element.type == CKEDITOR.NODE_TEXT) {
            let tmpElement = element;
            while (!!tmpElement && tmpElement.type !== CKEDITOR.NODE_ELEMENT) {
                tmpElement = tmpElement.getParent();
            }
            element = !!tmpElement ? tmpElement : element;
        }
        return (leosPluginUtils.isListIntro(element) && !element.getParent().$.previousSibling);
    }

    function _isFirstSubelement(element) {
        if (element.type == CKEDITOR.NODE_TEXT) {
            let tmpElement = element;
            while (!!tmpElement && tmpElement.type !== CKEDITOR.NODE_ELEMENT) {
                tmpElement = tmpElement.getParent();
            }
            element = !!tmpElement ? tmpElement : element;
        }
        return (leosPluginUtils.isSubparagraph(element) && element.getParent().getName().toLowerCase() != 'ol' && !element.$.previousSibling);
    }

    function _manageListIntro(element) {
        if (element.type == CKEDITOR.NODE_TEXT) {
            let tmpElement = element;
            while (!!tmpElement && tmpElement.type !== CKEDITOR.NODE_ELEMENT) {
                tmpElement = tmpElement.getParent();
            }
            element = !!tmpElement ? tmpElement : element;
        }
        if (_isListIntroAndFirstSubelement(element)) {
            return element.getParent().getParent();
        }
        if (_isFirstSubelement(element)) {
            return element.getParent();
        }
        return element;
    }

    function _manageSubparagraphs(range) {
        range.startContainer = _manageListIntro(range.startContainer);
        range.endContainer = _manageListIntro(range.endContainer);
        return range.startPath();
    }

    function aknindentList(editor) {
        var that = this, database = this.database, context = this.context, range;
        editor.fire("beforeAknIndentList");

        function indent(listNode) {
            // Our starting and ending points of the range might be inside some blocks under a list item...
            // So before playing with the iterator, we need to expand the block to include the list items.
            //Indent and outdent done from only number editor(number profile)
            if(_isLevelNumElement(listNode)){
                that.isIndent ? _onNumberIndent(editor, listNode) :  _onNumberOutdent(editor, listNode);
                editor.fire('change');
                return 0;
            }

            var isListElement = leosPluginUtils.isAnnexList(listNode);
            var isRangeStartInTextElement = isListElement && !!range.startContainer && range.startContainer.type === CKEDITOR.NODE_TEXT;
            var startContainer = isRangeStartInTextElement ? range.startContainer.getParent() : range.startContainer,
                endContainer = isRangeStartInTextElement ? range.endContainer.getParent() : range.endContainer;
            while (!isListElement && startContainer && !startContainer.getParent().equals(listNode))
                startContainer = startContainer.getParent();
            while (!isListElement && endContainer && !endContainer.getParent().equals(listNode))
                endContainer = endContainer.getParent();

            if (!startContainer || !endContainer
                || (that.isIndent && _isListDepthMoreThanThreshold(startContainer, endContainer, leosPluginUtils.MAX_LIST_LEVEL)) && !_isOnlyLevelElementSelected(startContainer, endContainer)){
                return false;
            }

            // Now we can iterate over the individual items on the same tree depth.
            var block = startContainer, itemsToMove = [], stopFlag = false;

            while (!stopFlag) {
                if(block){
                    if ( block.equals( endContainer ) )
                        stopFlag = true;

                    itemsToMove.push(block);
                    block = block.getNext();

                }else{
                    stopFlag = true;
                }
            }

            if ( itemsToMove.length < 1 )
                return false;

            // Do indent or outdent operations on the array model of the list, not the
            // list's DOM tree itself. The array model demands that it knows as much as
            // possible about the surrounding lists, we need to feed it the further
            // ancestor node that is still a list.
            var listParents = listNode.getParents(true);
            for (var i = 0; i < listParents.length; i++) {
                if (listParents[i].getName && context[listParents[i].getName()]) {
                    listNode = listParents[i];
                    break;
                }
            }

            var indentOffset = that.isIndent ? 1 : -1,

                startItem = itemsToMove[0], lastItem = itemsToMove[itemsToMove.length - 1], listArray;

            // Convert the list DOM tree into a one dimensional array.
            // Is this is a subparagraph, no need to go to the list of points' logic, just set it as a point
            if (leosPluginUtils.isSubparagraph(startItem)) {
                startItem.setAttribute(leosPluginUtils.DATA_AKN_ELEMENT, leosPluginUtils.POINT);
                startItem.renameNode('li');
                // Check if point has an ol as parent, if not add it
                if (!startItem.getParent().is('ol')) {
                    var doc = startItem.getParent().getDocument();
                    var newOl = doc.createElement('ol');
                    startItem.getParent().$.insertBefore(newOl.$, startItem.$);
                    newOl.append(startItem);
                }
                return true;
            } else {
                listArray = CKEDITOR.plugins.leosAnnexList.listToArray(listNode, database);
            }

            // Apply indenting or outdenting on the array.
            if (!listArray[lastItem.getCustomData('listarray_index')]) {
                return false;
            }
            var baseIndent = listArray[lastItem.getCustomData('listarray_index')].indent;

            for (i = startItem.getCustomData('listarray_index'); i <= lastItem.getCustomData('listarray_index'); i++) {
                listArray[i].indent += indentOffset;
                // Make sure the newly created sublist get a brand-new element of the same type. (http://dev.ckeditor.com/ticket/5372)
                if (indentOffset > 0) {

                    //LEOS: 4062 On indent the first level list just update the num and depth attribute
                    //values without changing the structure
                    var firstLevelList = isFirstLevelList(editor, listNode);
                    if(firstLevelList && isFirstItemInLevel(editor.elementPath(), listNode)) {
                        _renumberOnIndent(editor, listNode);

                        editor.fire('contentDomInvalidated');
                        return true;
                    }

                    var isLevelElement = leosPluginUtils.isAnnexList(listArray[i].element.getAscendant('ol',true));
                    var listRoot = isLevelElement ? listArray[i].grandparent : listArray[i].parent;

                    // Find previous list item which has the same indention offset as the new indention offset
                    // of current item to copy its root tag (so the proper list-style-type is used) (#842).
                    for ( var j = i - 1; j >= 0; j-- ) {
                        if ( listArray[ j ].indent === indentOffset ) {
                            listRoot = isLevelElement ? listArray[ j ].grandparent : listArray[ j ].parent;
                            break;
                        }
                    }
                    if(_isLevelElementContainsOrderedList(listArray[i].element)){
                        listArray[i+1].indent = listArray[i].indent;
                        listArray[i+1].indentThisElement = true;
                    }
                    if (isLevelElement) {
                        var newElement = new CKEDITOR.dom.element(listArray[i].parent.getName(), listArray[i].parent.getDocument());
                        newElement.setAttributes(listArray[i].element.getAttributes());
                        newElement.setAttribute('data-akn-mp-tag-name', 'p');
                        listArray[i].element = newElement;
                        listArray[i].indentThisElement = true;
                    }
                    listArray[i].parent = new CKEDITOR.dom.element(listRoot.getName(), listRoot.getDocument());
                }
            }

            for (i = lastItem.getCustomData('listarray_index') + 1; i < listArray.length && listArray[i].indent > baseIndent; i++)
                listArray[i].indent += indentOffset;

            // Convert the array back to a DOM forest (yes we might have a few subtrees now).
            // And replace the old list with the new forest.
            var newList = CKEDITOR.plugins.leosAnnexList.arrayToList(listArray, database, null, editor.config.enterMode, listNode.getDirection());

            // Avoid nested <li> after outdent even they're visually same,
            // recording them for later refactoring.(http://dev.ckeditor.com/ticket/3982)
            if (!that.isIndent) {

                //LEOS: 4062 On outdent the first level list just update the num and depth attribute
                //values without changing the structure

                var firstLevelList = isFirstLevelList(editor, listNode);
                if(firstLevelList) {
                    _renumberOnOutdent(editor, listNode);

                    editor.fire('contentDomInvalidated');
                    return true;
                }
                var parentLiElement;
                if ((parentLiElement = listNode.getParent()) && parentLiElement.is('li')) {
                    var children = newList.listNode.getChildren(), pendingLis = [], count = children.count(), child;

                    for (i = count - 1; i >= 0; i--) {
                        if ((child = children.getItem(i)) && child.is && child.is('li'))
                            pendingLis.push(child);

                        if((child = children.getItem(i)) && child.is && child.is('p')) {
                            var indentOriginNumId = startItem.getAttribute("data-indent-origin-num-id");
                            var indentOriginType = startItem.getAttribute("data-indent-origin-type");
                            var indentOriginNumber = startItem.getAttribute("data-indent-origin-num");
                            var indentOriginNumOrigin = startItem.getAttribute("data-indent-origin-num-origin");
                            child.setAttribute("data-indent-origin-num-id", indentOriginNumId);
                            child.setAttribute("data-indent-origin-type", indentOriginType);
                            child.setAttribute("data-indent-origin-num", indentOriginNumber);
                            child.setAttribute("data-indent-origin-num-origin", indentOriginNumOrigin);
                        }
                    }
                }
            }

            if (leosPluginUtils.isAnnexList(listNode) && _isElementSubparagraph(startItem) && startItem.is('p') && listNode.find('ol').count() > 0) {
                listNode = listNode.find('ol').getItem(0);
                startItem.remove();
            }
            if (leosPluginUtils.isAnnexList(listNode)) {
                newList.listNode.replace(listNode.getChildren().toArray().find(element => element.getName() === 'li'));
            } else {
                newList.listNode.replace(listNode);
            }

            // Move the nested <li> to be appeared after the parent.
            if (pendingLis && pendingLis.length) {
                for (i = 0; i < pendingLis.length; i++) {
                    var li = pendingLis[i], followingList = li;

                    if (!li.getParent().getAttribute(leosPluginUtils.DATA_AKN_ELEMENT) || li.getParent().getAttribute(leosPluginUtils.DATA_AKN_ELEMENT) !== leosPluginUtils.LEVEL) {
                        // Nest preceding <ul>/<ol> inside current <li> if any.
                        while ((followingList = followingList.getNext()) && followingList.is && followingList.getName() in context) {
                            // IE requires a filler NBSP for nested list inside empty list item,
                            // otherwise the list item will be inaccessiable. (http://dev.ckeditor.com/ticket/4476)
                            if (CKEDITOR.env.needsNbspFiller && !li.getFirst(neitherWhitespacesNorBookmark))
                                li.append(range.document.createText('\u00a0'));

                            li.append(followingList);
                        }

                        li.insertAfter(parentLiElement);
                    }
                }
            }

            if (newList)
                editor.fire('contentDomInvalidated');

            return true;
        }

        function indentParagraph() {
            var prevLevel = indentationStatus.current.level;
            // Do Indent
            if (that.isIndent && (indentationStatus.current.level < leosPluginUtils.MAX_LIST_LEVEL || !indentationStatus.current.numbered)) {
                if (indentationStatus.current.move < 0) {
                    indentationStatus.current.numbered = indentationStatus.current.prevNumbered.pop();

                } else {
                    indentationStatus.current.prevNumbered.push(indentationStatus.current.numbered);
                    indentationStatus.current.numbered = !indentationStatus.current.numbered;
                }

                indentationStatus.current.move++;
                if (!indentationStatus.current.numbered) {
                    indentationStatus.current.level++;
                }
                _calculateNewLevel(editor, true);
            }
            // Do Outdent
            else if (indentationStatus.current.level > 0 || indentationStatus.current.numbered) {
                if (indentationStatus.current.move > 0) {
                    indentationStatus.current.numbered = indentationStatus.current.prevNumbered.pop();

                } else {
                    indentationStatus.current.prevNumbered.push(indentationStatus.current.numbered);
                    indentationStatus.current.numbered = !indentationStatus.current.numbered;
                }

                indentationStatus.current.move--;
                if (indentationStatus.current.numbered) {
                    indentationStatus.current.level--;
                }
                _calculateNewLevel(editor, false);
            }

            var newNumValue = indentationStatus.original.num;
            if (indentationStatus.current.move != 0) {
                newNumValue = indentationStatus.current.numbered ? '#' : undefined;
                leosPluginUtils.doIndent(editor, indentationStatus);
                leosPluginUtils.setIndentAttributes(editor, indentationStatus);
            } else {
                indentationStatus.current.level = indentationStatus.original.level;
                leosPluginUtils.resetIndent(editor, that.isIndent);
                leosPluginUtils.resetIndentAttributes(editor, indentationStatus);
            }
            leosPluginUtils.setCurrentNumValue(newNumValue, editor, indentationStatus);

            return 0;
        }

        var selection = editor.getSelection(),
            ranges = selection && selection.getRanges(),
            iterator = ranges.createIterator();

        while ((range = iterator.getNextRange())) {
            range.endContainer = _manageListIntro(range.endContainer);
            range.startContainer = _manageListIntro(range.startContainer);

            var nearestListBlock = range.getCommonAncestor();

            if (_isPlaceHolderOfNumElement(nearestListBlock)) {
                nearestListBlock = _findNumElement(nearestListBlock);
            } else {
                while (nearestListBlock && !((nearestListBlock.type == CKEDITOR.NODE_ELEMENT && context[nearestListBlock.getName()])
                    || (nearestListBlock.type == CKEDITOR.NODE_ELEMENT && _isLevelNumElement(nearestListBlock)))) {
                    // Avoid having plugin propagate to parent of editor in inline mode by canceling the indentation. (http://dev.ckeditor.com/ticket/12796)
                    if (editor.editable().equals(nearestListBlock)) {
                        nearestListBlock = false;
                        break;
                    }
                    nearestListBlock = nearestListBlock.getParent();
                }
            }

            if (leosPluginUtils.isAnnexList(range.startContainer.getParent())
                && !!range.startContainer.getParent().getAttribute("data-akn-element")
                && range.startContainer.getParent().getAttribute("data-akn-element") !== leosPluginUtils.PARAGRAPH) {
                if (that.isIndent) {
                    _renumberOnIndent(editor, leosPluginUtils.isAnnexList(nearestListBlock) ? nearestListBlock : nearestListBlock.getParent().getParent());
                } else {
                    _renumberOnOutdent(editor, leosPluginUtils.isAnnexList(nearestListBlock) ? nearestListBlock : nearestListBlock.getParent().getParent());
                }
                editor.fire('contentDomInvalidated');
                return true;
            }

            // Avoid having selection boundaries out of the list.
            // <ul><li>[...</li></ul><p>...]</p> => <ul><li>[...]</li></ul><p>...</p>
            if (!nearestListBlock) {
                if ((nearestListBlock = range.startPath().contains(context)))
                    range.setEndAt(nearestListBlock, CKEDITOR.POSITION_BEFORE_END);
            }

            // Avoid having selection enclose the entire list. (http://dev.ckeditor.com/ticket/6138)
            // [<ul><li>...</li></ul>] =><ul><li>[...]</li></ul>
            if (!nearestListBlock) {
                var selectedNode = range.getEnclosedNode();
                if (selectedNode && selectedNode.type == CKEDITOR.NODE_ELEMENT && selectedNode.getName() in context) {
                    range.setStartAt(selectedNode, CKEDITOR.POSITION_AFTER_START);
                    range.setEndAt(selectedNode, CKEDITOR.POSITION_BEFORE_END);
                    nearestListBlock = selectedNode;
                }
            }

            // Avoid selection anchors under list root.
            // <ul>[<li>...</li>]</ul> => <ul><li>[...]</li></ul>
            if (nearestListBlock && range.startContainer.type == CKEDITOR.NODE_ELEMENT && range.startContainer.getName() in context) {
                var walker = new CKEDITOR.dom.walker(range);
                walker.evaluator = listItem;
                range.startContainer = walker.next();
            }

            if (nearestListBlock && range.endContainer.type == CKEDITOR.NODE_ELEMENT && range.endContainer.getName() in context) {
                walker = new CKEDITOR.dom.walker(range);
                walker.evaluator = listItem;
                range.endContainer = walker.previous();
            }

            var crossheading = _getCrossHeading(editor.elementPath());
            var list = this.getContext(editor.elementPath());
            var type = list ? list.getAttribute(DATA_AKN_NAME_ELEMENT) : null;
            if (!!crossheading && leosPluginUtils.isCrossHeading(crossheading)) {
                var indentLevel = _getCrossheadingIndentAttribute(crossheading) ? _getCrossheadingIndentAttribute(crossheading) : 0;
                if (that.isIndent) {
                    indentLevel++;
                    leosPluginUtils.setCrossheadingIndentAttribute(crossheading.$, indentLevel);
                } else {
                    if (indentLevel > 0) {
                        indentLevel--;
                        leosPluginUtils.setCrossheadingIndentAttribute(crossheading.$, indentLevel);
                    }
                }
                return true;
            } else if (leosPluginUtils.PARAGRAPH === type && _shouldUseCouncilIndentation(list, editor)) {
                return indentParagraph();
            } else if (nearestListBlock) {
                if (leosPluginUtils.isSubparagraph(range.startContainer) && !leosPluginUtils.isListIntro(range.startContainer)) {
                    range.startContainer.setAttribute(leosPluginUtils.DATA_AKN_ELEMENT, leosPluginUtils.POINT);
                    range.startContainer.renameNode('li');
                    // Check if point has an ol as parent, if not add it
                    if (!range.startContainer.getParent().is('ol')) {
                        var doc = range.startContainer.getParent().getDocument();
                        var newOl = doc.createElement('ol');
                        range.startContainer.getParent().$.insertBefore(newOl.$, range.startContainer.$);
                        newOl.append(range.startContainer);
                    }
                    result = true;
                } else {
                    var result = indent(nearestListBlock);
                }
                leosPluginUtils.manageEmptyLists(editor);
                leosPluginUtils.managePoints(editor);
                leosPluginUtils.manageEmptySubparagraphs(editor);
                leosPluginUtils.manageCrossheadings(editor);
                leosPluginUtils.manageSiblingLists(editor);
                return result;
            }
        }
        return 0;
    }

    function _calculateNewLevel(editor, isIndent) {
        var source = $(editor.element.$);
        if (!isIndent && (leosPluginUtils.isUnumberedHtmlParagraph(editor, indentationStatus)
            || (source.parents(PARAGRAPH).length
                && leosPluginUtils.isUnumberedparagraph(source.parents(PARAGRAPH))
                && indentationStatus.current.level == 1
                && !indentationStatus.current.numbered))) {
            indentationStatus.current.numbered = true;
        }
        if (isIndent && (leosPluginUtils.isUnumberedHtmlParagraph(editor, indentationStatus)
            || (source.parents(PARAGRAPH).length
                && leosPluginUtils.isUnumberedparagraph(source.parents(PARAGRAPH))
                && indentationStatus.current.level == 1
                && !indentationStatus.current.numbered
                && indentationStatus.original.numbered))) {
            indentationStatus.current.numbered = true;
        }
    }

    function _isSubparagraphInsideAnnexParagraph(range) {
        var element = range.startContainer.getAscendant('p', true);
        if (element) {
            var parentElement = element.getAscendant('ol');
            if (leosPluginUtils.isAnnexList(parentElement)
                && PARAGRAPH === parentElement.getAttribute(DATA_AKN_NAME_ELEMENT)) {
                return true;
            }
        }
        return false;
    }

    function _isSubparagraphInsideAnnexLevel(range, path, list) {
        var element = range.startContainer.getAscendant('p', true);
        if (!list || isFirstItemInLevel(path, list)) {
            return false;
        } else if (element) {
            var parentElement = element.getAscendant('ol');
            if (leosPluginUtils.isAnnexList(parentElement)
                && (LEVEL === parentElement.getAttribute(DATA_AKN_NAME_ELEMENT) || PARAGRAPH === parentElement.getAttribute(DATA_AKN_NAME_ELEMENT))
                && SUBPARAGRAPH === element.getAttribute(DATA_AKN_NAME_ELEMENT)) {
                return true;
            }
        }
        return false;
    }

    function _isSelectionInFirstLevelListInsideAnnexParagraph(selected) {
        if (!selected || !(selected = selected.getAscendant(leosPluginUtils.HTML_POINT, true))) {
            return false;
        }
        let listDepth = 0;
        let olElement = selected.getAscendant(leosPluginUtils.ORDER_LIST_ELEMENT);
        while (!leosPluginUtils.isAnnexList(olElement)) {
            if (olElement) {
                listDepth = listDepth + 1;
                olElement = olElement.getAscendant(leosPluginUtils.ORDER_LIST_ELEMENT);
            } else {
                break;
            }
        }
        return listDepth === 1 && leosPluginUtils.isAnnexList(olElement)
            && ('paragraph' === olElement.getAttribute(DATA_AKN_NAME_ELEMENT));
    }

    function _isHeadingSelected(range) {
        var startContainer = range.startContainer;
        var endContainer = range.endContainer;
        var startAtHeading = range.startContainer.getAscendant('h2', true);
        var endAtHeading = range.endContainer.getAscendant('h2', true);

        if (leosPluginUtils.isSelectionInFirstLevelList(startContainer) || leosPluginUtils.isSelectionInFirstLevelList(endContainer)) {
            return (startContainer && endContainer && _isCrossListSelectionIncludesFirstLevelList(startContainer, endContainer))
                || startAtHeading || endAtHeading;
        }
        return false;
    }

    function _isCrossListSelectionIncludesFirstLevelList(startItem, endItem) {
        return !startItem.getAscendant('ol').equals(endItem.getAscendant('ol'))
    }

    function _isFirstLevelListAtLastIndentPosition(list) {
        var listNum = _getListNumber(list);
        var lastNum = listNum ? listNum.substring(listNum.lastIndexOf(".") - 1, listNum.lastIndexOf(".")) : "";
        return lastNum === "1" ;
    }

    function _isElementSubparagraph(currentElement) {
        return (!!currentElement && (currentElement.is('p') || currentElement.is('li'))
            && !!currentElement.getAttribute(leosPluginUtils.DATA_AKN_ELEMENT)
            && currentElement.getAttribute(leosPluginUtils.DATA_AKN_ELEMENT) == leosPluginUtils.SUBPARAGRAPH);
        return false;
    }

    function _isSubparagraph(path) {
        if (!!path) {
            var currentElement = path.lastElement;
            return (!!currentElement && (currentElement.is('p') || currentElement.is('li'))
                && !!currentElement.getAttribute(leosPluginUtils.DATA_AKN_ELEMENT)
                && currentElement.getAttribute(leosPluginUtils.DATA_AKN_ELEMENT) == leosPluginUtils.SUBPARAGRAPH);
        }
        return false;
    }

    function _isPoint(element) {
        var isLi = leosPluginUtils.getElementName(element) === leosPluginUtils.HTML_POINT;
        var crossheadingAttr = element.getAttribute(leosPluginUtils.CROSSHEADING_LIST_ATTR);
        var dataAknElementAttr = element.getAttribute(leosPluginUtils.DATA_AKN_ELEMENT);

        if (!!element && isLi && (dataAknElementAttr == null || dataAknElementAttr.toLowerCase() != leosPluginUtils.SUBPARAGRAPH.toLowerCase()) && (crossheadingAttr == null || crossheadingAttr != leosPluginUtils.LIST)) {
            return true;
        } else {
            return false;
        }
    }

    function _isNotFirstLevelElement (path){
        return path && path.lastElement && path.lastElement.getAttribute('data-akn-mp-id') && !!path.lastElement.getParent().getFirst()
            && path.lastElement.getParent().getFirst().type !== CKEDITOR.NODE_TEXT
            && path.lastElement.getAttribute('data-akn-mp-id') !== path.lastElement.getParent().getFirst().getAttribute('data-akn-mp-id');
    }

    function _isLevelListDepthMoreThanThreshold(startSelection, endSelection, maxLevel) {
        var isStartSelectionDeeperThanThreshold = isSingleLevelElementDeeperThanThreshold(startSelection, maxLevel);
        var isEndSelectionDeeperThanThreshold = isSingleLevelElementDeeperThanThreshold(endSelection, maxLevel);
        return isStartSelectionDeeperThanThreshold || isEndSelectionDeeperThanThreshold;
    }

    function isSingleLevelElementDeeperThanThreshold(selected, maxLevel){
        if(!selected){
            return false;
        }
        return (maxLevel > 0) ? _isLevelDownsideDepthMoreThanThreshold(selected, maxLevel) : true;
    }

    function _isLevelDepthDiffGreaterThanOne(list) {
        if(leosPluginUtils.isAnnexList(list)) {
            const prevLevelNum = _getPrevLevelNum(list);
            const prevLevelDepth = _getLevelDepth(prevLevelNum);
            const currLevelDepth = _getLevelDepth(_getListNumber(list));
            return (currLevelDepth - prevLevelDepth) >= 1;
        }
        return false;
    }

    /**
     * Returns true if the depth of the whole tree (ol structure) is more than the threshold maxLevel.
     * The total tree depth is given by the level upside + level downside of the selected element.
     */
    function _isListDepthMoreThanThreshold(startSelection, endSelection, maxLevel) {
        var isStartSelectionDeeperThanThreshold = isSingleElementDeeperThanThreshold(startSelection, maxLevel);
        var isEndSelectionDeeperThanThreshold = isSingleElementDeeperThanThreshold(endSelection, maxLevel);
        LOG.debug("Depth calculation. isStartSelectionDeeperThanThreshold:" + isStartSelectionDeeperThanThreshold + ", isEndSelectionDeeperThanThreshold:" + isEndSelectionDeeperThanThreshold);
        return isStartSelectionDeeperThanThreshold || isEndSelectionDeeperThanThreshold;
    }

    function _isOnlyLevelElementSelected(startSelection, endSelection) {
        return ((startSelection && (startSelection.type === CKEDITOR.NODE_ELEMENT || startSelection.type === CKEDITOR.NODE_TEXT) && startSelection.getAscendant(leosPluginUtils.ORDER_LIST_ELEMENT,true) &&
                leosPluginUtils.isAnnexList(startSelection.getAscendant(leosPluginUtils.ORDER_LIST_ELEMENT,true))) &&
            (endSelection && (endSelection.type === CKEDITOR.NODE_ELEMENT || endSelection.type === CKEDITOR.NODE_TEXT) && endSelection.getAscendant(leosPluginUtils.ORDER_LIST_ELEMENT,true) &&
                leosPluginUtils.isAnnexList(endSelection.getAscendant(leosPluginUtils.ORDER_LIST_ELEMENT,true))));
    }

    function _isLevelDepthMoreThanThreshold(levelItemVo, depth){
        if(levelItemVo && (depth < leosPluginUtils.MAX_LEVEL_DEPTH)) {
            var changeInDepth = levelItemVo.levelDepth - originalDepth; //Need to keep account of outdent/indent of Level
            return _isDownSideLevelDepthMoreThanThreshold(levelItemVo, changeInDepth);
        }
        return true;
    }

    function _isMaxDepthLevelAchieved(levelItemVo, numElement) {
        let depth = _getLevelDepth(numElement);
        if(depth >= leosPluginUtils.MAX_LEVEL_DEPTH) {
            return true;
        }
        return false;
    }

    function isSingleElementDeeperThanThreshold(selected, maxLevel) {
        if(!selected) {
            return false;
        }
        selected = selected.getAscendant(leosPluginUtils.HTML_POINT, true);//get first li it founds in hierarchy considering even itself
        var olElement = selected.getAscendant(leosPluginUtils.ORDER_LIST_ELEMENT);

        if(leosPluginUtils.isAnnexList(olElement)) {
            var listNum = _getListNumber(olElement);
            var depth = _getLevelDepth(listNum);
            return _isLevelDepthMoreThanThreshold(levelItemVo, depth);

        } else {
            var actualLevel = leosPluginUtils.calculateListLevel(selected);
            var stopLevelDownside = maxLevel - actualLevel;
            if (stopLevelDownside > 0) {
                // if maxlevel is 4, and we selected an element in the second level, checks if any of the child has depth more than 2.
                return _isDownsideDepthMoreThanThreshold(selected, stopLevelDownside);
            } else {
                return true;
            }
        }
        return true;
    }

    function _isDownSideLevelDepthMoreThanThreshold(item, changeInDepth) {
        var depth = item.levelDepth;
        if(depth >= (leosPluginUtils.MAX_LEVEL_DEPTH - changeInDepth)) {
            return true;
        } else {
            var children = item.children;
            for(var idx = 0; idx < children.length; idx++) {
                var childItem = children[idx];
                if(_isDownSideLevelDepthMoreThanThreshold(childItem, changeInDepth)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if the depth of any of child of the selected element reach stopLevel.
     * Html Structure:
     * <article>
     *     <ol>
     *        <li> ... </li>
     *        <li>
     *            <p/>                     (1, 2, 3 items)  (cursor here, actual depth level 1)
     *            <ol>
     *               <li> ... </li>
     *               <li>
     *                    <p/>             (a, b, c items)  (cursor here, actual depth level 2)
     *                    <ol>
     *                       <li> ... </li>
     *                       <li>
     *                           <p/>      (i, ii items)    (cursor here, actual depth level 3)
     *                           <ol>
     *                               <li/> (- items)        (cursor here, actual depth level 4)
     *                               <li/>
     *                          </ol>
     *                       </li>
     *                    </ol>
     *               </li>
     *            <ol>
     *        </li>
     *     </ol>
     * <article>
     */
    function _isDownsideDepthMoreThanThreshold(element, stopLevel) {
        var level = 0;
        var childList = element.getChildren();
        for (var child_idx = 0; child_idx < childList.count(); child_idx++) {
            var child = childList.getItem(child_idx);
            var child_name = leosPluginUtils.getElementName(child);
            // only if we find an order_list_element (ol) it means we found another depth level
            if (child_name === leosPluginUtils.ORDER_LIST_ELEMENT) {
                level++;
                //LOG.debug(child_idx+"-th child found: " + child_name + ", calculated level: " + level + ", stopLevel: " + stopLevel);
                if (level >= stopLevel) {
                    return true;
                } else {
                    // go deeper in the list, check the children (li elements)
                    for (var li_item_idx = 0; li_item_idx < child.getChildren().count(); li_item_idx++) {
                        var listItem = child.getChildren().getItem(li_item_idx);
                        if (_isDownsideDepthMoreThanThreshold(listItem, (stopLevel - level))) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    function _isLevelDownsideDepthMoreThanThreshold(element, stopLevel) {
        var level = 0;
        var childList =  _isLevelElementContainsOrderedList(element) ? new Array(element.getNext()) : [];
        for (var i = 0; i < childList.length; i++) {
            var child = childList[i];
            var child_name = leosPluginUtils.getElementName(child);
            // only if we find an order_list_element (ol) it means we found another depth level
            if (child_name === leosPluginUtils.ORDER_LIST_ELEMENT) {
                level++;
                if (level >= stopLevel) {
                    return true;
                } else {
                    // go deeper in the list, check the children (li elements)
                    for (var li_item_idx = 0; li_item_idx < child.getChildren().count(); li_item_idx++) {
                        var listItem = child.getChildren().getItem(li_item_idx);
                        if (_isDownsideDepthMoreThanThreshold(listItem, (stopLevel - level))) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    function _isLevelElementContainsOrderedList(element){
        return element && leosPluginUtils.isAnnexList(element.getAscendant(leosPluginUtils.ORDER_LIST_ELEMENT), true) && element.getNext()
            && element.getNext().type === CKEDITOR.NODE_ELEMENT && element.getNext().getName() === leosPluginUtils.ORDER_LIST_ELEMENT;
    }

    function _isLevelNumElement(element){
        return element.getAttribute('data-akn-name') && element.getAttribute('data-akn-name') === 'aknLevelNum';
    }

    // Determines whether a node is a list <li> element.
    function listItem(node) {
        return node.type == CKEDITOR.NODE_ELEMENT && (node.is('li') || node.is('p')) && (!node.getAttribute(leosPluginUtils.DATA_AKN_ELEMENT) || node.getAttribute(leosPluginUtils.DATA_AKN_ELEMENT) != leosPluginUtils.SUBPARAGRAPH);
    }

    function neitherWhitespacesNorBookmark(node) {
        return isNotWhitespaces(node) && isNotBookmark(node);
    }

    /**
     * Checks whether the first child of the list is in the path.ORDER_LIST_ELEMENT
     * The list can be _renumberOnIndent from the path or given explicitly
     * e.g. for better performance if cached.
     *
     * @since 4.4.6
     * @param {Object} query See the {@link CKEDITOR.dom.elementPath#contains} method arguments.
     * @param {CKEDITOR.dom.elementPath} path
     * @param {CKEDITOR.dom.element} [list]
     * @returns {Boolean}
     * @member CKEDITOR.plugins.indentList
     */
    function firstItemInPath(query, path, list) {
        var firstListItemInPath = path.contains(listItem);
        if (!list)
            list = path.contains(query);

        return list && firstListItemInPath && firstListItemInPath.getParent().equals(list) && firstListItemInPath.equals(list.getFirst(listItem));
    }

    /**
     * returns true when:
     *
     * 1) Child of <li> is a Text node:
     * <li>
     *      Text
     * </li>
     *
     * 2) Child is a single <p> inside <li>
     * <li>
     *     <p>Text</p>
     * </li>
     **/
    function isFirstItemInLevel(path, list) {
        var firstItemInPath = path.contains(listItem);
        // case 1)
        if (firstItemInPath
            && firstItemInPath.type === CKEDITOR.NODE_TEXT
            && firstItemInPath.getParent().type === CKEDITOR.NODE_ELEMENT
            && firstItemInPath.getParent().getName() === 'li') {
            return true;
        }
        // case 2)
        if (firstItemInPath && firstItemInPath.type === CKEDITOR.NODE_ELEMENT) {
            if (firstItemInPath.getName() === 'p') {
                var isElementInList = $(list.$).find(firstItemInPath).length > 0;
                var listElement = list && list.getFirst(listItem);
                var firstElement = listElement && listElement.getFirst(listItem);
                if (isElementInList && firstItemInPath.equals(firstElement)) {
                    return true;
                }
            } else if (firstItemInPath.getName() === 'li' && leosPluginUtils.isAnnexList(firstItemInPath.getAscendant('ol'))) {
                return firstItemInPath.find('p') && firstItemInPath.find('p').$.length <= 1;
            }
        }
        return false;
    }

    /**
     * Returns true if the current list item is at the first level.
     */
    var isFirstLevelList = function isFirstLevelList(editor, list) {
        var firstRange = getSelectedRange(editor);
        return isFirstLevelLiSelected(firstRange);
    };

    var getSelectedRange = function getSelectedRange(editor) {
        var selection = editor.getSelection(), ranges = selection && selection.getRanges();
        var firstRange;
        if (ranges && ranges.length > 0) {
            firstRange = ranges[0];
        }
        return firstRange;
    };

    var getEnclosedLiElement = function getEnclosedLiElement(element) {
        return element.getAscendant('li', true);
    };

    var getEnclosedLevelElement = function getEnclosedLevelElement(element){
        return element.getAscendant('p', true);
    }

    var isFirstLevelLiSelected = function isFirstLevelLiSelected(range) {
        if (range && !range.collapsed) {
            return findWithinRange(range, function(node) {
                //Check if the node is the first level list item.
                return node && node.getAscendant && !node.getAscendant('li');
            });
        } else {
            var liElement = getEnclosedLiElement(range.endContainer);
            return liElement && !liElement.getAscendant('li')
        }
    };

    var findWithinRange = function findWithinRange(range, isFirstLevelLi) {
        var walker = new CKEDITOR.dom.walker(range);
        var node = range.getTouchedStartNode();
        while (node) {
            node = getEnclosedLiElement(node);
            if (isFirstLevelLi(node)) {
                //If the node is the first level li item, return
                return true;
            }
            node = walker.next();
        }
        return false;
    };

    function _getCrossHeading(path) {
        return _getCrossHeadingAttrInElement(path, 'li, p');
    }

    function _getCrossHeadingAttrInElement(path, selector) {
        var element = path.lastElement.$.closest(selector);
        return element ? new CKEDITOR.dom.element(element) : null;
    }

    function _getCrossheadingIndentAttribute(element) {
        return element.$.style.getPropertyValue(leosPluginUtils.INDENT_LEVEL_ATTR);
    }

    function _isFirstPointOrIndentInList(startSelection, endSelection) {
        return _isFirstSinglePointOrIndentInList(startSelection) || _isFirstSinglePointOrIndentInList(endSelection);
    }

    function _isFirstSinglePointOrIndentInList(element) {
        if (!element || !element.$) {
            return false;
        }
        var previous = element.$.previousElementSibling;
        while (!!previous && !_isPoint(previous)) {
            previous = previous.previousElementSibling;
        }
        return !previous && _isPoint(element);
    }

    function _shouldUseCouncilIndentation(list, editor) {
        if (!!list && editor.LEOS.instanceType == leosPluginUtils.COUNCIL_INSTANCE) {
            var paragraph = list.$.querySelector('li[' + leosPluginUtils.DATA_AKN_ELEMENT + '=' + leosPluginUtils.PARAGRAPH + ']');
            if (!!paragraph) {
                var dataOrigin = paragraph.getAttribute(leosPluginUtils.DATA_ORIGIN);
                return !!dataOrigin && dataOrigin == leosPluginUtils.EC;
            }
        }
        return false;
    }

    pluginTools.addPlugin(pluginName, pluginDefinition);

    // return plugin module
    var pluginModule = {
        name: pluginName
    };

    return pluginModule;
});
