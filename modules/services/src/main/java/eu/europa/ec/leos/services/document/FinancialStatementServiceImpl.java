package eu.europa.ec.leos.services.document;

import com.google.common.base.Stopwatch;
import cool.graph.cuid.Cuid;
import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.document.FinancialStatement;
import eu.europa.ec.leos.domain.cmis.metadata.FinancialStatementMetadata;
import eu.europa.ec.leos.domain.common.TocMode;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.action.VersionVO;
import eu.europa.ec.leos.model.FinancialStatement.FinancialStatementStructureType;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.repository.document.FinancialStatementRepository;
import eu.europa.ec.leos.repository.store.PackageRepository;
import eu.europa.ec.leos.services.document.util.DocumentVOProvider;
import eu.europa.ec.leos.services.numbering.NumberService;
import eu.europa.ec.leos.services.processor.content.TableOfContentProcessor;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.processor.node.XmlNodeConfigProcessor;
import eu.europa.ec.leos.services.processor.node.XmlNodeProcessor;
import eu.europa.ec.leos.services.support.VersionsUtil;
import eu.europa.ec.leos.services.support.XPathCatalog;
import eu.europa.ec.leos.services.validation.ValidationService;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static eu.europa.ec.leos.services.processor.node.XmlNodeConfigProcessor.createValueMap;
import static eu.europa.ec.leos.services.support.XmlHelper.DOC;
import static eu.europa.ec.leos.services.support.XmlHelper.LEVEL;

@Service
public class FinancialStatementServiceImpl implements FinancialStatementService {

    private static final Logger LOG = LoggerFactory.getLogger(FinancialStatementServiceImpl.class);

    public static final String FINANCIAL_STATEMENT_NAME_PREFIX = "FINANCIAL_STATEMENT-";
    public static final String FINANCIAL_STATEMENT_DOC_EXTENSION = ".xml";

    private final FinancialStatementRepository financialStatementRepository;
    private final PackageRepository packageRepository;
    private final XmlNodeProcessor xmlNodeProcessor;
    private final XmlContentProcessor xmlContentProcessor;
    private final NumberService numberService;
    private final XmlNodeConfigProcessor xmlNodeConfigProcessor;
    private final DocumentVOProvider documentVOProvider;
    private final ValidationService validationService;
    private final MessageHelper messageHelper;
    private final XPathCatalog xPathCatalog;

    private final TableOfContentProcessor tableOfContentProcessor;

    @Autowired
    FinancialStatementServiceImpl(FinancialStatementRepository financialStatementRepository, PackageRepository packageRepository,
                                  XmlNodeProcessor xmlNodeProcessor, XmlContentProcessor xmlContentProcessor,
                                  NumberService numberService, XmlNodeConfigProcessor xmlNodeConfigProcessor,
                                  ValidationService validationService, DocumentVOProvider documentVOProvider,
                                  TableOfContentProcessor tableOfContentProcessor, MessageHelper messageHelper,
                                  XPathCatalog xPathCatalog) {
        this.financialStatementRepository = financialStatementRepository;
        this.packageRepository = packageRepository;
        this.xmlNodeProcessor = xmlNodeProcessor;
        this.xmlContentProcessor = xmlContentProcessor;
        this.xmlNodeConfigProcessor = xmlNodeConfigProcessor;
        this.validationService = validationService;
        this.documentVOProvider = documentVOProvider;
        this.messageHelper = messageHelper;
        this.tableOfContentProcessor = tableOfContentProcessor;
        this.numberService = numberService;
        this.xPathCatalog = xPathCatalog;
    }

