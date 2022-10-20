const {module, inject} = angular.mock;

describe('Discovery', function() {
  let fakeTopWindow = null;
  let fakeFrameWindow = null;
  let createDiscovery = null;

  before(() => angular.module('h', [])
    .value('Discovery', require('../discovery')));

  beforeEach(module('h'));
  beforeEach(inject(function(Discovery) {
    createDiscovery = (win, options) => new Discovery(win, options);

    const createWindow = () => ({
      top: null,
      addEventListener: sinon.stub(),
      removeEventListener: sinon.stub(),
      postMessage: sinon.stub(),
      length: 0,
      frames: [],
    });

    fakeTopWindow = createWindow();
    fakeTopWindow.top = fakeTopWindow;

    fakeFrameWindow = createWindow();
    fakeFrameWindow.top = fakeTopWindow;

    fakeTopWindow.frames = [fakeFrameWindow];}));

  afterEach(() => sinon.restore());

  describe('startDiscovery', () => it('adds a "message" listener to the window object', function() {
    const discovery = createDiscovery(fakeTopWindow);
    discovery.startDiscovery(function() {});
    assert.called(fakeTopWindow.addEventListener);
    assert.calledWith(fakeTopWindow.addEventListener, 'message', sinon.match.func, false);
  }));

  describe('when acting as a server (options.server = true)', function() {
    let server = null;

    beforeEach(() => server = createDiscovery(fakeFrameWindow, {server: true}));

    it('sends out a "offer" message to every frame', function() {
      server.startDiscovery(function() {});
      assert.called(fakeTopWindow.postMessage);
      assert.calledWith(fakeTopWindow.postMessage, '__cross_frame_dhcp_offer', '*');
    });

    it('allows the origin to be provided', function() {
      server = createDiscovery(fakeFrameWindow, {server: true, origin: 'foo'});
      server.startDiscovery(function() {});
      assert.called(fakeTopWindow.postMessage);
      assert.calledWith(fakeTopWindow.postMessage, '__cross_frame_dhcp_offer', 'foo');
    });

    it('does not send the message to itself', function() {
      server.startDiscovery(function() {});
      assert.notCalled(fakeFrameWindow.postMessage);
    });

    it('sends an "ack" on receiving a "request"', function() {
      fakeFrameWindow.addEventListener.yields({
        data: '__cross_frame_dhcp_request',
        source: fakeTopWindow,
        origin: 'top',
      });
      server.startDiscovery(function() {});

      assert.called(fakeTopWindow.postMessage);
      const matcher = sinon.match(/__cross_frame_dhcp_ack:\d+/);
      assert.calledWith(fakeTopWindow.postMessage, matcher, 'top');
    });

    it('sends an "ack" to the wildcard origin if a request comes from a frame with null origin', function() {
      fakeFrameWindow.addEventListener.yields({
        data: '__cross_frame_dhcp_request',
        source: fakeTopWindow,
        origin: 'null',
      });
      server.startDiscovery(function() {});

      assert.called(fakeTopWindow.postMessage);
      const matcher = sinon.match(/__cross_frame_dhcp_ack:\d+/);
      assert.calledWith(fakeTopWindow.postMessage, matcher, '*');
    });

    it('calls the discovery callback on receiving "request"', function() {
      fakeFrameWindow.addEventListener.yields({
        data: '__cross_frame_dhcp_request',
        source: fakeTopWindow,
        origin: 'top',
      });
      const handler = sinon.stub();
      server.startDiscovery(handler);
      assert.called(handler);
      assert.calledWith(handler, fakeTopWindow, 'top', sinon.match(/\d+/));
    });

    it('raises an error if it recieves an event from another server', function() {
      fakeFrameWindow.addEventListener.yields({
        data: '__cross_frame_dhcp_offer',
        source: fakeTopWindow,
        origin: 'top',
      });
      const handler = sinon.stub();
      assert.throws(() => server.startDiscovery(handler));
    });
  });

  describe('when acting as a client (options.client = false)', function() {
    let client = null;

    beforeEach(() => client = createDiscovery(fakeTopWindow));

    it('sends out a discovery message to every frame', function() {
      client.startDiscovery(function() {});
      assert.called(fakeFrameWindow.postMessage);
      assert.calledWith(fakeFrameWindow.postMessage, '__cross_frame_dhcp_discovery', '*');
    });

    it('does not send the message to itself', function() {
      client.startDiscovery(function() {});
      assert.notCalled(fakeTopWindow.postMessage);
    });

    it('sends a "request" in response to an "offer"', function() {
      fakeTopWindow.addEventListener.yields({
        data: '__cross_frame_dhcp_offer',
        source: fakeFrameWindow,
        origin: 'iframe',
      });
      client.startDiscovery(function() {});

      assert.called(fakeFrameWindow.postMessage);
      assert.calledWith(fakeFrameWindow.postMessage, '__cross_frame_dhcp_request', 'iframe');
    });

    it('does not respond to an "offer" if a "request" is already in progress', function() {
      fakeTopWindow.addEventListener.yields({
        data: '__cross_frame_dhcp_offer',
        source: fakeFrameWindow,
        origin: 'iframe1',
      });
      fakeTopWindow.addEventListener.yields({
        data: '__cross_frame_dhcp_offer',
        source: fakeFrameWindow,
        origin: 'iframe2',
      });
      client.startDiscovery(function() {});

      // Twice, once for discovery, once for offer.
      assert.calledTwice(fakeFrameWindow.postMessage);
      const {
        lastCall,
      } = fakeFrameWindow.postMessage;
      assert(lastCall.notCalledWith(sinon.match.string, 'iframe2'));
    });

    it('allows responding to a "request" once a previous "request" has completed', function() {
      fakeTopWindow.addEventListener.yields({
        data: '__cross_frame_dhcp_offer',
        source: fakeFrameWindow,
        origin: 'iframe1',
      });
      fakeTopWindow.addEventListener.yields({
        data: '__cross_frame_dhcp_ack:1234',
        source: fakeFrameWindow,
        origin: 'iframe1',
      });
      fakeTopWindow.addEventListener.yields({
        data: '__cross_frame_dhcp_offer',
        source: fakeFrameWindow,
        origin: 'iframe2',
      });
      client.startDiscovery(function() {});

      assert.called(fakeFrameWindow.postMessage);
      assert.calledWith(fakeFrameWindow.postMessage, '__cross_frame_dhcp_request', 'iframe2');
    });

    it('calls the discovery callback on receiving an "ack"', function() {
      fakeTopWindow.addEventListener.yields({
        data: '__cross_frame_dhcp_ack:1234',
        source: fakeFrameWindow,
        origin: 'iframe',
      });
      const callback = sinon.stub();
      client.startDiscovery(callback);

      assert.called(callback);
      assert.calledWith(callback, fakeFrameWindow, 'iframe', '1234');
    });
  });

  describe('stopDiscovery', function() {
    it('removes the "message" listener from the window', function() {
      const discovery = createDiscovery(fakeFrameWindow);
      discovery.startDiscovery();
      discovery.stopDiscovery();

      const handler = fakeFrameWindow.addEventListener.lastCall.args[1];
      assert.called(fakeFrameWindow.removeEventListener);
      assert.calledWith(fakeFrameWindow.removeEventListener, 'message', handler);
    });

    it('allows startDiscovery to be called with a new handler', function() {
      const discovery = createDiscovery(fakeFrameWindow);
      discovery.startDiscovery();
      discovery.stopDiscovery();

      assert.doesNotThrow(() => discovery.startDiscovery());
    });
  });
});
