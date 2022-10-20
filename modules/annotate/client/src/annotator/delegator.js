/*
 * decaffeinate suggestions:
 * DS201: Simplify complex destructure assignments
 * DS206: Consider reworking classes to avoid initClass
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
let Delegator;
const $ = require('jquery');
const ANNOT_POPUP_DEFAULT_STATUS = require('../shared/annotationPopupDefaultStatus')

const _isScrollable = element => (element.scrollWidth > element.clientWidth) || (element.scrollHeight > element.clientHeight);
const _nearestNonScrollableAncestor = function(element) {
  if(typeof(element) === 'undefined') { return element; }
  let parentEl = element;
  while (parentEl.parentElement) {
    if (!_isScrollable(parentEl)) {
      break;
    }
    parentEl = parentEl.parentElement;
  }
  return parentEl;
};

/*
** Adapted from:
** https://github.com/openannotation/annotator/blob/v1.2.x/src/class.coffee
**
** Annotator v1.2.10
** https://github.com/openannotation/annotator
**
** Copyright 2015, the Annotator project contributors.
** Dual licensed under the MIT and GPLv3 licenses.
** https://github.com/openannotation/annotator/blob/master/LICENSE
*/

// Public: Delegator is the base class that all of Annotators objects inherit
// from. It provides basic functionality such as instance options, event
// delegation and pub/sub methods.
module.exports = (Delegator = (function() {
  Delegator = class Delegator {
    static initClass() {
      // Public: Events object. This contains a key/pair hash of events/methods that
      // should be bound. See Delegator#addEvents() for usage.
      this.prototype.events = {};
  
      // Public: Options object. Extended on initialisation.
      this.prototype.options = {};
  
      // A jQuery object wrapping the DOM Element provided on initialisation.
      this.prototype.element = null;
    }

    // Public: Constructor function that sets up the instance. Binds the @events
    // hash and extends the @options object.
    //
    // element - The DOM element that this instance represents.
    // config - An Object literal of config settings.
    //
    // Examples
    //
    //   element  = document.getElementById('my-element')
    //   instance = new Delegator(element, {
    //     option: 'my-option'
    //   })
    //
    // Returns a new instance of Delegator.
    constructor(element, config) {
      this.options = $.extend(true, {}, this.options, config);

      // Delegator creates closures for each event it binds. This is a private
      // registry of created closures, used to enable event unbinding.
      this._closures = {};

      // This is set for the normal toolbar
      if (this.options.hasOwnProperty('Document')) {
        this.options.Document.leosDocumentRootNode = config.leosDocumentRootNode;
        this.options.Document.annotationContainer = config.annotationContainer;
        this.options.Document.allowedSelectorTags = config.allowedSelectorTags;
        this.options.Document.editableSelector = config.editableSelector;
        this.options.Document.notAllowedSuggestSelector = config.notAllowedSuggestSelector;
        this.options.Document.operationMode = config.operationMode;
        this.options.Document.annotationPopupDefaultStatus = config.annotationPopupDefaultStatus;
        this.options.Document.showStatusFilter = config.showStatusFilter;
        this.options.Document.showGuideLinesButton = config.showGuideLinesButton;
        this.options.Document.connectedEntity = config.connectedEntity;
        this.options.Document.context = config.context;

        if (Array.isArray(config.services) && config.services.length > 0) {
          this.options.Document.authority = config.services[0].authority;
        }
        if (this.options.hasOwnProperty('BucketBar')) {
          //Set the scrollable element on which the bucket bar should be synced
          this.options.BucketBar.scrollables = [`${this.options.Document.annotationContainer}`];
        }
          

        this.annotationPopupStatus = true;
        if (this.options.Document.annotationPopupDefaultStatus === ANNOT_POPUP_DEFAULT_STATUS.OFF) {
          this.annotationPopupStatus = false;
        }

        //Get first non scrollable parent of the container to get an element on which side bar can be fixed
        this.container = $(this.options.Document.annotationContainer);
        if (this.container[0]) {
          element = _nearestNonScrollableAncestor(this.container[0]);
        }
      }
      // plugins set their own elements, for the toolbar it is set above using _nearestNonScrollableAncestor
      this.element = $(element);

      this.on = this.subscribe;
      this.addEvents();
    }

    // Public: Destroy the instance, unbinding all events.
    //
    // Returns nothing.
    destroy() {
      this.removeEvents();
    }

    // Public: binds the function names in the @events Object to their events.
    //
    // The @events Object should be a set of key/value pairs where the key is the
    // event name with optional CSS selector. The value should be a String method
    // name on the current class.
    //
    // This is called by the default Delegator constructor and so shouldn't usually
    // need to be called by the user.
    //
    // Examples
    //
    //   # This will bind the clickedElement() method to the click event on @element.
    //   @options = {"click": "clickedElement"}
    //
    //   # This will delegate the submitForm() method to the submit event on the
    //   # form within the @element.
    //   @options = {"form submit": "submitForm"}
    //
    //   # This will bind the updateAnnotationStore() method to the custom
    //   # annotation:save event. NOTE: Because this is a custom event the
    //   # Delegator#subscribe() method will be used and updateAnnotationStore()
    //   # will not receive an event parameter like the previous two examples.
    //   @options = {"annotation:save": "updateAnnotationStore"}
    //
    // Returns nothing.
    addEvents() {
      Delegator._parseEvents(this.events).map((event) =>
        this._addEvent(event.selector, event.event, event.functionName));
    }

    // Public: unbinds functions previously bound to events by addEvents().
    //
    // The @events Object should be a set of key/value pairs where the key is the
    // event name with optional CSS selector. The value should be a String method
    // name on the current class.
    //
    // Returns nothing.
    removeEvents() {
      Delegator._parseEvents(this.events).map((event) =>
        this._removeEvent(event.selector, event.event, event.functionName));
    }

    // Binds an event to a callback function represented by a String. A selector
    // can be provided in order to watch for events on a child element.
    //
    // The event can be any standard event supported by jQuery or a custom String.
    // If a custom string is used the callback function will not recieve an
    // event object as it's first parameter.
    //
    // selector     - Selector String matching child elements. (default: '')
    // event        - The event to listen for.
    // functionName - A String function name to bind to the event.
    //
    // Examples
    //
    //   # Listens for all click events on instance.element.
    //   instance._addEvent('', 'click', 'onClick')
    //
    //   # Delegates the instance.onInputFocus() method to focus events on all
    //   # form inputs within instance.element.
    //   instance._addEvent('form :input', 'focus', 'onInputFocus')
    //
    // Returns itself.
    _addEvent(selector, event, functionName) {
      const closure = function() { return this[functionName].apply(this, arguments); }.bind(this);

      if ((selector === '') && Delegator._isCustomEvent(event)) {
        this.subscribe(event, closure);
      } else {
        this.element.delegate(selector, event, closure);
      }

      this._closures[`${selector}/${event}/${functionName}`] = closure;

      return this;
    }

    // Unbinds a function previously bound to an event by the _addEvent method.
    //
    // Takes the same arguments as _addEvent(), and an event will only be
    // successfully unbound if the arguments to removeEvent() are exactly the same
    // as the original arguments to _addEvent(). This would usually be called by
    // _removeEvents().
    //
    // selector     - Selector String matching child elements. (default: '')
    // event        - The event to listen for.
    // functionName - A String function name to bind to the event.
    //
    // Returns itself.
    _removeEvent(selector, event, functionName) {
      const closure = this._closures[`${selector}/${event}/${functionName}`];

      if ((selector === '') && Delegator._isCustomEvent(event)) {
        this.unsubscribe(event, closure);
      } else {
        this.element.undelegate(selector, event, closure);
      }

      delete this._closures[`${selector}/${event}/${functionName}`];

      return this;
    }


    // Public: Fires an event and calls all subscribed callbacks with any parameters
    // provided. This is essentially an alias of @element.triggerHandler() but
    // should be used to fire custom events.
    //
    // NOTE: Events fired using .publish() will not bubble up the DOM.
    //
    // event  - A String event name.
    // params - An Array of parameters to provide to callbacks.
    //
    // Examples
    //
    //   instance.subscribe('annotation:save', (msg) -> console.log(msg))
    //   instance.publish('annotation:save', ['Hello World'])
    //   # => Outputs "Hello World"
    //
    // Returns itself.
    publish() {
      this.element.triggerHandler.apply(this.element, arguments);
      return this;
    }

    // Public: Listens for custom event which when published will call the provided
    // callback. This is essentially a wrapper around @element.bind() but removes
    // the event parameter that jQuery event callbacks always recieve. These
    // parameters are unnessecary for custom events.
    //
    // event    - A String event name.
    // callback - A callback function called when the event is published.
    //
    // Examples
    //
    //   instance.subscribe('annotation:save', (msg) -> console.log(msg))
    //   instance.publish('annotation:save', ['Hello World'])
    //   # => Outputs "Hello World"
    //
    // Returns itself.
    subscribe(event, callback) {
      const closure = function() { return callback.apply(this, [].slice.call(arguments, 1)); };

      // Ensure both functions have the same unique id so that jQuery will accept
      // callback when unbinding closure.
      closure.guid = (callback.guid = ($.guid += 1));

      this.element.bind(event, closure);
      return this;
    }

    // Public: Unsubscribes a callback from an event. The callback will no longer
    // be called when the event is published.
    //
    // event    - A String event name.
    // callback - A callback function to be removed.
    //
    // Examples
    //
    //   callback = (msg) -> console.log(msg)
    //   instance.subscribe('annotation:save', callback)
    //   instance.publish('annotation:save', ['Hello World'])
    //   # => Outputs "Hello World"
    //
    //   instance.unsubscribe('annotation:save', callback)
    //   instance.publish('annotation:save', ['Hello Again'])
    //   # => No output.
    //
    // Returns itself.
    unsubscribe() {
      this.element.unbind.apply(this.element, arguments);
      return this;
    }
  };
  Delegator.initClass();
  return Delegator;
})());


