'use strict';

const UI_CONSTANTS = require('../ui-constants');

const annotationMetadata = require('../annotation-metadata');
const authorityChecker = require('../authority-checker');
const authorizer = require('../authorizer');

// @ngInject
function SelectionPaneController($window, analytics, annotationMapper, flash, frameSync, permissions, session, settings, store, suggestionMerger) {
  const self = this;

  self.TAB_ANNOTATIONS = UI_CONSTANTS.TAB_ANNOTATIONS;
  self.TAB_NOTES = UI_CONSTANTS.TAB_NOTES;
  self.TAB_ORPHANS = UI_CONSTANTS.TAB_ORPHANS;

  self.isActionPending = false;

  self.accept = function() {
    const selectedSuggestions = getMergableSelectedAnnotations();
    const selectedSuggestionsCount = selectedSuggestions.length;
    let message = 'Are you sure you want to accept the ' + selectedSuggestionsCount + ' selected suggestion';
    if (selectedSuggestionsCount !== 1) message += 's';
    message += '?';
    if ($window.confirm(message)) {
      self.isActionPending = true;
      suggestionMerger.processSuggestionMergings(selectedSuggestions)
        .then(annotationMapper.acceptSuggestions)
        .then(logAnnotationDeletionToAnalytics)
        .then(suggestions => {
          const notMergedSuggestionsCount = selectedSuggestions.length - suggestions.length;
          const mergedSuggestionsCount = suggestions.length;
          if (notMergedSuggestionsCount === 0) {
            const title = getSuggestionsMergedText(mergedSuggestionsCount);
            flash.success(title);
          } else if (mergedSuggestionsCount === 0) {
            const title = getSuggestionsNotMergedText(notMergedSuggestionsCount);
            flash.error(title);
          } else {
            const title = getSuggestionsMergedText(mergedSuggestionsCount);
            const message = getSuggestionsNotMergedText(notMergedSuggestionsCount);
            flash.warning(message, title);
          }
        })
        .catch(error => flashActionFailure(error, 'Accepting', true, selectedSuggestionsCount))
        .finally(() => self.isActionPending = false);
    }
  };

  function getSuggestionsMergedText(count) {
    let text = count + ' Suggestion';
    if (count !== 1) text += 's';
    text += ' accepted';
    return text;
  }

  function getSuggestionsNotMergedText(count) {
    let text = count + ' Suggestion';
    if (count !== 1) text += 's';
    text += ' not accepted';
    return text;
  }

  self.delete = function() {
    const selectedAnnotations = getDeletableSelectedAnnotations();
    const selectedAnnotationsCount = selectedAnnotations.length;
    let message = 'Are you sure you want to delete the ' + selectedAnnotationsCount + ' selected deletable annotation';
    if (selectedAnnotationsCount !== 1) message += 's';
    message += '?';
    if ($window.confirm(message)) {
      self.isActionPending = true;
      annotationMapper.deleteAnnotations(selectedAnnotations)
        .then(logAnnotationDeletionToAnalytics)
        .then(annotations => {
          let title = 'Annotation';
          if (annotations.length !== 1) title += 's';
          title += ' deleted';
          flash.success(title);
        })
        .catch(error => flashActionFailure(error, 'Deleting', false, selectedAnnotationsCount))
        .finally(() => self.isActionPending = false);
    }
  };

  self.reject = function() {
    const selectedSuggestions = getMergableSelectedAnnotations();
    const selectedSuggestionsCount = selectedSuggestions.length;
    let message = 'Are you sure you want to reject the ' + selectedSuggestionsCount + ' selected suggestion';
    if (selectedSuggestionsCount !== 1) message += 's';
    message += '?';
    if ($window.confirm(message)) {
      self.isActionPending = true;
      annotationMapper.rejectSuggestions(selectedSuggestions)
        .then(logAnnotationDeletionToAnalytics)
        .then(suggestions => {
          let title = 'Suggestion';
          if (suggestions.length !== 1) title += 's';
          title += ' rejected';
          flash.success(title);
        })
        .catch(error => flashActionFailure(error, 'Rejecting', true, selectedSuggestionsCount))
        .finally(() => self.isActionPending = false);
    }
  };

  function logAnnotationDeletionToAnalytics(annotations) {
    annotations.forEach(annotation => {
      let event;
      if (annotationMetadata.isHighlight(annotation)) {
        event = analytics.events.HIGHLIGHT_DELETED;
      } else if (annotationMetadata.isPageNote(annotation)) {
        event = analytics.events.PAGE_NOTE_DELETED;
      } else if (annotationMetadata.isReply(annotation)) {
        event = analytics.events.REPLY_DELETED;
      } else {
        event = analytics.events.ANNOTATION_DELETED;
      }
      analytics.track(event);
    });
    return annotations;
  }

  function flashActionFailure(error, action, isSuggestion, annotationCount) {
    let title = action + ' ';
    if (isSuggestion)
      title += 'suggestion';
    else
      title += 'annotation';
    if (annotationCount !== 1) title += 's';
    title += ' failed';
    const message = error.message ? error.message : error;
    flash.error(message, title);
  }

  self.deselectAll = function() {
    store.deselectAllAnnotations();
    frameSync.deselectAllAnnotations();
  };

  /* Conditions to display extended actions: Accept, Reject, Mark as Processed and Mark as Not Processed */
  self.areExtendedActionsShown = function () {
    return !authorityChecker.isISC(settings);
  };

  self.isAcceptAndRejectDisabled = function () {
    return getMergableSelectedAnnotations().length === 0 ||
      self.isActionPending;
  };

  self.deleteTitleText = function () {
    return "Delete the selected annotations";
  };

  function getMergableSelectedAnnotations() {
    return store.getSelectedAnnotations()
      .filter(annotation => authorizer.canMergeSuggestion(annotation, permissions, settings));
  }

  self.isTreatDisabled = function() {
	return getTreatableSelectedAnnotations().length === 0
      || self.isActionPending;
  };

  function getTreatableSelectedAnnotations() {
	return store.getSelectedAnnotations()
      .filter(annotation => authorizer.canTreatAnnotation(annotation, permissions, settings));
  };

  self.treat = function() {
	const selectedAnnotations = getTreatableSelectedAnnotations();
    const selectedAnnotationsCount = selectedAnnotations.length;
    let message = 'Are you sure you want to mark ' + selectedAnnotationsCount + ' selected annotation';
    if (selectedAnnotationsCount !== 1) message += 's';
    message += ' as processed?';
    if ($window.confirm(message)) {
      self.isActionPending = true;
      annotationMapper.treatAnnotations(selectedAnnotations)
        .then(logAnnotationDeletionToAnalytics)
        .then(annotations => {
          if (annotations) {
            let title = annotations.length + ' Annotation';
            if (annotations.length !== 1) title += 's';
            title += ' marked as processed';
            flash.success(title);
          }
        })
        .catch(error => flashActionFailure(error, 'Marking as Processed', false, selectedAnnotationsCount))
        .finally(() => self.isActionPending = false);
    }
  };

  self.isResetDisabled = function() {
	return getResettableSelectedAnnotations().length === 0
      || self.isActionPending;
  };

  function getResettableSelectedAnnotations() {
	return store.getSelectedAnnotations()
      .filter(annotation => authorizer.canResetAnnotation(annotation, permissions, settings));
  };

  self.reset = function() {
	const selectedAnnotations = getResettableSelectedAnnotations();
    const selectedAnnotationsCount = selectedAnnotations.length;
    let message = 'Are you sure you want to mark ' + selectedAnnotationsCount + ' selected annotation';
    if (selectedAnnotationsCount !== 1) message += 's';
    message += ' as not processed?';
    if ($window.confirm(message)) {
      self.isActionPending = true;
      annotationMapper.resetAnnotations(selectedAnnotations)
        .then(logAnnotationDeletionToAnalytics)
        .then(annotations => {
          if (annotations) {
            let title = annotations.length + ' Annotation'	
            if (annotations.length !== 1) title += 's';
            title += ' marked as not processed';
            flash.success(title);
          }
        })
        .catch(error => flashActionFailure(error, 'Marking as Not Processed', false, selectedAnnotationsCount))
        .finally(() => self.isActionPending = false);
    }
  };

  self.isDeleteDisabled = function () {
    return getDeletableSelectedAnnotations().length === 0
      || self.isActionPending;
  };

  function getDeletableSelectedAnnotations() {
    return store.getSelectedAnnotations()
      .filter(annotation => authorizer.canDeleteAnnotation(annotation, permissions, settings, session.state.userid));
  }

  self.isDeselectAllDisabled = function() {
    return !store.hasSelectedAnnotations();
  };

  self.isSelectAllDisabled = function() {
    const annotationCount = store.getAnnotationCount();
    return annotationCount === 0 || (store.getSelectedAnnotationCount() === annotationCount);
  };

  self.selectAllFilteredActionableAnnotations = function() {
    const annotationsToSelect = getAllFilteredActionableAnnotations();
    store.selectAnnotations(annotationsToSelect.map(annotation => annotation.id));
    frameSync.LEOS_selectAnnotations(annotationsToSelect);
  };

  function getAllFilteredActionableAnnotations() {
    const allFilteredActionableAnnotations = self.thread.children.map(threadChild => threadChild.annotation)
      .filter(annotation => {
        return authorizer.canMergeSuggestion(annotation, permissions, settings)
          || authorizer.canDeleteAnnotation(annotation, permissions, settings, session.state.userid)
          || authorizer.canTreatAnnotation(annotation, permissions, settings)
          || authorizer.canResetAnnotation(annotation, permissions, settings);
      });
    return allFilteredActionableAnnotations;
  }

  self.selectedAnnotationCount = function () {
    return store.getSelectedAnnotationCount();
  };
}

export = {
  controller: SelectionPaneController,
  controllerAs: 'vm',
  bindings: {
    filterActive: '<',
    filterMatchCount: '<',
    selectedTab: '<',
    thread: '<',
  },
  template: require('../templates/selection-pane.html'),
};
