'use strict';

/** Return an comment domain model object for a new comment
 */
function newSuggestion() {
  return {
    id: undefined,
    $highlight: undefined,
    target: ['foo', 'bar'],
    references: [],
    text: 'Annotation text',
    tags: ['suggestion'],
  };
}

/** Return an comment domain model object for a new comment
 */
function newComment() {
  return {
    id: undefined,
    $highlight: undefined,
    target: ['foo', 'bar'],
    references: [],
    text: 'Annotation text',
    tags: ['comment'],
  };
}

/** Return an highlight domain model object for a new highlight
 */
function newHighlight() {
  return {
    id: undefined,
    $highlight: true,
    target: [{source: 'http://example.org'}],
    tags: ['highlight'],
  };
}

/**
 * Return a fake comment with the basic properties filled in.
 */
function defaultComment() {
  return {
    id: 'deadbeef',
    tags: ['comment'],
    permissions: {
        read:['group:__world__'],
        write:['group:__world__'],
    },
    document: {
      title: 'A special document',
    },
    target: [{source: 'source', 'selector': []}],
    uri: 'http://example.com',
    user: 'acct:bill@localhost',
    updated: '2015-05-10T20:18:56.613388+00:00',
  };
}

/**
 * Return a fake suggestion with the basic properties filled in.
 */
function defaultSuggestion() {
  return {
    id: 'deadbeef',
    tags: ['suggestion'],
    permissions: {
        read:['group:__world__'],
        write:['group:__world__'],
    },
    document: {
      title: 'A special document',
    },
    target: [{source: 'source', 'selector': []}],
    uri: 'http://example.com',
    user: 'acct:bill@localhost',
    updated: '2015-05-10T20:18:56.613388+00:00',
  };
}

/**
 * Return a fake suggestion with the basic properties filled in.
 */
function defaultHighlight() {
  return {
    id: 'deadbeef',
    tags: ['highlight'],
    permissions: {
        read:['group:__world__'],
        write:['group:__world__'],
    },
    document: {
      title: 'A special document',
    },
    target: [{source: 'source', 'selector': []}],
    uri: 'http://example.com',
    user: 'acct:bill@localhost',
    updated: '2015-05-10T20:18:56.613388+00:00',
  };
}

module.exports = {
  defaultComment: defaultComment,
  defaultSuggestion: defaultSuggestion,
  defaultHighlight: defaultHighlight,
  newComment: newComment,
  newSuggestion: newSuggestion,
  newHighlight: newHighlight,
};