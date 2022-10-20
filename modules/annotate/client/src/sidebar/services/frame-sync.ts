'use strict';

var debounce = require('lodash.debounce');

var events = require('../events');
var bridgeEvents = require('../../shared/bridge-events');
var metadata = require('../annotation-metadata');
var uiConstants = require('../ui-constants');

/**
 * @typedef FrameInfo
 * @property {string} uri - Current primary URI of the document being displayed
 * @property {string[]} searchUris - List of URIs that should be passed to the
 *           search API when searching for annotations on this document.
 * @property {string} documentFingerprint - Fingerprint of the document, used
 *                    for PDFs
 */

/**
 * Return a minimal representation of an annotation that can be sent from the
 * sidebar app to a connected frame.
 *
 * Because this representation will be exposed to untrusted third-party
 * JavaScript, it includes only the information needed to uniquely identify it
 * within the current session and anchor it in the document.
 */
function formatAnnot(ann) {
  return {
    tag: ann.$tag,
    msg: {
      document: ann.document,
      target: ann.target,
      uri: ann.uri,
      tags: ann.tags,  //LEOS change - be able to assign different class for highlight and comments/suggestions
      id: ann.id, //LEOS change - associate highlight with annotation for canvas line draw
    },
  };
}

/**
 * This service runs in the sidebar and is responsible for keeping the set of
 * annotations displayed in connected frames in sync with the set shown in the
 * sidebar.
 */