// Parse the @events object of a Delegator into an array of objects containing
// string-valued "selector", "event", and "func" keys.
Delegator._parseEvents = function(eventsObj) {
  const events = [];
  for (let sel in eventsObj) {
    const functionName = eventsObj[sel];
    const array = sel.split(' '), 
      adjustedLength = Math.max(array.length, 1), 
      selector = array.slice(0, adjustedLength - 1), 
      event = array[adjustedLength - 1];
    events.push({
      selector: selector.join(' '),
      event,
      functionName,
    });
  }
  return events;
};


// Native jQuery events that should recieve an event object. Plugins can
// add their own methods to this if required.
Delegator.natives = (function() {
  const specials = Object.keys($.event.special || {});
  return `\
blur focus focusin focusout load resize scroll unload click dblclick
mousedown mouseup mousemove mouseover mouseout mouseenter mouseleave
change select submit keydown keypress keyup error\
`.split(/[^a-z]+/).concat(specials);
})();


// Checks to see if the provided event is a DOM event supported by jQuery or
// a custom user event.
//
// event - String event name.
//
// Examples
//
//   Delegator._isCustomEvent('click')              # => false
//   Delegator._isCustomEvent('mousedown')          # => false
//   Delegator._isCustomEvent('annotation:created') # => true
//
// Returns true if event is a custom user event.
Delegator._isCustomEvent = function(event) {
  [event] = event.split('.');
  return $.inArray(event, Delegator.natives) === -1;
};
