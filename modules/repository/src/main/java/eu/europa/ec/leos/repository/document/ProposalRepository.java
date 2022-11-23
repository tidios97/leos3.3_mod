/*
 * Copyright 2017 European Commission
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
package eu.europa.ec.leos.repository.document;


import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.domain.cmis.metadata.ProposalMetadata;
import eu.europa.ec.leos.domain.vo.CloneProposalMetadataVO;
import org.springframework.security.access.prepost.PostAuthorize;

import java.util.List;
import java.util.Map;

/**
 * Proposal Repository interface.
 * <p>
 * Represents collections of *Proposal* documents, with specific methods to persist and retrieve.
 * Allows CRUD operations based on strongly typed Business Entities: [Proposal] and [ProposalMetadata].
 */
public interface ProposalRepository {

    /**
     * Creates a [Proposal] document from a given template and with the specified characteristics.
     *
     * @param templateId the ID of the template for the proposal.
     * @param path       the path where to create the proposal.
     * @param name       the name of the proposal.
     * @param metadata   the metadata of the proposal.
     * @return the created proposal document.
     */
    Proposal createProposal(String templateId, String path, String name, ProposalMetadata metadata);

    /**
     * Creates a [Proposal] document from a given content and with the specified characteristics.
     *
     * @param path     the path where to create the proposal.
     * @param name     the name of the proposal.
     * @param metadata the metadata of the proposal.
     * @return the created proposal document.
     */
    Proposal createProposalFromContent(String path, String name, ProposalMetadata metadata, byte[] content);

    /**
     * Creates a [Proposal] document from a given content and with the specified characteristics.
     *
     * @param path     the path where to create the proposal.
     * @param name     the name of the proposal.
     * @param metadata the metadata of the proposal.
     * @param cloneProposalMetadataVO clone proposal metadata vo
     * @return the created proposal document.
     */
    Proposal createClonedProposalFromContent(String path, String name, ProposalMetadata metadata,
                                       CloneProposalMetadataVO cloneProposalMetadataVO, byte[] content);

    /**
     * Updates a [Proposal] document with the given metadata.
     *
     * @param id       the ID of the proposal document to update.
     * @param metadata the metadata of the proposal.
     * @return the updated proposal document.
     */
    Proposal updateProposal(String id, ProposalMetadata metadata);

    Proposal updateProposal(String id, List<String> milestoneComments, byte[] content, VersionType versionType, String comment);

    Proposal updateMilestoneComments(String id, List<String> milestoneComments);

    /**
     * Updates a [Proposal] document with the given content and metadata properties.
     *
     * @param id      the ID of the proposal document to update.
     * @param content the content of the proposal.
     * @param properties the metadata properties to update
     * @return the updated proposal document.
     */
    Proposal updateProposal(String id, byte[] content, Map<String, Object> properties);

    /**
     * Updates a [Proposal] document with the given content.
     *
     * @param id      the ID of the proposal document to update.
     * @param content the content of the proposal.
     * @return the updated proposal document.
     */
    Proposal updateProposal(String id, byte[] content);

    /**
     * Updates a [Proposal] document with the given metadata and content.
     *
     * @param id       the ID of the proposal document to update.
     * @param metadata the metadata of the proposal.
     * @param content  the content of the proposal.
     * @param versionType  the version type to be created
     * @param comment  the comment of the update, optional.
     * @return the updated proposal document.
     */
    Proposal updateProposal(String id, ProposalMetadata metadata, byte[] content, VersionType versionType, String comment);

    /**
     * Finds a [Proposal] document with the specified characteristics.
     *
     * @param id     the ID of the proposal document to retrieve.
     * @param latest retrieves the latest version of the proposal document, when *true*.
     * @return the found proposal document.
     */
    Proposal findProposalById(String id, boolean latest);

    /**
     * Finds a [Proposal] document with the specified characteristics.
     *
     * @param ref the reference metadata of the proposal document to retrieve.
     * @return the found proposal document.
     */
    @PostAuthorize("hasPermission(returnObject, 'CAN_READ')")
    Proposal findProposalByRef(String ref);

    /**
     * Updates a [Proposal] metadata properties.
     *
     * @param id       the ID of the proposal document to update.
     * @param properties the cmis metadata properties to update.
     * @return the updated proposal document.
     */
    Proposal updateProposal(String id, Map<String, Object> properties);

    /**
     * Updates a [Proposal] document with the given metadata properties.
     *
     * @param id       the ID of the proposal document to update.
     * @param properties the metadata properties of the proposal.
     * @param latest
     * @return the updated proposal document.
     */
    Proposal updateProposal(String id, Map<String, Object> properties, boolean latest);

    /**
     * Updates a [Proposal] document with the given content.
     *
     * @param id      the ID of the proposal document to update.
     * @param content the content of the proposal.
     * @param versionType the version type to be created
     * @param comment the comment of the update, optional.
     * @return the updated proposal document.
     */
    Proposal updateProposal(String id, byte[] content, VersionType versionType, String comment);

    Proposal updateMilestoneComments(String id, List<String> milestoneComments, byte[] content, VersionType versionType, String comment);

    /**
     * Finds all versions of a [Proposal] document with the specified characteristics.
     *
     * @param id           the ID of the Proposal document to retrieve.
     * @param fetchContent streams the content
     * @return the list of found Proposal document versions or empty.
     */
    List<Proposal> findProposalVersions(String id, boolean fetchContent);

    List<Proposal> findAllMinorsForIntermediate(String docRef, String currIntVersion, int startIndex, int maxResults);

    int findAllMinorsCountForIntermediate(String docRef, String currIntVersion);

    Integer findAllMajorsCount(String docRef);

    List<Proposal> findAllMajors(String docRef, int startIndex, int maxResult);

    List<Proposal> findRecentMinorVersions(String documentId, String documentRef, int startIndex, int maxResults);

    Integer findRecentMinorVersionsCount(String documentId, String documentRef);

    Proposal findFirstVersion(String ref);


    /**
     * Only to be used internally to get the proposal document using ref.
     * @param ref
     * @return
     */
    Proposal getProposalByRef(String ref);
}