// @ngInject
function FrameSync($rootScope, $window, Discovery, store, bridge, rootThread) {

  // Set of tags of annotations that are currently loaded into the frame
  var inFrame = new Set();

  /**
   * Watch for changes to the set of annotations displayed in the sidebar and
   * notify connected frames about new/updated/deleted annotations.
   */
  function setupSyncToFrame() {
    // List of loaded annotations in previous state
    var prevAnnotations = [];
    var prevFrames = [];
    var prevPublicAnns = 0;

    store.subscribe(function () {
      var state = store.getState();
      if (state.annotations === prevAnnotations &&
          state.frames === prevFrames) {
        return;
      }

      var publicAnns = 0;
      var inSidebar = new Set();
      var added = [];

      state.annotations.forEach(function (annot) {
        if (metadata.isReply(annot)) {
          // The frame does not need to know about replies
          return;
        }

        if (metadata.isPublic(annot)) {
          ++publicAnns;
        }

        inSidebar.add(annot.$tag);
        if (!inFrame.has(annot.$tag)) {
          added.push(annot);
        }
      });
      var deleted = prevAnnotations.filter(function (annot) {
        return !inSidebar.has(annot.$tag) && !metadata.isReply(annot);
      });
      prevAnnotations = state.annotations;
      prevFrames = state.frames;

      // We currently only handle adding and removing annotations from the frame
      // when they are added or removed in the sidebar, but not re-anchoring
      // annotations if their selectors are updated.
      if (added.length > 0) {
        bridge.call('loadAnnotations', added.map(formatAnnot));
        added.forEach(function (annot) {
          inFrame.add(annot.$tag);
        });
      }
      deleted.forEach(function (annot) {
        bridge.call('deleteAnnotation', formatAnnot(annot));
        inFrame.delete(annot.$tag);
      });
      if (deleted.length > 0) {
        $rootScope.$broadcast('refreshAnnotations');
      }

      var frames = store.frames();
      if (frames.length > 0) {
        if (frames.every(function (frame) { return frame.isAnnotationFetchComplete; })) {
          if (publicAnns === 0 || publicAnns !== prevPublicAnns) {
            bridge.call(bridgeEvents.PUBLIC_ANNOTATION_COUNT_CHANGED, publicAnns);
            prevPublicAnns = publicAnns;
          }
        }
      }
    });
  }

  /**
   * Listen for messages coming in from connected frames and add new annotations
   * to the sidebar.
   */
  function setupSyncFromFrame() {
    // A new annotation, note or highlight was created in the frame
    bridge.on('beforeCreateAnnotation', function (event) {
      inFrame.add(event.tag);
      var annot = Object.assign({}, event.msg, {$tag: event.tag});
      $rootScope.$broadcast(events.BEFORE_ANNOTATION_CREATED, annot);
    });

    bridge.on('destroyFrame', destroyFrame.bind(this));

    // Map of annotation tag to anchoring status
    // {
    //   anchoringStatus: 'anchored'|'orphan'|'timeout',
    //   anchoredRangeText: "The text in the document, that corresponds to the
    //                        anchor position"
    // }
    //
    // Updates are coalesced to reduce the overhead from processing
    // triggered by each `UPDATE_ANCHOR_STATUS` action that is dispatched.
    var anchoringStatusUpdates = {};
    var scheduleAnchoringStatusUpdate = debounce(() => {
      store.updateAnchorStatus(anchoringStatusUpdates);
      $rootScope.$broadcast(events.ANNOTATIONS_SYNCED, Object.keys(anchoringStatusUpdates));
      anchoringStatusUpdates = {};
    }, 10);

    // Anchoring an annotation in the frame completed
    bridge.on('sync', function (events_) {
      events_.forEach(function (event) {
        inFrame.add(event.tag);
        anchoringStatusUpdates[event.tag] = {
          anchoringStatus: event.msg.$orphan ? 'orphan' : 'anchored',
          anchoredRangeText: event.msg.anchoredRangeText
        };
        scheduleAnchoringStatusUpdate();
      });
    });

    bridge.on('showAnnotations', function (tags) {
      store.selectTab(uiConstants.TAB_ANNOTATIONS);
      if (tags.length) {
        $rootScope.$broadcast(events.SCROLL_ANNOTATION_INTO_VIEW, tags[0]);
      }
      bridge.call('LEOS_refreshAnnotationLinkLines'); //#LEOS Change
    });

    bridge.on('focusAnnotations', function (tags) {
      store.focusAnnotations(tags || []);
    });

    bridge.on('toggleAnnotationSelection', function (tags) {
      store.toggleSelectedAnnotations(store.findIDsForTags(tags));
    });

    bridge.on('sidebarOpened', function () {
      $rootScope.$broadcast('sidebarOpened');
    });

    // These invoke the matching methods by name on the Guests
    bridge.on('showSidebar', function () {
      bridge.call('showSidebar');
    });
    bridge.on('hideSidebar', function () {
      bridge.call('hideSidebar');
    });
    bridge.on('setVisibleHighlights', function (state) {
      bridge.call('setVisibleHighlights', state);
    });

  }

  /**
   * Query the Hypothesis annotation client in a frame for the URL and metadata
   * of the document that is currently loaded and add the result to the set of
   * connected frames.
   */
  function addFrame(channel) {
    channel.call('getDocumentInfo', function (err, info) {
      if (err) {
        channel.destroy();
        return;
      }

      $rootScope.$broadcast(events.FRAME_CONNECTED);
      store.connectFrame({
        id: info.frameIdentifier,
        metadata: info.metadata,
        uri: info.uri,
      });
    });
  }

  function destroyFrame(frameIdentifier) {
    var frames = store.frames();
    var frameToDestroy = frames.find(function (frame) {
      return frame.id === frameIdentifier;
    });
    if (frameToDestroy) {
      store.destroyFrame(frameToDestroy);
    }
  }

  /**
   * Find and connect to Hypothesis clients in the current window.
   */
  this.connect = function () {
    var discovery = new Discovery(window, {server: true});
    discovery.startDiscovery(bridge.createChannel.bind(bridge));
    bridge.onConnect(addFrame);

    setupSyncToFrame();
    setupSyncFromFrame();

    bridge.on('stateChangeHandler', function (state) {
      store.hostState = state;
      if (store.hostState === 'OPEN') {
        bridge.call('LEOS_clearSelection');
      } else {
        bridge.call('LEOS_cancelFilterHighlights');
      }
    });

    bridge.on('reloadAnnotations', function () {
      $rootScope.$broadcast('reloadAnnotations');
    });

    bridge.on('LEOS_requestFilteredAnnotations', function () {
      $rootScope.$broadcast('LEOS_requestFilteredAnnotations');
    });

    bridge.on('LEOS_responseFilteredAnnotations', function (annotations) {
      bridge.call('LEOS_responseFilteredAnnotations', annotations);
    });

    bridge.on('LEOS_clearSelectedAnnotations', annotationIdToSelectInstead => {
      if (annotationIdToSelectInstead) {
        const thread = rootThread.thread(store.getState());
        if (thread.children.some(child => child.id === annotationIdToSelectInstead)) {
          store.toggleSelectedAnnotations([annotationIdToSelectInstead]);
        } else {
          // Ignore selection of annotations from document that are currently not shown in the sidebar, e.g. when a search filter is applied.
          // Therefore, deselect the annotation clicked on again in the document.
          const annot = store.findAnnotationByID(annotationIdToSelectInstead);
          this.LEOS_selectAnnotation(annot);
        }
      } else {
        store.deselectAllAnnotations();
      }
    });

    bridge.on('LEOS_changeOperationMode', function (operationMode) {
      store.operationMode = operationMode;
    });

    bridge.on('LEOS_syncCanvas', function (iFrameOffsetLeft, delayResp) {
      $rootScope.$broadcast('LEOS_syncCanvas', iFrameOffsetLeft, delayResp);
    });

    bridge.on('LEOS_syncCanvasResp', function () {
      bridge.call('LEOS_syncCanvasResp');
    });

    bridge.on('LEOS_setVisibleGuideLines', function (state, storePrevState) {
      bridge.call('LEOS_setVisibleGuideLines', state, storePrevState);
    });

    bridge.on('LEOS_restoreGuideLinesState', function () {
      bridge.call('LEOS_restoreGuideLinesState');
    });

    bridge.on('LEOS_updateIdForCreatedAnnotation', function (annotationTag, createdAnnotationId) {
      bridge.call('LEOS_updateIdForCreatedAnnotation', annotationTag, createdAnnotationId);
    });

    bridge.on('LEOS_refreshAnnotationLinkLines', function () {
      bridge.call('LEOS_refreshAnnotationLinkLines');
    });
  };

  /**
   * Focus annotations with the given tags.
   *
   * This is used to indicate the highlight in the document that corresponds to
   * a given annotation in the sidebar.
   *
   * @param {string[]} tags
   */
  this.focusAnnotations = function (tags) {
    bridge.call('focusAnnotations', tags);
  };

  /**
   * Scroll the frame to the highlight for an annotation with a given tag.
   *
   * @param {string} tag
   */
  this.scrollToAnnotation = function (tag) {
    bridge.call('scrollToAnnotation', tag);
  };

  /**
   * LEOS
   *
   * Selects the clicked annotation
   */
  this.LEOS_selectAnnotation = function (annotation) {
    bridge.call('LEOS_selectAnnotation', annotation);
  };

  this.LEOS_selectAnnotations = function (annotations) {
    bridge.call('LEOS_selectAnnotations', annotations);
  };

  this.deselectAllAnnotations = function () {
    bridge.call('LEOS_deselectAllAnnotations');
  };

  this.refreshAnnotationLinks = function () {
    bridge.call('LEOS_refreshAnnotationLinkLines');
  };
}

export = {
  default: FrameSync,
  formatAnnot: formatAnnot,
};
