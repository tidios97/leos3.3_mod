'use strict';

var addAnalytics = require('../sidebar/ga');
var disableOpenerForExternalLinks = require('../sidebar/util/disable-opener-for-external-links');
var apiUrls = require('../sidebar/get-api-url');
var serviceConfig = require('../sidebar/service-config');
var crossOriginRPC = require('../sidebar/cross-origin-rpc.js');
require('../shared/polyfills');

var raven;

// Read settings rendered into sidebar app HTML by service/extension.
var settings = require('../shared/settings').jsonConfigsFrom(document);

if (settings.raven) {
  // Initialize Raven. This is required at the top of this file
  // so that it happens early in the app's startup flow
  raven = require('../sidebar/raven');
  raven.init(settings.raven);
}

var hostPageConfig = require('../sidebar/host-config');
Object.assign(settings, hostPageConfig(window));

settings.apiUrl = apiUrls.getApiUrl(settings);

// Disable Angular features that are not compatible with CSP.
//
// See https://docs.angularjs.org/api/ng/directive/ngCsp
//
// The `ng-csp` attribute must be set on some HTML element in the document
// _before_ Angular is require'd for the first time.
document.body.setAttribute('ng-csp', '');

// Prevent tab-jacking.
disableOpenerForExternalLinks(document.body);

var angular = require('angular');

// autofill-event relies on the existence of window.angular so
// it must be require'd after angular is first require'd
require('autofill-event');

// Setup Angular integration for Raven
if (settings.raven) {
  raven.angularModule(angular);
} else {
  angular.module('ngRaven', []);
}

if(settings.googleAnalytics){
  addAnalytics(settings.googleAnalytics);
}

// @ngInject
function setupHttp($http, api) {
  //$http.defaults.headers.common['X-Client-Id'] = api.clientId;
}

// @ngInject
function configureLocation($locationProvider) {
  return $locationProvider.html5Mode({
    enabled: true,
    requireBase: false
  });  // Use HTML5 history
}

// @ngInject
function configureToastr(toastrConfig) {
  angular.extend(toastrConfig, {
    preventOpenDuplicates: true,
  });
}

function processAppOpts() {
  if (settings.liveReloadServer) {
    require('../sidebar/live-reload-client').connect(settings.liveReloadServer);
  }
}

module.exports = angular.module('sentapi', [
  // Angular addons which export the Angular module name
  // via module.exports
  require('angular-route'),
  require('angular-sanitize'),
  require('angular-toastr'),

  // Angular addons which do not export the Angular module
  // name via module.exports
  ['angulartics', require('angulartics')][0],
  ['angulartics.google.analytics', require('angulartics/src/angulartics-ga')][0],
  ['ngTagsInput', require('ng-tags-input')][0],
  ['ui.bootstrap', require('../sidebar/vendor/ui-bootstrap-custom-tpls-0.13.4')][0],

  // Local addons
  'ngRaven', 
])

  // The root component for the application
  .component('annotateSentapiApp', require('./components/annotate-sentapi-app'))

  // UI components

  .service('unicode', require('../sidebar/services/unicode'))
  .service('api', require('./services/api'))
  
  // Redux store
  .service('store', require('../sidebar/store'))

  // Utilities
  .value('Discovery', require('../shared/discovery'))
  .value('ExcerptOverflowMonitor', require('../sidebar/util/excerpt-overflow-monitor'))
  .value('VirtualThreadList', require('../sidebar/virtual-thread-list'))
  .value('random', require('../sidebar/util/random'))
  .value('raven', require('../sidebar/raven'))
  .value('serviceConfig', serviceConfig)
  .value('settings', settings)
  .value('time', require('../sidebar/util/time'))
  .value('urlEncodeFilter', require('../sidebar/filter/url').encode)

  .config(configureLocation)
  .config(configureToastr)
  .value('serviceConfig', serviceConfig)
  .value('settings', settings)
  .run(setupHttp)
  .run(crossOriginRPC.server.start);

processAppOpts();

type ChromeWindow = Window & typeof globalThis & {chrome: any}
let chromeWindow = window as ChromeWindow
// Work around a check in Angular's $sniffer service that causes it to
// incorrectly determine that Firefox extensions are Chrome Packaged Apps which
// do not support the HTML 5 History API. This results Angular redirecting the
// browser on startup and thus the app fails to load.
// See https://github.com/angular/angular.js/blob/a03b75c6a812fcc2f616fc05c0f1710e03fca8e9/src/ng/sniffer.js#L30
if (chromeWindow.chrome && !chromeWindow.chrome.app) {
  chromeWindow.chrome.app = {
    dummyAddedByHypothesisClient: true,
  };
}

var appEl = document.querySelector('annotate-sentapi-app');
angular.bootstrap(appEl, ['sentapi'], {strictDi: true});
