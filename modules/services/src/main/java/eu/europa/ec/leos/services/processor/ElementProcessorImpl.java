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
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.model.xml.Element;
import eu.europa.ec.leos.services.clone.CloneContext;
import eu.europa.ec.leos.services.compare.ContentComparatorContext;
import eu.europa.ec.leos.services.compare.ContentComparatorService;
import eu.europa.ec.leos.services.document.DocumentContentService;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.support.XPathCatalog;
import eu.europa.ec.leos.services.support.XercesUtils;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.vo.toc.OptionsType;
import eu.europa.ec.leos.vo.toc.StructureConfigUtils;
import eu.europa.ec.leos.vo.toc.TocItem;
import io.atlassian.fugue.Pair;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.inject.Provider;
import java.util.Collections;
import java.util.List;

import static eu.europa.ec.leos.services.support.XercesUtils.createXercesDocument;
import static eu.europa.ec.leos.services.support.XercesUtils.nodeToString;
import static eu.europa.ec.leos.services.support.XmlHelper.HEADING;

@Service
public class ElementProcessorImpl<T extends XmlDocument> implements ElementProcessor<T> {

    private XmlContentProcessor xmlContentProcessor;
    private Provider<StructureContext> structureContextProvider;
    private CloneContext cloneContext;
    private XPathCatalog xPathCatalog;
    private DocumentContentService documentContentService;
    private ContentComparatorService compareService;

    @Autowired
    public ElementProcessorImpl(XmlContentProcessor xmlContentProcessor, Provider<StructureContext> structureContextProvider,
                                CloneContext cloneContext, XPathCatalog xPathCatalog, DocumentContentService documentContentService,
                                ContentComparatorService compareService) {
        this.xmlContentProcessor = xmlContentProcessor;
        this.structureContextProvider = structureContextProvider;
        this.cloneContext = cloneContext;
        this.xPathCatalog = xPathCatalog;
        this.documentContentService = documentContentService;
        this.compareService = compareService;
    }

    @Override
    public String getElement(XmlDocument document, String elementName, String elementId) {
        Validate.notNull(document, "Document is required.");
        Validate.notNull(elementId, "Element id is required.");
    
        final byte[] contentBytes = document.getContent().get().getSource().getBytes();
        return xmlContentProcessor.getElementByNameAndId(contentBytes, elementName, elementId);
    }

    @Override
    public Element getSiblingElement(T document, String elementName, String elementId, boolean previous) {
        Validate.notNull(document, "Document is required.");
        Validate.notNull(elementName, "ElementName id is required.");
        Validate.notNull(elementId, "Element id is required.");
    
        final byte[] contentBytes = getContent(document);
        return xmlContentProcessor.getSiblingElement(contentBytes, elementName, elementId, Collections.emptyList(), previous);
    }

    @Override
    public Element getChildElement(T document, String elementName, String elementId, List<String> elementTags, int position) {
        Validate.notNull(document, "Document is required.");
        Validate.notNull(elementName, "ElementName id is required.");
        Validate.notNull(elementId, "Element id is required.");
    
        final byte[] contentBytes = getContent(document);
        return xmlContentProcessor.getChildElement(contentBytes, elementName, elementId, elementTags, position);
    }

    @Override
    public String getParentElement(T document, String elementId) {
        Validate.notNull(document, "Document is required.");
        Validate.notNull(elementId, "Element id is required.");

        final byte[] contentBytes = getContent(document);
        return xmlContentProcessor.getParentIdById(contentBytes, elementId);
    }

    @Override
    public byte[] addChildToParent(T document, String elementContent, String parentElementId) {
        Validate.notNull(document, "Document is required.");
        Validate.notNull(elementContent, "Element content is required.");
        Validate.notNull(parentElementId, "Parent element id is required.");

        byte[] contentBytes = getContent(document);
        return xmlContentProcessor.addChildToParent(contentBytes, elementContent ,parentElementId);
    }

