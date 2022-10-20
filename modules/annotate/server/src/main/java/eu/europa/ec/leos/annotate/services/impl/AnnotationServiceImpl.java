/*
 * Copyright 2018-2022 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.europa.ec.leos.annotate.services.impl;

import eu.europa.ec.leos.annotate.Authorities;
import eu.europa.ec.leos.annotate.model.AnnotationStatus;
import eu.europa.ec.leos.annotate.model.ResponseStatus;
import eu.europa.ec.leos.annotate.model.SimpleMetadata;
import eu.europa.ec.leos.annotate.model.UserInformation;
import eu.europa.ec.leos.annotate.model.entity.*;
import eu.europa.ec.leos.annotate.model.helper.*;
import eu.europa.ec.leos.annotate.model.web.annotation.JsonAnnotation;
import eu.europa.ec.leos.annotate.model.web.helper.JsonAnnotationHandler;
import eu.europa.ec.leos.annotate.repository.AnnotationRepository;
import eu.europa.ec.leos.annotate.services.*;
import eu.europa.ec.leos.annotate.services.exceptions.*;
import eu.europa.ec.leos.annotate.services.impl.util.TextShortener;
import org.hibernate.collection.internal.PersistentBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;

import java.rmi.activation.UnknownGroupException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service responsible for annotation administration functionality 
 */
@Service
public class AnnotationServiceImpl implements AnnotationService {

    private static final Logger LOG = LoggerFactory.getLogger(AnnotationServiceImpl.class);
    private static final String ERROR_USERINFO_MISSING = "Required user information missing.";
    private static final String ANNOTATION_NOT_FOUND = "Annotation not found";
    private static final int PRECEDING_AND_SUCCEEDING_MAX_LENGTH = 300;
    private static final int MAX_REPLIES_SUGGESTION_JUSTIFICATION = 1;

    // -------------------------------------
    // Required services and repositories
    // -------------------------------------
    // @Qualifier("annotationRepos")
    
    // note: do NOT declare final, otherwise mocking will have problems!
    private AnnotationRepository annotRepos;
    private AnnotationPermissionService annotPermService;
    private GroupService groupService;
    private UserGroupService userGroupService;
    private TagsService tagsService;
    private MetadataService metadataService;
    private MetadataMatchingService metadataMatchingService;
    private UUIDGeneratorService uuidService;
    private UserService userService;
    private DocumentService documentService;

    // -------------------------------------
    // Constructors & Setters
    // -------------------------------------

    @Autowired
    public AnnotationServiceImpl(final AnnotationRepository annotRepos, final AnnotationPermissionService annotPermService,
            final GroupService groupService, final UserGroupService userGroupService, final TagsService tagsService,
            final MetadataService metadataService,
            final MetadataMatchingService metadataMatchingService, final UUIDGeneratorService uuidService,
            final UserService userService, final DocumentService documentService) {
        this.annotRepos = annotRepos;
        this.annotPermService = annotPermService;
        this.groupService = groupService;
        this.userGroupService = userGroupService;
        this.tagsService = tagsService;
        this.metadataService = metadataService;
        this.metadataMatchingService = metadataMatchingService;
        this.uuidService = uuidService;
        this.userService = userService;
        this.documentService = documentService;
    }

    // -------------------------------------
    // Service functionality
    // -------------------------------------

    @Override
    public Annotation findById(final String annotId) {

        return annotRepos.findById(annotId).orElse(null);
    }

    @Override
    public Annotation findAnnotationById(final String annotId) {

        return annotRepos.findByIdAndStatus(annotId, AnnotationStatus.NORMAL);
    }

    @Override
    public Annotation findAnnotationById(final String annotId, final String userlogin, final String userContext) throws MissingPermissionException {

        Assert.isTrue(StringUtils.hasLength(annotId), "Cannot search for annotation, no annotation ID specified.");

        // retrieve the annotation first...
        final Annotation resultAnnotation = findAnnotationById(annotId);
        if (resultAnnotation == null) {
            LOG.error("The wanted annotation with id '{}' could not be found.", annotId);
            return null;
        }

        // ... then check the permissions
        // note: we did not combine the permission check directly with the database query here in order to be able
        // to easily distinguish between annotation being missing and annotation not being permitted to be viewed

        if (!annotPermService.hasUserPermissionToSeeAnnotation(resultAnnotation, userlogin, userContext)) {
            LOG.warn("User '{}' does not have permission to see annotation with id '{}'.", userlogin, annotId);
            throw new MissingPermissionException(userlogin);
        }

        return resultAnnotation;
    }

    @Override
    @Nonnull
    public List<Annotation> findSentDeletedByMetadataIdAndStatus(
            final List<Long> metadataIds, final List<AnnotationStatus> statuses) {

        return annotRepos.findByMetadataIdIsInAndStatusIsInAndSentDeletedIsTrue(
                metadataIds, statuses);
    }

