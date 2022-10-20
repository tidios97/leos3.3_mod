'use strict';

var scrollIntoView = require('scroll-into-view');

var events = require('../events');
var { parseAccountID } = require('../util/account-id');
var scopeTimeout = require('../util/scope-timeout');
var serviceConfig = require('../service-config');
var bridgeEvents = require('../../shared/bridge-events');

function _addSpellCheckerConfig(doc, serviceUrlString) {
    if (!serviceUrlString || serviceUrlString.length == 0) {
        return;
    }

    const serviceUrl = new URL(serviceUrlString);
    if (!doc.getElementById('spellChecker-config')) {
        var script = doc.createElement('script');
        script.id = "spellChecker-config";
        script.innerHTML = `
            function handleReplace(data, instance) {
                const replaceEvent = new CustomEvent('AnnotateSpellCheckerReplace', { detail: data } );
                window.document.dispatchEvent(replaceEvent);
            }

            function onStatistics(data, instance) {
                if (!data) {
                    return;
                }
                if (data.action !== 'replace') {
                    return;
                }
                handleReplace(data, instance);
            }

            window.WEBSPELLCHECKER_CONFIG = {
                autoSearch: true,
                autoDestroy: true,
                disableAutoSearchIn: ['#suggestion-editor'],
                enableGrammar: true,
                disableDictionariesPreferences: true,
                lang: 'en_GB',
                serviceProtocol: '${serviceUrl.protocol.substring(0, serviceUrl.protocol.length - 1)}',
                serviceHost: '${serviceUrl.hostname}',
                servicePort: '${serviceUrl.port}',
                servicePath: '${serviceUrl.pathname}',
                onStatistics: onStatistics
            };
        `;
        doc.body.appendChild(script);
    }
}

function _addSpellCheckerScript(doc, sourceUrl) {
    if (!sourceUrl || sourceUrl.length == 0) {
        return;
    }

    if (!doc.getElementById('spellChecker-service')) {
        var script = doc.createElement('script');
        script.id = "spellChecker-service"
        script.addEventListener('error', _onErrorLoad, false);
        script.type = 'text/javascript';
        script.src = sourceUrl;
        script.async = true;
        doc.head.appendChild(script);
        function _onErrorLoad(event) {
            console.debug('Error occurred while loading spell checker script ', event);
        }
    }
}

/**
 * Return the user's authentication status from their profile.
 *
 * @param {Profile} profile - The profile object from the API.
 */
function authStateFromProfile(profile) {
  if (profile.userid) {
    var parsed = parseAccountID(profile.userid);
    var displayName = parsed.username;
    if (profile.user_info && profile.user_info.display_name) {
      displayName = profile.user_info.display_name;
    }
    return {
      status: 'logged-in',
      displayName,
      userid: profile.userid,
      username: parsed.username,
      provider: parsed.provider,
    };
  } else {
    return {status: 'logged-out'};
  }
}

