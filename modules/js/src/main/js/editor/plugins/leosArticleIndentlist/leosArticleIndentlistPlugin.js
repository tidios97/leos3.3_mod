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
/**
 * @fileOverview Handles the indentation of lists. This is a customized plugin similar to CKEditor_4.9.2 indentList plugin. The customization is done to disable the
 * 'outdent' toolbar button for the first level list items so that it will not be possible to convert it into '<p>'.
 * Here are the main customizations:
 *     - Changed the pluginName from "indentlist" to "leosArticleIndentlist"
 *     - Changed the commands name from indentlist, outdentlist to aknindentlist, aknoutdentlist
 *     - Added function "isFirstLevelList" to check if cursor is on the first level of the list
 *     - Added function "_isListDepthMoreThanThreshold" to block the indent in case the list has reached the max level
 *     - Added function "_handleListOutdent" to handle the outdent of list items to the top level article issue reported in LEOS-5980
 */

; // jshint ignore:line
define(function leosArticleIndentListPluginModule(require) {
    "use strict";

    // load module dependencies
    var CKEDITOR = require("promise!ckEditor");
    var LOG = require("logger");
    var pluginTools = require("plugins/pluginTools");
    var leosPluginUtils = require("plugins/leosPluginUtils");

    var pluginName = "leosArticleIndentlist";
    var leosCommandStateHandler = require("plugins/leosCommandStateHandler/leosCommandStateHandler");

    var isNotWhitespaces = CKEDITOR.dom.walker.whitespaces( true ),
        isNotBookmark = CKEDITOR.dom.walker.bookmark( false, true ),
        TRISTATE_DISABLED = CKEDITOR.TRISTATE_DISABLED,
        TRISTATE_OFF = CKEDITOR.TRISTATE_OFF;

    var pluginDefinition = {
        requires: 'indent',
        init: function init(editor) {
            var globalHelpers = CKEDITOR.plugins.indent;

            // Register commands.
            globalHelpers.registerCommands( editor, {
                aknindentlist: new commandDefinition( editor, 'aknindentlist', true ),
                aknoutdentlist: new commandDefinition( editor, 'aknoutdentlist' )
            } );

            function commandDefinition(editor) {
                globalHelpers.specificDefinition.apply( this, arguments );

                // Require ul OR ol list.
                this.requiredContent = ['ul', 'ol'];

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
                            var list = this.getContext( path );
                            var range = getSelectedRange(editor);
                            if (!list
                                || firstItemInPath( this.context, path, list )
                                || _isListDepthMoreThanThreshold(getEnclosedLiElement(range.startContainer), getEnclosedLiElement(range.endContainer), leosPluginUtils.MAX_LIST_LEVEL) ) {
                                return TRISTATE_DISABLED;
                            } else {
                                return TRISTATE_OFF;
                            }
                        } : function(editor, path) {
                            var list = this.getContext(path);
                            // custom code to disable the outdent toolbar button for first level list items.
                            if (!list || isFirstLevelList(editor, list)) {
                                return TRISTATE_DISABLED;
                            } else {
                                return TRISTATE_OFF;
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

            editor.on('selectionChange', _onSelectionChange, null, null, 11);
        }
    };

    function aknindentList(editor) {
        var that = this, database = this.database, context = this.context, range;
        editor.fire("beforeAknIndentList");

        function indent(listNode) {
            // Our starting and ending points of the range might be inside some blocks under a list item...
            // So before playing with the iterator, we need to expand the block to include the list items.
            var startContainer = range.startContainer, endContainer = range.endContainer;
            while (startContainer && !startContainer.getParent().equals(listNode))
                startContainer = startContainer.getParent();
            while (endContainer && !endContainer.getParent().equals(listNode))
                endContainer = endContainer.getParent();

            if (!startContainer || !endContainer
                || (that.isIndent && _isListDepthMoreThanThreshold(startContainer, endContainer, leosPluginUtils.MAX_LIST_LEVEL))){
                return false;
            }

            // Now we can iterate over the individual items on the same tree depth.
            var block = startContainer, itemsToMove = [], stopFlag = false;

            while (!stopFlag) {
                if ( block.equals( endContainer ) )
                    stopFlag = true;

                itemsToMove.push(block);
                block = block.getNext();
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

            var indentOffset = that.isIndent ? 1 : -1, startItem = itemsToMove[0], lastItem = itemsToMove[itemsToMove.length - 1],

            // Convert the list DOM tree into a one dimensional array.
            listArray = CKEDITOR.plugins.leosArticleList.listToArray(listNode, database),

            // Apply indenting or outdenting on the array.
            baseIndent = listArray[lastItem.getCustomData('listarray_index')].indent;

            for (i = startItem.getCustomData('listarray_index'); i <= lastItem.getCustomData('listarray_index'); i++) {
                listArray[i].indent += indentOffset;
                // Make sure the newly created sublist get a brand-new element of the same type. (http://dev.ckeditor.com/ticket/5372)
                if (indentOffset > 0) {
                    var listRoot = listArray[i].parent;

					// Find previous list item which has the same indention offset as the new indention offset
					// of current item to copy its root tag (so the proper list-style-type is used) (#842).
					for ( var j = i - 1; j >= 0; j-- ) {
						if ( listArray[ j ].indent === indentOffset ) {
							listRoot = listArray[ j ].parent;
							break;
						}
					}
                    listArray[i].parent = new CKEDITOR.dom.element(listRoot.getName(), listRoot.getDocument());
                }
            }

            for (i = lastItem.getCustomData('listarray_index') + 1; i < listArray.length && listArray[i].indent > baseIndent; i++)
                listArray[i].indent += indentOffset;

            // Convert the array back to a DOM forest (yes we might have a few subtrees now).
            // And replace the old list with the new forest.
            var newList = CKEDITOR.plugins.leosArticleList.arrayToList(listArray, database, null, editor.config.enterMode, listNode.getDirection());

            // Avoid nested <li> after outdent even they're visually same,
            // recording them for later refactoring.(http://dev.ckeditor.com/ticket/3982)
            if (!that.isIndent) {
                var parentLiElement;
                if ((parentLiElement = listNode.getParent()) && parentLiElement.is('li')) {
                    var children = newList.listNode.getChildren(), pendingLis = [], count = children.count(), child;

                    for (i = count - 1; i >= 0; i--) {
                        if ((child = children.getItem(i)) && child.is && child.is('li')) {
                            pendingLis = _handleListOutdent(child, range.document, parentLiElement, pendingLis);
                        }
                    }
                }
            }

            if (newList)
                newList.listNode.replace(listNode);

            // Move the nested <li> to be appeared after the parent.
            if (pendingLis && pendingLis.length) {
                for (i = 0; i < pendingLis.length; i++) {
                    var li = pendingLis[i], followingList = li;

                    // Nest preceding <ul>/<ol> inside current <li> if any.
                    while ((followingList = followingList.getNext()) && followingList.is && followingList.getName() in context) {
                        // IE requires a filler NBSP for nested list inside empty list item,
                        // otherwise the list item will be inaccessiable. (http://dev.ckeditor.com/ticket/4476)
                        if (CKEDITOR.env.needsNbspFiller && !li.getFirst(neitherWhitespacesNorBookmark))
                            li.append(range.document.createText('\u00a0'));

                        li.append(followingList);
                    }

                    // Correction on outdent to avoid subparagraphs or alinea being moved before indented li
                    if ($(li.$).next("p").length>0) {
                        li.$.innerHTML = "<p>" + li.$.innerHTML + "</p>";
                        leosPluginUtils.copyContentAndMpAttributeToElement(li, li.getFirst());
                        var next;
                        while (next = li.getNext()) {
                            if (!!next.is && next.is('p')) {
                                li.append(next);
                            }
                        }

                    }

                    li.insertAfter(parentLiElement);
                }
            }

            if (newList)
                editor.fire('contentDomInvalidated');

            return true;
        }

        var selection = editor.getSelection(),
            ranges = selection && selection.getRanges(),
            iterator = ranges.createIterator();

        while ((range = iterator.getNextRange())) {
            var nearestListBlock = range.getCommonAncestor();

            while ( nearestListBlock && !( nearestListBlock.type == CKEDITOR.NODE_ELEMENT && context[ nearestListBlock.getName() ] ) ) {
                // Avoid having plugin propagate to parent of editor in inline mode by canceling the indentation. (http://dev.ckeditor.com/ticket/12796)
                if ( editor.editable().equals( nearestListBlock ) ) {
                    nearestListBlock = false;
                    break;
                }
                nearestListBlock = nearestListBlock.getParent();
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

            if (nearestListBlock)
                return indent(nearestListBlock);
        }
        return 0;
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

    function isSingleElementDeeperThanThreshold(selected, maxLevel){
        if(!selected){
            return false;
        }
        selected = selected.getAscendant(leosPluginUtils.HTML_POINT, true);//get first li it founds in hierarchy considering even itself
        var actualLevel = leosPluginUtils.calculateListLevel(selected);
        var stopLevelDownside = maxLevel - actualLevel;
        //LOG.debug("Depth maxLevel:" + maxLevel + ", actualLevel:" + actualLevel + ", stopLevelDownside: " + stopLevelDownside);
        if (stopLevelDownside > 0) {
            // if maxlevel is 4, and we selected an element in the second level, checks if any of the child has depth more than 2.
            return _isDownsideDepthMoreThanThreshold(selected, stopLevelDownside);
        } else {
            return true;
        }
    }

    function _handleListOutdent(child, document, parentLiElement, pendingLis) {
        // Check if list has more than one child item and is outdenting to top level, but only as an un-numbered paragraph - LEOS-5980, LEOS-6109
        var newLiArray = [];
        if(child.getChildCount() > 1 && parentLiElement.getParent().getParent().is(leosPluginUtils.ARTICLE) && (parentLiElement.$.attributes.getNamedItem(leosPluginUtils.DATA_AKN_NUM) === null
            || parentLiElement.$.attributes.getNamedItem(leosPluginUtils.DATA_AKN_NUM) === undefined
            || (!!parentLiElement.getAttribute(leosPluginUtils.DATA_AKN_NUM_ID) && parentLiElement.getAttribute(leosPluginUtils.DATA_AKN_NUM_ID).includes("deleted_")))) {
            newLiArray = [child];
            // Move list children into new lists with one item per list
            //  to avoid multi-subparagraphs inside paragraphs when outdenting
            if ($(child.$).children("p[" + leosPluginUtils.DATA_AKN_ELEMENT + "=" + leosPluginUtils.ALINEA + "]").length > 0
                || $(child.$).children("p[" + leosPluginUtils.DATA_AKN_ELEMENT + "=" + leosPluginUtils.SUBPARAGRAPH + "]").length > 0) {
                for (var k = 1; k < child.getChildCount(); k++) {
                    var innerItem = child.getChild(k);
                    if (innerItem.getText() === null || innerItem.getText() === undefined || innerItem.getText().length === 0
                        || !innerItem.is) {
                        continue;
                    }
                    if (innerItem.is('p') && innerItem.hasAttribute(leosPluginUtils.DATA_AKN_ELEMENT)
                        && (innerItem.getAttribute(leosPluginUtils.DATA_AKN_ELEMENT) == leosPluginUtils.ALINEA
                            || innerItem.getAttribute(leosPluginUtils.DATA_AKN_ELEMENT) == leosPluginUtils.SUBPARAGRAPH)) {
                        innerItem.renameNode('li');
                        innerItem.setAttribute(leosPluginUtils.DATA_AKN_ELEMENT, leosPluginUtils.PARAGRAPH);
                        innerItem.$.innerHTML = "<p>" + innerItem.$.innerHTML + "</p>";
                        innerItem.remove();
                        k--;
                        innerItem.insertAfter(newLiArray[newLiArray.length - 1]);
                        leosPluginUtils.copyContentAndMpAttributeToElement(innerItem, innerItem.getFirst());
                        newLiArray.push(innerItem);
                    }
                }
            }
            for (var j = newLiArray.length-1; j >= 0 ; j-- ) {
                pendingLis.push( newLiArray[j] );
            }
        } else {
            pendingLis.push(child);
        }
        return pendingLis;
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

    // Determines whether a node is a list <li> element.
    function listItem(node) {
        return node.type == CKEDITOR.NODE_ELEMENT && node.is('li');
    }

    function neitherWhitespacesNorBookmark(node) {
        return isNotWhitespaces(node) && isNotBookmark(node);
    }

    /**
     * Checks whether the first child of the list is in the path.
     * The list can be extracted from the path or given explicitly
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

        return list && firstListItemInPath && firstListItemInPath.equals(list.getFirst(listItem));
    }

    /**
     * Outdent:  Returns true if the current list item is at the first level.
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

    pluginTools.addPlugin(pluginName, pluginDefinition);

    function _onSelectionChange(event) {
        leosCommandStateHandler.changeCommandState(event, "outdent");
        leosCommandStateHandler.changeCommandState(event, "indent");
    }

    // return plugin module
    var pluginModule = {
        name: pluginName
    };

    return pluginModule;
});
