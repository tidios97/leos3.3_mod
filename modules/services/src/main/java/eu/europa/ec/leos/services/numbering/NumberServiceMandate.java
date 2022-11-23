/*
 * Copyright 2019 European Commission
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
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
import static eu.europa.ec.leos.services.support.XmlHelper.DIVISION;
import static eu.europa.ec.leos.services.support.XmlHelper.LEVEL;
import static eu.europa.ec.leos.services.support.XmlHelper.PARAGRAPH;
import static eu.europa.ec.leos.services.support.XmlHelper.RECITAL;
import static eu.europa.ec.leos.services.support.XmlHelper.UTF_8;
import static eu.europa.ec.leos.services.support.XercesUtils.createXercesDocument;
import static eu.europa.ec.leos.services.support.XercesUtils.nodeToByteArray;
import static eu.europa.ec.leos.services.support.XercesUtils.nodeToString;
import static eu.europa.ec.leos.vo.toc.StructureConfigUtils.isAutoNumberingEnabled;

@Component
@Instance(InstanceType.COUNCIL)
public class NumberServiceMandate implements NumberService {

    private static final Logger LOG = LoggerFactory.getLogger(NumberServiceMandate.class);

    private final XmlContentProcessor xmlContentProcessor;
    private final Provider<StructureContext> structureContextProvider;
    private final NumberProcessorHandler numberProcessorHandler;
    private final ParentChildConverter parentChildConverter;

    @Autowired
    public NumberServiceMandate(XmlContentProcessor xmlContentProcessor, Provider<StructureContext> structureContextProvider, NumberProcessorHandler numberProcessorHandler, ParentChildConverter parentChildConverter) {
        this.xmlContentProcessor = xmlContentProcessor;
        this.structureContextProvider = structureContextProvider;
        this.numberProcessorHandler = numberProcessorHandler;
        this.parentChildConverter = parentChildConverter;
    }

    @Override
    public String renumberImportedArticle(String xmlContent, String language) {
//        String updatedElements;
//        elementNumberingHelper.setImportArticleDefaultProperties();
//        try {
//            updatedElements = new String(elementNumberingHelper.renumberElements(ARTICLE, xmlContent.getBytes(UTF_8), false));
//        } catch (Exception e) {
//            LOG.error("Unable to perform the renumberArticles operation", e);
//            throw new RuntimeException("Unable to perform the renumberArticles operation", e);
//        } finally {
//            elementNumberingHelper.resetImportArticleDefaultProperties();
//        }
//        return updatedElements;
        List<TocItem> tocItems = structureContextProvider.get().getTocItems();
        if (isAutoNumberingEnabled(tocItems, ARTICLE)) {
            Document document = createXercesDocument(xmlContent.getBytes(UTF_8));
            numberProcessorHandler.renumberDocument(document, ARTICLE, true);
            return nodeToString(document);
        }
        return xmlContent;
    }

    @Override
    public String renumberImportedRecital(String xmlContent) {
        //No need to do pre process as this is done later stages
        return xmlContent;
    }

    @Override
    public byte[] renumberArticles(byte[] xmlContent) {
        List<TocItem> tocItems = structureContextProvider.get().getTocItems();
        if (isAutoNumberingEnabled(tocItems, ARTICLE)) {
            Document document = createXercesDocument(xmlContent);
            numberProcessorHandler.renumberDocument(document, ARTICLE, true);
            return nodeToByteArray(document);
        }
        return xmlContent;
    }

    @Override
    public byte[] renumberArticles(byte[] xmlContent, boolean renumberChildElement) {
        return renumberArticles(xmlContent);
    }

    @Override
    public byte[] renumberRecitals(byte[] xmlContent) {
        List<TocItem> tocItems = structureContextProvider.get().getTocItems();
        if (isAutoNumberingEnabled(tocItems, RECITAL)) {
            Document document = createXercesDocument(xmlContent);
            numberProcessorHandler.renumberDocument(document, RECITAL, true);
            return nodeToByteArray(document);
        }
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

    @Override
    public byte[] renumberParagraph(byte[] xmlContent) {
        List<TocItem> tocItems = structureContextProvider.get().getTocItems();
        if (isAutoNumberingEnabled(tocItems, PARAGRAPH)) {
            Document document = createXercesDocument(xmlContent);
            numberProcessorHandler.renumberDocument(document, PARAGRAPH, true);
            return nodeToByteArray(document);
        }
        return xmlContent;
    }

    @Override
    public byte[] renumberDivisions(byte[] xmlContent) {
        List<TocItem> tocItems = structureContextProvider.get().getTocItems();
        if (isAutoNumberingEnabled(tocItems, DIVISION)) {
            Stopwatch stopwatch = Stopwatch.createStarted();
            Document document = createXercesDocument(xmlContent);
            NodeList nodeList = document.getElementsByTagName(DIVISION);
            List<ParentChildNode> parentChildList = parentChildConverter.getParentChildStructure(nodeList, false);
            LOG.trace("renumberDivisions - Found {} '{}'s element in the document, and grouped them in {} top elements", nodeList.getLength(), DIVISION, parentChildList.size());
            numberProcessorHandler.renumberDepthBased(parentChildList, DIVISION, 1);
            LOG.debug("Renumbered {} '{}' in {} milliseconds ({} sec)", nodeList.getLength(), DIVISION, stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
            return nodeToByteArray(document);
        }
        return xmlContent;
    }
}