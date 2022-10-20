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
package eu.europa.ec.leos.annotate.model.entity;

import eu.europa.ec.leos.annotate.Generated;
import eu.europa.ec.leos.annotate.model.AnnotationStatus;

import javax.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *  Class corresponding to the main annotation metadata
 */
@SuppressWarnings("PMD.GodClass")
@Entity
@Table(name = "ANNOTATIONS", indexes = {
        @Index(columnList = "USER_ID", name = "ANNOTATIONS_IX_USERS"),
        @Index(columnList = "METADATA_ID", name = "ANNOTATIONS_IX_METADATA"),
        @Index(columnList = "STATUS, ROOT", name = "ANNOTATIONS_IX_STATUS_ROOT")})
public class Annotation {

    // -------------------------------------
    // collection of constants denoting the various types of annotations supported
    // -------------------------------------

    public static final String ANNOTATION_COMMENT = "comment";
    public static final String ANNOTATION_SUGGESTION = "suggestion";
    public static final String ANNOTATION_PAGENOTE = ""; // note: no tag is written for a page note
    public static final String ANNOTATION_HIGHLIGHT = "highlight";

    // -------------------------------------
    // column definitions
    // -------------------------------------

    /**
     * URL-safe UUID
     */
    @Id
    @Column(name = "ANNOTATION_ID", unique = true, nullable = false)
    @SuppressWarnings("PMD.ShortVariable")
    private String id;

    /**
     * ID of a linked annotation
     */
    @Column(name = "LINKED_ANNOT_ID", nullable = true)
    private String linkedAnnotationId;
    
    /**
     * the text of the annotation
     */
    @Column(name = "TEXT")
    private String text;

    /**
     * the text of the justification (for suggestions only)
     */
    @Column(name = "JUSTIFICATION_TEXT", nullable = true)
    private String justificationText;
    
    @Column(name = "CREATED", nullable = false)
    private LocalDateTime created;

    @Column(name = "UPDATED", nullable = false)
    private LocalDateTime updated;

    /**
     * flag indicating whether the annotation can be seen by the originating only (false)
     * or whether it is public in the user's group (true)
     */
    @Column(name = "SHARED", nullable = false)
    private boolean shared;

    /**
     * selectors to the target location that is annotated
     * treated as a black box string
     */
    @Column(name = "TARGET_SELECTORS", nullable = false)
    private String targetSelectors;

    /**
     * comma-separated list of strings (annotation IDs representing parent hierarchy in case of replies to annotations)
     */
    @Column(name = "REFERENCES")
    private String references;

    /**
     * computed database column, contains the first annotation id stored in the 'references' list, if any
     */
    @Column(name = "ROOT", insertable = false, updatable = false)
    private String rootAnnotationId;

    /**
     * user ID column, filled by hibernate
     */
    @Column(name = "USER_ID", insertable = false, updatable = false, nullable = false)
    private long userId;

    /**
     * associate user, mapped by hibernate using USERS.USER_ID column
     */
    @OneToOne
    @JoinColumn(name = "USER_ID")
    private User user;

    /**
     * flag indicating whether the annotation represents a forwarded annotation
     * (note: the forwarded justification text is not sufficient for this as it's optional)
     */
    @Column(name = "IS_FORWARDED", nullable = false)
    private boolean isAnnotForwarded;
    
    /**
     * justification for forwarding an annotation
     */
    @Column(name = "FORWARD_JUSTIFICATION", nullable = true)
    private String forwardJustification;

    /**
    * group id from which the forwarded annotation originates
    */
    @Column(name = "ORIGIN_GROUP_ID",insertable=false, updatable=false, nullable = true)
    private Long originGroupID;

    @OneToOne
    @JoinColumn(name = "ORIGIN_GROUP_ID")
    private Group originGroup;

    /**
     * text of a forwarded reply
     */
    @Column(name="REPLY_TEXT", nullable = true)
    private String replyText;

