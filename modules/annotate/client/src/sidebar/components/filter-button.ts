'use strict';

// @ngInject
function FilterButtonController($element, $rootScope) {
  var button = $element.find('button');

  button.on('click', function () {
      $rootScope.$broadcast('filterPane:toggleVisibility');
  })
}

export = {
  controllerAs: 'vm',
  controller: FilterButtonController,
  template: require('../templates/filter-button.html'),
};
