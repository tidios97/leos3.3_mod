'use strict';

function leosPermissionsDecorator($provide) {
  $provide.decorator('permissions', ['$delegate', '$injector', '$rootScope', '$http', function permissionsDecorator($delegate, $injector, $rootScope, $http) {
    var bridge = $injector.get('bridge');
    var hostUserPermissions;

    function requestUserPermissions(): Promise<any[]> {
      return new Promise( function(resolve, reject) {
        var timeout = setTimeout((() => reject('timeout')), 2000);
        bridge.call('requestUserPermissions', function (error, result) {
          clearTimeout(timeout);
          if (error) { return reject(error); } else { return resolve(result); }
        });
      });
    }

    requestUserPermissions().then(([permissions]) => {
      hostUserPermissions = permissions;
    }).catch(err => {
      hostUserPermissions = [];
    });

    $delegate.getUserPermissions = function() {
      if (!hostUserPermissions) {
        return [];
      }
      else {
        return hostUserPermissions;
      }
    };

    return $delegate;
  }]);
}

module.exports = leosPermissionsDecorator;