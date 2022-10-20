'use strict';

var diff_match_patch = require('diff-match-patch');
import 'diff-match-patch-line-and-word';
import systemId = require('../../shared/system-id');


var annotationMetadata = require('../annotation-metadata');
var authorityChecker = require('../authority-checker');
var authorizer = require('../authorizer');
var events = require('../events');
var { isThirdPartyUser } = require('../util/account-id');
var OPERATION_MODES = require('../../shared/operationMode');
var ORIGIN_MODES = require('../../shared/originMode');
var ANNOTATION_STATUS = require('../annotation-status');

var hasContent = annotationMetadata.hasContent;
var isHighlight = annotationMetadata.isHighlight;
var isNew = annotationMetadata.isNew;
var isReply = annotationMetadata.isReply;
var isPageNote = annotationMetadata.isPageNote;
var isForward = annotationMetadata.isForward;





/**
 * Return a copy of `annotation` with changes made in the editor applied.
 */
function updateModel(annotation, changes, permissions) {
  var userid = annotation.user;

  return Object.assign({}, annotation, {
    // Apply changes from the draft
    tags: changes.tags,
    text: changes.text,
    justification: changes.justification,
    forwardJustification : changes.forwardJustification,
    permissions: changes.isPrivate ?
      permissions.private(userid) : permissions.shared(userid, annotation.group),
  });
}

