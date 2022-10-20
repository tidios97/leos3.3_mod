// Do LEOS specific actions here
let LeosDocument;
const $ = require('jquery');
const Document = require('./document');

module.exports = (LeosDocument = class LeosDocument extends Document {
  constructor(element, config) {
    const containerSelector = `${config.annotationContainer}`;
    super($(containerSelector)[0], config);
    // Make a definitive check for commentable area Or even better -> read from config
    this.containerSelector = containerSelector;
    this.documentIdSelector = `${config.annotationContainer} ${config.leosDocumentRootNode}`;
  }

  getLeosDocumentMetadata() {
    const self = this;
    const requestLeosMetadata = function(resolve, reject) {
      const promiseTimeout = setTimeout(() => reject('timeout')
        , 500);
      if (self.getElement().hostBridge != null) {
        self.getElement().hostBridge.responseDocumentMetadata = function(metadata) {
          console.log('Received message from host for request DocumentMetadata');
          const leosMetadata = JSON.parse(metadata);
          return resolve(leosMetadata);
        };
        if (self.getElement().hostBridge.requestDocumentMetadata != null) {
          return self.getElement().hostBridge.requestDocumentMetadata();
        }
      }
    };
    return new Promise(requestLeosMetadata);
  }
    
  pluginInit() {
    this.annotator.anchoring = require('../anchoring/leos');
    super.pluginInit(...arguments);
  }

  //LEOS-2789 the reference element 'root' is now defined in the plugin document
  getElement() {
    //The root element tag is NOT taken in account while building the xpath by HTML and RANGE classes,
    //In XPath, we will get sth like '//akomantoso[1]/...'.
    const documentContainer = $(this.containerSelector)[0];
    return documentContainer;
  }

  // LEOS document will provide its own static id
  _getDocumentHref() {
    const leosDocument = $(this.documentIdSelector)[0];
    if (leosDocument.id) { return `uri://LEOS/${leosDocument.id}`; }
    return super._getDocumentHref();
  }
});
