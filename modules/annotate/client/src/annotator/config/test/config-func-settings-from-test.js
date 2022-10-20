'use strict';

var configFuncSettingsFrom = require('../config-func-settings-from');

describe('annotator.config.configFuncSettingsFrom', function() {
  afterEach('reset the sandbox', function() {
    sinon.restore();
  });

  context("when there's no window.hypothesisConfig() function", function() {
    it('returns {}', function() {
      var fakeWindow = {};

      assert.deepEqual(configFuncSettingsFrom(fakeWindow), {});
    });
  });

  context("when window.hypothesisConfig() isn't a function", function() {
    beforeEach('stub console.warn()', function() {
      sinon.stub(console, 'warn');
    });

    function fakeWindow() {
      return {hypothesisConfig: 42};
    }

    it('returns {}', function() {
      assert.deepEqual(configFuncSettingsFrom(fakeWindow()), {});
    });

    it('logs a warning', function() {
      configFuncSettingsFrom(fakeWindow());

      assert.calledOnce(console.warn);
      assert.isTrue(console.warn.firstCall.args[0].startsWith(
        'hypothesisConfig must be a function'
      ));
    });
  });

  context('when window.hypothesisConfig() is a function', function() {
    it('returns whatever window.hypothesisConfig() returns', function () {
      // It just blindly returns whatever hypothesisConfig() returns
      // (even if it's not an object).
      var fakeWindow = { hypothesisConfig: sinon.stub().returns(42) };

      assert.equal(configFuncSettingsFrom(fakeWindow), 42);
    });
  });
});
