'use strict';

const ANNOTATION_STATUS = require('./annotation-status');
const RESPONSE_STATUS = require('../shared/response-status');

/**
 * Utility functions for querying annotation metadata.
 */

/** Extract a URI, domain and title from the given domain model object.
 *
 * @param {object} annotation An annotation domain model object as received
 *   from the server-side API.
 * @returns {object} An object with three properties extracted from the model:
 *   uri, domain and title.
 *
 */
function documentMetadata(annotation) {
  var uri = annotation.uri;
  var domain;
  try {
    domain = new URL(uri).hostname;
  } catch (e) {
    domain = '';
  }
  var title = domain;

  if (annotation.document && annotation.document.title) {
    title = annotation.document.title[0];
  }

  if (domain === 'localhost') {
    domain = '';
  }

  return {
    uri: uri,
    domain: domain,
    title: title,
  };
}

/**
 * Return the domain and title of an annotation for display on an annotation
 * card.
 */
function domainAndTitle(annotation) {
  return {
    domain: domainTextFromAnnotation(annotation),
    titleText: titleTextFromAnnotation(annotation),
    titleLink: titleLinkFromAnnotation(annotation),
  };
}

function titleLinkFromAnnotation(annotation) {
  var titleLink = annotation.uri;

  if (titleLink && !(titleLink.indexOf('http://') === 0 || titleLink.indexOf('https://') === 0)) {
    // We only link to http(s) URLs.
    titleLink = null;
  }

  if (annotation.links && annotation.links.incontext) {
    titleLink = annotation.links.incontext;
  }

  return titleLink;
}

function domainTextFromAnnotation(annotation) {
  var document = documentMetadata(annotation);

  var domainText = '';
  if (document.uri && document.uri.indexOf('file://') === 0 && document.title) {
    var parts = document.uri.split('/');
    var filename = parts[parts.length - 1];
    if (filename) {
      domainText = filename;
    }
  } else if (document.domain && document.domain !== document.title) {
    domainText = document.domain;
  }

  return domainText;
}

function titleTextFromAnnotation(annotation) {
  var document = documentMetadata(annotation);

  var titleText = document.title;
  if (titleText.length > 30) {
    titleText = titleText.slice(0, 30) + '…';
  }

  return titleText;
}

/** Return `true` if the given annotation is a reply, `false` otherwise. */
function isReply(annotation) {
  return (annotation.references || []).length > 0;
}
//Return "true" if the annotation is a forward annotation, "false" otherwise
//Return "true" if the annotation is a forward annotation, "false" otherwise
function isForward(annotation):boolean {
  return annotation.forwarded;
}

/** Return `true` if the given annotation is new, `false` otherwise.
 *
 * "New" means this annotation has been newly created client-side and not
 * saved to the server yet.
 */
function isNew(annotation) {
  return !annotation.id;
}

/** Return `true` if the given annotation is public, `false` otherwise. */
function isPublic(annotation) {
  var isPublic = false;

  if (!annotation.permissions) {
    return isPublic;
  }

  annotation.permissions.read.forEach(function(perm) {
    var readPermArr = perm.split(':');
    if (readPermArr.length === 2 && readPermArr[0] === 'group') {
      isPublic = true;
    }
  });

  return isPublic;
}

/**
 * Return `true` if `annotation` has a selector.
 *
 * An annotation which has a selector refers to a specific part of a document,
 * as opposed to a Page Note which refers to the whole document or a reply,
 * which refers to another annotation.
 */
function hasSelector(annotation) {
  return !!(annotation.target &&
            annotation.target.length > 0 &&
            annotation.target[0].selector);
}

/**
 * Return `true` if the given annotation is not yet anchored.
 *
 * Returns false if anchoring is still in process but the flag indicating that
 * the initial timeout allowed for anchoring has expired.
 */
function isWaitingToAnchor(annotation) {
  return hasSelector(annotation) &&
         (typeof annotation.$orphan === 'undefined') &&
         !annotation.$anchorTimeout;
}

