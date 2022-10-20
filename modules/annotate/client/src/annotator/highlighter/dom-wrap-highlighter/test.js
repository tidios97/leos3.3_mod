const Range = require('../../anchoring/range');
const $ = require('jquery');

const highlighter = require('./index');

describe('highlightRange', function() {
  it('wraps a highlight span around the given range', function() {
    const txt = document.createTextNode('test highlight span');
    const el = document.createElement('span');
    el.appendChild(txt);
    const r = new Range.NormalizedRange({
      commonAncestor: el,
      start: txt,
      end: txt,
    });
    const result = highlighter.highlightRange(r);
    assert.equal(result.length, 1);
    assert.strictEqual(el.childNodes[0], result[0]);
    assert.equal(result[0].nodeName, 'HYPOTHESIS-HIGHLIGHT');
    assert.isTrue(result[0].classList.contains('annotator-hl'));
  });

  it('skips text nodes that are only white space', function() {
    const txt = document.createTextNode('one');
    const blank = document.createTextNode(' ');
    const txt2 = document.createTextNode('two');
    const el = document.createElement('span');
    el.appendChild(txt);
    el.appendChild(blank);
    el.appendChild(txt2);
    const r = new Range.NormalizedRange({
      commonAncestor: el,
      start: txt,
      end: txt2,
    });
    const result = highlighter.highlightRange(r);
    assert.equal(result.length, 2);
    assert.strictEqual(el.childNodes[0], result[0]);
    assert.strictEqual(el.childNodes[2], result[1]);
  });
});


describe('removeHighlights', function() {
  it('unwraps all the elements', function() {
    const txt = document.createTextNode('word');
    const el = document.createElement('span');
    const hl = document.createElement('span');
    const div = document.createElement('div');
    el.appendChild(txt);
    hl.appendChild(el);
    div.appendChild(hl);
    highlighter.removeHighlights([hl]);
    assert.isNull(hl.parentNode);
    assert.strictEqual(el.parentNode, div);
  });

  it('does not fail on nodes with no parent', function() {
    const txt = document.createTextNode('no parent');
    const hl = document.createElement('span');
    hl.appendChild(txt);
    highlighter.removeHighlights([hl]);
  });
});


describe('getBoundingClientRect', () => it('returns the bounding box of all the highlight client rectangles', function() {
  const rects = [
    {
      top: 20,
      left: 15,
      bottom: 30,
      right: 25,
    },
    {
      top: 10,
      left: 15,
      bottom: 20,
      right: 25,
    },
    {
      top: 15,
      left: 20,
      bottom: 25,
      right: 30,
    },
    {
      top: 15,
      left: 10,
      bottom: 25,
      right: 20,
    },
  ];
  const fakeHighlights = rects.map(r => ({
    getBoundingClientRect() { return r; },
  }));
  const result = highlighter.getBoundingClientRect(fakeHighlights);
  assert.equal(result.left, 10);
  assert.equal(result.top, 10);
  assert.equal(result.right, 30);
  assert.equal(result.bottom, 30);
}));
