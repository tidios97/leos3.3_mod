package eu.europa.ec.leos.services.document;

import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.domain.common.Result;
import eu.europa.ec.leos.domain.vo.CloneProposalMetadataVO;
import eu.europa.ec.leos.model.action.ContributionVO;

import java.io.IOException;
import java.util.List;

public interface ContributionService {

    <T extends XmlDocument> List<ContributionVO> getDocumentContributions(String documentId, int annexIndex, Class<T> filterType);

    Result<?> updateContributionStatusAfterContributionDone(String cloneProposalRef, String cloneLegFileName,
                                                            CloneProposalMetadataVO cloneProposalMetadataVO);

    void updateContributionMergeActions(String cloneDocumentId, String legFileName, String documentName,
                                        byte[] xmlContent) throws IOException;
}
