'use strict';

var boot = require('./bootSentAPI');
var settings = require('../../shared/settings').jsonConfigsFrom(document);

boot(document, {
  assetRoot: settings.assetRoot || '',
  // @ts-ignore (variable replaced at build time using gulp replace)
  manifest: __MANIFEST__,
});
