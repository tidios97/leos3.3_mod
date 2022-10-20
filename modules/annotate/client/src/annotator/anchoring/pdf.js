/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * DS201: Simplify complex destructure assignments
 * DS205: Consider reworking code to avoid use of IIFEs
 * DS207: Consider shorter variations of null checks
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
const seek = require('dom-seek');

const xpathRange = require('./range');

const html = require('./html');
const RenderingStates = require('../pdfjs-rendering-states');
const {TextPositionAnchor, TextQuoteAnchor} = require('./types');

// Caches for performance

// Map of page index to page text content as a `Promise<string>`
let pageTextCache = {};
// Two-dimensional map from `[quote][position]` to `{page, anchor}` intended to
// optimize re-anchoring of a pair of quote and position selectors if the
// position selector fails to anchor on its own.
let quotePositionCache = {};


const getSiblingIndex = function(node) {
  const siblings = Array.prototype.slice.call(node.parentNode.childNodes);
  return siblings.indexOf(node);
};


const getNodeTextLayer = function(node) {
  while (!(node.classList != null ? node.classList.contains('page') : undefined)) {
    node = node.parentNode;
  }
  return node.getElementsByClassName('textLayer')[0];
};


const getPage = pageIndex => PDFViewerApplication.pdfViewer.getPageView(pageIndex);


const getPageTextContent = function(pageIndex) {
  if (pageTextCache[pageIndex] != null) {
    return pageTextCache[pageIndex];
  } else {
    const joinItems = function({items}) {
      // Skip empty items since PDF-js leaves their text layer divs blank.
      // Excluding them makes our measurements match the rendered text layer.
      // Otherwise, the selectors we generate would not match this stored text.
      // See the appendText method of TextLayerBuilder in pdf.js.
      const nonEmpty = ((() => {
        const result = [];
        for (let item of items) {           if (/\S/.test(item.str)) {
          result.push(item.str);
        }
        }
        return result;
      })());
      const textContent = nonEmpty.join('');
      return textContent;
    };

    pageTextCache[pageIndex] = PDFViewerApplication.pdfViewer.getPageTextContent(pageIndex)
      .then(joinItems);
    return pageTextCache[pageIndex];
  }
};


// Return the offset in the text for the whole document at which the text for
// `pageIndex` begins.
const getPageOffset = function(pageIndex) {
  let index = -1;

  var next = function(offset) {
    if (++index === pageIndex) {
      return Promise.resolve(offset);
    }

    return getPageTextContent(index)
      .then(textContent => next(offset + textContent.length));
  };

  return next(0);
};


// Return an {index, offset, textContent} object for the page where the given
// `offset` in the full text of the document occurs.
const findPage = function(offset) {
  let index = 0;
  let total = 0;

  // We call `count` once for each page, in order. The passed offset is found on
  // the first page where the cumulative length of the text content exceeds the
  // offset value.
  //
  // When we find the page the offset is on, we return an object containing the
  // page index, the offset at the start of that page, and the textContent of
  // that page.
  //
  // To understand this a little better, here's a worked example. Imagine a
  // document with the following page lengths:
  //
  //    Page 0 has length 100
  //    Page 1 has length 50
  //    Page 2 has length 50
  //
  // Then here are the pages that various offsets are found on:
  //
  //    offset | index
  //    --------------
  //    0      | 0
  //    99     | 0
  //    100    | 1
  //    101    | 1
  //    149    | 1
  //    150    | 2
  //
  var count = function(textContent) {
    const lastPageIndex = PDFViewerApplication.pdfViewer.pagesCount - 1;
    if (((total + textContent.length) > offset) || (index === lastPageIndex)) {
      offset = total;
      return Promise.resolve({index, offset, textContent});
    } else {
      index++;
      total += textContent.length;
      return getPageTextContent(index).then(count);
    }
  };

  return getPageTextContent(0).then(count);
};


// Search for a position anchor within a page, creating a placeholder and
// anchoring to that if the page is not rendered.
const anchorByPosition = function(page, anchor, options) {
  const {
    renderingState,
  } = page;
  const renderingDone = page.textLayer != null ? page.textLayer.renderingDone : undefined;
  if ((renderingState === RenderingStates.FINISHED) && renderingDone) {
    const root = page.textLayer.textLayerDiv;
    const selector = anchor.toSelector(options);
    return html.anchor(root, [selector]);
  } else {
    const div = page.div != null ? page.div : page.el;
    let placeholder = div.getElementsByClassName('annotator-placeholder')[0];
    if (placeholder == null) {
      placeholder = document.createElement('span');
      placeholder.classList.add('annotator-placeholder');
      placeholder.textContent = 'Loading annotations…';
      div.appendChild(placeholder);
    }
    const range = document.createRange();
    range.setStartBefore(placeholder);
    range.setEndAfter(placeholder);
    return range;
  }
};


// Search for a quote (with optional position hint) in the given pages.
// Returns a `Promise<Range>` for the location of the quote.
var findInPages = function(...args) {
  const [pageIndex, ...rest] = args[0], 
    quote = args[1], 
    position = args[2];
  if (pageIndex == null) {
    return Promise.reject(new Error('Quote not found'));
  }

  const attempt = function(info) {
    // Try to find the quote in the current page.
    const [page, content, offset] = info;
    const root = {textContent: content};
    const anchor = new TextQuoteAnchor.fromSelector(root, quote);
    if (position != null) {
      let hint = position.start - offset;
      hint = Math.max(0, hint);
      hint = Math.min(hint, content.length);
      return anchor.toPositionAnchor({hint});
    } else {
      return anchor.toPositionAnchor();
    }
  };

  const next = () => findInPages(rest, quote, position);

  const cacheAndFinish = function(anchor) {
    if (position) {
      if (quotePositionCache[quote.exact] == null) { quotePositionCache[quote.exact] = {}; }
      quotePositionCache[quote.exact][position.start] = {page, anchor};
    }
    return anchorByPosition(page, anchor);
  };

  var page = getPage(pageIndex);
  const content = getPageTextContent(pageIndex);
  const offset = getPageOffset(pageIndex);

  return Promise.all([page, content, offset])
    .then(attempt)
    .then(cacheAndFinish)
    .catch(next);
};


