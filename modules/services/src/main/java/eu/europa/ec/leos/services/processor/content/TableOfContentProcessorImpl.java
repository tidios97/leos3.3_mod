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
package eu.europa.ec.leos.services.processor.content;

import eu.europa.ec.leos.domain.common.TocMode;
import eu.europa.ec.leos.services.support.IdGenerator;
import eu.europa.ec.leos.services.support.XercesUtils;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.vo.toc.NumberingConfig;
import eu.europa.ec.leos.vo.toc.StructureConfigUtils;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItem;
import eu.europa.ec.leos.vo.toc.indent.IndentedItemType;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.inject.Provider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static eu.europa.ec.leos.services.support.XmlHelper.CLASS_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.CN;
import static eu.europa.ec.leos.services.support.XmlHelper.CONTENT;
import static eu.europa.ec.leos.services.support.XmlHelper.EC;
import static eu.europa.ec.leos.services.support.XmlHelper.EMPTY_STRING;
import static eu.europa.ec.leos.services.support.XmlHelper.INDENT;
import static eu.europa.ec.leos.services.support.XmlHelper.INLINE_ELEMENTS;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_INDENT_ORIGIN_INDENT_LEVEL_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_INDENT_ORIGIN_NUM_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_INDENT_ORIGIN_NUM_ID_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_INDENT_ORIGIN_NUM_ORIGIN_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_INDENT_ORIGIN_TYPE_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_INDENT_UNUMBERED_PARAGRAPH;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_ORIGIN_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_ACTION_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_ACTION_ROOT_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_DATE_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_MOVE_FROM;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_MOVE_TO;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_TRANS_FROM;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_USER_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LIST;
import static eu.europa.ec.leos.services.support.XmlHelper.NUM;
import static eu.europa.ec.leos.services.support.XmlHelper.PARAGRAPH;
import static eu.europa.ec.leos.services.support.XmlHelper.POINT;
import static eu.europa.ec.leos.services.support.XmlHelper.SUBPARAGRAPH;
import static eu.europa.ec.leos.services.support.XmlHelper.SUBPOINT;
import static eu.europa.ec.leos.services.processor.content.indent.IndentConversionHelper.NUMBERED_AND_LEVEL_ITEMS;
import static eu.europa.ec.leos.services.processor.content.indent.IndentConversionHelper.NUMBERED_ITEMS;
import static eu.europa.ec.leos.services.processor.content.indent.IndentConversionHelper.UNUMBERED_ITEMS;
import static eu.europa.ec.leos.services.support.XercesUtils.createElement;
import static eu.europa.ec.leos.services.support.XercesUtils.createXercesDocument;
import static eu.europa.ec.leos.services.support.XercesUtils.getFirstElementByName;
import static eu.europa.ec.leos.services.support.XercesUtils.isCrossheadingNum;
import static eu.europa.ec.leos.services.processor.content.XmlContentProcessorHelper.getAllChildTableOfContentItems;

