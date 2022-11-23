package eu.europa.ec.leos.repository.document;

import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.document.FinancialStatement;
import eu.europa.ec.leos.domain.cmis.metadata.FinancialStatementMetadata;
import org.springframework.security.access.prepost.PostAuthorize;

import java.util.List;

public interface FinancialStatementRepository {
    /**
     * Creates an [FinancialStatement] document from a given template and with the specified characteristics.
     *
     * @param templateId the ID of the template for the FinancialStatement.
     * @param path       the path where to create the FinancialStatement.
     * @param name       the name of the FinancialStatement.
     * @param metadata   the metadata of the FinancialStatement.
     * @return the created FinancialStatement document.
     */
    FinancialStatement createFinancialStatement(String templateId, String path, String name, FinancialStatementMetadata metadata);

    /**
     * Creates an [FinancialStatement] document from a given content and with the specified characteristics.
     *
     * @param path     the path where to create the FinancialStatement.
     * @param name     the name of the FinancialStatement.
     * @param metadata the metadata of the FinancialStatement.
     * @param content  the content of the FinancialStatement.
     * @return the created FinancialStatement document.
     */
    FinancialStatement createFinancialStatementFromContent(String path, String name, FinancialStatementMetadata metadata, byte[] content);

    /**
     * Updates an [FinancialStatement] document with the given metadata.
     *
     * @param id       the ID of the FinancialStatement document to update.
     * @param metadata the metadata of the FinancialStatement.
     * @return the updated FinancialStatement document.
     */
    FinancialStatement updateFinancialStatement(String id, FinancialStatementMetadata metadata);

    /**
     * Updates an [FinancialStatement] document with the given content.
     *
     * @param id      the ID of the FinancialStatement document to update.
     * @param content the content of the FinancialStatement.
     * @param versionType  the version type to be created
     * @param comment the comment of the update, optional.
     * @return the updated FinancialStatement document.
     */
    FinancialStatement updateFinancialStatement(String id, byte[] content, VersionType versionType, String comment);

    /**
     * Updates a [FinancialStatement] document with the given metadata and content.
     *
     * @param id       the ID of the FinancialStatement document to update.
     * @param metadata the metadata of the FinancialStatement.
     * @param content  the content of the FinancialStatement.
     * @param versionType  the version type to be created
     * @param comment  the comment of the update, optional.
     * @return the updated FinancialStatement document.
     */
    FinancialStatement updateFinancialStatement(String id, FinancialStatementMetadata metadata, byte[] content, VersionType versionType, String comment);

    FinancialStatement updateMilestoneComments(String id, List<String> milestoneComments, byte[] content, VersionType versionType, String comment);

    FinancialStatement updateMilestoneComments(String id, List<String> milestoneComments);

    /**
     * Finds a [FinancialStatement] document with the specified characteristics.
     *
     * @param id     the ID of the FinancialStatement document to retrieve.
     * @param latest retrieves the latest version of the proposal document, when *true*.
     * @return the found FinancialStatement document.
     */
    FinancialStatement findFinancialStatementById(String id, boolean latest);

    /**
     * Deletes an [FinancialStatement] document with the specified characteristics.
     *
     * @param id the ID of the FinancialStatement document to delete.
     */
    void deleteFinancialStatement(String id);

    /**
     * Finds all versions of a [FinancialStatement] document with the specified characteristics.
     *
     * @param id           the ID of the FinancialStatement document to retrieve.
     * @param fetchContent streams the content
     * @return the list of found FinancialStatement document versions or empty.
     */
    List<FinancialStatement> findFinancialStatementVersions(String id, boolean fetchContent);

    /**
     * Finds a [FinancialStatement] document with the specified characteristics.
     *
     * @param ref the reference metadata of the FinancialStatement document to retrieve.
     * @return the found FinancialStatement document.
     */
    @PostAuthorize("hasPermission(returnObject, 'CAN_READ')")
    FinancialStatement findFinancialStatementByRef(String ref);

    List<FinancialStatement> findAllMinorsForIntermediate(String docRef, String curr, int startIndex, int maxResults);

    int findAllMinorsCountForIntermediate(String docRef, String currIntVersion);

    Integer findAllMajorsCount(String docRef);

    List<FinancialStatement> findAllMajors(String docRef, int startIndex, int maxResult);

    List<FinancialStatement> findRecentMinorVersions(String documentId, String documentRef, int startIndex, int maxResults);

    Integer findRecentMinorVersionsCount(String documentId, String documentRef);

    FinancialStatement findFirstVersion(String documentRef);
}
