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
define(function contentScrollerModule(require) {
    "use strict";

    var $ = require("jquery");
	var CKEDITOR = require("promise!ckEditor");

	function _scrollTop(styleName) {
		var editor = CKEDITOR && CKEDITOR.currentInstance;
        if (!editor) {
			var $scrollPane = $('.' + styleName);
			if($scrollPane) {
				$scrollPane.animate(
		            {scrollTop: 0},
		            500, 
					"swing",
		            function(){}
		        );
			}	
		}
	}
	
	function _scrollBottom(styleName) {
		var editor = CKEDITOR && CKEDITOR.currentInstance;
        if (!editor) {
			var $scrollPane = $('.' + styleName);
			if($scrollPane) {
				$scrollPane.animate(
		            {scrollTop: $scrollPane.prop("scrollHeight")},
		            500, 
					"swing",
		            function(){}
		        );
			}
		}
	}

    function _scrollTo(element, target, additionalAction, blink= true) {
        if (typeof element == 'string') {//if ID is passed get element
            element = _findEditElementById(element);
        }

        var $scrollPane = target != null ? $(target) : $('.leos-doc-content');
        
        if (element) {
            $scrollPane.animate(
                {scrollTop: _calculateScrollTopPosition(element, $scrollPane)},
                500, "swing",
                _onScrollCompletion
            );
        }

        function _onScrollCompletion() {
            if (blink) {
                var bgColor = element.style.backgroundColor;
                element.style.backgroundColor = "cornsilk";
                setTimeout(function () {
                    element.style.backgroundColor = bgColor;
                }, 500);
            }
            if (additionalAction) {
                additionalAction(element);
            }
        }

        function _calculateScrollTopPosition(element, $scrollPane) {
            element = _getPositionedElement(element);// this fix is required for the elements which are not displayed

            //get bounding rect returns position corresponding to browser top. So subtracting container top to convert it to reference to container
            var currentElementPositionRelativeToWindow = element.getBoundingClientRect().top - $scrollPane[0].getBoundingClientRect().top;

            var newScrollTopForPane = currentElementPositionRelativeToWindow
                + $scrollPane[0].scrollTop  //adding already scrolled factor to position to make it relative to target pane
                - ($('.cke_inner').length > 0 ? $('.cke_inner').outerHeight() : 76);//76 is to shift the selected content little away from top bar

            return newScrollTopForPane;
        }

        function _getPositionedElement(element) {
            while ($(element).is(':hidden')) {
                element = element.parentElement;
            }
            return element;
        }
    }

    function _scrollToElement(elementId, prefix) {
        var $markedContainer = $(".leos-"+prefix+"-content");
        if($markedContainer.length) {
            var $docContainer = $(".leos-doc-content");
            var $docElement = $docContainer.find("#"+elementId);
            var $markedElement = $markedContainer.find("#"+prefix+"-" + elementId);
            $markedContainer.animate({
                scrollTop: _calculateMarkedElementPosition($docElement.get(0), $markedElement.get(0), $docContainer.get(0), $markedContainer.get(0))
            }, 500, "swing");
        }
    }

    function _calculateMarkedElementPosition(docElement, markedElement, docContainer, markedContainer) {
        var docElementPosition = docElement.getBoundingClientRect().top - docContainer.getBoundingClientRect().top;
        var markedElementPosition = markedElement.getBoundingClientRect().top - markedContainer.getBoundingClientRect().top;
        var elementNewPosition = markedContainer.scrollTop + markedElementPosition - docElementPosition;

        return elementNewPosition;
    }

    function _findEditElementById(id) {
        let prefixElement = document.getElementById(id);
        if (!prefixElement) { // prefixElement not found
            const SOFT_MOVE_PLACEHOLDER_ID_PREFIX = "moved";
            const SOFT_DELETE_PLACEHOLDER_ID_PREFIX = "deleted";
            const SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX = "transformed";
            let softActions = [SOFT_MOVE_PLACEHOLDER_ID_PREFIX, SOFT_DELETE_PLACEHOLDER_ID_PREFIX, SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX];

            const prefixes = id.split('_');

            if(softActions.includes(prefixes[0])) {
                if(softActions.includes(prefixes[1])) {
	                id = id.replace(prefixes[1] + '_','');
	                prefixElement = document.getElementById(id);
                }
                if (!prefixElement) {
	                id = id.replace(prefixes[0] + '_','');
	                prefixElement = document.getElementById(id);
                }
            }
        }
        return prefixElement;
    }



    //Exposing this function to used only for cases where modules are not available
    LEOS.scrollTop = _scrollTop;
	LEOS.scrollBottom = _scrollBottom;
	LEOS.scrollTo = _scrollTo;
    LEOS.scrollToElement = _scrollToElement;

    return {
		scrollTop : _scrollTop,
		scrollBottom: _scrollBottom,
        scrollTo : _scrollTo,
        scrollToElement: _scrollToElement
    };
});
