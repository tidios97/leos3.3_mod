'use strict';

var retryUtil = require('../util/retry');
var events = require('../events');

function leosOauthAuthDecorator($provide) {
  $provide.decorator('auth', ['$delegate', '$injector', '$rootScope', '$http', function oauthAuthDecorator($delegate, $injector, $rootScope, $http) {
    var bridge = $injector.get('bridge');
    var localStorage = $injector.get('localStorage');
    var settings = $injector.get('settings');
    var frameConnected = false;
    var hostToken;

    function storageKey() {
      var apiDomain = new URL(settings.apiUrl).hostname;
      apiDomain = apiDomain.replace(/\./g, '%2E');
      return `hypothesis.oauth.${apiDomain}.token`;
    }

    function saveToken(token) {
      localStorage.setObject(storageKey(), token);
    }

    function waitForFrameConnection() {
      return retryUtil.retryPromiseOperation(function () {
        return new Promise(function(resolve, reject) {
          if (!frameConnected) {
            $rootScope.$on(events.FRAME_CONNECTED, function(event, error) {
              frameConnected = true;
              resolve(true);
            });
          } else {
            resolve(true);
          }
        });
      }, {
        retries: 5,
        minTimeout: 500,
        maxTimeout: 2000
      });
    }

    function requestHostToken() {
      return new Promise( function(resolve, reject) {
          var promiseTimeout = setTimeout(() => reject('timeout'), 500);
          bridge.call('requestSecurityToken', function (error, result) {
            clearTimeout(promiseTimeout);
          if (error) {
            return reject(error);
          } else {
            return resolve(result);
          }
        })
      });
    }

    function getHostToken() {
      return waitForFrameConnection().then(result => {
        return requestHostToken();
      }).catch(err => {
        return Promise.reject(err);
      });
    }

    var oldTokenGetter = $delegate.tokenGetter;
    $delegate.tokenGetter = function() {
      if (hostToken && Date.now() <= hostToken.expiresAt) {
        return Promise.resolve(hostToken.accessToken);
      }
      return Promise.all([getHostToken(), $delegate.getOauthClient()]).then(function([[grantToken], client]) {
        if (!grantToken) {
          throw "token is null";
        }
        else {
          return client.exchangeGrantToken(grantToken).then(function (resultToken) {
            hostToken = resultToken;
            saveToken(hostToken);
            return hostToken.accessToken;
          }).catch(err => {
            throw "Invalid token";
          });
        }
      }).catch(err => {
        return oldTokenGetter();
      });
    }
    return $delegate;
  }]);
}

module.exports = leosOauthAuthDecorator;