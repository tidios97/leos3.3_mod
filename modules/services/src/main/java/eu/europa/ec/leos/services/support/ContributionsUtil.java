package eu.europa.ec.leos.services.support;

import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.document.Annex;
import eu.europa.ec.leos.domain.cmis.document.Bill;
import eu.europa.ec.leos.domain.cmis.document.Memorandum;
import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.action.CheckinCommentVO;
import eu.europa.ec.leos.vo.contribution.ContributionLegDocumentVO;
import eu.europa.ec.leos.model.action.ContributionVO;
import eu.europa.ec.leos.services.document.util.CheckinCommentUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ContributionsUtil {
    public static <D extends XmlDocument> List<ContributionVO> buildContributionsVO(List<ContributionLegDocumentVO<D>> contributions, MessageHelper messageHelper) {
        List<ContributionVO> allContributions = new ArrayList<>();
        contributions.forEach(contribution -> {
            String contributionCreator = contribution.getOriginRef();
            D doc = contribution.getDocument();
            final String checkinCommentJson;
            if (doc.getMilestoneComments().size() > 0) {
                // Only the first comment is related to the document changes. All other comments, if presents,
                // means that the same document, without being changed, is included in other milestones.
                // Example: Create a Milestone 1. Enter inside Annex, make a change, and then create another Milestone 2.
                // In doc VersionCards is shown only the first Milestone1 and in milestoneComments of the doc are present both comments.
                // We need to show only Milestone 1 comment.
                checkinCommentJson = doc.getMilestoneComments().get(0);
            } else {
                checkinCommentJson = doc.getVersionComment() != null ? doc.getVersionComment() : messageHelper.getMessage("popup.label.contribution.nocomment");
            }
            final CheckinCommentVO checkinCommentVO = CheckinCommentUtil.getJavaObjectFromJson(checkinCommentJson);

            ContributionVO contributionVO = new ContributionVO();
            contributionVO.setDocumentId(doc.getId());
            contributionVO.setVersionNumber(new ContributionVO.VersionNumber(doc.getVersionLabel()));
            contributionVO.setUpdatedDate(doc.getLastModificationInstant());
            contributionVO.setContributionCreator(contributionCreator);
            contributionVO.setCollaborators(doc.getCollaborators());
            contributionVO.setCheckinCommentVO(checkinCommentVO);
            contributionVO.setVersionedReference(doc.getVersionedReference());
            contributionVO.setXmlContent(contribution.getContent());
            contributionVO.setLegFileName(contribution.getLegFileName());
            contributionVO.setDocumentName(contribution.getDocumentName());
            if (doc.getCategory().equals(LeosCategory.BILL)) {
                contributionVO.setContributionStatus(((Bill)doc).getContributionStatus());
            } else if (doc.getCategory().equals(LeosCategory.MEMORANDUM)) {
                contributionVO.setContributionStatus(((Memorandum)doc).getContributionStatus());
            } else if (doc.getCategory().equals(LeosCategory.ANNEX)) {
                contributionVO.setContributionStatus(((Annex)doc).getContributionStatus());
            } else if (doc.getCategory().equals(LeosCategory.PROPOSAL)) {
                contributionVO.setContributionStatus(((Proposal)doc).getContributionStatus());
            }
            allContributions.add(contributionVO);
        });
        Collections.sort(allContributions, Collections.reverseOrder());
        return allContributions.stream()
                .distinct()
                .collect(Collectors.toList());
    }
}
