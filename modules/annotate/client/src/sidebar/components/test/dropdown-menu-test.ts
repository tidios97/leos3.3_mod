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

describe('dropdownMenu', function () {
  before(function () {
    angular.module('app', [])
      .component('dropdownMenu', require('../dropdown-menu'))
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

    element = util.createDirective(document, 'dropdownMenu', {
      groups: fakeGroups,
      privateLabel: 'Only Me',
      settings: fakeSettings,
      onUpdateSelectedGroup: function() { },
      onSetPrivacy: function() { },
    });
  });

  [
    {
      groupType: 'open',
      expectedIcon: 'public',
    },
    {
      groupType: 'restricted',
      expectedIcon: 'group',
    },
    {
      groupType: 'private',
      expectedIcon: 'group',
    },
  ].forEach(({ groupType, expectedIcon }) => {
    it('should set the correct group-type icon class', function () {
      element.ctrl.group = {
        name: 'My Group',
        type: groupType,
      };
      fakeGroupsService.focusedGroup = element.ctrl.group;
      element.scope.$digest();
      var iconElement = element.find('.group-icon-container > i');
      assert.isTrue(iconElement.hasClass(`h-icon-${expectedIcon}`));
    });
  });

  it('should change privacy when privacy option selected', function () {
    var privacyChangedSpy = sinon.spy();
    // for existing annotations, the privacy should not be changed
    // unless the user makes a choice from the list
    element.ctrl.onSetPrivacy = privacyChangedSpy;

    assert.ok(!privacyChangedSpy.called);
    var privateOption = element.find('li')[1];
    var sharedOption = element.find('li')[0];

    angular.element(privateOption).click();
    assert.equal(privacyChangedSpy.callCount, 1);
    angular.element(sharedOption).click();
    assert.equal(privacyChangedSpy.callCount, 2);
  });

});
