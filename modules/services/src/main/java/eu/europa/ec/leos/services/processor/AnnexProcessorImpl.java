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
package eu.europa.ec.leos.services.processor;

import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.document.Annex;
import eu.europa.ec.leos.domain.common.TocMode;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.annex.AnnexStructureType;
import eu.europa.ec.leos.model.annex.LevelItemVO;
import eu.europa.ec.leos.model.xml.Element;
import eu.europa.ec.leos.services.numbering.NumberService;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.support.XmlHelper;
import eu.europa.ec.leos.services.processor.content.TableOfContentProcessor;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.vo.toc.NumberingConfig;
import eu.europa.ec.leos.vo.toc.StructureConfigUtils;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItem;
import io.atlassian.fugue.Pair;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Provider;
import java.util.Arrays;
import java.util.List;

import static eu.europa.ec.leos.services.support.XmlHelper.ARTICLE;
import static eu.europa.ec.leos.services.support.XmlHelper.CONTENT;
import static eu.europa.ec.leos.services.support.XmlHelper.DOC;
import static eu.europa.ec.leos.services.support.XmlHelper.INDENT;
import static eu.europa.ec.leos.services.support.XmlHelper.LEVEL;
import static eu.europa.ec.leos.services.support.XmlHelper.NUM;
import static eu.europa.ec.leos.services.support.XmlHelper.PARAGRAPH;
import static eu.europa.ec.leos.services.support.XmlHelper.POINT;
import static eu.europa.ec.leos.services.support.XmlHelper.SUBPARAGRAPH;
import static eu.europa.ec.leos.services.support.XmlHelper.SUBPOINT;
import static eu.europa.ec.leos.vo.toc.StructureConfigUtils.getNumberingConfigByTagName;

@Service
class AnnexProcessorImpl implements AnnexProcessor {

    private XmlContentProcessor xmlContentProcessor;
    protected NumberService numberService;
    private final ElementProcessor<Annex> elementProcessor;
    protected final TableOfContentProcessor tableOfContentProcessor;
    private MessageHelper messageHelper;
    private Provider<StructureContext> structureContextProvider;

    @Autowired
    public AnnexProcessorImpl(XmlContentProcessor xmlContentProcessor, NumberService numberService, ElementProcessor<Annex> elementProcessor,
                              MessageHelper messageHelper, Provider<StructureContext> structureContextProvider, TableOfContentProcessor tableOfContentProcessor) {
        super();
        this.xmlContentProcessor = xmlContentProcessor;
        this.numberService = numberService;
        this.elementProcessor = elementProcessor;
        this.messageHelper = messageHelper;
        this.structureContextProvider = structureContextProvider;
        this.tableOfContentProcessor = tableOfContentProcessor;
    }
    
    @Override
    public byte[] deleteAnnexBlock(Annex document, String elementId, String tagName) throws Exception {
        Validate.notNull(document, "Document is required.");
        Validate.notNull(elementId, "Element id is required.");
        
        byte[] xmlContent = elementProcessor.deleteElement(document, elementId, tagName);
        return updateAnnexContent(elementId, tagName, xmlContent);
    }
    