    @Override
    @Nonnull
    public List<Annotation> findByMetadataAndStatus(final List<Long> metadataIds,
            final AnnotationStatus status) {

        return annotRepos.findByMetadataIdIsInAndStatus(metadataIds, status);
    }

    @Override
    @Nonnull
    public List<Annotation> findByMetadata(final List<Long> metadataIds) {

        return annotRepos.findByMetadataIdIsIn(metadataIds);
    }

    @Override
    public List<Annotation> saveAll(final List<Annotation> annots) {

        return (List<Annotation>) annotRepos.saveAll(annots);
    }

    @Override
    public Annotation createAnnotation(final JsonAnnotation webAnnot, final UserInformation userInfo) throws CannotCreateAnnotationException {

        if (userInfo == null) {
            throw new CannotCreateAnnotationException(new IllegalArgumentException("userInfo is null"));
        }
        LOG.debug("createAnnotation: authUser='{}'", userInfo.getLogin());

        final User registeredUser = userInfo.getUser();
        if (registeredUser == null) {
            // user was already created during client's initialisation (i.e. token exchange/profile retrieval)
            // so it should be known here unless an error had occurred before
            // therefore we don't do anything if user is still missing now
            throw new CannotCreateAnnotationException(new UserNotFoundException(userInfo.getLogin(), userInfo.getContext()));
        }

        // find belonging group in DB
        final Group group = groupService.findGroupByName(webAnnot.getGroup());
        if (group == null) {
            LOG.error("Cannot create annotation as associate group is unknown");
            throw new CannotCreateAnnotationException(new UnknownGroupException(webAnnot.getGroup()));
        }

        // if there is a connected entity set, try to retrieve the corresponding group
        final Group connectedEntityGroup = userGroupService.getConnectedEntityGroup(userInfo);

        // search if document is already contained in DB
        final Document document = documentService.findOrCreateDocument(webAnnot);

        final Metadata metadata = prepareMetadata(webAnnot, document, group, userInfo);

        // save the annotation with all required reference IDs
        Annotation annot = new Annotation();
        annot.setCreated(webAnnot.getCreated());
        annot.setMetadata(metadata);
        annot.setConnectedEntity(connectedEntityGroup);
        annot.setUser(registeredUser);
        annot.setId(uuidService.generateUrlSafeUUID()); // as a new annotation is saved, we set an ID, regardless whether one was present before!
        AnnotationReferencesHandler.setReferences(annot, webAnnot.getReferences()); // take over all referenced annotations (is 'null' for new top-level
        // annotations, filled for replies)
        annot.setShared(!JsonAnnotationHandler.isPrivateAnnotation(webAnnot));
        annot.setText(webAnnot.getText());
        annot.setTargetSelectors(JsonAnnotationHandler.getSerializedTargets(webAnnot));
        annot.setUpdated(LocalDateTime.now(ZoneOffset.UTC));
        annot.setLinkedAnnotationId(webAnnot.getLinkedAnnotationId());
        annot.setTags(TagBuilder.getTagList(webAnnot.getTags(), annot));
        if (webAnnot.getPrecedingText() != null) {
            final String truncatedText = TextShortener.getLastGivenNumberOfCharacters(
                    webAnnot.getPrecedingText(), PRECEDING_AND_SUCCEEDING_MAX_LENGTH);
            annot.setPrecedingText(truncatedText);
        }
        if (webAnnot.getSucceedingText() != null) {
            final String truncatedText = TextShortener.getFirstGivenNumberOfCharacters(
                    webAnnot.getSucceedingText(), PRECEDING_AND_SUCCEEDING_MAX_LENGTH);
            annot.setSucceedingText(truncatedText);
        }
        if (webAnnot.getJustification() != null) {
            annot.setJustificationText(webAnnot.getJustification().getText());
        }
        annot.setForwarded(webAnnot.isForwarded());
        if (webAnnot.getForwardJustification() != null) {
            annot.setForwardJustification(webAnnot.getForwardJustification());
        }
        if (webAnnot.getOriginGroup() != null) {
            if (webAnnot.getOriginGroup().equals(webAnnot.getGroup())) {
                throw new CannotCreateAnnotationException("Group equal to group of original annotation");
            }
            annot.setOriginGroup(groupService.findGroupByName(webAnnot.getOriginGroup()));
        }
        if (webAnnot.getReplyText() != null) {
            annot.setReplyText(webAnnot.getReplyText());
        }

        try {
            annot = annotRepos.save(annot);
        } catch (Exception e) {
            throw new CannotCreateAnnotationException(e);
        }

        return annot;
    }

