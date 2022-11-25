package eu.europa.ec.leos.services.document;

import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.document.Explanatory;
import eu.europa.ec.leos.domain.cmis.metadata.ExplanatoryMetadata;
import eu.europa.ec.leos.domain.common.TocMode;
import eu.europa.ec.leos.model.action.VersionVO;
import eu.europa.ec.leos.model.explanatory.ExplanatoryStructureType;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;

import java.util.List;
import java.util.Map;

public interface ExplanatoryService {

    Explanatory createExplanatory(String templateId, String path, ExplanatoryMetadata metadata, String actionMessage, byte[] content);

    Explanatory createExplanatoryFromContent(String path, ExplanatoryMetadata metadata, String actionMessage, byte[] content, String name);

    void deleteExplanatory(Explanatory explanatory);

    Explanatory updateExplanatory(Explanatory explanatory, ExplanatoryMetadata metadata, VersionType versionType, String comment);

    Explanatory updateExplanatory(Explanatory explanatory, byte[] updatedExplanatoryContent, VersionType versionType, String comment);

    Explanatory updateExplanatory(Explanatory explanatory, byte[] updatedExplanatoryContent, ExplanatoryMetadata metadata, VersionType versionType, String comment);

    Explanatory updateExplanatory(Explanatory explanatory, byte[] updatedExplanatoryContent, String comment);

    Explanatory updateExplanatoryWithMilestoneComments(Explanatory explanatory, List<String> milestoneComments, VersionType versionType, String comment);

    Explanatory updateExplanatoryWithMilestoneComments(String explanatoryId, List<String> milestoneComments);

    Explanatory findExplanatory(String id);

    Explanatory findExplanatoryVersion(String id);

    List<Explanatory> findVersions(String id);

    Explanatory createVersion(String id, VersionType versionType, String comment);

    List<TableOfContentItemVO> getTableOfContent(Explanatory document, TocMode mode);

    Explanatory saveTableOfContent(Explanatory explanatory, List<TableOfContentItemVO> tocList, ExplanatoryStructureType explanatoryStructureType, String actionMsg, User user);

    Explanatory findExplanatoryByRef(String ref);

    List<VersionVO> getAllVersions(String id, String documentId);

    List<Explanatory> findAllMinorsForIntermediate(String docRef, String currIntVersion, int startIndex, int maxResults);

    int findAllMinorsCountForIntermediate(String docRef, String currIntVersion);

    Integer findAllMajorsCount(String docRef);

    List<Explanatory> findAllMajors(String docRef, int startIndex, int maxResults);

    List<Explanatory> findRecentMinorVersions(String documentId, String documentRef, int startIndex, int maxResults);

    Integer findRecentMinorVersionsCount(String documentId, String documentRef);

    List<String> getAncestorsIdsForElementId(Explanatory explanatory, List<String> elementIds);

    Explanatory findFirstVersion(String documentRef);

    List<Explanatory> findCouncilExplanatoryByPackagePath(String path);
    
    Explanatory updateExplanatory(String id, Map<String, Object> properties, boolean latest);

    Explanatory findExplanatoryByVersion(String documentRef, String versionLabel);
}
