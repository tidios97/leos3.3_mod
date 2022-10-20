'use strict';

const authorityChecker = require('../authority-checker');
var OPERATION_MODES = require('../../shared/operationMode');
/**
 * @description Displays a combined privacy/selection post button to post
 *              a new annotation
 */
// @ngInject
function PublishAnnotationController($injector, groups, settings) {
  const groupsToLoad = authorityChecker.isISC(settings) ? [] : groups;
  this.showDropdown = false;
  this.privateLabel = 'Only Me';
  this.settings = settings;

  this.groupCategory = function (group) {
    return group.type === 'open' ? 'public' : 'group';
  };

  this.getAllGroups = function () {
    var searchBarSelectGroup = groupsToLoad.focused();
    if (settings.operationMode === OPERATION_MODES.PRIVATE || groupsToLoad.length === 0) {
      //on private mode, annotations cannot be published to any group, only to self
      return [];
    } else if (searchBarSelectGroup.type === 'open') {
      return groupsToLoad.all();
    } else {
      return [searchBarSelectGroup];
    }
  };

  this.isAuthorityVisible = function() {
    var isVisible = true;
    if(authorityChecker.isISC(settings)) {
      isVisible = false;
    }
    return isVisible;
  };

  this.publishDestination = function () {
    return this.isShared ? this.group.name : this.privateLabel;
  };

  this.setPrivacy = function (level) {
    this.onSetPrivacy({level: level});
  };

  this.updateSelectedGroup = function(group) {
    this.onUpdateSelectedGroup({group});
  }

  this.hasDropdown = function() {
    return !this.isReply && !authorityChecker.isISC(settings);
  }
}

export = {
  controller: PublishAnnotationController,
  controllerAs: 'vm',
  bindings: {
    group: '<',
    onUpdateSelectedGroup: '&',
    canPost: '<',
    isShared: '<',
    onCancel: '&',
    onSave: '&',
    onSetPrivacy: '&',
    isReply: '<',
    isforward: '<',
    originGroup: '<',
  },
  template: require('../templates/publish-annotation-btn.html'),
};
