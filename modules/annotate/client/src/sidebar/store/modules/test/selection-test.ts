'use strict';

const selection = require('../selection');

const leosFixtures = require('../../../../sidebar/components/test/suggestion-fixtures-test');

describe('annotation store', () => {
  const fixtures = require('../../../test/annotation-fixtures');
  const { selectors } = selection;

  describe('#getSelectedAnnotationCount', () => {
    it('returns 0 if 0 of 0 annotations are selected', () => {
      const state = {
        annotations: [],
        selectedAnnotationMap: {},
      };
      assert.equal(selectors.getSelectedAnnotationCount(state), 0);
    });

    it('returns 1 if 1 of 1 annotation is selected', () => {
      const annotation = fixtures.oldAnnotation();
      const state = {
        annotations: [annotation],
        selectedAnnotationMap: { [annotation.id]: true },
      };
      assert.equal(selectors.getSelectedAnnotationCount(state), 1);
    });

    it('returns 1 if 1 of 2 annotations is selected', () => {
      const annotation1 = fixtures.oldAnnotation();
      const annotation2 = fixtures.oldAnnotation();
      annotation2.id += 2;
      const state = {
        annotations: [annotation1, annotation2],
        selectedAnnotationMap: { [annotation1.id]: true },
      };
      assert.equal(selectors.getSelectedAnnotationCount(state), 1);
    });

    it('returns 2 if 2 of 2 annotations are selected', () => {
      const annotation1 = fixtures.oldAnnotation();
      const annotation2 = fixtures.oldAnnotation();
      annotation2.id += 2;
      const state = {
        annotations: [annotation1, annotation2],
        selectedAnnotationMap: { [annotation1.id]: true, [annotation2.id]: true },
      };
      assert.equal(selectors.getSelectedAnnotationCount(state), 2);
    });

  });

  describe('#getSelectedAnnotations', () => {
    it('returns 0 if 0 of 0 annotations are selected', () => {
      const state = {
        annotations: [],
        selectedAnnotationMap: {},
      };
      assert.deepEqual(selectors.getSelectedAnnotations(state), []);
    });

    it('returns 0 if 0 of 1 annotation is selected', () => {
      const annotation = fixtures.oldAnnotation();
      const state = {
        annotations: [annotation],
        selectedAnnotationMap: {},
      };
      assert.deepEqual(selectors.getSelectedAnnotations(state), []);
    });

    it('returns 1 if 1 of 1 annotation is selected', () => {
      const annotation = fixtures.oldAnnotation();
      const state = {
        annotations: [annotation],
        selectedAnnotationMap: { [annotation.id]: true },
      };
      assert.deepEqual(selectors.getSelectedAnnotations(state), [annotation]);
    });

    it('returns 1 if 1 of 2 annotations is selected', () => {
      const annotation1 = fixtures.oldAnnotation();
      const annotation2 = fixtures.oldAnnotation();
      annotation2.id += 2;
      const state = {
        annotations: [annotation1, annotation2],
        selectedAnnotationMap: { [annotation1.id]: true },
      };
      assert.deepEqual(selectors.getSelectedAnnotations(state), [annotation1]);
    });

    it('returns 2 if 2 of 2 annotations are selected', () => {
      const annotation1 = fixtures.oldAnnotation();
      const annotation2 = fixtures.oldAnnotation();
      annotation2.id += 2;
      const state = {
        annotations: [annotation1, annotation2],
        selectedAnnotationMap: { [annotation1.id]: true, [annotation2.id]: true },
      };
      assert.deepEqual(selectors.getSelectedAnnotations(state), [annotation1, annotation2]);
    });
  });

  describe('#getSelectedSuggestions', () => {
    it('returns 0 if 0 of 0 annotations are selected', () => {
      const state = {
        annotations: [],
        selectedAnnotationMap: {},
      };
      assert.deepEqual(selectors.getSelectedSuggestions(state), []);
    });

    it('returns 1 if 1 of 1 suggestion is selected', () => {
      const suggestion = leosFixtures.defaultSuggestion();
      const state = {
        annotations: [suggestion],
        selectedAnnotationMap: { [suggestion.id]: true },
      };
      assert.deepEqual(selectors.getSelectedSuggestions(state), [suggestion]);
    });

    it('returns 0 if 1 non-suggestion of 2 annotations is selected', () => {
      const annotation = fixtures.oldAnnotation();
      const suggestion = leosFixtures.defaultSuggestion();
      const state = {
        annotations: [annotation, suggestion],
        selectedAnnotationMap: { [annotation.id]: true },
      };
      assert.deepEqual(selectors.getSelectedSuggestions(state), []);
    });

    it('returns 1 if 1 suggestion of 2 annotations is selected', () => {
      const annotation = fixtures.oldAnnotation();
      const suggestion = leosFixtures.defaultSuggestion();
      const state = {
        annotations: [annotation, suggestion],
        selectedAnnotationMap: { [suggestion.id]: true },
      };
      assert.deepEqual(selectors.getSelectedSuggestions(state), [suggestion]);
    });

    it('returns 1 if 2 of 2 annotations, one suggestion and one non-suggestion, are selected', () => {
      const annotation = fixtures.oldAnnotation();
      const suggestion = leosFixtures.defaultSuggestion();
      const state = {
        annotations: [annotation, suggestion],
        selectedAnnotationMap: { [annotation.id]: true, [suggestion.id]: true },
      };
      assert.deepEqual(selectors.getSelectedSuggestions(state), [suggestion]);
    });
  });

});
