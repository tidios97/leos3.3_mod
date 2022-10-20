/*
 * decaffeinate suggestions:
 * DS206: Consider reworking classes to avoid initClass
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
let Sidebar;
const extend = require('extend');
const raf = require('raf');
const Hammer = require('hammerjs');

const Host = require('./host');
const annotationCounts = require('./annotation-counts');
const sidebarTrigger = require('./sidebar-trigger');
const events = require('../shared/bridge-events');
const features = require('./features');

// Minimum width to which the frame can be resized.
const MIN_RESIZE = 280;


module.exports = (Sidebar = (function () {
  Sidebar = class Sidebar extends Host {
    static initClass() {
      this.prototype.options = {
        Document: {},
        TextSelection: {},
        BucketBar: {
          container: '.annotator-frame',
        },
        Toolbar: {
          container: '.annotator-frame',
        },
      };

      this.prototype.renderFrame = null;
      this.prototype.gestureState = null;
    }

    constructor(element, config) {
      super(...arguments);
      this._notifyOfLayoutChange = this._notifyOfLayoutChange.bind(this);
      this.onPan = this.onPan.bind(this);
      this.onSwipe = this.onSwipe.bind(this);
      this.hide();

      if (config.openSidebar || config.annotations || config.query) {
        this.on('panelReady', () => this.show());
      }

      if (this.plugins.BucketBar != null) {
        this.plugins.BucketBar.element.on('click', event => this.show());
      }

      if (this.plugins.Toolbar != null) {
        this.toolbarWidth = this.plugins.Toolbar.getWidth();
        if (config.theme === 'clean') {
          this.plugins.Toolbar.disableMinimizeBtn();
          this.plugins.Toolbar.disableHighlightsBtn();
          this.plugins.Toolbar.disableNewNoteBtn();
        } else {
          this.plugins.Toolbar.disableCloseBtn();
        }

        this._setupGestures();
      }

      // The partner-provided callback functions.
      const serviceConfig = config.services != null ? config.services[0] : undefined;
      if (serviceConfig) {
        this.onLoginRequest = serviceConfig.onLoginRequest;
        this.onLogoutRequest = serviceConfig.onLogoutRequest;
        this.onSignupRequest = serviceConfig.onSignupRequest;
        this.onProfileRequest = serviceConfig.onProfileRequest;
        this.onHelpRequest = serviceConfig.onHelpRequest;
      }

      this.onLayoutChange = config.onLayoutChange;

      // initial layout notification
      this._notifyOfLayoutChange(false);

      this._setupSidebarEvents();
    }

    _setupSidebarEvents() {
      annotationCounts(document.body, this.crossframe);
      sidebarTrigger(document.body, () => this.show());
      features.init(this.crossframe);

      this.crossframe.on('showSidebar', () => this.show());
      this.crossframe.on('hideSidebar', () => this.hide());
      this.crossframe.on(events.LOGIN_REQUESTED, () => {
        if (this.onLoginRequest) {
          this.onLoginRequest();
        }
      });
      this.crossframe.on(events.LOGOUT_REQUESTED, () => {
        if (this.onLogoutRequest) {
          this.onLogoutRequest();
        }
      });
      this.crossframe.on(events.SIGNUP_REQUESTED, () => {
        if (this.onSignupRequest) {
          this.onSignupRequest();
        }
      });
      this.crossframe.on(events.PROFILE_REQUESTED, () => {
        if (this.onProfileRequest) {
          this.onProfileRequest();
        }
      });
      this.crossframe.on(events.HELP_REQUESTED, () => {
        if (this.onHelpRequest) {
          this.onHelpRequest();
        }
      });
      // Return this for chaining
      return this;
    }

    _setupGestures() {
      // Check whether @toolbar is set as tests fail otherwise.
      if (!this.toolbar) { return; }
      const $toggle = this.toolbar.find('[name=sidebar-toggle]');

      if ($toggle[0]) {
        // Prevent any default gestures on the handle
        $toggle.on('touchmove', event => event.preventDefault());

        // Set up the Hammer instance and handlers
        const mgr = new Hammer.Manager($toggle[0])
          .on('panstart panend panleft panright', this.onPan)
          .on('swipeleft swiperight', this.onSwipe);

        // Set up the gesture recognition
        const pan = mgr.add(new Hammer.Pan({ direction: Hammer.DIRECTION_HORIZONTAL }));
        const swipe = mgr.add(new Hammer.Swipe({ direction: Hammer.DIRECTION_HORIZONTAL }));
        swipe.recognizeWith(pan);

        // Set up the initial state
        this._initializeGestureState();

        // Return this for chaining
        return this;
      }
    }

    _initializeGestureState() {
      this.gestureState = {
        initial: null,
        final: null,
      };
    }

    // Schedule any changes needed to update the sidebar layout.
    _updateLayout() {
      // Only schedule one frame at a time
      if (this.renderFrame) { return; }

      // Schedule a frame
      this.renderFrame = raf(() => {
        this.renderFrame = null;  // Clear the schedule

        // Process the resize gesture
        if (this.gestureState.final !== this.gestureState.initial) {
          const m = this.gestureState.final;
          const w = -m;
          this.frame.css('margin-left', `${m}px`);
          if (w >= MIN_RESIZE) { this.frame.css('width', `${w}px`); }
          this._notifyOfLayoutChange();
        }
      });
    }

    /**
     * Notify integrator when sidebar layout changes via `onLayoutChange` callback.
     *
     * @param [boolean] explicitExpandedState - `true` or `false` if the sidebar
     *   is being directly opened or closed, as opposed to being resized via
     *   the sidebar's drag handles.
     */
    _notifyOfLayoutChange(explicitExpandedState) {
      const toolbarWidth = this.toolbarWidth || 0;

      // The sidebar structure is:
      //
      // [ Toolbar    ][                                   ]
      // [ ---------- ][ Sidebar iframe container (@frame) ]
      // [ Bucket Bar ][                                   ]
      //
      // The sidebar iframe is hidden or shown by adjusting the left margin of its
      // container.

      if (this.onLayoutChange) {
        const rect = this.frame[0].getBoundingClientRect();
        const computedStyle = window.getComputedStyle(this.frame[0]);
        const width = parseInt(computedStyle.width);
        const leftMargin = parseInt(computedStyle.marginLeft);

        // The width of the sidebar that is visible on screen, including the
        // toolbar, which is always visible.
        let frameVisibleWidth = toolbarWidth;

        if (explicitExpandedState != null) {
          // When we are explicitly saying to open or close, jump
          // straight to the upper and lower bounding widths.
          if (explicitExpandedState) {
            frameVisibleWidth += width;
          }
        } else if (leftMargin < MIN_RESIZE) {
          // When the width hits its threshold of MIN_RESIZE,
          // the left margin continues to push the sidebar off screen.
          // So it's the best indicator of width when we get below that threshold.
          // Note: when we hit the right edge, it will be -0
          frameVisibleWidth += -leftMargin;
        } else {
          frameVisibleWidth += width;
        }

        // Since we have added logic on if this is an explicit show/hide
        // and applied proper width to the visible value above, we can infer
        // expanded state on that width value vs the lower bound
        const expanded = frameVisibleWidth > toolbarWidth;

        this.onLayoutChange({
          expanded,
          width: expanded ? frameVisibleWidth : toolbarWidth,
          height: rect.height,
        });
      }
    }

    onPan(event) {
      switch (event.type) {
        case 'panstart':
          // Initialize the gesture state
          this._initializeGestureState();
          // Immadiate response
          this.frame.addClass('annotator-no-transition');
          // Escape iframe capture
          this.frame.css('pointer-events', 'none');
          // Set origin margin
          this.gestureState.initial = parseInt(getComputedStyle(this.frame[0]).marginLeft);
          break;

        case 'panend':
          // Re-enable transitions
          this.frame.removeClass('annotator-no-transition');
          // Re-enable iframe events
          this.frame.css('pointer-events', '');
          // Snap open or closed
          if (this.gestureState.final <= -MIN_RESIZE) {
            this.show();
          } else {
            this.hide();
          }
          // Reset the gesture state
          this._initializeGestureState();
          break;

        case 'panleft': case 'panright':
          if (this.gestureState.initial == null) { return; }
          // Compute new margin from delta and initial conditions
          var m = this.gestureState.initial;
          var d = event.deltaX;
          this.gestureState.final = Math.min(Math.round(m + d), 0);
          // Start updating
          this._updateLayout();
          break;
      }

    }

    onSwipe(event) {
      switch (event.type) {
        case 'swipeleft':
          this.show();
          break;
        case 'swiperight':
          this.hide();
          break;
      }
    }

    show() {
      this.crossframe.call('sidebarOpened');

      this.frame.css({ 'margin-left': `${-1 * this.frame.width()}px` });
      this.frame.removeClass('annotator-collapsed');

      if (this.plugins.Toolbar != null) {
        this.plugins.Toolbar.showCollapseSidebarBtn();
        this.plugins.Toolbar.showCloseBtn();
      }


      if (this.options.showHighlights === 'whenSidebarOpen') {
        this.setVisibleHighlights(true);
      }

      this._notifyOfLayoutChange(true);
    }

    hide() {
      this.frame.css({ 'margin-left': '' });
      this.frame.addClass('annotator-collapsed');

      this.plugins.Toolbar.hideCloseBtn();

      if (this.plugins.Toolbar != null) {
        this.plugins.Toolbar.showExpandSidebarBtn();
      }

      if (this.options.showHighlights === 'whenSidebarOpen') {
        this.setVisibleHighlights(false);
      }

      this._notifyOfLayoutChange(false);
    }

    isOpen() {
      return !this.frame.hasClass('annotator-collapsed');
    }

    setAllVisibleHighlights(shouldShowHighlights) {
      this.crossframe.call('setVisibleHighlights', shouldShowHighlights);

      // Let the Toolbar know about this event
      this.publish('setVisibleHighlights', shouldShowHighlights);
    }
  };
  Sidebar.initClass();
  return Sidebar;
})());