    @Override
    public FinancialStatement createFinancialStatement(String templateId, String path, FinancialStatementMetadata metadata, String actionMessage, byte[] content) {
        LOG.trace("Creating FinancialStatement... [templateId={}, path={}, metadata={}]", templateId, path, metadata);
        final String FinancialStatementUid = Cuid.createCuid();
        final String language = metadata.getLanguage();
        final String ref = FINANCIAL_STATEMENT_NAME_PREFIX + FinancialStatementUid +  "-" + language.toLowerCase();
        final String fileName = ref + FINANCIAL_STATEMENT_DOC_EXTENSION;
        metadata = metadata.withRef(ref);
        FinancialStatement FinancialStatement = financialStatementRepository.createFinancialStatement(templateId, path, fileName, metadata);
        byte[] updatedBytes = updateDataInXml((content == null) ? getContent(FinancialStatement) : content, metadata);
        return financialStatementRepository.updateFinancialStatement(FinancialStatement.getId(), metadata, updatedBytes, VersionType.MINOR, actionMessage);
    }

    @Override
    public FinancialStatement createFinancialStatementFromContent(String path, FinancialStatementMetadata metadata, String actionMessage, byte[] content, String name) {
        LOG.trace("Creating FinancialStatement From Content... [path={}, metadata={}]", path, metadata);
        FinancialStatement FinancialStatement = financialStatementRepository.createFinancialStatementFromContent(path, name, metadata, content);
        return financialStatementRepository.updateFinancialStatement(FinancialStatement.getId(), metadata, content, VersionType.MINOR, actionMessage);
    }

    @Override
    public void deleteFinancialStatement(FinancialStatement FinancialStatement) {
        LOG.trace("Deleting FinancialStatement... [id={}]", FinancialStatement.getId());
        financialStatementRepository.deleteFinancialStatement(FinancialStatement.getId());
    }

    @Override
    public FinancialStatement findFinancialStatement(String id) {
        LOG.trace("Finding FinancialStatement... [id={}]", id);
        return financialStatementRepository.findFinancialStatementById(id, true);
    }

    @Override
    @Cacheable(value = "docVersions")
    public FinancialStatement findFinancialStatementVersion(String id) {
        LOG.trace("Finding FinancialStatement version... [it={}]", id);
        return financialStatementRepository.findFinancialStatementById(id, false);
    }

    @Override
    public FinancialStatement updateFinancialStatement(FinancialStatement FinancialStatement, byte[] updatedFinancialStatementContent, VersionType versionType, String comment) {
        LOG.trace("Updating FinancialStatement Xml Content... [id={}]", FinancialStatement.getId());

        FinancialStatement = financialStatementRepository.updateFinancialStatement(FinancialStatement.getId(), updatedFinancialStatementContent, versionType, comment);

        //call validation on document with updated content
        validationService.validateDocumentAsync(documentVOProvider.createDocumentVO(FinancialStatement, updatedFinancialStatementContent));

        return FinancialStatement;
    }

    @Override
    public FinancialStatement updateFinancialStatement(FinancialStatement FinancialStatement, FinancialStatementMetadata updatedMetadata, VersionType versionType, String comment) {
        LOG.trace("Updating FinancialStatement... [id={}, updatedMetadata={}, versionType={}, comment={}]", FinancialStatement.getId(), updatedMetadata, versionType, comment);
        Stopwatch stopwatch = Stopwatch.createStarted();
        byte[] updatedBytes = updateDataInXml(getContent(FinancialStatement), updatedMetadata);

        FinancialStatement = financialStatementRepository.updateFinancialStatement(FinancialStatement.getId(), updatedMetadata, updatedBytes, versionType, comment);

        //call validation on document with updated content
        validationService.validateDocumentAsync(documentVOProvider.createDocumentVO(FinancialStatement, updatedBytes));

        LOG.trace("Updated FinancialStatement ...({} milliseconds)", stopwatch.elapsed(TimeUnit.MILLISECONDS));
        return FinancialStatement;
    }

    @Override
    public FinancialStatement updateFinancialStatement(FinancialStatement FinancialStatement, byte[] updatedFinancialStatementContent, FinancialStatementMetadata metadata, VersionType versionType, String comment) {
        LOG.trace("Updating FinancialStatement... [id={}, updatedMetadata={}, versionType={}, comment={}]", FinancialStatement.getId(), metadata, versionType, comment);
        Stopwatch stopwatch = Stopwatch.createStarted();
        updatedFinancialStatementContent = updateDataInXml(updatedFinancialStatementContent, metadata);

        FinancialStatement = financialStatementRepository.updateFinancialStatement(FinancialStatement.getId(), metadata, updatedFinancialStatementContent, versionType, comment);

        //call validation on document with updated content
        validationService.validateDocumentAsync(documentVOProvider.createDocumentVO(FinancialStatement, updatedFinancialStatementContent));

        LOG.trace("Updated FinancialStatement ...({} milliseconds)", stopwatch.elapsed(TimeUnit.MILLISECONDS));
        return FinancialStatement;
    }

