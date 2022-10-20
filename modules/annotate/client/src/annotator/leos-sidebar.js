/*
 * decaffeinate suggestions:
 * DS206: Consider reworking classes to avoid initClass
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
// Do LEOS specific actions in this class
const $ = require('jquery');

const Sidebar = require('./sidebar');
const HostBridgeManager = require('./host-bridge-manager');
const LeosSuggestionSelection = require('./anchoring/suggestion-selection');
const events = require('../sidebar/events');
const LEOS_config = require('../shared/config');
const LEOS_SYSTEM_IDS = require('../shared/system-id');
const OPERATION_MODES = require('../shared/operationMode');
const ANNOT_POPUP_DEFAULT_STATUS = require('../shared/annotationPopupDefaultStatus');
require('./adder');

const {
  FRAME_DEFAULT_WIDTH,
} = LEOS_config;
const {
  FRAME_DEFAULT_MIN_WIDTH,
} = LEOS_config;


module.exports = class LeosSidebar extends Sidebar {

  constructor(element, config) {
    super(...arguments); 

    this.cachedCoordinates = null;
    this.hoveredAnnotations = {};
    this.selectedAnnotations = {};

    const self = this;
    this.container[0].addEventListener('annotationRefresh', () => this._refresh.call(self));
    this.container[0].addEventListener('annotationSidebarResize', () => this._setFramePosition.call(self));
    this.container[0].ownerDocument.defaultView.addEventListener('resize', () => this._onResize.call(self, true));
    this.container[0].addEventListener('scroll', () => this._onScroll.call(self));
    $(window).on('click', event => this._onWindowClick(event, self));
    this.container[0].addEventListener('filterHighlights', event => this.options.Document.filterParentElementId = event.filterParentElementId);

    this._setupCanvas(this.container[0]);

    config.docType = 'LeosDocument';

    this.hostBridgeManager = new HostBridgeManager(this.container[0].hostBridge, this.crossframe);
    this.leosSuggestionSelection =
        new LeosSuggestionSelection(this.options.Document.allowedSelectorTags, this.options.Document.editableSelector, this.options.Document.notAllowedSuggestSelector);
    this.crossframe.on('requestAnnotationAnchor', (annot, callback) => {
      const suggestionPositionSelector = self.leosSuggestionSelection.getSuggestionSelectors(annot, self.anchors);
      if (suggestionPositionSelector != null) {
        return callback(null, suggestionPositionSelector);
      } else {
        return callback('No available highlights');
      }
    });

    this.adderCtrl.extend(this.adder[0], this.container[0].hostBridge, {
      onComment: this.onComment.bind(self),
      onSuggest: this.onSuggest.bind(self),
      onHighlight: this.onHighlight.bind(self),
    });

    this.on('panelReady', () => {
      this._setFrameStyling.call(self, element);
    });

    this.on(events.ANNOTATIONS_LOADED, () => {
      if (eval(localStorage.getItem('shouldAnnotationTabOpen'))) {
        this.onDefaultLoad.call(self);
        localStorage.setItem('shouldAnnotationTabOpen', false);
        this._refreshAnnotationLinkLines(this.visibleGuideLines, self);
      }
    });

    this.on('LEOS_annotationsSynced', () => {
      this._setupAnchorListeners(self);
      this._setupOverlaidAnnotations();
      this._refreshAnnotationLinkLines(this.visibleGuideLines, self, 500);
      this._filterHighlights(self);
    });

    this.crossframe.on('LEOS_cancelFilterHighlights', () => { return self.options.Document.filterParentElementId = null; });
    this.crossframe.on('LEOS_clearSelection', () => self._onClearSelection());
    this.crossframe.on('LEOS_updateIdForCreatedAnnotation',
      (annotationTag, createdAnnotationId) => this._updateIdForCreatedAnnotation(annotationTag, createdAnnotationId, self));
    this.crossframe.on('LEOS_createdAnnotation', (annotationTag, createdAnnotationId) => this._setupOverlaidAnnotations());
    this.crossframe.on('LEOS_refreshAnnotationLinkLines', () => this._refreshAnnotationLinkLines(this.visibleGuideLines, self));
    this.crossframe.on('LEOS_syncCanvasResp', response => this.drawGuideLines(response, self));
    this.crossframe.on('LEOS_selectAnnotation', annotation => this._crossFrameSelectAnnotation(annotation, self));
    this.crossframe.on('LEOS_selectAnnotations', annotations => this._crossFrameSelectAnnotations(annotations, self));
    this.crossframe.on('LEOS_deselectAllAnnotations', () => this._deselectAllAnnotations(self));
    this.crossframe.on('focusAnnotations', tags => { if (tags == null) { tags = []; } return this.focusAnnotations(tags, self); });
    this.crossframe.on('LEOS_refreshAnnotationHighlights', (annotations) => { this._refreshAnnotationHighlights(annotations) });

    this._onResize.call(this, false);

    if (!this.options.Document.showGuideLinesButton) {
      this.plugins.Toolbar.disableGuideLinesBtn();
    }
    if (this.options.Document.annotationPopupDefaultStatus === ANNOT_POPUP_DEFAULT_STATUS.HIDE) {
      this.plugins.Toolbar.disableAnnotPopupBtn();
    }

    this._setToolbarMode.call(this);

  }

  _onSelection(range) {
    if ((this.options.Document.operationMode === OPERATION_MODES.READ_ONLY) || !this.annotationPopupStatus) {
      return;
    }

    if (this._hasElements($(range.endContainer.parentNode).closest('.leos-authnote-table'))) {
      this.adderCtrl.disableAnnotations();
      return;
    } else {
      this.adderCtrl.enableAnnotations();
    }

    const isSuggestionAllowed = this.leosSuggestionSelection.isSelectionAllowedForSuggestion(range.cloneContents(), range.commonAncestorContainer);
    if (isSuggestionAllowed) {
      this.adderCtrl.enableSuggestionButton();
    } else {
      this.adderCtrl.disableSuggestionButton();
    }

    const isHighlightAllowed = this.options.Document.authority !== LEOS_SYSTEM_IDS.ISC;
    if (isHighlightAllowed) {
      this.adderCtrl.addHighlightButton();
    } else {
      this.adderCtrl.removeHighlightButton();
    }
    super._onSelection(...arguments);
  }

  getDocumentInfo() {
    const self = this;
    const getInfoPromise = super.getDocumentInfo(...arguments);
    return getInfoPromise.then(docInfo => self.plugins.Document.getLeosDocumentMetadata()
      .then(leosMetadata => {
        if (leosMetadata != null) {
          docInfo.metadata.metadata = leosMetadata;
        }
        return {
          uri: docInfo.uri,
          metadata: docInfo.metadata,
          frameIdentifier: docInfo.frameIdentifier,
        };
      })
      .catch(() => {
        return {
          uri: docInfo.uri,
          metadata: docInfo.metadata,
          frameIdentifier: docInfo.frameIdentifier,
        };
      }));
  }

  setAllVisibleGuideLines(shouldShowGuideLines, storePrevState) {
    if (this.visibleHighlights) {
      this.crossframe.call('LEOS_setVisibleGuideLines', shouldShowGuideLines, storePrevState);
      this.publish('LEOS_setVisibleGuideLines', shouldShowGuideLines, storePrevState);
      setTimeout((() => this._refreshAnnotationLinkLines(shouldShowGuideLines, this)), 500);
    }
  }

  restoreAllGuideLinesState() {
    this.crossframe.call('LEOS_restoreGuideLinesState');
    this.publish('LEOS_restoreGuideLinesState');
    setTimeout((() => this._refreshAnnotationLinkLinesAndUpdateIcon(this.visibleGuideLines, this)), 500);
  }

  changeAnnotationPopupStatus(shouldShowAnnotationPopup) {
    if (shouldShowAnnotationPopup !== undefined) {
      this.annotationPopupStatus = shouldShowAnnotationPopup;
    } else {
      this.annotationPopupStatus = !this.annotationPopupStatus;
    }
  }

  getAnnotationPopupStatus() {
    return this.annotationPopupStatus;
  }

  show() {
    super.show(...arguments);
    if ((this.frame.width() !== this.frameCurrentWidth) && (this.frame.width() !== 0)) {
      this.frameCurrentWidth = this.frame.width();
    }
    this.frame.css({'margin-left': `${-1 * this.frameCurrentWidth}px`});
    this.frame.css({'width': `${this.frameCurrentWidth}px`});
    if (this.visibleHighlights) {
      this.restoreAllGuideLinesState();
    }
  }

  hide() {
    super.hide(...arguments);
    this._clearAnnotationLinkLines();
    if ((this.frameCurrentWidth != null) && (this.frame.width() !== 0)) {
      this.frameCurrentWidth = this.frame.width();
    } else {
      this.frameCurrentWidth =
          this._isMilestoneExplorer() ?
            FRAME_DEFAULT_WIDTH - 150
            :
            FRAME_DEFAULT_WIDTH;
    }
    this.frame.css({'width': '0px'});
  }

  destroy() {
    super.destroy(...arguments);
    this._cleanup.call(this);
  }


  // Copied methods
  onSuggest() {
    this._onResize.call(this, true);
    const range = this.selectedRanges[0];
    if (range != null) {
      const annotationText = this.leosSuggestionSelection.extractSuggestionTextFromSelection(range.cloneContents());
  
      this.createAnnotation({$suggestion: true, tags: ['suggestion'], text: annotationText});
    }
    document.getSelection().removeAllRanges();
  }
  
  onComment() {
    this._onResize.call(this, true);
    this.createAnnotation({$comment: true, tags: ['comment']});
    document.getSelection().removeAllRanges();
  }
  
  onHighlight() {
    this._onResize.call(this, true);
    this.setVisibleHighlights(true);
    this.createAnnotation({$highlight: true, tags: ['highlight']});
    document.getSelection().removeAllRanges();
  }
  
  onDefaultLoad() {
    this.showAnnotations({tags: ['comment','suggestion','highlight']});
  }

  focusAnnotations(tags, scope) {
    for (let anchor of scope.anchors) {
      if (anchor.highlights != null) {
        const toggle = tags.includes(anchor.annotation.$tag);
        $(anchor.highlights).toggleClass('annotator-hl-focused', toggle);
        scope.hoveredAnnotations[anchor.annotation.$tag] = toggle;
      }
    }
    this.drawGuideLines(null, scope);
  }

  //NOTE If coordinates = null, cached values are used. Useful when redraw is needed but coordinates are sure not to have changed their values
  drawGuideLines(coordinates, scope) {
    if (coordinates !== null) {
      this.cachedCoordinates = coordinates;
    }
    // Check whether this is set as tests fail otherwise.
    if (this && (this.cachedCoordinates === null) || !scope.visibleGuideLines) {
      return;
    }
    this._clearAnnotationLinkLines();
    const leosCanvas = document.getElementById('leosCanvas');
    if (leosCanvas) {
      const context = leosCanvas.getContext('2d');
      const hypothesisIFrameOffset = this._getHypothesisFrame().offsetParent;
      const endOfPageHorzCoords = this._getEndOfPageHorzCoordinates();
      const annLineDrawControl = {}; //control object used to limit to one guide line annotations composed by more than one lines of text

      //    get all highlight anchors
      const highlights = $('hypothesis-highlight');
      if (highlights && (highlights.length > 0)) {
        for (let highlight of highlights) {
          const annotation = $(highlight).data('annotation');
          if (!annLineDrawControl[annotation.$tag]) {
            annLineDrawControl[annotation.$tag] = true;
            this._configCanvasContextForAnnotation(context, highlight, scope);
            //        select the correct annotation that relates to the current highlight in the loop
            const annotationCoordinate = this._getAnnotationCoordinate(this.cachedCoordinates, annotation.id);
            if (annotationCoordinate) {
              const docViewportTopLimit = hypothesisIFrameOffset.offsetTop;
              const footer = document.querySelector('.leos-footer');
              const docViewportBottomLimit = (document.documentElement.clientHeight || window.innerHeight) - (footer ? footer.offsetHeight : 0);
              const highlightRect = highlight.getBoundingClientRect();
              const anchorEndpointTop = annotationCoordinate.y + hypothesisIFrameOffset.offsetTop;
              //          highlight is visible on the viewport
              const anchorInViewport = (highlightRect.top >= (docViewportTopLimit + 5)) &&
                                 (highlightRect.top <= docViewportBottomLimit) &&
                                 (highlightRect.right <= endOfPageHorzCoords);
              //          the annotation on the sidebar is on the viewport
              //          Note: following hardcoded values are hypothesis element dims that cannot be retrieved by document query due to crossDomain issues
              const anchorEndpointInViewport = (anchorEndpointTop >= (docViewportTopLimit + 40)) &&
                                         (anchorEndpointTop <= (docViewportBottomLimit - 15));
              if (anchorInViewport && anchorEndpointInViewport) {
                const fromLeft = highlightRect.right - 5;
                const fromTop = highlightRect.top;
                const toLeft = annotationCoordinate.x - 10;
                this._drawGuidLine(context, endOfPageHorzCoords, fromLeft, fromTop, toLeft, anchorEndpointTop);
              }
            }
          }
        }
      }
    }
  }
  _refreshOrDrawGuideLines(selectFromDocument, scope) {
    if (selectFromDocument) {
      this._refreshAnnotationLinkLines(scope.visibleGuideLines, scope, 500);
    } else {
      this.drawGuideLines(null, scope);
    }
  }
  _hasElements (list) { return (list != null) && (list.length > 0); }

  _setFrameStyling () {
  //Set Sidebar specific styling to stick to the container
    this.frame.addClass('leos-sidebar');
    this._setFramePosition.call(this);
  }

  _setFramePosition () {
    const position = this.element[0].getBoundingClientRect();
    this.frame[0].style.top = position.top + 'px';
    this.frame[0].style.height = position.height + 'px';
    this.frame[0].style.left = this._isScrollbarVisible ? (position.right - 18) + 'px' : position.right + 'px';
  }

  _isMilestoneExplorer () { return $('#milestonedocContainer').get(0) !== undefined; }

  _getFrameWidth (sidebarWidth, currentWidth) {
    if (this._isMilestoneExplorer()) {
      return currentWidth;
    } else if (sidebarWidth > FRAME_DEFAULT_WIDTH) {
      return FRAME_DEFAULT_WIDTH;
    } else if (sidebarWidth < FRAME_DEFAULT_MIN_WIDTH) {
      return FRAME_DEFAULT_MIN_WIDTH;
    } else {
      return sidebarWidth;
    }
  }

  _onResize (redraw) {
    const collapsed = this.crossframe.annotator.frame.hasClass('annotator-collapsed');
    const frameWidth = this.element[0].ownerDocument.defaultView.outerWidth;
    const sidebarWidth = (frameWidth * Math.exp(((frameWidth + 70) * 4.6) / 1920)) / 100;
    this.frameCurrentWidth = this._getFrameWidth(sidebarWidth, this.frameCurrentWidth);
    if (redraw && !collapsed) {
      this.frame.css({'margin-left': `${-1 * this.frameCurrentWidth}px`});
      this.frame.css({'width': `${this.frameCurrentWidth}px`});
    }
    this._setupCanvas(this.container[0]);
    this._refreshAnnotationLinkLines(this.visibleGuideLines, this);
  }

  _isScrollbarVisible () {
    return this.container[0].scrollHeight > this.container[0].clientHeight;
  }

  // LEOS-4588 while on read only operation mode, annotation and new note buttons should be disabled (on milestones explorer and editing toc)
  _setToolbarMode () {
    if (this.options.Document.operationMode === OPERATION_MODES.READ_ONLY) {
      this.plugins.Toolbar.disableAnnotPopupBtn();
      this.plugins.Toolbar.disableNewNoteBtn();
    } else {
      this.plugins.Toolbar.enableAnnotPopupBtn();
      this.plugins.Toolbar.enableNewNoteBtn();
    }
  }

  _refresh () {
    const self = this;
    this.options.Document.operationMode = event.operationMode; //LEOS-3796
    this._setToolbarMode.call(this);
    if (this.crossframe != null) {
      this.crossframe.call('LEOS_changeOperationMode', event.operationMode);
    }
    // A list of annotations that need to be refreshed.
    const refreshAnnotations = new Set;
    // Find all the anchors that have been invalidated by page state changes.
    // Would be better to loop on all existing annotations, but didn't find any other way to access annotations from sidebar
    if (this.anchors != null) {
      let anchor;
      for (anchor of this.anchors) {
      // The annotations for these anchors need to be refreshed.
      // Remove highlights in the DOM
        if (anchor.highlights != null) {
          for (let h of anchor.highlights) {
            if (h.parentNode != null) {
              $(h).replaceWith(h.childNodes);
            }
          }
          delete anchor.highlights;
          delete anchor.range;
          // Add annotation to be anchored again
          refreshAnnotations.add(anchor.annotation);
        }
      }

      refreshAnnotations.forEach(function(annotation) {
        this.anchor(annotation);
      }
      , self);
    }

    self.setVisibleHighlights(true);
    if (self.crossframe != null) self.crossframe.call('reloadAnnotations');
  }

  _setupCanvas (container) {
    const canvasWidth = container.ownerDocument.defaultView.innerWidth;
    const canvasHeight = container.ownerDocument.defaultView.innerHeight;
    let leosCanvas = document.getElementById('leosCanvas');
    if(!leosCanvas) {
      const canvasElem = '<canvas id="leosCanvas" class="leos-guideline-canvas" width='+canvasWidth+' height='+canvasHeight+'></canvas>';
      $(canvasElem).insertBefore(container);
      leosCanvas = document.getElementById('leosCanvas');
    } else {
      leosCanvas.width = canvasWidth;
      leosCanvas.height = canvasHeight;
    }

    if (leosCanvas) {
      const context = leosCanvas.getContext('2d');
      context.setLineDash([5,5]);
      context.lineWidth='3';
      context.strokeStyle='#FDD7DF'; //default color
    }
  }

  _setupAnchorListeners (scope) {
    const highlights = document.getElementsByTagName('hypothesis-highlight');
    if (highlights) {
      for (let highlight of highlights) {
        if (!$(highlight).data('has-listeners-set') && (highlight.lastElementChild === null)) {
          highlight.addEventListener('mouseover', event => this._onHighlightFocus(event, true, scope));
          highlight.addEventListener('mouseout', event => this._onHighlightFocus(event, false, scope));
          $(highlight).data('has-listeners-set', true);
        }
      }
    }
  }

  _setupOverlaidAnnotations() {
    const highlights = document.getElementsByTagName('hypothesis-highlight');
    if (highlights) {
      for (let highlight of highlights) {
        const annotation = $(highlight).data('annotation');
        const previousSiblingAnnotation = $(highlight.parentNode.previousElementSibling).data('annotation');
        const nextSiblingAnnotation = $(highlight.parentNode.nextElementSibling).data('annotation');
        if (annotation && annotation.id && ((previousSiblingAnnotation && (annotation.id === previousSiblingAnnotation.id)) || (nextSiblingAnnotation && (annotation.id === nextSiblingAnnotation.id)))) {
          $(highlight).addClass('no-pointer-events');
        }
      }
    }
  }

  _onHighlightFocus (event, isMouseEnter, scope) {
    const annotation = $(event.target).data('annotation');
    if (annotation) {
      const anchor = this._getAnchorFromAnnotationTag(annotation.$tag, scope);
      if (anchor) {
        $(anchor.highlights).toggleClass('annotator-hl-focused', isMouseEnter);
        scope.hoveredAnnotations[annotation.$tag] = isMouseEnter;
        this.drawGuideLines(null, scope);
      }
    }
  }

  _refreshAnnotationLinkLinesAndUpdateIcon (visibleGuideLines, scope, delayResp) {
    this._refreshAnnotationLinkLines(visibleGuideLines, scope, delayResp);
    scope.publish('LEOS_setVisibleGuideLines', visibleGuideLines, false);
  }


  _refreshAnnotationLinkLines (visibleGuideLines, scope, delayResp) {
    if (visibleGuideLines && scope) {
      const hypothesisIFrameOffset = this._getHypothesisFrame().offsetParent;
      scope.crossframe.call('LEOS_syncCanvas', hypothesisIFrameOffset.offsetLeft, delayResp);
    } else {
      this._clearAnnotationLinkLines();
    }
  }

  //   select annotation from Anchor
  _onWindowClick (event, scope) { this._toggleAnnotationSelectedState($(event.target).data('annotation'), true, scope); }

  //   select annotation from Annotation on sidebar
  _crossFrameSelectAnnotation (annotation, scope) { this._toggleAnnotationSelectedState(annotation, false, scope); }

  //   select multiple annotations from Annotation on sidebar
  _crossFrameSelectAnnotations(annotations, scope) { annotations.map((annotation) =>
    this._changeAnnotationSelectedState(annotation, false, scope, true)); }


  _toggleAnnotationSelectedState (annotation, selectFromDocument, scope) {

    const targetSelectionState = annotation && !scope.selectedAnnotations[annotation.$tag];

    this._changeAnnotationSelectedState(annotation, selectFromDocument, scope, targetSelectionState);
  }

  //   selectFromDocument -> true : click was done on Anchor
  //   selectFromDocument -> false : click was done on Annotation on sidebar
  _changeAnnotationSelectedState (annotation, selectFromDocument, scope, isSelectedTargetState) {
    //      if anchor not selected -> select anchor, drawGuideLines
    //      if anchor selected -> de-select anchor, drawGuideLines, clear filtering
    //      if click outside annotations -> de-select all anchors, drawGuideLines, clear filtering

    if (!annotation) {
      this._clearSelectedAnnotations(scope);
      this._sendClearSelectedAnnotationsToSidebar(scope, true, null);
      this._refreshOrDrawGuideLines(selectFromDocument, scope);
      return;
    }

    if (this._isPageNote(annotation) || this._isOrphan(annotation)) {
      this._clearSelectedAnnotations(scope);
      this._refreshOrDrawGuideLines(selectFromDocument, scope);
      return;
    }

    if (isSelectedTargetState && selectFromDocument) {  //If select on anchor - select only one anchor and filter by it
      this._sendClearSelectedAnnotationsToSidebar(scope, false, annotation.id);
    }

    this._setAnnotationSelectedState(annotation, scope, isSelectedTargetState);

    if (!isSelectedTargetState && selectFromDocument) { //If anchor de-selected - deselect only one anchor
      this._sendClearSelectedAnnotationsToSidebar(scope, true, annotation.id);
    }

    this._refreshOrDrawGuideLines(selectFromDocument, scope);
  }


  _setAnnotationSelectedState (annotation, scope, isSelectedTargetState) {
    scope.selectedAnnotations[annotation.$tag] = isSelectedTargetState;
    const anchor = this._getAnchorFromAnnotationTag(annotation.$tag, scope);
    $(anchor.highlights).toggleClass('annotator-hl-selected', isSelectedTargetState);
    $(anchor.highlights).find('.annotator-hl').toggleClass('transparent-bg', isSelectedTargetState);
  }


  _clearSelectedAnnotations (scope) {
    scope.selectedAnnotations = {};
    if ($('hypothesis-highlight') !== null) {
      $('hypothesis-highlight').toggleClass('annotator-hl-selected', false);
      $('hypothesis-highlight').find('.annotator-hl').toggleClass('transparent-bg', false);
    }
  }

  _sendClearSelectedAnnotationsToSidebar (scope, useTimeout, annotationIdToSelectInstead) {
    if (useTimeout && scope.crossframe != null) {
      setTimeout(() => scope.crossframe.call('LEOS_clearSelectedAnnotations', annotationIdToSelectInstead), 500);
    } else if (scope.crossframe != null) {
      scope.crossframe.call('LEOS_clearSelectedAnnotations', annotationIdToSelectInstead);
    }
  }

  _deselectAllAnnotations (scope) {
    this._clearSelectedAnnotations(scope);
    this._refreshOrDrawGuideLines(false, scope);
  }

  _getAnchorFromAnnotationTag (tag, scope) {
    for (let anchor of scope.anchors) {
      if (anchor.highlights != null && anchor.annotation.$tag === tag) {
        return anchor;
      }
    }
  }

  _isOrphan (annotation) { return this._hasSelector(annotation) && annotation.$orphan; }

  _isPageNote (annotation) { return !this._hasSelector(annotation) && !this._isReply(annotation); }

  _hasSelector (annotation) { return !!(annotation.target && (annotation.target.length > 0) && annotation.target[0].selector); }

  _isReply (annotation) { return (annotation.references || []).length > 0; }

  _configCanvasContextForAnnotation (canvasContext, highlight, scope) {
    let lineColor;
    const annotation = $(highlight).data('annotation');
    //    Config line color
    if (lineColor = $(highlight).css( 'background-color' )) {
      canvasContext.strokeStyle = lineColor;
    }

    //    Config line type: dashed for regular, solid for focused
    if (scope.selectedAnnotations[annotation.$tag]) {
      canvasContext.setLineDash([]);
      canvasContext.strokeStyle = LEOS_config.LEOS_SELECTED_ANNOTATION_COLOR;
    } else if (scope.hoveredAnnotations[annotation.$tag]) {
      canvasContext.setLineDash([]);
      canvasContext.strokeStyle = LEOS_config.LEOS_HOVER_ANNOTATION_COLOR;
    } else { //Dashed line
      canvasContext.setLineDash([5,5]);
    }
  }

  _drawGuidLine(canvasContext, endOfPageHorzCoords, fromLeft, fromTop, toLeft, toTop) {
    canvasContext.beginPath();
    canvasContext.moveTo(fromLeft, fromTop);
    canvasContext.lineTo(fromLeft, fromTop - 5);
    canvasContext.lineTo(endOfPageHorzCoords, fromTop - 5);
    canvasContext.lineTo(toLeft, toTop);
    canvasContext.stroke();
  }

  _getAnnotationCoordinate(coordinates, id) {
    if (coordinates && id) {
      for (let annotationCoordinate of coordinates) {
        if (annotationCoordinate.id === id) {
          return annotationCoordinate;
        }
      }
    }
  }

  _getHypothesisFrame () {
    return $("iframe[name='hyp_sidebar_frame']")[0];
  }

  _getEndOfPageHorzCoordinates() {
  //TOC width plus the document page width
    const LEOSTocPanelWidth = $('.v-splitpanel-first-container').outerWidth();
    const ISCTocPanelWidth = $('.renditionTocContent').outerWidth();
    const endOfDocCoordinates = ( LEOSTocPanelWidth || ISCTocPanelWidth || 0) + ($('doc :first-child').outerWidth() || $('bill :first-child').outerWidth());
    const annotFrameLeftOffset = this._getHypothesisFrame().offsetParent.offsetLeft - 20;
    return Math.min(endOfDocCoordinates, annotFrameLeftOffset);
  }

  _clearAnnotationLinkLines() {
    const leosCanvas = document.getElementById('leosCanvas');
    if (leosCanvas != null) {
      const ctx = leosCanvas.getContext('2d');
      ctx.clearRect(0, 0, leosCanvas.width, leosCanvas.height);
      // TODO: Removed this on CoffeeScript -> JS migration. Check this vs scopes parameter.
      // if (this) {
      //   this.hoveredAnnotations = {};
      //   this.selectedAnnotations = {};
      // }
    }
  }


  // Method called for CREATE, UPDATE and DELETE.
  // createdAnnotationId is only != null on CREATE
  _updateIdForCreatedAnnotation (annotationTag, createdAnnotationId, scope) {
    const highlights = $('hypothesis-highlight');
    if (createdAnnotationId && highlights && (highlights.length > 0)) {
      for (let highlight of highlights) {
        const annotation = $(highlight).data('annotation');
        if (annotation.$tag === annotationTag) {
          annotation.id = createdAnnotationId;
        }
      }
      this._refreshAnnotationLinkLines(scope.visibleGuideLines, scope);
    }
  }

  _refreshAnnotationHighlights(annotations) {
    if (!annotations || annotations.length == 0) {
        return;
    }

    const hypothesisHighlights = Array.from(document.querySelectorAll(`hypothesis-highlight`));
    if (hypothesisHighlights.length == 0) {
        return;
    }

    const toggleAnnotationHighlight = (element, highlightTag, showHighlight) => {
        if (showHighlight) {
            $(element).addClass(highlightTag);
        } else {
            $(element).removeClass(highlightTag);
        }
    }

    const toggleAnnotationHighlights = (annotation, hypothesisHighlights) => {
        if (!annotation) {
            return;
        }

        let showHighlight = (annotation.showHighlight === undefined) ? true : annotation.showHighlight;
        let isLeosHighlight = annotation.tags && annotation.tags[0] && (annotation.tags[0] === 'highlight');
        let highlightTag = isLeosHighlight ? 'leos-annotator-highlight' : 'annotator-hl';
        hypothesisHighlights.filter(highlightEl => highlightEl.classList.contains(`hl-${annotation.id}`))
            .filter(annotationHighlightEl => showHighlight ? !annotationHighlightEl.classList.contains(highlightTag) : annotationHighlightEl.classList.contains(highlightTag))
            .forEach(annotationHighlightEl => { toggleAnnotationHighlight(annotationHighlightEl, highlightTag, showHighlight) });
    }

    annotations.forEach((annotation) => { toggleAnnotationHighlights(annotation, hypothesisHighlights) });
  }

  //    refresh lines for all three events

  _onScroll() {
    this._refreshAnnotationLinkLines(this.visibleGuideLines, this);
  }

  _filterHighlights(scope) {
    if (scope.options.Document.filterParentElementId) {
      const highlights = $(`#${scope.options.Document.filterParentElementId}`).find('hypothesis-highlight');
      if (highlights && (highlights.length > 0)) {
        for (let h of highlights) {
          if (h.parentNode != null) {
            $(h).replaceWith(h.childNodes);
          }
        }
      }
    }
  }

  _cleanup() {
  // TODO : Temporary solution to cleanup references to annotation scripts
    const quote = regex => regex.replace(/([()[{*+.$^\\|?\/])/g, '\\$1');

    const annotateScripts = document.querySelectorAll(`script[src*=${quote(this.contextRoot)}]`);
    annotateScripts.forEach(annotateScript => annotateScript.parentNode.removeChild(annotateScript));

    const annotateLinks = document.querySelectorAll(`link[href*=${quote(this.contextRoot)}]`);
    annotateLinks.forEach(annotateLink => annotateLink.parentNode.removeChild(annotateLink));

    const configScripts = document.querySelectorAll('script.js-hypothesis-config');
    configScripts.forEach(configScript => configScript.parentNode.removeChild(configScript));
  }

};