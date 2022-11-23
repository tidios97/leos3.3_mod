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
package eu.europa.ec.leos.services.document;


import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.domain.cmis.metadata.ProposalMetadata;
import eu.europa.ec.leos.domain.common.TocMode;
import eu.europa.ec.leos.domain.vo.CloneProposalMetadataVO;
import eu.europa.ec.leos.model.action.VersionVO;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;

import java.util.List;
import java.util.Map;

public interface ProposalService {

    Proposal createProposal(String templateId, String path, ProposalMetadata metadata, byte[] content);

    Proposal createProposalFromContent(String path, ProposalMetadata metadata, byte[] content);

    Proposal createClonedProposalFromContent(String path, ProposalMetadata metadata,
                                       CloneProposalMetadataVO cloneProposalMetadataVO, byte[] content);

    Proposal findProposal(String id);

    Proposal updateProposal(Proposal proposal, ProposalMetadata metadata, VersionType versionType, String comment);

    Proposal updateProposal(Proposal proposal, ProposalMetadata metadata);

    Proposal updateProposal(String id, Map<String, Object> properties);

    Proposal updateProposal(String proposalId, byte[] updatedBytes);

    Proposal updateProposal(String proposalId, byte[] updatedBytes, Map<String, Object> properties);

    Proposal addComponentRef(Proposal proposal, String href, LeosCategory leosCategory);

    Proposal addComponent(Proposal proposal, String id, LeosCategory leosCategory);

    Proposal updateProposalWithMilestoneComments(Proposal proposal, List<String> milestoneComments, VersionType versionType, String comment);

    Proposal updateProposalWithMilestoneComments(String proposalId, List<String> milestoneComments);

    Proposal removeComponentRef(Proposal proposal, String href);

    void updateProposalAsync(String id, String comment);

    Proposal findProposalByPackagePath(String path);

    Proposal createVersion(String id, VersionType versionType, String comment);

    Proposal findProposalByRef(String ref);

    CloneProposalMetadataVO getClonedProposalMetadata(byte[] xmlContent);

    void removeClonedProposalMetadata(String proposalId, String clonedProposalId, CloneProposalMetadataVO cloneProposalMetadataVO);

    List<CloneProposalMetadataVO> getClonedProposalMetadataVOs(String proposalId, String legDocumentName);

    String generateProposalName(String ref, String language);

    Map<String, String> getExplanatoryDocumentRef(byte[] xmlContent);

    Proposal findProposal(String id, boolean latest);

    Proposal findProposalVersion(String id);

    Proposal updateProposal(Proposal proposal, byte[] updatedProposalContent, VersionType versionType, String comment);

    Proposal updateProposal(String proposalId, ProposalMetadata metadata);

    Proposal updateProposal(String id, Map<String, Object> properties, boolean latest);

    Proposal updateProposal(Proposal proposal, byte[] updatedProposalContent, String comment);

    List<TableOfContentItemVO> getCoverPageTableOfContent(Proposal document, TocMode mode);

    List<Proposal> findVersions(String id);

    List<VersionVO> getAllVersions(String documentID, String docRef);

    List<Proposal> findAllMinorsForIntermediate(String docRef, String currIntVersion, int startIndex, int maxResults);

    int findAllMinorsCountForIntermediate(String docRef, String currIntVersion);

    Integer findAllMajorsCount(String docRef);

    List<Proposal> findAllMajors(String docRef, int startIndex, int maxResults);

    List<Proposal> findRecentMinorVersions(String documentId, String documentRef, int startIndex, int maxResults);

    Integer findRecentMinorVersionsCount(String documentId, String documentRef);

    XmlDocument findFirstVersion(String ref);

    String getPurposeFromXml(byte[] xml);

    Proposal getProposalByRef(String ref);

    String getOriginalMilestoneName(String docName, byte[] xmlContent);
}
