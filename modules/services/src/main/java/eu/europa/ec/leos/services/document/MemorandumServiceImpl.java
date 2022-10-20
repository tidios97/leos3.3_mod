/*
 * Copyright 2017 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.europa.ec.leos.services.document;

import com.google.common.base.Stopwatch;
import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.document.Memorandum;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.domain.cmis.metadata.MemorandumMetadata;
import eu.europa.ec.leos.domain.common.TocMode;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.action.VersionVO;
import eu.europa.ec.leos.repository.document.MemorandumRepository;
import eu.europa.ec.leos.repository.store.PackageRepository;
import eu.europa.ec.leos.services.document.util.DocumentVOProvider;
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

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static eu.europa.ec.leos.services.processor.node.XmlNodeConfigProcessor.createValueMap;
import static eu.europa.ec.leos.services.support.XmlHelper.DOC;
import static eu.europa.ec.leos.services.support.XmlHelper.XML_DOC_EXT;

public abstract class MemorandumServiceImpl implements MemorandumService {

    private static final Logger LOG = LoggerFactory.getLogger(MemorandumServiceImpl.class);

    protected final MemorandumRepository memorandumRepository;
    protected final PackageRepository packageRepository;
    protected final XmlNodeProcessor xmlNodeProcessor;
    protected final XmlContentProcessor xmlContentProcessor;
    protected final XmlNodeConfigProcessor xmlNodeConfigProcessor;
    protected final TableOfContentProcessor tableOfContentProcessor;

    protected final ValidationService validationService;
    protected final DocumentVOProvider documentVOProvider;
    protected final MessageHelper messageHelper;
    protected final XPathCatalog xPathCatalog;

    @Autowired
    MemorandumServiceImpl(MemorandumRepository memorandumRepository,
                          PackageRepository packageRepository,
                          XmlNodeProcessor xmlNodeProcessor,
                          XmlContentProcessor xmlContentProcessor,
                          XmlNodeConfigProcessor xmlNodeConfigProcessor, ValidationService validationService,
                          DocumentVOProvider documentVOProvider, TableOfContentProcessor tableOfContentProcessor,
                          MessageHelper messageHelper, XPathCatalog xPathCatalog) {
        this.memorandumRepository = memorandumRepository;
        this.packageRepository = packageRepository;
        this.xmlNodeProcessor = xmlNodeProcessor;
        this.xmlContentProcessor = xmlContentProcessor;
        this.xmlNodeConfigProcessor = xmlNodeConfigProcessor;
        this.validationService = validationService;
        this.documentVOProvider = documentVOProvider;
        this.tableOfContentProcessor = tableOfContentProcessor;
        this.messageHelper = messageHelper;
        this.xPathCatalog = xPathCatalog;
    }

    @Override
    public Memorandum findMemorandum(String id, boolean latest) {
        LOG.trace("Finding Memorandum... [id={}]", id);
        return memorandumRepository.findMemorandumById(id, latest);
    }

    @Override
    @Cacheable(value = "docVersions")
    public Memorandum findMemorandumVersion(String id) {
        LOG.trace("Finding Memorandum version... [id={}]", id);
        return memorandumRepository.findMemorandumById(id, false);
    }
    
    @Override
    public Memorandum findMemorandumByPackagePath(String path) {
        LOG.trace("Finding Memorandum by package path... [path={}]", path);
        // FIXME can be improved, now we don't fetch ALL docs because it's loaded later the one needed, 
        // this can be improved adding a page of 1 item or changing the method/query.
        List<Memorandum> docs = packageRepository.findDocumentsByPackagePath(path, Memorandum.class, false);
        if (!docs.isEmpty()) {
            return findMemorandum(docs.get(0).getId(), true);
        } else {
            return null;
        }
    }

    @Override
    public Memorandum updateMemorandum(Memorandum memorandum, byte[] updatedMemorandumContent, VersionType versionType, String comment) {
        LOG.trace("Updating Memorandum Xml Content... [id={}]", memorandum.getId());
        
        memorandum = memorandumRepository.updateMemorandum(memorandum.getId(), updatedMemorandumContent, versionType, comment);
        //call validation on document with updated content
        validationService.validateDocumentAsync(documentVOProvider.createDocumentVO(memorandum, updatedMemorandumContent));
        
        return memorandum;
    }

    @Override
    public Memorandum updateMemorandum(String memorandumId, MemorandumMetadata updatedMetadata) {
        LOG.trace("Updating Memorandum Xml Content... [id={}]", memorandumId);
        return memorandumRepository.updateMemorandum(memorandumId, updatedMetadata);
    }

    @Override
    public Memorandum updateMemorandum(String memorandumId, Map<String, Object> properties, boolean latest) {
        LOG.trace("Updating Memorandum metadata properties...");
        return memorandumRepository.updateMemorandum(memorandumId, properties, latest);
    }

    @Override
    public Memorandum updateMemorandum(Memorandum memorandum, byte[] updatedMemorandumContent, String comment) {
        LOG.trace("Updating Memorandum Xml Content... [id={}]", memorandum.getId());
        memorandum = memorandumRepository.updateMemorandum(memorandum.getId(), updatedMemorandumContent, VersionType.MINOR, comment);
        return memorandum;
    }

    @Override
    public Memorandum updateMemorandum(Memorandum memorandum, MemorandumMetadata updatedMetadata, VersionType versionType, String comment) {
        LOG.trace("Updating Memorandum... [id={}, metadata={}]", memorandum.getId(), updatedMetadata);
        Stopwatch stopwatch = Stopwatch.createStarted();
        byte[] updatedBytes = updateDataInXml(getContent(memorandum), updatedMetadata); //FIXME: Do we need latest data again??
        
        memorandum = memorandumRepository.updateMemorandum(memorandum.getId(), updatedMetadata, updatedBytes, versionType, comment);
        
        //call validation on document with updated content
        validationService.validateDocumentAsync(documentVOProvider.createDocumentVO(memorandum, updatedBytes));
        
        LOG.trace("Updated Memorandum ...({} milliseconds)", stopwatch.elapsed(TimeUnit.MILLISECONDS));
        return memorandum;
    }

    @Override
    public Memorandum updateMemorandumWithMilestoneComments(Memorandum memorandum, List<String> milestoneComments, VersionType versionType, String comment){
        LOG.trace("Updating Memorandum... [id={}, milestoneComments={}, versionType={}, comment={}]", memorandum.getId(), milestoneComments, versionType, comment);
        final byte[] updatedBytes = getContent(memorandum);
        memorandum = memorandumRepository.updateMilestoneComments(memorandum.getId(), milestoneComments, updatedBytes, versionType, comment);
        return memorandum;
    }

    @Override
    public Memorandum updateMemorandumWithMilestoneComments(String memorandumId, List<String> milestoneComments){
        LOG.trace("Updating Memorandum... [id={}, milestoneComments={}]", memorandumId, milestoneComments);
        return memorandumRepository.updateMilestoneComments(memorandumId, milestoneComments);
    }

    @Override
    public List<TableOfContentItemVO> getTableOfContent(Memorandum memorandum, TocMode mode) {
        Validate.notNull(memorandum, "Memorandum is required");
        final Content content = memorandum.getContent().getOrError(() -> "Memorandum content is required!");
        final byte[] memorandumContent = content.getSource().getBytes();
        return tableOfContentProcessor.buildTableOfContent(DOC, memorandumContent, mode);
    }

    protected byte[] updateDataInXml(final byte[] content, MemorandumMetadata dataObject) {
        byte[] updatedBytes = xmlNodeProcessor.setValuesInXml(content, createValueMap(dataObject), xmlNodeConfigProcessor.getConfig(dataObject.getCategory()));
        return xmlContentProcessor.doXMLPostProcessing(updatedBytes);
    }

    @Override
    public List<Memorandum> findVersions(String id) {
        LOG.trace("Finding Memorandum versions... [id={}]", id);
        //LEOS-2813 We have memory issues is we fetch the content of all versions.
        return memorandumRepository.findMemorandumVersions(id, false);
    }

    @Override
    public Memorandum createVersion(String id, VersionType versionType, String comment) {
        LOG.trace("Creating Memorandum version... [id={}, versionType={}, comment={}]", id, versionType, comment);
        final Memorandum memorandum = findMemorandum(id, true);
        final MemorandumMetadata metadata = memorandum.getMetadata().getOrError(() -> "Memorandum metadata is required!");
        final Content content = memorandum.getContent().getOrError(() -> "Memorandum content is required!");
        final byte[] contentBytes = content.getSource().getBytes();
        return memorandumRepository.updateMemorandum(id, metadata, contentBytes, versionType, comment);
    }

    protected byte[] getContent(Memorandum memorandum) {
        final Content content = memorandum.getContent().getOrError(() -> "Memorandum content is required!");
        return content.getSource().getBytes();
    }

    @Override
    public Memorandum findMemorandumByRef(String ref) {
        LOG.trace("Finding Memorandum by ref... [ref=" + ref + "]");
        return memorandumRepository.findMemorandumByRef(ref);
    }
    
    @Override
    public List<VersionVO> getAllVersions(String documentId, String docRef) {
        // TODO temporary call. paginated loading will be implemented in the future Story
        List<Memorandum> majorVersions = findAllMajors(docRef, 0, 9999);
        LOG.trace("Found {} majorVersions for [id={}]", majorVersions.size(), documentId);
    
        List<VersionVO> majorVersionsVO = VersionsUtil.buildVersionVO(majorVersions, messageHelper);
        return majorVersionsVO;
    }
    
    @Override
    public List<Memorandum> findAllMinorsForIntermediate(String docRef, String currIntVersion, int startIndex, int maxResults) {
        return memorandumRepository.findAllMinorsForIntermediate(docRef, currIntVersion, startIndex, maxResults);
    }
    
    @Override
    public int findAllMinorsCountForIntermediate(String docRef, String currIntVersion) {
        return memorandumRepository.findAllMinorsCountForIntermediate(docRef, currIntVersion);
    }
    
    @Override
    public Integer findAllMajorsCount(String docRef) {
        return memorandumRepository.findAllMajorsCount(docRef);
    }
    
    @Override
    public List<Memorandum> findAllMajors(String docRef, int startIndex, int maxResults) {
        return memorandumRepository.findAllMajors(docRef, startIndex, maxResults);
    }
    
    @Override
    public List<Memorandum> findRecentMinorVersions(String documentId, String documentRef, int startIndex, int maxResults) {
        return memorandumRepository.findRecentMinorVersions(documentId, documentRef, startIndex, maxResults);
    }
    
    @Override
    public Integer findRecentMinorVersionsCount(String documentId, String documentRef) {
        return memorandumRepository.findRecentMinorVersionsCount(documentId, documentRef);
    }

    @Override
    public XmlDocument findFirstVersion(String ref) {
        return memorandumRepository.findFirstVersion(ref);
    }

    @Override
    public Memorandum createMemorandum(String templateId, String path, MemorandumMetadata metadata, String actionMsg, byte[] content) {
        LOG.trace("Creating Memorandum... [templateId={}, path={}, metadata={}]", templateId, path, metadata);
        final String ref = generateMemorandumReference(templateId, content, metadata.getLanguage());
        metadata = metadata.withRef(ref);
        Memorandum memorandum = memorandumRepository.createMemorandum(templateId, path, ref + XML_DOC_EXT, metadata);
        LOG.info("Created Memorandum ref {} in path {}", ref, path);
        byte[] updatedBytes = updateDataInXml((content == null) ? getContent(memorandum) : content, metadata);
        return memorandumRepository.updateMemorandum(memorandum.getId(), metadata, updatedBytes, VersionType.MINOR, actionMsg);
    }

    @Override
    public Memorandum createMemorandumFromContent(String path, MemorandumMetadata metadata, String actionMsg, byte[] content, String name) {
        LOG.trace("Creating Memorandum... [ path={}, metadata={}]", path, metadata);
        Memorandum memorandum = memorandumRepository.createMemorandumFromContent(path, name, metadata, content);
        return memorandumRepository.updateMemorandum(memorandum.getId(), metadata, content, VersionType.MINOR, actionMsg);
    }
}
