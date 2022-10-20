'use strict';

var angular = require('angular');

var fixtures = require('./suggestion-fixtures-test');
var testUtil = require('../../../shared/test/util');
var util = require('../../directive/test/util');

var testInject = angular.mock.inject;

function isSuggestButtonHidden(annotationEl) {
  var btn = annotationEl[0].querySelector('.suggestion-merge-action');
  if (!btn) {
    return true;
  }
  return btn && btn.classList.contains('ng-hide');
}

function findSuggestButton(annotationEl) {
  var btns = Array.from(annotationEl[0].querySelectorAll('.suggestion-merge-action'));
  return btns.length != 0;
}

describe('suggestion', function() {

  var proxyquire = require('proxyquire');

  describe('AnnotationController', function() {
    var $rootScope;
    var $scope;

    // Unfortunately fakeAccountID needs to be initialised here because it
    // gets passed into proxyquire() _before_ the beforeEach() that initializes
    // the rest of the fakes runs.
    var fakeAccountID = {
      isThirdPartyUser: sinon.stub(),
    };
    var fakeAnalytics;
    let fakeAnnotationMapper;
    var fakeFlash;
    let fakeGroups;
    var fakePermissions;
    let fakeStore;
    let fakeDrafts;
    let fakeSettings;
    let fakeSuggestionMerger;

    /**
     * Returns the annotation directive with helpers stubbed out.
     */
    function suggestionButtonsComponent() {
      return proxyquire('../suggestion-buttons', {
        angular: testUtil.noCallThru(angular),
        '../../util/account-id': fakeAccountID,
        '@noCallThru': true,
      });
    }

    function createDirective(annotation) {
      annotation = annotation || fixtures.defaultAnnotation();
      var element = util.createDirective(document, 'suggestionButtons', {
        annotation: annotation,
      });

      fakeDrafts = {
        get: sinon.stub(),
      };

      // A new annotation won't have any saved drafts yet.
      if (!annotation.id) {
        fakeDrafts.get.returns(null);
      }

      return {
        annotation: annotation,
        controller: element.ctrl,
        element: element,
        scope: element.scope,
      };
    }

    before(function() {
      angular.module('h', [])
        .component('suggestionButtons', suggestionButtonsComponent())
        .service('permissions', require('../../services/permissions'));
    });

    beforeEach(angular.mock.module('h'));
    beforeEach(angular.mock.module(function($provide) {

      fakeAnalytics = {};
      fakeAnnotationMapper = {};
      fakeFlash = {};
      fakeGroups = {
        all: sinon.stub().returns([]),
      };
      fakeStore = {};
      fakeSettings = {};
      fakeSuggestionMerger = {};

      fakeAccountID.isThirdPartyUser.resetHistory();
      fakeAccountID.isThirdPartyUser.returns(false);

      fakePermissions = {
        getUserPermissions: function() {
          return ['CAN_MERGE_SUGGESTION'];
        },
        getHostState: function() {
          return false;
        },
      };

      $provide.value('analytics', fakeAnalytics);
      $provide.value('annotationMapper', fakeAnnotationMapper);
      $provide.value('flash', fakeFlash);
      $provide.value('groups', fakeGroups);
      $provide.value('permissions', fakePermissions);
      $provide.value('store', fakeStore);
      $provide.value('settings', fakeSettings);
      $provide.value('suggestionMerger', fakeSuggestionMerger);
    }));

    beforeEach(
      testInject(
        function(_$rootScope_) {
          $rootScope = _$rootScope_;
          $scope = $rootScope.$new();
        }
      )
    );

    afterEach(function() {
      sinon.restore();
    });
    it('checks presence of suggest button for suggestions', function() {
      var annotation = fixtures.defaultSuggestion();
      var el = createDirective(annotation).element;
      assert.isTrue(findSuggestButton(el));
      assert.isFalse(isSuggestButtonHidden(el));
    });
    it('checks that suggest button is disabled while no right', function() {
      fakePermissions.getUserPermissions = function() {
        return [];
      };
      var annotation = fixtures.defaultSuggestion();
      var el = createDirective(annotation).element;
      assert.isTrue(findSuggestButton(el));
      assert.isTrue(isSuggestButtonHidden(el));
    });
  });
});
