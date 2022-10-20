/*
 * decaffeinate suggestions:
 * DS206: Consider reworking classes to avoid initClass
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
const proxyquire = require('proxyquire');

const adder = require('../adder');
const {
  Observable,
} = require('../util/observable');
const Plugin = require('../plugin');

const Delegator = require('../delegator');
const $ = require('jquery');
Delegator['@noCallThru'] = true;

let Guest = null;
const anchoring = {};
const highlighter = {};
let rangeUtil = null;
let selections = null;

const raf = sinon.stub().yields();
raf['@noCallThru'] = true;

const scrollIntoView = sinon.stub();
scrollIntoView['@noCallThru'] = true;

class FakeAdder {
  static initClass() {
    this.prototype.instance = null;
  }

  constructor() {
    FakeAdder.prototype.instance = this;

    this.hide = sinon.stub();
    this.showAt = sinon.stub();
    this.target = sinon.stub();
  }
}
FakeAdder.initClass();

class FakePlugin extends Plugin {
  static initClass() {
    this.prototype.instance = null;
    this.prototype.events =
      {'customEvent': 'customEventHandler'};
  
    this.prototype.pluginInit = sinon.stub();
    this.prototype.customEventHandler = sinon.stub();
  }

  constructor() {
    super(...arguments);
    FakePlugin.prototype.instance = this;
  }
}
FakePlugin.initClass();

// A little helper which returns a promise that resolves after a timeout
const timeoutPromise = function(millis) {
  millis = millis || 0;
  return new Promise(resolve => setTimeout(resolve, millis));
};

describe('Guest', function() {
  let consoleWarnSpy = null;
  let CrossFrame = null;
  let fakeCrossFrame = null;
  let guestConfig = null;

  const createGuest = function(config) {
    config = config || {};
    config = Object.assign({}, guestConfig, config);
    const element = document.createElement('div');
    return new Guest(element, config);
  };

  beforeEach(function() {
    consoleWarnSpy = sinon.stub(console, 'warn');

    FakeAdder.prototype.instance = null;
    rangeUtil = {
      isSelectionBackwards: sinon.stub(),
      selectionFocusRect: sinon.stub(),
    };
    selections = null;
    guestConfig = {pluginClasses: {}};

    Guest = proxyquire('../guest', {
      './adder': {Adder: FakeAdder},
      './anchoring/html': anchoring,
      './highlighter': highlighter,
      './range-util': rangeUtil,
      './selections'(document) {
        return new Observable(function(obs) {
          selections = obs;
          return function() {};
        });
      },
      './delegator': Delegator,
      'raf': raf,
      'scroll-into-view': scrollIntoView,
    });

    fakeCrossFrame = {
      onConnect: sinon.stub(),
      on: sinon.stub(),
      call: sinon.stub(),
      sync: sinon.stub(),
      destroy: sinon.stub(),
    };

    CrossFrame = sinon.stub().returns(fakeCrossFrame);
    guestConfig.pluginClasses.CrossFrame = CrossFrame;
  });

  afterEach(function() {
    sinon.restore();
    consoleWarnSpy.restore();
  });

  describe('plugins', function() {
    let fakePlugin = null;
    let guest = null;

    beforeEach(function() {
      FakePlugin.prototype.instance = null;
      guestConfig.pluginClasses.FakePlugin = FakePlugin;
      guest = createGuest({FakePlugin: {}});
      fakePlugin = FakePlugin.prototype.instance;
    });

    it('load and "pluginInit" gets called', () => assert.calledOnce(fakePlugin.pluginInit));

    it('hold reference to instance', () => assert.equal(fakePlugin.annotator, guest));

    it('subscribe to events', function() {
      guest.publish('customEvent', ['1', '2']);
      assert.calledWith(fakePlugin.customEventHandler, '1', '2');
    });

    it('destroy when instance is destroyed', function() {
      sinon.spy(fakePlugin, 'destroy');
      guest.destroy();
      assert.called(fakePlugin.destroy);
    });
  });

  describe('cross frame', function() {

    it('provides an event bus for the annotation sync module', function() {
      const guest = createGuest();
      const options = CrossFrame.lastCall.args[1];
      assert.isFunction(options.on);
      assert.isFunction(options.emit);
    });

    it('publishes the "panelReady" event when a connection is established', function() {
      const handler = sinon.stub();
      const guest = createGuest();
      guest.subscribe('panelReady', handler);
      fakeCrossFrame.onConnect.yield();
      assert.called(handler);
    });

    describe('event subscription', function() {
      let options = null;
      let guest = null;

      beforeEach(function() {
        guest = createGuest();
        return options = CrossFrame.lastCall.args[1];});

      it('proxies the event into the annotator event system', function() {
        const fooHandler = sinon.stub();
        const barHandler = sinon.stub();

        options.on('foo', fooHandler);
        options.on('bar', barHandler);

        guest.publish('foo', ['1', '2']);
        guest.publish('bar', ['1', '2']);

        assert.calledWith(fooHandler, '1', '2');
        assert.calledWith(barHandler, '1', '2');
      });
    });

    describe('event publication', function() {
      let options = null;
      let guest = null;

      beforeEach(function() {
        guest = createGuest();
        return options = CrossFrame.lastCall.args[1];});

      it('detaches annotations on "annotationDeleted"', function() {
        const ann = {id: 1, $tag: 'tag1'};
        sinon.stub(guest, 'detach');
        options.emit('annotationDeleted', ann);
        assert.calledOnce(guest.detach);
        assert.calledWith(guest.detach, ann);
      });

      it('anchors annotations on "annotationsLoaded"', function() {
        const ann1 = {id: 1, $tag: 'tag1'};
        const ann2 = {id: 2, $tag: 'tag2'};
        sinon.stub(guest, 'anchor');
        options.emit('annotationsLoaded', [ann1, ann2]);
        assert.calledTwice(guest.anchor);
        assert.calledWith(guest.anchor, ann1);
        assert.calledWith(guest.anchor, ann2);
      });

      it('proxies all other events into the annotator event system', function() {
        const fooHandler = sinon.stub();
        const barHandler = sinon.stub();

        guest.subscribe('foo', fooHandler);
        guest.subscribe('bar', barHandler);

        options.emit('foo', '1', '2');
        options.emit('bar', '1', '2');

        assert.calledWith(fooHandler, '1', '2');
        assert.calledWith(barHandler, '1', '2');
      });
    });
  });

  describe('annotation UI events', function() {
    // TODO: Check if IIFE is properly transformed/resolved
    const emitGuestEvent = (event, ...args) => {
      const result = [];
      for (let [evt, fn] of fakeCrossFrame.on.args) {
        if (event === evt) {
          result.push(fn(...(args || [])));
        }
      }
      return result;
    };

    describe('on "scrollToAnnotation" event', function() {

      beforeEach(() => scrollIntoView.resetHistory());

      it('scrolls to the anchor with the matching tag', function() {
        const highlight = $('<span></span>');
        const guest = createGuest();
        guest.anchors = [
          {annotation: {$tag: 'tag1'}, highlights: highlight.toArray()},
        ];
        emitGuestEvent('scrollToAnnotation', 'tag1');
        assert.called(scrollIntoView);
        assert.calledWith(scrollIntoView, highlight[0]);
      });

      context('when dispatching the "scrolltorange" event', function() {

        it('emits with the range', function() {
          const highlight = $('<span></span>');
          const guest = createGuest();
          const fakeRange = sinon.stub();
          guest.anchors = [
            {annotation: {$tag: 'tag1'}, highlights: highlight.toArray(), range: fakeRange},
          ];

          return new Promise(function(resolve) {
            guest.element.on('scrolltorange', function(event) {
              assert.equal(event.detail, fakeRange);
              return resolve();
            });

            emitGuestEvent('scrollToAnnotation', 'tag1');
          });
        });

        it('allows the default scroll behaviour to be prevented', function() {
          const highlight = $('<span></span>');
          const guest = createGuest();
          const fakeRange = sinon.stub();
          guest.anchors = [
            {annotation: {$tag: 'tag1'}, highlights: highlight.toArray(), range: fakeRange},
          ];

          guest.element.on('scrolltorange', event => event.preventDefault());
          emitGuestEvent('scrollToAnnotation', 'tag1');
          assert.notCalled(scrollIntoView);
        });
      });
    });


    describe('on "getDocumentInfo" event', function() {
      let guest = null;

      beforeEach(function() {
        document.title = 'hi';
        guest = createGuest();
        guest.plugins.PDF = {
          uri: sinon.stub().returns(window.location.href),
          getMetadata: sinon.stub(),
        };
      });

      afterEach(() => sinon.restore());

      it('calls the callback with the href and pdf metadata', function(done) {
        const assertComplete = function(err, payload) {
          try {
            assert.equal(payload.uri, document.location.href);
            assert.equal(payload.metadata, metadata);
            return done();
          } catch (e) {
            return done(e);
          }
        };

        var metadata = {title: 'hi'};
        const promise = Promise.resolve(metadata);
        guest.plugins.PDF.getMetadata.returns(promise);

        emitGuestEvent('getDocumentInfo', assertComplete);
      });

      it('calls the callback with the href and basic metadata if pdf fails', function(done) {
        const assertComplete = function(err, payload) {
          try {
            assert.equal(payload.uri, window.location.href);
            assert.deepEqual(payload.metadata, metadata);
            return done();
          } catch (e) {
            return done(e);
          }
        };

        var metadata = {title: 'hi', link: [{href: window.location.href}]};
        const promise = Promise.reject(new Error('Not a PDF document'));
        guest.plugins.PDF.getMetadata.returns(promise);

        emitGuestEvent('getDocumentInfo', assertComplete);
      });
    });
  });

  describe('when the selection changes', function() {
    it('shows the adder if the selection contains text', function() {
      const guest = createGuest();
      rangeUtil.selectionFocusRect.returns({left: 0, top: 0, width: 5, height: 5});
      FakeAdder.prototype.instance.target.returns({
        left: 0, top: 0, arrowDirection: adder.ARROW_POINTING_UP,
      });
      selections.next({});
      assert.called(FakeAdder.prototype.instance.showAt);
    });

    it('hides the adder if the selection does not contain text', function() {
      const guest = createGuest();
      rangeUtil.selectionFocusRect.returns(null);
      selections.next({});
      assert.called(FakeAdder.prototype.instance.hide);
      assert.notCalled(FakeAdder.prototype.instance.showAt);
    });

    it('hides the adder if the selection is empty', function() {
      const guest = createGuest();
      selections.next(null);
      assert.called(FakeAdder.prototype.instance.hide);
    });
  });

  describe('#getDocumentInfo()', function() {
    let guest = null;

    beforeEach(function() {
      guest = createGuest();
      guest.plugins.PDF = {
        uri() { return 'urn:x-pdf:001122'; },
        getMetadata: sinon.stub(),
      };
    });

    it('preserves the components of the URI other than the fragment', function() {
      guest.plugins.PDF = null;
      guest.plugins.Document = {
        uri() { return 'http://foobar.com/things?id=42'; },
        metadata: {},
      };
      guest.getDocumentInfo().then(({uri}) => assert.equal(uri, 'http://foobar.com/things?id=42'));
    });

    it('removes the fragment identifier from URIs', function() {
      guest.plugins.PDF.uri = () => 'urn:x-pdf:aabbcc#';
      guest.getDocumentInfo().then(({uri}) => assert.equal(uri, 'urn:x-pdf:aabbcc'));
    });
  });

  describe('#createAnnotation()', function() {
    it('adds metadata to the annotation object', function() {
      const guest = createGuest();
      sinon.stub(guest, 'getDocumentInfo').returns(Promise.resolve({
        metadata: {title: 'hello'},
        uri: 'http://example.com/',
      }));
      const annotation = {};

      guest.createAnnotation(annotation);

      timeoutPromise()
        .then(function() {
          assert.equal(annotation.uri, 'http://example.com/');
          assert.deepEqual(annotation.document, {title: 'hello'});
        });
    });

    it('treats an argument as the annotation object', function() {
      const guest = createGuest();
      let annotation = {foo: 'bar'};
      annotation = guest.createAnnotation(annotation);
      assert.equal(annotation.foo, 'bar');
    });

    it('triggers a beforeAnnotationCreated event', function(done) {
      const guest = createGuest();
      guest.subscribe('beforeAnnotationCreated', () => done());

      guest.createAnnotation();
    });
  });

  describe('#createComment()', function() {
    it('adds metadata to the annotation object', function() {
      const guest = createGuest();
      sinon.stub(guest, 'getDocumentInfo').returns(Promise.resolve({
        metadata: {title: 'hello'},
        uri: 'http://example.com/',
      }));

      const annotation = guest.createComment();

      timeoutPromise()
        .then(function() {
          assert.equal(annotation.uri, 'http://example.com/');
          assert.deepEqual(annotation.document, {title: 'hello'});
        });
    });

    it('adds a single target with a source property', function() {
      const guest = createGuest();
      sinon.stub(guest, 'getDocumentInfo').returns(Promise.resolve({
        metadata: {title: 'hello'},
        uri: 'http://example.com/',
      }));

      const annotation = guest.createComment();

      timeoutPromise()
        .then(() => assert.deepEqual(annotation.target, [{source: 'http://example.com/'}]));
    });

    it('triggers a beforeAnnotationCreated event', function(done) {
      const guest = createGuest();
      guest.subscribe('beforeAnnotationCreated', () => done());

      guest.createComment();
    });
  });

  describe('#anchor()', function() {
    let el = null;
    let txt = null;
    let range = null;

    beforeEach(function() {
      el = document.createElement('span');
      txt = document.createTextNode('hello');
      txt.id = 'test-element';
      el.appendChild(txt);
      document.body.appendChild(el);
      range = document.createRange();
      return range.selectNode(el);
    });

    afterEach(() => document.body.removeChild(el));

    it("doesn't mark an annotation lacking targets as an orphan", function() {
      const guest = createGuest();
      const annotation = {target: []};

      guest.anchor(annotation).then(() => assert.isFalse(annotation.$orphan));
    });

    it("doesn't mark an annotation with a selectorless target as an orphan", function() {
      const guest = createGuest();
      const annotation = {target: [{source: 'wibble'}]};

      guest.anchor(annotation).then(() => assert.isFalse(annotation.$orphan));
    });

    it("doesn't mark an annotation with only selectorless targets as an orphan", function() {
      const guest = createGuest();
      const annotation = {target: [{source: 'foo'}, {source: 'bar'}]};

      guest.anchor(annotation).then(() => assert.isFalse(annotation.$orphan));
    });

    it("doesn't mark an annotation in which the target anchors as an orphan", function() {
      const guest = createGuest();
      const annotation = {
        target: [
          {selector: [{type: 'TextQuoteSelector', exact: 'hello'}]},
        ],
      };
      sinon.stub(anchoring, 'anchor').returns(Promise.resolve(range));

      guest.anchor(annotation).then(() => assert.isFalse(annotation.$orphan));
    });

    it("doesn't mark an annotation in which at least one target anchors as an orphan", function() {
      const guest = createGuest();
      const annotation = {
        target: [
          {selector: [{type: 'TextQuoteSelector', exact: 'notinhere'}]},
          {selector: [{type: 'TextQuoteSelector', exact: 'hello'}]},
        ],
      };
      sinon.stub(anchoring, 'anchor')
        .onFirstCall().returns(Promise.reject())
        .onSecondCall().returns(Promise.resolve(range));

      guest.anchor(annotation).then(() => assert.isFalse(annotation.$orphan));
    });

    it('marks an annotation in which the target fails to anchor as an orphan', function() {
      const guest = createGuest();
      const annotation = {
        target: [
          {selector: [{type: 'TextQuoteSelector', exact: 'notinhere'}]},
        ],
      };
      sinon.stub(anchoring, 'anchor').returns(Promise.reject());

      guest.anchor(annotation).then(() => assert.isTrue(annotation.$orphan));
    });

    it('marks an annotation in which all (suitable) targets fail to anchor as an orphan', function() {
      const guest = createGuest();
      const annotation = {
        target: [
          {selector: [{type: 'TextQuoteSelector', exact: 'notinhere'}]},
          {selector: [{type: 'TextQuoteSelector', exact: 'neitherami'}]},
        ],
      };
      sinon.stub(anchoring, 'anchor').returns(Promise.reject());

      guest.anchor(annotation).then(() => assert.isTrue(annotation.$orphan));
    });

    it('marks an annotation where the target has no TextQuoteSelectors as an orphan', function() {
      const guest = createGuest();
      const annotation = {
        target: [
          {selector: [{type: 'TextPositionSelector', start: 0, end: 5}]},
        ],
      };
      // This shouldn't be called, but if it is, we successfully anchor so that
      // this test is guaranteed to fail.
      sinon.stub(anchoring, 'anchor').returns(Promise.resolve(range));

      guest.anchor(annotation).then(() => assert.isTrue(annotation.$orphan));
    });

    it('#removeRangeIgnoringPrefix(): mark a non locatable comment/annotation as an orphan', function() {
      const guest = createGuest();
      const annotation = {
        tags: [
          'comment',
        ],
        target: [
          {selector: [{type: 'LeosSelector', id: 'test-element', exact: 'ell', prefix: 'i', suffix: 'o', start: 1, end: 4}]},
        ],
      };

      range = document.createRange();
      range.setStart(txt, 1);
      range.setEnd(txt, 4);
      sinon.stub(anchoring, 'anchor').returns(Promise.resolve(range));

      guest.anchor(annotation).then(() => assert.isTrue(annotation.$orphan));
    });

    // This test is commented in the development branch
    it('#removeRangeWithoutExactTextMatch(): mark a locatable comment/annotation (but containing modified text though) as an orphan', function() {
      const guest = createGuest();
      const annotation = {
        tags: [
          'comment',
        ],
        target: [
          {selector: [{type: 'LeosSelector', id: 'test-element', exact: 'elx', prefix: 'h', suffix: 'o', start: 1, end: 4}]},
          {selector: [{type: 'TextQuoteSelector', exact: 'elx'}]},
        ],
      };

      range = document.createRange();
      range.setStart(txt, 1);
      range.setEnd(txt, 4);

      sinon.stub(anchoring, 'anchor').returns(Promise.resolve(range));

      guest.anchor(annotation).then(() => assert.isFalse(annotation.$orphan));
    });

    // This test is commented in the development branch
    it("#removeRangeWithoutExactTextMatch(): don't mark a valid suggestion as an orphan", function() {
      const guest = createGuest();
      const annotation = {
        tags: [
          'suggestion',
        ],
        target: [
          {selector: [{type: 'LeosSelector', id: 'test-element', exact: 'ell', prefix: 'h', suffix: 'o', start: 1, end: 4}]},
          {selector: [{type: 'TextQuoteSelector', exact: 'ell'}]},
        ],
      };

      range = document.createRange();
      range.setStart(txt, 1);
      range.setEnd(txt, 4);

      sinon.stub(anchoring, 'anchor').returns(Promise.resolve(range));

      guest.anchor(annotation).then(() => assert.isTrue(annotation.$orphan));
    });

    it('#removeRangeWithoutExactTextMatch(): mark a locatable suggestion (but containing modified text though) as an orphan', function() {
      const guest = createGuest();
      const annotation = {
        tags: [
          'suggestion',
        ],
        target: [
          {selector: [{type: 'LeosSelector', id: 'test-element', exact: 'elx', prefix: 'h', suffix: 'o', start: 1, end: 4}]},
          {selector: [{type: 'TextQuoteSelector', exact: 'elx'}]},
        ],
      };

      sinon.stub(anchoring, 'anchor').returns(Promise.reject());

      guest.anchor(annotation).then(() => assert.isTrue(annotation.$orphan));
    });

    it('does not attempt to anchor targets which have no TextQuoteSelector', function() {
      const guest = createGuest();
      const annotation = {
        target: [
          {selector: [{type: 'TextPositionSelector', start: 0, end: 5}]},
        ],
      };
      sinon.spy(anchoring, 'anchor');

      guest.anchor(annotation).then(() => assert.notCalled(anchoring.anchor));
    });

    it('updates the cross frame and bucket bar plugins', function(done) {
      const guest = createGuest();
      guest.plugins.CrossFrame =
        {sync: sinon.stub()};
      guest.plugins.BucketBar =
        {update: sinon.stub()};
      const annotation = {};
      guest.anchor(annotation).then(function() {
        assert.called(guest.plugins.BucketBar.update);
        assert.called(guest.plugins.CrossFrame.sync);}).then(done, done);
    });

    it('returns a promise of the anchors for the annotation', function(done) {
      const guest = createGuest();
      const highlights = [document.createElement('span')];
      sinon.stub(anchoring, 'anchor').returns(Promise.resolve(range));
      sinon.stub(highlighter, 'highlightRange').returns(highlights);
      const target = {selector: [{type: 'TextQuoteSelector', exact: 'hello'}]};
      guest.anchor({target: [target]}).then(anchors => assert.equal(anchors.length, 1)).then(done, done);
    });

    it('adds the anchor to the "anchors" instance property"', function(done) {
      const guest = createGuest();
      const highlights = [document.createElement('span')];
      sinon.stub(anchoring, 'anchor').returns(Promise.resolve(range));
      sinon.stub(highlighter, 'highlightRange').returns(highlights);
      const target = {selector: [{type: 'TextQuoteSelector', exact: 'hello'}]};
      const annotation = {target: [target]};
      guest.anchor(annotation).then(function() {
        assert.equal(guest.anchors.length, 1);
        assert.strictEqual(guest.anchors[0].annotation, annotation);
        assert.strictEqual(guest.anchors[0].target, target);
        assert.strictEqual(guest.anchors[0].range, range);
        assert.strictEqual(guest.anchors[0].highlights, highlights);}).then(done, done);
    });

    it('destroys targets that have been removed from the annotation', function(done) {
      const annotation = {};
      const target = {};
      const highlights = [];
      const guest = createGuest();
      guest.anchors = [{annotation, target, highlights}];
      const removeHighlights = sinon.stub(highlighter, 'removeHighlights');

      guest.anchor(annotation).then(function() {
        assert.equal(guest.anchors.length, 0);
        assert.calledOnce(removeHighlights);
        assert.calledWith(removeHighlights, highlights);}).then(done, done);
    });

    it('does not reanchor targets that are already anchored', function(done) {
      const guest = createGuest();
      const annotation = {target: [{selector: [{type: 'TextQuoteSelector', exact: 'hello'}]}]};
      const stub = sinon.stub(anchoring, 'anchor').returns(Promise.resolve(range));
      guest.anchor(annotation).then(() => guest.anchor(annotation).then(function() {
        assert.equal(guest.anchors.length, 1);
        assert.calledOnce(stub);
      })).then(done, done);
    });
  });

  describe('#detach()', function() {
    it('removes the anchors from the "anchors" instance variable', function() {
      const guest = createGuest();
      const annotation = {};
      guest.anchors.push({annotation});
      guest.detach(annotation);
      assert.equal(guest.anchors.length, 0);
    });

    it('updates the bucket bar plugin', function() {
      const guest = createGuest();
      guest.plugins.BucketBar = {update: sinon.stub()};
      const annotation = {};

      guest.anchors.push({annotation});
      guest.detach(annotation);
      assert.calledOnce(guest.plugins.BucketBar.update);
    });

    it('publishes the "annotationDeleted" event', function() {
      const guest = createGuest();
      const annotation = {};
      const publish = sinon.stub(guest, 'publish');

      guest.deleteAnnotation(annotation);

      assert.calledOnce(publish);
      assert.calledWith(publish, 'annotationDeleted', [annotation]);
    });

    it('removes any highlights associated with the annotation', function() {
      const guest = createGuest();
      const annotation = {};
      const highlights = [document.createElement('span')];
      const removeHighlights = sinon.stub(highlighter, 'removeHighlights');

      guest.anchors.push({annotation, highlights});
      guest.deleteAnnotation(annotation);

      assert.calledOnce(removeHighlights);
      assert.calledWith(removeHighlights, highlights);
    });
  });
});
