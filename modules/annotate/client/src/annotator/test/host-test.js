const proxyquire = require('proxyquire');
const Host = proxyquire('../host', {});

describe('Host', function() {
  let CrossFrame = null;
  let fakeCrossFrame = null;
  const hostConfig = {pluginClasses: {}};

  const createHost = function(config, element=null) {
    config = config || {};
    config = Object.assign({sidebarAppUrl: '/base/annotator/test/empty.html'}, hostConfig, config);
    if (!element) {
      element = document.createElement('div');
    }
    return new Host(element, config);
  };

  beforeEach(function() {
    // Disable any Host logging.
    sinon.stub(console, 'log');

    fakeCrossFrame = {};
    fakeCrossFrame.onConnect = sinon.stub().returns(fakeCrossFrame);
    fakeCrossFrame.on = sinon.stub().returns(fakeCrossFrame);
    fakeCrossFrame.call = sinon.spy();

    CrossFrame = sinon.stub();
    CrossFrame.returns(fakeCrossFrame);
    hostConfig.pluginClasses.CrossFrame = CrossFrame;
  });

  afterEach(() => sinon.restore());

  describe('widget visibility', function() {
    it('starts hidden', function() {
      const host = createHost();
      assert.equal(host.frame.css('display'), 'none');
    });

    it('becomes visible when the "panelReady" event fires', function() {
      const host = createHost();
      host.publish('panelReady');
      assert.equal(host.frame.css('display'), '');
    });
  });

  describe('focus', function() {
    let element = null;
    let frame = null;
    let host = null;

    beforeEach(function() {
      element = document.createElement('div');
      document.body.appendChild(element);
      host = createHost({}, element);
      frame = element.querySelector('[name=hyp_sidebar_frame]');
      sinon.spy(frame.contentWindow, 'focus');
    });

    afterEach(function() {
      frame.contentWindow.focus.restore();
      element.parentNode.removeChild(element);
    });

    it('focuses the sidebar when a new annotation is created', function() {
      host.publish('beforeAnnotationCreated', [{
        $highlight: false,
      }]);
      assert.called(frame.contentWindow.focus);
    });

    it('does not focus the sidebar when a new highlight is created', function() {
      host.publish('beforeAnnotationCreated', [{
        $highlight: true,
      }]);
      assert.notCalled(frame.contentWindow.focus);
    });
  });

  describe('config', function() {
    it('disables highlighting if showHighlights: false is given', function(done) {
      const host = createHost({showHighlights: false});
      host.on('panelReady', function() {
        assert.isFalse(host.visibleHighlights);
        done();
      });
      host.publish('panelReady');
    });

    it('passes config to the sidebar iframe', function() {
      const appURL = new URL('/base/annotator/test/empty.html', window.location.href);
      const host = createHost({annotations: '1234'});
      const configStr = encodeURIComponent(JSON.stringify({annotations: '1234'}));
      assert.equal(host.frame[0].children[0].src, appURL + '?config=' + configStr);
    });

    it('adds drop shadow if the clean theme is enabled', function() {
      const host = createHost({theme: 'clean'});
      assert.isTrue(host.frame.hasClass('annotator-frame--drop-shadow-enabled'));
    });
  });
});
