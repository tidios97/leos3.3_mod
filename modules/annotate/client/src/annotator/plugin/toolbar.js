/*
 * decaffeinate suggestions:
 * DS206: Consider reworking classes to avoid initClass
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
let Toolbar;
const Plugin = require('../plugin');
const $ = require('jquery');

const LEOS_config = require('../../shared/config');

const makeButton = function(item) {
  const anchor = $('<button></button>')
    .attr('href', '')
    .attr('title', item.title)
    .attr('name', item.name)
    .on(item.on)
    .addClass('annotator-frame-button')
    .addClass(item.class);
  const button = $('<li></li>').append(anchor);
  return button[0];
};

module.exports = (Toolbar = (function() {
  let HIDE_CLASS;
  Toolbar = class Toolbar extends Plugin {
    static initClass() {
      HIDE_CLASS = 'annotator-hide';
  
      this.prototype.events = {
        'setVisibleHighlights': 'onSetVisibleHighlights',
        'LEOS_setVisibleGuideLines': 'onSetVisibleGuideLines',
      };
  
      this.prototype.html = '<div class="annotator-toolbar"></div>';
    }

    pluginInit() {
      let self = this;
      localStorage.setItem('shouldAnnotationTabOpen', true);

      this.annotator.toolbar = (this.toolbar = $(this.html));
      if (this.options.container != null) {
        $(this.options.container).append(this.toolbar);
      } else {
        $(this.element).append(this.toolbar);
      }

      const items = [{
        'title': 'Close Sidebar',
        'class': 'annotator-frame-button--sidebar_close h-icon-close',
        'name': 'sidebar-close',
        'on': {
          'click': event => {
            event.preventDefault();
            event.stopPropagation();
            self.annotator.hide();
            self.toolbar.find('[name=sidebar-close]').hide();
          },
        },
      }
      , {
        'title': 'Toggle or Resize Sidebar',
        'class': 'annotator-frame-button--sidebar_toggle h-icon-chevron-left',
        'name': 'sidebar-toggle',
        'on': {
          'click': event => {
            event.preventDefault();
            event.stopPropagation();
            const collapsed = this.annotator.frame.hasClass('annotator-collapsed');
            if (collapsed) {
              self.annotator.show();
              localStorage.setItem('shouldAnnotationTabOpen', true);
            } else {
              self.annotator.hide();
              localStorage.setItem('shouldAnnotationTabOpen', false);
              const state = false;
              const storePrevState = true;
              self.annotator.setAllVisibleGuideLines(state, storePrevState);
            }
          },
        },
      }
      , {
        //     LEOS change 4453
        'title': this.annotator.getAnnotationPopupStatus() ? "Disable Annotations' Popup" : "Enable Annotations' Popup",
        'class': this.annotator.getAnnotationPopupStatus() ? 'enable-anot-icon' : 'disable-anot-icon',
        'name': 'anot-state',
        'on': {
          'click': event => {
            self.annotator.changeAnnotationPopupStatus();
            self.onEnableAnnotationPopup(self.annotator.getAnnotationPopupStatus());
            event.preventDefault();
            event.stopPropagation();
          },
        },
      }
      , {
        'title': 'Hide Highlights',
        'class': 'h-icon-visibility',
        'name': 'highlight-visibility',
        'on': {
          'click': event => {
            event.preventDefault();
            event.stopPropagation();
            const state = !self.annotator.visibleHighlights;
            self.annotator.setAllVisibleHighlights(state);
            if (state && (self.annotator.frame.width() >= LEOS_config.FRAME_DEFAULT_MIN_WIDTH)) {
              self.annotator.restoreAllGuideLinesState();
            } else {
              self.annotator.setAllVisibleGuideLines(false, true);
            }
            self.onEnableAnnotationPopup(self.annotator.getAnnotationPopupStatus());
          },
        },
      }
      , {
        //      LEOS change 3630
        'title': 'Hide Line Guides',
        'class': 'hide-guidelines-icon',
        'name': 'lineguide-visibility',
        'on': {
          'click': event => {
            let state;
            if (self.annotator.frame.width() < LEOS_config.FRAME_DEFAULT_MIN_WIDTH) {
              state = false;
            } else {
              state = !self.annotator.visibleGuideLines;
            }
            self.annotator.setAllVisibleGuideLines(state, false);
            event.preventDefault();
            event.stopPropagation();
          },
        },
      }
      , {
        //     LEOS change 3632
        'title': 'New Document Note',
        'class': 'h-icon-note',
        'name': 'insert-comment',
        'on': {
          'click': event => {
            event.preventDefault();
            event.stopPropagation();
            self.annotator.createAnnotation();
            self.annotator.show();
          },
        },
      },
      ];

      this.annotator.visibleHighlights = true;
      const state = (this.annotator.visibleGuideLines = false);
      this.annotator.setAllVisibleGuideLines(state);

      this.buttons = $(items.map((item) => makeButton(item)));

      const list = $('<ul></ul>');
      this.buttons.appendTo(list);
      this.toolbar.append(list);

      // Remove focus from the anchors when clicked, this removes the focus
      // styles intended only for keyboard navigation. IE/FF apply the focus
      // psuedo-class to a clicked element.
      this.toolbar.on('mouseup', 'a', event => $(event.target).trigger('blur'));
    }

    onSetVisibleHighlights(state) {
      if (state) {
        $('[name=highlight-visibility]')
          .removeClass('h-icon-visibility-off')
          .addClass('h-icon-visibility')
          .prop('title', 'Hide Highlights');
      } else {
        $('[name=highlight-visibility]')
          .removeClass('h-icon-visibility')
          .addClass('h-icon-visibility-off')
          .prop('title', 'Show Highlights');
      }
    }

    onSetVisibleGuideLines(state) {
      if (state) {
        $('[name=lineguide-visibility]')
          .removeClass('hide-guidelines-icon')
          .addClass('show-guidelines-icon')
          .prop('title', 'Hide Guide Lines');
      } else {
        $('[name=lineguide-visibility]')
          .removeClass('show-guidelines-icon')
          .addClass('hide-guidelines-icon')
          .prop('title', 'Show Guide Lines');
      }
    }

    //     LEOS change 4453
    onEnableAnnotationPopup(state) {
      if (state) {
        $('[name=anot-state]')
          .removeClass('disable-anot-icon')
          .addClass('enable-anot-icon')
          .prop('title', 'Disable Annotations\' Popup');
      } else {
        $('[name=anot-state]')
          .removeClass('enable-anot-icon')
          .addClass('disable-anot-icon')
          .prop('title', 'Enable Annotations\' Popup');
      }
    }
    //---------------------

    disableMinimizeBtn() {
      $('[name=sidebar-toggle]').remove();
    }

    disableHighlightsBtn() {
      $('[name=highlight-visibility]').remove();
    }

    disableNewNoteBtn() {
      $('[name=insert-comment]').hide();
    }

    enableNewNoteBtn() {
      $('[name=insert-comment]').show();
    }

    disableCloseBtn() {
      $('[name=sidebar-close]').remove();
    }

    disableGuideLinesBtn() {
      $('[name=lineguide-visibility]').remove();
    }

    //     LEOS change 4453
    disableAnnotPopupBtn() {
      $('[name=anot-state]').hide();
    }

    enableAnnotPopupBtn() {
      $('[name=anot-state]').show();
    }
    //---------------------

    getWidth() {
      return parseInt(window.getComputedStyle(this.toolbar[0]).width);
    }

    hideCloseBtn() {
      $('[name=sidebar-close]').hide();
    }

    showCloseBtn() {
      $('[name=sidebar-close]').show();
    }

    showCollapseSidebarBtn() {
      $('[name=sidebar-toggle]')
        .removeClass('h-icon-chevron-left')
        .addClass('h-icon-chevron-right');
    }

    showExpandSidebarBtn() {
      $('[name=sidebar-toggle]')
        .removeClass('h-icon-chevron-right')
        .addClass('h-icon-chevron-left');
    }
  };
  Toolbar.initClass();
  return Toolbar;
})());