// @ngInject
function HypothesisAppController(
  $document, $location, $rootScope, $route, $scope, $timeout,
  $window, analytics, store, auth, bridge, drafts, features,
  flash, frameSync, groups, serviceUrl, session, settings, streamer
) {
  var self = this;

  //LEOS Change : set global annotation group tokenizer token
  if(!$rootScope.ANNOTATION_GROUP_SPACE_REPLACE_TOKEN) {
    $rootScope.ANNOTATION_GROUP_SPACE_REPLACE_TOKEN = "#";
  }

  const isEnableSpellChecker = (settings.spellChecker !== null && settings.spellChecker.enabled === "true");
  if (isEnableSpellChecker) {
    _addSpellCheckerConfig(window.document, settings.spellChecker.serviceUrl);
    _addSpellCheckerScript(window.document, settings.spellChecker.sourceUrl);
  }

  //LEOS Change : sync canvas
  $scope.$on('LEOS_syncCanvas', function (event ,iFrameOffsetLeft, delayResp) {
    $timeout(function () {
      var coordinates = [];

      var annotationElems = Array.prototype.slice.call(document.querySelectorAll('.is-suggestion'));
      annotationElems = annotationElems.concat(Array.prototype.slice.call(document.querySelectorAll('.is-comment')));
      annotationElems = annotationElems.concat(Array.prototype.slice.call(document.querySelectorAll('.is-highlight')));
      annotationElems.forEach(function (annotationElem) {
        var left = iFrameOffsetLeft + 20;
        var top = annotationElem.getBoundingClientRect().top + 50;

        coordinates.push({id:annotationElem.id,x:left,y:top});
      });

      bridge.call('LEOS_syncCanvasResp', coordinates);
      
    }, delayResp || 0);
  });

  // This stores information about the current user's authentication status.
  // When the controller instantiates we do not yet know if the user is
  // logged-in or not, so it has an initial status of 'unknown'. This can be
  // used by templates to show an intermediate or loading state.
  this.auth = {status: 'unknown'};

  // App dialogs
  this.shareDialog = {visible: false};
  this.helpPanel = {visible: false};

  // Check to see if we're in the sidebar, or on a standalone page such as
  // the stream page or an individual annotation page.
  this.isSidebar = $window.top !== $window;
  if (this.isSidebar) {
    frameSync.connect();
  }

  this.sortKey = function () {
    return store.getState().sortKey;
  };

  this.sortKeysAvailable = function () {
    return store.getState().sortKeysAvailable;
  };

  this.setSortKey = store.setSortKey;

  // Reload the view when the user switches accounts
  $scope.$on(events.USER_CHANGED, function (event, data) {
    self.auth = authStateFromProfile(data.profile);
  });

  session.load().then(profile => {
    self.auth = authStateFromProfile(profile);
  });

  /** Scroll to the view to the element matching the given selector */
  function scrollToView(selector) {
    // Add a timeout so that if the element has just been shown (eg. via ngIf)
    // it is added to the DOM before we try to locate and scroll to it.
    scopeTimeout($scope, function () {
      scrollIntoView($document[0].querySelector(selector));
    }, 0);
  }

  /**
   * Start the login flow. This will present the user with the login dialog.
   *
   * @return {Promise<void>} - A Promise that resolves when the login flow
   *   completes. For non-OAuth logins, always resolves immediately.
   */
  this.login = function () {
    if (serviceConfig(settings)) {
      // Let the host page handle the login request
      bridge.call(bridgeEvents.LOGIN_REQUESTED);
      return Promise.resolve();
    }

    return auth.login().then(() => {
      session.reload();
    }).catch((err) => {
      flash.error(err.message);
    });
  };

  this.signUp = function(){
    analytics.track(analytics.events.SIGN_UP_REQUESTED);

    if (serviceConfig(settings)) {
      // Let the host page handle the signup request
      bridge.call(bridgeEvents.SIGNUP_REQUESTED);
      return;
    }
    $window.open(serviceUrl('signup'));
  };

  // Display the dialog for sharing the current page
  this.share = function () {
    this.shareDialog.visible = true;
    scrollToView('share-dialog');
  };

  this.showHelpPanel = function () {
    var service = serviceConfig(settings) || {};
    if (service.onHelpRequestProvided) {
      // Let the host page handle the help request.
      bridge.call(bridgeEvents.HELP_REQUESTED);
      return;
    }

    this.helpPanel.visible = true;
  };

  // Prompt to discard any unsaved drafts.
  var promptToLogout = function () {
    // TODO - Replace this with a UI which doesn't look terrible.
    var text = '';
    if (drafts.count() === 1) {
      text = 'You have an unsaved annotation.\n' +
        'Do you really want to discard this draft?';
    } else if (drafts.count() > 1) {
      text = 'You have ' + drafts.count() + ' unsaved annotations.\n' +
        'Do you really want to discard these drafts?';
    }
    return (drafts.count() === 0 || $window.confirm(text));
  };

  // Log the user out.
  this.logout = function () {
    if (!promptToLogout()) {
      return;
    }
    drafts.unsaved().forEach(function (draft) {
      $rootScope.$emit(events.ANNOTATION_DELETED, draft);
    });
    drafts.discard();

    if (serviceConfig(settings)) {
      // Let the host page handle the signup request
      bridge.call(bridgeEvents.LOGOUT_REQUESTED);
      return;
    }

    session.logout();
  };
  
  this.search = {
    query: function () {
      return store.getState().filterQuery;
    },
    update: function (query) {
      store.setFilterQuery(query);
    },
    filterReseted: function () {
      return store.getState().filterReseted;
    },
    setFilterReseted: function (filterReset) {
      store.setFilterReseted(filterReset);
    },
  };

  this.countPendingUpdates = streamer.countPendingUpdates;
  this.applyPendingUpdates = streamer.applyPendingUpdates;
}

export = {
  controller: HypothesisAppController,
  controllerAs: 'vm',
  template: require('../templates/hypothesis-app.html'),
};
