'use strict';

const unroll = require('../../../shared/test/util').unroll;
const fixture = require('./anchor-test-fixture.html');
const testUtils = require('./common-test-utils');
const { LeosAnchor } = require('../leos-types');




describe('anchoring selectors of type "LeosSelector"', function () {

 /**
 * The list of LEOS selectors, that should be mapped to a range within the document
 * provided within @see fixture.
 */
  const testCases = [
    {
      selector: {
        "type": "LeosSelector",
        "id": "rec_1__p",
        "exact": "accusam et justo duo ",
        "prefix": "At vero eos et ",
        "suffix": "dolores et ea rebum"
      },
      expectedResult: {
        rangeStart: 171,
        rangeEnd: 192
      },
      description: "selector in the middle of a paragraph (without additional whitespace)"
    },
    {
      selector: {
        "type": "LeosSelector",
        id: "rec_2__p",
        exact: "Lorem ipsum dolor sit amet, consetetur",
        prefix: "",
        suffix: " sadipscing elitr, sed diam nonu"
      },
      expectedResult: {
        rangeStart: 0,
        rangeEnd: 38
      },
      description: "selector at the beginning of a paragraph (without additional whitespace)"
    },
    {
      selector: {
        "type": "LeosSelector",
        id: "rec_2__p",
        exact: "no sea takimata sanctus est Lorem ipsum dolor sit amet.",
        prefix: "bum. Stet clita kasd gubergren, ",
        suffix: ""
      },
      expectedResult: {
        rangeStart: 240,
        rangeEnd: 295
      },
      description: "selector at the end of a paragraph (without additional whitespace)"
    },
    {
      selector: {
        "type": "LeosSelector",
        id: "rec_2__p",
        exact: "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.",
        prefix: "",
        suffix: "",
      },
      expectedResult: {
        rangeStart: 0,
        rangeEnd: 295
      },
      description: "selector spanning the entire paragraph (without additional whitespace)"
    },
 
    {
      selector: {
        "type": "LeosSelector",
        id: "rec_2__p",
        exact: "\n                                            \n                                                Lorem ipsum dolor sit amet",
        prefix: "                             (2)",
        suffix: ", consetetur sadipscing elitr, s"
      },
      expectedResult: {
        rangeStart: 0,
        rangeEnd: 27
      },
      description: "selector at the beginning of a paragraph (with additional whitespace)"
    },
    {
      selector: {
        "type": "LeosSelector",
        id: "rec_2__p",
        exact: "Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.",
        prefix: " justo duo dolores et ea rebum. ",
        suffix: "\n                               "
      },
      expectedResult: {
        rangeStart: 213,
        rangeEnd: 295
      },
      description: "selector at the end of a paragraph (with additional whitespace)"
    },
    {
      selector: {
        "type": "LeosSelector",
        id: "rec_2__p",
        exact: "\n                                            \n                                                Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.",
        prefix: "                             (2)",
        suffix: ", consetetur sadipscing elitr, s",
      },
      expectedResult: {
        rangeStart: 0,
        rangeEnd: 295
      },
      description: "selector spanning the entire paragraph (with additional whitespace)"
    },
];

  var dummyHtmlElements;

  beforeEach(function () {
    dummyHtmlElements = testUtils.initializeDummyHtml(fixture);
  });

  afterEach(function () {
    testUtils.removeDummyHtml(dummyHtmlElements);
  });

  unroll('#description', function (testCase) {

    const selector = testCase.selector;
    const leosAnchor = LeosAnchor.fromSelector(dummyHtmlElements.container, selector);

    const computedRange = leosAnchor.toRange();
    const reComputedAnchor = LeosAnchor.fromRange(dummyHtmlElements.container, computedRange)

    const expectedResult = testCase.expectedResult

    assert.equal(reComputedAnchor.start, expectedResult.rangeStart);
    assert.equal(reComputedAnchor.end, expectedResult.rangeEnd);

  }, testCases);
});
