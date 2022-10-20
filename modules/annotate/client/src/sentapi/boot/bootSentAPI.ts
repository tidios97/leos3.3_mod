'use strict';

function injectStylesheet(doc, href) {
  var link = doc.createElement('link');
  link.rel = 'stylesheet';
  link.type = 'text/css';
  link.href = href;
  doc.head.appendChild(link);
}

function injectScript(doc, src) {
  var script = doc.createElement('script');
  script.type = 'text/javascript';
  script.src = src;

  // Set 'async' to false to maintain execution order of scripts.
  // See https://developer.mozilla.org/en-US/docs/Web/HTML/Element/script
  script.async = false;
  doc.head.appendChild(script);
}

function injectAssets(doc, config, assets) {
  assets.forEach(function (path) {
    var url = config.assetRoot + '/' + config.manifest[path]; //LEOS changes
    if (url.match(/\.css/)) {
      injectStylesheet(doc, url);
    } else {
      injectScript(doc, url);
    }
  });
}

/**
 * Bootstrap the sidebar application which displays annotations.
 */
function bootSentAPIApp(doc, config) {
  injectAssets(doc, config, [
    // Vendor code and polyfills required by app.bundle.js
    'scripts/raven.bundle.js',
    'scripts/angular.bundle.js',
    'scripts/katex.bundle.js',
    'scripts/showdown.bundle.js',
    'scripts/polyfills.bundle.js',
    'scripts/unorm.bundle.js',

    // The sentapi app
    'scripts/sentapi.bundle.js',

    'styles/angular-csp.css',
    'styles/angular-toastr.css',
    'styles/icomoon.css',
    'styles/katex.min.css',
    'styles/sidebar.css',
    'styles/sentapi.css',
  ]);
}

function injectApp(doc) {
  var app = doc.createElement('annotate-sentapi-app');
  app.async = false;
  doc.body.appendChild(app);
}

function boot(document_, config) {
  bootSentAPIApp(document_, config);
  injectApp(document);
}

export = boot;
