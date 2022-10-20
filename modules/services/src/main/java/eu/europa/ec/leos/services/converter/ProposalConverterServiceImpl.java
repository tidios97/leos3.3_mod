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

package eu.europa.ec.leos.services.converter;

import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.domain.vo.MetadataVO;
import eu.europa.ec.leos.services.document.DocumentContentService;
import eu.europa.ec.leos.services.export.ZipPackageUtil;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.processor.node.XmlNodeConfigProcessor;
import eu.europa.ec.leos.services.processor.node.XmlNodeProcessor;
import eu.europa.ec.leos.services.store.TemplateService;
import eu.europa.ec.leos.services.support.XPathCatalog;
import eu.europa.ec.leos.vo.catalog.CatalogItem;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static eu.europa.ec.leos.services.support.XmlHelper.PROPOSAL_FILE;
import static eu.europa.ec.leos.services.support.XmlHelper.XML_DOC_EXT;

public abstract class ProposalConverterServiceImpl implements ProposalConverterService {

    private static final Logger LOG = LoggerFactory.getLogger(ProposalConverterServiceImpl.class);

    private final XmlNodeProcessor xmlNodeProcessor;
    private final XmlNodeConfigProcessor xmlNodeConfigProcessor;
    protected final XmlContentProcessor xmlContentProcessor;
    private final TemplateService templateService;
    protected final XPathCatalog xPathCatalog;
    protected final DocumentContentService documentContentService;

    private List<CatalogItem> templatesCatalog;

    @Autowired
    ProposalConverterServiceImpl(
            XmlNodeProcessor xmlNodeProcessor,
            XmlNodeConfigProcessor xmlNodeConfigProcessor,
            XmlContentProcessor xmlContentProcessor,
            TemplateService templateService, XPathCatalog xPathCatalog,
            DocumentContentService documentContentService) {
        this.xmlNodeProcessor = xmlNodeProcessor;
        this.xmlNodeConfigProcessor = xmlNodeConfigProcessor;
        this.xmlContentProcessor = xmlContentProcessor;
        this.templateService = templateService;
        this.xPathCatalog = xPathCatalog;
        this.documentContentService = documentContentService;
    }

    /**
     * Creates a DocumentVO for the leg file passed as parameter.
     * After the computation the file will be deleted from the filesystem.
     *
     * The xml files inside the leg/zip file are mapped into array source[] of DocumentVO.
     * When canModifySource is true, some tags are not included in the source[] field, otherwise when is false the
     * array source contains the xml as it is in the zip/leg file.
     *
     * @param file leg file from where to create the DocumentVO.
     * @param proposal DocumentVO with the data of the proposal. The same object will be enriched and returned by the method
     * @param canModifySource true to exclude some xml tags into byte array source, false if you need to keep the original integrity of the document
     * @return the enriched DocumentVO representing the proposal inside the leg file.
     */
    public DocumentVO createProposalFromLegFile(File file, final DocumentVO proposal, boolean canModifySource) {
        proposal.clean();
        proposal.setCategory(LeosCategory.PROPOSAL);
        // unzip file
        Map<String, Object> unzippedFiles = ZipPackageUtil.unzipFiles(file, "/unzip/");
        try {
            templatesCatalog = templateService.getTemplatesCatalog();
            String proposalFileKey = unzippedFiles.keySet().stream().filter(x -> x.startsWith(PROPOSAL_FILE)).findFirst().get();
            if (unzippedFiles.containsKey(proposalFileKey)) {
                List<DocumentVO> propChildDocs = new ArrayList<>();
                File proposalFile = (File) unzippedFiles.get(proposalFileKey);
                updateSource(proposal, proposalFile, canModifySource);
                updateDocIdFromXml(proposal, LeosCategory.PROPOSAL, proposalFileKey);
                updateMetadataVO(proposal);
                List<DocumentVO> billChildDocs = new ArrayList<>();
                DocumentVO billDoc = null;
                HashMap<Integer, DocumentVO> annexes = new HashMap<>();
                for (String docName : unzippedFiles.keySet()) {
                    File docFile = (File) unzippedFiles.get(docName);
                    DocumentVO doc = createDocument(docName, docFile, canModifySource);
                    if (doc != null) {
                        if (doc.getCategory() == LeosCategory.ANNEX) {
                            annexes.put(new Integer(doc.getMetadata().getIndex()), doc);
                        } else if (doc.getCategory() == LeosCategory.MEDIA) {
                            billChildDocs.add(doc);
                        } else if (doc.getCategory() == LeosCategory.BILL) {
                            billDoc = doc;
                        } else {
                            propChildDocs.add(doc);
                        }
                    }
                }
                billChildDocs.addAll(annexes.values());
                if (billDoc != null) {
                    billDoc.setChildDocuments(billChildDocs);
                    propChildDocs.add(billDoc);
                }
                proposal.setChildDocuments(propChildDocs);
            }
        } catch (Exception e) {
            LOG.error("Error generating the map of the document: {}", e);
        } finally {
            deleteFiles(file, unzippedFiles);
        }
        return proposal;
    }