    /** 
     * associate tags, mapped bidirectional by hibernate using Tag.annotation property
     * notes:
     * - without the "mappedBy", hibernate would require an intermediate mapping table 
     * - with lazy loading, there are LazyInitializationExceptions; but as we require the tags anyway, using eager loading is ok 
     */
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "annotation", fetch = FetchType.EAGER)
    private List<Tag> tags = new ArrayList<>();

    /**
     * metadata ID column, filled by hibernate
     */
    @Column(name = "METADATA_ID", insertable = false, updatable = false, nullable = false)
    private long metadataId;

    /**
     * associate metadata, mapped by hibernate using METADATA.ID column
     */
    @OneToOne
    @JoinColumn(name = "METADATA_ID")
    private Metadata metadata;

    /**
     * ID of the connected entity (= group) used while creating/updating the annotation
     */
    @Column(name = "CONNECTED_ENTITY_ID", insertable = false, updatable = false)
    private Long connectedEntityId; // Long instead of long -> is nullable
    
    /**
     * associate group, mapped by hibernate using GROUPS.ID column
     */
    @OneToOne
    @JoinColumn(name = "CONNECTED_ENTITY_ID")
    private Group connectedEntity;
    
    /**
     * status column, denotes whether the annotation is existing/deleted/accepted/rejected
     * see the {@link AnnotationStatus} for possible states
     */
    @Column(name = "STATUS")
    @Enumerated(EnumType.ORDINAL)
    private AnnotationStatus status = AnnotationStatus.NORMAL;

    /**
     * track the datetime of modification of the status
     * note: NOT auto-filled by DB trigger as this would require different implementations for Oracle and H2
     */
    @Column(name = "STATUS_UPDATED", nullable = true)
    private LocalDateTime statusUpdated;

    /**
     * track who modified the status (user id, but we don't create a foreign key as it might slow down things unintentionally)
     */
    @Column(name = "STATUS_UPDATED_BY_USER", nullable = true)
    private Long statusUpdatedByUser;
    
    /**
     * track by which group the status was modified (group id, but we don't create a foreign key as it might slow down things)
     */
    @Column(name = "STATUS_UPDATED_BY_GROUP", nullable = true)
    private Long statusUpdatedByGroup;
    
    /**
     * flag indicating whether the annotation was prepared for deletion - maybe define a "flags" field in future if there are more?
     */
    @Column(name = "SENT_DELETED", nullable = false)
    private boolean sentDeleted;

    /**
     * the response version with which the annotation was sentDeleted
     */
    @Column(name = "RESP_VERSION_SENT_DELETED", nullable = true)
    private long respVersionSentDeleted;

    /**
     * Text before the annotation
     */
    @Column(name = "PRECEDING_TEXT", nullable = true)
    private String precedingText;

    /**
     * Text after the annotation
     */
    @Column(name = "SUCCEEDING_TEXT", nullable = true)
    private String succeedingText;

    // -------------------------------------
    // constructor
    // -------------------------------------

    @SuppressWarnings("PMD.UnnecessaryConstructor")
    public Annotation() {
        // default constructor required by JPA
    }

    // -------------------------------------
    // Getters & setters
    // -------------------------------------

    @Generated
    public String getId() {
        return id;
    }

    @Generated
    public void setId(final String newId) {
        this.id = newId;
    }

    @Generated
    public String getLinkedAnnotationId() {
        return linkedAnnotationId;
    }
    
    @Generated
    public void setLinkedAnnotationId(final String newId) {
        this.linkedAnnotationId = newId;
    }
    
    @Generated
    public String getText() {
        return text;
    }

    @Generated
    public void setText(final String text) {
        this.text = text;
    }

    @Generated
    public String getJustificationText() {
        return justificationText;
    }

    @Generated
    public void setJustificationText(final String text) {
        this.justificationText = text;
    }
    
    @Generated
    public LocalDateTime getCreated() {
        return created;
    }

    @Generated
    public void setCreated(final LocalDateTime created) {
        this.created = created;
    }

    @Generated
    public LocalDateTime getUpdated() {
        return updated;
    }

    @Generated
    public void setUpdated(final LocalDateTime updated) {
        this.updated = updated;
    }

    @Generated
    public boolean isShared() {
        return shared;
    }

    @Generated
    public void setShared(final boolean shared) {
        this.shared = shared;
    }

    @Generated
    public String getTargetSelectors() {
        return targetSelectors;
    }

    @Generated
    public void setTargetSelectors(final String targetSelectors) {
        this.targetSelectors = targetSelectors;
    }

    @Generated
    public long getUserId() {
        return userId;
    }

    @Generated
    public void setUserId(final long userId) {
        this.userId = userId;
    }

    // shortcut to access the related document
    @Transient
    public Document getDocument() {
        if (this.metadata == null) {
            return null;
        }
        return this.metadata.getDocument();
    }

    @Generated
    public User getUser() {
        return user;
    }

    @Generated
    public void setUser(final User user) {
        this.user = user;
    }

    // shortcut to access the related group
    @Transient
    public Group getGroup() {
        if (this.metadata == null) {
            return null;
        }
        return this.metadata.getGroup();
    }

    @Generated
    public boolean isForwarded() {
        return isAnnotForwarded;
    }
    
    @Generated
    public void setForwarded(final boolean value) {
        this.isAnnotForwarded = value;
    }
    
    @Generated
    public String getForwardJustification() {
        return forwardJustification;
    }
    
    @Generated
    public void setForwardJustification(final String justif) {
        this.forwardJustification = justif;
    }

    @Generated
    public Group getOriginGroup() {
        return originGroup;
    }
    
    @Generated
    public void setOriginGroup(final Group orGroup) {
        this.originGroup = orGroup;
    }
    @Generated
    public Long getOriginGroupID() {
        return originGroupID;
    }
    
    @Generated
    public void setOriginGroupID(final Long orGroup) {
        this.originGroupID = orGroup;
    }

    @Generated
    public String getReplyText() {
        return replyText;
    }

    @Generated
    public void setReplyText(String replyText) {
        this.replyText = replyText;
    }
    
    @Generated
    public List<Tag> getTags() {
        return tags;
    }

    @Generated
    public void setTags(final List<Tag> tags) {
        this.tags = tags;
    }

    @Generated
    public void setMetadataId(final long metadataId) {
        this.metadataId = metadataId;
    }

    @Generated
    public long getMetadataId() {
        return metadataId;
    }

    @Generated
    public void setMetadata(final Metadata meta) {
        this.metadata = meta;
    }

    @Generated
    public Metadata getMetadata() {
        return this.metadata;
    }

    @Generated
    public void setConnectedEntityId(final long entityId) {
        this.connectedEntityId = entityId;
    }
    
    @Generated
    public Long getConnectedEntityId() {
        return connectedEntityId;
    }
    
    @Generated
    public void setConnectedEntity(final Group entity) {
        this.connectedEntity = entity;
    }
    
    @Generated
    public Group getConnectedEntity() {
        return this.connectedEntity;
    }
    
    @Generated
    public String getRootAnnotationId() {
        return rootAnnotationId;
    }

    @Generated
    public void setRootAnnotationId(final String rootAnnotationId) {
        this.rootAnnotationId = rootAnnotationId;
    }

    @Generated
    public String getReferences() {
        return references;
    }

    @Generated
    public void setReferences(final String references) {
        this.references = references;
    }

    @Generated
    public AnnotationStatus getStatus() {
        return status;
    }

    @Generated
    public void setStatus(final AnnotationStatus status) {
        this.status = status;
    }

    @Generated
    public LocalDateTime getStatusUpdated() {
        return statusUpdated;
    }

    @Generated
    public void setStatusUpdated(final LocalDateTime upd) {
        this.statusUpdated = upd;
    }

    @Generated
    public Long getStatusUpdatedByUser() {
        return statusUpdatedByUser;
    }

    @Generated
    public void setStatusUpdatedByUser(final Long upd) {
        this.statusUpdatedByUser = upd;
    }
    
    @Generated
    public Long getStatusUpdatedByGroup() {
        return statusUpdatedByGroup;
    }

    @Generated
    public void setStatusUpdatedByGroup(final Long upd) {
        this.statusUpdatedByGroup = upd;
    }
    
    @Generated
    public boolean isSentDeleted() {
        return sentDeleted;
    }
    
    @Generated
    public void setSentDeleted(final boolean sentDeletedNow) {
        this.sentDeleted = sentDeletedNow;
    }

    @Generated
    public long getRespVersionSentDeleted() {
        return respVersionSentDeleted;
    }
    
    @Generated
    public void setRespVersionSentDeleted(final long rvsd) {
        this.respVersionSentDeleted = rvsd;
    }

    @Generated
    public String getPrecedingText() {
        return precedingText;
    }

    @Generated
    public void setPrecedingText(final String precedingText) {
        this.precedingText = precedingText;
    }

    @Generated
    public String getSucceedingText() {
        return succeedingText;
    }

    @Generated
    public void setSucceedingText(final String succeedingText) {
        this.succeedingText = succeedingText;
    }
    
    // -------------------------------------
    // equals and hashCode
    // -------------------------------------

    @Generated
    @Override
    public int hashCode() {
        return Objects.hash(created, updated, id, linkedAnnotationId, userId, rootAnnotationId, shared, text, 
                targetSelectors, references, metadataId, metadata, tags, connectedEntityId, connectedEntity, user, 
                status, statusUpdated, statusUpdatedByUser, statusUpdatedByGroup, sentDeleted, 
                respVersionSentDeleted, precedingText, succeedingText, justificationText,
                isAnnotForwarded, forwardJustification, originGroupID, originGroup);
    }

    @Generated
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Annotation other = (Annotation) obj;
        return Objects.equals(this.id, other.id) &&
                Objects.equals(this.created, other.created) &&
                Objects.equals(this.updated, other.updated) &&
                Objects.equals(this.userId, other.userId) &&
                Objects.equals(this.rootAnnotationId, other.rootAnnotationId) &&
                Objects.equals(this.references, other.references) &&
                Objects.equals(this.shared, other.shared) &&
                Objects.equals(this.text, other.text) &&
                Objects.equals(this.justificationText, other.justificationText) &&
                Objects.equals(this.status, other.status) &&
                Objects.equals(this.statusUpdated, other.statusUpdated) &&
                Objects.equals(this.statusUpdatedByUser, other.statusUpdatedByUser) &&
                Objects.equals(this.statusUpdatedByGroup, other.statusUpdatedByGroup) &&
                Objects.equals(this.sentDeleted, other.sentDeleted) &&                
                Objects.equals(this.respVersionSentDeleted, other.respVersionSentDeleted) &&
                Objects.equals(this.linkedAnnotationId, other.linkedAnnotationId) &&
                Objects.equals(this.targetSelectors, other.targetSelectors) &&
                Objects.equals(this.metadataId, other.metadataId) &&
                Objects.equals(this.metadata, other.metadata) &&
                Objects.equals(this.connectedEntityId, other.connectedEntityId) &&
                Objects.equals(this.connectedEntity, other.connectedEntity) &&
                Objects.equals(this.user, other.user) &&
                Objects.equals(this.tags, other.tags) &&
                Objects.equals(this.precedingText, other.precedingText) &&
                Objects.equals(this.succeedingText, other.succeedingText) &&
                Objects.equals(this.isAnnotForwarded, other.isAnnotForwarded) &&
                Objects.equals(this.forwardJustification, other.forwardJustification);
    }

}