// @ngInject
function AnnotationController($rootScope, $scope, $timeout, $window, analytics, store, annotationMapper, api, drafts, flash, groups, permissions, serviceUrl,
  session, settings, streamer, features) {
  let metadataToDisplay = {};
  var self = this;
  var newlyCreatedByHighlightButton;
  
  self.markAsTreated = false;

  /** Save an annotation to the server. */
  function save(annot, loggedUserId?, loggedUserDisplayName?) {
    
    var saved;
    var updating = !!annot.id;

    if (authorityChecker.isISC(settings)) {
      //LEOS-3992 : In ISC context, we need to certify that the group is always the connectedEntity
      if (settings.connectedEntity) {
        annot.group = settings.connectedEntity;
      }

      //LEOS-3839 : In ISC context, if annotation is SENT and we are editing it, we need to increment the responseVersion
      if (annot.document
        && annotationMetadata.isSent(annot)
        && settings.displayMetadataCondition.responseVersion
        && !isNaN(parseInt(settings.displayMetadataCondition.responseVersion))) {
        annot.document.metadata.responseVersion = settings.displayMetadataCondition.responseVersion;
        if (loggedUserId && loggedUserDisplayName) {
          annot.user = loggedUserId;
          annot.user_info.display_name = loggedUserDisplayName;
        }
      }
    }

    if (updating) {
      //LEOS-4046 : Send connectedEntity on annotation create
      saved = api.annotation.update({ id: annot.id, connectedEntity: settings.connectedEntity }, annot);
    } else {
      //LEOS-4046 : Send connectedEntity on annotation update
      saved = api.annotation.create({ connectedEntity: settings.connectedEntity }, annot);
    }

    return saved.then(function (savedAnnot) {

      var event;

      // Copy across internal properties which are not part of the annotation
      // model saved on the server
      savedAnnot.$tag = annot.$tag;
      Object.keys(annot).forEach(function (k) {
        if (k[0] === '$') {
          savedAnnot[k] = annot[k];
        }
      });


      if (self.isReply()) {
        event = updating ? analytics.events.REPLY_UPDATED : analytics.events.REPLY_CREATED;
      } else if (self.isHighlight()) {
        event = updating ? analytics.events.HIGHLIGHT_UPDATED : analytics.events.HIGHLIGHT_CREATED;
      } else if (isPageNote(self.annotation)) {
        event = updating ? analytics.events.PAGE_NOTE_UPDATED : analytics.events.PAGE_NOTE_CREATED;
      } else {
        event = updating ? analytics.events.ANNOTATION_UPDATED : analytics.events.ANNOTATION_CREATED;
      }

      analytics.track(event);

      return savedAnnot;
    });
  
}

  /**
    * Initialize the controller instance.
    *
    * All initialization code except for assigning the controller instance's
    * methods goes here.
    */
  this.$onInit = () => {
    /** Determines whether controls to expand/collapse the annotation body
     * are displayed adjacent to the tags field.
     */
    self.canCollapseBody = false;

    /** Determines whether the annotation body should be collapsed. */
    self.collapseBody = true;

    /** True if the annotation is currently being saved. */
    self.isSaving = false;

    /** True if the 'Share' dialog for this annotation is currently open. */
    self.showShareDialog = false;

    /**
      * `true` if this AnnotationController instance was created as a result of
      * the highlight button being clicked.
      *
      * `false` if the annotation button was clicked, or if this is a highlight
      * or annotation that was fetched from the server (as opposed to created
      * new client-side).
      */
    newlyCreatedByHighlightButton = self.annotation.$highlight || false;

    // New annotations (just created locally by the client, rather then
    // received from the server) have some fields missing. Add them.
    //
    // FIXME: This logic should go in the `addAnnotations` Redux action once all
    // required state is in the store.
    self.annotation.user = self.annotation.user || session.state.userid;
    self.annotation.user_info = self.annotation.user_info || session.state.user_info;
    //LEOS-3992 : on ISC -> always use connectedEntity when creating new annotations
    if (authorityChecker.isISC(settings) && groups.get(settings.connectedEntity) && isNew(self.annotation)) {
      self.annotation.group = groups.get(settings.connectedEntity).id;
      groups.focus(self.annotation.group);
    }
    //If group not defined means: not in ISC or connectedEntity not valid. In that case, use default behaviour
    if (!self.annotation.group) {
      self.annotation.group = self.annotation.group || groups.focused().id;
    }

    if (!self.annotation.permissions) {
      //LEOS-3992 : on operationMode.PRIVATE (used by ISC) annotations can only be private
      if (settings.operationMode === OPERATION_MODES.PRIVATE) {
        self.annotation.permissions = permissions.private(self.annotation.user);
      } else {
        self.annotation.permissions = permissions.default(self.annotation.user, self.annotation.group);
      }

    }
    self.annotation.text = self.annotation.text || '';
    self.annotation.precedingText = self.annotation.precedingText || '';
    self.annotation.succeedingText = self.annotation.succeedingText || '';
    self.annotation.justification = self.annotation.justification || { text: '' };
    self.annotation.forwardJustification = self.annotation.forwardJustification || '';
    self.annotation.forwarded = self.annotation.forwarded || false;

    if (!Array.isArray(self.annotation.tags)) {
      self.annotation.tags = [];
    }

    // Automatically save new highlights to the server when they're created.
    // Note that this line also gets called when the user logs in (since
    // AnnotationController instances are re-created on login) so serves to
    // automatically save highlights that were created while logged out when you
    // log in.
    saveNewHighlight();

    // If this annotation is not a highlight and if it's new (has just been
    // created by the annotate button) or it has edits not yet saved to the
    // server - then open the editor on AnnotationController instantiation.
    if (!newlyCreatedByHighlightButton) {
      if (isNew(self.annotation) || drafts.get(self.annotation)) {
        self.edit();
      }
    }

    if (self.annotation.document && self.annotation.document.metadata && !self.isReplyOfRootSuggestion()) {
      metadataToDisplay = Object.keys(self.annotation.document.metadata)
        .filter(key => Object.keys(settings.displayMetadataCondition).indexOf(key) !== -1)
        .reduce((obj, key) => {
          obj[settings.displayMetadataCondition[key]] = self.annotation.document.metadata[key];
          return obj;
        }, {});
    }

    self.annotation.hovered = false;

    self.markAsTreated = annotationMetadata.isTreated(self.annotation);
  };

  this.isHovered = function () {
    return this.annotation.hovered;
  };

  this.deleteOrphanSuggestion = function () {
    var isOrphanTab = store.getState().selectedTab === 'orphan';
    if (!isOrphanTab) {
      return self.isSuggestion();
    } else {
      return false;
    }
  };

  this.showButtons = function () {
    self.annotation.hovered = true;
  };

  this.hideButtons = function () {
    self.annotation.hovered = false;
  };

  this.isDocumentNote = function () {
    return annotationMetadata.isPageNote(self.annotation);
  }

  this.isSuggestion = function () {
    return annotationMetadata.isSuggestion(this.state());
  };

  this.updateSelectedGroup = function (group) {
    self.annotation.group = group.id; 
  };

  this.getMetadata = function () {
    return metadataToDisplay;
  };

  this.getReaders = function ()  {
    return self.annotation.group;
  };
  this.shouldDisplayMetadata = function () {
    return (Object.keys(metadataToDisplay).length >= 0);
  };

  this.getMetadataInfoStyle = function (keytoFind) {
    var index = Object.keys(metadataToDisplay).indexOf(keytoFind);
    return `leos-metadata-info-${index}`;
  };

  this.diffText = function () {
    var htmlDiff = self.state().text;
    if ((self.editing() && !self.isForward())) {
      return htmlDiff;
    }
    var origText = self.quote();
    if (self.isSuggestion() && origText) {
      var dmp = new diff_match_patch();
      var textDiff = dmp.diff_wordMode(origText, self.state().text);
      htmlDiff = '<span class="leos-content-modified">';
      for (let d of textDiff) {
        if (d[0] === -1) {
          htmlDiff += `<span class="leos-content-removed">${d[1]}</span>`;
        }
        else if (d[0] === 0) {
          htmlDiff += d[1];
        }
        else if (d[0] === 1) {
          htmlDiff += `<span class="leos-content-new">${d[1]}</span>`;
        }
      }
      htmlDiff += '</span>';
    }
    return htmlDiff;
  };

  this.justificationText = function () {
    return this.state().justification ? this.state().justification.text : '';
  };

  this.setJustificationText = function (text) {
    drafts.update(this.annotation, {
      isPrivate: this.state().isPrivate,
      tags: this.state().tags,
      text: this.state().text,
      justification: { text },
      forwardJustification: self.state().forwardJustification
    });
  };

  this.isReplyOfRootSuggestion = function () {
    return this.isRootSuggestion && this.isReply();
  };

  this.replyLabel = function () {

    if (this.isChildReplyInEditingState) {
      return 'Add reply';
    }

    let label = this.isCollapsed ? 'Show' : 'Hide';
    label += ' ';
    if (this.replyCount === 1) { // eslint-disable-line no-lonely-if
      label += 'reply';
    } else {
      label += 'replies';
    }
    return label;
  };

  this.isISC = function() {
	return authorityChecker.isISC(settings);
  }

  this.isProcessed = function () {
    return annotationMetadata.isProcessed(self.annotation);
  };

  this.wasDeleted = function() {
	return annotationMetadata.isDeleted(self.annotation);
  };

  this.isAcceptedOrRejected = function() {
	return annotationMetadata.isAcceptedOrRejected(self.annotation);
  };

  this.isDeleteButtonShown = function () {
    return self.isHovered()
      && !self.isSaving 
      && !self.isProcessed()
      && authorizer.canDeleteAnnotation(self.annotation, permissions, settings, session.state.userid, self.isRootSuggestion)
      && !(authorizer.canMergeSuggestion(self.annotation, permissions, settings) && self.deleteOrphanSuggestion());
  };

  this.isEditButtonShown = function () {
    return self.isHovered()
      && !self.isSaving 
      && !self.isProcessed()
      && authorizer.canUpdateAnnotation(self.annotation, permissions, settings, session.state.userid);
  };

  this.isReplyButtonShown = function () {
    return self.isHovered()
      && !self.isSaving && !self.isProcessed()
      && authorizer.canReplyToAnnotation(self.annotation, settings)
      && !self.isProcessed()
      && !self.state().isPrivate;
  };

  this.isForwardButtonShown = function () {
    return self.isHovered()
      && features.flagEnabled('forward_annotations')
      && !authorityChecker.isISC(settings)
      && !self.isSaving
      && authorizer.canForwardAnnotation(self.annotation, settings)
      && !self.state().isPrivate
      && !self.isProcessed()
      && !self.isForward();
  };

  this.isContributionLabelShown = function () {
    return this.isHovered()
      && !this.isSaving
      && this.annotation.document.metadata.originMode === ORIGIN_MODES.PRIVATE
      && this.annotation.user_info.display_name !== this.annotation.document.metadata.ISCReference
      && authorizer.canUpdateAnnotation(this.annotation, permissions, settings, session.state.userid);
  }

  this.isJustificationShown = function () {

    const justificationText = this.justificationText();

    if (this.editing()) {
      return this.isSuggestion();
    } else {
      return this.isSuggestion() && justificationText;
    }

  };

  this.isMarkAsTreatedShown = function() {

    if(!authorizer.canTreatAnnotation(self.annotation, permissions, settings)) {
      return false;
    };
    return !this.isReply() 
        && !this.isDocumentNote() 
        && !this.isISC() 
        && !this.wasDeleted() 
        && !this.isAcceptedOrRejected();
  }

  this.justificationCollapsedState = true;

  this.isJustificationCollapsed = function () {
    return this.justificationCollapsedState;
  };

  this.toggleJustificationCollapsed = function () {
    this.justificationCollapsedState = !this.justificationCollapsedState;
  };

  this.justificationLabel = function () {
    if (this.editing()) {
      if (this.justificationText()) {
        return 'Justification';
     } else {
        return 'Add justification';
     }
    } else {
      if (this.isJustificationCollapsed()) {
        return 'Show justification';
      } else {
        return 'Hide justification';
      }
    }

  };

  this.annotationForwardedState = false;
  this.isAnnotationForwarded = function () {
    return this.annotation.forwarded;
  }

  this.forwardJustifText = function () {
    return this.state().forwardJustification ? this.state().forwardJustification : '';
  };

  this.setForwardJustifText = function (text: String) {
    drafts.update(this.annotation, {
      isPrivate: this.state().isPrivate,
      tags: this.state().tags,
      text: this.annotation.text, // take over the original annotation
      forwardJustification: text,
      justification: self.annotation.justification,
    });
  };

  this.user = function () {
    if ((!self.annotation.user) && (self.session().state.userid)) {
      self.annotation.user = self.session().state.userid;
      self.annotation.user_info = self.session().state.user_info;
    }
    return self.annotation.user;
  };
  /** Save this annotation if it's a new highlight.
   *
   * The highlight will be saved to the server if the user is logged in,
   * saved to drafts if they aren't.
   *
   * If the annotation is not new (it has already been saved to the server) or
   * is not a highlight then nothing will happen.
   *
   */
  function saveNewHighlight() {
    if (!isNew(self.annotation)) {
      // Already saved.
      return;
    }

    if (!self.isHighlight()) {
      // Not a highlight,
      return;
    }

    if (self.annotation.user) {
      // User is logged in, save to server.
      // Highlights are always private.
      self.annotation.permissions = permissions.private(self.annotation.user);
      save(self.annotation).then(function (model) {
        model.$tag = self.annotation.$tag;
        $rootScope.$broadcast(events.ANNOTATION_CREATED, model);
      });
    } else {
      // User isn't logged in, save to drafts.
      drafts.update(self.annotation, self.state());
    }
  }

  this.authorize = function (action) {
    return permissions.permits(
      self.annotation.permissions,
      action,
      session.state.userid
    );
  };

  /**
    * @ngdoc method
    * @name annotation.AnnotationController#flag
    * @description Flag the annotation.
    */
  this.flag = function () {
    if (!session.state.userid) {
      flash.error(
        'You must be logged in to report an annotation to the moderators.',
        'Login to flag annotations'
      );
      return;
    }

    var onRejected = function (err) {
      flash.error(err.message, 'Flagging annotation failed');
    };
    annotationMapper.flagAnnotation(self.annotation).then(function () {
      analytics.track(analytics.events.ANNOTATION_FLAGGED);
      store.updateFlagStatus(self.annotation.id, true);
    }, onRejected);
  };

  this.treat = function (_markAsTreated) {
    if(_markAsTreated) {
      var msg = 'Are you sure you want to mark as processed?';
      if ($window.confirm(msg)) {
        var onRejected = function (err) {
          flash.error(err.message, 'Marking annotation as processed failed');
        };
        annotationMapper.treatAnnotation(self.annotation).then(function () {
          analytics.track(analytics.events.ANNOTATION_DELETED);
        }, onRejected);
      } else {
	    self.markAsTreated = false;
      }
    } else {
	    var msg = 'Are you sure you want to mark as not processed?';
		if($window.confirm(msg)) {
			var onRejected = function (err) {
	          flash.error(err.message, 'Marking annotation as not processed failed');
	        };
	        annotationMapper.resetAnnotation(self.annotation).then(function () {
	          analytics.track(analytics.events.ANNOTATION_DELETED);
	        }, onRejected);
		} else {
			self.markAsTreated = true;
		}
    }
  }

  /**
    * @ngdoc method
    * @name annotation.AnnotationController#delete
    * @description Deletes the annotation.
    */
  this.delete = function () {
    return $timeout(function () {  // Don't use confirm inside the digest cycle.
      var msg = 'Are you sure you want to delete this annotation?';
      if ($window.confirm(msg)) {
        var onRejected = function (err) {
          flash.error(err.message, 'Deleting annotation failed');
        };
        $scope.$apply(function () {
          annotationMapper.deleteAnnotation(self.annotation).then(function () {
            var event;

            if (self.isReply()) {
              event = analytics.events.REPLY_DELETED;
            } else if (self.isHighlight()) {
              event = analytics.events.HIGHLIGHT_DELETED;
            } else if (isPageNote(self.annotation)) {
              event = analytics.events.PAGE_NOTE_DELETED;
            } else {
              event = analytics.events.ANNOTATION_DELETED;
            }

            analytics.track(event);

          }, onRejected);
        });
      }
    }, true);
  };

  /**
    * @ngdoc method
    * @name annotation.AnnotationController#edit
    * @description Switches the view to an editor.
    */
  this.edit = function () {

    if (!drafts.get(self.annotation)) {
      drafts.update(self.annotation, self.state());
    }
  };

  this.oldEditingState = false;
  /**
   * @ngdoc method
   * @name annotation.AnnotationController#editing.
   * @returns {boolean} `true` if this annotation is currently being edited
   *   (i.e. the annotation editor form should be open), `false` otherwise.
   */
  this.editing = function () : boolean {
    const isEditing = drafts.get(self.annotation) && !self.isSaving;
    if (this.oldEditingState !== isEditing) {
      this.onEditStateChanged({ editing: isEditing });
    }
    this.oldEditingState = isEditing;
    return isEditing;
  };


  /**
    * @ngdoc method
    * @name annotation.AnnotationController#group.
    * @returns {Object} The full group object associated with the annotation.
    */
  this.group = function () {
    return groups.get(self.annotation.group);
  };

  /**
    * @ngdoc method
    * @name annotation.AnnotaitonController#hasContent
    * @returns {boolean} `true` if this annotation has content, `false`
    *   otherwise.
    */
  this.hasContent = function () {
    return hasContent(self.state());
  };

  /**
    * Return the annotation's quote if it has one or `null` otherwise.
    */
  this.quote = function () {
    if (self.annotation.target.length === 0) {
      return null;
    }
    var target = self.annotation.target[0];
    if (!target.selector) {
      return null;
    }
    var quoteSel = target.selector.find(function (sel) {
      return sel.type === 'TextQuoteSelector';
    });
    return quoteSel ? quoteSel.exact : null;
  };

  this.id = function () {
    return self.annotation.id;
  };

  this.precedingText = function () {
    return self.annotation.precedingText;
  };

  this.succeedingText = function () {
    return self.annotation.succeedingText;
  };

  /**
    * @ngdoc method
    * @name annotation.AnnotationController#isHighlight.
    * @returns {boolean} true if the annotation is a highlight, false otherwise
    */
  this.isHighlight = function () {
    return newlyCreatedByHighlightButton || isHighlight(self.annotation);
  };
  this.isSent = function () {
    return annotationMetadata.isSent(self.annotation);
  };
  
  /**
    * @ngdoc method
    * @name annotation.AnnotationController#isShared
    * @returns {boolean} True if the annotation is shared (either with the
    * current group or with everyone).
    */
  this.isShared = function () {
    return !self.state().isPrivate;
  };

  // Save on Meta + Enter or Ctrl + Enter.
  this.onKeydown = function (event) {
    if (event.keyCode === 13 && (event.metaKey || event.ctrlKey)) {
      event.preventDefault();
      self.save();
    }
  };

  this.toggleCollapseBody = function (event) {
    event.stopPropagation();
    self.collapseBody = !self.collapseBody;
  };

  /**
    * @ngdoc method
    * @name annotation.AnnotationController#reply
    * @description
    * Creates a new message in reply to this annotation.
    */
  this.reply = function () {
    var references = (self.annotation.references || []).concat(self.annotation.id);
    var group = self.annotation.group;
    var replyPermissions;
    var userid = session.state.userid;
    if (userid) {
      replyPermissions = self.state().isPrivate ?
        permissions.private(userid) : permissions.shared(userid, group);
    }
    annotationMapper.createAnnotation({
      group: group,
      references: references,
      permissions: replyPermissions,
      target: [{ source: self.annotation.target[0].source }],
      uri: self.annotation.uri,
    });
  };
  
  this.forward = function () {

    var group = self.annotation.group;
    var forwardPermissions;
    var userid = session.state.userid;
    if (userid) {
      forwardPermissions = self.state().isPrivate ?
        permissions.private(userid) : permissions.shared(userid, group);
    }
    var forwardedAnnot;

    // if a reply is forwarded the root annotation is copied an then some necessary adjustmends are made
    if(self.isReply()) {
      var parentId : String = self.annotation.references[0];
      var parent = store.findAnnotationByID(parentId);
      forwardedAnnot = Object.assign({}, parent);
      forwardedAnnot.replyText = self.annotation.text;
      forwardedAnnot.isReply = true;
      forwardedAnnot.forwardJustification = "";
    }
    else {
      forwardedAnnot = Object.assign({}, self.annotation);
    }
    // clear the original tag to avoid interference with original annotation and
    // assign the next tag to be used; events will later pass by the nextTag and increase it
    forwardedAnnot.$tag = "t" + store.getState().nextTag; 
    forwardedAnnot.forwarded = true;
    forwardedAnnot.references = null;
    forwardedAnnot.id = null;
    var now = new Date();
    forwardedAnnot.created = now.toISOString();
    forwardedAnnot.updated = now.toISOString();
    forwardedAnnot.originGroup=self.annotation.group;
    // set the current user!
    forwardedAnnot.user_info = session.state.user_info;
    forwardedAnnot.permissions = forwardPermissions;
    forwardedAnnot.user = userid;

    annotationMapper.createAnnotation(forwardedAnnot);    
  };
  
  /**
    * @ngdoc method
    * @name annotation.AnnotationController#revert
    * @description Reverts an edit in progress and returns to the viewer.
    */
  this.revert = function () {
    drafts.remove(self.annotation);
    if (isNew(self.annotation)) {
      $rootScope.$broadcast(events.ANNOTATION_DELETED, self.annotation);
    }
  };

  // LEOS change
  /**
   * @returns Whether the text of this annotation can be submitted
   * (i.e. is not empty and for forwards, if a group to forward it to has been choosen).
   */
  this.isTextValid = function () {
    // ANOT-198
    return self.hasContent()
          && (!self.isForward() || self.annotation.group != self.annotation.originGroup) //if it is forwarded and a group to forward it to has been chosen
  }

  this.save = function () {
    if (!self.annotation.user) {
      flash.info('Please log in to save your annotations.');
      return Promise.resolve();
    }
    if (!self.isTextValid() && self.isShared()) { // LEOS change
      flash.info('Please add text or a tag before publishing.');
      return Promise.resolve();
    }

    var updatedModel = updateModel(self.annotation, self.state(), permissions);

    // Optimistically switch back to view mode and display the saving
    // indicator
    self.isSaving = true;

    var loggedUserId = this.session().state.userid;
    var loggedUserDisplayName = this.session().state.user_info.display_name;

    return save(updatedModel, loggedUserId, loggedUserDisplayName).then(function (model) {
      Object.assign(updatedModel, model);

      self.isSaving = false;

      var event = isNew(self.annotation) ?
        events.ANNOTATION_CREATED : events.ANNOTATION_UPDATED;
      drafts.remove(self.annotation);

      $rootScope.$broadcast(event, updatedModel);
    }).catch(function (err) {
      self.isSaving = false;
      self.edit();
      flash.error(err.message, 'Saving annotation failed');
    });
  };

  /**
    * @ngdoc method
    * @name annotation.AnnotationController#setPrivacy
    *
    * Set the privacy settings on the annotation to a predefined
    * level. The supported levels are 'private' which makes the annotation
    * visible only to its creator and 'shared' which makes the annotation
    * visible to everyone in the group.
    *
    * The changes take effect when the annotation is saved
    */
  this.setPrivacy = function (privacy) {
    // When the user changes the privacy level of an annotation they're
    // creating or editing, we cache that and use the same privacy level the
    // next time they create an annotation.
    // But _don't_ cache it when they change the privacy level of a reply.
    if (!isReply(self.annotation)) {
      permissions.setDefault(privacy);
    }
    drafts.update(self.annotation, {
      tags: self.state().tags,
      text: self.state().text,
      justification: self.state().justification,
      isPrivate: privacy === 'private',
      forwardJustification: self.state().forwardJustification
    });
  };

  this.tagSearchURL = function (tag) {
    if (this.isThirdPartyUser()) {
      return null;
    }
    return serviceUrl('search.tag', { tag: tag });
  };

  this.isOrphan = function () {
    if (typeof self.annotation.$orphan === 'undefined') {
      return self.annotation.$anchorTimeout;
    }
    return self.annotation.$orphan;
  };

  this.user = function () {
    return self.annotation.user;
  };

  this.isThirdPartyUser = function () {
    return isThirdPartyUser(self.annotation.user, settings.authDomain);
  };

  this.isDeleted = function () {
    return streamer.hasPendingDeletion(self.annotation.id);
  };

  this.isHiddenByModerator = function () {
    return self.annotation.hidden;
  };

  this.canFlag = function () {
    // Users can flag any annotations except their own.
    return session.state.userid !== self.annotation.user;
  };

  this.isFlagged = function () {
    return self.annotation.flagged;
  };

  this.isReply = function ():boolean {
    return isReply(self.annotation);
  };
  this.isForward = function ():boolean {
    return isForward(self.annotation);
  };
  this.incontextLink = function () {
    if (self.annotation.links) {
      return self.annotation.links.incontext ||
        self.annotation.links.html ||
        '';
    }
    return '';
  };

  /**
   * Sets whether or not the controls for expanding/collapsing the body of
   * lengthy annotations should be shown.
   */
  this.setBodyCollapsible = function (canCollapse) {
    if (canCollapse === self.canCollapseBody) {
      return;
    }
    self.canCollapseBody = canCollapse;

    // This event handler is called from outside the digest cycle, so
    // explicitly trigger a digest.
    $scope.$digest();
  };

  this.setText = function (text) {
    drafts.update(self.annotation, {
      isPrivate: self.state().isPrivate,
      tags: self.state().tags,
      text: text,
      justification: self.state().justification,
      forwardJustification: self.state().forwardJustification
    });
  };

  this.setTags = function (tags) {
    drafts.update(self.annotation, {
      isPrivate: self.state().isPrivate,
      tags: tags,
      text: self.state().text,
      justification: self.state().justification,
      forwardJustification: self.state().forwardJustification
    });
  };

  this.state = function () {
    var draft = drafts.get(self.annotation);
    if (draft) {
      return draft;
    }
    return {
      tags: self.annotation.tags,
      text: self.annotation.text,
      justification: self.annotation.justification,
      isPrivate: !permissions.isShared(self.annotation.permissions, self.annotation.user),
      precedingText: self.annotation.precedingText,
      succeedingText: self.annotation.succeedingText,
      forwardJustification: self.annotation.forwardJustification,
    };
  };

  this.session = function () {
    return session;
  };

  /**
   * Return true if the CC 0 license notice should be shown beneath the
   * annotation body.
   */
  this.shouldShowLicense = function () {
    if (!self.editing() || !self.isShared()) {
      return false;
    }
    return self.group().type !== 'private';
  };
}

export = {
  controller: AnnotationController,
  controllerAs: 'vm',
  bindings: {
    annotation: '<',
    showDocumentInfo: '<',
    isChildReplyInEditingState: '<',
    onReplyCountClick: '&',
    onEditStateChanged: '&',
    replyCount: '<',
    isCollapsed: '<',
    isRootSuggestion: '<',
  },
  template: require('../templates/annotation.html'),

  // Private helper exposed for use in unit tests.
  updateModel: updateModel,
};
