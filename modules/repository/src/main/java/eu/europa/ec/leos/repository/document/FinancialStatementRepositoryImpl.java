package eu.europa.ec.leos.repository.document;

import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.document.FinancialStatement;
import eu.europa.ec.leos.domain.cmis.metadata.FinancialStatementMetadata;
import eu.europa.ec.leos.repository.LeosRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FinancialStatementRepositoryImpl implements FinancialStatementRepository {

    private static final Logger logger = LoggerFactory.getLogger(FinancialStatementRepositoryImpl.class);

    private final LeosRepository leosRepository;

    @Autowired
    public FinancialStatementRepositoryImpl(LeosRepository leosRepository) {
        this.leosRepository = leosRepository;
    }

    @Override
    public FinancialStatement createFinancialStatement(String templateId, String path, String name, FinancialStatementMetadata metadata) {
        logger.debug("Creating FinancialStatement... [template=" + templateId + ", path=" + path + ", name=" + name + "]");
        return leosRepository.createDocument(templateId, path, name, metadata, FinancialStatement.class);
    }

    @Override
    public FinancialStatement createFinancialStatementFromContent(String path, String name, FinancialStatementMetadata metadata, byte[] content) {
        logger.debug("Creating FinancialStatement From Content... [tpath=" + path + ", name=" + name + "]");
        return leosRepository.createDocumentFromContent(path, name, metadata, FinancialStatement.class,
                LeosCategory.FINANCIAL_STATEMENT.name(), content);
    }

    @Override
    public FinancialStatement updateFinancialStatement(String id, FinancialStatementMetadata metadata) {
        logger.debug("Updating FinancialStatement metadata... [id=" + id + "]");
        return leosRepository.updateDocument(id, metadata, FinancialStatement.class);
    }

    @Override
    public FinancialStatement updateFinancialStatement(String id, byte[] content, VersionType versionType, String comment) {
        logger.debug("Updating FinancialStatement content... [id=" + id + "]");
        return leosRepository.updateDocument(id, content, versionType, comment, FinancialStatement.class);
    }

    @Override
    public FinancialStatement updateFinancialStatement(String id, FinancialStatementMetadata metadata, byte[] content, VersionType versionType, String comment) {
        logger.debug("Updating FinancialStatement metadata and content... [id=" + id + "]");
        return leosRepository.updateDocument(id, metadata, content, versionType, comment, FinancialStatement.class);
    }

    @Override
    public FinancialStatement updateMilestoneComments(String id, List<String> milestoneComments, byte[] content, VersionType versionType, String comment) {
        logger.debug("Updating FinancialStatement milestoneComments and content... [id=" + id + "]");
        return leosRepository.updateMilestoneComments(id, content, milestoneComments, versionType, comment, FinancialStatement.class);
    }

    @Override
    public FinancialStatement updateMilestoneComments(String id, List<String> milestoneComments) {
        logger.debug("Updating FinancialStatement milestoneComments... [id=" + id + "]");
        return leosRepository.updateMilestoneComments(id, milestoneComments, FinancialStatement.class);
    }

    @Override
    public FinancialStatement findFinancialStatementById(String id, boolean latest) {
        logger.debug("Finding FinancialStatement by ID... [id=" + id + ", latest=" + latest + "]");
        return leosRepository.findDocumentById(id, FinancialStatement.class, latest);
    }

    @Override
    public void deleteFinancialStatement(String id) {
        logger.debug("Deleting FinancialStatement... [id=" + id + "]");
        leosRepository.deleteDocumentById(id);
    }

    @Override
    public List<FinancialStatement> findFinancialStatementVersions(String id, boolean fetchContent) {
        logger.debug("Finding FinancialStatement versions... [id=" + id + "]");
        return leosRepository.findDocumentVersionsById(id, FinancialStatement.class, fetchContent);
    }

    @Override
    public FinancialStatement findFinancialStatementByRef(String ref) {
        logger.debug("Finding FinancialStatement by ref... [ref=" + ref + "]");
        return leosRepository.findDocumentByRef(ref, FinancialStatement.class);
    }

    @Override
    public List<FinancialStatement> findAllMinorsForIntermediate(String docRef, String currIntVersion, int startIndex, int maxResults) {
        logger.debug("Finding FinancialStatement versions between intermediates...");
        return leosRepository.findAllMinorsForIntermediate(FinancialStatement.class, docRef, currIntVersion, startIndex, maxResults);
    }

    @Override
    public int findAllMinorsCountForIntermediate(String docRef, String currIntVersion) {
        logger.debug("Finding FinancialStatement minor versions count between intermediates...");
        return leosRepository.findAllMinorsCountForIntermediate(FinancialStatement.class, docRef, currIntVersion);
    }

    @Override
    public Integer findAllMajorsCount(String docRef) {
        return leosRepository.findAllMajorsCount(FinancialStatement.class, docRef);
    }

    @Override
    public List<FinancialStatement> findAllMajors(String docRef, int startIndex, int maxResult) {
        return leosRepository.findAllMajors(FinancialStatement.class, docRef, startIndex, maxResult);
    }

    @Override
    public List<FinancialStatement> findRecentMinorVersions(String documentId, String documentRef, int startIndex, int maxResults) {
        final FinancialStatement FinancialStatement = leosRepository.findLatestMajorVersionById(FinancialStatement.class, documentId);
        return leosRepository.findRecentMinorVersions(FinancialStatement.class, documentRef, FinancialStatement.getCmisVersionLabel(), startIndex, maxResults);
    }

    @Override
    public Integer findRecentMinorVersionsCount(String documentId, String documentRef) {
        final FinancialStatement FinancialStatement = leosRepository.findLatestMajorVersionById(FinancialStatement.class, documentId);
        return leosRepository.findRecentMinorVersionsCount(FinancialStatement.class, documentRef, FinancialStatement.getCmisVersionLabel());
    }

    @Override
    public FinancialStatement findFirstVersion(String documentRef) {
        return leosRepository.findFirstVersion(FinancialStatement.class, documentRef);
    }
}
