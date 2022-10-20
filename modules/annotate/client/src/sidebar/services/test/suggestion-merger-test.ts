'use strict';

const _fixtures = require('../../components/test/suggestion-fixtures-test');


function createSuggestionWithAnchorInfo({ idSuffix = undefined, completeOuterHTML = undefined, startOffset = undefined} = {}) {

  const suggestion = _fixtures.defaultSuggestion();
  if (idSuffix !== undefined) suggestion.id += idSuffix;

  suggestion.anchorInfo = {
    completeOuterHTML: 'a',
    elementId: 'b',
    endOffset: 0,
    newText: 'c',
    origText: 'd',
    parentElementId: 'e',
    startOffset: 1,
  };
  if (completeOuterHTML !== undefined) suggestion.anchorInfo.completeOuterHTML = completeOuterHTML;
  if (startOffset !== undefined) suggestion.anchorInfo.startOffset = startOffset;
  
  return suggestion;
}

describe('suggestion-merger', function() {
  const angular = require('angular');

  let suggestionMerger;

  before(() =>
  angular.module('h', [])
      .service('suggestionMerger', require('../suggestion-merger'))
  );

  beforeEach(() => {
    const fakeBridge = {};
    angular.mock.module('h', {
      bridge: fakeBridge,
    });
  });

  beforeEach(angular.mock.inject((_suggestionMerger_) => {
    suggestionMerger = _suggestionMerger_;
  }));

  describe('#checkValidityOfSuggestion', () => {

    it('suggestion is okay', (done) => {
      const suggestion = createSuggestionWithAnchorInfo();
      suggestionMerger.checkValidityOfSuggestion(suggestion)
        .then(() => done())
        .catch(() => assert.fails());
    });

    it('undefined suggestion is not okay', () => {
      suggestionMerger.checkValidityOfSuggestion(undefined)
        .then(() => assert.fails())
        .catch((error) => assert.equals(error, 'Suggestion is not valid.'));
    });

    it('suggestion with undefined anchorinfo is not okay', () => {
      const suggestion = createSuggestionWithAnchorInfo();
      suggestion.anchorInfo = undefined;
      suggestionMerger.checkValidityOfSuggestion(suggestion)
        .then(() => assert.fails())
        .catch((error) => assert.equals(error, 'Suggestion is not valid.'));
    });
  });
  
});
