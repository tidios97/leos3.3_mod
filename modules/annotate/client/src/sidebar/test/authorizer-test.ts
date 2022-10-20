'use strict';

var assert = require('assert');
var fixtures = require('./annotation-fixtures');
var authorizer = require('../authorizer');
const OPERATION_MODES = require('../../shared/operationMode');

describe('authorization', function () {
  describe('merging suggestions', function () {
    var annotation = Object.assign(fixtures.defaultAnnotation(), {$orphan:false});
    
    it('merging is not allowed in read-only mode', function () {

      let settings = {
        operationMode: OPERATION_MODES.READ_ONLY
      };

      let isAllowed = authorizer.canMergeSuggestion(annotation, undefined, settings);
      assert.equal(isAllowed, false); //assert.isFalse not working!?
    });
  });
});