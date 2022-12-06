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
define(function leosIndentMandatePluginModule(require) {
    "use strict";

    $.fn.appendToWithIndex = function (to,index) {
        if (! to instanceof jQuery) {
            to=$(to);
        };
        if (index===0) {
            $(this).prependTo(to)
        } else {
            $(this).insertAfter(to.children().eq(index-1));
        }
    };

    // load module dependencies
    let CKEDITOR = require("promise!ckEditor");
    let pluginTools = require("plugins/pluginTools");
    let leosPluginUtils = require("plugins/leosPluginUtils");

    const NUMBERED_ITEM = "point, indent, paragraph";
    const UNUMBERED_ITEM = "alinea, subparagraph";
    const PARAGRAPH = "paragraph";
    const LEVEL = "level";
    const ITEMS_SELECTOR = leosPluginUtils.LIST + "," + NUMBERED_ITEM + "," + UNUMBERED_ITEM;

    let modeRealTimeIndent = true;

    let indentationStatus = {
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

    function resetIndentStatus() {
        indentationStatus.original.num = undefined;
        indentationStatus.original.level = -1;
        indentationStatus.original.parent = '';
        indentationStatus.original.position = -1;
        indentationStatus.original.realPosition = -1;
        indentationStatus.original.numbered = true;
        indentationStatus.current.prevNumbered = [];
        indentationStatus.current.level = indentationStatus.original.level;
        indentationStatus.current.move = 0;
        indentationStatus.current.numbered = true;
    }

    const pluginName = "leosIndentMandate";

    const TRISTATE_DISABLED = CKEDITOR.TRISTATE_DISABLED,
        TRISTATE_OFF = CKEDITOR.TRISTATE_OFF;

    let pluginDefinition = {
        requires: 'indent',
        init: function init(editor) {
            let globalHelpers = CKEDITOR.plugins.indent;
            modeRealTimeIndent = true;

            resetIndentStatus();
            
            // Register commands.
            globalHelpers.registerCommands( editor, {
                aknindentlist: new commandDefinition( editor, 'aknindentlist', true ),
                aknoutdentlist: new commandDefinition( editor, 'aknoutdentlist' )
            } );

            function commandDefinition(editor) {
                globalHelpers.specificDefinition.apply( this, arguments );

                // Indent and outdent lists with TAB/SHIFT+TAB key. Indenting can
                // be done for any list item that isn't the first child of the parent.
                editor.on('key', function(evt) {
                    let path = editor.elementPath();

                    if (editor.mode != 'wysiwyg')
                        return;

                    if (evt.data.keyCode == this.indentKey) {
                        // Prevent of getting context of empty path (#424)(https://dev.ckeditor.com/ticket/17028).
                        if (!path) {
                            return;
                        }
                        const ol = $(this.getContext(path).$);

                        // Don't indent if in first list item of the parent.
                        if (this.isIndent && (!_shouldIndent(ol)))
                            return;
                        if ((!this.isIndent) && (!_shouldOutdent(ol)))
                            return;

                        editor.execCommand(this.relatedGlobal);

                        evt.cancel();
                    }
                }, this);

                this.jobs[this.isIndent ? 10 : 30] = {
                    refresh: this.isIndent ?
                        function(editor, path) {
                            _initIndentStatus(editor);
                            if (this.getContext(path) == null) {
                                return;
                            }
                            const ol = $(this.getContext(path).$);

                            if (!_shouldIndent(ol)) {
                                return TRISTATE_DISABLED;
                            } else {
                                return TRISTATE_OFF;
                            }
                        } : function(editor, path) {
                            _initIndentStatus(editor);
                            if (this.getContext(path) == null) {
                                return;
                            }
                            const ol = $(this.getContext(path).$);

                            if (!_shouldOutdent(ol)) {
                                return TRISTATE_DISABLED;
                            } else {
                                return TRISTATE_OFF;
                            }
                        },

                    exec: CKEDITOR.tools.bind(aknIndent, this)
                };

                editor.on('instanceReady', function(evt) {
                    _initIndentStatus(editor);
                }, this);
            }

            CKEDITOR.tools.extend(commandDefinition.prototype, globalHelpers.specificDefinition.prototype, {
                // Elements that, if in an elementpath, will be handled by this
                // command. They restrict the scope of the plugin.
                context: {ol: 1}
            });
        }
    };

    function aknIndent(editor) {
        const prevLevel = indentationStatus.current.level;
        // Do Indent
        if (this.isIndent && (indentationStatus.current.level < leosPluginUtils.MAX_LIST_LEVEL || !indentationStatus.current.numbered)) {
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

        let newNumValue = indentationStatus.original.num;
        if (indentationStatus.current.move != 0) {
            newNumValue = indentationStatus.current.numbered ? '#' : undefined;
            leosPluginUtils.doIndent(editor, indentationStatus);
            leosPluginUtils.setIndentAttributes(editor, indentationStatus);
        } else {
            indentationStatus.current.level = indentationStatus.original.level;
            leosPluginUtils.resetIndent(editor, this.isIndent);
            leosPluginUtils.resetIndentAttributes(editor, indentationStatus);
        }
        leosPluginUtils.setCurrentNumValue(newNumValue, editor, indentationStatus);

        return 0;
    }

    function _calculateNewLevel(editor, isIndent) {
        let source = $(editor.element.$);
        if (!isIndent && (leosPluginUtils.isUnumberedHtmlParagraph(editor)
            || (source.parents(PARAGRAPH).length
                && leosPluginUtils.isUnumberedparagraph(source.parents(PARAGRAPH))
                && indentationStatus.current.level == 1
                && !indentationStatus.current.numbered))) {
            indentationStatus.current.level --;
            indentationStatus.current.numbered = true;
        }
        if (isIndent && (leosPluginUtils.isUnumberedHtmlParagraph(editor)
            || (source.parents(PARAGRAPH).length
                && leosPluginUtils.isUnumberedparagraph(source.parents(PARAGRAPH))
                && indentationStatus.current.level == 1
                && !indentationStatus.current.numbered
                && indentationStatus.original.numbered))) {
            indentationStatus.current.numbered = true;
        }
        if (isIndent && (leosPluginUtils.isUnumberedHtmlParagraph(editor)
            || (source.parents(PARAGRAPH).length
                && leosPluginUtils.isUnumberedparagraph(source.parents(PARAGRAPH))
                && indentationStatus.current.level == 0
                && indentationStatus.current.numbered
                && !indentationStatus.original.numbered))) {
            indentationStatus.current.level++;
        }
    }

    function _initIndentStatus(editor) {
        if (indentationStatus.original.level == -1) {
            indentationStatus.original.level = leosPluginUtils.getIndentLevel(editor);
            let source = $(editor.element.$);
            if (leosPluginUtils.isSubpoint(editor) && leosPluginUtils.isFirstChild(editor)) {
                source = $(editor.element.$).parents(NUMBERED_ITEM).first();
            }
            indentationStatus.original.realPosition = source.index();
            indentationStatus.original.position = source.prevAll(ITEMS_SELECTOR).length;
            indentationStatus.original.parent = source.parent().attr('id');
            indentationStatus.current.level = indentationStatus.original.level;
            indentationStatus.original.num = leosPluginUtils.getCurrentNumValue(editor);
            indentationStatus.original.numbered = !(leosPluginUtils.isSubpoint(editor) && !leosPluginUtils.isFirstChild(editor));
            if (leosPluginUtils.isUnumberedHtmlParagraph(editor)) {
                indentationStatus.original.numbered = false;
            }
            indentationStatus.current.numbered = indentationStatus.original.numbered;
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

    function _checkParentAndPosition() {
        let prevSibling = undefined;

        const currentParent = $('#' + indentationStatus.original.parent);
        if (!indentationStatus.original.position && currentParent.prop("tagName").toLowerCase() != LEVEL) {
            return false;
        }

        let parentChildren = currentParent.children(NUMBERED_ITEM);
        if (!indentationStatus.original.numbered) {
            parentChildren = currentParent.children(ITEMS_SELECTOR);
        }
        prevSibling = $(parentChildren[indentationStatus.original.position - 1]);
        if (!!prevSibling.attr(leosPluginUtils.LEOS_SOFTACTION)
            && (prevSibling.attr(leosPluginUtils.LEOS_SOFTACTION) == leosPluginUtils.DEL
                || prevSibling.attr(leosPluginUtils.LEOS_SOFTACTION) == leosPluginUtils.MOVETO)) {
            return false;
        }

        let currentParentDepth = 0;
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

    pluginTools.addPlugin(pluginName, pluginDefinition);

    // return plugin module
    let pluginModule = {
        name: pluginName
    };

    return pluginModule;
});
