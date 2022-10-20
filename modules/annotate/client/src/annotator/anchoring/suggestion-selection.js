/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * DS206: Consider reworking classes to avoid initClass
 * DS207: Consider shorter variations of null checks
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
let LeosSuggestionSelection;
const $ = require('jquery');
const RulesEngine = require('../../utils/rules-engine');

// extends string indexOf functionnality
String.prototype.nthIndexOf = function(pattern, n) {
  let i = -1;
  while (n-- && (i++ < this.length)) {
    i = this.indexOf(pattern, i);
    if (i < 0) {
      break;
    }
  }
  return i;
};

const escape = string => string.replace(/([ #;&,.+*~\':"!^$[\]()=>|\/])/g, '\\$1');

module.exports = (LeosSuggestionSelection = (function() {
  let HIGHLIGHT_TAG;
  let _isEditable;
  let _isAllowed;
  let _getClosestAncestorWithId;
  LeosSuggestionSelection = class LeosSuggestionSelection {
    static initClass() {
      HIGHLIGHT_TAG = 'hypothesis-highlight';
  
      //
      // Returns true if selection is done on an editable part
      // @param element: range common ancestor
      // @param editableSelector: attribute name which define if an element is editable or not
      // @return: returns true if selection is done on an editable part.
      //
      _isEditable = (element, editableSelector) => {
        const editableSelectors = editableSelector.split(',');
  
        for (let editIndex in editableSelectors) {
          var editableElement;
          const editSelector = editableSelectors[editIndex];
          if (editSelector === '') {
            return true;
          }
  
          try {
            editableElement = $(element).closest(editSelector);
          } catch (error) {
            console.log('Error: ' + error);
          }
  
          if ((editableElement != null) && (editableElement.length > 0)) {
            return true;
          }
        }
        return false;
      };
  
      //
      // Returns true if selection for suggestion is allowed - checks the parent's elements
      // @param element: range common ancestor
      // @param notAllowedSuggestSelector: attribute containing JQuery selector of not allowed parent elements
      // @return: returns true if selection is allowed.
      //
      _isAllowed = (element, notAllowedSuggestSelector) => {
        let notAllowedSuggestElement;
        try {
          notAllowedSuggestElement = $(element).closest(notAllowedSuggestSelector);
        } catch (error) {
          return true;
        }
  
        if ((notAllowedSuggestElement != null) && (notAllowedSuggestElement.length > 0)) {
          return false;
        } else {
          return true;
        }
      };
  
      //
      // It gets the closest parent of @param element with a non null/empty id
      // @param element
      // @return: returns the closest parent of @param element with a non empty id or returns null if not found.
      //
      _getClosestAncestorWithId = function(element) {
        if ((element != null) && (element.id != null) && (element.id !== '')) {
          return element;
        } else if (element.parentElement != null) {
          return _getClosestAncestorWithId(element.parentElement);
        } else {
          return null;
        }
      };
    }

    constructor(allowedSelectorTags, editableSelector, notAllowedSuggestSelector) {
      this.allowedSelectorTags = allowedSelectorTags;
      this.editableSelector = editableSelector;
      this.notAllowedSuggestSelector = notAllowedSuggestSelector;
    }

    //
    // Returns true if selection text contains only text or allowed elements
    // @param selection's range
    // @return: returns true if selection text contains only text or allowed elements.
    //
    isSelectionAllowedForSuggestion(documentFragment, rangeCommonAncestorContainer) {
      const self = this;
      const treeWalker = document.createTreeWalker(documentFragment, NodeFilter.SHOW_ELEMENT, { acceptNode(node) {
        if ((node.nodeType === Node.TEXT_NODE) || ((node.nodeType === Node.ELEMENT_NODE) && (node.matches(self.allowedSelectorTags) || node.matches(HIGHLIGHT_TAG)))) {
          return NodeFilter.FILTER_SKIP;
        } else {
          return NodeFilter.FILTER_ACCEPT;
        }
      },
      }, false);
      return ((treeWalker.nextNode() === null) && _isEditable(rangeCommonAncestorContainer, self.editableSelector) && _isAllowed(rangeCommonAncestorContainer, self.notAllowedSuggestSelector));
    }

    //
    // Extracts from selection the content in Html removing allowed elements tags
    // @param selection's range document fragment
    // @return: returns from selection the content in Html removing allowed elements tags.
    //
    extractSuggestionTextFromSelection(documentFragment) {
      const self = this;
      const rulesEngine = new RulesEngine();
      const tmpDiv = document.createElement('div');
      tmpDiv.appendChild(documentFragment);
      const selectionsRules = { element: {
        [self.allowedSelectorTags]() {
          this.insertAdjacentHTML('afterend', this.innerHTML);
          this.parentNode.removeChild(this);
        },
        [HIGHLIGHT_TAG]() {
          this.insertAdjacentHTML('afterend', this.innerHTML);
          this.parentNode.removeChild(this);
        },
      },
      };

      rulesEngine.processElement(selectionsRules, tmpDiv);
      return tmpDiv.textContent;
    }

    //
    // While accepting a suggestion (@param annot) it generates appropriated array of selectors to send to LEOS
    // @param suggestion: suggestion itself
    // @param anchors: all anchors of the document
    // @return: returns array of selectors, one selector contains
    //          {origText: this.textContent, elementId: elementId, startOffset: startOffset, endOffset: endOffset, parentElementId: parentElementId}.
    //
    // FIXME temporary fix we consider that only one highlight will be used here
    getSuggestionSelectors(suggestion, anchors) {
      const anchor = anchors.find(a => {
        return a.annotation.$tag === suggestion.$tag;
      });
      if (anchor != null) {
        let highlightedText = '';
        let completeOuterHTML = '';
        if (anchor.highlights != null) {
          let grandParentElement;
          anchor.highlights.every(h => {
            highlightedText += h.textContent;
            return completeOuterHTML += h.outerHTML;
          });
          const parentElement = _getClosestAncestorWithId(anchor.range.commonAncestorContainer); //Common Ancestor could a highlight or a wrapper, find appropriate one
          const childHighlights = parentElement.querySelectorAll(HIGHLIGHT_TAG);
          const hIndex = Array.prototype.indexOf.call(childHighlights, anchor.highlights[0]);
          const eltContent = parentElement.innerHTML;
          const startOffset = eltContent.nthIndexOf(`<${HIGHLIGHT_TAG}`, hIndex + 1); // Takes only the first one: temporary fix because until now only one text node
          // could highlighted
          const endOffset = startOffset + highlightedText.length;
          const elementId = parentElement.id;
          const parentElementId = null;
          if (parentElement.parentElement != null) {
            grandParentElement = _getClosestAncestorWithId(parentElement.parentElement);
          }
          return {
            origText: highlightedText,
            elementId,
            startOffset,
            endOffset,
            parentElementId: grandParentElement.id,
            completeOuterHTML,
          };
        } else {
          return null;
        }
      } else {
        return null;
      }
    }
  };
  LeosSuggestionSelection.initClass();
  return LeosSuggestionSelection;
})());
