/*
 * Copyright 2022 European Commission
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
define(function mergeContributionExtensionModule(require) {
    "use strict";

    var log = require("logger");
    var $ = require("jquery");
    var UTILS = require("core/leosUtils");

    var MERGE_ACTION_ATTR = "leos:mergeAction";
    var LEOS_SOFT_ACTION = "leos:softaction";
    var CONTRIBUTION_SELECTED = "selected-contribution-wrapper";
    var LEOS_CONTENT_REMOVED = "leos-content-removed";
    var LEOS_CONTENT_NEW = "leos-content-new";
    var MERGE_CONTRIBUTION = "merge-contribution-wrapper";
    var REVISION_MOVED = "revision-moved_";
    var MERGE_ACTION_WRAPPER = ".merge-actions-wrapper";
    var MOVE_FROM = "move_from",MOVE_TO = "move_to", PARENT_AFFECTED = "parent_affected";
    var MOVE = "move", DELETE = "delete", ADD = "add", CONTENT_CHANGE = "content_change";
    var SUB_ELEMENT = ["span","content"];

    var wrapperElementsList;
    var mergeActionList = new Array();

    function _initExtension(connector) {
        connector.refreshContributions = _refreshContributions;
        connector.populateMergeActionList = _populateMergeActionList;
        connector.populateTocItemList = _populateTocItemList;
        connector.onStateChange = _connectorStateChangeListener;
        log.debug("Registering merge contribution extension unregistration listener...");
        connector.onUnregister = _unregisterActionTriggers;
        mergeActionList = new Array();
    }

    function _connectorStateChangeListener() {
        var connector = this;
        log.debug("Change details extension state changed...");
        _requestTocItemList(connector);
    }

    function _registerActionTriggers(connector) {
        var removed_elements = $(".leos-content-removed");
        var new_elements = $(".leos-content-new");
        var changed_element = $.merge(removed_elements, new_elements);
        changed_element.each(function (index) {
            var $element = $(changed_element[index]);
            if (SUB_ELEMENT.includes(UTILS.getElementTagName($element))) {
                if (!($element.parent().hasClass(MERGE_CONTRIBUTION))) {
                    var elementParentName = UTILS.getElementTagName($element.parent());
                    var $parent = (elementParentName === "aknp" || elementParentName === "block") ? $element.parent() : UTILS.getParentWrapper($element, wrapperElementsList);
                    if ($parent) {
                        $parent.attr(PARENT_AFFECTED, true);
                        _attachWrapperActionEvents(connector, $parent)
                    }
                }
            } else {
                _attachWrapperActionEvents(connector, $element);
            }
        });
    }

    function _attachWrapperActionEvents(connector, $element) {
       if (!($element.hasClass(MERGE_CONTRIBUTION) || ($element.parent().hasClass(LEOS_CONTENT_NEW) &&
            $element.parent().attr(LEOS_SOFT_ACTION) !== MOVE_FROM) ||
            $element.parent().hasClass(LEOS_CONTENT_REMOVED))) {
            $element.addClass(MERGE_CONTRIBUTION);
            _attachActions(connector, $element);
            _createClickActions(connector, $element);
       } else if ($element.hasClass(CONTRIBUTION_SELECTED)) {
            let $parent = UTILS.getParentWrapper($element, wrapperElementsList);
            $parent.children(MERGE_ACTION_WRAPPER).remove();
            $parent.removeClass(CONTRIBUTION_SELECTED);
            _attachActions(connector, $element);
            _createClickActions(connector, $element);
       }
    }

    function _createClickActions(connector, $element) {
        $element.on("click.actions", "[data-widget-type='accept']", _handleAction.bind(undefined, connector, "accept", $element));
        $element.on("click.actions", "[data-widget-type='reject']", _handleAction.bind(undefined, connector, "reject", $element));
        $element.on("click.actions", "[data-widget-type='undo']", _handleAction.bind(undefined, connector, "undo", $element));
        $element.on("click.actions", "[data-widget-type='unselect']", _handleAction.bind(undefined, connector, "unselect", $element));
    }

    function _attachActions(connector, element) {
        let actions = _getActionButtons(connector, element);
        actions.target = element;
        element.actions = actions;
    }

    function _generateActions(processed) {
        let template = ['<div class="Vaadin-Icons merge-actions-wrapper">'];
        template.push('<div class="merge-actions">');
        if (processed) {
            template.push('<span class="undo" data-widget-type="undo" title="Undo">reply</span>');
        } else {
            template.push('<span class="accept" data-widget-type="accept" title="Accept">check</span>');
            template.push('<span class="reject" data-widget-type="reject" title="Reject">close</span>');
        }
        template.push('</div>');
        template.push('</div>');
        return template.join('');
    }

    function _generateUnselectAction(action) {
        let template = ['<div class="Vaadin-Icons merge-actions-wrapper">'];
        template.push('<span class="unselect" data-widget-type="unselect" title="Unselect">rotate-left</span>');
        if (action === 'accept') {
            template.push('<span class="accept" data-widget-type="accept" title="Accepted">check</span>');
        } else if (action === 'reject') {
            template.push('<span class="reject" data-widget-type="reject" title="Rejected">close</span>');
        }

        template.push('</div>');
        return template.join('');
    }

    function _handleAction(connector, action, $element, event) {
        log.debug("Handling action for: "+$element);
        event.stopPropagation();
        _executeAction(connector, action, $element)
        _handleMovedElement(connector, action, $element);
    }

    function _handleMovedElement(connector, action, $element) {
        var softAction = $element.attr(LEOS_SOFT_ACTION);
        if(softAction) {
            var $movedElement;
            if( softAction === MOVE_FROM) {
                $movedElement = $('#'+$element.attr('leos:softmove_from'));
            } else if(softAction === MOVE_TO) {
                $movedElement = $('#'+ $element.attr('leos:softmove_to'));
            }
            if($movedElement) {
                _executeAction(connector, action, $movedElement)
            }
        }
    }

    function _executeAction(connector, action, $element) {
        let element = $element[0];
        let $parent = $element.hasClass(MERGE_CONTRIBUTION) ? $element : UTILS.getParentWrapper($element, wrapperElementsList);
        let wrapperId = $parent[0].getAttribute("id");
        let elementState = getElementState($element);
        const data = {
            action: action,
            elementState: elementState,
            elementId: element.id,
            elementTagName: element.localName,
        };
        if (action === 'unselect') {
            $parent.children(MERGE_ACTION_WRAPPER).remove();
            $parent.removeClass(CONTRIBUTION_SELECTED);
            let actionString = _generateActions($parent.attr(MERGE_ACTION_ATTR) && $parent.attr(MERGE_ACTION_ATTR) !== null);
            let actions = ($.parseHTML(actionString))[0];
            $parent.prepend(actions);
            let index = mergeActionList.findIndex(element => element.elementId === data.elementId);
            if (index !== -1) {
                mergeActionList.splice(index, 1);
            }
            const selectionData = {
                selected : false,
            };
            connector.handleContributionSelection(selectionData);
        } else if (!$parent.hasClass(CONTRIBUTION_SELECTED)) {
            $parent.addClass(CONTRIBUTION_SELECTED);
            $parent.children(MERGE_ACTION_WRAPPER).remove();
            let unselectActionString = _generateUnselectAction(action);
            let unseletAction = ($.parseHTML(unselectActionString))[0];
            $parent.prepend(unseletAction);
            //in case of move there are two elements TO & FROM, element TO should not be sent for processing, only FROM
            // element is sent, FROM element is first removed from original (left side) document and then inserted at new position
            if (wrapperId !== null && wrapperId !== undefined && !wrapperId.startsWith(REVISION_MOVED)) {
                mergeActionList.push(data);
            }
            const selectionData = {
                selected : true,
            };
            connector.handleContributionSelection(selectionData);
        }
    }

    function getElementState($element) {
        let elementState;
        if ($element.attr(LEOS_SOFT_ACTION) && ($element.attr(LEOS_SOFT_ACTION) === MOVE_FROM || $element.attr(LEOS_SOFT_ACTION) === MOVE_TO)) {
            elementState = MOVE;
        } else if ($element.hasClass(LEOS_CONTENT_REMOVED)) {
            elementState = DELETE;
        } else if ($element.hasClass(LEOS_CONTENT_NEW)) {
            elementState = ADD;
        } else if (($element.attr(PARENT_AFFECTED))) {
            elementState = CONTENT_CHANGE;
        }
        return elementState;
    }

    function _getActionButtons(connector, $element) {
        let actions = $element.actions;
        if (!actions) {
            let mergeActionAttrVal = $element.attr(MERGE_ACTION_ATTR);
            let processed = mergeActionAttrVal != null ? true : false;
            let actionString = _generateActions(processed);
            actions = ($.parseHTML(actionString))[0];
            $element.prepend(actions);
        }
        return actions;
    }

    function _requestTocItemList(connector) {
        connector.requestTocItemList();
    }

    function _populateTocItemList() {
        let connector = this;
        wrapperElementsList = JSON.parse(connector.getState().tocItemsJsonArray);
        _registerActionTriggers(connector);
    }

    function _refreshContributions() {
        let connector = this;
        mergeActionList = new Array();
        _registerActionTriggers(connector);
    }

    function _populateMergeActionList(selectAll) {
        let connector = this;
        return connector.handleMergeAction(selectAll === true ? _acceptAllElements(connector) : mergeActionList);
    }

    function _acceptAllElements(connector) {
        mergeActionList = new Array();
        var removed_elements = $(".leos-content-removed");
        var new_elements = $(".leos-content-new");
        var changed_element = $.merge(removed_elements, new_elements);
        for (let i = 0; i < changed_element.length; i++) {
            var $element = $(changed_element[i]);
            if (SUB_ELEMENT.includes(UTILS.getElementTagName($element))) {
                if ($element.parent().hasClass(MERGE_CONTRIBUTION)) {
                    var elementParentName = UTILS.getElementTagName($element.parent());
                    var $parent = elementParentName === "aknp" ? $element.parent() : UTILS.getParentWrapper($element, wrapperElementsList);
                    if($parent) {
                        $parent.attr(PARENT_AFFECTED,true);
                        _triggerAction(connector, $parent, "accept")
                    }
                }
            } else {
                _triggerAction(connector, $element, "accept");
            }
        }
        return mergeActionList;
    }

    function _triggerAction(connector, $element, action) {
       if (($element.hasClass(MERGE_CONTRIBUTION) || ($element.parent().hasClass(LEOS_CONTENT_NEW) &&
            $element.parent().attr(LEOS_SOFT_ACTION) !== MOVE_FROM) ||
            $element.parent().hasClass(LEOS_CONTENT_REMOVED))) {
             _executeAction(connector, action, $element)
             _handleMovedElement(connector, action, $element);
       }
    }

    function _unregisterActionTriggers() {
        log.debug("Unregistering action triggers...");
        let connector = this;
        connector.target = null;
    }

    return {
        init: _initExtension,
    };
});