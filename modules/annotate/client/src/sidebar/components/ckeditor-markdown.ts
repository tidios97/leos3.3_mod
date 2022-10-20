'use strict';

var debounce = require('lodash.debounce');
require('ckeditor');

var commands = require('../markdown-commands');
var mediaEmbedder = require('../media-embedder');
var renderMarkdown = require('../render-markdown');
var scopeTimeout = require('../util/scope-timeout');

// @ngInject
function MarkdownController($element, $sanitize, $scope, settings) {
  var input = $element.find('textarea')[0];
  var output = $element[0].querySelector('.js-markdown-preview');

  var self = this;

  const editorConfig: any = {
     disableAutoInline: true,
     extraPlugins: 'autolink',
     height: '8em',
     startupFocus: 'end',
     toolbar: [
       { name: 'basicstyles', items: [ 'Bold', 'Italic' ] },
       { name: 'links', items: [ 'Link', 'Unlink' ] }
     ],
  };

  self.$onInit = function() {
    const editor = CKEDITOR.replace(input, editorConfig);

    const handleInputChange = debounce(function () {
      $scope.$apply(function () {
        self.onEditText({text: editor.getData()});
      });
    }, 100);

    // Adding event handler to react on spell checker changes
    window.document.addEventListener('AnnotateSpellCheckerReplace', function(data) {
        // Event will be triggered for all editor.Teh editor that has hasFocus == true is the current used editor.
        if (editor.editable() === undefined) {
            return;
        }
        if (editor.editable().hasFocus !== true) {
            return;
        }
        handleInputChange();
    });

    editor.on('change', handleInputChange);

    editor.on('instanceReady', function() {
      // Somehow, an on event listener can not be directly added to the editor instance. Therefore, the editor is retrieved anew.
      document.getElementById('cke_' + editor.name).onclick = function(event) {
        event.stopPropagation();
      };
    });

    $scope.$watch('vm.showEditor()', function (show) {
      if (show) {
        editor.setData(self.text || '');
      }
    });

    editor.on('paste', function(event) {
        var pastedText = event.data.dataValue;
        // ANOT-314: only keep characters 9 (tab), 10, 11, 13 (line breaks), but drop control characters
        event.data.dataValue = pastedText.replace(/[\u0001-\u0008]/g, '')
                                         .replace(/[\u000E-\u001F]/g, '')
                                         .replace(/\u000C/g, '');
    });

    CKEDITOR.on('dialogDefinition', function(event) {
      const dialog = event.data.dialog;
      const dialogName = event.data.name;
      const dialogDefinition = event.data.definition;

      if (dialogName === 'link') {
        // Set "New Windows (_blank)" as default target option.
        const targetTab = dialogDefinition.getContents('target');
        const targetField = targetTab.get('linkTargetType');
        targetField['default'] = '_blank';

        // Hide tab row.
        dialog.parts.tabs.$.style.display = 'none';
        const dialogContents = dialog.parts.dialog.$.querySelector('.cke_dialog_contents');
        dialogContents.style['margin-top'] = 0;
        dialogContents.style['border-top'] = 'none';

        dialogDefinition.dialog.on('show', function () {
          // Make height adjust automaticaly.
          const dialogContentsBody = dialog.parts.dialog.$.querySelector('.cke_dialog_contents_body');
          dialogContentsBody.style.height = 'auto';

          // Hide link type field.
          const linkTypeFieldRow = dialog.parts.dialog.$.querySelector('div > table tr:nth-child(2)');
          linkTypeFieldRow.style.display = 'none';

          // Hide protocol field.
          const protocolFieldCell = dialog.parts.dialog.$.querySelector('div > table tr:nth-child(3) tr tr td:first-child');
          protocolFieldCell.style.display = 'none';
        });

        // Disable resizing dialog window.
        dialogDefinition.resizable = CKEDITOR.DIALOG_RESIZE_NONE;
      }
    });
  };


  /**
   * Transform the editor's input field with an editor command.
   */
  function updateState(newStateFn) {
    var newState = newStateFn({
      text: input.value,
      selectionStart: input.selectionStart,
      selectionEnd: input.selectionEnd,
    });

    input.value = newState.text;
    input.selectionStart = newState.selectionStart;
    input.selectionEnd = newState.selectionEnd;

    // The input field currently loses focus when the contents are
    // changed. This re-focuses the input field but really it should
    // happen automatically.
    input.focus();

    self.onEditText({text: input.value});
  }

  function focusInput() {
    // When the visibility of the editor changes, focus it.
    // A timeout is used so that focus() is not called until
    // the visibility change has been applied (by adding or removing
    // the relevant CSS classes)
    scopeTimeout($scope, function () {
      input.focus();
    }, 0);
  }

  this.insertBold = function() {
    updateState(function (state) {
      return commands.toggleSpanStyle(state, '**', '**', 'Bold');
    });
  };

  this.insertItalic = function() {
    updateState(function (state) {
      return commands.toggleSpanStyle(state, '*', '*', 'Italic');
    });
  };

  this.insertMath = function() {
    updateState(function (state) {
      var before = state.text.slice(0, state.selectionStart);

      if (before.length === 0 ||
          before.slice(-1) === '\n' ||
          before.slice(-2) === '$$') {
        return commands.toggleSpanStyle(state, '$$', '$$', 'Insert LaTeX');
      } else {
        return commands.toggleSpanStyle(state, '\\(', '\\)',
                                            'Insert LaTeX');
      }
    });
  };

  this.insertLink = function() {
    updateState(function (state) {
      return commands.convertSelectionToLink(state);
    });
  };

  this.insertIMG = function() {
    updateState(function (state) {
      return commands.convertSelectionToLink(state,
        commands.LinkType.IMAGE_LINK);
    });
  };

  this.insertList = function() {
    updateState(function (state) {
      return commands.toggleBlockStyle(state, '* ');
    });
  };

  this.insertNumList = function() {
    updateState(function (state) {
      return commands.toggleBlockStyle(state, '1. ');
    });
  };

  this.insertQuote = function() {
    updateState(function (state) {
      return commands.toggleBlockStyle(state, '> ');
    });
  };

  // Keyboard shortcuts for bold, italic, and link.
  $element.on('keydown', function(e) {
    var shortcuts = {
      66: self.insertBold,
      73: self.insertItalic,
      75: self.insertLink,
    };

    var shortcut = shortcuts[e.keyCode];
    if (shortcut && (e.ctrlKey || e.metaKey)) {
      e.preventDefault();
      shortcut();
    }
  });

  this.preview = false;
  this.togglePreview = function () {
    self.preview = !self.preview;
  };

  var handleInputChange = debounce(function () {
    $scope.$apply(function () {
      self.onEditText({text: input.value});
    });
  }, 100);
  input.addEventListener('input', handleInputChange);

  // Re-render the markdown when the view needs updating.
  $scope.$watch('vm.text', function () {
    output.innerHTML = renderMarkdown(self.text || '', $sanitize);
    mediaEmbedder.replaceLinksWithEmbeds(output);
  });

  this.showEditor = function () {
    return !self.readOnly && !self.preview;
  };

  // Exit preview mode when leaving edit mode
  $scope.$watch('vm.readOnly', function () {
    self.preview = false;
  });

  $scope.$watch('vm.showEditor()', function (show) {
    if (show) {
      input.value = self.text || '';
      focusInput();
    }
  });
}

/**
 * @name markdown
 * @description
 * This directive controls both the rendering and display of markdown, as well as
 * the markdown editor.
 */
// @ngInject
export = {
  controller: MarkdownController,
  controllerAs: 'vm',
  bindings: {
    customTextClass: '<?',
    readOnly: '<',
    text: '<?',
    onEditText: '&',
  },
  template: require('../templates/ckeditor-markdown.html'),
};
