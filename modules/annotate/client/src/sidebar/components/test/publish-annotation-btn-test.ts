'use strict';

var angular = require('angular');

var util = require('../../directive/test/util');

var fakeStorage = {};
var fakeLocalStorage = {
  setItem: function (key, value) {
    fakeStorage[key] = value;
  },
  getItem: function (key) {
    return fakeStorage[key];
  },
};

var fakeGroups = [
  {
    id: 'Public test group',
    type: 'public'
  },
  {
    id: 'Private test group',
    type: 'private'
  },
  {
    id: 'Open test group',
    type: 'open'
  },
  {
    id: 'Restricted test group',
    type: 'restricted'
  },
];

var fakeGroupsService = {
  focusedGroup: undefined,
  all: function () {
    return fakeGroups;
  },
  get: function (id) {
    const matchedGroups = fakeGroups.filter(group => group.id === id);
    return matchedGroups.length > 0 ? matchedGroups[0] : undefined;
  },
  leave: sinon.stub(),
  focus: sinon.stub(),
  focused: function () {
    return this.focusedGroup;
  }
};

var fakeSettings = {};

describe('publishAnnotationBtn', function () {
  before(function () {
    angular.module('app', [])
      .component('dropdownMenuBtn', require('../dropdown-menu-btn'))
      .component('publishAnnotationBtn', require('../publish-annotation-btn'))
      .factory('localStorage', function () {
        return fakeLocalStorage;
      })
      .factory('groups', function () {
        return fakeGroupsService;
      })
      .value('settings', fakeSettings);
  });

  var element;

  beforeEach(function () {
    angular.mock.module('app');

    // create a new instance of the directive with default
    // attributes
    const group = {
      name: 'Public',
    };
    fakeGroupsService.focusedGroup = group;

    element = util.createDirective(document, 'publishAnnotationBtn', {
      group,
      canPost: true,
      isShared: false,
      onSave: function () { },
      onSetPrivacy: function () { },
      onCancel: function () { },
    });
  });

  it('should display "Post to Only Me"', function () {
    var buttons = element.find('button');
    assert.equal(buttons.length, 3);
    assert.equal(buttons[0].innerHTML, 'Post to Only Me');
  });

  it('should display "Post to Research Lab"', function () {
    element.ctrl.group = {
      name: 'Research Lab',
    };
    element.ctrl.isShared = true;
    element.scope.$digest();
    var buttons = element.find('button');
    assert.equal(buttons[0].innerHTML, 'Post to Research Lab');
  });

  it('should save when "Post..." is clicked', function () {
    var savedSpy = sinon.spy();
    element.ctrl.onSave = savedSpy;
    assert.ok(!savedSpy.called);
    angular.element(element.find('button')[0]).click();
    assert.ok(savedSpy.calledOnce);
  });

  it('should disable post buttons when posting is not possible', function () {
    element.ctrl.canPost = false;
    element.scope.$digest();
    var disabledBtns = element.find('button[disabled]');
    assert.equal(disabledBtns.length, 1);

    // check that buttons are enabled when posting is possible
    element.ctrl.canPost = true;
    element.scope.$digest();
    disabledBtns = element.find('button[disabled]');
    assert.equal(disabledBtns.length, 0);
  });

  it('should revert changes when cancel is clicked', function () {
    var cancelSpy = sinon.spy();
    element.ctrl.onCancel = cancelSpy;
    element.scope.$digest();
    var cancelBtn = element.find('.publish-annotation-cancel-btn');
    assert.equal(cancelBtn.length, 1);
    angular.element(cancelBtn).click();
    assert.equal(cancelSpy.callCount, 1);
  });

});