    // prepare the metadata to be associated to an annotation
    // this can either be an existing metadata set, or a new one that is created
    @Nonnull
    private Metadata prepareMetadata(final JsonAnnotation webAnnot, final Document document, final Group group, final UserInformation userInfo)
            throws CannotCreateAnnotationException {

        // determine system ID
        String systemId = userInfo.getAuthority();
        Metadata receivedMetadata = null;
        if (JsonAnnotationHandler.hasMetadata(webAnnot)) {
            receivedMetadata = new Metadata();
            MetadataHandler.setKeyValuePropertyFromSimpleMetadata(receivedMetadata, webAnnot.getDocument().getMetadata());

            // if we received a system ID, we use it; otherwise, we propagate the system ID of the user
            // note: usually, they should be identical anyway!
            if (StringUtils.hasLength(receivedMetadata.getSystemId())) {
                systemId = receivedMetadata.getSystemId();
            } else {
                receivedMetadata.setSystemId(systemId);
            }
        }

        if (receivedMetadata != null && MetadataHandler.isResponseStatusSent(receivedMetadata)) {
            throw new CannotCreateAnnotationException("Cannot create new annotations having response status SENT already");
        }

        // search if there is already a metadata set for the group+document+systemId combination
        Metadata metadata;
        if (JsonAnnotationHandler.isReply(webAnnot)) {
            metadata = findOrCreateReplyMetadata(webAnnot, document, group, userInfo, receivedMetadata);

            // no reply, but a new annotation
        } else {

            metadata = metadataMatchingService.findExactMetadata(document, group, systemId, receivedMetadata);
            if (metadata == null) {

                // register the new metadata
                try {
                    metadata = new Metadata(document, group, systemId);
                    MetadataHandler.setKeyValuePropertyFromSimpleMetadata(metadata, webAnnot.getDocument().getMetadata());

                    metadata = metadataService.saveMetadata(metadata);
                } catch (CannotCreateMetadataException ccme) {
                    LOG.error("Metadata could not be persisted while creating annotation");
                    throw new CannotCreateAnnotationException(ccme);
                } catch (Exception e) {
                    LOG.error("Received unexpected exception when trying to persist metadata during creation of annotation", e);
                    throw new CannotCreateAnnotationException(e);
                }
            }
        }

        return metadata;
    }

    /**
     * finds existing metadata that will be associated to a reply; or creates appropriate new metadata
     *
     * @param webAnnot
     *        incoming annotation (JSON-based)
     * @param document
     *        associate document
     * @param group
     *        associate group to which the reply should be posted
     * @param userInfo
     *        information about user
     * @param receivedMetadata
     *        incoming metadata
     *
     * @return found or newly created {@link Metadata} object to be used for the reply
     *
     * @throws CannotCreateAnnotationException
     *         thrown when no parent annotation found, parent is SENT, parent is suggestion and reply not the first one in ISC context, or another error
     */
    private Metadata findOrCreateReplyMetadata(final JsonAnnotation webAnnot, final Document document,
            final Group group, final UserInformation userInfo,
            final Metadata receivedMetadata) throws CannotCreateAnnotationException {

        // for replies, the annotation does not contain metadata; we reuse the metadata of the thread's root
        final Annotation rootAnnot = findAnnotationById(JsonAnnotationHandler.getRootAnnotationId(webAnnot));
        if (rootAnnot == null) {
            throw new CannotCreateAnnotationException("No root annotation found for reply");
        }

        final String authority = userInfo.getAuthority();
        final boolean isIsc = Authorities.isIsc(authority);
        final boolean isReplyToSuggestion = AnnotationChecker.isSuggestion(rootAnnot);

        // outside of ISC, replying to SENT annotations is not allowed
        if (!isIsc &&
                !isReplyToSuggestion &&
                AnnotationChecker.isResponseStatusSent(rootAnnot)) {
            throw new CannotCreateAnnotationException(
                    "Replies on SENT annotations( no suggestions) in non-ISC (e.g. LEOS) context are not allowed");
        }
        if (isReplyToSuggestion) {
            checkSuggestionReplyMetadataPreconditions(webAnnot, rootAnnot, userInfo);
        }

        // if we are in ISC and the annotation is a suggestion and we reached this point, we must be
        // adding a justification, which can only be done by members of the same group;
        // in this case, we can simply reuse the metadata of the root
        if (isIsc && isReplyToSuggestion) {
            return rootAnnot.getMetadata();
        }

        // we always need to check if appropriate metadata is already available;
        // otherwise, we create a new set
        // the cases occurring here can be:
        // - in Edit, a user is replying to an annotation/suggestion of the same group -> use same metadata
        // - in Edit, a user is replying to an annotation/suggestion of another group -> use different metadata
        // - in Edit, a user is replying to an ISC annotation/suggestion -> use different metadata
        // - in ISC, a user is replying to an annotation of the same group -> use same metadata
        // - in ISC, a user is replying to an annotation of another group -> use different metadata
        //
        // note: the case that
        // - in ISC, somebody is replying to an Edit item is not possible (by workflow)
        // - in ISC, a user is replying a second time to a suggestion is not possible (checked above)
        Metadata metadata = metadataMatchingService.findExactMetadata(document, group,
                authority, receivedMetadata); // receivedMetadata is null for replies!
        if (metadata == null) {

            // register the new metadata
            try {
                metadata = new Metadata(document, group, authority);
                metadata = metadataService.saveMetadata(metadata);
            } catch (CannotCreateMetadataException ccme) {
                LOG.error("Metadata could not be persisted while creating annotation reply");
                throw new CannotCreateAnnotationException(ccme);
            } catch (Exception e) {
                LOG.error("Received unexpected exception when trying to persist metadata during creation of annotation reply", e);
                throw new CannotCreateAnnotationException(e);
            }
        }

        return metadata;
    }

