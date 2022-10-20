package eu.europa.ec.leos.services.clone;

import eu.europa.ec.leos.domain.vo.CloneProposalMetadataVO;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class CloneContext {

    private CloneProposalMetadataVO cloneProposalMetadataVO;
    private boolean contribution;

    public CloneProposalMetadataVO getCloneProposalMetadataVO() {
        return cloneProposalMetadataVO;
    }

    public void setCloneProposalMetadataVO(CloneProposalMetadataVO cloneProposalMetadataVO) {
        this.cloneProposalMetadataVO = cloneProposalMetadataVO;
    }

    public boolean isClonedProposal() {
        return cloneProposalMetadataVO != null && cloneProposalMetadataVO.isClonedProposal();
    }

    public boolean isContribution() {
        return contribution;
    }

    public void setContribution(boolean contribution) {
        this.contribution = contribution;
    }
}