    @Override
    public byte[] insertAnnexBlock(Annex document, String elementId, String tagName, boolean before) {
        Validate.notNull(document, "Document is required.");
        Validate.notNull(elementId, "Element id is required.");

        final Element parentElement;
        String template;
        byte[] updatedContent;
        List<TocItem> items = structureContextProvider.get().getTocItems();

        switch (tagName) {
            case LEVEL:
                template = XmlHelper.getTemplate(StructureConfigUtils.getTocItemByNameOrThrow(items, LEVEL), StructureConfigUtils.HASH_NUM_VALUE, messageHelper);
                template = addDocTypeToTemplateXmlId(template);
                updatedContent = xmlContentProcessor.insertElementByTagNameAndId(getContent(document), template, tagName, elementId, before);
                updatedContent = xmlContentProcessor.insertDepthAttribute(updatedContent, tagName, elementId);
                updatedContent = numberService.renumberLevel(updatedContent);
                break;
            case ARTICLE:
                template = XmlHelper.getTemplate(StructureConfigUtils.getTocItemByNameOrThrow(items, ARTICLE), StructureConfigUtils.HASH_NUM_VALUE, "Article heading...", messageHelper);
                template = addDocTypeToTemplateXmlId(template);
                updatedContent = xmlContentProcessor.insertElementByTagNameAndId(getContent(document), template, tagName, elementId, before);
                updatedContent = numberService.renumberArticles(updatedContent);
                break;
            case PARAGRAPH:
                template = XmlHelper.getTemplate(StructureConfigUtils.getTocItemByNameOrThrow(items, tagName), messageHelper);
                template = addDocTypeToTemplateXmlId(template);
                updatedContent = xmlContentProcessor.insertElementByTagNameAndId(getContent(document), template, tagName, elementId, before);
                break;
            case SUBPARAGRAPH:
                parentElement = xmlContentProcessor.getParentElement(getContent(document), elementId);
                if (isFirstSubParagraph(document, parentElement.getElementId(), parentElement.getElementTagName(), elementId)) {
                    updatedContent = insertAnnexBlock(document, parentElement.getElementId(), parentElement.getElementTagName(), before);
                } else if (!PARAGRAPH.equals(parentElement.getElementTagName())) {
                    template = XmlHelper.getTemplate(StructureConfigUtils.getTocItemByNameOrThrow(items, SUBPARAGRAPH), messageHelper);
                    template = addDocTypeToTemplateXmlId(template);
                    updatedContent = xmlContentProcessor.insertElementByTagNameAndId(getContent(document), template, tagName, elementId, before);
                } else {
                    throw new UnsupportedOperationException("Unsupported operation for tag: " + tagName);
                }
                break;
            case POINT:
            case INDENT:
                template = XmlHelper.getTemplate(StructureConfigUtils.getTocItemByNameOrThrow(items, tagName), StructureConfigUtils.HASH_NUM_VALUE, messageHelper);
                template = addDocTypeToTemplateXmlId(template);
                updatedContent = xmlContentProcessor.insertElementByTagNameAndId(getContent(document), template, tagName, elementId, before);
                updatedContent = xmlContentProcessor.insertAffectedAttributeIntoParentElements(updatedContent, elementId);
                updatedContent = numberService.renumberLevel(updatedContent);
                updatedContent = numberService.renumberParagraph(updatedContent);
                break;
            case CONTENT:
            case SUBPOINT:
                parentElement = xmlContentProcessor.getParentElement(getContent(document), elementId);
                updatedContent = insertAnnexBlock(document, parentElement.getElementId(), parentElement.getElementTagName(), before);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported operation for tag: " + tagName);
        }

        updatedContent = xmlContentProcessor.doXMLPostProcessing(updatedContent);
        return updatedContent;
    }