    /**
     * for a suggestion, there may only be a certain number of replies; check all precoditions applying
     * to creating a reply for a suggestion
     *
     * @param webAnnot
     *        new reply to be created for a suggestion
     * @param rootAnnot
     *        the suggestion for which the reply is to be created
     * @param userInfo
     *        user requesting the creation of the reply
     * @throws CannotCreateAnnotationException
     *        this exception is thrown when any of the checks fails
     */
    private void checkSuggestionReplyMetadataPreconditions(
            final JsonAnnotation webAnnot,
            final Annotation rootAnnot,
            final UserInformation userInfo)
            throws CannotCreateAnnotationException {

        final List<Group> groupsOfUser = userGroupService.getGroupsOfUser(userInfo.getUser());
        if (!groupsOfUser.contains(rootAnnot.getGroup())) {
            throw new CannotCreateAnnotationException("A reply to a suggestion from a foreign group is not allowed.");
        }

        final boolean isIsc = Authorities.isIsc(userInfo.getAuthority());
        if (isIsc && webAnnot.getReferences().size() > MAX_REPLIES_SUGGESTION_JUSTIFICATION) {
            throw new CannotCreateAnnotationException("A reply to a reply on a suggestion is not allowed.");
        }
    }

    @SuppressWarnings({"PMD.PrematureDeclaration"})
    @Override
    public Annotation updateAnnotation(final String annotationId, final JsonAnnotation webAnnot,
            final UserInformation userInfo)
            throws CannotUpdateAnnotationException, CannotUpdateSentAnnotationException, MissingPermissionException {

        Assert.isTrue(StringUtils.hasLength(annotationId), "Required annotation ID missing.");

        if (userInfo == null) {
            throw new CannotUpdateAnnotationException(new IllegalArgumentException("userInfo is null"));
        }

        final Annotation ann = findAnnotationById(annotationId);
        if (ann == null) {
            throw new CannotUpdateAnnotationException("Annotation not found");
        }

        // check if the annotation is final already and may be updated by group members (e.g. when having ResponseStatus SENT),
        // or whether annotation was created by the user
        if (!annotPermService.hasUserPermissionToUpdateAnnotation(ann, userInfo)) {
            if (AnnotationChecker.isResponseStatusSent(ann)) {
                LOG.warn("Annotation with id '{}' is final (SENT) and cannot be updated.", annotationId);
                throw new CannotUpdateSentAnnotationException(
                        String.format("Annotation with id '%s' is final (responseStatus=SENT) and cannot be updated.", annotationId));
            } else {
                LOG.warn("User '{}' does not have permission to update annotation with id '{}'.", userInfo.getLogin(), annotationId);
                throw new MissingPermissionException(userInfo.getLogin());
            }
        }

        if (AnnotationChecker.isResponseStatusSent(ann)) {
            // permission check above made sure that only ISC users of the same group as the annotation may update the annotation
            // now we create a new annotation, link it to the original, and return it
            return updateSentAnnotation(ann, webAnnot, userInfo);
        }

        if (userInfo.getAuthority().equals(Authorities.ISC)) {
            ann.setUser(userInfo.getUser());
        }
        return updateNormalAnnotation(ann, webAnnot, userInfo);
    }

    /**
     * normal update procedure for an annotation
     * 
     * @param ann 
     *        database annotation to be updated 
     * @param webAnnot
     *        the incoming annotation
     * @param userInfo
     *        information about the user wanting to update an annotation
     * @return updated annotation
     * @throws CannotUpdateAnnotationException
     */
    private Annotation updateNormalAnnotation(
            final Annotation ann, final JsonAnnotation webAnnot, final UserInformation userInfo)
            throws CannotUpdateAnnotationException {

        // normal update

        // only the following properties of the annotation can be updated:
        // - text
        // - shared
        // - updated (current timestamp)
        // - tags
        // - connectedEntity
        // - justification text
        // - forwarding justification text
        ann.setText(webAnnot.getText());
        ann.setShared(!JsonAnnotationHandler.isPrivateAnnotation(webAnnot));
        ann.setUpdated(LocalDateTime.now(ZoneOffset.UTC));

        // update the connected entity
        // note: if there was an entry set already, but now we cannot resolve
        // the current connected entity, then the information is lost (last one wins)
        ann.setConnectedEntity(userGroupService.getConnectedEntityGroup(userInfo));

        @SuppressWarnings("PMD.PrematureDeclaration")
        final long originalMetadataId = ann.getMetadata().getId();
        updateTags(webAnnot, ann);
        updateGroup(webAnnot, ann, userInfo.getAuthority());
        updateJustificationText(webAnnot, ann);
        ann.setForwardJustification(webAnnot.getForwardJustification());

        try {
            annotRepos.save(ann);
        } catch (Exception e) {
            throw new CannotUpdateAnnotationException(e);
        }

        // if new metadata was assigned and the original metadata set is no longer referenced by any annotation, we can remove it
        // note: retrieving the ID of the associate metadata object is more reliable than asking for metadataId of annotation object!
        if (ann.getMetadata().getId() != originalMetadataId &&
                annotRepos.countByMetadataId(originalMetadataId) == 0) {
            metadataService.deleteMetadataById(originalMetadataId);
        }

        return ann;
    }

