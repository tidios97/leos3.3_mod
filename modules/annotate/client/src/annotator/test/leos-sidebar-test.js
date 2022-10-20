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

const Delegator = require('../delegator');
const $ = require('jquery');
Delegator['@noCallThru'] = true;

let LeosSidebar = null;
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
    this.extend = sinon.stub();
  }
}
FakeAdder.initClass();

describe('LEOS Sidebar', function() {
  let consoleWarnSpy = null;
  let CrossFrame = null;
  let fakeCrossFrame = null;
  let guestConfig = null;

  const createGuest = function(config) {
    config = config || {};
    config = Object.assign({}, guestConfig, config);
    const element = document.createElement('div');
    return new LeosSidebar(element, config);
  };

  beforeEach(function() {
    consoleWarnSpy = sinon.stub(console, 'warn');

    FakeAdder.prototype.instance = null;
    rangeUtil = {
      isSelectionBackwards: sinon.stub(),
      selectionFocusRect: sinon.stub(),
    };
    selections = null;
    guestConfig = {
      annotationContainer: '#docContainer',
      clientUrl: 'http://example.com/app.html',
      leosDocumentRootNode: 'akomantoso',
      pluginClasses: {},
      services: [{
        authority: 'hypothes.is',
      }],
    };

    LeosSidebar = proxyquire('../leos-sidebar', {
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

    const fakeToolbar = {
      destroy: sinon.stub(),
      disableCloseBtn: sinon.spy(),
      disableGuideLinesBtn: sinon.spy(),
      disableHighlightsBtn: sinon.spy(),
      disableMinimizeBtn: sinon.spy(),
      disableNewNoteBtn: sinon.spy(),
      enableAnnotPopupBtn: sinon.spy(),
      enableNewNoteBtn: sinon.spy(),
      getWidth: sinon.stub(),
      hideCloseBtn: sinon.spy(),
      showCloseBtn: sinon.spy(),
      showCollapseSidebarBtn: sinon.spy(),
      showExpandSidebarBtn: sinon.spy(),
    };
    
    const Toolbar = sinon.stub();
    Toolbar.returns(fakeToolbar);

    guestConfig.pluginClasses.Toolbar = Toolbar;
  });
    
  afterEach(function() {
    sinon.restore();
    consoleWarnSpy.restore();
  });
  
  describe('annotation UI events', function() {
    const emitGuestEvent = (event, ...args) => {
      for (let [evt, fn] of fakeCrossFrame.on.args) {
        if (event === evt) {
          fn(...(args || []));
        }
      }
    };

    describe('on "focusAnnotations" event', function() {
      it('focuses any annotations with a matching tag', function() {
        const highlight0 = $('<span></span>');
        const highlight1 = $('<span></span>');
        const guest = createGuest();
        guest.anchors = [
          {annotation: {$tag: 'tag1'}, highlights: highlight0.toArray()},
          {annotation: {$tag: 'tag2'}, highlights: highlight1.toArray()},
        ];
        emitGuestEvent('focusAnnotations', ['tag1']);
        assert.isTrue(highlight0.hasClass('annotator-hl-focused'));
      });

      it('unfocuses any annotations without a matching tag', function() {
        const highlight0 = $('<span class="annotator-hl-focused"></span>');
        const highlight1 = $('<span class="annotator-hl-focused"></span>');
        const guest = createGuest();
        guest.anchors = [
          {annotation: {$tag: 'tag1'}, highlights: highlight0.toArray()},
          {annotation: {$tag: 'tag2'}, highlights: highlight1.toArray()},
        ];
        emitGuestEvent('focusAnnotations', 'ctx', ['tag1']);
        assert.isFalse(highlight1.hasClass('annotator-hl-focused'));
      });
    });
  });
});