// When a position anchor is available, quote search can prioritize pages by
// the position, searching pages outward starting from the page containing the
// expected offset. This should speed up anchoring by searching fewer pages.
const prioritizePages = function(position) {
  const {pagesCount} = PDFViewerApplication.pdfViewer;
  const pageIndices = __range__(0, pagesCount, false);

  const sort = function(pageIndex) {
    const left = pageIndices.slice(0, pageIndex);
    const right = pageIndices.slice(pageIndex);
    const result = [];
    while (left.length || right.length) {
      if (right.length) {
        result.push(right.shift());
      }
      if (left.length) {
        result.push(left.pop());
      }
    }
    return result;
  };

  if (position != null) {
    return findPage(position.start)
      .then(({index}) => sort(index));
  } else {
    return Promise.resolve(pageIndices);
  }
};


/**
 * Anchor a set of selectors.
 *
 * This function converts a set of selectors into a document range.
 * It encapsulates the core anchoring algorithm, using the selectors alone or
 * in combination to establish the best anchor within the document.
 *
 * :param Element root: The root element of the anchoring context.
 * :param Array selectors: The selectors to try.
 * :param Object options: Options to pass to the anchor implementations.
 * :return: A Promise that resolves to a Range on success.
 * :rtype: Promise
 *///
exports.anchor = function(root, selectors, options) {
  // Selectors
  if (options == null) { options = {}; }
  let position = null;
  let quote = null;

  // Collect all the selectors
  for (let selector of selectors != null ? selectors : []) {
    switch (selector.type) {
    case 'TextPositionSelector':
      position = selector;
      break;
    case 'TextQuoteSelector':
      quote = selector;
      break;
    }
  }

  // Until we successfully anchor, we fail.
  let promise = Promise.reject('unable to anchor');

  // Assert the quote matches the stored quote, if applicable
  const assertQuote = function(range) {
    if (((quote != null ? quote.exact : undefined) != null) && (range.toString() !== quote.exact)) {
      throw new Error('quote mismatch');
    } else {
      return range;
    }
  };

  if (position != null) {
    promise = promise.catch(() => findPage(position.start)
      .then(function({index, offset, textContent}) {
        const page = getPage(index);
        const start = position.start - offset;
        const end = position.end - offset;
        const length = end - start;
        assertQuote(textContent.substr(start, length));
        const anchor = new TextPositionAnchor(root, start, end);
        return anchorByPosition(page, anchor, options);
      }));
  }

  if (quote != null) {
    promise = promise.catch(function() {
      if ((position != null) && ((quotePositionCache[quote.exact] != null ? quotePositionCache[quote.exact][position.start] : undefined) != null)) {
        const {page, anchor} = quotePositionCache[quote.exact][position.start];
        return anchorByPosition(page, anchor, options);
      }

      return prioritizePages(position)
        .then(pageIndices => findInPages(pageIndices, quote, position));
    });
  }

  return promise;
};


/**
 * Convert a DOM Range object into a set of selectors.
 *
 * Converts a DOM `Range` object describing a start and end point within a
 * `root` `Element` and converts it to a `[position, quote]` tuple of selectors
 * which can be saved into an annotation and later passed to `anchor` to map
 * the selectors back to a `Range`.
 *
 * :param Element root: The root Element
 * :param Range range: DOM Range object
 * :param Object options: Options passed to `TextQuoteAnchor` and
 *                        `TextPositionAnchor`'s `toSelector` methods.
 */
exports.describe = function(root, range, options) {

  if (options == null) { options = {}; }
  range = new xpathRange.BrowserRange(range).normalize();

  const startTextLayer = getNodeTextLayer(range.start);
  const endTextLayer = getNodeTextLayer(range.end);

  // XXX: range covers only one page
  if (startTextLayer !== endTextLayer) {
    throw new Error('selecting across page breaks is not supported');
  }

  const startRange = range.limit(startTextLayer);
  const endRange = range.limit(endTextLayer);

  const startPageIndex = getSiblingIndex(startTextLayer.parentNode);
  const endPageIndex = getSiblingIndex(endTextLayer.parentNode);

  const iter = document.createNodeIterator(startTextLayer, NodeFilter.SHOW_TEXT);

  let start = seek(iter, range.start);
  let end = seek(iter, range.end) + start + range.end.textContent.length;

  return getPageOffset(startPageIndex).then(function(pageOffset) {
    // XXX: range covers only one page
    start += pageOffset;
    end += pageOffset;

    const position = new TextPositionAnchor(root, start, end).toSelector(options);

    const r = document.createRange();
    r.setStartBefore(startRange.start);
    r.setEndAfter(endRange.end);

    const quote = TextQuoteAnchor.fromRange(root, r, options).toSelector(options);

    return Promise.all([position, quote]);
  });
};


/**
 * Clear the internal caches of page text contents and quote locations.
 *
 * This exists mainly as a helper for use in tests.
 */
exports.purgeCache = function() {
  pageTextCache = {};
  return quotePositionCache = {};
};

function __range__(left, right, inclusive) {
  let range = [];
  let ascending = left < right;
  let end = !inclusive ? right : ascending ? right + 1 : right - 1;
  for (let i = left; ascending ? i < end : i > end; ascending ? i++ : i--) {
    range.push(i);
  }
  return range;
}