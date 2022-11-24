package eu.europa.ec.leos.services.document;

import com.google.common.base.Stopwatch;
import cool.graph.cuid.Cuid;
import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.document.Explanatory;
import eu.europa.ec.leos.domain.cmis.metadata.ExplanatoryMetadata;
import eu.europa.ec.leos.domain.common.TocMode;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.action.VersionVO;
import eu.europa.ec.leos.model.explanatory.ExplanatoryStructureType;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.repository.document.ExplanatoryRepository;
import eu.europa.ec.leos.repository.store.PackageRepository;
import eu.europa.ec.leos.services.document.util.DocumentVOProvider;
import eu.europa.ec.leos.services.numbering.NumberService;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.support.VersionsUtil;
import eu.europa.ec.leos.services.processor.node.XmlNodeConfigProcessor;
import eu.europa.ec.leos.services.processor.node.XmlNodeProcessor;
import eu.europa.ec.leos.services.processor.content.TableOfContentProcessor;
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
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static eu.europa.ec.leos.services.support.XmlHelper.DOC;
import static eu.europa.ec.leos.services.support.XmlHelper.LEVEL;
import static eu.europa.ec.leos.services.processor.node.XmlNodeConfigProcessor.createValueMap;
import static eu.europa.ec.leos.services.support.XmlHelper.XML_DOC_EXT;
import static eu.europa.ec.leos.services.support.XmlHelper.XML_NAME;

@Service
public class ExplanatoryServiceImpl implements ExplanatoryService {

    private static final Logger LOG = LoggerFactory.getLogger(ExplanatoryServiceImpl.class);

    public static final String EXPLANATORY_NAME_PREFIX = "explanatory_";
    public static final String EXPLANATORY_DOC_EXTENSION = ".xml";

