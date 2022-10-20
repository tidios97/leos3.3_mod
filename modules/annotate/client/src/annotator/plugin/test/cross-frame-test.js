const proxyquire = require('proxyquire');

const Plugin = require('../../plugin');
let CrossFrame = null;

describe('CrossFrame', function() {
  let fakeDiscovery = null;
  let fakeBridge = null;
  let fakeAnnotationSync = null;

  let proxyDiscovery = null;
  let proxyBridge = null;
  let proxyAnnotationSync = null;

  const createCrossFrame = function(options) {
    const defaults = {
      config: {},
      on: sinon.stub(),
      emit: sinon.stub(),
    };
    const element = document.createElement('div');
    return new CrossFrame(element, $.extend({}, defaults, options));
  };

  beforeEach(function() {
    fakeDiscovery = {
      startDiscovery: sinon.stub(),
      stopDiscovery: sinon.stub(),
    };

    fakeBridge = {
      destroy: sinon.stub(),
      createChannel: sinon.stub(),
      onConnect: sinon.stub(),
      call: sinon.stub(),
      on: sinon.stub(),
    };

    fakeAnnotationSync =
      {sync: sinon.stub()};

    proxyAnnotationSync = sinon.stub().returns(fakeAnnotationSync);
    proxyDiscovery = sinon.stub().returns(fakeDiscovery);
    proxyBridge = sinon.stub().returns(fakeBridge);

    return CrossFrame = proxyquire('../cross-frame', {
      '../plugin': Plugin,
      '../annotation-sync': proxyAnnotationSync,
      '../../shared/bridge': proxyBridge,
      '../../shared/discovery': proxyDiscovery,
    });
  });


  afterEach(() => sinon.restore());

  describe('CrossFrame constructor', function() {
    it('instantiates the Discovery component', function() {
      createCrossFrame();
      assert.calledWith(proxyDiscovery, window);
    });

    it('passes the options along to the bridge', function() {
      createCrossFrame({server: true});
      assert.calledWith(proxyDiscovery, window, {server: true});
    });

    it('instantiates the CrossFrame component', function() {
      createCrossFrame();
      assert.calledWith(proxyDiscovery);
    });

    it('instantiates the AnnotationSync component', function() {
      createCrossFrame();
      assert.called(proxyAnnotationSync);
    });

    it('passes along options to AnnotationSync', function() {
      createCrossFrame();
      assert.calledWith(proxyAnnotationSync, fakeBridge, {
        on: sinon.match.func,
        emit: sinon.match.func,
      });
    });
  });

  describe('.pluginInit', function() {
    it('starts the discovery of new channels', function() {
      const bridge = createCrossFrame();
      bridge.pluginInit();
      assert.called(fakeDiscovery.startDiscovery);
    });

    it('creates a channel when a new frame is discovered', function() {
      const bridge = createCrossFrame();
      bridge.pluginInit();
      fakeDiscovery.startDiscovery.yield('SOURCE', 'ORIGIN', 'TOKEN');
      assert.called(fakeBridge.createChannel);
      assert.calledWith(fakeBridge.createChannel, 'SOURCE', 'ORIGIN', 'TOKEN');
    });
  });

  describe('.destroy', function() {
    it('stops the discovery of new frames', function() {
      const cf = createCrossFrame();
      cf.destroy();
      assert.called(fakeDiscovery.stopDiscovery);
    });

    it('destroys the bridge object', function() {
      const cf = createCrossFrame();
      cf.destroy();
      assert.called(fakeBridge.destroy);
    });
  });

  describe('.sync', () => it('syncs the annotations with the other frame', function() {
    const bridge = createCrossFrame();
    bridge.sync();
    assert.called(fakeAnnotationSync.sync);
  }));

  describe('.on', () => it('proxies the call to the bridge', function() {
    const bridge = createCrossFrame();
    bridge.on('event', 'arg');
    assert.calledWith(fakeBridge.on, 'event', 'arg');
  }));

  describe('.call', () => it('proxies the call to the bridge', function() {
    const bridge = createCrossFrame();
    bridge.call('method', 'arg1', 'arg2');
    assert.calledWith(fakeBridge.call, 'method', 'arg1', 'arg2');
  }));

  describe('.onConnect', () => it('proxies the call to the bridge', function() {
    const bridge = createCrossFrame();
    const fn = function() {};
    bridge.onConnect(fn);
    assert.calledWith(fakeBridge.onConnect, fn);
  }));
});
