'use strict';

const OPERATION_MODES = require('../shared/operationMode');
const ORIGIN_MODES = require('../shared/originMode');

const annotationMetadata = require('./annotation-metadata');
const authorityChecker = require('./authority-checker');

function canTreatAnnotation(annotation, permissions, settings) {
  if (annotationMetadata.isTreated(annotation) 
      || annotationMetadata.isAcceptedOrRejected(annotation)
      || annotationMetadata.isDeleted(annotation) 
      || !isActionAllowedOnOperationMode(annotation, settings, null)) {
    return false;
  }

  return permissions.getUserPermissions().includes('CAN_MARK_TREATED');
}

function canResetAnnotation(annotation, permissions, settings) {
  if (!annotationMetadata.isTreated(annotation) 
      || annotationMetadata.isAcceptedOrRejected(annotation)
      || annotationMetadata.isDeleted(annotation) 
      || !isActionAllowedOnOperationMode(annotation, settings, null)) {
    return false;
  }
  return true;
}

function canDeleteAnnotation(annotation, permissions, settings, userId, isRootSuggestion = false) {
  if (!isActionAllowedOnOperationMode(annotation, settings, userId) || annotationMetadata.isProcessed(annotation)) {
    return false;
  }
  //Validate SENT annotations
  if (annotationMetadata.isSent(annotation)
    && (authorityChecker.isISC(settings) || (annotationMetadata.isReply(annotation) && isRootSuggestion))) {
    if (annotation.document.metadata.responseId !== settings.connectedEntity) {
      // Annotation not from same group as connected unit, so no edit nor delete are allowed.
      return false;
    } else {
      return true;
    }
  }
  //Allow to delete annotations IN_PREPARATION from same DG
  if (canUpdateOrDeleteAnnotationInPreparation(annotation, settings)) {
    return true;
  }
  if (permissions.getUserPermissions().includes('CAN_DELETE')) {
    return true;
  }
  return permissions.permits(annotation.permissions, 'delete', userId);
}

function canMergeSuggestion(annotation, permissions, settings):boolean  {
  if (!isActionAllowedOnOperationMode(annotation, settings, null) || annotationMetadata.isProcessed(annotation)) {
    return false;
  }

  return permissions.getUserPermissions().includes('CAN_MERGE_SUGGESTION')
    && annotationMetadata.isSuggestion(annotation)
    && !originalTextHasBeenModified(annotation);
}

function originalTextHasBeenModified(annotation):boolean {

  const leosSelector = annotation.target[0].selector.find(selector => selector.type === 'LeosSelector')
  const originalAnchoredText = leosSelector !== undefined ? leosSelector.exact : undefined;
  const anchoredTextMatchesOriginalText = annotation.anchoredRangeText === originalAnchoredText;

  return !anchoredTextMatchesOriginalText;
}

function canUpdateAnnotation(annotation, permissions, settings, userId):boolean {
  if (!isActionAllowedOnOperationMode(annotation, settings, userId) || annotationMetadata.isProcessed(annotation)) {
    return false;
  }
  if (annotationMetadata.isSent(annotation) && authorityChecker.isISC(settings)) {
    if (annotation.document.metadata.responseId !== settings.connectedEntity) {
      // Annotation not from same group as connected unit, so no edit nor delete are allowed.
      return false;
    } else {
      return true;
    }
  }
  //Allow to update annotations IN_PREPARATION is from same DG
  if (canUpdateOrDeleteAnnotationInPreparation(annotation, settings)) {
    return true;
  }
  return permissions.permits(annotation.permissions, 'update', userId);
}

function isActionAllowedOnOperationMode(annotation, settings, userId):boolean {
  //In READ ONLY mode no action is allowed
  if (settings.operationMode === OPERATION_MODES.READ_ONLY) {
    return false;
  }
  //In PRIVATE mode user can only operate over his own annotations IN PREPARATION
  if (settings.operationMode === OPERATION_MODES.PRIVATE) {
    return !annotationMetadata.isSent(annotation)
      && authorityChecker.isISC(settings)
      && annotation.document.metadata.responseId === settings.connectedEntity
      && !annotationMetadata.isProcessed(annotation)
      && annotation.user === userId;
  }
  return true;
}

function canReplyToAnnotation(annotation, settings):boolean {
  if (!isActionAllowedOnOperationMode(annotation, settings, null)) {
    return false;
  }
  return !authorityChecker.isISC(settings)
    && !annotationMetadata.isSent(annotation);
}

function canForwardAnnotation(annotation, settings):boolean {
  if (!isActionAllowedOnOperationMode(annotation, settings, null)) {
    return false;
  }
  return !authorityChecker.isISC(settings)
    && !annotationMetadata.isSent(annotation);
}

function canUpdateOrDeleteAnnotationInPreparation(annotation, settings) {
  //Allow to update or delete annotations IN_PREPARATION from if same DG
  return !annotationMetadata.isSent(annotation) &&
    authorityChecker.isISC(settings) &&
    annotation.document.metadata.responseId === settings.connectedEntity;
}

export = {
  canDeleteAnnotation,
  canMergeSuggestion,
  originalTextHasBeenModified,
  canUpdateAnnotation,
  canReplyToAnnotation,
  canTreatAnnotation,
  canForwardAnnotation,
  canResetAnnotation
};
