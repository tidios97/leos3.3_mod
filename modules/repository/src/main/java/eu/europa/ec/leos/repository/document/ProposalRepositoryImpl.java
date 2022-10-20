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

import java.util.List;
import java.util.Map;

import eu.europa.ec.leos.domain.vo.CloneProposalMetadataVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.domain.cmis.metadata.ProposalMetadata;
import eu.europa.ec.leos.repository.LeosRepository;

/**
 * Proposal Repository implementation.
 *
 * @constructor Creates a specific Proposal Repository, injected with a generic LEOS Repository.
 */
@Repository
public class ProposalRepositoryImpl implements ProposalRepository {

    private static final Logger logger = LoggerFactory.getLogger(ProposalRepositoryImpl.class);

    private final LeosRepository leosRepository;

    @Autowired
    public ProposalRepositoryImpl(LeosRepository leosRepository) {
        this.leosRepository = leosRepository;
    }

    @Override
    public Proposal createProposal(String templateId, String path, String name, ProposalMetadata metadata) {
        logger.debug("Creating Proposal... [template=" + templateId + ", path=" + path + ", name=" + name + "]");
        return leosRepository.createDocument(templateId, path, name, metadata, Proposal.class);
    }

    @Override
    public Proposal createProposalFromContent(String path, String name, ProposalMetadata metadata, byte[] contentBytes) {
        logger.debug("Creating Proposal With Content... [path=" + path + ", name=" + name + "]");
        return leosRepository.createDocumentFromContent(path, name, metadata, Proposal.class, LeosCategory.PROPOSAL.name(), contentBytes);
    }

    @Override
    public Proposal createClonedProposalFromContent(String path, String name, ProposalMetadata metadata,
                                              CloneProposalMetadataVO cloneProposalMetadataVO, byte[] contentBytes) {
        logger.debug("Creating Proposal With Content... [path=" + path + ", name=" + name + "]");
        return leosRepository.createClonedDocumentFromContent(path, name, metadata, cloneProposalMetadataVO, Proposal.class,
                LeosCategory.PROPOSAL.name(), contentBytes);
    }

    @Override
    public Proposal updateProposal(String id, ProposalMetadata metadata) {
        logger.debug("Updating Proposal metadata... [id=" + id + "]");
        return leosRepository.updateDocument(id, metadata, Proposal.class);
    }

    @Override
    public Proposal updateProposal(String id, Map<String, Object> properties) {
        logger.debug("Updating Proposal custom properties... [id=" + id + "]");
        return leosRepository.updateDocument(id, properties, Proposal.class, true);
    }

    @Override public Proposal updateProposal(String id, Map<String, Object> properties, boolean latest) {
        logger.debug("Updating Proposal metadata properties... [id=" + id + "]");
        return leosRepository.updateDocument(id, properties, Proposal.class, latest);
    }

    @Override public Proposal updateProposal(String id, byte[] content, VersionType versionType, String comment) {
        logger.debug("Updating Proposal content... [id=" + id + "]");
        return leosRepository.updateDocument(id, content, versionType, comment, Proposal.class);
    }

    @Override public Proposal updateMilestoneComments(String id, List<String> milestoneComments, byte[] content, VersionType versionType, String comment) {
        logger.debug("Updating Proposal milestoneComments... [id=" + id + "]");
        return leosRepository.updateMilestoneComments(id, content, milestoneComments, versionType, comment, Proposal.class);
    }

    @Override
    public Proposal updateProposal(String id, List<String> milestoneComments, byte[] content, VersionType versionType, String comment) {
        logger.debug("Updating Proposal metadata... [id=" + id + "]");
        return leosRepository.updateMilestoneComments(id, content, milestoneComments, versionType, comment, Proposal.class);
    }

    @Override
    public Proposal updateMilestoneComments(String id, List<String> milestoneComments) {
        logger.debug("Updating Proposal metadata... [id=" + id + "]");
        return leosRepository.updateMilestoneComments(id, milestoneComments, Proposal.class);
    }

    @Override
    public Proposal updateProposal(String id, byte[] content) {
        logger.debug("Updating Proposal content... [id=" + id + "]");
        return leosRepository.updateDocument(id, content, VersionType.MINOR, "Content updated.", Proposal.class);
    }

    @Override
    public Proposal updateProposal(String id, byte[] content, Map<String, Object> properties) {
        logger.debug("Updating Proposal content and properties... [id=" + id + "]");
        return leosRepository.updateDocument(id, content, properties, VersionType.MINOR,
                "Content and properties updated", Proposal.class);
    }

    @Override
    public Proposal updateProposal(String id, ProposalMetadata metadata, byte[] content, VersionType versionType, String comment) {
        logger.debug("Updating Proposal metadata and content... [id=" + id + ", major=" + versionType + "]");
        return leosRepository.updateDocument(id, metadata, content, versionType, comment, Proposal.class);
    }

    @Override
    public Proposal findProposalById(String id, boolean latest) {
        logger.debug("Finding Proposal by ID... [id=" + id + ", latest=" + latest + "]");
        return leosRepository.findDocumentById(id, Proposal.class, latest);
    }

    @Override
    public List<Proposal> findProposalVersions(String id, boolean fetchContent) {
        logger.debug("Finding Proposal versions... [id=" + id + "]");
        return leosRepository.findDocumentVersionsById(id, Proposal.class, fetchContent);
    }

    @Override
    public List<Proposal> findAllMinorsForIntermediate(String docRef, String currIntVersion, int startIndex, int maxResults) {
        logger.debug("Finding Proposal versions between intermediates...");
        return leosRepository.findAllMinorsForIntermediate(Proposal.class, docRef, currIntVersion, startIndex, maxResults);
    }

    @Override
    public int findAllMinorsCountForIntermediate(String docRef, String currIntVersion) {
        logger.debug("Finding Proposal minor versions count between intermediates...");
        return leosRepository.findAllMinorsCountForIntermediate(Proposal.class, docRef, currIntVersion);
    }

    @Override
    public Integer findAllMajorsCount(String docRef) {
        return leosRepository.findAllMajorsCount(Proposal.class, docRef);
    }

    @Override
    public List<Proposal> findAllMajors(String docRef, int startIndex, int maxResult) {
        return leosRepository.findAllMajors(Proposal.class, docRef, startIndex, maxResult);
    }

    @Override
    public List<Proposal> findRecentMinorVersions(String documentId, String documentRef, int startIndex, int maxResults) {
        final Proposal proposal = leosRepository.findLatestMajorVersionById(Proposal.class, documentId);
        return leosRepository.findRecentMinorVersions(Proposal.class, documentRef, proposal.getCmisVersionLabel(), startIndex, maxResults);
    }

    @Override
    public Integer findRecentMinorVersionsCount(String documentId, String documentRef) {
        final Proposal proposal = leosRepository.findLatestMajorVersionById(Proposal.class, documentId);
        return leosRepository.findRecentMinorVersionsCount(Proposal.class, documentRef, proposal.getCmisVersionLabel());
    }

    @Override
    public Proposal findFirstVersion(String ref) {
        return leosRepository.findFirstVersion(Proposal.class, ref);
    }

    @Override
    public Proposal findProposalByRef(String ref) {
        logger.debug("Finding Proposal by ref... [ref=" + ref + "]");
        return leosRepository.findDocumentByRef(ref, Proposal.class);
    }
}
