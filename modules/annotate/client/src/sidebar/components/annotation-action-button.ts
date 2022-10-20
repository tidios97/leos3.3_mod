'use strict';

export = {
  controllerAs: 'vm',
  bindings: {
    icon: '<',
    isDisabled: '<',
    label: '<',
    onClick: '&',
  },
  template: require('../templates/annotation-action-button.html'),
};
