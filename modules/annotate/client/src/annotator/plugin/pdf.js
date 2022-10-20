/*
 * decaffeinate suggestions:
 * DS202: Simplify dynamic range loops
 * DS206: Consider reworking classes to avoid initClass
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
let PDF;
const Plugin = require('../plugin');

const RenderingStates = require('../pdfjs-rendering-states');

module.exports = (PDF = (function() {
  PDF = class PDF extends Plugin {
    static initClass() {
      this.prototype.documentLoaded = null;
      this.prototype.observer = null;
      this.prototype.pdfViewer = null;
    }

    pluginInit() {
      this.annotator.anchoring = require('../anchoring/pdf');
      const PDFMetadata = require('./pdf-metadata');

      this.pdfViewer = PDFViewerApplication.pdfViewer;
      this.pdfViewer.viewer.classList.add('has-transparent-text-layer');

      this.pdfMetadata = new PDFMetadata(PDFViewerApplication);

      this.observer = new MutationObserver(mutations => this._update());
      this.observer.observe(this.pdfViewer.viewer, {
        attributes: true,
        attributeFilter: ['data-loaded'],
        childList: true,
        subtree: true,
      });
    }

    destroy() {
      this.pdfViewer.viewer.classList.remove('has-transparent-text-layer');
      this.observer.disconnect();
    }

    uri() {
      return this.pdfMetadata.getUri();
    }

    getMetadata() {
      return this.pdfMetadata.getMetadata();
    }

    //LEOS-2789 the reference element 'root' is now defined in the plugin document
    getElement() {
      return this.element[0];
    }

    // This method (re-)anchors annotations when pages are rendered and destroyed.
    _update() {
      let anchor;
      const {annotator, pdfViewer} = this;

      // A list of annotations that need to be refreshed.
      const refreshAnnotations = [];

      // Check all the pages with text layers that have finished rendering.
      for (let pageIndex = 0, end = pdfViewer.pagesCount, asc = 0 <= end; asc ? pageIndex < end : pageIndex > end; asc ? pageIndex++ : pageIndex--) {
        const page = pdfViewer.getPageView(pageIndex);
        if (!(page.textLayer != null ? page.textLayer.renderingDone : undefined)) { continue; }

        const div = page.div || page.el;
        const placeholder = div.getElementsByClassName('annotator-placeholder')[0];

        // Detect what needs to be done by checking the rendering state.
        switch (page.renderingState) {
        case RenderingStates.INITIAL:
          // This page has been reset to its initial state so its text layer
          // is no longer valid. Null it out so that we don't process it again.
          page.textLayer = null;
          break;
        case RenderingStates.FINISHED:
          // This page is still rendered. If it has a placeholder node that
          // means the PDF anchoring module anchored annotations before it was
          // rendered. Remove this, which will cause the annotations to anchor
          // again, below.
          if (placeholder != null) {
            placeholder.parentNode.removeChild(placeholder);
          }
          break;
        }
      }

      // Find all the anchors that have been invalidated by page state changes.
      for (anchor of annotator.anchors) {
        // Skip any we already know about.
        if (anchor.highlights != null) {
          if (refreshAnnotations.includes(anchor.annotation)) {
            continue;
          }

          // If the highlights are no longer in the document it means that either
          // the page was destroyed by PDF.js or the placeholder was removed above.
          // The annotations for these anchors need to be refreshed.
          for (let hl of anchor.highlights) {
            if (!document.body.contains(hl)) {
              delete anchor.highlights;
              delete anchor.range;
              refreshAnnotations.push(anchor.annotation);
              break;
            }
          }
        }
      }

      return refreshAnnotations.map((annotation) =>
        annotator.anchor(annotation));
    }
  };
  PDF.initClass();
  return PDF;
})());
