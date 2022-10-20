const BucketBar = require('../bucket-bar');

describe('BucketBar', function() {
  const createBucketBar = function(options) {
    const element = document.createElement('div');
    return new BucketBar(element, options || {});
  };

  // Yes this is testing a private method. Yes this is bad practice, but I'd
  // rather test this functionality in a private method than not test it at all.
  describe('_buildTabs', function() {
    const setup = function(tabs) {
      const bucketBar = createBucketBar();
      bucketBar.tabs = tabs;
      bucketBar.buckets = [['AN ANNOTATION?']];
      bucketBar.index = [
        0,
        BucketBar.BUCKET_TOP_THRESHOLD - 1,
        BucketBar.BUCKET_TOP_THRESHOLD,
      ];
      return bucketBar;
    };

    it('creates a tab with a title', function() {
      const tab = $('<div />');
      const bucketBar = setup(tab);

      bucketBar._buildTabs();
      assert.equal(tab.attr('title'), 'Show one annotation');
    });

    it('creates a tab with a pluralized title', function() {
      const tab = $('<div />');
      const bucketBar = setup(tab);
      bucketBar.buckets[0].push('Another Annotation?');

      bucketBar._buildTabs();
      assert.equal(tab.attr('title'), 'Show 2 annotations');
    });

    it('sets the tab text to the number of annotations', function() {
      const tab = $('<div />');
      const bucketBar = setup(tab);
      bucketBar.buckets[0].push('Another Annotation?');

      bucketBar._buildTabs();
      assert.equal(tab.text(), '2');
    });

    it('sets the tab text to the number of annotations', function() {
      const tab = $('<div />');
      const bucketBar = setup(tab);
      bucketBar.buckets[0].push('Another Annotation?');

      bucketBar._buildTabs();
      assert.equal(tab.text(), '2');
    });

    it('adds the class "upper" if the annotation is at the top', function() {
      const tab = $('<div />');
      const bucketBar = setup(tab);
      sinon.stub(bucketBar, 'isUpper').returns(true);

      bucketBar._buildTabs();
      assert.equal(tab.hasClass('upper'), true);
    });

    it('removes the class "upper" if the annotation is not at the top', function() {
      const tab = $('<div />').addClass('upper');
      const bucketBar = setup(tab);
      sinon.stub(bucketBar, 'isUpper').returns(false);

      bucketBar._buildTabs();
      assert.equal(tab.hasClass('upper'), false);
    });

    it('adds the class "lower" if the annotation is at the top', function() {
      const tab = $('<div />');
      const bucketBar = setup(tab);
      sinon.stub(bucketBar, 'isLower').returns(true);

      bucketBar._buildTabs();
      assert.equal(tab.hasClass('lower'), true);
    });

    it('removes the class "lower" if the annotation is not at the top', function() {
      const tab = $('<div />').addClass('lower');
      const bucketBar = setup(tab);
      sinon.stub(bucketBar, 'isLower').returns(false);

      bucketBar._buildTabs();
      assert.equal(tab.hasClass('lower'), false);
    });

    it('reveals the tab if there are annotations in the bucket', function() {
      const tab = $('<div />');
      const bucketBar = setup(tab);

      bucketBar._buildTabs();
      assert.equal(tab.css('display'), '');
    });

    it('hides the tab if there are no annotations in the bucket', function() {
      const tab = $('<div />');
      const bucketBar = setup(tab);
      bucketBar.buckets = [];

      bucketBar._buildTabs();
      assert.equal(tab.css('display'), 'none');
    });
  });
});