    private final ExplanatoryRepository explanatoryRepository;
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
    ExplanatoryServiceImpl(ExplanatoryRepository explanatoryRepository, PackageRepository packageRepository,
                           XmlNodeProcessor xmlNodeProcessor, XmlContentProcessor xmlContentProcessor,
                           NumberService numberService, XmlNodeConfigProcessor xmlNodeConfigProcessor,
                           ValidationService validationService, DocumentVOProvider documentVOProvider,
                           TableOfContentProcessor tableOfContentProcessor, MessageHelper messageHelper,
                           XPathCatalog xPathCatalog) {
        this.explanatoryRepository = explanatoryRepository;
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
    public Explanatory createExplanatory(String templateId, String path, ExplanatoryMetadata metadata, String actionMessage, byte[] content) {
        LOG.trace("Creating Explanatory... [templateId={}, path={}, metadata={}]", templateId, path, metadata);
        final String explanatoryUid = Cuid.createCuid();
        final String ref = EXPLANATORY_NAME_PREFIX + explanatoryUid;
        final String fileName = ref + EXPLANATORY_DOC_EXTENSION;
        metadata = metadata.withRef(ref);
        Explanatory explanatory = explanatoryRepository.createExplanatory(templateId, path, fileName, metadata);
        byte[] updatedBytes = updateDataInXml((content == null) ? getContent(explanatory) : content, metadata);
        return explanatoryRepository.updateExplanatory(explanatory.getId(), metadata, updatedBytes, VersionType.MINOR, actionMessage);
    }

    @Override
    public Explanatory createExplanatoryFromContent(String path, ExplanatoryMetadata metadata, String actionMessage, byte[] content, String name) {
        LOG.trace("Creating Explanatory From Content... [path={}, metadata={}]", path, metadata);
        Explanatory explanatory = explanatoryRepository.createExplanatoryFromContent(path, name, metadata, content);
        return explanatoryRepository.updateExplanatory(explanatory.getId(), metadata, content, VersionType.MINOR, actionMessage);
    }

    @Override
    public void deleteExplanatory(Explanatory explanatory) {
        LOG.trace("Deleting Explanatory... [id={}]", explanatory.getId());
        explanatoryRepository.deleteExplanatory(explanatory.getId());
    }

    @Override
    public Explanatory findExplanatory(String id) {
        LOG.trace("Finding Explanatory... [id={}]", id);
        return explanatoryRepository.findExplanatoryById(id, true);
    }

    @Override
    @Cacheable(value = "docVersions")
    public Explanatory findExplanatoryVersion(String id) {
        LOG.trace("Finding Explanatory version... [it={}]", id);
        return explanatoryRepository.findExplanatoryById(id, false);
    }

    @Override
    public Explanatory updateExplanatory(Explanatory explanatory, byte[] updatedExplanatoryContent, VersionType versionType, String comment) {
        LOG.trace("Updating Explanatory Xml Content... [id={}]", explanatory.getId());

        explanatory = explanatoryRepository.updateExplanatory(explanatory.getId(), updatedExplanatoryContent, versionType, comment);

        //call validation on document with updated content
        validationService.validateDocumentAsync(documentVOProvider.createDocumentVO(explanatory, updatedExplanatoryContent));

        return explanatory;
    }

    @Override
    public Explanatory updateExplanatory(Explanatory explanatory, ExplanatoryMetadata updatedMetadata, VersionType versionType, String comment) {
        LOG.trace("Updating Explanatory... [id={}, updatedMetadata={}, versionType={}, comment={}]", explanatory.getId(), updatedMetadata, versionType, comment);
        Stopwatch stopwatch = Stopwatch.createStarted();
        byte[] updatedBytes = updateDataInXml(getContent(explanatory), updatedMetadata);

        explanatory = explanatoryRepository.updateExplanatory(explanatory.getId(), updatedMetadata, updatedBytes, versionType, comment);

        //call validation on document with updated content
        validationService.validateDocumentAsync(documentVOProvider.createDocumentVO(explanatory, updatedBytes));

        LOG.trace("Updated Explanatory ...({} milliseconds)", stopwatch.elapsed(TimeUnit.MILLISECONDS));
        return explanatory;
    }

    @Override
    public Explanatory updateExplanatory(Explanatory explanatory, byte[] updatedExplanatoryContent, ExplanatoryMetadata metadata, VersionType versionType, String comment) {
        LOG.trace("Updating Explanatory... [id={}, updatedMetadata={}, versionType={}, comment={}]", explanatory.getId(), metadata, versionType, comment);
        Stopwatch stopwatch = Stopwatch.createStarted();
        updatedExplanatoryContent = updateDataInXml(updatedExplanatoryContent, metadata);

        explanatory = explanatoryRepository.updateExplanatory(explanatory.getId(), metadata, updatedExplanatoryContent, versionType, comment);

        //call validation on document with updated content
        validationService.validateDocumentAsync(documentVOProvider.createDocumentVO(explanatory, updatedExplanatoryContent));

        LOG.trace("Updated Explanatory ...({} milliseconds)", stopwatch.elapsed(TimeUnit.MILLISECONDS));
        return explanatory;
    }

    @Override
    public Explanatory updateExplanatory(Explanatory explanatory, byte[] updatedExplanatoryContent, String comment) {
        LOG.trace("Updating Explanatory... [id={}, updatedMetadata={} , comment={}]", explanatory.getId(), updatedExplanatoryContent, comment);
        Stopwatch stopwatch = Stopwatch.createStarted();
        explanatory = explanatoryRepository.updateExplanatory(explanatory.getId(), updatedExplanatoryContent, VersionType.MINOR, comment);
        LOG.trace("Updated Explanatory ...({} milliseconds)", stopwatch.elapsed(TimeUnit.MILLISECONDS));
        return explanatory;
    }

    @Override
    public Explanatory updateExplanatoryWithMilestoneComments(Explanatory explanatory, List<String> milestoneComments, VersionType versionType, String comment){
        LOG.trace("Updating Explanatory... [id={}, milestoneComments={}, versionType={}, comment={}]", explanatory.getId(), milestoneComments, versionType, comment);
        final byte[] updatedBytes = getContent(explanatory);
        explanatory = explanatoryRepository.updateMilestoneComments(explanatory.getId(), milestoneComments, updatedBytes, versionType, comment);
        return explanatory;
    }

    @Override
    public Explanatory updateExplanatoryWithMilestoneComments(String explanatoryId, List<String> milestoneComments){
        LOG.trace("Updating Explanatory... [id={}, milestoneComments={}]", explanatoryId, milestoneComments);
        return explanatoryRepository.updateMilestoneComments(explanatoryId, milestoneComments);
    }

    @Override
    public List<Explanatory> findVersions(String id) {
        LOG.trace("Finding Explanatory versions... [id={}]", id);
        //LEOS-2813 We have memory issues is we fetch the content of all versions.
        return explanatoryRepository.findExplanatoryVersions(id,false);
    }

    @Override
    public Explanatory createVersion(String id, VersionType versionType, String comment) {
        LOG.trace("Creating Explanatory version... [id={}, versionType={}, comment={}]", id, versionType, comment);
        final Explanatory explanatory = findExplanatory(id);
        final ExplanatoryMetadata metadata = explanatory.getMetadata().getOrError(() -> "Explanatory metadata is required!");
        final Content content = explanatory.getContent().getOrError(() -> "Explanatory content is required!");
        final byte[] contentBytes = content.getSource().getBytes();
        return explanatoryRepository.updateExplanatory(id, metadata, contentBytes, versionType, comment);
    }

    @Override
    public List<TableOfContentItemVO> getTableOfContent(Explanatory explanatory, TocMode mode) {
        Validate.notNull(explanatory, "Explanatory is required");
        final Content content = explanatory.getContent().getOrError(() -> "Explanatory content is required!");
        final byte[] explanatoryContent = content.getSource().getBytes();
        return tableOfContentProcessor.buildTableOfContent(DOC, explanatoryContent, mode);
    }

    @Override
    public Explanatory saveTableOfContent(Explanatory explanatory, List<TableOfContentItemVO> tocList, ExplanatoryStructureType explanatoryStructureType, String actionMsg, User user) {
        Validate.notNull(explanatory, "Explanatory is required");
        Validate.notNull(tocList, "Table of content list is required");
        byte[] newXmlContent;

        newXmlContent = xmlContentProcessor.createDocumentContentWithNewTocList(tocList, getContent(explanatory), user);
        if (explanatoryStructureType != null && LEVEL.equals(explanatoryStructureType.getType())) {
            newXmlContent = numberService.renumberLevel(newXmlContent);
        }
        newXmlContent = numberService.renumberParagraph(newXmlContent);
        newXmlContent = numberService.renumberDivisions(newXmlContent);
        newXmlContent = xmlContentProcessor.doXMLPostProcessing(newXmlContent);

        return updateExplanatory(explanatory, newXmlContent, VersionType.MINOR, actionMsg);
    }

    private byte[] getContent(Explanatory explanatory) {
        final Content content = explanatory.getContent().getOrError(() -> "Explanatory content is required!");
        return content.getSource().getBytes();
    }

    private byte[] updateDataInXml(final byte[] content, ExplanatoryMetadata dataObject) {
        byte[] updatedBytes = xmlNodeProcessor.setValuesInXml(content, createValueMap(dataObject), xmlNodeConfigProcessor.getConfig(dataObject.getCategory()));
        return xmlContentProcessor.doXMLPostProcessing(updatedBytes);
    }

    @Override
    public Explanatory findExplanatoryByRef(String ref) {
        LOG.trace("Finding Explanatory by ref... [ref=" + ref + "]");
        return explanatoryRepository.findExplanatoryByRef(ref);
    }

    @Override
    public List<VersionVO> getAllVersions(String documentId, String docRef) {
        // TODO temporary call. paginated loading will be implemented in the future Story
        List<Explanatory> majorVersions = findAllMajors(docRef, 0, 9999);
        LOG.trace("Found {} majorVersions for [id={}]", majorVersions.size(), documentId);

        List<VersionVO> majorVersionsVO = VersionsUtil.buildVersionVO(majorVersions, messageHelper);
        return majorVersionsVO;
    }

    @Override
    public List<Explanatory> findAllMinorsForIntermediate(String docRef, String currIntVersion, int startIndex, int maxResults) {
        return explanatoryRepository.findAllMinorsForIntermediate(docRef, currIntVersion, startIndex, maxResults);
    }

    @Override
    public int findAllMinorsCountForIntermediate(String docRef, String currIntVersion) {
        return explanatoryRepository.findAllMinorsCountForIntermediate(docRef, currIntVersion);
    }

    @Override
    public Integer findAllMajorsCount(String docRef) {
        return explanatoryRepository.findAllMajorsCount(docRef);
    }

    @Override
    public List<Explanatory> findAllMajors(String docRef, int startIndex, int maxResults) {
        return explanatoryRepository.findAllMajors(docRef, startIndex, maxResults);
    }

    @Override
    public List<Explanatory> findRecentMinorVersions(String documentId, String documentRef, int startIndex, int maxResults) {
        return explanatoryRepository.findRecentMinorVersions(documentId, documentRef, startIndex, maxResults);
    }

    @Override
    public Integer findRecentMinorVersionsCount(String documentId, String documentRef) {
        return explanatoryRepository.findRecentMinorVersionsCount(documentId, documentRef);
    }

    @Override
    public List<String> getAncestorsIdsForElementId(Explanatory explanatory, List<String> elementIds) {
        Validate.notNull(explanatory, "Explanatory is required");
        Validate.notNull(elementIds, "Element id is required");
        List<String> ancestorIds = new ArrayList<String>();
        byte[] content = getContent(explanatory);
        for (String elementId : elementIds) {
            ancestorIds.addAll(xmlContentProcessor.getAncestorsIdsForElementId(content, elementId));
        }
        return ancestorIds;
    }

    @Override
    public Explanatory findFirstVersion(String documentRef) {
        return explanatoryRepository.findFirstVersion(documentRef);
    }

    @Override
    public List<Explanatory> findCouncilExplanatoryByPackagePath(String path) {
        return packageRepository.findDocumentsByPackagePath(path, Explanatory.class, true);
    }
    
    @Override
    public Explanatory updateExplanatory(String id, Map<String, Object> properties, boolean latest) {
    	LOG.trace("Updating Explanatory Xml Content... [id={}]", id);
    	Stopwatch stopwatch = Stopwatch.createStarted();
    	Explanatory explanatory = explanatoryRepository.updateExplanatory(id, properties, latest);
    	LOG.trace("Updated Explanatory ...({} milliseconds)", stopwatch.elapsed(TimeUnit.MILLISECONDS));
        return explanatory;
    }
    
    @Override
    public Explanatory findExplanatoryByVersion(String documentRef, String versionLabel) {
    	LOG.trace("Finding Explanatory by version... [ref=" + documentRef + " and version=" + versionLabel + "]");
        return explanatoryRepository.findExplanatoryByVersion(documentRef, versionLabel);
    }
}