    @Override
    public byte[] insertAnnexBlockWithElementContent(Annex document, String elementId, String tagName, boolean before, String elementContent) {
        Validate.notNull(document, "Document is required.");
        Validate.notNull(elementId, "Element id is required.");

        final Element parentElement;
        byte[] updatedContent;
        List<TocItem> items = structureContextProvider.get().getTocItems();

        switch (tagName) {
            case LEVEL:
                updatedContent = xmlContentProcessor.insertElementByTagNameAndId(getContent(document), elementContent, tagName, elementId, before);
                updatedContent = xmlContentProcessor.insertDepthAttribute(updatedContent, tagName, elementId);
                updatedContent = numberService.renumberLevel(updatedContent);
                break;
            case ARTICLE:
                updatedContent = xmlContentProcessor.insertElementByTagNameAndId(getContent(document), elementContent, tagName, elementId, before);
                updatedContent = numberService.renumberArticles(updatedContent);
                break;
            case PARAGRAPH:
                updatedContent = xmlContentProcessor.insertElementByTagNameAndId(getContent(document), elementContent, tagName, elementId, before);
                break;
            case SUBPARAGRAPH:
                parentElement = xmlContentProcessor.getParentElement(getContent(document), elementId);
                if (isFirstSubParagraph(document, parentElement.getElementId(), parentElement.getElementTagName(), elementId)) {
                    updatedContent = insertAnnexBlock(document, parentElement.getElementId(), parentElement.getElementTagName(), before);
                } else if (!PARAGRAPH.equals(parentElement.getElementTagName())) {
                    updatedContent = xmlContentProcessor.insertElementByTagNameAndId(getContent(document), elementContent, tagName, elementId, before);
                } else {
                    throw new UnsupportedOperationException("Unsupported operation for tag: " + tagName);
                }
                break;
            case POINT:
            case INDENT:
                updatedContent = xmlContentProcessor.insertElementByTagNameAndId(getContent(document), elementContent, tagName, elementId, before);
                updatedContent = xmlContentProcessor.insertAffectedAttributeIntoParentElements(updatedContent, elementId);
                updatedContent = numberService.renumberLevel(updatedContent);
                updatedContent = numberService.renumberParagraph(updatedContent);
                break;
            case CONTENT:
            case SUBPOINT:
                parentElement = xmlContentProcessor.getParentElement(getContent(document), elementId);
                updatedContent = insertAnnexBlockWithElementContent(document, parentElement.getElementId(), parentElement.getElementTagName(), before, elementContent);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported operation for tag: " + tagName);
        }

        updatedContent = xmlContentProcessor.doXMLPostProcessing(updatedContent);
        return updatedContent;
    }

    private String addDocTypeToTemplateXmlId(String template) {
        return XmlHelper.addDocTypeToXmlId(template, XmlHelper.ANNEX);
    }

    private boolean isFirstSubParagraph(Annex annex, String elementId, String tagName, String subParElementId) {
        Element firstSubParElement = xmlContentProcessor.getChildElement(getContent(annex), tagName, elementId, Arrays.asList(SUBPARAGRAPH), 1);
        return firstSubParElement != null && subParElementId.equals(firstSubParElement.getElementId());
    }

    @Override
    public byte[] updateAnnexBlock(Annex annex, String elementId, String tagName, String elementFragment) {
        byte[] updatedContent = null;
        if (xmlContentProcessor.needsToBeIndented(elementFragment)) {
            byte[] contentBytes = getContent(annex);
            List<TableOfContentItemVO> toc = tableOfContentProcessor.buildTableOfContent(DOC, contentBytes, TocMode.NOT_SIMPLIFIED);
            updatedContent = xmlContentProcessor.indentElement(contentBytes, tagName, elementId, elementFragment, toc);
        } else {
            updatedContent = elementProcessor.updateElement(annex, elementFragment, tagName, elementId);
        }
        return updateAnnexContent(elementId, tagName, updatedContent);
    }

    public byte[] renumberDocument(Annex document, AnnexStructureType structureType) {
        Validate.notNull(document, "Document is required.");
        byte[] updatedContent = getContent(document);
        updatedContent = xmlContentProcessor.prepareForRenumber(updatedContent);
        switch(structureType) {
            case ARTICLE:
                updatedContent = numberService.renumberArticles(updatedContent, true);
                break;
            case LEVEL:
                updatedContent = numberService.renumberLevel(updatedContent);
                updatedContent = numberService.renumberParagraph(updatedContent);
                break;
        }
        updatedContent = xmlContentProcessor.doXMLPostProcessing(updatedContent);
        return updatedContent;
    }