    private void updateDocIdFromXml(final DocumentVO documentVO, LeosCategory docCategory, String docName) {
        Map<String, String> metadataMap = xmlNodeProcessor.getValuesFromXml(documentVO.getSource(),
                new String[]{XmlNodeConfigProcessor.DOC_OBJECT_ID, XmlNodeConfigProcessor.DOC_REF_META},
                xmlNodeConfigProcessor.getConfig(docCategory));
        String docId = metadataMap.get(XmlNodeConfigProcessor.DOC_OBJECT_ID);
        String docRef = metadataMap.get(XmlNodeConfigProcessor.DOC_REF_META);
        documentVO.setId(docId != null ? docId : docName);
        documentVO.setRef(docRef != null ? docRef : docName);
    }

    private DocumentVO createDocument(String docName, File docFile, boolean canModifySource) {
        DocumentVO doc = null;
        try {
            if(docName.endsWith(XML_DOC_EXT)) {
                byte[] xmlBytes = Files.readAllBytes(docFile.toPath());
                LeosCategory category = xmlContentProcessor.identifyCategory(docName, xmlBytes);
                if (category != null) {
                    doc = new DocumentVO(category);
                    updateSource(doc, docFile, canModifySource);
                    updateDocIdFromXml(doc, category, docName);
                    updateMetadataVO(doc);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Unexpected error occurred while reading doc file", e);
        }
        return doc;
    }



    protected abstract void updateSource(final DocumentVO document, File documentFile, boolean canModifySource);

    private void updateMetadataVO(final DocumentVO document) {
        if (document.getSource() != null) {
            try {
                MetadataVO metadata = document.getMetadata();
                Map<String, String> metadataVOMap = xmlNodeProcessor.getValuesFromXml(document.getSource(), new String[]{
                        XmlNodeConfigProcessor.DOC_PURPOSE_META,
                        XmlNodeConfigProcessor.DOC_STAGE_META,
                        XmlNodeConfigProcessor.DOC_TYPE_META,
                        XmlNodeConfigProcessor.DOC_LANGUAGE,
                        XmlNodeConfigProcessor.DOC_SPECIFIC_TEMPLATE,
                        XmlNodeConfigProcessor.DOC_TEMPLATE,
                        XmlNodeConfigProcessor.DOC_EEA_RELEVANCE_COVER,
                        XmlNodeConfigProcessor.ANNEX_TITLE_META,
                        XmlNodeConfigProcessor.ANNEX_INDEX_META,
                        XmlNodeConfigProcessor.ANNEX_NUMBER_META,
                }, xmlNodeConfigProcessor.getConfig(document.getCategory()));

                metadata.setDocPurpose(metadataVOMap.get(XmlNodeConfigProcessor.DOC_PURPOSE_META));
                metadata.setDocStage(metadataVOMap.get(XmlNodeConfigProcessor.DOC_STAGE_META));
                metadata.setDocType(metadataVOMap.get(XmlNodeConfigProcessor.DOC_TYPE_META));
                metadata.setLanguage(metadataVOMap.get(XmlNodeConfigProcessor.DOC_LANGUAGE));
                metadata.setDocTemplate(metadataVOMap.get(XmlNodeConfigProcessor.DOC_SPECIFIC_TEMPLATE));
                metadata.setTemplate(metadataVOMap.get(XmlNodeConfigProcessor.DOC_TEMPLATE));
                metadata.setTitle(metadataVOMap.get(XmlNodeConfigProcessor.ANNEX_TITLE_META));
                metadata.setIndex(metadataVOMap.get(XmlNodeConfigProcessor.ANNEX_INDEX_META));
                metadata.setNumber(metadataVOMap.get(XmlNodeConfigProcessor.ANNEX_NUMBER_META));

                // For now, only check for the existence of an eeaRelevance: text-> boolean
                String eeaRelevanceText = metadataVOMap.get(XmlNodeConfigProcessor.DOC_EEA_RELEVANCE_COVER);
                metadata.setEeaRelevance(eeaRelevanceText != null && !eeaRelevanceText.isEmpty());

                // if the template doesnt exist in the system we don't continue, we won't import it.
                metadata.setTemplateName(templateService.getTemplateName(templatesCatalog, metadata.getDocTemplate(), metadata.getLanguage()));
            } catch (Exception e) {
                LOG.error("Error parsing metadata {}", e);
            }
        }
    }

    /**
     * Will delete form the temporary folder the files uploaded and the unzipped files + parent folder.
     * @param mainFile
     * @param unzippedFiles
     */
    private void deleteFiles(File mainFile, Map<String, Object> unzippedFiles) {
        mainFile.delete();
        List<String> parentFolders = new ArrayList<>();
        for (String docName : unzippedFiles.keySet()) {
            File unzippedFile = (File) unzippedFiles.get(docName);
            String parent = unzippedFile.getParent();
            if (!parentFolders.contains(parent)) {
                parentFolders.add(parent);
            }
            if (!unzippedFile.delete()) {
                LOG.info("File not deleted {}", unzippedFile.getPath());
            }
        }
        try {
            // we must clean also the folder.
            for (String parent : parentFolders) {
                FileUtils.deleteDirectory(new File(parent));
            }
        } catch (IOException e) {
            LOG.error("Error deleting the folder {}", e);
        }
    }
}
