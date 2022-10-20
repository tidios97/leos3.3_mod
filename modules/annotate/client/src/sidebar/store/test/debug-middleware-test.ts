'use strict';

/* eslint-disable no-console */

var redux = require('redux');

var debugMiddleware = require('../debug-middleware');

function id(state) {
  return state;
}

describe('debug middleware', function () {
  type ExtendedConsole = Console & { log: any, group: any, groupEnd: any };
  type ExtendedWindow = Window & typeof globalThis & { debug: any };

  var store;

  beforeEach(function () {
    sinon.stub(console, 'log');
    sinon.stub(console, 'group');
    sinon.stub(console, 'groupEnd');

    var enhancer = redux.applyMiddleware(debugMiddleware);
    store = redux.createStore(id, {}, enhancer);
  });

  afterEach(function () {
    var extendedConsole = console as ExtendedConsole;
    extendedConsole.log.restore();
    extendedConsole.group.restore();
    extendedConsole.groupEnd.restore();

    var extendedWindow = window as ExtendedWindow;
    delete extendedWindow.debug;
  });

  it('logs app state changes when "window.debug" is truthy', function () {
    var extendedWindow = window as ExtendedWindow;
    extendedWindow.debug = true;
    store.dispatch({type: 'SOMETHING_HAPPENED'});
    assert.called(console.log);
  });

  it('logs nothing when "window.debug" is falsey', function () {
    store.dispatch({type: 'SOMETHING_HAPPENED'});
    assert.notCalled(console.log);
  });
});