    @Override
    public FinancialStatement updateFinancialStatement(FinancialStatement FinancialStatement, byte[] updatedFinancialStatementContent, String comment) {
        LOG.trace("Updating FinancialStatement... [id={}, updatedMetadata={} , comment={}]", FinancialStatement.getId(), updatedFinancialStatementContent, comment);
        Stopwatch stopwatch = Stopwatch.createStarted();
        FinancialStatement = financialStatementRepository.updateFinancialStatement(FinancialStatement.getId(), updatedFinancialStatementContent, VersionType.MINOR, comment);
        LOG.trace("Updated FinancialStatement ...({} milliseconds)", stopwatch.elapsed(TimeUnit.MILLISECONDS));
        return FinancialStatement;
    }

    @Override
    public FinancialStatement updateFinancialStatementWithMilestoneComments(FinancialStatement FinancialStatement, List<String> milestoneComments, VersionType versionType, String comment){
        LOG.trace("Updating FinancialStatement... [id={}, milestoneComments={}, versionType={}, comment={}]", FinancialStatement.getId(), milestoneComments, versionType, comment);
        final byte[] updatedBytes = getContent(FinancialStatement);
        FinancialStatement = financialStatementRepository.updateMilestoneComments(FinancialStatement.getId(), milestoneComments, updatedBytes, versionType, comment);
        return FinancialStatement;
    }

    @Override
    public FinancialStatement updateFinancialStatementWithMilestoneComments(String FinancialStatementId, List<String> milestoneComments){
        LOG.trace("Updating FinancialStatement... [id={}, milestoneComments={}]", FinancialStatementId, milestoneComments);
        return financialStatementRepository.updateMilestoneComments(FinancialStatementId, milestoneComments);
    }

    @Override
    public List<FinancialStatement> findVersions(String id) {
        LOG.trace("Finding FinancialStatement versions... [id={}]", id);
        //LEOS-2813 We have memory issues is we fetch the content of all versions.
        return financialStatementRepository.findFinancialStatementVersions(id,false);
    }

    @Override
    public FinancialStatement createVersion(String id, VersionType versionType, String comment) {
        LOG.trace("Creating FinancialStatement version... [id={}, versionType={}, comment={}]", id, versionType, comment);
        final FinancialStatement FinancialStatement = findFinancialStatement(id);
        final FinancialStatementMetadata metadata = FinancialStatement.getMetadata().getOrError(() -> "FinancialStatement metadata is required!");
        final Content content = FinancialStatement.getContent().getOrError(() -> "FinancialStatement content is required!");
        final byte[] contentBytes = content.getSource().getBytes();
        return financialStatementRepository.updateFinancialStatement(id, metadata, contentBytes, versionType, comment);
    }

    @Override
    public List<TableOfContentItemVO> getTableOfContent(FinancialStatement FinancialStatement, TocMode mode) {
        Validate.notNull(FinancialStatement, "FinancialStatement is required");
        final Content content = FinancialStatement.getContent().getOrError(() -> "FinancialStatement content is required!");
        final byte[] FinancialStatementContent = content.getSource().getBytes();
        return tableOfContentProcessor.buildTableOfContent(DOC, FinancialStatementContent, mode);
    }

