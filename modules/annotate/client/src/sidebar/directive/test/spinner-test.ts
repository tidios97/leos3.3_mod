'use strict';



describe('spinner', function () {
  var $animate = null;
  var $element = null;
  var angular = require('angular');
  var module = angular.mock.module;
  var inject = angular.mock.inject;
  
  before(function () {
    angular.module('h', []).directive('spinner', require('../spinner'));
  });
  beforeEach(module('h'));

  beforeEach(inject(function (_$animate_, $compile, $rootScope) {
    $animate = _$animate_;
    sinon.spy($animate, 'enabled');

    $element = angular.element('<span class="spinner"></span>');
    $compile($element)($rootScope.$new());
  }));

  afterEach(function () {
    sinon.restore();
  });

  it('disables ngAnimate animations for itself', function () {
    assert.calledWith($animate.enabled, false, sinon.match($element));
  });
});