    /**
     * update procedure for ISC annotations having response status SENT
     * create a new annotation, link it to the original (and vice versa), set response status IN_PREPARATION
     * 
     * @param annot 
     *        original annotation, will only receive a "linked annotation id" entry
     * @param webAnnot 
     *        original annotation, will be used as blueprint for the new annotation
     * @param userInfo 
     *        information about the user updating the annotation
     * 
     * @return a new annotation, linked to the original
     * 
     * @throws CannotUpdateSentAnnotationException 
     */
    private Annotation updateSentAnnotation(final Annotation annot, final JsonAnnotation webAnnot, final UserInformation userInfo)
            throws CannotUpdateSentAnnotationException {

        final JsonAnnotation newWebAnnot = new JsonAnnotation(webAnnot);

        // remove the ID in order to have a NEW annotation saved
        newWebAnnot.setId(null);

        // note: we do not update the "created" timestamp
        newWebAnnot.setUpdated(LocalDateTime.now(ZoneOffset.UTC));

        // assign a different response status: IN_PREPARATION
        final SimpleMetadata meta = newWebAnnot.getDocument().getMetadata();
        meta.put(Metadata.PROP_RESPONSE_STATUS, ResponseStatus.IN_PREPARATION.toString());

        // link the annotation to the original annotation
        newWebAnnot.setLinkedAnnotationId(annot.getId());

        // now let the new annotation be saved
        Annotation newAnnotation;
        try {
            newAnnotation = createAnnotation(newWebAnnot, userInfo);
        } catch (CannotCreateAnnotationException ccae) {
            LOG.error("Error upon updating SENT annotation (creating and linking to new one)");
            final CannotUpdateSentAnnotationException cusae = new CannotUpdateSentAnnotationException("Error updating annotation");
            cusae.initCause(ccae);
            throw cusae;
        }

        // assign the ID of the new annotation to the original
        annot.setLinkedAnnotationId(newAnnotation.getId());
        annotRepos.save(annot);

        return newAnnotation;
    }

    /**
    * update the tags associated to an annotation
    * 
    * @param webAnnot 
    *        incoming annotation containing updated tags
    * @param annot 
    *        database annotation, tags may have to be updated
    */
    private void updateTags(final JsonAnnotation webAnnot, final Annotation annot) {

        // update the tags - due to hibernate mapping involved, we need to be more careful with this
        final boolean oldAnnotHasTags = annot.getTags() != null && !annot.getTags().isEmpty();
        final boolean newAnnotHasTags = webAnnot.getTags() != null && !webAnnot.getTags().isEmpty();

        if (oldAnnotHasTags && newAnnotHasTags) {

            // there were tags before and now, so we have to check more closely; comparing the total number of tags is not sufficient!
            updateTagsByComparingOldAndNew(webAnnot, annot);

            // keep simple cases simple
        } else if (!oldAnnotHasTags && newAnnotHasTags) {

            // store all new tags
            annot.setTags(TagBuilder.getTagList(webAnnot.getTags(), annot));

        } else if (oldAnnotHasTags && !newAnnotHasTags) {

            // remove all existing tags
            tagsService.removeTags(annot.getTags());
            annot.setTags(null);
        }
        // last case (= !oldAnnotHasTags && !newAnnotHasTags): nothing to do
    }

    /**
     * most complicated tag update case: old and new annotations already have tags
     * moved out to reduce complexity
     * 
     * @param webAnnot 
     *        incoming annotation containing updated tags
     * @param annot 
     *        database annotation, tags have to be updated
     */
    private void updateTagsByComparingOldAndNew(final JsonAnnotation webAnnot, final Annotation annot) {

        // idea: check which ones to remove, which ones to add

        // retrieve those present in old annotation, but not contained in new annotation
        final List<Tag> tagsToRemove = annot.getTags().stream()
                .filter(tag -> !webAnnot.getTags().contains(tag.getName()))
                .collect(Collectors.toList());

        // retrieve those present in new annotation, but not contained in old annotation
        final List<String> tagsToAdd = webAnnot.getTags().stream()
                .filter(tagString -> !TagListChecker.hasTag(annot.getTags(), tagString))
                .collect(Collectors.toList());

        if (!tagsToRemove.isEmpty()) {
            annot.getTags().removeAll(tagsToRemove);

            // note: we also need to remove the tags from the internally stored list of items cached by hibernate
            // alternative could be two first remove all items, save, add new items, save again - could be overhead
            try {
                final PersistentBag persBag = (PersistentBag) annot.getTags();
                if (persBag.getStoredSnapshot() instanceof ArrayList) {
                    @SuppressWarnings("unchecked")
                    final List<Tag> snapshotList = (List<Tag>) (persBag.getStoredSnapshot());
                    snapshotList.removeAll(tagsToRemove);
                }
            } catch (Exception e) {
                LOG.error("Error removing cleaned tags: ", e);
            }

            // remove from database
            tagsService.removeTags(tagsToRemove);
        }

        // add new items
        if (!tagsToAdd.isEmpty()) {
            annot.getTags().addAll(TagBuilder.getTagList(tagsToAdd, annot));
        }
    }