/** Return `true` if the given annotation is an orphan. */
function isOrphan(annotation) {
  return hasSelector(annotation) && annotation.$orphan;
}

function isOrphanAndNotProcessed(annotation) {
  return hasSelector(annotation) && annotation.$orphan && !isProcessed(annotation);
}

/** Return `true` if the given annotation is a page note. */
function isPageNote(annotation) {
  return !hasSelector(annotation) && !isReply(annotation);
}

function isPageNoteAndNotProcessed(annotation) {
  return !hasSelector(annotation) && !isReply(annotation) && !isProcessed(annotation);
}

/** Return `true` if the given annotation is a top level annotation, `false` otherwise. */
function isAnnotation(annotation) {
  return !!(hasSelector(annotation) && !isOrphan(annotation));
}

function isAnnotationAndNotProcessed(annotation) {
  return !!(hasSelector(annotation) && !isOrphan(annotation) && !isProcessed(annotation));
}

/** Return a numeric key that can be used to sort annotations by location.
 *
 * @return {number} - A key representing the location of the annotation in
 *                    the document, where lower numbers mean closer to the
 *                    start.
 */
function location(annotation) {
  if (annotation) {
    var targets = annotation.target || [];
    for (var i = 0; i < targets.length; i++) {
      var selectors = targets[i].selector || [];
      for (var k = 0; k < selectors.length; k++) {
        if (selectors[k].type === 'TextPositionSelector') {
          return selectors[k].start;
        }
      }
    }
  }
  return Number.POSITIVE_INFINITY;
}

/**
 * Return the number of times the annotation has been flagged
 * by other users. If moderation metadata is not present, returns `null`.
 *
 * @return {number|null}
 */
function flagCount(ann) {
  if (!ann.moderation) {
    return null;
  }
  return ann.moderation.flagCount;
}

function hasContent(annotation) {
  return annotation.text !== undefined && annotation.text.length > 0;
}

function hasTags(annotation) {
  return annotation.tags && annotation.tags.length > 0;
}

function isHighlight(annotation) {
  // Once an annotation has been saved to the server there's no longer a simple property that says whether it's a highlight or not. Instead an annotation is
  // considered a highlight if it a) has content, b) is linked to a specific part of the document and c) has not been censored (hidden).
  if (isNew(annotation) || isPageNote(annotation) || isReply(annotation) || annotation.hidden) {
    return false;
  }
  return !hasContent(annotation) && !hasTags(annotation);
}

function isSuggestion(annotation) {
  return annotation.tags && annotation.tags.includes('suggestion');
}

function isProcessed(annotation) {
  return annotation.status !== undefined && annotation.status.status !== ANNOTATION_STATUS.NORMAL;
}

function isSent(annotation) {
  return annotation.document !== undefined && annotation.document.metadata !== undefined && annotation.document.metadata.responseStatus === RESPONSE_STATUS.SENT;
}

function isTreated(annotation) {
	return annotation.status !== undefined && annotation.status.status === ANNOTATION_STATUS.TREATED;
}

function isDeleted(annotation) {
	return annotation.status !== undefined && annotation.status.status === ANNOTATION_STATUS.DELETED;
}

function isAcceptedOrRejected(annotation) {
	return isSuggestion(annotation) && annotation.status !== undefined && 
	    (annotation.status.status === ANNOTATION_STATUS.ACCEPTED || annotation.status.status === ANNOTATION_STATUS.REJECTED);
}

export = {
  documentMetadata,
  domainAndTitle,
  flagCount,
  hasContent,
  hasTags,
  isAnnotation,
  isAnnotationAndNotProcessed,
  isForward,
  isHighlight,
  isNew,
  isOrphan,
  isOrphanAndNotProcessed,
  isPageNote,
  isPageNoteAndNotProcessed,
  isProcessed,
  isPublic,
  isReply,
  isSent,
  isSuggestion,
  isWaitingToAnchor,
  isTreated,
  isDeleted,
  isAcceptedOrRejected,
  location,
};
