'use strict';

var angular = require('angular');

var events = require('../events');
const authorityChecker = require('../authority-checker');

function getExistingAnnotation(store, id) {
  return store.getState().annotations.find(function (annot) {
    return annot.id === id;
  });
}

//LEOS Change
function LEOS_processAnnotations(annotations, _rootScope) {
  annotations.forEach(function (annotation) {
    if (annotation.group) {
      annotation.group = annotation.group.replace(' ', _rootScope.ANNOTATION_GROUP_SPACE_REPLACE_TOKEN);
    }
  });
}

// Wraps the annotation store to trigger events for the CRUD actions
// @ngInject
function annotationMapper($rootScope, store, api, settings) {

  function loadAnnotations(annotations, replies) {
    annotations = annotations.concat(replies || []);
    //LEOS Change : remove white spaces from GROUP names
    LEOS_processAnnotations(annotations, $rootScope);

    var loaded = [];
    annotations.forEach(function (annotation) {
      var existing = getExistingAnnotation(store, annotation.id);
      if (existing) {
        $rootScope.$broadcast(events.ANNOTATION_UPDATED, annotation);
        return;
      }
      loaded.push(annotation);
    });

    $rootScope.$broadcast(events.ANNOTATIONS_LOADED, loaded);
  }

  function LEOS_isToBeProcessed(annotation) {
    return authorityChecker.isISC(settings) && annotation && annotation.document && annotation.document.metadata.responseStatus && annotation.document.metadata.responseStatus == 'SENT';
  }

  function unloadAnnotations(annotations, reset) {
        let unloaded = [];
        let toBeProcessed = false;
        annotations.forEach(function (annotation) {
            let existing = getExistingAnnotation(store, annotation.id);
            if ((reset == null || !reset) && existing && LEOS_isToBeProcessed(existing)) {
                toBeProcessed = true;
            } else if (existing && annotation !== existing) {
                annotation = angular.copy(annotation, existing);
                unloaded.push(annotation);
            } else {
                unloaded.push(annotation);
            }
        });
        if (unloaded.length>0) {
            $rootScope.$broadcast(events.ANNOTATIONS_UNLOADED, unloaded);
        } else if ((reset == null || !reset) && toBeProcessed) {
            $rootScope.$broadcast("reloadAnnotations");
        }
  }

  function createAnnotation(annotation) {
    $rootScope.$broadcast(events.BEFORE_ANNOTATION_CREATED, annotation);
    return annotation;
  }

  function deleteAnnotation(annotation) {
    return api.annotation.delete({
      id: annotation.id,
    }).then(function () {
      $rootScope.$broadcast(events.ANNOTATION_DELETED, annotation);
      return annotation;
    });
  }

  function deleteAnnotations(annotations) {
    return api.annotation.deleteMultiple({}, {
      ids: annotations.map(a => a.id),
    }).then(function () {
      $rootScope.$broadcast(events.ANNOTATIONS_DELETED, annotations);
      return annotations;
    });
  }

  function flagAnnotation(annot) {
    return api.annotation.flag({
      id: annot.id,
    }).then(function () {
      $rootScope.$broadcast(events.ANNOTATION_FLAGGED, annot);
      return annot;
    });
  }

  function resetAnnotation(annotation) {
    return api.annotation.reset({
      id: annotation.id,
    }).then(function () {
      $rootScope.$broadcast(events.ANNOTATION_DELETED, annotation);
      return annotation;
    });
  }

  function resetAnnotations(annotations) {
    return api.annotation.resetMultiple({}, {
      ids: annotations.map(a => a.id),
    }).then(function () {
      $rootScope.$broadcast(events.ANNOTATIONS_DELETED, annotations);
      return annotations;
    });
  }

  function treatAnnotation(annotation) {
    return api.annotation.treat({
      id: annotation.id,
    }).then(function () {
      $rootScope.$broadcast(events.ANNOTATION_DELETED, annotation);
      return annotation;
    });
  }

  function treatAnnotations(annotations) {
    return api.annotation.treatMultiple({}, {
      ids: annotations.map(a => a.id),
    }).then(function () {
      $rootScope.$broadcast(events.ANNOTATIONS_DELETED, annotations);
      return annotations;
    });
  }

  function acceptSuggestion(annotation) {
    return api.suggestion.accept({
      id: annotation.id,
    }).then(function () {
      $rootScope.$broadcast(events.ANNOTATION_DELETED, annotation);
      return annotation;
    });
  }

  function acceptSuggestions(annotations) {
    return Promise.all(annotations.map(annotation => api.suggestion.accept({
      id: annotation.id,
    }))).then(function () {
      $rootScope.$broadcast(events.ANNOTATIONS_DELETED, annotations);
      return annotations;
    });
  }

  function rejectSuggestion(annotation) {
    return api.suggestion.reject({
      id: annotation.id,
    }).then(function () {
      $rootScope.$broadcast(events.ANNOTATION_DELETED, annotation);
      return annotation;
    });
  }

  function rejectSuggestions(annotations) {
    return Promise.all(annotations.map(annotation => api.suggestion.reject({
      id: annotation.id,
    }))).then(function () {
      $rootScope.$broadcast(events.ANNOTATIONS_DELETED, annotations);
      return annotations;
    });
  }

  return {
    acceptSuggestion,
    acceptSuggestions,
    loadAnnotations,
    unloadAnnotations,
    createAnnotation,
    deleteAnnotation,
    deleteAnnotations,
    flagAnnotation,
    rejectSuggestion,
    rejectSuggestions,
    treatAnnotation,
	  treatAnnotations,
    resetAnnotation,
	  resetAnnotations,
    LEOS_isToBeProcessed
  };
}

export = annotationMapper;
