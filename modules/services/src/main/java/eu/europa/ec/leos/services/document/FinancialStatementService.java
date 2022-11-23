package eu.europa.ec.leos.services.document;

import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.document.FinancialStatement;
import eu.europa.ec.leos.domain.cmis.metadata.FinancialStatementMetadata;
import eu.europa.ec.leos.domain.common.TocMode;
import eu.europa.ec.leos.model.action.VersionVO;
import eu.europa.ec.leos.model.FinancialStatement.FinancialStatementStructureType;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;

import java.util.List;

public interface FinancialStatementService {

    FinancialStatement createFinancialStatement(String templateId, String path, FinancialStatementMetadata metadata, String actionMessage, byte[] content);

    FinancialStatement createFinancialStatementFromContent(String path, FinancialStatementMetadata metadata, String actionMessage, byte[] content, String name);

    void deleteFinancialStatement(FinancialStatement FinancialStatement);

    FinancialStatement updateFinancialStatement(FinancialStatement FinancialStatement, FinancialStatementMetadata metadata, VersionType versionType, String comment);

    FinancialStatement updateFinancialStatement(FinancialStatement FinancialStatement, byte[] updatedFinancialStatementContent, VersionType versionType, String comment);

    FinancialStatement updateFinancialStatement(FinancialStatement FinancialStatement, byte[] updatedFinancialStatementContent, FinancialStatementMetadata metadata, VersionType versionType, String comment);

    FinancialStatement updateFinancialStatement(FinancialStatement FinancialStatement, byte[] updatedFinancialStatementContent, String comment);

    FinancialStatement updateFinancialStatementWithMilestoneComments(FinancialStatement FinancialStatement, List<String> milestoneComments, VersionType versionType, String comment);

    FinancialStatement updateFinancialStatementWithMilestoneComments(String FinancialStatementId, List<String> milestoneComments);

    FinancialStatement findFinancialStatement(String id);

    FinancialStatement findFinancialStatementVersion(String id);

    List<FinancialStatement> findVersions(String id);

    FinancialStatement createVersion(String id, VersionType versionType, String comment);

    List<TableOfContentItemVO> getTableOfContent(FinancialStatement document, TocMode mode);

    FinancialStatement saveTableOfContent(FinancialStatement FinancialStatement, List<TableOfContentItemVO> tocList, FinancialStatementStructureType FinancialStatementStructureType, String actionMsg, User user);

    FinancialStatement findFinancialStatementByRef(String ref);

    List<VersionVO> getAllVersions(String id, String documentId);

    List<FinancialStatement> findAllMinorsForIntermediate(String docRef, String currIntVersion, int startIndex, int maxResults);

    int findAllMinorsCountForIntermediate(String docRef, String currIntVersion);

    Integer findAllMajorsCount(String docRef);

    List<FinancialStatement> findAllMajors(String docRef, int startIndex, int maxResults);

    List<FinancialStatement> findRecentMinorVersions(String documentId, String documentRef, int startIndex, int maxResults);

    Integer findRecentMinorVersionsCount(String documentId, String documentRef);

    List<String> getAncestorsIdsForElementId(FinancialStatement FinancialStatement, List<String> elementIds);

    FinancialStatement findFirstVersion(String documentRef);

    List<FinancialStatement> findFinancialStatementByPackagePath(String path);
}
