package eu.europa.ec.leos.services.document;

import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.domain.common.Result;
import eu.europa.ec.leos.domain.vo.CloneProposalMetadataVO;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.model.action.ContributionVO;
import io.atlassian.fugue.Pair;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Instance(InstanceType.COUNCIL)
public class ContributionServiceMandateImpl implements ContributionService {

    @Override
    public <T extends XmlDocument> List<ContributionVO> getDocumentContributions(String documentId, int annexIndex, Class<T> filterType) {
        return Arrays.asList();
    }

    @Override
    public Result<?> updateContributionStatusAfterContributionDone(String cloneProposalRef, String cloneLegFileName,
                                                                   CloneProposalMetadataVO cloneProposalMetadataVO) {
        return new Result<>(new Pair(null, null), null);
    }

    @Override
    public void updateContributionMergeActions(String cloneDocumentId, String legFileName, String documentName, byte[] xmlContent) {
    }
}