    @Override
    public byte[] updateElement(T document, String elementContent, String elementName, String elementId) {
        Validate.notNull(document, "Document is required.");
        Validate.notNull(elementId, "Element id is required.");

        List<TocItem> tocItems = structureContextProvider.get().getTocItems();
        elementContent = removeEmptyHeading(document.getContent().get().getSource().getBytes(), elementContent, elementName, elementId, tocItems);
        // merge the updated content with the actual document and return updated document
        byte[] contentBytes = getContent(document);
        if(isClonedProposal()) {
            Pair<byte[], String> result = xmlContentProcessor.updateSoftMovedElement(contentBytes, elementContent);
            if(result.left() != null && result.left().length > 0) {
                contentBytes = result.left();
            } else if(result.right() != null && result.right().getBytes().length > 0) {
                elementContent = result.right();
            }
            byte[] originalContentBytes = documentContentService.getOriginalContentToCompare(document);
            Document doc = createXercesDocument(originalContentBytes);
            Node node = XercesUtils.getElementById(doc, elementId);
            String originalContent = nodeToString(node);
            if(!StringUtils.isEmpty(originalContent)) {
                elementContent = compareService.compareDeletedElements(new ContentComparatorContext.Builder(originalContent, elementContent)
                        .withDisplayRemovedContentAsReadOnly(Boolean.TRUE)
                        .build());
            }
        }
        return xmlContentProcessor.replaceElementById(contentBytes, elementContent, elementId);
    }

    @Override
    public byte[] deleteElement(T document, String elementId, String elementType) {
        Validate.notNull(document, "Document is required.");
        Validate.notNull(elementId, "Element id is required.");
    
        final byte[] contentBytes = getContent(document);
        return xmlContentProcessor.removeElementById(contentBytes, elementId);
    }

    @Override
    public List<Element> getChangedElements(byte[] xmlContent) {
        Validate.notNull(xmlContent, "Content is required.");
        return xmlContentProcessor.getElementsByPath(xmlContent, xPathCatalog.getXPathElementWithSoftAction());
    }

    @Override
    public byte[] replaceTextInElement(T document, String origText, String newText, String elementId, int startOffset, int endOffset) {
        Validate.notNull(document, "Document is required.");
        Validate.notEmpty(origText, "Orginal Text is required");
        Validate.notNull(elementId, "Element Id is required");
        Validate.notNull(newText, "New Text is required");
        
        final byte[] byteXmlContent = getContent(document);
        return xmlContentProcessor.replaceTextInElement(byteXmlContent, origText, newText, elementId, startOffset, endOffset);
    }

    @Override
    public byte[] insertAttribute(byte[] xmlContent, String elementTag, String elementId, String attrName, String attrVal) {
        return xmlContentProcessor.insertAttributeToElement(xmlContent, elementTag, elementId, attrName, attrVal);
    }

    @Override
    public byte[] removeAttribute(byte[] xmlContent, String elementId, String attrName) {
        return xmlContentProcessor.removeAttributeFromElement(xmlContent, elementId, attrName);
    }

    private String removeEmptyHeading(byte[] xmlContent, String newContent, String tagName, String idAttributeValue, List<TocItem> tocItems) {
        String elementTagName;
        if (tagName.equals(HEADING)) {
            elementTagName = xmlContentProcessor.getParentElement(xmlContent, idAttributeValue).getElementTagName();
        } else {
            elementTagName = tagName;
        }
        TocItem tocItem = StructureConfigUtils.getTocItemByName(tocItems, elementTagName);
        if (tocItem != null && tocItem.getItemHeading() == OptionsType.OPTIONAL) {
            newContent = xmlContentProcessor.removeEmptyHeading(newContent);
        }
        return newContent;
    }

    @Override
    public String getElementAttributeValueByNameAndId(T document, String attributeName, String tagName, String idAttributeValue) {
        Validate.notNull(document, "Document is required.");
        Validate.notNull(idAttributeValue, "Element id is required.");

        return xmlContentProcessor.getElementAttributeValueByNameAndId(getContent(document), attributeName, tagName, idAttributeValue);
    }

    @Override
    public String getElementAttributeValue(byte[] xmlContent, String attributeName, String tagName, String idAttributeValue) {
        Validate.notNull(xmlContent, "Document content is required.");
        Validate.notNull(idAttributeValue, "Element id is required.");

        return xmlContentProcessor.getElementAttributeValueByNameAndId(xmlContent, attributeName, tagName, idAttributeValue);
    }
    
    private byte[] getContent(T document) {
        final Content content = document.getContent().getOrError(() -> "Annex content is required!");
        return content.getSource().getBytes();
    }

    private boolean isClonedProposal() {
        return cloneContext != null && cloneContext.isClonedProposal();
    }
}