    /**
     * update the justification properties associated with an annotation
     * 
     * @param webAnnot 
     *        incoming annotation containing updated justification data; note: if no justification is given,
     *        the appropriate object is {@literal null}
     * @param annot 
     *        database annotation, justification may have to be updated
     */
    private void updateJustificationText(final JsonAnnotation webAnnot, final Annotation annot) {

        if (webAnnot.getJustification() == null) {
            annot.setJustificationText(null);
            return;
        }

        // empty string?
        final String webJust = webAnnot.getJustification().getText();
        if (!StringUtils.hasLength(webJust)) {
            annot.setJustificationText(null);
            return;
        }

        // justification is filled, copy it
        annot.setJustificationText(webJust);
    }

    /**
     * update the group associated to an annotation, if necessary
     * technically, this means assigning a different metadata set
     * 
     * @param webAnnot 
     *        incoming annotation possibly containing a new group
     * @param annot 
     *        database annotation whose group may have to be updated
     * @param systemId 
     *        the authority of the user requesting the update
     */
    private void updateGroup(final JsonAnnotation webAnnot, final Annotation annot, final String systemId) throws CannotUpdateAnnotationException {

        if (webAnnot.getGroup().equals(annot.getGroup().getName())) {
            LOG.trace("No need to update annotation's group, new and old are identical");
            return;
        }

        final Group newGroup = groupService.findGroupByName(webAnnot.getGroup());

        // search if there is already a metadata set for the group+document+systemId combination
        Metadata metadata = metadataMatchingService.findExactMetadata(annot.getDocument(), newGroup, systemId, annot.getMetadata());
        if (metadata == null) {

            // register the new metadata
            try {
                metadata = new Metadata(annot.getDocument(), newGroup, systemId);
                MetadataHandler.setKeyValuePropertyFromSimpleMetadata(metadata, webAnnot.getDocument().getMetadata());

                metadata = metadataService.saveMetadata(metadata);
            } catch (CannotCreateMetadataException ccme) {
                LOG.error("Metadata could not be persisted while creating annotation");
                throw new CannotUpdateAnnotationException(ccme);
            } catch (Exception e) {
                LOG.error("Received unexpected exception when trying to persist metadata during creation of annotation", e);
                throw new CannotUpdateAnnotationException(e);
            }
        }
        annot.setMetadata(metadata);
    }

    @Override
    public void resetAnnotationById(final String annotationId, final UserInformation userInfo)
            throws CannotResetAnnotationException {

        if (!StringUtils.hasLength(annotationId)) throw new CannotResetAnnotationException("Required suggestion/annotation ID missing.");
        if (userInfo == null) throw new CannotResetAnnotationException(ERROR_USERINFO_MISSING);

        final Annotation ann = findById(annotationId);
        if (ann == null) {
            throw new CannotResetAnnotationException(ANNOTATION_NOT_FOUND);
        }

        if (!Authorities.isLeos(userInfo.getAuthority())) {
            final String errorMsg = "If not in Leos, it is not possible to reset annotation status";
            LOG.warn(errorMsg);
            throw new CannotResetAnnotationException(errorMsg);
        }

        final User user = userInfo.getUser();
        try {
            updateAnnotationStatus(ann, AnnotationStatus.NORMAL, user.getId());
        } catch (CannotUpdateAnnotationException cuae) {
            // wrap into more specific exception
            throw new CannotResetAnnotationException(cuae);
        }
    }

    @Override
    public List<String> resetAnnotationsById(final List<String> annotationIds, final UserInformation userInfo)
            throws CannotResetAnnotationException {

        if (userInfo == null) throw new CannotResetAnnotationException(ERROR_USERINFO_MISSING);

        final List<String> resetList = new ArrayList<>();
        if (annotationIds == null || annotationIds.isEmpty()) {
            LOG.warn("No annotations for bulk reset received.");
            return resetList;
        }

        final List<String> errors = new ArrayList<>();

        // simply call the method to reset a single annotation and keep track of success and errors
        for (final String annotationId : annotationIds) {
            try {
                resetAnnotationById(annotationId, userInfo);
                resetList.add(annotationId);
            } catch (RuntimeException e) {
                LOG.warn("Error while resetting one of several annotations", e);
                throw e;
            } catch (Exception e) {
                errors.add(annotationId);
            }
        }
        LOG.info("Annotation bulk reset: {} annotations got reset successfully, {} errors", resetList.size(), errors.size());

        return resetList;
    }

