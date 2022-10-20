'use strict';

// @ngInject
function DropdownMenuBtnController($timeout) {
  var self = this;
  this.toggleDropdown = function($event) {
    $event.stopPropagation();
    $timeout(function () {
      self.onToggleDropdown();
    }, 0);
  };
}

export = {
  controller: DropdownMenuBtnController,
  controllerAs: 'vm',
  bindings: {
    isDisabled: '<',
    label: '<',
    dropdownMenuLabel: '@',
    onClick: '&',
    onToggleDropdown: '&',
    isReply: '<',
  },
  template: require('../templates/dropdown-menu-btn.html'),
};
