'use strict';
var sinon = require('sinon');

class FakeWindow {

  private callbacks;
  public screen;

  public open;
  private url;

  public setTimeout;
  public clearTimeout;

  constructor() {
    this.callbacks = [];

    this.screen = {
      width: 1024,
      height: 768,
    };


    this.location = 'https://client.hypothes.is/app.html';
    this.open = sinon.spy(href => {
      var win = new FakeWindow;
      win.location = href;
      return win;
    });

    this.setTimeout = window.setTimeout.bind(window);
    this.clearTimeout = window.clearTimeout.bind(window);
  }

  get location() {
    return this.url;
  }

  set location(href) {
    this.url = new URL(href);
  }

  addEventListener(event, callback) {
    this.callbacks.push({event, callback});
  }

  removeEventListener(event, callback) {
    this.callbacks = this.callbacks.filter((cb) =>
      !(cb.event === event && cb.callback === callback)
    );
  }

  trigger(event) {
    this.callbacks.forEach((cb) => {
      if (cb.event === event.type) {
        cb.callback(event);
      }
    });
  }

  sendMessage(data) {
    var evt = new MessageEvent('message', { data });
    this.trigger(evt);
  }
}

export = FakeWindow;
