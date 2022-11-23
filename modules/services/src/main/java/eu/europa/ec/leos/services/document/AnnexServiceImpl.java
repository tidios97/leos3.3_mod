/*
 * Copyright 2019 European Commission
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
import eu.europa.ec.leos.domain.cmis.document.Annex;
import eu.europa.ec.leos.domain.cmis.metadata.AnnexMetadata;
import eu.europa.ec.leos.domain.common.TocMode;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.action.VersionVO;
import eu.europa.ec.leos.model.annex.AnnexStructureType;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.repository.document.AnnexRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static eu.europa.ec.leos.services.processor.node.XmlNodeConfigProcessor.createValueMap;
import static eu.europa.ec.leos.services.support.XmlHelper.DOC;
import static eu.europa.ec.leos.services.support.XmlHelper.XML_DOC_EXT;

public abstract class AnnexServiceImpl implements AnnexService {

    private static final Logger LOG = LoggerFactory.getLogger(AnnexServiceImpl.class);

    protected final AnnexRepository annexRepository;
    protected final XmlNodeProcessor xmlNodeProcessor;
    protected final XmlContentProcessor xmlContentProcessor;
    protected final NumberService numberService;
    protected final XmlNodeConfigProcessor xmlNodeConfigProcessor;
    protected final DocumentVOProvider documentVOProvider;
    protected final ValidationService validationService;
    protected final MessageHelper messageHelper;
    protected final XPathCatalog xPathCatalog;
    protected final TableOfContentProcessor tableOfContentProcessor;

    @Autowired
    AnnexServiceImpl(AnnexRepository annexRepository, XmlNodeProcessor xmlNodeProcessor,
                     XmlContentProcessor xmlContentProcessor, NumberService numberService, XmlNodeConfigProcessor xmlNodeConfigProcessor,
                     ValidationService validationService, DocumentVOProvider documentVOProvider, TableOfContentProcessor tableOfContentProcessor,
                     MessageHelper messageHelper, XPathCatalog xPathCatalog) {
        this.annexRepository = annexRepository;
        this.xmlNodeProcessor = xmlNodeProcessor;
        this.xmlContentProcessor = xmlContentProcessor;
        this.numberService = numberService;
        this.xmlNodeConfigProcessor = xmlNodeConfigProcessor;
        this.validationService = validationService;
        this.documentVOProvider = documentVOProvider;
        this.messageHelper = messageHelper;
        this.xPathCatalog = xPathCatalog;
        this.tableOfContentProcessor = tableOfContentProcessor;
    }

    @Override
    public void deleteAnnex(Annex annex) {
        LOG.trace("Deleting Annex... [id={}]", annex.getId());
        annexRepository.deleteAnnex(annex.getId());
    }

    @Override
    public Annex findAnnex(String id, boolean latest) {
        LOG.trace("Finding Annex... [id={}]", id);
        return annexRepository.findAnnexById(id, latest);
    }

    @Override
    @Cacheable(value = "docVersions")
    public Annex findAnnexVersion(String id) {
        LOG.trace("Finding Annex version... [it={}]", id);
        return annexRepository.findAnnexById(id, false);
    }

    @Override
    public Annex updateAnnex(Annex annex, byte[] updatedAnnexContent, VersionType versionType, String comment) {
        LOG.trace("Updating Annex Xml Content... [id={}]", annex.getId());

        annex = annexRepository.updateAnnex(annex.getId(), updatedAnnexContent, versionType, comment);

        //call validation on document with updated content
        validationService.validateDocumentAsync(documentVOProvider.createDocumentVO(annex, updatedAnnexContent));

        return annex;
    }

    @Override
    public Annex updateAnnex(Annex annex, AnnexMetadata updatedMetadata, VersionType versionType, String comment) {
        LOG.trace("Updating Annex... [id={}, updatedMetadata={}, versionType={}, comment={}]", annex.getId(), updatedMetadata, versionType, comment);
        Stopwatch stopwatch = Stopwatch.createStarted();
        byte[] updatedBytes = updateDataInXml(getContent(annex), updatedMetadata);

        annex = annexRepository.updateAnnex(annex.getId(), updatedMetadata, updatedBytes, versionType, comment);

        //call validation on document with updated content
        validationService.validateDocumentAsync(documentVOProvider.createDocumentVO(annex, updatedBytes));

        LOG.trace("Updated Annex ...({} milliseconds)", stopwatch.elapsed(TimeUnit.MILLISECONDS));
        return annex;
    }

    @Override
    public Annex updateAnnex(Annex annex, byte[] updatedAnnexContent, AnnexMetadata metadata, VersionType versionType, String comment) {
        LOG.trace("Updating Annex... [id={}, updatedMetadata={}, versionType={}, comment={}]", annex.getId(), metadata, versionType, comment);
        Stopwatch stopwatch = Stopwatch.createStarted();
        updatedAnnexContent = updateDataInXml(updatedAnnexContent, metadata);

        annex = annexRepository.updateAnnex(annex.getId(), metadata, updatedAnnexContent, versionType, comment);

        //call validation on document with updated content
        validationService.validateDocumentAsync(documentVOProvider.createDocumentVO(annex, updatedAnnexContent));

        LOG.trace("Updated Annex ...({} milliseconds)", stopwatch.elapsed(TimeUnit.MILLISECONDS));
        return annex;
    }

    @Override
    public Annex updateAnnex(Annex annex, byte[] updatedAnnexContent, String comment) {
        LOG.trace("Updating Annex... [id={}, updatedMetadata={} , comment={}]", annex.getId(), updatedAnnexContent, comment);
        Stopwatch stopwatch = Stopwatch.createStarted();
        annex = annexRepository.updateAnnex(annex.getId(), updatedAnnexContent, VersionType.MINOR, comment);
        LOG.trace("Updated Annex ...({} milliseconds)", stopwatch.elapsed(TimeUnit.MILLISECONDS));
        return annex;
    }

    @Override
    public Annex updateAnnex(String id, Map<String, Object> properties, boolean latest) {
        LOG.trace("Updating Annex metadata properties... [id={}]", id);
        return annexRepository.updateAnnex(id, properties, latest);
    }

    @Override
    public Annex updateAnnexWithMilestoneComments(Annex annex, List<String> milestoneComments, VersionType versionType, String comment){
        LOG.trace("Updating Annex... [id={}, milestoneComments={}, versionType={}, comment={}]", annex.getId(), milestoneComments, versionType, comment);
        final byte[] updatedBytes = getContent(annex);
        annex = annexRepository.updateMilestoneComments(annex.getId(), milestoneComments, updatedBytes, versionType, comment);
        return annex;
    }

    @Override
    public Annex updateAnnexWithMilestoneComments(String annexId, List<String> milestoneComments){
        LOG.trace("Updating Annex... [id={}, milestoneComments={}]", annexId, milestoneComments);
        return annexRepository.updateMilestoneComments(annexId, milestoneComments);
    }

    @Override
    public List<Annex> findVersions(String id) {
        LOG.trace("Finding Annex versions... [id={}]", id);
        //LEOS-2813 We have memory issues is we fetch the content of all versions.
        return annexRepository.findAnnexVersions(id,false);
    }

    @Override
    public Annex createVersion(String id, VersionType versionType, String comment) {
        LOG.trace("Creating Annex version... [id={}, versionType={}, comment={}]", id, versionType, comment);
        final Annex annex = findAnnex(id, true);
        final AnnexMetadata metadata = annex.getMetadata().getOrError(() -> "Annex metadata is required!");
        final Content content = annex.getContent().getOrError(() -> "Annex content is required!");
        byte[] contentBytes = content.getSource().getBytes();
        return annexRepository.updateAnnex(id, metadata, contentBytes, versionType, comment);
    }

    @Override
    public List<TableOfContentItemVO> getTableOfContent(Annex annex, TocMode mode) {
        Validate.notNull(annex, "Annex is required");
        final Content content = annex.getContent().getOrError(() -> "Annex content is required!");
        final byte[] annexContent = content.getSource().getBytes();
        return tableOfContentProcessor.buildTableOfContent(DOC, annexContent, mode);
    }

    @Override
    public Annex saveTableOfContent(Annex annex, List<TableOfContentItemVO> tocList, AnnexStructureType structureType, String actionMsg, User user) {
        Validate.notNull(annex, "Annex is required");
        Validate.notNull(tocList, "Table of content list is required");
        byte[] newXmlContent;

        newXmlContent = xmlContentProcessor.createDocumentContentWithNewTocList(tocList, getContent(annex), user);
        switch(structureType) {
            case ARTICLE:
                newXmlContent = numberService.renumberArticles(newXmlContent, true);
                break;
            case LEVEL:
                newXmlContent = numberService.renumberLevel(newXmlContent);
                newXmlContent = numberService.renumberParagraph(newXmlContent);
                break;
        }
        newXmlContent = xmlContentProcessor.doXMLPostProcessing(newXmlContent);

        return updateAnnex(annex, newXmlContent, VersionType.MINOR, actionMsg);
    }

    protected byte[] getContent(Annex annex) {
        final Content content = annex.getContent().getOrError(() -> "Annex content is required!");
        return content.getSource().getBytes();
    }

    protected byte[] updateDataInXml(final byte[] content, AnnexMetadata dataObject) {
        byte[] updatedBytes = xmlNodeProcessor.setValuesInXml(content, createValueMap(dataObject), xmlNodeConfigProcessor.getConfig(dataObject.getCategory()),
                xmlNodeConfigProcessor.getOldPrefaceOfAnnexConfig());
        return xmlContentProcessor.doXMLPostProcessing(updatedBytes);
    }

    @Override
    public Annex findAnnexByRef(String ref) {
        LOG.trace("Finding Annex by ref... [ref=" + ref + "]");
        return annexRepository.findAnnexByRef(ref);
    }

    @Override
    public List<VersionVO> getAllVersions(String documentId, String docRef) {
        // TODO temporary call. paginated loading will be implemented in the future Story
        List<Annex> majorVersions = findAllMajors(docRef, 0, 9999);
        LOG.trace("Found {} majorVersions for [id={}]", majorVersions.size(), documentId);

        List<VersionVO> majorVersionsVO = VersionsUtil.buildVersionVO(majorVersions, messageHelper);
        return majorVersionsVO;
    }

    @Override
    public List<Annex> findAllMinorsForIntermediate(String docRef, String currIntVersion, int startIndex, int maxResults) {
        return annexRepository.findAllMinorsForIntermediate(docRef, currIntVersion, startIndex, maxResults);
    }

    @Override
    public int findAllMinorsCountForIntermediate(String docRef, String currIntVersion) {
        return annexRepository.findAllMinorsCountForIntermediate(docRef, currIntVersion);
    }

    @Override
    public Integer findAllMajorsCount(String docRef) {
        return annexRepository.findAllMajorsCount(docRef);
    }

    @Override
    public List<Annex> findAllMajors(String docRef, int startIndex, int maxResults) {
        return annexRepository.findAllMajors(docRef, startIndex, maxResults);
    }

    @Override
    public List<Annex> findRecentMinorVersions(String documentId, String documentRef, int startIndex, int maxResults) {
        return annexRepository.findRecentMinorVersions(documentId, documentRef, startIndex, maxResults);
    }

    @Override
    public Integer findRecentMinorVersionsCount(String documentId, String documentRef) {
        return annexRepository.findRecentMinorVersionsCount(documentId, documentRef);
    }

    @Override
    public List<String> getAncestorsIdsForElementId(Annex annex, List<String> elementIds) {
        Validate.notNull(annex, "Annex is required");
        Validate.notNull(elementIds, "Element id is required");
        List<String> ancestorIds = new ArrayList<String>();
        byte[] content = getContent(annex);
        for (String elementId : elementIds) {
            ancestorIds.addAll(xmlContentProcessor.getAncestorsIdsForElementId(content, elementId));
        }
        return ancestorIds;
    }

    @Override
    public Annex findFirstVersion(String documentRef) {
        return annexRepository.findFirstVersion(documentRef);
    }

    @Override
    public Annex createAnnex(String templateId, String path, AnnexMetadata metadata, String actionMessage, byte[] content) {
        LOG.trace("Creating Annex... [templateId={}, path={}, metadata={}]", templateId, path, metadata);
        String ref = generateAnnexReference(templateId, content, metadata.getLanguage());
        metadata = metadata.withRef(ref);
        Annex annex = annexRepository.createAnnex(templateId, path, ref + XML_DOC_EXT, metadata);
        LOG.info("Created Annex with ref '{}' in path {}", ref, path);
        byte[] updatedBytes = updateDataInXml((content == null) ? getContent(annex) : content, metadata);
        return annexRepository.updateAnnex(annex.getId(), metadata, updatedBytes, VersionType.MINOR, actionMessage);
    }

    @Override
    public Annex createAnnexFromContent(String path, AnnexMetadata metadata, String actionMessage, byte[] content, String name) {
        LOG.trace("Creating Annex From Content... [path={}, metadata={}]", path, metadata);
        Annex annex = annexRepository.createAnnexFromContent(path, name, metadata, content);
        return annexRepository.updateAnnex(annex.getId(), metadata, content, VersionType.MINOR, actionMessage);
    }
}
