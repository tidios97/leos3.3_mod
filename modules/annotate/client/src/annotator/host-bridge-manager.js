/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * DS201: Simplify complex destructure assignments
 * DS206: Consider reworking classes to avoid initClass
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */

let HostBridgeManager;
const REQUEST_TYPES = ['SecurityToken','UserPermissions','MergeSuggestion','MergeSuggestions','DocumentMetadata','SearchMetadata'];

module.exports = (HostBridgeManager = (function() {
  let _addHostBridgeHandlers;
  HostBridgeManager = class HostBridgeManager {
    static initClass() {
      this.prototype.callbackManager = {};
  
      _addHostBridgeHandlers = function(requestType) {
        const self = this;
        // Add listeners on cross frame (communication between iframe and host) for each request type
        return self.crossframe.on(`request${requestType}`, (...args1) => {
          const adjustedLength = Math.max(args1.length, 1), 
            args = args1.slice(0, adjustedLength - 1), 
            callback = args1[adjustedLength - 1];
          console.log(`Request ${requestType} to be sent to host`);
          if (self.hostBridge[`request${requestType}`] && (typeof self.hostBridge[`request${requestType}`] === 'function')) {
            // Add handler on host bridge to let leos application responds
            self.hostBridge[`response${requestType}`] = function(data) {
              console.log(`Received message from host for request ${requestType}`);
              return callback(null, data);
            };
            return self.hostBridge[`request${requestType}`](...(args || []));
          } else {
            return callback('No available request handler on bridge');
          }
        });
      };
    }

    constructor(hostBridge, crossframe) {
      this.crossframe = crossframe;
      this.hostBridge = hostBridge;
      const self = this;

      if ((this.crossframe != null) && (this.hostBridge != null)) {
        for (let requestType of REQUEST_TYPES) {
          _addHostBridgeHandlers.call(self, requestType);
        }
        self.hostBridge.stateChangeHandler = state => self.crossframe.call('stateChangeHandler', state);
        self.hostBridge.requestFilteredAnnotations = callback => self.crossframe.call('LEOS_requestFilteredAnnotations', callback);
        self.crossframe.on('LEOS_responseFilteredAnnotations', annotations => self.hostBridge.responseFilteredAnnotations(annotations));
      }
    }
  };
  HostBridgeManager.initClass();
  return HostBridgeManager;
})());