    @Override
    public FinancialStatement saveTableOfContent(FinancialStatement FinancialStatement, List<TableOfContentItemVO> tocList, FinancialStatementStructureType FinancialStatementStructureType, String actionMsg, User user) {
        Validate.notNull(FinancialStatement, "FinancialStatement is required");
        Validate.notNull(tocList, "Table of content list is required");
        byte[] newXmlContent;

        newXmlContent = xmlContentProcessor.createDocumentContentWithNewTocList(tocList, getContent(FinancialStatement), user);
        if (FinancialStatementStructureType != null && LEVEL.equals(FinancialStatementStructureType.getType())) {
            newXmlContent = numberService.renumberLevel(newXmlContent);
        }
        newXmlContent = numberService.renumberParagraph(newXmlContent);
        newXmlContent = numberService.renumberDivisions(newXmlContent);
        newXmlContent = xmlContentProcessor.doXMLPostProcessing(newXmlContent);

        return updateFinancialStatement(FinancialStatement, newXmlContent, VersionType.MINOR, actionMsg);
    }

    private byte[] getContent(FinancialStatement FinancialStatement) {
        final Content content = FinancialStatement.getContent().getOrError(() -> "FinancialStatement content is required!");
        return content.getSource().getBytes();
    }

    private byte[] updateDataInXml(final byte[] content, FinancialStatementMetadata dataObject) {
        byte[] updatedBytes = xmlNodeProcessor.setValuesInXml(content, createValueMap(dataObject), xmlNodeConfigProcessor.getConfig(dataObject.getCategory()));
        return xmlContentProcessor.doXMLPostProcessing(updatedBytes);
    }

    @Override
    public FinancialStatement findFinancialStatementByRef(String ref) {
        LOG.trace("Finding FinancialStatement by ref... [ref=" + ref + "]");
        return financialStatementRepository.findFinancialStatementByRef(ref);
    }

    @Override
    public List<VersionVO> getAllVersions(String documentId, String docRef) {
        // TODO temporary call. paginated loading will be implemented in the future Story
        List<FinancialStatement> majorVersions = findAllMajors(docRef, 0, 9999);
        LOG.trace("Found {} majorVersions for [id={}]", majorVersions.size(), documentId);

        List<VersionVO> majorVersionsVO = VersionsUtil.buildVersionVO(majorVersions, messageHelper);
        return majorVersionsVO;
    }

    @Override
    public List<FinancialStatement> findAllMinorsForIntermediate(String docRef, String currIntVersion, int startIndex, int maxResults) {
        return financialStatementRepository.findAllMinorsForIntermediate(docRef, currIntVersion, startIndex, maxResults);
    }

    @Override
    public int findAllMinorsCountForIntermediate(String docRef, String currIntVersion) {
        return financialStatementRepository.findAllMinorsCountForIntermediate(docRef, currIntVersion);
    }

    @Override
    public Integer findAllMajorsCount(String docRef) {
        return financialStatementRepository.findAllMajorsCount(docRef);
    }

    @Override
    public List<FinancialStatement> findAllMajors(String docRef, int startIndex, int maxResults) {
        return financialStatementRepository.findAllMajors(docRef, startIndex, maxResults);
    }

    @Override
    public List<FinancialStatement> findRecentMinorVersions(String documentId, String documentRef, int startIndex, int maxResults) {
        return financialStatementRepository.findRecentMinorVersions(documentId, documentRef, startIndex, maxResults);
    }

    @Override
    public Integer findRecentMinorVersionsCount(String documentId, String documentRef) {
        return financialStatementRepository.findRecentMinorVersionsCount(documentId, documentRef);
    }

    @Override
    public List<String> getAncestorsIdsForElementId(FinancialStatement FinancialStatement, List<String> elementIds) {
        Validate.notNull(FinancialStatement, "FinancialStatement is required");
        Validate.notNull(elementIds, "Element id is required");
        List<String> ancestorIds = new ArrayList<String>();
        byte[] content = getContent(FinancialStatement);
        for (String elementId : elementIds) {
            ancestorIds.addAll(xmlContentProcessor.getAncestorsIdsForElementId(content, elementId));
        }
        return ancestorIds;
    }

    @Override
    public FinancialStatement findFirstVersion(String documentRef) {
        return financialStatementRepository.findFirstVersion(documentRef);
    }

    @Override
    public List<FinancialStatement> findFinancialStatementByPackagePath(String path) {
        return packageRepository.findDocumentsByPackagePath(path, FinancialStatement.class, true);
    }
}
