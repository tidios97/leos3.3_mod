'use strict';

var settingsFrom = require('./settings');

/**
 * Reads the Hypothesis configuration from the environment.
 *
 * @param {Window} window_ - The Window object to read config from.
 */
function configFrom(window_) {
  var settings = settingsFrom(window_);
  return {
    annotations: settings.annotations,
    // URL where client assets are served from. Used when injecting the client
    // into child iframes.
    assetRoot: settings.hostPageSetting('assetRoot', {allowInBrowserExt: true}),
    branding: settings.hostPageSetting('branding'),
    // URL of the client's boot script. Used when injecting the client into
    // child iframes.
    clientUrl: settings.clientUrl,
    enableExperimentalNewNoteButton: settings.hostPageSetting('enableExperimentalNewNoteButton'),
    theme: settings.hostPageSetting('theme'),
    usernameUrl: settings.hostPageSetting('usernameUrl'),
    onLayoutChange: settings.hostPageSetting('onLayoutChange'),
    openSidebar: settings.hostPageSetting('openSidebar', {allowInBrowserExt: true}),
    query: settings.query,
    services: settings.hostPageSetting('services'),
    showHighlights: settings.showHighlights,
    sidebarAppUrl: settings.sidebarAppUrl,
    spellChecker: settings.hostPageSetting('spellChecker'),
    // Subframe identifier given when a frame is being embedded into
    // by a top level client
    subFrameIdentifier: settings.hostPageSetting('subFrameIdentifier', {allowInBrowserExt: true}),

    annotationContainer: settings.hostPageSetting('annotationContainer'), //LEOS Change
    leosDocumentRootNode: settings.hostPageSetting('leosDocumentRootNode'), //LEOS Change
    ignoredTags: settings.hostPageSetting('ignoredTags', {defaultValue: []}), //LEOS Change
    allowedSelectorTags: settings.hostPageSetting('allowedSelectorTags', {defaultValue: '*'}), //LEOS Change
    editableSelector: settings.hostPageSetting('editableSelector', {defaultValue: ''}), // LEOS Change
    notAllowedSuggestSelector: settings.hostPageSetting('notAllowedSuggestSelector', {defaultValue: '*'}), // LEOS Change
    displayMetadataCondition: settings.hostPageSetting('displayMetadataCondition', {defaultValue: {}}), // LEOS Change
    operationMode: settings.hostPageSetting('operationMode', {defaultValue: 'NORMAL'}), // LEOS Change
    showStatusFilter: settings.hostPageSetting('showStatusFilter', {defaultValue: false}), // LEOS Change
    showGuideLinesButton: settings.hostPageSetting('showGuideLinesButton', {defaultValue: true}), // LEOS Change
    annotationPopupDefaultStatus: settings.hostPageSetting('annotationPopupDefaultStatus', {defaultValue: 'HIDE'}), // LEOS Change
    connectedEntity: settings.hostPageSetting('connectedEntity', {defaultValue: ''}), // LEOS Change
    context: settings.hostPageSetting('context', {defaultValue: ''}) // LEOS Change
  };
}

module.exports = configFrom;
