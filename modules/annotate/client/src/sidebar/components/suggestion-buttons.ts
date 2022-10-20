'use strict';

const OPERATION_MODES = require('../../shared/operationMode');

const annotationMetadata = require('../annotation-metadata');
const authorityChecker = require('../authority-checker');
const authorizer = require('../authorizer');

//@ngInject
function SuggestionController($scope, $timeout, $window, analytics, annotationMapper, flash, groups, permissions, store, settings, suggestionMerger) {
  
  let self = this;
  self.isAccepting = false;

  /**
   * Initialize the controller instance.
   *
   * All initialization code except for assigning the controller instance's
   * methods goes here.
   */
  this.$onInit = () => {
    if (!store.operationMode && settings.operationMode) {
      store.operationMode = settings.operationMode;
    }

    self.showButtons = () => {
      return authorizer.canMergeSuggestion(self.annotation, permissions, settings)
        || authorizer.originalTextHasBeenModified(self.annotation);
    };

    self.isAcceptDisabled = function () {

      return store.hostState === 'OPEN'
        || self.isAccepting
        || authorizer.originalTextHasBeenModified(self.annotation);
    };

    self.isAnnotationGroupInUserGroups = function () {
      const userGroups = groups.all().map(group => group.id);
      return userGroups.includes(self.annotation.group);
    };

    self.isRejectDisabled = function () {
      return store.operationMode === OPERATION_MODES.READ_ONLY
        || self.isAccepting;
    };

    self.getAcceptTitle = function () {

      if (authorizer.originalTextHasBeenModified(self.annotation)) {
        return 'The original text has been modified. The suggestion can not be automatically merged.';
      }

      if (self.isAcceptDisabled()) {
        return 'Not possible to accept suggestion while editing';
      }

      return 'Accept suggestion';
    };

    self.getRejectTitle = function () {
      return self.isRejectDisabled()
        ? 'Not possible to reject suggestion while editing'
        : 'Reject suggestion';
    };

    self.accept = function () {
      self.isAccepting = true;
      if (!self.annotation.user) {
        flash.info('Please log in to accept suggestions.');
        return Promise.resolve();
      }

      return suggestionMerger.processSuggestionMerging(self.annotation).then(function (suggestion) {
        annotationMapper.acceptSuggestion(suggestion).then(function () {
          analytics.track(analytics.events.ANNOTATION_DELETED);
          flash.success('Suggestion successfully merged');
          self.isAccepting = false;
        }).catch(function () {
          flash.error('Suggestion content merging failed');
          self.isAccepting = false;
        });
      }).catch(function (err) {
        flash.error(err.message);
        self.isAccepting = false;
      });
    };

    self.reject = function () {
      return $timeout(function () {
        const msg = 'Are you sure you want to reject this suggestion?';
        if ($window.confirm(msg)) {
          $scope.$apply(function () {
            annotationMapper.rejectSuggestion(self.annotation).then(function () {
              analytics.track(analytics.events.ANNOTATION_DELETED);
            }).catch(function (err) {
              flash.error(err.message);
            });
          });
        }
      }, true);
    };
  };
}

export = {
  controller: SuggestionController,
  controllerAs: 'vm',
  bindings: {
    annotation: '<',
    reply: '&',
    replyCount: '<',
  },
  template: require('../templates/suggestion-buttons.html'),
};
