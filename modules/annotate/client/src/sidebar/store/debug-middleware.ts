'use strict';

/**
 * A debug utility that prints information about internal application state
 * changes to the console.
 *
 * Debugging is enabled by setting `window.debug` to a truthy value.
 *
 * When enabled, every action that changes application state will be printed
 * to the console, along with the application state before and after the action
 * was handled.
 */
function debugMiddleware(store) {
  /* eslint-disable no-console */
  var serial = 0;

  return function (next) {
    return function (action) {

      const debugWindow = window as Window & typeof globalThis & { debug?: any };

      if (!debugWindow.debug) {
        next(action);
        return;
      }

      ++serial;

      var groupTitle = action.type + ' (' + serial.toString() + ')';
      console.group();
      console.log('Prev State:', store.getState());
      console.log('Action:', action);

      next(action);

      console.log('Next State:', store.getState());
      console.log(groupTitle)
      console.groupEnd();
    };
  };
  /* eslint-enable no-console */
}

export = debugMiddleware;
