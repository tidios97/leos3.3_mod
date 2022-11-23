package eu.europa.ec.leos.services.dto.response;

import eu.europa.ec.leos.domain.vo.DocumentVO;

import java.util.List;

public class WorkspaceProposalResponse {

    private List<DocumentVO> proposals;
    private Integer proposalCount;

    public WorkspaceProposalResponse(List<DocumentVO> proposals, Integer proposalCount) {
        this.proposals = proposals;
        this.proposalCount = proposalCount;
    }

    public List<DocumentVO> getProposals() {
        return proposals;
    }

    public Integer getProposalCount() {
        return proposalCount;
    }
}