    @Override
    public void treatAnnotationById(final String annotationId, final UserInformation userInfo)
            throws CannotTreatAnnotationException {

        if (!StringUtils.hasLength(annotationId)) throw new CannotTreatAnnotationException("Required suggestion/annotation ID missing.");
        if (userInfo == null) throw new CannotTreatAnnotationException(ERROR_USERINFO_MISSING);

        final Annotation ann = findById(annotationId);
        if (ann == null) {
            throw new CannotTreatAnnotationException(ANNOTATION_NOT_FOUND);
        }

        if (!Authorities.isLeos(userInfo.getAuthority())) {
            final String errorMsg = "If not in Leos, it is not possible to mark as treated";
            LOG.warn(errorMsg);
            throw new CannotTreatAnnotationException(errorMsg);
        }

        final User user = userInfo.getUser();
        try {
            updateAnnotationStatus(ann, AnnotationStatus.TREATED, user.getId());
        } catch (CannotUpdateAnnotationException cuae) {
            // wrap into more specific exception
            throw new CannotTreatAnnotationException(cuae);
        }
    }

    @Override
    public List<String> treatAnnotationsById(final List<String> annotationIds, final UserInformation userInfo)
            throws CannotTreatAnnotationException {

        if (userInfo == null) throw new CannotTreatAnnotationException(ERROR_USERINFO_MISSING);

        final List<String> treated = new ArrayList<>();
        if (annotationIds == null || annotationIds.isEmpty()) {
            LOG.warn("No annotations for bulk treatment received.");
            return treated;
        }

        final List<String> errors = new ArrayList<>();

        // simply call the method for treating a single annotation and keep track of success and errors
        for (final String annotationId : annotationIds) {
            try {
                treatAnnotationById(annotationId, userInfo);
                treated.add(annotationId);
            } catch (RuntimeException e) {
                LOG.warn("Error while treating one of several annotations", e);
                throw e;
            } catch (Exception e) {
                errors.add(annotationId);
            }
        }
        LOG.info("Annotation bulk treatment: {} annotations treated successfully, {} errors", treated.size(), errors.size());

        return treated;
    }

    @Override
    public void deleteAnnotationById(final String annotationId, final UserInformation userInfo)
            throws CannotDeleteAnnotationException, CannotDeleteSentAnnotationException {

        Assert.isTrue(StringUtils.hasLength(annotationId), "Required annotation ID missing.");
        Assert.notNull(userInfo, ERROR_USERINFO_MISSING);

        final Annotation ann = findAnnotationById(annotationId);
        if (ann == null) {
            throw new CannotDeleteAnnotationException("Annotation not found");
        }

        if (AnnotationChecker.isResponseStatusSent(ann)) {

            final Annotation rootAnnot = findAnnotationById(ann.getRootAnnotationId());
            if (Authorities.isIsc(userInfo.getAuthority()) ||
                    rootAnnot != null && AnnotationChecker.isSuggestion(rootAnnot)) {

                // check if user is group member (OK) or not (NOK)
                if (userGroupService.isUserMemberOfGroup(userInfo.getUser(), ann.getGroup())) {
                    LOG.debug("Annotation '{}' has response status SENT and will be sentDeleted now", ann.getId());
                    ann.setSentDeleted(true); // note: we only set the flag, NOT the DELETED status!
                    ann.setRespVersionSentDeleted(metadataMatchingService.getHighestResponseVersion(ann));
                    annotRepos.save(ann);

                    // make sure there is at least a dummy Metadata item in the DB for this response version
                    metadataMatchingService.findOrCreateInPrepItemForAnnotToDelete(ann);
                    return;
                } else {
                    LOG.info("Annotation '{}' has response status SENT and thus cannot be deleted by user of other group", ann.getId());
                    throw new CannotDeleteSentAnnotationException("Annotation has response status SENT, cannot be deleted from other group");
                }
            }
        }

        final User user = userService.findByLoginAndContext(userInfo.getLogin(), userInfo.getContext());
        softDeleteAnnotation(ann, user.getId());
    }

    @Override
    public List<String> deleteAnnotationsById(final List<String> annotationIds, final UserInformation userInfo) {

        Assert.notNull(userInfo, ERROR_USERINFO_MISSING);

        final List<String> deleted = new ArrayList<>();
        if (annotationIds == null || annotationIds.isEmpty()) {
            LOG.warn("No annotations for bulk deletion received.");
            return deleted;
        }

        final List<String> errors = new ArrayList<>();

        // simply call the method for deleting a single annotation and keep track of success and errors
        for (final String annotationId : annotationIds) {
            try {
                deleteAnnotationById(annotationId, userInfo);
                deleted.add(annotationId);
            } catch (RuntimeException e) {
                LOG.warn("Error while deleting one of several annotations", e);
                throw e;
            } catch (Exception e) {
                errors.add(annotationId);
            }
        }
        LOG.info("Annotation bulk deletion: {} annotations deleted successfully, {} errors", deleted.size(), errors.size());

        return deleted;
    }

