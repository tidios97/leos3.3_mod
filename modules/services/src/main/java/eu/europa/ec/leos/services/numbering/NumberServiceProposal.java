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
package eu.europa.ec.leos.services.numbering;

import com.google.common.base.Stopwatch;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.services.numbering.depthBased.ParentChildConverter;
import eu.europa.ec.leos.services.numbering.depthBased.ParentChildNode;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.vo.toc.TocItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.inject.Provider;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static eu.europa.ec.leos.services.support.XmlHelper.ARTICLE;
import static eu.europa.ec.leos.services.support.XmlHelper.LEVEL;
import static eu.europa.ec.leos.services.support.XmlHelper.RECITAL;
import static eu.europa.ec.leos.services.support.XmlHelper.UTF_8;
import static eu.europa.ec.leos.services.support.XercesUtils.createXercesDocument;
import static eu.europa.ec.leos.services.support.XercesUtils.nodeToByteArray;
import static eu.europa.ec.leos.vo.toc.StructureConfigUtils.isAutoNumberingEnabled;

/**
 * Service used for Numbering a full XNL document.
 *
 * The service makes use of two components to achieve the goal:
 * - NumberProcessorHandler: CompositePattern used for generic numbering configured in structure_xx.xml
 * - NumberProcessorLevel: Specific/custom implementation for Level numbering
 */
@Component
@Instance(instances = {InstanceType.COMMISSION, InstanceType.OS})
public class NumberServiceProposal implements NumberService {

    private static final Logger LOG = LoggerFactory.getLogger(NumberServiceProposal.class);

    private final Provider<StructureContext> structureContextProvider;
    private final NumberProcessorHandler numberProcessorHandler;
    private final ParentChildConverter parentChildConverter;
    private final XmlContentProcessor xmlContentProcessor;

    private List<TocItem> tocItems;

    @Autowired
    public NumberServiceProposal(Provider<StructureContext> structureContextProvider, NumberProcessorHandler numberProcessorHandler, ParentChildConverter parentChildConverter, XmlContentProcessor xmlContentProcessor) {
        this.structureContextProvider = structureContextProvider;
        this.numberProcessorHandler = numberProcessorHandler;
        this.parentChildConverter = parentChildConverter;
        this.xmlContentProcessor = xmlContentProcessor;
    }

    @Override
    public byte[] renumberArticles(byte[] xmlContent) {
        return renumberDocument(xmlContent, ARTICLE, true, false);
    }

    @Override
    public byte[] renumberArticles(byte[] xmlContent, boolean renumberChildren) {
        return renumberDocument(xmlContent, ARTICLE, true, renumberChildren);
    }

    @Override
    public byte[] renumberRecitals(byte[] xmlContent) {
        return renumberDocument(xmlContent, RECITAL, true, false);
    }

    @Override
    public String renumberImportedArticle(String xmlContentAsString, String language) {
        byte[] initialContent = xmlContentAsString.getBytes(UTF_8);
        byte[] renumberedContent = renumberDocument(initialContent, ARTICLE, false, true);
        if (!initialContent.equals(renumberedContent)) {
            return new String(renumberedContent);
        }
        return xmlContentAsString;
    }

    private byte[] renumberDocument(byte[] xmlContent, String elementName, boolean namespaceEnabled, boolean renumberChildren) {
        tocItems = structureContextProvider.get().getTocItems();
        if (isAutoNumberingEnabled(tocItems, elementName)) {
            Document document = createXercesDocument(xmlContent, namespaceEnabled);
            numberProcessorHandler.renumberDocument(document, elementName, renumberChildren);
            return nodeToByteArray(document);
        }
        return xmlContent;
    }

    @Override
    public String renumberImportedRecital(String xmlContent) {
        return xmlContent;
    }

    @Override
    public byte[] renumberParagraph(byte[] xmlContent) {
        return xmlContent;
    }

    @Override
    public byte[] renumberDivisions(byte[] xmlContent) {
        return xmlContent;
    }

    @Override
    public byte[] renumberLevel(byte[] xmlContent) {
        List<TocItem> tocItems = structureContextProvider.get().getTocItems();
        if (isAutoNumberingEnabled(tocItems, LEVEL)) {
            Stopwatch stopwatch = Stopwatch.createStarted();
            Document document = createXercesDocument(xmlContent);
            NodeList nodeList = document.getElementsByTagName(LEVEL);
            List<ParentChildNode> parentChildList = parentChildConverter.getParentChildStructure(nodeList, true);
            LOG.trace("renumberLevel - Found {} '{}'s element in the document, and grouped them in {} top elements", nodeList.getLength(), LEVEL, parentChildList.size());
            numberProcessorHandler.renumberDepthBased(parentChildList, LEVEL, 1);
            LOG.debug("Renumbered {} '{}' in {} milliseconds ({} sec)", nodeList.getLength(), LEVEL, stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
            return nodeToByteArray(document);
        }
        return xmlContent;
    }

}
