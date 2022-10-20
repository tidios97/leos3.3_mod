/*
 * decaffeinate suggestions:
 * DS201: Simplify complex destructure assignments
 * DS206: Consider reworking classes to avoid initClass
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
let Guest;
const extend = require('extend');
const raf = require('raf');
const scrollIntoView = require('scroll-into-view');
const CustomEvent = require('custom-event');

const Delegator = require('./delegator');
const $ = require('jquery');

const adder = require('./adder');
const highlighter = require('./highlighter');
const rangeUtil = require('./range-util');
const selections = require('./selections');
const xpathRange = require('./anchoring/range');
const { normalizeURI } = require('./util/url');
const animationPromise = fn => new Promise((resolve, reject) => raf(function () {
  try {
    return resolve(fn());
  } catch (error) {
    return reject(error);
  }
}));

module.exports = (Guest = (function () {
  let SHOW_HIGHLIGHTS_CLASS;

  Guest = class Guest extends Delegator {
    static initClass() {
      SHOW_HIGHLIGHTS_CLASS = 'annotator-highlights-always-on';

      // Events to be bound on Delegator#element.
      this.prototype.events = {
        '.annotator-hl click': 'onHighlightClick',
        '.annotator-hl mouseover': 'onHighlightMouseover',
        '.annotator-hl mouseout': 'onHighlightMouseout',
        'click': 'onElementClick',
        'touchstart': 'onElementTouchStart',
      };

      this.prototype.options = {
        Document: {},
        TextSelection: {},
      };

      // Anchoring module
      this.prototype.anchoring = require('./anchoring/html');

      // Internal state
      this.prototype.plugins = null;
      this.prototype.anchors = null;
      this.prototype.visibleHighlights = false;
      this.prototype.visibleGuideLines = false; //LEOS Change
      this.prototype.visibleGuideLinesPrevState = true; //LEOS Change
      this.prototype.annotationPopupStatusPrevState = true; //LEOS Change
      this.prototype.frameIdentifier = null;

      this.prototype.html =
        { adder: '<hypothesis-adder></hypothesis-adder>' };
    }

    constructor(element, config) {
      super(...arguments);

      this.adder = $(this.html.adder).appendTo(this.element).hide();

      const self = this;
      this.adderCtrl = new adder.Adder(this.adder[0], {
        onAnnotate() {
          self.createAnnotation();
          return document.getSelection().removeAllRanges();
        },
        onHighlight() {
          self.setVisibleHighlights(true);
          self.createHighlight();
          return document.getSelection().removeAllRanges();
        },
      });
      this.selections = selections(element).subscribe({ //LEOS Change: selections allowed on element not document
        next(range) {
          if (range) {
            return self._onSelection(range);
          } else {
            return self._onClearSelection();
          }
        },
      });

      this.plugins = {};
      this.anchors = [];

      // Set the frame identifier if it's available.
      // The "top" guest instance will have this as null since it's in a top frame not a sub frame
      this.frameIdentifier = config.subFrameIdentifier || null;

      const cfOptions = {
        config,
        on: (event, handler) => {
          return this.subscribe(event, handler);
        },
        emit: (event, ...args) => {
          return this.publish(event, args);
        },
      };

      if (typeof (config.clientUrl) !== 'undefined') {
        const hypothesisUrl = new URL(config.clientUrl);
        // Set options
        this.contextRoot = hypothesisUrl.pathname.substring(0, hypothesisUrl.pathname.indexOf('/', 2));
      }

      this.addPlugin('CrossFrame', cfOptions);
      this.crossframe = this.plugins.CrossFrame;

      this.crossframe.onConnect(() => this._setupInitialState(config));
      this._connectAnnotationSync(this.crossframe);
      this._connectAnnotationUISync(this.crossframe);

      // Load plugins
      for (let name of Object.keys(this.options || {})) {
        const opts = this.options[name];
        if (!this.plugins[name] && this.options.pluginClasses[name]) {
          this.addPlugin(name, opts);
        }
      }
    }

    addPlugin(name, options) {
      if (this.plugins[name]) {
        console.error('You cannot have more than one instance of any plugin.');
      } else {
        const klass = this.options.pluginClasses[name];
        if (typeof klass === 'function') {
          this.plugins[name] = new klass(this.element[0], options);
          this.plugins[name].annotator = this;
          if (typeof this.plugins[name].pluginInit === 'function') {
            this.plugins[name].pluginInit();
          }
        } else {
          console.error('Could not load ' + name + ' plugin. Have you included the appropriate <script> tag?');
        }
      }
      return this; // allow chaining
    }

    // Get the document info
    getDocumentInfo() {
      let metadataPromise,
        uriPromise;
      if (this.plugins.PDF != null) {
        metadataPromise = Promise.resolve(this.plugins.PDF.getMetadata());
        uriPromise = Promise.resolve(this.plugins.PDF.uri());
      } else if (this.plugins.Document != null) {
        uriPromise = Promise.resolve(this.plugins.Document.uri());
        metadataPromise = Promise.resolve(this.plugins.Document.metadata);
      } else {
        uriPromise = Promise.reject();
        metadataPromise = Promise.reject();
      }

      uriPromise = uriPromise.catch(() => decodeURIComponent(window.location.href));
      metadataPromise = metadataPromise.catch(() => ({
        title: document.title,
        link: [{ href: decodeURIComponent(window.location.href) }],
      }));

      return Promise.all([metadataPromise, uriPromise]).then((...args) => {
        const [metadata, href] = args[0];
        return {
          uri: normalizeURI(href),
          metadata,
          frameIdentifier: this.frameIdentifier,
        };
      });
    }

    _setupInitialState(config) {
      this.publish('panelReady');
      this.setVisibleHighlights(config.showHighlights === 'always');
      this.setVisibleGuideLines(config.showGuideLinesButton);
    }

    _connectAnnotationSync(crossframe) {
      this.subscribe('annotationDeleted', annotation => {
        this.detach(annotation);
      });

      return this.subscribe('annotationsLoaded', annotations => {
        let addToPromiseChain = false;
        for (let annotation of annotations) {
          var promiseChain = this.anchor(annotation, promiseChain, addToPromiseChain);
          addToPromiseChain = true;
        }
      });
    }

    _connectAnnotationUISync(crossframe) {
      crossframe.on('scrollToAnnotation', tag => {
        for (let anchor of this.anchors) {
          if (anchor.highlights != null && anchor.annotation.$tag === tag) {
            const event = new CustomEvent('scrolltorange', {
              bubbles: true,
              cancelable: true,
              detail: anchor.range,
            });
            if (typeof (this.element[0]) !== 'undefined') {
              const defaultNotPrevented = this.element[0].dispatchEvent(event);
              if (defaultNotPrevented) {
                scrollIntoView(anchor.highlights[0]);
              }
            }
          }
        }
      });

      crossframe.on('getDocumentInfo', cb => this.getDocumentInfo()
        .then(info => cb(null, info))
        .catch(reason => cb(reason))
      );

      crossframe.on('setVisibleHighlights', state => this.setVisibleHighlights(state));

      crossframe.on('LEOS_setVisibleGuideLines', (state, storePrevState) => this.setVisibleGuideLines(state, storePrevState));

      crossframe.on('LEOS_restoreGuideLinesState', () => this.restoreGuideLinesState());
    }

    destroy() {
      $('#annotator-dynamic-style').remove();

      this.selections.unsubscribe();
      this.adder.remove();

      this.element.find('.annotator-hl').each(function () {
        $(this).contents().insertBefore(this);
        return $(this).remove();
      });

      this.element.data('annotator', null);

      for (let name in this.plugins) {
        const plugin = this.plugins[name];
        this.plugins[name].destroy();
      }

      return super.destroy(...arguments);
    }

    anchor(annotation, promiseChain, addToPromiseChain) {
      const self = this;
      const root = this.element[0];

      // Anchors for all annotations are in the `anchors` instance property. These
      // are anchors for this annotation only. After all the targets have been
      // processed these will be appended to the list of anchors known to the
      // instance. Anchors hold an annotation, a target of that annotation, a
      // document range for that target and an Array of highlights.
      const anchors = [];

      // The targets that are already anchored. This function consults this to
      // determine which targets can be left alone.
      const anchoredTargets = [];

      // These are the highlights for existing anchors of this annotation with
      // targets that have since been removed from the annotation. These will
      // be removed by this function.
      let deadHighlights = [];

      // Initialize the target array.
      if (annotation.target == null) { annotation.target = []; }

      const locate = function (target) {
        // Check that the anchor has a TextQuoteSelector -- without a
        // TextQuoteSelector we have no basis on which to verify that we have
        // reanchored correctly and so we shouldn't even try.
        //
        // Returning an anchor without a range will result in this annotation being
        // treated as an orphan (assuming no other targets anchor).
        if (!(target.selector || []).some(s => s.type === 'TextQuoteSelector')) {
          return Promise.resolve({ annotation, target });
        }

        // Find a target using the anchoring module.
        const options = {
          cache: self.anchoringCache,
          ignoreSelector: '[class^="annotator-"]',
        };
        return self.anchoring.anchor(root, target.selector, options)
          .then(range => ({
            annotation,
            target,
            range,
          }))
          .catch(() => ({
            annotation,
            target,
          }));
      };


      // If the original match has been removed, 'dom-anchor-text-quote' returns the next best match, without taking the selector's prefix and suffix into account.
      // This method only compares some characters of the prefix, which is usually good enough. Increasing the number of characters compared or taking the suffix
      // into account are possible improvements.
      const removeRangeIgnoringPrefix = function (anchor) {
        if ((anchor.range == null)) {
          return anchor;
        }

        const anchorLeosSelector = (anchor.target.selector || []).find(s => s.type === 'LeosSelector');
        if ((anchorLeosSelector == null)) {
          return anchor;
        }

        let charactersToCompare = 5;
        const rangeStartContainerText = anchor.range.startContainer.textContent;
        const previousRangeCharacters = rangeStartContainerText.substring(anchor.range.startOffset - charactersToCompare, anchor.range.startOffset);
        if (!previousRangeCharacters || previousRangeCharacters.trim()  === '') {
          return anchor;
        }

        if (previousRangeCharacters.length < charactersToCompare) {
          charactersToCompare = previousRangeCharacters.length;
        }

        const selectorPreviousCharacters = anchorLeosSelector.prefix.substring(anchorLeosSelector.prefix.length - charactersToCompare);

        if (previousRangeCharacters !== selectorPreviousCharacters) {
          anchor.range = null;
        }

        return anchor;
      };

      // Remove the range of a comment anchor if text coverage is lower than a given threshold
      const removeCommentRangeIfNotMatchEnough = function (anchor) {
        if (!(anchor.annotation.tags && anchor.annotation.tags[0] && (anchor.annotation.tags[0] === 'comment'))) {
          return anchor;
        }

        if ((anchor.range == null)) {
          return anchor;
        }

        // If anchor text coverage is lower then threshold (in percent), comment will be moved to orphans
        const coverageThreshold = 66;
        const anchorLeosSelector = (anchor.target.selector != null ? anchor.target.selector : []).find(selector => (selector.type === 'LeosSelector'));

        if ((anchorLeosSelector == null)) {
          return anchor;
        }

        const rangeText = anchor.range.toString();
        const anchorText = anchorLeosSelector.exact;

        // Compare range and anchor text to set new range if they differ
        if (rangeText !== anchorText) {
          const rangeTextWords = rangeText.trim().split(' ');
          const anchorTextWords = anchorText.trim().split(' ');

          // Counter for found matching words
          let matchCounter = 0;
          // End offset of the last found word
          let endOffsetLastWordMatch = 0;

          for (let word of Array.from(anchorTextWords)) {
            const indexOfWord = rangeText.indexOf(word, endOffsetLastWordMatch);
            if (indexOfWord >= endOffsetLastWordMatch) {
              endOffsetLastWordMatch = indexOfWord + word.length;
              matchCounter++;
            }
          }

          const totalMatchInPercent = ((matchCounter / rangeTextWords.length) * 100).toFixed(2);

          // Coverage of anchor and rangeText in percent
          if (totalMatchInPercent >= coverageThreshold) {
            if (anchor.range.endContainer.length < endOffsetLastWordMatch) {
              endOffsetLastWordMatch = 0;
            }
            anchor.range.setEnd(anchor.range.endContainer, endOffsetLastWordMatch);
          }

          if (totalMatchInPercent < coverageThreshold) {
            anchor.range = null;
          }
        }

        return anchor;
      };

      // Mark a suggestion if the "exact" text does not match 100%.
      const markSuggestionRangeWithoutExactTextMatch = function (anchor) {
        if (!(anchor.annotation.tags && anchor.annotation.tags[0] && (anchor.annotation.tags[0] === 'suggestion'))) {
          return anchor;
        }

        if ((anchor.range == null)) {
          return anchor;
        }

        const anchorTextSelector = (anchor.target.selector != null ? anchor.target.selector : []).find(s => s.type === 'TextQuoteSelector');

        if ((anchorTextSelector == null)) {
          return anchor;
        }

        const rangeText = anchor.range.toString();
        const anchorText = anchorTextSelector.exact;

        if (!(anchorText === rangeText)) {
          anchor.annotation.isRangeNotExactMatch = true;
        } else {
          anchor.annotation.isRangeNotExactMatch = false;
        }

        return anchor;
      };

      const addAnchoredRangeText = function(anchor) {
        
        if (!anchor.range) {
          return anchor;
        }

        const rangeText = anchor.range.toString();

        anchor.annotation.anchoredRangeText = rangeText;
        
        return anchor;
      };

      const highlight = function (anchor) {
        // Highlight the range for an anchor.
        if (anchor.range == null) { return anchor; }
        return animationPromise(function () {
          let highlights;
          const range = xpathRange.sniff(anchor.range);
          const normedRange = range.normalize(root);
          //LEOS Change - differentiate between annotation/suggestion and highlight
          if (anchor.annotation.tags && anchor.annotation.tags[0] && (anchor.annotation.tags[0] === 'highlight')) {
            highlights = highlighter.highlightRange(normedRange, `leos-annotator-highlight hl-${annotation.id}`);
          } else {
            highlights = highlighter.highlightRange(normedRange, `annotator-hl hl-${annotation.id}`);
          }
          $(highlights).data('annotation', anchor.annotation);
          anchor.highlights = highlights;

          return anchor;
        });
        
      };

      const sync = function (anchors) {
        // Store the results of anchoring.

        // An annotation is considered to be an orphan if it has at least one
        // target with selectors, and all targets with selectors failed to anchor
        // (i.e. we didn't find it in the page and thus it has no range).
        let hasAnchorableTargets = false;
        let hasAnchoredTargets = false;
        for (let anchor of anchors) {
          if (anchor.target.selector != null) {
            hasAnchorableTargets = true;
            if (anchor.range != null) {
              hasAnchoredTargets = true;
              break;
            }
          }
        }
        annotation.$orphan = hasAnchorableTargets && !hasAnchoredTargets;

        // Add the anchors for this annotation to instance storage.
        self.anchors = self.anchors.concat(anchors);

        // Let plugins know about the new information.
        if (self.plugins.BucketBar != null) {
          self.plugins.BucketBar.update();
        }
        if (self.plugins.CrossFrame != null) {
          self.plugins.CrossFrame.sync([annotation]);
        }
        self.publish('LEOS_annotationsSynced');

        return anchors;
      };

      if (addToPromiseChain) {
        return promiseChain.then(() => this.anchor(annotation, [], false));
      } else {
        // Remove all the anchors for this annotation from the instance storage.
        let anchor;
        for (anchor of self.anchors.splice(0, self.anchors.length)) {
          if (anchor.annotation === annotation) {
            // Anchors are valid as long as they still have a range and their target
            // is still in the list of targets for this annotation.
            if ((anchor.range != null) && annotation.target.includes(anchor.target)) {
              anchors.push(anchor);
              anchoredTargets.push(anchor.target);
            } else if (anchor.highlights != null) {
              // These highlights are no longer valid and should be removed.
              deadHighlights = deadHighlights.concat(anchor.highlights);
              delete anchor.highlights;
              delete anchor.range;
            }
          } else {
            // These can be ignored, so push them back onto the new list.
            self.anchors.push(anchor);
          }
        }

        // Remove all the highlights that have no corresponding target anymore.
        raf(() => highlighter.removeHighlights(deadHighlights));

        // Anchor any targets of this annotation that are not anchored already.
        for (let target of annotation.target) {
          if (!anchoredTargets.includes(target)) {
            anchor = locate(target)
              .then(removeRangeIgnoringPrefix)
              .then(addAnchoredRangeText)
              .then(removeCommentRangeIfNotMatchEnough)
              .then(markSuggestionRangeWithoutExactTextMatch)
              .then(highlight);
            anchors.push(anchor);
          }
        }

        return Promise.all(anchors).then(sync);
      }
    }

    detach(annotation) {
      const anchors = [];
      const targets = [];
      let unhighlight = [];

      for (let anchor of this.anchors) {
        if (anchor.annotation === annotation) {
          unhighlight.push(anchor.highlights || []);
        } else {
          anchors.push(anchor);
        }
      }

      this.anchors = anchors;

      unhighlight = Array.prototype.concat(...(unhighlight || []));
      return raf(() => {
        highlighter.removeHighlights(unhighlight);
        return (this.plugins.BucketBar != null ? this.plugins.BucketBar.update() : undefined);
      });
    }

    createAnnotation(annotation) {
      if (annotation == null) { annotation = {}; }
      const self = this;
      const root = this.element[0];

      const ranges = this.selectedRanges || [];
      this.selectedRanges = null;

      const getSelectors = function (range) {
        const options = {
          cache: self.anchoringCache,
          ignoreSelector: '[class^="annotator-"]',
        };
        // Returns an array of selectors for the passed range.
        return self.anchoring.describe(root, range, options);
      };

      const setDocumentInfo = function (info) {
        annotation.document = info.metadata;
        return annotation.uri = info.uri;
      };

      const setTargets = function (...args) {
        // `selectors` is an array of arrays: each item is an array of selectors
        // identifying a distinct target.
        let info,
          selectors;
        [info, selectors] = args[0];
        const source = info.uri;
        return annotation.target = (selectors.map((selector) => ({ source, selector })));
      };

      const info = this.getDocumentInfo();
      const metadata = info.then(setDocumentInfo);
      const selectors = Promise.all(ranges.map(getSelectors));
      if (annotation.$suggestion) { selectors.then(selectorsValue => self.addSucceedingAndPrecedingText(annotation, ranges[0], selectorsValue[0])); }
      const targets = Promise.all([info, selectors]).then(setTargets);

      targets.then(() => self.publish('beforeAnnotationCreated', [annotation]));
      targets.then(() => self.anchor(annotation, [], false));

      if (!annotation.$highlight) {
        if (this.crossframe != null) {
          this.crossframe.call('showSidebar');
        }
      }
      return annotation;
    }

    addSucceedingAndPrecedingText(annotation, range, selectors) {
      if (!range || !selectors) { return; }
      let textInCommonAncestor = range.commonAncestorContainer.innerText;
      if (!textInCommonAncestor) {
        textInCommonAncestor = range.commonAncestorContainer.parentElement.innerText;
      }
      const leosSelector = selectors.find(s => s.type === 'LeosSelector');
      if (leosSelector) {
        annotation.precedingText = textInCommonAncestor.substring(0, leosSelector.start);
        return annotation.succeedingText = textInCommonAncestor.substring(leosSelector.end);
      }
    }

    createHighlight() {
      return this.createAnnotation({ $highlight: true });
    }

    // Create a blank comment (AKA "page note")
    createComment() {
      const annotation = {};
      const self = this;

      const prepare = function (info) {
        annotation.document = info.metadata;
        annotation.uri = info.uri;
        return annotation.target = [{ source: info.uri }];
      };

      this.getDocumentInfo()
        .then(prepare)
        .then(() => self.publish('beforeAnnotationCreated', [annotation]));

      return annotation;
    }

    // Public: Deletes the annotation by removing the highlight from the DOM.
    // Publishes the 'annotationDeleted' event on completion.
    //
    // annotation - An annotation Object to delete.
    //
    // Returns deleted annotation.
    deleteAnnotation(annotation) {
      if (annotation.highlights != null) {
        for (let h of annotation.highlights) {
          if (h.parentNode != null) {
            $(h).replaceWith(h.childNodes);
          }
        }
      }

      this.publish('annotationDeleted', [annotation]);
      return annotation;
    }

    showAnnotations(annotations) {
      const tags = Array.isArray(annotations) ? annotations.map(el => el.$tag) : [];
      if (this.crossframe != null) {
        this.crossframe.call('showAnnotations', tags);
        this.crossframe.call('showSidebar');
      }
    }

    toggleAnnotationSelection(annotations) {
      const tags = Array.isArray(annotations) ? annotations.map(el => el.$tag) : [];
      if (this.crossframe != null) {
        this.crossframe.call('toggleAnnotationSelection', tags);
      }
    }

    updateAnnotations(annotations) {
      const tags = Array.isArray(annotations) ? annotations.map(el => el.$tag) : [];
      if (this.crossframe != null) {
        this.crossframe.call('updateAnnotations', tags);
      }
    }

    // TODO: See for other strange effects. The "this" in methods call the outmost focusAnnotations (leos-sidebar) that takes 2 parameters
    // but then this is undefined, we want the this.crossframe.call. Why did this work before, CoffeeScript should behave the same.
    callFocusAnnotations(annotations) {
      const tags = Array.isArray(annotations) ? annotations.map(el => el.$tag) : [];
      if (this.crossframe != null) {
        this.crossframe.call('focusAnnotations', tags);
      }
    }

    _getRoot() {
      //Root element is coming from the document plugin
      //LEOS-2789 to evaluate ranges correctly, the reference element 'root' should be parent of 'akamontoso' as xpaths are stored like /akamontoso/.../.../
      if (this.plugins.PDF != null) {
        return this.plugins.PDF.getElement();
      } else if (this.plugins.Document != null) {
        return this.plugins.Document.getElement();
      } else {
        return this.element[0];
      }
    }

    _onSelection(range) {
      const selection = document.getSelection();
      const isBackwards = rangeUtil.isSelectionBackwards(selection);
      const focusRect = rangeUtil.selectionFocusRect(selection);
      if (!focusRect) {
        // The selected range does not contain any text
        this._onClearSelection();
        return;
      }

      this.selectedRanges = [range];

      $('.annotator-toolbar .h-icon-note')
        .attr('title', 'New Annotation')
        .removeClass('h-icon-note')
        .addClass('h-icon-annotate');

      const { left, top, arrowDirection } = this.adderCtrl.target(focusRect, isBackwards);
      this.adderCtrl.showAt(left, top, arrowDirection);
    }

    _onClearSelection() {
      this.adderCtrl.hide();
      this.selectedRanges = [];

      //    LEOS change 3632
      $('.annotator-toolbar .h-icon-annotate')
        .attr('title', 'New Document Note')
        .removeClass('h-icon-annotate')
        .addClass('h-icon-note');
    }

    selectAnnotations(annotations, toggle) {
      if (toggle) {
        return this.toggleAnnotationSelection(annotations);
      } else {
        return this.showAnnotations(annotations);
      }
    }

    onElementClick(event) {
      if (false) { //!@selectedTargets?.length #LEOS Change: Disable auto close of Sidebar
        if (this.crossframe != null) this.crossframe.call('hideSidebar');
      }
    }

    onElementTouchStart(event) {
      // Mobile browsers do not register click events on
      // elements without cursor: pointer. So instead of
      // adding that to every element, we can add the initial
      // touchstart event which is always registered to
      // make up for the lack of click support for all elements.
      if (false) { //!@selectedTargets?.length #LEOS Change: Disable auto close of Sidebar
        if (this.crossframe != null) this.crossframe.call('hideSidebar');
      }
    }

    onHighlightMouseover(event) {
      if (!this.visibleHighlights) { return; }
      const annotation = $(event.currentTarget).data('annotation');
      const annotations = event.annotations != null ? event.annotations : (event.annotations = []);
      annotations.push(annotation);

      // The innermost highlight will execute this.
      // The timeout gives time for the event to bubble, letting any overlapping
      // highlights have time to add their annotations to the list stored on the
      // event object.
      if (event.target === event.currentTarget) {
        setTimeout(() => this.callFocusAnnotations(annotations));
      }
    }

    onHighlightMouseout(event) {
      if (!this.visibleHighlights) { return; }
      this.callFocusAnnotations([]);
    }

    onHighlightClick(event) {
      if (!this.visibleHighlights) { return; }
      const annotation = $(event.currentTarget).data('annotation');
      const annotations = event.annotations != null ? event.annotations : (event.annotations = []);
      annotations.push(annotation);

      // See the comment in onHighlightMouseover
      if (event.target === event.currentTarget) {
        const xor = (event.metaKey || event.ctrlKey);
        setTimeout(() => this.selectAnnotations(annotations, xor));
      }
    }

    // Pass true to show the highlights in the frame or false to disable.
    setVisibleHighlights(shouldShowHighlights) {
      this.toggleHighlightClass(shouldShowHighlights);
    }

    toggleHighlightClass(shouldShowHighlights) {
      if (shouldShowHighlights) {
        this.element.addClass(SHOW_HIGHLIGHTS_CLASS);
      } else {
        this.element.removeClass(SHOW_HIGHLIGHTS_CLASS);
      }

      this.visibleHighlights = shouldShowHighlights;
    }

    //Toggles guidelines visibility, if StoreState = true, saves previous state
    setVisibleGuideLines(shouldShowGuideLines, storePrevState) {
      if (storePrevState) {
        this.visibleGuideLinesPrevState = this.visibleGuideLines;
      }
      this.visibleGuideLines = shouldShowGuideLines;
    }

    restoreGuideLinesState() {
      this.visibleGuideLines = this.visibleGuideLinesPrevState;
    }
  };
  Guest.initClass();
  return Guest;
})());
