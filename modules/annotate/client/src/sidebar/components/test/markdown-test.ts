'use strict';

var angular = require('angular');

var util = require('../../directive/test/util');
var noCallThru = require('../../../shared/test/util').noCallThru;

describe('markdown', function () {

  var proxyquire = require('proxyquire');

  function isHidden(element) {
    return element.classList.contains('ng-hide');
  }

  function inputElement(editor) {
    return editor[0].querySelector('.form-input');
  }

  function viewElement(editor) {
    return editor[0].querySelector('.markdown-body');
  }

  function toolbarButtons(editor) {
    return Array.from(editor[0].querySelectorAll('.markdown-tools-button'));
  }

  function getRenderedHTML(editor) {
    var contentElement = viewElement(editor);
    if (isHidden(contentElement)) {
      return 'rendered markdown is hidden';
    }
    return contentElement.innerHTML;
  }

  function mockFormattingCommand() {
    return {
      text: 'formatted text',
      selectionStart: 0,
      selectionEnd: 0,
    };
  }

  before(function () {
    angular.module('app', ['ngSanitize'])
      .component('markdown', proxyquire('../markdown', noCallThru({
        angular: angular,
        katex: {
          renderToString: function (input) {
            return 'math:' + input.replace(/$$/g, '');
          },
        },
        'lodash.debounce': function (fn) {
          // Make input change debouncing synchronous in tests
          return function () {
            fn();
          };
        },
        '../render-markdown': noCallThru(function (markdown, $sanitize) {
          return $sanitize('rendered:' + markdown);
        }),
        '../markdown-commands': {
          convertSelectionToLink: mockFormattingCommand,
          toggleBlockStyle: mockFormattingCommand,
          toggleSpanStyle: mockFormattingCommand,
          LinkType: require('../../markdown-commands').LinkType,
        },
        '../media-embedder': noCallThru({
          replaceLinksWithEmbeds: function (element) {
            // Tag the element as having been processed
            element.dataset.replacedLinksWithEmbeds = 'yes';
          },
        }),
      })));
  });

  beforeEach(function () {
    angular.mock.module('app');
  });

  describe('toolbar display', function () {
    it('should not show toolbar', function () {
      var editor = util.createDirective(document, 'markdown', {
        readOnly: false,
        text: 'Hello World',
      });
      assert.notEqual(viewElement(editor), null);
      assert.deepEqual(toolbarButtons(editor), []);
    });
  });
});