    @Override
    public void softDeleteAnnotation(
            final Annotation annot,
            final long userId,
            final Long groupId) throws CannotDeleteAnnotationException {

        /*
         * note:
         * due to the database schema construction, there is a foreign key constraint on the 'root' column;
         * this makes all replies be deleted in case the thread's root annotation is deleted;
         * however, with our soft delete, the "DELETED" information is not automatically propagated
         *
         * to propagate the information, a trigger would be a good option, but this fails as the trigger would
         *  change other data of the table and thus again activate the trigger (ORA-04091)
         * 
         * so we have to do it ourselves: if a thread root is deleted, we delete all children;
         *  if it is not the root, only the particular annotation is removed, but NO child
         */

        try {
            updateAnnotationStatus(annot, AnnotationStatus.DELETED, userId, groupId);
        } catch (CannotUpdateAnnotationException cuae) {
            // wrap into more specific exception
            throw new CannotDeleteAnnotationException(cuae);
        }
    }

    @Override
    public void softDeleteAnnotation(
            final Annotation annot,
            final long userId) throws CannotDeleteAnnotationException {

        softDeleteAnnotation(annot, userId, null);
    }

    @Override
    public void updateAnnotationStatus(
            final Annotation annot,
            final AnnotationStatus newStatus,
            final long userId) throws CannotUpdateAnnotationException {

        updateAnnotationStatus(annot, newStatus, userId, null);
    }

    @Override
    public void updateAnnotationStatus(
            final Annotation annot,
            final AnnotationStatus newStatus,
            final long userId,
            final Long groupId) throws CannotUpdateAnnotationException {

        saveAnnotationStatus(annot, newStatus, userId, groupId);

        final boolean isThreadRoot = !StringUtils.hasLength(annot.getRootAnnotationId());

        if (isThreadRoot) {
            // get all children - fortunately, they all have the same root, no matter how many layers might be between the root and a successor
            // note: we only adapt those items still being in "NORMAL" state (since e.g. we don't want to change an already DELETED item to ACCEPTED)
            final List<Annotation> children = annotRepos.findByRootAnnotationId(annot.getId());
            for (final Annotation child : children) {
                saveAnnotationStatus(child, newStatus, userId, groupId);
            }
        }
    }

    @Override
    public void makeShared(final List<Annotation> items) {

        if (CollectionUtils.isEmpty(items)) {
            LOG.debug("No annotations received for making them shared");
            return;
        }

        items.stream().forEach(annot -> annot.setShared(true));
        annotRepos.saveAll(items);
    }

    @Override
    public void saveWithUpdatedTimestamp(final List<Annotation> annots, final LocalDateTime timestamp) {

        if (CollectionUtils.isEmpty(annots)) {
            LOG.debug("No annotations received for updating the 'updated' property");
            return;
        }

        annots.stream().forEach(annot -> annot.setUpdated(timestamp));
        annotRepos.saveAll(annots);
    }

    private void saveAnnotationStatus(final Annotation annot, final AnnotationStatus newStatus, final long userId,
            final Long groupId) throws CannotUpdateAnnotationException {

        if (!isStatusUpdateAllowed(annot, newStatus)) {
            final String errorMsg = "Updating status for annotation with ID '" + annot.getId() + "' from '" + annot.getStatus() + "' to '" + newStatus +
                    "' is not allowed";
            LOG.error(errorMsg);
            return;
        }

        try {
            annot.setStatus(newStatus);
            annot.setStatusUpdated(LocalDateTime.now(ZoneOffset.UTC));
            annot.setStatusUpdatedByUser(userId);
            if (groupId != null) annot.setStatusUpdatedByGroup(groupId);
            annotRepos.save(annot);
        } catch (Exception e) {
            LOG.error("Error updating annotation status for annotation with ID '" + annot.getId() + "' to status '" + newStatus + '"', e);
            throw new CannotUpdateAnnotationException(e);
        }
    }

    private boolean isStatusUpdateAllowed(final Annotation annot, final AnnotationStatus newStatus) {
        boolean isAllowed = false;

        switch (annot.getStatus()) {
            case NORMAL:
                // if NORMAL, it is possible to set any status
                isAllowed = true;
                break;

            case DELETED:
                // if already DELETED, it is not possible to update
                isAllowed = false;
                break;

            case TREATED:
                // if already TREATED, it is possible set only NORMAL
                if (AnnotationStatus.NORMAL.equals(newStatus)) isAllowed = true;
                break;

            case ACCEPTED:
                // if already ACCEPTED, it is possible set only REJECTED
                if (AnnotationStatus.REJECTED.equals(newStatus)) isAllowed = true;
                break;

            case REJECTED:
                // if already REJECTED, it is possible set only ACCEPTED
                if (AnnotationStatus.ACCEPTED.equals(newStatus)) isAllowed = true;
                break;

            default:
                break;
        }

        return isAllowed;
    }
}