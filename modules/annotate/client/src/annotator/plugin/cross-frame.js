let CrossFrame;
const Plugin = require('../plugin');

const AnnotationSync = require('../annotation-sync');
const Bridge = require('../../shared/bridge');
const Discovery = require('../../shared/discovery');
const FrameUtil = require('../util/frame-util');
const FrameObserver = require('../frame-observer');

// Extracts individual keys from an object and returns a new one.
var extract = (extract = function(obj, ...keys) {
  const ret = {};
  for (let key of keys) { if (obj.hasOwnProperty(key)) { ret[key] = obj[key]; } }
  return ret;
});

// Class for establishing a messaging connection to the parent sidebar as well
// as keeping the annotation state in sync with the sidebar application, this
// frame acts as the bridge client, the sidebar is the server. This plugin
// can also be used to send messages through to the sidebar using the
// call method. This plugin also enables the discovery and management of
// not yet known frames in a multiple frame scenario.
module.exports = (CrossFrame = class CrossFrame extends Plugin {
  constructor(elem, options) {
    super(...arguments);

    const {
      config,
    } = options;
    let opts = extract(options, 'server');
    const discovery = new Discovery(window, opts);

    const bridge = new Bridge();

    opts = extract(options, 'on', 'emit');
    const annotationSync = new AnnotationSync(bridge, opts);
    const frameObserver = new FrameObserver(elem);
    const frameIdentifiers = new Map();

    this.pluginInit = function() {
      const onDiscoveryCallback = (source, origin, token) => bridge.createChannel(source, origin, token);

      discovery.startDiscovery(onDiscoveryCallback);

      frameObserver.observe(_injectToFrame, _iframeUnloaded);
    };

    this.destroy = function() {
      // super doesnt work here :(
      Plugin.prototype.destroy.apply(this, arguments);
      bridge.destroy();
      discovery.stopDiscovery();
      frameObserver.disconnect();
    };

    this.sync = (annotations, cb) => annotationSync.sync(annotations, cb);

    this.on = (event, fn) => bridge.on(event, fn);

    this.call = (message, ...args) => bridge.call(message, ...args);

    this.onConnect = fn => bridge.onConnect(fn);

    var _injectToFrame = function(frame) {
      if (!FrameUtil.hasHypothesis(frame)) {
        // Take the embed script location from the config
        // until an alternative solution comes around.
        const {
          clientUrl,
        } = config;

        FrameUtil.isLoaded(frame, function() {
          const subFrameIdentifier = discovery._generateToken();
          frameIdentifiers.set(frame, subFrameIdentifier);
          const injectedConfig = Object.assign({}, config, {subFrameIdentifier});

          FrameUtil.injectHypothesis(frame, clientUrl, injectedConfig);
        });
      }
    };

    var _iframeUnloaded = function(frame) {
      bridge.call('destroyFrame', frameIdentifiers.get(frame));
      frameIdentifiers.delete(frame);
    };
  }
});
