const html = require('./html');

const configFrom = require('../config/index');
const config = configFrom(window);

const {
  LeosAnchor,
  FragmentAnchor,
  RangeAnchor,
  TextPositionAnchor,
  TextQuoteAnchor,
} = require('./leos-types');

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


exports.anchor = function(root, selectors, options) {
  if (options == null) { options = {}; }
  let leos = null;

  outer:
  for (let selector of (selectors || [])) {
    switch (selector.type) {
    case 'LeosSelector':
      leos = selector;
      break outer;
    }
  }


  if (leos != null) {
    const promise = Promise.reject('unable to anchor');
    return promise.catch(() => querySelector(LeosAnchor, root, leos, options));
  } else {
    return html.anchor(root, selectors, options);
  }
};

exports.describe = function(root, range, options) {
  if (options == null) { options = {}; }
  const types = [LeosAnchor, FragmentAnchor, RangeAnchor, TextPositionAnchor, TextQuoteAnchor];

  const selectors = [];
  for (let type of types) {
    try {
      const anchor = type.fromRange(root, range, options);
      selectors.push(anchor.toSelector(options));
    } catch (error) {
      continue;
    }
  }

  //LEOS-2789 replace wrappers tags by "//" in xpaths
  if (config.ignoredTags != null) {
    const tags = config.ignoredTags.join('|');
    const matchTags = new RegExp('(' + tags + ')(\\[\\d+\\])?', 'g');
    const matchSlashes = new RegExp('\\/(\\/)+\\/', 'g');
    selectors.filter(s => s.type === 'RangeSelector').forEach(function(selector) {
      selector.endContainer = selector.endContainer.replace(matchTags, '').replace(matchSlashes, '//');
      selector.startContainer = selector.startContainer.replace(matchTags, '').replace(matchSlashes, '//');
    });
  }
  return selectors;
};
