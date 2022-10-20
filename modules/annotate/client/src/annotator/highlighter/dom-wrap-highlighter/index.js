/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
const $ = require('jquery');

// Public: Wraps the DOM Nodes within the provided range with a highlight
// element of the specified class and returns the highlight Elements.
//
// normedRange - A NormalizedRange to be highlighted.
// cssClass - A CSS class to use for the highlight (default: 'annotator-hl')
//
// Returns an array of highlight Elements.
exports.highlightRange = function(normedRange, cssClass) {
  if (cssClass == null) { cssClass = 'annotator-hl'; }
  const white = /^\s*$/;

  // A custom element name is used here rather than `<span>` to reduce the
  // likelihood of highlights being hidden by page styling.
  const hl = $(`<hypothesis-highlight class='${cssClass}'></hypothesis-highlight>`);

  // Ignore text nodes that contain only whitespace characters. This prevents
  // spans being injected between elements that can only contain a restricted
  // subset of nodes such as table rows and lists. This does mean that there
  // may be the odd abandoned whitespace node in a paragraph that is skipped
  // but better than breaking table layouts.
  const nodes = $(normedRange.textNodes()).filter(function(i) { return !white.test(this.nodeValue); });

  return nodes.wrap(hl).parent().toArray();
};


exports.removeHighlights = highlights => {
  const result = [];
  for (let h of highlights) {
    if (h.parentNode != null) {
      result.push($(h).replaceWith(h.childNodes));
    }
  }
  return result;
};


// Get the bounding client rectangle of a collection in viewport coordinates.
// Unfortunately, Chrome has issues[1] with Range.getBoundingClient rect or we
// could just use that.
// [1] https://code.google.com/p/chromium/issues/detail?id=324437
exports.getBoundingClientRect = function(collection) {
  // Reduce the client rectangles of the highlights to a bounding box
  const rects = collection.map(n => n.getBoundingClientRect());
  return rects.reduce((acc, r) => ({
    top: Math.min(acc.top, r.top),
    left: Math.min(acc.left, r.left),
    bottom: Math.max(acc.bottom, r.bottom),
    right: Math.max(acc.right, r.right),
  }));
};
