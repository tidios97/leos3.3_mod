const {
  FragmentAnchor,
  RangeAnchor,
  TextPositionAnchor,
  TextQuoteAnchor,
} = require('./types');

const querySelector = function(type, root, selector, options) {
  const doQuery = function(resolve, reject) {
    try {
      const anchor = type.fromSelector(root, selector, options);
      const range = anchor.toRange(options);
      return resolve(range);
    } catch (error) {
      return reject(error);
    }
  };
  return new Promise(doQuery);
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
  let fragment = null;
  let position = null;
  let quote = null;
  let range = null;

  // Collect all the selectors
  for (let selector of (selectors || [])) {
    switch (selector.type) {
    case 'FragmentSelector':
      fragment = selector;
      break;
    case 'TextPositionSelector':
      position = selector;
      options.hint = position.start;  // TextQuoteAnchor hint
      break;
    case 'TextQuoteSelector':
      quote = selector;
      break;
    case 'RangeSelector':
      range = selector;
      break;
    }
  }

  // Assert the quote matches the stored quote, if applicable
  const maybeAssertQuote = function(range) {
    if (quote != null && quote.exact != null && range.toString() !== quote.exact) {
      throw new Error('quote mismatch');
    } else {
      return range;
    }
  };

  // From a default of failure, we build up catch clauses to try selectors in
  // order, from simple to complex.
  let promise = Promise.reject('unable to anchor');

  if (fragment != null) {
    promise = promise.catch(() => querySelector(FragmentAnchor, root, fragment, options)
      .then(maybeAssertQuote));
  }

  if (range != null) {
    promise = promise.catch(() => querySelector(RangeAnchor, root, range, options)
      .then(maybeAssertQuote));
  }

  if (position != null) {
    promise = promise.catch(() => querySelector(TextPositionAnchor, root, position, options)
      .then(maybeAssertQuote));
  }

  if (quote != null) {
    promise = promise.catch(() => // Note: similarity of the quote is implied.
      querySelector(TextQuoteAnchor, root, quote, options));
  }

  return promise;
};


exports.describe = function(root, range, options) {
  if (options == null) { options = {}; }
  const types = [FragmentAnchor, RangeAnchor, TextPositionAnchor, TextQuoteAnchor];

  const selectors = [];
  for (let type of types) {
    try {
      const anchor = type.fromRange(root, range, options);
      selectors.push(anchor.toSelector(options));
    } catch (error) {
      continue;
    }
  }
  return selectors;
};
