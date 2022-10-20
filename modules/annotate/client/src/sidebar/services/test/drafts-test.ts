'use strict';

var draftsService = require('../drafts');

describe('drafts', function () {
  var drafts;

  var fixtures = {
    draftWithText: {
      forwardJustification: '',
      isPrivate: false,
      text: 'some text',
      tags: [],
      justification: null,
    },
    draftWithTags: {
      forwardJustification: '',
      isPrivate: false,
      text: '',
      tags: ['atag'],
      justification: null,
    },
    emptyDraft: {
      isPrivate: false,
      text: '',
      tags: [],
      justification: null,
    },
  };
  
  beforeEach(function () {
    drafts = draftsService();
  });

  describe('#getIfNotEmpty', function () {
    it('returns the draft if it has tags', function () {
      var model = {id: 'foo'};
      drafts.update(model, fixtures.draftWithTags);
      assert.deepEqual(drafts.getIfNotEmpty(model), fixtures.draftWithTags);
    });

    it('returns the draft if it has text', function () {
      var model = {id: 'foo'};
      drafts.update(model, fixtures.draftWithText);
      assert.deepEqual(drafts.getIfNotEmpty(model), fixtures.draftWithText);
    });

    it('returns null if the text and tags are empty', function () {
      var model = {id: 'foo'};
      drafts.update(model, fixtures.emptyDraft);
      assert.isNull(drafts.getIfNotEmpty(model));
    });
  });

  describe('#update', function () {
    it('should save changes', function () {
      var model = {id: 'foo'};
      assert.notOk(drafts.get(model));
      drafts.update(model, {isPrivate:true, tags:['foo'], text:'edit'});
      assert.deepEqual(
        drafts.get(model),
        {forwardJustification:undefined, isPrivate: true, tags: ['foo'], text: 'edit', justification: undefined});
    });

    it('should replace existing drafts', function () {
      var model = {id: 'foo'};
      drafts.update(model, {isPrivate:true, tags:['foo'], text:'foo'});
      drafts.update(model, {isPrivate:true, tags:['foo'], text:'bar'});
      assert.equal(drafts.get(model).text, 'bar');
    });

    it('should replace existing drafts with the same ID', function () {
      var modelA = {id: 'foo'};
      var modelB = {id: 'foo'};
      drafts.update(modelA, {isPrivate:true, tags:['foo'], text:'foo'});
      drafts.update(modelB, {isPrivate:true, tags:['foo'], text:'bar'});
      assert.equal(drafts.get(modelA).text, 'bar');
    });

    it('should replace drafts with the same tag', function () {
      var modelA = {$tag: 'foo'};
      var modelB = {$tag: 'foo'};
      drafts.update(modelA, {isPrivate:true, tags:['foo'], text:'foo'});
      drafts.update(modelB, {isPrivate:true, tags:['foo'], text:'bar'});
      assert.equal(drafts.get(modelA).text, 'bar');
    });
  });

  describe('#remove', function () {
    it('should remove drafts', function () {
      var model = {id: 'foo'};
      drafts.update(model, {text: 'bar'});
      drafts.remove(model);
      assert.notOk(drafts.get(model));
    });
  });

  describe('#unsaved', function () {
    it('should return drafts for unsaved annotations', function () {
      var model = {$tag: 'local-tag', id: undefined};
      drafts.update(model, {text: 'bar'});
      assert.deepEqual(drafts.unsaved(), [model]);
    });

    it('should not return drafts for saved annotations', function () {
      var model = {id: 'foo'};
      drafts.update(model, {text: 'baz'});
      assert.deepEqual(drafts.unsaved(), []);
    });
  });
});
