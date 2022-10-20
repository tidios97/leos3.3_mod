package eu.europa.ec.leos.services.processor;

import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.document.Annex;
import eu.europa.ec.leos.domain.cmis.document.Explanatory;
import eu.europa.ec.leos.domain.common.TocMode;
import eu.europa.ec.leos.i18n.MessageHelper;
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
import static eu.europa.ec.leos.services.support.XmlHelper.DIVISION;
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
public class ExplanatoryProcessorImpl implements ExplanatoryProcessor {
    
    private XmlContentProcessor xmlContentProcessor;
    protected NumberService numberService;
    private final ElementProcessor<Explanatory> elementProcessor;
    protected final TableOfContentProcessor tableOfContentProcessor;
    private Provider<StructureContext> structureContextProvider;
    protected MessageHelper messageHelper;

    @Autowired
    public ExplanatoryProcessorImpl(XmlContentProcessor xmlContentProcessor, NumberService numberService, ElementProcessor<Explanatory> elementProcessor,
                                    Provider<StructureContext> structureContextProvider, TableOfContentProcessor tableOfContentProcessor,
                                    MessageHelper messageHelper) {
        this.xmlContentProcessor = xmlContentProcessor;
        this.numberService = numberService;
        this.elementProcessor = elementProcessor;
        this.structureContextProvider = structureContextProvider;
        this.tableOfContentProcessor = tableOfContentProcessor;
        this.messageHelper = messageHelper;
    }

    private byte[] getContent(Explanatory explanatory) {
        final Content content = explanatory.getContent().getOrError(() -> "Explanatory content is required!");
        return content.getSource().getBytes();
    }

    @Override
    public byte[] insertNewElement(Explanatory document, String elementId, String tagName, boolean before) {
        Validate.notNull(document, "Document is required.");
        Validate.notNull(elementId, "Element id is required.");
        
        final String template;
        byte[] updatedContent;
        List<TocItem> items = structureContextProvider.get().getTocItems();
        
        switch (tagName) {
            case LEVEL:
                template = XmlHelper.getTemplate(StructureConfigUtils.getTocItemByNameOrThrow(items, tagName), StructureConfigUtils.HASH_NUM_VALUE, messageHelper);
                updatedContent = xmlContentProcessor.insertElementByTagNameAndId(getContent(document), template, tagName, elementId, before);
                updatedContent = xmlContentProcessor.insertDepthAttribute(updatedContent, tagName, elementId);
                updatedContent = numberService.renumberLevel(updatedContent);
                break;
            case PARAGRAPH:
                template = XmlHelper.getTemplate(StructureConfigUtils.getTocItemByNameOrThrow(items, tagName), messageHelper);
                updatedContent = xmlContentProcessor.insertElementByTagNameAndId(getContent(document), template, tagName, elementId, before);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported operation for tag: " + tagName);
        }
        
        updatedContent = xmlContentProcessor.doXMLPostProcessing(updatedContent);
        return updatedContent;
    }
    
    @Override
    public byte[] deleteElement(Explanatory document, String elementId, String tagName) throws Exception {
        Validate.notNull(document, "Document is required.");
        Validate.notNull(elementId, "Element id is required.");
        
        byte[] xmlContent = elementProcessor.deleteElement(document, elementId, tagName);
        return updateExplanatoryContent(elementId, tagName, xmlContent);
    }

    @Override
    public Pair<byte[], Element> getSplittedElement(byte[] docContent, String elementContent, String elementName, String elementId) throws Exception {
    	Validate.notNull(docContent, "Document is required.");
        Validate.notNull(elementContent, "ElementContent is required.");
        Validate.notNull(elementName, "ElementName is required.");
        Validate.notNull(elementId, "ElementId is required.");

        return xmlContentProcessor.getSplittedElement(docContent, elementContent, elementName, elementId);
    }

    @Override
    public Element getMergeOnElement(Explanatory document, String elementContent, String elementName, String elementId) throws Exception {
        return null;
    }

    @Override
    public Element getTocElement(Explanatory document, String elementId, List<TableOfContentItemVO> toc) {
        Validate.notNull(document, "Document is required.");
        Validate.notNull(elementId, "ElementId is required.");

        final byte[] contentBytes = getContent(document);
        return xmlContentProcessor.getTocElement(contentBytes, elementId, toc, Arrays.asList(SUBPARAGRAPH));
    }

    @Override
    public byte[] mergeElement(Explanatory document, String elementContent, String elementName, String elementId) {
        return new byte[0];
    }

    @Override
    public byte[] updateElement(Explanatory document, String elementId, String tagName, String elementFragment) {
        byte[] updatedContent = null;
        if (xmlContentProcessor.needsToBeIndented(elementFragment)) {
            byte[] contentBytes = getContent(document);
            List<TableOfContentItemVO> toc = tableOfContentProcessor.buildTableOfContent(DOC, contentBytes, TocMode.NOT_SIMPLIFIED);
            updatedContent = xmlContentProcessor.indentElement(contentBytes, tagName, elementId, elementFragment, toc);
        } else {
            updatedContent = elementProcessor.updateElement(document, elementFragment, tagName, elementId);
        }
        return updateExplanatoryContent(elementId, tagName, updatedContent);
    }

    private byte[] updateExplanatoryContent(String elementId, String tagName, byte[] xmlContent) {
        if (tagName.equals(NUM)) {
            tagName = xmlContentProcessor.getParentTagNameById(xmlContent, elementId);
            elementId = xmlContentProcessor.getParentIdById(xmlContent, elementId);
        }

        if (DIVISION.equals(tagName)) {
            xmlContent = xmlContentProcessor.insertAutoNumOverwriteAttributeIntoParentElements(xmlContent, elementId);
        }

        if (hasDepth(tagName)) {
            xmlContent = xmlContentProcessor.insertDepthAttribute(xmlContent, tagName, elementId);
            xmlContent = numberService.renumberLevel(xmlContent);
            xmlContent = numberService.renumberDivisions(xmlContent);
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

    @Override
    public LevelItemVO getLevelItemVO(Explanatory document, String elementId, String elementTagName) throws Exception {
        Validate.notNull(document, "Document is required.");
        Validate.notNull(elementId, "ElementId is required.");

        final byte[] contentBytes = getContent(document);
        return xmlContentProcessor.getLevelItemVo(contentBytes, elementId, elementTagName);
    }

}
