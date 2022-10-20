let Plugin;
const Delegator = require('./delegator');

module.exports = (Plugin = class Plugin extends Delegator {
  constructor(element, options) {
    super(...arguments);
  }

  pluginInit() {}
});
