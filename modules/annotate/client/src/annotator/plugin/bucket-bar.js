/*
 * DS201: Simplify complex destructure assignments
 * DS202: Simplify dynamic range loops
 * DS206: Consider reworking classes to avoid initClass
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
let BucketBar;
const raf = require('raf');

const $ = require('jquery');
const Plugin = require('../plugin');

const scrollIntoView = require('scroll-into-view');

const highlighter = require('../highlighter');

const BUCKET_SIZE = 16;                              // Regular bucket size
const BUCKET_NAV_SIZE = BUCKET_SIZE + 6;             // Bucket plus arrow (up/down)
const BUCKET_TOP_THRESHOLD = 115 + BUCKET_NAV_SIZE;  // Toolbar


// Scroll to the next closest anchor off screen in the given direction.
const scrollToClosest = function(anchors, direction) {
  const dir = direction === 'up' ? +1 : -1;
  var {next} = anchors.reduce(function(acc, anchor) {
    let start;
    if (!(anchor.highlights != null ? anchor.highlights.length : undefined)) {
      return acc;
    }

    ({start, next} = acc);
    const rect = highlighter.getBoundingClientRect(anchor.highlights);

    // Ignore if it's not in the right direction.
    if ((dir === 1) && (rect.top >= BUCKET_TOP_THRESHOLD)) {
      return acc;
    } else if ((dir === -1) && (rect.top <= (window.innerHeight - BUCKET_NAV_SIZE))) {
      return acc;
    }

    // Select the closest to carry forward
    if ((next == null)) {
      return {
        start: rect.top,
        next: anchor,
      };
    } else if ((start * dir) < (rect.top * dir)) {
      return {
        start: rect.top,
        next: anchor,
      };
    } else {
      return acc;
    }
  }
  , {});

  scrollIntoView(next.highlights[0]);
};


module.exports = (BucketBar = (function() {
  BucketBar = class BucketBar extends Plugin {
    static initClass() {
      // svg skeleton
      this.prototype.html = '';
  
      // Plugin options
      this.prototype.options = {
        // gapSize parameter is used by the clustering algorithm
        // If an annotation is farther then this gapSize from the next bucket
        // then that annotation will not be merged into the bucket
        gapSize: 60,
  
        // Selectors for the scrollable elements on the page
        scrollables: ['body'],
      };
  
      // buckets of annotations that overlap
      this.prototype.buckets = [];
  
      // index for fast hit detection in the buckets
      this.prototype.index = [];
  
      // tab elements
      this.prototype.tabs = null;
    }

    constructor(element, options) {
      // TODO: Verify that this constructor works, rewrite because of CoffeeScript -> JS (this before super access)
      const html = `\
<div class="annotator-bucket-bar">
</div>\
`;
      super($(html), options);
      this.html = html;
      this.update = this.update.bind(this);

      if (this.options.container != null) {
        $(this.options.container).append(this.element);
      } else {
        $(element).append(this.element);
      }
    }

    pluginInit() {
      $(window).on('resize scroll', this.update);

      (this.options.scrollables || []).map((scrollable) =>
        $(scrollable).on('resize scroll', this.update));
    }

    destroy() {
      $(window).off('resize scroll', this.update);

      (this.options.scrollables || []).map((scrollable) =>
        $(scrollable).off('resize scroll', this.update));
    }

    _collate(a, b) {
      for (let i = 0, end = a.length-1, asc = 0 <= end; asc ? i <= end : i >= end; asc ? i++ : i--) {
        if (a[i] < b[i]) {
          return -1;
        }
        if (a[i] > b[i]) {
          return 1;
        }
      }
      return 0;
    }

    // Update sometime soon
    update() {
      if (this._updatePending != null) { return; }
      return this._updatePending = raf(() => {
        delete this._updatePending;
        this._update();
      });
    }

    _update() {
      // Keep track of buckets of annotations above and below the viewport
      const above = [];
      const below = [];

      const sidebarContainerRect = this.annotator.frame[0].getBoundingClientRect(); //LEOS Change taking account of sidebar frame position
      // Construct indicator points
      const points = this.annotator.anchors.reduce((points, anchor) => {
        if (!(anchor.highlights != null ? anchor.highlights.length : undefined)) {
          return points;
        }

        const rect = highlighter.getBoundingClientRect(anchor.highlights);
        const x = rect.top - sidebarContainerRect.top; //LEOS Change - alignement of the pins: takes account of sidebar position and height compared to the window
        const h = rect.bottom - rect.top;

        if (x < BUCKET_TOP_THRESHOLD) {
          if (!above.includes(anchor)) { above.push(anchor); }
        } else if (x > (sidebarContainerRect.height - BUCKET_NAV_SIZE)) { //LEOS Change - alignement of the pins: takes account of sidebar position and height compared to the window
          if (!below.includes(anchor)) { below.push(anchor); }
        } else {
          points.push([x, 1, anchor]);
          points.push([x + h, -1, anchor]);
        }
        return points;
      }
      , []);

      // Accumulate the overlapping annotations into buckets.
      // The algorithm goes like this:
      // - Collate the points by sorting on position then delta (+1 or -1)
      // - Reduce over the sorted points
      //   - For +1 points, add the annotation at this point to an array of
      //     "carried" annotations. If it already exists, increase the
      //     corresponding value in an array of counts which maintains the
      //     number of points that include this annotation.
      //   - For -1 points, decrement the value for the annotation at this point
      //     in the carried array of counts. If the count is now zero, remove the
      //     annotation from the carried array of annotations.
      //   - If this point is the first, last, sufficiently far from the previous,
      //     or there are no more carried annotations, add a bucket marker at this
      //     point.
      //   - Otherwise, if the last bucket was not isolated (the one before it
      //     has at least one annotation) then remove it and ensure that its
      //     annotations and the carried annotations are merged into the previous
      //     bucket.
      ({buckets: this.buckets, index: this.index} = points
        .sort(this._collate)
        .reduce(({buckets, index, carry}, ...rest) => {
          let a, 
            d, 
            i, 
            j, 
            x;
          let points;
          [x, d, a] = rest[0], i = rest[1], points = rest[2];
          if (d > 0) {                                            // Add annotation
            if ((j = carry.anchors.indexOf(a)) < 0) {
              carry.anchors.unshift(a);
              carry.counts.unshift(1);
            } else {
              carry.counts[j]++;
            }
          } else {                                                // Remove annotation
            j = carry.anchors.indexOf(a);                       // XXX: assert(i >= 0)
            if (--carry.counts[j] === 0) {
              carry.anchors.splice(j, 1);
              carry.counts.splice(j, 1);
            }
          }

          if (
            ((index.length === 0) || (i === (points.length - 1))) ||  // First or last?
          (carry.anchors.length === 0) ||                      // A zero marker?
          ((x - index[index.length-1]) > this.options.gapSize)      // A large gap?
          ) {                                                   // Mark a new bucket.
            buckets.push(carry.anchors.slice());
            index.push(x);
          } else {
          // Merge the previous bucket, making sure its predecessor contains
          // all the carried annotations and the annotations in the previous
          // bucket.
            let a0, 
              last, 
              toMerge;
            if (buckets[buckets.length-2] != null && buckets[buckets.length-2].length) {
              last = buckets[buckets.length-2];
              toMerge = buckets.pop();
              index.pop();
            } else {
              last = buckets[buckets.length-1];
              toMerge = [];
            }
            for (a0 of carry.anchors) { if (!last.includes(a0)) { last.push(a0); } }
            for (a0 of toMerge) { if (!last.includes(a0)) { last.push(a0); } }
          }

          return {buckets, index, carry};
        }
        , {
          buckets: [],
          index: [],
          carry: {
            anchors: [],
            counts: [],
            latest: 0,
          },
        }
        ));

      // Scroll up
      this.buckets.unshift([], above, []);
      this.index.unshift(0, BUCKET_TOP_THRESHOLD - 1, BUCKET_TOP_THRESHOLD);

      // Scroll down
      this.buckets.push([], below, []);
      this.index.push(sidebarContainerRect.height - BUCKET_NAV_SIZE,    //LEOS Change alignement of the bottom pin: takes account of sidebar height - NOT window height
        (sidebarContainerRect.height - BUCKET_NAV_SIZE) + 1,          //LEOS Change alignement of the bottom pin: takes account of sidebar height - NOT window height
        sidebarContainerRect.height);                                 //LEOS Change alignement of the bottom pin: takes account of sidebar height - NOT window height

      // Calculate the total count for each bucket (without replies) and the
      // maximum count.
      let max = 0;
      for (let b of this.buckets) {
        max = Math.max(max, b.length);
      }

      // Update the data bindings
      const {
        element,
      } = this;

      // Keep track of tabs to keep element creation to a minimum.
      if (!this.tabs) { this.tabs = $([]); }

      // Remove any extra tabs and update @tabs.
      this.tabs.slice(this.buckets.length).remove();
      this.tabs = this.tabs.slice(0, this.buckets.length);

      // Create any new tabs if needed.
      $.each(this.buckets.slice(this.tabs.length), () => {
        const div = $('<div/>').appendTo(element);

        this.tabs.push(div[0]);

        div.addClass('annotator-bucket-indicator')

        // Focus corresponding highlights bucket when mouse is hovered
        // TODO: This should use event delegation on the container.
          .on('mousemove', event => {
            const bucket = this.tabs.index(event.currentTarget);
            for (let anchor of this.annotator.anchors) {
              const toggle = this.buckets[bucket].includes(anchor);
              $(anchor.highlights).toggleClass('annotator-hl-focused', toggle);
            };
          }).on('mouseout', event => {
            const bucket = this.tabs.index(event.currentTarget);
            this.buckets[bucket].map((anchor) =>
              $(anchor.highlights).removeClass('annotator-hl-focused'));
          }).on('click', event => {
            const bucket = this.tabs.index(event.currentTarget);
            event.stopPropagation();

            // If it's the upper tab, scroll to next anchor above
            if (this.isUpper(bucket)) {
              scrollToClosest(this.buckets[bucket], 'up');
              // If it's the lower tab, scroll to next anchor below
            } else if (this.isLower(bucket)) {
              scrollToClosest(this.buckets[bucket], 'down');
            } else {
              const annotations = (this.buckets[bucket].map((anchor) => anchor.annotation));
              this.annotator.selectAnnotations(annotations,  //LEOS Change: correction for null pointer exception
                (event.ctrlKey || event.metaKey));
            }
          });
      });
      this._buildTabs(this.tabs, this.buckets);
    }

    _buildTabs() {
      this.tabs.each((d, el) => {
        let bucketSize;
        el = $(el);
        const bucket = this.buckets[d];
        const bucketLength = bucket != null ? bucket.length : undefined;

        let title = 'Show one annotation';
        if (bucketLength !== 1) {
          title = `Show ${bucketLength} annotations`;
        }

        el.attr('title', title);
        el.toggleClass('upper', this.isUpper(d));
        el.toggleClass('lower', this.isLower(d));

        if (this.isUpper(d) || this.isLower(d)) {
          bucketSize = BUCKET_NAV_SIZE;
        } else {
          bucketSize = BUCKET_SIZE;
        }

        el.css({
          top: (this.index[d] + this.index[d+1]) / 2,
          marginTop: -bucketSize / 2,
          display: !bucketLength ? 'none' : '',
        });

        if (bucket) {
          el.html(`<div class='label'>${bucketLength}</div>`);
        }
      });
    }

    isUpper(i) { return i === 1; }
    isLower(i) { return i === (this.index.length - 2); }
  };
  BucketBar.initClass();
  return BucketBar;
})());


// Export constants
BucketBar.BUCKET_SIZE = BUCKET_SIZE;
BucketBar.BUCKET_NAV_SIZE = BUCKET_NAV_SIZE;
BucketBar.BUCKET_TOP_THRESHOLD = BUCKET_TOP_THRESHOLD;
