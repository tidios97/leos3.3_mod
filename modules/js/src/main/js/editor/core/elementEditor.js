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
define(function elementEditorModule(require) {
    "use strict";

    // load module dependencies
    var _ = require("lodash");
    var $ = require("jquery");
    var log = require("logger");
    var CKEDITOR = require("promise!ckEditor");
    var UTILS = require("core/leosUtils");
    var pluginTools = require("../plugins/pluginTools");
    var dialogDefinition = require("./leosEmptyElementDialog");
    var ZERO_WIDTH_SPACE = "^\u200B{7}$";
    var NUM = "num";

    function _setupElementEditor(connector) {
        log.debug("Setting up element editor...");
        // disable automatic editor creation
        CKEDITOR.disableAutoInline = true;
        // register client side API
        connector.editElement = _editElement;
        connector.refreshElement = _refreshElement;
        connector.receiveElement = _receiveElement;
        connector.receiveToc = _receiveToc;
        connector.receiveRefLabel = _receiveRefLabel;
        connector.closeElement = _closeElement;
    }

    function _editElement(elementId, elementType, elementFragment, docType, instanceType, alternatives, levelItemVo,
                            isClonedProposal) {
        log.debug("Initializing element editor...");
        let connector = this;

        let rootElement = UTILS.getParentElement(connector);
        let element = _createEditorPlaceholder(rootElement, elementId);
        let placeholder = _getEditorPlaceholder(rootElement, elementId);
        if(NUM === elementType) {
            _updatePlaceholderStyleForNumElement(placeholder);
        }
       
        if (!placeholder) {
            throw new Error("Editing element wrapper not found!");
        }

        // restrict scope to the extended target
        _scopeEvents(placeholder);

        var tocItemsList = JSON.parse(connector.getState().tocItemsJsonArray);
        var numberingConfigs = JSON.parse(connector.getState().numberingConfigsJsonArray);
        var listNumberConfig = JSON.parse(connector.getState().listNumberConfigJsonArray);
        var articleTypesConfigJsonArray = JSON.parse(connector.getState().articleTypesConfigJsonArray);
        var alternateConfigs = JSON.parse(connector.getState().alternateConfigsJsonArray);
        var levelItemObject = JSON.parse(levelItemVo);

        docType = docType.toLowerCase();
        var isAlternative = (alternatives != null && alternatives != "");
        let profileId = _getEditorProfileId(tocItemsList, elementType, element, isAlternative, alternateConfigs);

        if (profileId) {
            var params = {
                elementId: elementId,
                elementType: elementType,
                elementFragment: elementFragment,
                placeholder: placeholder,
                docType: docType,
                instanceType: instanceType,
                alternatives: alternatives,
                levelItemObject: levelItemObject,
                tocItemsList: tocItemsList,
                numberingConfigs: numberingConfigs,
                listNumberConfig: listNumberConfig,
                articleTypesConfig: articleTypesConfigJsonArray,
                isClonedProposal: isClonedProposal
            }
            // load the specific profile and initialize the editor
            require(["profiles/" + profileId],
                _initEditor.bind(undefined, connector, params));
        } else {
            throw new Error("Unknown element editor profile!");
        }
    }

    function _getEditorProfileId(tocItemsList, elementType, element, isAlternative, alternateConfigs) {
        let selectedProfile = null;
          if(isAlternative) {
              selectedProfile = alternateConfigs.find(function(config){return (config.type.toLowerCase() ===
                elementType.toLowerCase())}).profile;
          } else {
              tocItemsList.forEach(function(e) {
                if(elementType.toLowerCase() === e.aknTag.toLowerCase()) {
                     e.profiles["profiles"].forEach(function(profile) {
                         if(!profile.elementSelector || $(element).is(profile.elementSelector)) {
                             selectedProfile = profile.profileName;
                             return false;        
                          }
                     });
                }
                if(selectedProfile != null) {
                    return false;
                }
            });
          }
          return selectedProfile;
     }

    function _initEditor(connector, params, profile) {
        log.debug("Initializing element editor with %s profile...", profile.name);
        // retrieve the current user
        var user = connector.getState().user;
        user['permissions'] = connector.getState().permissions;

        if (connector.editorChannel) {
            connector.editorChannel.publish('editor.open', {elementId: params.elementId});
        }
        var placeholder = params.placeholder;
        // set content editable to start the editor in edit mode
        placeholder.contentEditable = true;
        // clear HTML content because fresh XML will be loaded
        placeholder.innerHTML = null;

        var config = _getConfig(connector, user, profile.config);

        // create editor instance
        var editor = CKEDITOR.inline(placeholder, config);

        if (editor) {
            // store LEOS data in editor
            editor.LEOS = {
                profile: profile,
                type: params.docType,
                instanceType: params.instanceType,
                user: user,
                implicitSaveEnabled: connector.getState().isImplicitSaveEnabled,
                elementType: params.elementType,
                isSpellCheckerEnabled: connector.getState().isSpellCheckerEnabled,
                spellCheckerServiceUrl: connector.getState().spellCheckerServiceUrl,
                spellCheckerSourceUrl: connector.getState().spellCheckerSourceUrl,
                alternatives: params.alternatives,
                tocItemsList : params.tocItemsList,
                numberingConfigs : params.numberingConfigs,
                listNumberConfig : params.listNumberConfig,
                articleTypesConfig : params.articleTypesConfig,
                documentsMetadata: JSON.parse(connector.getState().documentsMetadataJsonArray),
                documentRef: connector.getState().documentRef,
                isClonedProposal: params.isClonedProposal
            };
            // register editor event callbacks
            editor.on("close", _destroyEditor.bind(undefined, connector, params.elementId, params.elementType));
            editor.on("save", _saveElement.bind(undefined, connector, params.elementId, params.elementType));
            editor.on("requestElement", _requestElement.bind(undefined, connector));
            editor.on("requestToc", _requestToc.bind(undefined, connector));
            editor.on("requestRefLabel", _requestRefLabel.bind(undefined, connector));
            editor.on("merge", _mergeElement.bind(undefined, connector, params.elementId, params.elementType));
            editor.on('selectionChange', _removeZeroWidthSpacesOnFocus.bind(undefined, connector, params.elementId));

            // load XML fragment in editor
            var options = {
                internal: true,
                callback: function() {
                    var editor = this;
                    editor.fire("receiveData", params.elementFragment);
                    if(params.levelItemObject) {
                        editor.fire("receiveLevelItemVo", params.levelItemObject);
                    }
                    placeholder.style.height = ''; //reset the height to let editor grow
                }
            };
            editor.setData(params.elementFragment, options);

            // register EmptyTrimSelector
            UTILS.registerEmptyTrimSelector(params.tocItemsList);
        } else {
            throw new Error("Unable to initialize the element editor!");
        }
    }

    function _createEditorPlaceholder(rootElement, elementId) {
        let element;
        if (rootElement) {
            element = rootElement.querySelector(`#${elementId}`);
        } else {
            element = document.querySelector(`#${elementId}`);
        }

        $(element).wrap(function () {
            return `<div class="leos-placeholder" data-wrapped-id='${elementId}' style='height:${_getEditableAreaHeight(elementId)}px'></div>`;
        });
        
        return element;
    }

    function _getEditableAreaHeight(elementId) {
        return document.getElementById(elementId) != null ? document.getElementById(elementId).clientHeight : 0;
    }

    function _getEditorPlaceholder(rootElement, elementId) {
        var wrap = null;
        var selector = ".leos-placeholder[data-wrapped-id='" + elementId + "']";
        if (rootElement) {
            wrap = rootElement.querySelector(selector);
        } else {
            wrap = document.querySelector(selector);
        }
        return wrap;
    }
    
    function _updatePlaceholderStyleForNumElement(placeholder) {
       $(placeholder).css({
           "width": "fit-content",
           "grid-column": "1",
           "float": "left",
           "margin-right": "50px"
       });
    }

    var _setEventType = function (event) {
        event.hostEventType = "ckEvent";
    };

    function _scopeEvents(contentWrap) {
        // Scope all events from editor by a specific type
        for(var key in contentWrap){
            if(key.search('on') === 0) {
                contentWrap.addEventListener(key.slice(2), _setEventType)
            }
        }
    }

    function _unscopeEvents(contentWrap) {
        // Remove scoping on all events by removing listeners
        for(var key in contentWrap){
            if(key.search('on') === 0) {
                contentWrap.removeEventListener(key.slice(2), _setEventType)
            }
        }
    }

    function _getConfig(connector, user, defaultConfig) {
        // clone the profile configuration for this editor instance
        var config = _.cloneDeep(defaultConfig);

        if (user.permissions && user.permissions.includes("CAN_SEE_SOURCE")) {
            config.extraPlugins = [config.extraPlugins, "sourcedialog"].join();
        } else {
            config.removePlugins = [config.removePlugins, "sourcedialog"].join();
        }

        return config;
    }


    function _destroyEditor(connector, elementId, elementType, event) {
        log.debug("Destroying element editor...");
        var editor = event.editor;
        // set read-only to prevent changes
        editor.setReadOnly(true);

        var rootElement = UTILS.getParentElement(connector);
        var placeholder = _getEditorPlaceholder(rootElement, elementId);
        _unscopeEvents(placeholder);
        $(placeholder).height(_getEditableAreaHeight(elementId));
        editor.placeholder = placeholder;

        // release the element being edited
        var data = {
            elementId: elementId,
            elementType: elementType
        };
        connector.releaseElement(data);
        // destroy editor instance, without updating DOM
        if (placeholder != null) {
            placeholder.innerHTML = null; //used to avoid flickering text
        }
        editor.destroy(true);

        if (connector.editorChannel) {
            connector.editorChannel.publish('editor.close', {elementId: elementId});
        }
        // clear LEOS data from editor
        editor.LEOS = null;
    }

    function _saveElement(connector, elementId, elementType, event) {
        log.debug("Saving element...");
        var editor = event.editor;
        // LEOS-3418 : to save modification in the Alternatives clause.
        if (!editor.readOnly||editor.config.isClause) {
            _removeZeroWidthSpaces(elementId);
            if (!_isEmptyElement(elementId, editor)) {
                // set read-only to prevent changes
                editor.setReadOnly(true);
                // save the element being edited
                var data = {
                    elementId: elementId,
                    elementType: elementType,
                    elementFragment: event.data.data,
                    isSplit: event.data.origin === "split" ? true : false
                };
                connector.saveElement(data);
                return true;
            }
        }
        return false;
    }

    function _removeZeroWidthSpacesOnFocus(connector, elementId) {
        _removeZeroWidthSpaces(elementId);
    }

    function _removeZeroWidthSpaces(elementId) {
        $("#" + elementId).find("*").addBack().contents().filter(function() {
            if (this.nodeType === Node.TEXT_NODE && this.textContent) {
                return this.textContent.match(ZERO_WIDTH_SPACE);
            }
            return false;
        }).remove();
    }

    function _isEmptyElement(elementId, editor) {
        var emptyElements = $("#" + elementId + ", h2[data-akn-heading-id='" + elementId + "'], p[data-akn-num-id='" + elementId + "']").find(":emptyTrim").addBack(":emptyTrim");
        if (emptyElements.length > 0) {
            pluginTools.addDialog(dialogDefinition.dialogName, dialogDefinition.initializeDialog);
            var dialogCommand = editor.addCommand(dialogDefinition.dialogName, new CKEDITOR.dialogCommand(dialogDefinition.dialogName));
            dialogCommand.exec();
            return true;
        }
        return false;
    }

    function _refreshElement(elementId, elementType, elementFragment) {
        log.debug("Refreshing element editor...");
        var editor = _getEditor();
        if (editor) {
            // set editable to allow changes
            editor.setReadOnly(false);
            // reload XML fragment in editor
            var options = {
                    callback: function() {
                        var editor = this;
                        editor.fire("receiveData", elementFragment);
                        // reset dirty state as for unchanged content
                        editor.resetDirty();
                    }
            };
            editor.setData(elementFragment, options);
        }
    }

    function _requestElement(connector, event) {
        log.debug("Requesting element...");
        var data = {
            elementId: event.data.elementId,
            elementType: event.data.elementType,
            documentRef: event.data.documentRef
        };
        connector.requestElement(data);
    }

    function _receiveElement(elementId, elementType, elementFragment, documentRef) {
        log.debug("Received element...");
        var editor = _getEditor();
        var data = {
            elementId :elementId,
            elementType: elementType,
            elementFragment: elementFragment,
            documentRef: documentRef
        };
        editor.fire("receiveElement", data);
    }

    function _requestToc(connector, event) {
        log.debug("Requesting toc...");
        var data = {
            elementIds: event.data.selectedNodeIds
        };
        connector.requestToc(data);
    }

    function _receiveToc(tocWrapper) {
        log.debug("Received toc...");
        var editor = _getEditor();
        editor.fire("receiveToc", tocWrapper );
    }

    function _requestRefLabel(connector, event) {
        log.debug("Requesting reference labels...");
        connector.requestRefLabel(event.data);
    }

    function _receiveRefLabel(references, documentRef) {
        log.debug("Received reference labels...");
        var editor = _getEditor();
        var data = {
            references: references,
            documentRef: documentRef
        };
        editor.fire("receiveRefLabel", data);
    }

    function _closeElement() {
        log.debug("Requesting close element...");
        for (var name in CKEDITOR.instances) {
            if (CKEDITOR.instances.hasOwnProperty(name)) {
                var editor = CKEDITOR.instances[name];
                if (editor) {
                    editor.fire("close");
                }
            }
        }
    }

    function _teardownElementEditor(connector) {
        log.debug("Tearing down element editor...");
        // destroy any editor instances remaining
        for (var name in CKEDITOR.instances) {
            if (CKEDITOR.instances.hasOwnProperty(name)) {
                var editor = CKEDITOR.instances[name];
                if (editor) {
                    log.debug("Destroying editor instance:", name);
                    // destroy editor instance, without updating DOM
                    editor.destroy(true);
                }
            }
        }
    }

    function _mergeElement(connector, elementId, elementType, event) {
        log.debug("Merging element...");
        var editor = event.editor;
        var data = {
            elementId: elementId,
            elementType: elementType,
            elementFragment: event.data.data
        };
        connector.mergeElement(data);
    }

    function _getEditor() {
        var editor = CKEDITOR.currentInstance;
        if (!editor) {
            for (var name in CKEDITOR.instances) {
                if (CKEDITOR.instances.hasOwnProperty(name)) {
                    editor = CKEDITOR.instances[name];
                    editor.focus();
                    break;
                }
            }
        }
        return editor;
    }

    return {
        setup: _setupElementEditor,
        teardown: _teardownElementEditor
    };
});