@Component
public class TableOfContentProcessorImpl implements TableOfContentProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(TableOfContentProcessorImpl.class);

    @Autowired
    protected Provider<StructureContext> structureContextProvider;

    public List<TableOfContentItemVO> buildTableOfContent(String startingNode, byte[] xmlContent, TocMode mode) {
        LOG.trace("Start building TOC from tag {} and mode {}", startingNode, mode);
        long startTime = System.currentTimeMillis();
        List<TocItem> tocItems = structureContextProvider.get().getTocItems();
        Map<TocItem, List<TocItem>> tocRules = structureContextProvider.get().getTocRules();
        List<NumberingConfig> numberingConfigs = structureContextProvider.get().getNumberingConfigs();

        List<TableOfContentItemVO> itemVOList = new ArrayList<>();
        try {
            Document document = createXercesDocument(xmlContent);
            Node node = getFirstElementByName(document, startingNode);
            if (node != null) {
                itemVOList = getAllChildTableOfContentItems(node, tocItems, tocRules, numberingConfigs, mode);
            }
            LOG.debug("Xerces Build table of content completed in {} ms", (System.currentTimeMillis() - startTime));
            return itemVOList;
        } catch (Exception e) {
            throw new RuntimeException("Unable to build the Table of content item list", e);
        }
    }

    public static TocItem getTocItemFromNumberingType(String number, String tagName, TocItem originalTocItem, List<NumberingConfig> numberingConfigs, List<TocItem> tocItems, Node node) {
        if (tagName.equals(INDENT)) {
            if (node.getParentNode().getParentNode().getNodeName().equalsIgnoreCase(POINT)) {
                TocItem pointTocItem = StructureConfigUtils.getTocItemByName(tocItems, POINT);
                return StructureConfigUtils.getTocItemByNumberingConfig(tocItems, pointTocItem.getNumberingType(), tagName);
            } else {
                List<TocItem> foundTocItems = StructureConfigUtils.getTocItemsByName(tocItems, INDENT);
                return StructureConfigUtils.getTocItemByNumValue(numberingConfigs, foundTocItems, number);
            }
        }
        return originalTocItem;
    }

    private Node buildElement(Node node, String tagName, TableOfContentItemVO tocVo) {
        String newId = tocVo.getId() != null ? tocVo.getId() : IdGenerator.generateId(tagName.substring(0, 3), 7);
        Node elementNode = createElement(node.getOwnerDocument(), tagName, newId, EMPTY_STRING);
        XercesUtils.insertOrUpdateAttributeValue(elementNode, LEOS_ORIGIN_ATTR, tocVo.getOriginAttr());
        return elementNode;
    }

    public boolean isFirstElement(TableOfContentItemVO tableOfContentItemVO, String elementName) {
        return this.containsElement(tableOfContentItemVO, elementName);
    }

    public boolean containsElement(TableOfContentItemVO tableOfContentItemVO, String elementName) {
        return XercesUtils.getFirstChild(tableOfContentItemVO.getNode(), elementName) != null;
    }

    public void convertTocItemContent(TableOfContentItemVO item, TableOfContentItemVO subelement, IndentedItemType beforeIndentedType, IndentedItemType afterIndentedType, boolean restored) {
        switch (afterIndentedType) {
            case POINT:
                convertToPoint(item, beforeIndentedType, item.getNumber());
                break;
            case OTHER_SUBPOINT:
                convertToSubpoint(item, beforeIndentedType);
                break;
            case FIRST_SUBPOINT:
                convertToFirstSubpoint(item, subelement, beforeIndentedType, item.getNumber());
                break;
            case PARAGRAPH:
                convertToParagraph(item, beforeIndentedType, item.getNumber());
                break;
            case OTHER_SUBPARAGRAPH:
                convertToSubparagraph(item, beforeIndentedType);
                break;
            case FIRST_SUBPARAGRAPH:
                convertToFirstSubparagraph(item, subelement, beforeIndentedType, item.getNumber());
                break;
        }
        if (restored) {
            restoreElement(item);
        }
    }

    void restoreElement(TableOfContentItemVO item) {
        Node originalItem = item.getNode();
        resetNum(originalItem);
        resetIndentAttributes(originalItem);
        item.setNode(originalItem);
    }

    void convertToSubpoint(TableOfContentItemVO item, IndentedItemType beforeIndentItemType) {
        Node originalItem = item.getNode();
        List<Node> children = getChildren(originalItem);

        switch (beforeIndentItemType) {
            case FIRST_SUBPOINT:
            case FIRST_SUBPARAGRAPH:
                originalItem = changeTagName(originalItem, SUBPOINT, false);
                break;
            case POINT:
            case PARAGRAPH:
            case OTHER_SUBPARAGRAPH:
                if (!children.isEmpty()) {
                    return;
                }
                originalItem = changeTagName(originalItem, SUBPOINT, false);
                break;
        }
        copyAttributesAndSetId(item, originalItem);
        item.setNode(originalItem);
    }

    void convertToPoint(TableOfContentItemVO item, IndentedItemType beforeIndentItemType, String num) {
        Node originalItem = item.getNode();
        List<Node> children = getChildren(originalItem);

        switch (beforeIndentItemType) {
            case FIRST_SUBPOINT:
            case FIRST_SUBPARAGRAPH:
                Node firstSubelement;
                if (children.isEmpty()) {
                    return;
                } else {
                    firstSubelement = children.get(0);
                }

                Node content = XercesUtils.getFirstChild(firstSubelement, CONTENT);
                moveToParent(content, false);
                originalItem = changeTagName(originalItem, TableOfContentProcessor.getTagValueFromTocItemVo(item), false);
                updateNumTag(originalItem, num);
                break;
            case OTHER_SUBPOINT:
            case OTHER_SUBPARAGRAPH:
                if (!children.isEmpty()) {
                    return;
                }
                createNumTag(originalItem, num);
                originalItem = changeTagName(originalItem, TableOfContentProcessor.getTagValueFromTocItemVo(item), false);
                break;
            case PARAGRAPH:
                if (!children.isEmpty()) {
                    return;
                }
                updateNumTag(originalItem, num);
                originalItem = changeTagName(originalItem, TableOfContentProcessor.getTagValueFromTocItemVo(item), false);
                break;
        }
        copyAttributesAndSetId(item, originalItem);
        item.setNode(originalItem);
    }

    void convertToFirstSubpoint(TableOfContentItemVO item, TableOfContentItemVO subelement, IndentedItemType beforeIndentItemType, String num) {
        Node originalItem = item.getNode();
        List<Node> children = getChildren(originalItem);
        Node firstSubpoint = null;

        switch (beforeIndentItemType) {
            case FIRST_SUBPARAGRAPH:
                Node firstSubelement;
                if (children.isEmpty()) {
                    return;
                } else {
                    firstSubelement = children.get(0);
                }

                firstSubpoint = changeTagName(firstSubelement, SUBPOINT, false);
                originalItem.replaceChild(firstSubpoint, firstSubelement);
                originalItem = changeTagName(originalItem, TableOfContentProcessor.getTagValueFromTocItemVo(item), true);
                updateNumTag(originalItem, num);
                break;
            case OTHER_SUBPOINT:
            case OTHER_SUBPARAGRAPH:
                firstSubpoint = changeTagName(originalItem, SUBPOINT, false);
                originalItem.appendChild(firstSubpoint);
                originalItem = changeTagName(originalItem, TableOfContentProcessor.getTagValueFromTocItemVo(item), true);
                createNumTag(originalItem, num);
                break;
            case PARAGRAPH:
            case POINT:
                firstSubpoint = changeTagName(originalItem, SUBPOINT, false);
                originalItem.appendChild(firstSubpoint);
                originalItem = changeTagName(originalItem, TableOfContentProcessor.getTagValueFromTocItemVo(item), true);
                updateNumTag(originalItem, num);
                break;
        }
        copyAttributesAndSetId(item, originalItem);
        item.setNode(originalItem);
        copyAttributesAndSetId(subelement, firstSubpoint);
        subelement.setNode(firstSubpoint);
    }

    void convertToSubparagraph(TableOfContentItemVO item, IndentedItemType beforeIndentItemType) {
        Node originalItem = item.getNode();
        List<Node> children = getChildren(originalItem);

        switch (beforeIndentItemType) {
            case FIRST_SUBPOINT:
            case FIRST_SUBPARAGRAPH:
                originalItem = changeTagName(originalItem, SUBPARAGRAPH, false);
                break;
            case POINT:
            case PARAGRAPH:
            case OTHER_SUBPOINT:
                if (!children.isEmpty()) {
                    return;
                }
                originalItem = changeTagName(originalItem, SUBPARAGRAPH, false);
                break;
        }
        copyAttributesAndSetId(item, originalItem);
        item.setNode(originalItem);
    }

    void convertToParagraph(TableOfContentItemVO item, IndentedItemType beforeIndentItemType, String num) {
        Node originalItem = item.getNode();
        List<Node> children = getChildren(originalItem);

        switch (beforeIndentItemType) {
            case FIRST_SUBPOINT:
            case FIRST_SUBPARAGRAPH:
                Node firstSubelement;
                if (children.isEmpty()) {
                    return;
                } else {
                    firstSubelement = children.get(0);
                }

                Node content = XercesUtils.getFirstChild(firstSubelement, CONTENT);
                moveToParent(content, false);
                originalItem = changeTagName(originalItem, PARAGRAPH, false);
                updateNumTag(originalItem, num);
                break;
            case OTHER_SUBPOINT:
            case OTHER_SUBPARAGRAPH:
                if (!children.isEmpty()) {
                    return;
                }
                createNumTag(originalItem, num);
                originalItem = changeTagName(originalItem, PARAGRAPH, false);
                break;
            case POINT:
                if (!children.isEmpty()) {
                    return;
                }
                updateNumTag(originalItem, num);
                originalItem = changeTagName(originalItem, PARAGRAPH, false);
                break;
        }
        copyAttributesAndSetId(item, originalItem);
        item.setNode(originalItem);
    }

    void convertToFirstSubparagraph(TableOfContentItemVO item, TableOfContentItemVO subelement, IndentedItemType beforeIndentItemType, String num) {
        Node originalItem = item.getNode();
        List<Node> children = getChildren(originalItem);
        Node firstSubpoint = null;

        switch (beforeIndentItemType) {
            case FIRST_SUBPOINT:
                Node firstSubelement;
                if (children.isEmpty()) {
                    return;
                } else {
                    firstSubelement = children.get(0);
                }

                firstSubpoint = changeTagName(firstSubelement, SUBPARAGRAPH, false);
                originalItem.replaceChild(firstSubpoint, firstSubelement);
                originalItem = changeTagName(originalItem, PARAGRAPH, true);
                updateNumTag(originalItem, num);
                break;
            case OTHER_SUBPOINT:
            case OTHER_SUBPARAGRAPH:
                firstSubpoint = changeTagName(originalItem, SUBPARAGRAPH, false);
                originalItem.appendChild(firstSubpoint);
                originalItem = changeTagName(originalItem, PARAGRAPH, true);
                createNumTag(originalItem, num);
                break;
            case PARAGRAPH:
            case POINT:
                firstSubpoint = changeTagName(originalItem, SUBPARAGRAPH, false);
                originalItem.appendChild(firstSubpoint);
                originalItem = changeTagName(originalItem, PARAGRAPH, true);
                updateNumTag(originalItem, num);
                break;
        }
        copyAttributesAndSetId(item, originalItem);
        item.setNode(originalItem);
        copyAttributesAndSetId(subelement, firstSubpoint);
        subelement.setNode(firstSubpoint);
    }

    public int getIndentedItemIndentLevel(Node node) {
        return getItemIndentLevel(node, 0);
    }

    int getItemIndentLevel(Node node, int startingDepth) {
        Node parent = node.getParentNode();
        while (parent != null) {
            if (ArrayUtils.contains(NUMBERED_AND_LEVEL_ITEMS, parent.getNodeName().toLowerCase())) {
                startingDepth++;
            }
            parent = parent.getParentNode();
        }

        return startingDepth;
    }

    public List<Node> getChildren(Node node) {
        List<String> elementNames = new ArrayList<String>();
        elementNames.addAll(Arrays.asList(UNUMBERED_ITEMS));
        elementNames.add(LIST);
        return XercesUtils.getChildren(node, elementNames);
    }

    public void moveToParent(Node node, boolean copy) {
        Node parent = node.getParentNode();
        Node next = parent.getNextSibling();
        if (next != null) {
            parent.getParentNode().insertBefore(node, next);
        } else {
            parent.getParentNode().appendChild(node);
        }
        if (copy) {
            copyAttributes(node, parent, XercesUtils.getId(node), XercesUtils.getAttributeValue(node, LEOS_ORIGIN_ATTR));
        }
        parent.getParentNode().removeChild(parent);
    }

    public void resetSoftActionAttributes(Node node) {
        XercesUtils.removeAttribute(node, LEOS_SOFT_ACTION_ATTR);
        XercesUtils.removeAttribute(node, LEOS_SOFT_TRANS_FROM);
        XercesUtils.removeAttribute(node, LEOS_SOFT_MOVE_FROM);
        XercesUtils.removeAttribute(node, LEOS_SOFT_MOVE_TO);
        XercesUtils.removeAttribute(node, LEOS_SOFT_USER_ATTR);
        XercesUtils.removeAttribute(node, LEOS_SOFT_DATE_ATTR);
        XercesUtils.removeAttribute(node, LEOS_SOFT_ACTION_ROOT_ATTR);
    }

    public void resetIndentAttributes(Node node) {
        XercesUtils.removeAttribute(node, LEOS_INDENT_ORIGIN_INDENT_LEVEL_ATTR);
        XercesUtils.removeAttribute(node, LEOS_INDENT_ORIGIN_TYPE_ATTR);
        XercesUtils.removeAttribute(node, LEOS_INDENT_ORIGIN_NUM_ATTR);
        XercesUtils.removeAttribute(node, LEOS_INDENT_ORIGIN_NUM_ID_ATTR);
        XercesUtils.removeAttribute(node, LEOS_INDENT_ORIGIN_NUM_ORIGIN_ATTR);
        XercesUtils.removeAttribute(node, LEOS_INDENT_UNUMBERED_PARAGRAPH);
    }

    private Node changeTagName(Node oldNode, String newTagName, boolean hasList) {
        Node newNode = XercesUtils.createElement(oldNode.getOwnerDocument(), newTagName, EMPTY_STRING);
        copyAttributes(newNode, oldNode, XercesUtils.getId(oldNode), XercesUtils.getAttributeValue(oldNode, LEOS_ORIGIN_ATTR));
        copyContent(newNode, oldNode, hasList);
        return newNode;
    }

    private void copyAttributes(Node node, Node oldNode, String id, String origin) {
        Map<String, String> attributes = XercesUtils.getAttributes(oldNode);
        for (Map.Entry<String, String> attr : attributes.entrySet()) {
            if (!attr.getKey().equals(CLASS_ATTR)) {
                XercesUtils.insertOrUpdateAttributeValue(node, attr.getKey(), attr.getValue());
            }
        }
        XercesUtils.setId(node, id);
        XercesUtils.insertOrUpdateAttributeValue(node, LEOS_ORIGIN_ATTR, origin);
    }

    private void copyAttributesAndSetId(TableOfContentItemVO item, Node node) {
        if (item.isIndented()) {
            XercesUtils.insertOrUpdateAttributeValue(node, LEOS_INDENT_ORIGIN_INDENT_LEVEL_ATTR, String.valueOf(item.getIndentOriginIndentLevel()));
            XercesUtils.insertOrUpdateAttributeValue(node, LEOS_INDENT_ORIGIN_TYPE_ATTR, item.getIndentOriginType().name());
            XercesUtils.insertOrUpdateAttributeValue(node, LEOS_INDENT_ORIGIN_NUM_ATTR, item.getIndentOriginNumValue());
            XercesUtils.insertOrUpdateAttributeValue(node, LEOS_INDENT_ORIGIN_NUM_ID_ATTR, item.getIndentOriginNumId());
            XercesUtils.insertOrUpdateAttributeValue(node, LEOS_INDENT_ORIGIN_NUM_ORIGIN_ATTR, item.getIndentOriginNumOrigin());
            if (item.getTocItem().getAknTag().name().equalsIgnoreCase(PARAGRAPH) && StringUtils.isEmpty(item.getNumber())) {
                XercesUtils.insertOrUpdateAttributeValue(node, LEOS_INDENT_UNUMBERED_PARAGRAPH, "true");
            }
        } else {
            resetIndentAttributes(node);
        }
        if (item.getSoftActionAttr() != null) {
            XercesUtils.insertOrUpdateAttributeValue(node, LEOS_SOFT_ACTION_ATTR, item.getSoftActionAttr().getSoftAction());
        } else {
            resetSoftActionAttributes(node);
        }
        if (item.getSoftTransFrom() != null) {
            XercesUtils.insertOrUpdateAttributeValue(node, LEOS_SOFT_TRANS_FROM, item.getSoftTransFrom());
        } else {
            XercesUtils.removeAttribute(node, LEOS_SOFT_TRANS_FROM);
        }
        XercesUtils.setId(node, item.getId());
        XercesUtils.insertOrUpdateAttributeValue(node, LEOS_ORIGIN_ATTR, item.getOriginAttr());
    }

    private void copyContent(Node node, Node oldNode, boolean hasList) {
        Node num = XercesUtils.getFirstChild(oldNode, NUM);
        if (num != null && Arrays.asList(NUMBERED_ITEMS).contains(node.getNodeName())) {
            node.appendChild(num);
        }
        Node content = XercesUtils.getFirstChild(oldNode, CONTENT);
        if (content != null) {
            node.appendChild(content);
        }
        if ((node.getNodeName().equalsIgnoreCase(POINT) || node.getNodeName().equalsIgnoreCase(INDENT)) && hasList) {
            Node subpoint = XercesUtils.getFirstChild(oldNode, SUBPOINT);
            if (subpoint != null) {
                node.appendChild(subpoint);
            }
        }
        if (node.getNodeName().equalsIgnoreCase(PARAGRAPH) && hasList) {
            Node subparagraph = XercesUtils.getFirstChild(oldNode, SUBPARAGRAPH);
            if (subparagraph != null) {
                node.appendChild(subparagraph);
            }
        }
    }

    private boolean hasOrigin(Node node, String origin) {
        String attrOrigin = XercesUtils.getAttributeValue(node, LEOS_ORIGIN_ATTR);
        if (origin.equals(CN)) {
            return attrOrigin == null || origin.equalsIgnoreCase(attrOrigin);
        } else {
            return attrOrigin.equalsIgnoreCase(EC);
        }
    }

    private void createNumTag(Node node, String num) {
        Node numNode = XercesUtils.createElement(node.getOwnerDocument(), NUM, num);
        String originalNumId = XercesUtils.getAttributeValue(node, LEOS_INDENT_ORIGIN_NUM_ID_ATTR);
        if (originalNumId != null) {
            XercesUtils.setId(numNode, originalNumId);
        }
        String originalNum = XercesUtils.getAttributeValue(node, LEOS_INDENT_ORIGIN_NUM_ATTR);
        if (originalNum != null && originalNum.equals(num) && hasOrigin(node, EC)) {
            XercesUtils.insertOrUpdateAttributeValue(node, LEOS_ORIGIN_ATTR, EC);
        } else {
            XercesUtils.insertOrUpdateAttributeValue(node, LEOS_ORIGIN_ATTR, CN);
        }
        Node firstChild = XercesUtils.getFirstChild(node);
        node.insertBefore(numNode, firstChild);
    }

    private void updateNumTag(Node node, String num) {
        Node numNode = XercesUtils.getFirstChild(node, NUM);
        if (numNode != null) {
            if (!numNode.getTextContent().equals(num)) {
                XercesUtils.insertOrUpdateAttributeValue(numNode, LEOS_ORIGIN_ATTR, CN);
            } else {
                XercesUtils.insertOrUpdateAttributeValue(numNode, LEOS_ORIGIN_ATTR, EC);
            }
            numNode.setTextContent(num);
        } else {
            createNumTag(node, num);
        }
    }

    private void resetNum(Node node) {
        Node numNode = XercesUtils.getFirstChild(node, NUM);
        if (numNode != null) {
            String originalNumId = XercesUtils.getAttributeValue(node, LEOS_INDENT_ORIGIN_NUM_ID_ATTR);
            if (originalNumId != null) {
                XercesUtils.setId(numNode, originalNumId);
            }
            String originalNumValue = XercesUtils.getAttributeValue(node, LEOS_INDENT_ORIGIN_NUM_ATTR);
            if (originalNumValue != null) {
                numNode.setTextContent(originalNumValue);
            }
            String originalNumOrigin = XercesUtils.getAttributeValue(node, LEOS_INDENT_ORIGIN_NUM_ORIGIN_ATTR);
            if (originalNumOrigin != null) {
                XercesUtils.insertOrUpdateAttributeValue(node, LEOS_ORIGIN_ATTR, originalNumOrigin);
            }
        }
    }

    public boolean containsInlineElement(TableOfContentItemVO item) {
        if (item.getNode() == null) {
            return false;
        }
        Node node = item.getNode();
        List<Node> children = XercesUtils.getChildren(node, INLINE_ELEMENTS);
        for (Node child : children) {
            if (!isCrossheadingNum(child)) {
                return true;
            }
        }
        return false;
    }

    public void replaceContentFromTocItem(TableOfContentItemVO tocItem, String updatedContent) {
        Node node = tocItem.getNode();
        if (node != null) {
            NodeList children = node.getChildNodes();
            Node textNode = node.getOwnerDocument().createTextNode(updatedContent);
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.TEXT_NODE) {
                    node.removeChild(child);
                }
            }
            node.appendChild(textNode);
        } else {
            tocItem.setContent(updatedContent);
        }
    }

    public void setContentInNodeFromTocItem(TableOfContentItemVO tocItem, Node node) {
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeType() == Node.TEXT_NODE) {
                childNode.setTextContent(tocItem.getContent());
            }
        }
    }
}
