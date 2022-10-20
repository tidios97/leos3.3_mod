'use strict';

const annotationMetadata = require('../annotation-metadata');

function hiddenCount(thread) {
  var isHidden = thread.annotation && !thread.visible;
  return thread.children.reduce(function (count, reply) {
    return count + hiddenCount(reply);
  }, isHidden ? 1 : 0);
}

function visibleCount(thread) {
  var isVisible = thread.annotation && thread.visible;
  return thread.children.reduce(function (count, reply) {
    return count + visibleCount(reply);
  }, isVisible ? 1 : 0);
}

function showAllChildren(thread, showFn) {
  thread.children.forEach(function (child) {
    showFn({ thread: child });
    showAllChildren(child, showFn);
  });
}

function showAllParents(thread, showFn) {
  while (thread.parent && thread.parent.annotation) {
    showFn({ thread: thread.parent });
    thread = thread.parent;
  }
}

// @ngInject
function AnnotationThreadController() {
  // Flag that tracks whether the content of the annotation is hovered,
  // excluding any replies.
  this.annotationHovered = false;

  this.checkIsRootSuggestion = function () {
    if (this.isTopLevelThread()) {
      return annotationMetadata.isSuggestion(this.thread.annotation);
    } else {
      return this.isRootSuggestion;
    }
  };

  this.toggleCollapsed = function () {
    this.onChangeCollapsed({
      id: this.thread.id,
      collapsed: !this.thread.collapsed,
    });
  };

  this.threadClasses = function () {
    return {
      'annotation-thread': true,
      'annotation-thread--reply': this.thread.depth > 0,
      'annotation-thread--top-reply': this.thread.depth === 1,
    };
  };

  this.threadToggleClasses = function () {
    return {
      'annotation-thread__collapse-toggle': true,
      'is-open': !this.thread.collapsed,
      'is-hovered': this.annotationHovered,
    };
  };

  this.annotationClasses = function () {
    return {
      annotation: true,
      'annotation--reply': this.thread.depth > 0,
      'is-collapsed': this.thread.collapsed,
      'is-highlighted': this.thread.highlightState === 'highlight',
      'is-dimmed': this.thread.highlightState === 'dim',
    };
  };

  /**
   * Show this thread and any of its children
   */
  this.showThreadAndReplies = function () {
    showAllParents(this.thread, this.onForceVisible);
    this.onForceVisible({ thread: this.thread });
    showAllChildren(this.thread, this.onForceVisible);
  };

  this.isTopLevelThread = function () {
    return !this.thread.parent;
  };

  /**
   * Return the total number of annotations in the current
   * thread which have been hidden because they do not match the current
   * search filter.
   */
  this.hiddenCount = function () {
    return hiddenCount(this.thread);
  };

  this.shouldShowReply = function (child) {
    return visibleCount(child) > 0;
  };

  this.replyInEditingState = false;
  this.annotationInEditingState = false;
  this.oldEditingState = false;

  this.evaluateEditingState = function () {
    const editing = this.replyInEditingState || this.annotationInEditingState;

    if (this.oldEditingState !== editing) {
      this.onAnyAnnotationEditingStateChanged({ editing });
    }

    this.oldEditingState = editing;
  }

  this.onAnnotationEditStateChanged = function (editing) {
    this.annotationInEditingState = editing;
    this.evaluateEditingState();
  }

  this.onReplyEditingStateChanged = function (editing) {
    this.replyInEditingState = editing;
    this.evaluateEditingState();
  }

  this.isChildReplyInEditingState = function () {
    return this.replyInEditingState;
  }
}

/**
 * Renders a thread of annotations.
 */
export = {
  controllerAs: 'vm',
  controller: AnnotationThreadController,
  bindings: {
    /** The annotation thread to render. */
    thread: '<',
    /**
     * Specify whether document information should be shown
     * on annotation cards.
     */
    showDocumentInfo: '<',
    /** Called when the user clicks on the expand/collapse replies toggle. */
    onChangeCollapsed: '&',
    /**
     * Called when the user clicks the button to show this thread or
     * one of its replies.
     */
    onForceVisible: '&',
    /**
     * Indicates whether any child annotation in the hierarchy is in editing
     * mode.
     */
    onAnyAnnotationEditingStateChanged: '&',
    isRootSuggestion: '<',
  },
  template: require('../templates/annotation-thread.html'),
};
