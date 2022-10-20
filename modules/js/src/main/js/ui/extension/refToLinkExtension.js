/*
 * Copyright 2021 European Commission
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
define(function refToLinkExtensionModule(require) {
    "use strict";

    // load module dependencies
    var log = require("logger");
    var $ = require("jquery");
    var refToLink = require("refToLink");
    var referencesCache = new Map();

    var regExpEscape = function (pattern) {
        return pattern.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
    };

    function replaceNbsps(str) {
        return str.replace(/&nbsp;/gi, String.fromCharCode(160));
    }

    function replaceNbspsBySpaces(str) {
        return str.replace(/&nbsp;/gi, " ");
    }

    function _initRefToLink(connector) {
        log.debug("Initializing refToLink extension...");

        // configure ref2Link
        $.fn.ref2link.options = {tooltipTrigger: 'notooltip'}; //Disabling the tooltip 
        $.fn.ref2link.setFilter('environments', ['EC-PRD']);// enable sets of rules

        log.debug("Registering refToLink extension unregistration listener...");
        connector.onUnregister = _connectorUnregistrationListener;

        log.debug("Registering refToLink extension state change listener...");
        connector.onStateChange = _connectorStateChangeListener;
    }

    // handle connector unregistration on client-side
    function _connectorUnregistrationListener() {
        log.debug("Unregistering refToLink extension...");
        $.fn.ref2link.clearCache();
        referencesCache.clear();
    }

    // handle connector state change on client-side
    function _connectorStateChangeListener() {
        log.debug("refToLink extension state changed...");
        // KLUGE delay execution due to sync issues with target update
        setTimeout(_registerObservers, 500);
    }

    function _registerObservers() {
        log.debug("Registering observers for elements...");
        const observer = new IntersectionObserver(function (entries) {
            entries.forEach(entry => {
                if (entry.isIntersecting === true) { // Element appears in the screen
                    observer.unobserve(entry.target); // Element refreshed then not needed to observe anymore
                    setTimeout(_renderLinks, 1000, entry.target);
                }
            });
        });

        _addToObserver(["preface", "preamble", "aknbody > *", "mainbody > *", ".leos-authnote-table"]);

        function _addToObserver(selectors) {
            selectors.forEach(selector => {
                const elementsToObserve = document.querySelectorAll("#docContainer " + selector);
                elementsToObserve.forEach(elementToObserve => observer.observe(elementToObserve));
            });
        }
    }

    function _renderLinks(el) {
        log.debug("Rendering links...");
        var textNodes = _textNodesUnder(el),
            references = _getReferences(el);

        //1. check for all references in text nodes
        //2. check the reference with Longest match first and if found, store a placeholder
        //3. if cache has something replace placeholders with values
        var cache = {}; //placeholder-reference cache. 
        textNodes.forEach(function (textNode, txtIndex) {
            var newVal = textNode.nodeValue;
            references.forEach(function (ref, refIndex) {
                // Check first that ref context is included in text node
                var refToFind = replaceNbsps(ref.match);
                var originalText = textNode.nodeValue;
                if (originalText.indexOf(replaceNbspsBySpaces(ref.context)) > -1
                    || originalText.indexOf(replaceNbsps(ref.context)) > -1) {
                    // Done like that to avoid too many matches if ref match is only one digit
                    if ((refToFind.split(new RegExp('\\b')).length > 1 && newVal.indexOf(refToFind) > -1)
                        || (newVal.search(new RegExp('\\b' + regExpEscape(refToFind) + '\\b')) > -1)) {
                        newVal = _injectPlaceholders(newVal, '##R' + refIndex + '##', ref, cache);
                    }
                } else if (refToFind.split(new RegExp(' ')).length > 1
                    && refToFind.includes("/")
                    && newVal.indexOf(refToFind) > -1) {
                    // Case for LEOS-5351 where ref context is not present in the node but ref contains a ref to OJ
                    newVal = _injectPlaceholders(newVal, '##R' + refIndex + '##', ref, cache);
                }
            });

            if (Object.keys(cache).length > 0) {
                newVal = _ejectPlaceholders(newVal, cache);
                $(textNode).replaceWith(newVal); //inject in DOM
            }
        });
        
        //helper functions
        function _getReferences(el) {
            let references, referenceKey = el.id + '_' + _getHash(el.innerText);
            if (!referencesCache.has(referenceKey)) {
                references = $(el).clone().getReferences();
                //Sort to handle case where two references are in same line Example art 2 directive 2017/11/EC and directive 2017/11/EC
                references.sort(function (left, right) {
                    return replaceNbsps(right.match).length - replaceNbsps(left.match).length;
                });
                referencesCache.set(referenceKey, references);
            } else {
                references = referencesCache.get(referenceKey);
            }
            return references;
        }

        function _getHash(text) {
            let hash = 0;
            for (let i = 0; i < text.length; i++) {
                const char = text.charCodeAt(i);
                hash = (hash << 5) - hash + char;
                hash &= hash; // Convert to 32bit integer
            }
            return new Uint32Array([hash])[0].toString(36);
        }

        function _injectPlaceholders(text, placeholder, ref, cache) {
            cache[placeholder] = ref;
            var refToFind = replaceNbsps(ref.match);
            if (refToFind.split(new RegExp('\\b')).length > 1) {
                return text.replace(new RegExp(regExpEscape(refToFind), 'g'), placeholder);
            } else {
                return text.replace(new RegExp('\\b' + regExpEscape(refToFind) + '\\b', 'g'), placeholder);
            }
        }

        function _ejectPlaceholders(text, cache) {
            Object.keys(cache).forEach(function (placeholder) {
                // the new value to replace is coming as an attribute of the array cache[placeholder].views
                var arrViews = cache[placeholder].views;
                Object.keys(arrViews).forEach(function (key) {
                    text = text.replace(new RegExp(placeholder, 'g'), arrViews[key].trim());
                });
            });
            return text;
        }
    }

    function _textNodesUnder(el) {
        var node, result = [],
            walker = document.createTreeWalker(el, NodeFilter.SHOW_TEXT,
                {
                    acceptNode: function (node) {
                        return /^(\s*)(\S+)/.test(node.nodeValue)
                            ? NodeFilter.FILTER_ACCEPT
                            : NodeFilter.FILTER_REJECT;
                    }
                }, false);

        //walk
        var editedElement = el.querySelector('div.leos-placeholder'); // Skip text nodes inside CKEditor
        while (node = walker.nextNode()) {
            if ((editedElement == null) || ((editedElement != null) && (!editedElement.contains(node)))) {
                result.push(node);
            }
        }
        return result;
    }

    return {
        init: _initRefToLink
    };
});