    private byte[] updateAnnexContent(String elementId, String tagName, byte[] xmlContent) {
        if (tagName.equals(NUM)) {
            tagName = xmlContentProcessor.getParentTagNameById(xmlContent, elementId);
            elementId = xmlContentProcessor.getParentIdById(xmlContent, elementId);
        }

        if (hasDepth(tagName)) {
            xmlContent = xmlContentProcessor.insertDepthAttribute(xmlContent, tagName, elementId);
            xmlContent = numberService.renumberLevel(xmlContent);
        } else if (Arrays.asList(PARAGRAPH, SUBPARAGRAPH, POINT, INDENT, SUBPOINT).contains(tagName)) {
            xmlContent = numberService.renumberParagraph(xmlContent);
            if (Arrays.asList(POINT, INDENT, SUBPOINT, SUBPARAGRAPH).contains(tagName)) {
                xmlContent = numberService.renumberLevel(xmlContent);
            }
        } else if (tagName.equals(ARTICLE)) {
            xmlContent = numberService.renumberArticles(xmlContent);
        }
        return xmlContentProcessor.doXMLPostProcessing(xmlContent);
    }

    private boolean hasDepth(String tagName) {
        List<TocItem> tocItems = structureContextProvider.get().getTocItems();
        List<NumberingConfig> numberingConfigs = structureContextProvider.get().getNumberingConfigs();
        NumberingConfig numberingConfig = getNumberingConfigByTagName(tocItems, numberingConfigs, tagName);
        return numberingConfig.getLevels() != null && numberingConfig.getLevels().getLevels().size() > 0;
    }
    
    private byte[] getContent(Annex annex) {
        final Content content = annex.getContent().getOrError(() -> "Annex content is required!");
        return content.getSource().getBytes();
    }
    
    @Override
    public Pair<byte[], Element> getSplittedElement(byte[] docContent, String elementContent, String elementName, String elementId) throws Exception{
        Validate.notNull(docContent, "Document is required.");
        Validate.notNull(elementContent, "ElementContent is required.");
        Validate.notNull(elementName, "ElementName is required.");
        Validate.notNull(elementId, "ElementId is required.");

        return xmlContentProcessor.getSplittedElement(docContent, elementContent, elementName, elementId);
    }
    
    @Override
    public Element getMergeOnElement(Annex document, String elementContent, String elementName, String elementId) throws Exception {
        Validate.notNull(document, "Document is required.");
        Validate.notNull(elementContent, "ElementContent is required.");
        Validate.notNull(elementName, "ElementName is required.");
        Validate.notNull(elementId, "ElementId is required.");
    
        final byte[] contentBytes = getContent(document);
        return xmlContentProcessor.getMergeOnElement(contentBytes, elementContent, elementName, elementId);
    }

    @Override
    public Element getTocElement(final Annex document, final String elementId, final List<TableOfContentItemVO> toc) {
        Validate.notNull(document, "Document is required.");
        Validate.notNull(elementId, "ElementId is required.");

        final byte[] contentBytes = getContent(document);
        return xmlContentProcessor.getTocElement(contentBytes, elementId, toc, Arrays.asList(SUBPARAGRAPH));
    }

    @Override
    public byte[] mergeElement(Annex document, String elementContent, String elementName, String elementId) {
        Validate.notNull(document, "Document is required.");
        Validate.notNull(elementContent, "ElementContent is required.");
        Validate.notNull(elementName, "ElementName is required.");
        Validate.notNull(elementId, "ElementId is required.");
        
        final byte[] contentBytes = getContent(document);
        byte[] updatedContent = xmlContentProcessor.mergeElement(contentBytes, elementContent, elementName, elementId);
        if (updatedContent != null) {
            updatedContent = xmlContentProcessor.doXMLPostProcessing(updatedContent);
        }
        return updatedContent;
    }

    @Override
    public LevelItemVO getLevelItemVO(Annex document, String elementId, String elementTagName) throws Exception {
        Validate.notNull(document, "Document is required.");
        Validate.notNull(elementId, "ElementId is required.");

        final byte[] contentBytes = getContent(document);
        return xmlContentProcessor.getLevelItemVo(contentBytes, elementId, elementTagName);
    }
}
